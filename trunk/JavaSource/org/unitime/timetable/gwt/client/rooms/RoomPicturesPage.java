/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureRequest.Apply;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

/**
 * @author Tomas Muller
 */
public class RoomPicturesPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	
	private SimpleForm iForm;
	private UniTimeTable<RoomPictureInterface> iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private UniTimeFileUpload iFileUpload;
	private ListBox iApply;
	
	private Long iLocationId = null;
	
	public RoomPicturesPage() {
		iForm = new SimpleForm();
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("update", MESSAGES.buttonUpdate(), 75, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				iHeader.showLoading();
				RPC.execute(RoomPictureRequest.save(iLocationId, Apply.values()[iApply.getSelectedIndex()], iTable.getData()), new AsyncCallback<RoomPictureResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedToSaveRoomPictures(caught.getMessage()));
					}
					
					@Override
					public void onSuccess(RoomPictureResponse result) {
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
		
		
		iApply = new ListBox();
		for (Apply apply: Apply.values()) {
			String item = apply.name();
			switch (apply) {
			case THIS_SESSION_ONLY:
				item = MESSAGES.itemThisSessionOnly();
				break;
			case ALL_FUTURE_SESSIONS:
				item = MESSAGES.itemAllFutureSessions();
				break;
			case ALL_SESSIONS:
				item = MESSAGES.itemAllSessions();
				break;
			}
			iApply.addItem(item, apply.name());
		}
		iForm.addRow(MESSAGES.propAppliesTo(), iApply);
		iApply.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iHeader.setEnabled("update", true);
			}
		});
		
		iTable = new UniTimeTable<RoomPictureInterface>();
		iTable.setStyleName("unitime-RoomPictures");
		
		iFileUpload = new UniTimeFileUpload();
		iForm.addRow(MESSAGES.propNewPicture(), iFileUpload);
		iFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				RPC.execute(RoomPictureRequest.upload(iLocationId), new AsyncCallback<RoomPictureResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedToUploadRoomPicture(caught.getMessage()));
					}
					
					@Override
					public void onSuccess(RoomPictureResponse result) {
						if (result.hasPictures()) {
							for (final RoomPictureInterface picture: result.getPictures()) {
								for (int row = 1; row < iTable.getRowCount(); row ++)
									if (picture.getName().equals(iTable.getData(row).getName())) {
										iTable.removeRow(row);
										break;
									}
								iTable.addRow(picture, line(picture));
							}
							iHeader.setEnabled("update", true);
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
		iTable.addRow(null, header);
		
		iForm.addRow(iTable);
		
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		iFooter.setVisible(false);

		initWidget(iForm);
		
		try {
			iLocationId = Long.valueOf(Window.Location.getParameter("id"));
			LoadingWidget.getInstance().show(MESSAGES.waitLoadingRoomPictures());
			RPC.execute(RoomPictureRequest.load(iLocationId), new AsyncCallback<RoomPictureResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MESSAGES.failedToSaveRoomAvailability(caught.getMessage()));
				}
				
				@Override
				public void onSuccess(RoomPictureResponse result) {
					LoadingWidget.getInstance().hide();
					if (result.hasPictures())
						for (final RoomPictureInterface picture: result.getPictures())
							iTable.addRow(picture, line(picture));
					iApply.setSelectedIndex(result.getApply().ordinal());
					iFooter.setVisible(true);
					iHeader.setHeaderTitle(result.getName());
					iFileUpload.reset();
				}
			});
			
		} catch (Exception e) {
			iHeader.setErrorMessage(MESSAGES.failedToLoadRoomPictures(e.getMessage()));
		}
	}

	private List<Widget> line(final RoomPictureInterface picture) {
		List<Widget> line = new ArrayList<Widget>();
		
		Image image = new Image(GWT.getHostPageBaseURL() + "picture?id=" + picture.getUniqueId());
		image.addStyleName("image");
		line.add(image);
		
		line.add(new Label(picture.getName()));
		line.add(new Label(picture.getType()));
		
		Image remove = new Image(RESOURCES.delete());
		remove.setTitle(MESSAGES.titleDeleteRow());
		remove.addStyleName("remove");
		remove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for (int row = 1; row < iTable.getRowCount(); row ++)
					if (picture.getUniqueId().equals(iTable.getData(row).getUniqueId())) {
						iTable.removeRow(row);
						break;
					}
				iHeader.setEnabled("update", true);
				event.stopPropagation();
			}
		});
		line.add(remove);
		
		return line;
	}
}
