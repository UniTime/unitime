/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.command.server;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;

import net.sf.cpsolver.ifs.util.JProf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.filter.QueryLogFilter;
import org.unitime.timetable.gwt.command.client.GwtRpcCancelledException;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.util.Formats;

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
		// create helper
		try {
			// retrieve implementation from given request
			GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = getImplementation(request);
			
			// execute request
			T response = implementation.execute(request, getSessionContext());
			
			// log request
			log(request, response, null, JProf.currentTimeMillis() - t0, getSessionContext());
			
			// return response
			return response;
		} catch (Throwable t) {
			// log exception
			log(request, null, t, JProf.currentTimeMillis() - t0, getSessionContext());
			
			// re-throw exception as GwtRpcException or IsSerializable runtime exception
			if (t instanceof GwtRpcException) {
				GwtRpcException e = (GwtRpcException)t;
				if (e.hasCause())
					sLog.warn("Seen server exception: " + e.getMessage(), e);
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
	
	private <T extends GwtRpcResponse> void log(GwtRpcRequest<T> request, T response, Throwable exception, long time, SessionContext context) {
		try {
			if (iSaver == null) return;
			QueryLog q = new QueryLog();
			String requestName = request.getClass().getName();
			if (requestName.indexOf('.') >= 0) requestName = requestName.substring(requestName.lastIndexOf('.') + 1);
			q.setUri("RPC:" + requestName);
			q.setType(QueryLog.Type.RPC.ordinal());
			q.setTimeStamp(new Date());
			q.setTimeSpent(time);
			q.setSessionId(context.getHttpSessionId());
			q.setUid(context.isAuthenticated() ? context.getUser().getExternalUserId() : null);
			q.setQuery(request.toString());
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
			try {
				// retrieve implementation from given request
				GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = getImplementation(iRequest);
				
				// execute request
				iResponse = implementation.execute(iRequest, iContext);
				
				// log request
				log(iRequest, iResponse, null, JProf.currentTimeMillis() - t0, iContext);
			} catch (Throwable t) {
				// log exception
				log(iRequest, null, t, JProf.currentTimeMillis() - t0, iContext);
				
				// re-throw exception as GwtRpcException or IsSerializable runtime exception
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