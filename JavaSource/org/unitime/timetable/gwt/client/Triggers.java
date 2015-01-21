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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.admin.ClearHibernateCache;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityHint;
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
	}),
	instructorAvailabilityHint(new Command() {
		public void execute() {
			InstructorAvailabilityHint.createTriggers();
		}
	}),
	;

	private Command iCommand;
	
	Triggers(Command registerCommand) { iCommand = registerCommand; }
	
	public void register() { iCommand.execute(); }

}
