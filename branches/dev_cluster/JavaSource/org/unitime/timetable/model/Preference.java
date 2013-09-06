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

import org.unitime.timetable.model.base.BasePreference;



public abstract class Preference extends BasePreference implements Comparable {
	private static final long serialVersionUID = 1L;

    /** Blank Pref Value **/
    public static final String BLANK_PREF_VALUE = "-";

    /*[CONSTRUCTOR MARKER BEGIN]*/
	public Preference () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Preference (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public String preferenceTitle() {
		return getPrefLevel().getPrefName()+" "+preferenceText();
	}

    public String preferenceHtml() {
	StringBuffer sb = new StringBuffer("<span ");
		if (this.getPrefLevel().getPrefId().intValue() != 4) {
			sb.append("style='color:"+this.getPrefLevel().prefcolor()+";font-weight:bold;' ");
		} else {
			sb.append("style='font-weight:bold;' ");
		}
		sb.append("onmouseover=\"showGwtHint(this, '" + preferenceTitle() + "');\" onmouseout=\"hideGwtHint();\">");
		
		sb.append(this.preferenceAbbv());
		sb.append("</span>");
		return (sb.toString());
    }
    
    public abstract String preferenceText();
    public String preferenceAbbv() {
        return preferenceText();
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
        if(o == null || !(o instanceof Preference))
            throw new RuntimeException("Object must be of type Preference");
        Preference p = (Preference) o;
        int cmp = getClass().getName().compareTo(o.getClass().getName());
        if (cmp!=0) return cmp;
        
        if (this.getUniqueId()==null || p.getUniqueId()==null)
        	return -1;

        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(p.getUniqueId() == null ? -1 : p.getUniqueId());
	}
	
	public boolean appliesTo(PreferenceGroup group) {
		return true;
	}
	
    public boolean weakenHardPreferences() {
    	if (PreferenceLevel.sRequired.equals(getPrefLevel().getPrefProlog()))
    		setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
    	if (PreferenceLevel.sProhibited.equals(getPrefLevel().getPrefProlog()))
    		setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
    	return true;
    }
    
    public abstract Object clone();
    public abstract boolean isSame(Preference other);
}
