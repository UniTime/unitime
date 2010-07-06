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

import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

public abstract class BaseSchedulingSubpartDAO extends _RootDAO {

	private static SchedulingSubpartDAO sInstance;

	public static SchedulingSubpartDAO getInstance () {
		if (sInstance == null) sInstance = new SchedulingSubpartDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SchedulingSubpart.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SchedulingSubpart get(Long uniqueId) {
		return (SchedulingSubpart) get(getReferenceClass(), uniqueId);
	}

	public SchedulingSubpart get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SchedulingSubpart) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SchedulingSubpart load(Long uniqueId) {
		return (SchedulingSubpart) load(getReferenceClass(), uniqueId);
	}

	public SchedulingSubpart load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SchedulingSubpart) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SchedulingSubpart loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SchedulingSubpart schedulingSubpart = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(schedulingSubpart)) Hibernate.initialize(schedulingSubpart);
		return schedulingSubpart;
	}

	public void save(SchedulingSubpart schedulingSubpart) {
		save((Object) schedulingSubpart);
	}

	public void save(SchedulingSubpart schedulingSubpart, org.hibernate.Session hibSession) {
		save((Object) schedulingSubpart, hibSession);
	}

	public void saveOrUpdate(SchedulingSubpart schedulingSubpart) {
		saveOrUpdate((Object) schedulingSubpart);
	}

	public void saveOrUpdate(SchedulingSubpart schedulingSubpart, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) schedulingSubpart, hibSession);
	}


	public void update(SchedulingSubpart schedulingSubpart) {
		update((Object) schedulingSubpart);
	}

	public void update(SchedulingSubpart schedulingSubpart, org.hibernate.Session hibSession) {
		update((Object) schedulingSubpart, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SchedulingSubpart schedulingSubpart) {
		delete((Object) schedulingSubpart);
	}

	public void delete(SchedulingSubpart schedulingSubpart, org.hibernate.Session hibSession) {
		delete((Object) schedulingSubpart, hibSession);
	}

	public void refresh(SchedulingSubpart schedulingSubpart, org.hibernate.Session hibSession) {
		refresh((Object) schedulingSubpart, hibSession);
	}

	public List<SchedulingSubpart> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SchedulingSubpart").list();
	}

	public List<SchedulingSubpart> findByItype(org.hibernate.Session hibSession, Integer itypeId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.itype.itype = :itypeId").setInteger("itypeId", itypeId).list();
	}

	public List<SchedulingSubpart> findByParentSubpart(org.hibernate.Session hibSession, Long parentSubpartId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.parentSubpart.uniqueId = :parentSubpartId").setLong("parentSubpartId", parentSubpartId).list();
	}

	public List<SchedulingSubpart> findByInstrOfferingConfig(org.hibernate.Session hibSession, Long instrOfferingConfigId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.instrOfferingConfig.uniqueId = :instrOfferingConfigId").setLong("instrOfferingConfigId", instrOfferingConfigId).list();
	}

	public List<SchedulingSubpart> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}
}
