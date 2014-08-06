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
package org.unitime.timetable.spring.gwt;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.UnexpectedException;

/**
 * @author Tomas Muller
 */
public class GwtDispatcherServlet extends RemoteServiceServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
    public String processCall(String payload) throws SerializationException {
        try {
            Object handler = getBean(getThreadLocalRequest());
            RPCRequest rpcRequest = RPC.decodeRequest(payload, handler.getClass(), this);
            onAfterRequestDeserialized(rpcRequest);
            try {
            	return RPC.invokeAndEncodeResponse(handler, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest.getSerializationPolicy());
            } catch (UnexpectedException ex) {
            	if (ex.getCause() instanceof AccessDeniedException)
            		return RPC.encodeResponseForFailure(rpcRequest.getMethod(), new PageAccessException(ex.getCause().getMessage()), rpcRequest.getSerializationPolicy());
            	throw ex;
            }
        } catch (IncompatibleRemoteServiceException ex) {
        	return RPC.encodeResponseForFailure(null, ex);
        }
	}

	protected String getService(HttpServletRequest request) {
		String url = request.getRequestURI();
        String service = url.substring(url.lastIndexOf("/") + 1);
        return service;
	}
	
	protected Object getBean(HttpServletRequest request) {
		String service = getService(request);
		Object bean = getBean(service);
		return bean;
	}
	
	protected Object getBean(String name) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return applicationContext.getBean(name);
	}
}