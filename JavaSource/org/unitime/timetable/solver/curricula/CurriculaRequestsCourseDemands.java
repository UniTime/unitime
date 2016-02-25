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
import org.cpsolver.ifs.model.Constraint;
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
import org.unitime.timetable.model.Student;
import org.unitime.timetable.solver.curricula.CurriculaCourseDemands.CurriculumCourseGroupsProvider;
import org.unitime.timetable.solver.curricula.CurriculaCourseDemands.DefaultCurriculumCourseGroupsProvider;
import org.unitime.timetable.solver.curricula.CurriculaCourseDemands.TableCurriculumCourseGroupsProvider;
import org.unitime.timetable.solver.curricula.students.CurCourse;
import org.unitime.timetable.solver.curricula.students.CurModel;
import org.unitime.timetable.solver.curricula.students.CurStudent;
import org.unitime.timetable.solver.curricula.students.CurValue;
import org.unitime.timetable.solver.curricula.students.CurVariable;

/**
 * Combining curricula with course requests. 
 * @author Tomas Muller
 */
public class CurriculaRequestsCourseDemands implements StudentCourseDemands {
	private static Log sLog = LogFactory.getLog(CurriculaRequestsCourseDemands.class);

	private StudentCourseRequests iStudentCourseRequests;
	
	private IdGenerator iLastStudentId = new IdGenerator();
	private Hashtable<Long, Set<WeightedStudentId>> iDemands = new Hashtable<Long, Set<WeightedStudentId>>();
	private Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
	private Hashtable<String, Set<String>> iLoadedCurricula = new Hashtable<String, Set<String>>();
	private Hashtable<Long, Hashtable<Long, Double>> iEnrollmentPriorities = new Hashtable<Long, Hashtable<Long, Double>>();
	private HashSet<Long> iCheckedCourses = new HashSet<Long>();
	private boolean iIncludeOtherStudents = true;
	private boolean iIncludeOtherCourses = true;
	private boolean iSetStudentCourseLimits = false;
	private CurriculumEnrollmentPriorityProvider iEnrollmentPriorityProvider = null;
	private DataProperties iProperties = null;
	
	public CurriculaRequestsCourseDemands(DataProperties config) {
		iProperties = config;
		iStudentCourseRequests = new StudentCourseRequests(config);
		iIncludeOtherStudents = config.getPropertyBoolean("CurriculaCourseDemands.IncludeOtherStudents", iIncludeOtherStudents);
		iIncludeOtherCourses = config.getPropertyBoolean("CurriculaCourseDemands.IncludeOtherCourses", config.getPropertyBoolean("CurriculaCourseDemands.IncludeOtherStudents", iIncludeOtherCourses));
		iSetStudentCourseLimits = config.getPropertyBoolean("CurriculaCourseDemands.SetStudentCourseLimits", iSetStudentCourseLimits);
		iEnrollmentPriorityProvider = new DefaultCurriculumEnrollmentPriorityProvider(config);
		if (config.getProperty("CurriculaCourseDemands.CurriculumEnrollmentPriorityProvider") != null) {
			try {
				iEnrollmentPriorityProvider = (CurriculumEnrollmentPriorityProvider)Class.forName(
						config.getProperty("CurriculaCourseDemands.CurriculumEnrollmentPriorityProvider"))
						.getConstructor(DataProperties.class).newInstance(config);
			} catch (Exception e) {
				sLog.error("Failed to use custom enrollment priority provider: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {

		iStudentCourseRequests.init(hibSession, progress, session, offerings);
		
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
					inits.add(new Initialization(clasf, templates, loadCourseRegistrations(hibSession, clasf)));
				}
			}
		}
		new ParallelInitialization(
				"Loading curricula",
				iProperties.getPropertyInt("CurriculaCourseDemands.NrThreads", 1),
				inits).execute(hibSession, progress);		
	}
	
