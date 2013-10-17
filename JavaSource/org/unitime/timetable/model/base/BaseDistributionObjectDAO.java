/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DistributionObjectDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseDistributionObjectDAO extends _RootDAO<DistributionObject,Long> {

	private static DistributionObjectDAO sInstance;

	public static DistributionObjectDAO getInstance() {
		if (sInstance == null) sInstance = new DistributionObjectDAO();
		return sInstance;
	}

	public Class<DistributionObject> getReferenceClass() {
		return DistributionObject.class;
	}

	@SuppressWarnings("unchecked")
	public List<DistributionObject> findByDistributionPref(org.hibernate.Session hibSession, Long distributionPrefId) {
		return hibSession.createQuery("from DistributionObject x where x.distributionPref.uniqueId = :distributionPrefId").setLong("distributionPrefId", distributionPrefId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DistributionObject> findByPrefGroup(org.hibernate.Session hibSession, Long prefGroupId) {
		return hibSession.createQuery("from DistributionObject x where x.prefGroup.uniqueId = :prefGroupId").setLong("prefGroupId", prefGroupId).list();
	}
}
