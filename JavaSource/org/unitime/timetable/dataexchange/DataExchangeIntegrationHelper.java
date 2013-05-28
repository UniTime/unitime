/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.dataexchange;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;

@Service("dataExchangeHelper")
public class DataExchangeIntegrationHelper {
	
	public Document file2document(File file) throws DocumentException {
		return new SAXReader().read(file);
	}
	
	public String exception2message(Exception exception) throws IOException {
		StringWriter out = new StringWriter();
		exception.printStackTrace(new PrintWriter(out));
		out.flush(); out.close();
		return out.toString();
	}

	public String importDocument(Document document) throws Exception {
		final StringBuffer log = new StringBuffer("<html><header><title>XML Import Log</title></header><body>\n");
		DataExchangeHelper.LogWriter logger = new DataExchangeHelper.LogWriter() {
        	@Override
        	public void println(String message) {
        		log.append(message + "<br>\n");
        	}
        };
        String manager = document.getRootElement().attributeValue("manager", ApplicationProperties.getProperty("unitime.xml.manager"));
		DataExchangeHelper.importDocument(document, manager, logger);
		log.append("</body></html>");
		return log.toString();
	}
}
