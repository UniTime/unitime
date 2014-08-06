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

import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseSolverParameterGroup;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;




/**
 * @author Tomas Muller
 */
public class SolverParameterGroup extends BaseSolverParameterGroup {
	private static final long serialVersionUID = 1L;
	
	public static final int sTypeCourse = 0;
	public static final int sTypeExam = 1;
	public static final int sTypeStudent = 2;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverParameterGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverParameterGroup (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static SolverParameterGroup findByName(String name) {
		List list = (new SolverParameterGroupDAO()).getSession().
			createCriteria(SolverParameterGroup.class).add(Restrictions.eq("name", name)).list();

		if (!list.isEmpty())
			return (SolverParameterGroup)list.get(0);
		
		return null;
	}
	
	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static String[] getGroupNames() {
		List groups = (new SolverParameterGroupDAO()).findAll(Order.asc("order"));
		String[] ret = new String[groups.size()];
		int idx = 0;
		
		for (Iterator i=groups.iterator();i.hasNext();) {
			SolverParameterGroup group = (SolverParameterGroup)i.next();
			ret[idx++] = group.getName();
		}
			    
	    return ret;
	}	
}
