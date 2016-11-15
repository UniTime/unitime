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
import java.util.Iterator;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Student;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class StudentScheduleTable extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	private boolean iOnline;
	
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader;
	private WebTable iTable;
	private ClassAssignmentInterface.Student iStudent;
	
	public StudentScheduleTable(final boolean showHeader, boolean online) {
		iOnline = online;
		iPanel = new SimpleForm();
			
		iHeader = new UniTimeHeaderPanel(showHeader ? MESSAGES.enrollmentsTable() : "&nbsp;");
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				SectioningCookie.getInstance().setEnrollmentCoursesDetails(event.getValue());
				if (iTable.getTable().getRowCount() > 2) {
					for (int row = 1; row < iTable.getTable().getRowCount(); row++) {
						iTable.getTable().getRowFormatter().setVisible(row, event.getValue());
					}
				}
				if (iTable.getTable().getRowCount() == 0)
					refresh();
			}
		});
		iHeader.setCollapsible(showHeader ? SectioningCookie.getInstance().getEnrollmentCoursesDetails() : null);
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		if (showHeader) {
			iPanel.addHeaderRow(iHeader);
			iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
		}
		
		iTable = new WebTable();
		iTable.addStyleName("unitime-Enrollments");
		iPanel.addRow(iTable);
		
		if (!showHeader)
			iPanel.addBottomRow(iHeader);
		
		iHeader.setEnabled("approve", false);
		iHeader.setEnabled("reject", false);
		
		iHeader.addButton("registration", MESSAGES.buttonRegistration(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				EnrollmentTable.showCourseRequests(iStudent, iOnline, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(caught);
					}
					@Override
					public void onSuccess(Boolean result) {}
				});
			}
		});
		iHeader.addButton("assistant", MESSAGES.buttonAssistant(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				EnrollmentTable.showStudentAssistant(iStudent, iOnline, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(caught);
					}
					@Override
					public void onSuccess(Boolean result) {}
				});
			}
		});
		if (iOnline) {
			iHeader.addButton("log", MESSAGES.buttonChangeLog(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					LoadingWidget.getInstance().show(MESSAGES.loadingChangeLog(iStudent.getName()));
					EnrollmentTable.showChangeLog(iStudent, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught);
						}
						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
						}
					});
				}
			});		
		}
				
		initWidget(iPanel);
	}
	
	public void insert(final RootPanel panel) {
		String studentId = panel.getElement().getInnerText().trim();
		panel.getElement().setInnerText(null);
		panel.add(this);
		sSectioningService.lookupStudent(iOnline, studentId, new AsyncCallback<ClassAssignmentInterface.Student>() {
			@Override
			public void onSuccess(Student result) {
				if (result != null) {
					panel.setVisible(true);
					setStudent(result);
					if (SectioningCookie.getInstance().getEnrollmentCoursesDetails()) {
						refresh();
					} else {
						clear();
						iHeader.clearMessage();
						iHeader.setCollapsible(false);
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {}
		});
	}
	
	public void setStudent(ClassAssignmentInterface.Student student) {
		iStudent = student;
		iHeader.setEnabled("registration", iStudent != null && iStudent.isCanRegister());
		iHeader.setEnabled("assistant", iStudent != null && iStudent.isCanUseAssistant());
		iHeader.setEnabled("log", iStudent != null && iStudent.isCanUseAssistant());
	}
	
	public void clear() {
		for (int row = iTable.getTable().getRowCount() - 1; row >= 0; row--) {
			iTable.getTable().removeRow(row);
		}
		iTable.getTable().clear(true);
	}
	
	public void refresh() {
		sSectioningService.getEnrollment(iOnline, iStudent.getId(), new AsyncCallback<ClassAssignmentInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
			}
			@Override
			public void onSuccess(ClassAssignmentInterface result) {
				populate(result.getCourseAssignments());
			}
		});
	}

	public void populate(Collection<CourseAssignment> data) {
		clear();
		iTable.setHeader(new WebTable.Row(
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
				new WebTable.Cell(MESSAGES.colCredit(), 1, "75px")
			));
		iTable.setEmptyMessage(MESSAGES.emptySchedule());
		iHeader.clearMessage();
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		for (ClassAssignmentInterface.CourseAssignment course: data) {
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
							(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
							new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
							new WebTable.Cell(clazz.getParentSection()),
							clazz.hasNote() ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "") : new WebTable.Cell(""),
							new WebTable.AbbvTextCell(clazz.getCredit()));
					rows.add(row);
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					firstClazz = false;
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
							new WebTable.AbbvTextCell(clazz.getCredit()));
					break;
				}
				if (row == null) {
					row = new WebTable.Row(
							new WebTable.Cell(course.getSubject()),
							new WebTable.Cell(course.getCourseNbr()),
							new WebTable.Cell(unassignedMessage, 12, null));
				}
				for (WebTable.Cell cell: row.getCells())
					cell.setStyleName(style);
				row.getCell(row.getNrCells() - 1).setStyleName("text-gray" + (!rows.isEmpty() ? " top-border-dashed": ""));
				rows.add(row);
			}
		}
		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		iTable.setData(rowArray);
	}

}
