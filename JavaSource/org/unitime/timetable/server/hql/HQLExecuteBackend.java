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
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.HQLExecuteRpcRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Table;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.model.dao.SavedHQLDAO;
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
			String query = null;
			Collection<SavedHQLParameter> parameters = null;
			if (request.getQuery() != null) {
				query = request.getQuery().getQuery();
				parameters = new ArrayList<SavedHQLParameter>();
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
			} else {
				SavedHQL hql = SavedHQLDAO.getInstance().get(request.getQuery().getId());
				query = hql.getQuery();
				parameters = hql.getParameters();
			}
			SavedHqlExportToCSV.execute(sessionContext.getUser(), out, 
					query,
					request.getOptions(),
					request.getFromRow(),
					request.getMaxRows(),
					parameters);
		
			return ret;
		} catch (PageAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
		}		
	}

}
