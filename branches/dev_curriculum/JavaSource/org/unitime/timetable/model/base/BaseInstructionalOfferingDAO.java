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

import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;

public abstract class BaseInstructionalOfferingDAO extends _RootDAO {

	private static InstructionalOfferingDAO sInstance;

	public static InstructionalOfferingDAO getInstance () {
		if (sInstance == null) sInstance = new InstructionalOfferingDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return InstructionalOffering.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public InstructionalOffering get(Long uniqueId) {
		return (InstructionalOffering) get(getReferenceClass(), uniqueId);
	}

	public InstructionalOffering get(Long uniqueId, org.hibernate.Session hibSession) {
		return (InstructionalOffering) get(getReferenceClass(), uniqueId, hibSession);
	}

	public InstructionalOffering load(Long uniqueId) {
		return (InstructionalOffering) load(getReferenceClass(), uniqueId);
	}

	public InstructionalOffering load(Long uniqueId, org.hibernate.Session hibSession) {
		return (InstructionalOffering) load(getReferenceClass(), uniqueId, hibSession);
	}

	public InstructionalOffering loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		InstructionalOffering instructionalOffering = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(instructionalOffering)) Hibernate.initialize(instructionalOffering);
		return instructionalOffering;
	}

	public void save(InstructionalOffering instructionalOffering) {
		save((Object) instructionalOffering);
	}

	public void save(InstructionalOffering instructionalOffering, org.hibernate.Session hibSession) {
		save((Object) instructionalOffering, hibSession);
	}

	public void saveOrUpdate(InstructionalOffering instructionalOffering) {
		saveOrUpdate((Object) instructionalOffering);
	}

	public void saveOrUpdate(InstructionalOffering instructionalOffering, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) instructionalOffering, hibSession);
	}


	public void update(InstructionalOffering instructionalOffering) {
		update((Object) instructionalOffering);
	}

	public void update(InstructionalOffering instructionalOffering, org.hibernate.Session hibSession) {
		update((Object) instructionalOffering, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(InstructionalOffering instructionalOffering) {
		delete((Object) instructionalOffering);
	}

	public void delete(InstructionalOffering instructionalOffering, org.hibernate.Session hibSession) {
		delete((Object) instructionalOffering, hibSession);
	}

	public void refresh(InstructionalOffering instructionalOffering, org.hibernate.Session hibSession) {
		refresh((Object) instructionalOffering, hibSession);
	}

	public List<InstructionalOffering> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from InstructionalOffering").list();
	}

	public List<InstructionalOffering> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from InstructionalOffering x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	public List<InstructionalOffering> findByConsentType(org.hibernate.Session hibSession, Long consentTypeId) {
		return hibSession.createQuery("from InstructionalOffering x where x.consentType.uniqueId = :consentTypeId").setLong("consentTypeId", consentTypeId).list();
	}
}
