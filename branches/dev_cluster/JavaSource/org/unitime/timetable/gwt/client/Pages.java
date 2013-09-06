/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.admin.PasswordPage;
import org.unitime.timetable.gwt.client.admin.ScriptPage;
import org.unitime.timetable.gwt.client.admin.SimpleEditPage;
import org.unitime.timetable.gwt.client.curricula.CurriculaPage;
import org.unitime.timetable.gwt.client.curricula.CurriculumProjectionRulesPage;
import org.unitime.timetable.gwt.client.events.EventResourceTimetable;
import org.unitime.timetable.gwt.client.events.EventRoomAvailability;
import org.unitime.timetable.gwt.client.hql.SavedHQLPage;
import org.unitime.timetable.gwt.client.reservations.ReservationEdit;
import org.unitime.timetable.gwt.client.reservations.ReservationsPage;
import org.unitime.timetable.gwt.client.rooms.RoomSharingPage;
import org.unitime.timetable.gwt.client.rooms.TravelTimes;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage;
import org.unitime.timetable.gwt.client.sectioning.StudentSectioningPage;
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
	;
	
	private PageFactory iFactory;
	
	Pages(String oldTitle, PageFactory factory) { iFactory = factory; }
	Pages(PageFactory factory) { iFactory = factory; }
	public String name(GwtMessages messages) { return iFactory.name(messages); }
	public Widget widget() { return iFactory.create(); }
	
	public interface PageFactory {
		Widget create();
		String name(GwtMessages messages);
	}
}
