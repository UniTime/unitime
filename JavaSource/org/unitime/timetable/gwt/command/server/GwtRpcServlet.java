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
package org.unitime.timetable.gwt.command.server;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.JProf;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.filter.QueryLogFilter;
import org.unitime.timetable.gwt.command.client.GwtRpcCancelledException;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.util.Formats;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Tomas Muller
 */
public class GwtRpcServlet extends RemoteServiceServlet implements GwtRpcService {
	private static final long serialVersionUID = 1L;
	private static Log sLog = LogFactory.getLog(GwtRpcServlet.class);
	private QueryLogFilter.Saver iSaver = null;
	private static IdGenerator sIdGenerator = new IdGenerator();
	private static Map<Long, Execution> sExecutions = new Hashtable<Long, Execution>();
	private Gson iGson = null;
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}
	
	protected PermissionCheck getPermissionCheck() {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return (PermissionCheck)applicationContext.getBean("unitimePermissionCheck");
	}

	
	@Override
	public void init() throws ServletException {
		iSaver = new QueryLogFilter.Saver();
		iSaver.setName("GwtRpcLogSaver");
		iSaver.start();
		iGson = new GsonBuilder()
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
				.create();
	}
	
	@Override
	public void destroy() {
		if (iSaver != null) iSaver.interrupt();
	}
	
	public static <T extends GwtRpcResponse> GwtRpcImplementation<GwtRpcRequest<T>, T> getImplementation(Class<? extends GwtRpcRequest<T>> requestClass, ApplicationContext applicationContext) throws BeansException {
		return (GwtRpcImplementation<GwtRpcRequest<T>, T>)applicationContext.getBean(requestClass.getName());
	}
	
	protected <T extends GwtRpcResponse> GwtRpcImplementation<GwtRpcRequest<T>, T> getImplementation(GwtRpcRequest<T> request) throws Exception {
		return getImplementation((Class<GwtRpcRequest<T>>)request.getClass(), WebApplicationContextUtils.getWebApplicationContext(getServletContext()));
	}
	
	public static <T extends GwtRpcResponse> T execute(GwtRpcRequest<T> request, ApplicationContext applicationContext, SessionContext sessionContext) throws GwtRpcException {
		try {
			// retrieve implementation from given request
			GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = getImplementation((Class<GwtRpcRequest<T>>)request.getClass(), applicationContext);
			
			// execute request
			T response = implementation.execute(request, sessionContext);
			
			// return response
			return response;
		} catch (Throwable t) {
			// re-throw exception as GwtRpcException or IsSerializable runtime exception
			if (t instanceof GwtRpcException) {
				GwtRpcException e = (GwtRpcException)t;
				if (e.hasCause())
					sLog.warn("Seen server exception: " + e.getMessage(), e.getCause());
				else
					sLog.info("Seen server exception: " + e.getMessage());
				throw e;
			}
			if (t instanceof IsSerializable) {
				if (t.getCause() != null)
					sLog.error("Seen server exception: " + t.getMessage(), t);
				else
					sLog.warn("Seen server exception: " + t.getMessage(), t);
				throw new GwtRpcException(t.getMessage(), t);
			}
			sLog.error("Seen exception: " + t.getMessage(), t);
			throw new GwtRpcException(t.getMessage());
		}
	}
	
	@Override
	public <T extends GwtRpcResponse> T execute(GwtRpcRequest<T> request) throws GwtRpcException {
		// start time
		long t0 = JProf.currentTimeMillis();
		GwtRpcLogging logging = null;
		// create helper
		try {
			// retrieve implementation from given request
			GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = getImplementation(request);
			
			// get logging
			logging = implementation.getClass().getAnnotation(GwtRpcLogging.class);
			
			// execute request
			T response = implementation.execute(request, getSessionContext());
			
			// log request
			log(request, response, null, JProf.currentTimeMillis() - t0, getSessionContext(), logging);
			
			// return response
			return response;
		} catch (Throwable t) {
			// log exception
			log(request, null, t, JProf.currentTimeMillis() - t0, getSessionContext(), logging);
			
			// re-throw exception as GwtRpcException or IsSerializable runtime exception
			if (t instanceof GwtRpcException) {
				GwtRpcException e = (GwtRpcException)t;
				if (e.hasCause())
					sLog.warn("Seen server exception: " + e.getMessage(), e);
				else
					sLog.info("Seen server exception: " + e.getMessage());
				throw e;
			}
			if (t instanceof AccessDeniedException)
				throw new PageAccessException(t.getMessage(), t);
			if (t instanceof IsSerializable) {
				if (t.getCause() != null)
					sLog.error("Seen server exception: " + t.getMessage(), t);
				else
					sLog.warn("Seen server exception: " + t.getMessage(), t);
				throw new GwtRpcException(t.getMessage(), t);
			}
			sLog.error("Seen exception: " + t.getMessage(), t);
			throw new GwtRpcException(t.getMessage());
		}
	}
	
	private <T extends GwtRpcResponse> void log(GwtRpcRequest<T> request, T response, Throwable exception, long time, SessionContext context, GwtRpcLogging logging) {
		try {
			if (iSaver == null) return;
			if (logging != null) {
				switch (logging.value()) {
				case DISABLED:
					return;
				case ON_EXCEPTION:
					if (exception != null) return;
				}
			}
			QueryLog q = new QueryLog();
			String requestName = request.getClass().getSimpleName();
			q.setUri("RPC:" + requestName);
			q.setType(QueryLog.Type.RPC.ordinal());
			q.setTimeStamp(new Date());
			q.setTimeSpent(time);
			q.setSessionId(context.getHttpSessionId());
			q.setUid(context.isAuthenticated() ? context.getUser().getTrueExternalUserId() : null);
			if (ApplicationProperty.QueryLogJSON.isTrue()) {
				q.setQuery(iGson.toJson(request));
			} else {
				q.setQuery(request.toString());
			}
			if (exception != null) {
				Throwable t = exception;
				String ex = "";
				while (t != null) {
					String clazz = t.getClass().getName();
					if (clazz.indexOf('.') >= 0) clazz = clazz.substring(1 + clazz.lastIndexOf('.'));
					if (!ex.isEmpty()) ex += "\n";
					ex += clazz + ": " + t.getMessage();
					if (t.getStackTrace() != null && t.getStackTrace().length > 0)
						ex += " (at " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber() + ")";
					t = t.getCause();
				}
				if (!ex.isEmpty())
					q.setException(ex);
			}
			iSaver.add(q);
		} catch (Throwable t) {
			sLog.warn("Failed to log a request: " + t.getMessage(), t);
		}
	}

	@Override
	public <T extends GwtRpcResponse> Long executeAsync(GwtRpcRequest<T> request) throws GwtRpcException {
		try {
			Execution<GwtRpcRequest<T>, T> execution = new Execution<GwtRpcRequest<T>, T>(request);
			synchronized (sExecutions) {
				sExecutions.put(execution.getExecutionId(), execution);
			}
			execution.start();
			return execution.getExecutionId();
		} catch (Exception e) {
			sLog.warn("Execute async failed: " + e.getMessage());
			throw new GwtRpcException(e.getMessage(), e);
		}
	}

	@Override
	public <T extends GwtRpcResponse> T waitForResults(Long executionId) throws GwtRpcException {
		try {
			Execution<GwtRpcRequest<T>, T> execution = null;
			synchronized (sExecutions) {
				execution = sExecutions.get(executionId);
			}
			if (execution == null) throw new GwtRpcException("No execution with given id found.");
			try {
				execution.waitToFinish();
			} catch (InterruptedException e) {
			}
			synchronized (sExecutions) {
				sExecutions.remove(executionId);
			}
			if (execution.getException() != null) throw execution.getException();
			return execution.getResponse();
		} catch (GwtRpcCancelledException e) {
			throw e;
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			sLog.warn("Wait for results failed: " + e.getMessage());
			throw new GwtRpcException(e.getMessage(), e);
		}
	}

	@Override
	public Boolean cancelExecution(Long executionId) throws GwtRpcException {
		try {
			Execution execution = null;
			synchronized (sExecutions) {
				execution = sExecutions.get(executionId);
			}
			if (execution == null) return false;
			execution.cancelExecution();
			return true;
		} catch (Exception e) {
			sLog.warn("Cancel execution failed: " + e.getMessage());
			throw new GwtRpcException(e.getMessage(), e);
		}
	}
		
	private static class IdGenerator {
		long iNextId = 0;
		synchronized Long generatedId() {
			return iNextId++;
		}
	}
	
	private class Execution<R extends GwtRpcRequest<T>, T extends GwtRpcResponse> extends Thread {
		R iRequest;
		T iResponse = null;
		SessionContext iContext = null;
		GwtRpcException iException = null;
		Thread iWaitingThread = null;
		long iExecutionId;
		boolean iRunning = false;
		String iLocale = null;
		
		Execution(R request) {
			setName("RPC:" + request);
			setDaemon(true);
			iRequest = request;
			iExecutionId = sIdGenerator.generatedId();
			iContext = new GwtRpcHelper(getSessionContext(), getPermissionCheck());
			iLocale = Localization.getLocale();
		}

		@Override
		public void run() {
			iRunning = true;
			Localization.setLocale(iLocale);
			ApplicationProperties.setSessionId(iContext.getUser() == null ? null : iContext.getUser().getCurrentAcademicSessionId());
			// start time
			long t0 = JProf.currentTimeMillis();
			GwtRpcLogging logging = null;
			try {
				// retrieve implementation from given request
				GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = getImplementation(iRequest);
				
				// get logging
				logging = implementation.getClass().getAnnotation(GwtRpcLogging.class);
				
				// execute request
				iResponse = implementation.execute(iRequest, iContext);
				
				// log request
				log(iRequest, iResponse, null, JProf.currentTimeMillis() - t0, iContext, logging);
			} catch (Throwable t) {
				// log exception
				log(iRequest, null, t, JProf.currentTimeMillis() - t0, iContext, logging);
				
				// re-throw exception as GwtRpcException or IsSerializable runtime exception
				if (t instanceof GwtRpcException) {
					iException = (GwtRpcException)t;
					if (iException.hasCause())
						sLog.warn("Seen server exception: " + t.getMessage(), t.getCause());
					else
						sLog.info("Seen server exception: " + t.getMessage());
				} else if (t instanceof IsSerializable) {
					if (t.getCause() != null)
						sLog.error("Seen server exception: " + t.getMessage(), t);
					else
						sLog.warn("Seen server exception: " + t.getMessage(), t);
					iException = new GwtRpcException(t.getMessage(), t);
				} else {
					sLog.error("Seen exception: " + t.getMessage(), t);
					iException = new GwtRpcException(t.getMessage());
				}
			} finally {
				Localization.removeLocale();
				Formats.removeFormats();
				ApplicationProperties.setSessionId(null);
				_RootDAO.closeCurrentThreadSessions();
			}
			synchronized (this) {
				iWaitingThread = null;
				iRunning = false;
				iContext = null;
			}
		}
		
		void waitToFinish() throws InterruptedException {
			synchronized (this) {
				if (iRunning) {
					iWaitingThread = Thread.currentThread();
					join();
				}
			}
		}
		
		void cancelExecution() {
			synchronized (this) {
				iException = new GwtRpcCancelledException("Operation cancelled by the user.");
				if (iWaitingThread != null) iWaitingThread.interrupt();
			}
			interrupt();
		}
		
		T getResponse() { return iResponse; }
		
		GwtRpcException getException() { return iException; }
		
		Long getExecutionId() { return iExecutionId; }
	}
}