/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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

import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.RoomInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class RoomSharingHint extends PopupPanel {
	private static RoomSharingHint sInstance;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private RoomSharingWidget iSharing;
	private Long iLocationId = null;
	
	public RoomSharingHint() {
		super();
		setStyleName("unitime-PopupHint");
		iSharing = new RoomSharingWidget(false);
		setWidget(iSharing.getPanel());
	}
	
	public void setModel(RoomInterface.RoomSharingModel model) {
		iSharing.setModel(model);
	}
	
	public void setLocationId(Long locationId) { iLocationId = locationId; }
	public Long getLoncationId() { return iLocationId; }
	
	/** Never use from GWT code */
	public static void _showHint(JavaScriptObject source, String locationId) {
		showHint((Element) source.cast(), Long.valueOf(locationId));
	}
	
	public static void showHint(final Element relativeObject, final long locationId) {
		getInstance().setLocationId(locationId);
		RPC.execute(RoomInterface.RoomSharingRequest.load(locationId), new AsyncCallback<RoomInterface.RoomSharingModel>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(RoomInterface.RoomSharingModel result) {
				if (locationId != getInstance().getLoncationId()) return;
				getInstance().iSharing.setModel(result);
				getInstance().setPopupPositionAndShow(new PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
					    int textBoxOffsetWidth = relativeObject.getOffsetWidth();
					    int offsetWidthDiff = offsetWidth - textBoxOffsetWidth;
					    int left;
					    if (LocaleInfo.getCurrentLocale().isRTL()) {
						      int textBoxAbsoluteLeft = relativeObject.getAbsoluteLeft();
						      left = textBoxAbsoluteLeft - offsetWidthDiff;
						      if (offsetWidthDiff > 0) {
							        int windowRight = Window.getClientWidth() + Window.getScrollLeft();
							        int windowLeft = Window.getScrollLeft();
							        int textBoxLeftValForRightEdge = textBoxAbsoluteLeft + textBoxOffsetWidth;
							        int distanceToWindowRight = windowRight - textBoxLeftValForRightEdge;
							        int distanceFromWindowLeft = textBoxLeftValForRightEdge - windowLeft;
							        if (distanceFromWindowLeft < offsetWidth && distanceToWindowRight >= offsetWidthDiff) {
								          left = textBoxAbsoluteLeft;
							        }
						      }
					    } else {
						      left = relativeObject.getAbsoluteLeft();
						      if (offsetWidthDiff > 0) {
							        int windowRight = Window.getClientWidth() + Window.getScrollLeft();
							        int windowLeft = Window.getScrollLeft();
							        int distanceToWindowRight = windowRight - left;
							        int distanceFromWindowLeft = left - windowLeft;
							        if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff) {
								          left -= offsetWidthDiff;
							        }
						      }
					    }
					    int top = relativeObject.getAbsoluteTop();
					    int windowTop = Window.getScrollTop();
					    int windowBottom = Window.getScrollTop() + Window.getClientHeight();
					    int distanceFromWindowTop = top - windowTop;
					    int distanceToWindowBottom = windowBottom - (top + relativeObject.getOffsetHeight());
					    if (distanceToWindowBottom < offsetHeight && distanceFromWindowTop >= offsetHeight) {
						      top -= offsetHeight;
					    } else {
						      top += relativeObject.getOffsetHeight();
					    }
					    getInstance().setPopupPosition(left, top);
					}
				});				
			}
		});
	}
	
	public static void hideHint() {
		getInstance().setLocationId(null);
		getInstance().hide();
	}
	
	public static RoomSharingHint getInstance() {
		if (sInstance == null) {
			sInstance = new RoomSharingHint();
		}
		return sInstance;
	}

	
	public static native void createTriggers()/*-{
	$wnd.showGwtRoomAvailabilityHint = function(source, content) {
		@org.unitime.timetable.gwt.client.rooms.RoomSharingHint::_showHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
	};
	$wnd.hideGwtRoomAvailabilityHint = function() {
		@org.unitime.timetable.gwt.client.rooms.RoomSharingHint::hideHint()();
	};
}-*/;


}
