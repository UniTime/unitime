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
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AssignmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAssignmentDAO extends _RootDAO<Assignment,Long> {

	private static AssignmentDAO sInstance;

	public static AssignmentDAO getInstance() {
		if (sInstance == null) sInstance = new AssignmentDAO();
		return sInstance;
	}

	public Class<Assignment> getReferenceClass() {
		return Assignment.class;
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findByTimePattern(org.hibernate.Session hibSession, Long timePatternId) {
		return hibSession.createQuery("from Assignment x where x.timePattern.uniqueId = :timePatternId").setLong("timePatternId", timePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from Assignment x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from Assignment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from Assignment x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
