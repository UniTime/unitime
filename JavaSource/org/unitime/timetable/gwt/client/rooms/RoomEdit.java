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

import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
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
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomTypeInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

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
	
	private ListBox iType;
	private ListBox iBuilding;
	private int iBuildingRow;
	private Label iNameLabel;
	private UniTimeWidget<TextBox> iName;
	private TextBox iDisplayName, iExternalId;
	private NumberBox iCapacity, iExamCapacity;
	private ListBox iControllingDepartment;
	private NumberBox iX, iY;
	private P iCoordinatesFormat;
	private NumberBox iArea;
	private P iAreaFormat;
	private CheckBox iDistanceCheck, iRoomCheck;
	private AbsolutePanel iGoogleMap;
	private boolean iGoogleMapInitialized = false;
	private ListBox iEventDepartment;
	private ListBox iEventStatus;
	private NumberBox iBreakTime;
	private TextArea iNote;
	private Map<Long, CheckBox> iExaminationRooms = new HashMap<Long, CheckBox>();
	private Map<Long, CheckBox> iGroups = new HashMap<Long, CheckBox>();
	private Map<Long, CheckBox> iFeatures = new HashMap<Long, CheckBox>();
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
	private TextArea iRoomSharingNote;
	private int iRoomSharingNoteRow;
	
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
		
		iType = new ListBox();
		int firstRow = iForm.addRow(MESSAGES.propRoomType(), iType, 1);
		iType.addItem(MESSAGES.itemSelect(), "-1");
		for (RoomTypeInterface type: iProperties.getRoomTypes())
			iType.addItem(type.getLabel(), type.getId().toString());
		iType.setSelectedIndex(0);
		iType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				typeChanged();
			}
		});
		
		iBuilding = new ListBox();
		iBuildingRow = iForm.addRow(MESSAGES.propBuilding(), iBuilding, 1);
		iBuilding.addItem(MESSAGES.itemSelect(), "-1");
		for (BuildingInterface building: iProperties.getBuildings())
			iBuilding.addItem(building.getAbbreviation() + " - " + building.getName(), building.getId().toString());
		iBuilding.setSelectedIndex(0);
		iBuilding.addChangeHandler(new ChangeHandler() {
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
		
		iDisplayName = new TextBox();
		iDisplayName.setStyleName("unitime-TextBox");
		iDisplayName.setMaxLength(100);
		iDisplayName.setWidth("480px");
		iForm.addRow(MESSAGES.propDisplayName(), iDisplayName, 1);
		
		iExternalId = new TextBox();
		iExternalId.setStyleName("unitime-TextBox");
		iExternalId.setMaxLength(40);
		iExternalId.setWidth("300px");
		iForm.addRow(MESSAGES.propExternalId(), iExternalId, 1);
		
		iCapacity = new NumberBox(); iCapacity.setDecimal(false); iCapacity.setNegative(false);
		iCapacity.setMaxLength(6);
		iCapacity.setWidth("80px");
		iForm.addRow(MESSAGES.propCapacity(), iCapacity, 1);
		
		iControllingDepartment = new ListBox();
		iControllingDepartment.addItem(MESSAGES.itemNoControlDepartment(), "-1");
		iForm.addRow(MESSAGES.propControllingDepartment(), iControllingDepartment, 1);
		for (DepartmentInterface department: iProperties.getDepartments())
			iControllingDepartment.addItem(department.getExtAbbreviationOrCode() + " - " + department.getExtLabelWhenExist(), department.getId().toString());
		
		iX = new NumberBox(); iX.setMaxLength(12); iX.setWidth("80px"); iX.setDecimal(true); iX.setNegative(true); iX.addStyleName("number");
		iY = new NumberBox(); iY.setMaxLength(12); iY.setWidth("80px"); iY.setDecimal(true); iY.setNegative(true); iY.addStyleName("number");
		iX.getElement().setId("coordX"); iY.getElement().setId("coordY");
		P p = new P("coordinates");
		p.add(iX);
		P comma = new P("comma"); comma.setText(", ");
		p.add(comma);
		p.add(iY);
		iCoordinatesFormat = new P("format");
		iCoordinatesFormat.setText(iProperties.getEllipsoid());
		p.add(iCoordinatesFormat);
		iForm.addRow(MESSAGES.propCoordinates(), p, 1);
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
		
		iArea = new NumberBox(); iArea.setDecimal(true); iArea.setNegative(false); iArea.addStyleName("number");
		iArea.setWidth("80px");
		iArea.setMaxLength(12);
		P q = new P("area");
		q.add(iArea);
		iAreaFormat = new P("format");
		iAreaFormat.setText(CONSTANTS.roomAreaUnitsLong());
		q.add(iAreaFormat);
		iForm.addRow(MESSAGES.propRoomArea(), q, 1);
		
		iDistanceCheck = new CheckBox();
		iForm.addRow(MESSAGES.propDistanceCheck(), iDistanceCheck, 1);
		
		iRoomCheck = new CheckBox();
		iForm.addRow(MESSAGES.propRoomCheck(), iRoomCheck, 1);
		
		if (!iProperties.getExamTypes().isEmpty()) {
			P exams = new P("exams");
			for (final ExamTypeInterface type: iProperties.getExamTypes()) {
				final CheckBox ch = new CheckBox(type.getLabel());
				ch.addStyleName("exam");
				iExaminationRooms.put(type.getId(), ch);
				exams.add(ch);
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
			iForm.addRow(MESSAGES.propExamRooms(), exams, 1);
			
			iExamCapacity = new NumberBox(); iExamCapacity.setDecimal(false); iExamCapacity.setNegative(false);
			iExamCapacity.setMaxLength(6);
			iExamCapacity.setWidth("80px");
			iForm.addRow(MESSAGES.propExamCapacity(), iExamCapacity, 1);
		}
		
		iEventDepartment = new ListBox();
		iEventDepartment.addItem(MESSAGES.itemNoEventDepartment(), "-1");
		iForm.addRow(MESSAGES.propEventDepartment(), iEventDepartment, 1);
		for (DepartmentInterface department: iProperties.getDepartments())
			if (department.isEvent())
				iEventDepartment.addItem(department.getDeptCode() + " - " + department.getLabel(), department.getId().toString());
		
		iEventStatus = new ListBox();
		iEventStatus.addItem(MESSAGES.itemDefault(), "-1");
		iForm.addRow(MESSAGES.propEventStatus(), iEventStatus, 1);
		for (int i = 0; i < CONSTANTS.eventStatusName().length; i++)
			iEventStatus.addItem(CONSTANTS.eventStatusName()[i], String.valueOf(i));
		
		iNote = new TextArea();
		iNote.setStyleName("unitime-TextArea");
		iNote.setVisibleLines(5);
		iNote.setCharacterWidth(70);
		iForm.addRow(MESSAGES.propEventNote(), iNote);

		iBreakTime = new NumberBox(); iBreakTime.setDecimal(false); iBreakTime.setNegative(false); iBreakTime.addStyleName("number");
		iBreakTime.setWidth("80px");
		iBreakTime.setMaxLength(12); 
		P b = new P("breaktime");
		b.add(iBreakTime);
		P f = new P("note");
		f.setText(MESSAGES.useDefaultBreakTimeWhenEmpty());
		b.add(f);
		iForm.addRow(MESSAGES.propBreakTime(), b, 1);
		
		if (iProperties.isGoogleMap()) {
			// ScriptInjector.fromUrl("https://maps.google.com/maps/api/js?sensor=false").inject();
			iGoogleMap = new AbsolutePanel();
			iGoogleMap.getElement().setId("map_canvas");
			iGoogleMap.setStyleName("map");
			iForm.setWidget(firstRow, 2, iGoogleMap);
			iForm.getFlexCellFormatter().setRowSpan(firstRow, 2, iForm.getRowCount() - firstRow);
		}
		
		iForm.addHeaderRow(MESSAGES.headerRoomGroups());
		if (!iProperties.getGroups().isEmpty()) {
			P groups = new P("groups");
			Map<Long, P> departmental = new HashMap<Long, P>();
			for (GroupInterface group: iProperties.getGroups()) {
				CheckBox ch = new CheckBox(group.getLabel());
				ch.addStyleName("group");
				iGroups.put(group.getId(), ch);
				if (group.getDepartment() != null) {
					P d = departmental.get(group.getDepartment().getId());
					if (d == null) {
						d = new P("groups");
						departmental.put(group.getDepartment().getId(), d);
					}
					d.add(ch);
				} else {
					groups.add(ch);
				}
			}
			if (groups.getWidgetCount() > 0)
				iForm.addRow(MESSAGES.propGlobalGroups(), groups);
			for (DepartmentInterface dept: iProperties.getDepartments()) {
				P d = departmental.get(dept.getId());
				if (d != null)
					iGroupRow.put(dept.getId(), iForm.addRow(dept.getExtLabelWhenExist() + ":", d));
			}
		}
		
		iForm.addHeaderRow(MESSAGES.headerRoomFeatures());
		if (!iProperties.getFeatures().isEmpty()) {
			P features = new P("features");
			Map<Long, P> types = new HashMap<Long, P>();
			for (FeatureInterface feature: iProperties.getFeatures()) {
				CheckBox ch = new CheckBox(feature.getTitle());
				ch.addStyleName("feature");
				iFeatures.put(feature.getId(), ch);
				if (feature.getType() != null) {
					P d = types.get(feature.getType().getId());
					if (d == null) {
						d = new P("features");
						types.put(feature.getType().getId(), d);
					}
					d.add(ch);
				} else {
					features.add(ch);
				}
			}
			if (features.getWidgetCount() > 0)
				iForm.addRow(MESSAGES.propFeatures(), features);
			for (FeatureTypeInterface type: iProperties.getFeatureTypes()) {
				P d = types.get(type.getId());
				if (d != null)
					iForm.addRow(type.getLabel() + ":", d);
			}
		}
		
		iRoomSharingHeader = new UniTimeHeaderPanel(MESSAGES.headerRoomSharing());
		iForm.addHeaderRow(iRoomSharingHeader);
		iRoomSharing = new RoomSharingWidget(true, false);
		iForm.addRow(iRoomSharing);
		iRoomSharingNote = new TextArea();
		iRoomSharingNote.setStyleName("unitime-TextArea");
		iRoomSharingNote.setVisibleLines(10);
		iRoomSharingNote.setCharacterWidth(50);
		iRoomSharingNote.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iRoomSharing.getModel() != null)
					iRoomSharing.getModel().setNote(event.getValue());
			}
		});
		iRoomSharingNoteRow = iForm.addRow(MESSAGES.propRoomSharingNote(), iRoomSharingNote);

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

		iFooter = iHeader.clonePanel();
		
		initWidget(iForm);
	}
	
	public void setRoom(RoomDetailInterface room) {
		iRoom = room;
		if (iRoom == null) {
			iRoom = new RoomDetailInterface();
		}
		iRoomSharingHeader.showLoading();
		RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getUniqueId(), false, true), new AsyncCallback<RoomSharingModel>() {
			@Override
			public void onFailure(Throwable caught) {
				iRoomSharingHeader.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
			}
			@Override
			public void onSuccess(RoomSharingModel result) {
				iRoomSharingHeader.clearMessage();
				iRoomSharing.setModel(result);
				iRoomSharing.setVisible(true);
				iRoomSharingNote.setValue(result.hasNote() ? result.getNote() : "");
				iForm.getRowFormatter().setVisible(iRoomSharingNoteRow, result.isNoteEditable());
			}
		});
		for (ExamTypeInterface type: iProperties.getExamTypes()) {
			iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), false);
		}
		iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, false);
		iPeriodPreferencesHeader.showLoading();
		for (final ExamTypeInterface type: iProperties.getExamTypes()) {
			final PeriodPreferencesWidget pref = iPeriodPreferences.get(type.getId());
			RPC.execute(RoomInterface.PeriodPreferenceRequest.load(iRoom.getUniqueId(), type.getId()), new AsyncCallback<PeriodPreferenceModel>() {
				@Override
				public void onFailure(Throwable caught) {
					iPeriodPreferencesHeader.setErrorMessage(MESSAGES.failedToLoadPeriodPreferences(caught.getMessage()));
				}

				@Override
				public void onSuccess(PeriodPreferenceModel result) {
					iPeriodPreferencesHeader.clearMessage();
					pref.setModel(result);
					iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), iExaminationRooms.get(type.getId()).getValue());
				}
			});
		}
		iEventAvailabilityHeader.showLoading();
		RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getUniqueId(), true), new AsyncCallback<RoomSharingModel>() {
			@Override
			public void onFailure(Throwable caught) {
				iEventAvailabilityHeader.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
			}
			@Override
			public void onSuccess(RoomSharingModel result) {
				iEventAvailabilityHeader.clearMessage();
				iEventAvailability.setModel(result);
				iEventAvailability.setVisible(true);
			}
		});
	}
	
	protected void buildingChanged() {
		BuildingInterface building = iProperties.getBuilding(Long.valueOf(iBuilding.getValue(iBuilding.getSelectedIndex())));
		if (building != null) {
			iX.setValue(building.getX());
			iY.setValue(building.getY());
		}
		if (iProperties.isGoogleMap())
			setMarker();
	}
	
	protected void typeChanged() {
		RoomTypeInterface type = iProperties.getRoomType(Long.valueOf(iType.getValue(iType.getSelectedIndex())));
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
			try {
				addGoogleMap(iGoogleMap.getElement());
			} catch (Exception e) {
				UniTimeNotifications.error(e.getMessage(), e);
			}
		}
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
	
	protected native void addGoogleMap(Element canvas) /*-{
		$wnd.createGoogleSeachControl = function createGoogleSeachControl(map) {
			var controlDiv = $doc.createElement('DIV');
		    controlDiv.index = 1;
			controlDiv.style.marginBottom = '15px';
			var controlUI = $doc.createElement('DIV');
			controlUI.style.backgroundColor = 'transparent';
			controlUI.style.cursor = 'pointer';
			controlUI.style.textAlign = 'center';
			controlUI.title = "Seach";
			controlDiv.appendChild(controlUI);
			var controltxtbox = $doc.createElement('input');
			controltxtbox.setAttribute("id", "txt_googleseach");
			controltxtbox.setAttribute("type", "text");
			controltxtbox.setAttribute("value", "");
			controltxtbox.style.height = '22px';
			controltxtbox.style.width = '450px';
			controltxtbox.style.marginRight = '2px';
			controlUI.appendChild(controltxtbox);
			var controlbtn = $doc.createElement('input');
			controlbtn.setAttribute("id", "btn_googleseach");
			controlbtn.setAttribute("type", "button");
			controlbtn.setAttribute("value", "Geocode");
			controlUI.appendChild(controlbtn);
			$wnd.google.maps.event.addDomListener(controlbtn, 'click', function() {
				geoceodeAddress(controltxtbox.value);
			});
			controltxtbox.onkeypress = function(e) {
				var key = e.keyCode || e.which;
				if (key == 13) {
					geoceodeAddress(controltxtbox.value);
					return false;
				}
				return true;
			};
			map.controls[$wnd.google.maps.ControlPosition.BOTTOM_LEFT].push(controlDiv);
			return controltxtbox;
		}
		
		var latlng = new $wnd.google.maps.LatLng(50, -58);
		var myOptions = {
			zoom: 2,
			center: latlng,
			mapTypeId: $wnd.google.maps.MapTypeId.ROADMAP
		};
		
		var geocoder = new $wnd.google.maps.Geocoder();
		var map = new $wnd.google.maps.Map(canvas, myOptions);
		$wnd.marker = new $wnd.google.maps.Marker({
			position: latlng,
			map: map,
			draggable: true,
			visible: false
		});
		
		var searchBox = $wnd.createGoogleSeachControl(map);
		
		$wnd.geoceodeAddress = function geoceodeAddress(address) {
			var address = $doc.getElementById("txt_googleseach").value;
			geocoder.geocode({ 'address': address }, function(results, status) {
				if (status == $wnd.google.maps.GeocoderStatus.OK) {
					if (results[0]) {
						$wnd.marker.setPosition(results[0].geometry.location);
						$wnd.marker.setTitle(results[0].formatted_address);
						$wnd.marker.setVisible(true);
						if (map.getZoom() <= 10) map.setZoom(16);
						map.panTo(results[0].geometry.location);
					} else {
						$wnd.marker.setVisible(false);
					}
				} else {
					$wnd.marker.setVisible(false);
				}
			});
		}
		
		$wnd.geoceodeMarker = function geoceodeMarker() {
			geocoder.geocode({'location': $wnd.marker.getPosition()}, function(results, status) {
				if (status == $wnd.google.maps.GeocoderStatus.OK) {
					if (results[0]) {
						$wnd.marker.setTitle(results[0].formatted_address);
						if (searchBox != null)
							searchBox.value = results[0].formatted_address;
					} else {
						$wnd.marker.setTitle(null);
						if (searchBox != null) searchBox.value = "";
					}
				} else {
					$wnd.marker.setTitle(null);
					if (searchBox != null) searchBox.value = "";
				}
			});
		}
		
		var t = null;
		
		$wnd.google.maps.event.addListener($wnd.marker, 'position_changed', function() {
			$doc.getElementById("coordX").value = '' + $wnd.marker.getPosition().lat().toFixed(6);
			$doc.getElementById("coordY").value = '' + $wnd.marker.getPosition().lng().toFixed(6);
			if (t != null) clearTimeout(t);
			t = setTimeout($wnd.geoceodeMarker, 500);
		});
		
		$wnd.google.maps.event.addListener(map, 'rightclick', function(event) {
			$wnd.marker.setPosition(event.latLng);
			$wnd.marker.setVisible(true);
		});
	}-*/;
	
	protected native void setMarker() /*-{
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
	}-*/;
}
