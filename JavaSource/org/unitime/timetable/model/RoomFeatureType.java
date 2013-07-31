/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseRoomFeatureType;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;

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
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(t.getUniqueId() == null ? -1 : t.getUniqueId());
	}
	
	public static boolean hasFeatureTypes(Long sessionId) {
		if (sessionId == null)
			return !RoomFeatureTypeDAO.getInstance().findAll().isEmpty();
		
		if (((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(distinct featureType) from GlobalRoomFeature where session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).intValue() > 0)
			return true;
		return ((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(distinct featureType) from DepartmentRoomFeature where department.session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).intValue() > 0;
	}
	
	public static Set<RoomFeatureType> getRoomFeatureTypes(Long sessionId, boolean includeDepartmental) {
		Set<RoomFeatureType> types = new TreeSet<RoomFeatureType>();
		types.addAll(RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select distinct f.featureType from GlobalRoomFeature f where f.session.uniqueId = :sessionId and f.featureType is not null")
				.setLong("sessionId", sessionId).setCacheable(true).list());
		if (includeDepartmental) {
			types.addAll(RoomFeatureTypeDAO.getInstance().getSession().createQuery(
					"select distinct f.featureType from DepartmentRoomFeature f where f.department.session.uniqueId = :sessionId and f.featureType is not null")
					.setLong("sessionId", sessionId).setCacheable(true).list());
		}
		return types;
	}
	
	public static boolean hasRoomFeatureWithNoType(Long sessionId, boolean includeDepartmental) {
		if (((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(f) from GlobalRoomFeature f where f.session.uniqueId = :sessionId and f.featureType is null")
				.setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).intValue() > 0)
			return true;
		return includeDepartmental && ((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(f) from DepartmentRoomFeature f where f.department.session.uniqueId = :sessionId and f.featureType is null")
				.setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).intValue() > 0;
	}
	
	public static Set<RoomFeatureType> getRoomFeatureTypes(Long sessionId, Long examTypeId) {
		Set<RoomFeatureType> types = new TreeSet<RoomFeatureType>();
		types.addAll(RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select distinct f.featureType from GlobalRoomFeature f inner join f.rooms l, ExamType t where f.session.uniqueId = :sessionId and f.featureType is not null and t.uniqueId = :examTypeId and t in elements(l.examTypes)")
				.setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list());
		return types;
	}

	public static boolean hasRoomFeatureWithNoType(Long sessionId, Long examTypeId) {
		return ((Number)RoomFeatureTypeDAO.getInstance().getSession().createQuery(
				"select count(distinct f) from GlobalRoomFeature f inner join f.rooms l, ExamType t where l.session.uniqueId = :sessionId and f.featureType is null and t.uniqueId = :examTypeId and t in elements(l.examTypes)")
				.setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).uniqueResult()).intValue() > 0;
	}
}
