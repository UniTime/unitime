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

import org.unitime.timetable.model.base.BaseTeachingResponsibility;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;

public class TeachingResponsibility extends BaseTeachingResponsibility {
	private static final long serialVersionUID = 1L;

	public TeachingResponsibility() {
		super();
	}
	
	public static List<TeachingResponsibility> getInstructorTeachingResponsibilities() {
		return (List<TeachingResponsibility>)TeachingResponsibilityDAO.getInstance().getSession().createQuery(
				"from TeachingResponsibility where instructor = true order by label"
				).setCacheable(true).list();
    }
	
	public static List<TeachingResponsibility> getCoordinatorTeachingResponsibilities() {
		return (List<TeachingResponsibility>)TeachingResponsibilityDAO.getInstance().getSession().createQuery(
				"from TeachingResponsibility where coordinator = true order by label"
				).setCacheable(true).list();
    }
    
	public static TeachingResponsibility getTeachingResponsibility(String reference, org.hibernate.Session hibSession) {
		if (reference == null || reference.isEmpty()) return null;
		return (TeachingResponsibility)hibSession.createQuery(
				"from TeachingResponsibility where reference = :reference")
				.setString("reference", reference).setMaxResults(1).setCacheable(true).uniqueResult();
	}

}
