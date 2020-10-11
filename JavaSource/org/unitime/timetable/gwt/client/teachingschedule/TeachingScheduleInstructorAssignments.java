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
package org.unitime.timetable.gwt.client.teachingschedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;

public class TeachingScheduleInstructorAssignments extends P {
	public static GwtResources RESOURCES = GWT.create(GwtResources.class);
	
	private TeachingSchedule iOffering; 
	private MeetingAssignment iAssignment;
	private List<UniTimeWidget<ListBox>> iInstructors = new ArrayList<UniTimeWidget<ListBox>>();
	private ChangeHandler iChange = null;
	
	public TeachingScheduleInstructorAssignments(TeachingSchedule offering, MeetingAssignment assignment) {
		addStyleName("unitime-TeachingScheduleInstructorAssignments");
		iOffering = offering;
		iAssignment = assignment;
		iChange = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (iAssignment.hasInstructors()) iAssignment.getInstructor().clear();
				for (UniTimeWidget<ListBox> i: iInstructors)
					if (i.getWidget().getSelectedIndex() > 0)
						iAssignment.addInstructor(Long.valueOf(i.getWidget().getSelectedValue()));
			}
		};
		if (assignment.hasInstructors()) {
			for (Iterator<Long> i = assignment.getInstructor().iterator(); i.hasNext(); ) {
				Long instructorId = i.next();
				UniTimeWidget<ListBox> instructor = addInstructorLine(instructorId);
				if (instructor.getWidget().getSelectedIndex() == 0) i.remove();
			}
		} else {
			addInstructorLine(null);
		}
	}
	
	public void assignmentChanged() {
		Iterator<Long> ids = (iAssignment.hasInstructors() ? iAssignment.getInstructor().iterator() : null);
		for (UniTimeWidget<ListBox> instructor: iInstructors) {
			instructor.getWidget().clear();
			instructor.getWidget().addItem("", "");
			Long id = (ids == null || !ids.hasNext() ? null : ids.next());
			boolean selected = false;
			if (iOffering.hasInstructors())
				for (Instructor i: iOffering.getInstructors()) {
					if (iAssignment.hasAttributeRef() && i.getAttribute(iAssignment.getAttributeRef()) == null) continue;
					instructor.getWidget().addItem(i.getName(), i.getInstructorId().toString());
					if (i.getInstructorId().equals(id)) {
						instructor.getWidget().setSelectedIndex(instructor.getWidget().getItemCount() - 1);
						selected = true;
					}
				}
			if (id != null && !selected) ids.remove();
		}
	}
	
	public UniTimeWidget<ListBox> addInstructorLine(Long instructorId) {
		final P line = new P("line");
		final UniTimeWidget<ListBox> instructor = new UniTimeWidget<ListBox>(new ListBox());
		instructor.getWidget().setMultipleSelect(false);
		instructor.getWidget().setWidth("150px");
		instructor.getWidget().setStyleName("unitime-TextBox");
		instructor.getWidget().addItem("", "");
		if (iOffering.hasInstructors())
			for (Instructor i: iOffering.getInstructors()) {
				if (iAssignment.hasAttributeRef() && i.getAttribute(iAssignment.getAttributeRef()) == null) continue;
				instructor.getWidget().addItem(i.getName(), i.getInstructorId().toString());
				if (i.getInstructorId().equals(instructorId))
					instructor.getWidget().setSelectedIndex(instructor.getWidget().getItemCount() - 1);
			}
		line.add(instructor);
		instructor.getWidget().addChangeHandler(iChange);
		iInstructors.add(instructor);
		if (iInstructors.size() == 1) {
			Image add = new Image(RESOURCES.add());
			add.getElement().getStyle().setCursor(Cursor.POINTER);
			add.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addInstructorLine(null);
				}
			});
			line.add(add);
		} else {
			Image delete = new Image(RESOURCES.delete());
			delete.getElement().getStyle().setCursor(Cursor.POINTER);
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (instructor.getWidget().getSelectedIndex() > 0)
						iAssignment.removeInstructor(Long.valueOf(instructor.getWidget().getSelectedValue()));
					iInstructors.remove(instructor);
					remove(line);
				}
			});
			line.add(delete);
		}
		add(line);
		return instructor;
	}

}
