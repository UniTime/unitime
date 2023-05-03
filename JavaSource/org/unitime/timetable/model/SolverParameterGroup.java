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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseSolverParameterGroup;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "solver_parameter_group")
public class SolverParameterGroup extends BaseSolverParameterGroup {
	private static final long serialVersionUID = 1L;
	
	public static enum SolverType {
		COURSE("crs_"),
		EXAM("exm_"),
		STUDENT("std_"),
		INSTRUCTOR("ins_"),
		;
		
		private String iPrefix;
		SolverType(String prefix) { iPrefix = prefix; }
		
	@Transient
		public String getPrefix() { return iPrefix; }
	}

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
		return SolverParameterGroupDAO.getInstance().getSession()
				.createQuery("from SolverParameterGroup where name = :name", SolverParameterGroup.class)
				.setParameter("name", name)
				.setCacheable(true)
				.setMaxResults(1)
                .uniqueResult();
	}
	
	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	@Transient
	public static String[] getGroupNames() {
		List<SolverParameterGroup> groups = SolverParameterGroupDAO.getInstance().getSession().createQuery(
				"from SolverParameterGroup order by order", SolverParameterGroup.class)
				.setCacheable(true).list();
		String[] ret = new String[groups.size()];
		int idx = 0;
		
		for (SolverParameterGroup group: groups) {
			ret[idx++] = group.getName();
		}
			    
	    return ret;
	}

	@Transient
	public SolverParameterGroup.SolverType getSolverType() { return getType() == null ? null : SolverParameterGroup.SolverType.values()[getType()]; }
	public void setSolverType(SolverParameterGroup.SolverType type) { setType(type == null ? null : Integer.valueOf(type.ordinal())); }
	
	@Transient
	public boolean isVisible() {
		for (SolverParameterDef d: getParameters()) {
			if (d.isVisible()) return true;
		}
		return false;
	}
	
	@Transient
	public Set<SolverParameterDef> getVisibleParameters() {
		Set<SolverParameterDef> ret = new TreeSet<SolverParameterDef>();
		for (SolverParameterDef d: getParameters()) {
			if (d.isVisible())
				ret.add(d);
		}
		return ret;
	}
}
