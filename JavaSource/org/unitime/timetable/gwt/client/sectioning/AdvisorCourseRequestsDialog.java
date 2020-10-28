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

import java.util.Iterator;

import org.unitime.timetable.gwt.client.sectioning.DegreePlanDialog.AssignmentProvider;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestsDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private UniTimeHeaderPanel iFooter;
	private TakesValue<CourseRequestInterface> iRequests;
	private AdvisorCourseRequestsTable iTable;
	private AssignmentProvider iAssignments;
	
	public AdvisorCourseRequestsDialog(TakesValue<CourseRequestInterface> requests, AssignmentProvider assignments) {
		super(true, false);
		setEscapeToHide(true);
		addStyleName("unitime-AdvisorCourseRequestsDialog");
		setText(MESSAGES.dialogAdvisorCourseRequests());
		iRequests = requests;
		iAssignments = assignments;
		
		iForm = new SimpleForm();
		
		iTable = new AdvisorCourseRequestsTable();
		
		ScrollPanel scroll = new ScrollPanel(iTable);
		scroll.setStyleName("unitime-ScrollPanel");
		scroll.addStyleName("requests");
		iForm.addRow(scroll);
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("apply", MESSAGES.buttonAdvisorRequestsApply(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doApply();
			}
		});
		
		iFooter.addButton("close", MESSAGES.buttonAdvisorRequestsClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm.addBottomRow(iFooter);
		setWidget(iForm);
	}
	
	protected void doApply() {
		hide();
		iRequests.setValue(createRequests());
	}
	
	protected CourseRequestInterface createRequests() {
		CourseRequestInterface requests = iRequests.getValue();
		for (Iterator<CourseRequestInterface.Request> i = requests.getCourses().iterator(); i.hasNext(); ) {
			CourseRequestInterface.Request request = i.next();
			if (!request.isCanDelete()) continue;
			if (!isLast(request)) i.remove();
		}
		for (Iterator<CourseRequestInterface.Request> i = requests.getAlternatives().iterator(); i.hasNext(); ) {
			CourseRequestInterface.Request request = i.next();
			if (!request.isCanDelete()) continue;
			if (isLast(request)) requests.getCourses().add(request);
			i.remove();
		}
		requests.applyAdvisorRequests(iTable.getValue());
		return requests;
	}
	
	protected boolean isLast(CourseRequestInterface.Request request) {
		for (RequestedCourse course: request.getRequestedCourse())
			if (isLast(course)) return true;
		return false;
	}
	
	protected boolean isLast(RequestedCourse course) {
		if (course == null || course.isEmpty()) return false;
		if (iAssignments != null && iAssignments.getLastAssignment() != null) {
			for (ClassAssignmentInterface.CourseAssignment c: iAssignments.getLastAssignment().getCourseAssignments())
				if (course.equals(c) && c.isAssigned())
					return true;
		}
		return false;
	}
	
	public void open(CourseRequestInterface requests) {
		iTable.setMode(requests.getWaitListMode());
		iTable.setValue(requests);
		center();
	}
}
