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

import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StandardEventNoteDAO;

public abstract class BaseStandardEventNoteDAO extends _RootDAO {

	private static StandardEventNoteDAO sInstance;

	public static StandardEventNoteDAO getInstance () {
		if (sInstance == null) sInstance = new StandardEventNoteDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StandardEventNote.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StandardEventNote get(Long uniqueId) {
		return (StandardEventNote) get(getReferenceClass(), uniqueId);
	}

	public StandardEventNote get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StandardEventNote) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StandardEventNote load(Long uniqueId) {
		return (StandardEventNote) load(getReferenceClass(), uniqueId);
	}

	public StandardEventNote load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StandardEventNote) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StandardEventNote loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StandardEventNote standardEventNote = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(standardEventNote)) Hibernate.initialize(standardEventNote);
		return standardEventNote;
	}

	public void save(StandardEventNote standardEventNote) {
		save((Object) standardEventNote);
	}

	public void save(StandardEventNote standardEventNote, org.hibernate.Session hibSession) {
		save((Object) standardEventNote, hibSession);
	}

	public void saveOrUpdate(StandardEventNote standardEventNote) {
		saveOrUpdate((Object) standardEventNote);
	}

	public void saveOrUpdate(StandardEventNote standardEventNote, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) standardEventNote, hibSession);
	}


	public void update(StandardEventNote standardEventNote) {
		update((Object) standardEventNote);
	}

	public void update(StandardEventNote standardEventNote, org.hibernate.Session hibSession) {
		update((Object) standardEventNote, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StandardEventNote standardEventNote) {
		delete((Object) standardEventNote);
	}

	public void delete(StandardEventNote standardEventNote, org.hibernate.Session hibSession) {
		delete((Object) standardEventNote, hibSession);
	}

	public void refresh(StandardEventNote standardEventNote, org.hibernate.Session hibSession) {
		refresh((Object) standardEventNote, hibSession);
	}

	public List<StandardEventNote> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StandardEventNote").list();
	}
}
