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



import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import org.unitime.timetable.model.base.BaseSolverInfoDef;
import org.unitime.timetable.model.dao.SolverInfoDefDAO;




/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "solver_info_def")
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
		return hibSession.createQuery("from SolverInfoDef where name = :name", SolverInfoDef.class)
				.setParameter("name", name)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}

	public static SolverInfoDef findByName(String name) {
		return findByName((new SolverInfoDefDAO()).getSession(), name);
	}	
}
