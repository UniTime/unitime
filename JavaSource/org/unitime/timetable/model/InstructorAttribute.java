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

import org.hibernate.HibernateException;
import org.unitime.timetable.model.base.BaseInstructorAttribute;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;

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
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(s.getUniqueId() == null ? -1 : s.getUniqueId());
	}
	
	public static List<InstructorAttribute> getAllGlobalAttributes(Long sessionId) throws HibernateException {
		return (List<InstructorAttribute>)InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute ia where ia.session.uniqueId = :sessionId and ia.department is null order by name"
				).setLong("sessionId", sessionId).setCacheable(true).list();
	}

	public static List<InstructorAttribute> getAllDepartmentalAttributes(Long departmentId) throws HibernateException {
		return (List<InstructorAttribute>)InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute ia where ia.department.uniqueId = :departmentId order by name"
				).setLong("departmentId", departmentId).setCacheable(true).list();
	}
	
	public boolean isParentOf(InstructorAttribute attribute) {
		while (attribute != null) {
			if (this.equals(attribute.getParentAttribute())) return true;
			attribute = attribute.getParentAttribute();
		}
		return false;
	}
	
	public String getNameWithType() {
		return getName() + (getType() == null ? "" : " (" + getType().getLabel() + ")");
	}

}
