/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Enumeration;

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
	
	public String getParameter(String name);
	
	public String[] getParameterValues(String name);
	
	public Enumeration<String> getParameterNames();
}
