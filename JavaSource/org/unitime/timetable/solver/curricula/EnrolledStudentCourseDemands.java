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
		for (Object[] o: (List<Object[]>)hibSession.createQuery(
				"select distinct e.courseOffering, s " +
				"from StudentClassEnrollment e inner join e.student s left join fetch s.areaClasfMajors where " +
				"e.courseOffering.subjectArea.session.uniqueId = :sessionId").setLong("sessionId", session.getUniqueId()).list()) {
			CourseOffering course = (CourseOffering)o[0];
			Student student = (Student)o[1];
			Set<WeightedStudentId> students = iDemands.get(course.getUniqueId());
			if (students == null) {
				students = new HashSet<WeightedStudentId>();
				iDemands.put(course.getUniqueId(), students);
			}
			WeightedStudentId studentId = new WeightedStudentId(student);
			students.add(studentId);
			Set<WeightedCourseOffering> courses = iStudentRequests.get(student.getUniqueId());
			if (courses == null) {
				courses = new HashSet<WeightedCourseOffering>();
				iStudentRequests.put(student.getUniqueId(), courses);
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
