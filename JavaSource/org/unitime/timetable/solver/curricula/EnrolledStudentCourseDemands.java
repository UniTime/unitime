/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.curricula;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

public class EnrolledStudentCourseDemands implements StudentCourseDemands {
	private org.hibernate.Session iHibSession = null;
	private Hashtable<Long, Set<WeightedStudentId>> iDemands = new Hashtable<Long, Set<WeightedStudentId>>();
	private boolean iCacheAll = true;
	
	public EnrolledStudentCourseDemands(DataProperties properties) {
		iCacheAll = properties.getPropertyBoolean("EnrolledStudentsCourseDemands.CacheAll", iCacheAll);
	}

	public void init(org.hibernate.Session hibSession, Progress progress, Session session) {
		iHibSession = hibSession;
		if (iCacheAll)
			for (Object[] o: (List<Object[]>)hibSession.createQuery("select e.courseOffering.uniqueId, e.student.uniqueId from StudentClassEnrollment e where " +
					"e.courseOffering.subjectArea.session.uniqueId = :sessionId").setLong("sessionId", session.getUniqueId()).list()) {
				Long courseId = (Long)o[0];
				Long studentId = (Long)o[1];
				Set<WeightedStudentId> students = iDemands.get(courseId);
				if (students == null) {
					students = new HashSet<WeightedStudentId>();
					iDemands.put(courseId, students);
				}
				students.add(new WeightedStudentId(studentId));
			}
	}

	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		if (iCacheAll)
			return iDemands.get(course.getUniqueId());
		Set<WeightedStudentId> students = new HashSet<WeightedStudentId>();
		for (Long studentId: (List<Long>)iHibSession.createQuery("select distinct e.student.uniqueId from StudentClassEnrollment e where " +
				"e.courseOffering.uniqueId = :courseId").setLong("courseId", course.getUniqueId()).list()) {
			students.add(new WeightedStudentId(studentId));
		}
		return students;
	}

}
