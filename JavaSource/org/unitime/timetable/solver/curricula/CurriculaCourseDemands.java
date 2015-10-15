/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.solver.curricula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.IdGenerator;
import org.cpsolver.ifs.util.Progress;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.curricula.students.CurCourse;
import org.unitime.timetable.solver.curricula.students.CurModel;
import org.unitime.timetable.solver.curricula.students.CurStudent;
import org.unitime.timetable.solver.curricula.students.CurValue;
import org.unitime.timetable.solver.curricula.students.CurVariable;


/**
 * @author Tomas Muller
 */
public class CurriculaCourseDemands implements StudentCourseDemands {
	private static Log sLog = LogFactory.getLog(CurriculaCourseDemands.class);
	private Hashtable<Long, Set<WeightedStudentId>> iDemands = new Hashtable<Long, Set<WeightedStudentId>>();
	private Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
	private Hashtable<Long, Hashtable<Long, Double>> iEnrollmentPriorities = new Hashtable<Long, Hashtable<Long, Double>>();
	private IdGenerator lastStudentId = new IdGenerator();
	protected ProjectedStudentCourseDemands iFallback;
	private Hashtable<Long, Hashtable<String, Set<String>>> iLoadedCurricula = new Hashtable<Long,Hashtable<String, Set<String>>>();
	private HashSet<Long> iCheckedCourses = new HashSet<Long>();
	private boolean iIncludeOtherStudents = true;
	private boolean iSetStudentCourseLimits = false;
	private CurriculumEnrollmentPriorityProvider iEnrollmentPriorityProvider = null;
	private DataProperties iProperties = null;

	public CurriculaCourseDemands(DataProperties properties) {
		iProperties = properties;
		if (properties != null)
			iFallback = new ProjectedStudentCourseDemands(properties);
		iIncludeOtherStudents = properties.getPropertyBoolean("CurriculaCourseDemands.IncludeOtherStudents", iIncludeOtherStudents);
		iSetStudentCourseLimits = properties.getPropertyBoolean("CurriculaCourseDemands.SetStudentCourseLimits", iSetStudentCourseLimits);
		iEnrollmentPriorityProvider = new DefaultCurriculumEnrollmentPriorityProvider(properties);
		if (properties.getProperty("CurriculaCourseDemands.CurriculumEnrollmentPriorityProvider") != null) {
			try {
				iEnrollmentPriorityProvider = (CurriculumEnrollmentPriorityProvider)Class.forName(
						properties.getProperty("CurriculaCourseDemands.CurriculumEnrollmentPriorityProvider"))
						.getConstructor(DataProperties.class).newInstance(properties);
			} catch (Exception e) {
				sLog.error("Failed to use custom enrollment priority provider: " + e.getMessage(), e);
			}
		}
	}
	
	public CurriculaCourseDemands() {
		this(null);
	}
	
	public boolean isMakingUpStudents() { return true; }
	
	public boolean canUseStudentClassEnrollmentsAsSolution() { return false; }
	
	public boolean isWeightStudentsToFillUpOffering() { return false; }

	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		iFallback.init(hibSession, progress, session, offerings);
		
		List<Curriculum> curricula = null;
		if (offerings != null && offerings.size() <= 1000) {
			String courses = "";
			int nrCourses = 0;
			for (InstructionalOffering offering: offerings)
				for (CourseOffering course: offering.getCourseOfferings()) {
					if (!courses.isEmpty()) courses += ",";
					courses += course.getUniqueId();
					nrCourses++;
				}
			if (nrCourses > 0 && nrCourses <= 1000) {
				Set<Curriculum> curriculaSet = new HashSet<Curriculum>(hibSession.createQuery(
						"select distinct c from CurriculumCourse cc inner join cc.classification.curriculum c where " +
						"c.academicArea.session.uniqueId = :sessionId and cc.course.uniqueId in (" + courses + ")")
						.setLong("sessionId", session.getUniqueId()).list());
				// include children curricula
				curriculaSet.addAll(
						hibSession.createQuery(
							"select distinct d from CurriculumCourse cc inner join cc.classification.curriculum c, Curriculum d " +
							"where c.academicArea = d.academicArea and d.multipleMajors = true and size(c.majors) <= 1 and size(c.majors) < size(d.majors) and " +
							"(select count(m) from Curriculum x inner join x.majors m where x.uniqueId = c.uniqueId and m not in elements(d.majors)) = 0 and " +
							"c.academicArea.session.uniqueId = :sessionId and cc.course.uniqueId in (" + courses + ")")
							.setLong("sessionId", session.getUniqueId()).list()
						);
				// include parent curricula
				curriculaSet.addAll(
						hibSession.createQuery(
							"select distinct d from CurriculumCourse cc inner join cc.classification.curriculum c, Curriculum d " +
							"where c.multipleMajors = true and size(c.majors) >= 1 and size(c.majors) > size(d.majors) and c.academicArea = d.academicArea and " +
							"(select count(m) from Curriculum x inner join x.majors m where x.uniqueId = d.uniqueId and m not in elements(c.majors)) = 0 and " +
							"c.academicArea.session.uniqueId = :sessionId and cc.course.uniqueId in (" + courses + ")")
							.setLong("sessionId", session.getUniqueId()).list());
				curricula = new ArrayList<Curriculum>(curriculaSet);
			}
		}
		
