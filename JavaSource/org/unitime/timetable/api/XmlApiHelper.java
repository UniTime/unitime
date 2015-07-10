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
package org.unitime.timetable.api;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.CacheMode;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class XmlApiHelper extends AbstractApiHelper {
	
	public XmlApiHelper(HttpServletRequest request, HttpServletResponse response, SessionContext context, CacheMode cacheMode) {
		super(request, response, context, cacheMode);
	}

	@Override
	public Document getRequest(Type requestType) throws IOException {
		Reader reader = iRequest.getReader();
		try {
			return new SAXReader().read(reader);
		} catch (DocumentException e) {
			throw new IOException(e.getMessage(), e);
		} finally {
			reader.close();
		}
	}

	@Override
	public <R> void setResponse(R response) throws IOException {
		iResponse.setContentType("application/xml");
		iResponse.setCharacterEncoding("UTF-8");
		iResponse.setHeader("Pragma", "no-cache" );
		iResponse.addHeader("Cache-Control", "must-revalidate" );
		iResponse.addHeader("Cache-Control", "no-cache" );
		iResponse.addHeader("Cache-Control", "no-store" );
		iResponse.setDateHeader("Date", new Date().getTime());
		iResponse.setDateHeader("Expires", 0);
		iResponse.setHeader("Content-Disposition", "attachment; filename=\"response.xml\"" );
		Writer writer = iResponse.getWriter();
		try {
			new XMLWriter(writer, OutputFormat.createPrettyPrint()).write(response);
		} finally {
			writer.flush();
			writer.close();
		}
	}

}
