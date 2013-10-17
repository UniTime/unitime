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
