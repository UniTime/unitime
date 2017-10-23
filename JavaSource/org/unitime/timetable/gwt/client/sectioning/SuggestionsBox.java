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

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
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
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

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
	private AsyncCallback<ClassAssignmentInterface> iCustomCallback = null;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iCurrent = null;
	private ArrayList<ClassAssignmentInterface> iResult = null;
	private ArrayList<SuggestionSelectedHandler> iSuggestionSelectedHandlers = new ArrayList<SuggestionSelectedHandler>();
	private ArrayList<QuickDropHandler> iQuickDropHandlers = new ArrayList<QuickDropHandler>();
	
	private WebTable iSuggestions;
	private HTML iMessages;
	private HTML iLegend;
	private ScrollPanel iSuggestionsScroll;
	private String iSource;
	private AriaTextBox iFilter;
	private int iIndex;
	private CourseRequestInterface iRequest;
	private AriaButton iSearch;
	private boolean iOnline;
	private AriaButton iQuickDrop;
	private F iFilterPanel;
	private CheckBox iAllChoices = null;
	
	private TimeGrid iGrid;
	private PopupPanel iHint;
	private String iHintId = null;
	private Timer iHideHint;
	private boolean iUseGwtConfirmations = false;
	
	public SuggestionsBox(TimeGrid.ColorProvider color, boolean online, boolean allowAllChoices) {
		super(true, false);
		addStyleName("unitime-SuggestionsBox");
		
		iOnline = online;
		
		setText("Alternatives");
		setAnimationEnabled(true);
		setAutoHideEnabled(true);
		setGlassEnabled(true);
		setModal(false);
		
		P panel = new P("panel");
		
		iFilterPanel = new F(new HTML(MESSAGES.suggestionsFilterHint(), false), "filter");
		P label = new P("label");
		label.setText(MESSAGES.filter());
		iFilterPanel.add(label);
		
		iFilter = new AriaTextBox();
		iFilter.setStyleName("gwt-SuggestBox");
		
		HTML ariaDescription = new HTML(MESSAGES.suggestionsFilterHint(), false);
		ariaDescription.setStyleName("unitime-AriaHiddenLabel");
		ariaDescription.getElement().setId(DOM.createUniqueId());
		iFilterPanel.add(ariaDescription);
		Roles.getTextboxRole().setAriaDescribedbyProperty(iFilterPanel.getElement(), Id.of(ariaDescription.getElement()));
		
		P buttons = new P("buttons");
		iSearch = new AriaButton(MESSAGES.buttonSearch());
		buttons.add(iSearch);
		
		iQuickDrop = new AriaButton();
		buttons.add(iQuickDrop);
		iQuickDrop.setVisible(false);
		iQuickDrop.setEnabled(false);
		iQuickDrop.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				if (iAssignment == null || iAssignment.isFreeTime()) {
					fireQuickDropEvent();
				} else {
					UniTimeConfirmationDialog.confirm(iUseGwtConfirmations, MESSAGES.confirmQuickDrop(MESSAGES.course(iAssignment.getSubject(), iAssignment.getCourseNbr())), new Command() {
						@Override
						public void execute() {
							fireQuickDropEvent();
						}
					});
				}
			}
			protected void fireQuickDropEvent() {
				QuickDropEvent e = new QuickDropEvent(iAssignment);
				for (QuickDropHandler h: iQuickDropHandlers)
					h.onQuickDrop(e);
			}
		});
		iFilterPanel.add(buttons);
		
		P text = new P(DOM.createSpan(), "text"); text.add(iFilter);
		iFilterPanel.add(text);

		panel.add(iFilterPanel);

		iSuggestions = new WebTable();
		iSuggestions.setHeader(new WebTable.Row(
				new WebTable.Cell(""),
				new WebTable.Cell(MESSAGES.colSubject()),
				new WebTable.Cell(MESSAGES.colCourse()),
				new WebTable.Cell(MESSAGES.colSubpart()),
				new WebTable.Cell(MESSAGES.colClass()),
				new WebTable.Cell(MESSAGES.colTime()).aria(ARIA.colTimeCurrent()),
				new WebTable.Cell("").aria(ARIA.colTimeNew()),
				new WebTable.Cell(MESSAGES.colDate()).aria(ARIA.colDateCurrent()),
				new WebTable.Cell("").aria(ARIA.colDateNew()),
				new WebTable.Cell(MESSAGES.colRoom()).aria(ARIA.colRoomCurrent()),
				new WebTable.Cell("").aria(ARIA.colRoomNew()),
				new WebTable.Cell(MESSAGES.colInstructor()),
				new WebTable.Cell(MESSAGES.colParent()),
				new WebTable.Cell(MESSAGES.colIcons())
			));
		iSuggestions.setSelectSameIdRows(true);
		iSuggestions.setEmptyMessage(MESSAGES.suggestionsLoading());
		iSuggestionsScroll = new ScrollPanel(iSuggestions);
		iSuggestionsScroll.setStyleName("unitime-ScrollPanel");
		panel.add(iSuggestionsScroll);
		
		if (allowAllChoices) {
			iAllChoices = new CheckBox(MESSAGES.suggestionsShowAllChoices());
			iAllChoices.setValue(SectioningCookie.getInstance().isAllChoices());
			iAllChoices.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					SectioningCookie.getInstance().setAllChoices(event.getValue());
					LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
					iRequest.setShowAllChoices(iAllChoices.getValue());
					iSectioningService.computeSuggestions(iOnline, iRequest, iCurrent, iIndex, iFilter.getText(), iCallback);
				}
			});
			P ac = new P("all-choices"); ac.add(iAllChoices); panel.add(ac);
		}

		iLegend = new HTML();
		iLegend.setStyleName("legend");
		panel.add(iLegend);

		iMessages = new HTML();
		iMessages.setStyleName("message");
		panel.add(iMessages);
		
		iCallback = new AsyncCallback<Collection<ClassAssignmentInterface>>() {
			public void onFailure(Throwable caught) {
				iSuggestions.clearData(true);
				iSuggestions.setEmptyMessage("<font color='red'>" + caught.getMessage() + "</font>");
				iMessages.setHTML("");
				LoadingWidget.getInstance().hide();
				center();
				AriaStatus.getInstance().setHTML(caught.getMessage());
			}
			
			protected String room(ClassAssignmentInterface.ClassAssignment clazz) {
				if (clazz == null) return null;
				if (clazz.hasRoom()) return clazz.getRooms(", ");
				if (clazz.getClassId() != null) return MESSAGES.noRoom();
				return "";
			}
			
			protected String dates(ClassAssignmentInterface.ClassAssignment clazz) {
				if (clazz == null) return null;
				if (clazz.hasDatePattern()) return clazz.getDatePattern();
				if (clazz.getClassId() != null) return MESSAGES.noDate();
				return "";
			}
			
			protected String time(ClassAssignmentInterface.ClassAssignment clazz) {
				if (clazz == null) return null;
				return clazz.getTimeString(CONSTANTS.shortDays(), CONSTANTS.useAmPm(), MESSAGES.arrangeHours());
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
							if (course.isTeachingAssignment()) continue;
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
							if (iAssignment != null && iAssignment.isFreeTime() && course.isFreeTime() &&
								course.getClassAssignments().get(0).getDaysString(CONSTANTS.shortDays()).equals(iAssignment.getDaysString(CONSTANTS.shortDays())) &&
								course.getClassAssignments().get(0).getStart() == iAssignment.getStart() &&
								course.getClassAssignments().get(0).getLength() == iAssignment.getLength()) selected = true;
							if (iAssignment != null && !iAssignment.isFreeTime() && !iAssignment.isAssigned() && iAssignment.getCourseId().equals(course.getCourseId())) selected = true;
							if (course.isAssigned()) {
								int clazzIdx = 0;
								Long selectClassId = null;
								String selectSubpart = null;
								if (iAssignment != null && iAssignment.getSubpartId() != null && iAssignment.getCourseId().equals(course.getCourseId())) {
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
								if (iAssignment == null && !course.isFreeTime()) {
									boolean found = false;
									for (ClassAssignmentInterface.ClassAssignment x: iCurrent) {
										if (x != null && course.getCourseId().equals(x.getCourseId())) { found = true; break; }
									}
									if (!found) selected = true;
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
									if (clazz != null && clazz.hasOverlapNote())
										icons.add(RESOURCES.overlap(), clazz.getOverlapNote());
									if (clazz.isCancelled())
										icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection())));
									
									final WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
											new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()).aria(clazzIdx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(clazzIdx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())).aria(clazzIdx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())),
											new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), CmpMode.SINGLE, selected, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), CmpMode.SINGLE, selected, clazz == null)),
											new WebTable.Cell(compare(time(old), time(clazz), CmpMode.BOTH_OLD, selected, clazz == null)),
											new WebTable.Cell(compare(time(old), time(clazz), CmpMode.BOTH_NEW, selected, clazz == null)),
											new WebTable.Cell(compare(dates(old), dates(clazz), CmpMode.BOTH_OLD, selected, clazz == null)),
											new WebTable.Cell(compare(dates(old), dates(clazz), CmpMode.BOTH_NEW, selected, clazz == null)),
											(clazz != null && clazz.hasDistanceConflict() ? 
													new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()),
															compare(room(old), room(clazz), CmpMode.BOTH_OLD, selected, clazz == null)) : 
													new WebTable.Cell(compare(room(old), room(clazz), CmpMode.BOTH_OLD, selected, clazz == null))),
											new WebTable.Cell(compare(room(old), room(clazz), CmpMode.BOTH_NEW, selected, clazz == null)),
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
									if (old != null && old.isCancelled())
										icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())));
									
									WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
											new WebTable.Cell(idx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()).aria(idx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(idx > 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())).aria(idx == 0 ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())),
											new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), CmpMode.SINGLE, false, clazz == null)),
											new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), CmpMode.SINGLE, false, clazz == null)),
											//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
											new WebTable.Cell(compare(time(old), time(clazz), CmpMode.BOTH_OLD, false, clazz == null)),
											new WebTable.Cell(compare(time(old), time(clazz), CmpMode.BOTH_NEW, false, clazz == null)),
											new WebTable.Cell(compare(dates(old), dates(clazz), CmpMode.BOTH_OLD, false, clazz == null)),
											new WebTable.Cell(compare(dates(old), dates(clazz), CmpMode.BOTH_NEW, false, clazz == null)),
											(old != null && old.hasDistanceConflict() ? 
													new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(old.getBackToBackRooms(), old.getBackToBackDistance()),
															compare(room(old), room(clazz), CmpMode.BOTH_OLD, false, clazz == null)) : 
													new WebTable.Cell(compare(room(old), room(clazz), CmpMode.BOTH_OLD, false, clazz == null))),
											new WebTable.Cell(compare(room(old), room(clazz), CmpMode.BOTH_NEW, false, clazz == null)),
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
							if (old == null || old.isFreeTime() || old.isTeachingAssignment()) continue;
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
							if (old != null && old.isCancelled())
								icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(MESSAGES.clazz(old.getSubject(), old.getCourseNbr(), old.getSubpart(), old.getSection())));
							
							WebTable.Row row = new WebTable.Row(
									new WebTable.Cell(rows.size() == lastSize ? suggestionId + "." : ""),
									new WebTable.Cell(old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeSubject() : old.getSubject()).aria(!old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeSubject() : old.getSubject()),
									new WebTable.Cell(old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeCourse() : old.getCourseNbr(CONSTANTS.showCourseTitle())).aria(!old.getCourseId().equals(lastCourseId) ? "" : old.isFreeTime() ? MESSAGES.freeTimeCourse() : old.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(compare(old == null ? null : old.getSubpart(), clazz == null ? null : clazz.getSubpart(), CmpMode.SINGLE, false, clazz == null)),
									new WebTable.Cell(compare(old == null ? null : old.getSection(), clazz == null ? null : clazz.getSection(), CmpMode.SINGLE, false, clazz == null)),
									//new WebTable.Cell(compare(old == null ? null : old.getLimitString(), clazz == null ? null : clazz.getLimitString(), false)),
									new WebTable.Cell(compare(time(old), time(clazz), CmpMode.BOTH_OLD, false, clazz == null)),
									new WebTable.Cell(compare(time(old), time(clazz), CmpMode.BOTH_NEW, false, clazz == null)),
									new WebTable.Cell(compare(dates(old), dates(clazz), CmpMode.BOTH_OLD, false, clazz == null)),
									new WebTable.Cell(compare(dates(old), dates(clazz), CmpMode.BOTH_NEW, false, clazz == null)),
									(old != null && old.hasDistanceConflict() ? 
											new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(old.getBackToBackRooms(), old.getBackToBackDistance()),
													compare(room(old), room(clazz), CmpMode.BOTH_OLD, false, clazz == null)) : 
											new WebTable.Cell(compare(room(old), room(clazz), CmpMode.BOTH_OLD, false, clazz == null))),
									new WebTable.Cell(compare(room(old), room(clazz), CmpMode.BOTH_NEW, false, clazz == null)),
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
						if (!iMessages.getHTML().isEmpty()) {
							iSuggestions.setEmptyMessage(iMessages.getHTML());
							ariaStatus += "<br>" + iMessages.getHTML();
							iMessages.setHTML("");
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
				if (iCustomCallback != null) iCustomCallback.onSuccess(suggestion);
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
						if (iCustomCallback != null) iCustomCallback.onSuccess(suggestion);
					} else {
						LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
						iRequest.setShowAllChoices(iAllChoices != null && iAllChoices.getValue());
						iSectioningService.computeSuggestions(iOnline, iRequest, iCurrent, iIndex, iFilter.getText(), iCallback);
					}
				}
			}
		});
		
		iSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
				iRequest.setShowAllChoices(iAllChoices != null && iAllChoices.getValue());
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
					TimeGrid w = (TimeGrid)iGrid.getPrintWidget(Math.min(900, Window.getClientWidth()));
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
		
		setWidget(panel);
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
	
	public void open(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> rows, int index, boolean quickDrop, boolean useGwtConfirmations) {
		ClassAssignmentInterface.ClassAssignment row = rows.get(index);
		if (row == null || row.isTeachingAssignment()) return;
		LoadingWidget.getInstance().show(MESSAGES.suggestionsLoading());
		iAssignment = row;
		iCurrent = rows;
		iSource = null;
		iRequest = request;
		iIndex = index;
		iHintId = null;
		iUseGwtConfirmations = useGwtConfirmations;
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
		if (quickDrop && !iQuickDropHandlers.isEmpty()) {
			if (iAssignment.isFreeTime())
				iQuickDrop.setHTML(MESSAGES.buttonQuickDrop(MESSAGES.freeTime(row.getDaysString(CONSTANTS.shortDays()), row.getStartString(CONSTANTS.useAmPm()), row.getEndString(CONSTANTS.useAmPm()))));
			else
				iQuickDrop.setHTML(MESSAGES.buttonQuickDrop(MESSAGES.course(row.getSubject(), row.getCourseNbr())));
			iQuickDrop.setVisible(true); iQuickDrop.setEnabled(true);
		} else {
			iQuickDrop.setVisible(false); iQuickDrop.setEnabled(false);
		}
		iCustomCallback = null;
		request.setShowAllChoices(iAllChoices != null && iAllChoices.getValue());
		iSectioningService.computeSuggestions(iOnline, request, rows, index, iFilter.getText(), iCallback);
	}
	
	public void open(final CourseRequestInterface request, final ArrayList<ClassAssignmentInterface.ClassAssignment> rows, RequestedCourse course, boolean useGwtConfirmations, AsyncCallback<ClassAssignmentInterface> callback) {
		LoadingWidget.getInstance().show(MESSAGES.suggestionsLoadingChoices());
		iAssignment = null;
		iCurrent = rows;
		iRequest = request;
		iIndex = -1;
		iHintId = null;
		iUseGwtConfirmations = useGwtConfirmations;
		iSource = course.getCourseName();
		setText(MESSAGES.suggestionsChoices(iSource));
		iSuggestions.setSelectedRow(-1);
		iSuggestions.clearData(true);
		iSuggestions.setEmptyMessage(MESSAGES.suggestionsLoadingChoices());
		iLegend.setHTML(MESSAGES.suggestionsLegendOnNewCourse(course.getCourseName()));
		iMessages.setHTML("");
		iFilter.setText("");
		iQuickDrop.setVisible(false); iQuickDrop.setEnabled(false);
		request.addCourse(course);
		iCustomCallback = callback;
		if (iAllChoices != null) iAllChoices.setValue(false);
		request.setShowAllChoices(false);
		iSectioningService.computeSuggestions(iOnline, request, rows, -1, iFilter.getText(), new AsyncCallback<Collection<ClassAssignmentInterface>>() {
			@Override
			public void onSuccess(Collection<ClassAssignmentInterface> result) {
				if (result == null || result.isEmpty()) {
					LoadingWidget.getInstance().hide();
					iCustomCallback.onFailure(new SectioningException(MESSAGES.suggestionsNoChoices(iSource)));
				} else if (result.size() == 1) {
					ClassAssignmentInterface a = result.iterator().next();
					if (a.getCourseAssignments().isEmpty()) {
						if (iAllChoices == null) {
							LoadingWidget.getInstance().hide();	
							if (a.hasMessages())
								iCustomCallback.onFailure(new SectioningException(a.getMessages(", ")));
							else
								iCustomCallback.onFailure(new SectioningException(MESSAGES.suggestionsNoChoices(iSource)));
						} else {
							request.setShowAllChoices(true);
							iSectioningService.computeSuggestions(iOnline, request, rows, -1, iFilter.getText(), new AsyncCallback<Collection<ClassAssignmentInterface>>() {
								@Override
								public void onSuccess(Collection<ClassAssignmentInterface> result) {
									iAllChoices.setValue(true);
									if (result == null || result.isEmpty()) {
										LoadingWidget.getInstance().hide();
										iCustomCallback.onFailure(new SectioningException(MESSAGES.suggestionsNoChoices(iSource)));
									} else if (result.size() == 1) {
										ClassAssignmentInterface a = result.iterator().next();
										if (a.getCourseAssignments().isEmpty()) {
											LoadingWidget.getInstance().hide();	
											if (a.hasMessages())
												iCustomCallback.onFailure(new SectioningException(a.getMessages(", ")));
											else
												iCustomCallback.onFailure(new SectioningException(MESSAGES.suggestionsNoChoices(iSource)));
										} else {
											iCallback.onSuccess(result);
										}
									} else {
										iCallback.onSuccess(result);
									}
								}
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									iCustomCallback.onFailure(caught);
								}
							});
						}
					} else {
						iCallback.onSuccess(result);
					}
				} else {
					iCallback.onSuccess(result);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iCustomCallback.onFailure(caught);
			}
		});
	}

	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
	    switch (DOM.eventGetType((Event) event.getNativeEvent())) {
	    case Event.ONKEYUP:
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
				hide();
			}
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
				if (iSuggestions.getRowsCount() > 0) {
					String id = (iSuggestions.getSelectedRow() < 0 ? null : iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId());
					int row = iSuggestions.getSelectedRow() + 1;
					while (id != null && id.equals(iSuggestions.getRows()[row % iSuggestions.getRowsCount()].getId())) row++;
					iSuggestions.setSelectedRow(row % iSuggestions.getRowsCount());
					
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					AriaStatus.getInstance().setText(ARIA.showingAlternative(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()), Integer.parseInt(iSuggestions.getRows()[iSuggestions.getRows().length - 1].getId()), toString(suggestion)));
				}
			}
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
				if (iSuggestions.getRowsCount() > 0) {
					int row = iSuggestions.getSelectedRow() <= 0 ? iSuggestions.getRowsCount() - 1 : iSuggestions.getSelectedRow() - 1;
					String id = iSuggestions.getRows()[row % iSuggestions.getRowsCount()].getId();
					while (id.equals(iSuggestions.getRows()[(iSuggestions.getRowsCount() + row - 1) % iSuggestions.getRowsCount()].getId())) row--;
					iSuggestions.setSelectedRow((iSuggestions.getRowsCount() + row) % iSuggestions.getRowsCount());
					
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					AriaStatus.getInstance().setText(ARIA.showingAlternative(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()), Integer.parseInt(iSuggestions.getRows()[iSuggestions.getRows().length - 1].getId()), toString(suggestion)));
				}
			}
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				if (iSuggestions.getSelectedRow() >= 0) {
					ClassAssignmentInterface suggestion = iResult.get(Integer.parseInt(iSuggestions.getRows()[iSuggestions.getSelectedRow()].getId()));
					
					AriaStatus.getInstance().setText(ARIA.selectedAlternative(toString(suggestion)));

					SuggestionSelectedEvent e = new SuggestionSelectedEvent(suggestion);
					for (SuggestionSelectedHandler h: iSuggestionSelectedHandlers)
						h.onSuggestionSelected(e);
					hide();
					if (iCustomCallback != null) iCustomCallback.onSuccess(suggestion);
				}
			}
			break;
	    }
	}
	
	private String toString(ClassAssignmentInterface suggestion) {
		String ret = "";
		for (ClassAssignmentInterface.CourseAssignment course: suggestion.getCourseAssignments()) {
			if (course.isTeachingAssignment()) continue;
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
			if (old == null || old.isFreeTime() || old.isTeachingAssignment()) continue;
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

	public interface QuickDropHandler {
		public void onQuickDrop(QuickDropEvent event);
	}

	public class QuickDropEvent {
		private ClassAssignmentInterface.ClassAssignment iAssignment;
		private QuickDropEvent(ClassAssignmentInterface.ClassAssignment assignment) {
			iAssignment = assignment;
		}
		public ClassAssignmentInterface.ClassAssignment getAssignment() { return iAssignment; }
	}

	public void addQuickDropHandler(QuickDropHandler h) {
		iQuickDropHandlers.add(h);
	}
	
	public static class F extends P {
		private PopupPanel iHint = null;
		private Timer iShowHint, iHideHint = null;

		public F(Widget hint, String... styles) {
			super(styles);
			iHint = new PopupPanel();
			iHint.setWidget(hint);
			iHint.setStyleName("unitime-PopupHint");
			addMouseMoveHandler(new MouseMoveHandler() {
				@Override
				public void onMouseMove(MouseMoveEvent event) {
					int x = 10 + event.getClientX() + getElement().getOwnerDocument().getScrollLeft();
					int y = 10 + event.getClientY() + getElement().getOwnerDocument().getScrollTop();
					if (iHint.isShowing()) {
						iHint.setPopupPosition(x, y);
					} else {
						iShowHint.cancel();
						iHint.setPopupPosition(x, y);
						iShowHint.schedule(1000);
					}
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					iShowHint.cancel();
					if (iHint.isShowing())
						iHideHint.schedule(1000);					
				}
			});
			iShowHint = new Timer() {
				@Override
				public void run() {
					iHint.show();
				}
			};
			iHideHint = new Timer() {
				@Override
				public void run() {
					iHint.hide();
				}
			};
		}
		
		public void hideHint() {
			iShowHint.cancel();
			if (iHint.isShowing()) {
				iHint.hide();
			}
		}
	}
}
