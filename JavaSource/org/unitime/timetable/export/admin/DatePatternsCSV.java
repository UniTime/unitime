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
import org.unitime.timetable.export.courses.ClassesCSV;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternsRequest;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;

@Service("org.unitime.timetable.export.Exporter:date-patterns.csv")
public class DatePatternsCSV extends ClassesCSV {
	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "date-patterns.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		GwtRpcImplementation<DatePatternsRequest, DatePatternsResponse> service = (GwtRpcImplementation<DatePatternsRequest, DatePatternsResponse>)applicationContext.getBean(DatePatternsRequest.class.getName());
		DatePatternsRequest request = new DatePatternsRequest();
		request.setExport(true);
		DatePatternsResponse response = service.execute(request, helper.getSessionContext());
		
		List<TableInterface> tables = new ArrayList<TableInterface>(1); tables.add(sorted(response.getTable(), helper));
		exportDataCsv(tables, helper);
	}

}
