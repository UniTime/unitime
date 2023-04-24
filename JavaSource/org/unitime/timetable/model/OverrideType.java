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

import org.unitime.timetable.model.base.BaseOverrideType;
import org.unitime.timetable.model.dao.OverrideTypeDAO;

@Entity
@Table(name = "override_type")
public class OverrideType extends BaseOverrideType implements Comparable<OverrideType> {
	private static final long serialVersionUID = 1L;

	public OverrideType() {
		super();
	}

	@Override
	public int compareTo(OverrideType o) {
		return getReference().compareTo(o.getReference());
	}
	
	public static OverrideType findByReference(String reference) {
		if (reference == null || reference.isEmpty()) return null;
		return OverrideTypeDAO.getInstance().getSession().createQuery(
				"from OverrideType where reference = :reference", OverrideType.class
				).setParameter("reference", reference, String.class).setMaxResults(1).uniqueResult();
	}

}
