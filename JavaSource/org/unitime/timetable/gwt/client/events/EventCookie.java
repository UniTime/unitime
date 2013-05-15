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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;

import com.google.gwt.user.client.Cookies;

public class EventCookie {
	private int iFlags = EventInterface.sDefaultEventFlags;
	private Map<String, String> iHash = new HashMap<String, String>();
	private boolean iShowDeltedMeetings = true;
	private int iSortRoomsBy = -1;
	private boolean iRoomsHorizontal = true;
	private boolean iExpandRoomConflicts = false;
	private static EventCookie sInstance = null;
	
	private EventCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Event");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iFlags = Integer.parseInt(params[idx++]);
				iShowDeltedMeetings = "T".equals(params[idx++]);
				iSortRoomsBy = Integer.valueOf(params[idx++]);
				iRoomsHorizontal = !"F".equals(params[idx++]);
				iExpandRoomConflicts = "T".equals(params[idx++]);
				while (idx < params.length) {
					String hash = params[idx++];
					int colon = hash.indexOf(':');
					iHash.put(hash.substring(0, colon), hash.substring(colon + 1));
				}
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = String.valueOf(iFlags) + 
				"|" + (iShowDeltedMeetings ? "T": "F") +
				"|" + iSortRoomsBy +
				"|" + (iRoomsHorizontal ? "T" : "F") +
				"|" + (iExpandRoomConflicts ? "T" : "F");
		for (Map.Entry<String, String> entry: iHash.entrySet())
			cookie += "|" + entry.getKey() + ":" + entry.getValue();
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Event", cookie, expires);
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
		String hash = getHash(type);
		return hash != null && !hash.isEmpty();
	}
	
	public String getHash(String type) {
		return iHash.get(type);
	}
	
	public void setHash(String type, String hash) {
		iHash.put(type, hash);
		save();
	}
	
	public boolean isShowDeletedMeetings() {
		return iShowDeltedMeetings;
	}
	
	public void setShowDeletedMeetings(boolean show) {
		iShowDeltedMeetings = show;
		save();
	}
	
	public int getRoomsSortBy() {
		return iSortRoomsBy;
	}
	
	public void setSortRoomsBy(int sortRoomsBy) {
		iSortRoomsBy = sortRoomsBy;
		save();
	}
	
	public boolean areRoomsHorizontal() {
		return iRoomsHorizontal;
	}
	
	public void setRoomsHorizontal(boolean roomsHorizontal) {
		iRoomsHorizontal = roomsHorizontal;
		save();
	}
	
	public boolean isExpandRoomConflicts() {
		return iExpandRoomConflicts;
	}
	
	public void setExpandRoomConflicts(boolean expandRoomConflicts) {
		iExpandRoomConflicts = expandRoomConflicts;
		save();
	}
}
