/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.sectioning;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class SectioningCookie {
	private boolean iCourseDetails = false, iShowClassNumbers = false;
	private int iRelatedSortBy = 0;
	
	private static SectioningCookie sInstance = null;
	
	private SectioningCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Sectioning");
			if (cookie != null && cookie.length() > 0) {
				String[] values = cookie.split(":");
				iCourseDetails = "T".equals(values[0]);
				iShowClassNumbers = "T".equals(values.length >= 2 ? values[1] : "F");
				iRelatedSortBy = Integer.parseInt(values[2]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = 
			(iCourseDetails ? "T": "F") + ":" +
			(iShowClassNumbers ? "T": "F") + ":" + iRelatedSortBy;
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
}
