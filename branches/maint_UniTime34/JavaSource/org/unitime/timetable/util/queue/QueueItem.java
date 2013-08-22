/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util.queue;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;

/**
 * 
 * @author Tomas Muller
 *
 */
public abstract class QueueItem {
    protected static Logger sLog = Logger.getLogger(QueueItem.class);

	private Long iSessionId;
	private String iOwnerId;
	private String iOwnerName;
	private String iOwnerEmail;
	private File iOutput = null;
	private String iLog = "";
	private String iStatus = "Waiting...";
	private Date iCreated = new Date(), iStarted = null, iFinished = null;
	private Throwable iException = null;
	
	private Long iId = null;
	
	public QueueItem(Long sessionId, UserContext owner) {
		iSessionId = sessionId;
		iOwnerId = owner.getExternalUserId();
		iOwnerName = owner.getName();
		iOwnerEmail = owner.getEmail();
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
	
	public abstract String type();
	public abstract String name();
	public abstract double progress();
	protected abstract void execute() throws Exception;
	
	public void executeItem() {
		iStarted = new Date();
		ApplicationProperties.setSessionId(getSessionId());
		try {
			execute();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			setError(e);
		} finally {
			ApplicationProperties.setSessionId(null);
		}
		iFinished = new Date();
		iStatus = "All done.";
		if (iException != null) iStatus = "Failed (" + iException.getMessage() + ")";
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
}
