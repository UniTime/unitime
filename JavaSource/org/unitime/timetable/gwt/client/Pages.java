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

import org.unitime.timetable.gwt.client.admin.PasswordPage;
import org.unitime.timetable.gwt.client.admin.ScriptPage;
import org.unitime.timetable.gwt.client.admin.SimpleEditPage;
import org.unitime.timetable.gwt.client.admin.TasksPage;
import org.unitime.timetable.gwt.client.curricula.CurriculaPage;
import org.unitime.timetable.gwt.client.curricula.CurriculumProjectionRulesPage;
import org.unitime.timetable.gwt.client.departments.DepartmentsPage;
import org.unitime.timetable.gwt.client.events.EventResourceTimetable;
import org.unitime.timetable.gwt.client.events.EventRoomAvailability;
import org.unitime.timetable.gwt.client.hql.SavedHQLPage;
import org.unitime.timetable.gwt.client.instructor.InstructorAssignmentPreferencesPage;
import org.unitime.timetable.gwt.client.instructor.InstructorAttributesPage;
import org.unitime.timetable.gwt.client.instructor.InstructorDetailPage;
import org.unitime.timetable.gwt.client.instructor.InstructorEditPage;
import org.unitime.timetable.gwt.client.instructor.InstructorPreferencesPage;
import org.unitime.timetable.gwt.client.instructor.InstructorsPage;
import org.unitime.timetable.gwt.client.instructor.SetupTeachingRequestsPage;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsChangesPage;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsPage;
import org.unitime.timetable.gwt.client.instructor.TeachingRequestsPage;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyPage;
import org.unitime.timetable.gwt.client.limitandprojectionsnapshot.LimitAndProjectionSnapshotPage;
import org.unitime.timetable.gwt.client.offerings.AssignClassInstructorsPage;
import org.unitime.timetable.gwt.client.offerings.ClassAssignmentsPage;
import org.unitime.timetable.gwt.client.offerings.ClassDetailPage;
import org.unitime.timetable.gwt.client.offerings.ClassEditPage;
import org.unitime.timetable.gwt.client.offerings.ClassesPage;
import org.unitime.timetable.gwt.client.offerings.CourseOfferingEdit;
import org.unitime.timetable.gwt.client.offerings.CrossListPage;
import org.unitime.timetable.gwt.client.offerings.InstrOfferingConfigPage;
import org.unitime.timetable.gwt.client.offerings.InstructionalOfferingsPage;
import org.unitime.timetable.gwt.client.offerings.MultipleClassSetupPage;
import org.unitime.timetable.gwt.client.offerings.OfferingDetailPage;
import org.unitime.timetable.gwt.client.offerings.SubpartDetailPage;
import org.unitime.timetable.gwt.client.offerings.SubpartEditPage;
import org.unitime.timetable.gwt.client.page.SolverWarnings;
import org.unitime.timetable.gwt.client.pointintimedata.PointInTimeDataReportsPage;
import org.unitime.timetable.gwt.client.reservations.ReservationEdit;
import org.unitime.timetable.gwt.client.reservations.ReservationsPage;
import org.unitime.timetable.gwt.client.rooms.BuildingsPage;
import org.unitime.timetable.gwt.client.rooms.RoomFeaturesPage;
import org.unitime.timetable.gwt.client.rooms.RoomGroupsPage;
import org.unitime.timetable.gwt.client.rooms.RoomPicturesPage;
import org.unitime.timetable.gwt.client.rooms.RoomSharingPage;
import org.unitime.timetable.gwt.client.rooms.RoomsPage;
import org.unitime.timetable.gwt.client.rooms.TravelTimes;
import org.unitime.timetable.gwt.client.sectioning.AdvisorCourseRequestsPage;
import org.unitime.timetable.gwt.client.sectioning.PublishedSectioningSolutionsPage;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage;
import org.unitime.timetable.gwt.client.sectioning.StudentSectioningPage;
import org.unitime.timetable.gwt.client.solver.AssignedClassesPage;
import org.unitime.timetable.gwt.client.solver.AssignmentHistoryPage;
import org.unitime.timetable.gwt.client.solver.ConflictBasedStatisticsPage;
import org.unitime.timetable.gwt.client.solver.ListSolutionsPage;
import org.unitime.timetable.gwt.client.solver.NotAssignedClassesPage;
import org.unitime.timetable.gwt.client.solver.SolutionChangesPage;
import org.unitime.timetable.gwt.client.solver.SolutionReportsPage;
import org.unitime.timetable.gwt.client.solver.SolverLogPage;
import org.unitime.timetable.gwt.client.solver.SolverPage;
import org.unitime.timetable.gwt.client.solver.SuggestionsPage;
import org.unitime.timetable.gwt.client.solver.TimetablePage;
import org.unitime.timetable.gwt.client.test.OnlineSectioningTest;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.user.client.ui.Widget;

