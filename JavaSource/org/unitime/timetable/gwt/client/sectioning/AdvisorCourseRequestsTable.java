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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestsTable extends WebTable implements TakesValue<CourseRequestInterface> {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private CourseRequestInterface iAdvisorRequests;
	
	public AdvisorCourseRequestsTable() {
		setEmptyMessage(MESSAGES.emptyRequests());
		setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colPriority(), 1, "25px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colTitle(), 1, "200px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colPreferences(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colCritical(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colNotes(), 1, "300px")));
		addStyleName("unitime-AdvisorCourseRequestsTable");
		setSelectSameIdRows(true);
	}

	@Override
	public void setValue(CourseRequestInterface requests) {
		iAdvisorRequests = requests;
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		boolean hasPref = false;
		boolean hasCrit = false;
		int priority = 1;
		for (Request request: iAdvisorRequests.getCourses()) {
			if (request.hasRequestedCourse()) {
				if (request.isCritical() || request.isImportant()) hasCrit = true;
				boolean first = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					WebTable.Row row = null;
					if (rc.isCourse()) {
						Collection<Preference> prefs = null;
						if (rc.hasSelectedIntructionalMethods()) {
							if (rc.hasSelectedClasses()) {
								prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
							} else {
								prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
							}
						} else if (rc.hasSelectedClasses()) {
							prefs = new TreeSet<Preference>(rc.getSelectedClasses());
						}
						if (prefs != null) hasPref = true;
						if (first) {
							WebTable.Cell credit = new WebTable.Cell(request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
							credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
							WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
							note.setRowSpan(request.getRequestedCourse().size());
							row = new WebTable.Row(
								new WebTable.Cell(MESSAGES.courseRequestsPriority(priority)),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit,
								new WebTable.Cell(ToolBox.toString(prefs), true),
								request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
								request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell(""),
								note
								);
						} else {
							row = new WebTable.Row(
								new WebTable.Cell(""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								new WebTable.Cell(""),
								new WebTable.Cell(ToolBox.toString(prefs), true),
								new WebTable.Cell("")
								);
						}
					} else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
						}
						if (first) {
							WebTable.Cell credit = new WebTable.Cell(first && request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
							credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
							WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
							note.setRowSpan(request.getRequestedCourse().size());
							row = new WebTable.Row(
								new WebTable.Cell(MESSAGES.courseRequestsPriority(priority)),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 2, null),
								credit,
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								note
								);
						} else {
							row = new WebTable.Row(
								new WebTable.Cell(""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 2, null),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.Cell("")
								);
						}
					}
					if (priority > 1 && first)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
					row.setId("P" + priority);
					rows.add(row);
					first = false;
				}
				priority ++;
			} else {
				WebTable.Cell credit = new WebTable.Cell(request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
				credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
				WebTable.Row row = new WebTable.Row(
					new WebTable.Cell(MESSAGES.courseRequestsPriority(priority)),
					new WebTable.Cell(""),
					new WebTable.Cell(""),
					credit,
					new WebTable.Cell(""),
					new WebTable.Cell(""),
					note
					);
				if (priority > 1)
					for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
				row.setId("P" + priority);
				rows.add(row);
			}
		}
		priority = 1;
		for (Request request: iAdvisorRequests.getAlternatives()) {
			if (request.hasRequestedCourse()) {
				if (request.isCritical() || request.isImportant()) hasCrit = true;
				boolean first = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					WebTable.Row row = null;
					if (rc.isCourse()) {
						Collection<Preference> prefs = null;
						if (rc.hasSelectedIntructionalMethods()) {
							if (rc.hasSelectedClasses()) {
								prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
								prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
							} else {
								prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
							}
						} else if (rc.hasSelectedClasses()) {
							prefs = new TreeSet<Preference>(rc.getSelectedClasses());
						}
						if (prefs != null) hasPref = true;
						if (first) {
							WebTable.Cell credit = new WebTable.Cell(request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
							credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
							WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
							note.setRowSpan(request.getRequestedCourse().size());
							row = new WebTable.Row(
								new WebTable.Cell(MESSAGES.courseRequestsAlternate(priority)),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit,
								new WebTable.Cell(ToolBox.toString(prefs), true),
								request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
								request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell(""),
								note
								);
						} else {
							row = new WebTable.Row(
								new WebTable.Cell(""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								new WebTable.Cell(""),
								new WebTable.Cell(ToolBox.toString(prefs), true),
								new WebTable.Cell("")
								);
						}
					}
					if (first)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
					row.setId("A" + priority);
					rows.add(row);
					first = false;
				}
				priority ++;
			} else {
				WebTable.Cell credit = new WebTable.Cell(request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
				credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
				WebTable.Row row = new WebTable.Row(
					new WebTable.Cell(MESSAGES.courseRequestsAlternate(priority)),
					new WebTable.Cell(""),
					new WebTable.Cell(""),
					credit,
					new WebTable.Cell(""),
					new WebTable.Cell(""),
					note
					);
				for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
				row.setId("A" + priority);
				rows.add(row);
			}
		}
		float min = 0, max = 0;
		for (Request request: iAdvisorRequests.getCourses()) {
			min += request.getAdvisorCreditMin();
			max += request.getAdvisorCreditMax();
		}
		WebTable.Cell credit = new WebTable.Cell(min < max ? MESSAGES.creditRange(min, max) : MESSAGES.credit(min));
		String noteMessage = (iAdvisorRequests.hasCreditNote() ? iAdvisorRequests.getCreditNote() : "");
		if (iAdvisorRequests.hasReleasedPin() && !noteMessage.contains(iAdvisorRequests.getPin()))
			noteMessage += (noteMessage.isEmpty() ? "" : "\n") + MESSAGES.advisorNotePin(iAdvisorRequests.getPin());
		WebTable.NoteCell note = new WebTable.NoteCell(noteMessage, null);
		note.setColSpan(3);
		credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		WebTable.Row crow = new WebTable.Row(
				new WebTable.Cell(MESSAGES.rowTotalPriorityCreditHours(), 2, null),
				new WebTable.Cell(""),
				credit,
				note
				);
		for (WebTable.Cell cell: crow.getCells()) cell.setStyleName("top-border-solid");
		crow.getCell(0).setStyleName("top-border-solid text-bold");
		crow.setId("C");
		rows.add(crow);
		
		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		setData(rowArray);
		setColumnVisible(4, hasPref);
		setColumnVisible(5, hasCrit);
	}

	@Override
	public CourseRequestInterface getValue() {
		return iAdvisorRequests;
	}

}
