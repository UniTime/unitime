/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.custom;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider;

public class CustomStudentEnrollmentHolder {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private static StudentEnrollmentProvider sProvider = null;
	
	public synchronized static StudentEnrollmentProvider getProvider() {
		if (sProvider == null) {
			try {
				sProvider = ((StudentEnrollmentProvider)Class.forName(ApplicationProperty.CustomizationStudentEnrollments.value()).newInstance());
			} catch (Exception e) {
				throw new SectioningException(MSG.exceptionStudentEnrollmentProvider(e.getMessage()), e);
			}
		}
		return sProvider;
	}
	
	public synchronized static void release() {
		if (sProvider != null) {
			sProvider.dispose();
			sProvider = null;
		}
	}
	
	public synchronized static boolean hasProvider() {
		return sProvider != null || ApplicationProperty.CustomizationStudentEnrollments.value() != null;
	}
	
	public synchronized static boolean isAllowWaitListing() {
		return !hasProvider() || getProvider().isAllowWaitListing();
	}
}
