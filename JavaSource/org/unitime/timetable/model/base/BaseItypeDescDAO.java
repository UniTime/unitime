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

import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;

public abstract class BaseItypeDescDAO extends _RootDAO {

	private static ItypeDescDAO sInstance;

	public static ItypeDescDAO getInstance () {
		if (sInstance == null) sInstance = new ItypeDescDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ItypeDesc.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ItypeDesc get(Integer itype) {
		return (ItypeDesc) get(getReferenceClass(), itype);
	}

	public ItypeDesc get(Integer itype, org.hibernate.Session hibSession) {
		return (ItypeDesc) get(getReferenceClass(), itype, hibSession);
	}

	public ItypeDesc load(Integer itype) {
		return (ItypeDesc) load(getReferenceClass(), itype);
	}

	public ItypeDesc load(Integer itype, org.hibernate.Session hibSession) {
		return (ItypeDesc) load(getReferenceClass(), itype, hibSession);
	}

	public ItypeDesc loadInitialize(Integer itype, org.hibernate.Session hibSession) {
		ItypeDesc itypeDesc = load(itype, hibSession);
		if (!Hibernate.isInitialized(itypeDesc)) Hibernate.initialize(itypeDesc);
		return itypeDesc;
	}

	public void save(ItypeDesc itypeDesc) {
		save((Object) itypeDesc);
	}

	public void save(ItypeDesc itypeDesc, org.hibernate.Session hibSession) {
		save((Object) itypeDesc, hibSession);
	}

	public void saveOrUpdate(ItypeDesc itypeDesc) {
		saveOrUpdate((Object) itypeDesc);
	}

	public void saveOrUpdate(ItypeDesc itypeDesc, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) itypeDesc, hibSession);
	}


	public void update(ItypeDesc itypeDesc) {
		update((Object) itypeDesc);
	}

	public void update(ItypeDesc itypeDesc, org.hibernate.Session hibSession) {
		update((Object) itypeDesc, hibSession);
	}

	public void delete(Integer itype) {
		delete(load(itype));
	}

	public void delete(Integer itype, org.hibernate.Session hibSession) {
		delete(load(itype, hibSession), hibSession);
	}

	public void delete(ItypeDesc itypeDesc) {
		delete((Object) itypeDesc);
	}

	public void delete(ItypeDesc itypeDesc, org.hibernate.Session hibSession) {
		delete((Object) itypeDesc, hibSession);
	}

	public void refresh(ItypeDesc itypeDesc, org.hibernate.Session hibSession) {
		refresh((Object) itypeDesc, hibSession);
	}

	public List<ItypeDesc> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ItypeDesc").list();
	}

	public List<ItypeDesc> findByParent(org.hibernate.Session hibSession, Integer parentId) {
		return hibSession.createQuery("from ItypeDesc x where x.parent.itype = :parentId").setInteger("parentId", parentId).list();
	}
}
