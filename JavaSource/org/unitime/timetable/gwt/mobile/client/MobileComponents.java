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
package org.unitime.timetable.gwt.mobile.client;

import org.unitime.timetable.gwt.mobile.client.page.MobileMenu;
import org.unitime.timetable.gwt.mobile.client.page.ReportFormFactor;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public enum MobileComponents {
	detectFormFactor("UniTimeGWT:DetectFormFactor", new ComponentFactory() { public void insert(RootPanel panel) { ReportFormFactor.report(panel); } }),
	menubar_mobile("UniTimeGWT:MobileMenu", new ComponentFactory() { public void insert(RootPanel panel) { new MobileMenu().insert(panel); } }),
	;
	private String iId;
	private ComponentFactory iFactory;
	private boolean iMultiple = false;
	
	MobileComponents(String id, ComponentFactory factory) { iId = id; iFactory = factory; }
	MobileComponents(String id, boolean multiple, ComponentFactory factory) { iId = id; iFactory = factory; iMultiple = multiple; }
	public String id() { return iId; }
	public void insert(RootPanel panel) { iFactory.insert(panel); }
	public boolean isMultiple() { return iMultiple; }
	
	public interface ComponentFactory {
		void insert(RootPanel panel);
	}
}
