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

import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;

/**
 * @author Tomas Muller
 */
public class SectioningStatusCookie {
	private static SectioningStatusCookie sInstance = null;
	private int iOnlineTab = 0, iBashTab = 0;
	private String iOnlineQuery = "", iBashQuery = "";
	private int[] iSortBy = new int[] {0, 0, 0, 0, 0, 0};
	private int iStudentTab = 2;
	private String[] iSortByGroup = new String[] {"", ""};
	private boolean iEmailIncludeCourseRequests = false, iEmailIncludeClassSchedule = true, iEmailAdvisorRequests = false;
	private String iEmailSubject = "", iEmailCC = "";
	private boolean iAdvisorRequestsEmailStudent;
	private Boolean iOptionalEmailToggle = null;
	private Set<String>[] iHide = new Set[] {
		new HashSet<String>(), new HashSet<String>(), new HashSet<String>(),
		new HashSet<String>(), new HashSet<String>(), new HashSet<String>()};
	
	private SectioningStatusCookie() {
		try {
			String cookie = ToolBox.getCookie("UniTime:StudentStatus");
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
				iSortByGroup[0] = params[idx++];
				iSortByGroup[1] = params[idx++];
				iEmailIncludeCourseRequests = "1".equals(params[idx++]);
				iEmailIncludeClassSchedule = "1".equals(params[idx++]);
				iEmailCC = params[idx++];
				iEmailSubject = params[idx++];
				iEmailAdvisorRequests = "1".equals(params[idx++]);
				iAdvisorRequestsEmailStudent = "1".equals(params[idx++]);
				iOptionalEmailToggle = parseBoolean(params[idx++]);
				for (int i = 0; i < iHide.length; i++)
					for (String col: params[idx++].split(","))
						iHide[i].add(col);
			}
		} catch (Exception e) {
		}
	}
	
	private static Boolean parseBoolean(String value) {
		if ("1".equals(value)) return true;
		if ("0".equals(value)) return false;
		return null;
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
		cookie += "|" + iSortByGroup[0] + "|" + iSortByGroup[1];
		cookie += "|" + (iEmailIncludeCourseRequests ? "1" : "0") + "|" + (iEmailIncludeClassSchedule ? "1" : "0")
				+ "|" + (iEmailCC == null ? "" : iEmailCC) + "|" + (iEmailSubject == null ? "" : iEmailSubject)
				+ "|" + (iEmailAdvisorRequests ? "1" : "0") + "|" + (iAdvisorRequestsEmailStudent ? "1" : "0")
				+ "|" + (iOptionalEmailToggle == null ? "N" : iOptionalEmailToggle.booleanValue() ? "1" : "0");
		for (int i = 0; i < iHide.length; i++) {
			String hide = "";
			for (String col: iHide[i])
				hide += (hide.isEmpty()?"":",") + col;
			cookie += "|" + hide;
		}
		ToolBox.setCookie("UniTime:StudentStatus", cookie);
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
	
	public void setSortBy(boolean online, int tab, int ord, String group) {
		iSortBy[online ? tab : 3 + tab] = ord;
		iSortByGroup[online ? 0 : 1] = group;
		save();
	}
	
	public boolean isHidden(boolean online, int tab, String col) {
		return iHide[online ? tab : 3 + tab].contains(col);
	}
	
	public void setHidden(boolean online, int tab, String col, boolean hidden) {
		if (hidden)
			iHide[online ? tab : 3 + tab].add(col);
		else
			iHide[online ? tab : 3 + tab].remove(col);
		save();
	}
	
	public String getSortByGroup(boolean online) {
		return iSortByGroup[online ? 0 : 1];
	}
	
	public int getStudentTab() { return iStudentTab; }
	
	public void setStudentTab(int tab) { iStudentTab = tab; save(); }
	
	public boolean isEmailIncludeCourseRequests() { return iEmailIncludeCourseRequests; }
	public boolean isEmailIncludeClassSchedule() { return iEmailIncludeClassSchedule; }
	public boolean isEmailIncludeAdvisorRequests() { return iEmailAdvisorRequests; }
	public String getEmailCC() { return iEmailCC; }
	public boolean hasEmailCC() { return iEmailCC != null && !iEmailCC.isEmpty(); }
	public String getEmailSubject() { return iEmailSubject; }
	public boolean hasEmailSubject() { return iEmailSubject != null && !iEmailSubject.isEmpty(); }
	
	public void setEmailDefaults(boolean includeCourseRequests, boolean includeClassSchedule, boolean includeAdvisorRequests, String cc, String subject, Boolean optionalEmailToggle) {
		iEmailIncludeCourseRequests = includeCourseRequests;
		iEmailIncludeClassSchedule = includeClassSchedule;
		iEmailAdvisorRequests = includeAdvisorRequests;
		iEmailCC = cc;
		iEmailSubject = subject;
		if (optionalEmailToggle != null)
			iOptionalEmailToggle = optionalEmailToggle;
		save();
	}
	
	public void setEmailCC(String cc) {
		iEmailCC = cc;
		save();
	}
	
	public boolean isAdvisorRequestsEmailStudent() { return iAdvisorRequestsEmailStudent; }
	public void setAdvisorRequestsEmailStudent(boolean email) { iAdvisorRequestsEmailStudent = email; save(); }
	
	public boolean isOptionalEmailToggle(boolean defaultValue) {
		return (iOptionalEmailToggle == null ? defaultValue : iOptionalEmailToggle.booleanValue());
	}
}
