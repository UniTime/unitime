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
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SubjectAreaInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPageRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Tomas Muller
 */
public class TeachingRequestsPage extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	private boolean iAssigned = true;
	private UniTimeHeaderPanel iFilterPanel;
	private ListBox iFilter;
	private TeachingRequestsTable iTable;
	
	public TeachingRequestsPage() {
		iAssigned = "true".equalsIgnoreCase(Location.getParameter("assigned"));
		if (iAssigned)
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAssignedTeachingRequests());
		else
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageUnassignedTeachingRequests());
		
		iFilterPanel = new UniTimeHeaderPanel(MESSAGES.propSubjectArea());
		iFilter = new ListBox();
		iFilter.setStyleName("unitime-TextBox");
		iFilterPanel.insertLeft(iFilter, false);
		iFilter.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iFilterPanel.setEnabled("search", iFilter.getSelectedIndex() > 0);
				iFilterPanel.setEnabled("csv", iFilter.getSelectedIndex() > 0);
				iFilterPanel.setEnabled("pdf", iFilter.getSelectedIndex() > 0);
			}
		});
		iFilter.getElement().getStyle().setMarginLeft(5, Unit.PX);
		
		iFilterPanel.addButton("search", MESSAGES.buttonSearch(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				search();
			}
		});
		iFilterPanel.setEnabled("search", false);

		iFilterPanel.addButton("csv", MESSAGES.buttonExportCSV(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("teaching-requests.csv");
			}
		});
		iFilterPanel.setEnabled("csv", false);
		iFilterPanel.addButton("pdf", MESSAGES.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("teaching-requests.pdf");
			}
		});
		iFilterPanel.setEnabled("pdf", false);
		addRow(iFilterPanel);
		
		iTable = new TeachingRequestsTable(iAssigned) {
			@Override
			protected void onAssignmentChanged(List<AssignmentInfo> assignments) {
				if (iTable.isVisible()) search();
			}
		};
		iTable.setVisible(false);
		addRow(iTable);
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		RPC.execute(new TeachingRequestsPagePropertiesRequest(), new AsyncCallback<TeachingRequestsPagePropertiesResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterPanel.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TeachingRequestsPagePropertiesResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setProperties(result);
				iFilter.clear();
				iFilter.addItem(MESSAGES.itemSelect(), "");
				iFilter.addItem(MESSAGES.itemAll(), "-1");
				iFilter.setSelectedIndex(result.getLastSubjectAreaId() != null && result.getLastSubjectAreaId() == -1l ? 1 : 0);
				for (SubjectAreaInterface s: result.getSubjectAreas()) {
					iFilter.addItem(s.getAbbreviation(), s.getId().toString());
					if (s.getId().equals(result.getLastSubjectAreaId()))
						iFilter.setSelectedIndex(iFilter.getItemCount() - 1);
				}
				iFilterPanel.setEnabled("search", iFilter.getSelectedIndex() > 0);
				iFilterPanel.setEnabled("csv", iFilter.getSelectedIndex() > 0);
				iFilterPanel.setEnabled("pdf", iFilter.getSelectedIndex() > 0);
			}
		});
	}
	
	void search() {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingTeachingRequests());
		RPC.execute(new TeachingRequestsPageRequest(iFilter.getSelectedIndex() <= 1 ? null : Long.valueOf(iFilter.getSelectedValue()), iAssigned), new AsyncCallback<GwtRpcResponseList<TeachingRequestInfo>>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterPanel.setErrorMessage(MESSAGES.failedToLoadTeachingRequests(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingRequests(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<TeachingRequestInfo> result) {
				LoadingWidget.getInstance().hide();
				iTable.populate(result);
				iTable.setVisible(true);
			}
		});
	}
	
	void export(String type) {
		RoomCookie cookie = RoomCookie.getInstance();
		String query = "output=" + type + "&assigned=" + iAssigned + (iFilter.getSelectedIndex() <= 1 ? "" : "&subjectId=" + Long.valueOf(iFilter.getSelectedValue())) +
				"&sort=" + InstructorCookie.getInstance().getSortTeachingRequestsBy(iAssigned) +
				"&columns=" + InstructorCookie.getInstance().getTeachingRequestsColumns(iAssigned) + 
				"&grid=" + (cookie.isGridAsText() ? "0" : "1") +
				"&vertical=" + (cookie.areRoomsHorizontal() ? "0" : "1") +
				(cookie.hasMode() ? "&mode=" + cookie.getMode() : "");
		RPC.execute(EncodeQueryRpcRequest.encode(query), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
			}
		});
	}
}
