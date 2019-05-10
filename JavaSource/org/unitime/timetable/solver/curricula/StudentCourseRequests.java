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

import java.text.DecimalFormat;
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
	protected boolean iIncludeAlternatives = true;
	
	public StudentCourseRequests(DataProperties conf) {
		iBasePriorityWeight = conf.getPropertyDouble("StudentCourseRequests.BasePriorityWeight", iBasePriorityWeight);
		iIncludeAlternatives = conf.getPropertyBoolean("StudentCourseRequests.IncludeAlternatives", iIncludeAlternatives);
	}
	
	public boolean isIncludeAlternatives() { return iIncludeAlternatives; }

	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		iHibSession = hibSession;
		iSessionId = session.getUniqueId();
	}
	
	protected static float getWeight(Integer order, Integer max) {
		if (order == null || max == null) return 1.0f;
		return (float)(Math.pow(0.5, 1 + order) + Math.pow(0.5, 1 + max) / (1 + max));
	}
	
	protected Hashtable<Long, Set<WeightedStudentId>> loadDemandsForSubjectArea(SubjectArea subjectArea) {
		Hashtable<Long, Set<WeightedStudentId>> demands = new Hashtable<Long, Set<WeightedStudentId>>();
		for (Object[] o: (List<Object[]>) iHibSession.createQuery(
					"select distinct s, r.courseOffering.uniqueId, r.courseDemand.priority, r.courseDemand.alternative" +
					(iIncludeAlternatives ? ", r.order, " +
						"(select max(x.order) from CourseRequest x where x.courseDemand = r.courseDemand)," +
						"(select x.courseOffering.instructionalOffering.uniqueId from CourseRequest x where x.courseDemand = r.courseDemand and x.order = 0)" : "") +
					" from CourseRequest r inner join r.courseDemand.student s left join fetch s.areaClasfMajors where " +
					"r.courseOffering.subjectArea.uniqueId = :subjectId" + (iIncludeAlternatives ? "" : " and r.order = 0"))
					.setLong("subjectId", subjectArea.getUniqueId()).setCacheable(true).list()) {
			Student s = (Student)o[0];
			Long courseId = (Long)o[1];
			Integer priority = (Integer)o[2];
			Boolean alternative = (Boolean)o[3];
			Integer order = (iIncludeAlternatives ? (Integer)o[4] : null);
			Integer maxOrder = (iIncludeAlternatives ? (Integer)o[5] : null);
			Long primaryOfferingId = (iIncludeAlternatives ? (Long)o[6] : null);
			Set<WeightedStudentId> students = demands.get(courseId);
			if (students == null) {
				students = new HashSet<WeightedStudentId>();
				demands.put(courseId, students);
			}
			WeightedStudentId student = new WeightedStudentId(s, getWeight(order, maxOrder));
			student.setPrimaryOfferingId(primaryOfferingId);
			students.add(student);
			if (priority != null && Boolean.FALSE.equals(alternative)) {
				Hashtable<Long, Double> priorities = iEnrollmentPriorities.get(s.getUniqueId());
				if (priorities == null) {
					priorities = new Hashtable<Long, Double>();
					iEnrollmentPriorities.put(s.getUniqueId(), priorities);
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
					"select distinct d.student.uniqueId, c, d.priority, d.alternative" +
					(iIncludeAlternatives ? ", r.order, " +
							"(select max(x.order) from CourseRequest x where x.courseDemand = r.courseDemand), " +
							"(select x.courseOffering.instructionalOffering.uniqueId from CourseRequest x where x.courseDemand = r.courseDemand and x.order = 0)" : "") +
					" from CourseRequest r inner join r.courseOffering c inner join r.courseDemand d where d.student.session.uniqueId = :sessionId" +
					(iIncludeAlternatives ? "" : " and r.order = 0"))
					.setLong("sessionId", iSessionId).setCacheable(true).list()) {
				Long sid = (Long)o[0];
				CourseOffering co = (CourseOffering)o[1];
				Integer priority = (Integer)o[2];
				Boolean alternative = (Boolean)o[3];
				Integer order = (iIncludeAlternatives ? (Integer)o[4] : null);
				Integer maxOrder = (iIncludeAlternatives ? (Integer)o[5] : null);
				Long primaryOfferingId = (iIncludeAlternatives ? (Long)o[6] : null);
				Set<WeightedCourseOffering> courses = iStudentRequests.get(sid);
				if (courses == null) {
					courses = new HashSet<WeightedCourseOffering>();
					iStudentRequests.put(sid, courses);
				}
				WeightedCourseOffering wco = new WeightedCourseOffering(co, getWeight(order, maxOrder));
				wco.setPrimaryOfferingId(primaryOfferingId);
				courses.add(wco);
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

	public static void main(String[] args) {
		DecimalFormat df = new DecimalFormat("0.00000");
		for (int max = 0; max <= 5; max++) {
			for (int order = 0; order <= max; order++) {
				System.out.println((1 + order) + " of " + (1 + max) + ": " + df.format(getWeight(order, max)));
			}
			System.out.println();
		}
	}
}
