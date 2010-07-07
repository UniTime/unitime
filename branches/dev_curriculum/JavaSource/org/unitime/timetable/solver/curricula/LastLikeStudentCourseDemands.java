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
import org.unitime.timetable.model.SubjectArea;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

public class LastLikeStudentCourseDemands implements StudentCourseDemands {
	private org.hibernate.Session iHibSession;
	private Hashtable<String, Set<WeightedStudentId>> iDemandsForPemId = new Hashtable<String, Set<WeightedStudentId>>();
	private Hashtable<Long, Hashtable<String, Set<WeightedStudentId>>> iDemandsForSubjectCourseNbr = new Hashtable<Long, Hashtable<String,Set<WeightedStudentId>>>();
	
	public LastLikeStudentCourseDemands(DataProperties properties) {
	}
	
	public void init(org.hibernate.Session hibSession, Progress progress, Session session) {
		iHibSession = hibSession;
	}
	
	public Hashtable<String, Set<WeightedStudentId>> loadSubject(SubjectArea subject) {
		Hashtable<String, Set<WeightedStudentId>> demandsForCourseNbr = new Hashtable<String, Set<WeightedStudentId>>();
		iDemandsForSubjectCourseNbr.put(subject.getUniqueId(), demandsForCourseNbr);
		for (Object[] d: (List<Object[]>)iHibSession.createQuery("select d.courseNbr, d.student.uniqueId, d.coursePermId "+
				"from LastLikeCourseDemand d where d.subjectArea.uniqueId=:subjectAreaId")
				.setLong("subjectAreaId", subject.getUniqueId()).setCacheable(true).list()) {
			String courseNbr = (String)d[0];
			WeightedStudentId studentId = new WeightedStudentId((Long)d[1]);
			String coursePermId = (String)d[2];
			
			Set<WeightedStudentId> studentIds = demandsForCourseNbr.get(courseNbr);
			if (studentIds == null) {
				studentIds = new HashSet<WeightedStudentId>();
				demandsForCourseNbr.put(courseNbr, studentIds);
			}
			studentIds.add(studentId);
			
			if (coursePermId!=null) {
			    studentIds = iDemandsForPemId.get(coursePermId);
			    if (studentIds==null) {
                    studentIds = new HashSet<WeightedStudentId>();
                    iDemandsForPemId.put(coursePermId, studentIds);
                }
                studentIds.add(studentId);
			}
		}
		return demandsForCourseNbr;
	}
	
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		Hashtable<String, Set<WeightedStudentId>> demandsForCourseNbr = iDemandsForSubjectCourseNbr.get(course.getSubjectArea().getUniqueId());
		if (demandsForCourseNbr == null) {
			demandsForCourseNbr = loadSubject(course.getSubjectArea());
		}
		Set<WeightedStudentId> studentIds = null;
		if (course.getPermId() != null)
			studentIds = iDemandsForPemId.get(course.getPermId());
		if (studentIds == null)
			studentIds = demandsForCourseNbr.get(course.getCourseNbr());

		if (course.getDemandOffering() != null) {
			if (studentIds == null)
				studentIds = getDemands(course.getDemandOffering());
			else {
				studentIds = new HashSet<WeightedStudentId>(studentIds);
				studentIds.addAll(getDemands(course.getDemandOffering()));
			}
		}
		
		if (studentIds == null)
			studentIds = new HashSet<WeightedStudentId>();
		
		return studentIds;
	}

}
