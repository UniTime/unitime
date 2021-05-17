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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.MenuBarWithAccessKeys;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Note;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestPriority;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class StudentSchedule extends Composite implements TakesValue<ClassAssignmentInterface> {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private static DateTimeFormat sTSF = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private ClassAssignmentInterface iAssignment;
	private UniTimeTabPanel iTabs;
	private TimeGrid iGrid;
	private WebTable iAssignments, iRequests, iAdvReqs, iNotes;
	private UniTimeTable<RetrieveSpecialRegistrationResponse> iSpecialRegistrations;
	private boolean iOnline = false;
	private float iTotalCredit = 0f;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private SelectionHandler<Integer> iHandler;
	private static DateTimeFormat sModifiedDateFormat = ServerDateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	public StudentSchedule(boolean online) {
		iOnline = online;
		
		iTabs = new UniTimeTabPanel();
		iTabs.setDeckStyleName("unitime-TabPanel");
		
		iAdvReqs = new WebTable();
		iAdvReqs.setEmptyMessage(MESSAGES.emptyRequests());
		iAdvReqs.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colPriority(), 1, "25px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colTitle(), 1, "200px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colPreferences(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colCritical(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colWaitList(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colNotes(), 1, "300px"),
				new WebTable.Cell(MESSAGES.colChanges(), 1, "100px")));
		iAdvReqs.setSelectSameIdRows(true);
		iAdvReqs.addStyleName("unitime-AdvisorCourseRequestsTable");
		iTabs.add(iAdvReqs, MESSAGES.tabAdvisorRequests(), true);
		Character cha = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabAdvisorRequests());
		if (cha != null)
			iTabAccessKeys.put(cha, 0);
		
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
				new WebTable.Cell(MESSAGES.colCritical(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colWaitList(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colRequestTimeStamp(), 1, "50px")));
		iTabs.add(iRequests, MESSAGES.tabRequests(), true);
		Character ch0 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabRequests());
		if (ch0 != null)
			iTabAccessKeys.put(ch0, 1);
		
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
		
		iNotes = new WebTable();
		iNotes.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colTimeStamp(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colNote(), 1, "400x"),
				new WebTable.Cell(MESSAGES.colNoteAuthor(), 1, "75x")
			));
		iNotes.setEmptyMessage(MESSAGES.emptyNotes());
		
		iSpecialRegistrations = new UniTimeTable<RetrieveSpecialRegistrationResponse>();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(""));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegSubmitted()));
		header.add(new UniTimeTableHeader(MESSAGES.colSubject()));
		header.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		header.add(new UniTimeTableHeader(MESSAGES.colSubpart()));
		header.add(new UniTimeTableHeader(MESSAGES.colClass()));
		header.add(new UniTimeTableHeader(MESSAGES.colLimit()));
		header.add(new UniTimeTableHeader(MESSAGES.colCredit()));
		header.add(new UniTimeTableHeader(MESSAGES.colGradeMode()));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegErrors()));
		header.add(new UniTimeTableHeader(""));
		iSpecialRegistrations.addStyleName("unitime-SpecialRegistrationsPanel");
		iSpecialRegistrations.addRow(null, header);
		
		iTabs.add(iAssignments, MESSAGES.tabClasses(), true);
		Character ch1 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabClasses());
		if (ch1 != null)
			iTabAccessKeys.put(ch1, 2);
		
		iGrid = new TimeGrid();
		iTabs.add(iGrid, MESSAGES.tabTimetable(), true);
		Character ch2 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabTimetable());
		if (ch2 != null)
			iTabAccessKeys.put(ch2, 3);
		
		if (iOnline) {
			iTabs.add(iSpecialRegistrations, MESSAGES.tabSpecialRegistrations(), true);
			Character ch4 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabSpecialRegistrations());
			if (ch4 != null)
				iTabAccessKeys.put(ch4, 4);
		}
		
		if (iOnline) {
			iTabs.add(iNotes, MESSAGES.tabNotes(), true);
			Character ch3 = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabNotes());
			if (ch3 != null)
				iTabAccessKeys.put(ch3, 5);
		}
		
		if (!iOnline && SectioningStatusCookie.getInstance().getStudentTab() >= 4)
			iTabs.selectTab(2);
		else
			iTabs.selectTab(SectioningStatusCookie.getInstance().getStudentTab());
		
		iTabs.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				SectioningStatusCookie.getInstance().setStudentTab(event.getSelectedItem());
				if (iHandler != null) iHandler.onSelection(event);
			}
		});
		
		iTabs.addStyleName("unitime-StudentSchedule");
		initWidget(iTabs);
	}
	
	public void setWaitListMode(WaitListMode mode, boolean advisorRequests, boolean studentRequests) {
		switch (mode) {
		case WaitList:
			if (studentRequests) iRequests.getTable().setHTML(0, 8, MESSAGES.colWaitList());
			if (advisorRequests) iAdvReqs.getTable().setHTML(0, 6, MESSAGES.colWaitList());
			break;
		case NoSubs:
		default:
			if (studentRequests) iRequests.getTable().setHTML(0, 8, MESSAGES.colNoSubs());
			if (advisorRequests) iAdvReqs.getTable().setHTML(0, 6, MESSAGES.colNoSubs());
			break;
		}
	}

	@Override
	public ClassAssignmentInterface getValue() { return iAssignment; }

	@Override
	public void setValue(ClassAssignmentInterface result) {
		iAssignment = result;
		fillInAdvisorRequests();
		fillInRequests();
		fillInAssignments();
		fillInTimeGrid();
		if (iOnline) fillInNotes();
		if (iOnline) fillInSpecialRegistrations();
	}
	
	protected String getChanges(Request request, RequestedCourse rc) {
		if (!rc.hasCourseId()) return null;
		RequestPriority arp = iAssignment.getAdvisorRequest().getRequestPriority(rc);
		RequestPriority rp = (iAssignment.hasRequest() ? iAssignment.getRequest().getRequestPriority(rc) : null);
		if (arp.getChoice() == 0) {
			if (rp == null) {
				// not present among student's CR
				if (request.getRequestedCourse().size() > 1) {
					RequestPriority top = null;
					RequestedCourse other = null;
					for (RequestedCourse x: request.getRequestedCourse()) {
						RequestPriority p = (iAssignment.hasRequest() ? iAssignment.getRequest().getRequestPriority(x) : null);
						if (p != null && (top == null || top.compareTo(p) >= 0)) {
							top = p; other = x;
						}
					}
					if (top != null) {
						String prio = "&rarr; ";
						if (top.getPriority() != arp.getPriority()) {
							prio = top.getPriority() < arp.getPriority() ? "&uarr; " : "&darr; ";
						}
						return prio + MESSAGES.advChangesMissingCourseButHasAlt(rc.getCourseName(), other.getCourseName());
					}
					return MESSAGES.advChangesMissingCourseWithAllAlts(rc.getCourseName());
				}
				return MESSAGES.advChangesMissingCourse(rc.getCourseName()); 
			}
			if (rp.isAlternative() != arp.isAlternative()) {
				if (rp.isAlternative()) return MESSAGES.advChangesPrimaryToSubstitute(rc.getCourseName(), rp.getPriority());
				return MESSAGES.advChangesSubstituteToPrimary(rc.getCourseName(), rp.getPriority());
			}
			String prio = "&rarr; ";
			if (rp.getPriority() != arp.getPriority()) {
				prio = rp.getPriority() < arp.getPriority() ? "&uarr; " : "&darr; ";
			}
			if (rp.getChoice() != arp.getChoice()) {
				RequestedCourse ch1 = (rp.isAlternative() ? iAssignment.getRequest().getAlternatives() : iAssignment.getRequest().getCourses()).get(rp.getPriority() - 1).getRequestedCourse(0);
				RequestPriority p = iAssignment.getAdvisorRequest().getRequestPriority(ch1);
				if (p != null && p.getPriority() == arp.getPriority()) {
					return prio + MESSAGES.advChanges1stChoiceChanged(ch1.getCourseName());
				} else {
					return prio + MESSAGES.advChangesDifferent1stChoice(ch1.getCourseName());
				}
			}
			if (rp.getPriority() != arp.getPriority()) {
				return prio + (rp.isAlternative() ? MESSAGES.advChangesMovedToSubstitute(rp.getPriority()) : MESSAGES.advChangesMovedToPriority(rp.getPriority()));
			}
		} else {
			if (rp == null) {
				if (iAssignment.getRequest().getRequestPriority(request.getRequestedCourse(0)) != null)
					return MESSAGES.advChangesMissingCourse(rc.getCourseName());
				int nrContains = 0;
				for (RequestedCourse x: request.getRequestedCourse())
					if (iAssignment.getRequest().getRequestPriority(x) != null) nrContains ++;
				if (nrContains > 1)
					return MESSAGES.advChangesMissingCourse(rc.getCourseName());
				return null;
			}
			if (rp.isAlternative() != arp.isAlternative()) {
				if (rp.isAlternative()) return MESSAGES.advChangesPrimaryToSubstitute(rc.getCourseName(), rp.getPriority());
				return MESSAGES.advChangesSubstituteToPrimary(rc.getCourseName(), rp.getPriority());
			}
			RequestPriority top = null;
			for (RequestedCourse x: request.getRequestedCourse()) {
				RequestPriority p = (iAssignment.hasRequest() ? iAssignment.getRequest().getRequestPriority(x) : null);
				if (p != null && (top == null || top.compareTo(p) >= 0)) {
					top = p;
				}
			}
			if (rp.getChoice() != arp.getChoice()) {
				if (rp.getChoice() == 0) {
					if (top.getPriority() == rp.getPriority() && top.getChoice() == rp.getChoice()) return null;
					if (top.getPriority() != rp.getPriority() && rp.getPriority() != arp.getPriority()) {
						String prio = rp.getPriority() < arp.getPriority() ? "&uarr; " : "&darr; ";
						return prio + (rp.isAlternative() ? MESSAGES.advChangesMovedToSubstitute(rp.getPriority()) : MESSAGES.advChangesMovedToPriority(rp.getPriority()));
					}
					return MESSAGES.advChangesMoved1stChoice();
				}
				RequestedCourse ch1 = (rp.isAlternative() ? iAssignment.getRequest().getAlternatives() : iAssignment.getRequest().getCourses()).get(rp.getPriority() - 1).getRequestedCourse(0);
				RequestPriority p = iAssignment.getAdvisorRequest().getRequestPriority(ch1);
				if (p == null || p.getPriority() != arp.getPriority()) {
					String prio = "&rarr; ";
					if (rp.getPriority() != arp.getPriority()) {
						prio = rp.getPriority() < arp.getPriority() ? "&uarr; " : "&darr; ";
					}
					return prio + MESSAGES.advChangesDifferent1stChoice(ch1.getCourseName());
				}
				if (rp.getChoice() == 1) return MESSAGES.advChangesMoved2ndChoice();
				if (rp.getChoice() == 2) return MESSAGES.advChangesMoved3rdChoice();
			} else if (top.getPriority() != rp.getPriority() && rp.getPriority() != arp.getPriority()) {
				String prio = "";
				if (top.getPriority() != rp.getPriority() && rp.getPriority() != arp.getPriority()) {
					prio = rp.getPriority() < arp.getPriority() ? "&uarr; " : "&darr; ";
				}
				return prio + (rp.isAlternative() ? MESSAGES.advChangesMovedToSubstitute(rp.getPriority()) : MESSAGES.advChangesMovedToPriority(rp.getPriority()));
			}
		}
		return null;
	}
	
	protected void fillInAdvisorRequests() {
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		boolean hasPref = false;
		boolean hasCrit = false;
		boolean hasWL = false;
		boolean hasChanges = false;
		iTabs.getTabBar().setTabEnabled(0, iAssignment.hasAdvisorRequest());
		if (iAssignment.hasAdvisorRequest()) {
			setWaitListMode(iAssignment.getAdvisorRequest().getWaitListMode(), true, false);
			int priority = 1;
			for (Request request: iAssignment.getAdvisorRequest().getCourses()) {
				if (request.hasRequestedCourse()) {
					if (request.isCritical() || request.isImportant()) hasCrit = true;
					if (iAssignment.getAdvisorRequest().getWaitListMode() == WaitListMode.WaitList) {
						if (request.isWaitList()) hasWL = true;
					} else {
						if (request.isNoSub()) hasWL = true;
					}
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
							String changes = getChanges(request, rc);
							if (changes != null) hasChanges = true;
							boolean enrolled = false;
							for (ClassAssignmentInterface.CourseAssignment course: iAssignment.getCourseAssignments()) {
								if (course.isAssigned() && !course.isTeachingAssignment() && !course.isFreeTime() && course.getCourseId().equals(rc.getCourseId())) {
									enrolled = true;
									break;
								}
							}
							if (first) {
								WebTable.Cell credit = new WebTable.Cell(request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
								credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
								WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
								note.setRowSpan(request.getRequestedCourse().size());
								row = new WebTable.Row(
									new WebTable.Cell(MESSAGES.courseRequestsPriority(priority)),
									(enrolled ? new WebTable.IconCell(RESOURCES.courseEnrolled(), MESSAGES.titleCourseEnrolled(rc.getCourseName()), rc.getCourseName(), true) : new WebTable.Cell(rc.getCourseName())),
									new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
									credit,
									new WebTable.Cell(ToolBox.toString(prefs), true),
									request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
									request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell(""),
									(iAssignment.getAdvisorRequest().getWaitListMode() == WaitListMode.WaitList
										? (request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell(""))
										: (request.isNoSub() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestNoSubs(), "") : new WebTable.Cell(""))),
									note,
									new WebTable.Cell(changes)
									);
							} else {
								row = new WebTable.Row(
									new WebTable.Cell(""),
									(enrolled ? new WebTable.IconCell(RESOURCES.courseEnrolled(), MESSAGES.titleCourseEnrolled(rc.getCourseName()), rc.getCourseName(), true) : new WebTable.Cell(rc.getCourseName())),
									new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
									new WebTable.Cell(""),
									new WebTable.Cell(ToolBox.toString(prefs), true),
									new WebTable.Cell(""),
									new WebTable.Cell(""),
									new WebTable.Cell(changes)
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
									new WebTable.Cell(""),
									note,
									new WebTable.Cell("")
									);
							} else {
								row = new WebTable.Row(
									new WebTable.Cell(""),
									new WebTable.Cell(CONSTANTS.freePrefix() + free, 2, null),
									new WebTable.Cell(""),
									new WebTable.Cell(""),
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
						new WebTable.Cell(""),
						note,
						new WebTable.Cell("")
						);
					if (priority > 1)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
					row.setId("P" + priority);
					rows.add(row);
				}
			}
			priority = 1;
			for (Request request: iAssignment.getAdvisorRequest().getAlternatives()) {
				if (request.hasRequestedCourse()) {
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
							String changes = getChanges(request, rc);
							if (changes != null) hasChanges = true;
							boolean enrolled = false;
							for (ClassAssignmentInterface.CourseAssignment course: iAssignment.getCourseAssignments()) {
								if (course.isAssigned() && !course.isTeachingAssignment() && !course.isFreeTime() && course.getCourseId().equals(rc.getCourseId())) {
									enrolled = true;
									break;
								}
							}
							if (first) {
								WebTable.Cell credit = new WebTable.Cell(request.hasAdvisorCredit() ? request.getAdvisorCredit() : "");
								credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
								WebTable.NoteCell note = new WebTable.NoteCell(request.hasAdvisorNote() ? request.getAdvisorNote() : "", null);
								note.setRowSpan(request.getRequestedCourse().size());
								row = new WebTable.Row(
									new WebTable.Cell(MESSAGES.courseRequestsAlternate(priority)),
									(enrolled ? new WebTable.IconCell(RESOURCES.courseEnrolled(), MESSAGES.titleCourseEnrolled(rc.getCourseName()), rc.getCourseName(), true) : new WebTable.Cell(rc.getCourseName())),
									new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
									credit,
									new WebTable.Cell(ToolBox.toString(prefs), true),
									new WebTable.Cell(""),
									new WebTable.Cell(""),
									note,
									new WebTable.Cell(changes)
									);
							} else {
								row = new WebTable.Row(
									new WebTable.Cell(""),
									(enrolled ? new WebTable.IconCell(RESOURCES.courseEnrolled(), MESSAGES.titleCourseEnrolled(rc.getCourseName()), rc.getCourseName(), true) : new WebTable.Cell(rc.getCourseName())),
									new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
									new WebTable.Cell(""),
									new WebTable.Cell(ToolBox.toString(prefs), true),
									new WebTable.Cell(""),
									new WebTable.Cell(""),
									new WebTable.Cell(changes)
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
						new WebTable.Cell(""),
						note,
						new WebTable.Cell("")
						);
					for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
					row.setId("A" + priority);
					rows.add(row);
				}
			}
			float min = 0, max = 0;
			for (Request request: iAssignment.getAdvisorRequest().getCourses()) {
				min += request.getAdvisorCreditMin();
				max += request.getAdvisorCreditMax();
			}
			WebTable.Cell credit = new WebTable.Cell(min < max ? MESSAGES.creditRange(min, max) : MESSAGES.credit(min));
			credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			String noteMessage = (iAssignment.getAdvisorRequest().hasCreditNote() ? iAssignment.getAdvisorRequest().getCreditNote() : "");
			if (iAssignment.getAdvisorRequest().hasReleasedPin() && !noteMessage.contains(iAssignment.getAdvisorRequest().getPin()))
				noteMessage += (noteMessage.isEmpty() ? "" : "\n") + MESSAGES.advisorNotePin(iAssignment.getAdvisorRequest().getPin());
			WebTable.NoteCell note = new WebTable.NoteCell(noteMessage, null);
			note.setColSpan(5);
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
		}
		
		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		iAdvReqs.setData(rowArray);
		iAdvReqs.setColumnVisible(4, hasPref);
		iAdvReqs.setColumnVisible(5, hasCrit);
		iAdvReqs.setColumnVisible(6, hasWL);
		iAdvReqs.setColumnVisible(8, hasChanges);
	}
	
	protected void fillInRequests() {
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		boolean hasPref = false, hasWarn = false, hasWait = false, hasCrit = iAssignment.isCanSetCriticalOverrides();
		NumberFormat df = NumberFormat.getFormat("0.#");
		if (iAssignment.hasRequest()) {
			setWaitListMode(iAssignment.getRequest().getWaitListMode(), false, true);
			CheckCoursesResponse check = new CheckCoursesResponse(iAssignment.getRequest().getConfirmations());
			hasWarn = iAssignment.getRequest().hasConfirmations();
			int priority = 1;
			for (Request request: iAssignment.getRequest().getCourses()) {
				if (!request.hasRequestedCourse()) continue;
				boolean first = true;
				if (iAssignment.getRequest().getWaitListMode() == WaitListMode.WaitList) {
					if (request.isWaitList()) hasWait = true;
				} else {
					if (request.isNoSub()) hasWait = true;
				}
				if (request.isCritical() || request.isImportant()) hasCrit = true;
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
						if (rc.hasRequestorNote()) iconText += "\n" + MESSAGES.requestNote(rc.getRequestorNote());
						if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
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
						String status = "";
						if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
							}
						}
						if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
						if (prefs != null) hasPref = true;
						WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
						credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						String note = null, noteTitle = null;
						if (check != null) { note = check.getMessageWithColor(rc.getCourseName(), "<br>", "CREDIT"); noteTitle = check.getMessage(rc.getCourseName(), "\n", "CREDIT"); }
						if (rc.hasRequestorNote()) { note = (note == null ? "" : note + "<br>") + rc.getRequestorNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getRequestorNote(); }
						if (rc.hasStatusNote()) { note = (note == null ? "" : note + "<br>") + rc.getStatusNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getStatusNote(); }
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit, 
								new WebTable.Cell(ToolBox.toString(prefs)),
								new WebTable.NoteCell(note, noteTitle),
								(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
								(first && iAssignment.isCanSetCriticalOverrides() ? new CriticalCell(request) : first && request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
									first && request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell("")),
								(iAssignment.getRequest().getWaitListMode() == WaitListMode.WaitList
									? (first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), (request.hasWaitListedTimeStamp() ? sDF.format(request.getWaitListedTimeStamp()) : "")) : new WebTable.Cell(""))
									: (first && request.isNoSub() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestNoSubs(), "") : new WebTable.Cell(""))),
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
						String note = null, noteTitle = null;
						if (check != null) {
							note = check.getMessageWithColor(CONSTANTS.freePrefix() + free, "<br>");
							noteTitle = check.getMessage(CONSTANTS.freePrefix() + free, "\n", "CREDIT");
						}
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
								new WebTable.Cell(""),
								new WebTable.NoteCell(note, noteTitle),
								new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
								new WebTable.Cell(""),
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
				if (request.isCritical() || request.isImportant()) hasCrit = true;
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
						if (rc.hasRequestorNote()) iconText += "\n" + MESSAGES.requestNote(rc.getRequestorNote());
						if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
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
						String status = "";
						if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
							}
						}
						if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
						WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
						credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						String note = null, noteTitle = null;
						if (check != null) { note = check.getMessageWithColor(rc.getCourseName(), "<br>", "CREDIT"); noteTitle = check.getMessage(rc.getCourseName(), "\n", "CREDIT"); }
						if (rc.hasRequestorNote()) { note = (note == null ? "" : note + "<br>") + rc.getRequestorNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getRequestorNote(); }
						if (rc.hasStatusNote()) { note = (note == null ? "" : note + "<br>") + rc.getStatusNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getStatusNote(); }
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsAlternate(priority) : ""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit,
								new WebTable.Cell(ToolBox.toString(prefs)),
								new WebTable.NoteCell(note, noteTitle),
								(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
								(first && iAssignment.isCanSetCriticalOverrides() ? new CriticalCell(request) : first && request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
									first && request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell("")),
								(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), (request.hasWaitListedTimeStamp() ? sDF.format(request.getWaitListedTimeStamp()) : "")) : new WebTable.Cell("")),
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
								new WebTable.Cell(first ? MESSAGES.courseRequestsAlternate(priority) : ""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
								new WebTable.Cell(""),
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
		
		if (iAssignment.getRequest().getMaxCreditOverrideStatus() != null) {
			ImageResource icon = null;
			String status = "";
			String note = null, noteTitle = null;
			String iconText = null;
			if (iAssignment.getRequest().hasCreditWarning()) {
				note = iAssignment.getRequest().getCreditWarning().replace("\n", "<br>");
				noteTitle = iAssignment.getRequest().getCreditWarning();
				iconText = iAssignment.getRequest().getCreditWarning();
				hasWarn = true;
			} else if (iAssignment.getRequest().getMaxCreditOverrideStatus() != RequestedCourseStatus.SAVED) {
				note = noteTitle = iconText = MESSAGES.creditWarning(iAssignment.getRequest().getCredit(iAssignment.getAdvisorWaitListedCourseIds()));
			}
			switch (iAssignment.getRequest().getMaxCreditOverrideStatus()) {
			case CREDIT_HIGH:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusWarning();
				note = "<span class='text-red'>" + note + "</span>";
				iconText += "\n" + MESSAGES.creditStatusTooHigh();
				break;
			case OVERRIDE_REJECTED:
				icon = RESOURCES.requestError();
				status = MESSAGES.reqStatusRejected();
				note = "<span class='text-red'>" + note + "</span>";
				iconText += "\n" + MESSAGES.creditStatusDenied();
				break;
			case CREDIT_LOW:
			case OVERRIDE_NEEDED:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusWarning();
				note = "<span class='text-orange'>" + note + "</span>";
				break;
			case OVERRIDE_CANCELLED:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusCancelled();
				iconText += "\n" + MESSAGES.creditStatusCancelled();
				note = "<span class='text-orange'>" + note + "</span>";
				break;
			case OVERRIDE_PENDING:
				icon = RESOURCES.requestPending();
				status = MESSAGES.reqStatusPending();
				iconText += "\n" + MESSAGES.creditStatusPending();
				note = "<span class='text-orange'>" + note + "</span>";
				break;
			case OVERRIDE_APPROVED:
				icon = RESOURCES.requestSaved();
				status = MESSAGES.reqStatusApproved();
				iconText += (iconText == null ? "" : iconText + "\n") + MESSAGES.creditStatusApproved();
				break;
			case SAVED:
				icon = null;
				status = "";
				break;
			}
			if (iAssignment.getRequest().hasRequestorNote()) {
				note = (note == null ? "" : note + "<br>") + iAssignment.getRequest().getRequestorNote().replace("\n", "<br>");
				noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + MESSAGES.requestNote(iAssignment.getRequest().getRequestorNote());
				iconText = (iconText == null ? "" : iconText + "\n") + iAssignment.getRequest().getRequestorNote();
				hasWarn = true;
			}
			if (iAssignment.getRequest().hasCreditNote()) {
				note = (note == null ? "" : note + "<br>") + iAssignment.getRequest().getCreditNote().replace("\n", "<br>");
				noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + MESSAGES.overrideNote(iAssignment.getRequest().getCreditNote());
				iconText = (iconText == null ? "" : iconText + "\n") + iAssignment.getRequest().getCreditNote();
				hasWarn = true;
			}
			float[] range = iAssignment.getRequest().getCreditRange(iAssignment.getAdvisorWaitListedCourseIds());
			WebTable.Cell credit = new WebTable.Cell(range != null ? range[0] < range[1] ? df.format(range[0]) + " - " + df.format(range[1]) : df.format(range[0]) : "");
			credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			WebTable.Row row = new WebTable.Row(
					new WebTable.Cell(MESSAGES.rowRequestedCredit(), 2, null),
					new WebTable.Cell(""),
					credit,
					new WebTable.Cell(""),
					new WebTable.NoteCell(note, noteTitle),
					(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
					new WebTable.Cell(""),
					new WebTable.Cell("")
					);
			for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-solid");
			row.getCell(0).setStyleName("top-border-solid text-bold");
			rows.add(row);
		}

		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		iRequests.setData(rowArray);
		iRequests.setColumnVisible(4, hasPref);
		iRequests.setColumnVisible(5, hasWarn);
		iRequests.setColumnVisible(7, hasCrit);
		iRequests.setColumnVisible(8, hasWait);
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
				if (course.isOverMaxCredit())
					unassignedMessage = MESSAGES.conflictOverMaxCredit(course.getOverMaxCredit())
						+ (MESSAGES.courseNotAssigned().equals(unassignedMessage) ? "" : " " + unassignedMessage);
				if (course.getWaitListedDate() != null)
					unassignedMessage += MESSAGES.conflictWaitListed(sDF.format(course.getWaitListedDate()));
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
	
	public void fillInNotes() {
		iTabs.getTabBar().setTabEnabled(5, iAssignment.hasNotes());
		if (iTabs.getSelectedTab() != 5)
			((Widget)iTabs.getTabBar().getTab(5)).setVisible(iAssignment.hasNotes());

		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		if (iAssignment.hasNotes()) {
			for (Note note: iAssignment.getNotes()) {
				WebTable.Row row = new WebTable.Row(
						new WebTable.Cell(sTSF.format(note.getTimeStamp())),
						new WebTable.Cell(note.getMessage()),
						new WebTable.Cell(note.getOwner()));
				rows.add(row);
			}
		}
			
		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		iNotes.setData(rowArray);
	}
	
	public void fillInSpecialRegistrations() {
		iTabs.getTabBar().setTabEnabled(4, iAssignment.hasSpecialRegistrations());
		if (iTabs.getSelectedTab() != 4)
			((Widget)iTabs.getTabBar().getTab(4)).setVisible(iAssignment.hasSpecialRegistrations());
		iSpecialRegistrations.clearTable(1);
		
		if (iAssignment.hasSpecialRegistrations())
			for (final RetrieveSpecialRegistrationResponse reg: iAssignment.getSpecialRegistrations()) {
				P p = new P("icons");
				if (reg.isFullyApplied(iAssignment)) {
					p.add(new Icon(RESOURCES.specRegApplied(), MESSAGES.hintSpecRegApplied()));
				} else if (reg.getStatus() != null) {
					switch (reg.getStatus()) {
					case Approved:
						if (reg.isGradeModeChange() || reg.isVariableTitleCourseChange() || reg.isExtended())
							p.add(new Icon(RESOURCES.specRegApproved(), MESSAGES.hintSpecRegApproved()));
						else
							p.add(new Icon(RESOURCES.specRegApproved(), MESSAGES.hintSpecRegApprovedNoteApply()));
						break;
					case Cancelled:
						p.add(new Icon(RESOURCES.specRegCancelled(), MESSAGES.hintSpecRegCancelled()));
						break;
					case Pending:
						if (reg.isHonorsGradeModeNotFullyMatching(iAssignment)) {
							p.add(new Icon(RESOURCES.specRegCancelled(), MESSAGES.hintSpecRegHonorsGradeModeNotMatchingSchedule()));
						} else {
							p.add(new Icon(RESOURCES.specRegPending(), MESSAGES.hintSpecRegPending()));
						}
						break;
					case Rejected:
						p.add(new Icon(RESOURCES.specRegRejected(), MESSAGES.hintSpecRegRejected()));
						break;
					case Draft:
						p.add(new Icon(RESOURCES.specRegDraft(), MESSAGES.hintSpecRegDraft()));
						break;
					}
				}
				if (reg.hasChanges()) {
					Long lastCourseId = null;
					String[] dateAndNote = (reg.getSubmitDate() == null ? reg.getNote() == null ? "" : reg.getNote() : sModifiedDateFormat.format(reg.getSubmitDate()) + (reg.getNote() == null || reg.getNote().isEmpty() ? "" : "\n" + reg.getNote())).split("\n");

					List<ClassAssignment> rows = new ArrayList<ClassAssignment>();
					for (ClassAssignment ca: reg.getChanges()) {
						if (ca.getParentSection() != null && ca.getParentSection().equals(ca.getSection())) continue;
						rows.add(ca);
					}
					
					for (int r = 0; r < rows.size(); r++) {
						ClassAssignment ca = rows.get(r);
						List<Widget> row = new ArrayList<Widget>();
						if (lastCourseId == null) {
							row.add(p);
						} else {
							row.add(new P("icons"));
						}
						if (r < dateAndNote.length) {
							Label label = null;
							if (r + 1 == rows.size()) {
								String text = dateAndNote[r];
								for (int i = r + 1; i < dateAndNote.length; i++)
									text += "\n" + dateAndNote[i];
								label = new Label(text); label.addStyleName("date-and-note");
							} else {
								label = new Label(dateAndNote[r]); label.addStyleName("date-and-note");
							}
							row.add(label);
						} else {
							row.add(new Label());
						}
						if (lastCourseId == null || !lastCourseId.equals(ca.getCourseId())) {
							row.add(new Label(ca.getSubject(), false));
							row.add(new Label(ca.getCourseNbr(), false));
						} else {
							row.add(new Label());
							row.add(new Label());
						}
						row.add(new Label(ca.getSubpart(), false));
						row.add(new Label(ca.getSection(), false));
						row.add(new HTML(ca.getLimitString(), false));
						if (ca.getCreditHour() != null) {
							row.add(new Label(MESSAGES.credit(ca.getCreditHour())));
						} else {
							row.add(new CreditCell(ca.getCredit()));
						}
						if (ca.getGradeMode() != null) {
							Label gm = new Label(ca.getGradeMode().getCode());
							if (ca.getGradeMode().getLabel() != null) gm.setTitle(ca.getGradeMode().getLabel());
							row.add(gm);
						} else {
							row.add(new Label());
						}
						HTML errorsLabel = new HTML(ca.hasError() ? ca.getError() : ""); errorsLabel.addStyleName("registration-errors");
						row.add(errorsLabel);
						P s = new P("icons");
						switch (ca.getSpecRegOperation()) {
						case Add:
							s.add(new Icon(RESOURCES.assignment(), MESSAGES.specRegAssignment(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection())));
							break;
						case Drop:
							s.add(new Icon(RESOURCES.unassignment(), MESSAGES.specRegRemoved(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection())));
							break;
						case Keep:
							if (ca.getGradeMode() != null && ca.getGradeMode().isHonor()) {
								boolean found = false;
								for (ClassAssignmentInterface.ClassAssignment x: iAssignment.getClassAssignments())
									if (x.isSaved() && ca.getClassId().equals(x.getClassId())) {
										found = true; break;
									}
								if (!found)
									s.add(new Icon(RESOURCES.unassignment(), MESSAGES.specRegRemoved(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection())));
							}
							// s.add(new Icon(RESOURCES.saved(), MESSAGES.saved(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection())));
							// break;
						default:
							s.add(new Label());
						}
						row.add(s);
						int idx = iSpecialRegistrations.addRow(reg, row);
						if (reg.getStatus() == SpecialRegistrationStatus.Approved)
							iSpecialRegistrations.setBackGroundColor(idx, "#D7FFD7");
						if (idx > 1 && lastCourseId == null)
							for (int c = 0; c < iSpecialRegistrations.getCellCount(idx); c++)
								iSpecialRegistrations.getCellFormatter().addStyleName(idx, c, "top-border-solid");
						if (lastCourseId != null && !lastCourseId.equals(ca.getCourseId()))
							for (int c = 2; c < iSpecialRegistrations.getCellCount(idx) - 1; c++)
								iSpecialRegistrations.getCellFormatter().addStyleName(idx, c, "top-border-dashed");
						if (!ca.isCourseAssigned()) {
							for (int c = 2; c < iSpecialRegistrations.getCellCount(idx) - 1; c++)
								iSpecialRegistrations.getCellFormatter().addStyleName(idx, c, ca.hasError() ? "change-drop-with-errors" : "change-drop");
						} else  {
							for (int c = 2; c < iSpecialRegistrations.getCellCount(idx) - 1; c++)
								iSpecialRegistrations.getCellFormatter().addStyleName(idx, c, "change-add");
						}
						lastCourseId = ca.getCourseId();
					}
				} else if (reg.hasErrors()) {
					List<Widget> row = new ArrayList<Widget>();
					row.add(p);
					row.add(new DateAndNoteCell(reg.getSubmitDate(), reg.getNote()));
					row.add(new DescriptionCell(reg.getDescription()));
					String errors = "";
					for (ErrorMessage e: reg.getErrors())
						errors += (errors.isEmpty() ? "" : "\n") + e.getMessage();
					HTML errorsLabel = new HTML(errors); errorsLabel.addStyleName("registration-errors");
					row.add(errorsLabel);
					row.add(new Label());
					int idx = iSpecialRegistrations.addRow(reg, row);
					if (idx > 1)
						for (int c = 0; c < iSpecialRegistrations.getCellCount(idx); c++)
							iSpecialRegistrations.getCellFormatter().addStyleName(idx, c, "top-border-solid");
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
	
	public float[] getCreditRange() { return iAssignment == null || !iAssignment.hasRequest() ? null : iAssignment.getRequest().getCreditRange(iAssignment.getAdvisorWaitListedCourseIds()); }
	
	public String getCreditMessage() {
		if (iTabs.getSelectedTab() == 0) {
			return "";
		} else if (iTabs.getSelectedTab() == 1) {
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
	
	public String getErrorMessage() { return iAssignment.getRequest().getErrorMessaeg(); }
	public boolean hasErrorMessage() { return iAssignment != null && iAssignment.hasRequest() && iAssignment.getRequest().hasErrorMessage(); }
	
	public void checkAccessKeys(NativePreviewEvent event) {
		if (event.getTypeInt() == Event.ONKEYUP && (event.getNativeEvent().getAltKey() || event.getNativeEvent().getCtrlKey())) {
			for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
				if (event.getNativeEvent().getKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeEvent().getKeyCode()  == Character.toUpperCase(entry.getKey())) {
					if (entry.getValue() >= 4 && !((Widget)iTabs.getTabBar().getTab(entry.getValue())).isVisible()) return;
					iTabs.selectTab(entry.getValue());
				}
		}
	}
	
	public void setSelectionHandler(SelectionHandler<Integer> handler) {
		iHandler = handler;
	}
	
	protected void setCritical(Long studentId, Request request, Integer critical, AsyncCallback<Integer> callback) {
		callback.onSuccess(request.getCritical());
	}
	
	class CriticalCell extends WebTable.IconCell {
		private Request iRequest;
		
		CriticalCell(Request request) {
			super(request.isCritical() ? RESOURCES.requestsCritical() : request.isImportant() ? RESOURCES.requestsImportant() : RESOURCES.requestsNotCritical(), "", null);
			getIcon().setTitle(request.isCritical() ? MESSAGES.descriptionRequestCritical() : request.isImportant() ? MESSAGES.descriptionRequestImportant() : MESSAGES.descriptionRequestNotCritical());
			getIcon().setAltText(request.isCritical() ? MESSAGES.descriptionRequestCritical() : request.isImportant() ? MESSAGES.descriptionRequestImportant() : MESSAGES.descriptionRequestNotCritical());
			iRequest = request;
			getIcon().getElement().getStyle().setCursor(Cursor.POINTER);
			getIcon().addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel(true);
					popup.addStyleName("unitime-Menu");
					MenuBar menu = new MenuBarWithAccessKeys();
					
					MenuItem item1 = new MenuItem(MESSAGES.opSetCritical(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							change(1);
						}
					});
					menu.addItem(item1);
					
					MenuItem item2 = new MenuItem(MESSAGES.opSetImportant(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							change(2);
						}
					});
					menu.addItem(item2);
					
					MenuItem item3 = new MenuItem(MESSAGES.opSetNotCritical(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							change(0);
						}
					});
					menu.addItem(item3);
					
					MenuItem item4 = new MenuItem(MESSAGES.opSetCriticalNotSet(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							change(null);
						}
					});
					menu.addItem(item4);
					menu.setVisible(true);
					
					popup.add(menu);
					popup.showRelativeTo((Widget)event.getSource());
					((MenuBar)popup.getWidget()).focus();
				}
			});
		}
		
		void change(Integer value) {
			setCritical(iAssignment.getRequest().getStudentId(), iRequest, value, new AsyncCallback<Integer>() {
				@Override
				public void onFailure(Throwable caught) {
					UniTimeNotifications.error(caught);
				}

				@Override
				public void onSuccess(Integer result) {
					if (result == null) return;
					if (result == 1) {
						getIcon().setResource(RESOURCES.requestsCritical());
						getIcon().setTitle(MESSAGES.descriptionRequestCritical());
						getIcon().setAltText(MESSAGES.descriptionRequestCritical());
					} else if (result == 2) {
						getIcon().setResource(RESOURCES.requestsImportant());
						getIcon().setTitle(MESSAGES.descriptionRequestImportant());
						getIcon().setAltText(MESSAGES.descriptionRequestImportant());
					} else {
						getIcon().setResource(RESOURCES.requestsNotCritical());
						getIcon().setTitle(MESSAGES.descriptionRequestNotCritical());
						getIcon().setAltText(MESSAGES.descriptionRequestNotCritical());
					}
				}
			});
		}
	}
	
	protected class Icon extends Image {
		public Icon(ImageResource image, final String text) {
			super(image);
			if (text != null && !text.isEmpty()) {
				setAltText(text);
				setTitle(text);
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.preventDefault(); event.stopPropagation();
						UniTimeConfirmationDialog.info(text);
					}
				});
			}
		}
	}
	
	protected class CreditCell extends HTML {
		public CreditCell(String text) {
			if (text != null && text.indexOf('|') >= 0) {
				setHTML(text.substring(0, text.indexOf('|')));
				setTitle(text.substring(text.indexOf('|') + 1).replace("\n", "<br>"));
			} else {
				setHTML(text == null ? "" : text.replace("\n", "<br>"));
				if (text != null) setTitle(text);
			}
		}
	}
	
	protected class DateAndNoteCell extends Label {
		public DateAndNoteCell(Date date, String note) {
			super(date == null ? note == null ? "" : note : sModifiedDateFormat.format(date) + (note == null || note.isEmpty() ? "" : "\n" + note));
			addStyleName("date-and-note");
		}
	}
	
	protected class DescriptionCell extends Label implements UniTimeTable.HasColSpan {
		public DescriptionCell(String text) {
			super(text == null ? "" : text);
		}
		@Override
		public int getColSpan() { return 7; }		
	}
}
