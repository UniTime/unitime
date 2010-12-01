/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseRoomGroupPref;

import net.sf.cpsolver.ifs.util.ToolBox;



public class RoomGroupPref extends BaseRoomGroupPref {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomGroupPref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomGroupPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public String preferenceText() { 
		return (this.getRoomGroup().getName());
    }

    public String preferenceAbbv() { 
        return (this.getRoomGroup().getAbbv());
    }

    public Object clone() {
    	RoomGroupPref pref = new RoomGroupPref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setRoomGroup(getRoomGroup());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof RoomGroupPref)) return false;
    	return ToolBox.equals(getRoomGroup(),((RoomGroupPref)other).getRoomGroup());
    }

	public String preferenceTitle() {
		return getPrefLevel().getPrefName()+" Room Group "+getRoomGroup().getNameWithTitle();
	}
}
