/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iOrder;
	private Boolean iAllowOverlap;
	private Integer iCredit;

	private CourseDemand iCourseDemand;
	private CourseOffering iCourseOffering;
	private Set<CourseRequestOption> iCourseRequestOptions;
	private Set<ClassWaitList> iClassWaitLists;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_ORD = "order";
	public static String PROP_ALLOW_OVERLAP = "allowOverlap";
	public static String PROP_CREDIT = "credit";

	public BaseCourseRequest() {
		initialize();
	}

	public BaseCourseRequest(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getOrder() { return iOrder; }
	public void setOrder(Integer order) { iOrder = order; }

	public Boolean isAllowOverlap() { return iAllowOverlap; }
	public Boolean getAllowOverlap() { return iAllowOverlap; }
	public void setAllowOverlap(Boolean allowOverlap) { iAllowOverlap = allowOverlap; }

	public Integer getCredit() { return iCredit; }
	public void setCredit(Integer credit) { iCredit = credit; }

	public CourseDemand getCourseDemand() { return iCourseDemand; }
	public void setCourseDemand(CourseDemand courseDemand) { iCourseDemand = courseDemand; }

	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	public Set<CourseRequestOption> getCourseRequestOptions() { return iCourseRequestOptions; }
	public void setCourseRequestOptions(Set<CourseRequestOption> courseRequestOptions) { iCourseRequestOptions = courseRequestOptions; }
	public void addTocourseRequestOptions(CourseRequestOption courseRequestOption) {
		if (iCourseRequestOptions == null) iCourseRequestOptions = new HashSet<CourseRequestOption>();
		iCourseRequestOptions.add(courseRequestOption);
	}

	public Set<ClassWaitList> getClassWaitLists() { return iClassWaitLists; }
	public void setClassWaitLists(Set<ClassWaitList> classWaitLists) { iClassWaitLists = classWaitLists; }
	public void addToclassWaitLists(ClassWaitList classWaitList) {
		if (iClassWaitLists == null) iClassWaitLists = new HashSet<ClassWaitList>();
		iClassWaitLists.add(classWaitList);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseRequest)) return false;
		if (getUniqueId() == null || ((CourseRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseRequest)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseRequest[" +
			"\n	AllowOverlap: " + getAllowOverlap() +
			"\n	CourseDemand: " + getCourseDemand() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	Credit: " + getCredit() +
			"\n	Order: " + getOrder() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
