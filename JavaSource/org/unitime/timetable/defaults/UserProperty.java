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
package org.unitime.timetable.defaults;

import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
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
	
	RoomFeaturesInOneColumn("roomFeaturesInOneColumn", CommonValues.Yes, "Display Room Features In One Column"),
	HighlighClassPreferences("highlightClassPrefs", CommonValues.UseSystemDefault, "Highlight preferences that are set directly on classes"),
	PrimaryCampus("primaryAcademicInitiative", "Primary academic initiative"),
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
