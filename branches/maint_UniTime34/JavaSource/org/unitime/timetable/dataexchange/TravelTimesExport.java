/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.dataexchange;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TravelTime;

public class TravelTimesExport extends BaseExport {

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("traveltimes");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        root.addAttribute("created", new Date().toString());
	        
	        document.addDocType("traveltimes", "-//UniTime//UniTime Travel Times DTD/EN", "http://www.unitime.org/interface/TravelTimes.dtd");
	        
	        Map<Long, Map<Long, Integer>> matrix = new HashMap<Long, Map<Long,Integer>>();
	        for (TravelTime travel: (List<TravelTime>)getHibSession().createQuery(
    			"from TravelTime where session.uniqueId = :sessionId")
    			.setLong("sessionId", session.getUniqueId()).list()) {
	        	Map<Long, Integer> m = matrix.get(travel.getLocation1Id());
	        	if (m == null) {
	        		m = new HashMap<Long, Integer>();
	        		matrix.put(travel.getLocation1Id(), m);
	        	}
	        	m.put(travel.getLocation2Id(), travel.getDistance());
	        }
	        
	        
	        List<Location> locations = Location.findAll(session.getUniqueId());
	        Collections.sort(locations);
	        
	        for (Location from: locations) {
	        	Map<Long, Integer> m = matrix.get(from.getUniqueId());
	        	if (m == null || m.isEmpty()) continue;
	        	Element fromEl = root.addElement("from");
	        	fillLocationData(from, fromEl);
	        	for (Location to: locations) {
	        		Integer distance = m.get(to.getUniqueId());
	        		if (distance == null) continue;
	        		Element toEl = fromEl.addElement("to");
	        		fillLocationData(to, toEl);
	        		toEl.setText(distance.toString());
	        	}
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}
	
	private void fillLocationData(Location location, Element element) {
		if (location instanceof Room) {
			Room room = (Room)location;
			element.addAttribute("building", room.getBuilding().getAbbreviation());
			element.addAttribute("roomNbr", room.getRoomNumber());
		} else {
			element.addAttribute("name", location.getLabel());
		}
		if (location.getExternalUniqueId() != null && !location.getExternalUniqueId().isEmpty())
			element.addAttribute("id", location.getExternalUniqueId());
	}

}
