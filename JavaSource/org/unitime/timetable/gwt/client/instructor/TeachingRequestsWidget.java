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

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPageRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class TeachingRequestsWidget extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private UniTimeHeaderPanel iHeader;
	private TeachingRequestsTable iTable;
	private Long iOfferingId;
	
	public TeachingRequestsWidget() {
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectTeachingRequests());
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				InstructorCookie.getInstance().setShowTeachingRequests(event.getValue());
				if (iTable != null) {
					iTable.setVisible(event.getValue());
				} else if (event.getValue()) {
					refresh();
				}
			}
		});
		iHeader.setCollapsible(InstructorCookie.getInstance().isShowTeachingRequests());
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		removeStyleName("unitime-NotPrintableBottomLine");
		
		addHeaderRow(iHeader);
		iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
		
		iHeader.addButton("setup", MESSAGES.buttonSetupTeachingRequests(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=setupTeachingRequests&offeringId=" + iOfferingId);
			}
		});
	}
	
	public void insert(final RootPanel panel) {
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		if (InstructorCookie.getInstance().isShowTeachingRequests()) {
			refresh();
		}
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	protected void refresh() {
		iHeader.showLoading();
		if (iTable == null)
			init();
		else
			populate();
	}
	
	protected void init() {
		RPC.execute(new TeachingRequestsPagePropertiesRequest(), new AsyncCallback<TeachingRequestsPagePropertiesResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				iHeader.setCollapsible(null);
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TeachingRequestsPagePropertiesResponse result) {
				iTable = new TeachingRequestsTable() {
					@Override
					protected void onAssignmentChanged(List<AssignmentInfo> assignments) {
						if (iTable.isVisible()) refresh();
					}
				};
				iTable.setProperties(result);
				iTable.setVisible(false);
				addRow(iTable);
				populate();
			}
		});
	}
	
	protected void populate() {
		RPC.execute(new TeachingRequestsPageRequest(iOfferingId), new AsyncCallback<GwtRpcResponseList<TeachingRequestInfo>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setCollapsible(null);
				iHeader.setErrorMessage(MESSAGES.failedToLoadTeachingRequests(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingRequests(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<TeachingRequestInfo> result) {
				iHeader.clearMessage();
				LoadingWidget.getInstance().hide();
				iTable.populate(result);
				iTable.setVisible(true);
			}
		});
	}

}
