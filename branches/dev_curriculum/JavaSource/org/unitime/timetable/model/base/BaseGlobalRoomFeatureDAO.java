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

import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;

public abstract class BaseGlobalRoomFeatureDAO extends _RootDAO {

	private static GlobalRoomFeatureDAO sInstance;

	public static GlobalRoomFeatureDAO getInstance () {
		if (sInstance == null) sInstance = new GlobalRoomFeatureDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return GlobalRoomFeature.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public GlobalRoomFeature get(Long uniqueId) {
		return (GlobalRoomFeature) get(getReferenceClass(), uniqueId);
	}

	public GlobalRoomFeature get(Long uniqueId, org.hibernate.Session hibSession) {
		return (GlobalRoomFeature) get(getReferenceClass(), uniqueId, hibSession);
	}

	public GlobalRoomFeature load(Long uniqueId) {
		return (GlobalRoomFeature) load(getReferenceClass(), uniqueId);
	}

	public GlobalRoomFeature load(Long uniqueId, org.hibernate.Session hibSession) {
		return (GlobalRoomFeature) load(getReferenceClass(), uniqueId, hibSession);
	}

	public GlobalRoomFeature loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		GlobalRoomFeature globalRoomFeature = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(globalRoomFeature)) Hibernate.initialize(globalRoomFeature);
		return globalRoomFeature;
	}

	public void save(GlobalRoomFeature globalRoomFeature) {
		save((Object) globalRoomFeature);
	}

	public void save(GlobalRoomFeature globalRoomFeature, org.hibernate.Session hibSession) {
		save((Object) globalRoomFeature, hibSession);
	}

	public void saveOrUpdate(GlobalRoomFeature globalRoomFeature) {
		saveOrUpdate((Object) globalRoomFeature);
	}

	public void saveOrUpdate(GlobalRoomFeature globalRoomFeature, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) globalRoomFeature, hibSession);
	}


	public void update(GlobalRoomFeature globalRoomFeature) {
		update((Object) globalRoomFeature);
	}

	public void update(GlobalRoomFeature globalRoomFeature, org.hibernate.Session hibSession) {
		update((Object) globalRoomFeature, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(GlobalRoomFeature globalRoomFeature) {
		delete((Object) globalRoomFeature);
	}

	public void delete(GlobalRoomFeature globalRoomFeature, org.hibernate.Session hibSession) {
		delete((Object) globalRoomFeature, hibSession);
	}

	public void refresh(GlobalRoomFeature globalRoomFeature, org.hibernate.Session hibSession) {
		refresh((Object) globalRoomFeature, hibSession);
	}

	public List<GlobalRoomFeature> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from GlobalRoomFeature").list();
	}
}
