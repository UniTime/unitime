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
