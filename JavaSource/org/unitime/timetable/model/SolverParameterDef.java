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
	
	@Deprecated
	public static SolverParameterDef findByNameGroup(String name) {
		SolverParameterDef def = null;
		
        try {
			List list = SolverParameterDefDAO.getInstance().getSession().
					createCriteria(SolverParameterDef.class).add(Restrictions.eq("name", name)).setCacheable(true).list();

			if (!list.isEmpty())
				def = (SolverParameterDef)list.get(0);

	    } catch (Exception e) {
			Debug.error(e);
	    }
	    
	    return def;
	}
	
	public static SolverParameterDef findByNameGroup(String name, String group) {
		return findByNameGroup(SolverParameterDefDAO.getInstance().getSession(), name, group);
	}

	public static SolverParameterDef findByNameGroup(org.hibernate.Session hibSession, String name, String group) {
		List<SolverParameterDef> list = (List<SolverParameterDef>)hibSession.createQuery(
				"from SolverParameterDef where name = :name and group.name = :group")
				.setString("name", name)
				.setString("group", group)
				.setCacheable(true).list();
		return list.isEmpty() ? null : list.get(0);
	}
	
	public static SolverParameterDef findByNameType(String name, int type) {
		return findByNameType(SolverParameterDefDAO.getInstance().getSession(), name, type);
	}
	
	public static SolverParameterDef findByNameType(org.hibernate.Session hibSession, String name, int type) {
		List<SolverParameterDef> list = (List<SolverParameterDef>)hibSession.createQuery(
				"from SolverParameterDef where name = :name and group.type = :type")
				.setString("name", name)
				.setInteger("type", type)
				.setCacheable(true).list();
		return list.isEmpty() ? null : list.get(0);
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
