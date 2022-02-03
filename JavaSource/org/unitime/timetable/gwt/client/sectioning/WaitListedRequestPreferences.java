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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.aria.AriaHiddenLabel;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.CourseFinderCourseDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseEvent;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CodeLabel;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class WaitListedRequestPreferences extends UniTimeDialogBox implements HasValue<Request>, HasSelectionHandlers<Request>, KeyUpHandler {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	protected static SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	protected static Logger sLogger = Logger.getLogger(WaitListedRequestPreferences.class.getName());
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestWaitListedDateFormat());
	
	private StudentSectioningContext iContext;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private SimpleForm iForm;
	private UniTimeTable<CourseAssignment> iCourses;
	private FocusPanel iCoursesPanel;
	private AriaTabBar iCourseDetailsTabBar;
	private ScrollPanel iCourseDetailsPanel;
	private CourseFinderCourseDetails[] iDetails = null;
	private P iInstructionalMethodsPanel = null;
	private Map<Preference, CheckBox> iInstructionalMethods = new HashMap<Preference, CheckBox>();
	private UniTimeHeaderPanel iButtons;
	
	private CourseAssignment iLastDetails = null;
	private Request iRequest = null;
	private ListBox iEnrolledCoursesList = null;
	private CourseRequestLine iLine = null;
	private CheckBox iWaitListed;
	
	public WaitListedRequestPreferences(StudentSectioningContext context) {
		super(true, true);
		addStyleName("unitime-WaitListedRequestPreferences");
		setText(MESSAGES.dialogWaitListedRequestPreferences());
		iContext = context;
		
		iForm = new SimpleForm();
		
		iWaitListed = new CheckBox(MESSAGES.checkWaitListSwapWithNewWaitList());
		iForm.addRow(MESSAGES.propWaitListSwapWithWaitListed(), iWaitListed);
		
		iCourses = new UniTimeTable<CourseAssignment>();
		iCourses.setAllowMultiSelect(false);
		iCourses.setAllowSelection(true);
		List<UniTimeTableHeader> head = new ArrayList<UniTimeTableHeader>();
		head.add(new UniTimeTableHeader(MESSAGES.colSubject()));
		head.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		head.add(new UniTimeTableHeader(MESSAGES.colLimit()));
		head.add(new UniTimeTableHeader(MESSAGES.colTitle()));
		head.add(new UniTimeTableHeader(MESSAGES.colCredit()));
		head.add(new UniTimeTableHeader(MESSAGES.colNote()));
		head.add(new UniTimeTableHeader(MESSAGES.colWaitListAndAllowedOverrides()));
		iCourses.addRow(null, head);
		iCourses.addMouseClickListener(new UniTimeTable.MouseClickListener<CourseAssignment>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<CourseAssignment> event) {
				updateCourseDetails();
			}
		});
		iCoursesPanel = new FocusPanel(iCourses);
		iCoursesPanel.setStyleName("unitime-ScrollPanel");
		iCoursesPanel.addStyleName("course-table");
		iCoursesPanel.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (iCourses.getRowCount() < 2 || iCourses.getData(1) == null) return;
				int row = iCourses.getSelectedRow();
				if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
					if (row < 0 || iCourses.getSelectedRow() + 1 >= iCourses.getRowCount())
						iCourses.setSelected(1, true);
					else
						iCourses.setSelected(row + 1, true);
		            scrollToSelectedRow();
		            updateCourseDetails();
				}
				if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
					if (row - 1 < 1)
						iCourses.setSelected(iCourses.getRowCount() - 1, true);
					else
						iCourses.setSelected(row - 1, true);
					scrollToSelectedRow();
					updateCourseDetails();
				}
				if (event.isControlKeyDown() || event.isAltKeyDown()) {
					for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
						if (event.getNativeKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeKeyCode() == Character.toUpperCase(entry.getKey())) {
							iCourseDetailsTabBar.selectTab(entry.getValue(), true);
							event.preventDefault();
							event.stopPropagation();
						}
				}
			}
		});
		
		iCourseDetailsTabBar = new AriaTabBar();
		iCourseDetailsTabBar.addStyleName("course-details-tabs");
		iCourseDetailsPanel = new ScrollPanel();
		iCourseDetailsPanel.addStyleName("course-details");
		iCourseDetailsTabBar.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				Cookies.setCookie("UniTime:CourseFinderCourses", String.valueOf(event.getSelectedItem()));
				iCourseDetailsPanel.setWidget(iDetails[event.getSelectedItem()]);
			}
		});
		iInstructionalMethodsPanel = new P("instructional-methods");
		iCourseDetailsTabBar.setRestWidget(iInstructionalMethodsPanel);
		
		iForm.addRow(iCoursesPanel);
		P courseDetailsPanel = new P("course-details-panel");
		courseDetailsPanel.add(iCourseDetailsTabBar);
		courseDetailsPanel.add(iCourseDetailsPanel);
		iForm.addRow(courseDetailsPanel);
		
		P swapCoursePanel = new P("swap-courses-panel");
		iEnrolledCoursesList = new ListBox();
		iEnrolledCoursesList.addItem(MESSAGES.itemWaitListSwapWithNoCourseOffering(), "");
		swapCoursePanel.add(iEnrolledCoursesList);
		swapCoursePanel.add(new Label(MESSAGES.descWaitListSwapWithCourseOffering()));
		iForm.addRow(MESSAGES.propWaitListSwapWithCourseOffering(), swapCoursePanel);
		
		iButtons = new UniTimeHeaderPanel();
		iButtons.addButton("submit", MESSAGES.buttonSubmitWaitListedRequestPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit();
			}
		});
		iButtons.addButton("cancel", MESSAGES.buttonCloseWaitListedRequestPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		iForm.addBottomRow(iButtons);
		
		setWidget(iForm);
		
		CourseFinderDetails details = new CourseFinderDetails();
		details.setDataProvider(new DataProvider<CourseAssignment, String>() {
			@Override
			public void getData(CourseAssignment source, AsyncCallback<String> callback) {
				sSectioningService.retrieveCourseDetails(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
			}
		});
		CourseFinderClasses classes = new CourseFinderClasses(true, null, null);
		classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
			@Override
			public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
				sSectioningService.listClasses(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
			}
		});
		setCourseDetails(details, classes);
		
		sinkEvents(Event.ONKEYUP);
	}
	
	protected void onSubmit() {
		hide();
		iLine.setValue(getValue());
	}
	
	public void show(CourseRequestLine line) {
		if (line == null) return;
		iLine = line;
		setValue(line.getValue());
		center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iCoursesPanel.setFocus(true);
			}
		});
	}
	
	public void showChecked(CourseRequestLine line) {
		if (line == null) return;
		iLine = line;
		setValue(line.getValue());
		iWaitListed.setValue(true);
		center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iCoursesPanel.setFocus(true);
			}
		});
	}
	
	private void selectLastTab() {
		try {
			int tab = Integer.valueOf(Cookies.getCookie("UniTime:CourseFinderCourses"));
			if (tab >= 0 || tab < iCourseDetailsTabBar.getTabCount() && tab != iCourseDetailsTabBar.getSelectedTab())
				iCourseDetailsTabBar.selectTab(tab, true);
			else
				iCourseDetailsTabBar.selectTab(0, true);
		} catch (Exception e) {
			iCourseDetailsTabBar.selectTab(0, true);
		}
	}
	
	public void setCourseDetails(CourseFinderCourseDetails... details) {
		iDetails = details;
		int tabIndex = 0;
		for (CourseFinderCourseDetails detail: iDetails) {
			ScrollPanel panel = new ScrollPanel(detail.asWidget());
			panel.setStyleName("unitime-ScrollPanel-inner");
			panel.addStyleName("course-info");
			iCourseDetailsTabBar.addTab(detail.getName(), true);
			Character ch = UniTimeHeaderPanel.guessAccessKey(detail.getName());
			if (ch != null)
				iTabAccessKeys.put(ch, tabIndex);
			tabIndex++;
		}
		selectLastTab();
	}
	
	protected void updateCourseDetails() {
		if (iLastDetails != null) {
			for (RequestedCourse rc: iRequest.getRequestedCourse()) {
				if (rc.equals(iLastDetails)) {
					rc.clearSelection();
					for (Map.Entry<Preference, CheckBox> e: iInstructionalMethods.entrySet())
						if (e.getValue().isEnabled() && e.getValue().getValue())
							rc.setSelectedIntructionalMethod(e.getKey(), true);
					if (iDetails != null)
						for (CourseFinderCourseDetails d: iDetails)
							d.onGetValue(rc);
				}
			}
		}
		int row = iCourses.getSelectedRow();
		CourseAssignment record = iCourses.getData(row);
		iLastDetails = record;
		if (record == null) {
			if (iDetails != null)
				for (CourseFinderCourseDetails detail: iDetails) {
					detail.setValue(null);
				}
			if (isVisible() && isAttached())
				AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
			iInstructionalMethodsPanel.clear();
			iInstructionalMethods.clear();
		} else {
			final RequestedCourse rc = iRequest.getRequestedCourse(row - 1);
			for (CourseFinderCourseDetails detail: iDetails) {
				detail.setValue(record);
			}
			if (record.hasTitle()) {
				if (record.hasNote()) {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithTitleAndNote(iCourses.getSelectedRow(), iCourses.getRowCount() - 1, record.getSubject(), record.getCourseNbr(), record.getTitle(), record.getNote()));
				} else {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithTitle(iCourses.getSelectedRow(), iCourses.getRowCount() - 1, record.getSubject(), record.getCourseNbr(), record.getTitle()));
				}
			} else {
				if (record.hasNote()) {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithNote(iCourses.getSelectedRow(), iCourses.getRowCount() - 1, record.getSubject(), record.getCourseNbr(), record.getNote()));
				} else {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelected(iCourses.getSelectedRow(), iCourses.getRowCount() - 1, record.getSubject(), record.getCourseNbr()));
				}
			}
			iInstructionalMethodsPanel.clear();
			iInstructionalMethods.clear();
			if (record.hasInstructionalMethodSelection()) {
				P imp = new P("preference-label"); imp.setText(MESSAGES.labelInstructionalMethodPreference()); iInstructionalMethodsPanel.add(imp);
				for (final IdValue m: record.getInstructionalMethods()) {
					CheckBox ch = new CheckBox(m.getValue());
					ch.setValue(rc.isSelectedIntructionalMethod(m.getId(), true));
					ch.addStyleName("instructional-method");
					iInstructionalMethodsPanel.add(ch);
					final Preference p = new Preference(m.getId(), m.getValue(), true);
					ch.addStyleName("instructional-method");
					iInstructionalMethods.put(p, ch);
				}
			}
		}
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<Request> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Request> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Request getValue() {
		if (iLastDetails != null) {
			for (RequestedCourse rc: iRequest.getRequestedCourse()) {
				if (rc.equals(iLastDetails)) {
					rc.clearSelection();
					for (Map.Entry<Preference, CheckBox> e: iInstructionalMethods.entrySet())
						if (e.getValue().isEnabled() && e.getValue().getValue())
							rc.setSelectedIntructionalMethod(e.getKey(), true);
					if (iDetails != null)
						for (CourseFinderCourseDetails d: iDetails)
							d.onGetValue(rc);
					if (rc.hasSelectedClasses())
						for (Preference p: rc.getSelectedClasses())
							p.setRequired(true);
				}
			}
		}
		iRequest.setWaitList(iWaitListed.getValue());
		iRequest.setWaitListSwapWithCourseOfferingId(iEnrolledCoursesList.getSelectedIndex() <= 0 ? null : Long.valueOf(iEnrolledCoursesList.getSelectedValue()));
		return iRequest;
	}
	
	public void setSchedule(ClassAssignmentInterface lastEnrollment) {
		iEnrolledCoursesList.clear();
		iEnrolledCoursesList.addItem(MESSAGES.itemWaitListSwapWithNoCourseOffering(), "");
		for (ClassAssignmentInterface.CourseAssignment course: lastEnrollment.getCourseAssignments()) {
			if (course.isAssigned() && !course.isTeachingAssignment() && !course.isFreeTime())
				iEnrolledCoursesList.addItem(course.getCourseNameWithTitle(), course.getCourseId().toString());
		}
	}

	@Override
	public void setValue(Request value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Request value, boolean fireEvents) {
		iRequest = new Request(value);
		if (iRequest.getWaitListedTimeStamp() == null) {
			iWaitListed.setText(MESSAGES.checkWaitListSwapWithNewWaitList()); iWaitListed.addStyleName("new-wait-list"); 
		} else {
			iWaitListed.setText(sDF.format(iRequest.getWaitListedTimeStamp())); iWaitListed.removeStyleName("new-wait-list");
		}
		iWaitListed.setValue(iRequest.isWaitList());
		iLastDetails = null;
		for (RequestedCourse rc: iRequest.getRequestedCourse()) {
			if (rc.hasSelectedClasses())
				for (Iterator<Preference> i = rc.getSelectedClasses().iterator(); i.hasNext(); ) {
					Preference p = i.next();
					if (!p.isRequired()) i.remove();
				}
			if (rc.hasSelectedIntructionalMethods())
				for (Iterator<Preference> i = rc.getSelectedIntructionalMethods().iterator(); i.hasNext(); ) {
					Preference p = i.next();
					if (!p.isRequired()) i.remove();
				}
		}
		for (CourseFinderCourseDetails detail: iDetails) {
			detail.onSetValue(iRequest.getRequestedCourse().toArray(new RequestedCourse[0]));
		}
		iEnrolledCoursesList.setSelectedIndex(0);
		if (iRequest.hasWaitListSwapWithCourseOfferingId()) {
			for (int i = 1; i < iEnrolledCoursesList.getItemCount(); i++)
				if (iEnrolledCoursesList.getValue(i).equals(iRequest.getWaitListSwapWithCourseOfferingId().toString())) {
					iEnrolledCoursesList.setSelectedIndex(i);
					break;
				}
		}
		
		sSectioningService.getCoursesFromRequest(iContext, value, new AsyncCallback<Collection<CourseAssignment>>() {
			@Override
			public void onSuccess(Collection<CourseAssignment> result) {
				iCourses.clearTable(1);
				boolean hasCredit = false, hasNote = false, hasWaitList = false;
				for (final CourseAssignment record: result) {
					List<Widget> line = new ArrayList<Widget>();
					line.add(new Label(record.getSubject(), false));
					line.add(new Label(record.getCourseNbr(), false));
					line.add(new HTML(record.getLimit() == null || record.getLimit() == 0 || record.getEnrollment() == null ? "" : record.getLimit() < 0 ? "&infin;" : (record.getLimit() - record.getEnrollment()) + " / " + record.getLimit(), false));
					line.add(new Label(record.getTitle() == null ? "" : record.getTitle(), false));
					if (record.hasCredit()) {
						Label credit = new Label(record.getCreditAbbv(), false);
						if (record.hasCredit()) credit.setTitle(record.getCreditText());
						line.add(credit);
						hasCredit = true;
					} else {
						line.add(new Label());
					}
					line.add(new Label(record.getNote() == null ? "" : record.getNote()));
					if (record.hasNote()) hasNote = true;
					P wl = new P("courses-wl");
					if (record.isCanWaitList()) {
						Label l = new Label(MESSAGES.courseAllowsForWaitListing());
						l.setTitle(MESSAGES.courseAllowsForWaitListingTitle(record.getCourseName())); 
						wl.add(l);
						hasWaitList = true;
					}
					if (record.hasOverrides()) {
						for (CodeLabel override: record.getOverrides()) {
							Label l = new Label(override.getCode()); l.setTitle(override.getLabel());
							wl.add(l);
							hasWaitList = true;
						}
					}
					line.add(wl);
					if (record.hasTitle()) {
						if (record.hasNote()) {
							line.add(new AriaHiddenLabel(ARIA.courseFinderCourseWithTitleAndNote(record.getSubject(), record.getCourseNbr(), record.getTitle(), record.getNote())));
						} else {
							line.add(new AriaHiddenLabel(ARIA.courseFinderCourseWithTitle(record.getSubject(), record.getCourseNbr(), record.getTitle())));
						}
					} else {
						if (record.hasNote()) {
							line.add(new AriaHiddenLabel(ARIA.courseFinderCourseWithNote(record.getSubject(), record.getCourseNbr(), record.getNote())));
						} else {
							line.add(new AriaHiddenLabel(ARIA.courseFinderCourse(record.getSubject(), record.getCourseNbr())));
						}
					}
					iCourses.addRow(record, line);
				}
				iCourses.setColumnVisible(5, hasCredit);
				iCourses.setColumnVisible(6, hasNote);
				iCourses.setColumnVisible(7, hasWaitList);
				iCourses.setSelected(1, true);
				scrollToSelectedRow();
				if (fireEvents)
					ValueChangeEvent.fire(WaitListedRequestPreferences.this, getValue());
				updateCourseDetails();
				ResponseEvent.fire(WaitListedRequestPreferences.this, !result.isEmpty());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iCourses.clearTable(1);
				iCourses.setEmptyMessage(caught.getMessage());
				if (isVisible())
					AriaStatus.getInstance().setText(caught.getMessage());
				updateCourseDetails();
				ResponseEvent.fire(WaitListedRequestPreferences.this, false);	
			}
		}); 
		
		iInstructionalMethods.clear();
	}
	
	public void scrollToSelectedRow() {
		int row = iCourses.getSelectedRow(); 
		if (row >= 0)
			iCourses.getRowFormatter().getElement(row).scrollIntoView();
	}
	
	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (event.isControlKeyDown() || event.isAltKeyDown()) {
			for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
				if (event.getNativeKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeKeyCode() == Character.toUpperCase(entry.getKey())) {
					iCourseDetailsTabBar.selectTab(entry.getValue(), true);
					event.preventDefault();
					event.stopPropagation();
				}
		}
	}

}
