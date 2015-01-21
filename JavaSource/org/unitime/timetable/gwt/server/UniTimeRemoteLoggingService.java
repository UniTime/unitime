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
package org.unitime.timetable.gwt.server;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.google.gwt.core.server.StackTraceDeobfuscator;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Tomas Muller
 */
public class UniTimeRemoteLoggingService extends RemoteServiceServlet implements RemoteLoggingService {
	private static Logger sLogger = Logger.getLogger(UniTimeRemoteLoggingService.class);
	private static final long serialVersionUID = 1L;
	private StackTraceDeobfuscator iDeobfuscator = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
	    super.init(config);
	    String path = config.getServletContext().getRealPath("/WEB-INF/deploy/unitime/symbolMaps/");
	    if (path != null)
	    	iDeobfuscator = StackTraceDeobfuscator.fromFileSystem(path);
	}
	
	@Override
	public String logOnServer(LogRecord record) {
		try {
			if (iDeobfuscator != null && record.getThrown() != null)
				iDeobfuscator.deobfuscateStackTrace(record.getThrown(), getPermutationStrongName());
			Logger logger = Logger.getLogger(record.getLoggerName());
			if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
				logger.error(record.getMessage(), record.getThrown());
			} else if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
				logger.warn(record.getMessage(), record.getThrown());
			} else if (record.getLevel().intValue() >= Level.INFO.intValue()) {
				logger.info(record.getMessage(), record.getThrown());
			} else if (record.getLevel().intValue() >= Level.FINE.intValue()) {
				logger.debug(record.getMessage(), record.getThrown());
			} else {
				logger.trace(record.getMessage(), record.getThrown());
			}
			return null;
		} catch (Exception e) {
			sLogger.warn("Logging failed, reason: " + e.getMessage(), e);
		}
		return null;
	}

}
