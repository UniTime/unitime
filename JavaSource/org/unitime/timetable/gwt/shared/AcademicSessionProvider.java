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
package org.unitime.timetable.gwt.shared;

import java.util.Date;

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
	
	public static class AcademicSessionInfo implements IsSerializable, Comparable<AcademicSessionInfo> {
		private Long iSessionId;
		private String iYear, iTerm, iInitiative, iCampus, iName;
		private String iExternalTerm, iExternalCampus;
		private Date iStartDate;
		private Boolean iPrimary = null;
		private Boolean iOnline = null;
		private Boolean iSectioning = null;
		
		public AcademicSessionInfo() {}
		
		public AcademicSessionInfo(Long sessionId, String year, String term, String initiative, String campus, String name, Date sessionStartDate) {
			iSessionId = sessionId;
			iTerm = term;
			iYear = year;
			iInitiative = initiative;
			iCampus = campus;
			iName = name;
			iStartDate = sessionStartDate;
		}

		public AcademicSessionInfo(Long sessionId, String year, String term, String initiative, String name, Date sessionStartDate) {
			this(sessionId, year, term, initiative, initiative, name, sessionStartDate);
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public String getYear() { return iYear; }
		public void setYear(String year) { iYear = year; }
		
		public String getInitiative() { return iInitiative; }
		public void setInitiative(String initiative) { iInitiative = initiative; }

		public String getCampus() { return iCampus; }
		public void setCampus(String campus) { iCampus = campus; }
		
		public String getTerm() { return iTerm; }
		public void setTerm(String term) { iTerm = term; }
		
		public String getName() { return (iName == null || iName.isEmpty() ? iTerm + " " + iYear + " (" + iInitiative + ")" : iName); }
		public void setName(String name) { iName = name; }
		
		public String getExternalCampus() { return iExternalCampus; }
		public boolean hasExternalCampus() { return iExternalCampus != null && !iExternalCampus.isEmpty(); }
		public AcademicSessionInfo setExternalCampus(String extCampus) { iExternalCampus = extCampus; return this; }
		
		public String getExternalTerm() { return iExternalTerm; }
		public boolean hasExternalTerm() { return iExternalTerm != null && !iExternalTerm.isEmpty(); }
		public AcademicSessionInfo setExternalTerm(String extTerm) { iExternalTerm = extTerm; return this; }
		
		public boolean hasStartDate() { return iStartDate != null; }
		public Date getStartDate() { return iStartDate; }
		public void setStartDate(Date startDate) { iStartDate = startDate; }
		
		public boolean isPrimary() { return Boolean.TRUE.equals(iPrimary); }
		public AcademicSessionInfo setPrimary(boolean primary) {
			iPrimary = primary;
			return this;
		}
		public boolean hasSectioning() { return iSectioning; }
		public boolean isSectioning() { return Boolean.TRUE.equals(iSectioning); }
		public AcademicSessionInfo setSectioning(boolean sectioning) {
			iSectioning = sectioning;
			return this;
		}
		
		public boolean isOnline() { return !Boolean.FALSE.equals(iOnline); }
		public AcademicSessionInfo setOnline(boolean online) {
			iOnline = online;
			return this;
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AcademicSessionInfo)) return false;
			if (isSectioning() != ((AcademicSessionInfo)o).isSectioning()) return false;
			return getSessionId().equals(((AcademicSessionInfo)o).getSessionId());
		}

		@Override
		public int compareTo(AcademicSessionInfo s) {
			// if (isOnline() != s.isOnline()) return isOnline() ? -1 : 1;
			int cmp = s.getStartDate().compareTo(getStartDate());
			if (cmp != 0) return cmp; //(isOnline() ? cmp : -cmp);
			if (isPrimary() != s.isPrimary())
				return isPrimary() ? -1 : 1;
			if (isSectioning() != s.isSectioning()) return (isSectioning() ? 1 : -1);
			cmp = getName().compareTo(s.getName());
			if (cmp != 0) return cmp;
			return getSessionId().compareTo(s.getSessionId());
		}
	}
	
	public static interface AcademicSessionMatcher {
		public boolean match(AcademicSessionInfo session);
	}
	
	public static class AcademicSessionMatchSessionId implements AcademicSessionMatcher {
		Long iSessionId;
		boolean iSectioning;
		public AcademicSessionMatchSessionId(Long sessionId, boolean sectioning) { iSessionId = sessionId; iSectioning = sectioning; }
		public boolean match(AcademicSessionInfo session) {
			return session.getSessionId().equals(iSessionId) && iSectioning == session.isSectioning();
		}
	}
	
	public static interface HasMode {
		public void selectSession(Long sessionId, boolean sectioning, AsyncCallback<Boolean> callback);
	}
}
