/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.api.connectors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.api.XmlApiHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper.LogWriter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/exchange")
public class DataExchangeConnector extends ApiConnector {

	@Override
	protected ApiHelper createHelper(HttpServletRequest request, HttpServletResponse response) {
		return new XmlApiHelper(request, response, sessionContext, getCacheMode());
	}

	@Override
	public void doGet(ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnyAuthority(Right.ApiDataExchangeConnector);
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");
		
		String type = helper.getParameter("type");
		if (type == null)
			throw new IllegalArgumentException("Export TYPE parameter not provided.");
		
		ApplicationProperties.setSessionId(sessionId);
			
		try {
			helper.setResponse(DataExchangeHelper.exportDocument(type, session, ApplicationProperties.getProperties(), new LogWriter() {
				@Override
				public void println(String message) {}
			}));
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnyAuthority(Right.ApiDataExchangeConnector);
		
		Document document = helper.getRequest(Document.class);
		Document output = DocumentHelper.createDocument();
		final Element messages = output.addElement("html");
		try {
			DataExchangeHelper.importDocument(document, helper.getSessionContext().isAuthenticated() ? helper.getSessionContext().getUser().getExternalUserId() : null, new LogWriter() {
				@Override
				public void println(String message) {
					messages.addElement("p").setText(message);
				}
			});
			helper.setResponse(output);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	protected String getName() {
		return "exchange";
	}
}
