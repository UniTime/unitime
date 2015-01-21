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
package org.unitime.timetable.interfaces;

import java.util.StringTokenizer;

/**
 * Interface to lookup external ids for manager
 * 
 * @author Tomas Muller
 * 
 */
public interface ExternalUidLookup {
	
	public UserInfo doLookup(String searchId) throws Exception;

	public static class UserInfo {
		private String iExternalId = null;
		private String iUserName = null;
		private String iFirstName = null;
		private String iMiddleName = null;
		private String iLastName = null;
		private String iAcadTitle = null;
		private String iEmail = null;
		private String iName = null;
		private String iPhone = null;
		
		public UserInfo() {}
		
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		
		public String getUserName() { return iUserName; }
		public void setUserName(String userName) { iUserName = userName; }
		
		public String getFirstName() {
			if (iFirstName != null) return iFirstName;
			if (iName != null) {
		        StringTokenizer s = new StringTokenizer(iName);
		        if (s.countTokens() > 1) return s.nextToken();
			}
			return "";
		}
		public void setFirstName(String firstName) { iFirstName = firstName; }
		
		public String getMiddleName() {
			if (iMiddleName != null) return iMiddleName;
			if (iName != null) {
		        StringTokenizer s = new StringTokenizer(iName);
		        if (s.countTokens() > 2) {
		        	s.nextToken();
		        	String m = "";
		        	while (true) {
		        		String n = s.nextToken();
		        		if (!s.hasMoreTokens()) break;
		        		m += (m.isEmpty() ? "" : " ") + n;
		        	}
		        	return m;
		        }
			}
			return "";
		}
		public void setMiddleName(String middleName) { iMiddleName  = middleName; }
		
		public String getLastName() { 
			if (iLastName != null) return iLastName;
			if (iName != null) {
		        StringTokenizer s = new StringTokenizer(iName);
		        if (s.countTokens() > 0) {
		        	String l = s.nextToken();
		        	while (s.hasMoreTokens()) l = s.nextToken();
		        	return l;
		        }
			}
			return "";
		}
		public void setLastName(String lastName) { iLastName = lastName; }
		
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		
		public String getPhone() { return iPhone; }
		public void setPhone(String phone) { iPhone = phone; }
		
		public String getName() {
			if (iName != null) return iName;
			return ((iLastName == null ? "" : iLastName) + ", " + (iFirstName == null ? "" : iFirstName) + (iMiddleName == null ? "" : " " + iMiddleName)).toString();
		}
		public void setName(String name) { iName = name; }
		
		public void setAcademicTitle(String academicTitle) { iAcadTitle = academicTitle; }
		public String getAcademicTitle() { return iAcadTitle; }
	}
	
}
