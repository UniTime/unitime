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

import org.unitime.timetable.model.JointEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.JointEnrollmentDAO;

public abstract class BaseJointEnrollmentDAO extends _RootDAO {

	private static JointEnrollmentDAO sInstance;

	public static JointEnrollmentDAO getInstance () {
		if (sInstance == null) sInstance = new JointEnrollmentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return JointEnrollment.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public JointEnrollment get(Long uniqueId) {
		return (JointEnrollment) get(getReferenceClass(), uniqueId);
	}

	public JointEnrollment get(Long uniqueId, org.hibernate.Session hibSession) {
		return (JointEnrollment) get(getReferenceClass(), uniqueId, hibSession);
	}

	public JointEnrollment load(Long uniqueId) {
		return (JointEnrollment) load(getReferenceClass(), uniqueId);
	}

	public JointEnrollment load(Long uniqueId, org.hibernate.Session hibSession) {
		return (JointEnrollment) load(getReferenceClass(), uniqueId, hibSession);
	}

	public JointEnrollment loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		JointEnrollment jointEnrollment = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(jointEnrollment)) Hibernate.initialize(jointEnrollment);
		return jointEnrollment;
	}

	public void save(JointEnrollment jointEnrollment) {
		save((Object) jointEnrollment);
	}

	public void save(JointEnrollment jointEnrollment, org.hibernate.Session hibSession) {
		save((Object) jointEnrollment, hibSession);
	}

	public void saveOrUpdate(JointEnrollment jointEnrollment) {
		saveOrUpdate((Object) jointEnrollment);
	}

	public void saveOrUpdate(JointEnrollment jointEnrollment, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) jointEnrollment, hibSession);
	}


	public void update(JointEnrollment jointEnrollment) {
		update((Object) jointEnrollment);
	}

	public void update(JointEnrollment jointEnrollment, org.hibernate.Session hibSession) {
		update((Object) jointEnrollment, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(JointEnrollment jointEnrollment) {
		delete((Object) jointEnrollment);
	}

	public void delete(JointEnrollment jointEnrollment, org.hibernate.Session hibSession) {
		delete((Object) jointEnrollment, hibSession);
	}

	public void refresh(JointEnrollment jointEnrollment, org.hibernate.Session hibSession) {
		refresh((Object) jointEnrollment, hibSession);
	}

	public List<JointEnrollment> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from JointEnrollment").list();
	}

	public List<JointEnrollment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from JointEnrollment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	public List<JointEnrollment> findByClass1(org.hibernate.Session hibSession, Long class1Id) {
		return hibSession.createQuery("from JointEnrollment x where x.class1.uniqueId = :class1Id").setLong("class1Id", class1Id).list();
	}

	public List<JointEnrollment> findByClass2(org.hibernate.Session hibSession, Long class2Id) {
		return hibSession.createQuery("from JointEnrollment x where x.class2.uniqueId = :class2Id").setLong("class2Id", class2Id).list();
	}
}
