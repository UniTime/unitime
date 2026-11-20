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
package org.unitime.timetable.server.hql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.hql.SavedHqlExportToCSV;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseLong;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLTestRpcRequest;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(HQLTestRpcRequest.class)
public class HQLTestBackend implements GwtRpcImplementation<HQLTestRpcRequest, GwtRpcResponseLong>{

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public GwtRpcResponseLong execute(HQLTestRpcRequest request, SessionContext context) {
		if (request.getQuery().getId() != null)
			context.checkPermission(request.getQuery().getId(), Right.HQLReportEdit);
		else
			context.checkPermission(Right.HQLReportAdd);
		String query = request.getQuery().getQuery();
		Collection<SavedHQLParameter> parameters = new ArrayList<SavedHQLParameter>();
		if (request.getQuery().hasParameters()) {
			for (SavedHQLInterface.Parameter p: request.getQuery().getParameters()) {
				SavedHQLParameter parameter = new SavedHQLParameter();
				parameter.setDefaultValue(p.getDefaultValue());
				parameter.setLabel(p.getLabel());
				parameter.setName(p.getName());
				parameter.setType(p.getType());
				parameters.add(parameter);
			}
		}
		final GwtRpcResponseLong ret = new GwtRpcResponseLong();
		ret.setValue(0l);
		Printer out = new Printer() {
			int lines = 0;
			@Override
			public void printLine(String... fields) throws IOException {
				lines ++;
				ret.setValue((long)lines);
			}
			
			@Override
			public void printHeader(String... fields) throws IOException {
			}
			
			@Override
			public void hideColumn(int col) {}
			
			@Override
			public String getContentType() { return null; }
			
			@Override
			public void flush() throws IOException {}
			
			@Override
			public void close() throws IOException {}
		};
		SavedHqlExportToCSV.execute(context.getUser(), out, 
				query,
				new ArrayList<SavedHQLInterface.IdValue>(),
				0,
				101,
				parameters);
		return ret;
	}

}
