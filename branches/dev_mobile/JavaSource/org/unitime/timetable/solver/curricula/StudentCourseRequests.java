/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

/**
 * @author Tomas Muller
 */
public class StudentCourseRequests implements StudentCourseDemands {
	protected Hashtable<Long, Hashtable<Long, Set<WeightedStudentId>>> iDemands = new Hashtable<Long, Hashtable<Long, Set<WeightedStudentId>>>();
	protected org.hibernate.Session iHibSession = null;
	protected Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = null;
	protected Long iSessionId = null;
	protected double iBasePriorityWeight = 0.9;
	private Hashtable<Long, Hashtable<Long, Double>> iEnrollmentPriorities = new Hashtable<Long, Hashtable<Long, Double>>();
	
	public StudentCourseRequests(DataProperties conf) {
		iBasePriorityWeight = conf.getPropertyDouble("StudentCourseRequests.BasePriorityWeight", iBasePriorityWeight);
	}

	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		iHibSession = hibSession;
		iSessionId = session.getUniqueId();
	}
	
	protected Hashtable<Long, Set<WeightedStudentId>> loadDemandsForSubjectArea(SubjectArea subjectArea) {
		Hashtable<Long, Set<WeightedStudentId>> demands = new Hashtable<Long, Set<WeightedStudentId>>();
		for (Object[] o: (List<Object[]>) iHibSession.createQuery(
					"select distinct r.courseOffering.uniqueId, s.uniqueId, a.academicAreaAbbreviation, f.code, m.code, " +
					"r.courseDemand.priority, r.courseDemand.alternative, r.order from CourseRequest r inner join r.courseDemand.student s " +
					"left outer join s.academicAreaClassifications c left outer join s.posMajors m left outer join c.academicArea a left outer join c.academicClassification f " +
					"where r.courseOffering.subjectArea.uniqueId = :subjectId")
					.setLong("subjectId", subjectArea.getUniqueId()).setCacheable(true).list()) {
			Long courseId = (Long)o[0];
			Long studentId = (Long)o[1];
			String areaAbbv = (String)o[2];
			String clasfCode = (String)o[3];
			String majorCode = (String)o[4];
			Integer priority = (Integer)o[5];
			Boolean alternative = (Boolean)o[6];
			Integer order = (Integer)o[7];
			Set<WeightedStudentId> students = demands.get(courseId);
			if (students == null) {
				students = new HashSet<WeightedStudentId>();
				demands.put(courseId, students);
			}
			WeightedStudentId student = new WeightedStudentId(studentId);
			student.setStats(areaAbbv, clasfCode, majorCode);
			student.setCurriculum(areaAbbv == null ? null : majorCode == null ? areaAbbv : areaAbbv + "/" + majorCode);
			students.add(student);
			if (priority != null && Boolean.FALSE.equals(alternative)) {
				if (order != null) priority += order;
				Hashtable<Long, Double> priorities = iEnrollmentPriorities.get(studentId);
				if (priorities == null) {
					priorities = new Hashtable<Long, Double>();
					iEnrollmentPriorities.put(studentId, priorities);
				}
				priorities.put(courseId, Math.pow(iBasePriorityWeight, priority));
			}
		}
		return demands;
	}

	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		if (iStudentRequests == null) {
			iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
			for (Object[] o : (List<Object[]>)iHibSession.createQuery(
					"select distinct d.student.uniqueId, c, d.priority, d.alternative, r.order " +
					"from CourseRequest r inner join r.courseOffering c inner join r.courseDemand d where d.student.session.uniqueId = :sessionId")
					.setLong("sessionId", iSessionId).setCacheable(true).list()) {
				Long sid = (Long)o[0];
				CourseOffering co = (CourseOffering)o[1];
				Integer priority = (Integer)o[2];
				Boolean alternative = (Boolean)o[3];
				Integer order = (Integer)o[4];
				Set<WeightedCourseOffering> courses = iStudentRequests.get(sid);
				if (courses == null) {
					courses = new HashSet<WeightedCourseOffering>();
					iStudentRequests.put(sid, courses);
				}
				courses.add(new WeightedCourseOffering(co));
				if (priority != null && Boolean.FALSE.equals(alternative)) {
					if (order != null) priority += order;
					Hashtable<Long, Double> priorities = iEnrollmentPriorities.get(studentId);
					if (priorities == null) {
						priorities = new Hashtable<Long, Double>();
						iEnrollmentPriorities.put(studentId, priorities);
					}
					priorities.put(co.getUniqueId(), Math.pow(iBasePriorityWeight, priority));
				}
			}
		}
		return iStudentRequests.get(studentId);
	}

	@Override
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		Hashtable<Long, Set<WeightedStudentId>> demands = iDemands.get(course.getSubjectArea().getUniqueId());
		if (demands == null) {
			demands = loadDemandsForSubjectArea(course.getSubjectArea());
			iDemands.put(course.getSubjectArea().getUniqueId(), demands);
		}
		return demands.get(course.getUniqueId());
	}

	@Override
	public boolean isMakingUpStudents() {
		return false;
	}

	@Override
	public boolean isWeightStudentsToFillUpOffering() {
		return false;
	}
	
	@Override
	public boolean canUseStudentClassEnrollmentsAsSolution() {
		return true;
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		Hashtable<Long, Double> priorities = iEnrollmentPriorities.get(studentId);
		return (priorities == null ? null : priorities.get(courseId));
	}
}
