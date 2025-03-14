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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.curricula.CourseCurriculaTable;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsWidget;
import org.unitime.timetable.gwt.client.instructor.TeachingRequestsWidget;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyWidget;
import org.unitime.timetable.gwt.client.instructor.survey.OfferingDetailWidget;
import org.unitime.timetable.gwt.client.offerings.AssignInstructorsButton;
import org.unitime.timetable.gwt.client.offerings.InstrOfferingConfigButton;
import org.unitime.timetable.gwt.client.offerings.MultipleClassSetupButton;
import org.unitime.timetable.gwt.client.page.UniTimeBack;
import org.unitime.timetable.gwt.client.page.UniTimeMenuBar;
import org.unitime.timetable.gwt.client.page.UniTimeMobileMenu;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.page.UniTimeSideBar;
import org.unitime.timetable.gwt.client.page.UniTimeVersion;
import org.unitime.timetable.gwt.client.reservations.ReservationTable;
import org.unitime.timetable.gwt.client.rooms.MapWidget;
import org.unitime.timetable.gwt.client.rooms.PeriodPreferencesWidget;
import org.unitime.timetable.gwt.client.rooms.RoomNoteChanges;
import org.unitime.timetable.gwt.client.rooms.RoomSharingWidget;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.sectioning.ExaminationEnrollmentTable;
import org.unitime.timetable.gwt.client.sectioning.StudentScheduleTable;
import org.unitime.timetable.gwt.client.solver.SolverAllocatedMemory;
import org.unitime.timetable.gwt.client.widgets.CourseNumbersSuggestBox;
import org.unitime.timetable.gwt.client.widgets.SearchableListBox;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * Register GWT components here.
 * @author Tomas Muller
 *
 */
