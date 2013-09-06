/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.defaults;

import org.unitime.timetable.security.UserContext;

public enum UserProperty {
	LastAcademicSession("LastUsed.acadSessionId", "Last used academic session id"),
	
	CourseOfferingNoteDisplay("crsOffrNoteDisplay", CommonValues.NoteAsIcon, "Display an icon or shortened text when a course offering has a schedule note."),
	SchedulePrintNoteDisplay("printNoteDisplay", CommonValues.NoteAsIcon, "Display an icon or shortened text when a class has a schedule print note."),
	ManagerNoteDisplay("mgrNoteDisplay", CommonValues.NoteAsIcon, "Display an icon or shortened text when a class has a note to the schedule manager."),
	GridOrientation("timeGrid", CommonValues.VerticalGrid, "Time grid display format"),
	GridSize("timeGridSize", "Workdays x Daytime", "Time grid default selection"),
	NameFormat("name", CommonValues.NameLastInitial, "Instructor name display format"),
	ClassesKeepSort("keepSort", CommonValues.No, "Sort classes on detail pages based on Classes page sorting options."),
	ConfirmationDialogs("jsConfirm", CommonValues.Yes, "Display confirmation dialogs"),
	
	InheritInstructorPrefs("inheritInstrPref", CommonValues.Never, "Inherit instructor preferences on a class"),
	DisplayLastChanges("dispLastChanges", CommonValues.Yes, "Display last changes"),
	
	DispInstructorPrefs("InstructorDetail.distPref", CommonValues.Yes, "Display instructor preferences"),
	VariableClassLimits("showVarLimits", CommonValues.No, "Show the option to set variable class limits"),
	ConfigAutoCalc("cfgAutoCalc", CommonValues.Yes, "Automatically calculate number of classes and room size when editing configuration"),
		
	SortNames("instrNameSort", CommonValues.SortByLastName, "Sort instructor names"),
	
	RoomFeaturesInOneColumn("roomFeaturesInOneColumn", CommonValues.Yes, "Display Room Features In One Column");
	;

	String iKey, iDefault, iDescription;
	UserProperty(String key, String defaultValue, String description) {
		iKey = key; iDefault = defaultValue; iDescription = defaultValue;
	}
	UserProperty(String key, String description) { this(key, (String)null, description); }
	UserProperty(String key, CommonValues defaultValue, String description) { this(key, defaultValue.value(), description); }
	
	public String key() { return iKey; }
	public String defaultValue() { return iDefault; }
	public String description() { return iDescription; }
	
	public String get(UserContext user) {
		return user == null ? defaultValue() : user.getProperty(this);
	}
}
