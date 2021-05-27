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
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.server.DayCode;
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
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
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
			if (action.hasStartTime() && action.hasStudent() && action.hasOperation() && action.hasSession() && ApplicationProperty.OnlineSchedulingLogOperation.isTrue(action.getOperation())) {
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
	
	protected static String getRequestMessage(OnlineSectioningLog.Action action) {
		String request = "";
		int notAlt = 0, lastFT = -1;
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			if (!r.getAlternative()) notAlt = r.getPriority() + 1;
			int idx = 0;
			for (OnlineSectioningLog.Time f: r.getFreeTimeList()) {
				if (idx == 0) {
					request += (lastFT == r.getPriority() ? ", " : (request.isEmpty() ? "" : "\n") + (r.getAlternative() ? "A" + (1 + r.getPriority() - notAlt) : String.valueOf(1 + r.getPriority())) + ". " + CONST.freePrefix() + " ");
				} else {
					request += ", ";
				}
				idx++;
				request += DayCode.toString(f.getDays()) + " "  + time(f.getStart()) + " - " + time(f.getStart() + f.getLength());
				lastFT = r.getPriority();
			}
			if (r.getFreeTimeList().isEmpty())
				for (OnlineSectioningLog.Entity e: r.getCourseList()) {
					if (idx == 0) {
						request += (request.isEmpty() ? "" : "\n") + (r.getAlternative() ? "A" + (1 + r.getPriority() - notAlt) : String.valueOf(1 + r.getPriority())) + ". ";
					} else {
						request += ", ";
					}
					idx++;
					request += e.getName();
				}
		}
		return request;
	}
	
	protected static String getSelectedMessage(OnlineSectioningLog.Action action) {
		String selected = "";
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			for (OnlineSectioningLog.Section s: r.getSectionList()) {
				if (s.getPreference() == OnlineSectioningLog.Section.Preference.SELECTED) {
					if (!selected.isEmpty()) selected += "\n";
					String loc = "";
					for (OnlineSectioningLog.Entity e: s.getLocationList()) {
						if (!loc.isEmpty()) loc += ", ";
						loc += e.getName();
					}
					String instr = "";
					for (OnlineSectioningLog.Entity e: s.getInstructorList()) {
						if (!instr.isEmpty()) instr += ", ";
						instr += e.getName();
					}
					selected += s.getCourse().getName() + " " + s.getSubpart().getName() + " " + s.getClazz().getName() + " " +
						(s.hasTime() ? DayCode.toString(s.getTime().getDays()) + " " + time(s.getTime().getStart()) + " - " + time(s.getTime().getStart() + s.getTime().getLength()) : "") + " " + loc;
				}
			}
		}
		return selected;
	}
	
	protected static String getEnrollmentMessage(OnlineSectioningLog.Action action) {
		OnlineSectioningLog.Enrollment enrl = null;
		for (OnlineSectioningLog.Enrollment e: action.getEnrollmentList()) {
			enrl = e;
			if (e.getType() == OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED) break;
		}
		String enrollment = "";
		if (enrl != null)
			for (OnlineSectioningLog.Section s: enrl.getSectionList()) {
				if (!s.hasCourse()) continue;
				if (!enrollment.isEmpty()) enrollment += "\n";
				String loc = "";
				for (OnlineSectioningLog.Entity r: s.getLocationList()) {
					if (!loc.isEmpty()) loc += ", ";
					loc += r.getName();
				}
				String instr = "";
				for (OnlineSectioningLog.Entity r: s.getInstructorList()) {
					if (!instr.isEmpty()) instr += ", ";
					instr += r.getName();
				}
				enrollment += s.getCourse().getName() + " " + s.getSubpart().getName() + " " + s.getClazz().getName() + " " +
					(s.hasTime() ? DayCode.toString(s.getTime().getDays()) + " " + time(s.getTime().getStart()) : "") + " " + loc;
			}
		return enrollment;
	}
	
	public static String getMessage(OnlineSectioningLog.Action action) {
		String message = "";
		int level = 1;
		for (OnlineSectioningLog.Message m: action.getMessageList()) {
			if (!m.hasLevel()) continue; // skip messages with no level
			if (!message.isEmpty() && level > m.getLevel().getNumber()) continue; // if we have a message, ignore messages with lower level
			if (m.hasText()) {
				message = (level != m.getLevel().getNumber() || message.isEmpty() ? "" : message + "\n") + m.getText();
				level = m.getLevel().getNumber();
			} else if (m.hasException()) {
				message = (level != m.getLevel().getNumber() || message.isEmpty() ? "" : message + "\n") + m.getException();
				level = m.getLevel().getNumber();
			}
		}
		if (action.hasResult() && OnlineSectioningLog.Action.ResultType.FAILURE.equals(action.getResult()) && !message.isEmpty()) {
			return message;
		} else if ("suggestions".equals(action.getOperation())) {
			String selected = getSelectedMessage(action);
			return (selected.isEmpty() ? message : selected);
		} if ("section".equals(action.getOperation())) {
			String request = getRequestMessage(action);
			return (request.isEmpty() ? message : request);
		} else {
			String enrollment = getEnrollmentMessage(action);
			if (!enrollment.isEmpty()) return enrollment;
			String request = getRequestMessage(action);
			return (request.isEmpty() ? message : request);
		}
	}
	
	protected static String time(int slot) {
        int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONST.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
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
								try {
									String message = getMessage(q);
									if (message != null && !message.isEmpty())
										log.setMessage(message.length() > 255 ? message.substring(0, 252) + "..." : message);
								} catch (Exception e) {
									if (!q.getMessageList().isEmpty()) {
										String message = null; int level = 0;
										for (OnlineSectioningLog.Message m: q.getMessageList()) {
											if (message != null && !message.isEmpty() && (!m.hasLevel() || level > m.getLevel().getNumber())) continue;
											if (m.hasText()) { message = m.getText(); level = m.getLevel().getNumber(); }
											else if (m.hasException()) { message = m.getException(); level = m.getLevel().getNumber(); }
										}
										if (message != null && !message.isEmpty())
											log.setMessage(message.length() > 255 ? message.substring(0, 252) + "..." : message);
									}
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
