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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tomas Muller
 */
public class ErrorMessage {
	int code;
	String message;
	Map<String, String> parameters = new HashMap<String, String>();
	List<String> exception;
	
	public ErrorMessage(int code, ApiHelper helper) {
		this.code = code;
		for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
			String name = e.nextElement();
			parameters.put(name, helper.getParameter(name));
		}
	}
	
	public ErrorMessage(int code, String message, ApiHelper helper) {
		this(code, helper);
		this.message = message;
	}
	
	public ErrorMessage(int code, Throwable error, ApiHelper helper) {
		this(code, helper);
		this.message = error.getMessage();
		StringWriter trace = new StringWriter();
		error.printStackTrace(new PrintWriter(trace));
		exception = new ArrayList<String>();
		exception.add(error.toString());
		for (StackTraceElement e: error.getStackTrace())
			exception.add("at " + e.toString());
		Throwable cause = error.getCause();
		while (cause != null) {
			exception.add("Caused by: " + cause.toString());
			for (StackTraceElement e: cause.getStackTrace())
				exception.add("at " + e.toString());
			cause = cause.getCause();
		}
	}
}