	private Map<CourseOffering, Set<WeightedStudentId>> loadCourseRegistrations(org.hibernate.Session hibSession, CurriculumClassification cc) {
		List<Object[]> lines = null;
		String select = "distinct co, s";
		String from = "CourseRequest r inner join r.courseOffering co inner join r.courseDemand.student s inner join s.academicAreaClassifications a";
		String where = "s.session.uniqueId = :sessionId and a.academicArea.uniqueId = :acadAreaId and a.academicClassification.uniqueId = :clasfId";
		if (cc.getCurriculum().getMajors().isEmpty()) {
			// students with no major
			lines = hibSession.createQuery("select " + select + " from " + from + " where " + where + (cc.getCurriculum().isMultipleMajors() ? " and s.posMajors is empty" : ""))
					.setLong("sessionId", cc.getCurriculum().getAcademicArea().getSessionId())
					.setLong("acadAreaId", cc.getCurriculum().getAcademicArea().getUniqueId())
					.setLong("clasfId", cc.getAcademicClassification().getUniqueId())
					.setCacheable(true).list();
		} else if (!cc.getCurriculum().isMultipleMajors() || cc.getCurriculum().getMajors().size() == 1) {
			List<Long> majorIds = new ArrayList<Long>();
			for (PosMajor major: cc.getCurriculum().getMajors())
				majorIds.add(major.getUniqueId());
			// students with one major
			lines = hibSession.createQuery("select " + select + " from " + from + " inner join s.posMajors m where " + where + " and m.uniqueId in :majorIds")
					.setLong("sessionId", cc.getCurriculum().getAcademicArea().getSessionId())
					.setLong("acadAreaId", cc.getCurriculum().getAcademicArea().getUniqueId())
					.setLong("clasfId", cc.getAcademicClassification().getUniqueId())
					.setParameterList("majorIds", majorIds)
					.setCacheable(true).list();
		} else {
			// students with multiple majors
			Map<String, Long> params = new HashMap<String, Long>();
			int idx = 1;
			for (PosMajor major: cc.getCurriculum().getMajors()) {
				from += " inner join s.posMajors m" + idx;
				where += " and m" + idx + ".uniqueId = :m" + idx;
				params.put("m" + idx, major.getUniqueId());
				idx ++;
			}
			org.hibernate.Query q = hibSession.createQuery("select " + select + " from " + from + " where " + where)
					.setLong("sessionId", cc.getCurriculum().getAcademicArea().getSessionId())
					.setLong("acadAreaId", cc.getCurriculum().getAcademicArea().getUniqueId())
					.setLong("clasfId", cc.getAcademicClassification().getUniqueId());
			for (Map.Entry<String, Long> e: params.entrySet())
				q.setLong(e.getKey(), e.getValue());
			lines = q.setCacheable(true).list();
		}
		Map<CourseOffering, Set<WeightedStudentId>> course2req = new HashMap<CourseOffering,Set<WeightedStudentId>>();
		for (Object[] o : lines) {
			CourseOffering course = (CourseOffering)o[0];
			Student student = (Student)o[1];
			
			WeightedStudentId studentId = new WeightedStudentId(student);
			studentId.setCurriculum(cc.getCurriculum().getAbbv());

			Set<WeightedStudentId> students = course2req.get(course);
			if (students == null) {
				students = new HashSet<WeightedStudentId>();
				course2req.put(course, students);
			}
			students.add(studentId);
		}
		
		return course2req;
	}
	
	protected String getCacheName() {
		return "curriculum-lastlike-demands";
	}

