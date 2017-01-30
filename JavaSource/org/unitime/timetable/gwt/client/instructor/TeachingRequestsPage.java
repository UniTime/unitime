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
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterPanel;
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
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SubjectAreaInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsFilterRpcRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPageRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

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
	private FilterPanel iFilterPanel;
	private TeachingRequestsFilterBox iFilterBox;
	private TeachingRequestsTable iTable;
	private Button iSearch, iExportCSV, iExportPDF;
	
	public TeachingRequestsPage() {
		iAssigned = "true".equalsIgnoreCase(Location.getParameter("assigned"));
		if (iAssigned)
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAssignedTeachingRequests());
		else
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageUnassignedTeachingRequests());
		
		iFilterPanel = new FilterPanel();
		
		Label filterLabel = new Label(MESSAGES.propSubjectArea());
		iFilterPanel.addLeft(filterLabel);
		
		iFilterBox = new TeachingRequestsFilterBox(iAssigned);
		iFilterPanel.addLeft(iFilterBox);
		
		iSearch = new Button(UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonSearch()));
		Character searchAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonSearch());
		if (searchAccessKey != null)
		iSearch.setAccessKey(searchAccessKey);
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iSearch);
		iSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				search();
			}
		});
		
		iExportCSV = new Button(UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonExportCSV()));
		Character exportCsvAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonExportCSV());
		if (exportCsvAccessKey != null)
			iExportCSV.setAccessKey(exportCsvAccessKey);
		iExportCSV.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iExportCSV);
		iExportCSV.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("teaching-requests.csv");
			}
		});
		
		iExportPDF = new Button(UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonExportPDF()));
		Character exportPdfAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonExportPDF());
		if (exportPdfAccessKey != null)
			iExportPDF.setAccessKey(exportCsvAccessKey);
		iExportPDF.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iExportPDF);
		iExportPDF.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("teaching-requests.pdf");
			}
		});
		addHeaderRow(iFilterPanel);
		
		iTable = new TeachingRequestsTable() {
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
				iFilterBox.setErrorHint(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TeachingRequestsPagePropertiesResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setProperties(result);
				Chip subject = iFilterBox.getChip("subject");
				if (subject != null) {
					boolean match = false;
					for (SubjectAreaInterface s: result.getSubjectAreas()) {
						if (s.getAbbreviation().equalsIgnoreCase(subject.getValue())) {
							match = true;
						}
					}
					if (!match) iFilterBox.setValue("", true);
				}
				if (result.getLastSubjectAreaId() != null && iFilterBox.getValue().isEmpty()) {
					for (SubjectAreaInterface s: result.getSubjectAreas()) {
						if (s.getId().equals(result.getLastSubjectAreaId())) {
							iFilterBox.setValue("subject:\"" + s.getAbbreviation() + "\"", true);
							break;
						}
					}
				}
			}
		});
		
		if (Window.Location.getHash() != null && Window.Location.getHash().length() > 1) {
			iFilterBox.setValue(URL.decode(Window.Location.getHash().substring(1)), true);
			search();
		} else {
			String q = InstructorCookie.getInstance().getQuery(iAssigned);
			if (q != null && !q.isEmpty())
				iFilterBox.setValue(q, true);
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() != null && !event.getValue().isEmpty()) {
					iFilterBox.setValue(event.getValue().replace("%20", " "), true);
					search();
				}
			}
		});
	}
	
	void search() {
		History.newItem(iFilterBox.getValue(), false);
		InstructorCookie.getInstance().setQuery(iAssigned, iFilterBox.getValue());
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingTeachingRequests());
		final TeachingRequestsFilterRpcRequest filter = iFilterBox.getElementsRequest();
		RPC.execute(new TeachingRequestsPageRequest(filter), new AsyncCallback<GwtRpcResponseList<TeachingRequestInfo>>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterBox.setErrorHint(MESSAGES.failedToLoadTeachingRequests(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadTeachingRequests(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<TeachingRequestInfo> result) {
				LoadingWidget.getInstance().hide();
				iTable.populate(result, filter);
				iTable.setVisible(true);
			}
		});
	}
	
	void export(String type) {
		RoomCookie cookie = RoomCookie.getInstance();
		String query = "output=" + type;
		FilterRpcRequest requests = iFilterBox.getElementsRequest();
		if (requests.hasOptions()) {
			for (Map.Entry<String, Set<String>> option: requests.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&r:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
		}
		if (requests.getText() != null && !requests.getText().isEmpty()) {
			query += "&r:text=" + URL.encodeQueryString(requests.getText());
		}
		query += "&sort=" + InstructorCookie.getInstance().getSortTeachingRequestsBy(iAssigned) +
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
