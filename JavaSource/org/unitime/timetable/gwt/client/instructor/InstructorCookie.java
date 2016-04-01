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
package org.unitime.timetable.gwt.client.instructor;

import java.util.Date;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class InstructorCookie {
	private static InstructorCookie sInstance = null;
	private int iSortAttributesBy = 0;
	private int iSortInstructorsBy = 0;

	private InstructorCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Instructor");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iSortAttributesBy = Integer.valueOf(params[idx++]);
				iSortInstructorsBy = Integer.valueOf(params[idx++]);
			}
		} catch (Exception e) {
		}
	}
	
	public static InstructorCookie getInstance() { 
		if (sInstance == null)
			sInstance = new InstructorCookie();
		return sInstance;
	}

	private void save() {
		String cookie = iSortAttributesBy + "|" + iSortInstructorsBy;
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Instructor", cookie, expires);
	}
	
	public int getSortAttributesBy() {
		return iSortAttributesBy;
	}
	
	public void setSortAttributesBy(int sortAttributesBy) {
		iSortAttributesBy = sortAttributesBy;
		save();
	}

	public int getSortInstructorsBy() {
		return iSortInstructorsBy;
	}
	
	public void setSortInstructorsBy(int sortInstructorsBy) {
		iSortInstructorsBy = sortInstructorsBy;
		save();
	}
}
