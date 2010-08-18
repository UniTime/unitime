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

import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StaffDAO;

public abstract class BaseStaffDAO extends _RootDAO<Staff,Long> {

	private static StaffDAO sInstance;

	public static StaffDAO getInstance() {
		if (sInstance == null) sInstance = new StaffDAO();
		return sInstance;
	}

	public Class<Staff> getReferenceClass() {
		return Staff.class;
	}

	@SuppressWarnings("unchecked")
	public List<Staff> findByPositionCode(org.hibernate.Session hibSession, String positionCodeId) {
		return hibSession.createQuery("from Staff x where x.positionCode.positionCode = :positionCodeId").setString("positionCodeId", positionCodeId).list();
	}
}
