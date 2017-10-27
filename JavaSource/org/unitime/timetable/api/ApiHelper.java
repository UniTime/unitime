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
package org.unitime.timetable.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public interface ApiHelper {
	public Long getAcademicSessionId();
	
	public SessionContext getSessionContext();
		
	public <P> P getRequest(Type requestType) throws IOException;
	
	public <R> void setResponse(R response) throws IOException;
	
	public void sendError(int code) throws IOException;
	
	public void sendError(int code, String message) throws IOException;
	
	public void sendError(int code, Throwable error) throws IOException;
	
	public String getParameter(String name);
	
	public String[] getParameterValues(String name);
	
	public Enumeration<String> getParameterNames();
	
	public org.hibernate.Session getHibSession();
	
	public void close();
	
	public String getOptinalParameter(String name, String defaultValue);
	
	public String getRequiredParameter(String name);
	
	public Integer getOptinalParameterInteger(String name, Integer defaultValue);
	
	public Integer getRequiredParameterInteger(String name);
	
	public Long getOptinalParameterLong(String name, Long defaultValue);
	
	public Long getRequiredParameterLong(String name);
	
	public Boolean getOptinalParameterBoolean(String name, Boolean defaultValue);
	
	public Boolean getRequiredParameterBoolean(String name);
}
