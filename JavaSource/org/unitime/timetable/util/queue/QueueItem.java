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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.UserContext;

/**
 * 
 * @author Tomas Muller
 *
 */
public abstract class QueueItem implements Log, Serializable, Comparable<QueueItem> {
	private static final long serialVersionUID = 1L;
	protected static GwtMessages MSG = Localization.create(GwtMessages.class);

	private Long iSessionId;
	private String iOwnerId;
	private String iOwnerName;
	private String iOwnerEmail;
	private transient File iOutput = null;
	private String iOutputName = null;
	private String iOutputLink = null;
	private List<QueueMessage> iLog = new ArrayList<QueueMessage>();
	private String iStatus = null;
	private Date iCreated = new Date(), iStarted = null, iFinished = null;
	private transient Throwable iException = null;
	private String iLocale = null;
	private double iProgress = 0;
	private double iMaxProgress = 100.0;
	
	private String iId = null;
	private Address iAddress = null;
	private Long iTaskExecutionId = null;
	
	public QueueItem(Long sessionId, UserContext owner) {
		iSessionId = sessionId;
		iOwnerId = owner.getExternalUserId();
		iOwnerName = owner.getName();
		iOwnerEmail = owner.getEmail();
		iLocale = Localization.getLocale();
		iStatus = MSG.scriptStatusWaiting();
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
	public Long getTaskExecutionId() { return iTaskExecutionId; }
	public void setTaskExecutionId(Long executionId) { iTaskExecutionId = executionId; }
	
	public abstract String type();
	public abstract String name();
	protected abstract void execute() throws Exception;
	
	public void executeItem() {
		iStarted = new Date();
		ApplicationProperties.setSessionId(getSessionId());
		Localization.setLocale(getLocale());
		try {
			execute();
		} catch (ThreadDeath e) {
			fatal(MSG.scriptLogExecutionStopped(), e);
		} catch (Exception e) {
			fatal(MSG.scriptLogExecutionFailed(), e);
		} finally {
			ApplicationProperties.setSessionId(null);
			_RootDAO.closeCurrentThreadSessions();
			Localization.removeLocale();
		}
		iFinished = new Date();
		iStatus = MSG.scriptStatusAllDone();
		if (iException != null) {
			if (iException instanceof ThreadDeath)
				iStatus = MSG.scriptStatusKilled();
			else
				iStatus = MSG.scriptStatusFailed(iException.getMessage());
		}
	}
	
	public boolean hasOutput() { return iFinished != null && iOutputLink != null; }
	public File output() { return iOutput; }
	public void setOutput(File output) {
		iOutput = output;
		iOutputName = (output == null ? null : output.getName());
		iOutputLink = (output == null ? null : "qpfile?q=" + QueryEncoderBackend.encode(getId().toString()));
	}
	protected File createOutput(String prefix, String ext) {
		if (iOutput != null) throw new RuntimeException(MSG.scriptErrorOutputAlreadyCreated());
		iOutput = ApplicationProperties.getTempFile(prefix, ext);
		iOutputName = prefix + "." + ext;
		iOutputLink = "qpfile?q=" + QueryEncoderBackend.encode(getId().toString());
		return iOutput;
	}
	public String getOutputName() { return iOutputName; }
	public String getOutputLink() {
		if (iOutput != null) return "temp/" + iOutput.getName();
		return iOutputLink;
	}
	
	public List<QueueMessage> getLog() {
		synchronized (iLog) {
			return new ArrayList<QueueMessage>(iLog);
		}
	}
	public String log() {
		synchronized (iLog) {
			String ret = "";
			for (QueueMessage m: iLog) {
				if (!ret.isEmpty()) ret += "<br>";
				ret += m.toHTML();
			}
			return ret;
		}
	}
	
	public void log(String message) {
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.HTML, message));
		}
	}
	
	public void setStatus(String status) {
		setStatus(status, 100.0);
	}
	public void setStatus(String status, double maxProgress) {
		synchronized (iLog) {
			iStatus = status; iProgress = 0; iMaxProgress = maxProgress;
			iLog.add(new QueueMessage(QueueMessage.Level.STAGE, status));
		}
	}	
	public String status() { return iStatus; }
	public double progress() { return iProgress / iMaxProgress; }
	public void incProgress() { iProgress ++; }
	public void incProgress(double value) { iProgress += value; }
	public void setProgress(double value) { iProgress = value; }

	public boolean hasError() { return iException != null; }
	protected void setError(Throwable exception) { iException = exception; }
	public Throwable error() { return iException; }
	
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }
	public String getHost() { return (iAddress == null ? "Local" : iAddress.toString()); }
	public Address getAddress() { return iAddress; }
	public void setAddress(Address address) { iAddress = address; }
	
	public Date created() { return iCreated; }
	public Date started() { return iStarted; }
	public Date finished() { return iFinished; }

	@Override
	public boolean isDebugEnabled() {
		return Logger.getLogger(getClass()).isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public boolean isFatalEnabled() {
		return false;
	}

	@Override
	public boolean isInfoEnabled() {
		return Logger.getLogger(getClass()).isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return Logger.getLogger(getClass()).isTraceEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public void trace(Object message) {
		Logger.getLogger(getClass()).trace(message);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.TRACE, message));
		}
	}

	@Override
	public void trace(Object message, Throwable t) {
		Logger.getLogger(getClass()).trace(message, t);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.TRACE, message, t));
		}
	}

	@Override
	public void debug(Object message) {
		Logger.getLogger(getClass()).debug(message);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.DEBUG, message));
		}
	}

	@Override
	public void debug(Object message, Throwable t) {
		Logger.getLogger(getClass()).debug(message, t);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.DEBUG, message, t));
		}
	}

	@Override
	public void info(Object message) {
		Logger.getLogger(getClass()).info(message);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.INFO, message));
		}
	}

	@Override
	public void info(Object message, Throwable t) {
		Logger.getLogger(getClass()).info(message, t);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.INFO, message, t));
		}
	}

	@Override
	public void warn(Object message) {
		Logger.getLogger(getClass()).warn(message);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.WARN, message));
		}
	}

	@Override
	public void warn(Object message, Throwable t) {
		Logger.getLogger(getClass()).warn(message, t);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.WARN, message, t));
		}
	}

	@Override
	public void error(Object message) {
		Logger.getLogger(getClass()).error(message);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.ERROR, message));
		}
	}

	@Override
	public void error(Object message, Throwable t) {
		Logger.getLogger(getClass()).error(message, t);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.ERROR, message, t));
		}
	}

	@Override
	public void fatal(Object message) {
		Logger.getLogger(getClass()).fatal(message);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.FATAL, message));
		}
	}

	@Override
	public void fatal(Object message, Throwable t) {
		Logger.getLogger(getClass()).fatal(message, t);
		synchronized (iLog) {
			iLog.add(new QueueMessage(QueueMessage.Level.FATAL, message, t));
		}
	}
	
	@Override
	public int compareTo(QueueItem item) {
		int cmp = created().compareTo(item.created());
		if (cmp != 0) return cmp;
		return getId().compareTo(item.getId());
	}
}
