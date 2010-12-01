/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.dao;

import java.util.List;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.base.BaseSubjectAreaDAO;



public class SubjectAreaDAO extends BaseSubjectAreaDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public SubjectAreaDAO () {}

	public Order getDefaultOrder() {
		return Order.asc(SubjectArea.PROP_SUBJECT_AREA_ABBREVIATION);
	}
	
	public SubjectArea getSubjectAreaForSession(String subjectAreaAbbreviation, Session session){
	     StringBuffer sb = new StringBuffer();
	     sb.append("select sa.* from SubjectArea as sa ");
	     sb.append(" where sa.subjectAreaAbbreviation = '" + subjectAreaAbbreviation + "' ");
	     sb.append(" and sa.session.getUniqueId = " + session.getUniqueId());
	     
	     List results = getQuery(sb.toString()).list();
	     if (results.size() != 1){
	    	 return(null);
	     } else {
	    	 return((SubjectArea) results.get(0));
	     }
	}
	
}
