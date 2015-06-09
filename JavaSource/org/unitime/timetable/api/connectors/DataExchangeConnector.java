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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.api.XmlApiHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper.LogWriter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

/**
 * @author Tomas Muller
 */
@Service("/api/exchange")
public class DataExchangeConnector extends ApiConnector {

	@Override
	protected ApiHelper createHelper(HttpServletRequest request, HttpServletResponse response) {
		return new XmlApiHelper(request, response, sessionContext);
	}

	@Override
	@PreAuthorize("checkPermission('ApiDataExchangeConnector')")
	public void doGet(ApiHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
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
	@PreAuthorize("checkPermission('ApiDataExchangeConnector')")
	public void doPost(ApiHelper helper) throws IOException {
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
}
