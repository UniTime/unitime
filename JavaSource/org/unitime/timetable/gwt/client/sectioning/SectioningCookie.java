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
package org.unitime.timetable.gwt.client.sectioning;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class SectioningCookie {
	private boolean iCourseDetails = false, iShowClassNumbers = false;
	private int iRelatedSortBy = 0;
	private EnrollmentFilter iEnrollmentFilter = EnrollmentFilter.ALL;
	private int iEnrollmentSortBy = 0;
	private String iEnrollmentSortBySubpart = "";
	private boolean iAllChoices = false;
	private boolean iShowAllChanges = false;
	private boolean iRequestOverridesOpened = false;
	
	private static SectioningCookie sInstance = null;
	
	public static enum EnrollmentFilter {
		ALL,
		ENROLLED,
		NOT_ENROLLED,
		WAIT_LISTED
	}
	
	private SectioningCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Sectioning");
			if (cookie != null && cookie.length() > 0) {
				String[] values = cookie.split(":");
				iCourseDetails = "T".equals(values[0]);
				iShowClassNumbers = "T".equals(values.length >= 2 ? values[1] : "F");
				iRelatedSortBy = Integer.parseInt(values[2]);
				iEnrollmentFilter = EnrollmentFilter.values()[Integer.parseInt(values[3])];
				iEnrollmentSortBy = Integer.parseInt(values[4]);
				iEnrollmentSortBySubpart = values[5];
				iAllChoices = "T".equals(values[6]);
				iShowAllChanges = "T".equals(values[7]);
				iRequestOverridesOpened = "T".equals(values[8]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = 
			(iCourseDetails ? "T": "F") + ":" +
			(iShowClassNumbers ? "T": "F") + ":" + iRelatedSortBy + ":" + iEnrollmentFilter.ordinal() + ":" + iEnrollmentSortBy + ":" + iEnrollmentSortBySubpart +
			":" + (iAllChoices ? "T" : "F") + 
			":" + (iShowAllChanges ? "T" : "F") + ":" + (iRequestOverridesOpened ? "T" : "F");
		Cookies.setCookie("UniTime:Sectioning", cookie);
	}
	
	public static SectioningCookie getInstance() {
		if (sInstance == null)
			sInstance = new SectioningCookie();
		return sInstance;
	}
	
	public boolean getEnrollmentCoursesDetails() {
		return iCourseDetails;
	}
	
	public void setEnrollmentCoursesDetails(boolean details) {
		iCourseDetails = details;
		save();
	}

	public boolean getShowClassNumbers() {
		return iShowClassNumbers;
	}
	
	public void setShowClassNumbers(boolean showClassNumbers) {
		iShowClassNumbers = showClassNumbers;
		save();
	}
	
	public int getRelatedSortBy() {
		return iRelatedSortBy;
	}
	
	public void setRelatedSortBy(int sort) {
		iRelatedSortBy = sort;
		save();
	}
	
	public EnrollmentFilter getEnrollmentFilter() {
		return iEnrollmentFilter;
	}
	
	public void setEnrollmentFilter(EnrollmentFilter enrollmentFilter) {
		iEnrollmentFilter = enrollmentFilter;
		save();
	}
	
	public int getEnrollmentSortBy() {
		return iEnrollmentSortBy;
	}
	
	public void setEnrollmentSortBy(int sort) {
		iEnrollmentSortBy = sort; iEnrollmentSortBySubpart = "";
		save();
	}

	public String getEnrollmentSortBySubpart() {
		return iEnrollmentSortBySubpart;
	}

	public void setEnrollmentSortBySubpart(String subpart) {
		iEnrollmentSortBy = 0; iEnrollmentSortBySubpart = subpart;
		save();
	}
	
	public boolean isAllChoices() { return iAllChoices; }
	
	public void setAllChoices(boolean allChoices) { iAllChoices = allChoices; save(); }
	
	public boolean isShowAllChanges() { return iShowAllChanges; }
	public void setShowAllChanges(boolean showAllChanges) { iShowAllChanges = showAllChanges; save(); }
	
	public boolean isRequestOverridesOpened() { return iRequestOverridesOpened; }
	public void setRequestOverridesOpened(boolean requestOverridesOpened) { iRequestOverridesOpened = requestOverridesOpened; save(); }
}
