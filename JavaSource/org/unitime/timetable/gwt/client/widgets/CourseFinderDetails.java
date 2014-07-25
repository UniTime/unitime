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

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseFinderDetails extends HTML implements CourseFinder.CourseFinderCourseDetails<String> {
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private DataProvider<String, String> iDataProvider = null;
	private String iValue = null;
	
	public CourseFinderDetails() {
		setHTML(MESSAGES.courseSelectionNoCourseSelected());
		setStyleName("unitime-Message");
	}
	
	@Override
	public void setDataProvider(DataProvider<String, String> provider) {
		iDataProvider = provider;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setValue(String value) {
		if (value == null || value.isEmpty()) {
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
	public String getValue() {
		return iValue;
	}

	@Override
	public String getName() {
		return MESSAGES.courseSelectionDetails();
	}

}
