/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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

import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.security.SessionContext;

/**
 * {@link CourseMatcher} provider interface. Such provider can be used to filter out what courses
 * a student can see / select in the Scheduling Assistant.
 */
public interface CourseMatcherProvider {
	/**
	 * Create a course matcher instance
	 * @param context current session context (e.g., can be used to check permission, current user role etc.)
	 * @param studentId current student unique id
	 * @return course matcher instance, null if no additional filtering is to be made
	 */
	public CourseMatcher getCourseMatcher(SessionContext context, Long studentId);
}
