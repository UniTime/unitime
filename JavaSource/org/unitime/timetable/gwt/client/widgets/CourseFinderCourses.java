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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.CourseFinderCourseDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseEvent;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseHandler;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseFinderCourses extends VerticalPanel implements CourseFinder.CourseFinderTab<Collection<CourseAssignment>> {
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private DataProvider<String, Collection<CourseAssignment>> iDataProvider = null;
	private WebTable iCourses;
	private ScrollPanel iCoursesPanel;
	private Label iCoursesTip;
	private UniTimeTabPanel iCourseDetailsTabPanel;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private CourseFinderCourseDetails[] iDetails = null;
	private String iLastQuery = null;
	private boolean iShowCourses = false;
	
	public CourseFinderCourses() {
		this(false);
	}
	
	public CourseFinderCourses(boolean showCourses) {
		super();
		
		iShowCourses = showCourses;
		iCourses = new WebTable();
		iCourses.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colSubject(), 1, "80px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "80px"),
				new WebTable.Cell(MESSAGES.colLimit(), 1, "60px"),
				new WebTable.Cell(MESSAGES.colTitle(), 1, "300px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "60px"),
				new WebTable.Cell(MESSAGES.colNote(), 1, "300px")
				));
		iCourses.addRowDoubleClickHandler(new WebTable.RowDoubleClickHandler() {
			public void onRowDoubleClick(WebTable.RowDoubleClickEvent event) {
				iCourses.setSelectedRow(event.getRowIdx());
				updateCourseDetails();
				SelectionEvent.fire(CourseFinderCourses.this, getValue());
			}
		});
		iCourses.addRowClickHandler(new WebTable.RowClickHandler() {
			public void onRowClick(WebTable.RowClickEvent event) {
				iCourses.setSelectedRow(event.getRowIdx());
				updateCourseDetails();
			}
		});
		
		iCoursesPanel = new ScrollPanel(iCourses);
		iCoursesPanel.getElement().getStyle().setWidth(780, Unit.PX);
		iCoursesPanel.getElement().getStyle().setHeight(200, Unit.PX);
		iCoursesPanel.setStyleName("unitime-ScrollPanel");
		
		iCoursesTip = new Label(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		iCoursesTip.setStyleName("unitime-Hint");
		ToolBox.disableTextSelectInternal(iCoursesTip.getElement());
		iCoursesTip.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String oldText = iCoursesTip.getText();
				do {
					iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
				} while (oldText.equals(iCoursesTip.getText()));
			}
		});
		
		iCourseDetailsTabPanel = new UniTimeTabPanel();
		iCourseDetailsTabPanel.setDeckStyleName("unitime-TabPanel");
		iCourseDetailsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				Cookies.setCookie("UniTime:CourseFinderCourses", String.valueOf(event.getSelectedItem()));
			}
		});

		setSpacing(10);
		add(iCoursesPanel);
		add(iCourseDetailsTabPanel);
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
		if (iCourses.getSelectedRow() < 0) {
			return null;
		} else {
			WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
			String courseName = MESSAGES.courseName(row.getCell(0).getValue(), row.getCell(1).getValue());
			if ("false".equals(row.getId()) || iShowCourses)
				courseName = MESSAGES.courseNameWithTitle(row.getCell(0).getValue(), row.getCell(1).getValue(), row.getCell(3).getValue());
			return courseName;
		}
	}

	@Override
	public void setValue(String value, final boolean fireEvents) {
		if (value == null || value.isEmpty()) {
			iLastQuery = null;
			iCourses.clearData(true);
			iCourses.setEmptyMessage(MESSAGES.courseSelectionNoCourseFilter());
			updateCourseDetails();
		} else if (!value.equals(iLastQuery)) {
			iLastQuery = value;
			iDataProvider.getData(value, new AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>>() {
				public void onFailure(Throwable caught) {
					iCourses.clearData(true);
					iCourses.setEmptyMessage(caught.getMessage());
					if (isVisible())
						AriaStatus.getInstance().setText(caught.getMessage());
					updateCourseDetails();
					ResponseEvent.fire(CourseFinderCourses.this, false);
				}
				public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
					WebTable.Row[] records = new WebTable.Row[result.size()];
					int idx = 0;
					int selectRow = -1;
					for (ClassAssignmentInterface.CourseAssignment record: result) {
						records[idx] = new WebTable.Row(
								record.getSubject(),
								record.getCourseNbr(),
								(record.getLimit() == null || record.getLimit() == 0 || record.getEnrollment() == null ? "" : record.getLimit() < 0 ? "&infin;" : (record.getLimit() - record.getEnrollment()) + " / " + record.getLimit()),
								(record.getTitle() == null ? "" : record.getTitle()),
								(record.hasCredit() ? record.getCreditAbbv() : ""),
								(record.getNote() == null ? "" : record.getNote()));
						if (record.hasCredit())
							records[idx].getCell(4).setTitle(record.getCreditText());
						records[idx].setId(record.hasUniqueName() ? "true" : "false");
						if (iLastQuery.equalsIgnoreCase(MESSAGES.courseName(record.getSubject(), record.getCourseNbr())) || (record.getTitle() != null && iLastQuery.equalsIgnoreCase(MESSAGES.courseNameWithTitle(record.getSubject(), record.getCourseNbr(), record.getTitle()))))
							selectRow = idx;
						if (record.getTitle() == null || record.getTitle().isEmpty()) {
							if (record.getNote() == null || record.getNote().isEmpty()) {
								records[idx].setAriaLabel(ARIA.courseFinderCourse(record.getSubject(), record.getCourseNbr()));
							} else {
								records[idx].setAriaLabel(ARIA.courseFinderCourseWithNote(record.getSubject(), record.getCourseNbr(), record.getNote()));
							}
						} else {
							if (record.getNote() == null || record.getNote().isEmpty()) {
								records[idx].setAriaLabel(ARIA.courseFinderCourseWithTitle(record.getSubject(), record.getCourseNbr(), record.getTitle()));
							} else {
								records[idx].setAriaLabel(ARIA.courseFinderCourseWithTitleAndNote(record.getSubject(), record.getCourseNbr(), record.getTitle(), record.getNote()));
							}
						}
						idx++;
					}
					iCourses.setData(records);
					if (records.length == 1)
						selectRow = 0;
					if (selectRow >= 0) {
						iCourses.setSelectedRow(selectRow);
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
		if (iCourses.getSelectedRow()<0) return;
		
		Element scroll = iCoursesPanel.getElement();
		
		com.google.gwt.dom.client.Element item = iCourses.getTable().getRowFormatter().getElement(iCourses.getSelectedRow());
		if (item==null) return;
		
		int realOffset = 0;
		while (item !=null && !item.equals(scroll)) {
			realOffset += item.getOffsetTop();
			item = item.getOffsetParent();
		}
		
		scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
	}
	
	protected void updateCourseDetails() {
		if (iCourses.getSelectedRow() < 0 || iCourses.getRows() == null || iCourses.getRows().length == 0) {
			if (iDetails != null)
				for (CourseFinderCourseDetails detail: iDetails) {
					detail.setValue(null);
				}
			AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
		} else {
			WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
			String courseName = MESSAGES.courseName(row.getCell(0).getValue(), row.getCell(1).getValue());
			if ("false".equals(row.getId()) || iShowCourses)
				courseName = MESSAGES.courseNameWithTitle(row.getCell(0).getValue(), row.getCell(1).getValue(), row.getCell(3).getValue());
			for (CourseFinderCourseDetails detail: iDetails)
				detail.setValue(courseName);
			String title = row.getCell(3).getValue();
			String note = row.getCell(5).getValue();
			if (title.isEmpty()) {
				if (note.isEmpty()) {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelected(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue()));
				} else {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithNote(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue(), note));
				}
			} else {
				if (note.isEmpty()) {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithTitle(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue(), title));
				} else {
					AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithTitleAndNote(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue(), title, note));
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
			panel.getElement().getStyle().setWidth(780, Unit.PX);
			panel.getElement().getStyle().setHeight(200, Unit.PX);
			iCourseDetailsTabPanel.add(panel, detail.getName(), true);
			Character ch = UniTimeHeaderPanel.guessAccessKey(detail.getName());
			if (ch != null)
				iTabAccessKeys.put(ch, tabIndex);
			tabIndex++;
		}
		selectLastTab();
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
			iCourses.setSelectedRow(iCourses.getSelectedRow() + 1);
            scrollToSelectedRow();
            updateCourseDetails();
		} else if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
			iCourses.setSelectedRow(iCourses.getSelectedRow() == 0 ? iCourses.getRowsCount() - 1 : iCourses.getSelectedRow() - 1);
			scrollToSelectedRow();
			updateCourseDetails();
		} else if (event.isControlKeyDown() || event.isAltKeyDown()) {
			for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
				if (event.getNativeKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeKeyCode() == Character.toUpperCase(entry.getKey())) {
					iCourseDetailsTabPanel.selectTab(entry.getValue());
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
			if (tab >= 0 || tab < iCourseDetailsTabPanel.getTabCount() && tab != iCourseDetailsTabPanel.getSelectedTab())
				iCourseDetailsTabPanel.selectTab(tab);
			else
				iCourseDetailsTabPanel.selectTab(0);
		} catch (Exception e) {
			iCourseDetailsTabPanel.selectTab(0);
		}
	}

	@Override
	public void changeTip() {
		iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		selectLastTab();
	}
}
