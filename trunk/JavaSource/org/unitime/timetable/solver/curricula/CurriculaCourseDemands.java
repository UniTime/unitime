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
import java.util.List;
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
	private IdGenerator lastStudentId = new IdGenerator();
	protected ProjectedStudentCourseDemands iFallback;
	private Hashtable<Long, Hashtable<String, Set<String>>> iLoadedCurricula = new Hashtable<Long,Hashtable<String, Set<String>>>();
	private HashSet<Long> iCheckedCourses = new HashSet<Long>();
	private boolean iIncludeOtherStudents = true;

	public CurriculaCourseDemands(DataProperties properties) {
		if (properties != null)
			iFallback = new ProjectedStudentCourseDemands(properties);
		iIncludeOtherStudents = properties.getPropertyBoolean("CurriculaCourseDemands.IncludeOtherStudents", iIncludeOtherStudents);
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
			for (InstructionalOffering offering: offerings)
				for (CourseOffering course: offering.getCourseOfferings()) {
					if (!courses.isEmpty()) courses += ",";
					courses += course.getUniqueId();
				}
			curricula = hibSession.createQuery(
					"select distinct c from CurriculumCourse cc inner join cc.classification.curriculum c where " +
					"c.academicArea.session.uniqueId = :sessionId and cc.course.uniqueId in (" + courses + ")")
					.setLong("sessionId", session.getUniqueId()).list();
		} else {
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
		for (int i = 0; i < clasf.getNrStudents(); i++)
			students.add(new CurStudent(- lastStudentId.newId(), 1f));
		CurModel m = new CurModel(students);
		for (CurriculumCourse course: clasf.getCourses()) {
			int nrStudents = Math.round(clasf.getNrStudents() * course.getPercShare());
			m.addCourse(course.getUniqueId(), course.getCourse().getCourseName(), nrStudents);
			
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
		m.setStudentLimits();

		// Load model from cache (if exists)
		CurModel cachedModel = null;
		Element cache = (clasf.getStudents() == null ? null : clasf.getStudents().getRootElement());
		if (cache != null && cache.getName().equals(getCacheName())) {
			cachedModel = CurModel.loadFromXml(cache);
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
		for (CurriculumCourse course: clasf.getCourses()) {
			Set<WeightedStudentId> courseStudents = iDemands.get(course.getCourse().getUniqueId());
			if (courseStudents == null) {
				courseStudents = new HashSet<WeightedStudentId>();
				iDemands.put(course.getCourse().getUniqueId(), courseStudents);
			}
			for (CurStudent s: m.getCourse(course.getUniqueId()).getStudents()) {
				WeightedStudentId student = new WeightedStudentId(s.getStudentId());
				student.setStats(clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation(), clasf.getAcademicClassification().getCode(), null);
				student.setCurriculum(clasf.getCurriculum().getAbbv());
				courseStudents.add(student);
				Set<WeightedCourseOffering> courses = iStudentRequests.get(student.getStudentId());
				if (courses == null) {
					courses = new HashSet<WeightedCourseOffering>();
					iStudentRequests.put(student.getStudentId(), courses);
				}
				courses.add(new WeightedCourseOffering(course.getCourse(), student.getWeight()));
			}
		}
	}
	
	protected void computeTargetShare(CurriculumClassification clasf, CurModel model) {
		for (CurriculumCourse c1: clasf.getCourses()) {
			int x1 = Math.round(clasf.getNrStudents() * c1.getPercShare());
			for (CurriculumCourse c2: clasf.getCourses()) {
				int x2 = Math.round(clasf.getNrStudents() * c2.getPercShare());
				if (c1.getUniqueId() >= c2.getUniqueId()) continue;
				int share = Math.round(c1.getPercShare() * c2.getPercShare() * clasf.getNrStudents());
				CurriculumCourseGroup group = null;
				groups: for (CurriculumCourseGroup g1: c1.getGroups()) {
					for (CurriculumCourseGroup g2: c2.getGroups()) {
						if (g1.equals(g2)) { group = g1; break groups; }
					}
				}
				if (group != null) {
					share = (group.getType() == 0 ? 0 : Math.min(x1, x2));
				}
				model.setTargetShare(c1.getUniqueId(), c2.getUniqueId(), share);
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
		if (studentId >= 0 || iStudentRequests.isEmpty()) return iFallback.getCourses(studentId);
		return iStudentRequests.get(studentId);
	}
}
