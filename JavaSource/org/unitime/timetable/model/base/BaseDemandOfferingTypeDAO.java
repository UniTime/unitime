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

import org.unitime.timetable.model.DemandOfferingType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DemandOfferingTypeDAO;

public abstract class BaseDemandOfferingTypeDAO extends _RootDAO {

	private static DemandOfferingTypeDAO sInstance;

	public static DemandOfferingTypeDAO getInstance () {
		if (sInstance == null) sInstance = new DemandOfferingTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DemandOfferingType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DemandOfferingType get(Long uniqueId) {
		return (DemandOfferingType) get(getReferenceClass(), uniqueId);
	}

	public DemandOfferingType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DemandOfferingType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DemandOfferingType load(Long uniqueId) {
		return (DemandOfferingType) load(getReferenceClass(), uniqueId);
	}

	public DemandOfferingType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DemandOfferingType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DemandOfferingType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DemandOfferingType demandOfferingType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(demandOfferingType)) Hibernate.initialize(demandOfferingType);
		return demandOfferingType;
	}

	public void save(DemandOfferingType demandOfferingType) {
		save((Object) demandOfferingType);
	}

	public void save(DemandOfferingType demandOfferingType, org.hibernate.Session hibSession) {
		save((Object) demandOfferingType, hibSession);
	}

	public void saveOrUpdate(DemandOfferingType demandOfferingType) {
		saveOrUpdate((Object) demandOfferingType);
	}

	public void saveOrUpdate(DemandOfferingType demandOfferingType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) demandOfferingType, hibSession);
	}


	public void update(DemandOfferingType demandOfferingType) {
		update((Object) demandOfferingType);
	}

	public void update(DemandOfferingType demandOfferingType, org.hibernate.Session hibSession) {
		update((Object) demandOfferingType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DemandOfferingType demandOfferingType) {
		delete((Object) demandOfferingType);
	}

	public void delete(DemandOfferingType demandOfferingType, org.hibernate.Session hibSession) {
		delete((Object) demandOfferingType, hibSession);
	}

	public void refresh(DemandOfferingType demandOfferingType, org.hibernate.Session hibSession) {
		refresh((Object) demandOfferingType, hibSession);
	}

	public List<DemandOfferingType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DemandOfferingType").list();
	}
}
