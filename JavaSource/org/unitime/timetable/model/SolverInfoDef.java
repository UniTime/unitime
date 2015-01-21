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

import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseSolverInfoDef;
import org.unitime.timetable.model.dao.SolverInfoDefDAO;




/**
 * @author Tomas Muller
 */
public class SolverInfoDef extends BaseSolverInfoDef {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverInfoDef () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverInfoDef (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static SolverInfoDef findByName(org.hibernate.Session hibSession, String name) {
		try {
			try {
				return (SolverInfoDef)hibSession.createCriteria(SolverInfoDef.class).add(Restrictions.eq("name", name)).setCacheable(true).uniqueResult();
			} catch (NonUniqueResultException e) {
				List list = hibSession.createCriteria(SolverInfoDef.class).add(Restrictions.eq("name", name)).setCacheable(true).list();
				if (!list.isEmpty()) 
					return (SolverInfoDef)list.get(0);
			}
	    } catch (Exception e) {
			Debug.error(e);
	    }
		return null;
	}

	public static SolverInfoDef findByName(String name) {
		return findByName((new SolverInfoDefDAO()).getSession(), name);
	}	
}
