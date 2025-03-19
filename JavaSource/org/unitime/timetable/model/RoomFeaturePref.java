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
import org.unitime.timetable.model.base.BaseRoomFeaturePref;


/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "room_feature_pref")
public class RoomFeaturePref extends BaseRoomFeaturePref {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomFeaturePref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomFeaturePref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public String preferenceText() {
    	String ret = getRoomFeature().getLabelWithType();
        if (getRoomIndex() != null)
        	ret += " (" + MSG.itemOnlyRoom(1 + getRoomIndex()) + ")";
    	return ret;
    }
    
    public String preferenceAbbv() {
    	String ret = getRoomFeature().getAbbv();
        if (getRoomIndex() != null)
        	ret += " (" + MSG.itemOnlyRoom(1 + getRoomIndex()) + ")";
    	return ret;
    }

    public int compareTo(Object o) {
    	try {
    		RoomFeaturePref p = (RoomFeaturePref)o;
    		int cmp = Integer.compare(getRoomIndex() == null ? -1 : getRoomIndex(), p.getRoomIndex() == null ? -1 : p.getRoomIndex());
    		if (cmp != 0) return cmp;
    		cmp = getRoomFeature().getLabel().compareTo(p.getRoomFeature().getLabel()); 
    		if (cmp!=0) return cmp;
    	} catch (Exception e) {}
    	
    	return super.compareTo(o);
   }
    
    public Object clone() {
    	RoomFeaturePref pref = new RoomFeaturePref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setRoomFeature(getRoomFeature());
    	pref.setRoomIndex(getRoomIndex());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof RoomFeaturePref)) return false;
    	return ToolBox.equals(getRoomFeature(),((RoomFeaturePref)other).getRoomFeature()) && ToolBox.equals(getRoomIndex(), ((RoomFeaturePref)other).getRoomIndex());
    }
    public boolean isSame(Preference other, PreferenceGroup level) {
    	if (other==null || !(other instanceof RoomFeaturePref)) return false;
    	if (!ToolBox.equals(getRoomFeature(),((RoomFeaturePref)other).getRoomFeature())) return false;
    	if (level != null && level instanceof Class_ && ((Class_)level).getNbrRooms() == 1) {
    		if (((getRoomIndex() == null || getRoomIndex() == 0) && (((RoomFeaturePref)other).getRoomIndex() == null || ((RoomFeaturePref)other).getRoomIndex() == 0)))
    				return true;
    	}
    	return ToolBox.equals(getRoomIndex(), ((RoomFeaturePref)other).getRoomIndex());
    }
    public boolean isApplicable(PreferenceGroup group) {
    	return true;
    }

	public String preferenceTitle() {
		return MSG.prefTitleRoomFeature(getPrefLevel().getPrefName(), getRoomFeature().getLabelWithType());
	}
	
	@Transient
	public Type getType() { return Type.ROOM_FEATURE; }
	
	@Override
	public String preferenceDescription() {
		return getRoomFeature().getDescription();
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
