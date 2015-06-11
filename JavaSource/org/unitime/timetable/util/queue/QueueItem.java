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

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.UserContext;

/**
 * 
 * @author Tomas Muller
 *
 */
public abstract class QueueItem implements Log {
    protected static Logger iLogger;

	private Long iSessionId;
	private String iOwnerId;
	private String iOwnerName;
	private String iOwnerEmail;
	private File iOutput = null;
	private String iLog = "";
	private String iStatus = "Waiting...";
	private Date iCreated = new Date(), iStarted = null, iFinished = null;
	private Throwable iException = null;
	private String iLocale = null;
	
	private Long iId = null;
	
	public QueueItem(Long sessionId, UserContext owner) {
		iLogger = Logger.getLogger(getClass());
		iSessionId = sessionId;
		iOwnerId = owner.getExternalUserId();
		iOwnerName = owner.getName();
		iOwnerEmail = owner.getEmail();
		iLocale = Localization.getLocale();
	}
	
	public QueueItem(Session session, UserContext owner) {
		this(session.getUniqueId(), owner);
	}
	
	public QueueItem(UserContext owner) {
		this(owner.getCurrentAcademicSessionId(), owner);
	}
	
	public Long getSessionId() { return iSessionId; }
	public Session getSession() { return SessionDAO.getInstance().get(iSessionId); }
	public String getOwnerId() { return iOwnerId; }
	public String getOwnerName() { return iOwnerName; }
	public boolean hasOwnerEmail() { return iOwnerEmail != null && !iOwnerEmail.isEmpty(); }
	public String getOwnerEmail() { return iOwnerEmail; }
	public String getLocale() { return iLocale; }
	
	public abstract String type();
	public abstract String name();
	public abstract double progress();
	protected abstract void execute() throws Exception;
	
	public void executeItem() {
		iStarted = new Date();
		ApplicationProperties.setSessionId(getSessionId());
		Localization.setLocale(getLocale());
		try {
			execute();
		} catch (ThreadDeath e) {
			fatal("Execution stopped.", e);
		} catch (Exception e) {
			fatal("Execution failed.", e);
		} finally {
			ApplicationProperties.setSessionId(null);
			_RootDAO.closeCurrentThreadSessions();
			Localization.removeLocale();
		}
		iFinished = new Date();
		iStatus = "All done.";
		if (iException != null) {
			if (iException instanceof ThreadDeath)
				iStatus = "Killed";
			else
				iStatus = "Failed (" + iException.getMessage() + ")";
		}
	}
	
	public boolean hasOutput() { return iOutput != null && iOutput.exists() && iOutput.canRead(); }
	public File output() { return iOutput; }
	public void setOutput(File output) { iOutput = output; }
	protected File createOutput(String prefix, String ext) {
		if (iOutput != null) throw new RuntimeException("Output already created.");
		iOutput = ApplicationProperties.getTempFile(prefix, ext);
		return iOutput;
	}
	
	public String log() { return iLog; }
	protected void log(String message) {
		if (iLog.length() > 0) iLog += "<br>";
		iLog += message;
	}
	protected void warn(String message) {
		if (iLog.length() > 0) iLog += "<br>";
		iLog += "<font color='orange'>" + message + "</font>";
	}
	protected void error(String message) {
		if (iLog.length() > 0) iLog += "<br>";
		iLog += "<font color='red'>" + message + "</font>";
	}
	
	protected void setStatus(String status) {
		iStatus = status;
		log("<i>" + iStatus + "</i>");
	}
	public String status() { return iStatus; }

	public boolean hasError() { return iException != null; }
	protected void setError(Throwable exception) { iException = exception; }
	public Throwable error() { return iException; }
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	
	public Date created() { return iCreated; }
	public Date started() { return iStarted; }
	public Date finished() { return iFinished; }
	
	@Override
	public void trace(Object message) {
		iLogger.trace(message);
	}

	@Override
	public void trace(Object message, Throwable exception) {
		iLogger.trace(message, exception);
	}
	
	@Override
	public void debug(Object message) {
		debug(message, null);
	}

	@Override
	public void debug(Object message, Throwable exception) {
		iLogger.debug(message, exception);
		if (exception == null) {
			if (message != null) log(message.toString());
		} else if (message == null) {
			log(exception.getClass().getSimpleName() + ": " + exception.getMessage());
		} else {
			log(message + " (" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
		}
	}
	
	@Override
	public void warn(Object message) {
		warn(message, null);	
	}

	@Override
	public void warn(Object message, Throwable exception) {
		iLogger.warn(message, exception);
		if (exception == null) {
			if (message != null) warn(message.toString());
		} else if (message == null) {
			warn(exception.getClass().getSimpleName() + ": " + exception.getMessage());
		} else {
			warn(message + " (" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
		}
	}
	
	@Override
	public void info(Object message) {
		info(message, null);
	}

	@Override
	public void info(Object message, Throwable exception) {
		iLogger.info(message, exception);
		if (exception == null) {
			if (message != null) log(message.toString());
		} else if (message == null) {
			log(exception.getClass().getSimpleName() + ": " + exception.getMessage());
		} else {
			log(message + " (" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
		}
	}

	@Override
	public void error(Object message) {
		error(message, null);
	}

	@Override
	public void error(Object message, Throwable exception) {
		iLogger.error(message, exception);
		if (exception == null) {
			if (message != null) error(message.toString());
		} else if (message == null) {
			error(exception.getClass().getSimpleName() + ": " + exception.getMessage());
		} else {
			error(message + " (" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
		}
	}

	@Override
	public void fatal(Object message) {
		fatal(message, null);
	}

	@Override
	public void fatal(Object message, Throwable exception) {
		iLogger.fatal(message, exception);
		if (exception == null) {
			if (message != null) error(message.toString());
		} else if (message == null) {
			error(exception.getClass().getSimpleName() + ": " + exception.getMessage());
		} else {
			error(message + " (" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + ")");
		}
		if (exception != null)
			setError(exception);
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public boolean isFatalEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}
}
