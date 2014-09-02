/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.core.shared.GWT;

/**
 * @author Tomas Muller
 */
public class UniTimeFrameDialog {
	private static UniTimeFrameDialogDisplay sDialog = null;

	public static native void createTriggers()/*-{
		$wnd.showGwtDialog = function(title, source, width, height) {
			@org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog::openDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(title, source, width, height);
		};
		$wnd.hideGwtDialog = function() {
			@org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog::hideDialog()();
		};
		$wnd.hasGwtDialog = function() {
			return @org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog::hasDialog()();
		};
	}-*/;
	
	public static void openDialog(String title, String source) {
		openDialog(title, source, null, null);
	}
	
	public static void openDialog(String title, String source, String width, String height) {
		if (sDialog == null)
			sDialog = GWT.create(UniTimeFrameDialogDisplay.class);
		sDialog.openDialog(title, source, width, height);
	}
	
	public static void hideDialog() {
		if (sDialog != null && sDialog.isShowing()) sDialog.hideDialog();
	}
	
	public static boolean hasDialog() {
		return sDialog != null && sDialog.isShowing();
	}
}
