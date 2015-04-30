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
package org.unitime.timetable.gwt.client.rooms;

import java.util.Date;

import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFlag;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class RoomCookie {
	private static RoomCookie sInstance = null;
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
	private int[] iFlags = null;
	private String[] iHash = null;
	private int iSortRoomsBy = 0;
	private int iDeptMode = 1;
	private boolean iRoomsHorizontal = true;
	private String iMode = "";
	
	private RoomCookie() {
		iFlags = new int[RoomsPageMode.values().length];
		iHash = new String[RoomsPageMode.values().length];
		for (int i = 0; i < iFlags.length; i++) {
			iFlags[i] = RoomsPageMode.values()[i].getFlags();
			iHash[i] = RoomsPageMode.values()[i].getQuery();
		}
		try {
			String cookie = Cookies.getCookie("UniTime:Room");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iSortRoomsBy = Integer.valueOf(params[idx++]);
				iRoomsHorizontal = !"F".equals(params[idx++]);
				iMode = params[idx++];
				iDeptMode = Integer.valueOf(params[idx++]);
				for (int i = 0; i < iFlags.length; i++) {
					iFlags[i] = Integer.parseInt(params[idx++]);
					iHash[i] = params[idx++];
				}
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = iSortRoomsBy + "|" + (iRoomsHorizontal ? "T" : "F") + "|" + iMode + "|" + iDeptMode;
		for (int i = 0; i < iFlags.length; i++) {
			cookie += "|" + iFlags[i] + "|" + (iHash[i] == null ? "" : iHash[i]);
		}
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Room", cookie, expires);
	}
	
	public static RoomCookie getInstance() { 
		if (sInstance == null)
			sInstance = new RoomCookie();
		return sInstance;
	}
	
	public boolean get(RoomsPageMode mode, RoomFlag f) { return f.in(iFlags[mode.ordinal()]); }
	public void set(RoomsPageMode mode, RoomFlag f, boolean value) {
		iFlags[mode.ordinal()] = (value ? f.set(iFlags[mode.ordinal()]) : f.clear(iFlags[mode.ordinal()]));
		save();
	}
	public int getFlags(RoomsPageMode mode) { return iFlags[mode.ordinal()]; }
	
	public boolean hasHash(RoomsPageMode mode) {
		return iHash[mode.ordinal()] != null && !iHash[mode.ordinal()].isEmpty();
	}
	
	public String getHash(RoomsPageMode mode) {
		return iHash[mode.ordinal()];
	}
	
	public void setHash(RoomsPageMode mode, String hash) {
		iHash[mode.ordinal()] = hash;
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
	
	public String getMode() {
		return iMode;
	}
	
	public boolean hasMode() {
		return !iMode.isEmpty();
	}
	
	public void setMode(boolean horizontal, String mode) {
		iRoomsHorizontal = horizontal;
		iMode = (mode == null ? "" : mode);
		save();
	}
	
	public int getDeptMode() {
		return iDeptMode;
	}
	
	public void setDeptMode(int deptMode) {
		iDeptMode = deptMode;
		save();
	}
}
