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
package org.unitime.timetable.model.dao;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
import java.util.List;
import org.unitime.timetable.model.InstructorAttribute;

public class InstructorAttributeDAO extends _RootDAO<InstructorAttribute,Long> {
	private static InstructorAttributeDAO sInstance;

	public InstructorAttributeDAO() {}

	public static InstructorAttributeDAO getInstance() {
		if (sInstance == null) sInstance = new InstructorAttributeDAO();
		return sInstance;
	}

	public Class<InstructorAttribute> getReferenceClass() {
		return InstructorAttribute.class;
	}

	@SuppressWarnings("unchecked")
	public List<InstructorAttribute> findByType(org.hibernate.Session hibSession, Long typeId) {
		return hibSession.createQuery("from InstructorAttribute x where x.type.uniqueId = :typeId", InstructorAttribute.class).setParameter("typeId", typeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<InstructorAttribute> findByParentAttribute(org.hibernate.Session hibSession, Long parentAttributeId) {
		return hibSession.createQuery("from InstructorAttribute x where x.parentAttribute.uniqueId = :parentAttributeId", InstructorAttribute.class).setParameter("parentAttributeId", parentAttributeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<InstructorAttribute> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from InstructorAttribute x where x.session.uniqueId = :sessionId", InstructorAttribute.class).setParameter("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<InstructorAttribute> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from InstructorAttribute x where x.department.uniqueId = :departmentId", InstructorAttribute.class).setParameter("departmentId", departmentId).list();
	}
}
