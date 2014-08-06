/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
		iAcademicSessionProvider = acadSession;
		
		if (allowFreeTime) {
			FreeTimeParser parser = new FreeTimeParser();
			setFreeTimes(parser);
		}
		
		setCourseFinderFactory(new CourseFinderFactory() {
			@Override
			public CourseFinder createCourseFinder() {
				CourseFinder finder = new CourseFinderDialog();
				
				CourseFinderCourses courses = new CourseFinderCourses();
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
	
	private void swapWith(CourseSelectionBox other) {
		hideSuggestionList();
		other.hideSuggestionList();
		String x = getError(); setError(other.getError()); other.setError(x);
		boolean b = isEnabled(); setEnabled(other.isEnabled()); other.setEnabled(b);
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
