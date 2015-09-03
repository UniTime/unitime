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
import org.unitime.timetable.gwt.client.rooms.RoomDetail.Check;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
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
import org.unitime.timetable.gwt.shared.RoomInterface.AcademicSessionInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.AttachmentTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FutureOperation;
import org.unitime.timetable.gwt.shared.RoomInterface.FutureRoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomException;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomUpdateRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
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
	private DepartmentInterface iLastControllingDept = null, iLastEventDept = null;
	private Long iLastSelectedDepartmentId = null;
	
	private UniTimeWidget<ListBox> iType;
	private UniTimeWidget<ListBox> iBuilding;
	private int iBuildingRow;
	private Label iNameLabel;
	private UniTimeWidget<TextBox> iName;
	private TextBox iDisplayName, iExternalId;
	private UniTimeWidget<NumberBox> iCapacity, iExamCapacity;
	private UniTimeWidget<ListBox> iControllingDepartment;
	private NumberBox iX, iY;
	private UniTimeWidget<P> iCoordinates;
	private P iCoordinatesFormat;
	private UniTimeWidget<P> iAreaPanel;
	private NumberBox iArea;
	private P iAreaFormat;
	private CheckBox iDistanceCheck, iRoomCheck;
	private AbsolutePanel iGoogleMap;
	private AbsolutePanel iGoogleMapControl;
	private boolean iGoogleMapInitialized = false;
	private ListBox iEventDepartment;
	private ListBox iEventStatus;
	private P iBreakTimePanel;
	private NumberBox iBreakTime;
	private UniTimeWidget<TextArea> iNote;
	private P iExaminationRoomsPanel;
	private Map<Long, CheckBox> iExaminationRooms = new HashMap<Long, CheckBox>();
	private Map<Long, CheckBox> iGroups = new HashMap<Long, CheckBox>();
	private Map<Long, CheckBox> iFeatures = new HashMap<Long, CheckBox>();
	private P iGlobalGroupsPanel;
	private Map<Long, P> iGroupPanel = new HashMap<Long, P>();
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
	private UniTimeHeaderPanel iApplyToHeader;
	private UniTimeTable<FutureRoomInterface> iApplyTo;
	private RoomsPageMode iMode = null;
	
	public RoomEdit(RoomsPageMode mode) {
		iMode = mode;
		iHeader = new UniTimeHeaderPanel();
		ClickHandler clickCreateOrUpdate = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) {
					iHeader.setErrorMessage(MESSAGES.failedValidationCheckForm());
					UniTimeNotifications.error(MESSAGES.failedValidationCheckForm());
				} else {
					RoomUpdateRpcRequest request = RoomUpdateRpcRequest.createSaveOrUpdateRequest(getRoom());
					String future = generateAlsoUpdateMessage(false);
					if (future != null) {
						if (Window.confirm(getRoom().getUniqueId() == null ? MESSAGES.confirmCreateRoomInFutureSessions(future) : MESSAGES.confirmUpdateRoomInFutureSessions(future)))
							fillFutureFlags(request, false);
						else
							return;
					}
					LoadingWidget.getInstance().show(getRoom().getUniqueId() == null ? MESSAGES.waitSavingRoom() : MESSAGES.waitUpdatingRoom());
					RPC.execute(request, new AsyncCallback<RoomDetailInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							String message = null;
							RoomDetailInterface result = null;
							if (caught instanceof RoomException) {
								message = caught.getMessage();
								result = ((RoomException)caught).getRoom();
							} else if (getRoom().getUniqueId() == null) {
								message = MESSAGES.errorFailedToSaveRoom(caught.getMessage());
							} else {
								message = MESSAGES.errorFailedToUpdateRoom(caught.getMessage());
							}
							iHeader.setErrorMessage(message);
							UniTimeNotifications.error(message);
							if (result != null)
								hide(result, true, message);
						}

						@Override
						public void onSuccess(RoomDetailInterface result) {
							LoadingWidget.getInstance().hide();
							hide(result, true, null);
						}
					});
				}
			}
		};
		iHeader.addButton("create", MESSAGES.buttonCreateRoom(), 100, clickCreateOrUpdate);
		iHeader.addButton("update", MESSAGES.buttonUpdateRoom(), 100, clickCreateOrUpdate);
		iHeader.addButton("delete", MESSAGES.buttonDeleteRoom(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RoomUpdateRpcRequest request = RoomUpdateRpcRequest.createDeleteRequest(getRoom().getSessionId(), getRoom().getUniqueId());
				String future = generateAlsoUpdateMessage(true);
				if (Window.confirm(future == null ? MESSAGES.confirmDeleteRoom() : MESSAGES.confirmDeleteRoomInFutureSessions(future))) {
					if (future != null) fillFutureFlags(request, true);
				} else {
					return;
				}
				LoadingWidget.getInstance().show(MESSAGES.waitDeletingRoom());
				RPC.execute(request, new AsyncCallback<RoomDetailInterface>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						String message = null;
						RoomDetailInterface result = null;
						if (caught instanceof RoomException) {
							message = caught.getMessage();
							result = ((RoomException)caught).getRoom();
						} else {
							message = MESSAGES.errorFailedToDeleteRoom(caught.getMessage());
						}
						iHeader.setErrorMessage(message);
						UniTimeNotifications.error(message);
						if (result != null)
							hide(result, true, message);
					}

					@Override
					public void onSuccess(RoomDetailInterface result) {
						LoadingWidget.getInstance().hide();
						hide(null, false, null);
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide(null, true, null);
			}
		});
		
		iForm = new SimpleForm(2);
		iForm.addStyleName("unitime-RoomEdit");
		
		iType = new UniTimeWidget<ListBox>(new ListBox()); iType.getWidget().setStyleName("unitime-TextBox");
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				typeChanged();
				iType.clearHint();
				iHeader.clearMessage();
			}
		});
		
		iBuilding = new UniTimeWidget<ListBox>(new ListBox()); iBuilding.getWidget().setStyleName("unitime-TextBox");
		iBuilding.getWidget().addItem(MESSAGES.itemSelect(), "-1");
		iBuilding.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				buildingChanged();
				iBuilding.clearHint();
				iHeader.clearMessage();
			}
		});
		
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
		
		iDisplayName = new TextBox();
		iDisplayName.setStyleName("unitime-TextBox");
		iDisplayName.setMaxLength(100);
		iDisplayName.setWidth("480px");
		
		iExternalId = new TextBox();
		iExternalId.setStyleName("unitime-TextBox");
		iExternalId.setMaxLength(40);
		iExternalId.setWidth("300px");
		
		iCapacity = new UniTimeWidget<NumberBox>(new NumberBox());
		iCapacity.getWidget().setDecimal(false);
		iCapacity.getWidget().setNegative(false);
		iCapacity.getWidget().setMaxLength(6);
		iCapacity.getWidget().setWidth("80px");
		iCapacity.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCapacity.clearHint();
				iHeader.clearMessage();
			}
		});
		
		iControllingDepartment = new UniTimeWidget<ListBox>(new ListBox());
		iControllingDepartment.getWidget().setStyleName("unitime-TextBox");
		
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
		iX.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iProperties.isGoogleMap())
					setMarker();
			}
		});
		iY.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iProperties.isGoogleMap())
					setMarker();
			}
		});
		
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
		
		iDistanceCheck = new CheckBox();
		iDistanceCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				distanceCheckChanged();
			}
		});
		
		iRoomCheck = new CheckBox();
		iRoomCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				roomCheckChanged();
			}
		});
		
		iExaminationRoomsPanel = new P("exams"); iExaminationRoomsPanel.setWidth("100%");
		
		iExamCapacity = new UniTimeWidget<NumberBox>(new NumberBox());
		iExamCapacity.getWidget().setDecimal(false);
		iExamCapacity.getWidget().setNegative(false);
		iExamCapacity.getWidget().setMaxLength(6);
		iExamCapacity.getWidget().setWidth("80px");
		iExamCapacity.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iExamCapacity.clearHint();
				iHeader.clearMessage();
			}
		});
		
		iEventDepartment = new ListBox();
		iEventDepartment.setStyleName("unitime-TextBox");
		iEventDepartment.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iEventAvailabilityHeader.setVisible(iEventDepartment.getSelectedIndex() > 0);
				iEventAvailability.setVisible(iEventDepartment.getSelectedIndex() > 0);
			}
		});
		
		iEventStatus = new ListBox();
		iEventStatus.setStyleName("unitime-TextBox");
		iEventStatus.addItem(MESSAGES.itemDefault(), "-1");
		for (int i = 0; i < CONSTANTS.eventStatusName().length; i++)
			iEventStatus.addItem(CONSTANTS.eventStatusName()[i], String.valueOf(i));
		
		iNote = new UniTimeWidget<TextArea>(new TextArea());
		iNote.getWidget().setStyleName("unitime-TextArea");
		iNote.getWidget().setVisibleLines(5);
		iNote.getWidget().setCharacterWidth(70);
		
		iBreakTime = new NumberBox();
		iBreakTime.setDecimal(false);
		iBreakTime.setNegative(false);
		iBreakTime.addStyleName("number");
		iBreakTime.setWidth("80px");
		iBreakTime.setMaxLength(12); 
		iBreakTimePanel = new P("breaktime");
		iBreakTimePanel.add(iBreakTime);
		P f = new P("note");
		f.setText(MESSAGES.useDefaultBreakTimeWhenEmpty());
		iBreakTimePanel.add(f);
		
		iRoomSharingHeader = new UniTimeHeaderPanel(MESSAGES.headerRoomSharing());
		iRoomSharing = new RoomSharingWidget(true, true);
		iRoomSharing.addValueChangeHandler(new ValueChangeHandler<RoomInterface.RoomSharingModel>() {
			@Override
			public void onValueChange(ValueChangeEvent<RoomSharingModel> event) {
				iRoomSharingHeader.clearMessage();
			}
		});
		iControllingDepartment.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iControllingDepartment.clearHint();
				if (iRoom.getDepartment(iLastSelectedDepartmentId) == null)
					iRoomSharing.removeOption(iLastSelectedDepartmentId);
				iLastSelectedDepartmentId = Long.valueOf(iControllingDepartment.getWidget().getValue(iControllingDepartment.getWidget().getSelectedIndex()));
				if (iLastSelectedDepartmentId > 0)
					iRoomSharing.addOption(iLastSelectedDepartmentId);
			}
		});

		iPeriodPreferencesHeader = new UniTimeHeaderPanel(MESSAGES.headerExaminationPeriodPreferences());
		
		iEventAvailabilityHeader = new UniTimeHeaderPanel(MESSAGES.headerEventAvailability());
		iEventAvailability = new RoomSharingWidget(true);
		
		iPicturesHeader = new UniTimeHeaderPanel(MESSAGES.headerRoomPictures());
		
		iPictures = new UniTimeTable<RoomPictureInterface>();
		iPictures.setStyleName("unitime-RoomPictures");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colPicture()));
		header.add(new UniTimeTableHeader(MESSAGES.colName()));
		header.add(new UniTimeTableHeader(MESSAGES.colContentType()));
		header.add(new UniTimeTableHeader(MESSAGES.colPictureType()));
		header.add(new UniTimeTableHeader("&nbsp;"));
		iPictures.addRow(null, header);
		
		iFileUpload = new UniTimeFileUpload();
		iFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				RPC.execute(RoomPictureRequest.upload(iRoom.getSessionId(), iRoom.getUniqueId()), new AsyncCallback<RoomPictureResponse>() {
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
		
		iApplyToHeader = new UniTimeHeaderPanel(MESSAGES.headerRoomApplyToFutureRooms());
		iApplyToHeader.setTitleStyleName("update-options");
		iApplyTo = new UniTimeTable<FutureRoomInterface>();
		iApplyTo.setStyleName("unitime-RoomApplyTo");
		List<UniTimeTableHeader> ah = new ArrayList<UniTimeTableHeader>();
		ah.add(new UniTimeTableHeader("&nbsp;"));
		ah.add(new UniTimeTableHeader(MESSAGES.colName()));
		ah.add(new UniTimeTableHeader(MESSAGES.colSession()));
		for (FutureOperation op: FutureOperation.values()) {
			ah.add(new UniTimeTableHeader(getFutureOperationLabel(op)));
		}
		iApplyTo.addRow(null, ah);
		
		iFooter = iHeader.clonePanel();
		
		initWidget(iForm);		
	}
	
	public void setProperties(RoomPropertiesInterface properties) {
		iProperties = properties;
		
		iForm.setColSpan(iProperties.isGoogleMap() ? 3 : 2);
		
		iBuilding.getWidget().clear();
		for (BuildingInterface building: iProperties.getBuildings())
			iBuilding.getWidget().addItem(building.getAbbreviation() + " - " + building.getName(), building.getId().toString());

		iCoordinatesFormat.setText(iProperties.getEllipsoid());
		
		iExaminationRooms.clear();
		iExaminationRoomsPanel.clear();
		for (final ExamTypeInterface type: iProperties.getExamTypes()) {
			final CheckBox ch = new CheckBox(type.getLabel());
			ch.addStyleName("exam");
			iExaminationRooms.put(type.getId(), ch);
			iExaminationRoomsPanel.add(ch);
			ch.setValue(false);
			ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					Integer row = iPeriodPreferencesRow.get(type.getId());
					if (row != null)
						iForm.getRowFormatter().setVisible(row, event.getValue());
					boolean prefVisible = false;
					for (ExamTypeInterface t: iProperties.getExamTypes()) {
						if (iExaminationRooms.get(t.getId()).getValue()) { prefVisible = true; break; }
					}
					if (iPeriodPreferencesHeaderRow > 0)
						iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, prefVisible);
					if (!event.getValue()) {
						iExamCapacity.clearHint();
						iHeader.clearMessage();
					}
				}
			});
		}
		
		if (iProperties.isGoogleMap() && iGoogleMap == null) {
			iGoogleMap = new AbsolutePanel();
			iGoogleMap.setStyleName("map");
			
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
		
		iGroups.clear();
		iGroupPanel.clear();
		iGlobalGroupsPanel = null;
		if (!iProperties.getGroups().isEmpty()) {
			P groups = new P("groups");
			for (GroupInterface group: iProperties.getGroups()) {
				CheckBox ch = new CheckBox(group.getLabel());
				ch.addStyleName("group");
				iGroups.put(group.getId(), ch);
				if (group.getDepartment() != null) {
					P d = iGroupPanel.get(group.getDepartment().getId());
					if (d == null) {
						d = new P("groups");
						d.setWidth("100%");
						iGroupPanel.put(group.getDepartment().getId(), d);
					}
					d.add(ch);
				} else {
					groups.add(ch);
				}
			}
			if (groups.getWidgetCount() > 0) {
				iGlobalGroupsPanel = groups;
			}
		}
		
		iFeatures.clear();
		if (!iProperties.getFeatures().isEmpty()) {
			for (FeatureInterface feature: iProperties.getFeatures()) {
				CheckBox ch = new CheckBox(feature.getTitle());
				ch.addStyleName("feature");
				iFeatures.put(feature.getId(), ch);
			}
		}
		
		iPeriodPreferences.clear();
		for (ExamTypeInterface type: iProperties.getExamTypes()) {
			PeriodPreferencesWidget pref = new PeriodPreferencesWidget(true);
			iPeriodPreferences.put(type.getId(), pref);
		}
	}
	
	public void setRoom(RoomDetailInterface room) {
		iRoom = room;
		if (iRoom == null) {
			iRoom = new RoomDetailInterface();
			iRoom.setSessionId(iProperties.getAcademicSessionId());
			iRoom.setSessionName(iProperties.getAcademicSessionName());
			iHeader.setEnabled("create", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
		} else {
			iHeader.setEnabled("create", false);
			iHeader.setEnabled("update", true);
			iHeader.setEnabled("delete", iRoom.isCanDelete());
		}
		iLastControllingDept = iRoom.getControlDepartment();
		iLastEventDept = iRoom.getEventDepartment();
		
		iForm.clear();
		
		iHeader.clearMessage();
		iForm.addHeaderRow(iHeader);
		
		int firstRow = iForm.getRowCount();
		
		if (iMode.hasSessionSelection()) {
			iForm.addRow(MESSAGES.propAcademicSession(), new Label(iRoom.hasSessionName() ? iRoom.getSessionName() : iProperties.getAcademicSessionName()), 1);
		}
		
		if (iRoom.getRoomType() == null || iRoom.isCanChangeType()) {
			iType.clearHint();
			iType.getWidget().clear();
			if (iRoom.getRoomType() == null) {
				iType.getWidget().addItem(MESSAGES.itemSelect(), "-1");
				for (RoomTypeInterface type: iProperties.getRoomTypes()) {
					if (type.isRoom() && iProperties.isCanAddRoom())
						iType.getWidget().addItem(type.getLabel(), type.getId().toString());
					else if (!type.isRoom() && iProperties.isCanAddNonUniversity())
						iType.getWidget().addItem(type.getLabel(), type.getId().toString());
				}
				iType.getWidget().setSelectedIndex(0);
			} else {
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
			iForm.addRow(MESSAGES.propRoomType(), iType, 1);
		} else {
			iForm.addRow(MESSAGES.propRoomType(), new Label(iRoom.getRoomType().getLabel(), false), 1);
		}
		
		if (iRoom.getUniqueId() != null && iRoom.getBuilding() == null) {
			iBuildingRow = -1;
		} else if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iBuilding.clearHint();
			if (iRoom.getBuilding() == null) {
				iBuilding.getWidget().setSelectedIndex(0);
			} else {
				iBuilding.getWidget().setSelectedIndex(1 + iProperties.getBuildings().indexOf(iRoom.getBuilding()));
			}
			iBuildingRow = iForm.addRow(MESSAGES.propBuilding(), iBuilding, 1);
		} else {
			iBuildingRow = iForm.addRow(MESSAGES.propBuilding(), new Label(iRoom.getBuilding().getAbbreviation() + " - " + iRoom.getBuilding().getName()), 1);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iName.clearHint();
			iName.getWidget().setText(iRoom.getName() == null ? "" : iRoom.getName());
			iForm.addRow(iNameLabel, iName, 1);
		} else {
			iForm.addRow(iNameLabel, new Label(iRoom.getName()), 1);
		}
		
		if (iRoom.getRoomType() == null || iRoom.isCanChangeType()) {
			typeChanged();
		} else {
			if (iBuildingRow >= 0)
				iForm.getRowFormatter().setVisible(iBuildingRow, iRoom.getRoomType() != null && iRoom.getRoomType().isRoom());
			iNameLabel.setText(iRoom.getRoomType() != null && iRoom.getRoomType().isRoom() ? MESSAGES.propRoomNumber() : MESSAGES.propRoomName());
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iDisplayName.setText(iRoom.getDisplayName() == null ? "" : iRoom.getDisplayName());
			iForm.addRow(MESSAGES.propDisplayName(), iDisplayName, 1);
		} else if (iRoom.hasDisplayName()) {
			iForm.addRow(MESSAGES.propDisplayName(), new Label(iRoom.getDisplayName()), 1);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExternalId()) || iRoom.isCanChangeExternalId()) {
			iExternalId.setText(iRoom.getExternalId() == null ? "" : iRoom.getExternalId());
			iForm.addRow(MESSAGES.propExternalId(), iExternalId, 1);
		} else if (iRoom.hasExternalId()) {
			iForm.addRow(MESSAGES.propExternalId(), new Label(iRoom.getExternalId()), 1);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeCapacity()) {
			iCapacity.clearHint();
			iCapacity.getWidget().setValue(iRoom.getCapacity());
			iForm.addRow(MESSAGES.propCapacity(), iCapacity, 1);
		} else if (iRoom.getCapacity() != null) {
			iForm.addRow(MESSAGES.propCapacity(), new Label(iRoom.getCapacity().toString()), 1);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeControll()) || iRoom.isCanChangeControll()) {
			iControllingDepartment.clearHint();
			iControllingDepartment.getWidget().clear();
			iControllingDepartment.getWidget().addItem(MESSAGES.itemNoControlDepartment(), "-1");
			for (DepartmentInterface department: iProperties.getDepartments())
				iControllingDepartment.getWidget().addItem(department.getExtAbbreviationOrCode() + " - " + department.getExtLabelWhenExist(), department.getId().toString());
			if (iRoom.getControlDepartment() == null) {
				iControllingDepartment.getWidget().setSelectedIndex(0);
			} else {
				int index = iProperties.getDepartments().indexOf(iRoom.getControlDepartment());
				if (index >= 0) {
					iControllingDepartment.getWidget().setSelectedIndex(1 + index);
				} else {
					iControllingDepartment.getWidget().addItem(iRoom.getControlDepartment().getExtAbbreviationOrCode() + " - " + iRoom.getControlDepartment().getExtLabelWhenExist(), iRoom.getControlDepartment().getId().toString());
					iControllingDepartment.getWidget().setSelectedIndex(iControllingDepartment.getWidget().getItemCount() - 1);
				}
			}
			if (iRoom.getUniqueId() == null && iControllingDepartment.getWidget().getItemCount() == 2)
				iControllingDepartment.getWidget().setSelectedIndex(1);
			iForm.addRow(MESSAGES.propControllingDepartment(), iControllingDepartment, 1);
/*		} else if (iRoom.getUniqueId() == null) {
			iControllingDepartment.getWidget().clear();
			for (DepartmentInterface department: iProperties.getDepartments())
				iControllingDepartment.getWidget().addItem(department.getExtAbbreviationOrCode() + " - " + department.getExtLabelWhenExist(), department.getId().toString());
			//TODO: guess selected department from filter
			iForm.addRow(MESSAGES.propDepartment(), iControllingDepartment, 1);*/
		} else if (iRoom.getControlDepartment() != null && iProperties.isCanSeeCourses()) {
			iForm.addRow(MESSAGES.propControllingDepartment(), new Label(RoomDetail.toString(iRoom.getControlDepartment())), 1);
		}
		
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			iX.setValue(iRoom.getX());
			iY.setValue(iRoom.getY());
			iForm.addRow(MESSAGES.propCoordinates(), iCoordinates, 1);
			iArea.setValue(iRoom.getArea());
			iForm.addRow(MESSAGES.propRoomArea(), iAreaPanel, 1);
			if (iProperties.isCanSeeCourses()) {
				iDistanceCheck.setValue(!iRoom.isIgnoreTooFar());
				distanceCheckChanged();
				iForm.addRow(MESSAGES.propDistanceCheck(), iDistanceCheck, 1);
			}
			iRoomCheck.setValue(!iRoom.isIgnoreRoomCheck());
			roomCheckChanged();
			iForm.addRow(MESSAGES.propRoomCheck(), iRoomCheck, 1);
			if (iGoogleMapControl != null) iGoogleMapControl.setVisible(true);
		} else {
			if (iRoom.hasCoordinates())
				if (iProperties != null && iProperties.hasEllipsoid())
					iForm.addRow(MESSAGES.propCoordinates(), new HTML(MESSAGES.coordinatesWithEllipsoid(iRoom.getX(), iRoom.getY(), iProperties.getEllipsoid())), 1);
				else
					iForm.addRow(MESSAGES.propCoordinates(), new HTML(MESSAGES.coordinates(iRoom.getX(), iRoom.getY())), 1);
			if (iRoom.getArea() != null)
				iForm.addRow(MESSAGES.propRoomArea(), new HTML(MESSAGES.roomArea(iRoom.getArea()) + " " + CONSTANTS.roomAreaUnitsShort()), 1);
			if (iProperties.isCanSeeCourses()) {
				iForm.addRow(MESSAGES.propDistanceCheck(), new Check(!room.isIgnoreTooFar(), MESSAGES.infoDistanceCheckOn(), MESSAGES.infoDistanceCheckOff()), 1);
				iForm.addRow(MESSAGES.propRoomCheck(), new Check(!room.isIgnoreRoomCheck(), MESSAGES.infoRoomCheckOn(), MESSAGES.infoRoomCheckOff()), 1);
			} else if (iProperties.isCanSeeEvents()) {
				iForm.addRow(MESSAGES.propRoomCheck(), new Check(!room.isIgnoreRoomCheck(), MESSAGES.infoRoomCheckOn(), MESSAGES.infoRoomCheckOff()), 1);
			}
			if (iGoogleMapControl != null) iGoogleMapControl.setVisible(false);
		}
		
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExamStatus()) || iRoom.isCanChangeExamStatus()) {
			for (Map.Entry<Long, CheckBox> e: iExaminationRooms.entrySet())
				e.getValue().setValue(false);
			if (iRoom.hasExamTypes()) {
				for (ExamTypeInterface type: iRoom.getExamTypes())
					iExaminationRooms.get(type.getId()).setValue(true);
			}
			iForm.addRow(MESSAGES.propExamRooms(), iExaminationRoomsPanel, 1);
			iExamCapacity.getWidget().setValue(iRoom.getExamCapacity());
			iForm.addRow(MESSAGES.propExamCapacity(), iExamCapacity, 1);
		} else if (iProperties.isCanSeeExams() && (iRoom.getExamCapacity() != null || iRoom.hasExamTypes())) {
			iForm.addRow(MESSAGES.propExamCapacity(), new RoomDetail.ExamSeatingCapacityLabel(iRoom), 1);
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeEventProperties()) || iRoom.isCanChangeEventProperties()) {
			iEventDepartment.clear();
			if ((iRoom.getUniqueId() == null || iRoom.getEventDepartment() != null) && ((iRoom.getUniqueId() == null && iProperties.isCanChangeControll()) || iRoom.isCanChangeControll()))
				iEventDepartment.addItem(MESSAGES.itemNoEventDepartment(), "-1");
			for (DepartmentInterface department: iProperties.getDepartments())
				if (department.isEvent())
					iEventDepartment.addItem(department.getDeptCode() + " - " + department.getLabel(), department.getId().toString());
			if (iRoom.getEventDepartment() == null) {
				iEventDepartment.setSelectedIndex(0);
			} else {
				iEventDepartment.setSelectedIndex(0);
				for (int i = 1; i < iEventDepartment.getItemCount(); i++) {
					if (iEventDepartment.getValue(i).equals(iRoom.getEventDepartment().getId().toString())) {
						iEventDepartment.setSelectedIndex(i); break;
					}
				}
				if (iRoom.getEventDepartment() != null && iEventDepartment.getSelectedIndex() == 0) {
					iEventDepartment.addItem(iRoom.getEventDepartment().getDeptCode() + " - " + iRoom.getEventDepartment().getLabel(), iRoom.getEventDepartment().getId().toString());
					iEventDepartment.setSelectedIndex(iEventDepartment.getItemCount() + - 1);
				}
			}
			iForm.addRow(MESSAGES.propEventDepartment(), iEventDepartment, 1);
			iEventStatus.setSelectedIndex(iRoom.getEventStatus() == null ? 0 : iRoom.getEventStatus() + 1);
			iForm.addRow(MESSAGES.propEventStatus(), iEventStatus, 1);
			iNote.getWidget().setText(iRoom.getEventNote() == null ? "" : iRoom.getEventNote());
			iForm.addRow(MESSAGES.propEventNote(), iNote, 1);
			iBreakTime.setValue(iRoom.getBreakTime());
			iForm.addRow(MESSAGES.propBreakTime(), iBreakTimePanel, 1);
		} else if (iProperties.isCanSeeEvents()) {
			if (iRoom.getEventDepartment() != null)
				iForm.addRow(MESSAGES.propEventDepartment(), new Label(RoomDetail.toString(iRoom.getEventDepartment(), true)), 1);
			if (iRoom.getEventStatus() != null || iRoom.getDefaultEventStatus() != null) {
				Label status = new Label(CONSTANTS.eventStatusName()[iRoom.getEventStatus() == null ? iRoom.getDefaultEventStatus() : iRoom.getEventStatus()]);
				if (iRoom.getEventStatus() == null) status.addStyleName("default");
				iForm.addRow(MESSAGES.propEventStatus(), status, 1);
			}
			if (iRoom.hasEventNote() || iRoom.hasDefaultEventNote()) {
				HTML note = new HTML(iRoom.hasEventNote() ? iRoom.getEventNote() : iRoom.getDefaultEventNote());
				if (!iRoom.hasEventNote()) note.addStyleName("default");
				iForm.addRow(MESSAGES.propEventNote(), note, 1);
			}
			if (iRoom.getBreakTime() != null || iRoom.getDefaultBreakTime() != null) {
				Label bt = new Label((iRoom.getBreakTime() == null ? iRoom.getDefaultBreakTime() : iRoom.getBreakTime()).toString());
				if (iRoom.getBreakTime() == null) bt.addStyleName("default");
				iForm.addRow(MESSAGES.propBreakTime(), bt, 1);
			}
		}
		
		if (iProperties.isGoogleMap()) {
			iForm.setWidget(firstRow, 2, iGoogleMap);
			iForm.getFlexCellFormatter().setRowSpan(firstRow, 2, iForm.getRowCount() - firstRow - 1);
		}
		
		if (((iRoom.getUniqueId() == null && iProperties.isCanChangeGroups()) || iRoom.isCanChangeGroups()) && !iProperties.getGroups().isEmpty()) {
			iForm.addHeaderRow(MESSAGES.headerRoomGroups());
			for (Map.Entry<Long, CheckBox> e: iGroups.entrySet())
				e.getValue().setValue(iRoom.hasGroup(e.getKey()));
			if (iGlobalGroupsPanel != null) {
				iForm.addRow(MESSAGES.propGlobalGroups(), iGlobalGroupsPanel);
			} else {
				List<GroupInterface> globalGroups = iRoom.getGlobalGroups();
				if (!globalGroups.isEmpty())
					iForm.addRow(MESSAGES.propGlobalGroups(), new RoomDetail.GroupsCell(globalGroups), 1);
			}
			for (DepartmentInterface dept: iProperties.getDepartments()) {
				P d = iGroupPanel.get(dept.getId());
				if (d != null)
					iForm.addRow(dept.getExtLabelWhenExist() + ":", d);
			}
		} else if (iRoom.hasGroups()) {
			iForm.addHeaderRow(MESSAGES.headerRoomGroups());			
			List<GroupInterface> globalGroups = iRoom.getGlobalGroups();
			if (!globalGroups.isEmpty())
				iForm.addRow(MESSAGES.propGlobalGroups(), new RoomDetail.GroupsCell(globalGroups), 1);
			List<GroupInterface> departmentalGroups = iRoom.getDepartmentalGroups(null);
			if (!departmentalGroups.isEmpty())
				iForm.addRow(MESSAGES.propDepartmenalGroups(), new RoomDetail.GroupsCell(departmentalGroups), 1);
		}
		
		if (((iRoom.getUniqueId() == null && iProperties.isCanChangeFeatures()) || iRoom.isCanChangeFeatures()) && !iProperties.getFeatures().isEmpty()) {
			iForm.addHeaderRow(MESSAGES.headerRoomFeatures());			
			for (Map.Entry<Long, CheckBox> e: iFeatures.entrySet())
				e.getValue().setValue(iRoom.hasFeature(e.getKey()));
			P features = new P("features");
			Map<Long, P> fp = new HashMap<Long, P>();
			for (FeatureInterface feature: iProperties.getFeatures()) {
				CheckBox ch = iFeatures.get(feature.getId());
				if (feature.getType() != null) {
					P d = fp.get(feature.getType().getId());
					if (d == null) {
						d = new P("features");
						d.setWidth("100%");
						fp.put(feature.getType().getId(), d);
					}
					d.add(ch);
				} else {
					features.add(ch);
				}
			}
			for (FeatureInterface feature: iRoom.getFeatures()) {
				if (!iFeatures.containsKey(feature.getId()) && feature.getDepartment() == null) {
					P f = new P("feature"); f.setText(feature.getTitle());
					if (feature.getType() != null) {
						P d = fp.get(feature.getType().getId());
						if (d == null) {
							d = new P("features");
							d.setWidth("100%");
							fp.put(feature.getType().getId(), d);
						}
						d.add(f);
					} else {
						features.add(f);
					}
				}
			}
			if (features.getWidgetCount() > 0)
				iForm.addRow(MESSAGES.propFeatures(), features);
			for (FeatureTypeInterface type: iProperties.getFeatureTypes()) {
				P d = fp.get(type.getId());
				if (d != null)
					iForm.addRow(type.getLabel() + ":", d);
			}
		} else if (iRoom.hasFeatures()) {
			iForm.addHeaderRow(MESSAGES.headerRoomFeatures());
			List<FeatureInterface> features = iRoom.getFeatures((Long)null);
			if (!features.isEmpty())
				iForm.addRow(MESSAGES.propFeatures(), new RoomDetail.FeaturesCell(features), 1);
			for (FeatureTypeInterface type: iProperties.getFeatureTypes()) {
				List<FeatureInterface> featuresOfType = iRoom.getFeatures(type);
				if (!featuresOfType.isEmpty())
					iForm.addRow(type.getLabel() + ":", new RoomDetail.FeaturesCell(featuresOfType), 1);
			}
		}
		
		if (iRoom.hasRoomSharingModel()) {
			iRoomSharingHeader.clearMessage();
			iRoomSharing.setEditable(iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability());
			iRoomSharing.setModel(iRoom.getRoomSharingModel());
			iRoomSharing.setVisible(true);
			if (iRoom.getUniqueId() == null) {
				iLastSelectedDepartmentId = Long.valueOf(iControllingDepartment.getWidget().getValue(iControllingDepartment.getWidget().getSelectedIndex()));
				if (iLastSelectedDepartmentId > 0)
					iRoomSharing.addOption(iLastSelectedDepartmentId);
			}
			iForm.addHeaderRow(iRoomSharingHeader);
			iForm.addRow(iRoomSharing);
		} else if (iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability()) {
			iForm.addHeaderRow(iRoomSharingHeader);
			iForm.addRow(iRoomSharing);
			iRoomSharingHeader.showLoading();
			iRoomSharing.setVisible(false);
			RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getSessionId(), iRoom.getUniqueId(), false, true), new AsyncCallback<RoomSharingModel>() {
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
					if (iRoom.getUniqueId() == null) {
						iLastSelectedDepartmentId = Long.valueOf(iControllingDepartment.getWidget().getValue(iControllingDepartment.getWidget().getSelectedIndex()));
						if (iLastSelectedDepartmentId > 0)
							iRoomSharing.addOption(iLastSelectedDepartmentId);
					}
				}
			});
		}
		
		if (iProperties.isCanEditRoomExams() || (iProperties.isCanSeeExams() && iRoom.isCanSeePeriodPreferences() && iRoom.hasExamTypes())) {
			iPeriodPreferencesHeaderRow = iForm.addHeaderRow(iPeriodPreferencesHeader);
			iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, false);
			for (ExamTypeInterface type: iProperties.getExamTypes()) {
				PeriodPreferencesWidget pref = iPeriodPreferences.get(type.getId());
				int row = iForm.addRow(MESSAGES.propExaminationPreferences(type.getLabel()), pref);
				iPeriodPreferencesRow.put(type.getId(), row);
				iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), false);
			}
			iPeriodPreferencesHeader.clearMessage();
			for (final ExamTypeInterface type: iProperties.getExamTypes()) {
				final PeriodPreferencesWidget pref = iPeriodPreferences.get(type.getId());
				if (iRoom.hasPeriodPreferenceModel(type.getId())) {
					pref.setEditable(iProperties.isCanEditRoomExams());
					pref.setModel(iRoom.getPeriodPreferenceModel(type.getId()));
					iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), iExaminationRooms.get(type.getId()).getValue());
					if (iExaminationRooms.get(type.getId()).getValue())
						iForm.getRowFormatter().setVisible(iPeriodPreferencesHeaderRow, true);
				} else if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExamStatus()) || iRoom.isCanChangeExamStatus()) {
					iPeriodPreferencesHeader.showLoading();
					iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), false);
					RPC.execute(RoomInterface.PeriodPreferenceRequest.load(iRoom.getSessionId(), iRoom.getUniqueId(), type.getId()), new AsyncCallback<PeriodPreferenceModel>() {
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
				} else {
					iForm.getRowFormatter().setVisible(iPeriodPreferencesRow.get(type.getId()), false);
				}
			}
		} else {
			iPeriodPreferencesHeaderRow = -1;
			iPeriodPreferencesRow.clear();
		}

		if (((iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability()) ||
			(iProperties.isCanSeeEvents() && iRoom.isCanSeeEventAvailability())) {
			iForm.addHeaderRow(iEventAvailabilityHeader);
			iForm.addRow(iEventAvailability);
			iEventAvailabilityHeader.setVisible(iEventDepartment.getSelectedIndex() > 0);
			if (iRoom.hasEventAvailabilityModel()) {
				iEventAvailabilityHeader.clearMessage();
				iEventAvailability.setEditable((iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability());
				iEventAvailability.setModel(iRoom.getEventAvailabilityModel());
				iEventAvailability.setVisible(iEventDepartment.getSelectedIndex() > 0);
			} else {
				iEventAvailabilityHeader.showLoading();
				iEventAvailability.setVisible(false);
				RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getSessionId(), iRoom.getUniqueId(), true), new AsyncCallback<RoomSharingModel>() {
					@Override
					public void onFailure(Throwable caught) {
						iEventAvailabilityHeader.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
					}
					@Override
					public void onSuccess(RoomSharingModel result) {
						iEventAvailabilityHeader.clearMessage();
						iEventAvailability.setEditable((iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability());
						iEventAvailability.setModel(result);
						iEventAvailability.setVisible(iEventDepartment.getSelectedIndex() > 0);
					}
				});
			}
		}
		
		if (iRoom.hasPictures() || (iRoom.getUniqueId() == null && iProperties.isCanChangePicture()) || iRoom.isCanChangePicture()) {
			iForm.addHeaderRow(iPicturesHeader);
			if ((iRoom.getUniqueId() == null && iProperties.isCanChangePicture()) || iRoom.isCanChangePicture())
				iForm.addRow(MESSAGES.propNewPicture(), iFileUpload);
			iForm.addRow(iPictures);
			iPictures.clearTable(1);
			
			if (iRoom.hasPictures()) {
				for (final RoomPictureInterface picture: iRoom.getPictures())
					iPictures.addRow(picture, line(picture));
			}
		}
		
		iForm.addBottomRow(iFooter);
		
		if (iRoom.getUniqueId() == null && iProperties.hasFutureSessions()) {
			int row = iForm.addHeaderRow(iApplyToHeader);
			iForm.getRowFormatter().addStyleName(row, "space-above");
			iApplyTo.clearTable(1);
			long id = 0;
			for (AcademicSessionInterface session: iProperties.getFutureSessions()) {
				List<Widget> line = new ArrayList<Widget>();
				CheckBox select = new CheckBox(); 
				line.add(select);
				line.add(new Label());
				line.add(new Label(session.getLabel()));
				Integer flags = RoomCookie.getInstance().getFutureFlags(session.getId());
				select.setValue(flags != null);
				for (FutureOperation op: FutureOperation.values()) {
					CheckBox ch = new CheckBox();
					ch.setValue(canFutureOperation(iRoom, op) && ((flags == null && op.getDefaultSelectionNewRoom()) || (flags != null && op.in(flags))));
					if (op == FutureOperation.ROOM_PROPERTIES) {
						ch.setValue(true);
						ch.setEnabled(false);
					}
					line.add(ch);
				}
				FutureRoomInterface fr = new FutureRoomInterface();
				fr.setSession(session);
				fr.setId(--id);
				iApplyTo.addRow(fr, line);
			}
			for (FutureOperation op: FutureOperation.values()) {
				iApplyTo.setColumnVisible(3 + op.ordinal(), canFutureOperation(iRoom, op));
			}
			iApplyTo.setColumnVisible(1, false);
			iForm.addRow(iApplyTo);
		} else if (iRoom.hasFutureRooms()) {
			int row = iForm.addHeaderRow(iApplyToHeader);
			iForm.getRowFormatter().addStyleName(row, "space-above");
			iApplyTo.clearTable(1);
			for (FutureRoomInterface fr: iRoom.getFutureRooms()) {
				List<Widget> line = new ArrayList<Widget>();
				CheckBox select = new CheckBox(); 
				line.add(select);
				line.add(new FutureRoomNameCell(fr));
				line.add(new Label(fr.getSession().getLabel()));
				Integer flags = RoomCookie.getInstance().getFutureFlags(fr.getSession().getId());
				select.setValue(flags != null);
				for (FutureOperation op: FutureOperation.values()) {
					CheckBox ch = new CheckBox();
					ch.setValue(canFutureOperation(iRoom, op) && ((flags == null && op.getDefaultSelection()) || (flags != null && op.in(flags))));
					line.add(ch);
				}
				iApplyTo.addRow(fr, line);
			}
			for (FutureOperation op: FutureOperation.values()) {
				iApplyTo.setColumnVisible(3 + op.ordinal(), canFutureOperation(iRoom, op));
			}
			iApplyTo.setColumnVisible(1, true);
			iForm.addRow(iApplyTo);
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
		if (iBuildingRow >= 0)
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
			final ListBox type = new ListBox(); type.setStyleName("unitime-TextBox");
			if (picture.getPictureType() == null) {
				type.addItem(MESSAGES.itemSelect(), "-1");
				for (AttachmentTypeInterface t: iProperties.getPictureTypes()) {
					type.addItem(t.getLabel(), t.getId().toString());
				}
				type.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						Long id = Long.valueOf(type.getValue(type.getSelectedIndex()));
						picture.setPictureType(iProperties.getPictureType(id));
					}
				});
			} else {
				final AttachmentTypeInterface last = picture.getPictureType();
				for (AttachmentTypeInterface t: iProperties.getPictureTypes()) {
					type.addItem(t.getLabel(), t.getId().toString());
				}
				boolean found = false;
				for (int i = 0; i < type.getItemCount(); i++) {
					if (type.getValue(i).equals(picture.getPictureType().getId().toString())) {
						type.setSelectedIndex(i); found = true; break;
					}
				}
				if (!found) {
					type.addItem(picture.getPictureType().getLabel(), picture.getPictureType().getId().toString());
					type.setSelectedIndex(type.getItemCount() - 1);
				}
				type.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						Long id = Long.valueOf(type.getValue(type.getSelectedIndex()));
						if (last.getId().equals(id))
							picture.setPictureType(last);
						else
							picture.setPictureType(iProperties.getPictureType(id));
					}
				});
			}
			line.add(type);
		} else {
			line.add(new Label(picture.getPictureType() == null ? "" : picture.getPictureType().getLabel()));
		}
		
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
	
	public boolean validate() {
		boolean result = true;
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeType()) {
			Long typeId = Long.valueOf(iType.getWidget().getValue(iType.getWidget().getSelectedIndex()));
			if (typeId < 0) {
				iType.setErrorHint(MESSAGES.errorRoomTypeMustBeSelected());
				result = false;
			}
			for (RoomTypeInterface type: iProperties.getRoomTypes()) {
				if (type.getId().equals(typeId))
					iRoom.setRoomType(type);
			}
			if (iRoom.getRoomType() == null) {
				iType.setErrorHint(MESSAGES.errorRoomTypeMustBeSelected());
				result = false;
			}
		}
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeRoomProperties()) {
			if (iRoom.getRoomType() != null && iRoom.getRoomType().isRoom()) {
				Long buildingId = Long.valueOf(iBuilding.getWidget().getValue(iBuilding.getWidget().getSelectedIndex()));
				if (buildingId < 0) {
					iBuilding.setErrorHint(MESSAGES.errorBuildingMustBeSelected(iRoom.getRoomType().getLabel()));
					result = false;
				} else {
					for (BuildingInterface building: iProperties.getBuildings()) {
						if (building.getId().equals(buildingId))
							iRoom.setBuilding(building);
					}
					if (iRoom.getBuilding() == null) {
						iBuilding.setErrorHint(MESSAGES.errorBuildingMustBeSelected(iRoom.getRoomType().getLabel()));
						result = false;
					}
				}
			}
			iRoom.setName(iName.getWidget().getText());
			if (iRoom.getName().isEmpty()) {
				if (iRoom.getRoomType() != null && iRoom.getRoomType().isRoom())
					iName.setErrorHint(MESSAGES.errorRoomNumberIsEmpty());
				else
					iName.setErrorHint(MESSAGES.errorLocationNameIsEmpty());
				result = false;
			}
			iRoom.setDisplayName(iDisplayName.getText());
			iRoom.setX(iX.toDouble());
			iRoom.setY(iY.toDouble());
			iRoom.setArea(iArea.toDouble());
			if (iProperties.isCanSeeCourses())
				iRoom.setIgnoreTooFar(!iDistanceCheck.getValue());
			iRoom.setIgnoreRoomCheck(!iRoomCheck.getValue());
		}
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExternalId()) || iRoom.isCanChangeExternalId()) {
			iRoom.setExternalId(iExternalId.getText());
		}
		if (iRoom.getUniqueId() == null || iRoom.isCanChangeCapacity()) {
			iRoom.setCapacity(iCapacity.getWidget().toInteger());
			if (iRoom.getCapacity() == null) {
				iCapacity.setErrorHint(MESSAGES.errorRoomCapacityIsEmpty());
				result = false;
			}
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeControll()) || iRoom.isCanChangeControll()) {
			Long deptId = Long.valueOf(iControllingDepartment.getWidget().getValue(iControllingDepartment.getWidget().getSelectedIndex()));
			if (deptId < 0) {
				iRoom.setControlDepartment(null);
			} else if (iLastControllingDept != null && deptId.equals(iLastControllingDept.getId())) {
				iRoom.setControlDepartment(iLastControllingDept);
			} else {
				for (DepartmentInterface dept: iProperties.getDepartments()) {
					if (deptId.equals(dept.getId()))
						iRoom.setControlDepartment(dept);
				}
			}
			if (iRoom.getControlDepartment() != null && iRoomSharing.getModel() != null) {
				boolean hasDepartment = false;
				if (iRoomSharing.getModel() != null) {
					for (RoomSharingOption opt: iRoomSharing.getModel().getOptions()) {
						if (iRoom.getControlDepartment().getId().equals(opt.getId())) { hasDepartment = true; break; }
					}
				}
				if (!hasDepartment)
					iControllingDepartment.setErrorHint(MESSAGES.errorControllingDepartmentNotAmongRoomSharing());
			}
		}

		if (iProperties.isCanEditRoomExams()) {
			iRoom.getExamTypes().clear();
			for (ExamTypeInterface type: iProperties.getExamTypes()) {
				if (iExaminationRooms.get(type.getId()).getValue()) {
					iRoom.getExamTypes().add(type);
					iRoom.setPeriodPreferenceModel(iPeriodPreferences.get(type.getId()).getModel());
				}
			}
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeExamStatus()) || iRoom.isCanChangeExamStatus()) {
			iRoom.setExamCapacity(iExamCapacity.getWidget().toInteger());
			if (iRoom.hasExamTypes() && iRoom.getExamCapacity() == null) {
				iExamCapacity.setErrorHint(MESSAGES.errorRoomExamCapacityIsEmpty());
				result = false;
			}
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeEventProperties()) || iRoom.isCanChangeEventProperties()) {
			Long deptId = Long.valueOf(iEventDepartment.getValue(iEventDepartment.getSelectedIndex()));
			if (deptId < 0) {
				iRoom.setEventDepartment(null);
			} else if (iLastEventDept != null && deptId.equals(iLastEventDept.getId())) {
				iRoom.setEventDepartment(iLastEventDept);
			} else {
				for (DepartmentInterface dept: iProperties.getDepartments()) {
					if (deptId.equals(dept.getId()))
						iRoom.setEventDepartment(dept);
				}
			}
			if (iEventStatus.getSelectedIndex() == 0)
				iRoom.setEventStatus(null);
			else
				iRoom.setEventStatus(iEventStatus.getSelectedIndex() - 1);
			iRoom.setEventNote(iNote.getWidget().getText().isEmpty() ? null : iNote.getWidget().getText());
			if (iNote.getWidget().getText().length() > 2048) {
				iNote.setErrorHint(MESSAGES.errorEventNoteTooLong());
				result = false;
			}
			iRoom.setBreakTime(iBreakTime.toInteger());
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeGroups()) || iRoom.isCanChangeGroups()) {
			for (GroupInterface group: iProperties.getGroups()) {
				if (iGroups.get(group.getId()).getValue()) {
					if (!iRoom.hasGroup(group.getId()))
						iRoom.addGroup(group);
				} else {
					if (iRoom.hasGroup(group.getId()))
						iRoom.removeGroup(group);
				}
			}
		}
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeFeatures()) || iRoom.isCanChangeFeatures()) {
			for (FeatureInterface feature: iProperties.getFeatures()) {
				if (iFeatures.get(feature.getId()).getValue()) {
					if (!iRoom.hasFeature(feature.getId()))
						iRoom.addFeature(feature);
				} else {
					if (iRoom.hasFeature(feature.getId()))
						iRoom.removeFeature(feature);
				}
			}
		}
		
		if (iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability()) {
			iRoom.setRoomSharingModel(iRoomSharing.getModel());
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability()) {
			iRoom.setEventAvailabilityModel(iEventAvailability.getModel());
		}
		
		if ((iRoom.getUniqueId() == null && iProperties.isCanChangePicture()) || iRoom.isCanChangePicture()) {
			iRoom.getPictures().clear();
			iRoom.getPictures().addAll(iPictures.getData());
		}
		
		if (iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability()) {
			boolean hasDepartment = (iRoom.getEventDepartment() != null);
			if (!hasDepartment && iRoomSharing.getModel() != null) {
				for (RoomSharingOption opt: iRoomSharing.getModel().getOptions()) {
					if (opt.getId() > 0) { hasDepartment = true; break; }
				}
			}
			if (!hasDepartment) {
				iRoomSharingHeader.setErrorMessage(MESSAGES.errorRoomHasNoDepartment());
				result = false;
			} else
				iRoomSharingHeader.clearMessage();
		}
		
		return result;
	}
	
	public boolean isGoogleMapEditable() {
		return iGoogleMapControl != null && iGoogleMapControl.isVisible();
	}
	
	public void hide(RoomDetailInterface room, boolean canShowDetail, String message) {
		setVisible(false);
		onHide(room, canShowDetail, message);
		Window.scrollTo(iLastScrollLeft, iLastScrollTop);
	}
	
	protected void onHide(RoomDetailInterface detail, boolean canShowDetail, String message) {
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
	
	protected String getFutureOperationLabel(FutureOperation op) {
		switch (op) {
		case ROOM_PROPERTIES:
			return MESSAGES.colChangeRoomProperties();
		case EXAM_PROPERTIES:
			return MESSAGES.colChangeExamProperties();
		case EVENT_PROPERTIES:
			return MESSAGES.colChangeEventProperties();
		case GROUPS:
			return MESSAGES.colChangeRoomGroups();
		case FEATURES:
			return MESSAGES.colChangeRoomFeatures();
		case ROOM_SHARING:
			return MESSAGES.colChangeRoomSharing();
		case EXAM_PREFS:
			return MESSAGES.colChangeRoomPeriodPreferences();
		case EVENT_AVAILABILITY:
			return MESSAGES.colChangeRoomEventAvailability();
		case PICTURES:
			return MESSAGES.colChangeRoomPictures();
		}
		return op.name();
	}
	
	protected boolean canFutureOperation(RoomDetailInterface room, FutureOperation op) {
		switch (op) {
		case ROOM_PROPERTIES:
			return iRoom.getRoomType() == null || iRoom.isCanChangeType() || iRoom.isCanChangeRoomProperties() || iRoom.isCanChangeExternalId() || iRoom.isCanChangeCapacity() || iRoom.isCanChangeControll();
		case EXAM_PROPERTIES:
			return (iRoom.getRoomType() == null && iProperties.isCanChangeExamStatus()) || iRoom.isCanChangeExamStatus();
		case EVENT_PROPERTIES:
			return (iRoom.getUniqueId() == null && iProperties.isCanChangeEventProperties()) || iRoom.isCanChangeEventProperties();
		case GROUPS:
			return ((iRoom.getUniqueId() == null && iProperties.isCanChangeGroups()) || iRoom.isCanChangeGroups()) && !iProperties.getGroups().isEmpty();
		case FEATURES:
			return ((iRoom.getUniqueId() == null && iProperties.isCanChangeFeatures()) || iRoom.isCanChangeFeatures()) && !iProperties.getFeatures().isEmpty();
		case ROOM_SHARING:
			return iProperties.isCanEditDepartments() || (iRoom.getUniqueId() == null && iProperties.isCanChangeAvailability()) || iRoom.isCanChangeAvailability();
		case EXAM_PREFS:
			return iProperties.isCanEditRoomExams();
		case EVENT_AVAILABILITY:
			return (iRoom.getUniqueId() == null && iProperties.isCanChangeEventAvailability()) || iRoom.isCanChangeEventAvailability();
		case PICTURES:
			return (iRoom.getUniqueId() == null && iProperties.isCanChangePicture()) || iRoom.isCanChangePicture();
		default:
			return false;
		}
	}
	
	protected void fillFutureFlags(RoomUpdateRpcRequest request, boolean includeWhenNoFlags) {
		request.clearFutureFlags();
		if (iRoom.getUniqueId() == null && iProperties.hasFutureSessions()) {
			for (int i = 1; i < iApplyTo.getRowCount(); i++) {
				CheckBox ch = (CheckBox)iApplyTo.getWidget(i, 0);
				if (ch.getValue()) {
					int flags = 0;
					for (FutureOperation op: FutureOperation.values()) {
						CheckBox x = (CheckBox)iApplyTo.getWidget(i, 3 + op.ordinal());
						if (x.getValue())
							flags = op.set(flags);
					}
					if (flags == 0 && !includeWhenNoFlags) continue;
					request.setFutureFlag(-iApplyTo.getData(1).getSession().getId(), flags);
					RoomCookie.getInstance().setFutureFlags(iApplyTo.getData(1).getSession().getId(), flags);
				} else {
					RoomCookie.getInstance().setFutureFlags(iApplyTo.getData(1).getSession().getId(), null);
				}
			}
		} else if (iRoom.hasFutureRooms()) {
			for (int i = 1; i < iApplyTo.getRowCount(); i++) {
				CheckBox ch = (CheckBox)iApplyTo.getWidget(i, 0);
				if (ch.getValue()) {
					int flags = 0;
					for (FutureOperation op: FutureOperation.values()) {
						CheckBox x = (CheckBox)iApplyTo.getWidget(i, 3 + op.ordinal());
						if (x.getValue())
							flags = op.set(flags);
					}
					if (flags == 0 && !includeWhenNoFlags) continue;
					request.setFutureFlag(iApplyTo.getData(1).getId(), flags);
					RoomCookie.getInstance().setFutureFlags(iApplyTo.getData(1).getSession().getId(), flags);
				} else  {
					RoomCookie.getInstance().setFutureFlags(iApplyTo.getData(1).getSession().getId(), null);
				}
			}
		}
	}
	
	protected String generateAlsoUpdateMessage(boolean includeWhenNoFlags) {
		if ((iRoom.getUniqueId() == null && iProperties.hasFutureSessions()) || iRoom.hasFutureRooms()) {
			List<String> ret = new ArrayList<String>();
			for (int i = 1; i < iApplyTo.getRowCount(); i++) {
				CheckBox ch = (CheckBox)iApplyTo.getWidget(i, 0);
				if (ch.getValue()) {
					int flags = 0;
					for (FutureOperation op: FutureOperation.values()) {
						CheckBox x = (CheckBox)iApplyTo.getWidget(i, 3 + op.ordinal());
						if (x.getValue())
							flags = op.set(flags);
					}
					if (flags == 0 && !includeWhenNoFlags) continue;
					ret.add(iApplyTo.getData(1).getSession().getLabel());
				}
			}
			if (!ret.isEmpty())
				return ToolBox.toString(ret);
		}
		return null;
	}
	
	class FutureRoomNameCell extends Label {
		FutureRoomNameCell(final FutureRoomInterface room) {
			super(room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel());
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomHint.showHint(FutureRoomNameCell.this.getElement(), room.getId(), null, null, true);
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomHint.hideHint();
				}
			});
		}
	}
	
	protected void roomCheckChanged() {
		iRoomCheck.setHTML(iRoomCheck.getValue() ? MESSAGES.infoRoomCheckOn() : MESSAGES.infoRoomCheckOff());
		if (iRoomCheck.getValue()) {
			iRoomCheck.addStyleName("check-enabled");
			iRoomCheck.removeStyleName("check-disabled");
		} else {
			iRoomCheck.addStyleName("check-disabled");
			iRoomCheck.removeStyleName("check-enabled");
		}
	}
	
	protected void distanceCheckChanged() {
		iDistanceCheck.setHTML(iDistanceCheck.getValue() ? MESSAGES.infoDistanceCheckOn() : MESSAGES.infoDistanceCheckOff());
		if (iDistanceCheck.getValue()) {
			iDistanceCheck.addStyleName("check-enabled");
			iDistanceCheck.removeStyleName("check-disabled");
		} else {
			iDistanceCheck.addStyleName("check-disabled");
			iDistanceCheck.removeStyleName("check-enabled");
		}
	}
}
