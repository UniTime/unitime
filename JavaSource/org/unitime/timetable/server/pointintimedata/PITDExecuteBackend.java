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
package org.unitime.timetable.server.pointintimedata;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.pointintimedata.PointInTimeDataReportsExportToCSV;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.PITDExecuteRpcRequest;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface.Table;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Stephanie Schluttenhofer
 */
@GwtRpcImplements(PITDExecuteRpcRequest.class)
public class PITDExecuteBackend implements GwtRpcImplementation<PITDExecuteRpcRequest, Table> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
    @Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("checkPermission('PointInTimeDataReports')")
	public Table execute(PITDExecuteRpcRequest request, SessionContext context) {
		try {
			final Table ret = new Table();
			Printer out = new Printer() {
				
				@Override
				public void printLine(String... fields) throws IOException {
					ret.add(fields);
				}
				
				@Override
				public void printHeader(String... fields) throws IOException {
					ret.add(fields);
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
			PointInTimeDataReportsExportToCSV.execute(sessionContext.getUser(), out, 
					request.getReport(),
					request.getParameters());
		
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
		}		
	}

}
