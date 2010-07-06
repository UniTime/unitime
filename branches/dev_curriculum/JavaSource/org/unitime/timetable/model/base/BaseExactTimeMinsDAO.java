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

import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExactTimeMinsDAO;

public abstract class BaseExactTimeMinsDAO extends _RootDAO {

	private static ExactTimeMinsDAO sInstance;

	public static ExactTimeMinsDAO getInstance () {
		if (sInstance == null) sInstance = new ExactTimeMinsDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExactTimeMins.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExactTimeMins get(Long uniqueId) {
		return (ExactTimeMins) get(getReferenceClass(), uniqueId);
	}

	public ExactTimeMins get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExactTimeMins) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExactTimeMins load(Long uniqueId) {
		return (ExactTimeMins) load(getReferenceClass(), uniqueId);
	}

	public ExactTimeMins load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExactTimeMins) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExactTimeMins loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExactTimeMins exactTimeMins = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(exactTimeMins)) Hibernate.initialize(exactTimeMins);
		return exactTimeMins;
	}

	public void save(ExactTimeMins exactTimeMins) {
		save((Object) exactTimeMins);
	}

	public void save(ExactTimeMins exactTimeMins, org.hibernate.Session hibSession) {
		save((Object) exactTimeMins, hibSession);
	}

	public void saveOrUpdate(ExactTimeMins exactTimeMins) {
		saveOrUpdate((Object) exactTimeMins);
	}

	public void saveOrUpdate(ExactTimeMins exactTimeMins, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) exactTimeMins, hibSession);
	}


	public void update(ExactTimeMins exactTimeMins) {
		update((Object) exactTimeMins);
	}

	public void update(ExactTimeMins exactTimeMins, org.hibernate.Session hibSession) {
		update((Object) exactTimeMins, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExactTimeMins exactTimeMins) {
		delete((Object) exactTimeMins);
	}

	public void delete(ExactTimeMins exactTimeMins, org.hibernate.Session hibSession) {
		delete((Object) exactTimeMins, hibSession);
	}

	public void refresh(ExactTimeMins exactTimeMins, org.hibernate.Session hibSession) {
		refresh((Object) exactTimeMins, hibSession);
	}

	public List<ExactTimeMins> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExactTimeMins").list();
	}
}
