/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.tags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.restlet.resource.ClientResource;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class Registration extends BodyTagSupport {
	private static final long serialVersionUID = 6840487122075978529L;

	private static Log sLog = LogFactory.getLog(Registration.class);

	private static String sMessage = null;
	private static String sKey = null;
	private static boolean sRegistered = false;
	private static String sNote = null;
	
	private static enum Method { hasMessage, message, status };
	private Method iMethod = Method.status;
	public String getMethod() { return iMethod.toString(); }
	public void setMethod(String method) { iMethod = Method.valueOf(method); }
	
	private boolean iUpdate = false;
	public void setUpdate(boolean update) { iUpdate = update; }
	public boolean isUpdate() { return iUpdate; }
	
	private static long sLastRefresh = -1;
	
	private static enum Obtrusiveness {
		high,
		medium,
		low,
		none
	}
	
    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }
	
	private synchronized void init() {
		sLastRefresh = System.currentTimeMillis();
		try {
			File regFile = new File(ApplicationProperties.getDataFolder(), "unitime.reg");
			if (sKey == null && regFile.exists()) {
				Properties reg = new Properties();
				FileInputStream in = new FileInputStream(regFile);
				try {
					reg.load(in);
				} finally {
					in.close();
				}
				sKey = reg.getProperty("key");
			}
			
			HashMap<String, String> registration = new HashMap<String, String>();
			if (sKey != null)
				registration.put("key", sKey);
			else
				sLog.debug("No registration key found..." );
			registration.put("version", Constants.getVersion());
			registration.put("sessions", String.valueOf(QueryLog.getNrSessions(31)));
			registration.put("users", String.valueOf(QueryLog.getNrActiveUsers(31)));
			registration.put("url", pageContext.getRequest().getScheme()+"://"+pageContext.getRequest().getServerName()+":"+pageContext.getRequest().getServerPort()+
					((HttpServletRequest)pageContext.getRequest()).getContextPath());
			sLog.debug("Sending the following registration info: " + registration);
			
			Document input = DocumentHelper.createDocument();
			Element regEl = input.addElement("registration");
			for (Map.Entry<String, String> entry: registration.entrySet()) {
				regEl.addElement(entry.getKey()).setText(entry.getValue());
			}
			
			sLog.debug("Contacting registration service..." );
			ClientResource cr = new ClientResource("http://register.unitime.org/xml");
			
			StringWriter w = new StringWriter();
			new XMLWriter(w, OutputFormat.createPrettyPrint()).write(input);
			w.flush(); w.close();

			String result = cr.post(w.toString(), String.class);
			sLog.info("Registration information received." );

			try { cr.release(); } catch (Exception e) {}

			StringReader r = new StringReader(result);
			Document output = (new SAXReader()).read(r);
			r.close();
			
			HashMap<String, String> ret = new HashMap<String, String>();
			for (Element e: (List<Element>)output.getRootElement().elements()) {
				ret.put(e.getName(), e.getText());
			}
			
			String newKey = ret.get("key");
			if (!newKey.equals(sKey)) {
				sKey = newKey;
				sLog.debug("New registration key received..." );
				Properties reg = new Properties();
				reg.setProperty("key", sKey);
				FileOutputStream out = new FileOutputStream(regFile);
				try {
					reg.store(out, "UniTime " + Constants.VERSION + " registration file, please do not delete or modify.");
					out.flush();
				} finally {
					out.close();
				}
			}
			
			sMessage = ret.get("message");
			sNote = ret.get("note");
			sRegistered = "1".equals(ret.get("registered"));
		} catch (Exception e) {
			sLog.error("Validation failed: " + e.getMessage(), e);
		}
	}
	
	private void refresh() {
		if ("1".equals(pageContext.getRequest().getParameter("refresh")) || System.currentTimeMillis() - sLastRefresh > 60 * 60 * 1000)
			init();
	}
	
	public int doStartTag() throws JspException {
		if (sLastRefresh < 0) init();
		switch (iMethod) {
		case hasMessage:
			if (!getSessionContext().hasPermission(Right.Registration))
				return SKIP_BODY;
			try {
				refresh();
				if (Registration.sNote != null && !Registration.sNote.isEmpty())
					return EVAL_BODY_INCLUDE;
			} catch (Exception e) {}
			return SKIP_BODY;
		default:
			return EVAL_BODY_BUFFERED;
		}
	}
	
	public int doEndTag() throws JspException {
		switch (iMethod) {
		case hasMessage:
			return EVAL_PAGE;
		case message:
			try {
				if (Registration.sNote != null) pageContext.getOut().println(Registration.sNote);
			} catch (IOException e) {}
			return EVAL_PAGE;
		case status:
			if (sMessage == null) return EVAL_PAGE;
			try {
				pageContext.getOut().println(sMessage);
				if (isUpdate()) {
					Obtrusiveness obtrusiveness = Obtrusiveness.valueOf(ApplicationProperties.getProperty("unitime.registration.obtrusiveness", Obtrusiveness.high.name()));
					if (getSessionContext().hasPermission(Right.Registration)) {
						String backUrl = URLEncoder.encode(((HttpServletRequest)pageContext.getRequest()).getRequestURL().toString() + "?refresh=1", "ISO-8859-1");
						pageContext.getOut().println(
								"<br><span style=\"font-size: x-small;\">Click <a "+
								"onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" " +
								"onClick=\"showGwtDialog('UniTime " + Constants.VERSION + " Registration', 'https://unitimereg.appspot.com?key=" + sKey + "&back=" + backUrl + "', '750px', '75%');\" " +
								"title='UniTime " + Constants.VERSION + " Registration'>here</a> to " +
								(sRegistered ? "update the current registration" : "register") + "." +
								"</span>");
						switch (obtrusiveness) {
						case low:
							if (sRegistered) break;
						case high:
						case medium:
							pageContext.getOut().println("<script>function gwtOnLoad() { gwtShowMessage(\"" + sMessage +
									"<br><span style='font-size: x-small;'>Click <a " +
									"onMouseOver=\\\"this.style.cursor='hand';this.style.cursor='pointer';\\\" " +
									"onCLick=\\\"showGwtDialog('UniTime " + Constants.VERSION + " Registration', 'https://unitimereg.appspot.com?key=" + sKey + "&back=" + backUrl + "', '750px', '75%');\\\" " +
									"title='UniTime " + Constants.VERSION + " Registration'>here</a> to " +
									(sRegistered ? "update the current registration" : "register") + "." +
									"</span>\"); }</script>");							
						}
					} else {
						switch (obtrusiveness) {
						case medium:
							if (sRegistered) break;
						case high:
							pageContext.getOut().println("<script>function gwtOnLoad() { gwtShowMessage(\"" + sMessage + "\"); }</script>");
						}
					}
				}
			} catch (IOException e) {}
			return EVAL_PAGE;
		default:
			return EVAL_PAGE;
		}
	}
}
