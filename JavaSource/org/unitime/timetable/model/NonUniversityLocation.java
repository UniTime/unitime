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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseNonUniversityLocation;
import org.unitime.timetable.model.dao.LocationDAO;
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

		newNonUniversityLocation = (NonUniversityLocation) nulDao.getSession().createCriteria(NonUniversityLocation.class)
				.add(Restrictions.eq("permanentId", getPermanentId()))
				.add(Restrictions.eq("session.uniqueId", newSession.getUniqueId()))
				.setCacheable(true).uniqueResult();
		if (newNonUniversityLocation == null && getExternalUniqueId() != null) {
			newNonUniversityLocation = (NonUniversityLocation) nulDao.getSession().createCriteria(NonUniversityLocation.class)
					.add(Restrictions.eq("externalUniqueId", getExternalUniqueId()))
					.add(Restrictions.eq("session.uniqueId", newSession.getUniqueId()))
					.setCacheable(true).uniqueResult();
		}

		return(newNonUniversityLocation);
	}
	
    public String getRoomTypeLabel() {
        return getRoomType().getLabel();
    }

	@Override
    public List<Location> getFutureLocations() {
    	List<Location> ret = new ArrayList<Location>();
    	Set<Long> futureSessionIds = new HashSet<Long>();
    	Set<Long> blackListedSessionIds = new HashSet<Long>();

    	for (Location location: (List<Location>)LocationDAO.getInstance().getSession().createQuery(
    			"select f from NonUniversityLocation l, NonUniversityLocation f where " +
    			"l.uniqueId = :uniqueId and " +
    			"l.session.academicInitiative = f.session.academicInitiative and l.session.sessionBeginDateTime < f.session.sessionBeginDateTime and " +
    			"((l.permanentId = f.permanentId) or " + // match on permanent ids
    			"(not exists (from Location x where x.permanentId = f.permanentId and x.session = l.session) and " + // no match on permanent id exist
    			"l.roomType = f.roomType and " + // room type match
    			"((length(f.externalUniqueId) > 0 and l.externalUniqueId = f.externalUniqueId) or " + // external id match
    			"((f.externalUniqueId is null or length(f.externalUniqueId) = 0) and (l.externalUniqueId is null or length(l.externalUniqueId) = 0) and " + // no external id match
    			"f.name = l.name and f.capacity = l.capacity)))) " + // name & capacity match
    			"order by f.session.sessionBeginDateTime"
    			).setLong("uniqueId", getUniqueId()).setCacheable(true).list()) {
    		if (futureSessionIds.add(location.getSession().getUniqueId()))
    			ret.add(location);
    		else
    			blackListedSessionIds.add(location.getSession().getUniqueId());
    	}

    	if (!blackListedSessionIds.isEmpty())
    		for (Iterator<Location> i = ret.iterator(); i.hasNext(); ) { 
    			Location location = i.next();
    			if (blackListedSessionIds.contains(location.getSession().getUniqueId()))
    				i.remove();
    		}

    	return ret;
    }
}
