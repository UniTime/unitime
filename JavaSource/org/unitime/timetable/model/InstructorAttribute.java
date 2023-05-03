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

import org.hibernate.HibernateException;
import org.unitime.timetable.model.base.BaseInstructorAttribute;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "attribute")
public class InstructorAttribute extends BaseInstructorAttribute implements Comparable<InstructorAttribute> {
	private static final long serialVersionUID = 331064011983395675L;
	
	public static String ATTRIBUTES_LIST_ATTR_NAME = "attributesList";

	public InstructorAttribute() {
		super();
	}

	@Override
	public int compareTo(InstructorAttribute s) {
		int cmp = (getType() == null ? "" : getType().getLabel()).compareTo(s.getType() == null ? "" : s.getType().getLabel());
		if (cmp != 0) return cmp;
		cmp = getName().compareTo(s.getName());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(s.getUniqueId() == null ? -1 : s.getUniqueId());
	}
	
	public static List<InstructorAttribute> getAllGlobalAttributes(Long sessionId) throws HibernateException {
		return InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute ia where ia.session.uniqueId = :sessionId and ia.department is null order by name", InstructorAttribute.class
				).setParameter("sessionId", sessionId).setCacheable(true).list();
	}

	public static List<InstructorAttribute> getAllDepartmentalAttributes(Long departmentId) throws HibernateException {
		return InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute ia where ia.department.uniqueId = :departmentId order by name", InstructorAttribute.class
				).setParameter("departmentId", departmentId).setCacheable(true).list();
	}
	
	public InstructorAttribute findSameAttributeInSession(Session session) {
		if (session == null) return null;
		if (getDepartment() != null) {
			Department d = getDepartment().findSameDepartmentInSession(session);
			if (d == null) return null;
			return InstructorAttributeDAO.getInstance().getSession().createQuery(
					"from InstructorAttribute ia where ia.department.uniqueId = :departmentId and ia.code = :code", InstructorAttribute.class)
					.setParameter("departmentId", d.getUniqueId()).setParameter("code", getCode()).setCacheable(true)
					.setMaxResults(1).uniqueResult();
		} else {
			return InstructorAttributeDAO.getInstance().getSession().createQuery(
					"from InstructorAttribute ia where ia.session.uniqueId = :sessionId and ia.department is null and ia.code = :code", InstructorAttribute.class)
					.setParameter("sessionId", session.getUniqueId()).setParameter("code", getCode()).setCacheable(true)
					.setMaxResults(1).uniqueResult();
		}
	}
	
	public boolean isParentOf(InstructorAttribute attribute) {
		while (attribute != null) {
			if (this.equals(attribute.getParentAttribute())) return true;
			attribute = attribute.getParentAttribute();
		}
		return false;
	}
	
	@Transient
	public String getNameWithType() {
		return getName() + (getType() == null ? "" : " (" + getType().getLabel() + ")");
	}
}
