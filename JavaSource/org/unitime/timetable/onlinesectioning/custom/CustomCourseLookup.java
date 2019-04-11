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

import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourseId;

/**
 * @author Tomas Muller
 */
public interface CustomCourseLookup {
	
	public List<XCourseId> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String query);
	public List<CourseOffering> getCourses(AcademicSessionInfo session, org.hibernate.Session hibSession, String query);
	public List<String> getCourses(AcademicSessionInfo session, String query);
	public void addSuggestions(OnlineSectioningServer server, OnlineSectioningHelper helper, String query, FilterRpcResponse filter);
	
	public void dispose();

}
