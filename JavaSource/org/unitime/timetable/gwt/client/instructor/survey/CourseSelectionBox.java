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
package org.unitime.timetable.gwt.client.instructor.survey;

import java.util.Collection;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.curricula.CourseFinderCurricula;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CourseDetail;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.ListAcademicClassifications;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderCourses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDialog;
import org.unitime.timetable.gwt.client.widgets.CourseFinderFactory;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionSuggestBox;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class CourseSelectionBox extends CourseSelectionSuggestBox {
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private Long iSessionId;
	private Label iTitle;
	
	protected CourseSelectionBox(Long sessionId) {
		iSessionId = sessionId;
		iTitle = new Label(); iTitle.addStyleName("course-title"); iTitle.setVisible(false);
		add(iTitle);
		addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				CourseSelectionBox.this.setTitle(event.getValue() == null ? null : event.getValue().getCourseTitle());
			}
		});
		setCourseFinderFactory(new CourseFinderFactory() {
			@Override
			public CourseFinder createCourseFinder() {
				CourseFinder finder = new CourseFinderDialog();
				CourseFinderCourses courses = new CourseFinderCourses();
				courses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
					@Override
					public void getData(String source, final AsyncCallback<Collection<CourseAssignment>> callback) {
						RPC.execute(new InstructorSurveyInterface.ListCourseOfferings(iSessionId, source, null), new AsyncCallback<GwtRpcResponseList<CourseAssignment>>() {
							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseList<CourseAssignment> result) {
								callback.onSuccess(result);
							}
						});
					}
				});
				CourseFinderDetails details = new CourseFinderDetails();
				details.setDataProvider(new DataProvider<CourseAssignment, String>() {
					@Override
					public void getData(CourseAssignment source, AsyncCallback<String> callback) {
						RPC.execute(new InstructorSurveyInterface.RetrieveCourseDetail(iSessionId, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), source.getCourseId()), new AsyncCallback<CourseDetail>() {
							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
							@Override
							public void onSuccess(CourseDetail result) {
								callback.onSuccess(result.getDetail());
							}
						});
					}
				});
				CourseFinderClasses classes = new CourseFinderClasses(false);
				classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
					@Override
					public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
						RPC.execute(new InstructorSurveyInterface.ListClasses(iSessionId, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), source.getCourseId()), new AsyncCallback<GwtRpcResponseList<ClassAssignment>>() {
							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseList<ClassAssignment> result) {
								callback.onSuccess(result);
							}
						});
					}
				});
				CourseFinderCurricula curricula = new CourseFinderCurricula() {
					@Override
					protected void ensureInitialized(final AsyncCallback<Boolean> callback) {
						if (iClassifications != null)
							callback.onSuccess(true);
						RPC.execute(new ListAcademicClassifications(iSessionId), new AsyncCallback<GwtRpcResponseList<AcademicClassificationInterface>>() {
							@Override
							public void onSuccess(GwtRpcResponseList<AcademicClassificationInterface> result) {
								iClassifications = new TreeSet<AcademicClassificationInterface>(result);
								if (callback != null) callback.onSuccess(true);
							}
							
							@Override
							public void onFailure(Throwable caught) {
								iHeader.setErrorMessage(MESSAGES.failedToLoadClassifications(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedToLoadClassifications(caught.getMessage()), caught);
								if (callback != null) callback.onFailure(caught);
							}
						});
					}
				};
				curricula.setDataProvider(new DataProvider<CourseAssignment, TreeSet<CurriculumInterface>>() {
					@Override
					public void getData(CourseAssignment source, AsyncCallback<TreeSet<CurriculumInterface>> callback) {
						RPC.execute(new InstructorSurveyInterface.ListCurricula(iSessionId, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), source.getCourseId()), new AsyncCallback<GwtRpcResponseList<CurriculumInterface>>() {
							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseList<CurriculumInterface> result) {
								if (result == null)
									callback.onSuccess(null);
								else
									callback.onSuccess(new TreeSet<CurriculumInterface>(result));
							}
						});
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
				RPC.execute(new InstructorSurveyInterface.ListCourseOfferings(iSessionId, source, 20), new AsyncCallback<GwtRpcResponseList<CourseAssignment>>() {
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
					@Override
					public void onSuccess(GwtRpcResponseList<CourseAssignment> result) {
						callback.onSuccess(result);
					}
				});
			}
		});
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			iFinderButton.setTabIndex(0);
			iFinderButton.setVisible(true);
			iSuggest.getElement().getStyle().clearBackgroundColor();
			iSuggest.getElement().getStyle().clearBorderColor();
		} else {
			iFinderButton.setTabIndex(-1);
			iFinderButton.setVisible(false);
			iSuggest.getElement().getStyle().setBorderColor("transparent");
            iSuggest.getElement().getStyle().setBackgroundColor("transparent");
		}
	}
	
	public void setTitle(String title) {
		if (title == null || title.isEmpty()) {
			iTitle.setText(""); iTitle.setVisible(false);
		} else {
			iTitle.setText(title); iTitle.setVisible(true);
		}
	}
	
	public void setValue(Course course, boolean fireEvents) {
		RequestedCourse rc = new RequestedCourse();
		rc.setCourseId(course.getId());
		rc.setCourseName(course.getCourseName());
		rc.setCourseTitle(course.getCourseTitle());
		super.setValue(rc, fireEvents);
		setTitle(course.getCourseTitle());
	}
	
	public void setValue(Course course) {
		setValue(course, false);
	}
}