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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class CurriculumInterface implements IsSerializable, Comparable<CurriculumInterface> {
	private Long iId;
	private String iAbbv, iName;
	private boolean iEditable = false;
	private String iLastChange = null;
	private boolean iMultipleMajors = false;
	private boolean iSessionHasSnapshotData = false;
	
	private AcademicAreaInterface iAcademicArea;
	private TreeSet<MajorInterface> iMajors;
	private DepartmentInterface iDept;
	private TreeSet<CurriculumClassificationInterface> iClasf;
	private TreeSet<CourseInterface> iCourses;
	
	public CurriculumInterface() {}
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }
	
	public String getLastChange() { return iLastChange; }
	public void setLastChange(String lastChange) { iLastChange = lastChange; }
	public boolean hasLastChange() { return iLastChange != null && !iLastChange.isEmpty(); }
	
	public AcademicAreaInterface getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicAreaInterface area) { iAcademicArea = area; }
	
	public boolean isMultipleMajors() { return iMultipleMajors; }
	public void setMultipleMajors(boolean multipleMajors) { iMultipleMajors = multipleMajors; }
	
	public boolean isSessionHasSnapshotData() { return(iSessionHasSnapshotData); }
	public void setSessionHasSnapshotData(boolean sessionHasSnapshotData) { iSessionHasSnapshotData = sessionHasSnapshotData; }

	
	public TreeSet<MajorInterface> getMajors() { return iMajors; }
	public boolean hasMajors() { return iMajors != null && !iMajors.isEmpty(); }
	public void addMajor(MajorInterface major) { 
		if (iMajors == null) iMajors = new TreeSet<MajorInterface>();
		iMajors.add(major);
	}
	public String getMajorNames(String delim) {
		String ret = "";
		if (iMajors == null) return ret;
		for (MajorInterface major: iMajors) {
			if (!ret.isEmpty()) ret += delim;
			ret += major.getName();
		}
		return ret;
	}
	public String getMajorCodes(String delim) {
		String ret = "";
		if (iMajors == null) return ret;
		for (MajorInterface major: iMajors) {
			if (!ret.isEmpty()) ret += delim;
			ret += major.getCode();
		}
		return ret;
	}
	
	public String getCodeMajorNames(String delim) {
		String ret = "";
		if (iMajors == null) return ret;
		for (MajorInterface major: iMajors) {
			if (!ret.isEmpty()) ret += delim;
			ret += major.getCode() + " - " + major.getName();
		}
		return ret;
	}
	
	public DepartmentInterface getDepartment() { return iDept; }
	public void setDepartment(DepartmentInterface dept) { iDept = dept; }
	
	public TreeSet<CurriculumClassificationInterface> getClassifications() { return iClasf; }
	public boolean hasClassifications() { return iClasf != null; }
	public void addClassification(CurriculumClassificationInterface clasf) {
		if (iClasf == null) iClasf = new TreeSet<CurriculumClassificationInterface>();
		iClasf.add(clasf);
	}
	public CurriculumClassificationInterface getClassification(Long academicClassificationId) {
		if (iClasf == null) return null;
		for (CurriculumClassificationInterface clasf: iClasf) {
			if (clasf.getAcademicClassification().getId().equals(academicClassificationId))
				return clasf;
		}
		return null;
	}
	public CurriculumClassificationInterface getClassification(String academicClassificationCode) {
		if (iClasf == null) return null;
		for (CurriculumClassificationInterface clasf: iClasf) {
			if (clasf.getAcademicClassification().getCode().equals(academicClassificationCode))
				return clasf;
		}
		return null;
	}
	
	public TreeSet<CourseInterface> getCourses() { return iCourses; }
	public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
	public void addCourse(CourseInterface course) {
		if (iCourses == null) iCourses = new TreeSet<CourseInterface>();
		iCourses.add(course);
	}
	public CourseInterface getCourse(Long courseOfferingId) {
		if (iCourses == null) return null;
		for (CourseInterface course: iCourses)
			if (course.getId().equals(courseOfferingId)) return course;
		return null;
	}
	
	public CourseInterface getCourse(String courseName) {
		if (iCourses == null) return null;
		for (CourseInterface course: iCourses)
			if (course.getCourseName().equals(courseName)) return course;
		return null;
	}

	public Integer getExpected() {
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getExpected() == null ? 0 : c.getExpected());
		return ret;
	}
	
	public Integer getEnrollment() {
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getEnrollment() == null ? 0 : c.getEnrollment());
		return ret;
	}

	public Integer getLastLike() {
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getLastLike() == null ? 0 : c.getLastLike());
		return ret;
	}
	
	public Integer getProjection() {
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getProjection() == null ? 0 : c.getProjection());
		return ret;
	}
	
	public Integer getSnapshotExpected() {
		if (!isSessionHasSnapshotData()) return null;
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getSnapshotExpected() == null ? 0 : c.getSnapshotExpected());
		return ret;
	}

	public Integer getSnapshotProjection() {
		if (!isSessionHasSnapshotData()) return null;
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getSnapshotProjection() == null ? 0 : c.getSnapshotProjection());
		return ret;
	}

	public Integer getRequested() {
		if (!hasClassifications()) return null;
		int ret = 0;
		for (CurriculumClassificationInterface c: getClassifications())
			ret += (c.getRequested() == null ? 0 : c.getRequested());
		return ret;
	}
	
	public String getExpectedString() {
		if (!hasClassifications()) return "?";
		Integer count = getExpected();
		return (count == null ? "N/A" : count.toString());
	}

	public String getSnapshotExpectedString() {
		if (!hasClassifications()) return "?";
		Integer count = getSnapshotExpected();
		return (count == null ? "N/A" : count.toString());
	}

	public String getLastLikeString() {
		if (!hasClassifications()) return "?";
		Integer count = getLastLike();
		return (count == null ? "N/A" : count.toString());
	}

	public String getProjectionString() {
		if (!hasClassifications()) return "?";
		Integer count = getProjection();
		return (count == null ? "N/A" : count.toString());
	}

	public String getSnapshotProjectionString() {
		if (!hasClassifications()) return "?";
		Integer count = getSnapshotProjection();
		return (count == null ? "N/A" : count.toString());
	}

	public String getEnrollmentString() {
		if (!hasClassifications()) return "?";
		Integer count = getEnrollment();
		return (count == null ? "N/A" : count.toString());
	}
	
	public String getRequestedString() {
		if (!hasClassifications()) return "?";
		Integer count = getRequested();
		return (count == null ? "N/A" : count.toString());
	}
	
	public void setEditable(boolean editable) { iEditable = editable; }
	public boolean isEditable() { return iEditable; }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumInterface)) return false;
		return getId().equals(((CurriculumInterface)o).getId());
	}
	
	public int hashCode() {
		return (getId() == null ? 0 : getId().hashCode());
	}
	
	public int compareTo(CurriculumInterface curriculum) {
		int cmp = getAbbv().compareTo(curriculum.getAbbv());
		if (cmp != 0) return cmp;
		return (getId() == null ? new Long(-1) : getId()).compareTo(curriculum.getId() == null ? -1 : curriculum.getId());
	}

	public static class AcademicAreaInterface implements IsSerializable, Comparable<AcademicAreaInterface> {
		private Long iAreaId;
		private String iAreaAbbv, iAreaName;

		public AcademicAreaInterface() {}
		
		public Long getId() { return iAreaId; }
		public void setId(Long id) { iAreaId = id; }
		
		public String getAbbv() { return iAreaAbbv; }
		public void setAbbv(String abbv) { iAreaAbbv = abbv; }
		
		public String getName() { return iAreaName; }
		public void setName(String name) { iAreaName = name; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AcademicAreaInterface)) return false;
			return getId().equals(((AcademicAreaInterface)o).getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(AcademicAreaInterface area) {
			int cmp = getAbbv().compareTo(area.getAbbv());
			if (cmp != 0) return cmp;
			return getId().compareTo(area.getId());
		}
	}
	
	public static class MajorInterface implements IsSerializable, Comparable<MajorInterface> {
		private Long iMajorId;
		private String iMajorCode, iMajorName;

		public MajorInterface() {}
		
		public Long getId() { return iMajorId; }
		public void setId(Long id) { iMajorId = id; }
		
		public String getCode() { return iMajorCode; }
		public void setCode(String code) { iMajorCode = code; }
		
		public String getName() { return iMajorName; }
		public void setName(String name) { iMajorName = name; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MajorInterface)) return false;
			return getId().equals(((MajorInterface)o).getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(MajorInterface major) {
			if (getCode() != null) {
				int cmp = getCode().compareTo(major.getCode());
				if (cmp != 0) return cmp;
			}
			return getId().compareTo(major.getId());
		}
	}
	
	public static class DepartmentInterface implements IsSerializable, Comparable<DepartmentInterface> {
		private Long iDeptId;
		private String iDeptCode, iDeptAbbv, iDeptName;

		public DepartmentInterface() {}
		
		public Long getId() { return iDeptId; }
		public void setId(Long id) { iDeptId = id; }
		
		public String getCode() { return iDeptCode; }
		public void setCode(String code) { iDeptCode = code; }
		
		public String getAbbv() { return iDeptAbbv; }
		public void setAbbv(String abbv) { iDeptAbbv = abbv; }

		public String getName() { return iDeptName; }
		public void setName(String name) { iDeptName = name; }
		
		public String getLabel() { return iDeptCode + " - " + iDeptName; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof DepartmentInterface)) return false;
			return getId().equals(((DepartmentInterface)o).getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(DepartmentInterface dept) {
			int cmp = getLabel().compareTo(dept.getLabel());
			if (cmp != 0) return cmp;
			return getId().compareTo(dept.getId());
		}
	}
	
	public static class AcademicClassificationInterface implements IsSerializable, Comparable<AcademicClassificationInterface> {
		private Long iClasfId;
		private String iClasfCode, iClasfName;
		
		public AcademicClassificationInterface() {}
		
		public Long getId() { return iClasfId; }
		public void setId(Long id) { iClasfId = id; }
		
		public String getCode() { return iClasfCode; }
		public void setCode(String code) { iClasfCode = code; }
		
		public String getName() { return iClasfName; }
		public void setName(String name) { iClasfName = name; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AcademicClassificationInterface)) return false;
			return getId().equals(((AcademicClassificationInterface)o).getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(AcademicClassificationInterface clasf) {
			int cmp = getCode().compareTo(clasf.getCode());
			if (cmp != 0) return cmp;
			cmp = getName().compareTo(clasf.getName());
			if (cmp != 0) return cmp;
			return getId().compareTo(clasf.getId());
		}
	}
	
	public static class CurriculumClassificationInterface implements IsSerializable, Comparable<CurriculumClassificationInterface> {
		private Long iCurriculumId, iClasfId;
		private String iName;
		private Integer iNrStudents = null, iEnrollment = null, iLastLike = null, iProjection = null, iRequested = null, iSnapshotProjection = null, iSnapshotNrStudents = null;
		private AcademicClassificationInterface iClasf;
		private TreeSet<CurriculumCourseInterface> iCourses = null;
		private boolean iSessionHasSnapshotData = false;
		
		public CurriculumClassificationInterface() {}
		
		public Long getId() { return iClasfId; }
		public void setId(Long id) { iClasfId = id; }
		
		public Long getCurriculumId() { return iCurriculumId; }
		public void setCurriculumId(Long curriculumId) { iCurriculumId = curriculumId; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Integer getExpected() { return iNrStudents; }
		public void setExpected(Integer nrStudents) { iNrStudents = nrStudents; }
		
		public Integer getSnapshotExpected() { return iSnapshotNrStudents; }
		public void setSnapshotExpected(Integer snapshotNrStudents) { iSnapshotNrStudents = snapshotNrStudents; }

		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		
		public Integer getRequested() { return iRequested; }
		public void setRequested(Integer requested) { iRequested = requested; }

		public Integer getLastLike() { return iLastLike; }
		public void setLastLike(Integer lastLike) { iLastLike = lastLike; }
		
		public Integer getProjection() { return iProjection; }
		public void setProjection(Integer projection) { iProjection = projection; }

		public Integer getSnapshotProjection() { return iSnapshotProjection; }
		public void setSnapshotProjection(Integer snapshotProjection) { iSnapshotProjection = snapshotProjection; }

		public AcademicClassificationInterface getAcademicClassification() { return iClasf; }
		public void setAcademicClassification(AcademicClassificationInterface clasf) { iClasf = clasf; }
		
		public boolean isSessionHasSnapshotData() { return(iSessionHasSnapshotData); }
		public void setSessionHasSnapshotData(boolean sessionHasSnapshotData) { iSessionHasSnapshotData = sessionHasSnapshotData; }
		
		public TreeSet<CurriculumCourseInterface> getCourses() { return iCourses; }
		public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
		public void addCourse(CurriculumCourseInterface course) { 
			if (iCourses == null) iCourses = new TreeSet<CurriculumCourseInterface>();
			iCourses.add(course);
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CurriculumClassificationInterface)) return false;
			return getId().equals(((CurriculumClassificationInterface)o).getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(CurriculumClassificationInterface clasf) {
			return getAcademicClassification().compareTo(clasf.getAcademicClassification());
		}
	}
	
	public static class CourseInterface implements IsSerializable, Comparable<CourseInterface> {
		private Long iCourseId;
		private String iCourseName;
		private List<CurriculumCourseInterface> iCurriculumCourses;
		private TreeSet<CurriculumCourseGroupInterface> iGroups;
		
		public CourseInterface() {}
		
		public Long getId() { return iCourseId; }
		public void setId(Long id) { iCourseId = id; }
		
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String courseName) { iCourseName = courseName; }
		
		public boolean hasCurriculumCourses() {
			return iCurriculumCourses != null && !iCurriculumCourses.isEmpty();
		}
		public void setCurriculumCourse(int idx, CurriculumCourseInterface course) {
			if (iCurriculumCourses == null) iCurriculumCourses = new ArrayList<CurriculumCourseInterface>();
			if (idx < iCurriculumCourses.size()) {
				iCurriculumCourses.set(idx, course);
			} else {
				while (iCurriculumCourses.size()<idx) iCurriculumCourses.add(null);
				iCurriculumCourses.add(course);
			}
		}
		public CurriculumCourseInterface getCurriculumCourse(int idx) {
			if (iCurriculumCourses == null || idx >= iCurriculumCourses.size()) return null;
			return iCurriculumCourses.get(idx);
		}
		public List<CurriculumCourseInterface> getCurriculumCourses() {
			return iCurriculumCourses;
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CourseInterface)) return false;
			return getId().equals(((CourseInterface)o).getId());
		}
		
		public boolean hasGroups() {
			return iGroups != null && !iGroups.isEmpty();
		}
		public void addGroup(CurriculumCourseGroupInterface group) {
			if (iGroups == null) iGroups = new TreeSet<CurriculumCourseGroupInterface>();
			iGroups.add(group);
		}
		public TreeSet<CurriculumCourseGroupInterface> getGroups() {
			return iGroups;
		}
		public CurriculumCourseGroupInterface getGroup(String name) {
			if (iGroups == null) return null;
			for (CurriculumCourseGroupInterface group: iGroups)
				if (group.getName().equals(name)) return group;
			return null;
		}
		public CurriculumCourseGroupInterface getGroup(Long groupId) {
			if (iGroups == null) return null;
			for (CurriculumCourseGroupInterface group: iGroups)
				if (group.getId().equals(groupId)) return group;
			return null;
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		private int firstClassification() {
			if (!hasCurriculumCourses()) return -1;
			for (int i = 0; i < iCurriculumCourses.size(); i++) {
				CurriculumCourseInterface c = iCurriculumCourses.get(i);
				if (c == null) continue;
				if (c.getDisplayedShare() > 0.0f) return i;
			}
			return iCurriculumCourses.size();
		}
		
		private int highestClassification() {
			if (!hasCurriculumCourses()) return -1;
			int best = iCurriculumCourses.size();
			double bestShare = -1.0f;
			for (int i = 0; i < iCurriculumCourses.size(); i++) {
				CurriculumCourseInterface c = iCurriculumCourses.get(i);
				if (c == null) continue;
				if (c.getDisplayedShare() > bestShare) {
					bestShare = c.getDisplayedShare(); best = i;
				}
			}
			return best;
		}
		
		public boolean hasDefaultShare() {
			if (iCurriculumCourses == null) return false;
			for (CurriculumCourseInterface c: iCurriculumCourses)
				if (c != null && c.getDefaultShare() != null)
					return true;
			return false;
		}
		
		public boolean hasDefaultSnapshotShare() {
			if (iCurriculumCourses == null) return false;
			for (CurriculumCourseInterface c: iCurriculumCourses) {
				if (!c.isSessionHasSnapshotData()) {
					return(false);
				}
				if (c != null && c.getDefaultShare() != null)
					return true;
			}
			return false;
		}

		public boolean hasTemplate() { 
			if (iCurriculumCourses == null) return false;
			for (CurriculumCourseInterface c: iCurriculumCourses)
				if (c != null && c.hasTemplates())
					return true;
			return false;
		}
		public String getTemplate() {
			if (iCurriculumCourses == null) return null;
			TreeSet<String> templates = new TreeSet<String>();
			for (CurriculumCourseInterface c: iCurriculumCourses)
				if (c != null && c.hasTemplates())
					templates.addAll(c.getTemplates());
			if (templates.isEmpty()) return null;
			String ret = "";
			for (String template: templates) {
				ret += (ret.isEmpty() ? "" : ", ") + template;
			}
			return ret;
		}

		public int compareTo(CourseInterface course) {
			if (hasCurriculumCourses()) {
				int a = highestClassification();
				int b = course.highestClassification();
				if (a < b) return -1;
				if (a > b) return 1;
				if (a <= iCurriculumCourses.size()) {
					CurriculumCourseInterface c = getCurriculumCourse(a);
					CurriculumCourseInterface d = course.getCurriculumCourse(a);
					int cmp = Double.compare(d == null ? 0f : d.getDisplayedShare(), c == null ? 0f : c.getDisplayedShare());
					if (cmp != 0) return cmp;
				}
				a = firstClassification();
				b = course.firstClassification();
				if (a < b) return -1;
				if (a > b) return 1;
				while (a <= iCurriculumCourses.size()) {
					CurriculumCourseInterface c = getCurriculumCourse(a);
					CurriculumCourseInterface d = course.getCurriculumCourse(a);
					int cmp = Double.compare(d == null ? 0f : d.getDisplayedShare(), c == null ? 0f : c.getDisplayedShare());
					if (cmp != 0) return cmp;
					a++;
				}
			}
			return getCourseName().compareTo(course.getCourseName());
		}
		
	}
	
	public static class CurriculumCourseInterface implements IsSerializable, Comparable<CurriculumCourseInterface> {
		private Long iId, iCourseId, iClasfId;
		private String iCourseName;
		private Float iShare = null, iDefaultShare = null;
		private Float iSnapshotShare = null, iDefaultSnapshotShare = null;
		private Integer iLastLike = null, iEnrollment = null, iProjection = null, iRequested = null, iSnapshotProjection = null;
		private TreeSet<String> iTemplates = null;
		private boolean iSessionHasSnapshotData = false;
		
		public CurriculumCourseInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public Long getCourseOfferingId() { return iCourseId; }
		public void setCourseOfferingId(Long courseId) { iCourseId = courseId; }
		
		public Long getCurriculumClassificationId() { return iClasfId; }
		public void setCurriculumClassificationId(Long clasfId) { iClasfId = clasfId; }
		
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String courseName) { iCourseName = courseName; }
		
		public float getShare() { return iShare == null ? 0f : iShare.floatValue(); }
		public boolean hasShare() { return iShare != null; }
		public void setShare(Float share) { iShare = share; }
		
		public Float getSnapshotShare() { return iSnapshotShare; }
		public boolean hasSnapshotShare() { return iSnapshotShare != null; }
		public void setSnapshotShare(Float snapshotShare) { iSnapshotShare = snapshotShare; }

		public Float getDefaultShare() { return iDefaultShare; }
		public void setDefaultShare(Float share) { iDefaultShare = share; }
		
		public Float getDefaultSnapshotShare() { return iDefaultSnapshotShare; }
		public void setDefaultSnapshotShare(Float snapshotShare) { iDefaultSnapshotShare = snapshotShare; }

		public TreeSet<String> getTemplates() { return iTemplates; }
		public boolean hasTemplates() { return iTemplates != null && !iTemplates.isEmpty(); }
		public void addTemplate(String template) {
			if (iTemplates == null) iTemplates = new TreeSet<String>();
			iTemplates.add(template);
		}
		
		public boolean isSessionHasSnapshotData() { return(iSessionHasSnapshotData); }
		public void setSessionHasSnapshotData(boolean sessionHasSnapshotData) { iSessionHasSnapshotData = sessionHasSnapshotData; }
		
		public float getDisplayedShare() {
			return iShare != null ? iShare.floatValue() :  iDefaultShare != null ? iDefaultShare.floatValue() : 0.0f;
		}

		public Integer getLastLike() { return iLastLike; }
		public void setLastLike(Integer lastLike) { iLastLike = lastLike; }
		
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		
		public Integer getRequested() { return iRequested; }
		public void setRequested(Integer requested) { iRequested = requested; }
		
		public Integer getProjection() { return iProjection; }
		public void setProjection(Integer projection) { iProjection = projection; }

		public Integer getSnapshotProjection() { return iSnapshotProjection; }
		public void setSnapshotProjection(Integer snapshotProjection) { iSnapshotProjection = snapshotProjection; }

		public boolean equals(Object o) {
			if (o == null || !(o instanceof CurriculumCourseInterface)) return false;
			return getId().equals(((CurriculumCourseInterface)o).getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(CurriculumCourseInterface course) {
			return getCourseName().compareTo(course.getCourseName());
		}
	}
	
	public static class CurriculumCourseGroupInterface implements IsSerializable, Comparable<CurriculumCourseGroupInterface> {
		private Long iId;
		private String iName, iColor;
		private int iType;
		private boolean iEditable = true;
		
		public CurriculumCourseGroupInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public int getType() { return iType; }
		public void setType(int type) { iType = type; }
		
		public boolean isEditable() { return iEditable; }
		public void setEditable(boolean editable) { iEditable = editable; }
		
		public int hashCode() { return iName.hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CurriculumCourseGroupInterface)) return false;
			return getName().equals(((CurriculumCourseGroupInterface)o).getName());
		}
		public int compareTo(CurriculumCourseGroupInterface g) {
			return getName().compareTo(g.getName());
		}
	}
	
	public static class CurriculumStudentsInterface implements IsSerializable {
		private Set<Long> iEnrollment = null;
		private Set<Long> iRequested = null;
		private HashMap<Long, Set<String>> iLastLike = null;
		private HashMap<String, Float> iProjection = null;
		private HashMap<String, Float> iSnapshotProjection = null;
		private boolean iSessionHasSnapshotData = false;
		
		public CurriculumStudentsInterface() {}
		
		public int getEnrollment() {
			return (iEnrollment == null || iEnrollment.isEmpty() ? 0 : iEnrollment.size());
		}		
		public int getLastLike() {
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			return iLastLike.size();
		}
		
		public boolean isSessionHasSnapshotData() { return(iSessionHasSnapshotData); }
		public void setSessionHasSnapshotData(boolean sessionHasSnapshotData) { iSessionHasSnapshotData = sessionHasSnapshotData; }

		public int getProjection() {
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			if (iProjection == null) return iLastLike.size();
			double proj = 0;
			for (Map.Entry<Long, Set<String>> entry: iLastLike.entrySet()) {
				double weight = 1.0;
				int cnt = 0;
				for (String major: entry.getValue()) {
					Float f = iProjection.get(major);
					if (f == null) f = iProjection.get("");
					if (f != null) {
						weight *= f; cnt ++;
					}
				}
				proj += (cnt == 0 ? 1.0f : cnt == 1 ? weight : Math.pow(weight, 1.0 / cnt));
			}
			return (int) Math.round(proj);
		}
		
		public int getSnapshotProjection() {
			if (!iSessionHasSnapshotData) {
				return(0);
			}
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			if (iSnapshotProjection == null) return iLastLike.size();
			double ssProj = 0;
			for (Map.Entry<Long, Set<String>> entry: iLastLike.entrySet()) {
				double weight = 1.0;
				int cnt = 0;
				for (String major: entry.getValue()) {
					Float f = iSnapshotProjection.get(major);
					if (f == null) f = iSnapshotProjection.get("");
					if (f != null) {
						weight *= f; cnt ++;
					}
				}
				ssProj += (cnt == 0 ? 1.0f : cnt == 1 ? weight : Math.pow(weight, 1.0 / cnt));
			}
			return (int) Math.round(ssProj);
		}

		public int getRequested() {
			return (iRequested == null || iRequested.isEmpty() ? 0 : iRequested.size());
		}
		
		public Set<Long> getEnrolledStudents() {
			return iEnrollment;
		}
		
		public Set<Long> getLastLikeStudents() {
			if (iLastLike == null || iLastLike.isEmpty()) return null;
			return iLastLike.keySet();
		}
		
		public Set<Long> getProjectedStudents() {
			return getLastLikeStudents();
		}
		
		public int countProjectedStudents(Set<Long> students) {
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			if (iProjection == null) return students.size();
			double proj = 0;
			for (Long student: students) {
				Set<String> majors = iLastLike.get(student);
				if (majors == null) continue;
				double weight = 1.0;
				int cnt = 0;
				for (String major: majors) {
					Float f = iProjection.get(major);
					if (f == null) f = iProjection.get("");
					if (f != null)
						weight *= f;
					cnt ++;
				}
				proj += (cnt == 0 ? 1.0f : cnt == 1 ? weight : Math.pow(weight, 1.0 / cnt));
			}
			return (int) Math.round(proj);
		}
		
		public int countSnapshotProjectedStudents(Set<Long> students) {
			if (!iSessionHasSnapshotData) {
				return(0);
			}
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			if (iSnapshotProjection == null) return students.size();
			double ssproj = 0;
			for (Long student: students) {
				Set<String> majors = iLastLike.get(student);
				if (majors == null) continue;
				double weight = 1.0;
				int cnt = 0;
				for (String major: majors) {
					Float f = iSnapshotProjection.get(major);
					if (f == null) f = iSnapshotProjection.get("");
					if (f != null)
						weight *= f;
					cnt ++;
				}
				ssproj += (cnt == 0 ? 1.0f : cnt == 1 ? weight : Math.pow(weight, 1.0 / cnt));
			}
			return (int) Math.round(ssproj);
		}

		public Set<Long> getRequestedStudents() {
			return iRequested;
		}
		
		public void setEnrolledStudents(Set<Long> students) { iEnrollment = students; }
		
		public void setLastLikeStudents(HashMap<String, Set<Long>> students) {
			if (students == null) {
				iLastLike = null;
			} else {
				iLastLike = new HashMap<Long, Set<String>>();
				for (Map.Entry<String, Set<Long>> entry: students.entrySet()) {
					for (Long student: entry.getValue()) {
						Set<String> majors = iLastLike.get(student);
						if (majors == null) {
							majors = new HashSet<String>();
							iLastLike.put(student, majors);
						}
						majors.add(entry.getKey());
					}
				}
			}
		}
		
		public void setProjection(HashMap<String, Float> projection) { iProjection = projection; }
		public void setSnapshotProjection(HashMap<String, Float> snapshotProjection) { iSnapshotProjection = snapshotProjection; }
		
		public void setRequestedStudents(Set<Long> students) { iRequested = students; }
	}
	
	public static class CurriculumFilterRpcRequest extends FilterRpcRequest {
		private static final long serialVersionUID = 1L;
		
		public CurriculumFilterRpcRequest() {}
	}
}
