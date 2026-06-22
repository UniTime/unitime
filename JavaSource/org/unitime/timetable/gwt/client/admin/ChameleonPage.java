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
package org.unitime.timetable.gwt.client.admin;

import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.SearchableListBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.ListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

public class ChameleonPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ListBox iList;
	private Lookup iLookup;
	
	public ChameleonPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-ChameleonPage");
		initWidget(iPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		iHeader.addButton("submit", COURSE.actionChangeUser(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iList.getSelectedIndex() > 0) {
					iHeader.clearMessage();
					submit(iList.getSelectedValue(), iList.getSelectedItemText());
				} else {
					iHeader.setErrorMessage(COURSE.warnNoUser());
				}
			}
		});
		iHeader.getButton("submit").setTitle(COURSE.titleChangeUser(COURSE.accessChangeUser()));
		iHeader.getButton("submit").setAccessKey(COURSE.accessChangeUser().charAt(0));
		iHeader.setEnabled("submit", false);
		
		iFooter = iHeader.clonePanel();
		
		iHeader.showLoading();
		RPC.execute(new ChameleonRequest(), new AsyncCallback<ChameleonResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ChameleonResponse result) {
				iHeader.clearMessage();
				iList = new ListBox();
				iList.addItem("", "");
				if (result.hasManagers())
					for (ListItem item: result.getManagers()) {
						iList.addItem(item.getText(), item.getValue());
						if (item.getValue().equals(result.getExternalId()))
							iList.setSelectedIndex(iList.getItemCount() - 1);
					}
				iPanel.addRow(COURSE.propertyTimetableManager(), new SearchableListBox(iList));
				if (result.isCanLookup()) {
					iLookup = new Lookup();
					iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
						@Override
						public void onValueChange(ValueChangeEvent<PersonInterface> event) {
							if (event.getValue() != null)
								submit(event.getValue().getId(), event.getValue().getFormattedName());
						}
					});
					iLookup.setOptions("mustHaveExternalId");
					Button button = new Button(COURSE.actionLookupUser());
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iLookup.center();
						}
					});
					iPanel.addRow(COURSE.propertyOtherUser(), button);
				}
				iPanel.addBottomRow(iFooter);
				iHeader.setEnabled("submit", true);
			}
		});
	}
	
	protected void submit(String externalId, String name) {
		iHeader.setEnabled("submit", false);
		iHeader.showLoading();
		RPC.execute(new ChameleonRequest(externalId, name), new AsyncCallback<ChameleonResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ChameleonResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + "selectPrimaryRole");
			}
		});
	}
	
	public static class ChameleonRequest implements GwtRpcRequest<ChameleonResponse> {
		private String iExternalId;
		private String iName;
		
		public ChameleonRequest() {}
		public ChameleonRequest(String externalId, String name) { iExternalId = externalId; iName = name; }
		
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
	}
	public static class ChameleonResponse implements GwtRpcResponse {
		private boolean iCanLookup = false;
		private String iExternalId;
		private List<ListItem> iManagers;
		
		public boolean isCanLookup() { return iCanLookup; }
		public void setCanLookup(boolean canLookup) { iCanLookup = canLookup; }
		public List<ListItem> getManagers() { return iManagers; }
		public boolean hasManagers() { return iManagers != null && !iManagers.isEmpty(); }
		public void addManager(String externalId, String name) {
			if (iManagers == null) iManagers = new ArrayList<ListItem>();
			iManagers.add(new ListItem(externalId, name));
		}
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
	}
}
