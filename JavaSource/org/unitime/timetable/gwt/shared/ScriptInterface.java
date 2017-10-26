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
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class ScriptInterface implements GwtRpcResponse, Comparable<ScriptInterface> {
	private Long iId;
	private String iName, iDescription, iEngine, iPermission, iScript;
	private List<ScriptParameterInterface> iParameters;
	private boolean iCanEdit = false, iCanDelete = false, iCanExecute;
	
	public ScriptInterface() {}
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }
	
	public String getEngine() { return iEngine; }
	public void setEngine(String engine) { iEngine = engine; }
	
	public String getPermission() { return iPermission; }
	public void setPermission(String permission) { iPermission = permission; }
	
	public String getScript() { return iScript; }
	public void setScript(String script) { iScript = script; }
	
	public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
	public List<ScriptParameterInterface> getParameters() { return iParameters; }
	public void addParameter(ScriptParameterInterface parameter) {
		if (iParameters == null)
			iParameters = new ArrayList<ScriptParameterInterface>();
		iParameters.add(parameter);
	}
	
	public boolean canEdit() { return iCanEdit; }
	public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
	
	public boolean canDelete() { return iCanDelete; }
	public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }

	public boolean canExecute() { return iCanExecute; }
	public void setCanExecute(boolean canExecute) { iCanExecute = canExecute; }

	@Override
	public int compareTo(ScriptInterface o) {
		return getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public static class ScriptParameterInterface implements IsSerializable, Comparable<ScriptParameterInterface> {
		private String iName, iLabel, iType, iValue, iDefault;
		private Set<ListItem> iOptions = null;
		private boolean iMultiSelect = false;
		
		public ScriptParameterInterface() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }

		public String getDefaultValue() { return iDefault; }
		public void setDefaultValue(String defaultValue) { iDefault = defaultValue; }
		
		public boolean hasOptions() { return iOptions != null && !iOptions.isEmpty(); }
		public void addOption(String value, String text) {
			if (iOptions == null)
				iOptions = new TreeSet<ListItem>();
			iOptions.add(new ListItem(value, text));
		}
		public Set<ListItem> getOptions() { return iOptions; }
		
		public boolean isMultiSelect() { return iMultiSelect; }
		public void setMultiSelect(boolean multiSelect) { iMultiSelect = multiSelect; }
		
		@Override
		public String toString() {
			return getName() + "=" + (getValue() == null ? getDefaultValue() : getValue());
		}
		
		@Override
		public int compareTo(ScriptParameterInterface o) {
			int cmp = getLabel().compareTo(o.getLabel());
			if (cmp != 0) return cmp;
			return getName().compareTo(o.getName());
		}
	}
	
	public static class ScriptOptionsInterface implements GwtRpcResponse {
		private List<String> iEngines = new ArrayList<String>();
		private List<String> iPermissions = new ArrayList<String>();
		private boolean iCanAdd;
		private String iEmail;
		
		public ScriptOptionsInterface() {
		}
		
		public void addEngine(String engine) { iEngines.add(engine); }
		public List<String> getEngines() { return iEngines; }
		
		public void addPermission(String permission) { iPermissions.add(permission); }
		public List<String> getPermissions() { return iPermissions; }
		
		public boolean canAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
		
		public void setEmail(String email) { iEmail = email; }
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
		public String getEmail() { return iEmail; }
	}
	
	public static class GetScriptOptionsRpcRequest implements GwtRpcRequest<ScriptOptionsInterface> {

		public GetScriptOptionsRpcRequest() {}
		
		@Override
		public String toString() {
			return "";
		}
	}
	
	public static class LoadAllScriptsRpcRequest implements GwtRpcRequest<GwtRpcResponseList<ScriptInterface>> {

		public LoadAllScriptsRpcRequest() {}
		
		@Override
		public String toString() {
			return "";
		}
	}
	
	public static class SaveOrUpdateScriptRpcRequest implements GwtRpcRequest<ScriptInterface> {
		private ScriptInterface iScript;

		public SaveOrUpdateScriptRpcRequest() {}
		
		public SaveOrUpdateScriptRpcRequest(ScriptInterface script) {
			iScript = script;
		}
		
		public ScriptInterface getScript() { return iScript; }
		public void setScript(ScriptInterface script) { iScript = script; }
		
		@Override
		public String toString() {
			return getScript().toString();
		}
	}
	
	public static class DeleteScriptRpcRequest implements GwtRpcRequest<ScriptInterface> {
		private Long iId;
		private String iName;

		public DeleteScriptRpcRequest() {}
		
		public DeleteScriptRpcRequest(Long id, String name) {
			iId = id; iName = name;
		}
		
		public Long getScriptId() { return iId; }
		public void setScriptId(Long id) { iId = id; }
		
		public String getScriptName() { return iName; }
		public void setScriptName(String name) { iName = name; }
		
		@Override
		public String toString() {
			return getScriptName();
		}
	}
	
	public static class ExecuteScriptRpcRequest implements GwtRpcRequest<QueueItemInterface>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iName;
		private Map<String, String> iParameters = new HashMap<String, String>();
		private String iEmail;

		public ExecuteScriptRpcRequest() {}
		
		public Long getScriptId() { return iId; }
		public void setScriptId(Long id) { iId = id; }
		
		public String getScriptName() { return iName; }
		public void setScriptName(String name) { iName = name; }
		
		public Map<String, String> getParameters() { return iParameters; }
		public void setParameter(String name, String value) { iParameters.put(name, value); }
		
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		
		public static ExecuteScriptRpcRequest executeScript(Long scriptId, String scriptName, Map<String, String> parameters, String email) {
			ExecuteScriptRpcRequest request = new ExecuteScriptRpcRequest();
			request.setScriptId(scriptId);
			request.setScriptName(scriptName);
			if (parameters != null)
				for (Map.Entry<String, String> e: parameters.entrySet())
					request.setParameter(e.getKey(), e.getValue());
			request.setEmail(email);
			return request;
		}
		
		public static ExecuteScriptRpcRequest executeScript(ScriptInterface script) {
			ExecuteScriptRpcRequest request = new ExecuteScriptRpcRequest();
			request.setScriptId(script.getId());
			request.setScriptName(script.getName());
			if (script.hasParameters())
				for (ScriptParameterInterface parameter: script.getParameters())
					if (parameter.getValue() != null || parameter.getDefaultValue() != null)
						request.setParameter(parameter.getName(), parameter.getValue() == null ? parameter.getDefaultValue() : parameter.getValue());
			return request;
		}
		
		@Override
		public String toString() {
			return getScriptName() + getParameters();
		}
	}

	public static class ListItem implements IsSerializable, Comparable<ListItem> {
		private String iValue, iText;
		public ListItem() {}
		public ListItem(String value, String text) {
			iValue = value; iText = text;
		}
		public String getValue() { return iValue; }
		public String getText() { return iText; }
		
		@Override
		public int compareTo(ListItem item) {
			int cmp = getText().compareTo(item.getText());
			if (cmp != 0) return cmp;
			return getValue().compareTo(item.getValue());
		}
	}
	
	public static class QueueItemInterface implements GwtRpcResponse {
		private String iId;
		private String iName, iStatus, iProgress, iOwner, iSession, iOutput, iLog, iHost, iOutputLink;
		private Date iCreated, iStarted, iFinished;
		private boolean iCanDelete = false;
		
		private ExecuteScriptRpcRequest iExecutionRequest;
		
		public QueueItemInterface() {}
		
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
		
		public String getHost() { return iHost; }
		public void setHost(String host) { iHost = host; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getStatus() { return iStatus; }
		public void setStatus(String status) { iStatus = status; }
		
		public String getProgress() { return iProgress; }
		public void setProgress(String progress) { iProgress = progress; }
		
		public String getOwner() { return iOwner; }
		public void setOwner(String owner) { iOwner = owner; }
		
		public String getSession() { return iSession; }
		public void setSession(String session) { iSession = session; }
		
		public String getOtuput() { return iOutput; }
		public void setOutput(String output) { iOutput = output; }
		
		public String getOtuputLink() { return iOutputLink; }
		public void setOutputLink(String outputLink) { iOutputLink = outputLink; }
		
		public Date getCreated() { return iCreated; }
		public void setCreated(Date created) { iCreated = created; }
		
		public Date getStarted() { return iStarted; }
		public void setStarted(Date started) { iStarted = started; }
		
		public Date getFinished() { return iFinished; }
		public void setFinished(Date finished) { iFinished = finished; }
		
		public String getLog() { return iLog; }
		public void setLog(String log) { iLog = log; }
		
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		public boolean isCanDelete() { return iCanDelete; }
		
		public void setExecutionRequest(ExecuteScriptRpcRequest request) { iExecutionRequest = request; }
		public ExecuteScriptRpcRequest getExecutionRequest() { return iExecutionRequest; }
	}
	
	public static class GetQueueTableRpcRequest implements GwtRpcRequest<GwtRpcResponseList<QueueItemInterface>> {
		private String iDeleteId = null;

		public GetQueueTableRpcRequest() {}
		public GetQueueTableRpcRequest(String deleteId) {  iDeleteId = deleteId; }
		
		public String getDeleteId() { return iDeleteId; }
		
		@Override
		public String toString() {
			return (iDeleteId == null ? "" : iDeleteId);
		}
	}
}
