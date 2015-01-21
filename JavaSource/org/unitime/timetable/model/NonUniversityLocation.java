/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model;

import java.util.HashSet;

import org.unitime.timetable.model.base.BaseNonUniversityLocation;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
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
