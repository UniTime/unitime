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

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public abstract class FilterBoxBackend<T extends FilterRpcRequest> implements GwtRpcImplementation<T, FilterRpcResponse> {
	
	@Override
	public FilterRpcResponse execute(T request, SessionContext context) {
		
		FilterRpcResponse response = new FilterRpcResponse();
		
		if (context.isAuthenticated())
			request.setOption("user", context.getUser().getExternalUserId());
		
		if (request.getSessionId() == null && context.isAuthenticated())
			request.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		switch (request.getCommand()) {
			case LOAD:
				load(request, response, context);
				break;
			case SUGGESTIONS:
				suggestions(request, response, context);
				break;
			case ENUMERATE:
				enumarate(request, response, context);
				break;
		}
		
		return response;
	}
	
	public abstract void load(T request, FilterRpcResponse response, SessionContext context);
	
	public abstract void suggestions(T request, FilterRpcResponse response, SessionContext context);
	
	public abstract void enumarate(T request, FilterRpcResponse response, SessionContext context);
}
