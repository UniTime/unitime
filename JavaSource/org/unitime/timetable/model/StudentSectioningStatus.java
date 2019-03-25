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
package org.unitime.timetable.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.base.BaseStudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class StudentSectioningStatus extends BaseStudentSectioningStatus {
	private static final long serialVersionUID = -33276457852954947L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	public static enum Option {
		/*    1 */ enabled("Scheduling Assistant Access Enabled"),
		/*    2 */ advisor("Advisor Can Enroll"),
		/*    4 */ email("Email Notifications"),
		/*    8 */ notype("Must Have Course Type"),
		/*   16 */ waitlist("Wait-Listing Enabled"),
		/*   32 */ nobatch("Do Not Schedule in Batch Solver"),
		/*   64 */ enrollment("Student Can Enrol"),
		/*  128 */ admin("Admin Can Enroll"),
		/*  256 */ registration("Student Can Register"),
		/*  512 */ regenabled("Course Requests Access Enabled"),
		/* 1024 */ regadvisor("Advisor Can Register"),
		/* 2048 */ regadmin("Admin Can Register"),
		/* 4196 */ advcanset("Advisor Can Set Status"),
		/* 8192 */ reqval("Course Request Validation"),
		/*16384 */ specreg("Special Registration"),
		/*32768 */ canreq("Can Require Sections / IMs"),
		;
		
		private String iName;
		
		Option(String name) {
			iName = name;
		}
		
		public String getName() { return iName; }
		
		public int toggle() { return 1 << ordinal(); }
	}
	
	public boolean hasOption(Option option) {
		return getStatus() != null && (getStatus() & option.toggle()) != 0;
	}
	
	public void addOption(Option option) {
		if (!hasOption(option)) setStatus((getStatus() == null ? 0 : getStatus()) + option.toggle());
	}

	public void removeOption(Option option) {
		if (hasOption(option)) setStatus(getStatus() - option.toggle());
	}

	public StudentSectioningStatus() {
		super();
	}
	
	public static StudentSectioningStatus getStatus(String reference, Long sessionId, org.hibernate.Session hibSession) {
		if (reference != null) {
			StudentSectioningStatus status = (StudentSectioningStatus)hibSession.createQuery("from StudentSectioningStatus s where s.reference = :reference and s.session is null")
					.setString("reference", reference).setMaxResults(1).setCacheable(true).uniqueResult();
			if (status != null)
				return status;
			if (sessionId != null) {
				status = (StudentSectioningStatus)hibSession.createQuery("from StudentSectioningStatus s where s.reference = :reference and s.session = :sessionId")
						.setString("reference", reference).setLong("sessionId", sessionId).setMaxResults(1).setCacheable(true).uniqueResult();
				if (status != null)
					return status;
			}
		}
		if (sessionId != null) {
			StudentSectioningStatus status = (StudentSectioningStatus)hibSession.createQuery("select s.defaultSectioningStatus from Session s where s.uniqueId = :sessionId")
					.setLong("sessionId", sessionId).setMaxResults(1).setCacheable(true).uniqueResult();
			if (status != null) return status;
		}
		return null;
	}
	
	public static boolean hasOption(Option option, String reference, Long sessionId, org.hibernate.Session hibSession) {
		StudentSectioningStatus status = getStatus(reference, sessionId, hibSession);
		return status == null || status.hasOption(option);
	}
	
	public static Set<String> getMatchingStatuses(Option option, Long sessionId) {
		org.hibernate.Session hibSession = StudentSectioningStatusDAO.getInstance().createNewSession();
		try {
			Set<String> statuses = new HashSet<String>();
			for (StudentSectioningStatus status: StudentSectioningStatus.findAll(hibSession, sessionId)) {
				if (status.hasOption(option) && status.isEffectiveNow())
					statuses.add(status.getReference());
			}
			return statuses;
		} finally {
			hibSession.close();
		}
	}
	
	public boolean isEffectiveNow() {
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		int slot = 12 * cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 5;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		if (getEffectiveStartDate() != null && today.before(getEffectiveStartDate())) return false;
		if (getEffectiveStartPeriod() != null && (getEffectiveStartDate() == null || today.equals(getEffectiveStartDate()))  && slot < getEffectiveStartPeriod()) return false;
		if (getEffectiveStopDate() != null && today.after(getEffectiveStopDate())) return false;
		if (getEffectiveStopPeriod() != null && (getEffectiveStopPeriod() == null || today.equals(getEffectiveStopDate())) && slot >= getEffectiveStopPeriod()) return false;
		return true;
	}
	
	public boolean isPast() {
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		int slot = 12 * cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 5;
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		if (getEffectiveStopDate() != null && today.after(getEffectiveStopDate())) return true;
		if (getEffectiveStopPeriod() != null && (getEffectiveStopPeriod() == null || today.equals(getEffectiveStopDate())) && slot >= getEffectiveStopPeriod()) return true;
		return false;
	}
	
	public static boolean hasEffectiveOption(StudentSectioningStatus status, Session session, Option option) {
		if (status != null) {
			if (status.isEffectiveNow())
				return status.hasOption(option);
			StudentSectioningStatus fallback = status.getFallBackStatus();
			int depth = 10;
			while (fallback != null && depth -- > 0) {
				if (fallback.isEffectiveNow())
					return fallback.hasOption(option);
				else
					fallback = fallback.getFallBackStatus();
			}
		}
		StudentSectioningStatus defaultStatus = (session == null ? null : session.getDefaultSectioningStatus());
		return (defaultStatus == null ? true : defaultStatus.hasOption(option));
	}
	
	public String getEffectivePeriod() {
		String start = null, stop = null;
		if (getEffectiveStartDate() != null || getEffectiveStartPeriod() != null) {
			if (getEffectiveStartDate() == null)
				start = Constants.slot2str(getEffectiveStartPeriod());
			else if (getEffectiveStartPeriod() == null)
				start = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(getEffectiveStartDate());
			else
				start = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(getEffectiveStartDate()) + " " + Constants.slot2str(getEffectiveStartPeriod());
		}
		if (getEffectiveStopDate() != null || getEffectiveStopPeriod() != null) {
			if (getEffectiveStopDate() == null)
				stop = Constants.slot2str(getEffectiveStopPeriod());
			else if (getEffectiveStopPeriod() == null)
				stop = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(getEffectiveStopDate());
			else
				stop = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).format(getEffectiveStopDate()) + " " + Constants.slot2str(getEffectiveStopPeriod());
		}
		if (start != null) {
			if (stop != null) {
				return MSG.messageEffectivePeriodBetween(start, stop);
			} else {
				return MSG.messageEffectivePeriodAfter(start);
			}
		} else if (stop != null) {
			return MSG.messageEffectivePeriodBefore(stop);
		} else {
			return null;
		}
	}
	
	public static List<StudentSectioningStatus> findAll(Long sessionId) {
		return findAll(null, sessionId);
	}
	
	public static List<StudentSectioningStatus> findAll(org.hibernate.Session hibSession, Long sessionId) {
		if (sessionId == null)
			return (List<StudentSectioningStatus>)(hibSession == null ? StudentSectioningStatusDAO.getInstance().getSession() : hibSession).createQuery(
					"from StudentSectioningStatus where session is null order by label"
					).setCacheable(true).list();
		else
			return (List<StudentSectioningStatus>)(hibSession == null ? StudentSectioningStatusDAO.getInstance().getSession() : hibSession).createQuery(
					"from StudentSectioningStatus where session is null or session = :sessionId order by label"
					).setLong("sessionId", sessionId).setCacheable(true).list();
	}
}
