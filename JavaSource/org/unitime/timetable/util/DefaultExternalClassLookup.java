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
package org.unitime.timetable.util;

import java.util.List;

import org.unitime.timetable.interfaces.ExternalClassLookupInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.CourseOfferingDAO;

/**
 * @author Tomas Muller
 *
 */
public class DefaultExternalClassLookup implements ExternalClassLookupInterface {
	
	@Override
	public CourseOffering findCourseByExternalId(Long sessionId, String externalId) {
		return (CourseOffering) CourseOfferingDAO.getInstance().getSession().createQuery(
				"select distinct co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co " +
				"where io.session.uniqueId = :sessionId and c.externalUniqueId = :externalId and co.isControl = true"
				).setLong("sessionId", sessionId).setString("externalId", externalId).setCacheable(true).setMaxResults(1).uniqueResult();
	}

	@Override
	public List<Class_> findClassesByExternalId(Long sessionId, String externalId) {
		return (List<Class_>) CourseOfferingDAO.getInstance().getSession().createQuery(
				"select c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io " +
				"where io.session.uniqueId = :sessionId and c.externalUniqueId = :externalId"
				).setLong("sessionId", sessionId).setString("externalId", externalId).setCacheable(true).list();
	}
	
}
