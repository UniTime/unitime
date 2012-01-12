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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseStudentSectioningStatus;

public class StudentSectioningStatus extends BaseStudentSectioningStatus {
	private static final long serialVersionUID = -33276457852954947L;

	public static enum Option {
		enabled("Access Enabled"),
		advisor("Advisor Can Enroll"),
		email("Email Notifications");
		
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

}
