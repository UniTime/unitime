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
package org.unitime.timetable.onlinesectioning.server;

import java.util.HashMap;

import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourse;

public class CourseCache {
	HashMap<Long, XCourse> iCache = new HashMap<Long, XCourse>();
	OnlineSectioningServer iServer;
	
	public CourseCache(OnlineSectioningServer server) {
		iServer = server;
	}
	
	public XCourse getCourse(Long courseId) {
		XCourse course = iCache.get(courseId);
		if (course == null) {
			course = iServer.getCourse(courseId);
			if (course != null) iCache.put(courseId, course);
		}
		return course;
	}
}
