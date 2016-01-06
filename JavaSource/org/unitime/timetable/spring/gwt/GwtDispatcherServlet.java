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
package org.unitime.timetable.spring.gwt;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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
	private Gson iGson = null;
	private static ThreadLocal<GwtCallInfo> sLastQuery = new ThreadLocal<GwtCallInfo>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
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
    public String processCall(String payload) throws SerializationException {
        try {
            Object handler = getBean(getThreadLocalRequest());
            RPCRequest rpcRequest = RPC.decodeRequest(payload, handler.getClass(), this);
            onAfterRequestDeserialized(rpcRequest);
            try {
            	if (ApplicationProperty.QueryLogJSON.isTrue())
            		sLastQuery.set(new GwtCallInfo(rpcRequest.getMethod().getDeclaringClass().getSimpleName() + "#" + rpcRequest.getMethod().getName(), iGson.toJson(rpcRequest.getParameters())));
            	else
            		sLastQuery.set(new GwtCallInfo(rpcRequest.getMethod().getDeclaringClass().getSimpleName() + "#" + rpcRequest.getMethod().getName(), payload));
            } catch (Throwable t) {}
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
	
	public static GwtCallInfo getLastQuery() {
		GwtCallInfo q = sLastQuery.get();
		if (q != null) sLastQuery.remove();
		return q;
	}
	
	public static class GwtCallInfo {
		String iTarget;
		String iQuery;
		
		public GwtCallInfo(String target, String query) {
			iTarget = target;
			iQuery = query;
		}
		
		public String getTarget() {
			return iTarget;
		}
		
		public String getQuery() {
			return iQuery;
		}
	}
}