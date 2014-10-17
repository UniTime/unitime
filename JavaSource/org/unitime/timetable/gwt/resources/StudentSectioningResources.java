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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Tomas Muller
 */
public interface StudentSectioningResources extends ClientBundle {
	@Source("org/unitime/timetable/gwt/resources/icons/up_Down.png")
	ImageResource up_Down();

	@Source("org/unitime/timetable/gwt/resources/icons/up.png")
	ImageResource up();

	@Source("org/unitime/timetable/gwt/resources/icons/up_Over.png")
	ImageResource up_Over();

	@Source("org/unitime/timetable/gwt/resources/icons/down_Down.png")
	ImageResource down_Down();

	@Source("org/unitime/timetable/gwt/resources/icons/down.png")
	ImageResource down();

	@Source("org/unitime/timetable/gwt/resources/icons/down_Over.png")
	ImageResource down_Over();

	@Source("org/unitime/timetable/gwt/resources/icons/search_picker_Disabled.png")
	ImageResource search_picker_Disabled();

	@Source("org/unitime/timetable/gwt/resources/icons/search_picker_Down.png")
	ImageResource search_picker_Down();

	@Source("org/unitime/timetable/gwt/resources/icons/search_picker_Normal.png")
	ImageResource search_picker_Normal();

	@Source("org/unitime/timetable/gwt/resources/icons/search_picker_Over.png")
	ImageResource search_picker_Over();

	@Source("org/unitime/timetable/gwt/resources/icons/search_picker.png")
	ImageResource search_picker();
	
	@Source("org/unitime/timetable/gwt/resources/icons/roadrunner16.png")
	ImageResource distantConflict();
	
	@Source("org/unitime/timetable/gwt/resources/icons/tick.png")
	ImageResource saved();
	
	@Source("org/unitime/timetable/gwt/resources/icons/lock.png")
	ImageResource locked();

	@Source("org/unitime/timetable/gwt/resources/icons/lock_unlock.png")
	ImageResource unlocked();
	
	@Source("org/unitime/timetable/gwt/resources/icons/warning.png")
	ImageResource courseLocked();
	
	@Source("org/unitime/timetable/gwt/resources/icons/error.png")
	ImageResource error();

	@Source("org/unitime/timetable/gwt/resources/icons/printer.png")
	ImageResource print();

	@Source("org/unitime/timetable/gwt/resources/icons/letter.png")
	ImageResource email();
	
	@Source("org/unitime/timetable/gwt/resources/icons/date.png")
	ImageResource calendar();

	@Source("org/unitime/timetable/gwt/resources/icons/comments.png")
	ImageResource comments();

	@Source("org/unitime/timetable/gwt/resources/icons/group.png")
	ImageResource highDemand();
	
	@Source("org/unitime/timetable/gwt/resources/icons/action_delete.png")
	ImageResource unassignment();

	@Source("org/unitime/timetable/gwt/resources/icons/action_add.png")
	ImageResource assignment();

	@Source("org/unitime/timetable/gwt/resources/icons/expand_node_btn.gif")
	ImageResource treeClosed();

	@Source("org/unitime/timetable/gwt/resources/icons/collapse_node_btn.gif")
	ImageResource treeOpen();
	
	@Source("org/unitime/timetable/gwt/resources/icons/loading_small.gif")
	ImageResource loading_small();

	@Source("org/unitime/timetable/gwt/resources/icons/note.png")
	ImageResource note();
	
	@Source("org/unitime/timetable/gwt/resources/icons/overlap.png")
	ImageResource overlap();
}
