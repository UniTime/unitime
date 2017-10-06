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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.SolverInterface.HasPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverConfiguration;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOperation;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOwner;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;

import com.google.gwt.user.client.rpc.IsSerializable;

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
	
	public static class SolutionChangesFilterRequest implements GwtRpcRequest<SolutionChangesFilterResponse>, Serializable {
		private static final long serialVersionUID = 0l;
	}
	
	public static class SolutionChangesFilterResponse extends AssignedClassesFilterResponse {
		private static final long serialVersionUID = 0l;
	}

	public static class SolutionChangesRequest implements GwtRpcRequest<SolutionChangesResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		private FilterInterface iFilter;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
	}
	
	public static class SolutionChangesResponse extends AssignedClassesResponse {
		private static final long serialVersionUID = 0l;
		private String iMessage = null;
		
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
	}
	
	public static class AssignmentHistoryFilterRequest implements GwtRpcRequest<AssignmentHistoryFilterResponse>, Serializable {
		private static final long serialVersionUID = 0l;
	}
	
	public static class AssignmentHistoryFilterResponse extends AssignedClassesFilterResponse {
		private static final long serialVersionUID = 0l;
	}
	
	public static class AssignmentHistoryRequest implements GwtRpcRequest<AssignmentHistoryResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		private FilterInterface iFilter;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
	}
	
	public static class AssignmentHistoryResponse extends SolutionChangesResponse {
		private static final long serialVersionUID = 0l;
	}
	
	public static enum SolutionOperation implements IsSerializable {
		INIT, CHECK, SELECT, DESELECT, LOAD, LOAD_EMPTY, UNLOAD, COMMIT, UNCOMMIT, EXPORT, UPDATE_NOTE, DELETE, RELOAD,
		SAVE, SAVE_AS_NEW, SAVE_COMMIT, SAVE_AS_NEW_COMMIT,
		;
		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}
	
	public static class ListSolutionsRequest implements GwtRpcRequest<ListSolutionsResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		private SolutionOperation iOperation;
		private List<Long> iSolutionIds;
		private Long iConfigurationId;
		private Long iOwnerId;
		private String iHost;
		private String iNote;
		
		public ListSolutionsRequest() {}
		public ListSolutionsRequest(SolutionOperation operation) {
			iOperation = operation;
		}
		
		public SolutionOperation getOperation() { return iOperation; }
		public void SolutionOperation(SolutionOperation operation) { iOperation = operation; }
		
		public Long getConfigurationId() { return iConfigurationId; }
		public void setConfigurationId(Long configId) { iConfigurationId = configId; }
		
		public boolean hasOwner() { return iOwnerId != null && iOwnerId >= 0l; }
		public Long getOwnerId() { return iOwnerId; }
		public void setOwnerId(Long configId) { iOwnerId = configId; }
		
		public boolean hasSolutionIds() { return iSolutionIds != null && !iSolutionIds.isEmpty(); }
		public List<Long> getSolutionIds() { return iSolutionIds; }
		public void addSolutionId(Long solutionId) {
			if (iSolutionIds == null) iSolutionIds = new ArrayList<Long>();
			iSolutionIds.add(solutionId);
		}
		
		public String getHost() { return iHost; }
		public void setHost(String host) { iHost = host; }
		
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
	}
	
	public static class ListSolutionsResponse extends TableInterface implements HasPageMessages {
		private static final long serialVersionUID = 0l;
		private Date iLoadDate;
		private List<PageMessage> iPageMessages = null;
		private SolutionInfo iCurrentSolution;
		private List<ProgressMessage> iLog;
		private List<SolutionInfo> iSelectedSolutions;
		private String iHost;
		private List<String> iHosts;
		private String iSolverStatus, iSolverProgress;
		private Long iConfigurationId;
		private List<SolverConfiguration> iConfigurations;
		private boolean iWorking = false;
		private SolutionOperation iOperation;
		private Map<Long, Integer> iOperations = new HashMap<Long, Integer>();
		private String iMessage = null;
		private List<SolverOwner> iSolverOwners;
		private List<Long> iOwnerIds;
		private List<String> iErrors = new ArrayList<String>();
		private Boolean iSuccess = false;
		
		public boolean hasPageMessages() { return iPageMessages != null && !iPageMessages.isEmpty(); }
		public List<PageMessage> getPageMessages() { return iPageMessages; }
		public void addPageMessage(PageMessage message) {
			if (iPageMessages == null) iPageMessages = new ArrayList<PageMessage>();
			iPageMessages.add(message);
		}
		
		public String getSolverStatus() { return iSolverStatus; }
		public void setSolverStatus(String solverStatus) { iSolverStatus = solverStatus; }
		
		public String getSolverProgress() { return iSolverProgress; }
		public void setSolverProgress(String solverProgress) { iSolverProgress = solverProgress; }

		public Long getConfigurationId() { return iConfigurationId; }
		public void setConfigurationId(Long ownerId) { iConfigurationId = ownerId; }
		
		public boolean hasConfigurations() { return iConfigurations != null && !iConfigurations.isEmpty(); }
		public SolverConfiguration getConfiguration(Long id) {
			if (iConfigurations == null) return null;
			for (SolverConfiguration config: iConfigurations)
				if (id.equals(config.getId())) return config;
			return null;
		}
		public List<SolverConfiguration> getConfigurations() { return iConfigurations; }
		public void addConfiguration(SolverConfiguration config) {
			if (iConfigurations == null) iConfigurations = new ArrayList<SolverConfiguration>();
			iConfigurations.add(config);
		}
		
		public boolean hasCurrentSolution() { return iCurrentSolution != null; }
		public SolutionInfo getCurrentSolution() { return iCurrentSolution; }
		public void setCurrentSolution(SolutionInfo current) { iCurrentSolution = current; }

		public boolean hasSelectedSolutions() { return iSelectedSolutions != null && !iSelectedSolutions.isEmpty(); }
		public void addSelectedSolution(SolutionInfo selected) {
			if (iSelectedSolutions == null) iSelectedSolutions = new ArrayList<SolutionInfo>();
			iSelectedSolutions.add(selected);
		}
		public List<SolutionInfo> getSelectedSolutions() { return iSelectedSolutions; }
		
		public String getHost() { return iHost; }
		public void setHost(String host) { iHost = host; }
		
		public boolean hasHosts() { return iHosts != null && !iHosts.isEmpty(); }
		public List<String> getHosts() { return iHosts; }
		public void addHost(String host) {
			if (iHosts == null) iHosts = new ArrayList<String>();
			iHosts.add(host);
		}
		public void addHost(int index, String host) {
			if (iHosts == null) iHosts = new ArrayList<String>();
			iHosts.add(index, host);
		}
		
		public boolean hasLog() { return iLog != null && !iLog.isEmpty(); }
		public void addMessage(int level, Date date, String message, String[] trace) {
			if (iLog == null) iLog = new ArrayList<ProgressMessage>();
			iLog.add(new ProgressMessage(level, date, message, trace));
		}
		public List<ProgressMessage> getLog() { return iLog; }
		
		public Date getLoadDate() { return iLoadDate; }
		public void setLoadDate(Date date) { iLoadDate = date; }
		
		public boolean isWorking() { return iWorking; }
		public void setWorking(boolean working) { iWorking = working; }
		
		public SolutionOperation getOperation() { return iOperation; }
		public void setOperation(SolutionOperation operation) { iOperation = operation; }

		public boolean canExecute(Long solutionId, SolutionOperation operation) {
			Integer operations = iOperations.get(solutionId);
			return operations != null && operation.in(operations);
		}
		public void setCanExecute(Long solutionId, SolutionOperation operation, boolean enabled) {
			Integer operations = iOperations.get(solutionId);
			if (operations == null) operations = 0;
			if (enabled)
				iOperations.put(solutionId, operation.set(operations));
			else
				iOperations.put(solutionId, operation.clear(operations));
		}
		public void setCanExecute(Long solutionId, SolverOperation... operations) {
			Integer op = iOperations.get(solutionId);
			if (op == null) op = 0;
			for (SolverOperation operation: operations)
				op = operation.set(op);
			iOperations.put(solutionId, op);
		}
		
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public boolean hasSolverOwners() { return iSolverOwners != null && !iSolverOwners.isEmpty(); }
		public List<SolverOwner> getSolverOwners() { return iSolverOwners; }
		public void addSolverOwner(SolverOwner solverOwner) {
			if (iSolverOwners == null) iSolverOwners = new ArrayList<SolverOwner>();
			iSolverOwners.add(solverOwner);
		}
		public SolverOwner getSolverOwner(Long id) {
			if (iSolverOwners == null || id == null) return null;
			for (SolverOwner g: iSolverOwners)
				if (id.equals(g.getId())) return g;
			return null;
		}
		
		public boolean hasOwerIds() { return iOwnerIds != null && !iOwnerIds.isEmpty(); }
		public List<Long> getOwnerIds() { return iOwnerIds; }
		public void addOwnerId(Long ownerId) {
			if (iOwnerIds == null) iOwnerIds = new ArrayList<Long>();
			iOwnerIds.add(ownerId);
		}
		
		public boolean hasErrors() { return !iErrors.isEmpty(); }
		public List<String> getErrors() { return iErrors; }
		public String getErrorMessage(String separator) {
			String ret = "";
			if (iErrors != null)
				for (String error: iErrors)
					if (error != null && !error.isEmpty())
						ret += (ret.isEmpty() ? "" : separator) + error;
			return ret;
		}
		public void addError(String error) { iErrors.add(error); }
		
		public boolean hasSuccess() { return iSuccess != null; }
		public boolean isSucceeded() { return iSuccess != null && iSuccess.booleanValue(); }
		public void setSuccess(Boolean success) { iSuccess = success; }
	}
}
