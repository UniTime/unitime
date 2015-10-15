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
import java.util.TreeSet;

import org.cpsolver.ifs.util.Progress;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
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
	
	public static class AreaCode implements Comparable<AreaCode> {
		String iArea, iCode;
		public AreaCode(String area, String code) {
			iArea = area; iCode = code;
		}
		
		public String getArea() { return iArea; }
		public String getCode() { return iCode; }
		
		public String toString() { return getArea() + (getCode().isEmpty() ? "" : " " + getCode()); }
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AreaCode)) return false;
			return toString().equals(o.toString());
		}
		
		@Override
		public int compareTo(AreaCode ac) {
			return toString().compareTo(ac.toString());
		}
	}
	
	public static class WeightedStudentId {
		private long iStudentId;
		private float iWeight;
		private Set<AreaCode> iAreas = new TreeSet<AreaCode>();
		private Set<AreaCode> iMajors = new TreeSet<AreaCode>();
		private Set<String> iCurricula = new TreeSet<String>();
		
		public WeightedStudentId(Student student, ProjectionsProvider projections) {
			iStudentId = student.getUniqueId();
			iWeight = 1.0f;
			float rule = 1.0f; int cnt = 0;
			for (AcademicAreaClassification aac: student.getAcademicAreaClassifications()) {
				iAreas.add(new AreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(), aac.getAcademicClassification().getCode()));
				boolean hasMajor = false;
				for (PosMajor major: student.getPosMajors()) {
					if (major.getAcademicAreas().contains(aac.getAcademicArea())) {
						if (projections != null) {
							rule *= projections.getProjection(aac.getAcademicArea().getAcademicAreaAbbreviation(), aac.getAcademicClassification().getCode(), major.getCode());
							cnt ++;
							hasMajor = true;
						}
						iMajors.add(new AreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(), major.getCode()));
					}
				}
				if (!hasMajor && projections != null) {
					rule *= projections.getProjection(aac.getAcademicArea().getAcademicAreaAbbreviation(), aac.getAcademicClassification().getCode(), "");
					cnt ++;
				}
			}
			if (cnt == 1)
				iWeight = rule;
			else if (cnt > 1)
				iWeight = (float) Math.pow(rule, 1.0 / cnt);
		}
		
		public WeightedStudentId(Long studentId, CurriculumClassification cc, ProjectionsProvider projections) {
			Curriculum curriculum = cc.getCurriculum();
			iWeight = 1.0f;
			if (projections != null) {
				if (curriculum.getMajors().isEmpty()) {
					iWeight = projections.getProjection(curriculum.getAcademicArea().getAcademicAreaAbbreviation(), cc.getAcademicClassification().getCode(), "");
				} else if (curriculum.getMajors().size() == 1) {
					for (PosMajor m: curriculum.getMajors())
						iWeight = projections.getProjection(curriculum.getAcademicArea().getAcademicAreaAbbreviation(), cc.getAcademicClassification().getCode(), m.getCode());
				} else {
					double rule = 1.0;
					for (PosMajor m: curriculum.getMajors())
						rule *= projections.getProjection(curriculum.getAcademicArea().getAcademicAreaAbbreviation(), cc.getAcademicClassification().getCode(), m.getCode());
					iWeight = (float)Math.pow(rule, 1.0 / curriculum.getMajors().size());
				}
			}
			iAreas.add(new AreaCode(curriculum.getAcademicArea().getAcademicAreaAbbreviation(), cc.getAcademicClassification().getCode()));
			for (PosMajor major: curriculum.getMajors())
				iMajors.add(new AreaCode(curriculum.getAcademicArea().getAcademicAreaAbbreviation(), major.getCode()));
			iCurricula.add(curriculum.getAbbv());
		}
		
		public WeightedStudentId(Student student) {
			this(student, null);
		}
		
		public WeightedStudentId(Long studentId, CurriculumClassification cc) {
			this(studentId, cc, null);
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
		
		public void setCurriculum(String curriculum) {
			iCurricula.clear(); iCurricula.add(curriculum);
		}
		
		public boolean hasArea(String areaAbbv) {
			for (AreaCode a: iAreas)
				if (a.getArea().equals(areaAbbv)) return true;
			return false;
		}
		
		public boolean hasClassification(String areaAbbv, String clasfCode) {
			for (AreaCode a: iAreas)
				if (a.getArea().equals(areaAbbv) && a.getCode().equals(clasfCode)) return true;
			return false;
		}
		
		public boolean hasMajor(String areaAbbv, String majorCode) {
			for (AreaCode a: iMajors)
				if (a.getArea().equals(areaAbbv) && a.getCode().equals(majorCode)) return true;
			return false;
		}
		
		public Set<AreaCode> getAreas() { return iAreas; }
		public Set<String> getMajors(String area) {
			Set<String> ret = new TreeSet<String>();
			for (AreaCode m: iMajors)
				if (m.getArea().equals(area))
					ret.add(m.getCode());
			return ret;
		}
		
		public String getArea() { return toString(iAreas, true, ","); }
		public String getClasf() { return toString(iAreas, false, ","); }
		public String getMajor() { return toString(iMajors, false, ","); }
		public String getCurriculum() {
			StringBuffer ret = new StringBuffer();
			if (iCurricula.isEmpty()) {
				for (AreaCode a: iAreas) {
					StringBuffer majors = new StringBuffer();
					for (AreaCode m: iMajors) {
						if (a.getArea().equals(m.getArea())) {
							if (majors.length() > 0) majors.append(",");
							majors.append(m.getCode());
						}
					}
					if (ret.length() > 0) ret.append(", ");
					ret.append(a.getArea());
					if (majors.length() > 0) {
						ret.append("/");
						ret.append(majors);
					}
				}
			} else {
				for (String curriculum: iCurricula) {
					if (ret.length() > 0) ret.append(", ");
					ret.append(curriculum);	
				}
			}
			return ret.toString();
		}
		
		private static String toString(Set<AreaCode> set, boolean area, String delim) {
			if (set == null || set.isEmpty()) return null;
			StringBuffer ret = new StringBuffer();
			for (AreaCode s: set) {
				if (ret.length() > 0) ret.append(delim);
				ret.append(area ? s.getArea() : s.getCode());
			}
			return ret.toString();
		}
		
		public boolean match(String areaAbbv, Set<String> majors) {
			for (AreaCode a: iAreas) {
				if (a.getArea().equals(areaAbbv)) {
					for (AreaCode m: iMajors) {
						if (m.getArea().equals(areaAbbv) && majors.contains(m.getCode()))
							return true;
					}
				}
			}
			return false;
		}
		
		public boolean match(CurriculumClassification clasf) {
			for (AreaCode a: iAreas) {
				if (a.getArea().equals(clasf.getCurriculum().getAcademicArea().getAcademicAreaAbbreviation()) && a.getCode().equals(clasf.getAcademicClassification().getCode())) {
					if (clasf.getCurriculum().isMultipleMajors()) {
						for (PosMajor major: clasf.getCurriculum().getMajors()) {
							boolean found = false;
							for (AreaCode m: iMajors) {
								if (m.getArea().equals(a.getArea()) && m.getCode().equals(major.getCode())) {
									found = true; break;
								}
							}
							if (found) return true;
						}
					} else {
						for (PosMajor major: clasf.getCurriculum().getMajors()) {
							for (AreaCode m: iMajors) {
								if (m.getArea().equals(a.getArea()) && m.getCode().equals(major.getCode()))
									return true;
							}
						}
					}
				}
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
	
	public static interface ProjectionsProvider {
		public float getProjection(String areaAbbv, String clasfCode, String majorCode);
	}
}
