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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Tomas Muller
 */
public class CourseSelectionEvent extends GwtEvent<CourseSelectionHandler> {
	static Type<CourseSelectionHandler> TYPE = new Type<CourseSelectionHandler>();
	private String iCourse;
	private boolean iValid;
	
	public CourseSelectionEvent(String course, boolean valid) { iCourse = course; iValid = valid; }
	
	public boolean isValid() { return iValid; }
	
	public String getCourse() { return iCourse; }

	@Override
	public Type<CourseSelectionHandler> getAssociatedType() { return TYPE; }
	public static Type<CourseSelectionHandler> getType() { return TYPE; }

	@Override
	protected void dispatch(CourseSelectionHandler handler) {
		handler.onCourseSelection(this);
	}
	
	public static void fire(HasHandlers source, String course, boolean valid) {
		source.fireEvent(new CourseSelectionEvent(course, valid));
	}
}
