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
package org.unitime.timetable.onlinesectioning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.OnlineSectioningLogDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningLogger extends Thread {
	private static Log sLog = LogFactory.getLog(OnlineSectioningLogger.class);
	private List<OnlineSectioningLog.Action> iActions = new Vector<OnlineSectioningLog.Action>();
	private boolean iActive = false;
	private boolean iEnabled = false;
	private int iLogLimit = -1;
	private PrintWriter iOut = null;
	
	private static OnlineSectioningLogger sInstance = null;
	
	public static OnlineSectioningLogger getInstance() {
		if (sInstance == null) startLogger();
		return sInstance;
	}
	
	public static void startLogger() {
		if (sInstance == null) {
			sInstance = new OnlineSectioningLogger();
			sInstance.start();
		}
	}
	
	public static void stopLogger() {
		if (sInstance != null) {
			sInstance.iActive = false;
			sInstance.interrupt();
			try {
				sInstance.join();
			} catch (InterruptedException e) {}
			sInstance = null;
		}
	}
	
	private OnlineSectioningLogger() {
		super("OnlineSectioningLogger");
		setDaemon(true);
		iEnabled = ApplicationProperty.OnlineSchedulingLoggingEnabled.isTrue(); 
		iLogLimit = ApplicationProperty.OnlineSchedulingLogLimit.intValue();
		try {
			if (ApplicationProperty.OnlineSchedulingLogFile.value() != null)
				iOut = new PrintWriter(new FileWriter(new File(ApplicationProperty.OnlineSchedulingLogFile.value()), true));
		} catch (IOException e) {
			sLog.warn("Unable to create sectioning log: " + e.getMessage(), e);
		}
	}
	
	public boolean isEnabled() { return iEnabled; }
	public void setEnabled(boolean enabled) { iEnabled = enabled; }
	public boolean isActive() { return iActive; }

	public void record(OnlineSectioningLog.Log log) {
		if (log == null || !isEnabled() || !isActive()) return;
		for (OnlineSectioningLog.Action action: log.getActionList()) {
			if (action.hasStartTime() && action.hasStudent() && action.hasOperation() && action.hasSession()) {
				synchronized (iActions) {
					if (iLogLimit <= 0 || iActions.size() < iLogLimit)
						iActions.add(action);
				}
				if (iOut != null) {
					synchronized (iOut) {
						iOut.print(OnlineSectioningLog.Log.newBuilder().addAction(action).build().toString());
						iOut.flush();
					}
				}
			}
		}
	}
	
	public void run() {
		sLog.info("Online Sectioning Logger is up.");
		try {
			iActive = true;
			while (true) {
				try {
					sleep(60000);
				} catch (InterruptedException e) {
				}
				List<OnlineSectioningLog.Action> actionsToSave = null;
				synchronized (iActions) {
					if (!iActions.isEmpty()) {
						actionsToSave = new ArrayList<OnlineSectioningLog.Action>(iActions);
						iActions.clear();
					}
				}
				try {
					if (actionsToSave != null) {
						sLog.debug("Persisting " + actionsToSave.size() + " actions...");
						if (iLogLimit > 0 && actionsToSave.size() >= iLogLimit)
							sLog.warn("The limit of " + iLogLimit + " unpersisted log messages was reached, some messages have been dropped.");
						org.hibernate.Session hibSession = OnlineSectioningLogDAO.getInstance().createNewSession();
						hibSession.setCacheMode(CacheMode.IGNORE);
						try {
							Hashtable<Long, Session> sessions = new Hashtable<Long, Session>();
							for (OnlineSectioningLog.Action q: actionsToSave) {
								org.unitime.timetable.model.OnlineSectioningLog log = new org.unitime.timetable.model.OnlineSectioningLog();
								log.setAction(q.toByteArray());
								log.setOperation(q.getOperation());
								String studentExternalId = (q.getStudent().hasExternalId() ? q.getStudent().getExternalId() : null);
								if (studentExternalId == null || studentExternalId.isEmpty()) {
									Student student = StudentDAO.getInstance().get(q.getStudent().getUniqueId(), hibSession);
									if (student == null) continue;
									studentExternalId = student.getExternalUniqueId();
								}
								log.setStudent(studentExternalId);
								log.setTimeStamp(new Date(q.getStartTime()));
								if (q.hasResult())
									log.setResult(q.getResult().getNumber());
								if (q.hasUser() && q.getUser().hasExternalId())
									log.setUser(q.getUser().getExternalId());
								if (q.hasCpuTime())
									log.setCpuTime(q.getCpuTime());
								if (q.hasStartTime() && q.hasEndTime())
									log.setWallTime(q.getEndTime() - q.getStartTime());
								if (q.hasApiGetTime())
									log.setApiGetTime(q.getApiGetTime());
								if (q.hasApiPostTime())
									log.setApiPostTime(q.getApiPostTime());
								if (q.hasApiException())
									log.setApiException(q.getApiException() != null && q.getApiException().length() > 255 ? q.getApiException().substring(0, 255) : q.getApiException());
								if (!q.getMessageList().isEmpty()) {
									String message = null; int level = 0;
									for (OnlineSectioningLog.Message m: q.getMessageList()) {
										if (message != null && !message.isEmpty() && (!m.hasLevel() || level < m.getLevel().getNumber())) continue;
										if (m.hasText()) { message = m.getText(); level = m.getLevel().getNumber(); }
										else if (m.hasException()) { message = m.getException(); level = m.getLevel().getNumber(); }
									}
									if (message != null && !message.isEmpty())
										log.setMessage(message.length() > 255 ? message.substring(0, 255) : message);
								}
								Long sessionId = q.getSession().getUniqueId();
								Session session = sessions.get(sessionId);
								if (session == null) {
									session = SessionDAO.getInstance().get(sessionId, hibSession);
									sessions.put(sessionId, session);
								}
								log.setSession(session);
								hibSession.save(log);
							}
							hibSession.flush();
						} finally {
							hibSession.close();
						}
					}
				} catch (Throwable t) {
					sLog.warn("Failed to save " + actionsToSave.size() + " log actions: " + t.getMessage(), t);
				}
				if (!iActive) break;
			}
		} catch (Throwable t) {
			sLog.error("Online Sectioning Logger failed: " + t.getMessage(), t);
		} finally {
			iActive = false;
			if (iOut != null) { iOut.flush(); iOut.close(); }
		}
		sLog.info("Online Sectioning Logger is down.");	}

}