		if (curricula == null) {
			curricula = hibSession.createQuery(
					"select c from Curriculum c where c.academicArea.session.uniqueId = :sessionId")
					.setLong("sessionId", session.getUniqueId()).list();
		}

		List<Initialization> inits = new ArrayList<Initialization>();
		for (Curriculum curriculum: curricula) {
			for (CurriculumClassification clasf: curriculum.getClassifications()) {
				if (clasf.getNrStudents() > 0) {
					List<CurriculumClassification> templates = new ArrayList<CurriculumClassification>();
					if (curriculum.isMultipleMajors())
						for (Curriculum parent: curricula)
							if (parent.isTemplateFor(curriculum)) {
								for (CurriculumClassification parentClasf: parent.getClassifications()) {
									if (parentClasf.getAcademicClassification().equals(clasf.getAcademicClassification()))
										templates.add(parentClasf);
								}
							}
					inits.add(new Initialization(clasf, templates));
				}
			}
		}
		new ParallelInitialization("Loading curricula",
				iProperties.getPropertyInt("CurriculaCourseDemands.NrThreads", 1),
				inits).execute(hibSession, progress);
		
		if (iDemands.isEmpty()) {
			progress.warn("There are no curricula, using projected course demands instead.");
		}
	}
	
	protected String getCacheName() {
		return "curriculum-demands";
	}
	
	protected void computeTargetShare(int nrStudents, Collection<CurriculumCourse> courses, CurriculumCourseGroupsProvider course2groups, CurModel model) {
		for (CurriculumCourse c1: courses) {
			float x1 = c1.getPercShare() * nrStudents;
			Set<CurriculumCourse>[] group = new HashSet[] { new HashSet<CurriculumCourse>(), new HashSet<CurriculumCourse>()};
			Queue<CurriculumCourse> queue = new LinkedList<CurriculumCourse>();
			queue.add(c1);
			Set<CurriculumCourseGroup> done = new HashSet<CurriculumCourseGroup>();
			while (!queue.isEmpty()) {
				CurriculumCourse c = queue.poll();
				for (CurriculumCourseGroup g: course2groups.getGroups(c))
					if (done.add(g))
						for (CurriculumCourse x: courses)
							if (!x.equals(c) && !x.equals(c1) && course2groups.getGroups(x).contains(g) && group[group[0].contains(c) ? 0 : g.getType()].add(x))
								queue.add(x);
			}
			for (CurriculumCourse c2: courses) {
				float x2 = c2.getPercShare() * nrStudents;
				if (c1.getUniqueId() >= c2.getUniqueId()) continue;
				float share = c1.getPercShare() * c2.getPercShare() * nrStudents;
				boolean opt = group[0].contains(c2);
				boolean req = !opt && group[1].contains(c2);
				model.setTargetShare(c1.getUniqueId(), c2.getUniqueId(), opt ? 0.0 : req ? Math.min(x1, x2) : share, true);
			}
		}
	}
	
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		if (iDemands.isEmpty()) return iFallback.getDemands(course);
		Set<WeightedStudentId> demands = iDemands.get(course.getUniqueId());
		if (!iIncludeOtherStudents) return demands;
		if (demands == null) {
			demands = new HashSet<WeightedStudentId>();
			iDemands.put(course.getUniqueId(), demands);
		}
		if (iCheckedCourses.add(course.getUniqueId())) {
			int was = demands.size();
			Hashtable<String,Set<String>> curricula = iLoadedCurricula.get(course.getUniqueId());
			Set<WeightedStudentId> other = iFallback.getDemands(course);
			if (curricula == null || curricula.isEmpty()) {
				demands.addAll(other);
			} else {
				other: for (WeightedStudentId student: other) {
					// if (student.getAreas().isEmpty()) continue; // ignore students w/o academic area
					for (AreaCode area: student.getAreas()) {
						Set<String> majors = curricula.get(area.getArea());
						if (majors != null && (majors.contains("") || student.match(area.getArea(), majors))) continue other; // all majors or match
					}
					demands.add(student);
				}
			}
			if (demands.size() > was)
				sLog.info(course.getCourseName() + " has " + (demands.size() - was) + " other students (besides of the " + was + " curriculum students).");
		}
		return demands;
	}
	
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		if (iIncludeOtherStudents && studentId >= 0) return iFallback.getCourses(studentId);
		return iStudentRequests.get(studentId);
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		Hashtable<Long, Double> priorities = iEnrollmentPriorities.get(studentId);
		return (priorities == null ? null : priorities.get(courseId));
	}
	
	public class Initialization implements ParallelInitialization.Task {
		private CurriculumClassification iClassification;
		private List<CurriculumClassification> iTemplates;
		private boolean iUpdateClassification = false;
		private CurModel iModel;
		private Hashtable<Long, CourseOffering> iCourses;
		private Assignment<CurVariable, CurValue> iAssignment;
		
		public Initialization(CurriculumClassification classification, List<CurriculumClassification> templates) {
			iClassification = classification;
			iTemplates = templates;
		}
		
		@Override
		public void setup(org.hibernate.Session hibSession) {
			sLog.debug("Processing " + iClassification.getCurriculum().getAbbv() + " " + iClassification.getName() + " ... (" + iClassification.getNrStudents() + " students, " + iClassification.getCourses().size() + " courses)");
			
			// Create model
			List<CurStudent> students = new ArrayList<CurStudent>();
			for (long i = 0; i < iClassification.getNrStudents(); i++)
				students.add(new CurStudent(- (1 + i), 1f));
			iModel = new CurModel(students);
			iCourses = new Hashtable<Long, CourseOffering>();
			
			Collection<CurriculumCourse> courses = null;
			CurriculumCourseGroupsProvider course2groups = null;
			if (iTemplates == null || iTemplates.isEmpty()) {
				courses = iClassification.getCourses();
				course2groups = new DefaultCurriculumCourseGroupsProvider();
			} else {
				Map<Long, CurriculumCourse> curriculumCourses = new HashMap<Long, CurriculumCourse>();
				course2groups = new TableCurriculumCourseGroupsProvider();
				// Populate with templates (if a course is present two or more times, maximize percent share
				for (CurriculumClassification template: iTemplates) {
					for (CurriculumCourse course: template.getCourses()) {
						CurriculumCourse prev = curriculumCourses.get(course.getCourse().getUniqueId());
						if (prev == null || prev.getPercShare() < course.getPercShare())
							curriculumCourses.put(course.getCourse().getUniqueId(), course);
						((TableCurriculumCourseGroupsProvider)course2groups).add(course);
					}
				}
				// Override with courses on the curriculum
				for (CurriculumCourse course: iClassification.getCourses()) {
					curriculumCourses.put(course.getCourse().getUniqueId(), course);
					((TableCurriculumCourseGroupsProvider)course2groups).add(course);
				}
				courses = curriculumCourses.values();
			}
			
			for (CurriculumCourse course: courses) {
				iModel.addCourse(course.getUniqueId(), course.getCourse().getCourseName(), course.getPercShare() * iClassification.getNrStudents(), iEnrollmentPriorityProvider.getEnrollmentPriority(course, course2groups));
				iCourses.put(course.getUniqueId(), course.getCourse());
				
				Hashtable<String,Set<String>> curricula = iLoadedCurricula.get(course.getCourse().getUniqueId());
				if (curricula == null) {
					curricula = new Hashtable<String, Set<String>>();
					iLoadedCurricula.put(course.getCourse().getUniqueId(), curricula);
				}
				Set<String> majors = curricula.get(iClassification.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation());
				if (majors == null) {
					majors = new HashSet<String>();
					curricula.put(iClassification.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation(), majors);
				}
				if (iClassification.getCurriculum().getMajors().isEmpty()) {
					majors.add("");
				} else {
					for (PosMajor mj: iClassification.getCurriculum().getMajors())
						majors.add(mj.getCode());
				}

			}
			computeTargetShare(iClassification.getNrStudents(), courses, course2groups, iModel);
			if (iSetStudentCourseLimits)
				iModel.setStudentLimits();
			
			// Load model from cache (if exists)
			Solution<CurVariable, CurValue> cachedSolution = null;
			iAssignment = new DefaultSingleAssignment<CurVariable, CurValue>();
			Document cachedXml = iClassification.getStudentsDocument();
			Element cache = (cachedXml == null ? null : cachedXml.getRootElement());
			if (cache != null && cache.getName().equals(getCacheName())) {
				cachedSolution = CurModel.loadFromXml(cache);
				if (iSetStudentCourseLimits)
					((CurModel)cachedSolution.getModel()).setStudentLimits();
			}

			// Check the cached model
			if (cachedSolution != null && ((CurModel)cachedSolution.getModel()).isSameModel(iModel)) {
				// Reuse
				sLog.debug("  using cached model...");
				iModel = ((CurModel)cachedSolution.getModel());
				iAssignment = cachedSolution.getAssignment();
			} else {
				iUpdateClassification = true;
			}			
		}
		
		@Override
		public void execute() {
			if (iUpdateClassification) {
				// Solve model
				iModel.solve(iProperties, iAssignment);
			}
		}
		
		@Override
		public void teardown(org.hibernate.Session hibSession) {
			if (iUpdateClassification) {
				// Save into the cache
				Document doc = DocumentHelper.createDocument();
				iModel.saveAsXml(doc.addElement(getCacheName()), iAssignment);
				// sLog.debug("Model:\n" + doc.asXML());
				iClassification.setStudentsDocument(doc);

				hibSession.update(iClassification);
			}

			// Save results
			String majors = "";
			for (PosMajor major: iClassification.getCurriculum().getMajors()) {
				if (!majors.isEmpty()) majors += ",";
				majors += major.getCode();
			}
			for (CurStudent s: iModel.getStudents()) {
				WeightedStudentId student = new WeightedStudentId(- lastStudentId.newId(), iClassification);
				Set<WeightedCourseOffering> studentCourses = new HashSet<WeightedCourseOffering>();
				iStudentRequests.put(student.getStudentId(), studentCourses);
				Hashtable<Long, Double> priorities = new Hashtable<Long, Double>(); iEnrollmentPriorities.put(student.getStudentId(), priorities);
				for (CurCourse course: s.getCourses(iAssignment)) {
					CourseOffering co = iCourses.get(course.getCourseId());
					if (course.getPriority() != null) priorities.put(co.getUniqueId(), course.getPriority());
					Set<WeightedStudentId> courseStudents = iDemands.get(co.getUniqueId());
					if (courseStudents == null) {
						courseStudents = new HashSet<WeightedStudentId>();
						iDemands.put(co.getUniqueId(), courseStudents);
					}
					courseStudents.add(student);
					studentCourses.add(new WeightedCourseOffering(co, student.getWeight()));
				}
			}
		}
	}
	
	public static interface CurriculumCourseGroupsProvider {
		public Set<CurriculumCourseGroup> getGroups(CurriculumCourse course);
	}
	
	public static class DefaultCurriculumCourseGroupsProvider implements CurriculumCourseGroupsProvider {
		@Override
		public Set<CurriculumCourseGroup> getGroups(CurriculumCourse course) {
			return course.getGroups();
		}
	}
	
	public static class TableCurriculumCourseGroupsProvider implements CurriculumCourseGroupsProvider {
		private Map<Long, Set<CurriculumCourseGroup>> iTable = new HashMap<Long, Set<CurriculumCourseGroup>>();
		
		public TableCurriculumCourseGroupsProvider() {
		}
		
		public void add(CurriculumCourse course) {
			Set<CurriculumCourseGroup> groups = iTable.get(course.getCourse().getUniqueId());
			if (groups == null) {
				groups = new HashSet<CurriculumCourseGroup>();
				iTable.put(course.getCourse().getUniqueId(), groups);
			}
			groups.addAll(course.getGroups());
		}

		@Override
		public Set<CurriculumCourseGroup> getGroups(CurriculumCourse course) {
			Set<CurriculumCourseGroup> groups = iTable.get(course.getCourse().getUniqueId());
			return (groups == null ? course.getGroups() : groups);
		}
		
	}
}
