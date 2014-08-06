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
