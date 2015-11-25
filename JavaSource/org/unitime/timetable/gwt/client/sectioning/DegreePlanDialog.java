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

import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.CourseFinderCourseDetails;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeGroupInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreePlaceHolderInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class DegreePlanDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private DegreePlanTable iDegreePlanTable;
	private ScrollPanel iDegreePlanPanel;
	private CourseFinderCourseDetails[] iDetails = null;
	private UniTimeTabPanel iCourseDetailsTabPanel = null;
	private Button iBack;
	private UniTimeHeaderPanel iFooter;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private TakesValue<CourseRequestInterface> iRequests;
	
	public DegreePlanDialog(TakesValue<CourseRequestInterface> requests, AssignmentProvider assignments, CourseFinderCourseDetails... details) {
		super(true, false);
		setEscapeToHide(true);
		addStyleName("unitime-DegreePlanDialog");
		iRequests = requests;
		
		iForm = new SimpleForm();
		
		iDegreePlanTable = new DegreePlanTable(requests, assignments);
		iDegreePlanPanel = new ScrollPanel(iDegreePlanTable);
		iDegreePlanPanel.setStyleName("unitime-ScrollPanel");
		iDegreePlanPanel.addStyleName("plan");
		iForm.addRow(iDegreePlanPanel);
		iDegreePlanTable.addMouseClickListener(new UniTimeTable.MouseClickListener<Object>() {
			@Override
			public void onMouseClick(TableEvent<Object> event) {
				updateAriaStatus();
				updateCourseDetails(event.getData());
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
		iForm.addRow(iCourseDetailsTabPanel);
		iDetails = details;
		int tabIndex = 0;
		for (CourseFinderCourseDetails detail: iDetails) {
			ScrollPanel panel = new ScrollPanel(detail.asWidget());
			panel.setStyleName("unitime-ScrollPanel-inner");
			panel.getElement().getStyle().setWidth(786, Unit.PX);
			panel.getElement().getStyle().setHeight(200, Unit.PX);
			iCourseDetailsTabPanel.add(panel, detail.getName(), true);
			Character ch = UniTimeHeaderPanel.guessAccessKey(detail.getName());
			if (ch != null)
				iTabAccessKeys.put(ch, tabIndex);
			tabIndex++;
		}
		selectLastTab();
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("apply", MESSAGES.buttonDegreePlanApply(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doApply();
			}
		});
		
		iFooter.addButton("close", MESSAGES.buttonDegreePlanClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iBack = new AriaButton(MESSAGES.buttonDegreePlanBack());
		iBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doBack();
			}
		});
		Character backAck = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonDegreePlanBack());
		if (backAck != null)
			iBack.setAccessKey(backAck);
		ToolBox.setWhiteSpace(iBack.getElement().getStyle(), "nowrap");
		iFooter.getPanel().insert(iBack, 0);
		
		iForm.addBottomRow(iFooter);
		setWidget(iForm);
	}
	
	public void open(DegreePlanInterface plan, boolean hasBack) {
		iDegreePlanTable.setValue(plan);
		setText(MESSAGES.dialogDegreePlan(plan.getName()));
		iBack.setVisible(hasBack); iBack.setEnabled(hasBack);
		center();
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

	protected void doBack() {
		hide();
	}
	
	protected void doApply() {
		hide();
		iRequests.setValue(iDegreePlanTable.createRequests());
	}
	
    @Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
		if (event.getTypeInt() == Event.ONKEYUP && (event.getNativeEvent().getAltKey() || event.getNativeEvent().getCtrlKey())) {
			for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
				if (event.getNativeEvent().getKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeEvent().getKeyCode()  == Character.toUpperCase(entry.getKey())) {
					iCourseDetailsTabPanel.selectTab(entry.getValue());
				}
		}
		if (event.getTypeInt() == Event.ONKEYDOWN) {
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
				int row = iDegreePlanTable.getSelectedRow();
				if (row >= 0)
					iDegreePlanTable.setSelected(row, false);
				row --;
				if (row <= 0) row = iDegreePlanTable.getRowCount() - 1;
				iDegreePlanTable.setSelected(row, true);
				updateCourseDetails(iDegreePlanTable.getData(row));
				scrollToSelectedRow();
				updateAriaStatus();
			} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
				int row = iDegreePlanTable.getSelectedRow();
				if (row >= 0)
					iDegreePlanTable.setSelected(row, false);
				else
					row = 0;
				row ++;
				if (row >= iDegreePlanTable.getRowCount()) row = 1;
				iDegreePlanTable.setSelected(row, true);
				updateCourseDetails(iDegreePlanTable.getData(row));
				scrollToSelectedRow();
				updateAriaStatus();
			}
		}
		if (event.getTypeInt() == Event.ONKEYUP && (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_SPACE || event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)) {
			if (iDegreePlanTable.canChoose(iDegreePlanTable.getSelectedRow()))
				iDegreePlanTable.chooseRow(iDegreePlanTable.getSelectedRow(), true);
		}
	}
    
	protected void updateCourseDetails(Object data) {
		if (data == null || !(data instanceof CourseAssignment)) {
			if (iDetails != null)
				for (CourseFinderCourseDetails detail: iDetails) {
					detail.setValue(null);
				}
		} else {
			CourseAssignment course = (CourseAssignment)data;
			String courseName = MESSAGES.courseName(course.getSubject(), course.getCourseNbr());
			if (CONSTANTS.showCourseTitle() && course.hasTitle())
				courseName = MESSAGES.courseNameWithTitle(course.getSubject(), course.getCourseNbr(), course.getTitle());
			for (CourseFinderCourseDetails detail: iDetails)
				detail.setValue(courseName);
		}
	}
	
	public static interface AssignmentProvider {
		ClassAssignmentInterface getLastAssignment();
		ClassAssignmentInterface getSavedAssignment();
	}
	
	protected void scrollToSelectedRow() {
		if (iDegreePlanTable.getSelectedRow() < 0) return;
		
		Element scroll = iDegreePlanPanel.getElement();
		
		com.google.gwt.dom.client.Element item = iDegreePlanTable.getRowFormatter().getElement(iDegreePlanTable.getSelectedRow());
		if (item == null) return;
		
		int realOffset = 0;
		while (item !=null && !item.equals(scroll)) {
			realOffset += item.getOffsetTop();
			item = item.getOffsetParent();
		}
		
		scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
	}
	
	protected void updateAriaStatus() {
		int row = iDegreePlanTable.getSelectedRow();
		Object data = iDegreePlanTable.getData(row);
		String status = null;
		String name = null;
		if (data instanceof DegreePlaceHolderInterface) {
			DegreePlaceHolderInterface ph = (DegreePlaceHolderInterface)data;
			status = ARIA.degreePlaceholder(ph.getName());
			name = ph.getType();
		} else if (data instanceof DegreeGroupInterface) {
			DegreeGroupInterface group = (DegreeGroupInterface)data;
			if (group.isChoice())
				status = ARIA.degreeChoiceGroup(group.toString(MESSAGES));
			else
				status = ARIA.degreeUnionGroup(group.toString(MESSAGES));
			name = group.toString(MESSAGES);
		} else if (data instanceof DegreeCourseInterface) {
			DegreeCourseInterface course = (DegreeCourseInterface)data;
			if (course.hasCourses())
				status = ARIA.degreeCourseWithChoice(course.getCourseName(), course.getTitle(), course.getCourses().size());
			else
				status = ARIA.degreeCourseNotOffered(course.getCourseName(), course.getTitle());
			name = course.getCourseName();
		} else if (data instanceof CourseAssignment) {
			CourseAssignment course = (CourseAssignment)data;
			if (course.getNote() == null || course.getNote().isEmpty())
				status = ARIA.degreeCourse(course.getCourseName(), course.getTitle());
			else
				status = ARIA.degreeCourseWithNote(course.getCourseName(), course.getTitle(), course.getNote());
			name = course.getCourseName();
		}
		if (name != null) {
			Widget w = iDegreePlanTable.getWidget(row, 1);
			if (w != null && w instanceof RadioButton) {
				RadioButton radio = (RadioButton)w;
				if (radio.getValue())
					status += " " + ARIA.degreeCourseSelected(name);
				else
					status += " " + ARIA.degreeSpaceToSelectCourse(name);
			}
		}
		if (status != null)
			AriaStatus.getInstance().setText(ARIA.selectedLine(row, iDegreePlanTable.getRowCount() - 1) + " " + status);
	}
}
