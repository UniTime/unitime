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
package org.unitime.timetable.gwt.client.curricula;

import java.util.TreeSet;

import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CurriculumInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class CourseFinderCurricula extends CourseCurriculaTable implements CourseFinder.CourseFinderCourseDetails<TreeSet<CurriculumInterface>>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final StudentSectioningMessages SCT_MESSAGES = GWT.create(StudentSectioningMessages.class);
	
	private String iValue = null;
	private DataProvider<String, TreeSet<CurriculumInterface>> iDataProvider;
	
	public CourseFinderCurricula() {
		super(false, false);
		setMessage(SCT_MESSAGES.courseSelectionNoCourseSelected());
	}

	@Override
	public void setValue(final String value) {
		if (value == null || value.isEmpty()) {
			iValue = value;
			clear(false);
			setMessage(SCT_MESSAGES.courseSelectionNoCourseSelected());
		} else {
			iValue = value;
			clear(true);
			ensureInitialized(new AsyncCallback<Boolean>() {
				@Override
				public void onSuccess(Boolean result) {
					iDataProvider.getData(value, new AsyncCallback<TreeSet<CurriculumInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							setMessage(MESSAGES.failedToLoadCurricula(caught.getMessage()));
							CurriculumCookie.getInstance().setCurriculaCoursesDetails(false);
						}

						@Override
						public void onSuccess(TreeSet<CurriculumInterface> result) {
							if (result.isEmpty()) {
								setMessage(MESSAGES.offeringHasNoCurricula());
							} else {
								populate(result);
							}
						}
					});
				}
				
				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
		
	}

	@Override
	public String getValue() {
		return iValue;
	}

	@Override
	public void setDataProvider(DataProvider<String, TreeSet<CurriculumInterface>> provider) {
		iDataProvider = provider;
	}

	@Override
	public String getName() {
		return MESSAGES.tabCurricula();
	}
}
