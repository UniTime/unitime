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

import org.unitime.timetable.model.SolutionInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolutionInfoDAO;

public abstract class BaseSolutionInfoDAO extends _RootDAO {

	private static SolutionInfoDAO sInstance;

	public static SolutionInfoDAO getInstance () {
		if (sInstance == null) sInstance = new SolutionInfoDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolutionInfo.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolutionInfo get(Long uniqueId) {
		return (SolutionInfo) get(getReferenceClass(), uniqueId);
	}

	public SolutionInfo get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolutionInfo) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolutionInfo load(Long uniqueId) {
		return (SolutionInfo) load(getReferenceClass(), uniqueId);
	}

	public SolutionInfo load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolutionInfo) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolutionInfo loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolutionInfo solutionInfo = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solutionInfo)) Hibernate.initialize(solutionInfo);
		return solutionInfo;
	}

	public void save(SolutionInfo solutionInfo) {
		save((Object) solutionInfo);
	}

	public void save(SolutionInfo solutionInfo, org.hibernate.Session hibSession) {
		save((Object) solutionInfo, hibSession);
	}

	public void saveOrUpdate(SolutionInfo solutionInfo) {
		saveOrUpdate((Object) solutionInfo);
	}

	public void saveOrUpdate(SolutionInfo solutionInfo, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solutionInfo, hibSession);
	}


	public void update(SolutionInfo solutionInfo) {
		update((Object) solutionInfo);
	}

	public void update(SolutionInfo solutionInfo, org.hibernate.Session hibSession) {
		update((Object) solutionInfo, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolutionInfo solutionInfo) {
		delete((Object) solutionInfo);
	}

	public void delete(SolutionInfo solutionInfo, org.hibernate.Session hibSession) {
		delete((Object) solutionInfo, hibSession);
	}

	public void refresh(SolutionInfo solutionInfo, org.hibernate.Session hibSession) {
		refresh((Object) solutionInfo, hibSession);
	}

	public List<SolutionInfo> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolutionInfo").list();
	}

	public List<SolutionInfo> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from SolutionInfo x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}
}
