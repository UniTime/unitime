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
package org.unitime.timetable.gwt.client.admin;

import java.util.Date;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class AdminCookie {
	private static AdminCookie sInstance = null;
	
	private int iSortTasksBy = 0, iSortTaskExecutionsBy = 0, iSortBuildingsBy = 0, iSortDepartmentsBy = 0, iShowAlldepartments = 0, iSortSurveyCourses = 0, iSortCourseRequirements = 0;
	
	private AdminCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Admin");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iSortTasksBy = Integer.valueOf(params[idx++]);
				iSortTaskExecutionsBy = Integer.valueOf(params[idx++]);
				iSortBuildingsBy = Integer.valueOf(params[idx++]);
				iSortDepartmentsBy = Integer.valueOf(params[idx++]);
				iShowAlldepartments = Integer.valueOf(params[idx++]);
				iSortSurveyCourses = Integer.valueOf(params[idx++]);
				iSortCourseRequirements = Integer.valueOf(params[idx++]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = iSortTasksBy + "|" + iSortTaskExecutionsBy + "|" + iSortBuildingsBy+ "|" + iSortDepartmentsBy + "|" + iShowAlldepartments +
				"|" + iSortSurveyCourses + "|" + iSortCourseRequirements;
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Admin", cookie, expires);
	}
	
	public static AdminCookie getInstance() { 
		if (sInstance == null)
			sInstance = new AdminCookie();
		return sInstance;
	}
	
	public int getSortTasksBy() { return iSortTasksBy; }
	public void setSortTasksBy(int sortTasksBy) { iSortTasksBy = sortTasksBy; save(); }
	
	public int getSortTaskExecutionsBy() { return iSortTaskExecutionsBy; }
	public void setSortTaskExecutionsBy(int sortTakExecutionsBy) { iSortTaskExecutionsBy = sortTakExecutionsBy; save(); }

	public int getSortBuildingsBy() { return iSortBuildingsBy; }
	public void setSortBuildingsBy(int sortBuildingsBy) { iSortBuildingsBy = sortBuildingsBy; save(); }
	
	public int getSortDepartmentsBy() { return iSortDepartmentsBy; }
	public void setSortDepartmentsBy(int sortDepartmentsBy) { iSortDepartmentsBy = sortDepartmentsBy; save(); }
	
	public int getShowAllDepartments() { return iShowAlldepartments; }
	public void setShowAllDepartments(int showAlldepartments) { iShowAlldepartments = showAlldepartments; save(); }
	
	public int getSortSurveyCourses() { return iSortSurveyCourses; }
	public void setSortSurveyCourses(int sort) { iSortSurveyCourses = sort; save(); }
	
	public int getSortCourseRequirements() { return iSortCourseRequirements; }
	public void setSortCourseRequirements(int sort) { iSortCourseRequirements = sort; save(); }
}
