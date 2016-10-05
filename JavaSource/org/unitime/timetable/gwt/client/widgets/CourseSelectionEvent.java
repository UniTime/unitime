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
package org.unitime.timetable.gwt.client.widgets;

import java.util.List;

import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Tomas Muller
 */
public class CourseSelectionEvent extends GwtEvent<CourseSelectionHandler> {
	static Type<CourseSelectionHandler> TYPE = new Type<CourseSelectionHandler>();
	private RequestedCourse iValue;
	
	public CourseSelectionEvent(RequestedCourse value) { iValue = value; }
	
	public RequestedCourse getValue() { return iValue; }
	
	public boolean isValid() { return getValue() != null && (getValue().isFreeTime() || getValue().hasCourseId()); }
	
	public String getCourse() { return getValue() == null ? "" : getValue().toString(); }

	@Override
	public Type<CourseSelectionHandler> getAssociatedType() { return TYPE; }
	public static Type<CourseSelectionHandler> getType() { return TYPE; }

	@Override
	protected void dispatch(CourseSelectionHandler handler) {
		handler.onCourseSelection(this);
	}
	
	public static void fire(HasHandlers source, RequestedCourse value) {
		source.fireEvent(new CourseSelectionEvent(value));
	}
	
	public static void fire(HasHandlers source, List<FreeTime> freeTime) {
		RequestedCourse value = new RequestedCourse(); value.setFreeTime(freeTime);
		fire(source, value);
	}
	
	public static void fire(HasHandlers source, String courseName) {
		RequestedCourse course = new RequestedCourse(); course.setCourseName(courseName);
		fire(source, course);
	}
}
