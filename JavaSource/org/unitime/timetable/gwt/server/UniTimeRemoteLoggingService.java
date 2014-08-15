/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
