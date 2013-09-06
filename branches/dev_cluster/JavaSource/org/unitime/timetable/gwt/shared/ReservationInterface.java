/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
	private boolean iEditable = false;
	
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
	
	public List<Config> getConfigs() { return iConfigs; }
	public List<Clazz> getClasses() { return iClasses; }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ReservationInterface)) return false;
		return getId().equals(((ReservationInterface)o).getId());
	}
	public int hashCode() {
		return getId().hashCode();
	}
	
	public int getPriority() {
		if (this instanceof ReservationInterface.IndividualReservation) return 0;
		if (this instanceof ReservationInterface.GroupReservation) return 1;
		if (this instanceof ReservationInterface.CourseReservation) return 2;
		if (this instanceof ReservationInterface.CurriculumReservation) return 3;
		return 4;
	}
	
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
	}
	
	public static class GroupReservation extends ReservationInterface {
		private IdName iGroup;
		
		public GroupReservation() {
			super();
		}
		
		public IdName getGroup() { return iGroup; }
		public void setGroup(IdName group) { iGroup = group; }
		
		public String toString() { return getGroup().toString(); }
	}

	public static class IndividualReservation extends ReservationInterface {
		private List<IdName> iStudents = new ArrayList<IdName>();
		
		public IndividualReservation() {
			super();
		}
		
		public List<IdName> getStudents() { return iStudents; }
		
		public Integer getLimit() { return iStudents.size(); }
		
		public String toString() { return getStudents().toString(); }
	}

	public static class CurriculumReservation extends ReservationInterface {
		private Area iCurriculum;
		
		public CurriculumReservation() {
			super();
		}
		
		public Area getCurriculum() { return iCurriculum; }
		public void setCurriculum(Area curriculum) { iCurriculum = curriculum; }
		
		public String toString() { return getCurriculum().toString(); }

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

		public Config() { super(); }
		
		public List<Subpart> getSubparts() { return iSubparts; }
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
	}

	public static class Clazz extends IdName {
		private Subpart iSubpart = null;
		private Long iParentId = null;
		
		public Clazz() { super(); }
		
		public Long getParentId() { return iParentId; }
		public void setParentId(Long parentId) { iParentId = parentId; }
		public Subpart getSubpart() { return iSubpart; }
		public void setSubpart(Subpart subpart) { iSubpart = subpart; }
	}

	public static class Offering extends IdName {
		private boolean iOffered = true;
		private List<Course> iCourses = new ArrayList<Course>();
		private List<Config> iConfigs = new ArrayList<Config>();
		
		public Offering() { super(); }
		
		public List<Course> getCourses() { return iCourses; }
		public List<Config> getConfigs() { return iConfigs; }
		
		public boolean isOffered() { return iOffered; }
		public void setOffered(boolean offered) { iOffered = offered; }
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
	
	public static class ReservationFilterRpcRequest extends FilterRpcRequest {
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
