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
