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
package org.unitime.timetable.gwt.client.instructor;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InstructorPreferencesEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InstructorPreferencesEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PreferenceEditWidget;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class InstructorPreferencesPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private InstructorPreferencesEditResponse iData;
	
	private PreferenceEditWidget iPreferences;
		
	public InstructorPreferencesPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ClassEditPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("instructorId");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoInstructorId());
		} else {
			load(Long.valueOf(id), Operation.GET, true, null);
		}
		
		iHeader.addButton("update", COURSE.actionUpdatePreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (validate()) {
					load(iData.getId(), Operation.UPDATE, true, null);
				}
			}
		});
		iHeader.getButton("update").setTitle(COURSE.titleUpdatePreferences(COURSE.accessUpdatePreferences()));
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdatePreferences().charAt(0));
		iHeader.setEnabled("update", false);

		iHeader.addButton("clear", COURSE.actionClearInstructorPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				load(iData.getId(), Operation.CLEAR_CLASS_PREFS, true, null);
			}
		});
		iHeader.getButton("clear").setTitle(COURSE.titleClearInstructorPreferences(COURSE.accessClearInstructorPreferences()));
		iHeader.getButton("clear").setAccessKey(COURSE.accessClearInstructorPreferences().charAt(0));
		iHeader.setEnabled("clear", false);
		
		iHeader.addButton("previous", COURSE.actionPreviousInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (validate()) {
					load(iData.getId(), Operation.PREVIOUS, true, null);
				}
			}
		});
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousInstructorWithUpdate(COURSE.accessPreviousInstructor()));
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousInstructor().charAt(0));
		iHeader.setEnabled("previous", false);
		
		iHeader.addButton("next", COURSE.actionNextInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (validate()) {
					load(iData.getId(), Operation.NEXT, true, null);
				}
			}
		});
		iHeader.getButton("next").setTitle(COURSE.titleNextInstructorWithUpdate(COURSE.accessNextInstructor()));
		iHeader.getButton("next").setAccessKey(COURSE.accessNextInstructor().charAt(0));
		iHeader.setEnabled("next", false);
		
		iHeader.addButton("back", COURSE.actionBackToDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructor?id=" + iData.getId());
			}
		});
		iHeader.getButton("back").setTitle(COURSE.titleBackToDetail(COURSE.accessBackToDetail()));
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToDetail().charAt(0));
		
		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long instructorId, final Operation op, final boolean showLoading, final Command command) {
		if (showLoading) LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		InstructorPreferencesEditRequest req = new InstructorPreferencesEditRequest();
		req.setOperation(op);
		if (op != null && iData != null) {
			iPreferences.update();
			req.setPayLoad(iData);
		}
		req.setId(instructorId);
		RPC.execute(req, new AsyncCallback<InstructorPreferencesEditResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				if (showLoading) LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final InstructorPreferencesEditResponse response) {
				iData = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				if (showLoading) LoadingWidget.getInstance().hide();
				
				if (op == Operation.DATE_PATTERN) {
					iPreferences.setValue(response);
					return;
				}
				
				iPanel.clear();
				iHeader.setHeaderTitle(response.getName());
				iPanel.addHeaderRow(iHeader);
				
				iPreferences = new PreferenceEditWidget(false);
				iPreferences.setValue(response);
				iPanel.addRow(iPreferences);
				
				iPanel.addBottomRow(iFooter);
				
				UniTimeNavigation.getInstance().refresh();
				
				iHeader.setEnabled("update", true);
				iHeader.setEnabled("previous", response.getPreviousId() != null);
				iHeader.setEnabled("next", response.getNextId() != null);
				iHeader.setEnabled("clear", response.canClearPrefs());
				
				if (command != null)
					command.execute();
			}
		});
	}
	
	public boolean validate() {
		iHeader.clearMessage();

		String error = iPreferences.validate();
		if (error != null) {
			iHeader.setErrorMessage(error);
			return false;
		}
		return true;
	}
}
