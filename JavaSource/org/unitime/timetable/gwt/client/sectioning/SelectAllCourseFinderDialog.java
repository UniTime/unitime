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
import java.util.Collections;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDialog;
import org.unitime.timetable.gwt.client.widgets.CourseFinderMultipleCourses;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

/**
 * @author Tomas Muller
 */
public class SelectAllCourseFinderDialog extends CourseFinderDialog {
	AriaButton iFilterSelectAll = null;
	
	public SelectAllCourseFinderDialog() {
		iFilterSelectAll = new AriaButton(MESSAGES.buttonSelectAll());
		iFilterSelectAll.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CourseFinderTab tab = getSelectedTab();
				if (tab != null && tab instanceof CourseFinderMultipleCourses) {
					CourseFinderMultipleCourses courses = (CourseFinderMultipleCourses)tab;
					if (courses.isAllowMultiSelection()) {
						List<CourseAssignment> selected = new ArrayList<CourseAssignment>();
						for (CourseAssignment ca: courses.getCourses())
							if (ca.getSelection() != null) selected.add(ca);
						if (!selected.isEmpty()) {
							Collections.sort(selected);
							for (CourseAssignment ca: selected)
								courses.selectCourse(ca);
						}
					}
				}
				RequestedCourse rc = (RequestedCourse)(tab == null ? null : tab.getValue());
				if (rc != null) iFilter.setValue(rc.toString(CONSTANTS));
				hide();
				SelectionEvent.fire(SelectAllCourseFinderDialog.this, getValue());
			}
		});
		iFilterSelectAll.setEnabled(false);
		iFilterSelectAll.setVisible(false);
		
		P filterButton = new P("button");
		filterButton.add(iFilterSelectAll);
		iFilterPanel.insert(filterButton, 0);
	}
	
	public void checkSelectAll() {
		CourseFinderTab tab = getSelectedTab();
		if (tab != null && tab instanceof CourseFinderMultipleCourses) {
			CourseFinderMultipleCourses course = (CourseFinderMultipleCourses)tab;
			if (course.isAllowMultiSelection()) {
				int nrCourses = 0, nrSelect = 0;
				for (CourseAssignment ca: course.getCourses()) {
					nrCourses ++;
					if (ca.getSelection() != null) nrSelect++;
				}
				if (nrSelect > 0) {
					if (nrSelect == nrCourses) {
						iFilterSelectAll.setHTML(MESSAGES.buttonSelectAll());
					} else {
						iFilterSelectAll.setHTML(MESSAGES.buttonPickN(nrSelect));
					}
					iFilterSelectAll.setEnabled(true);
					iFilterSelectAll.setVisible(true);
					return;
				}
			}
			iFilterSelectAll.setEnabled(false);
			iFilterSelectAll.setVisible(false);
		} else {
			iFilterSelectAll.setEnabled(false);
			iFilterSelectAll.setVisible(false);
		}
	}
	
	@Override
	public void setTabs(CourseFinderTab... tabs) {
		super.setTabs(tabs);
		for (CourseFinderTab tab: tabs)
			if (tab instanceof CourseFinderMultipleCourses)
				tab.addResponseHandler(new ResponseHandler() {
					@Override
					public void onResponse(ResponseEvent event) {
						checkSelectAll();
					}
				});
		if (iTabPanel != null)
			iTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
				@Override
				public void onSelection(SelectionEvent<Integer> event) {
					checkSelectAll();
				}
			});
	}
}
