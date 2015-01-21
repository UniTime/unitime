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
package org.unitime.timetable.dataexchange;

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TravelTime;

/**
 * @author Tomas Muller
 */
public class TravelTimesImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("traveltimes")) {
        	throw new Exception("Given XML file is not a Travel Times load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if(session == null) {
                throw new Exception("No session found for the given campus, year, and term.");
            }
            
        	info("Deleting existing travel times...");
        	getHibSession().createQuery(
    				"delete from TravelTime where session.uniqueId = :sessionId")
    				.setLong("sessionId", session.getUniqueId())
    				.executeUpdate();

        	info("Importing travel times...");
        	for (Iterator i = root.elementIterator("from"); i.hasNext(); ) {
                Element fromEl = (Element) i.next();
                Location from = findLocation(session.getUniqueId(), fromEl);
                if (from == null) continue;
                
                for (Iterator j = fromEl.elementIterator("to"); j.hasNext(); ) {
                	Element toEl = (Element) j.next();
                	Location to = findLocation(session.getUniqueId(), toEl);
                	if (to == null) continue;
                	
					TravelTime time = new TravelTime();
					time.setSession(session);
					time.setLocation1Id(Math.min(from.getUniqueId(), to.getUniqueId()));
					time.setLocation2Id(Math.max(from.getUniqueId(), to.getUniqueId()));
					time.setDistance(Integer.parseInt(toEl.getTextTrim()));
					
					getHibSession().saveOrUpdate(time);
                }
                
        	}

        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
    
	private Location findLocation(Long sessionId, Element element){
		if (element.attributeValue("id") != null) {
			Room room = (Room)getHibSession().createQuery(
					"select r from Room r where r.externalUniqueId=:externalId and r.building.session.uniqueId=:sessionId")
					.setLong("sessionId", sessionId)
					.setString("externalId", element.attributeValue("id"))
					.setCacheable(true)
					.setMaxResults(1)
					.uniqueResult();
			if (room != null) return room;
		} 
		if (element.attributeValue("building") != null && element.attributeValue("roomNbr") != null) {
			Room room = (Room)getHibSession().createQuery(
					"select  r from Room r where r.roomNumber=:roomNbr and r.building.abbreviation = :building and r.session.uniqueId=:sessionId")
					.setLong("sessionId", sessionId)
					.setString("building", element.attributeValue("building"))
					.setString("roomNbr", element.attributeValue("roomNbr"))
					.setCacheable(true)
					.setMaxResults(1)
					.uniqueResult();
			if (room != null) return room;
		}
		if (element.attributeValue("name") != null) {
			Room room = (Room)getHibSession().createQuery(
					"select  r from Room r where (r.building.abbreviation || ' ' || r.roomNumber) = :name and r.session.uniqueId=:sessionId")
					.setLong("sessionId", sessionId)
					.setString("name", element.attributeValue("name"))
					.setCacheable(true)
					.setMaxResults(1)
					.uniqueResult();
			if (room != null) return room;
			
			NonUniversityLocation location = (NonUniversityLocation)getHibSession().createQuery(
					"select  r from NonUniversityLocation r where r.name = :name and r.session.uniqueId=:sessionId")
					.setLong("sessionId", sessionId)
					.setString("name", element.attributeValue("name"))
					.setCacheable(true)
					.setMaxResults(1)
					.uniqueResult();
			if (location != null) return location;
		}
		warn("Location " + element.asXML() + " not found.");
		return null;
	}	

}
