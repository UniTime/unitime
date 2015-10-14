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
package org.unitime.timetable.api.connectors;

import java.io.IOException;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/json")
public class JsonConnector extends ApiConnector {
	@Autowired ApplicationContext applicationContext;
	
	protected <T extends GwtRpcResponse> void execute(final String type, ApiHelper helper) throws IOException {
		try {
			GwtRpcRequest<T> request = helper.getRequest(Class.forName(type));
			
			// retrieve implementation from given request
			GwtRpcImplementation<GwtRpcRequest<T>, T> implementation = (GwtRpcImplementation<GwtRpcRequest<T>, T>)applicationContext.getBean(type);
			
			// execute request
			T response = implementation.execute(request, helper.getSessionContext());
			
			// return response
			helper.setResponse(response);
		} catch (NoSuchBeanDefinitionException e) {
			throw new IllegalArgumentException("There is no implementation for the given request type.", e);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Given type was not recognized.", e);
		}
	}

	public void doPost(ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnyAuthority(Right.ApiJsonConnector);
		
		String type = helper.getParameter("type");
		if (type == null)
			throw new IllegalArgumentException("TYPE parameter not provided.");
		
		execute(type, helper);
	}
	
	@Override
	protected String getName() {
		return "json";
	}
}
