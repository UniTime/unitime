/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolutionDAO;

public abstract class BaseSolutionDAO extends _RootDAO<Solution,Long> {

	private static SolutionDAO sInstance;

	public static SolutionDAO getInstance() {
		if (sInstance == null) sInstance = new SolutionDAO();
		return sInstance;
	}

	public Class<Solution> getReferenceClass() {
		return Solution.class;
	}

	@SuppressWarnings("unchecked")
	public List<Solution> findByOwner(org.hibernate.Session hibSession, Long ownerId) {
		return hibSession.createQuery("from Solution x where x.owner.uniqueId = :ownerId").setLong("ownerId", ownerId).list();
	}
}
