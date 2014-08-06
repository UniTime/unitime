/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.List;

import org.hibernate.HibernateException;
import org.unitime.timetable.model.base.BaseAcademicAreaHistory;




/**
 * @author Tomas Muller
 */
public class AcademicAreaHistory extends BaseAcademicAreaHistory {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AcademicAreaHistory () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AcademicAreaHistory (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Retrieves all academic areas history data for the academic session
	 * @param sessionId academic session
	 * @return List of AcademicAreaHistory objects
	 */
	public static List getAcademicAreaHistoryList(Long sessionId) 
			throws HibernateException {
	    
		return getHistoryList(sessionId, AcademicAreaHistory.class);
	}
	

}
