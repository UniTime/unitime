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

import org.unitime.timetable.model.base.BaseSolverParameter;



public class SolverParameter extends BaseSolverParameter implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverParameter () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverParameter (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public SolverParameter (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SolverParameterDef definition) {

		super (
			uniqueId,
			definition);
	}

/*[CONSTRUCTOR MARKER END]*/

    public int compareTo(Object o) {
        if (o==null || !(o instanceof SolverParameter)) return -1;
        SolverParameter p = (SolverParameter)o;
        int cmp = getDefinition().compareTo(p.getDefinition());
        if (cmp!=0) return cmp;
        return getUniqueId().compareTo(p.getUniqueId());
    }

}