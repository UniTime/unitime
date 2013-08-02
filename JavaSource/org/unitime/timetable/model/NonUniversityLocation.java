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
package org.unitime.timetable.model;

import java.util.HashSet;

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
		l.setArea(getArea());
		l.setDisplayName(getDisplayName());
		l.setName(getName());
		l.setIgnoreRoomCheck(isIgnoreRoomCheck());
		l.setIgnoreTooFar(isIgnoreTooFar());
		l.setPattern(getPattern());
		l.setSession(getSession());
		l.setPermanentId(getPermanentId());
		l.setExamCapacity(getExamCapacity());
		l.setExamTypes(new HashSet<ExamType>(getExamTypes()));
		l.setRoomType(getRoomType());
		l.setEventStatus(null);
		l.setBreakTime(getBreakTime());
		l.setNote(getNote());
		l.setEventAvailability(getEventAvailability());
		l.setExternalUniqueId(getExternalUniqueId());
		l.setShareNote(getShareNote());
		return l;
	}
	
	public NonUniversityLocation findSameNonUniversityLocationInSession(Session newSession) throws Exception{
		if (newSession == null) {
			return(null);
		}
		NonUniversityLocation newNonUniversityLocation = null;
		NonUniversityLocationDAO nulDao = new NonUniversityLocationDAO();
		
		String query = "from NonUniversityLocation nul inner join RoomDept rd where nul.permanentId = '" + getPermanentId() + "'";
		query += " and nul.session.uniqueId = " + newSession.getUniqueId().toString();
		query += " and rd.control = " + 1;
		query += " and rd.department.uniqueId =" + getControllingDepartment().getUniqueId();
		newNonUniversityLocation = (NonUniversityLocation)nulDao.getQuery(query).uniqueResult();
		 
		return(newNonUniversityLocation);
	}
	
    public String getRoomTypeLabel() {
        return getRoomType().getLabel();
    }

}
