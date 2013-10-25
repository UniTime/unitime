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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.curricula.CourseCurriculaTable;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.page.UniTimeBack;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.page.UniTimeMenuBar;
import org.unitime.timetable.gwt.client.page.UniTimeSideBar;
import org.unitime.timetable.gwt.client.page.UniTimeVersion;
import org.unitime.timetable.gwt.client.reservations.ReservationTable;
import org.unitime.timetable.gwt.client.rooms.RoomNoteChanges;
import org.unitime.timetable.gwt.client.rooms.RoomSharingWidget;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.sectioning.ExaminationEnrollmentTable;
import org.unitime.timetable.gwt.client.solver.SolverAllocatedMemory;

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
	header("UniTimeGWT:Header", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimePageHeader().insert(panel); } }),
	version("UniTimeGWT:Version", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeVersion().insert(panel); } }),
	back("UniTimeGWT:Back", new ComponentFactory() { public void insert(RootPanel panel) { new UniTimeBack().insert(panel); } }),
	offeringReservations("UniTimeGWT:OfferingReservations", new ComponentFactory() { public void insert(RootPanel panel) { new ReservationTable(true, true).insert(panel); } }),
	offeringReservationsReadOnly("UniTimeGWT:OfferingReservationsRO", new ComponentFactory() { public void insert(RootPanel panel) { new ReservationTable(false, true).insert(panel); } }),
	offeringEnrollments("UniTimeGWT:OfferingEnrollments", new ComponentFactory() { public void insert(RootPanel panel) { new EnrollmentTable(true, true).insert(panel); } }),
	examEnrollments("UniTimeGWT:ExamEnrollments", new ComponentFactory() { public void insert(RootPanel panel) { new ExaminationEnrollmentTable(true, true).insert(panel); } }),
	roomSharing("UniTimeGWT:RoomSharingWidget", new ComponentFactory() { public void insert(RootPanel panel) { new RoomSharingWidget(false).insert(panel, false); } }),
	roomEventAvailability("UniTimeGWT:RoomEventAvailabilityWidget", new ComponentFactory() { public void insert(RootPanel panel) { new RoomSharingWidget(false).insert(panel, true); } }),
	roomNoteChanges("UniTimeGWT:RoomNoteChanges", new ComponentFactory() { public void insert(RootPanel panel) { new RoomNoteChanges().insert(panel); } }),
	instructorAvailability("UniTimeGWT:InstructorAvailability", new ComponentFactory() { public void insert(RootPanel panel) { new InstructorAvailabilityWidget().insert(panel); } }),
	courseLink("UniTimeGWT:CourseLink", true, new ComponentFactory() { public void insert(RootPanel panel) { new CourseDetailsWidget(true).insert(panel); } }),
	courseDetails("UniTimeGWT:CourseDetails", new ComponentFactory() { public void insert(RootPanel panel) { new CourseDetailsWidget(false).insert(panel); } }),
	solverAllocatedMemory("UniTimeGWT:SolverAllocatedMem", true, new ComponentFactory() { public void insert(RootPanel panel) { new SolverAllocatedMemory().insert(panel); } }),
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
