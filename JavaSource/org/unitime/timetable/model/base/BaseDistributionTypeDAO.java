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

import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;

public abstract class BaseDistributionTypeDAO extends _RootDAO {

	private static DistributionTypeDAO sInstance;

	public static DistributionTypeDAO getInstance () {
		if (sInstance == null) sInstance = new DistributionTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DistributionType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DistributionType get(Long uniqueId) {
		return (DistributionType) get(getReferenceClass(), uniqueId);
	}

	public DistributionType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DistributionType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DistributionType load(Long uniqueId) {
		return (DistributionType) load(getReferenceClass(), uniqueId);
	}

	public DistributionType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DistributionType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DistributionType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DistributionType distributionType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(distributionType)) Hibernate.initialize(distributionType);
		return distributionType;
	}

	public void save(DistributionType distributionType) {
		save((Object) distributionType);
	}

	public void save(DistributionType distributionType, org.hibernate.Session hibSession) {
		save((Object) distributionType, hibSession);
	}

	public void saveOrUpdate(DistributionType distributionType) {
		saveOrUpdate((Object) distributionType);
	}

	public void saveOrUpdate(DistributionType distributionType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) distributionType, hibSession);
	}


	public void update(DistributionType distributionType) {
		update((Object) distributionType);
	}

	public void update(DistributionType distributionType, org.hibernate.Session hibSession) {
		update((Object) distributionType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DistributionType distributionType) {
		delete((Object) distributionType);
	}

	public void delete(DistributionType distributionType, org.hibernate.Session hibSession) {
		delete((Object) distributionType, hibSession);
	}

	public void refresh(DistributionType distributionType, org.hibernate.Session hibSession) {
		refresh((Object) distributionType, hibSession);
	}

	public List<DistributionType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DistributionType").list();
	}
}
