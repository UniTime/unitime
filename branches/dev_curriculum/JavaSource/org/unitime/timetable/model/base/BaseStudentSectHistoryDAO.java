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

import org.unitime.timetable.model.StudentSectHistory;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentSectHistoryDAO;

public abstract class BaseStudentSectHistoryDAO extends _RootDAO {

	private static StudentSectHistoryDAO sInstance;

	public static StudentSectHistoryDAO getInstance () {
		if (sInstance == null) sInstance = new StudentSectHistoryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentSectHistory.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentSectHistory get(Long uniqueId) {
		return (StudentSectHistory) get(getReferenceClass(), uniqueId);
	}

	public StudentSectHistory get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentSectHistory) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentSectHistory load(Long uniqueId) {
		return (StudentSectHistory) load(getReferenceClass(), uniqueId);
	}

	public StudentSectHistory load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentSectHistory) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentSectHistory loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentSectHistory studentSectHistory = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentSectHistory)) Hibernate.initialize(studentSectHistory);
		return studentSectHistory;
	}

	public void save(StudentSectHistory studentSectHistory) {
		save((Object) studentSectHistory);
	}

	public void save(StudentSectHistory studentSectHistory, org.hibernate.Session hibSession) {
		save((Object) studentSectHistory, hibSession);
	}

	public void saveOrUpdate(StudentSectHistory studentSectHistory) {
		saveOrUpdate((Object) studentSectHistory);
	}

	public void saveOrUpdate(StudentSectHistory studentSectHistory, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentSectHistory, hibSession);
	}


	public void update(StudentSectHistory studentSectHistory) {
		update((Object) studentSectHistory);
	}

	public void update(StudentSectHistory studentSectHistory, org.hibernate.Session hibSession) {
		update((Object) studentSectHistory, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentSectHistory studentSectHistory) {
		delete((Object) studentSectHistory);
	}

	public void delete(StudentSectHistory studentSectHistory, org.hibernate.Session hibSession) {
		delete((Object) studentSectHistory, hibSession);
	}

	public void refresh(StudentSectHistory studentSectHistory, org.hibernate.Session hibSession) {
		refresh((Object) studentSectHistory, hibSession);
	}

	public List<StudentSectHistory> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentSectHistory").list();
	}

	public List<StudentSectHistory> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from StudentSectHistory x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}
}
