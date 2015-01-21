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
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class RoomSharingPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private RoomSharingWidget iSharing;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private Long iLocationId;
	private boolean iEventAvailability;
	
	public RoomSharingPage() {
		iEventAvailability = "1".equals(Window.Location.getParameter("events"));
		if (iEventAvailability)
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEditRoomEventAvailability());
		iForm = new SimpleForm();
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("update", MESSAGES.buttonUpdate(), 75, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				iHeader.showLoading();
				RPC.execute(RoomInterface.RoomSharingRequest.save(iLocationId, iSharing.getModel(), iEventAvailability), new AsyncCallback<RoomInterface.RoomSharingModel>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedToSaveRoomAvailability(caught.getMessage()));
					}
					
					@Override
					public void onSuccess(RoomInterface.RoomSharingModel result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "roomDetail.do?id=" + iLocationId);
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "roomDetail.do?id=" + iLocationId);
			}
		});
		
		iHeader.setEnabled("update", false);

		iForm.addHeaderRow(iHeader);
		
		iSharing = new RoomSharingWidget(true);
		iSharing.setVisible(false);
		
		iForm.addRow(iSharing);
		
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		iFooter.setVisible(false);
				
		initWidget(iForm);
		
		try {
			iLocationId = Long.valueOf(Window.Location.getParameter("id"));
			LoadingWidget.getInstance().show(MESSAGES.waitLoadingRoomAvailability());
			RPC.execute(RoomInterface.RoomSharingRequest.load(iLocationId, iEventAvailability), new AsyncCallback<RoomInterface.RoomSharingModel>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MESSAGES.failedToSaveRoomAvailability(caught.getMessage()));
				}
				
				@Override
				public void onSuccess(RoomInterface.RoomSharingModel result) {
					LoadingWidget.getInstance().hide();
					iSharing.setModel(result);
					iSharing.setVisible(true);
					if (iSharing.isEditable()) iHeader.setEnabled("update", true);
					iFooter.setVisible(true);
					iHeader.setHeaderTitle(result.getName());
				}
			});
			
		} catch (Exception e) {
			iHeader.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(e.getMessage()));
		}
		
	}

}
