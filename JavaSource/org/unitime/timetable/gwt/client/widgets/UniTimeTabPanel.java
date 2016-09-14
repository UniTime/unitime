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

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link TabPanel} wrapper to avoid deprecation warnings.
 * To be used only for pages not based on LayoutPanel (and preferably avoided in the future development).
 */

/**
 * @author Tomas Muller
 */
public class UniTimeTabPanel extends TabPanel {
	public UniTimeTabPanel() {
		super();
	}

	public void add(Widget w, String tabText, boolean asHTML) {
		super.add(w, tabText, asHTML);
	}
	
	public void insert(Widget widget, String tabText, boolean asHTML, int beforeIndex) {
		super.insert(widget, tabText, asHTML, beforeIndex);
	}
	
	public void setDeckSize(String width, String height) {
		getDeckPanel().setSize(width, height);
	}
	
	public void setDeckStyleName(String style) {
		getDeckPanel().setStyleName(style);
	}

	public void selectTab(int index) {
		super.selectTab(index);
	}
	
	public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
		return super.addSelectionHandler(handler);
	}
	
	public int getTabCount() {
		return getTabBar().getTabCount();
	}
	
	public int getSelectedTab() {
		return getTabBar().getSelectedTab();
	}
}
