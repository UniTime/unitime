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
package org.unitime.timetable.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.base.BaseRoomGroupPref;


/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "room_group_pref")
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
		String ret = getRoomGroup().getName();
    	if (getRoomIndex() != null && getOwner() instanceof Class_ && ((Class_)getOwner()).getNbrRooms() > 1 && getRoomIndex() < ((Class_)getOwner()).getNbrRooms())
    		ret += " (" + MSG.itemOnlyRoom(1 + getRoomIndex()) + ")";
    	return ret;
    }

    public String preferenceAbbv() { 
        String ret = getRoomGroup().getAbbv();
        if (getRoomIndex() != null && getOwner() instanceof Class_ && ((Class_)getOwner()).getNbrRooms() > 1 && getRoomIndex() < ((Class_)getOwner()).getNbrRooms())
    		ret += " (" + MSG.itemOnlyRoom(1 + getRoomIndex()) + ")";
    	return ret;
    }

    public Object clone() {
    	RoomGroupPref pref = new RoomGroupPref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setRoomGroup(getRoomGroup());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof RoomGroupPref)) return false;
    	return ToolBox.equals(getRoomGroup(),((RoomGroupPref)other).getRoomGroup()) && ToolBox.equals(getRoomIndex(), ((RoomGroupPref)other).getRoomIndex());
    }

	public String preferenceTitle() {
		return MSG.prefTitleRoomGroup(getPrefLevel().getPrefName(), getRoomGroup().getNameWithTitle());
	}
	
	@Transient
	public Type getType() { return Type.ROOM_GROUP; }
	
	@Override
	public String preferenceDescription() {
		return getRoomGroup().getDescription();
	}
}
