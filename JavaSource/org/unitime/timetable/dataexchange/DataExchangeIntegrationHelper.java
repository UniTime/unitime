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

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.util.queue.QueueMessage;

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
		Log logger = new Log() {
			protected void log(QueueMessage.Level level, Object message, Throwable t) {
				log.append(new QueueMessage(level, message, t).toHTML() + "<br>\n");
			}

			@Override
			public void warn(Object message, Throwable t) {
				log(QueueMessage.Level.WARN, message, t);
			}
			
			@Override
			public void warn(Object message) {
				log(QueueMessage.Level.WARN, message, null);
			}
			
			@Override
			public void trace(Object message, Throwable t) {
				log(QueueMessage.Level.TRACE, message, t);
			}
			
			@Override
			public void trace(Object message) {
				log(QueueMessage.Level.TRACE, message, null);
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
				log(QueueMessage.Level.INFO, message, t);
			}
			
			@Override
			public void info(Object message) {
				log(QueueMessage.Level.INFO, message, null);
			}
			
			@Override
			public void fatal(Object message, Throwable t) {
				log(QueueMessage.Level.FATAL, message, t);
			}
			
			@Override
			public void fatal(Object message) {
				log(QueueMessage.Level.FATAL, message, null);
			}
			
			@Override
			public void error(Object message, Throwable t) {
				log(QueueMessage.Level.ERROR, message, t);
			}
			
			@Override
			public void error(Object message) {
				log(QueueMessage.Level.ERROR, message, null);
			}
			
			@Override
			public void debug(Object message, Throwable t) {
				log(QueueMessage.Level.DEBUG, message, t);					
			}
			
			@Override
			public void debug(Object message) {
				log(QueueMessage.Level.DEBUG, message, null);
			}
		};
        String manager = document.getRootElement().attributeValue("manager", ApplicationProperty.DataExchangeXmlManager.value());
		DataExchangeHelper.importDocument(document, manager, logger);
		log.append("</body></html>");
		return log.toString();
	}
}
