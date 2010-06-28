/*
 * UniTime 3.2 (University Timetabling Application)
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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GwtResources extends ClientBundle, com.google.gwt.user.client.ui.Tree.Resources {

	@Source("org/unitime/timetable/gwt/resources/icons/loading.gif")
	ImageResource loading();

	@Source("org/unitime/timetable/gwt/resources/icons/loading_small.gif")
	ImageResource loading_small();

	@Source("org/unitime/timetable/gwt/resources/icons/help_icon.gif")
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
}
