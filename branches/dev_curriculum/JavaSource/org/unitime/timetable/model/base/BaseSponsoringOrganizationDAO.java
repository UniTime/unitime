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

import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;

public abstract class BaseSponsoringOrganizationDAO extends _RootDAO {

	private static SponsoringOrganizationDAO sInstance;

	public static SponsoringOrganizationDAO getInstance () {
		if (sInstance == null) sInstance = new SponsoringOrganizationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SponsoringOrganization.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SponsoringOrganization get(Long uniqueId) {
		return (SponsoringOrganization) get(getReferenceClass(), uniqueId);
	}

	public SponsoringOrganization get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SponsoringOrganization) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SponsoringOrganization load(Long uniqueId) {
		return (SponsoringOrganization) load(getReferenceClass(), uniqueId);
	}

	public SponsoringOrganization load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SponsoringOrganization) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SponsoringOrganization loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SponsoringOrganization sponsoringOrganization = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(sponsoringOrganization)) Hibernate.initialize(sponsoringOrganization);
		return sponsoringOrganization;
	}

	public void save(SponsoringOrganization sponsoringOrganization) {
		save((Object) sponsoringOrganization);
	}

	public void save(SponsoringOrganization sponsoringOrganization, org.hibernate.Session hibSession) {
		save((Object) sponsoringOrganization, hibSession);
	}

	public void saveOrUpdate(SponsoringOrganization sponsoringOrganization) {
		saveOrUpdate((Object) sponsoringOrganization);
	}

	public void saveOrUpdate(SponsoringOrganization sponsoringOrganization, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) sponsoringOrganization, hibSession);
	}


	public void update(SponsoringOrganization sponsoringOrganization) {
		update((Object) sponsoringOrganization);
	}

	public void update(SponsoringOrganization sponsoringOrganization, org.hibernate.Session hibSession) {
		update((Object) sponsoringOrganization, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SponsoringOrganization sponsoringOrganization) {
		delete((Object) sponsoringOrganization);
	}

	public void delete(SponsoringOrganization sponsoringOrganization, org.hibernate.Session hibSession) {
		delete((Object) sponsoringOrganization, hibSession);
	}

	public void refresh(SponsoringOrganization sponsoringOrganization, org.hibernate.Session hibSession) {
		refresh((Object) sponsoringOrganization, hibSession);
	}

	public List<SponsoringOrganization> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SponsoringOrganization").list();
	}
}
