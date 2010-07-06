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

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DistributionObjectDAO;

public abstract class BaseDistributionObjectDAO extends _RootDAO {

	private static DistributionObjectDAO sInstance;

	public static DistributionObjectDAO getInstance () {
		if (sInstance == null) sInstance = new DistributionObjectDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DistributionObject.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DistributionObject get(Long uniqueId) {
		return (DistributionObject) get(getReferenceClass(), uniqueId);
	}

	public DistributionObject get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DistributionObject) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DistributionObject load(Long uniqueId) {
		return (DistributionObject) load(getReferenceClass(), uniqueId);
	}

	public DistributionObject load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DistributionObject) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DistributionObject loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DistributionObject distributionObject = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(distributionObject)) Hibernate.initialize(distributionObject);
		return distributionObject;
	}

	public void save(DistributionObject distributionObject) {
		save((Object) distributionObject);
	}

	public void save(DistributionObject distributionObject, org.hibernate.Session hibSession) {
		save((Object) distributionObject, hibSession);
	}

	public void saveOrUpdate(DistributionObject distributionObject) {
		saveOrUpdate((Object) distributionObject);
	}

	public void saveOrUpdate(DistributionObject distributionObject, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) distributionObject, hibSession);
	}


	public void update(DistributionObject distributionObject) {
		update((Object) distributionObject);
	}

	public void update(DistributionObject distributionObject, org.hibernate.Session hibSession) {
		update((Object) distributionObject, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DistributionObject distributionObject) {
		delete((Object) distributionObject);
	}

	public void delete(DistributionObject distributionObject, org.hibernate.Session hibSession) {
		delete((Object) distributionObject, hibSession);
	}

	public void refresh(DistributionObject distributionObject, org.hibernate.Session hibSession) {
		refresh((Object) distributionObject, hibSession);
	}

	public List<DistributionObject> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DistributionObject").list();
	}

	public List<DistributionObject> findByDistributionPref(org.hibernate.Session hibSession, Long distributionPrefId) {
		return hibSession.createQuery("from DistributionObject x where x.distributionPref.uniqueId = :distributionPrefId").setLong("distributionPrefId", distributionPrefId).list();
	}

	public List<DistributionObject> findByPrefGroup(org.hibernate.Session hibSession, Long prefGroupId) {
		return hibSession.createQuery("from DistributionObject x where x.prefGroup.uniqueId = :prefGroupId").setLong("prefGroupId", prefGroupId).list();
	}
}
