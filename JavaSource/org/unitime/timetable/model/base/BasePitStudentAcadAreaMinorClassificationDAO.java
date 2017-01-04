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

import org.unitime.timetable.model.PitStudentAcadAreaMinorClassification;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitStudentAcadAreaMinorClassificationDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitStudentAcadAreaMinorClassificationDAO extends _RootDAO<PitStudentAcadAreaMinorClassification,Long> {

	private static PitStudentAcadAreaMinorClassificationDAO sInstance;

	public static PitStudentAcadAreaMinorClassificationDAO getInstance() {
		if (sInstance == null) sInstance = new PitStudentAcadAreaMinorClassificationDAO();
		return sInstance;
	}

	public Class<PitStudentAcadAreaMinorClassification> getReferenceClass() {
		return PitStudentAcadAreaMinorClassification.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMinorClassification> findByPitStudent(org.hibernate.Session hibSession, Long pitStudentId) {
		return hibSession.createQuery("from PitStudentAcadAreaMinorClassification x where x.pitStudent.uniqueId = :pitStudentId").setLong("pitStudentId", pitStudentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMinorClassification> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from PitStudentAcadAreaMinorClassification x where x.academicArea.uniqueId = :academicAreaId").setLong("academicAreaId", academicAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMinorClassification> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from PitStudentAcadAreaMinorClassification x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMinorClassification> findByMinor(org.hibernate.Session hibSession, Long minorId) {
		return hibSession.createQuery("from PitStudentAcadAreaMinorClassification x where x.minor.uniqueId = :minorId").setLong("minorId", minorId).list();
	}
}
