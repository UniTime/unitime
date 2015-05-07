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
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
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
	private Boolean iGridAsText = null;
	private Boolean iHorizontal = null;
	private String iMode = "";
	
	private RoomCookie() {
		iFlags = new int[RoomsPageMode.values().length];
		iHash = new String[RoomsPageMode.values().length];
		for (int i = 0; i < iFlags.length; i++) {
			iFlags[i] = RoomsPageMode.values()[i].getColumns();
			iHash[i] = RoomsPageMode.values()[i].getQuery();
		}
		try {
			String cookie = Cookies.getCookie("UniTime:Room");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iSortRoomsBy = Integer.valueOf(params[idx++]);
				setOrientation(params[idx++]);
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
		String cookie = iSortRoomsBy + "|" + getOrientation() + "|" + iDeptMode;
		for (int i = 0; i < iFlags.length; i++) {
			cookie += "|" + iFlags[i] + "|" + (iHash[i] == null ? "" : iHash[i]);
		}
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Room", cookie, expires);
	}
	
	private void setOrientation(String parameter) {
		if (parameter == null || parameter.isEmpty()) {
			iGridAsText = null;
			iHorizontal = null;
			iMode = "";
		} else {
			switch (parameter.charAt(0)) {
			case 'a': iHorizontal = null; iGridAsText = true; break;
			case 'h': iHorizontal = true; iGridAsText = true; break;
			case 'v': iHorizontal = false; iGridAsText = true; break;
			case 'A': iHorizontal = null; iGridAsText = false; break;
			case 'H': iHorizontal = true; iGridAsText = false; break;
			case 'V': iHorizontal = false; iGridAsText = false; break;
			case 'b': iHorizontal = true; iGridAsText = null; break;
			case 'B': iHorizontal = false; iGridAsText = null; break;
			default: iHorizontal = null; iGridAsText = null;
			}
			iMode = parameter.substring(1);
		}
	}
	
	private String getOrientation() {
		char orientation = 'X';
		if (iGridAsText == null) {
			orientation = (iHorizontal == null ? 'X' : iHorizontal.booleanValue() ? 'b' : 'B');
		} else if (iGridAsText.booleanValue()) {
			orientation = (iHorizontal == null ? 'a' : iHorizontal.booleanValue() ? 'h' : 'v');
		} else {
			orientation = (iHorizontal == null ? 'A' : iHorizontal.booleanValue() ? 'H' : 'V');
		}
		return orientation + (iMode == null ? "" : iMode);
	}
	
	public static RoomCookie getInstance() { 
		if (sInstance == null)
			sInstance = new RoomCookie();
		return sInstance;
	}
	
	public boolean get(RoomsPageMode mode, RoomsColumn f) { return f.in(iFlags[mode.ordinal()]); }
	public void set(RoomsPageMode mode, RoomsColumn f, boolean value) {
		iFlags[mode.ordinal()] = (value ? f.set(iFlags[mode.ordinal()]) : f.clear(iFlags[mode.ordinal()]));
		save();
	}
	public boolean get(RoomsPageMode mode, int ftIndex) {
		int flag = (1 << (RoomsColumn.values().length + ftIndex));
		return (iFlags[mode.ordinal()] & flag) == 0;
	}
	public void set(RoomsPageMode mode, int ftIndex, boolean value) {
		int flag = (1 << (RoomsColumn.values().length + ftIndex));
		boolean in = ((iFlags[mode.ordinal()] & flag) != 0);
		if (!value && !in)
			iFlags[mode.ordinal()] += flag;
		if (value && in)
			iFlags[mode.ordinal()] -= flag;
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
		return iHorizontal == null ? false : iHorizontal.booleanValue();
	}
	
	public boolean isGridAsText() {
		return iGridAsText == null ? false : iGridAsText.booleanValue();
	}
	
	public boolean hasOrientation() {
		return iHorizontal != null && iGridAsText != null;
	}
	
	public void setOrientation(boolean gridAsText, boolean horizontal) {
		iGridAsText = gridAsText;
		iHorizontal = horizontal;
		save();
	}
	
	public String getMode() {
		return iMode;
	}
	
	public boolean hasMode() {
		return iMode != null && !iMode.isEmpty();
	}
	
	public void setMode(boolean horizontal, String mode) {
		iMode = mode;
		iHorizontal = horizontal;
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
