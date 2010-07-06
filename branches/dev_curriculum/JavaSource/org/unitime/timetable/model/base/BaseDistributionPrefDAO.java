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

import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;

public abstract class BaseDistributionPrefDAO extends _RootDAO {

	private static DistributionPrefDAO sInstance;

	public static DistributionPrefDAO getInstance () {
		if (sInstance == null) sInstance = new DistributionPrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DistributionPref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DistributionPref get(Long uniqueId) {
		return (DistributionPref) get(getReferenceClass(), uniqueId);
	}

	public DistributionPref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DistributionPref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DistributionPref load(Long uniqueId) {
		return (DistributionPref) load(getReferenceClass(), uniqueId);
	}

	public DistributionPref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DistributionPref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DistributionPref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DistributionPref distributionPref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(distributionPref)) Hibernate.initialize(distributionPref);
		return distributionPref;
	}

	public void save(DistributionPref distributionPref) {
		save((Object) distributionPref);
	}

	public void save(DistributionPref distributionPref, org.hibernate.Session hibSession) {
		save((Object) distributionPref, hibSession);
	}

	public void saveOrUpdate(DistributionPref distributionPref) {
		saveOrUpdate((Object) distributionPref);
	}

	public void saveOrUpdate(DistributionPref distributionPref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) distributionPref, hibSession);
	}


	public void update(DistributionPref distributionPref) {
		update((Object) distributionPref);
	}

	public void update(DistributionPref distributionPref, org.hibernate.Session hibSession) {
		update((Object) distributionPref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DistributionPref distributionPref) {
		delete((Object) distributionPref);
	}

	public void delete(DistributionPref distributionPref, org.hibernate.Session hibSession) {
		delete((Object) distributionPref, hibSession);
	}

	public void refresh(DistributionPref distributionPref, org.hibernate.Session hibSession) {
		refresh((Object) distributionPref, hibSession);
	}

	public List<DistributionPref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DistributionPref").list();
	}

	public List<DistributionPref> findByDistributionType(org.hibernate.Session hibSession, Long distributionTypeId) {
		return hibSession.createQuery("from DistributionPref x where x.distributionType.uniqueId = :distributionTypeId").setLong("distributionTypeId", distributionTypeId).list();
	}
}
