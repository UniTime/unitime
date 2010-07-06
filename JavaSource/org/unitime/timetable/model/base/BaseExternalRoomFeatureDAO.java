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

import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExternalRoomFeatureDAO;

public abstract class BaseExternalRoomFeatureDAO extends _RootDAO {

	private static ExternalRoomFeatureDAO sInstance;

	public static ExternalRoomFeatureDAO getInstance () {
		if (sInstance == null) sInstance = new ExternalRoomFeatureDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExternalRoomFeature.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExternalRoomFeature get(Long uniqueId) {
		return (ExternalRoomFeature) get(getReferenceClass(), uniqueId);
	}

	public ExternalRoomFeature get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalRoomFeature) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalRoomFeature load(Long uniqueId) {
		return (ExternalRoomFeature) load(getReferenceClass(), uniqueId);
	}

	public ExternalRoomFeature load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalRoomFeature) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalRoomFeature loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExternalRoomFeature externalRoomFeature = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(externalRoomFeature)) Hibernate.initialize(externalRoomFeature);
		return externalRoomFeature;
	}

	public void save(ExternalRoomFeature externalRoomFeature) {
		save((Object) externalRoomFeature);
	}

	public void save(ExternalRoomFeature externalRoomFeature, org.hibernate.Session hibSession) {
		save((Object) externalRoomFeature, hibSession);
	}

	public void saveOrUpdate(ExternalRoomFeature externalRoomFeature) {
		saveOrUpdate((Object) externalRoomFeature);
	}

	public void saveOrUpdate(ExternalRoomFeature externalRoomFeature, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) externalRoomFeature, hibSession);
	}


	public void update(ExternalRoomFeature externalRoomFeature) {
		update((Object) externalRoomFeature);
	}

	public void update(ExternalRoomFeature externalRoomFeature, org.hibernate.Session hibSession) {
		update((Object) externalRoomFeature, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExternalRoomFeature externalRoomFeature) {
		delete((Object) externalRoomFeature);
	}

	public void delete(ExternalRoomFeature externalRoomFeature, org.hibernate.Session hibSession) {
		delete((Object) externalRoomFeature, hibSession);
	}

	public void refresh(ExternalRoomFeature externalRoomFeature, org.hibernate.Session hibSession) {
		refresh((Object) externalRoomFeature, hibSession);
	}

	public List<ExternalRoomFeature> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExternalRoomFeature").list();
	}

	public List<ExternalRoomFeature> findByRoom(org.hibernate.Session hibSession, Long roomId) {
		return hibSession.createQuery("from ExternalRoomFeature x where x.room.uniqueId = :roomId").setLong("roomId", roomId).list();
	}
}
