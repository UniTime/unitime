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

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingCheckCanDeleteRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateBuildingAction;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateBuildingRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

public class BuildingsEdit extends Composite implements TakesValue<BuildingInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private UniTimeWidget<TextBox> iName;
	private UniTimeWidget<TextBox> iAbbreviation;
	private TextBox iExternalId;
	private NumberBox iX, iY;
	private UniTimeWidget<P> iCoordinates;
	private P iCoordinatesFormat;
	private MapWidget iMap = null;
	private BuildingInterface iBuilding = null;
	private CheckBox iUpdateRoomCoordinates = null;
	
	public BuildingsEdit() {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-BuildingEdit");
		
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("save", MESSAGES.buttonSave(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) return;
				UpdateBuildingRequest request = new UpdateBuildingRequest();
				request.setAction(UpdateBuildingAction.CREATE);
				request.setBuilding(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<BuildingInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectBuilding(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedCreate(MESSAGES.objectBuilding(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(BuildingInterface result) {
						LoadingWidget.getInstance().hide();
						onBack(true, result.getId());
					}
				});
			}
		});
		iHeader.addButton("update", MESSAGES.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) return;
				UpdateBuildingRequest request = new UpdateBuildingRequest();
				request.setAction(UpdateBuildingAction.UPDATE);
				request.setBuilding(getValue());
				request.setUpdateRoomCoordinates(iUpdateRoomCoordinates.getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<BuildingInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedUpdate(MESSAGES.objectBuilding(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedUpdate(MESSAGES.objectBuilding(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(BuildingInterface result) {
						LoadingWidget.getInstance().hide();
						onBack(true, result.getId());
					}
				});
			}
		});
		iHeader.addButton("delete", MESSAGES.buttonDelete(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UpdateBuildingRequest request = new UpdateBuildingRequest();
				request.setAction(UpdateBuildingAction.DELETE);
				request.setBuilding(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<BuildingInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedDelete(MESSAGES.objectBuilding(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedDelete(MESSAGES.objectBuilding(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(BuildingInterface result) {
						LoadingWidget.getInstance().hide();
						onBack(true, null);
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onBack(false, iBuilding.getId());
			}
		});
		iForm.addHeaderRow(iHeader);
		
		iAbbreviation = new UniTimeWidget<TextBox>(new TextBox());
		iAbbreviation.getWidget().setStyleName("unitime-TextBox");
		iAbbreviation.getWidget().setMaxLength(20);
		iAbbreviation.getWidget().setWidth("100px");
		iAbbreviation.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iAbbreviation.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propAbbreviation(), iAbbreviation);
		
		iName = new UniTimeWidget<TextBox>(new TextBox());
		iName.getWidget().setStyleName("unitime-TextBox");
		iName.getWidget().setMaxLength(100);
		iName.getWidget().setWidth("600px");
		iName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iName.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propName(), iName);
		
		iExternalId = new TextBox();
		iExternalId.setStyleName("unitime-TextBox");
		iExternalId.setMaxLength(40);
		iExternalId.setWidth("200px");
		iForm.addRow(MESSAGES.propExternalId(), iExternalId);
		
		iX =new NumberBox();
		iX.setMaxLength(12);
		iX.setWidth("80px");
		iX.setDecimal(true);
		iX.setNegative(true);
		iX.addStyleName("number");
		iY = new NumberBox();
		iY.setMaxLength(12);
		iY.setWidth("80px");
		iY.setDecimal(true);
		iY.setNegative(true);
		iY.addStyleName("number");
		iX.getElement().setId("coordX");
		iY.getElement().setId("coordY");
		iCoordinates = new UniTimeWidget<P>(new P("coordinates"));
		iCoordinates.getWidget().add(iX);
		P comma = new P("comma"); comma.setText(", ");
		iCoordinates.getWidget().add(comma);
		iCoordinates.getWidget().add(iY);
		iCoordinatesFormat = new P("format");
		iCoordinates.getWidget().add(iCoordinatesFormat);
		iForm.addRow(MESSAGES.propCoordinates(), iCoordinates);
		
		iFooter = iHeader.clonePanel("");
		
		iUpdateRoomCoordinates = new CheckBox(MESSAGES.checkBuildingUpdateRoomCoordinates());
		iUpdateRoomCoordinates.setVisible(false);
		iUpdateRoomCoordinates.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
		iFooter.insertLeft(iUpdateRoomCoordinates, true);

		MapWidget.createWidget(iX, iY, new AsyncCallback<MapWidget>() {
			@Override
			public void onSuccess(MapWidget result) {
				iMap = result;
				if (iMap != null) {
					iMap.setEnabled(true);
					iForm.addRow(MESSAGES.propMap(), iMap);
				}
				iForm.addBottomRow(iFooter);
			}
			@Override
			public void onFailure(Throwable caught) {
				iForm.addBottomRow(iFooter);
			}
		});
		
		initWidget(iForm);
	}
	
	protected void onBack(boolean refresh, Long buildingId) {}
	
	protected boolean isAbbreviationUnique(BuildingInterface building) {
		return true;
	}
	
	public void show() {
		if (iMap != null) iMap.onShow();
	}
	
	public void setCoordinatesFormat(String format) {
		iCoordinatesFormat.setText(format);
	}

	@Override
	public void setValue(BuildingInterface building) {
		iName.clearHint();
		iAbbreviation.clearHint();
		iHeader.clearMessage();
		if (building == null) {
			iHeader.setHeaderTitle(MESSAGES.sectAddBuilding());
			iHeader.setEnabled("save", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
			iHeader.setEnabled("back", true);
			iName.getWidget().setText("");
			iAbbreviation.getWidget().setText("");
			iExternalId.setText("");
			iX.setValue((Number)null); iY.setValue((Number)null);
			if (iMap != null) iMap.setMarker();
			iBuilding = new BuildingInterface();
			iUpdateRoomCoordinates.setVisible(false);
		} else {
			iHeader.setHeaderTitle(MESSAGES.sectEditBuilding());
			iHeader.setEnabled("save", false);
			iHeader.setEnabled("update", true);
			iHeader.setEnabled("delete", false);
			RPC.execute(new BuildingCheckCanDeleteRequest(building.getId()), new AsyncCallback<GwtRpcResponseBoolean>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(GwtRpcResponseBoolean result) {
					iHeader.setEnabled("delete", result.getValue());
				}
			});
			iHeader.setEnabled("back", true);
			iName.getWidget().setText(building.getName() == null ? "" : building.getName());
			iAbbreviation.getWidget().setText(building.getAbbreviation() == null ? "" : building.getAbbreviation());
			iExternalId.setText(building.getExternalId() == null ? "" : building.getExternalId());
			iX.setValue(building.getX()); iY.setValue(building.getY());
			if (iMap != null) iMap.setMarker();
			iBuilding = building;
			iUpdateRoomCoordinates.setVisible(true);
		}
	}

	@Override
	public BuildingInterface getValue() {
		iBuilding.setName(iName.getWidget().getText());
		iBuilding.setAbbreviation(iAbbreviation.getWidget().getText());
		iBuilding.setExternalId(iExternalId.getText());
		iBuilding.setX(iX.toDouble());
		iBuilding.setY(iY.toDouble());
		return iBuilding;
	}
	
	protected boolean validate() {
		boolean ok = true;
		if (iAbbreviation.getWidget().getText().isEmpty()) {
			iAbbreviation.setErrorHint(MESSAGES.errorAbbreviationIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorAbbreviationIsEmpty());
			ok = false;
		} else if (!isAbbreviationUnique(getValue())) {
			iAbbreviation.setErrorHint(MESSAGES.errorAbbreviationMustBeUnique());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorAbbreviationMustBeUnique());
			ok = false;
		}
		if (iName.getWidget().getText().isEmpty()) {
			iName.setErrorHint(MESSAGES.errorNameIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorNameIsEmpty());
			ok = false;
		}
		return ok;
	}

}
