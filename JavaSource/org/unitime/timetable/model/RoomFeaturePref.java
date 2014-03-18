/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.base.BaseRoomFeaturePref;


/**
 * @author Tomas Muller
 */
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
    	return(this.getRoomFeature().getLabel());
    }
    
    public String preferenceAbbv() {
        return(this.getRoomFeature().getAbbv());
    }

    public int compareTo(Object o) {
    	try {
    		RoomFeaturePref p = (RoomFeaturePref)o;
    		int cmp = getRoomFeature().getLabel().compareTo(p.getRoomFeature().getLabel()); 
    		if (cmp!=0) return cmp;
    	} catch (Exception e) {}
    	
    	return super.compareTo(o);
   }
    
    public Object clone() {
    	RoomFeaturePref pref = new RoomFeaturePref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setRoomFeature(getRoomFeature());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof RoomFeaturePref)) return false;
    	return ToolBox.equals(getRoomFeature(),((RoomFeaturePref)other).getRoomFeature());
    }
    public boolean isApplicable(PreferenceGroup group) {
    	return true;
    }

	public String preferenceTitle() {
		return getPrefLevel().getPrefName()+" Room Feature "+getRoomFeature().getLabelWithType();
	}
}
