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

import org.unitime.timetable.model.base.BaseCurriculumClassification;



/**
 * @author Tomas Muller
 */
public class CurriculumClassification extends BaseCurriculumClassification implements Comparable<CurriculumClassification> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumClassification () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumClassification (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculumClassification cc) {
		if (getAcademicClassification() != null && cc.getAcademicClassification() != null) {
			int cmp = getAcademicClassification().getCode().compareTo(cc.getAcademicClassification().getCode());
			if (cmp != 0) return cmp;
		}
	    if (getOrd()!=null && cc.getOrd()!=null && !getOrd().equals(cc.getOrd()))
	        return getOrd().compareTo(cc.getOrd());
	    int cmp = getName().compareToIgnoreCase(cc.getName());
	    if (cmp!=0) return cmp;
	    return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(cc.getUniqueId() == null ? -1 : cc.getUniqueId());
	}
}