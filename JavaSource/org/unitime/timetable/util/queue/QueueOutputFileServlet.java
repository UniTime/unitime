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
package org.unitime.timetable.util.queue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
public class QueueOutputFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}

	protected QueueProcessor getQueueProcessor() {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		SolverServerService solverServerService = (SolverServerService)applicationContext.getBean("solverServerService");
		return solverServerService.getQueueProcessor();
	}
	
	public static Map<String, String> decode(String text) throws UnsupportedEncodingException {
		Map<String, String> params = new HashMap<String, String>();
		for (String p: QueryEncoderBackend.decode(text, false).split("&")) {
			String name = p.substring(0, p.indexOf('='));
			String value = URLDecoder.decode(p.substring(p.indexOf('=') + 1), "UTF-8");
			params.put(name, value);
		}
		return params;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String q = request.getParameter("q");
		if (q == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Q parameter not provided.");
			return;
		}
		DataSource ds = getQueueProcessor().getFile(QueryEncoderBackend.decode(q, false));
		if (ds != null) {
			response.setContentType(ds.getContentType());
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + ds.getName() + "\"" );
			OutputStream out = response.getOutputStream();
			InputStream in = ds.getInputStream();
			try {
				IOUtils.copy(ds.getInputStream(), out);
				out.flush();
			} finally {
				in.close();	
				out.close();
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Output file is not available.");
		}
	}

}
