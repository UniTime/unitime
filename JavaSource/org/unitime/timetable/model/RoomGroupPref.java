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
        if (getRoomIndex() != null)
        	ret += " (" + MSG.itemOnlyRoom(1 + getRoomIndex()) + ")";
    	return ret;
    }

    public String preferenceAbbv() { 
        String ret = getRoomGroup().getAbbv();
        if (getRoomIndex() != null)
        	ret += " (" + MSG.itemOnlyRoom(1 + getRoomIndex()) + ")";
    	return ret;
    }
    
    public int compareTo(Object o) {
    	try {
    		RoomGroupPref p = (RoomGroupPref)o;
    		int cmp = Integer.compare(getRoomIndex() == null ? -1 : getRoomIndex(), p.getRoomIndex() == null ? -1 : p.getRoomIndex());
    		if (cmp != 0) return cmp;
    		cmp = getRoomGroup().getAbbv().compareTo(p.getRoomGroup().getAbbv());
    		if (cmp!=0) return cmp;
    	} catch (Exception e) {}
    	
    	return super.compareTo(o);
    }

    public Object clone() {
    	RoomGroupPref pref = new RoomGroupPref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setRoomGroup(getRoomGroup());
    	pref.setRoomIndex(getRoomIndex());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof RoomGroupPref)) return false;
    	return ToolBox.equals(getRoomGroup(),((RoomGroupPref)other).getRoomGroup()) && ToolBox.equals(getRoomIndex(), ((RoomGroupPref)other).getRoomIndex());
    }
    public boolean isSame(Preference other, PreferenceGroup level) {
    	if (other==null || !(other instanceof RoomGroupPref)) return false;
    	if (!ToolBox.equals(getRoomGroup(),((RoomGroupPref)other).getRoomGroup())) return false;
    	if (level != null && level instanceof Class_ && ((Class_)level).getNbrRooms() == 1) {
    		if (((getRoomIndex() == null || getRoomIndex() == 0) && (((RoomGroupPref)other).getRoomIndex() == null || ((RoomGroupPref)other).getRoomIndex() == 0)))
    				return true;
    	}
    	return ToolBox.equals(getRoomIndex(), ((RoomGroupPref)other).getRoomIndex());
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
	
	@Override
	public boolean appliesTo(PreferenceGroup group) {
		if (!super.appliesTo(group)) return false;
		if (getRoomIndex() != null && group instanceof Class_)
			return getRoomIndex() < ((Class_)group).getNbrRooms();
		if (getRoomIndex() != null && group instanceof SchedulingSubpart)
			return getRoomIndex() < ((SchedulingSubpart)group).getMaxRooms();
		return true;
	}
}
