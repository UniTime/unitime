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
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.ProjectionsProvider;


/**
 * @author Tomas Muller
 */
public class LastLikeStudentCourseDemands implements StudentCourseDemands, ProjectionsProvider {
	protected org.hibernate.Session iHibSession;
	protected Hashtable<String, Set<WeightedStudentId>> iDemandsForPemId = new Hashtable<String, Set<WeightedStudentId>>();
	protected Hashtable<Long, Hashtable<String, Set<WeightedStudentId>>> iDemandsForSubjectCourseNbr = new Hashtable<Long, Hashtable<String,Set<WeightedStudentId>>>();
	protected Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = null;
	protected Long iSessionId = null;
	
	public LastLikeStudentCourseDemands(DataProperties properties) {
	}
	
	@Override
	public boolean isMakingUpStudents() { return false; }
	
	@Override
	public boolean canUseStudentClassEnrollmentsAsSolution() { return false; }

	@Override
	public boolean isWeightStudentsToFillUpOffering() { return false; }
	
	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		iHibSession = hibSession;
		iSessionId = session.getUniqueId();
	}
	
	@Override
	public float getProjection(String areaAbbv, String clasfCode, String majorCode) {
		return 1.0f;
	}
	
	protected Hashtable<String, Set<WeightedStudentId>> loadSubject(SubjectArea subject) {
		Hashtable<String, Set<WeightedStudentId>> demandsForCourseNbr = new Hashtable<String, Set<WeightedStudentId>>();
		iDemandsForSubjectCourseNbr.put(subject.getUniqueId(), demandsForCourseNbr);
		for (Object[] d: (List<Object[]>)iHibSession.createQuery(
				"select distinct d.courseNbr, d.coursePermId, s " +
				"from LastLikeCourseDemand d inner join d.student s left join fetch s.academicAreaClassifications left join fetch s.posMajors where " +
				"d.subjectArea.uniqueId = :subjectAreaId")
				.setLong("subjectAreaId", subject.getUniqueId()).setCacheable(true).list()) {
			String courseNbr = (String)d[0];
			String coursePermId = (String)d[1];
			Student student = (Student)d[2];
			WeightedStudentId studentId = new WeightedStudentId(student, this);
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
	
	@Override
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

		if (course.getDemandOffering() != null && !course.getDemandOffering().equals(course)) {
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
	
	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		if (iStudentRequests == null) {
			iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
			for (Object[] o : (List<Object[]>)iHibSession.createQuery(
					"select distinct s, co " +
					"from LastLikeCourseDemand x inner join x.student s left join fetch s.academicAreaClassifications left join fetch s.posMajors, CourseOffering co where " +
					"x.subjectArea.session.uniqueId = :sessionId and "+
					"co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
					"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr))")
					.setLong("sessionId", iSessionId)
					.setCacheable(true).list()) {
				Student student = (Student)o[0];
				CourseOffering co = (CourseOffering)o[1];
				Set<WeightedCourseOffering> courses = iStudentRequests.get(student.getUniqueId());
				if (courses == null) {
					courses = new HashSet<WeightedCourseOffering>();
					iStudentRequests.put(student.getUniqueId(), courses);
				}
				courses.add(new WeightedCourseOffering(co, new WeightedStudentId(student, this).getWeight()));
			}
		}
		return iStudentRequests.get(studentId);
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		return null;
	}
}
