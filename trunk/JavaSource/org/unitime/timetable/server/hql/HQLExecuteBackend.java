/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.server.hql;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.hql.SavedHqlExportToCSV;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLExecuteRpcRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Table;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(HQLExecuteRpcRequest.class)
public class HQLExecuteBackend implements GwtRpcImplementation<HQLExecuteRpcRequest, Table> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
    @Autowired 
	private SessionContext sessionContext;

	@Override
	@PreAuthorize("checkPermission('HQLReports')")
	public Table execute(HQLExecuteRpcRequest request, SessionContext context) {
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
			SavedHqlExportToCSV.execute(sessionContext.getUser(), out, 
					request.getQuery(),
					request.getOptions(),
					request.getFromRow(),
					request.getMaxRows());
		
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
		}		
	}

}
