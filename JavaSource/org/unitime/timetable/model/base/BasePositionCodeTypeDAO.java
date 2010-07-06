/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;

import org.unitime.timetable.model.PositionCodeType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PositionCodeTypeDAO;

public abstract class BasePositionCodeTypeDAO extends _RootDAO {

	private static PositionCodeTypeDAO sInstance;

	public static PositionCodeTypeDAO getInstance () {
		if (sInstance == null) sInstance = new PositionCodeTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PositionCodeType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PositionCodeType get(String positionCode) {
		return (PositionCodeType) get(getReferenceClass(), positionCode);
	}

	public PositionCodeType get(String positionCode, org.hibernate.Session hibSession) {
		return (PositionCodeType) get(getReferenceClass(), positionCode, hibSession);
	}

	public PositionCodeType load(String positionCode) {
		return (PositionCodeType) load(getReferenceClass(), positionCode);
	}

	public PositionCodeType load(String positionCode, org.hibernate.Session hibSession) {
		return (PositionCodeType) load(getReferenceClass(), positionCode, hibSession);
	}

	public PositionCodeType loadInitialize(String positionCode, org.hibernate.Session hibSession) {
		PositionCodeType positionCodeType = load(positionCode, hibSession);
		if (!Hibernate.isInitialized(positionCodeType)) Hibernate.initialize(positionCodeType);
		return positionCodeType;
	}

	public void save(PositionCodeType positionCodeType) {
		save((Object) positionCodeType);
	}

	public void save(PositionCodeType positionCodeType, org.hibernate.Session hibSession) {
		save((Object) positionCodeType, hibSession);
	}

	public void saveOrUpdate(PositionCodeType positionCodeType) {
		saveOrUpdate((Object) positionCodeType);
	}

	public void saveOrUpdate(PositionCodeType positionCodeType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) positionCodeType, hibSession);
	}


	public void update(PositionCodeType positionCodeType) {
		update((Object) positionCodeType);
	}

	public void update(PositionCodeType positionCodeType, org.hibernate.Session hibSession) {
		update((Object) positionCodeType, hibSession);
	}

	public void delete(Object positionCode) {
		if (positionCode instanceof String)
			delete((Object) load((String)positionCode));
		else
		super.delete(positionCode);
	}

	public void delete(Object positionCode, org.hibernate.Session hibSession) {
		if (positionCode instanceof String)
			delete((Object) load((String)positionCode, hibSession), hibSession);
		else
			super.delete(positionCode, hibSession);
	}

	public void delete(PositionCodeType positionCodeType) {
		delete((Object) positionCodeType);
	}

	public void delete(PositionCodeType positionCodeType, org.hibernate.Session hibSession) {
		delete((Object) positionCodeType, hibSession);
	}

	public void refresh(PositionCodeType positionCodeType, org.hibernate.Session hibSession) {
		refresh((Object) positionCodeType, hibSession);
	}

	public List<PositionCodeType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from PositionCodeType").list();
	}

	public List<PositionCodeType> findByPositionType(org.hibernate.Session hibSession, Long positionTypeId) {
		return hibSession.createQuery("from PositionCodeType x where x.positionType.uniqueId = :positionTypeId").setLong("positionTypeId", positionTypeId).list();
	}
}
