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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.List;

import org.unitime.timetable.model.base.BasePosMajorConcentration;
import org.unitime.timetable.model.dao.PosMajorConcentrationDAO;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "pos_major_conc")
public class PosMajorConcentration extends BasePosMajorConcentration {
	private static final long serialVersionUID = 1L;

	public PosMajorConcentration() {
		super();
	}
	
	public static List<PosMajorConcentration> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return (hibSession == null ? PosMajorConcentrationDAO.getInstance().getSession() : hibSession).createQuery(
				"from PosMajorConcentration x where x.major.session.uniqueId = :sessionId order by x.major.code, x.code", PosMajorConcentration.class)
				.setParameter("sessionId", sessionId).list();
	}
	
    public Object clone() {
    	PosMajorConcentration m = new PosMajorConcentration();
    	m.setExternalUniqueId(getExternalUniqueId());
    	m.setCode(getCode());
    	m.setName(getName());
    	return m;
    }

}
