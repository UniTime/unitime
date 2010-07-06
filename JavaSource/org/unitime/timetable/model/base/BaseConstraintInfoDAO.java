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

import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ConstraintInfoDAO;

public abstract class BaseConstraintInfoDAO extends _RootDAO {

	private static ConstraintInfoDAO sInstance;

	public static ConstraintInfoDAO getInstance () {
		if (sInstance == null) sInstance = new ConstraintInfoDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ConstraintInfo.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ConstraintInfo get(Long uniqueId) {
		return (ConstraintInfo) get(getReferenceClass(), uniqueId);
	}

	public ConstraintInfo get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ConstraintInfo) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ConstraintInfo load(Long uniqueId) {
		return (ConstraintInfo) load(getReferenceClass(), uniqueId);
	}

	public ConstraintInfo load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ConstraintInfo) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ConstraintInfo loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ConstraintInfo constraintInfo = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(constraintInfo)) Hibernate.initialize(constraintInfo);
		return constraintInfo;
	}

	public void save(ConstraintInfo constraintInfo) {
		save((Object) constraintInfo);
	}

	public void save(ConstraintInfo constraintInfo, org.hibernate.Session hibSession) {
		save((Object) constraintInfo, hibSession);
	}

	public void saveOrUpdate(ConstraintInfo constraintInfo) {
		saveOrUpdate((Object) constraintInfo);
	}

	public void saveOrUpdate(ConstraintInfo constraintInfo, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) constraintInfo, hibSession);
	}


	public void update(ConstraintInfo constraintInfo) {
		update((Object) constraintInfo);
	}

	public void update(ConstraintInfo constraintInfo, org.hibernate.Session hibSession) {
		update((Object) constraintInfo, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ConstraintInfo constraintInfo) {
		delete((Object) constraintInfo);
	}

	public void delete(ConstraintInfo constraintInfo, org.hibernate.Session hibSession) {
		delete((Object) constraintInfo, hibSession);
	}

	public void refresh(ConstraintInfo constraintInfo, org.hibernate.Session hibSession) {
		refresh((Object) constraintInfo, hibSession);
	}

	public List<ConstraintInfo> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ConstraintInfo").list();
	}
}