	protected void computeTargetShare(CurriculumClassification clasf, Collection<CurriculumCourse> courses, CurriculumCourseGroupsProvider course2groups,int nrStudents, double factor, double w, CurModel model) {
		for (CurriculumCourse c1: courses) {
			double x1 = model.getCourse(c1.getCourse().getUniqueId()).getOriginalMaxSize();
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
				double x2 = model.getCourse(c2.getCourse().getUniqueId()).getOriginalMaxSize();
				boolean opt = group[0].contains(c2);
				boolean req = !opt && group[1].contains(c2);
				double defaultShare = (opt ? 0.0 : req ? Math.min(x1, x2) : c1.getPercShare() * c2.getPercShare() * nrStudents);
				if (c1.getUniqueId() >= c2.getUniqueId()) continue;
				double share = defaultShare;
				Set<WeightedStudentId> s1 = iStudentCourseRequests.getDemands(c1.getCourse());
				Set<WeightedStudentId> s2 = iStudentCourseRequests.getDemands(c2.getCourse());
				int sharedStudents = 0, registered = 0;
				if (s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty()) {
					for (WeightedStudentId s: s1) {
						if (s.match(clasf)) {
							registered ++;
							if (s2.contains(s)) sharedStudents ++;
						}
					}
				}
				if (registered == 0) {
					share = (1.0 - w) * defaultShare;
				} else {
					share = w * (x1 / registered) * sharedStudents + (1.0 - w) * defaultShare;
				}
				model.setTargetShare(c1.getCourse().getUniqueId(), c2.getCourse().getUniqueId(), share, false);
			}
		}
	}

	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		Set<WeightedCourseOffering> courses = iStudentRequests.get(studentId);
		if (iIncludeOtherStudents && studentId >= 0 && courses == null)
			return iStudentCourseRequests.getCourses(studentId);
		return iStudentRequests.get(studentId);
	}
	

	@Override
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		if (iDemands.isEmpty()) return iStudentCourseRequests.getDemands(course);
		Set<WeightedStudentId> demands = iDemands.get(course.getUniqueId());
		if (!iIncludeOtherStudents) return demands;
		if (demands == null) {
			demands = new HashSet<WeightedStudentId>();
			iDemands.put(course.getUniqueId(), demands);
		}
		if (iCheckedCourses.add(course.getUniqueId())) {
			int was = demands.size();
			Set<WeightedStudentId> other = iStudentCourseRequests.getDemands(course);
			if (other == null) {
				sLog.debug(course.getCourseName() + " has no students.");
			} else {
				if (iLoadedCurricula == null || iLoadedCurricula.isEmpty()) {
					demands.addAll(other);
				} else {
					other: for (WeightedStudentId student: other) {
						// if (student.getAreas().isEmpty()) continue; // ignore students w/o academic area
						for (AreaCode area: student.getAreas()) {
							Set<String> majors = iLoadedCurricula.get(area.getArea() + ":" + area.getCode());
							if (majors != null && (majors.contains("") || student.match(area.getArea(), majors))) continue other; // all majors or match
						}
						demands.add(student);
					}
				}
				if (demands.size() > was)
					sLog.debug(course.getCourseName() + " has " + (demands.size() - was) + " other students (besides of the " + was + " curriculum students).");
			}
		}
		return demands;
	}
	
	@Override
	public boolean canUseStudentClassEnrollmentsAsSolution() {
		return false;
	}

	@Override
	public boolean isMakingUpStudents() {
		return false; // most students should be last-like students
	}

	@Override
	public boolean isWeightStudentsToFillUpOffering() {
		return false;
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		if (studentId >= 0)
			return iStudentCourseRequests.getEnrollmentPriority(studentId, courseId);
		Hashtable<Long, Double> priorities = iEnrollmentPriorities.get(studentId);
		return (priorities == null ? null : priorities.get(courseId));
	}
	
	public class Initialization implements ParallelInitialization.Task {
		private CurriculumClassification iClassification;
		private List<CurriculumClassification> iTemplates;
		private Map<CourseOffering, Set<WeightedStudentId>> iCourseRequests;
		private boolean iUpdateClassification = false;
		private CurModel iModel;
		private Assignment<CurVariable, CurValue> iAssignment;
		private List<WeightedStudentId> iMadeUpStudents;
		private Hashtable<Long, WeightedStudentId> iStudentIds;
		private Hashtable<Long, CourseOffering> iCourses;
		private Hashtable<WeightedStudentId, Set<CourseOffering>> iStudents;
		
		public Initialization(CurriculumClassification clasf, List<CurriculumClassification> templates, Map<CourseOffering, Set<WeightedStudentId>> courseRequest) {
			iClassification = clasf;
			iCourseRequests = courseRequest;
			iTemplates = templates;
		}
		
		@Override
		public void setup(org.hibernate.Session hibSession) {
			sLog.debug("Processing " + iClassification.getCurriculum().getAbbv() + " " + iClassification.getName() + " ... (" + iClassification.getNrStudents() + " students, " + iClassification.getCourses().size() + " iCourses)");
			
			iStudents = new Hashtable<WeightedStudentId, Set<CourseOffering>>();
			if (iCourseRequests != null) {
				for (Map.Entry<CourseOffering, Set<WeightedStudentId>> entry: iCourseRequests.entrySet()) {
					for (WeightedStudentId student: entry.getValue()) {
						Set<CourseOffering> courses = iStudents.get(student);
						if (courses == null) {
							courses = new HashSet<CourseOffering>();
							iStudents.put(student, courses);
						}
						courses.add(entry.getKey());
					}
				}
			}
			
			float totalWeight = 0;
			for (WeightedStudentId student: iStudents.keySet())
				totalWeight += student.getWeight();
			
			sLog.debug("  registered students: " + totalWeight + ", target: " + iClassification.getNrStudents());
			int nrStudents = iClassification.getNrStudents();
			iMadeUpStudents = new ArrayList<StudentCourseDemands.WeightedStudentId>();
			double w = Math.min(totalWeight / iClassification.getNrStudents(), 1.0);
			float factor = 1.0f;
			if (2 * totalWeight < iClassification.getNrStudents()) { // students are less than 1/2 of the requested size -> make up some students
				int studentsToMakeUp = Math.round(iClassification.getNrStudents() - totalWeight);
				sLog.debug("    making up " + studentsToMakeUp + " students");
				String majors = "";
				for (PosMajor major: iClassification.getCurriculum().getMajors()) {
					if (!majors.isEmpty()) majors += "|";
					majors += major.getCode();
				}
				for (int i = 0; i < studentsToMakeUp; i++) {
					WeightedStudentId student = new WeightedStudentId(-iLastStudentId.newId(), iClassification);
					iStudents.put(student, new HashSet<CourseOffering>());
					iMadeUpStudents.add(student);
				}
			} else if (totalWeight < iClassification.getNrStudents()) { // change weights to fit the requested size
				factor = iClassification.getNrStudents() / totalWeight;
				w = 1.0;
				sLog.debug("    changing student weight " + factor + " times");
				for (WeightedStudentId student: iStudents.keySet())
					student.setWeight(student.getWeight() * factor);
			} else if (totalWeight > iClassification.getNrStudents()) {
				sLog.debug("    more registered students than needed, keeping all");
				nrStudents = Math.round(totalWeight);
			}
			
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
			
			// Setup model
			List<CurStudent> curStudents = new ArrayList<CurStudent>();
			iStudentIds = new Hashtable<Long, WeightedStudentId>();
			int idx = 0;
			for (WeightedStudentId student: iStudents.keySet()) {
				curStudents.add(new CurStudent(student.getStudentId() < 0 ? - (++idx) : student.getStudentId(), student.getWeight()));
				iStudentIds.put(student.getStudentId() < 0 ? - idx : student.getStudentId(), student);
			}
			iModel = new CurModel(curStudents);
			iCourses = new Hashtable<Long, CourseOffering>();
			for (CurriculumCourse course: courses) {
				Set<WeightedStudentId> requests = (iCourseRequests == null ? null : iCourseRequests.get(course.getCourse()));
				double size =
						w * factor * (requests == null ? 0 : requests.size()) +
						(1 - w) * nrStudents * course.getPercShare();
				/*
				if (factor > 1.0f)
					size = Math.max(nrStudents * course.getPercShare(), factor * (requests == null ? 0 : requests.size()));
				*/
				iModel.addCourse(course.getCourse().getUniqueId(), course.getCourse().getCourseName(), size, iEnrollmentPriorityProvider.getEnrollmentPriority(course, course2groups));
				iCourses.put(course.getCourse().getUniqueId(), course.getCourse());
			}
			computeTargetShare(iClassification, courses, course2groups, nrStudents, factor, w, iModel);
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
				// initial iAssignment
				for (CurStudent student: curStudents) { 
					for (CourseOffering course: iStudents.get(iStudentIds.get(student.getStudentId()))) {
						CurCourse curCourse = iModel.getCourse(course.getUniqueId());
						if (curCourse == null) continue;
						CurVariable var = null;
						for (CurVariable v: curCourse.variables())
							if (iAssignment.getValue(v) == null) { var = v; break; }
						if (var != null) {
							CurValue val = new CurValue(var, student);
							if (!iModel.inConflict(iAssignment, val))
								iAssignment.assign(0, val);
							else {
								sLog.debug("Unable to assign " + student + " to " + var);
								Map<Constraint<CurVariable, CurValue>, Set<CurValue>> conf = iModel.conflictConstraints(iAssignment, val);
								for (Map.Entry<Constraint<CurVariable, CurValue>, Set<CurValue>> entry: conf.entrySet()) {
									sLog.debug(entry.getKey() + ": " + entry.getValue());
								}
							}
						} else {
							sLog.debug("No variable for " + student + " to " + curCourse);
						}
					}
				}
				
				iUpdateClassification = true;
			}
		}
		
		public void execute() {
			if (iUpdateClassification) {
				// Solve model
				sLog.debug("Initial: " + iModel.getInfo(iAssignment));
				iModel.solve(iProperties, iAssignment);
				sLog.debug("Final: " + iModel.getInfo(iAssignment));
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
			int idx = 0;
			for (CurStudent s: iModel.getStudents()) {
				WeightedStudentId student = null;
				if (s.getStudentId() < 0) {
					student = iMadeUpStudents.get(idx++);
				} else {
					student = iStudentIds.get(s.getStudentId());
				}
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
				if (iIncludeOtherCourses) {
					// include courses of the student that are not in the curriculum
					for (CourseOffering co: iStudents.get(iStudentIds.get(student.getStudentId()))) {
						CurCourse curCourse = iModel.getCourse(co.getUniqueId());
						if (curCourse == null) {
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
			
			// Update loaded curricula
			Set<String> majors = iLoadedCurricula.get(iClassification.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation() + ":" + iClassification.getAcademicClassification().getCode());
			if (majors == null) {
				majors = new HashSet<String>();
				iLoadedCurricula.put(iClassification.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation() + ":" + iClassification.getAcademicClassification().getCode(), majors);
			}
			if (iClassification.getCurriculum().getMajors().isEmpty()) {
				majors.add("");
			} else {
				for (PosMajor mj: iClassification.getCurriculum().getMajors())
					majors.add(mj.getCode());
			}
		}		
		
	}
}
