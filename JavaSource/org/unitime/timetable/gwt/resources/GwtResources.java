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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Tomas Muller
 */
public interface GwtResources extends ClientBundle, com.google.gwt.user.client.ui.Tree.Resources {

	@Source("org/unitime/timetable/gwt/resources/icons/loading.gif")
	ImageResource loading();

	@Source("org/unitime/timetable/gwt/resources/icons/loading_small.gif")
	ImageResource loading_small();

	@Source("org/unitime/timetable/gwt/resources/icons/help.png")
	ImageResource help();

	@Source("org/unitime/timetable/gwt/resources/icons/expand_node_btn.gif")
	ImageResource treeClosed();

	@Source("org/unitime/timetable/gwt/resources/icons/collapse_node_btn.gif")
	ImageResource treeOpen();

	@Source("org/unitime/timetable/gwt/resources/icons/end_node_btn.gif")
	ImageResource treeLeaf();

	@Source("org/unitime/timetable/gwt/resources/icons/minimize.gif")
	ImageResource menu_opened();

	@Source("org/unitime/timetable/gwt/resources/icons/minimize_RO.gif")
	ImageResource menu_opened_hover();

	@Source("org/unitime/timetable/gwt/resources/icons/openMenu.gif")
	ImageResource menu_closed();

	@Source("org/unitime/timetable/gwt/resources/icons/openMenu_RO.gif")
	ImageResource menu_closed_hover();

	@Source("org/unitime/timetable/gwt/resources/icons/action_add.png")
	ImageResource add();

	@Source("org/unitime/timetable/gwt/resources/icons/action_delete.png")
	ImageResource delete();

	@Source("org/unitime/timetable/gwt/resources/icons/accept.png")
	ImageResource on();

	@Source("org/unitime/timetable/gwt/resources/icons/cross.png")
	ImageResource off();
	
	@Source("org/unitime/timetable/gwt/resources/icons/application_edit.png")
	ImageResource edit();

	@Source("org/unitime/timetable/gwt/resources/icons/date.png")
	ImageResource calendar();

	@Source("org/unitime/timetable/gwt/resources/icons/download.png")
	ImageResource download();
	
	@Source("org/unitime/timetable/gwt/resources/icons/times.png")
	ImageResource filter_clear();
	
	@Source("org/unitime/timetable/gwt/resources/icons/dropdown_close.png")
	ImageResource filter_close();
	
	@Source("org/unitime/timetable/gwt/resources/icons/dropdown_open.png")
	ImageResource filter_open();
	
	@Source("org/unitime/timetable/gwt/resources/icons/white_star.png")
	ImageResource star();

	@Source("org/unitime/timetable/gwt/resources/icons/black_star.png")
	ImageResource starSelected();

	@Source("org/unitime/timetable/gwt/resources/icons/confirm.png")
	ImageResource confirm();

	@Source("org/unitime/timetable/gwt/resources/icons/alert.png")
	ImageResource alert();
}
