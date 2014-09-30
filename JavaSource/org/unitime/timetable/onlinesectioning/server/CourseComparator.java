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
