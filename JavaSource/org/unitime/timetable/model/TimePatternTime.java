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

import org.unitime.timetable.model.base.BaseTimePatternTime;



public class TimePatternTime extends BaseTimePatternTime implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public TimePatternTime () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public TimePatternTime (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public boolean equals(Object o) {
		return (compareTo(o)==0);
	}

	public int compareTo(Object o) {
		if (o==null || !(o instanceof TimePatternTime)) return -1;
		return getStartSlot().compareTo(((TimePatternTime)o).getStartSlot());
	}
	
	public int hashCode() {
		return getStartSlot().hashCode();
	}

}
