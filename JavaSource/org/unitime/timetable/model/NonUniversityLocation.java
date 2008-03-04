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
package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseNonUniversityLocation;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;




public class NonUniversityLocation extends BaseNonUniversityLocation {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public NonUniversityLocation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public NonUniversityLocation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public NonUniversityLocation (
		java.lang.Long uniqueId,
		java.lang.Long permanent_id,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		super (
			uniqueId,
			permanent_id,
			capacity,
			coordinateX,
			coordinateY,
			ignoreTooFar,
			ignoreRoomCheck);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String getLabel(){
		return(this.getName());
	}
    
    public String toString() {
        return getName();
    }
    
	public Object clone() {
		NonUniversityLocation l = new NonUniversityLocation();
		l.setCapacity(getCapacity());
		l.setCoordinateX(getCoordinateX());
		l.setCoordinateY(getCoordinateY());
		l.setDisplayName(getDisplayName());
		l.setName(getName());
		l.setIgnoreRoomCheck(isIgnoreRoomCheck());
		l.setIgnoreTooFar(isIgnoreTooFar());
		l.setPattern(getPattern());
		l.setSession(getSession());
		return l;
	}
	
	public NonUniversityLocation findSameNonUniversityLocationInSession(Session newSession) throws Exception{
		if (newSession == null) {
			return(null);
		}
		NonUniversityLocation newNonUniversityLocation = null;
		NonUniversityLocationDAO nulDao = new NonUniversityLocationDAO();
		
		String query = "from NonUniversityLocation nul inner join RoomDept rd where nul.name = '" + getName() + "'";
		query += " and nul.session.uniqueId = " + newSession.getUniqueId().toString();
		query += " and rd.control = " + 1;
		query += " and rd.department.uniqueId =" + getControllingDepartment().getUniqueId();
		List l = nulDao.getQuery(query).list();
		if (l != null && l.size() == 1) {
			newNonUniversityLocation = (NonUniversityLocation) l.get(0);
		} 
		return(newNonUniversityLocation);
	}

}