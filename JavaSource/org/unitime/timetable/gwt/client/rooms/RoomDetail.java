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

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.DepartmentCell;
import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class RoomDetail extends Composite {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);

	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private RoomPropertiesInterface iProperties = null;
	private RoomDetailInterface iRoom = null;
	private RoomsPageMode iMode = null;
	
	public RoomDetail(RoomsPageMode mode) {
		iMode = mode;
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("edit", MESSAGES.buttonEditRoom(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				edit();
			}
		});
		iHeader.addButton("previous", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RoomDetailInterface prev = getPrevious(getRoom().getUniqueId());
				if (prev != null) previous(prev);
			}
		});
		iHeader.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RoomDetailInterface next = getNext(getRoom().getUniqueId());
				if (next != null) next(next);
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ("1".equals(Location.getParameter("back")))
					ToolBox.open(GWT.getHostPageBaseURL() + "back.do");
				else
					hide();
			}
		});
		
		iForm = new SimpleForm(3);
		iForm.addStyleName("unitime-RoomDetail");
		
		iFooter = iHeader.clonePanel();
		
		initWidget(iForm);
	}
	
	private int iLastScrollLeft, iLastScrollTop;
	public void show() {
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRoomDetail());
		setVisible(true);
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		onShow();
		Window.scrollTo(0, 0);
	}
	
	public void show(String message) {
		show();
		if (message != null)
			iHeader.setErrorMessage(message);
		else
			iHeader.clearMessage();
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
	
	protected void edit() {
	}
	
	protected RoomDetailInterface getNext(Long roomId) { return null; }
	protected void next(RoomDetailInterface event) {}
	
	protected RoomDetailInterface getPrevious(Long roomId) { return null; }
	protected void previous(RoomDetailInterface previous) {}
	
	public void setProperties(RoomPropertiesInterface properties) { iProperties = properties; }
	
	public RoomDetailInterface getRoom() { return iRoom; }
	
	public void setRoom(RoomDetailInterface room) {
		iRoom = room;
		
		iForm.clear();

		iHeader.clearMessage();
		iHeader.setHeaderTitle(iRoom.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : iRoom.getLabel());
		iHeader.setEnabled("edit", iRoom.isCanEdit());
		iHeader.setEnabled("previous", getPrevious(iRoom.getUniqueId()) != null);
		iHeader.setEnabled("next", getNext(iRoom.getUniqueId()) != null);
		iForm.addHeaderRow(iHeader);
		
		boolean courses = iProperties != null && iProperties.isCanSeeCourses();
		boolean exams = iProperties != null && iProperties.isCanSeeExams();
		boolean events = iProperties != null && iProperties.isCanSeeEvents();
		
		int firstRow = iForm.getRowCount();
		
		if (iMode.hasSessionSelection())
			iForm.addRow(MESSAGES.propAcademicSession(), new Label(iRoom.hasSessionName() ? iRoom.getSessionName() : iProperties.getAcademicSessionName()), 1);
		
		iForm.addRow(MESSAGES.propType(), new Label(iRoom.getRoomType().getLabel()), 1);
		if (iRoom.hasExternalId()) {
			iForm.addRow(MESSAGES.propExternalId(), new Label(iRoom.getExternalId()), 1);
		}
		if (iRoom.getCapacity() != null)
			iForm.addRow(MESSAGES.propCapacity(), new Label(iRoom.getCapacity().toString()), 1);
		if (exams && (iRoom.getExamCapacity() != null || iRoom.hasExamTypes()))
			iForm.addRow(MESSAGES.propExamCapacity(), new ExamSeatingCapacityLabel(iRoom), 1);
		if (courses && iRoom.getControlDepartment() != null)
			iForm.addRow(MESSAGES.propControllingDepartment(), new Label(RoomDetail.toString(iRoom.getControlDepartment())), 1);
		if (iRoom.hasCoordinates())
			if (iProperties != null && iProperties.hasEllipsoid())
				iForm.addRow(MESSAGES.propCoordinates(), new HTML(MESSAGES.coordinatesWithEllipsoid(iRoom.getX(), iRoom.getY(), iProperties.getEllipsoid())), 1);
			else
				iForm.addRow(MESSAGES.propCoordinates(), new HTML(MESSAGES.coordinates(iRoom.getX(), iRoom.getY())), 1);
		if (iRoom.getArea() != null)
			iForm.addRow(MESSAGES.propRoomArea(), new HTML(MESSAGES.roomArea(iRoom.getArea()) + " " + (iProperties != null && iProperties.isRoomAreaInMetricUnits() ? CONSTANTS.roomAreaMetricUnitsShort() : CONSTANTS.roomAreaUnitsShort())), 1);
		if (courses) {
			iForm.addRow(MESSAGES.propDistanceCheck(), new Check(!room.isIgnoreTooFar(), MESSAGES.infoDistanceCheckOn(), MESSAGES.infoDistanceCheckOff()));
			iForm.addRow(MESSAGES.propRoomCheck(), new Check(!room.isIgnoreRoomCheck(), MESSAGES.infoRoomCheckOn(), MESSAGES.infoRoomCheckOff()));
		} else if (events) {
			iForm.addRow(MESSAGES.propRoomCheck(), new Check(!room.isIgnoreRoomCheck(), MESSAGES.infoRoomCheckOn(), MESSAGES.infoRoomCheckOff()));
		}
		if (events && iRoom.getEventDepartment() != null)
			iForm.addRow(MESSAGES.propEventDepartment(), new Label(RoomDetail.toString(iRoom.getEventDepartment(), true)), 1);
		if (events && (iRoom.getEventStatus() != null || iRoom.getDefaultEventStatus() != null)) {
			Label status = new Label(CONSTANTS.eventStatusName()[iRoom.getEventStatus() == null ? iRoom.getDefaultEventStatus() : iRoom.getEventStatus()]);
			if (iRoom.getEventStatus() == null) status.addStyleName("default");
			iForm.addRow(MESSAGES.propEventStatus(), status, 1);
		}
		if (events && (iRoom.hasEventNote() || iRoom.hasDefaultEventNote())) {
			HTML note = new HTML(iRoom.hasEventNote() ? iRoom.getEventNote() : iRoom.getDefaultEventNote());
			if (!iRoom.hasEventNote()) note.addStyleName("default");
			iForm.addRow(MESSAGES.propEventNote(), note, 1);
		}
		if (events && (iRoom.getBreakTime() != null || iRoom.getDefaultBreakTime() != null)) {
			Label bt = new Label((iRoom.getBreakTime() == null ? iRoom.getDefaultBreakTime() : iRoom.getBreakTime()).toString());
			if (iRoom.getBreakTime() == null) bt.addStyleName("default");
			iForm.addRow(MESSAGES.propBreakTime(), bt, 1);
		}
		if (courses && iRoom.hasPreference())
			iForm.addRow(MESSAGES.propPreference(), new PreferenceCell(iRoom.getDepartments()), 1);
		List<GroupInterface> globalGroups = iRoom.getGlobalGroups();
		if (!globalGroups.isEmpty())
			iForm.addRow(MESSAGES.propGlobalGroups(), new GroupsCell(globalGroups), 1);
		List<GroupInterface> departmentalGroups = iRoom.getDepartmentalGroups(null);
		if (!departmentalGroups.isEmpty())
			iForm.addRow(MESSAGES.propDepartmenalGroups(), new GroupsCell(departmentalGroups), 1);
		List<FeatureInterface> features = iRoom.getFeatures((Long)null);
		if (!features.isEmpty())
			iForm.addRow(MESSAGES.propFeatures(), new FeaturesCell(features), 1);
		if (iProperties != null) {
			for (FeatureTypeInterface type: iProperties.getFeatureTypes()) {
				List<FeatureInterface> featuresOfType = iRoom.getFeatures(type);
				if (!featuresOfType.isEmpty())
					iForm.addRow(type.getLabel() + ":", new FeaturesCell(featuresOfType), 1);
			}
		}
		if (iRoom.hasLastChange())
			iForm.addRow(MESSAGES.propLastChange(), new Label(iRoom.getLastChange()), 1);
		
		if (iRoom.hasMapUrl()) {
			Image image = new Image(iRoom.getMapUrl()); image.setStyleName("map");
			iForm.setWidget(firstRow, 2, image);
			iForm.getFlexCellFormatter().setRowSpan(firstRow, 2, iForm.getRowCount() - firstRow);
		}
		
		if (courses && iRoom.isCanSeeAvailability()) {
			final UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.headerRoomSharing());
			header.showLoading();
			iForm.addHeaderRow(header);
			
			final RoomSharingWidget sharing = new RoomSharingWidget(false);
			sharing.setVisible(false);
			iForm.addRow(sharing);
			RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getSessionId(), iRoom.getUniqueId(), false), new AsyncCallback<RoomSharingModel>() {
				@Override
				public void onFailure(Throwable caught) {
					header.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
				}
				@Override
				public void onSuccess(RoomSharingModel result) {
					header.clearMessage();
					sharing.setModel(result);
					sharing.setVisible(true);
				}
			});
		}
		
		if (exams && iRoom.isCanSeePeriodPreferences() && iRoom.hasExamTypes()) {
			final UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.headerExaminationPeriodPreferences());
			header.showLoading();
			iForm.addHeaderRow(header);
			
			for (ExamTypeInterface type: iRoom.getExamTypes()) {
				final PeriodPreferencesWidget pref = new PeriodPreferencesWidget(false);
				final int row = iForm.addRow(MESSAGES.propExaminationPreferences(type.getLabel()), pref);
				iForm.getRowFormatter().setVisible(row, false);
				
				RPC.execute(RoomInterface.PeriodPreferenceRequest.load(iRoom.getSessionId(), iRoom.getUniqueId(), type.getId()), new AsyncCallback<PeriodPreferenceModel>() {
					@Override
					public void onFailure(Throwable caught) {
						header.setErrorMessage(MESSAGES.failedToLoadPeriodPreferences(caught.getMessage()));
					}

					@Override
					public void onSuccess(PeriodPreferenceModel result) {
						header.clearMessage();
						pref.setModel(result);
						if (!result.getPeriods().isEmpty())
							iForm.getRowFormatter().setVisible(row, true);
					}
				});
			}
		}
		
		if (events && iRoom.isCanSeeEventAvailability()) {
			final UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.headerEventAvailability());
			header.showLoading();
			iForm.addHeaderRow(header);
			final RoomSharingWidget sharing = new RoomSharingWidget(false);
			sharing.setVisible(false);
			iForm.addRow(sharing);
			RPC.execute(RoomInterface.RoomSharingRequest.load(iRoom.getSessionId(), iRoom.getUniqueId(), true), new AsyncCallback<RoomSharingModel>() {
				@Override
				public void onFailure(Throwable caught) {
					header.setErrorMessage(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
				}
				@Override
				public void onSuccess(RoomSharingModel result) {
					header.clearMessage();
					sharing.setModel(result);
					sharing.setVisible(true);
				}
			});
		}
		
		if (iRoom.hasPictures()) {
			UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.headerRoomPictures());
			iForm.addHeaderRow(header);
			iForm.addRow(new PicturesCell(iRoom));
		}
		
		if (iRoom.isCanShowDetail()) {
			RoomNoteChanges noteChanges = new RoomNoteChanges();
			noteChanges.load(iRoom.getUniqueId());
			iForm.addRow(noteChanges);
		}
		
		iForm.addNotPrintableBottomRow(iFooter);
	}
	
	static class ExamSeatingCapacityLabel extends Label {
		ExamSeatingCapacityLabel(RoomDetailInterface room) {
			if (room.hasExamTypes()) {
				String types = "";
				for (ExamTypeInterface type: room.getExamTypes())
					types += (types.isEmpty() ? "" : ", ") + type.getLabel();
				if (room.getExamCapacity() != null)
					setText(room.getExamCapacity() + " (" + types + ")");
				else
					setText("(" + types + ")");
			} else if (room.getExamCapacity() != null) {
				setText(room.getExamCapacity().toString());
			}
		}
	}
	
	static class GroupsCell extends P {
		GroupsCell(List<? extends GroupInterface> groups) {
			this(groups, true);
		}
		
		GroupsCell(List<? extends GroupInterface> groups, boolean department) {
			super();
			setStyleName("groups");
			for (GroupInterface group: groups) {
				P p = new P("group");
				p.setText(group.getLabel());
				if (group.getTitle() != null) p.setTitle(group.getTitle());
				if (group.getDepartment() != null && department)
					p.setText(group.getLabel() + " (" + RoomDetail.toString(group.getDepartment()) + ")");
				add(p);
			}
		}
	}
	
	static class FeaturesCell extends P {
		FeaturesCell(List<? extends FeatureInterface> features) {
			super();
			setStyleName("features");
			for (FeatureInterface feature: features) {
				P p = new P("feature");
				p.setText(feature.getLabel());
				if (feature.getTitle() != null) p.setTitle(feature.getTitle());
				if (feature.getDepartment() != null)
					p.setText(feature.getLabel() + " (" + RoomDetail.toString(feature.getDepartment()) + ")");
				add(p);
			}
		}
	}
	
	static class PreferenceCell extends DepartmentCell {
		PreferenceCell(List<DepartmentInterface> departments) {
			super(true);
			for (DepartmentInterface department: departments) {
				if (department.getPreference() == null) continue;
				P p = new P("department");
				p.setText(MESSAGES.roomPreference(RoomDetail.toString(department), department.getPreference().getName()));
				p.setTitle(department.getPreference().getName() + " " + department.getExtLabelWhenExist());
				p.getElement().getStyle().setColor(department.getPreference().getColor());
				iP.put(department, p);
				add(p);
			}
		}
	}
	
	static class PicturesCell extends P {
		PicturesCell(RoomDetailInterface room) {
			super("pictures");
			if (room.hasPictures()) {
				for (RoomPictureInterface picture: room.getPictures()) {
					add(picture.getPictureType() == null || picture.getPictureType().isImage() ? new PictureCell(picture) : new LinkCell(picture));
				}
			}
		}
	}
	
	static class PictureCell extends Image {
		private P iPopupWidget;
		private RoomPictureInterface iPicture;
		
		PictureCell(RoomPictureInterface picture) {
			super();
			iPicture = picture;
			setStyleName("picture");
			setUrl(GWT.getHostPageBaseURL() + "picture?id=" + picture.getUniqueId());
			setAltText(picture.getName() + (picture.getPictureType() == null ? "" : " (" + picture.getPictureType().getAbbreviation() + ")"));
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					GwtHint.showHint(getElement(), getPopupWidget());
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					GwtHint.hideHint();
				}
			});
		}
		
		protected P getPopupWidget() {
			if (iPopupWidget == null) {
				iPopupWidget = new P("unitime-RoomPictureHint");
				Image image = new Image(GWT.getHostPageBaseURL() + "picture?id=" + iPicture.getUniqueId());
				image.setStyleName("picture");
				iPopupWidget.add(image);
				P caption = new P("caption");
				caption.setText(iPicture.getName() + (iPicture.getPictureType() == null ? "" : " (" + iPicture.getPictureType().getAbbreviation() + ")"));
				iPopupWidget.add(caption);
			}
			return iPopupWidget;
		}
	}
	
	static class LinkCell extends ImageLink {
		LinkCell(RoomPictureInterface picture) {
			super(new Image(RESOURCES.download()), GWT.getHostPageBaseURL() + "picture?id=" + picture.getUniqueId());
			setStyleName("link");
			setTitle(picture.getName() + (picture.getPictureType() == null ? "" : " (" + picture.getPictureType().getLabel() + ")"));
			setText(picture.getName() + (picture.getPictureType() == null ? "" : " (" + picture.getPictureType().getAbbreviation() + ")"));
		}
	}
	
	static String toString(DepartmentInterface department, boolean events) {
		if (events)
			return department.getDeptCode() + " - " + department.getLabel();
		else
			return department.getExtAbbreviationOrCode() + " - " + department.getExtLabelWhenExist();
	}
	
	static String toString(DepartmentInterface department) {
		return toString(department, false);
	}
	
	static class Check extends P {
		Check(boolean value, String onMessage, String offMessage) {
			Image image = new Image(value ? RESOURCES.on() : RESOURCES.off());
			image.addStyleName("image");
			add(image);
			InlineHTML text = new InlineHTML(value ? onMessage : offMessage);
			text.addStyleName("message");
			add(text);
			if (value)
				addStyleName("check-enabled");
			else
				addStyleName("check-disabled");
		}
		
	}

}
