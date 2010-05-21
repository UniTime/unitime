/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.widgets.WebTable.RowClickEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SuggestionsBox extends DialogBox {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);

	private ClassAssignmentInterface.ClassAssignment iAssignment;
	private AsyncCallback<Collection<ClassAssignmentInterface>> iCallback = null;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iCurrent = null;
	private ArrayList<ClassAssignmentInterface> iResult = null;
	private ArrayList<SuggestionSelectedHandler> iSuggestionSelectedHandlers = new ArrayList<SuggestionSelectedHandler>();
	
	private WebTable iSuggestions;
	private HTML iMessages;
	private ScrollPanel iSuggestionsScroll;
	private String iSource;
	
	public SuggestionsBox() {
		super();
		
		setText("Alternatives");
		setAnimationEnabled(true);
		setAutoHideEnabled(true);
		setGlassEnabled(true);
		setModal(true);
		
		VerticalPanel suggestionPanel = new VerticalPanel();
		suggestionPanel.setSpacing(5);

		iSuggestions = new WebTable();
		iSuggestions.setHeader(new WebTable.Row(
				new WebTable.Cell("", 1, "10"),
				new WebTable.Cell(MESSAGES.colSubject(), 1, "75"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75"),
				new WebTable.Cell(MESSAGES.colSubpart(), 1, "50"),
				new WebTable.Cell(MESSAGES.colClass(), 1, "85"),
				new WebTable.Cell(MESSAGES.colTime(), 1, "175"),
				new WebTable.Cell(MESSAGES.colDate(), 1, "90"),
				new WebTable.Cell(MESSAGES.colRoom(), 1, "100"),
				new WebTable.Cell(MESSAGES.colInstructor(), 1, "100"),
				new WebTable.Cell(MESSAGES.colParent(), 1, "85"),
				new WebTable.Cell(MESSAGES.colSaved(), 1, "10"),
				new WebTable.Cell(MESSAGES.colHighDemand(), 1, "10")
			));
		iSuggestions.setSelectSameIdRows(true);
		iSuggestions.setEmptyMessage(MESSAGES.suggestionsLoading());
		iSuggestionsScroll = new ScrollPanel(iSuggestions);
		iSuggestionsScroll.setSize("950", "400");
		iSuggestionsScroll.setStyleName("unitime-ScrollPanel");
		suggestionPanel.add(iSuggestionsScroll);

		iMessages = new HTML();
		suggestionPanel.add(iMessages);

		iCallback = new AsyncCallback<Collection<ClassAssignmentInterface>>() {
			public void onFailure(Throwable caught) {
				iSuggestions.clearData(true);
				iSuggestions.setEmptyMessage("<font color='red'>" + caught.getMessage() + "</font>");
				iMessages.setHTML(null);
			}

			public void onSuccess(Collection<ClassAssignmentInterface> result) {
				iResult = (ArrayList<ClassAssignmentInterface>)result;
 
				if (result.isEmpty()) {
					iSuggestions.clearData(true);
					iSuggestions.setEmptyMessage(MESSAGES.suggestionsNoAlternative(iSource));
				} else {
					ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
					int lastSize = 0;
					int suggestionId = 0;
					for (ClassAssignmentInterface suggestion: result) {
						if (suggestion.hasMessages()) iMessages.setHTML(suggestion.getMessages("<br>"));
						if (suggestion.getCourseAssignments().isEmpty()) {
							suggestionId++; continue;
						}
						for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
							ArrayList<ClassAssignmentInterface.ClassAssignment> sameCourse = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
							if (!course.isFreeTime()) {
								for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
									if (course.getCourseId().equals(x.getCourseId())) sameCourse.add(x);
								}
							} else {
								ClassAssignmentInterface.ClassAssignment clazz = course.getClassAssignments().get(0);
								for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
									if (x.isFreeTime() && x.getDaysString(CONSTANTS.shortDays()).equals(clazz.getDaysString(CONSTANTS.shortDays())) && x.getStart() == clazz.getStart() && x.getLength() == clazz.getLength()) sameCourse.add(x);
								}
							}
							boolean selected = false;
							if (iAssignment.isFreeTime() && course.isFreeTime() &&
								course.getClassAssignments().get(0).getDaysString(CONSTANTS.shortDays()).equals(iAssignment.getDaysString(CONSTANTS.shortDays())) &&
								course.getClassAssignments().get(0).getStart() == iAssignment.getStart() &&
								course.getClassAssignments().get(0).getLength() == iAssignment.getLength()) selected = true;
							if (!iAssignment.isFreeTime() && !iAssignment.isAssigned() && iAssignment.getCourseId().equals(course.getCourseId())) selected = true;
							if (course.isAssigned()) {
								int clazzIdx = 0;
								Long selectClassId = null;
								String selectSubpart = null;
								if (iAssignment.getSubpartId() != null && iAssignment.getCourseId().equals(course.getCourseId())) {
									for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
										if (iAssignment.getSubpartId().equals(clazz.getSubpartId()))
											selectClassId = clazz.getClassId();
									}
									if (selectClassId == null)
										for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
											if (iAssignment.getSubpart().equals(clazz.getSubpart()))
												selectSubpart = clazz.getSubpart();
										}
									if (selectClassId == null && selectSubpart == null) selected = true;
								}
								clazz: for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
									if (selectClassId != null) selected = selectClassId.equals(clazz.getClassId());
									if (selectSubpart != null) selected = selectSubpart.equals(clazz.getSubpart());
									ClassAssignmentInterface.ClassAssignment old = null;
									for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
										if (course.isFreeTime()) {
											if (x.isFreeTime() && x.isCourseAssigned() && x.getDaysString(CONSTANTS.shortDays()).equals(clazz.getDaysString(CONSTANTS.shortDays())) &&
												x.getStart() == clazz.getStart() && x.getLength() == clazz.getLength()) continue clazz;
										} else {
											if (clazz.getClassId().equals(x.getClassId())) continue clazz; // the exact same assignment
											if (clazz.getSubpartId().equals(x.getSubpartId())) { old = x; break; }
										}
									}
									if (old == null && clazzIdx < sameCourse.size()) old = sameCourse.get(clazzIdx);
									if (old == null && sameCourse.size() == 1 && !sameCourse.get(0).isAssigned()) old = sameCourse.get(0);
									final WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
											new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
											new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), false)),
											new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), false)),
											new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays()), true)),
											new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), true)),
											(clazz != null && clazz.hasDistanceConflict() ? 
													new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()),
															compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true)) : 
													new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true))),
											new WebTable.InstructorCell(clazz == null ? null : clazz.getInstructors(), clazz == null ? null : clazz.getInstructorEmails(), ", "),
											new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), false)),
											(clazz != null && clazz.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : new WebTable.Cell("")),
											(clazz != null && clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")));
									String style = "unitime-ClassRow" + (selected?"Blue":"") + (lastSize > 0 && rows.size() == lastSize ? "First2" : clazzIdx == 0 && !rows.isEmpty() ? "First": "");
									row.setId(String.valueOf(suggestionId));
									for (WebTable.Cell cell: row.getCells())
										cell.setStyleName(style);
									row.getCell(0).setStyleName("unitime-ClassRow" + (lastSize > 0 && rows.size() == lastSize ? "First2" : ""));
									rows.add(row);
									clazzIdx++;
								}
								/*
								if (sameCourse.size() > course.getClassAssignments().size()) {
									for (int idx = course.getClassAssignments().size(); idx < sameCourse.size(); idx++) {
										ClassAssignmentInterface.ClassAssignment old = sameCourse.get(idx);
										ClassAssignmentInterface.ClassAssignment clazz = null;
										final WebTable.Row row = new WebTable.Row(
												new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
												new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? "Free" : course.getSubject()),
												new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? "Time" : course.getCourseNbr()),
												new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), false)),
												new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), false)),
												//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
												new WebTable.Cell(compare(old == null ? null : old.getTimeString(), clazz == null ? null : clazz.getTimeString(), true)),
												new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), true)),
												(old != null && old.hasDistanceConflict() ? 
														new WebTable.IconCell(RESOURCES.distantConflict(), old.getBackToBackDistanceMessage(),
																compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true)) : 
														new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true))),
												new WebTable.Cell(compare(old == null ? null : old.getInstructors(", "), clazz == null ? null : clazz.getInstructors(", "), true)),
												new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), false)),
												(old != null && old.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : new WebTable.Cell("")));
										row.setId(String.valueOf(suggestionId));
										String style = "unitime-ClassRow" + (lastSize > 0 && rows.size() == lastSize ? "First2" : clazzIdx == 0 && !rows.isEmpty() ? "First": "");
										for (WebTable.Cell cell: row.getCells())
											cell.setStyleName(style);
										row.getCell(0).setStyleName("unitime-ClassRow" + (lastSize > 0 && rows.size() == lastSize ? "First2" : ""));
										rows.add(row);
										clazzIdx++;
									}
								}
								*/
							} else {
								if (sameCourse.isEmpty() || !sameCourse.get(0).isCourseAssigned()) continue;
								for (int idx = 0; idx < sameCourse.size(); idx++) {
									ClassAssignmentInterface.ClassAssignment old = sameCourse.get(idx);
									ClassAssignmentInterface.ClassAssignment clazz = null;
									WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
											new WebTable.Cell(idx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(idx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
											new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), false)),
											new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), false)),
											//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
											new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays()), true)),
											new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), true)),
											(old != null && old.hasDistanceConflict() ? 
													new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(old.getBackToBackRooms(), old.getBackToBackDistance()),
															compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true)) : 
													new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true))),
											//new WebTable.Cell(compare(old == null ? null : old.getInstructors(", "), clazz == null ? null : clazz.getInstructors(", "), true)),
											new WebTable.InstructorCell(old == null ? null : old.getInstructors(), old == null ? null : old.getInstructorEmails(), ", "),
											new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), false)),
											(old != null && old.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : new WebTable.Cell("")),
											(old != null && old.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(old.getExpected(), old.getAvailableLimit()), null) : new WebTable.Cell("")));
									row.setId(String.valueOf(suggestionId));
									String style = "unitime-ClassRowRed" + (lastSize > 0 && rows.size() == lastSize ? "First2" : idx == 0 && !rows.isEmpty() ? "First": "");
									for (WebTable.Cell cell: row.getCells())
										cell.setStyleName(style);
									row.getCell(0).setStyleName("unitime-ClassRow" + (lastSize > 0 && rows.size() == lastSize ? "First2" : ""));
									rows.add(row);
								}
							}
						}
						Long lastCourseId = null;
						current: for (ClassAssignmentInterface.ClassAssignment old: iCurrent) {
							if (old.isFreeTime()) continue;
							for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
								if (old.getCourseId().equals(course.getCourseId())) continue current;
							}
							ClassAssignmentInterface.ClassAssignment clazz = null;
							WebTable.Row row = new WebTable.Row(
									new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
									new WebTable.Cell(old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeSubject() : old.getSubject()),
									new WebTable.Cell(old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeCourse() : old.getCourseNbr()),
									new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), false)),
									new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), false)),
									//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
									new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays()), true)),
									new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), true)),
									(old != null && old.hasDistanceConflict() ? 
											new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(old.getBackToBackRooms(), old.getBackToBackDistance()),
													compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true)) : 
											new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), true))),
									//new WebTable.Cell(compare(old == null ? null : old.getInstructors(", "), clazz == null ? null : clazz.getInstructors(", "), true)),
									new WebTable.InstructorCell(old == null ? null : old.getInstructors(), old == null ? null : old.getInstructorEmails(), ", "),
									new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), false)),
									(old != null && old.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : new WebTable.Cell("")),
									(old != null && old.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(old.getExpected(), old.getAvailableLimit()), null) : new WebTable.Cell("")));
							row.setId(String.valueOf(suggestionId));
							String style = "unitime-ClassRowRed" + (lastSize > 0 && rows.size() == lastSize ? "First2" : !old.getCourseId().equals(lastCourseId) && !rows.isEmpty() ? "First": "");
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(style);
							row.getCell(0).setStyleName("unitime-ClassRow" + (lastSize > 0 && rows.size() == lastSize ? "First2" : ""));
							rows.add(row);
							lastCourseId = old.getCourseId();
						}
						lastSize = rows.size();
						suggestionId++;
					}
					WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
					int idx = 0;
					for (WebTable.Row row: rows) rowArray[idx++] = row;
					iSuggestions.setData(rowArray);
					if (rows.isEmpty())
						iSuggestions.setEmptyMessage(MESSAGES.suggestionsNoAlternative(iSource));
				}
			}
		};
		
		iSuggestions.addRowClickHandler(new WebTable.RowClickHandler() {
			public void onRowClick(RowClickEvent event) {
				ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(event.getRow().getId()));
				SuggestionSelectedEvent e = new SuggestionSelectedEvent(suggestion);
				for (SuggestionSelectedHandler h: iSuggestionSelectedHandlers)
					h.onSuggestionSelected(e);
				hide();
			}
		});

		setWidget(suggestionPanel);
	}
	
	private String compare(String oldVal, String newVal, boolean both) {
		if (oldVal == null || oldVal.isEmpty())
			return (newVal == null || newVal.isEmpty() ? null : newVal);
		if (newVal == null || newVal.isEmpty())
			return "<font color='red'>" + oldVal + "</font>";
		if (both)
			return oldVal.equals(newVal) ? newVal : (both ? oldVal + " &rarr; " : "") + newVal;
		else
			return oldVal.equals(newVal) ? newVal : newVal;
	}
	
	public void setRow(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> rows, int index) {
		ClassAssignmentInterface.ClassAssignment row = rows.get(index);
		iAssignment = row;
		iCurrent = rows;
		iSource = null;
		if (row.isFreeTime()) {
			iSource = MESSAGES.freeTime(row.getDaysString(CONSTANTS.shortDays()), row.getStartString(), row.getEndString());
		} else {
			if (row.getSubpart() == null)
				iSource = MESSAGES.course(row.getSubject(), row.getCourseNbr());
			else
				iSource = MESSAGES.clazz(row.getSubject(), row.getCourseNbr(), row.getSubpart(), row.getSection());
		}
		setText(MESSAGES.suggestionsAlternatives(iSource));
		iSuggestions.setSelectedRow(-1);
		iSuggestions.clearData(true);
		iSuggestions.setEmptyMessage(MESSAGES.suggestionsLoading());
		iMessages.setHTML(null);
		iSectioningService.computeSuggestions(request, rows, index, iCallback);
	}

	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
	    switch (DOM.eventGetType((Event) event.getNativeEvent())) {
	    case Event.ONKEYUP:
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE) {
				hide();
			}
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_DOWN) {
				if (iSuggestions.getRowsCount() > 0) {
					String id = (iSuggestions.getSelectedRow() < 0 ? null : iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId());
					int row = iSuggestions.getSelectedRow() + 1;
					while (id != null && id.equals(iSuggestions.getRows()[row % iSuggestions.getRowsCount()].getId())) row++;
					iSuggestions.setSelectedRow(row % iSuggestions.getRowsCount());
				}
			}
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_UP) {
				if (iSuggestions.getRowsCount() > 0) {
					int row = iSuggestions.getSelectedRow() <= 0 ? iSuggestions.getRowsCount() - 1 : iSuggestions.getSelectedRow() - 1;
					String id = iSuggestions.getRows()[row % iSuggestions.getRowsCount()].getId();
					while (id.equals(iSuggestions.getRows()[(iSuggestions.getRowsCount() + row - 1) % iSuggestions.getRowsCount()].getId())) row--;
					iSuggestions.setSelectedRow((iSuggestions.getRowsCount() + row) % iSuggestions.getRowsCount());
				}
			}
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ENTER) {
				if (iSuggestions.getSelectedRow() >= 0) {
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					SuggestionSelectedEvent e = new SuggestionSelectedEvent(suggestion);
					for (SuggestionSelectedHandler h: iSuggestionSelectedHandlers)
						h.onSuggestionSelected(e);
					hide();
				}
			}
			break;
	    }
	}
	
	public interface SuggestionSelectedHandler {
		public void onSuggestionSelected(SuggestionSelectedEvent event);
	}
	
	public class SuggestionSelectedEvent {
		private ClassAssignmentInterface iSuggestion;
		private SuggestionSelectedEvent(ClassAssignmentInterface suggestion) {
			iSuggestion = suggestion;
		}
		public ClassAssignmentInterface getSuggestion() { return iSuggestion; }
	}
	
	public void addSuggestionSelectedHandler(SuggestionSelectedHandler h) {
		iSuggestionSelectedHandlers.add(h);
	}
}
