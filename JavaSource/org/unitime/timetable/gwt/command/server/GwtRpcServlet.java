/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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

import javax.servlet.ServletException;

import net.sf.cpsolver.ifs.util.JProf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.filter.QueryLogFilter;
import org.unitime.timetable.gwt.command.client.GwtRpcImplementedBy;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.model.QueryLog;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GwtRpcServlet extends RemoteServiceServlet implements GwtRpcService {
	private static final long serialVersionUID = 1L;
	private static Log sLog = LogFactory.getLog(GwtRpcServlet.class);
	private QueryLogFilter.Saver iSaver = null;
	
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

	@Override
	public <T extends GwtRpcResponse> T execute(GwtRpcRequest<T> request) throws GwtRpcException {
		// start time
		long t0 = JProf.currentTimeMillis();
		try {
			// retrieve implementation from given request
			GwtRpcImplementedBy annotation = request.getClass().getAnnotation(GwtRpcImplementedBy.class);
			if (annotation == null || annotation.value() == null || annotation.value().isEmpty()) {
				throw new GwtRpcException("Request " + request.getClass().getName().substring(request.getClass().getName().lastIndexOf('.') + 1) +
						" does not have the GwtRpcImplementedBy annotation.");
			}
			GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = (GwtRpcImplementation<GwtRpcRequest<T>, T>)Class.forName(annotation.value()).newInstance();
			
			// execute request
			T response = implementation.execute(request, new GwtRpcHelper(getThreadLocalRequest().getSession()));
			
			// log request
			log(request, response, null, JProf.currentTimeMillis() - t0);
			
			// return response
			return response;
		} catch (Throwable t) {
			// log exception
			log(request, null, t, JProf.currentTimeMillis() - t0);
			
			// re-throw exception as GwtRpcException or IsSerializable runtime exception
			if (t instanceof GwtRpcException) {
				sLog.info("Seen server exception: " + t.getMessage());
				throw (GwtRpcException)t;
			}
			if (t instanceof IsSerializable) {
				sLog.warn("Seen server exception: " + t.getMessage());
				if (t instanceof RuntimeException)
					throw (RuntimeException)t;
				else
					throw new GwtRpcException(t.getMessage(), t);
			}
			sLog.error("Seen exception: " + t.getMessage(), t);
			throw new GwtRpcException(t.getMessage());
		}
	}
	
	private <T extends GwtRpcResponse> void log(GwtRpcRequest<T> request, T response, Throwable exception, long time) {
		try {
			if (iSaver == null) return;
			QueryLog q = new QueryLog();
			String requestName = request.getClass().getName();
			if (requestName.indexOf('.') >= 0) requestName = requestName.substring(requestName.lastIndexOf('.') + 1);
			q.setUri("RPC:" + requestName);
			q.setType(QueryLog.Type.RPC.ordinal());
			q.setTimeStamp(new Date());
			q.setTimeSpent(time);
			try {
				q.setSessionId(getThreadLocalRequest().getSession().getId());
				User user = Web.getUser(getThreadLocalRequest().getSession()); 
				if (user != null) q.setUid(user.getId());
			} catch (IllegalStateException e) {}
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

}