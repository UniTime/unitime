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
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypesRequest;
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypesResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;

@Service("org.unitime.timetable.export.Exporter:distribution-types.pdf")
public class DistributionTypesPDF extends ClassesPDF {
	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "distribution-types.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		GwtRpcImplementation<DistributionTypesRequest, DistributionTypesResponse> service = (GwtRpcImplementation<DistributionTypesRequest, DistributionTypesResponse>)applicationContext.getBean(DistributionTypesRequest.class.getName());
		DistributionTypesRequest request = new DistributionTypesRequest();
		request.setExport(true);
		DistributionTypesResponse response = service.execute(request, helper.getSessionContext());
		
		List<TableInterface> tables = new ArrayList<TableInterface>(1); tables.add(sorted(response.getTable(), helper));
		exportDataPdf(tables, helper);
	}

}
