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
