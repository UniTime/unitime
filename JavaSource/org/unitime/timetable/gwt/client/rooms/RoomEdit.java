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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomTypeInterface;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

/**
 * @author Tomas Muller
 */
public class RoomEdit extends Composite {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);

	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private RoomPropertiesInterface iProperties = null;
	private RoomDetailInterface iRoom = null;
	
	private UniTimeWidget<ListBox> iType;
	private UniTimeWidget<ListBox> iBuilding;
	private int iBuildingRow;
	private Label iNameLabel;
	private UniTimeWidget<TextBox> iName;
	private UniTimeWidget<TextBox> iDisplayName, iExternalId;
	private UniTimeWidget<NumberBox> iCapacity, iExamCapacity;
	private UniTimeWidget<ListBox> iControllingDepartment;
	private NumberBox iX, iY;
	private UniTimeWidget<P> iCoordinates;
	private P iCoordinatesFormat;
	private UniTimeWidget<P> iAreaPanel;
	private NumberBox iArea;
	private P iAreaFormat;
	private UniTimeWidget<CheckBox> iDistanceCheck, iRoomCheck;
	private AbsolutePanel iGoogleMap;
	private AbsolutePanel iGoogleMapControl;
	private boolean iGoogleMapInitialized = false;
	private UniTimeWidget<ListBox> iEventDepartment;
	private UniTimeWidget<ListBox> iEventStatus;
	private UniTimeWidget<P> iBreakTimePanel;
	private NumberBox iBreakTime;
	private UniTimeWidget<TextArea> iNote;
	private UniTimeWidget<P> iExaminationRoomsPanel;
	private Map<Long, CheckBox> iExaminationRooms = new HashMap<Long, CheckBox>();
	private Map<Long, CheckBox> iGroups = new HashMap<Long, CheckBox>();
	private Map<Long, CheckBox> iFeatures = new HashMap<Long, CheckBox>();
	private int iGlobalGroupsRow;
	private UniTimeWidget<P> iGlobalGroupsPanel;
	private Map<Long, UniTimeWidget<P>> iGroupPanel = new HashMap<Long, UniTimeWidget<P>>();
	private int iFeaturesWithNoTypeRow;
	private UniTimeWidget<P> iFeaturesWithNoTypePanel;
	private Map<Long, UniTimeWidget<P>> iFeaturePanel = new HashMap<Long, UniTimeWidget<P>>();
	private Map<Long, Integer> iGroupRow = new HashMap<Long, Integer>();
	private Map<Long, Integer> iFeatureRow = new HashMap<Long, Integer>();
	private UniTimeHeaderPanel iRoomSharingHeader;
	private RoomSharingWidget iRoomSharing;
	private UniTimeHeaderPanel iPeriodPreferencesHeader;
	private int iPeriodPreferencesHeaderRow;
	private Map<Long, PeriodPreferencesWidget> iPeriodPreferences = new HashMap<Long, PeriodPreferencesWidget>();
	private Map<Long, Integer> iPeriodPreferencesRow = new HashMap<Long, Integer>();
	private UniTimeHeaderPanel iEventAvailabilityHeader;
	private RoomSharingWidget iEventAvailability;
	private UniTimeHeaderPanel iPicturesHeader;
	private UniTimeFileUpload iFileUpload;
	private UniTimeTable<RoomPictureInterface> iPictures;
	private int iPictureHeaderRow, iPictureUploadRow;
	
	public RoomEdit(RoomPropertiesInterface properties) {
		iProperties = properties;
		
		iHeader = new UniTimeHeaderPanel();
		ClickHandler clickCreateOrUpdate = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
			}
		};
		iHeader.addButton("create", MESSAGES.buttonCreateRoom(), 100, clickCreateOrUpdate);
		iHeader.addButton("update", MESSAGES.buttonUpdateRoom(), 100, clickCreateOrUpdate);
		iHeader.addButton("delete", MESSAGES.buttonDeleteRoom(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm = new SimpleForm(iProperties.isGoogleMap() ? 3 : 2);
		iForm.addStyleName("unitime-RoomEdit");
		iForm.addHeaderRow(iHeader);
		
		iType = new UniTimeWidget<ListBox>(new ListBox()); iType.getWidget().setStyleName("unitime-TextBox");
		int firstRow = iForm.addRow(MESSAGES.propRoomType(), iType, 1);
		iType.getWidget().addItem(MESSAGES.itemSelect(), "-1");
		for (RoomTypeInterface type: iProperties.getRoomTypes())
			iType.getWidget().addItem(type.getLabel(), type.getId().toString());
		iType.getWidget().setSelectedIndex(0);
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				typeChanged();
			}
		});
		
		iBuilding = new UniTimeWidget<ListBox>(new ListBox()); iBuilding.getWidget().setStyleName("unitime-TextBox");
		iBuildingRow = iForm.addRow(MESSAGES.propBuilding(), iBuilding, 1);
		iBuilding.getWidget().addItem(MESSAGES.itemSelect(), "-1");
		for (BuildingInterface building: iProperties.getBuildings())
			iBuilding.getWidget().addItem(building.getAbbreviation() + " - " + building.getName(), building.getId().toString());
		iBuilding.getWidget().setSelectedIndex(0);
		iBuilding.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				buildingChanged();
			}
		});
		iForm.getRowFormatter().setVisible(iBuildingRow, false);
		
		iName = new UniTimeWidget<TextBox>(new TextBox());
		iName.getWidget().setStyleName("unitime-TextBox");
		iName.getWidget().setMaxLength(20);
		iName.getWidget().setWidth("150px");
		iName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iName.clearHint();
				iHeader.clearMessage();
			}
		});
		iNameLabel = new Label(MESSAGES.propRoomName());
		iForm.addRow(iNameLabel, iName, 1);
		
		iDisplayName = new UniTimeWidget<TextBox>(new TextBox());
		iDisplayName.getWidget().setStyleName("unitime-TextBox");
		iDisplayName.getWidget().setMaxLength(100);
		iDisplayName.getWidget().setWidth("480px");
		iForm.addRow(MESSAGES.propDisplayName(), iDisplayName, 1);
		
		iExternalId = new UniTimeWidget<TextBox>(new TextBox());
		iExternalId.getWidget().setStyleName("unitime-TextBox");
		iExternalId.getWidget().setMaxLength(40);
		iExternalId.getWidget().setWidth("300px");
		iForm.addRow(MESSAGES.propExternalId(), iExternalId, 1);
		
		iCapacity = new UniTimeWidget<NumberBox>(new NumberBox());
		iCapacity.getWidget().setDecimal(false);
		iCapacity.getWidget().setNegative(false);
		iCapacity.getWidget().setMaxLength(6);
		iCapacity.getWidget().setWidth("80px");
		iForm.addRow(MESSAGES.propCapacity(), iCapacity, 1);
		
		iControllingDepartment = new UniTimeWidget<ListBox>(new ListBox());
		iControllingDepartment.getWidget().setStyleName("unitime-TextBox");
		iControllingDepartment.getWidget().addItem(MESSAGES.itemNoControlDepartment(), "-1");
		iForm.addRow(MESSAGES.propControllingDepartment(), iControllingDepartment, 1);
		for (DepartmentInterface department: iProperties.getDepartments())
			iControllingDepartment.getWidget().addItem(department.getExtAbbreviationOrCode() + " - " + department.getExtLabelWhenExist(), department.getId().toString());
		
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
		iCoordinatesFormat.setText(iProperties.getEllipsoid());
		iCoordinates.getWidget().add(iCoordinatesFormat);
		iForm.addRow(MESSAGES.propCoordinates(), iCoordinates, 1);
		if (iProperties.isGoogleMap()) {
			iX.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					setMarker();
				}
			});
			iY.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					setMarker();
				}
			});
		}
		
		iArea = new NumberBox();
		iArea.setDecimal(true);
		iArea.setNegative(false);
		iArea.addStyleName("number");
		iArea.setWidth("80px");
		iArea.setMaxLength(12);
		iAreaPanel = new UniTimeWidget<P>(new P("area"));
		iAreaPanel.getWidget().add(iArea);
		iAreaFormat = new P("format");
		iAreaFormat.setText(CONSTANTS.roomAreaUnitsLong());
		iAreaPanel.getWidget().add(iAreaFormat);
		iForm.addRow(MESSAGES.propRoomArea(), iAreaPanel, 1);
		
		iDistanceCheck = new UniTimeWidget<CheckBox>(new CheckBox());
		iForm.addRow(MESSAGES.propDistanceCheck(), iDistanceCheck, 1);
		
		iRoomCheck = new UniTimeWidget<CheckBox>(new CheckBox());
		iForm.addRow(MESSAGES.propRoomCheck(), iRoomCheck, 1);
		
		if (!iProperties.getExamTypes().isEmpty()) {
			iExaminationRoomsPanel = new UniTimeWidget<P>(new P("exams")); iExaminationRoomsPanel.setWidth("100%");
			for (final ExamTypeInterface type: iProperties.getExamTypes()) {
				final CheckBox ch = new CheckBox(type.getLabel());
				ch.addStyleName("exam");
				iExaminationRooms.put(type.getId(), ch);
				iExaminationRoomsPanel.getWidget().add(ch);
				ch.setValue(false);
				ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), event.getValue());
						boolean prefVisible = false;
						for (ExamTypeInterface t: iProperties.getExamTypes()) {
							if (iExaminationRooms.get(t.getId()).getValue()) { prefVisible = true; break; }
						}
						iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, prefVisible);
					}
				});
			}
			iForm.addRow(MESSAGES.propExamRooms(), iExaminationRoomsPanel, 1);
			
			iExamCapacity = new UniTimeWidget<NumberBox>(new NumberBox());
			iExamCapacity.getWidget().setDecimal(false);
			iExamCapacity.getWidget().setNegative(false);
			iExamCapacity.getWidget().setMaxLength(6);
			iExamCapacity.getWidget().setWidth("80px");
			iForm.addRow(MESSAGES.propExamCapacity(), iExamCapacity, 1);
		}
		
		iEventDepartment = new UniTimeWidget<ListBox>(new ListBox());
		iEventDepartment.getWidget().setStyleName("unitime-TextBox");
		iEventDepartment.getWidget().addItem(MESSAGES.itemNoEventDepartment(), "-1");
		iForm.addRow(MESSAGES.propEventDepartment(), iEventDepartment, 1);
		for (DepartmentInterface department: iProperties.getDepartments())
			if (department.isEvent())
				iEventDepartment.getWidget().addItem(department.getDeptCode() + " - " + department.getLabel(), department.getId().toString());
		
		iEventStatus = new UniTimeWidget<ListBox>(new ListBox());
		iEventStatus.getWidget().setStyleName("unitime-TextBox");
		iEventStatus.getWidget().addItem(MESSAGES.itemDefault(), "-1");
		iForm.addRow(MESSAGES.propEventStatus(), iEventStatus, 1);
		for (int i = 0; i < CONSTANTS.eventStatusName().length; i++)
			iEventStatus.getWidget().addItem(CONSTANTS.eventStatusName()[i], String.valueOf(i));
		
		iNote = new UniTimeWidget<TextArea>(new TextArea());
		iNote.getWidget().setStyleName("unitime-TextArea");
		iNote.getWidget().setVisibleLines(5);
		iNote.getWidget().setCharacterWidth(70);
		iForm.addRow(MESSAGES.propEventNote(), iNote);

		iBreakTime = new NumberBox();
		iBreakTime.setDecimal(false);
		iBreakTime.setNegative(false);
		iBreakTime.addStyleName("number");
		iBreakTime.setWidth("80px");
		iBreakTime.setMaxLength(12); 
		iBreakTimePanel = new UniTimeWidget<P>(new P("breaktime"));
		iBreakTimePanel.getWidget().add(iBreakTime);
		P f = new P("note");
		f.setText(MESSAGES.useDefaultBreakTimeWhenEmpty());
		iBreakTimePanel.getWidget().add(f);
		iForm.addRow(MESSAGES.propBreakTime(), iBreakTimePanel, 1);
		
		if (iProperties.isGoogleMap()) {
			iGoogleMap = new AbsolutePanel();
			iGoogleMap.setStyleName("map");
			iForm.setWidget(firstRow, 2, iGoogleMap);
			iForm.getFlexCellFormatter().setRowSpan(firstRow, 2, iForm.getRowCount() - firstRow - 1);
			
			iGoogleMapControl = new AbsolutePanel(); iGoogleMapControl.setStyleName("control");
			final TextBox searchBox = new TextBox();
			searchBox.setStyleName("unitime-TextBox"); searchBox.addStyleName("searchBox");
			searchBox.getElement().setId("mapSearchBox");
			searchBox.setTabIndex(-1);
			iGoogleMapControl.add(searchBox);
			Button button = new Button(MESSAGES.buttonGeocode(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					geocodeAddress();
				}
			});
			button.setTabIndex(-1);
			searchBox.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_ENTER:
	            		event.preventDefault();
	            		geocodeAddress();
	            		return;
					}
				}
			});
			button.addStyleName("geocode");
			ToolBox.setWhiteSpace(button.getElement().getStyle(), "nowrap");
			Character accessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonGeocode());
			if (accessKey != null)
				button.setAccessKey(accessKey);
			iGoogleMapControl.add(button);
			
			iGoogleMap.add(iGoogleMapControl);

			addGoogleMap(iGoogleMap.getElement(), iGoogleMapControl.getElement());
		}
		
		iForm.addHeaderRow(MESSAGES.headerRoomGroups());
		if (!iProperties.getGroups().isEmpty()) {
			P groups = new P("groups");
			for (GroupInterface group: iProperties.getGroups()) {
				CheckBox ch = new CheckBox(group.getLabel());
				ch.addStyleName("group");
				iGroups.put(group.getId(), ch);
				if (group.getDepartment() != null) {
					UniTimeWidget<P> d = iGroupPanel.get(group.getDepartment().getId());
					if (d == null) {
						d = new UniTimeWidget<P>(new P("groups"));
						d.setWidth("100%");
						iGroupPanel.put(group.getDepartment().getId(), d);
					}
					d.getWidget().add(ch);
				} else {
					groups.add(ch);
				}
			}
			if (groups.getWidgetCount() > 0) {
				iGlobalGroupsPanel = new UniTimeWidget<P>(groups);
				iGlobalGroupsPanel.setWidth("100%");
				iGlobalGroupsRow = iForm.addRow(MESSAGES.propGlobalGroups(), iGlobalGroupsPanel);
			}
			for (DepartmentInterface dept: iProperties.getDepartments()) {
				UniTimeWidget<P> d = iGroupPanel.get(dept.getId());
				if (d != null)
					iGroupRow.put(dept.getId(), iForm.addRow(dept.getExtLabelWhenExist() + ":", d));
			}
		}
		
		iForm.addHeaderRow(MESSAGES.headerRoomFeatures());
		if (!iProperties.getFeatures().isEmpty()) {
			P features = new P("features");
			for (FeatureInterface feature: iProperties.getFeatures()) {
				CheckBox ch = new CheckBox(feature.getTitle());
				ch.addStyleName("feature");
				iFeatures.put(feature.getId(), ch);
				if (feature.getType() != null) {
					UniTimeWidget<P> d = iFeaturePanel.get(feature.getType().getId());
					if (d == null) {
						d = new UniTimeWidget<P>(new P("features"));
						d.setWidth("100%");
						iFeaturePanel.put(feature.getType().getId(), d);
					}
					d.getWidget().add(ch);
				} else {
					features.add(ch);
				}
			}
			if (features.getWidgetCount() > 0) {
				iFeaturesWithNoTypePanel = new UniTimeWidget<P>(features);
				iFeaturesWithNoTypePanel.setWidth("100%");
				iFeaturesWithNoTypeRow = iForm.addRow(MESSAGES.propFeatures(), iFeaturesWithNoTypePanel);
			}
			for (FeatureTypeInterface type: iProperties.getFeatureTypes()) {
				UniTimeWidget<P> d = iFeaturePanel.get(type.getId());
				if (d != null)
					iFeatureRow.put(type.getId(), iForm.addRow(type.getLabel() + ":", d));
			}
		}
		
		iRoomSharingHeader = new UniTimeHeaderPanel(MESSAGES.headerRoomSharing());
		iForm.addHeaderRow(iRoomSharingHeader);
		iRoomSharing = new RoomSharingWidget(true, true);
		iForm.addRow(iRoomSharing);

		iPeriodPreferencesHeader = new UniTimeHeaderPanel(MESSAGES.headerExaminationPeriodPreferences());
		iPeriodPreferencesHeaderRow = iForm.addHeaderRow(iPeriodPreferencesHeader);
		for (ExamTypeInterface type: iProperties.getExamTypes()) {
			PeriodPreferencesWidget pref = new PeriodPreferencesWidget(true);
			iPeriodPreferences.put(type.getId(), pref);
			int row = iForm.addRow(MESSAGES.propExaminationPreferences(type.getLabel()), pref);
			iPeriodPreferencesRow.put(type.getId(), row);
			iForm.getRowFormatter().setVisible(row, false);
		}
		iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, false);
		
		iEventAvailabilityHeader = new UniTimeHeaderPanel(MESSAGES.headerEventAvailability());
		iForm.addHeaderRow(iEventAvailabilityHeader);
		iEventAvailability = new RoomSharingWidget(true);
		iForm.addRow(iEventAvailability);
		
		iPicturesHeader = new UniTimeHeaderPanel(MESSAGES.headerRoomPictures());
		iPictureHeaderRow = iForm.addHeaderRow(iPicturesHeader);
		
		iPictures = new UniTimeTable<RoomPictureInterface>();
		iPictures.setStyleName("unitime-RoomPictures");
		
		iFileUpload = new UniTimeFileUpload();
		iPictureUploadRow = iForm.addRow(MESSAGES.propNewPicture(), iFileUpload);
		iFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				RPC.execute(RoomPictureRequest.upload(iRoom.getUniqueId()), new AsyncCallback<RoomPictureResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedToUploadRoomPicture(caught.getMessage()));
					}
					
					@Override
					public void onSuccess(RoomPictureResponse result) {
						if (result.hasPictures()) {
							for (final RoomPictureInterface picture: result.getPictures()) {
								for (int row = 1; row < iPictures.getRowCount(); row ++)
									if (picture.getName().equals(iPictures.getData(row).getName())) {
										iPictures.removeRow(row);
										break;
									}
								iPictures.addRow(picture, line(picture));
							}
							iFileUpload.reset();
						}
					}
				});
			}
		});
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colPicture()));
		header.add(new UniTimeTableHeader(MESSAGES.colName()));
		header.add(new UniTimeTableHeader(MESSAGES.colType()));
		header.add(new UniTimeTableHeader("&nbsp;"));
		iPictures.addRow(null, header);
		
		iForm.addRow(iPictures);

		iFooter = iHeader.clonePanel();
		iForm.addBottomRow(iFooter);
		
		initWidget(iForm);
	}
	
	public void setRoom(RoomDetailInterface room) {
		iRoom = room;
		if (iRoom == null) {
			iRoom = new RoomDetailInterface();
			iHeader.setEnabled("create", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
		} else {
			iHeader.setEnabled("create", false);
			iHeader.setEnabled("update", true);
			iHeader.setEnabled("delete", iRoom.isCanDelete());
		}
		
		if (iRoom.getRoomType() == null) {
			iType.getWidget().clear();
			iType.getWidget().addItem(MESSAGES.itemSelect(), "-1");
			for (RoomTypeInterface type: iProperties.getRoomTypes()) {
				if (type.isRoom() && iProperties.isCanAddRoom())
					iType.getWidget().addItem(type.getLabel(), type.getId().toString());
				else if (!type.isRoom() && iProperties.isCanAddNonUniversity())
					iType.getWidget().addItem(type.getLabel(), type.getId().toString());
			}
			iType.getWidget().setSelectedIndex(0);
		} else {
			iType.getWidget().clear();
			for (RoomTypeInterface type: iProperties.getRoomTypes()) {
				if (type.isRoom() && iRoom.getBuilding() != null)
					iType.getWidget().addItem(type.getLabel(), type.getId().toString());
				else if (!type.isRoom() && iRoom.getBuilding() == null)
					iType.getWidget().addItem(type.getLabel(), type.getId().toString());
			}
			for (int i = 0; i < iType.getWidget().getItemCount(); i++) {
				if (iType.getWidget().getValue(i).equals(iRoom.getRoomType().getId().toString())) {
					iType.getWidget().setSelectedIndex(i); break;
				}
			}
		}
		typeChanged();
		if (iRoom.getRoomType() == null || iRoom.isCanChangeType()) {
			iType.setReadOnly(false);
		} else {
			iType.setReadOnly(true);
			iType.setText(iRoom.getRoomType().getLabel());
		}

		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			if (iRoom.getBuilding() == null) {
				iBuilding.getWidget().setSelectedIndex(0);
			} else {
				iBuilding.getWidget().setSelectedIndex(1 + iProperties.getBuildings().indexOf(iRoom.getBuilding()));
			}
			iBuilding.setReadOnly(false);
		} else {
			iBuilding.setText(iRoom.getBuilding() == null ? "" : iRoom.getBuilding().getAbbreviation() + " - " + iRoom.getBuilding().getName());
			iBuilding.setReadOnly(true);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iName.getWidget().setText(iRoom.getName() == null ? "" : iRoom.getName());
			iName.setReadOnly(false);
		} else {
			iName.setText(iRoom.getName() == null ? "" : iRoom.getName());
			iName.setReadOnly(true);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iDisplayName.getWidget().setText(iRoom.getDisplayName() == null ? "" : iRoom.getDisplayName());
			iDisplayName.setReadOnly(false);
		} else {
			iDisplayName.setText(iRoom.getDisplayName() == null ? "" : iRoom.getDisplayName());
			iDisplayName.setReadOnly(true);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExternalId()) || iRoom.isCanChangeExternalId()) {
			iExternalId.getWidget().setText(iRoom.getExternalId() == null ? "" : iRoom.getExternalId());
			iExternalId.setReadOnly(false);
		} else {
			iExternalId.setText(iRoom.getExternalId() == null ? "" : iRoom.getExternalId());
			iExternalId.setReadOnly(true);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeCapacity()) {
			iCapacity.getWidget().setValue(iRoom.getCapacity());
			iCapacity.setReadOnly(false);
		} else {
			iCapacity.setText(iRoom.getCapacity() == null ? "" : iRoom.getCapacity().toString());
			iCapacity.setReadOnly(true);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeControll()) || iRoom.isCanChangeControll()) {
			if (iRoom.getControlDepartment() == null) {
				iControllingDepartment.getWidget().setSelectedIndex(0);
			} else {
				iControllingDepartment.getWidget().setSelectedIndex(1 + iProperties.getDepartments().indexOf(iRoom.getControlDepartment()));
			}
			iControllingDepartment.setReadOnly(false);
		} else {
			iControllingDepartment.setText(iRoom.getControlDepartment() == null ? "" : iRoom.getControlDepartment().getExtAbbreviationOrCode() + " - " + iRoom.getControlDepartment().getExtLabelWhenExist());
			iControllingDepartment.setReadOnly(true);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iX.setValue(iRoom.getX());
			iY.setValue(iRoom.getY());
			iCoordinates.setReadOnly(false);
			iArea.setValue(iRoom.getArea());
			iAreaPanel.setReadOnly(false);
			iDistanceCheck.getWidget().setValue(!iRoom.isIgnoreTooFar());
			iDistanceCheck.setReadOnly(false);
			iRoomCheck.getWidget().setValue(!iRoom.isIgnoreRoomCheck());
			iRoomCheck.setReadOnly(false);
			if (iGoogleMapControl != null) iGoogleMapControl.setVisible(true);
		} else {
			iX.setValue(iRoom.getX());
			iY.setValue(iRoom.getY());
			if (!iRoom.hasCoordinates())
				iCoordinates.setText("");
			else if (iProperties.hasEllipsoid())
				iCoordinates.setText(MESSAGES.coordinatesWithEllipsoid(iRoom.getX(), iRoom.getY(), iProperties.getEllipsoid()));
			else
				iCoordinates.setText(MESSAGES.coordinates(iRoom.getX(), iRoom.getY()));
			iCoordinates.setReadOnly(true);
			iAreaPanel.setText(iRoom.getArea() == null ? "" : MESSAGES.roomArea(iRoom.getArea()) + " " + CONSTANTS.roomAreaUnitsLong());
			iAreaPanel.setReadOnly(true);
			iDistanceCheck.setReadOnlyWidget(new Image(!iRoom.isIgnoreTooFar() ? RESOURCES.on() : RESOURCES.off()));
			iDistanceCheck.setReadOnly(true);
			iRoomCheck.setReadOnlyWidget(new Image(!iRoom.isIgnoreRoomCheck() ? RESOURCES.on() : RESOURCES.off()));
			iRoomCheck.setReadOnly(true);
			if (iGoogleMapControl != null) iGoogleMapControl.setVisible(false);
		}
		
		for (Map.Entry<Long, CheckBox> e: iExaminationRooms.entrySet()) {
			e.getValue().setValue(false);
			iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(e.getKey()), false);
		}
		iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, false);
		if (iRoom.hasExamTypes()) {
			for (ExamTypeInterface type: iRoom.getExamTypes()) {
				iExaminationRooms.get(type.getId()).setValue(true);
				iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, true);
			}
		}
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExamStatus()) || iRoom.isCanChangeExamStatus()) {
			iExaminationRoomsPanel.setReadOnly(false);			
		} else {
			String types = "";
			for (ExamTypeInterface type: iRoom.getExamTypes())
				types += (types.isEmpty() ? "" : ", ") + type.getLabel();
			iExaminationRoomsPanel.setText(types);
			iExaminationRoomsPanel.setReadOnly(true);
		}
		
		iPeriodPreferencesHeader.clearMessage();
		for (final ExamTypeInterface type: iProperties.getExamTypes()) {
			final PeriodPreferencesWidget pref = iPeriodPreferences.get(type.getId());
			if (iRoom.hasPeriodPreferenceModel(type.getId())) {
				pref.setEditable(iProperties.isCanEditRoomExams());
				pref.setModel(iRoom.getPeriodPreferenceModel(type.getId()));
				iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), iExaminationRooms.get(type.getId()).getValue());
			} else {
				iPeriodPreferencesHeader.showLoading();
				iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), false);
				RPC.execute(RoomInterface.PeriodPreferenceRequest.load(iRoom.getUniqueId(), type.getId()), new AsyncCallback<PeriodPreferenceModel>() {
					@Override
					public void onFailure(Throwable caught) {
						iPeriodPreferencesHeader.setErrorMessage(MESSAGES.failedToLoadPeriodPreferences(caught.getMessage()));
					}

					@Override
					public void onSuccess(PeriodPreferenceModel result) {
						iPeriodPreferencesHeader.clearMessage();
						pref.setEditable(iProperties.isCanEditRoomExams());
						pref.setModel(result);
						iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), iExaminationRooms.get(type.getId()).getValue());
					}
				});
			}
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExamStatus()) || iRoom.isCanChangeExamStatus()) {
			iExamCapacity.getWidget().setValue(iRoom.getExamCapacity());
			iExamCapacity.setReadOnly(false);
		} else {
			iExamCapacity.setText(iRoom.getExamCapacity() == null ? "" : iRoom.getExamCapacity().toString());
			iExamCapacity.setReadOnly(true);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeEventProperties()) || iRoom.isCanChangeEventProperties()) {
			if (iRoom.getEventDepartment() == null) {
				iEventDepartment.getWidget().setSelectedIndex(0);
			} else {
				iEventDepartment.getWidget().setSelectedIndex(0);
				for (int i = 1; i < iEventDepartment.getWidget().getItemCount(); i++) {
					if (iEventDepartment.getWidget().getValue(i).equals(iRoom.getEventDepartment().getId().toString())) {
						iEventDepartment.getWidget().setSelectedIndex(i); break;
					}
				}
			}
			iEventDepartment.setReadOnly(false);
			iEventStatus.getWidget().setSelectedIndex(iRoom.getEventStatus() == null ? 0 : iRoom.getEventStatus());
			iEventStatus.setReadOnly(false);
			iNote.getWidget().setText(iRoom.getEventNote() == null ? "" : iRoom.getEventNote());
			iNote.setReadOnly(false);
			iBreakTime.setValue(iRoom.getBreakTime());
			iBreakTimePanel.setReadOnly(false);
		} else {
			iEventDepartment.setText(iRoom.getEventDepartment() == null ? null : iRoom.getEventDepartment().getDeptCode() + " - " + iRoom.getEventDepartment().getLabel());
			iEventDepartment.setReadOnly(true);
			iEventStatus.setText(iRoom.getEventStatus() == null ? "" : CONSTANTS.eventStatusName()[iRoom.getEventStatus()]);
			iEventStatus.setReadOnly(true);
			iEventStatus.getReadOnlyWidget().removeStyleName("default");
			if (iRoom.getEventStatus() == null && iRoom.getDefaultEventStatus() != null) {
				iEventStatus.setText(CONSTANTS.eventStatusName()[iRoom.getDefaultEventStatus()]);
				iEventStatus.getReadOnlyWidget().addStyleName("default");
			}
			iNote.setText(iRoom.getEventNote() == null ? "" : iRoom.getEventNote());
			iNote.getReadOnlyWidget().removeStyleName("default");
			if (iRoom.getEventNote() == null && iRoom.getDefaultEventNote() != null) {
				iNote.setText(iRoom.getDefaultEventNote());
				iNote.getReadOnlyWidget().addStyleName("default");
			}
			iNote.setReadOnly(true);
			iBreakTimePanel.setText(iRoom.getBreakTime() == null ? "" : iRoom.getBreakTime().toString());
			iBreakTimePanel.getReadOnlyWidget().removeStyleName("default");
			if (iRoom.getBreakTime() == null && iRoom.getDefaultBreakTime() != null) {
				iBreakTimePanel.setText(iRoom.getDefaultBreakTime().toString()); iBreakTimePanel.addStyleName("default");
			}
			iBreakTimePanel.setReadOnly(true);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeGroups()) || iRoom.isCanChangeGroups()) {
			for (Map.Entry<Long, CheckBox> e: iGroups.entrySet())
				e.getValue().setValue(iRoom.hasGroup(e.getKey()));
			if (iGlobalGroupsPanel != null) {
				iGlobalGroupsPanel.setReadOnly(false);
				iForm.getRowFormatter().setVisible(iGlobalGroupsRow, true);
			}
			for (UniTimeWidget<P> panel: iGroupPanel.values())
				panel.setReadOnly(false);
			for (Integer row: iGroupRow.values())
				iForm.getRowFormatter().setVisible(row, true);
		} else {
			if (iGlobalGroupsPanel != null) {
				List<GroupInterface> groups = iRoom.getGlobalGroups();
				iGlobalGroupsPanel.setReadOnlyWidget(new RoomDetail.GroupsCell(groups));
				iGlobalGroupsPanel.setReadOnly(true);
				iForm.getRowFormatter().setVisible(iGlobalGroupsRow, !groups.isEmpty());
			}
			for (Map.Entry<Long, UniTimeWidget<P>> e: iGroupPanel.entrySet()) {
				List<GroupInterface> groups = iRoom.getDepartmentalGroups(e.getKey());
				e.getValue().setReadOnlyWidget(new RoomDetail.GroupsCell(groups, false));
				e.getValue().setReadOnly(true);
				iForm.getRowFormatter().setVisible(iGroupRow.get(e.getKey()), !groups.isEmpty());
			}
		}
		
		if ((iRoom.getUniqueId() == null && iRoom.isCanChangeFeatures()) || iRoom.isCanChangeFeatures()) {
			for (Map.Entry<Long, CheckBox> e: iFeatures.entrySet())
				e.getValue().setValue(iRoom.hasFeature(e.getKey()));
			if (iFeaturesWithNoTypePanel != null) {
				iFeaturesWithNoTypePanel.setReadOnly(false);
				iForm.getRowFormatter().setVisible(iFeaturesWithNoTypeRow, true);
			}
			for (UniTimeWidget<P> panel: iFeaturePanel.values())
				panel.setReadOnly(false);
			for (Integer row: iFeatureRow.values())
				iForm.getRowFormatter().setVisible(row, true);
		} else {
			if (iFeaturesWithNoTypePanel != null) {
				List<FeatureInterface> features = iRoom.getFeatures((Long)null);
				iFeaturesWithNoTypePanel.setReadOnlyWidget(new RoomDetail.FeaturesCell(features));
				iFeaturesWithNoTypePanel.setReadOnly(true);
				iForm.getRowFormatter().setVisible(iFeaturesWithNoTypeRow, !features.isEmpty());
			}
			for (Map.Entry<Long, UniTimeWidget<P>> e: iFeaturePanel.entrySet()) {
				List<FeatureInterface> features = iRoom.getFeatures(e.getKey());
				e.getValue().setReadOnlyWidget(new RoomDetail.FeaturesCell(features));
				e.getValue().setReadOnly(true);
				iForm.getRowFormatter().setVisible(iFeatureRow.get(e.getKey()), !features.isEmpty());
			}
		}
		
		if (iRoom.hasRoomSharingModel()) {
			iRoomSharingHeader.clearMessage();
			iRoomSharing.setEditable(iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability());
			iRoomSharing.setModel(iRoom.getRoomSharingModel());
			iRoomSharing.setVisible(true);
		} else {
			iRoomSharingHeader.showLoading();
			iRoomSharing.setVisible(false);
			RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getUniqueId(), false, true), new AsyncCallback<RoomSharingModel>() {
				@Override
				public void onFailure(Throwable caught) {
					iRoomSharingHeader.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
				}
				@Override
				public void onSuccess(RoomSharingModel result) {
					iRoomSharingHeader.clearMessage();
					iRoomSharing.setEditable(iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability());
					iRoomSharing.setModel(result);
					iRoomSharing.setVisible(true);
				}
			});
		}
		
		if (iRoom.hasEventAvailabilityModel()) {
			iEventAvailabilityHeader.clearMessage();
			iEventAvailability.setEditable((iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability());
			iEventAvailability.setModel(iRoom.getEventAvailabilityModel());
			iEventAvailability.setVisible(true);
		} else {
			iEventAvailabilityHeader.showLoading();
			iEventAvailability.setVisible(false);
			RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getUniqueId(), true), new AsyncCallback<RoomSharingModel>() {
				@Override
				public void onFailure(Throwable caught) {
					iEventAvailabilityHeader.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
				}
				@Override
				public void onSuccess(RoomSharingModel result) {
					iEventAvailabilityHeader.clearMessage();
					iEventAvailability.setEditable((iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability());
					iEventAvailability.setModel(result);
					iEventAvailability.setVisible(true);
				}
			});
		}
		
		iPictures.clearTable();
		if (iRoom.hasPictures()) {
			for (final RoomPictureInterface picture: iRoom.getPictures())
				iPictures.addRow(picture, line(picture));
		}
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangePicture()) || iRoom.isCanChangePicture()) {
			iForm.getRowFormatter().setVisible(iPictureHeaderRow, true);
			iForm.getRowFormatter().setVisible(iPictureUploadRow, true);
			iPictures.setVisible(true);
		} else {
			iForm.getRowFormatter().setVisible(iPictureUploadRow, false);
			if (!iRoom.hasPictures()) {
				iPictures.setVisible(false);
				iForm.getRowFormatter().setVisible(iPictureHeaderRow, false);
			} else {
				iPictures.setVisible(true);
				iForm.getRowFormatter().setVisible(iPictureHeaderRow, true);
			}
		}
	}
	
	public RoomDetailInterface getRoom() { return iRoom; }
	
	protected void buildingChanged() {
		BuildingInterface building = iProperties.getBuilding(Long.valueOf(iBuilding.getWidget().getValue(iBuilding.getWidget().getSelectedIndex())));
		if (building != null) {
			iX.setValue(building.getX());
			iY.setValue(building.getY());
		}
		if (iProperties.isGoogleMap())
			setMarker();
	}
	
	protected void typeChanged() {
		RoomTypeInterface type = iProperties.getRoomType(Long.valueOf(iType.getWidget().getValue(iType.getWidget().getSelectedIndex())));
		iForm.getRowFormatter().setVisible(iBuildingRow, type != null && type.isRoom());
		iNameLabel.setText(type != null && type.isRoom() ? MESSAGES.propRoomNumber() : MESSAGES.propRoomName());
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName(iRoom.getUniqueId() == null ? MESSAGES.pageAddRoom() : MESSAGES.pageEditRoom());
		setVisible(true);
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		onShow();
		Window.scrollTo(0, 0);
		if (iGoogleMap != null && !iGoogleMapInitialized) {
			iGoogleMapInitialized = true;
			ScriptInjector.fromUrl("https://maps.google.com/maps/api/js?sensor=false&callback=setupGoogleMap").setWindow(ScriptInjector.TOP_WINDOW).setCallback(
					new Callback<Void, Exception>() {
						@Override
						public void onSuccess(Void result) {
						}
						@Override
						public void onFailure(Exception e) {
							UniTimeNotifications.error(e.getMessage(), e);
							iGoogleMap = null;
							iGoogleMapControl = null;
						}
					}).inject();
		} else if (iGoogleMap != null) {
			setMarker();
		}
	}
	
	private List<Widget> line(final RoomPictureInterface picture) {
		List<Widget> line = new ArrayList<Widget>();
		
		Image image = new Image(GWT.getHostPageBaseURL() + "picture?id=" + picture.getUniqueId());
		image.addStyleName("image");
		line.add(image);
		
		line.add(new Label(picture.getName()));
		line.add(new Label(picture.getType()));
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangePicture()) || iRoom.isCanChangePicture()) {
			Image remove = new Image(RESOURCES.delete());
			remove.setTitle(MESSAGES.titleDeleteRow());
			remove.addStyleName("remove");
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					for (int row = 1; row < iPictures.getRowCount(); row ++)
						if (picture.getUniqueId().equals(iPictures.getData(row).getUniqueId())) {
							iPictures.removeRow(row);
							break;
						}
					iHeader.setEnabled("update", true);
					event.stopPropagation();
				}
			});
			line.add(remove);
		}
		
		return line;
	}
	
	public boolean isGoogleMapEditable() {
		return iGoogleMapControl != null && iGoogleMapControl.isVisible();
	}
	
	public void hide() {
		setVisible(false);
		onHide();
		Window.scrollTo(iLastScrollLeft, iLastScrollTop);
	}
	
	protected void onHide() {
	}
	
	protected void onShow() {
	}
	
	protected native void addGoogleMap(Element canvas, Element control) /*-{
		$wnd.geoceodeMarker = function geoceodeMarker() {
			var searchBox = $doc.getElementById('mapSearchBox'); 
			$wnd.geocoder.geocode({'location': $wnd.marker.getPosition()}, function(results, status) {
				if (status == $wnd.google.maps.GeocoderStatus.OK) {
					if (results[0]) {
						$wnd.marker.setTitle(results[0].formatted_address);
						searchBox.value = results[0].formatted_address;
					} else {
						$wnd.marker.setTitle(null);
						searchBox.value = "";
					}
				} else {
					$wnd.marker.setTitle(null);
					searchBox.value = "";
				}
			});
		}
		$wnd.that = this
		
		$wnd.setupGoogleMap = function setupGoogleMap() {
			var latlng = new $wnd.google.maps.LatLng(50, -58);
			var myOptions = {
				zoom: 2,
				center: latlng,
				mapTypeId: $wnd.google.maps.MapTypeId.ROADMAP
			};
		
			$wnd.geocoder = new $wnd.google.maps.Geocoder();
			$wnd.map = new $wnd.google.maps.Map(canvas, myOptions);
			$wnd.marker = new $wnd.google.maps.Marker({
				position: latlng,
				map: $wnd.map,
				draggable: true,
				visible: false
			});
		
			$wnd.map.controls[$wnd.google.maps.ControlPosition.BOTTOM_LEFT].push(control);		
		
			var t = null;
			
			$wnd.google.maps.event.addListener($wnd.marker, 'position_changed', function() {
				$doc.getElementById("coordX").value = '' + $wnd.marker.getPosition().lat().toFixed(6);
				$doc.getElementById("coordY").value = '' + $wnd.marker.getPosition().lng().toFixed(6);
				if (t != null) clearTimeout(t);
				t = setTimeout($wnd.geoceodeMarker, 500);
			});
			
			$wnd.google.maps.event.addListener($wnd.map, 'rightclick', function(event) {
				if ($wnd.marker.getDraggable()) {
					$wnd.marker.setPosition(event.latLng);
					$wnd.marker.setVisible(true);
				}
			});
			
			$wnd.that.@org.unitime.timetable.gwt.client.rooms.RoomEdit::setMarker()();
		};
	}-*/;
	
	protected native void setMarker() /*-{
		try {
			var x = $doc.getElementById("coordX").value;
			var y = $doc.getElementById("coordY").value;
			if (x && y) {
				var pos = new $wnd.google.maps.LatLng(x, y);
				$wnd.marker.setPosition(pos);
				$wnd.marker.setVisible(true);
				if ($wnd.marker.getMap().getZoom() <= 10) $wnd.marker.getMap().setZoom(16);
				$wnd.marker.getMap().panTo(pos);
			} else {
				$wnd.marker.setVisible(false);
			}
			$wnd.marker.setDraggable(this.@org.unitime.timetable.gwt.client.rooms.RoomEdit::isGoogleMapEditable()());
		} catch (error) {}
	}-*/;
	
	protected native void geocodeAddress() /*-{
		var address = $doc.getElementById("mapSearchBox").value;
		$wnd.geocoder.geocode({ 'address': address }, function(results, status) {
			if (status == $wnd.google.	maps.GeocoderStatus.OK) {
				if (results[0]) {
					$wnd.marker.setPosition(results[0].geometry.location);
					$wnd.marker.setTitle(results[0].formatted_address);
					$wnd.marker.setVisible(true);
					if ($wnd.map.getZoom() <= 10) $wnd.map.setZoom(16);
					$wnd.map.panTo(results[0].geometry.location);
				} else {
					$wnd.marker.setVisible(false);
				}
			} else {
				$wnd.marker.setVisible(false);
			}
		});
	}-*/;
}
