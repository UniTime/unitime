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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaHiddenLabel;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.CourseFinderCourseDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseEvent;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseHandler;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseFinderCourses extends P implements CourseFinder.CourseFinderTab<Collection<CourseAssignment>> {
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private DataProvider<String, Collection<CourseAssignment>> iDataProvider = null;
	private UniTimeTable<CourseAssignment> iCourses;
	private ScrollPanel iCoursesPanel;
	private Label iCoursesTip;
	private AriaTabBar iCourseDetailsTabBar;
	private ScrollPanel iCourseDetailsPanel;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private CourseFinderCourseDetails[] iDetails = null;
	private String iLastQuery = null;
	private P iInstructionalMethodsPanel = null;
	private Map<Long, CheckBox> iInstructionalMethods = new HashMap<Long, CheckBox>();
	
	private boolean iShowCourseTitles = false, iShowDefaultSuggestions = false;
	
	public CourseFinderCourses() {
		this(false, false);
	}
	
	public CourseFinderCourses(boolean showCourseTitles, boolean showDefaultSuggestions) {
		super("courses");
		
		iShowCourseTitles = showCourseTitles;
		iShowDefaultSuggestions = showDefaultSuggestions;
		
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
		iCourses.addRow(null, head);
		iCourses.addMouseDoubleClickListener(new UniTimeTable.MouseDoubleClickListener<CourseAssignment>() {
			@Override
			public void onMouseDoubleClick(UniTimeTable.TableEvent<CourseAssignment> event) {
				updateCourseDetails();
				SelectionEvent.fire(CourseFinderCourses.this, getValue());
			}
		});
		iCourses.addMouseClickListener(new UniTimeTable.MouseClickListener<CourseAssignment>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<CourseAssignment> event) {
				updateCourseDetails();
			}
		});
		iCoursesPanel = new ScrollPanel(iCourses);
		iCoursesPanel.setStyleName("unitime-ScrollPanel");
		iCoursesPanel.addStyleName("course-table");
		
		iCoursesTip = new Label(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		iCoursesTip.setStyleName("unitime-Hint");
		iCoursesTip.addStyleName("course-tip");
		ToolBox.disableTextSelectInternal(iCoursesTip.getElement());
		iCoursesTip.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String oldText = iCoursesTip.getText();
				do {
					iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
				} while (oldText.equals(iCoursesTip.getText()));
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

		add(iCoursesPanel);
		add(iCourseDetailsTabBar);
		add(iCourseDetailsPanel);
		add(iCoursesTip);
	}

	@Override
	public void setDataProvider(DataProvider<String, Collection<CourseAssignment>> provider) {
		iDataProvider = provider;
	}

	@Override
	public String getName() {
		return MESSAGES.courseSelectionCourses();
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setValue(final String value) {
		setValue(value, false);
	}

	@Override
	public String getValue() {
		int row = iCourses.getSelectedRow();
		if (iCourses.getSelectedRow() < 0) return null;
		CourseAssignment record = iCourses.getData(row);
		if (record == null) return null;
		String courseName = MESSAGES.courseName(record.getSubject(), record.getCourseNbr());
		if (record.hasTitle() && (!record.hasUniqueName() || iShowCourseTitles))
			courseName = MESSAGES.courseNameWithTitle(record.getSubject(), record.getCourseNbr(), record.getTitle());
		return courseName;
	}

	@Override
	public void setValue(String value, final boolean fireEvents) {
		if (value == null) value = "";
		if (value.isEmpty() && !iShowDefaultSuggestions) {
			iLastQuery = null;
			iCourses.clearTable(1);
			iCourses.setEmptyMessage(MESSAGES.courseSelectionNoCourseFilter());
			updateCourseDetails();
		} else if (!value.equals(iLastQuery)) {
			iLastQuery = value;
			iDataProvider.getData(value, new AsyncCallback<Collection<CourseAssignment>>() {
				public void onFailure(Throwable caught) {
					iCourses.clearTable(1);
					iCourses.setEmptyMessage(caught.getMessage());
					if (isVisible())
						AriaStatus.getInstance().setText(caught.getMessage());
					updateCourseDetails();
					ResponseEvent.fire(CourseFinderCourses.this, false);
				}
				public void onSuccess(Collection<CourseAssignment> result) {
					iCourses.clearTable(1);
					boolean hasCredit = false, hasNote = false;
					for (CourseAssignment record: result) {
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
						int row = iCourses.addRow(record, line);
						if (iLastQuery.equalsIgnoreCase(MESSAGES.courseName(record.getSubject(), record.getCourseNbr())) || (record.getTitle() != null && iLastQuery.equalsIgnoreCase(MESSAGES.courseNameWithTitle(record.getSubject(), record.getCourseNbr(), record.getTitle()))))
							iCourses.setSelected(row, true);
					}
					iCourses.setColumnVisible(4, hasCredit);
					iCourses.setColumnVisible(5, hasNote);
					if (result.size() == 1)
						iCourses.setSelected(1, true);
					if (iCourses.getSelectedRow() >= 0) {
						scrollToSelectedRow();
						if (fireEvents)
							ValueChangeEvent.fire(CourseFinderCourses.this, getValue());
					}
					updateCourseDetails();
					ResponseEvent.fire(CourseFinderCourses.this, !result.isEmpty());
				}
	        });
		}
	}

	protected void scrollToSelectedRow() {
		if (iCourses.getSelectedRow() < 0) return;
		
		Element scroll = iCoursesPanel.getElement();
		
		com.google.gwt.dom.client.Element item = iCourses.getRowFormatter().getElement(iCourses.getSelectedRow());
		if (item==null) return;
		
		int realOffset = 0;
		while (item !=null && !item.equals(scroll)) {
			realOffset += item.getOffsetTop();
			item = item.getOffsetParent();
		}
		
		scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
	}
	
	protected void updateCourseDetails() {
		int row = iCourses.getSelectedRow();
		CourseAssignment record = iCourses.getData(row);
		if (record == null) {
			if (iDetails != null)
				for (CourseFinderCourseDetails detail: iDetails) {
					detail.setValue(null);
				}
			AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
			iInstructionalMethodsPanel.clear();
			iInstructionalMethods.clear();
		} else {
			for (CourseFinderCourseDetails detail: iDetails)
				detail.setValue(record);
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
			AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
			iInstructionalMethodsPanel.clear();
			iInstructionalMethods.clear();
			if (record.hasInstructionalMethodSelection()) {
				for (IdValue m: record.getInstructionalMethods()) {
					CheckBox ch = new CheckBox(m.getValue());
					ch.addStyleName("instructional-method");
					iInstructionalMethods.put(m.getId(), ch);
					iInstructionalMethodsPanel.add(ch);
				}
			} else if (record.hasInstructionalMethods()) {
				for (IdValue m: record.getInstructionalMethods()) {
					CheckBox ch = new CheckBox(m.getValue());
					ch.addStyleName("instructional-method");
					ch.setValue(true); ch.setEnabled(false);
					iInstructionalMethods.put(m.getId(), ch);
					iInstructionalMethodsPanel.add(ch);
				}
			}
		}
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public boolean isCourseSelection() {
		return true;
	}

	@Override
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

	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (iCourses.getRowCount() < 2 || iCourses.getData(1) == null) return;
		int row = iCourses.getSelectedRow();
		if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
			if (row < 0 || iCourses.getSelectedRow() + 1 >= iCourses.getRowCount())
				iCourses.setSelected(1, true);
			else
				iCourses.setSelected(row + 1, true);
            scrollToSelectedRow();
            updateCourseDetails();
		} else if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
			if (row - 1 < 1)
				iCourses.setSelected(iCourses.getRowCount() - 1, true);
			else
				iCourses.setSelected(row - 1, true);
			scrollToSelectedRow();
			updateCourseDetails();
		} else if (event.isControlKeyDown() || event.isAltKeyDown()) {
			for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
				if (event.getNativeKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeKeyCode() == Character.toUpperCase(entry.getKey())) {
					iCourseDetailsTabBar.selectTab(entry.getValue(), true);
					event.preventDefault();
					event.stopPropagation();
				}
		}
	}
	
	@Override
	public HandlerRegistration addResponseHandler(ResponseHandler handler) {
		return addHandler(handler, ResponseEvent.getType());
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

	@Override
	public void changeTip() {
		iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		selectLastTab();
	}
}
