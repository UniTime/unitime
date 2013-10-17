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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

/**
 * @author Tomas Muller
 */
public class EnrolledStudentCourseDemands implements StudentCourseDemands {
	private Hashtable<Long, Set<WeightedStudentId>> iDemands = new Hashtable<Long, Set<WeightedStudentId>>();
	private Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
	
	public EnrolledStudentCourseDemands(DataProperties properties) {
	}

	public boolean isMakingUpStudents() { return false; }
	
	public boolean canUseStudentClassEnrollmentsAsSolution() { return true; }

	public boolean isWeightStudentsToFillUpOffering() { return false; }
	
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		for (Object[] o: (List<Object[]>)hibSession.createQuery("select e.courseOffering, s.uniqueId, a.academicAreaAbbreviation, f.code, m.code from StudentClassEnrollment e " +
				"inner join e.student s left outer join s.academicAreaClassifications c left outer join s.posMajors m " +
				"left outer join c.academicArea a left outer join c.academicClassification f where " +
				"e.courseOffering.subjectArea.session.uniqueId = :sessionId").setLong("sessionId", session.getUniqueId()).list()) {
			CourseOffering course = (CourseOffering)o[0];
			Long sid = (Long)o[1];
			String areaAbbv = (String)o[2];
			String clasfCode = (String)o[3];
			String majorCode = (String)o[4];
			Set<WeightedStudentId> students = iDemands.get(course.getUniqueId());
			if (students == null) {
				students = new HashSet<WeightedStudentId>();
				iDemands.put(course.getUniqueId(), students);
			}
			WeightedStudentId studentId = new WeightedStudentId(sid);
			studentId.setStats(areaAbbv, clasfCode, majorCode);
			studentId.setCurriculum(areaAbbv == null ? null : majorCode == null ? areaAbbv : areaAbbv + "/" + majorCode);
			students.add(studentId);
			Set<WeightedCourseOffering> courses = iStudentRequests.get(studentId);
			if (courses == null) {
				courses = new HashSet<WeightedCourseOffering>();
				iStudentRequests.put(sid, courses);
			}
			courses.add(new WeightedCourseOffering(course));
		}
	}

	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		return iDemands.get(course.getUniqueId());
	}
	
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		return iStudentRequests.get(studentId);
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		return null;
	}
}
