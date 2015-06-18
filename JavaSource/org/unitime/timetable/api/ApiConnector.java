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

import javax.servlet.http.HttpServletResponse;

/**
 * @author Tomas Muller
 */
public abstract class ApiConnector {
	
	public void doGet(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	public void doPut(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	public void doPost(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	public void doDelete(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
}