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

import java.io.Serializable;
import java.util.Comparator;

import org.unitime.timetable.onlinesectioning.model.XCourseId;

/**
 * XCourseId comparator preferring courses with with matching course name (to courses with matching title).
 * 
 * @author Tomas Muller
 */
public class CourseComparator implements Comparator<XCourseId>, Serializable {
	private static final long serialVersionUID = 1L;
	private String iQuery = null;
	
	public CourseComparator(String query) {
		iQuery = (query == null ? null : query.toLowerCase());
	}

	@Override
	public int compare(XCourseId c1, XCourseId c2) {
		if (iQuery != null && !iQuery.isEmpty()) {
			if (c1.matchCourseName(iQuery)) {
				if (!c2.matchCourseName(iQuery)) return -1;
			} else if (c2.matchCourseName(iQuery)) {
				return 1;
			}
		}
		return c1.compareTo(c2);
	}

}
