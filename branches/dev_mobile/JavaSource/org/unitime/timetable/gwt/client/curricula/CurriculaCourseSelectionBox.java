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
package org.unitime.timetable.gwt.client.curricula;

import java.util.Collection;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderCourses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDialog;
import org.unitime.timetable.gwt.client.widgets.CourseFinderFactory;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionSuggestBox;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class CurriculaCourseSelectionBox extends CourseSelectionSuggestBox {
	private final CurriculaServiceAsync iCurriculaService = GWT.create(CurriculaService.class);
		
	public CurriculaCourseSelectionBox() {
		setCourseFinderFactory(new CourseFinderFactory() {
			@Override
			public CourseFinder createCourseFinder() {
				CourseFinder finder = new CourseFinderDialog();
				CourseFinderCourses courses = new CourseFinderCourses();
				courses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
					@Override
					public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
						iCurriculaService.listCourseOfferings(source, null, callback);
					}
				});
				CourseFinderDetails details = new CourseFinderDetails();
				details.setDataProvider(new DataProvider<String, String>() {
					@Override
					public void getData(String source, AsyncCallback<String> callback) {
						iCurriculaService.retrieveCourseDetails(source, callback);
					}
				});
				CourseFinderClasses classes = new CourseFinderClasses();
				classes.setDataProvider(new DataProvider<String, Collection<ClassAssignment>>() {
					@Override
					public void getData(String source, AsyncCallback<Collection<ClassAssignment>> callback) {
						iCurriculaService.listClasses(source, callback);
					}
				});
				CourseFinderCurricula curricula = new CourseFinderCurricula();
				curricula.setDataProvider(new DataProvider<String, TreeSet<CurriculumInterface>>() {
					@Override
					public void getData(String source, AsyncCallback<TreeSet<CurriculumInterface>> callback) {
						iCurriculaService.findCurriculaForACourse(source, callback);
					}
				});
				courses.setCourseDetails(details, classes, curricula);				
				finder.setTabs(courses);
				return finder;
			}
		});
				

		setSuggestions(new DataProvider<String, Collection<CourseAssignment>>() {
			@Override
			public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
				iCurriculaService.listCourseOfferings(source, 20, callback);
			}
		});
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			iFinderButton.setTabIndex(0);
			iFinderButton.setVisible(true);
		} else {
			iFinderButton.setTabIndex(-1);
			iFinderButton.setVisible(false);
			iSuggest.getElement().getStyle().setBorderColor("transparent");
            iSuggest.getElement().getStyle().setBackgroundColor("transparent");
		}
	}
}
