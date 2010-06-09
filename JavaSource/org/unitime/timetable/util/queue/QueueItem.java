/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.util.queue;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;

/**
 * 
 * @author Tomas Muller
 *
 */
public abstract class QueueItem {
    protected static Logger sLog = Logger.getLogger(QueueItem.class);

	private Long iSessionId;
	private Long iOwnerId;
	private File iOutput = null;
	private String iLog = "";
	private String iStatus = "Waiting...";
	private Date iCreated = new Date(), iStarted = null, iFinished = null;
	private Throwable iException = null;
	
	private Long iId = null;
	
	public QueueItem(Session session, TimetableManager owner) {
		iSessionId = session.getUniqueId();
		iOwnerId = owner.getUniqueId();
	}
	
	public Long getSessionId() { return iSessionId; }
	public Session getSession() { return SessionDAO.getInstance().get(iSessionId); }
	public Long getOwnerId() { return iOwnerId; }
	public TimetableManager getOwner() { return TimetableManagerDAO.getInstance().get(iOwnerId); }
	
	public abstract String type();
	public abstract String name();
	public abstract double progress();
	protected abstract void execute() throws Exception;
	
	public void executeItem() {
		iStarted = new Date();
		try {
			execute();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			setError(e);
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
