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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseExternalBuilding;
import org.unitime.timetable.model.dao.ExternalBuildingDAO;




/**
 * @author Tomas Muller
 */
public class ExternalBuilding extends BaseExternalBuilding {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExternalBuilding () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExternalBuilding (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/


	/**
	 * Get External Buildings for a session
	 * @param sessionId
	 * @return Hashtable
	 */
	public static Hashtable getBuildings(Long sessionId) {
		
		List bldgs = (new ExternalBuildingDAO()).getQuery(
				"from ExternalBuilding as b " + 
				"where b.session.uniqueId = " + sessionId.longValue()).
				list();
		
		Hashtable buildings = new Hashtable((int)(bldgs.size() * 1.25));
		Iterator l = bldgs.iterator();
		
		while(l.hasNext()) {
			ExternalBuilding bldg = (ExternalBuilding)l.next();
			buildings.put(bldg.getExternalUniqueId(), bldg);
		}
		
		return buildings;
	}

	public static ExternalBuilding findExternalBuildingForSession(String externalUniqueId, Session session){
		ExternalBuildingDAO bDao = new ExternalBuildingDAO();
		List extBldgs = bDao.getSession().createCriteria(ExternalBuilding.class)
			.add(Restrictions.eq("externalUniqueId", externalUniqueId))
			.add(Restrictions.eq("session.uniqueId", session.getUniqueId()))
			.setCacheable(true).list();

		if (extBldgs.size() == 1){
			return((ExternalBuilding) extBldgs.get(0));
		}
		return(null);
	}



	/**
	 * Retrieve an external building for a given abbreviation and academic session
	 * @param sessionId
	 * @param bldgAbbr
	 * @return null if no match found
	 */
	public static ExternalBuilding findByAbbv (Long sessionId, String bldgAbbr) {

		ExternalBuildingDAO bldgDAO = new ExternalBuildingDAO();
	    List bldgs = bldgDAO.getSession().createCriteria(ExternalBuilding.class)
				    .add(Restrictions.eq("session.uniqueId", sessionId))
				    .add(Restrictions.eq("abbreviation", bldgAbbr))
				    .list();
	    
	    if (bldgs == null || bldgs.size()==0)
	    	return null;
	    
		return (ExternalBuilding)bldgs.get(0);
	}

	/**
	 * Retrieve an external room
	 * @param roomNbr
	 * @return null if no match found
	 */
	public ExternalRoom findRoom (String roomNbr) {

		ExternalRoom room = null;
		
		Iterator r = getRooms().iterator();
		while(r.hasNext()) {
			ExternalRoom rm = (ExternalRoom)r.next();
			if((roomNbr.trim()).equalsIgnoreCase(rm.getRoomNumber().trim())) {
				room = rm;
				break;
			}
		}
	    
		return room;
	}
}
