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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class SolverInterface implements IsSerializable {
	
	public static enum SolverType implements IsSerializable {
		COURSE,
		EXAM,
		STUDENT,
		INSTRUCTOR,
		;
	}
	
	public static enum SolverOperation implements IsSerializable {
		INIT,CHECK,
		LOAD,
		START,
		UNLOAD,
		RELOAD,
		STOP,
		CLEAR,
		EXPORT_CSV, EXPORT_XML,
		STUDENT_SECTIONING,
		SAVE_BEST,
		RESTORE_BEST,
		SAVE, SAVE_AS_NEW,
		SAVE_COMMIT, SAVE_AS_NEW_COMMIT, SAVE_UNCOMMIT,
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
	
	public static class SolverParameter implements IsSerializable {
		private Long iId;
		private String iKey, iType, iName, iValue, iDefaut;
		
		public SolverParameter() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getKey() { return iKey; }
		public void setKey(String key) { iKey = key; }
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		
		public String getDefaultValue() { return iDefaut; }
		public void setDefaultValue(String defaultValue) { iDefaut = defaultValue; }
		
		@Override
		public String toString() { return getKey() + "=" + (getValue() != null ? getValue() : getDefaultValue() != null ? getDefaultValue() : ""); }
	}
	
	public static class SolverConfiguration implements IsSerializable {
		private Long iId;
		private String iName;
		private Map<Long, String> iParameters;
		
		public SolverConfiguration() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
		public void addParameter(Long id, String value) {
			if (iParameters == null) iParameters = new HashMap<Long, String>();
			iParameters.put(id, value);
		}
		public Map<Long, String> getParameters() { return iParameters; }
		public String getParameter(Long id) {
			if (iParameters == null) return null;
			return (iParameters == null ? null : iParameters.get(id));
		}
	}
	
	public static class SolverOwner implements IsSerializable {
		private Long iId;
		private String iName;
		
		public SolverOwner() {}
		public SolverOwner(Long id, String name) {
			iId = id; iName = name;
		}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
	}
	
	public static class InfoPair implements IsSerializable {
		private String iName, iValue;
		
		public InfoPair() {}
		
		public InfoPair(String name, String value) {
			iName = name;
			iValue = value;
		}
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public void setValue(String value) { iValue = value; }
		public String getValue() { return iValue; }
		
		@Override
		public String toString() { return iName + ": " + iValue; }
	}
	
	public static class SolutionInfo implements IsSerializable {
		private List<InfoPair> iPairs = new ArrayList<InfoPair>();
		private String iName = null;
		private List<ProgressMessage> iLog = null;
		
		public SolutionInfo() {}
		
		public InfoPair addPair(String name, String value) {
			InfoPair pair = new InfoPair(name, value); 
			iPairs.add(pair);
			return pair;
		}
		public List<InfoPair> getPairs() { return iPairs; }
		public boolean isEmpty() { return iPairs.isEmpty(); }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public boolean hasLog() { return iLog != null && !iLog.isEmpty(); }
		public void addMessage(int level, Date date, String message, String[] trace) {
			if (iLog == null) iLog = new ArrayList<ProgressMessage>();
			iLog.add(new ProgressMessage(level, date, message, trace));
		}
		public List<ProgressMessage> getLog() { return iLog; }
		
		@Override
		public String toString() { return iPairs.toString(); }
	}
	
	public static enum PageMessageType implements IsSerializable {
		INFO,
		WARNING,
		ERROR,
		;
	}
	
	public static class PageMessage implements IsSerializable {
		private PageMessageType iType;
		private String iMessage;
		private String iUrl;
		
		public PageMessage() {}
		public PageMessage(PageMessageType type, String message, String url) {
			iType = type;
			iMessage = message;
			iUrl = url;
		}
		public PageMessage(PageMessageType type, String message) {
			this(type, message, null);
		}
		
		public PageMessageType getType() { return iType; }
		public void setType(PageMessageType type) { iType = type; }
		
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public String getUrl() { return iUrl; }
		public void setUrl(String url) { iUrl = url; }
	}
	
	public interface HasPageMessages {
		public boolean hasPageMessages();
		public List<PageMessage> getPageMessages();
		public void addPageMessage(PageMessage message);
	}
	
	public static class SolverPageResponse implements GwtRpcResponse, HasPageMessages {
		private Date iLoadDate;
		private SolverType iSolverType;
		private SolverOperation iOperation;
		private String iSolverStatus, iSolverProgress;
		private Long iConfigurationId;
		private List<SolverConfiguration> iConfigurations;
		private List<SolverParameter> iParameters;
		private List<Long> iOwnerIds;
		private List<SolverOwner> iSolverOwners;
		private String iHost;
		private List<String> iHosts;
		private SolutionInfo iCurrentSolution, iBestSolution;
		private List<SolutionInfo> iSelectedSolutions;
		private List<ProgressMessage> iLog;
		private int iOperations = 0;
		private boolean iAllowMultipleOwners = false;
		private boolean iWorking = false, iRefresh = false;
		private List<PageMessage> iPageMessages = null;
		
		public SolverPageResponse() {}
		
		public SolverType getSolverType() { return iSolverType; }
		public void setSolverType(SolverType type) { iSolverType = type; }
		
		public SolverOperation getOperation() { return iOperation; }
		public void setOperation(SolverOperation operation) { iOperation = operation; }
		
		public String getSolverStatus() { return iSolverStatus; }
		public void setSolverStatus(String solverStatus) { iSolverStatus = solverStatus; }
		
		public String getSolverProgress() { return iSolverProgress; }
		public void setSolverProgress(String solverProgress) { iSolverProgress = solverProgress; }

		public Long getConfigurationId() { return iConfigurationId; }
		public void setConfigurationId(Long configId) { iConfigurationId = configId; }
		
		public boolean hasConfigurations() { return iConfigurations != null && !iConfigurations.isEmpty(); }
		public List<SolverConfiguration> getConfigurations() { return iConfigurations; }
		public void addConfiguration(SolverConfiguration config) {
			if (iConfigurations == null) iConfigurations = new ArrayList<SolverConfiguration>();
			iConfigurations.add(config);
		}
		
		public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
		public List<SolverParameter> getParameters() { return iParameters; }
		public void addParameter(SolverParameter parameter) {
			if (iParameters == null) iParameters = new ArrayList<SolverParameter>();
			iParameters.add(parameter);
		}
		public SolverParameter getParameter(Long id) {
			if (iParameters == null || id == null) return null;
			for (SolverParameter paremeter: iParameters)
				if (id.equals(paremeter.getId())) return paremeter;
			return null;
		}
		
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
		public boolean isAllowMultipleOwners() { return iAllowMultipleOwners; }
		public void setAllowMultipleOwners(boolean allow) { iAllowMultipleOwners = allow; }
		
		public boolean hasOwerIds() { return iOwnerIds != null && !iOwnerIds.isEmpty(); }
		public List<Long> getOwnerIds() { return iOwnerIds; }
		public void addOwnerId(Long ownerId) {
			if (iOwnerIds == null) iOwnerIds = new ArrayList<Long>();
			iOwnerIds.add(ownerId);
		}
		
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
		
		public boolean hasCurrentSolution() { return iCurrentSolution != null; }
		public SolutionInfo getCurrentSolution() { return iCurrentSolution; }
		public void setCurrentSolution(SolutionInfo current) { iCurrentSolution = current; }
		
		public boolean hasBestSolution() { return iBestSolution != null; }
		public SolutionInfo getBestSolution() { return iBestSolution; }
		public void setBestSolution(SolutionInfo best) { iBestSolution = best; }
		
		public boolean hasSelectedSolutions() { return iSelectedSolutions != null && !iSelectedSolutions.isEmpty(); }
		public void addSelectedSolution(SolutionInfo selected) {
			if (iSelectedSolutions == null) iSelectedSolutions = new ArrayList<SolutionInfo>();
			iSelectedSolutions.add(selected);
		}
		public List<SolutionInfo> getSelectedSolutions() { return iSelectedSolutions; }
		
		public boolean hasLog() { return iLog != null && !iLog.isEmpty(); }
		public void addMessage(int level, Date date, String message, String[] trace) {
			if (iLog == null) iLog = new ArrayList<ProgressMessage>();
			iLog.add(new ProgressMessage(level, date, message, trace));
		}
		public List<ProgressMessage> getLog() { return iLog; }
		
		public boolean canExecute(SolverOperation operation) {
			return operation.in(iOperations);
		}
		public void setCanExecute(SolverOperation operation, boolean enabled) {
			if (enabled)
				iOperations = operation.set(iOperations);
			else
				iOperations = operation.clear(iOperations);
		}
		public void setCanExecute(SolverOperation... operations) {
			for (SolverOperation operation: operations)
				iOperations = operation.set(iOperations);
		}
		
		public boolean hasPageMessages() { return iPageMessages != null && !iPageMessages.isEmpty(); }
		public List<PageMessage> getPageMessages() { return iPageMessages; }
		public void addPageMessage(PageMessage message) {
			if (iPageMessages == null) iPageMessages = new ArrayList<PageMessage>();
			iPageMessages.add(message);
		}
		
		public Date getLoadDate() { return iLoadDate; }
		public void setLoadDate(Date date) { iLoadDate = date; }
		
		public boolean isWorking() { return iWorking; }
		public void setWorking(boolean working) { iWorking = working; }
		
		public boolean isRefresh() { return iRefresh; }
		public void setRefresh(boolean refresh) { iRefresh = refresh; }
	}
	
	public static class SolverPageRequest implements GwtRpcRequest<SolverPageResponse> {
		private SolverType iType;
		private SolverOperation iOperation;
		private List<Long> iOwnerIds;
		private Long iConfigurationId;
		private String iHost;
		private Map<Long, String> iParameters;
		
		public SolverPageRequest() {}
		public SolverPageRequest(SolverType type, SolverOperation operation) {
			iType = type; iOperation = operation;
		}

		public SolverType getType() { return iType; }
		public void setType(SolverType type) { iType = type; }
		
		public SolverOperation getOperation() { return iOperation; }
		public void setOperation(SolverOperation operation) { iOperation = operation; }
		
		public Long getConfigurationId() { return iConfigurationId; }
		public void setConfigurationId(Long configId) { iConfigurationId = configId; }
		
		public boolean hasOwerIds() { return iOwnerIds != null && !iOwnerIds.isEmpty(); }
		public List<Long> getOwnerIds() { return iOwnerIds; }
		public void addOwnerId(Long ownerId) {
			if (iOwnerIds == null) iOwnerIds = new ArrayList<Long>();
			iOwnerIds.add(ownerId);
		}
		
		public String getHost() { return iHost; }
		public void setHost(String host) { iHost = host; }

		public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
		public void addParameter(Long id, String value) {
			if (iParameters == null) iParameters = new HashMap<Long, String>();
			iParameters.put(id, value);
		}
		public Map<Long, String> getParameters() { return iParameters; }
		public String getParameter(Long id) {
			if (iParameters == null) return null;
			return (iParameters == null ? null : iParameters.get(id));
		}
		
		public void clear() {
			iOwnerIds = null; iConfigurationId = null; iHost = null; iParameters = null;
		}
		
		@Override
		public String toString() {
			return getType() + ": " + getOperation();
		}
	}
	
	public static enum ProgressLogLevel implements IsSerializable {
		TRACE,
		DEBUG,
		PROGRESS,
		INFO,
		STAGE,
		WARN,
		ERROR,
		FATAL,
		;		
	}
	
	public static class ProgressMessage implements IsSerializable {
		private ProgressLogLevel iLevel;
		private Date iDate;
		private String iMessage;
		private String[] iStackTrace;
		
		public ProgressMessage() {}
		public ProgressMessage(int level, Date date, String message, String[] trace) {
			iLevel = ProgressLogLevel.values()[level];
			iDate = date;
			iMessage = message;
			iStackTrace = trace;
		}
		
		public ProgressLogLevel getLevel() { return iLevel; }
		public Date getDate() { return iDate; }
		public String getMessage() { return iMessage; }
		public boolean hasStackTrace() { return iStackTrace != null && iStackTrace.length > 0; }
		public String[] getStackTrace() { return iStackTrace; }
	}
	
	public static class SolutionLog implements GwtRpcResponse {
		private List<ProgressMessage> iLog = null;
		private String iOwner;
		
		public SolutionLog() {}
		public SolutionLog(String owner) { iOwner = owner; }
		
		public String getOwner() { return iOwner; }
		
		public void addMessage(int level, Date date, String message, String[] trace) {
			if (iLog == null) iLog = new ArrayList<ProgressMessage>();
			iLog.add(new ProgressMessage(level, date, message, trace));
		}
		public boolean hasLog() { return iLog != null && !iLog.isEmpty(); }
		public List<ProgressMessage> getLog() { return iLog; }
	}
	
	public static class SolverLogPageResponse implements GwtRpcResponse {
		private List<ProgressMessage> iLog = null;
		private List<SolutionLog> iSolutionLogs = null;
		private ProgressLogLevel iLevel;
		
		public SolverLogPageResponse() {}
		public SolverLogPageResponse(int level) {
			iLevel = ProgressLogLevel.values()[level];
		}
		public SolverLogPageResponse(ProgressLogLevel level) {
			iLevel = level;
		}
		
		public ProgressLogLevel getLevel() { return iLevel; }
		public void addMessage(int level, Date date, String message, String[] trace) {
			if (iLog == null) iLog = new ArrayList<ProgressMessage>();
			iLog.add(new ProgressMessage(level, date, message, trace));
		}
		public boolean hasLog() { return iLog != null && !iLog.isEmpty(); }
		public List<ProgressMessage> getLog() { return iLog; }
		
		public boolean hasSolutionLogs() { return iSolutionLogs != null && !iSolutionLogs.isEmpty(); }
		public List<SolutionLog> getSolutionLogs() { return iSolutionLogs; }
		public void addSolutionLog(SolutionLog log) {
			if (iSolutionLogs == null) iSolutionLogs = new ArrayList<SolutionLog>();
			iSolutionLogs.add(log);
		}
	}
	
	public static class SolverLogPageRequest implements GwtRpcRequest<SolverLogPageResponse> {
		private ProgressLogLevel iLevel;
		private SolverType iType;
		private Date iLast;
		
		public SolverLogPageRequest() {}
		public SolverLogPageRequest(SolverType type, ProgressLogLevel level, Date last) {
			iType = type;
			iLevel = level;
			iLast = last;
		}

		public SolverType getType() { return iType; }
		public void setType(SolverType type) { iType = type; }
		
		public void setLevel(ProgressLogLevel level) { iLevel = level; }
		public boolean hasLevel() { return iLevel != null; }
		public ProgressLogLevel getLevel() { return iLevel; }
		
		public Date getLastDate() { return iLast; }
		
		@Override
		public String toString() { return getType() + ": " + getLevel(); }
	}

}
