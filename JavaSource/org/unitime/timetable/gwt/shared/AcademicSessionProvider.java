/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public interface AcademicSessionProvider {
	public Long getAcademicSessionId();
	public String getAcademicSessionName();
	public AcademicSessionInfo getAcademicSessionInfo();
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler);
	
	public static interface AcademicSessionChangeEvent {
		public Long getNewAcademicSessionId();
		public Long getOldAcademicSessionId();
		public boolean isChanged();
	}


	public static interface AcademicSessionChangeHandler {
		public void onAcademicSessionChange(AcademicSessionChangeEvent event);
	}
	
	public void selectSession(Long sessionId, AsyncCallback<Boolean> callback);
	
	public static class AcademicSessionInfo implements IsSerializable {
		private Long iSessionId;
		private String iYear, iTerm, iCampus, iName;
		
		public AcademicSessionInfo() {}
		
		public AcademicSessionInfo(Long sessionId, String year, String term, String campus, String name) {
			iSessionId = sessionId;
			iTerm = term;
			iYear = year;
			iCampus = campus;
			iName = name;
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public String getYear() { return iYear; }
		public void setYear(String year) { iYear = year; }
		
		public String getCampus() { return iCampus; }
		public void setCampus(String campus) { iCampus = campus; }
		
		public String getTerm() { return iTerm; }
		public void setTerm(String term) { iTerm = term; }
		
		public String getName() { return (iName == null || iName.isEmpty() ? iTerm + " " + iYear + " (" + iCampus + ")" : iName); }
		public void setname(String name) { iName = name; }
		
		@Override
		public String toString() {
			return getName();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AcademicSessionInfo)) return false;
			return getSessionId().equals(((AcademicSessionInfo)o).getSessionId());
		}
	}
}
