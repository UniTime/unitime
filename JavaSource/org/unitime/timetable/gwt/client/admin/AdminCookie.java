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
	
	private int iSortTasksBy = 0, iSortTaskExecutionsBy = 0;
	
	private AdminCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Admin");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iSortTasksBy = Integer.valueOf(params[idx++]);
				iSortTaskExecutionsBy = Integer.valueOf(params[idx++]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = iSortTasksBy + "|" + iSortTaskExecutionsBy;
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
}
