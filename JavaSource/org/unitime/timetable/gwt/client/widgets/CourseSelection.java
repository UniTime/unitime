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

import java.util.Collection;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;

import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

public interface CourseSelection extends HasValue<String>, IsWidget, HasEnabled, Focusable, HasCourseSelectionHandlers {
	public void setSuggestions(DataProvider<String, Collection<ClassAssignmentInterface.CourseAssignment>> provider);
	public void setFreeTimes(FreeTimeParser parser);
	public FreeTimeParser getFreeTimes();
	
	public void setCourseFinderFactory(CourseFinderFactory finder);
	public CourseFinder getCourseFinder();
	
	public void setLabel(String title, String finderTitle);
	
	public boolean isFreeTime();

	public void setHint(String hint);
	public String getHint();

	public void hideSuggestionList();
	public void showSuggestionList();

	public void setWidth(String width);
	
	public void setError(String error);
	public String getError();
	
	public void addValidator(Validator<CourseSelection> validator);
	public String validate();
}
