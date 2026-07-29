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
package org.unitime.timetable.server.administration.dataexchange;

import org.unitime.timetable.gwt.client.admin.DataExchangePage.DataExchangeConfigRequest;
import org.unitime.timetable.gwt.client.admin.DataExchangePage.DataExchangeConfigResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(DataExchangeConfigRequest.class)
public class DataExchangeConfigBackend implements GwtRpcImplementation<DataExchangeConfigRequest, DataExchangeConfigResponse>{

	@Override
	public DataExchangeConfigResponse execute(DataExchangeConfigRequest request, SessionContext context) {
		context.checkPermission(Right.DataExchange);
		
		DataExchangeConfigResponse response = new DataExchangeConfigResponse();
		
		for (ExportType et: ExportType.values())
			response.addExportType(et.name(), et.getLabel());
		response.setEmail(context.getUser().getEmail());
		
		return response;
	}

}
