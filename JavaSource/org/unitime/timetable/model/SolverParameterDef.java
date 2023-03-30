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
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
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
		return SolverParameterDefDAO.getInstance().getSession()
				.createQuery("from SolverParameterDef where name = :name", SolverParameterDef.class)
				.setParameter("name", name)
				.setCacheable(true)
				.setMaxResults(1)
                .uniqueResult();
	}
	
	public static SolverParameterDef findByNameGroup(String name, String group) {
		return findByNameGroup(SolverParameterDefDAO.getInstance().getSession(), name, group);
	}

	public static SolverParameterDef findByNameGroup(org.hibernate.Session hibSession, String name, String group) {
		List<SolverParameterDef> list = (List<SolverParameterDef>)hibSession.createQuery(
				"from SolverParameterDef where name = :name and group.name = :group")
				.setParameter("name", name, org.hibernate.type.StringType.INSTANCE)
				.setParameter("group", group, org.hibernate.type.StringType.INSTANCE)
				.setCacheable(true).list();
		return list.isEmpty() ? null : list.get(0);
	}
	
	public static SolverParameterDef findByNameType(String name, SolverType type) {
		return findByNameType(SolverParameterDefDAO.getInstance().getSession(), name, type);
	}
	
	public static SolverParameterDef findByNameType(org.hibernate.Session hibSession, String name, SolverType type) {
		List<SolverParameterDef> list = (List<SolverParameterDef>)hibSession.createQuery(
				"from SolverParameterDef where name = :name and group.type = :type")
				.setParameter("name", name, org.hibernate.type.StringType.INSTANCE)
				.setParameter("type", type.ordinal(), org.hibernate.type.IntegerType.INSTANCE)
				.setCacheable(true).list();
		return list.isEmpty() ? null : list.get(0);
	}	

	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static List<SolverParameterDef> findByGroup(SolverParameterGroup group) {
		return SolverParameterDefDAO.getInstance().getSession()
				.createQuery("from SolverParameterDef where group.uniqueId = :groupId order by order", SolverParameterDef.class)
				.setParameter("groupId", group.getUniqueId())
				.list();
	}
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof SolverParameterDef)) return -1;
        SolverParameterDef p = (SolverParameterDef)o;
        int cmp = getOrder().compareTo(p.getOrder());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(p.getUniqueId() == null ? -1 : p.getUniqueId());
    }
    
    @Override
    public String getDefault() {
    	String ret = super.getDefault();
    	return ret == null ? "" : ret;
    }
    
    public String[] getOptions() {
    	if (getType() != null && getType().startsWith("enum(") && getType().endsWith(")"))
    		return getType().substring(5, getType().length() - 1).split(",");
    	else
    		return null;
    }
}
