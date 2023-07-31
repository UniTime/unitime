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

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Filter;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event.NativePreviewEvent;

/**
 * @author Tomas Muller
 */
public class CourseFinderCoursesWithFilter extends CourseFinderCourses {
	private CourseFinderFilter iFilter;
	
	public CourseFinderCoursesWithFilter(StudentSectioningContext context, boolean showCourseTitles, boolean showDefaultSuggestions, boolean showRequired, SpecialRegistrationContext specReg, boolean showWaitLists) {
		super(showCourseTitles, showDefaultSuggestions, showRequired, specReg, showWaitLists);
		
		iFilter = new CourseFinderFilter(context);
		iFilter.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Filter>() {
			@Override
			public void onValueChange(ValueChangeEvent<Filter> event) {
				if (iLastQuery != null) {
					RequestedCourse rc = new RequestedCourse();
					rc.setCourseName(iLastQuery);
					reload(rc);
				}
			}
		});
		
		insert(iFilter, 0);
	}
	
	public CourseRequestInterface.Filter getFilter() {
		return iFilter.getValue();
	}
	public void setFilter(CourseRequestInterface.Filter filter) {
		iFilter.setValue(filter);
	}

	@Override
	public void onBeforeShow() {
		super.onBeforeShow();
		iFilter.init();
	}
	
	@Override
	public boolean isCanSubmit(NativePreviewEvent event) {
		return iFilter.isCanSubmit(event);
	}
	
	@Override
	public void reset() {
		super.reset();
		iFilter.setValue(null);
	}
}
