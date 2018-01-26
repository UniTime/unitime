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
