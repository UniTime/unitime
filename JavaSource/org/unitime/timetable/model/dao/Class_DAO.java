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
import org.unitime.timetable.model.Class_;

public class Class_DAO extends _RootDAO<Class_,Long> {
	private static Class_DAO sInstance;

	public Class_DAO() {}

	public static Class_DAO getInstance() {
		if (sInstance == null) sInstance = new Class_DAO();
		return sInstance;
	}

	public Class<Class_> getReferenceClass() {
		return Class_.class;
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByManagingDept(org.hibernate.Session hibSession, Long managingDeptId) {
		return hibSession.createQuery("from Class_ x where x.managingDept.uniqueId = :managingDeptId").setParameter("managingDeptId", managingDeptId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findBySchedulingSubpart(org.hibernate.Session hibSession, Long schedulingSubpartId) {
		return hibSession.createQuery("from Class_ x where x.schedulingSubpart.uniqueId = :schedulingSubpartId").setParameter("schedulingSubpartId", schedulingSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByParentClass(org.hibernate.Session hibSession, Long parentClassId) {
		return hibSession.createQuery("from Class_ x where x.parentClass.uniqueId = :parentClassId").setParameter("parentClassId", parentClassId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from Class_ x where x.datePattern.uniqueId = :datePatternId").setParameter("datePatternId", datePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByLmsInfo(org.hibernate.Session hibSession, Long lmsInfoId) {
		return hibSession.createQuery("from Class_ x where x.lmsInfo.uniqueId = :lmsInfoId").setParameter("lmsInfoId", lmsInfoId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByFundingDept(org.hibernate.Session hibSession, Long fundingDeptId) {
		return hibSession.createQuery("from Class_ x where x.fundingDept.uniqueId = :fundingDeptId").setParameter("fundingDeptId", fundingDeptId).list();
	}
}
