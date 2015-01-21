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
package org.unitime.timetable.dataexchange;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
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
        String manager = document.getRootElement().attributeValue("manager", ApplicationProperty.DataExchangeXmlManager.value());
		DataExchangeHelper.importDocument(document, manager, logger);
		log.append("</body></html>");
		return log.toString();
	}
}
