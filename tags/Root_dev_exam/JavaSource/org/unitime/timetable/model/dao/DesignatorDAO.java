/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.model.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.base.BaseDesignatorDAO;



public class DesignatorDAO extends BaseDesignatorDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public DesignatorDAO () {}

	/**
	 * Checks that the same designator code is not assigned to another
	 * instructor in the subject area
	 * @param subjectAreaId
	 * @param code
	 * @param uniqueId If this parameter is not null, it excludes records with this uniqueid
	 * @return true if duplicate exists, false otherwise
	 */
	public static boolean find(Long subjectAreaId, String code, Long uniqueId) {
		
		SubjectAreaDAO sdao = new SubjectAreaDAO();
		SubjectArea sa = sdao.get(subjectAreaId);
		Long acadSessionId = sa.getSession().getUniqueId();
		
		String sql = " from Designator d " +
					   "where d.subjectArea.session.uniqueId=:acadSessionId " +
					   "	and d.code=:code " +
					   "	and d.subjectArea.uniqueId=:subjectAreaId";
		
		if (uniqueId!=null) {
			sql += " and d.uniqueId!=:uniqueId";
		}
		
		Session hibSession = sdao.getSession();
		Query query = hibSession.createQuery(sql);
		query.setLong("acadSessionId", acadSessionId.longValue());
		query.setString("code", code);
		query.setLong("subjectAreaId", subjectAreaId.longValue());

		if (uniqueId!=null) {
			query.setLong("uniqueId", uniqueId.longValue());
		}
		
		List l = query.list();		
		if (l!=null && l.size()>0)
			return true;
		
		return false;
	}
}