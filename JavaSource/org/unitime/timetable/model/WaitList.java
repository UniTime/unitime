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
package org.unitime.timetable.model;

import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseWaitList;



/**
 * @author Tomas Muller
 */
public class WaitList extends BaseWaitList implements Comparable<WaitList> {
	private static final long serialVersionUID = 1L;
	
	public static enum WaitListType {
		SCHEDULING_ASSISTANT,
		COURSE_REQUESTS,
		XML_IMPORT,
		BATCH_SOLVER,
		WAIT_LIST_PORCESSING,
		MASS_CANCEL,
		EXTERNAL_UPDATE,
		RELOAD,
		OTHER,
		RE_BATCH_ON_RELOAD,
		RE_BATCH_ON_CHECK,
		;
		
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public WaitList () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public WaitList (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/

	
	public WaitListType getWaitListType() {
		if (getType() == null) return WaitListType.OTHER;
		return WaitListType.values()[getType()];
	}
	public void setWaitListType(WaitListType status) {
		if (status == null)
			setType(null);
		else
			setType(status.ordinal());
	}

	@Override
	public int compareTo(WaitList wl) {
		int cmp = getTimestamp().compareTo(wl.getTimestamp());
		if (cmp != 0) return cmp;
		return getUniqueId().compareTo(wl.getUniqueId());
	}
	
	public boolean hasMatchingCourse(CourseDemand cd) {
		if (cd == null) return false;
		for (CourseRequest cr: cd.getCourseRequests())
			if (cr.getCourseOffering().equals(getCourseOffering())) return true;
		return false;
	}
	
	public static String computeEnrollment(Student student, CourseOffering enrolledCourse) {
		if (student == null || enrolledCourse == null) return "";
		String enrl = null;
		for (StudentClassEnrollment e: student.getClassEnrollments())
			if (enrolledCourse.equals(e.getCourseOffering()))
				enrl = (enrl == null ? "" : enrl + "\n") + e.getClazz().getClassLabel(enrolledCourse, true);
		if (enrl != null && enrl.length() > 255)
			enrl = enrl.substring(0, 252) + "...";
		return enrl;
	}
	
	public static String computeRequest(CourseDemand cd) {
		if (cd == null) return null;
		String req = null;
		for (CourseRequest cr: new TreeSet<CourseRequest>(cd.getCourseRequests())) {
			String rp = null;
			if (cr.getPreferences() != null)
				for (StudentSectioningPref p: cr.getPreferences())
					if (p.isRequired()) {
						if (p instanceof StudentClassPref)
							rp = (rp == null ? "" : rp + ", ") + ((StudentClassPref)p).getClazz().getClassPrefLabel(cr.getCourseOffering());
						else if (p instanceof StudentInstrMthPref)
							rp = (rp == null ? "" : rp + ", ") + ((StudentInstrMthPref)p).getInstructionalMethod().getLabel();
					}
			req = (req == null ? "" : req + "\n") + cr.getCourseOffering().getCourseName() + (rp == null ? "" : " [" + rp + "]");
		}
		if (req != null && req.length() > 255)
			req = req.substring(0, 252) + "...";
		return req;
	}
	
	public void fillInNotes() {
		setEnrollment(computeEnrollment(getStudent(), getEnrolledCourse()));
		setRequest(computeRequest(getCourseDemand()));
	}

}
