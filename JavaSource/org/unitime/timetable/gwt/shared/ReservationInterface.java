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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public abstract class ReservationInterface implements IsSerializable, Comparable<ReservationInterface> {
	private Long iId;
	private Offering iOffering;
	private List<Config> iConfigs = new ArrayList<Config>();
	private List<Clazz> iClasses = new ArrayList<Clazz>();
	private Integer iLimit = null, iEnrollment = null, iLastLike = null, iProjection = null;
	private Date iExpirationDate;
	private boolean iEditable = false, iExpired = false;
	private boolean iOverride = false, iAlwaysExpired = false, iAllowOverlaps = false, iOverLimit = false, iMustBeUsed = false;
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	public Offering getOffering() { return iOffering; }
	public void setOffering(Offering offering) { iOffering = offering; }
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }
	public Integer getLastLike() { return iLastLike; }
	public void setLastLike(Integer lastLike) { iLastLike = lastLike; }
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
	public Integer getProjection() { return iProjection; }
	public void setProjection(Integer projection) { iProjection = projection; }
	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date d) { iExpirationDate = d; }
	public void setEditable(boolean editable) { iEditable = editable; }
	public boolean isEditable() { return iEditable; }
	public void setExpired(boolean expired) { iExpired = expired; }
	public boolean isExpired() { return iExpired; }
	
	public boolean isOverride() { return iOverride; }
	public void setOverride(boolean override) { iOverride = override; }
	public boolean isAlwaysExpired() { return iAlwaysExpired; }
	public void setAlwaysExpired(boolean alwaysExpired) { iAlwaysExpired = alwaysExpired; }
	public boolean isAllowOverlaps() { return iAllowOverlaps; }
	public void setAllowOverlaps(boolean allowOverlaps) { iAllowOverlaps = allowOverlaps; }
	public boolean isOverLimit() { return iOverLimit; }
	public void setOverLimit(boolean overLimit) { iOverLimit = overLimit; }
	public boolean isMustBeUsed() { return iMustBeUsed; }
	public void setMustBeUsed(boolean mustBeUsed) { iMustBeUsed = mustBeUsed; }
	
	public List<Config> getConfigs() { return iConfigs; }
	public List<Clazz> getClasses() { return iClasses; }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ReservationInterface)) return false;
		return getId().equals(((ReservationInterface)o).getId());
	}
	public int hashCode() {
		return getId().hashCode();
	}
	
	public abstract int getPriority();
	
	public int compareTo(ReservationInterface r2) {
		int cmp = getOffering().getAbbv().compareTo(r2.getOffering().getAbbv());
		if (cmp != 0) return cmp;
		cmp = new Integer(getPriority()).compareTo(r2.getPriority());
		if (cmp != 0) return cmp;
		cmp = this.toString().compareTo(r2.toString());
		if (cmp != 0) return cmp;
		return this.getId().compareTo(r2.getId());
	}
	
	public static class CourseReservation extends ReservationInterface {
		private Course iCourse;
		
		public CourseReservation() {
			super();
		}
		
		public Course getCourse() { return iCourse; }
		public void setCourse(Course course) { iCourse = course; }
		
		public Integer getLimit() { return (iCourse == null ? null : iCourse.getLimit()); }
		
		public String toString() { return getCourse().toString(); }
		
		@Override
		public int getPriority() { return 400; }
	}
	
	public static class GroupReservation extends ReservationInterface {
		private IdName iGroup;
		
		public GroupReservation() {
			super();
		}
		
		public IdName getGroup() { return iGroup; }
		public void setGroup(IdName group) { iGroup = group; }
		
		public String toString() { return getGroup().toString(); }
		
		@Override
		public int getPriority() { return isOverride() ? 250 : 200; }
	}

	public static class IndividualReservation extends ReservationInterface {
		private List<IdName> iStudents = new ArrayList<IdName>();
		
		public IndividualReservation() {
			super();
		}
		
		public List<IdName> getStudents() { return iStudents; }
		
		public Integer getLimit() { return iStudents.size(); }
		
		public String toString() {
			String ret = "";
			for (IdName s: iStudents) {
				if (!ret.isEmpty()) ret += "\n";
				ret += s.getName();
			}
			return ret;
		}
		
		@Override
		public int getPriority() { return isOverride() ? 150 : 100; }
	}
	
	public static class OverrideReservation extends IndividualReservation {
		private List<IdName> iStudents = new ArrayList<IdName>();
		private OverrideType iType = null;
		
		public OverrideReservation() {
			super();
		}
		
		public OverrideReservation(OverrideType type) {
			super();
			setType(type);
		}
		
		public List<IdName> getStudents() { return iStudents; }
		
		public Integer getLimit() { return iStudents.size(); }
		
		public OverrideType getType() { return iType; }
		
		public void setType(OverrideType type) { iType = type; }
		
		@Override
		public boolean isExpired() {
			return (getType().isCanHaveExpirationDate() ? super.isExpired() : getType().isExpired());
		}
		
		@Override
		public Date getExpirationDate() {
			return (getType().isCanHaveExpirationDate() ? super.getExpirationDate() : null);
		}
		
		@Override
		public int getPriority() { return 600 + (getType() == null ? 0 : 1 + getType().ordinal()); }
	}

	public static class CurriculumReservation extends ReservationInterface {
		private Area iCurriculum;
		
		public CurriculumReservation() {
			super();
		}
		
		public Area getCurriculum() { return iCurriculum; }
		public void setCurriculum(Area curriculum) { iCurriculum = curriculum; }
		
		public String toString() { return getCurriculum().toString(); }
		
		@Override
		public int getPriority() { return 300; }
	}
	
	public static class LCReservation extends ReservationInterface {
		private IdName iGroup;
		private Course iCourse;
		
		public LCReservation() {
			super();
		}
		
		public IdName getGroup() { return iGroup; }
		public void setGroup(IdName group) { iGroup = group; }
		
		public Course getCourse() { return iCourse; }
		public void setCourse(Course course) { iCourse = course; }
		
		public String toString() { return getCourse() + " " + getGroup(); }
		
		@Override
		public int getPriority() { return 500; }
	}

	public static class IdName implements IsSerializable, Comparable<IdName> {
		private Long iId;
		private String iAbbv;
		private String iName;
		private Integer iLimit = null;
		
		public IdName() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getAbbv() { return iAbbv; }
		public void setAbbv(String abbv) { iAbbv = abbv; }
		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdName)) return false;
			return getId().equals(((IdName)o).getId());
		}
		public int hashCode() {
			return getId().hashCode();
		}
		public String toString() { return ((iAbbv == null || iAbbv.isEmpty() ? "" : iAbbv) + (iName == null || iName.isEmpty() ? "" : " " + iName)).trim(); }
		
		public int compareTo(IdName other) {
			int cmp = (getAbbv() == null ? "" : getAbbv()).compareTo(other.getAbbv() == null ? "" : other.getAbbv());
			if (cmp != 0) return cmp;
			cmp = (getName() == null ? "" : getName()).compareTo(other.getName() == null ? "" : other.getName());
			if (cmp != 0) return cmp;
			return getId().compareTo(other.getId());
		}
	}
	
	public static class Course extends IdName {
		private boolean iControl = true;
		
		public Course() { super(); }
		
		public boolean isControl() { return iControl; }
		public void setControl(boolean control) { iControl = control; }
	}
	
	public static class Config extends IdName {
		private List<Subpart> iSubparts = new ArrayList<Subpart>();
		private String iInstructionalMethod = null;

		public Config() { super(); }
		
		public List<Subpart> getSubparts() { return iSubparts; }
		
		public boolean hasInstructionalMethod() { return iInstructionalMethod != null && !iInstructionalMethod.isEmpty(); }
		public String getInstructionalMethod() { return iInstructionalMethod; }
		public void setInstructionalMethod(String instructionalMethod) { iInstructionalMethod = instructionalMethod; }
		
		public int getIndent(Subpart subpart) {
			Subpart parent = getSubpart(subpart.getParentId());
			return parent == null ? 0 : 1 + getIndent(parent);
		}
		
		public String getIndent(Subpart subpart, String indent) {
			Subpart parent = getSubpart(subpart.getParentId());
			return parent == null ? "" : indent + getIndent(parent, indent);
		}
		
		public Subpart getSubpart(Long id) {
			if (id == null) return null;
			for (Subpart subpart: iSubparts)
				if (id.equals(subpart.getId())) return subpart;
			return null;
		}
		
		public boolean isParent(Subpart a, Subpart b) {
			Subpart parent = getSubpart(b.getParentId());
			return parent != null && (a.equals(parent) || isParent(a, parent));
		}
		
		public boolean isParent(Clazz a, Clazz b) {
			Subpart parentSubpart = getSubpart(b.getSubpart().getParentId());
			Clazz parent = (parentSubpart == null ? null : parentSubpart.getClazz(b.getParentId()));
			return parent != null && (a.equals(parent) || isParent(a, parent));
		}
		
		public Clazz getParentClazz(Clazz clazz, Subpart parentSubpart) {
			Subpart subpart = getSubpart(clazz.getSubpart().getParentId());
			if (subpart == null) return null;
			Clazz parent = subpart.getClazz(clazz.getParentId());
			if (parent == null) return null;
			if (subpart.equals(parentSubpart)) return parent;
			return getParentClazz(parent, parentSubpart);
		}
		
		public int[] countChildClasses(Subpart parent, Subpart child) {
			Map<Long, Integer> counts = new HashMap<Long, Integer>();
			for (Clazz clazz: child.getClasses()) {
				Clazz pc = getParentClazz(clazz, parent);
				if (pc == null) continue;
				Integer count = counts.get(pc.getId());
				counts.put(pc.getId(), count == null ? 1 : 1 + count.intValue());
			}
			int max = 0, min = Integer.MAX_VALUE;
			for (Integer count: counts.values()) {
				if (max < count) max = count;
				if (min > count) min = count;
			}
			if (min <= max) return new int[] {min, max};
			return null;
		}
	}

	public static class Subpart extends IdName {
		private Long iParentId = null;
		private List<Clazz> iClasses = new ArrayList<Clazz>();
		private Config iConfig;
		
		public Subpart() { super(); }
		
		public Config getConfig() { return iConfig; }
		public void setConfig(Config config) { iConfig = config; }
		public List<Clazz> getClasses() { return iClasses; }
		public Long getParentId() { return iParentId; }
		public void setParentId(Long parentId) { iParentId = parentId; }
		
		public Clazz getClazz(Long id) {
			if (id == null) return null;
			for (Clazz clazz: iClasses)
				if (id.equals(clazz.getId())) return clazz;
			return null;
		}
	}

	public static class Clazz extends IdName {
		private Subpart iSubpart = null;
		private Long iParentId = null;
		private String iExternalId = null;
		private boolean iCancelled = false;
		private String iTime = null;
		private String iDate = null;
		private String iRoom = null;
		private String iInstructor = null;
		private Integer iEnrollment = null;
		
		public Clazz() { super(); }
		
		public Long getParentId() { return iParentId; }
		public void setParentId(Long parentId) { iParentId = parentId; }
		public Subpart getSubpart() { return iSubpart; }
		public void setSubpart(Subpart subpart) { iSubpart = subpart; }
		
		public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		
		public boolean isCancelled() { return iCancelled; }
		public void setCancelled(boolean cancelled) { iCancelled = cancelled; }
		
		public boolean hasTime() { return iTime != null && !iTime.isEmpty(); }
		public String getTime() { return iTime; }
		public void setTime(String time) { iTime = time; }

		public boolean hasDate() { return iDate != null && !iDate.isEmpty(); }
		public String getDate() { return iDate; }
		public void setDate(String date) { iDate = date; }

		public boolean hasRoom() { return iRoom != null && !iRoom.isEmpty(); }
		public String getRoom() { return iRoom; }
		public void setRoom(String room) { iRoom = room; }

		public boolean hasInstructor() { return iInstructor != null && !iInstructor.isEmpty(); }
		public String getInstructor() { return iInstructor; }
		public void setInstructor(String instructor) { iInstructor = instructor; }
		
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	}

	public static class Offering extends IdName {
		private boolean iOffered = true;
		private boolean iNeedUnlock = false;
		private List<Course> iCourses = new ArrayList<Course>();
		private List<Config> iConfigs = new ArrayList<Config>();
		
		public Offering() { super(); }
		
		public List<Course> getCourses() { return iCourses; }
		public List<Config> getConfigs() { return iConfigs; }
		
		public boolean isOffered() { return iOffered; }
		public void setOffered(boolean offered) { iOffered = offered; }
		
		public boolean isUnlockNeeded() { return iNeedUnlock; }
		public void setUnlockNeeded(boolean unlockNeeded) { iNeedUnlock = unlockNeeded; }
		
		public Course getControllingCourse() {
			for (Course course: iCourses)
				if (course.isControl())
					return course;
			return null;
		}
	}
	
	public static class Area extends IdName {
		private List<IdName> iClassifications = new ArrayList<IdName>();
		private List<IdName> iMajors = new ArrayList<IdName>();

		public Area() { super(); }
		
		public List<IdName> getClassifications() { return iClassifications; }
		public List<IdName> getMajors() { return iMajors; }
		
		public String toString() { return super.toString() + " " + getClassifications().toString() + " " + getMajors().toString(); }
	}
	
	public static class Curriculum extends IdName {
		private List<IdName> iClassifications = new ArrayList<IdName>();
		private List<IdName> iMajors = new ArrayList<IdName>();
		private IdName iArea = null;
		private Integer iLimit = null;
		
		public Curriculum() { super(); }

		public List<IdName> getClassifications() { return iClassifications; }
		public List<IdName> getMajors() { return iMajors; }
		
		public IdName getArea() { return iArea; }
		public void setArea(IdName area) { iArea = area; }

		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }

		public String toString() { return super.toString() + " " + getArea().toString() + " " + getClassifications().toString() + " " + getMajors().toString(); }
	}
	
	public static enum OverrideType implements IsSerializable {
		AllowTimeConflict("time-cnflt", false, true, false, true, true),
		AllowOverLimit("closed", false, false, true, true, true),
		AllowOverLimitTimeConflict("time-limit-cnflt", false, true, true, true, true),
		ClassificationOverride("class", false, false, false, true, false),
		CoReqOverride("co-req", false, false, false, true, false),
		CohortOverride("cohort", false, false, false, true, false),
		CollegeRestrictionOverride("college", false, false, false, true, false),
		DegreeOverride("degree", false, false, false, true, false),
		DepartmentPermission("dpt-permit", true, false, false, true, false),
		HonorsPermission("honors", false, false, false, true, false),
		InstructorPermission("inst-permt", true, false, false, true, false),
		LevelOverride("level", false, false, false, true, false),
		MajorOverride("major", false, false, false, true, false),
		PreReqOverride("pre-req", false, false, false, true, false),
		Program("program", false, false, false, true, false),
		Other("other", false, false, false, true, true),
		;
		
		String iReference;
		boolean iMustBeUsed = false, iAllowTimeConflict = false, iAllowOverLimit = false, iEditable = false;
		Boolean iExpired = false;
		OverrideType(String reference, boolean mustBeUsed, boolean timeConflict, boolean overLimit, Boolean expired, boolean editable) {
			iReference = reference;
			iMustBeUsed = mustBeUsed; iAllowTimeConflict = timeConflict; iAllowOverLimit = overLimit; iExpired = expired;
			iEditable = editable;
		}
		
		public String getReference() { return iReference; }
		public boolean isMustBeUsed() { return iMustBeUsed; }
		public boolean isAllowTimeConflict() { return iAllowTimeConflict; }
		public boolean isAllowOverLimit() { return iAllowOverLimit; }
		public Boolean isExpired() { return iExpired; }
		public boolean isCanHaveExpirationDate() { return iExpired == null; }
		public boolean isEditable() { return iEditable; }
	}
	
	public static class ReservationFilterRpcRequest extends FilterRpcRequest {
		private static final long serialVersionUID = 1L;
		
		public ReservationFilterRpcRequest() {}
	}
	
	public static class DefaultExpirationDates implements GwtRpcResponse {
		private Map<String, Date> iExpirations = new HashMap<String, Date>();
		
		public DefaultExpirationDates() {}
		
		public Date getExpirationDate(String type) { return iExpirations.get(type); }
		public boolean hasExpirationDate(String type) { return getExpirationDate(type) != null; }
		public void setExpirationDate(String type, Date date) { 
			if (date == null)
				iExpirations.remove(type);
			else
				iExpirations.put(type, date);
		}
		
		public String toString() {
			return iExpirations.toString();
		}
	}
	
	public static class ReservationDefaultExpirationDatesRpcRequest implements GwtRpcRequest<DefaultExpirationDates> {
		private Long iSessionId = null;
		
		public ReservationDefaultExpirationDatesRpcRequest() {}
		
		public ReservationDefaultExpirationDatesRpcRequest(Long sessionId) { iSessionId = sessionId; }
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public String toString() {
			return (iSessionId == null ? "null" : iSessionId.toString());
		}
	}
}
