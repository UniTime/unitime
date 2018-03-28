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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * @author Tomas Muller
 */
public class StudentSchedule extends Composite implements TakesValue<ClassAssignmentInterface> {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private ClassAssignmentInterface iAssignment;
	private UniTimeTabPanel iTabs;
	private TimeGrid iGrid;
	private WebTable iAssignments, iRequests;
	private boolean iOnline = false;
	private float iTotalCredit = 0f;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private SelectionHandler<Integer> iHandler;
	
	public StudentSchedule(boolean online) {
		iOnline = online;
		
		iTabs = new UniTimeTabPanel();
		iTabs.setDeckStyleName("unitime-TabPanel");
		
		iRequests = new WebTable();
		iRequests.setEmptyMessage(MESSAGES.emptyRequests());
		iRequests.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colPriority(), 1, "25px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colTitle(), 1, "200px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colPreferences(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colWarnings(), 1, "200px"),
				new WebTable.Cell(MESSAGES.colStatus(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colWaitList(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colRequestTimeStamp(), 1, "50px")));
		iTabs.add(iRequests, MESSAGES.tabRequests(), true);
		Character ch0 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabRequests());
		if (ch0 != null)
			iTabAccessKeys.put(ch0, 0);
		
		iAssignments = new WebTable();
		iAssignments.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colSubject(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colSubpart(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colClass(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colLimit(), 1, "60px"),
				new WebTable.Cell(MESSAGES.colDays(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colStart(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colEnd(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colDate(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colRoom(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colInstructor(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colParent(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colNoteIcon(), 1, "10px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colEnrollmentTimeStamp(), 1, "75px")
			));
		iAssignments.setEmptyMessage(MESSAGES.emptySchedule());
		
		iTabs.add(iAssignments, MESSAGES.tabClasses(), true);
		Character ch1 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabClasses());
		if (ch1 != null)
			iTabAccessKeys.put(ch1, 1);
		
		iGrid = new TimeGrid();
		iTabs.add(iGrid, MESSAGES.tabTimetable(), true);
		Character ch2 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabTimetable());
		if (ch2 != null)
			iTabAccessKeys.put(ch2, 2);
		
		iTabs.selectTab(SectioningStatusCookie.getInstance().getStudentTab());
		
		iTabs.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				SectioningStatusCookie.getInstance().setStudentTab(event.getSelectedItem());
				if (iHandler != null) iHandler.onSelection(event);
			}
		});
		
		initWidget(iTabs);
	}

	@Override
	public ClassAssignmentInterface getValue() { return iAssignment; }

	@Override
	public void setValue(ClassAssignmentInterface result) {
		iAssignment = result;
		
		fillInRequests();
		fillInAssignments();
		fillInTimeGrid();
	}
	
	protected void fillInRequests() {
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		boolean hasPref = false, hasWarn = false, hasWait = false, hasStat = false;
		NumberFormat df = NumberFormat.getFormat("0.#");
		if (iAssignment.hasRequest()) {
			CheckCoursesResponse check = new CheckCoursesResponse(iAssignment.getRequest().getConfirmations());
			hasWarn = iAssignment.getRequest().hasConfirmations();
			int priority = 1;
			for (Request request: iAssignment.getRequest().getCourses()) {
				if (!request.hasRequestedCourse()) continue;
				boolean first = true;
				if (request.isWaitList()) hasWait = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isCourse()) {
						ImageResource icon = null; String iconText = null;
						String msg = check.getMessage(rc.getCourseName(), "\n");
						if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
							icon = RESOURCES.requestError(); iconText = (msg);
						} else if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED:
								icon = RESOURCES.requestEnrolled(); iconText = (MESSAGES.enrolled(rc.getCourseName()));
								break;
							case OVERRIDE_NEEDED:
								icon = RESOURCES.requestNeeded(); iconText = (MESSAGES.overrideNeeded(msg));
								break;
							case SAVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.requested(rc.getCourseName()));
								break;				
							case OVERRIDE_REJECTED:
								icon = RESOURCES.requestRejected(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejected(rc.getCourseName()));
								break;
							case OVERRIDE_PENDING:
								icon = RESOURCES.requestPending(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(rc.getCourseName()));
								break;
							case OVERRIDE_CANCELLED:
								icon = RESOURCES.requestCancelled(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelled(rc.getCourseName()));
								break;
							case OVERRIDE_APPROVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(rc.getCourseName()));
								break;
							default:
								if (check.isError(rc.getCourseName()))
									icon = RESOURCES.requestError(); iconText = (msg);
							}
						}
						if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
						Collection<String> prefs = null;
						if (rc.hasSelectedIntructionalMethods()) {
							if (rc.hasSelectedClasses()) {
								prefs = new ArrayList<String>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
								prefs.addAll(new TreeSet<String>(rc.getSelectedIntructionalMethods()));
								prefs.addAll(new TreeSet<String>(rc.getSelectedClasses()));
							} else {
								prefs = new TreeSet<String>(rc.getSelectedIntructionalMethods());
							}
						} else if (rc.hasSelectedClasses()) {
							prefs = new TreeSet<String>(rc.getSelectedClasses());
						}
						String status = "";
						if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); hasStat = true; break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); hasStat = true; break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); hasStat = true; break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); hasStat = true; break;
							}
						}
						if (status.isEmpty() && iAssignment.getRequest().getMaxCreditOverrideStatus() != null && check.hasMessage(rc.getCourseName(), "CREDIT")) {
							switch (iAssignment.getRequest().getMaxCreditOverrideStatus()) {
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); hasStat = true; break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); hasStat = true; break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); hasStat = true; break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); hasStat = true; break;
							}
						}
						if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
						if (prefs != null) hasPref = true;
						WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
						credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						String note = null, noteTitle = null;
						if (check != null) { note = check.getMessageWithColor(rc.getCourseName(), "<br>"); noteTitle = check.getMessage(rc.getCourseName(), "\n"); }
						if (rc.hasStatusNote()) { note = (note == null ? "" : note + "<br>") + rc.getStatusNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getStatusNote(); }
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit, 
								new WebTable.Cell(ToolBox.toString(prefs)),
								new WebTable.NoteCell(note, noteTitle),
								(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
								(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell("")),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : "")
								);
						if (priority > 1 && first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
						rows.add(row);
					} else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
						}
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
								new WebTable.Cell(""),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : ""));
						if (priority > 1 && first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
						rows.add(row);
					}
					first = false;
				}
				priority ++;
			}
			priority = 1;
			for (Request request: iAssignment.getRequest().getAlternatives()) {
				if (!request.hasRequestedCourse()) continue;
				boolean first = true;
				if (request.isWaitList()) hasWait = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isCourse()) {
						ImageResource icon = null; String iconText = null;
						String msg = check.getMessage(rc.getCourseName(), "\n");
						if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
							icon = RESOURCES.requestError(); iconText = (msg);
						} else if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED:
								icon = RESOURCES.requestEnrolled(); iconText = (MESSAGES.enrolled(rc.getCourseName()));
								break;
							case OVERRIDE_NEEDED:
								icon = RESOURCES.requestNeeded(); iconText = (MESSAGES.overrideNeeded(msg));
								break;
							case SAVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.requested(rc.getCourseName()));
								break;				
							case OVERRIDE_REJECTED:
								icon = RESOURCES.requestRejected(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejected(rc.getCourseName()));
								break;
							case OVERRIDE_PENDING:
								icon = RESOURCES.requestPending(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(rc.getCourseName()));
								break;
							case OVERRIDE_CANCELLED:
								icon = RESOURCES.requestCancelled(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelled(rc.getCourseName()));
								break;
							case OVERRIDE_APPROVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(rc.getCourseName()));
								break;
							default:
								if (check.isError(rc.getCourseName()))
									icon = RESOURCES.requestError(); iconText = (msg);
							}
						}
						if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
						Collection<String> prefs = null;
						if (rc.hasSelectedIntructionalMethods()) {
							if (rc.hasSelectedClasses()) {
								prefs = new ArrayList<String>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
								prefs.addAll(new TreeSet<String>(rc.getSelectedIntructionalMethods()));
								prefs.addAll(new TreeSet<String>(rc.getSelectedClasses()));
							} else {
								prefs = new TreeSet<String>(rc.getSelectedIntructionalMethods());
							}
						} else if (rc.hasSelectedClasses()) {
							prefs = new TreeSet<String>(rc.getSelectedClasses());
						}
						if (prefs != null) hasPref = true;
						String status = "";
						if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); hasStat = true; break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); hasStat = true; break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); hasStat = true; break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); hasStat = true; break;
							}
						}
						if (status.isEmpty() && iAssignment.getRequest().getMaxCreditOverrideStatus() != null && check.hasMessage(rc.getCourseName(), "CREDIT")) {
							switch (iAssignment.getRequest().getMaxCreditOverrideStatus()) {
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); hasStat = true; break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); hasStat = true; break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); hasStat = true; break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); hasStat = true; break;
							}
						}
						if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
						WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
						credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						String note = null, noteTitle = null;
						if (check != null) { note = check.getMessageWithColor(rc.getCourseName(), "<br>"); noteTitle = check.getMessage(rc.getCourseName(), "\n"); }
						if (rc.hasStatusNote()) { note = (note == null ? "" : note + "<br>") + rc.getStatusNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getStatusNote(); }
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsAlternative(priority) : ""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit,
								new WebTable.Cell(ToolBox.toString(prefs)),
								new WebTable.NoteCell(note, noteTitle),
								(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
								(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell("")),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : "")
								);
						if (first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
						rows.add(row);
					} else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
						}
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
								new WebTable.Cell(""),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : ""));
						if (first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
						rows.add(row);
					}
					first = false;
				}
				priority ++;
			}
		}

		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		iRequests.setData(rowArray);
		iRequests.setColumnVisible(4, hasPref);
		iRequests.setColumnVisible(5, hasWarn);
		iRequests.setColumnVisible(6, hasStat);
		iRequests.setColumnVisible(7, hasWait);
	}
	
	protected void fillInAssignments() {
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		iTotalCredit = 0f;
		for (ClassAssignmentInterface.CourseAssignment course: iAssignment.getCourseAssignments()) {
			if (course.isAssigned()) {
				boolean firstClazz = true;
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
					if (clazz.isTeachingAssignment()) style += (clazz.isInstructing() ? " text-steelblue" : " text-steelblue-italic");
					final WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : ""),
							new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr() : ""),
							new WebTable.Cell(clazz.getSubpart()),
							new WebTable.Cell(clazz.getSection()),
							new WebTable.Cell(clazz.getLimitString()),
							new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
							new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
							new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
							new WebTable.Cell(clazz.getDatePattern()),
							(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
							new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
							new WebTable.Cell(clazz.getParentSection()),
							clazz.hasNote() ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "") : new WebTable.Cell(""),
							new WebTable.AbbvTextCell(clazz.getCredit()),
							new WebTable.Cell(clazz.getEnrolledDate() == null ? "" : sDF.format(clazz.getEnrolledDate())));
					if (clazz.isTeachingAssignment())
						row.setStyleName("teaching-assignment");
					rows.add(row);
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					firstClazz = false;
					if (!clazz.isTeachingAssignment())
						iTotalCredit += clazz.guessCreditCount();
				}
			} else {
				String style = "text-red" + (!rows.isEmpty() ? " top-border-dashed": "");
				WebTable.Row row = null;
				String unassignedMessage = MESSAGES.courseNotAssigned();
				if (course.hasEnrollmentMessage())
					unassignedMessage = course.getEnrollmentMessage();
				else if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
					unassignedMessage = "";
					for (Iterator<String> i = course.getOverlaps().iterator(); i.hasNext();) {
						String x = i.next();
						if (unassignedMessage.isEmpty())
							unassignedMessage += MESSAGES.conflictWithFirst(x);
						else if (!i.hasNext())
							unassignedMessage += MESSAGES.conflictWithLast(x);
						else
							unassignedMessage += MESSAGES.conflictWithMiddle(x);
						if (i.hasNext()) unassignedMessage += ", ";
					}
					if (course.getInstead() != null)
						unassignedMessage += MESSAGES.conflictAssignedAlternative(course.getInstead());
					unassignedMessage += ".";
				} else if (course.isNotAvailable()) {
					if (course.isFull())
						unassignedMessage = MESSAGES.courseIsFull();
					else
						unassignedMessage = MESSAGES.classNotAvailable();
				} else if (course.isLocked()) {
					unassignedMessage = MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr());
				}
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					row = new WebTable.Row(
							new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
							new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
							new WebTable.Cell(clazz.getSubpart()),
							new WebTable.Cell(clazz.getSection()),
							new WebTable.Cell(clazz.getLimitString()),
							new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
							new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
							new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
							new WebTable.Cell(clazz.getDatePattern()),
							new WebTable.Cell(unassignedMessage, 3, null),
							clazz.getNote() == null ? new WebTable.Cell("") : new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), ""),
							new WebTable.AbbvTextCell(clazz.getCredit()),
							new WebTable.Cell(clazz.getEnrolledDate() != null ? sDF.format(clazz.getEnrolledDate()) : course.getRequestedDate() == null ? "" : sDF.format(course.getRequestedDate())));
					break;
				}
				if (row == null) {
					row = new WebTable.Row(
							new WebTable.Cell(course.getSubject()),
							new WebTable.Cell(course.getCourseNbr()),
							new WebTable.Cell(unassignedMessage, 12, null),
							new WebTable.Cell(course.getRequestedDate() == null ? "" : sDF.format(course.getRequestedDate())));
				}
				for (WebTable.Cell cell: row.getCells())
					cell.setStyleName(style);
				row.getCell(row.getNrCells() - 2).setStyleName("text-gray" + (!rows.isEmpty() ? " top-border-dashed": ""));
				rows.add(row);
			}
		}
		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		iAssignments.setData(rowArray);
		if (!iOnline) {
			for (int row = 0; row < iAssignments.getTable().getRowCount(); row++) {
				if (iAssignments.getTable().getCellCount(row) > 2)
					iAssignments.getTable().getFlexCellFormatter().setVisible(row, iAssignments.getTable().getCellCount(row) - 2, false);
			}
		}
	}
	
	protected void fillInTimeGrid() {
		iGrid.clear(true);
		int index = 0;
		for (ClassAssignmentInterface.CourseAssignment course: iAssignment.getCourseAssignments()) {
			for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
				if (clazz.isFreeTime()) {
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setLength(clazz.getLength());
					ft.setStart(clazz.getStart());
					for (int d: clazz.getDays()) ft.addDay(d);
					iGrid.addFreeTime(ft);
				} else if (clazz.isAssigned()) {
					iGrid.addClass(clazz, index++);
				}
			}
		}
		iGrid.shrink();
	}
	
	public float getTotalCredit() { return iTotalCredit; }
	
	public float[] getCreditRange() { return iAssignment == null || !iAssignment.hasRequest() ? null : iAssignment.getRequest().getCreditRange(); }
	
	public String getCreditMessage() {
		if (iTabs.getSelectedTab() == 0) {
			float[] range = getCreditRange();
			if (range != null && range[1] > 0f) {
				if (range[0] == range[1]) return MESSAGES.requestedCredit(range[0]);
				else return MESSAGES.requestedCreditRange(range[0], range[1]);
			} else {
				return "";
			}
		} else {
			if (iTotalCredit > 0f)
				return MESSAGES.totalCredit(iTotalCredit);
			else
				return "";
		}
	}
	
	public void checkAccessKeys(NativePreviewEvent event) {
		if (event.getTypeInt() == Event.ONKEYUP && (event.getNativeEvent().getAltKey() || event.getNativeEvent().getCtrlKey())) {
			for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
				if (event.getNativeEvent().getKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeEvent().getKeyCode()  == Character.toUpperCase(entry.getKey())) {
					iTabs.selectTab(entry.getValue());
				}
		}
	}
	
	public void setSelectionHandler(SelectionHandler<Integer> handler) {
		iHandler = handler;
	}
}
