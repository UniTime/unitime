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
