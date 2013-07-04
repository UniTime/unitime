/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.admin.ClearHibernateCache;
import org.unitime.timetable.gwt.client.page.Refresh;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.rooms.RoomSharingHint;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;

import com.google.gwt.user.client.Command;

/**
 * @author Tomas Muller
 */
public enum Triggers {
	gwtHint(new Command() {
		public void execute() {
			GwtHint.createTriggers();
		}}),
	lookup(new Command() {
		public void execute() {
			Lookup.createTriggers();
		}
	}),
	gwtDialog(new Command() {
		public void execute() {
			UniTimeFrameDialog.createTriggers();
		}
	}),
	loading(new Command() {
		public void execute() {
			LoadingWidget.createTriggers();
		}
	}),
	refresh(new Command() {
		public void execute() {
			Refresh.createTriggers();
		}
	}),
	notifications(new Command() {
		public void execute() {
			UniTimeNotifications.createTriggers();
		}
	}),
	roomAvailabilityHint(new Command() {
		public void execute() {
			RoomSharingHint.createTriggers();
		}
	}),
	roomHint(new Command() {
		public void execute() {
			RoomHint.createTriggers();
		}
	}),
	timeHint(new Command() {
		public void execute() {
			TimeHint.createTriggers();
		}
	}),
	clearHibCache(new Command() {
		public void execute() {
			ClearHibernateCache.createTriggers();
		}
	})
	;
	
	private Command iCommand;
	
	Triggers(Command registerCommand) { iCommand = registerCommand; }
	
	public void register() { iCommand.execute(); }

}
