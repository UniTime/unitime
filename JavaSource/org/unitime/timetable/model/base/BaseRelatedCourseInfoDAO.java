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

import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRelatedCourseInfoDAO extends _RootDAO<RelatedCourseInfo,Long> {

	private static RelatedCourseInfoDAO sInstance;

	public static RelatedCourseInfoDAO getInstance() {
		if (sInstance == null) sInstance = new RelatedCourseInfoDAO();
		return sInstance;
	}

	public Class<RelatedCourseInfo> getReferenceClass() {
		return RelatedCourseInfo.class;
	}

	@SuppressWarnings("unchecked")
	public List<RelatedCourseInfo> findByEvent(org.hibernate.Session hibSession, Long eventId) {
		return hibSession.createQuery("from RelatedCourseInfo x where x.event.uniqueId = :eventId").setLong("eventId", eventId).list();
	}

	@SuppressWarnings("unchecked")
	public List<RelatedCourseInfo> findByCourse(org.hibernate.Session hibSession, Long courseId) {
		return hibSession.createQuery("from RelatedCourseInfo x where x.course.uniqueId = :courseId").setLong("courseId", courseId).list();
	}
}
