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
package org.unitime.timetable.gwt.client.page;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class SelectUserRolePage extends Composite {
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	SimpleForm iPanel = new SimpleForm();
	CheckBox iInactive;
	UniTimeHeaderPanel iHeader, iFooter;
	Label iMessage = null;
	
	public SelectUserRolePage() {
		iPanel.addStyleName("unitime-SelectUserRolePage");
		initWidget(iPanel);
		iInactive = new CheckBox(COURSE.checkIncludeInactiveSessions());
		iInactive.setValue("1".equals(ToolBox.getCookie("SelectUserRole.Inactive")));
		iInactive.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setCookie("SelectUserRole.Inactive", event.getValue() ? "1" : "0");
				populate(null);
			}
		});
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() != null)
					populate(event.getValue());
			}
		});
		iInactive.setVisible(false);
		iMessage = new Label();
		iMessage.addStyleName("unitime-Message");
		
		iHeader = new UniTimeHeaderPanel(COURSE.sectSelectAcademicSession());
		iPanel.addHeaderRow(iHeader);
		iFooter = new UniTimeHeaderPanel();
		iFooter.insertLeft(iInactive, false);
		
		populate(null);
	}
	
	protected void populate(String auth) {
		SelectUserRoleRequest request = new SelectUserRoleRequest();
		request.setTarget(Window.Location.getParameter("target"));
		request.setList("Y".equalsIgnoreCase(Window.Location.getParameter("list")));
		request.setIncludeInactive(iInactive.getValue());
		request.setAuthority(auth);
		iHeader.showLoading();
		RPC.execute(request, new AsyncCallback<SelectUserRoleReponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(SelectUserRoleReponse response) {
				if (response.hasUrl()) {
					if (response.getUrl().startsWith(GWT.getHostPageBaseURL()))
						ToolBox.open(response.getUrl());
					else if (response.getUrl().startsWith("/"))
						ToolBox.open(response.getUrl());
					else if (response.getUrl().matches("\\w+://.*"))
						ToolBox.open(response.getUrl());
					else
						ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				iHeader.clear();
				iPanel.clear();
				if (response.hasMessage()) {
					iMessage.setText(response.getMessage());
					iPanel.addRow(iMessage);
				}
				iPanel.addHeaderRow(iHeader);
				if (response.hasTable()) {
					iHeader.setHTML(response.getTable().getName());
					iPanel.addRow(new TableWidget(response.getTable()));
					iPanel.addBottomRow(iFooter);
				}
				if (response.hasPageName())
					UniTimePageLabel.getInstance().setPageName(response.getPageName());
				iInactive.setVisible(response.hasInactive());
			}
		});		
	}

	
	public static class SelectUserRoleRequest implements GwtRpcRequest<SelectUserRoleReponse>{
		private String iTarget;
		private boolean iList = false; 
		private boolean iInactive = false;
		private String iAuthority;
		
		public boolean isList() { return iList; }
		public void setList(boolean list) { iList = list; }
		public String getTarget() { return iTarget; }
		public void setTarget(String target) { iTarget = target; }
		public boolean hasTarget() { return iTarget != null && !iTarget.isEmpty(); }
		public void setIncludeInactive(boolean includeInactive) { iInactive = includeInactive; }
		public boolean isIncludeInactive() { return iInactive; }
		public void setAuthority(String authority) { iAuthority = authority; }
		public String getAuthority() { return iAuthority; }
		public boolean hasAuthority() { return iAuthority != null && !iAuthority.isEmpty(); }
	}
	
	public static class SelectUserRoleReponse implements GwtRpcResponse {
		public String iMessage;
		public String iPageName;
		public TableInterface iTable;
		public String iUrl;
		private boolean iHasInactive = false;
		
		public String getMessage() { return iMessage; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public void setMessage(String message) { iMessage = message; }
		public TableInterface getTable() { return iTable; }
		public boolean hasTable() { return iTable != null; }
		public void setTable(TableInterface table) { iTable = table; }
		public String getPageName() { return iPageName; }
		public void setPageName(String pageName) { iPageName = pageName; }
		public boolean hasPageName() { return iPageName != null && !iPageName.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setHasInactive(boolean hasInactive) { iHasInactive = hasInactive; }
		public boolean hasInactive() { return iHasInactive; }
	}
}
