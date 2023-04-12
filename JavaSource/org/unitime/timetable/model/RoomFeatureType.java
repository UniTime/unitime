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



import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseRoomFeatureType;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "feature_type")
public class RoomFeatureType extends BaseRoomFeatureType implements Comparable<RoomFeatureType> {
	private static final long serialVersionUID = 1L;

	public RoomFeatureType() {
		super();
	}

	@Override
	public int compareTo(RoomFeatureType t) {
		int cmp = getLabel().compareTo(t.getLabel());
		if (cmp != 0)
			return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(t.getUniqueId() == null ? -1 : t.getUniqueId());
	}
	
	public static boolean hasFeatureTypes(Long sessionId) {
		if (sessionId == null)
			return !RoomFeatureTypeDAO.getInstance().findAll().isEmpty();
		
		if (((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(distinct featureType) from GlobalRoomFeature where session.uniqueId = :sessionId")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0)
			return true;
		return ((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(distinct featureType) from DepartmentRoomFeature where department.session.uniqueId = :sessionId")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0;
	}
	
	public static Set<RoomFeatureType> getRoomFeatureTypes(Long sessionId, boolean includeDepartmental) {
		Set<RoomFeatureType> types = new TreeSet<RoomFeatureType>();
		types.addAll(RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select distinct f.featureType from GlobalRoomFeature f where f.session.uniqueId = :sessionId and f.featureType is not null")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).list());
		if (includeDepartmental) {
			types.addAll(RoomFeatureTypeDAO.getInstance().getSession().createQuery(
					"select distinct f.featureType from DepartmentRoomFeature f where f.department.session.uniqueId = :sessionId and f.featureType is not null")
					.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).list());
		}
		return types;
	}
	
	public static boolean hasRoomFeatureWithNoType(Long sessionId, boolean includeDepartmental) {
		if (((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(f) from GlobalRoomFeature f where f.session.uniqueId = :sessionId and f.featureType is null")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0)
			return true;
		return includeDepartmental && ((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(f) from DepartmentRoomFeature f where f.department.session.uniqueId = :sessionId and f.featureType is null")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0;
	}
	
	public static Set<RoomFeatureType> getRoomFeatureTypes(Long sessionId, Long examTypeId) {
		Set<RoomFeatureType> types = new TreeSet<RoomFeatureType>();
		types.addAll(RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select distinct f.featureType from GlobalRoomFeature f inner join f.rooms l, ExamType t where f.session.uniqueId = :sessionId and f.featureType is not null and t.uniqueId = :examTypeId and t in elements(l.examTypes)")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setParameter("examTypeId", examTypeId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).list());
		return types;
	}

	public static boolean hasRoomFeatureWithNoType(Long sessionId, Long examTypeId) {
		return ((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(distinct f) from GlobalRoomFeature f inner join f.rooms l, ExamType t where l.session.uniqueId = :sessionId and f.featureType is null and t.uniqueId = :examTypeId and t in elements(l.examTypes)")
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setParameter("examTypeId", examTypeId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0;
	}
}
