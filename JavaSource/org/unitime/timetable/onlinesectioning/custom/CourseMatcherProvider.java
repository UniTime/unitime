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
