/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.instructor;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityRequest;
import org.unitime.timetable.gwt.client.rooms.RoomSharingWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.RoomInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class InstructorAvailabilityHint {
	private static RoomSharingWidget sSharing;
	private static String sLastInstructorId = null;
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public static RoomSharingWidget content(RoomInterface.RoomSharingModel model) {
		if (sSharing == null)
			sSharing = new InstructorAvailabilityWidget();
		sSharing.setModel(model);
		return sSharing;
	}
	
	/** Never use from GWT code */
	public static void _showInstructorSharingHint(JavaScriptObject source, String instructorId) {
		showHint((Element) source.cast(), instructorId);
	}
	
	public static void showHint(final Element relativeObject, final String instructorId) {
		sLastInstructorId = instructorId;
		RPC.execute(InstructorAvailabilityRequest.load(instructorId), new AsyncCallback<InstructorAvailabilityModel>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			
			@Override
			public void onSuccess(InstructorAvailabilityModel result) {
				if (instructorId.equals(sLastInstructorId) && result != null)
					GwtHint.showHint(relativeObject, content(result));
			}
		});
	}
	
	public static void hideHint() {
		GwtHint.hideHint();
	}
	
	public static native void createTriggers()/*-{
	$wnd.showGwtInstructorAvailabilityHint = function(source, content) {
		@org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityHint::_showInstructorSharingHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
	};
	$wnd.hideGwtInstructorAvailabilityHint = function() {
		@org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityHint::hideHint()();
	};
	}-*/;

}
