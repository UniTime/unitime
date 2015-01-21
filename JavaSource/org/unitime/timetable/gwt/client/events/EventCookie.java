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
package org.unitime.timetable.gwt.client.events;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class EventCookie {
	private int iFlags = EventInterface.sDefaultEventFlags;
	private Map<String, String> iHash = new HashMap<String, String>();
	private boolean iShowDeltedMeetings = true;
	private int iSortRoomsBy = -1;
	private boolean iRoomsHorizontal = true;
	private boolean iExpandRoomConflicts = false;
	private static EventCookie sInstance = null;
	private boolean iAutomaticallyApproveNewMeetings = false;
	private boolean iHideDuplicitiesForMeetings = false;
	
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
				iAutomaticallyApproveNewMeetings = "T".equals(params[idx++]);
				iHideDuplicitiesForMeetings = "T".equals(params[idx++]);
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
				"|" + (iExpandRoomConflicts ? "T" : "F") +
				"|" + (iAutomaticallyApproveNewMeetings ? "T": "F") +
				"|" + (iHideDuplicitiesForMeetings ? "T" : "F");
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
	
	public boolean isAutomaticallyApproveNewMeetings() {
		return iAutomaticallyApproveNewMeetings;
	}
	
	public void setAutomaticallyApproveNewMeetings(boolean autoApprove) {
		iAutomaticallyApproveNewMeetings = autoApprove;
		save();
	}
	
	public boolean isHideDuplicitiesForMeetings() {
		return iHideDuplicitiesForMeetings;
	}
	
	public void setHideDuplicitiesForMeetings(boolean hideDuplicitiesForMeetings) {
		iHideDuplicitiesForMeetings = hideDuplicitiesForMeetings;
		save();
	}
}
