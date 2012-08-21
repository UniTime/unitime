/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
	}
	
}
