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
	
	public TreeSet<CourseInterface> getCourses() { return iCourses; }
	public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
	public void addCourse(CourseInterface course) {
		if (iCourses == null) iCourses = new TreeSet<CourseInterface>();
		iCourses.add(course);
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
		return getId().compareTo(curriculum.getId());
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
		private Integer iNrStudents = null, iEnrollment = null, iLastLike = null, iProjection = null, iRequested = null;
		private AcademicClassificationInterface iClasf;
		private TreeSet<CurriculumCourseInterface> iCourses = null;
		
		public CurriculumClassificationInterface() {}
		
		public Long getId() { return iClasfId; }
		public void setId(Long id) { iClasfId = id; }
		
		public Long getCurriculumId() { return iCurriculumId; }
		public void setCurriculumId(Long curriculumId) { iCurriculumId = curriculumId; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Integer getExpected() { return iNrStudents; }
		public void setExpected(Integer nrStudents) { iNrStudents = nrStudents; }
		
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		
		public Integer getRequested() { return iRequested; }
		public void setRequested(Integer requested) { iRequested = requested; }

		public Integer getLastLike() { return iLastLike; }
		public void setLastLike(Integer lastLike) { iLastLike = lastLike; }
		
		public Integer getProjection() { return iProjection; }
		public void setProjection(Integer projection) { iProjection = projection; }

		public AcademicClassificationInterface getAcademicClassification() { return iClasf; }
		public void setAcademicClassification(AcademicClassificationInterface clasf) { iClasf = clasf; }
		
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
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		private int firstClassification() {
			if (!hasCurriculumCourses()) return -1;
			for (int i = 0; i < iCurriculumCourses.size(); i++) {
				CurriculumCourseInterface c = iCurriculumCourses.get(i);
				if (c == null) continue;
				if (c.getShare() > 0.0f) return i;
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
				if (c.getShare() > bestShare) {
					bestShare = c.getShare(); best = i;
				}
			}
			return best;
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
					int cmp = Double.compare(d == null ? 0f : d.getShare(), c == null ? 0f : c.getShare());
					if (cmp != 0) return cmp;
				}
				a = firstClassification();
				b = course.firstClassification();
				if (a < b) return -1;
				if (a > b) return 1;
				while (a <= iCurriculumCourses.size()) {
					CurriculumCourseInterface c = getCurriculumCourse(a);
					CurriculumCourseInterface d = course.getCurriculumCourse(a);
					int cmp = Double.compare(d == null ? 0f : d.getShare(), c == null ? 0f : c.getShare());
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
		private float iShare = 0.0f;
		private Integer iLastLike = null, iEnrollment = null, iProjection = null, iRequested = null;
		
		public CurriculumCourseInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public Long getCourseOfferingId() { return iCourseId; }
		public void setCourseOfferingId(Long courseId) { iCourseId = courseId; }
		
		public Long getCurriculumClassificationId() { return iClasfId; }
		public void setCurriculumClassificationId(Long clasfId) { iClasfId = clasfId; }
		
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String courseName) { iCourseName = courseName; }
		
		public float getShare() { return iShare; }
		public void setShare(float share) { iShare = share; }
		
		public Integer getLastLike() { return iLastLike; }
		public void setLastLike(Integer lastLike) { iLastLike = lastLike; }
		
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		
		public Integer getRequested() { return iRequested; }
		public void setRequested(Integer requested) { iRequested = requested; }
		
		public Integer getProjection() { return iProjection; }
		public void setProjection(Integer projection) { iProjection = projection; }

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
		
		public CurriculumCourseGroupInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public int getType() { return iType; }
		public void setType(int type) { iType = type; }
		
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
		private HashMap<String, Set<Long>> iLastLike = null;
		private HashMap<String, Float> iProjection = null;
		
		public CurriculumStudentsInterface() {}
		
		public int getEnrollment() {
			return (iEnrollment == null || iEnrollment.isEmpty() ? 0 : iEnrollment.size());
		}		
		public int getLastLike() {
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			int lastLike = 0;
			for (Map.Entry<String, Set<Long>> entry: iLastLike.entrySet())
				lastLike += entry.getValue().size();
			return lastLike;
		}
		
		public int getProjection() {
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			float proj = 0;
			for (Map.Entry<String, Set<Long>> entry: iLastLike.entrySet()) {
				Float f = (iProjection == null ? null : iProjection.get(entry.getKey()));
				if (f == null && iProjection != null) f = iProjection.get("");
				proj += (f == null ? 1.0f : f) * entry.getValue().size();
			}
			return Math.round(proj);
		}
		
		public int getRequested() {
			return (iRequested == null || iRequested.isEmpty() ? 0 : iRequested.size());
		}
		
		public Set<Long> getEnrolledStudents() {
			return iEnrollment;
		}
		
		public Set<Long> getLastLikeStudents() {
			if (iLastLike == null || iLastLike.isEmpty()) return null;
			if (iLastLike.size() == 1) return iLastLike.values().iterator().next();
			Set<Long> lastLike = new HashSet<Long>();
			for (Map.Entry<String, Set<Long>> entry: iLastLike.entrySet())
				lastLike.addAll(entry.getValue());
			return lastLike;
		}
		
		public Set<Long> getProjectedStudents() {
			return getLastLikeStudents();
		}
		
		public int countProjectedStudents(Set<Long> students) {
			if (iLastLike == null || iLastLike.isEmpty()) return 0;
			float proj = 0;
			for (Map.Entry<String, Set<Long>> entry: iLastLike.entrySet()) {
				int share = 0;
				for (Long student: entry.getValue())
					if (students.contains(student)) share++;
				if (share > 0) {
					Float f = (iProjection == null ? null : iProjection.get(entry.getKey()));
					if (f == null && iProjection != null) f = iProjection.get("");
					proj += (f == null ? 1.0f : f) * share;
				}
			}
			return Math.round(proj);
		}
		
		public Set<Long> getRequestedStudents() {
			return iRequested;
		}
		
		public void setEnrolledStudents(Set<Long> students) { iEnrollment = students; }
		
		public void setLastLikeStudents(HashMap<String, Set<Long>> students) { iLastLike = students; }
		
		public void setProjection(HashMap<String, Float> projection) { iProjection = projection; }
		
		public void setRequestedStudents(Set<Long> students) { iRequested = students; }
	}
	
	public static class CurriculumFilterRpcRequest extends FilterRpcRequest {
		public CurriculumFilterRpcRequest() {}
	}
}
