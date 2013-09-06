/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.curricula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.IdGenerator;
import net.sf.cpsolver.ifs.util.Progress;

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

	public CurriculaCourseDemands(DataProperties properties) {
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
			if (nrCourses <= 1000) {
				curricula = hibSession.createQuery(
						"select distinct c from CurriculumCourse cc inner join cc.classification.curriculum c where " +
						"c.academicArea.session.uniqueId = :sessionId and cc.course.uniqueId in (" + courses + ")")
						.setLong("sessionId", session.getUniqueId()).list();
			}
		}
		
		if (curricula == null) {
			curricula = hibSession.createQuery(
					"select c from Curriculum c where c.academicArea.session.uniqueId = :sessionId")
					.setLong("sessionId", session.getUniqueId()).list();
		}

		progress.setPhase("Loading curricula", curricula.size());
		for (Curriculum curriculum: curricula) {
			for (CurriculumClassification clasf: curriculum.getClassifications()) {
				init(hibSession, clasf);
			}
			progress.incProgress();
		}
		
		if (iDemands.isEmpty()) {
			progress.warn("There are no curricula, using projected course demands instead.");
		}
	}
	
	protected String getCacheName() {
		return "curriculum-demands";
	}
	
	private void init(org.hibernate.Session hibSession, CurriculumClassification clasf) {
		if (clasf.getNrStudents() <= 0) return;
		
		sLog.debug("Processing " + clasf.getCurriculum().getAbbv() + " " + clasf.getName() + " ... (" + clasf.getNrStudents() + " students, " + clasf.getCourses().size() + " courses)");
				
		// Create model
		List<CurStudent> students = new ArrayList<CurStudent>();
		for (long i = 0; i < clasf.getNrStudents(); i++)
			students.add(new CurStudent(- (1 + i), 1f));
		CurModel m = new CurModel(students);
		Hashtable<Long, CourseOffering> courses = new Hashtable<Long, CourseOffering>();
		for (CurriculumCourse course: clasf.getCourses()) {
			m.addCourse(course.getUniqueId(), course.getCourse().getCourseName(), course.getPercShare() * clasf.getNrStudents(), iEnrollmentPriorityProvider.getEnrollmentPriority(course));
			courses.put(course.getUniqueId(), course.getCourse());
			
			Hashtable<String,Set<String>> curricula = iLoadedCurricula.get(course.getCourse().getUniqueId());
			if (curricula == null) {
				curricula = new Hashtable<String, Set<String>>();
				iLoadedCurricula.put(course.getCourse().getUniqueId(), curricula);
			}
			Set<String> majors = curricula.get(clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation());
			if (majors == null) {
				majors = new HashSet<String>();
				curricula.put(clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation(), majors);
			}
			if (clasf.getCurriculum().getMajors().isEmpty()) {
				majors.add("");
			} else {
				for (PosMajor mj: clasf.getCurriculum().getMajors())
					majors.add(mj.getCode());
			}

		}
		computeTargetShare(clasf, m);
		if (iSetStudentCourseLimits)
			m.setStudentLimits();

		// Load model from cache (if exists)
		CurModel cachedModel = null;
		Element cache = (clasf.getStudents() == null ? null : clasf.getStudents().getRootElement());
		if (cache != null && cache.getName().equals(getCacheName())) {
			cachedModel = CurModel.loadFromXml(cache);
			if (iSetStudentCourseLimits)
				cachedModel.setStudentLimits();
		}

		// Check the cached model
		if (cachedModel != null && cachedModel.isSameModel(m)) {
			// Reuse
			sLog.debug("  using cached model...");
			m = cachedModel;
		} else {
			// Solve model
			m.solve();
			
			// Save into the cache
			Document doc = DocumentHelper.createDocument();
			m.saveAsXml(doc.addElement(getCacheName()));
			// sLog.debug("Model:\n" + doc.asXML());
			clasf.setStudents(doc);
			hibSession.update(clasf);
		}
		
		// Save results
		String majors = "";
		for (PosMajor major: clasf.getCurriculum().getMajors()) {
			if (!majors.isEmpty()) majors += "|";
			majors += major.getCode();
		}
		for (CurStudent s: m.getStudents()) {
			WeightedStudentId student = new WeightedStudentId(- lastStudentId.newId());
			student.setStats(clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation(), clasf.getAcademicClassification().getCode(), majors);
			student.setCurriculum(clasf.getCurriculum().getAbbv());
			Set<WeightedCourseOffering> studentCourses = new HashSet<WeightedCourseOffering>();
			iStudentRequests.put(student.getStudentId(), studentCourses);
			Hashtable<Long, Double> priorities = new Hashtable<Long, Double>(); iEnrollmentPriorities.put(student.getStudentId(), priorities);
			for (CurCourse course: s.getCourses()) {
				CourseOffering co = courses.get(course.getCourseId());
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
	
	protected void computeTargetShare(CurriculumClassification clasf, CurModel model) {
		for (CurriculumCourse c1: clasf.getCourses()) {
			float x1 = c1.getPercShare() * clasf.getNrStudents();
			Set<CurriculumCourse>[] group = new HashSet[] { new HashSet<CurriculumCourse>(), new HashSet<CurriculumCourse>()};
			Queue<CurriculumCourse> queue = new LinkedList<CurriculumCourse>();
			queue.add(c1);
			Set<CurriculumCourseGroup> done = new HashSet<CurriculumCourseGroup>();
			while (!queue.isEmpty()) {
				CurriculumCourse c = queue.poll();
				for (CurriculumCourseGroup g: c.getGroups())
					if (done.add(g))
						for (CurriculumCourse x: clasf.getCourses())
							if (!x.equals(c) && !x.equals(c1) && x.getGroups().contains(g) && group[group[0].contains(c) ? 0 : g.getType()].add(x))
								queue.add(x);
			}
			for (CurriculumCourse c2: clasf.getCourses()) {
				float x2 = c2.getPercShare() * clasf.getNrStudents();
				if (c1.getUniqueId() >= c2.getUniqueId()) continue;
				float share = c1.getPercShare() * c2.getPercShare() * clasf.getNrStudents();
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
				for (WeightedStudentId student: other) {
					if (student.getArea() == null) continue; // ignore students w/o academic area
					Set<String> majors = curricula.get(student.getArea());
					if (majors != null && majors.contains("")) continue; // all majors
					if (majors == null || (student.getMajor() != null && !majors.contains(student.getMajor())))
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
}
