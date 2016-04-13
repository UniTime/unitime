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
		return MSG.prefTitleRoomFeature(getPrefLevel().getPrefName(), getRoomFeature().getLabelWithType());
	}
	
	public Type getType() { return Type.ROOM_FEATURE; }
}
