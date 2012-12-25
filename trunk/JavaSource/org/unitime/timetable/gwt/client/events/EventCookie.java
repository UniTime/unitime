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

import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;

import com.google.gwt.user.client.Cookies;

public class EventCookie {
	private int iFlags = EventInterface.sDefaultEventFlags;
	private String iType = null;
	private String iHash = null;
	private boolean iShowDeltedMeetings = true;
	private static EventCookie sInstance = null;
	
	private EventCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Event");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				iFlags = Integer.parseInt(params[0]);
				iType = params[1];
				iHash = params[2];
				iShowDeltedMeetings = "T".equals(params[3]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		Cookies.setCookie("UniTime:Event",
				String.valueOf(iFlags) + "|" + (iType == null ? "" : iType) + "|" + (iHash == null ? "" : iHash) +
				"|" + (iShowDeltedMeetings ? "T": "F")
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
	public int getFlags() { return iFlags; }
	
	public boolean hasHash(String type) {
		return type.equals(iType) && iHash != null && !iHash.isEmpty();
	}
	
	public String getHash(String type) {
		return (type.equals(iType) ? iHash : null);
	}
	
	public void setHash(String type, String hash) {
		iType = type; iHash = hash;
		save();
	}
	
	public boolean isShowDeletedMeetings() {
		return iShowDeltedMeetings;
	}
	
	public void setShowDeletedMeetings(boolean show) {
		iShowDeltedMeetings = show;
		save();
	}
}
