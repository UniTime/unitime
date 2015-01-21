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
import java.util.Set;


import org.cpsolver.ifs.util.Progress;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.CourseOfferingDAO;

/**
 * @author Tomas Muller
 */
public interface StudentCourseDemands {
	/**
	 * Called only once
	 * @param hibSession opened hibernate session
	 * @param progress progress to print messages
	 * @param session current academic session
	 * @param offerings instructional offerings of the problem that is being loaded
	 */
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings);
	
	/**
	 * Called once for each course
	 * @param course course for which demands are requested
	 * @return set of students (their unique ids, and weights) that request the course
	 */
	public Set<WeightedStudentId> getDemands(CourseOffering course);
	
	/**
	 * Returns enrollment priority, i.e., an importance of a course request to a student 
	 * @param studentId identification of a student, e.g., as returned by {@link StudentCourseDemands#getDemands(CourseOffering)}
	 * @param course one of the course offerings requested by the student
	 * @return <code>null</code> if not implemented, 0.0 no priority, 1.0 highest priority
	 */
	public Double getEnrollmentPriority(Long studentId, Long courseId);
	
	/**
	 * Return true if students are made up (i.e, it does not make any sense to save them with the solution).
	 */
	public boolean isMakingUpStudents();
	
	/**
	 * Return true if students are real students (not last-like) for which the student class enrollments
	 * apply (real student class enrollments can be loaded instead of solution's student class assignments).
	 */
	public boolean canUseStudentClassEnrollmentsAsSolution();

	/**
	 * Return true if students should be weghted so that the offering is filled in completely.
	 */
	public boolean isWeightStudentsToFillUpOffering();
	
	/**
	 * List of courses for a student
	 */
	public Set<WeightedCourseOffering> getCourses(Long studentId);
	
	public static class WeightedStudentId {
		private long iStudentId;
		private float iWeight;
		private String iAreaAbbv, iClasfCode, iMajorCode, iCurriculum;
		
		public WeightedStudentId(long studentId, float weight) {
			iStudentId = studentId;
			iWeight = weight;
		}
		
		public WeightedStudentId(long studentId) {
			this(studentId, 1.0f);
		}
		
		public long getStudentId() {
			return iStudentId;
		}
		
		public float getWeight() {
			return iWeight;
		}
		
		public void setWeight(float weight) {
			iWeight = weight;
		}
		
		public void setStats(String areaAbbv, String clasfCode, String majorCode) {
			iAreaAbbv = areaAbbv;
			iClasfCode = clasfCode;
			iMajorCode = majorCode;
		}
		
		public void setCurriculum(String curriculum) { iCurriculum = curriculum; }
		
		public String getArea() { return iAreaAbbv; }
		public String getClasf() { return iClasfCode; }
		public String getMajor() { return iMajorCode; }
		public String getCurriculum() { return iCurriculum; }
		
		public boolean match(String areaAbbv, String clasfCode, String majorCode) {
			return areaAbbv.equals(iAreaAbbv) && clasfCode.equals(iClasfCode) && majorCode.equals(iMajorCode);
		}
		
		public boolean match(CurriculumClassification clasf) {
			if (!clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation().equals(iAreaAbbv)) return false;
			if (!clasf.getAcademicClassification().getCode().equals(iClasfCode)) return false;
			if (clasf.getCurriculum().getMajors().isEmpty()) return true;
			for (PosMajor m: clasf.getCurriculum().getMajors()) {
				if (m.getCode().equals(iMajorCode)) return true;
			}
			return false;
		}
		
		public int hashCode() {
			return new Long(getStudentId()).hashCode();
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof WeightedStudentId)) return false;
			return getStudentId() == ((WeightedStudentId)o).getStudentId();
		}
		
		public String toString() {
			return String.valueOf(getStudentId());
		}
	}
	
	public static class WeightedCourseOffering {
		private transient CourseOffering iCourseOffering = null;
		private long iCourseOfferingId;
		private float iWeight = 1.0f;
		
		public WeightedCourseOffering(CourseOffering courseOffering) {
			iCourseOffering = courseOffering;
			iCourseOfferingId = courseOffering.getUniqueId();
		}
		
		public WeightedCourseOffering(Long courseOfferingId) {
			iCourseOfferingId = courseOfferingId;
		}
		
		public WeightedCourseOffering(CourseOffering courseOffering, float weight) {
			this(courseOffering);
			iWeight = weight;
		}
		
		public WeightedCourseOffering(Long courseOfferingId, float weight) {
			this(courseOfferingId);
			iWeight = weight;
		}
		
		public Long getCourseOfferingId() { return iCourseOfferingId; }
		
		public CourseOffering getCourseOffering() { 
			if (iCourseOffering == null) iCourseOffering = CourseOfferingDAO.getInstance().get(iCourseOfferingId);
			return iCourseOffering;
		}
		
		public float getWeight() {
			return iWeight;
		}
	}
}
