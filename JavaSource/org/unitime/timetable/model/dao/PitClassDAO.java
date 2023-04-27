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
import org.unitime.timetable.model.PitClass;

public class PitClassDAO extends _RootDAO<PitClass,Long> {
	private static PitClassDAO sInstance;

	public PitClassDAO() {}

	public static PitClassDAO getInstance() {
		if (sInstance == null) sInstance = new PitClassDAO();
		return sInstance;
	}

	public Class<PitClass> getReferenceClass() {
		return PitClass.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from PitClass x where x.clazz.uniqueId = :clazzId", PitClass.class).setParameter("clazzId", clazzId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByManagingDept(org.hibernate.Session hibSession, Long managingDeptId) {
		return hibSession.createQuery("from PitClass x where x.managingDept.uniqueId = :managingDeptId", PitClass.class).setParameter("managingDeptId", managingDeptId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByPitSchedulingSubpart(org.hibernate.Session hibSession, Long pitSchedulingSubpartId) {
		return hibSession.createQuery("from PitClass x where x.pitSchedulingSubpart.uniqueId = :pitSchedulingSubpartId", PitClass.class).setParameter("pitSchedulingSubpartId", pitSchedulingSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByPitParentClass(org.hibernate.Session hibSession, Long pitParentClassId) {
		return hibSession.createQuery("from PitClass x where x.pitParentClass.uniqueId = :pitParentClassId", PitClass.class).setParameter("pitParentClassId", pitParentClassId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from PitClass x where x.datePattern.uniqueId = :datePatternId", PitClass.class).setParameter("datePatternId", datePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByTimePattern(org.hibernate.Session hibSession, Long timePatternId) {
		return hibSession.createQuery("from PitClass x where x.timePattern.uniqueId = :timePatternId", PitClass.class).setParameter("timePatternId", timePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClass> findByFundingDept(org.hibernate.Session hibSession, Long fundingDeptId) {
		return hibSession.createQuery("from PitClass x where x.fundingDept.uniqueId = :fundingDeptId", PitClass.class).setParameter("fundingDeptId", fundingDeptId).list();
	}
}
