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
package org.unitime.timetable.server.script;

import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.FileTypeMap;

import org.apache.commons.io.IOUtils;
import org.hibernate.Transaction;
import org.unitime.timetable.api.BinaryFileApiHelper.BinaryFile;
import org.unitime.timetable.api.connectors.ScriptConnector.BinaryFileItem;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.ScriptInterface.ExecuteScriptRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.dao.TaskExecutionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.security.rights.Right;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
public class TaskExecutionItem extends ScriptExecution {
	private static final long serialVersionUID = 1L;

	public TaskExecutionItem(TaskExecution execution, PermissionCheck check) {
		super(createRequest(execution), new TaskContext(execution, check));
	}
	
	private static ExecuteScriptRpcRequest createRequest(TaskExecution execution) {
		ExecuteScriptRpcRequest request = new ExecuteScriptRpcRequest();
		request.setEmail(execution.getTask().getEmail());
		for (TaskParameter p: execution.getTask().getParameters())
			request.setParameter(p.getName(), p.getValue());
		request.setScriptId(execution.getTask().getScript().getUniqueId());
		request.setScriptName(execution.getTask().getName());
		return request;
	}
	
	@Override
	public void executeItem() {
		onStarted();
		try {
			super.executeItem();
		} finally {
			onFinished();
		}
	}
	
