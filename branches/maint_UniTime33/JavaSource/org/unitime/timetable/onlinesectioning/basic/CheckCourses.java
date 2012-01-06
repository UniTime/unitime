/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

public class CheckCourses implements OnlineSectioningAction<Collection<String>> {
	private static final long serialVersionUID = 1L;
	private CourseRequestInterface iRequest;
	
	public CheckCourses(CourseRequestInterface request) {
		iRequest = request;
	}

	@Override
	public Collection<String> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		ArrayList<String> notFound = new ArrayList<String>();
		for (CourseRequestInterface.Request cr: iRequest.getCourses()) {
			if (!cr.hasRequestedFreeTime() && cr.hasRequestedCourse() && server.getCourseInfo(cr.getRequestedCourse()) == null)
				notFound.add(cr.getRequestedCourse());
			if (cr.hasFirstAlternative() && server.getCourseInfo(cr.getFirstAlternative()) == null)
				notFound.add(cr.getFirstAlternative());
			if (cr.hasSecondAlternative() && server.getCourseInfo(cr.getSecondAlternative()) == null)
				notFound.add(cr.getSecondAlternative());
		}
		for (CourseRequestInterface.Request cr: iRequest.getAlternatives()) {
			if (cr.hasRequestedCourse() && server.getCourseInfo(cr.getRequestedCourse()) == null)
				notFound.add(cr.getRequestedCourse());
			if (cr.hasFirstAlternative() && server.getCourseInfo(cr.getFirstAlternative()) == null)
				notFound.add(cr.getFirstAlternative());
			if (cr.hasSecondAlternative() && server.getCourseInfo(cr.getSecondAlternative()) == null)
				notFound.add(cr.getSecondAlternative());
		}
		return notFound;
	}

	@Override
	public String name() {
		return "check-courses";
	}

}
