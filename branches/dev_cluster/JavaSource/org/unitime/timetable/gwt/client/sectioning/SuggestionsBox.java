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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.widgets.HorizontalPanelWithHint;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.WebTable.RowClickEvent;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class SuggestionsBox extends UniTimeDialogBox {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);

	private ClassAssignmentInterface.ClassAssignment iAssignment;
	private AsyncCallback<Collection<ClassAssignmentInterface>> iCallback = null;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iCurrent = null;
	private ArrayList<ClassAssignmentInterface> iResult = null;
	private ArrayList<SuggestionSelectedHandler> iSuggestionSelectedHandlers = new ArrayList<SuggestionSelectedHandler>();
	
	private WebTable iSuggestions;
	private HTML iMessages;
	private HTML iLegend;
	private ScrollPanel iSuggestionsScroll;
	private String iSource;
	private AriaTextBox iFilter;
	private int iIndex;
	private CourseRequestInterface iRequest;
	private HorizontalPanelWithHint iFilterPanel;
	private Button iSearch;
	private boolean iOnline;
	
	private TimeGrid iGrid;
	private PopupPanel iHint;
	private String iHintId = null;
	private Timer iHideHint;
	
	public SuggestionsBox(TimeGrid.ColorProvider color, boolean online) {
		super(true, false);
		
		iOnline = online;
		
		setText("Alternatives");
		setAnimationEnabled(true);
		setAutoHideEnabled(true);
		setGlassEnabled(true);
		setModal(false);
		
		VerticalPanel suggestionPanel = new VerticalPanel();
		suggestionPanel.setSpacing(5);
		
		iFilterPanel = new HorizontalPanelWithHint(new HTML(MESSAGES.suggestionsFilterHint(), false));
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label("Filter:");
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new AriaTextBox();
		iFilter.setStyleName("gwt-SuggestBox");
		iFilter.getElement().getStyle().setWidth(600, Unit.PX);
		iFilter.getElement().getStyle().setHeight(26, Unit.PX);
		iFilterPanel.add(iFilter);
		
		HTML ariaDescription = new HTML(MESSAGES.suggestionsFilterHint(), false);
		ariaDescription.setStyleName("unitime-AriaHiddenLabel");
		ariaDescription.getElement().setId(DOM.createUniqueId());
		iFilterPanel.add(ariaDescription);
		Roles.getTextboxRole().setAriaDescribedbyProperty(iFilter.getElement(), Id.of(ariaDescription.getElement()));
		
		iSearch = new Button(MESSAGES.buttonSearch());
		iSearch.setAccessKey('s');
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);
		iFilterPanel.setCellVerticalAlignment(iSearch, HasVerticalAlignment.ALIGN_MIDDLE);

		suggestionPanel.add(iFilterPanel);
		suggestionPanel.setCellHorizontalAlignment(iFilter, HasHorizontalAlignment.ALIGN_CENTER);

		iSuggestions = new WebTable();
		iSuggestions.setHeader(new WebTable.Row(
				new WebTable.Cell("", 1, "10px"),
				new WebTable.Cell(MESSAGES.colSubject(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colSubpart(), 1, "40px"),
				new WebTable.Cell(MESSAGES.colClass(), 1, "40px"),
				new WebTable.Cell(MESSAGES.colTime(), 1, "75px").aria(ARIA.colTimeCurrent()),
				new WebTable.Cell("", 1, "1px").aria(ARIA.colTimeNew()),
				new WebTable.Cell(MESSAGES.colDate(), 1, "50px").aria(ARIA.colDateCurrent()),
				new WebTable.Cell("", 1, "1px").aria(ARIA.colDateNew()),
				new WebTable.Cell(MESSAGES.colRoom(), 1, "50px").aria(ARIA.colRoomCurrent()),
				new WebTable.Cell("", 1, "1px").aria(ARIA.colRoomNew()),
				new WebTable.Cell(MESSAGES.colInstructor(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colParent(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colIcons(), 1, "10px")
			));
		iSuggestions.setSelectSameIdRows(true);
		iSuggestions.setEmptyMessage(MESSAGES.suggestionsLoading());
		iSuggestionsScroll = new ScrollPanel(iSuggestions);
		iSuggestionsScroll.getElement().getStyle().setHeight(400, Unit.PX);
		iSuggestionsScroll.setStyleName("unitime-ScrollPanel");
		suggestionPanel.add(iSuggestionsScroll);

		iLegend = new HTML();
		iLegend.setStyleName("unitime-SuggestionsLegend");
		suggestionPanel.add(iLegend);

		iMessages = new HTML();
		iMessages.setStyleName("unitime-SuggestionsMessage");
		suggestionPanel.add(iMessages);
		
		iCallback = new AsyncCallback<Collection<ClassAssignmentInterface>>() {
			public void onFailure(Throwable caught) {
				iSuggestions.clearData(true);
				iSuggestions.setEmptyMessage("<font color='red'>" + caught.getMessage() + "</font>");
				iMessages.setHTML("");
				LoadingWidget.getInstance().hide();
				center();
				AriaStatus.getInstance().setHTML(caught.getMessage());
			}

			public void onSuccess(Collection<ClassAssignmentInterface> result) {
				iResult = (ArrayList<ClassAssignmentInterface>)result;
				iMessages.setHTML("");
				String ariaStatus = null;
 
				if (result.isEmpty()) {
					iSuggestions.clearData(true);
					if (iFilter.getText().isEmpty()) {
						iSuggestions.setEmptyMessage(MESSAGES.suggestionsNoAlternative(iSource));
						ariaStatus = ARIA.suggestionsNoAlternative(iSource);
						
					} else {
						iSuggestions.setEmptyMessage(MESSAGES.suggestionsNoAlternativeWithFilter(iSource, iFilter.getText()));
						ariaStatus = ARIA.suggestionsNoAlternativeWithFilter(iSource, iFilter.getText());
					}
					LoadingWidget.getInstance().hide();
					center();
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
									if (x == null) continue;
									if (course.getCourseId().equals(x.getCourseId())) sameCourse.add(x);
								}
							} else {
								ClassAssignmentInterface.ClassAssignment clazz = course.getClassAssignments().get(0);
								for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
									if (x == null) continue;
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
										if (x == null) continue;
										if (course.isFreeTime()) {
											if (x.isFreeTime() && x.isCourseAssigned() && x.getDaysString(CONSTANTS.shortDays()).equals(clazz.getDaysString(CONSTANTS.shortDays())) &&
												x.getStart() == clazz.getStart() && x.getLength() == clazz.getLength()) continue clazz;
										} else {
											if (clazz.getCourseId().equals(x.getCourseId()) && clazz.getClassId().equals(x.getClassId())) continue clazz; // the exact same assignment
											if (clazz.getCourseId().equals(x.getCourseId()) && clazz.getSubpartId().equals(x.getSubpartId())) { old = x; break; }
										}
									}
									if (old == null && clazzIdx < sameCourse.size()) old = sameCourse.get(clazzIdx);
									if (old == null && sameCourse.size() == 1 && !sameCourse.get(0).isAssigned()) old = sameCourse.get(0);
									
									WebTable.IconsCell icons = new WebTable.IconsCell();
									if (clazz != null && clazz.isSaved())
										icons.add(RESOURCES.saved(), MESSAGES.saved(MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection())));
									if (course.isLocked())
										icons.add(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
									if (clazz != null && clazz.isOfHighDemand())
										icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
									if (clazz != null && clazz.hasNote())
										icons.add(RESOURCES.note(), clazz.getNote());
									
									final WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
											new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()).aria(clazzIdx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()).aria(clazzIdx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
											new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), CmpMode.SINGLE, selected, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), CmpMode.SINGLE, selected, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), CmpMode.BOTH_OLD, selected, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), CmpMode.BOTH_NEW, selected, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), CmpMode.BOTH_OLD, selected, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), CmpMode.BOTH_NEW, selected, clazz == null)),
											(clazz != null && clazz.hasDistanceConflict() ? 
													new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()),
															compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_OLD, selected, clazz == null)) : 
													new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_OLD, selected, clazz == null))),
											new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_NEW, selected, clazz == null)),
											new WebTable.InstructorCell(clazz == null ? null : clazz.getInstructors(), clazz == null ? null : clazz.getInstructorEmails(), ", "),
											new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), CmpMode.SINGLE, selected, clazz == null)),
											icons);
									String style = (selected?"text-blue":"") + (lastSize > 0 && rows.size() == lastSize ? " top-border-solid" : clazzIdx == 0 && !rows.isEmpty() ? " top-border-dashed": "");
									row.setId(String.valueOf(suggestionId));
									for (WebTable.Cell cell: row.getCells())
										cell.setStyleName(style.trim());
									row.getCell(0).setStyleName((lastSize > 0 && rows.size() == lastSize ? "top-border-solid" : ""));
									rows.add(row);
									row.setAriaLabel(ARIA.assigned(
											(course.isFreeTime() ? MESSAGES.course(MESSAGES.freeTimeSubject(), MESSAGES.freeTimeCourse()) : MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection())) + " " +
											clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(", ")));
									clazzIdx++;
								}
							} else {
								if (sameCourse.isEmpty() || !sameCourse.get(0).isCourseAssigned()) continue;
								for (int idx = 0; idx < sameCourse.size(); idx++) {
									ClassAssignmentInterface.ClassAssignment old = sameCourse.get(idx);
									ClassAssignmentInterface.ClassAssignment clazz = null;
									
									WebTable.IconsCell icons = new WebTable.IconsCell();
									if (old != null && old.isSaved())
										icons.add(RESOURCES.saved(), MESSAGES.saved(MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())));
									if (course.isLocked())
										icons.add(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
									if (old != null && old.isOfHighDemand())
										icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(old.getExpected(), old.getAvailableLimit()));
									if (old != null && old.hasNote())
										icons.add(RESOURCES.note(), old.getNote());
									
									WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
											new WebTable.Cell(idx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()).aria(idx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(idx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()).aria(idx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
											new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), CmpMode.SINGLE, false, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), CmpMode.SINGLE, false, clazz == null)),
											//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
											new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), CmpMode.BOTH_OLD, false, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), CmpMode.BOTH_NEW, false, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), CmpMode.BOTH_OLD, false, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), CmpMode.BOTH_NEW, false, clazz == null)),
											(old != null && old.hasDistanceConflict() ? 
													new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(old.getBackToBackRooms(), old.getBackToBackDistance()),
															compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_OLD, false, clazz == null)) : 
													new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_OLD, false, clazz == null))),
											new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_NEW, false, clazz == null)),
											//new WebTable.Cell(compare(old == null ? null : old.getInstructors(", "), clazz == null ? null : clazz.getInstructors(", "), true)),
											new WebTable.InstructorCell(old == null ? null : old.getInstructors(), old == null ? null : old.getInstructorEmails(), ", "),
											new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), CmpMode.SINGLE, false, clazz == null)),
											icons);
									row.setId(String.valueOf(suggestionId));
									String style = "text-red" + (lastSize > 0 && rows.size() == lastSize ? " top-border-solid" : idx == 0 && !rows.isEmpty() ? " top-border-dashed": "");
									for (WebTable.Cell cell: row.getCells())
										cell.setStyleName(style);
									row.getCell(0).setStyleName((lastSize > 0 && rows.size() == lastSize ? "top-border-solid" : ""));
									row.setAriaLabel(ARIA.unassigned(
											(course.isFreeTime() ? MESSAGES.course(MESSAGES.freeTimeSubject(), MESSAGES.freeTimeCourse()) : MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())) + " " +
											old.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + old.getRooms(", ")));
									rows.add(row);
								}
							}
						}
						Long lastCourseId = null;
						current: for (ClassAssignmentInterface.ClassAssignment old: iCurrent) {
							if (old == null || old.isFreeTime()) continue;
							for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
								if (old.getCourseId().equals(course.getCourseId())) continue current;
							}
							ClassAssignmentInterface.ClassAssignment clazz = null;
							
							WebTable.IconsCell icons = new WebTable.IconsCell();
							if (old != null && old.isSaved())
								icons.add(RESOURCES.saved(), MESSAGES.saved(MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())));
							if (old != null && old.isOfHighDemand())
								icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(old.getExpected(), old.getAvailableLimit()));
							if (old != null && old.hasNote())
								icons.add(RESOURCES.note(), old.getNote());
							
							WebTable.Row row = new WebTable.Row(
									new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
									new WebTable.Cell(old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeSubject() : old.getSubject()).aria(!old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeSubject() : old.getSubject()),
									new WebTable.Cell(old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeCourse() : old.getCourseNbr()).aria(!old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeCourse() : old.getCourseNbr()),
									new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), CmpMode.SINGLE, false, clazz == null)),
									new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), CmpMode.SINGLE, false, clazz == null)),
									//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
									new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), CmpMode.BOTH_OLD, false, clazz == null)),
									new WebTable.Cell(compare(old == null ? null : old.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), clazz == null ? null : clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours()), CmpMode.BOTH_NEW, false, clazz == null)),
									new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), CmpMode.BOTH_OLD, false, clazz == null)),
									new WebTable.Cell(compare(old == null ? null : old.getDatePattern(), clazz == null ? null : clazz.getDatePattern(), CmpMode.BOTH_NEW, false, clazz == null)),
									(old != null && old.hasDistanceConflict() ? 
											new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(old.getBackToBackRooms(), old.getBackToBackDistance()),
													compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_OLD, false, clazz == null)) : 
											new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_OLD, false, clazz == null))),
									new WebTable.Cell(compare(old == null ? null : old.getRooms(", "), clazz == null ? null : clazz.getRooms(", "), CmpMode.BOTH_NEW, false, clazz == null)),
									//new WebTable.Cell(compare(old == null ? null : old.getInstructors(", "), clazz == null ? null : clazz.getInstructors(", "), true)),
									new WebTable.InstructorCell(old == null ? null : old.getInstructors(), old == null ? null : old.getInstructorEmails(), ", "),
									new WebTable.Cell(compare(old == null ? null : old.getParentSection(), clazz == null ? null : clazz.getParentSection(), CmpMode.SINGLE, false, clazz == null)),
									icons);
							row.setId(String.valueOf(suggestionId));
							String style = "text-red" + (lastSize > 0 && rows.size() == lastSize ? " top-border-solid" : !old.getCourseId().equals(lastCourseId) && !rows.isEmpty() ? " top-border-dashed": "");
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(style);
							row.getCell(0).setStyleName((lastSize > 0 && rows.size() == lastSize ? " top-border-solid" : ""));
							row.setAriaLabel(ARIA.unassigned(
									(old.isFreeTime() ? MESSAGES.course(MESSAGES.freeTimeSubject(), MESSAGES.freeTimeCourse()) : MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())) + " " +
									old.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + old.getRooms(", ")));
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
					if (rows.isEmpty()) {
						if (iFilter.getText().isEmpty()) {
							iSuggestions.setEmptyMessage(MESSAGES.suggestionsNoAlternative(iSource));
							ariaStatus = ARIA.suggestionsNoAlternative(iSource);
						} else {
							iSuggestions.setEmptyMessage(MESSAGES.suggestionsNoAlternativeWithFilter(iSource, iFilter.getText()));
							ariaStatus = ARIA.suggestionsNoAlternativeWithFilter(iSource, iFilter.getText());
						}
					} else {
						ariaStatus = ARIA.showingAlternatives(Integer.valueOf(rows.get(rows.size() - 1).getId()), iSource);
					}
					LoadingWidget.getInstance().hide();
					center();
					if (ariaStatus != null)
						AriaStatus.getInstance().setHTML(ariaStatus);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							iFilter.setFocus(true);
						}
					});
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
		
		iFilter.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					if (iSuggestions.getSelectedRow() >= 0) {
						ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
						SuggestionSelectedEvent e = new SuggestionSelectedEvent(suggestion);
						for (SuggestionSelectedHandler h: iSuggestionSelectedHandlers)
							h.onSuggestionSelected(e);
						hide();
					} else {
						LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
						iSectioningService.computeSuggestions(iOnline, iRequest, iCurrent, iIndex, iFilter.getText(), iCallback);
					}
				}
			}
		});
		
		iSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
				iSectioningService.computeSuggestions(iOnline, iRequest, iCurrent, iIndex, iFilter.getText(), iCallback);
			}
		});
		
		iGrid = new TimeGrid(color);
		iHint = new PopupPanel();
		iHint.setStyleName("unitime-SuggestionsHint");
		iHideHint = new Timer() {
			@Override
			public void run() {
				if (iHint.isShowing()) iHint.hide();
				
			}
		};

		iSuggestions.addRowOverHandler(new WebTable.RowOverHandler() {
			@Override
			public void onRowOver(final WebTable.RowOverEvent event) {
				iHideHint.cancel();
				
				if (iHint.isShowing() && event.getRow().getId().equals(iHintId)) return;

				if (!event.getRow().getId().equals(iHintId)) {
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(event.getRow().getId()));
					int index = 0;
					iGrid.clear(false);
					for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
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
					TimeGrid w = (TimeGrid)iGrid.getPrintWidget();
					w.addStyleName("unitime-SuggestionsHintWidget");
					iHint.setWidget(new SimplePanel(w));
					iHint.setSize((w.getWidth() / 2) + "px", (w.getHeight() / 2) + "px");
					iHintId = event.getRow().getId();
				}
				
				iHint.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						Element tr = iSuggestions.getTable().getRowFormatter().getElement(event.getRowIdx());
						boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 30 + offsetHeight > Window.getClientHeight());
						iHint.setPopupPosition(
								Math.max(Math.min(event.getEvent().getClientX() + 15, tr.getAbsoluteRight() - offsetWidth - 15), tr.getAbsoluteLeft() + 15),
								top ? tr.getAbsoluteTop() - offsetHeight - 30 : tr.getAbsoluteBottom() + 30);
					}
				});
			}
		});

		iSuggestions.addRowOutHandler(new WebTable.RowOutHandler() {
			@Override
			public void onRowOut(WebTable.RowOutEvent event) {
				if (iHint.isShowing()) iHideHint.schedule(500);
			}
		});
		
		iSuggestions.addRowMoveHandler(new WebTable.RowMoveHandler() {
			@Override
			public void onRowMove(WebTable.RowMoveEvent event) {
				if (iHint.isShowing()) {
					if (event.getRowIdx() < 0) { iHint.hide(); return; }
					Element tr = iSuggestions.getTable().getRowFormatter().getElement(event.getRowIdx());
					boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 30 + iHint.getOffsetHeight() > Window.getClientHeight());
					iHint.setPopupPosition(
							Math.max(Math.min(event.getEvent().getClientX() + 15, tr.getAbsoluteRight() - iHint.getOffsetWidth() - 15), tr.getAbsoluteLeft() + 15),
							top ? tr.getAbsoluteTop() - iHint.getOffsetHeight() - 30 : tr.getAbsoluteBottom() + 30);
			
				}
			}
		});
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (iHint.isShowing()) iHint.hide();
				iHideHint.cancel();
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
				iFilterPanel.hideHint();
			}
		});
		
		setWidget(suggestionPanel);
	}

	@Override
	public void center() {
		super.center();
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
	}
	
	private static enum CmpMode {
		SINGLE,
		BOTH_OLD,
		BOTH_NEW,
		ARIA
	};
	
	private String compare(String oldVal, String newVal, CmpMode mode, boolean selected, boolean conflict) {
		switch (mode) {
		case ARIA:
			return (newVal != null && !newVal.isEmpty() ? newVal : oldVal != null ? oldVal : "");
		case SINGLE:
			return (newVal != null && !newVal.isEmpty() ? newVal : oldVal != null ? "<font color='"+ (conflict ? "red" : selected ? "#9999FF" : "#999999") +"'>" + oldVal + "</font>" : null);
		case BOTH_OLD:
			return (oldVal == null || oldVal.isEmpty() ? newVal : newVal == null || newVal.isEmpty() ? "<font color='" + (conflict ? "red" : selected ? "#9999FF" : "#999999") + "'>" + oldVal + "</font>" : oldVal.equals(newVal) ? oldVal : "<font color='" + ( selected ? "#9999FF" : "#999999" ) + "'>" + oldVal + "</font>");
		case BOTH_NEW:
			return (oldVal != null && !oldVal.isEmpty() && newVal != null && !newVal.isEmpty() && !newVal.equals(oldVal) ? "<font color='#" + ( selected ? "#9999FF" : "#999999" ) + "'>&rarr;</font> " + newVal : null);
		default:
			return newVal;
		}
	}
	
	public void open(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> rows, int index) {
		LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
		ClassAssignmentInterface.ClassAssignment row = rows.get(index);
		iAssignment = row;
		iCurrent = rows;
		iSource = null;
		iRequest = request;
		iIndex = index;
		iHintId = null;
		if (row.isFreeTime()) {
			iSource = MESSAGES.freeTime(row.getDaysString(CONSTANTS.shortDays()), row.getStartString(CONSTANTS.useAmPm()), row.getEndString(CONSTANTS.useAmPm()));
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
		iLegend.setHTML(
				row.isFreeTime() ? MESSAGES.suggestionsLegendOnFreeTime(row.getDaysString(CONSTANTS.shortDays()) + " " + row.getStartString(CONSTANTS.useAmPm()) + " - " + row.getEndString(CONSTANTS.useAmPm())) :
				row.isAssigned() ? MESSAGES.suggestionsLegendOnClass(MESSAGES.clazz(row.getSubject(), row.getCourseNbr(), row.getSubpart(), row.getSection()))
				: MESSAGES.suggestionsLegendOnCourse(MESSAGES.course(row.getSubject(), row.getCourseNbr())));
		iMessages.setHTML("");
		iFilter.setText("");
		iSectioningService.computeSuggestions(iOnline, request, rows, index, iFilter.getText(), iCallback);
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
					
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					AriaStatus.getInstance().setText(ARIA.showingAlternative(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()), Integer.parseInt(iSuggestions.getRows()[iSuggestions.getRows().length - 1].getId()), toString(suggestion)));
				}
			}
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_UP) {
				if (iSuggestions.getRowsCount() > 0) {
					int row = iSuggestions.getSelectedRow() <= 0 ? iSuggestions.getRowsCount() - 1 : iSuggestions.getSelectedRow() - 1;
					String id = iSuggestions.getRows()[row % iSuggestions.getRowsCount()].getId();
					while (id.equals(iSuggestions.getRows()[(iSuggestions.getRowsCount() + row - 1) % iSuggestions.getRowsCount()].getId())) row--;
					iSuggestions.setSelectedRow((iSuggestions.getRowsCount() + row) % iSuggestions.getRowsCount());
					
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					AriaStatus.getInstance().setText(ARIA.showingAlternative(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()), Integer.parseInt(iSuggestions.getRows()[iSuggestions.getRows().length - 1].getId()), toString(suggestion)));
				}
			}
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ENTER) {
				if (iSuggestions.getSelectedRow() >= 0) {
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					
					AriaStatus.getInstance().setText(ARIA.selectedAlternative(toString(suggestion)));

					SuggestionSelectedEvent e = new SuggestionSelectedEvent(suggestion);
					for (SuggestionSelectedHandler h: iSuggestionSelectedHandlers)
						h.onSuggestionSelected(e);
					hide();
				}
			}
			break;
	    }
	}
	
	private String toString(ClassAssignmentInterface suggestion) {
		String ret = "";
		for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
			ArrayList<ClassAssignmentInterface.ClassAssignment> sameCourse = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			if (!course.isFreeTime()) {
				for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
					if (x == null) continue;
					if (course.getCourseId().equals(x.getCourseId())) sameCourse.add(x);
				}
			} else {
				ClassAssignmentInterface.ClassAssignment clazz = course.getClassAssignments().get(0);
				for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
					if (x == null) continue;
					if (x.isFreeTime() && x.getDaysString(CONSTANTS.shortDays()).equals(clazz.getDaysString(CONSTANTS.shortDays())) && x.getStart() == clazz.getStart() && x.getLength() == clazz.getLength()) sameCourse.add(x);
				}
			}
			if (course.isAssigned()) {
				clazz: for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
						if (x == null) continue;
						if (course.isFreeTime()) {
							if (x.isFreeTime() && x.isCourseAssigned() && x.getDaysString(CONSTANTS.shortDays()).equals(clazz.getDaysString(CONSTANTS.shortDays())) &&
								x.getStart() == clazz.getStart() && x.getLength() == clazz.getLength()) continue clazz;
						} else {
							if (clazz.getCourseId().equals(x.getCourseId()) && clazz.getClassId().equals(x.getClassId())) continue clazz; // the exact same assignment
							if (clazz.getCourseId().equals(x.getCourseId()) && clazz.getSubpartId().equals(x.getSubpartId())) { break; }
						}
					}
					
					ret += ARIA.assigned(
							(course.isFreeTime() ? MESSAGES.course(MESSAGES.freeTimeSubject(), MESSAGES.freeTimeCourse()) : MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection())) + " " +
							clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(", "));
				}
			} else {
				if (sameCourse.isEmpty() || !sameCourse.get(0).isCourseAssigned()) continue;
				for (int idx = 0; idx < sameCourse.size(); idx++) {
					ClassAssignmentInterface.ClassAssignment old = sameCourse.get(idx);

					ret += ARIA.unassigned(
							(course.isFreeTime() ? MESSAGES.course(MESSAGES.freeTimeSubject(), MESSAGES.freeTimeCourse()) : MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())) + " " +
							old.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + old.getRooms(", "));
				}
			}
		}
		current: for (ClassAssignmentInterface.ClassAssignment old: iCurrent) {
			if (old == null || old.isFreeTime()) continue;
			for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
				if (old.getCourseId().equals(course.getCourseId())) continue current;
			}
			
			ret += ARIA.unassigned(
					(old.isFreeTime() ? MESSAGES.course(MESSAGES.freeTimeSubject(), MESSAGES.freeTimeCourse()) : MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())) + " " +
					old.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + old.getRooms(", "));
		}
		
		return ret;
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
