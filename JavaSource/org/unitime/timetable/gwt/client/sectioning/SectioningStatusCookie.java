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

import java.util.Date;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class SectioningStatusCookie {
	private static SectioningStatusCookie sInstance = null;
	private int iOnlineTab = 0, iBashTab = 0;
	private String iOnlineQuery = "", iBashQuery = "";
	private int[] iSortBy = new int[] {0, 0, 0, 0, 0, 0};
	private int iStudentTab = 1;
	
	private SectioningStatusCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:StudentStatus");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iOnlineTab = Integer.parseInt(params[idx++]);
				iOnlineQuery = params[idx++];
				iBashTab = Integer.parseInt(params[idx++]);
				iBashQuery = params[idx++];
				for (int i = 0; i < iSortBy.length; i++)
					iSortBy[i] = Integer.parseInt(params[idx++]);
				iStudentTab = Integer.parseInt(params[idx++]);
			}
		} catch (Exception e) {
		}
	}
	
	public static SectioningStatusCookie getInstance() { 
		if (sInstance == null)
			sInstance = new SectioningStatusCookie();
		return sInstance;
	}
	
	private void save() {
		String cookie = iOnlineTab + "|" + iOnlineQuery + "|" + iBashTab + "|" + iBashQuery;
		for (int i = 0; i < iSortBy.length; i++)
			cookie += "|" + iSortBy[i];
		cookie += "|" + iStudentTab;
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:StudentStatus", cookie, expires);
	}
	
	public String getQuery(boolean online) {
		return online ? iOnlineQuery : iBashQuery;
	}

	public int getTab(boolean online) {
		return online ? iOnlineTab : iBashTab;
	}

	public void setQueryTab(boolean online, String query, int tab) {
		if (online) {
			iOnlineQuery = query;
			iOnlineTab = tab;
		} else {
			iBashQuery = query;
			iBashTab = tab;
		}
		save();
	}
	
	public int getSortBy(boolean online, int tab) {
		return iSortBy[online ? tab : 3 + tab];
	}
	
	public void setSortBy(boolean online, int tab, int ord) {
		iSortBy[online ? tab : 3 + tab] = ord;
		save();
	}
	
	public int getStudentTab() { return iStudentTab; }
	
	public void setStudentTab(int tab) { iStudentTab = tab; save(); }
}
