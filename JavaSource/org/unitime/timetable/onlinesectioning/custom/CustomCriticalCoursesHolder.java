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
package org.unitime.timetable.onlinesectioning.custom;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;

/**
 * @author Tomas Muller
 */
public class CustomCriticalCoursesHolder {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private static CriticalCoursesProvider sProvider = null;
	
	public synchronized static CriticalCoursesProvider getProvider() {
		if (sProvider == null) {
			try {
				sProvider = ((CriticalCoursesProvider)Class.forName(ApplicationProperty.CustomizationCriticalCourses.value()).newInstance());
			} catch (Exception e) {
				throw new SectioningException(MSG.exceptionCriticalCoursesProvider(e.getMessage()), e);
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
		return sProvider != null || ApplicationProperty.CustomizationCriticalCourses.value() != null;
	}
}
