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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;

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
		if (sDialog == null) {
			if (Window.getClientWidth() <= 800)
				sDialog = GWT.create(UniTimeFrameDialogDisplay.Mobile.class);
			else
				sDialog = GWT.create(UniTimeFrameDialogDisplay.class);
		}
		sDialog.openDialog(title, source, width, height);
	}
	
	public static void hideDialog() {
		if (sDialog != null && sDialog.isShowing()) sDialog.hideDialog();
	}
	
	public static boolean hasDialog() {
		return sDialog != null && sDialog.isShowing();
	}
}
