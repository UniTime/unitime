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

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseFinderDetails extends HTML implements CourseFinder.CourseFinderCourseDetails<CourseAssignment, String> {
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private DataProvider<CourseAssignment, String> iDataProvider = null;
	private CourseAssignment iValue = null;
	
	public CourseFinderDetails() {
		setHTML(MESSAGES.courseSelectionNoCourseSelected());
		setStyleName("unitime-Message");
	}
	
	@Override
	public void setDataProvider(DataProvider<CourseAssignment, String> provider) {
		iDataProvider = provider;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setValue(CourseAssignment value) {
		if (value == null) {
			iValue = value;
			setHTML(MESSAGES.courseSelectionNoCourseSelected());
			setStyleName("unitime-Message");
		} else if (!value.equals(iValue)) {
			iValue = value;
			setHTML(MESSAGES.courseSelectionLoadingDetails());
			setStyleName("unitime-Message");
			
			iDataProvider.getData(iValue, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					setHTML(result);
					setStyleName("");
				}
				
				@Override
				public void onFailure(Throwable caught) {
					setHTML(caught.getMessage());
					setStyleName("unitime-ErrorMessage");
				}
			});
		}
	}

	@Override
	public CourseAssignment getValue() {
		return iValue;
	}

	@Override
	public String getName() {
		return MESSAGES.courseSelectionDetails();
	}

	@Override
	public void onSetValue(RequestedCourse course) {}

	@Override
	public void onGetValue(RequestedCourse course) {}
	
	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {}
}
