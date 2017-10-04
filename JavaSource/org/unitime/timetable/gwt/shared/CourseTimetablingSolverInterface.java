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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.SolverInterface.HasPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;

/**
 * @author Tomas Muller
 */
public class CourseTimetablingSolverInterface {
	public static class AssignedClassesFilterRequest implements GwtRpcRequest<AssignedClassesFilterResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		
	}
	
	public static class AssignedClassesFilterResponse extends FilterInterface {
		private static final long serialVersionUID = 0l;
		
		private List<PreferenceInterface> iPreferences = new ArrayList<PreferenceInterface>();
		public void addPreference(PreferenceInterface preference) { iPreferences.add(preference); }
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
	}
	
	public static class NotAssignedClassesFilterRequest implements GwtRpcRequest<NotAssignedClassesFilterResponse>, Serializable {
		private static final long serialVersionUID = 0l;
	}
	
	public static class NotAssignedClassesFilterResponse extends AssignedClassesFilterResponse {
		private static final long serialVersionUID = 0l;
	}

	public static class AssignedClassesRequest implements GwtRpcRequest<AssignedClassesResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		private FilterInterface iFilter;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
	}
	
	public static class AssignedClassesResponse extends TableInterface implements HasPageMessages {
		private static final long serialVersionUID = 0l;
		private List<PageMessage> iPageMessages = null;
		
		public boolean hasPageMessages() { return iPageMessages != null && !iPageMessages.isEmpty(); }
		public List<PageMessage> getPageMessages() { return iPageMessages; }
		public void addPageMessage(PageMessage message) {
			if (iPageMessages == null) iPageMessages = new ArrayList<PageMessage>();
			iPageMessages.add(message);
		}
	}
	
	public static class NotAssignedClassesRequest implements GwtRpcRequest<NotAssignedClassesResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		private FilterInterface iFilter;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
	}
	
	public static class NotAssignedClassesResponse extends AssignedClassesResponse {
		private static final long serialVersionUID = 0l;
		private boolean iShowNote = false;
		
		public boolean isShowNote() { return iShowNote; }
		public void setShowNote(boolean showNote) { iShowNote = showNote; }
	}
	
	public static class ConflictStatisticsFilterRequest implements GwtRpcRequest<ConflictStatisticsFilterResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		
	}
	
	public static class ConflictStatisticsFilterResponse extends FilterInterface implements HasPageMessages {
		private static final long serialVersionUID = 0l;
		private SuggestionProperties iProperties;
		private List<PageMessage> iPageMessages = null;
		
		public SuggestionProperties getSuggestionProperties() { return iProperties; }
		public void setSuggestionProperties(SuggestionProperties properties) { iProperties = properties; }
		public boolean hasPageMessages() { return iPageMessages != null && !iPageMessages.isEmpty(); }
		public List<PageMessage> getPageMessages() { return iPageMessages; }
		public void addPageMessage(PageMessage message) {
			if (iPageMessages == null) iPageMessages = new ArrayList<PageMessage>();
			iPageMessages.add(message);
		}
	}
}
