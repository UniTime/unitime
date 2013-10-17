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

import org.unitime.timetable.model.base.BaseRoomPref;

import net.sf.cpsolver.ifs.util.ToolBox;


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
    public String preferenceHtml() {
    	StringBuffer sb = new StringBuffer("<span ");
    	if (this.getPrefLevel().getPrefId().intValue() != 4) {
    		sb.append("style='color:"+this.getPrefLevel().prefcolor()+";font-weight:bold;' ");
    	} else {
    		sb.append("style='font-weight:bold;' ");
    	}
    	sb.append("onmouseover=\"showGwtRoomHint(this, '" + getRoom().getUniqueId() + "', '" + getPrefLevel().getPrefName() + " Room');\" onmouseout=\"hideGwtRoomHint();\">");
    	sb.append(this.preferenceAbbv());
    	sb.append("</span>");
    	return (sb.toString());
    }
    
	public String preferenceTitle() {
    	return getPrefLevel().getPrefName() + " Room " + getRoom().getLabel();
	}
}
