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

import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateRoomDepartmentsRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class RoomDepartmentsEdit extends Composite {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private RoomPropertiesInterface iProperties;
	private DepartmentInterface iDepartment = null;
	private ExamTypeInterface iExamType = null;
	
	private RoomsTable iRooms = null;
	
	public RoomDepartmentsEdit() {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-RoomDepartmentsEdit");
		
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("update", MESSAGES.buttonUpdate(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UpdateRoomDepartmentsRequest request = new UpdateRoomDepartmentsRequest();
				request.setSessionId(iProperties.getAcademicSessionId());
				request.setDepartment(iDepartment);
				request.setExamType(iExamType);
				for (int i = 1; i < iRooms.getRowCount(); i++) {
					RoomDetailInterface room = iRooms.getData(i);
					boolean wasSelected = false;
					if (iDepartment != null) {
						wasSelected = (room.getDepartment(iDepartment.getId()) != null);
					} else if (iExamType != null) {
						wasSelected = (room.getExamType(iExamType.getId()) != null);
					}
					boolean selected = iRooms.isRoomSelected(i);
					if (selected != wasSelected) {
						if (selected)
							request.addLocation(room.getUniqueId());
						else
							request.dropLocation(room.getUniqueId());
					}
				}
				if (!request.hasAddLocations() && !request.hasDropLocations()) {
					hide(false);
					return;
				}
				LoadingWidget.getInstance().show(MESSAGES.waitUpdatingRoomDepartments());
				RPC.execute(request, new AsyncCallback<GwtRpcResponseNull>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.errorFailedToUpdateRoomDepartments(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.errorFailedToUpdateRoomDepartments(caught.getMessage()));
					}

					@Override
					public void onSuccess(GwtRpcResponseNull result) {
						LoadingWidget.getInstance().hide();
						hide(true);
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide(false);
			}
		});

		iForm.addHeaderRow(iHeader);
		
		iRooms = new RoomsTable(RoomsPageMode.COURSES, true);
		iForm.addRow(iRooms);
		iRooms.addMouseClickListener(new MouseClickListener<RoomDetailInterface>() {
			@Override
			public void onMouseClick(TableEvent<RoomDetailInterface> event) {
				iHeader.clearMessage();
			}
		});
		
		iFooter = iHeader.clonePanel();
		iForm.addBottomRow(iFooter);
		
		initWidget(iForm);
	}
	
	public void setProperties(RoomPropertiesInterface properties) {
		iProperties = properties;
		iRooms.setProperties(iProperties);
	}
	
	private void hide(boolean refresh) {
		setVisible(false);
		onHide(refresh);
		Window.scrollTo(iLastScrollLeft, iLastScrollTop);
	}
	
	public void hide() {
		hide(true);
	}
	
	protected void onHide(boolean refresh) {
	}
	
	protected void onShow() {
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEditRoomsDepartments());
		setVisible(true);
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		onShow();
		Window.scrollTo(0, 0);
	}
	
	public boolean setDepartmentOrExamType(String code) {
		if (iProperties == null) return false;
		iDepartment = null; iExamType = null;
		for (ExamTypeInterface type: iProperties.getExamTypes())
			if (type.getReference().equals(code)) {
				iHeader.setHeaderTitle(MESSAGES.examinationRooms(type.getLabel()));
				iExamType = type;
				return true;
			}
		for (DepartmentInterface department: iProperties.getDepartments())
			if (department.getDeptCode().equals(code)) {
				iHeader.setHeaderTitle(department.getExtAbbreviationWhenExist() + " - " + department.getExtLabelWhenExist());
				iDepartment = department; return true;
			}
		return false;
	}
	
	public boolean canEdit() {
		if (iDepartment != null) {
			return iDepartment.isCanEditRoomSharing();
		} else if (iExamType != null) {
			return iProperties.isCanEditRoomExams();
		}
		return false;
	}
	
	public boolean setDepartmentOrExamType(RoomFilterBox filter) {
		Chip dept = filter.getChip("department");
		if (dept != null) {
			iRooms.setDepartment(dept.getValue());
			return setDepartmentOrExamType(dept.getValue());
		}
		return false;
	}
	
	public void setRooms(List<Entity> rooms, Integer sortBy) {
		iRooms.clearTable(1);
		iHeader.clearMessage();
		ValueChangeHandler<Boolean> clearErrorMessage = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iHeader.clearMessage();
			}
		};
		for (Entity e: rooms) {
			RoomDetailInterface room = (RoomDetailInterface)e;
			int row = iRooms.addRoom(room);
			boolean selected = false;
			if (iDepartment != null) {
				selected = (room.getDepartment(iDepartment.getId()) != null);
			} else if (iExamType != null) {
				selected = (room.getExamType(iExamType.getId()) != null);
			}
			iRooms.selectRoom(row, selected);
			iRooms.setSelected(row, selected);
			iRooms.getRoomSelection(row).addValueChangeHandler(clearErrorMessage);
		}
		if (sortBy != null)
			iRooms.setSortBy(sortBy);
	}
}
