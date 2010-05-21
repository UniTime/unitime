/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.util.Date;

import org.unitime.timetable.model.base.BaseTimePref;
import org.unitime.timetable.webutil.RequiredTimeTable;

import net.sf.cpsolver.ifs.util.ToolBox;

public class TimePref extends BaseTimePref implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public TimePref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public TimePref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public TimePref (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.PreferenceGroup owner,
		org.unitime.timetable.model.PreferenceLevel prefLevel) {

		super (
			uniqueId,
			owner,
			prefLevel);
	}

/*[CONSTRUCTOR MARKER END]*/

    public String preferenceText() {
    	return this.getTimePattern().getName();
    }
    
    public TimePatternModel getTimePatternModel() {
    	return getTimePatternModel(null);
    }
    
    public TimePatternModel getTimePatternModel(Assignment assignment) {
    	TimePatternModel model = new TimePatternModel(getTimePattern(), assignment, true);
    	model.setPreferences(super.getPreference());
    	return model;
    }
    
	public void setTimePatternModel(TimePatternModel model) {
		if (model==null) {
			setPreference(null);
			setTimePattern(null);
		} else {
			setPreference(model.getPreferences());
			setTimePattern(model.getTimePattern());
		}
	}
	
    public int compareTo(Object o) {
    	try {
    		TimePattern t1 = getTimePattern();
    		TimePattern t2 = ((TimePref)o).getTimePattern();
        
    		int cmp = t1.compareTo(t2);
    		if (cmp!=0) return cmp;
    		cmp = -getPreference().compareTo(((TimePref)o).getPreference());
    		if (cmp!=0) return cmp;
    	} catch (Exception e) {};
    	
    	return super.compareTo(o);
    }
    
    public void setInOldFormat(String days, Date startTime, Date endTime, PreferenceLevel pref) {
    	TimePatternModel model = getTimePatternModel();
    	if (model==null) return;
    	
    	model.setInOldFormat(days, startTime, endTime, pref);
    	
    	setPreference(model.getPreferences());
    }
    
    public void combineWith(TimePref other, boolean clear) {
    	combineWith(other, clear, TimePatternModel.sMixAlgMinMax);
    }
    
    public void combineWith(TimePref other, boolean clear, int alg) {
    	TimePatternModel model = getTimePatternModel();
    	TimePatternModel otherModel = other.getTimePatternModel();
    	if (model==null || otherModel==null) return;
    	model.combineWith(otherModel, clear, alg);
    	setPreference(model.getPreferences());
    }
    
    public boolean weakenHardPreferences() {
    	TimePatternModel model = getTimePatternModel();
    	if (model==null) return false;
    	model.weakenHardPreferences();
    	setPreference(model.getPreferences());
    	return true;
    }

    public RequiredTimeTable getRequiredTimeTable() {
    	return getRequiredTimeTable(null);
    }
    public RequiredTimeTable getRequiredTimeTable(Assignment assignment) {
    	return new RequiredTimeTable(getTimePatternModel(assignment));
    }
    
    public Object clone() {
    	TimePref p = new TimePref();
    	p.setPreference(getPreference());
    	p.setTimePattern(getTimePattern());
    	p.setPrefLevel(getPrefLevel());
    	return p;
    }
    
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof TimePref)) return false;
    	return ToolBox.equals(getTimePattern(),((TimePref)other).getTimePattern());
    }
    
    public String getPreference() {
    	if (super.getPreference()==null) {
    		return getTimePatternModel().getPreferences();
    	} else return super.getPreference();
    }
}
