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
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.api.XmlApiHelper;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
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
			helper.setResponse(DataExchangeHelper.exportDocument(type, session, ApplicationProperties.getProperties(), null));
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
			DataExchangeHelper.importDocument(document, helper.getSessionContext().isAuthenticated() ? helper.getSessionContext().getUser().getExternalUserId() : null, new Log() {
				protected void printMessage(Element e, Object message, Throwable t) {
					e.setText(message == null ? "null" : message.toString());
					if (t != null) {
						StringWriter writer = new StringWriter();
						PrintWriter pw = new PrintWriter(writer);
						t.printStackTrace(new PrintWriter(writer));
						pw.flush(); pw.close();
						e.addElement("pre")
							.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve")
							.addAttribute("style", "color: red;")
							.setText(StringEscapeUtils.escapeHtml4(writer.toString()));
					}
				}

				@Override
				public void warn(Object message, Throwable t) {
					printMessage(messages.addElement("p").addAttribute("style", "color: orange;"), message, t);
				}
				
				@Override
				public void warn(Object message) {
					printMessage(messages.addElement("p").addAttribute("style", "color: orange;"), message, null);
				}
				
				@Override
				public void trace(Object message, Throwable t) {
					printMessage(messages.addElement("p").addAttribute("style", "font-style: italic; color: gray;"), message, t);
				}
				
				@Override
				public void trace(Object message) {
					printMessage(messages.addElement("p").addAttribute("style", "font-style: italic; color: gray;"), message, null);
				}
				
				@Override
				public boolean isWarnEnabled() {
					return true;
				}
				
				@Override
				public boolean isTraceEnabled() {
					return false;
				}
				
				@Override
				public boolean isInfoEnabled() {
					return true;
				}
				
				@Override
				public boolean isFatalEnabled() {
					return true;
				}
				
				@Override
				public boolean isErrorEnabled() {
					return true;
				}
				
				@Override
				public boolean isDebugEnabled() {
					return false;
				}
				
				@Override
				public void info(Object message, Throwable t) {
					printMessage(messages.addElement("p"), message, t);
				}
				
				@Override
				public void info(Object message) {
					printMessage(messages.addElement("p"), message, null);
				}
				
				@Override
				public void fatal(Object message, Throwable t) {
					printMessage(messages.addElement("p").addAttribute("style", "font-weight: bold; color: red;"), message, t);
				}
				
				@Override
				public void fatal(Object message) {
					printMessage(messages.addElement("p").addAttribute("style", "font-weight: bold; color: red;"), message, null);
				}
				
				@Override
				public void error(Object message, Throwable t) {
					printMessage(messages.addElement("p").addAttribute("style", "color: red;"), message, t);
				}
				
				@Override
				public void error(Object message) {
					printMessage(messages.addElement("p").addAttribute("style", "color: red;"), message, null);
				}
				
				@Override
				public void debug(Object message, Throwable t) {
					printMessage(messages.addElement("p").addAttribute("style", "color: gray;"), message, t);					
				}
				
				@Override
				public void debug(Object message) {
					printMessage(messages.addElement("p").addAttribute("style", "color: gray;"), message, null);
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