/**
 * Register GWT pages here.
 * @author Tomas Muller
 *
 */
public enum Pages {
	curricula(new PageFactory() {
		public Widget create() { return new CurriculaPage(); }
		public String name(GwtMessages messages) { return messages.pageCurricula(); }
		}),
	curprojrules(new PageFactory() {
		public Widget create() { return new CurriculumProjectionRulesPage(); }
		public String name(GwtMessages messages) { return messages.pageCurriculumProjectionRules(); }
		}),
	sectioning(new PageFactory() {
		public Widget create() { return new StudentSectioningPage(StudentSectioningPage.Mode.SECTIONING); }
		public String name(GwtMessages messages) { return messages.pageStudentSchedulingAssistant(); }
		}),
	requests(new PageFactory() {
		public Widget create() { return new StudentSectioningPage(StudentSectioningPage.Mode.REQUESTS); }
		public String name(GwtMessages messages) { return messages.pageStudentCourseRequests(); }
		}),
	admin(new PageFactory() {
		public Widget create() { return new SimpleEditPage(); }
		public String name(GwtMessages messages) { return messages.pageAdministration(); }
		}),
	events(new PageFactory() {
		public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Events); }
		public String name(GwtMessages messages) { return messages.pageEvents(); }
		}),
	timetable(new PageFactory() {
		public Widget create() {return new EventResourceTimetable(EventResourceTimetable.PageType.Timetable); }
		public String name(GwtMessages messages) { return messages.pageEventTimetable(); }
		}),
	roomtable(new PageFactory() {
		public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.RoomTimetable); }
		public String name(GwtMessages messages) { return messages.pageRoomTimetable(); }
		}),
	reservation(new PageFactory() {
		public Widget create() { return new ReservationEdit(true); }
		public String name(GwtMessages messages) { return messages.pageEditReservation(); }
		}),
	courseOffering(new PageFactory() {
		public Widget create() { return new CourseOfferingEdit(); }
		public String name(GwtMessages messages) { return messages.pageEditCourseOffering(); }
		}),
	reservations(new PageFactory() {
		public Widget create() { return new ReservationsPage(); }
		public String name(GwtMessages messages) { return messages.pageReservations(); }
		}),
	sectioningtest(new PageFactory() {
		public Widget create() { return new OnlineSectioningTest(); }
		public String name(GwtMessages messages) { return messages.pageOnlineStudentSectioningTest(); }
		}),
	hql(new PageFactory() {
		public Widget create() { return new SavedHQLPage(); }
		public String name(GwtMessages messages) { return messages.pageCourseReports(); }
		}),
	onlinesctdash(new PageFactory() {
		public Widget create() { return new SectioningStatusPage(true); }
		public String name(GwtMessages messages) { return messages.pageOnlineStudentSchedulingDashboard(); }
		}),
	batchsctdash(new PageFactory() {
		public Widget create() { return new SectioningStatusPage(false); }
		public String name(GwtMessages messages) { return messages.pageStudentSectioningDashboard(); }
		}),
	traveltimes(new PageFactory() { 
		public Widget create() { return new TravelTimes(); }
		public String name(GwtMessages messages) { return messages.pageTravelTimes(); }
		}),
	classes(new PageFactory() {
		public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Classes); }
		public String name(GwtMessages messages) { return messages.pageClasses(); }
		}),
	exams(new PageFactory() {
		public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Exams); }
		public String name(GwtMessages messages) { return messages.pageExaminations(); }
		}),
	personal(new PageFactory() {
		public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Personal); }
		public String name(GwtMessages messages) { return messages.pagePersonalTimetable(); }
		}),
	roomavailability(new PageFactory() {
		public Widget create() { return new RoomSharingPage(); }
		public String name(GwtMessages messages) { return messages.pageEditRoomAvailability(); }
		}),
	scripts(new PageFactory() {
		public Widget create() { return new ScriptPage(); }
		public String name(GwtMessages messages) { return messages.pageScripts(); }
		}),
	availability(new PageFactory() {
		public Widget create() { return new EventRoomAvailability(); }
		public String name(GwtMessages messages) { return messages.pageEventRoomAvailability(); }
		}),
	password(new PageFactory() {
		public Widget create() { return new PasswordPage(); }
		public String name(GwtMessages messages) { return messages.pageChangePassword(); }
		}),
	sctreport(new PageFactory() {
		public Widget create() { return new SectioningReports(false); }
		public String name(GwtMessages messages) { return messages.pageBatchSectioningReports(); }
		}),
	onlinereport(new PageFactory() {
		public Widget create() { return new SectioningReports(true); }
		public String name(GwtMessages messages) { return messages.pageOnlineSectioningReports(); }
		}),
	roompictures(new PageFactory() {
		public Widget create() { return new RoomPicturesPage(); }
		public String name(GwtMessages messages) { return messages.pageRoomPictures(); }
		}),
	rooms(new PageFactory() {
		public Widget create() { return new RoomsPage(); }
		public String name(GwtMessages messages) { return messages.pageRooms(); }
		}),
	roomgroups(new PageFactory() {
		public Widget create() { return new RoomGroupsPage(); }
		public String name(GwtMessages messages) { return messages.pageRoomGroups(); }
		}),
	roomfeatures(new PageFactory() {
		public Widget create() { return new RoomFeaturesPage(); }
		public String name(GwtMessages messages) { return messages.pageRoomFeatures(); }
		}),
	instructorattributes(new PageFactory() {
		public Widget create() { return new InstructorAttributesPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructorAttributes(); }
		}),
	solver(new PageFactory() {
		public Widget create() { return new SolverPage(); }
		public String name(GwtMessages messages) { return messages.pageSolver(); }
		}),
	solverlog(new PageFactory() {
		public Widget create() { return new SolverLogPage(); }
		public String name(GwtMessages messages) { return messages.pageSolverLog(); }
		}),
	teachingRequests(new PageFactory() {
		public Widget create() { return new TeachingRequestsPage(); }
		public String name(GwtMessages messages) { return messages.pageAssignedTeachingRequests(); }
		}),
	teachingAssignments(new PageFactory() {
		public Widget create() { return new TeachingAssignmentsPage(); }
		public String name(GwtMessages messages) { return messages.pageTeachingAssignments(); }
		}),
	teachingAssignmentChanges(new PageFactory() {
		public Widget create() { return new TeachingAssignmentsChangesPage(); }
		public String name(GwtMessages messages) { return messages.pageTeachingAssignmentChanges(); }
		}),
	setupTeachingRequests(new PageFactory() {
		public Widget create() { return new SetupTeachingRequestsPage(); }
		public String name(GwtMessages messages) { return messages.pageSetupTeachingRequests(); }
		}),
	timetableGrid(new PageFactory() {
		public Widget create() { return new TimetablePage(); }
		public String name(GwtMessages messages) { return messages.pageTimetableGrid(); }
		}),
	pointInTimeDataReports(new PageFactory() {
		public Widget create() { return new PointInTimeDataReportsPage(); }
		public String name(GwtMessages messages) { return messages.pageCourseReports(); }
		}),
	assignedClasses(new PageFactory() {
		public Widget create() { return new AssignedClassesPage(); }
		public String name(GwtMessages messages) { return messages.pageAssignedClasses(); }
		}),
	notAssignedClasses(new PageFactory() {
		public Widget create() { return new NotAssignedClassesPage(); }
		public String name(GwtMessages messages) { return messages.pageNotAssignedClasses(); }
		}),
	limitAndProjectionSnapshot(new PageFactory() {
		public Widget create() { return new LimitAndProjectionSnapshotPage(); }
		public String name(GwtMessages messages) { return messages.pageLimitAndProjectionSnapshot(); }
		}),
	suggestions(new PageFactory() {
		public Widget create() { return new SuggestionsPage(); }
		public String name(GwtMessages messages) { return messages.pageSuggestions(); }
		}),
	cbs(new PageFactory() {
		public Widget create() { return new ConflictBasedStatisticsPage(); }
		public String name(GwtMessages messages) { return messages.pageConflictBasedStatistics(); }
		}),
	solutionChanges(new PageFactory() {
		public Widget create() { return new SolutionChangesPage(); }
		public String name(GwtMessages messages) { return messages.pageSolutionChanges(); }
		}),
	assignmentHistory(new PageFactory() {
		public Widget create() { return new AssignmentHistoryPage(); }
		public String name(GwtMessages messages) { return messages.pageAssignmentHistory(); }
		}),
	listSolutions(new PageFactory() {
		public Widget create() { return new ListSolutionsPage(); }
		public String name(GwtMessages messages) { return messages.pageListSolutions(); }
		}),
	solutionReports(new PageFactory() {
		public Widget create() { return new SolutionReportsPage(); }
		public String name(GwtMessages messages) { return messages.pageSolutionReports(); }
		}),
	tasks(new PageFactory() {
		public Widget create() { return new TasksPage(); }
		public String name(GwtMessages messages) { return messages.pageTasks(); }
		}),
	publishedSolutions(new PageFactory() {
		public Widget create() { return new PublishedSectioningSolutionsPage(); }
		public String name(GwtMessages messages) { return messages.pagePublishedSectioningSolutions(); }
		}),
	acrf(new PageFactory() {
		public Widget create() { return new AdvisorCourseRequestsPage(); }
		public String name(GwtMessages messages) { return messages.pageAdvisorCourseRequests(); }
		}),
	buildings(new PageFactory() {
		public Widget create() { return new BuildingsPage(); }
		public String name(GwtMessages messages) { return messages.pageBuildings(); }
		}),
	departments(new PageFactory() {
			public Widget create() { return new DepartmentsPage(); }
			public String name(GwtMessages messages) { return messages.pageDepartments(); }
			}),
	assignClassInstructors(new PageFactory() {
		public Widget create() { return new AssignClassInstructorsPage(); }
		public String name(GwtMessages messages) { return messages.pageAdministration(); }
		}),
	instructorSurvey(new PageFactory() {
		public Widget create() { return new InstructorSurveyPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructorSurvey(); }
		}),
	multipleClassSetup(new PageFactory() {
		public Widget create() { return new MultipleClassSetupPage(); }
		public String name(GwtMessages messages) { return messages.pageMultipleClassSetup(); }
		}),
	instrOfferingConfig(new PageFactory() {
		public Widget create() { return new InstrOfferingConfigPage(); }
		public String name(GwtMessages messages) { return messages.pageInstrOfferingConfig(); }
		}),
	offerings(new PageFactory() {
		public Widget create() { return new InstructionalOfferingsPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructionalOfferings(); }
		}, SolverWarnings.Type.assignments),
	classSearch(new PageFactory() {
		public Widget create() { return new ClassesPage(); }
		public String name(GwtMessages messages) { return messages.pageClassSearch(); }
		}, SolverWarnings.Type.assignments),
	classAssignments(new PageFactory() {
		public Widget create() { return new ClassAssignmentsPage(); }
		public String name(GwtMessages messages) { return messages.pageClassAssignments(); }
		}, SolverWarnings.Type.assignments),
	offering(new PageFactory() {
		public Widget create() { return new OfferingDetailPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructionalOfferingDetail(); }
		}, SolverWarnings.Type.assignments),
	subpart(new PageFactory() {
		public Widget create() { return new SubpartDetailPage(); }
		public String name(GwtMessages messages) { return messages.pageSchedulingSubpartDetail(); }
		}, SolverWarnings.Type.assignments),
	clazz(new PageFactory() {
		public Widget create() { return new ClassDetailPage(); }
		public String name(GwtMessages messages) { return messages.pageClassDetail(); }
		}, SolverWarnings.Type.assignments),
	instructors(new PageFactory() {
		public Widget create() { return new InstructorsPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructors(); }
		}, SolverWarnings.Type.assignments),
	instructor(new PageFactory() {
		public Widget create() { return new InstructorDetailPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructorDetail(); }
		}, SolverWarnings.Type.assignments),
	crosslist(new PageFactory() {
		public Widget create() { return new CrossListPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructionalOfferingCrossLists(); }
		}),
	classEdit(new PageFactory() {
		public Widget create() { return new ClassEditPage(); }
		public String name(GwtMessages messages) { return messages.pageEditClass(); }
		}),
	subpartEdit(new PageFactory() {
		public Widget create() { return new SubpartEditPage(); }
		public String name(GwtMessages messages) { return messages.pageEditSchedulingSubpart(); }
		}),
	instructorPrefs(new PageFactory() {
		public Widget create() { return new InstructorPreferencesPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructorPreferences(); }
		}),
	instrAssignmentPrefs(new PageFactory() {
		public Widget create() { return new InstructorAssignmentPreferencesPage(); }
		public String name(GwtMessages messages) { return messages.pageInstructorAssignmentPreferences(); }
		}),
	instructorAdd(new PageFactory() {
		public Widget create() { return new InstructorEditPage(); }
		public String name(GwtMessages messages) { return messages.pageAddInstructor(); }
		}),
	instructorEdit(new PageFactory() {
		public Widget create() { return new InstructorEditPage(); }
		public String name(GwtMessages messages) { return messages.pageEditInstructor(); }
		}),
	;
	
	private PageFactory iFactory;
	private SolverWarnings.Type iType;
	
	Pages(String oldTitle, PageFactory factory) { iFactory = factory; }
	Pages(PageFactory factory) { iFactory = factory; }
	Pages(PageFactory factory, SolverWarnings.Type type) { iFactory = factory; iType = type; }
	public String name(GwtMessages messages) { return iFactory.name(messages); }
	public Widget widget() { return iFactory.create(); }
	public SolverWarnings.Type getSolverWarningType() { return iType; }
	
	public interface PageFactory {
		Widget create();
		String name(GwtMessages messages);
	}
}
