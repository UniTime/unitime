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

import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;

public abstract class BaseInstrOfferingConfigDAO extends _RootDAO {

	private static InstrOfferingConfigDAO sInstance;

	public static InstrOfferingConfigDAO getInstance () {
		if (sInstance == null) sInstance = new InstrOfferingConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return InstrOfferingConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public InstrOfferingConfig get(Long uniqueId) {
		return (InstrOfferingConfig) get(getReferenceClass(), uniqueId);
	}

	public InstrOfferingConfig get(Long uniqueId, org.hibernate.Session hibSession) {
		return (InstrOfferingConfig) get(getReferenceClass(), uniqueId, hibSession);
	}

	public InstrOfferingConfig load(Long uniqueId) {
		return (InstrOfferingConfig) load(getReferenceClass(), uniqueId);
	}

	public InstrOfferingConfig load(Long uniqueId, org.hibernate.Session hibSession) {
		return (InstrOfferingConfig) load(getReferenceClass(), uniqueId, hibSession);
	}

	public InstrOfferingConfig loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		InstrOfferingConfig instrOfferingConfig = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(instrOfferingConfig)) Hibernate.initialize(instrOfferingConfig);
		return instrOfferingConfig;
	}

	public void save(InstrOfferingConfig instrOfferingConfig) {
		save((Object) instrOfferingConfig);
	}

	public void save(InstrOfferingConfig instrOfferingConfig, org.hibernate.Session hibSession) {
		save((Object) instrOfferingConfig, hibSession);
	}

	public void saveOrUpdate(InstrOfferingConfig instrOfferingConfig) {
		saveOrUpdate((Object) instrOfferingConfig);
	}

	public void saveOrUpdate(InstrOfferingConfig instrOfferingConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) instrOfferingConfig, hibSession);
	}


	public void update(InstrOfferingConfig instrOfferingConfig) {
		update((Object) instrOfferingConfig);
	}

	public void update(InstrOfferingConfig instrOfferingConfig, org.hibernate.Session hibSession) {
		update((Object) instrOfferingConfig, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(InstrOfferingConfig instrOfferingConfig) {
		delete((Object) instrOfferingConfig);
	}

	public void delete(InstrOfferingConfig instrOfferingConfig, org.hibernate.Session hibSession) {
		delete((Object) instrOfferingConfig, hibSession);
	}

	public void refresh(InstrOfferingConfig instrOfferingConfig, org.hibernate.Session hibSession) {
		refresh((Object) instrOfferingConfig, hibSession);
	}

	public List<InstrOfferingConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from InstrOfferingConfig").list();
	}

	public List<InstrOfferingConfig> findByInstructionalOffering(org.hibernate.Session hibSession, Long instructionalOfferingId) {
		return hibSession.createQuery("from InstrOfferingConfig x where x.instructionalOffering.uniqueId = :instructionalOfferingId").setLong("instructionalOfferingId", instructionalOfferingId).list();
	}
}
