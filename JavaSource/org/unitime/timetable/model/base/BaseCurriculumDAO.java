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

import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;

public abstract class BaseCurriculumDAO extends _RootDAO {

	private static CurriculumDAO sInstance;

	public static CurriculumDAO getInstance () {
		if (sInstance == null) sInstance = new CurriculumDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Curriculum.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Curriculum get(Long uniqueId) {
		return (Curriculum) get(getReferenceClass(), uniqueId);
	}

	public Curriculum get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Curriculum) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Curriculum load(Long uniqueId) {
		return (Curriculum) load(getReferenceClass(), uniqueId);
	}

	public Curriculum load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Curriculum) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Curriculum loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Curriculum curriculum = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(curriculum)) Hibernate.initialize(curriculum);
		return curriculum;
	}

	public void save(Curriculum curriculum) {
		save((Object) curriculum);
	}

	public void save(Curriculum curriculum, org.hibernate.Session hibSession) {
		save((Object) curriculum, hibSession);
	}

	public void saveOrUpdate(Curriculum curriculum) {
		saveOrUpdate((Object) curriculum);
	}

	public void saveOrUpdate(Curriculum curriculum, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) curriculum, hibSession);
	}


	public void update(Curriculum curriculum) {
		update((Object) curriculum);
	}

	public void update(Curriculum curriculum, org.hibernate.Session hibSession) {
		update((Object) curriculum, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Curriculum curriculum) {
		delete((Object) curriculum);
	}

	public void delete(Curriculum curriculum, org.hibernate.Session hibSession) {
		delete((Object) curriculum, hibSession);
	}

	public void refresh(Curriculum curriculum, org.hibernate.Session hibSession) {
		refresh((Object) curriculum, hibSession);
	}

	public List<Curriculum> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Curriculum").list();
	}

	public List<Curriculum> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from Curriculum x where x.academicArea.uniqueId = :academicAreaId").setLong("academicAreaId", academicAreaId).list();
	}

	public List<Curriculum> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from Curriculum x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
