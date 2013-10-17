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

import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseSchedulingSubpartDAO extends _RootDAO<SchedulingSubpart,Long> {

	private static SchedulingSubpartDAO sInstance;

	public static SchedulingSubpartDAO getInstance() {
		if (sInstance == null) sInstance = new SchedulingSubpartDAO();
		return sInstance;
	}

	public Class<SchedulingSubpart> getReferenceClass() {
		return SchedulingSubpart.class;
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByItype(org.hibernate.Session hibSession, Integer itypeId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.itype.itype = :itypeId").setInteger("itypeId", itypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByParentSubpart(org.hibernate.Session hibSession, Long parentSubpartId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.parentSubpart.uniqueId = :parentSubpartId").setLong("parentSubpartId", parentSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByInstrOfferingConfig(org.hibernate.Session hibSession, Long instrOfferingConfigId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.instrOfferingConfig.uniqueId = :instrOfferingConfigId").setLong("instrOfferingConfigId", instrOfferingConfigId).list();
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}
}
