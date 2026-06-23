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
package org.unitime.timetable.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.StartupService;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.page.MainPage.MainPageRequest;
import org.unitime.timetable.gwt.client.page.MainPage.MainPageResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.tags.Registration;
import org.unitime.timetable.util.Constants;

@GwtRpcImplements(MainPageRequest.class)
public class MainPageBackend implements GwtRpcImplementation<MainPageRequest, MainPageResponse>{
	
	@Autowired StartupService startupService;

	@Override
	public MainPageResponse execute(MainPageRequest request, SessionContext context) {
		MainPageResponse response = new MainPageResponse();
		response.setVersion(Constants.getVersion());
		response.setSystemMessage(ApplicationProperty.SystemMessage.value());
		
		Throwable t = startupService.getInitializationException();
		while (t != null) {
			String clazz = t.getClass().getSimpleName();
			response.addInitializationError(clazz + ": " + t.getMessage() + (t.getStackTrace() != null && t.getStackTrace().length > 0 ? " (at " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber() + ")": ""));
			t = t.getCause();
		}
		
		Registration reg = new Registration();
		reg.setUpdate(true);
		reg.setRefresh(request.isRefresh());
		reg.setUniTimeUrl(request.getUniTimeUrl());
		reg.populate(response, context);
		
		return response;
	}

}
