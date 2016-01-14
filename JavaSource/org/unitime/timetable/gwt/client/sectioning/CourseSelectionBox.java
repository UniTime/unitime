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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.Collection;

import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderCourses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDialog;
import org.unitime.timetable.gwt.client.widgets.CourseFinderFactory;
import org.unitime.timetable.gwt.client.widgets.CourseFinderFreeTime;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionSuggestBox;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.client.widgets.FreeTimeParser;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * @author Tomas Muller
 */
public class CourseSelectionBox extends CourseSelectionSuggestBox {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private CourseSelectionBox iPrev, iNext;
	private CourseSelectionBox iPrimary, iAlternative;
	
	private CheckBox iWaitList;
	
	private AcademicSessionProvider iAcademicSessionProvider;
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	public CourseSelectionBox(AcademicSessionProvider acadSession, boolean enabled, boolean allowFreeTime) {
		super(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty());
		iAcademicSessionProvider = acadSession;
		
		if (allowFreeTime) {
			FreeTimeParser parser = new FreeTimeParser();
			setFreeTimes(parser);
		}
		
		setCourseFinderFactory(new CourseFinderFactory() {
			@Override
			public CourseFinder createCourseFinder() {
				CourseFinder finder = new CourseFinderDialog();
				
				CourseFinderCourses courses = new CourseFinderCourses(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty());
				courses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
					@Override
					public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
						iSectioningService.listCourseOfferings(iAcademicSessionProvider.getAcademicSessionId(), source, null, callback);
					}
				});
				CourseFinderDetails details = new CourseFinderDetails();
				details.setDataProvider(new DataProvider<String, String>() {
					@Override
					public void getData(String source, AsyncCallback<String> callback) {
						iSectioningService.retrieveCourseDetails(iAcademicSessionProvider.getAcademicSessionId(), source, callback);
					}
				});
				CourseFinderClasses classes = new CourseFinderClasses();
				classes.setDataProvider(new DataProvider<String, Collection<ClassAssignment>>() {
					@Override
					public void getData(String source, AsyncCallback<Collection<ClassAssignment>> callback) {
						iSectioningService.listClasses(iAcademicSessionProvider.getAcademicSessionId(), source, callback);
					}
				});
				courses.setCourseDetails(details, classes);
				if (getFreeTimes() != null) {
					CourseFinderFreeTime free = new CourseFinderFreeTime();
					free.setDataProvider(getFreeTimes());
					finder.setTabs(courses, free);
				} else {
					finder.setTabs(courses);
				}
				return finder;
			}
		});

		setSuggestions(new DataProvider<String, Collection<CourseAssignment>>() {
			@Override
			public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
				iSectioningService.listCourseOfferings(iAcademicSessionProvider.getAcademicSessionId(), source, 20, callback);
			}
		});
	}
	
	public void setNext(CourseSelectionBox next) { iNext = next; }
	public void setPrev(CourseSelectionBox prev) { iPrev = prev; }
	public void setPrimary(CourseSelectionBox primary) { iPrimary = primary; }
	public void setAlternative(CourseSelectionBox alternative) { iAlternative = alternative; }
	public void setWaitList(CheckBox waitList) { iWaitList = waitList; }
	
	public void setWaitList(boolean waitList) { if (iWaitList != null) iWaitList.setValue(waitList); }
	public Boolean getWaitList() { return iWaitList == null ? null : iWaitList.getValue(); }
	public void setWaitListEnabled(boolean enabled) { if (iWaitList != null) iWaitList.setEnabled(enabled); }
	
	private void swapWith(CourseSelectionBox other) {
		hideSuggestionList();
		other.hideSuggestionList();
		String x = getError(); setError(other.getError()); other.setError(x);
		boolean b = isEnabled(); setEnabled(other.isEnabled()); other.setEnabled(b);
		boolean s = isSaved(); setSaved(other.isSaved()); other.setSaved(s);
		if (iPrimary != null) {
			x = getHint();
			setHint(other.getHint());
			other.setHint(x);
		}
		x = getValue();
		setValue(other.getValue(), false);
		other.setValue(x, false);
		if (iAlternative!=null) iAlternative.swapWith(other.iAlternative);
		if (iWaitList != null && other.iWaitList != null) {
			Boolean ch = iWaitList.getValue(); iWaitList.setValue(other.iWaitList.getValue()); other.iWaitList.setValue(ch);
		}
	}
	
	private void replaceWith(CourseSelectionBox other) {
		hideSuggestionList();
		if (other != null) other.hideSuggestionList();
		setError(other == null ? null : other.getError());
		setEnabled(other == null ? iPrimary == null || !iPrimary.getValue().isEmpty() : other.isEnabled());
		setSaved(other == null ? false : other.isSaved());
		if (iPrimary != null)
			setHint(other == null ? "" : other.getHint());
		setValue(other == null ? null : other.getValue(), false);
		if (iAlternative!=null) iAlternative.replaceWith(other == null ? null : other.iAlternative);
		if (iWaitList != null) {
			iWaitList.setValue(other == null || other.iWaitList == null ? false : other.iWaitList.getValue());
		}
	}
	
	private void clearAllAlternatives() {
		if (iPrimary != null) {
			setHint("");
		}
		setValue(null);
		if (iAlternative!=null) iAlternative.clearAllAlternatives();
	}
	
	private void clearAll() {
		if (iPrimary != null) iPrimary.clearAll();
		else clearAllAlternatives();
	}
	
	public void moveDown() {
		if (iPrimary!=null) {
			iPrimary.moveDown();
		} else {
			if (iNext==null) {
				clearAll();
			} else {
				iNext.moveDown();
				swapWith(iNext);
			}
		}
	}
	
	public void moveUp() {
		if (iPrimary!=null) {
			iPrimary.moveUp();
		} else {
			if (iPrev==null) {
				clearAll();
			} else {
				iPrev.moveUp();
				swapWith(iPrev);
			}
		}
	}
	
	public void swapDown() {
		if (iPrimary!=null) {
			iPrimary.swapDown();
		} else {
			swapWith(iNext);
		}
	}

	public void swapUp() {
		if (iPrimary!=null) {
			iPrimary.swapUp();
		} else {
			swapWith(iPrev);
		}
	}

	public void remove() {
		if (iPrimary!=null) {
			iPrimary.remove();
		} else {
			if (iNext != null && isAllowFreeTime() == iNext.isAllowFreeTime()) {
				replaceWith(iNext);
				iNext.remove();
			} else {
				replaceWith(null);
			}
		}
	}
	
	public boolean fillInFreeTime(CourseRequestInterface.Request request) {
		try {
			if (getFreeTimes() != null)
			for (CourseRequestInterface.FreeTime ft: getFreeTimes().parseFreeTime(getValue()))
				request.addRequestedFreeTime(ft);
			return request.hasRequestedFreeTime();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
