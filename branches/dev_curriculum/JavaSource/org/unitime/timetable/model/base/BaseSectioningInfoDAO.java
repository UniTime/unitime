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

import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SectioningInfoDAO;

public abstract class BaseSectioningInfoDAO extends _RootDAO {

	private static SectioningInfoDAO sInstance;

	public static SectioningInfoDAO getInstance () {
		if (sInstance == null) sInstance = new SectioningInfoDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SectioningInfo.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SectioningInfo get(Long uniqueId) {
		return (SectioningInfo) get(getReferenceClass(), uniqueId);
	}

	public SectioningInfo get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SectioningInfo) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SectioningInfo load(Long uniqueId) {
		return (SectioningInfo) load(getReferenceClass(), uniqueId);
	}

	public SectioningInfo load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SectioningInfo) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SectioningInfo loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SectioningInfo sectioningInfo = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(sectioningInfo)) Hibernate.initialize(sectioningInfo);
		return sectioningInfo;
	}

	public void save(SectioningInfo sectioningInfo) {
		save((Object) sectioningInfo);
	}

	public void save(SectioningInfo sectioningInfo, org.hibernate.Session hibSession) {
		save((Object) sectioningInfo, hibSession);
	}

	public void saveOrUpdate(SectioningInfo sectioningInfo) {
		saveOrUpdate((Object) sectioningInfo);
	}

	public void saveOrUpdate(SectioningInfo sectioningInfo, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) sectioningInfo, hibSession);
	}


	public void update(SectioningInfo sectioningInfo) {
		update((Object) sectioningInfo);
	}

	public void update(SectioningInfo sectioningInfo, org.hibernate.Session hibSession) {
		update((Object) sectioningInfo, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SectioningInfo sectioningInfo) {
		delete((Object) sectioningInfo);
	}

	public void delete(SectioningInfo sectioningInfo, org.hibernate.Session hibSession) {
		delete((Object) sectioningInfo, hibSession);
	}

	public void refresh(SectioningInfo sectioningInfo, org.hibernate.Session hibSession) {
		refresh((Object) sectioningInfo, hibSession);
	}

	public List<SectioningInfo> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SectioningInfo").list();
	}

	public List<SectioningInfo> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from SectioningInfo x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
