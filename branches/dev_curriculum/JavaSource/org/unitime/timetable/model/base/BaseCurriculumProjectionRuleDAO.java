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

import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumProjectionRuleDAO;

public abstract class BaseCurriculumProjectionRuleDAO extends _RootDAO {

	private static CurriculumProjectionRuleDAO sInstance;

	public static CurriculumProjectionRuleDAO getInstance () {
		if (sInstance == null) sInstance = new CurriculumProjectionRuleDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CurriculumProjectionRule.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CurriculumProjectionRule get(Long uniqueId) {
		return (CurriculumProjectionRule) get(getReferenceClass(), uniqueId);
	}

	public CurriculumProjectionRule get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumProjectionRule) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumProjectionRule load(Long uniqueId) {
		return (CurriculumProjectionRule) load(getReferenceClass(), uniqueId);
	}

	public CurriculumProjectionRule load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumProjectionRule) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumProjectionRule loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CurriculumProjectionRule curriculumProjectionRule = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(curriculumProjectionRule)) Hibernate.initialize(curriculumProjectionRule);
		return curriculumProjectionRule;
	}

	public void save(CurriculumProjectionRule curriculumProjectionRule) {
		save((Object) curriculumProjectionRule);
	}

	public void save(CurriculumProjectionRule curriculumProjectionRule, org.hibernate.Session hibSession) {
		save((Object) curriculumProjectionRule, hibSession);
	}

	public void saveOrUpdate(CurriculumProjectionRule curriculumProjectionRule) {
		saveOrUpdate((Object) curriculumProjectionRule);
	}

	public void saveOrUpdate(CurriculumProjectionRule curriculumProjectionRule, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) curriculumProjectionRule, hibSession);
	}


	public void update(CurriculumProjectionRule curriculumProjectionRule) {
		update((Object) curriculumProjectionRule);
	}

	public void update(CurriculumProjectionRule curriculumProjectionRule, org.hibernate.Session hibSession) {
		update((Object) curriculumProjectionRule, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CurriculumProjectionRule curriculumProjectionRule) {
		delete((Object) curriculumProjectionRule);
	}

	public void delete(CurriculumProjectionRule curriculumProjectionRule, org.hibernate.Session hibSession) {
		delete((Object) curriculumProjectionRule, hibSession);
	}

	public void refresh(CurriculumProjectionRule curriculumProjectionRule, org.hibernate.Session hibSession) {
		refresh((Object) curriculumProjectionRule, hibSession);
	}

	public List<CurriculumProjectionRule> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CurriculumProjectionRule").list();
	}

	public List<CurriculumProjectionRule> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from CurriculumProjectionRule x where x.academicArea.uniqueId = :academicAreaId").setLong("academicAreaId", academicAreaId).list();
	}

	public List<CurriculumProjectionRule> findByMajor(org.hibernate.Session hibSession, Long majorId) {
		return hibSession.createQuery("from CurriculumProjectionRule x where x.major.uniqueId = :majorId").setLong("majorId", majorId).list();
	}

	public List<CurriculumProjectionRule> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from CurriculumProjectionRule x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}
}
