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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class QueueMessage implements Serializable, Comparable<QueueMessage> {
	private static final long serialVersionUID = 1L;
	public static enum Level {
		TRACE,
		DEBUG,
		PROGRESS,
		INFO,
		STAGE,
		WARN,
		ERROR,
		FATAL,
		HTML
	}
	
	private Level iLevel = Level.INFO;
    private String iMessage;
    private Date iDate = null;
    private String iError = null;
    
    public QueueMessage(Level level, String message) {
    	iLevel = level;
    	iMessage = message;
    	iDate = new Date();
    }
    
    public QueueMessage(Level level, Object message) {
    	this(level, message == null ? "" : message.toString());
    }
    
    public QueueMessage(Level level, String message, Throwable error) {
    	this(level, message);
    	if (error != null) {
    		StringWriter writer = new StringWriter();
    		PrintWriter pw = new PrintWriter(writer);
    		error.printStackTrace(new PrintWriter(writer));
    		pw.flush(); pw.close();
    		iError = writer.toString();
    	}
    }
    
    public QueueMessage(Level level, Object message, Throwable error) {
    	this(level, message == null ? "" : message.toString(), error);
    }
    
    public Level getLevel() { return iLevel; }
    public String getMessage() { return iMessage; }
    public Date getDate() { return iDate; }
    public boolean hasError() { return iError != null && !iError.isEmpty(); }
    public String getError() { return iError; }
    
    protected String formatMessagePlain() {
    	Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
    	switch (getLevel()) {
    	case TRACE:
    		return df.format(getDate()) + " " + getLevel().name() + ":    -- " + getMessage();
    	case DEBUG:
    		return df.format(getDate()) + " " + getLevel().name() + ":  -- " + getMessage();
    	case PROGRESS:
    	case STAGE:
    		return df.format(getDate()) + " " + getLevel().name() + ":[" + getMessage() + "]";
    	default:
    		return df.format(getDate()) + " " + getLevel().name() + ":" + getMessage();
		}
    }
    
    @Override
    public String toString() {
    	return formatMessagePlain() + (hasError() ? "\n" + getError(): "");
    }
    
    protected String formatMessageHTML() {
    	switch (getLevel()) {
    	case TRACE:
    		return "&nbsp;&nbsp;&nbsp;&nbsp;<i><font color='gray'> " + StringEscapeUtils.escapeHtml(getMessage()) + "</font></i>";
    	case DEBUG:
    		return "&nbsp;&nbsp;<i><font color='gray'> " + StringEscapeUtils.escapeHtml(getMessage()) + "</font></i>";
    	case INFO:
    		return "&nbsp;&nbsp;" + StringEscapeUtils.escapeHtml(getMessage());
    	case WARN:
    		return "<font color='orange'>" + StringEscapeUtils.escapeHtml(getMessage()) + "</font>";
    	case ERROR:
    		return "<font color='red'>" + StringEscapeUtils.escapeHtml(getMessage()) + "</font>";
    	case FATAL:
    		return "<font color='red'><b>" + StringEscapeUtils.escapeHtml(getMessage()) + "</b></font>";
    	case PROGRESS:
    		return "<b>" + StringEscapeUtils.escapeHtml(getMessage()) + "</b>";
    	case STAGE:
    		return "<b>" + StringEscapeUtils.escapeHtml(getMessage()) + "</b>";
    	case HTML:
    		return getMessage();
    	default:
    		return StringEscapeUtils.escapeHtml(getMessage());
		}
    }
    
    public String toHTML() {
    	return formatMessageHTML() + (hasError() ? "<br><font color='red'><pre>" + StringEscapeUtils.escapeHtml(getError()) + "</pre></font>" : "");
    }

	@Override
	public int compareTo(QueueMessage m) {
		return getDate().compareTo(m.getDate());
	}
}
