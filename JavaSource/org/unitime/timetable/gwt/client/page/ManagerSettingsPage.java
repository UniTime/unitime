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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.ListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class ManagerSettingsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private UniTimeTable<ManagerSettingInterface> iTable;
	private ManagerSettingInterface iSetting;
	private int iLastSort = 0;
	private boolean iLastAsc = true;
	
	public ManagerSettingsPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-ManagerSettingsPage");
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		initWidget(iPanel);

		iHeader = new UniTimeHeaderPanel(COURSE.sectionManagerSettings());
		iPanel.addHeaderRow(iHeader);
				
		iHeader.addButton("submit", COURSE.actionUpdateManagerSetting(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.showLoading();
				RPC.execute(new ManagerSettingsRequest(iSetting.getKey(), iSetting.getValue()), new AsyncCallback<GwtRpcResponseList<ManagerSettingInterface>>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(GwtRpcResponseList<ManagerSettingInterface> result) {
						populate(result);
					}
				});
				
				load();
			}
		});
		iHeader.getButton("submit").setTitle(COURSE.titleUpdateManagerSetting(COURSE.accessUpdateManagerSetting()));
		iHeader.getButton("submit").setAccessKey(COURSE.accessUpdateManagerSetting().charAt(0));
		iHeader.setEnabled("submit", false);
		
		iHeader.addButton("back", COURSE.actionBackToManagerSettings(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				load();
			}
		});
		iHeader.getButton("back").setTitle(COURSE.titleBackToManagerSettings(COURSE.accessBackToManagerSettings()));
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToManagerSettings().charAt(0));
		iHeader.setEnabled("back", false);
		
		iTable = new UniTimeTable<ManagerSettingInterface>();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(COURSE.columnManagerSettingKey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sort(0, !Boolean.TRUE.equals(iTable.getHeader(0).getOrder()));	
			}
		}));
		header.add(new UniTimeTableHeader(COURSE.columnManagerSettingValue(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sort(1, !Boolean.TRUE.equals(iTable.getHeader(1).getOrder()));	
			}
		}));
		iTable.addRow(null, header);
		iFooter = iHeader.clonePanel("");
		
		iTable.addMouseClickListener(new MouseClickListener<ManagerSettingInterface>() {
			@Override
			public void onMouseClick(TableEvent<ManagerSettingInterface> event) {
				if (event.getData() == null) return;
				iSetting = event.getData();
				UniTimePageLabel.getInstance().setPageName(MSG.pageEditManagerSetting());
				iHeader.setHeaderTitle(iSetting.getDescription());
				P options = new P("options");
				for (final ListItem item: iSetting.getOptions()) {
					RadioButton b = new RadioButton("options", item.getText() + (item.getValue().equals(iSetting.getDefaultValue()) ? " <i>" + COURSE.userSettingDefaultIndicator() + "</i>":""), true);
					b.setValue(item.getValue().equals(iSetting.getValue()));
					b.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue())
								iSetting.setValue(item.getValue());
						}
					});
					options.add(b);
				}
				iPanel.clear();
				iHeader.setEnabled("back", true);
				iHeader.setEnabled("submit", true);
				iPanel.addHeaderRow(iHeader);
				iPanel.addRow(options);
				iPanel.addBottomRow(iFooter);
			}
		});
		
		load();
	}
	
	protected void sort(int col, boolean asc) {
		iLastSort = col;
		iLastAsc = asc;
		iTable.sort(iTable.getHeader(iLastSort), new Comparator<ManagerSettingInterface>() {
			@Override
			public int compare(ManagerSettingInterface o1, ManagerSettingInterface o2) {
				if (iLastSort == 0)
					return o1.getDescription().compareTo(o2.getDescription());
				else
					return o1.getText(o1.getValue()).compareTo(o2.getText(o2.getValue()));
			}
		}, iLastAsc);
	}
	
	protected void load() {
		iHeader.showLoading();
		RPC.execute(new ManagerSettingsRequest(), new AsyncCallback<GwtRpcResponseList<ManagerSettingInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<ManagerSettingInterface> result) {
				populate(result);
			}
		});
	}
	
	protected void populate(GwtRpcResponseList<ManagerSettingInterface> result) {
		iPanel.clear();
		UniTimePageLabel.getInstance().setPageName(MSG.pageManagerSettings());
		iHeader.clearMessage();
		iHeader.setHeaderTitle(COURSE.sectionManagerSettings());
		iHeader.setEnabled("back", false);
		iHeader.setEnabled("submit", false);
		iPanel.addHeaderRow(iHeader);
		iTable.clearTable(1);
		for (ManagerSettingInterface m: result) {
			List<Widget> line = new ArrayList<Widget>();
			line.add(new Label(m.getDescription()));
			line.add(new Label(m.getText(m.getValue())));
			iTable.addRow(m, line);
		}
		sort(iLastSort, iLastAsc);
		iPanel.addRow(iTable);
	}

	
	public static class ManagerSettingsRequest implements GwtRpcRequest<GwtRpcResponseList<ManagerSettingInterface>> {
		private String iKey;
		private String iValue;
		
		public ManagerSettingsRequest() {}
		public ManagerSettingsRequest(String key, String value) {
			iKey = key;
			iValue = value;
		}
		
		public String getKey() { return iKey; }
		public void setKey(String key) { iKey = key; }
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
	}
	
	public static class ManagerSettingInterface implements IsSerializable {
		private Long iId;
		private String iKey;
		private String iValue;
		private String iDescription;
		private List<ListItem> iOptions;
		private String iDefaultValue;
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getKey() { return iKey; }
		public void setKey(String key) { iKey = key; }		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		public String getDescription() { return iDescription; }
		public void setDescription(String description) { iDescription = description; }
		
		public void addOption(String value, String text) {
			if (iOptions == null) iOptions = new ArrayList<ListItem>();
			iOptions.add(new ListItem(value, text));
		}
		public List<ListItem> getOptions() { return iOptions; }
		public boolean hasOptions() { return iOptions != null && !iOptions.isEmpty(); }
		
		public void setDefaultValue(String option) { iDefaultValue = option; }
		public String getDefaultValue() { return iDefaultValue; }
		
		public String getText(String option) {
			if (iOptions == null) return option;
			for (ListItem item: iOptions)
				if (item.getValue().equals(option)) return item.getText();
			return option;
		}
	}
}
