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
package org.unitime.timetable.gwt.client.rooms;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingsDataResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.GetBuildingsRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateBuildingAction;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateBuildingRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class BuildingsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iPanel;
	private SimpleForm iListBuildingsForm;
	private UniTimeHeaderPanel iListBuildingsHeader, iListBuildingsFooter;
	private BuildingsTable iBuildingsTable;
	private BuildingsEdit iBuildingsEdit;
	
	public BuildingsPage() {
		iPanel = new SimplePanel();
		
		iListBuildingsForm = new SimpleForm();
		
		iListBuildingsHeader = new UniTimeHeaderPanel(MESSAGES.sectBuildings());
		iListBuildingsHeader.addButton("add", MESSAGES.buttonAddBuilding(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addBuilding();
			}
		});
		iListBuildingsHeader.setEnabled("add", false);
		iListBuildingsHeader.addButton("export", MESSAGES.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "export?output=buildings.pdf&sort=" + iBuildingsTable.getSortBy());
			}
		});
		iListBuildingsHeader.setEnabled("export", false);
		iListBuildingsHeader.addButton("updateData", MESSAGES.buttonBuildingsUpdateData(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UpdateBuildingRequest request = new UpdateBuildingRequest();
				request.setAction(UpdateBuildingAction.UPDATE_DATA);
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<BuildingInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iListBuildingsHeader.setErrorMessage(MESSAGES.failedBuildingUpdateData(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedBuildingUpdateData(caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(BuildingInterface result) {
						LoadingWidget.getInstance().hide();
					}
				});
			}
		});
		iListBuildingsHeader.setEnabled("updateData", false);
		iListBuildingsForm.addHeaderRow(iListBuildingsHeader);
		
		iBuildingsTable = new BuildingsTable();
		iListBuildingsForm.addRow(iBuildingsTable);
		iBuildingsTable.setAllowSelection(true); iBuildingsTable.setAllowMultiSelect(false);
		
		iListBuildingsFooter = iListBuildingsHeader.clonePanel("");
		iListBuildingsForm.addBottomRow(iListBuildingsFooter);
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(new GetBuildingsRequest(), new AsyncCallback<BuildingsDataResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
				iListBuildingsHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(BuildingsDataResponse result) {
				iListBuildingsHeader.setEnabled("add", result.isCanAdd());
				iListBuildingsHeader.setEnabled("export", result.isCanExportPDF());
				iListBuildingsHeader.setEnabled("updateData", result.isCanUpdateData());
				iBuildingsTable.setData(result.getBuildings());
				LoadingWidget.getInstance().hide();
				if (result.getEllipsoid() != null)
					iBuildingsEdit.setCoordinatesFormat(result.getEllipsoid());
			}
		});

		iBuildingsTable.addMouseClickListener(new MouseClickListener<BuildingInterface>() {
			@Override
			public void onMouseClick(TableEvent<BuildingInterface> event) {
				if (event.getData() != null && event.getData().isCanEdit()) {
					iBuildingsTable.setSelected(event.getRow(), true);
					editBuilding(event.getData());
				}
			}
		});
		
		iBuildingsEdit = new BuildingsEdit() {
			@Override
			protected void onBack(boolean refresh, final Long buildingId) {
				iPanel.setWidget(iListBuildingsForm);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageBuildings());
				if (refresh) {
					LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
					RPC.execute(new GetBuildingsRequest(), new AsyncCallback<BuildingsDataResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
							iListBuildingsHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
							ToolBox.checkAccess(caught);
						}

						@Override
						public void onSuccess(BuildingsDataResponse result) {
							iListBuildingsHeader.setEnabled("add", result.isCanAdd());
							iListBuildingsHeader.setEnabled("export", result.isCanExportPDF());
							iListBuildingsHeader.setEnabled("updateData", result.isCanUpdateData());
							iBuildingsTable.setData(result.getBuildings());
							LoadingWidget.getInstance().hide();
							if (result.getEllipsoid() != null)
								iBuildingsEdit.setCoordinatesFormat(result.getEllipsoid());
							if (buildingId != null)
								for (int i = 0; i < iBuildingsTable.getRowCount(); i++) {
									BuildingInterface b = iBuildingsTable.getData(i);
									if (b != null && b.getId().equals(buildingId)) {
										iBuildingsTable.getRowFormatter().getElement(i).scrollIntoView();
										iBuildingsTable.setSelected(i, true);
										break;
									}
								}
						}
					});
				} else {
					if (iBuildingsTable.getSelectedRow() >= 0)
						iBuildingsTable.setSelected(iBuildingsTable.getSelectedRow(), false);
					if (buildingId != null)
						for (int i = 0; i < iBuildingsTable.getRowCount(); i++) {
							BuildingInterface b = iBuildingsTable.getData(i);
							if (b != null && b.getId().equals(buildingId)) {
								iBuildingsTable.getRowFormatter().getElement(i).scrollIntoView();
								iBuildingsTable.setSelected(i, true);
								break;
							}
						}
				}
			}
			@Override
			protected boolean isAbbreviationUnique(BuildingInterface building) {
				for (int i = 0; i < iBuildingsTable.getRowCount(); i++) {
					BuildingInterface b = iBuildingsTable.getData(i);
					if (b != null && !b.getId().equals(building.getId()) && b.getAbbreviation().equalsIgnoreCase(building.getAbbreviation())) {
						return false;
					}
				}
				return true;
			}
		};
		
		iPanel.setWidget(iListBuildingsForm);
		initWidget(iPanel);
	}
	
	protected void addBuilding() {
		iBuildingsEdit.setValue(null);
		iPanel.setWidget(iBuildingsEdit);
		iBuildingsEdit.show();		
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddBuilding());
		iBuildingsTable.clearHover();
	}
	
	protected void editBuilding(BuildingInterface building) {
		iBuildingsEdit.setValue(building);
		iPanel.setWidget(iBuildingsEdit);
		iBuildingsEdit.show();
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEditBuilding());
		iBuildingsTable.clearHover();
	}

}