public enum Components {
	courseCurricula("UniTimeGWT:CourseCurricula", new ComponentFactory() { public void insert(RootPanel panel) { new CourseCurriculaTable(true, true).insert(panel); } }),
	title("UniTimeGWT:Title", new ComponentFactory() { public void insert(RootPanel panel) { UniTimePageLabel.getInstance().insert(panel); } }),
	sidebar_stack("UniTimeGWT:SideStackMenu", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeSideBar(true, true).insert(panel); } }),
	sidebar_tree("UniTimeGWT:SideTreeMenu", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeSideBar(false, true).insert(panel); } }),
	sidebar_stack_static("UniTimeGWT:StaticSideStackMenu", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeSideBar(true, false).insert(panel); } }),
	sidebar_tree_static("UniTimeGWT:StaticSideTreeMenu", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeSideBar(false, false).insert(panel); } }),
	menubar_static("UniTimeGWT:TopMenu", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeMenuBar(false).insert(panel); } }),
	menubar_dynamic("UniTimeGWT:DynamicTopMenu", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeMenuBar(true).insert(panel); } }),
	mobile_menu("UniTimeGWT:MobileMenuButton", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeMobileMenu().insert(panel); } }),
	header("UniTimeGWT:Header", new ComponentFactory() { public void insert(RootPanel panel) { UniTimePageHeader.getInstance().insert(panel); } }),
	version("UniTimeGWT:Version", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeVersion().insert(panel); } }),
	back("UniTimeGWT:Back", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeBack().insert(panel); } }),
	offeringReservations("UniTimeGWT:OfferingReservations", new ComponentFactory() { public void insert(RootPanel panel) { new ReservationTable(true, true).insert(panel); } }),
	offeringReservationsReadOnly("UniTimeGWT:OfferingReservationsRO", new ComponentFactory() { public void insert(RootPanel panel) { new ReservationTable(false, true).insert(panel); } }),
	offeringEnrollments("UniTimeGWT:OfferingEnrollments", new ComponentFactory() { public void insert(RootPanel panel) { new EnrollmentTable(true, true, true).insert(panel); } }),
	examEnrollments("UniTimeGWT:ExamEnrollments", new ComponentFactory() { public void insert(RootPanel panel) { new ExaminationEnrollmentTable(true, true).insert(panel); } }),
	roomSharing("UniTimeGWT:RoomSharingWidget", new ComponentFactory() { public void insert(RootPanel panel) { new RoomSharingWidget(false).insert(panel, false); } }),
	roomEventAvailability("UniTimeGWT:RoomEventAvailabilityWidget", new ComponentFactory() { public void insert(RootPanel panel) { new RoomSharingWidget(false).insert(panel, true); } }),
	roomNoteChanges("UniTimeGWT:RoomNoteChanges", new ComponentFactory() { public void insert(RootPanel panel) { new RoomNoteChanges().insert(panel); } }),
	instructorAvailability("UniTimeGWT:InstructorAvailability", new ComponentFactory() { public void insert(RootPanel panel) { new InstructorAvailabilityWidget().insert(panel); } }),
	courseLink("UniTimeGWT:CourseLink", true, new ComponentFactory() { public void insert(RootPanel panel) { new CourseDetailsWidget(true).insert(panel); } }),
	courseDetails("UniTimeGWT:CourseDetails", new ComponentFactory() { public void insert(RootPanel panel) { new CourseDetailsWidget(false).insert(panel); } }),
	solverAllocatedMemory("UniTimeGWT:SolverAllocatedMem", true, new ComponentFactory() { public void insert(RootPanel panel) { new SolverAllocatedMemory().insert(panel); } }),
	calendar("UniTimeGWT:Calendar", true, new ComponentFactory() { public void insert(RootPanel panel) { SingleDateSelector.insert(panel); } }),
	courseNumberSuggestions("UniTimeGWT:CourseNumberSuggestBox", true, new ComponentFactory() { public void insert(RootPanel panel) { CourseNumbersSuggestBox.insert(panel); } }),
	periodPreferences("UniTimeGWT:PeriodPreferences", new ComponentFactory() { public void insert(RootPanel panel) { new PeriodPreferencesWidget(true).insert(panel); } }),
	teachingRequests("UniTimeGWT:TeachingRequests", new ComponentFactory() { public void insert(RootPanel panel) { new TeachingRequestsWidget().insert(panel); } }),
	studentEnrollments("UniTimeGWT:StudentEnrollments", new ComponentFactory() { public void insert(RootPanel panel) { new StudentScheduleTable(true, true, false).insert(panel); } }),
	teachingAssignments("UniTimeGWT:TeachingAssignments", new ComponentFactory() { public void insert(RootPanel panel) { new TeachingAssignmentsWidget().insert(panel); } }),
	dynamicMap("UniTimeGWT:Map", new ComponentFactory() { public void insert(RootPanel panel) { MapWidget.insert(panel); } }),
	assignClassInstructors("UniTimeGWT:AssignInstructorsButton", true, new ComponentFactory() { public void insert(RootPanel panel) { new AssignInstructorsButton(true).insert(panel); } }),
	instructorSurvey("UniTimeGWT:InstructorSurvey", true, new ComponentFactory() { public void insert(RootPanel panel) { new InstructorSurveyWidget().insert(panel); } }),
	instructorSurveyOffering("UniTimeGWT:InstructorSurveyOffering", true, new ComponentFactory() { public void insert(RootPanel panel) { new OfferingDetailWidget().insert(panel); } }),
	multipleClassSetup("UniTimeGWT:MultipleClassSetupButton", true, new ComponentFactory() { public void insert(RootPanel panel) { new MultipleClassSetupButton(true).insert(panel); } }),
	instrOfferingConfig("UniTimeGWT:InstrOfferingConfigButton", true, new ComponentFactory() { public void insert(RootPanel panel) { new InstrOfferingConfigButton(true).insert(panel); } }),
	searchableListBox("UniTimeGWT:SearchableListBox", true, new ComponentFactory() { public void insert(RootPanel panel) { new SearchableListBox().insert(panel); } }),
	titlePanel("UniTimeGWT:TitlePanel", new ComponentFactory() { public void insert(RootPanel panel) { UniTimeNavigation.getInstance().insert(panel); } }), 
	;
	
	private String iId;
	private ComponentFactory iFactory;
	private boolean iMultiple = false;
	
	Components(String id, ComponentFactory factory) { iId = id; iFactory = factory; }
	Components(String id, boolean multiple, ComponentFactory factory) { iId = id; iFactory = factory; iMultiple = multiple; }
	public String id() { return iId; }
	public void insert(RootPanel panel) { iFactory.insert(panel); }
	public boolean isMultiple() { return iMultiple; }
	
	public interface ComponentFactory {
		void insert(RootPanel panel);
	}
}
