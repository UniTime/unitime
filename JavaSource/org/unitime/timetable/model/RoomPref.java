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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseRoomPref;



/**
 * @author Tomas Muller
 */
public class RoomPref extends BaseRoomPref {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomPref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String preferenceText() { 
		return(this.getRoom().getLabel());
    }
	
    public int compareTo(Object o) {
    	try {
    		RoomPref p = (RoomPref)o;
    		int cmp = getRoom().getLabel().compareTo(p.getRoom().getLabel());
    		if (cmp!=0) return cmp;
    	} catch (Exception e) {}
    	
    	return super.compareTo(o);
    }
    
    public Object clone() {
 	   RoomPref pref = new RoomPref();
 	   pref.setPrefLevel(getPrefLevel());
 	   pref.setRoom(getRoom());
 	   return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof RoomPref)) return false;
    	return ToolBox.equals(getRoom(),((RoomPref)other).getRoom());
    }
    
    @Override
    public String preferenceHtml(String nameFormat) {
    	StringBuffer sb = new StringBuffer("<span ");
    	String style = "font-weight:bold;";
		if (this.getPrefLevel().getPrefId().intValue() != 4) {
			style += "color:" + this.getPrefLevel().prefcolor() + ";";
		}
		if (this.getOwner() != null && this.getOwner() instanceof Class_ && ApplicationProperty.PreferencesHighlighClassPreferences.isTrue()) {
			style += "background: #ffa;";
		}
		sb.append("style='" + style + "' ");
		String owner = "";
		if (getOwner() != null && getOwner() instanceof Class_) {
			owner = ", " + MSG.prefOwnerClass();
		} else if (getOwner() != null && getOwner() instanceof SchedulingSubpart) {
			owner = ", " + MSG.prefOwnerSchedulingSubpart();
		} else if (getOwner() != null && getOwner() instanceof DepartmentalInstructor) {
			owner = ", " + MSG.prefOwnerInstructor();
		} else if (getOwner() != null && getOwner() instanceof Exam) {
			owner = ", " + MSG.prefOwnerExamination();
		} else if (getOwner() != null && getOwner() instanceof Department) {
			owner = ", " + MSG.prefOwnerDepartment();
		} else if (getOwner() != null && getOwner() instanceof Session) {
			owner = ", " + MSG.prefOwnerSession();
		}
    	sb.append("onmouseover=\"showGwtRoomHint(this, '" + getRoom().getUniqueId() + "', '" + getPrefLevel().getPrefName() + " " + MSG.prefRoom() + " {0} ({1}" + owner + ")');\" onmouseout=\"hideGwtRoomHint();\">");
    	sb.append(this.preferenceAbbv());
    	sb.append("</span>");
    	return (sb.toString());
    }
    
	public String preferenceTitle() {
    	return MSG.prefTitleRoom(getPrefLevel().getPrefName(), getRoom().getLabel());
	}
}
