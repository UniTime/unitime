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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseSolverParameterDef;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;




/**
 * @author Tomas Muller
 */
public class SolverParameterDef extends BaseSolverParameterDef implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverParameterDef () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverParameterDef (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	/*
	 * @return all SolverParameterDefs
	 */
	public static ArrayList getAll() throws HibernateException {
		return (ArrayList) (new SolverParameterDefDAO().findAll());
	}

	/**
	 * @param id
	 * @return
	 * @throws HibernateException
	 */
	public static SolverParameterDef getSolverParameterDefById(Long id) throws HibernateException {
		return (new SolverParameterDefDAO()).get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @throws HibernateException
	 */
	public static void deleteSolverParameterDefById(Long id) throws HibernateException {
		SolverParameterDef solverParameterDef = SolverParameterDef.getSolverParameterDefById(id);
		if (solverParameterDef != null) {
			(new SolverParameterDefDAO()).delete(solverParameterDef);
		}
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void saveOrUpdate() throws HibernateException {
		(new SolverParameterDefDAO()).saveOrUpdate(this);
	}

	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static SolverParameterDef findByName(org.hibernate.Session hibSession, String name) {
		SolverParameterDef def = null;
		
        try {
			List list = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("name", name)).setCacheable(true).list();

			if (!list.isEmpty())
				def = (SolverParameterDef)list.get(0);

	    } catch (Exception e) {
			Debug.error(e);
	    }
	    
	    return def;
	}
	
	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static SolverParameterDef findByName(String name) {
		return findByName((new SolverParameterDefDAO()).getSession(),name);
	}	

	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static List findByGroup(SolverParameterGroup group) {
		return (new SolverParameterDefDAO()).getSession().
			createCriteria(SolverParameterDef.class).
			add(Restrictions.eq("group", group)).
			addOrder(Order.asc("order")).
			list();
	}
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof SolverParameterDef)) return -1;
        SolverParameterDef p = (SolverParameterDef)o;
        int cmp = getOrder().compareTo(p.getOrder());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(p.getUniqueId() == null ? -1 : p.getUniqueId());
    }
    
    @Override
    public String getDefault() {
    	String ret = super.getDefault();
    	return ret == null ? "" : ret;
    }
}
