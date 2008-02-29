/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import org.unitime.timetable.model.base.BaseDistributionObject;

public class DistributionObject extends BaseDistributionObject implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DistributionObject () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DistributionObject (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public DistributionObject (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.DistributionPref distributionPref,
		org.unitime.timetable.model.PreferenceGroup prefGroup) {

		super (
			uniqueId,
			distributionPref,
			prefGroup);
	}

/*[CONSTRUCTOR MARKER END]*/
		
	public String preferenceText(){
		PreferenceGroup prefGroup = this.getPrefGroup();
		if (prefGroup instanceof SchedulingSubpart){
			SchedulingSubpart ss = (SchedulingSubpart)prefGroup;
			return ss.getSchedulingSubpartLabel();
		} else if (prefGroup instanceof Class_) {
			Class_ c = (Class_)prefGroup;
			return c.getClassLabel();
		} else {
			return " unknown "+prefGroup.getClass().getName();
		}
	}
	
	/** Ordering based on sequence numbers or preference groups */
	public int compareTo(Object o) {
		if (o==null || !(o instanceof DistributionObject)) return -1;
		DistributionObject d = (DistributionObject)o;
		if (getSequenceNumber()!=null && d.getSequenceNumber()!=null)
			return getSequenceNumber().compareTo(d.getSequenceNumber());
		if (getPrefGroup() instanceof Comparable && d.getPrefGroup() instanceof Comparable)
			return ((Comparable)getPrefGroup()).compareTo(d.getPrefGroup());
		return getPrefGroup().toString().compareTo(d.getPrefGroup().toString());
	}
}