/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.events;

import com.google.gwt.user.client.Cookies;

public class EventCookie {
	private int iFlags = EventFlag.SHOW_PUBLISHED_TIME.flag() + EventFlag.SHOW_MAIN_CONTACT.flag();
	private static EventCookie sInstance = null;
	
	public static enum EventFlag {
		SHOW_PUBLISHED_TIME,
		SHOW_ALLOCATED_TIME,
		SHOW_SETUP_TIME,
		SHOW_TEARDOWN_TIME,
		SHOW_CAPACITY,
		SHOW_LIMIT,
		SHOW_ENROLLMENT,
		SHOW_MAIN_CONTACT;
		
		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}
	
	private EventCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Event");
			if (cookie != null) {
				String[] params = cookie.split(":");
				iFlags = Integer.parseInt(params[0]);
			}
		} catch (Exception e) {}
	}
	
	private void save() {
		Cookies.setCookie("UniTime:Event",
				String.valueOf(iFlags)
				);
	}
	
	public static EventCookie getInstance() { 
		if (sInstance == null)
			sInstance = new EventCookie();
		return sInstance;
	}
	
	public boolean get(EventFlag f) { return f.in(iFlags); }
	public void set(EventFlag f, boolean value) {
		iFlags = (value ? f.set(iFlags) : f.clear(iFlags));
		save();
	}
	
}