	protected void onStarted() {
		org.hibernate.Session hibSession = TaskExecutionDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			TaskExecution execution = TaskExecutionDAO.getInstance().get(getTaskExecutionId(), hibSession);
			if (execution != null) {
				execution.setStartedDate(new Date());
				execution.setExecutionStatus(ExecutionStatus.RUNNING.ordinal());
				hibSession.saveOrUpdate(execution);
			}
			hibSession.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	protected void onFinished() {
		org.hibernate.Session hibSession = TaskExecutionDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			TaskExecution execution = TaskExecutionDAO.getInstance().get(getTaskExecutionId(), hibSession);
			if (execution != null) {
				execution.setFinishedDate(new Date());
				execution.setExecutionStatus(hasError() ? ExecutionStatus.FAILED.ordinal() : ExecutionStatus.FINISHED.ordinal());
				execution.setStatusMessageCheckLength(status());
				execution.setLogFile(createGson().toJson(getLog()));
				if (output() != null && output().exists()) {
					execution.setOutputName(getOutputName());
					execution.setOutputContentType(FileTypeMap.getDefaultFileTypeMap().getContentType(output()));
					FileInputStream is = new FileInputStream(output());
					try {
						execution.setOutputFile(IOUtils.toByteArray(is));
					} finally {
						is.close();
					}
				}
				hibSession.saveOrUpdate(execution);
			}
			hibSession.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	public static Gson createGson() {
		return new GsonBuilder()
		.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {
			@Override
			public JsonElement serialize(java.sql.Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.registerTypeAdapter(java.sql.Date.class, new JsonSerializer<java.sql.Date>() {
			@Override
			public JsonElement serialize(java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.setFieldNamingStrategy(new FieldNamingStrategy() {
			Pattern iPattern = Pattern.compile("i([A-Z])(.*)");
			@Override
			public String translateName(Field f) {
				Matcher matcher = iPattern.matcher(f.getName());
				if (matcher.matches())
					return matcher.group(1).toLowerCase() + matcher.group(2);
				else
					return f.getName();
			}
		})
		.setPrettyPrinting().create();
	}
	
	private static class TaskContext implements SessionContext {
		private UserContext iUser; 
		private PermissionCheck iCheck;
		private Map<String, Object> iAttributes = new HashMap<String, Object>();
		
		TaskContext(TaskExecution execution, PermissionCheck check) {
			iCheck = check;
			if (execution.getTask().getInputFile() != null) {
				String type = null;
				String name = "unknown.file";
				for (ScriptParameter p: execution.getTask().getScript().getParameters()) {
					if ("file".equalsIgnoreCase(p.getType())) type = p.getName();
				}
				if (type != null)
					for (TaskParameter p: execution.getTask().getParameters()) {
						if (type.equals(p.getName())) name = p.getValue();
					}
				iAttributes.put(UploadServlet.SESSION_LAST_FILE, 
						new BinaryFileItem(new BinaryFile(execution.getTask().getInputFile(), "application/octet-stream", name)));
			}
			iUser = new UniTimeUserContext(execution.getTask().getOwner(), execution.getTask().getSession());
		}

		@Override
		public boolean isAuthenticated() {
			return true;
		}

		@Override
		public UserContext getUser() {
			return iUser;
		}

		@Override
		public boolean isHttpSessionNew() { return false; }

		@Override
		public String getHttpSessionId() { return null; }

		@Override
		public Object getAttribute(String name) { return iAttributes.get(name); }

		@Override
		public void removeAttribute(String name) { iAttributes.remove(name); }

		@Override
		public void setAttribute(String name, Object value) { iAttributes.put(name, value); }

		@Override
		public void removeAttribute(SessionAttribute attribute) { removeAttribute(attribute.key()); }

		@Override
		public void setAttribute(SessionAttribute attribute, Object value) { setAttribute(attribute.key(), value); }

		@Override
		public Object getAttribute(SessionAttribute attribute) { return getAttribute(attribute.key()); }

		@Override
		public void checkPermission(Right right) {
			iCheck.checkPermission(getUser(), null, null, right);
		}

		@Override
		public void checkPermission(Serializable targetId, String targetType, Right right) {
			iCheck.checkPermission(getUser(), targetId, targetType, right);
		}

		@Override
		public void checkPermission(Object targetObject, Right right) {
			iCheck.checkPermission(getUser(), targetObject, right);
		}
		
		@Override
		public boolean hasPermission(Right right) {
			return iCheck.hasPermission(getUser(), null, null, right);
		}

		@Override
		public boolean hasPermission(Serializable targetId, String targetType, Right right) {
			return iCheck.hasPermission(getUser(), targetId, targetType, right);
		}

		@Override
		public boolean hasPermission(Object targetObject, Right right) {
			return iCheck.hasPermission(getUser(), targetObject, right);
		}
		
		@Override
		public void checkPermissionAnyAuthority(Right right, Qualifiable... filter) {
			iCheck.checkPermissionAnyAuthority(getUser(), null, null, right, filter);
		}

		@Override
		public void checkPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
			iCheck.checkPermissionAnyAuthority(getUser(), targetId, targetType, right, filter);
		}

		@Override
		public void checkPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
			iCheck.checkPermissionAnyAuthority(getUser(), targetObject, right, filter);
		}
		
		@Override
		public boolean hasPermissionAnyAuthority(Right right, Qualifiable... filter) {
			return iCheck.hasPermissionAnyAuthority(getUser(), null, null, right, filter);
		}

		@Override
		public boolean hasPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
			return iCheck.hasPermissionAnyAuthority(getUser(), targetId, targetType, right, filter);
		}

		@Override
		public boolean hasPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
			return iCheck.hasPermissionAnyAuthority(getUser(), targetObject, right, filter);
		}
		
		@Override
		public void checkPermissionAnySession(Right right, Qualifiable... filter) {
			iCheck.checkPermissionAnySession(getUser(), null, null, right, filter);
		}

		@Override
		public void checkPermissionAnySession(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
			iCheck.checkPermissionAnySession(getUser(), targetId, targetType, right, filter);
		}

		@Override
		public void checkPermissionAnySession(Object targetObject, Right right, Qualifiable... filter) {
			iCheck.checkPermissionAnySession(getUser(), targetObject, right, filter);
		}
		
		@Override
		public boolean hasPermissionAnySession(Right right, Qualifiable... filter) {
			return iCheck.hasPermissionAnySession(getUser(), null, null, right, filter);
		}

		@Override
		public boolean hasPermissionAnySession(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
			return iCheck.hasPermissionAnySession(getUser(), targetId, targetType, right, filter);
		}

		@Override
		public boolean hasPermissionAnySession(Object targetObject, Right right, Qualifiable... filter) {
			return iCheck.hasPermissionAnySession(getUser(), targetObject, right, filter);
		}
	}
}
