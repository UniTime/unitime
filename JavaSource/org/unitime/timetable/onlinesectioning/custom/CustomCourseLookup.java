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

import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourse;

/**
 * @author Tomas Muller
 */
public interface CustomCourseLookup {
	
	public List<XCourse> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String query, boolean allowPartialMatch);
	public List<CourseOffering> getCourses(AcademicSessionInfo session, org.hibernate.Session hibSession, String query, boolean allowPartialMatch);
	public Set<Long> getCourseIds(AcademicSessionInfo session, org.hibernate.Session hibSession, String query, boolean allowPartialMatch);
	public void addSuggestions(OnlineSectioningServer server, OnlineSectioningHelper helper, String query, FilterRpcResponse filter);
	
	public void dispose();

}
