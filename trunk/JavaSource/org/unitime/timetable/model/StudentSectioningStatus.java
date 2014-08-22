/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.base.BaseStudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;

/**
 * @author Tomas Muller
 */
public class StudentSectioningStatus extends BaseStudentSectioningStatus {
	private static final long serialVersionUID = -33276457852954947L;

	public static enum Option {
		enabled("Access Enabled"),
		advisor("Advisor Can Enroll"),
		email("Email Notifications"),
		notype("Must Have Course Type"),
		waitlist("Wait-Listing Enabled"),
		nobatch("Do Not Schedule in Batch Solver"),
		enrollment("Enrollment Enabled"),
		admin("Admin Can Enroll"),
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
			StudentSectioningStatus status = (StudentSectioningStatus)hibSession.createQuery("from StudentSectioningStatus s where s.reference = :reference")
					.setString("reference", reference).setMaxResults(1).setCacheable(true).uniqueResult();
			if (status != null)
				return status;
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
	
	public static Set<String> getMatchingStatuses(Option option) {
		org.hibernate.Session hibSession = StudentSectioningStatusDAO.getInstance().createNewSession();
		try {
			Set<String> statuses = new HashSet<String>();
			for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll(hibSession)) {
				if (status.hasOption(option))
					statuses.add(status.getReference());
			}
			return statuses;
		} finally {
			hibSession.close();
		}
	}
}
