/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.DialogBox;

public class WebTableDialogBox extends DialogBox {
	private WebTable iTable;

	public WebTableDialogBox(WebTable table) {
		super();
		iTable = table;
	}
	
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
		if (DOM.eventGetType((Event) event.getNativeEvent()) == Event.ONKEYUP) {
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_DOWN) {
				iTable.setSelectedRow(iTable.getSelectedRow()+1);
			} else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_UP) {
				iTable.setSelectedRow(iTable.getSelectedRow()==0?iTable.getRowsCount()-1:iTable.getSelectedRow()-1);
			} else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ENTER) {
				if (iTable.getSelectedRow() > 0)
					iTable.fireDoubleRowClickEvent(null, iTable.getSelectedRow());
			}  else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE) {
				hide();
			}
		}
	}

}
