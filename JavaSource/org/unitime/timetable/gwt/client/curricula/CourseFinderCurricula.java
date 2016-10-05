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
package org.unitime.timetable.gwt.client.curricula;

import java.util.TreeSet;

import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CurriculumInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class CourseFinderCurricula extends CourseCurriculaTable implements CourseFinder.CourseFinderCourseDetails<CourseAssignment, TreeSet<CurriculumInterface>>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final StudentSectioningMessages SCT_MESSAGES = GWT.create(StudentSectioningMessages.class);
	
	private CourseAssignment iValue = null;
	private DataProvider<CourseAssignment, TreeSet<CurriculumInterface>> iDataProvider;
	
	public CourseFinderCurricula() {
		super(false, false);
		setMessage(SCT_MESSAGES.courseSelectionNoCourseSelected());
	}

	@Override
	public void setValue(final CourseAssignment value) {
		if (value == null) {
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
	public CourseAssignment getValue() {
		return iValue;
	}

	@Override
	public void setDataProvider(DataProvider<CourseAssignment, TreeSet<CurriculumInterface>> provider) {
		iDataProvider = provider;
	}

	@Override
	public String getName() {
		return MESSAGES.tabCurricula();
	}

	@Override
	public void onSetValue(RequestedCourse course) {}

	@Override
	public void onGetValue(RequestedCourse course) {}
}
