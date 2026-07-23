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
package org.unitime.timetable.export.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.courses.ClassesPDF;
import org.unitime.timetable.gwt.client.admin.LastChangesPage.LastChangesRequest;
import org.unitime.timetable.gwt.client.admin.LastChangesPage.LastChangesResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;

@Service("org.unitime.timetable.export.Exporter:last-changes.pdf")
public class ExportLastChangesPDF extends ClassesPDF {
	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "last-changes.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		GwtRpcImplementation<LastChangesRequest, LastChangesResponse> service = (GwtRpcImplementation<LastChangesRequest, LastChangesResponse>)applicationContext.getBean(LastChangesRequest.class.getName());
		LastChangesRequest request = new LastChangesRequest();
		request.setFilter(new Filter(helper));
		LastChangesResponse response = service.execute(request, helper.getSessionContext());
		
		List<TableInterface> tables = new ArrayList<TableInterface>(1); tables.add(sorted(response, helper));
		exportDataPdf(tables, helper);
	}

}
