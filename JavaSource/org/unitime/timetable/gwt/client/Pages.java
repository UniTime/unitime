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

import org.unitime.timetable.gwt.client.admin.SimpleEditPage;
import org.unitime.timetable.gwt.client.curricula.CurriculaPage;
import org.unitime.timetable.gwt.client.curricula.CurriculumProjectionRulesPage;
import org.unitime.timetable.gwt.client.events.EventResourceTimetable;
import org.unitime.timetable.gwt.client.hql.SavedHQLPage;
import org.unitime.timetable.gwt.client.reservations.ReservationEdit;
import org.unitime.timetable.gwt.client.reservations.ReservationsPage;
import org.unitime.timetable.gwt.client.rooms.RoomSharingPage;
import org.unitime.timetable.gwt.client.rooms.TravelTimes;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusPage;
import org.unitime.timetable.gwt.client.sectioning.StudentSectioningPage;
import org.unitime.timetable.gwt.client.test.OnlineSectioningTest;

import com.google.gwt.user.client.ui.Widget;

/**
 * Register GWT pages here.
 * @author Tomas Muller
 *
 */
public enum Pages {
	curricula("Curricula", new PageFactory() { public Widget create() { return new CurriculaPage(); } }),
	curprojrules("Curriculum Projection Rules", new PageFactory() { public Widget create() { return new CurriculumProjectionRulesPage(); } }),
	sectioning("Student Scheduling Assistant", new PageFactory() { public Widget create() { return new StudentSectioningPage(StudentSectioningPage.Mode.SECTIONING); } }),
	requests("Student Course Requests", new PageFactory() { public Widget create() { return new StudentSectioningPage(StudentSectioningPage.Mode.REQUESTS); } }),
	admin("Administration", new PageFactory() { public Widget create() { return new SimpleEditPage(); } }),
	events("Events", new PageFactory() { public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Events); } }),
	timetable("Event Timetable", new PageFactory() { public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Timetable); } }),
	roomtable("Room Timetable", new PageFactory() { public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.RoomTimetable); } }),
	reservation("Reservation", new PageFactory() { public Widget create() { return new ReservationEdit(true); } }),
	reservations("Reservations", new PageFactory() { public Widget create() { return new ReservationsPage(); } }),
	sectioningtest("Online Student Sectioning Test", new PageFactory() { public Widget create() { return new OnlineSectioningTest(); } }),
	hql("Simple Reports", new PageFactory() { public Widget create() { return new SavedHQLPage(); } }),
	onlinesctdash("Online Student Scheduling Dashboard", new PageFactory() { public Widget create() { return new SectioningStatusPage(true); } }),
	batchsctdash("Student Sectioning Dashboard", new PageFactory() { public Widget create() { return new SectioningStatusPage(false); } }),
	traveltimes("Travel Times", new PageFactory() { public Widget create() { return new TravelTimes(); } }),
	classes("Classes", new PageFactory() { public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Classes); } }),
	exams("Examinations", new PageFactory() { public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Exams); } }),
	personal("Personal Timetable", new PageFactory() { public Widget create() { return new EventResourceTimetable(EventResourceTimetable.PageType.Personal); } }),
	roomavailability("Edit Room Availability", new PageFactory() { public Widget create() { return new RoomSharingPage(); } }),
	;
	
	private String iTitle;
	private PageFactory iFactory;
	
	Pages(String title, PageFactory factory) { iTitle = title; iFactory = factory; }
	public String title() { return iTitle; }
	public Widget widget() { return iFactory.create(); }
	
	public interface PageFactory {
		Widget create();
	}
}
