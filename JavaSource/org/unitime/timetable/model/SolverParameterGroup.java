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
