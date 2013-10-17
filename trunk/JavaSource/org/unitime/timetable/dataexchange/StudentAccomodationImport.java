/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentAccomodation;


/**
 * @author Tomas Muller
 */
public class StudentAccomodationImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("studentAccomodations")) {
        	throw new Exception("Given XML file is not student accomodations load file.");
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
            
            Map<String, StudentAccomodation> id2accomodation = new Hashtable<String, StudentAccomodation>();
            Map<String, StudentAccomodation> code2accomodation = new Hashtable<String, StudentAccomodation>();
            for (StudentAccomodation accomodation: (List<StudentAccomodation>)getHibSession().createQuery(
            		"from StudentAccomodation where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (accomodation.getExternalUniqueId() != null)
            		id2accomodation.put(accomodation.getExternalUniqueId(), accomodation);
            	code2accomodation.put(accomodation.getAbbreviation(), accomodation);
            }
            
            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element)it.next();
                
                String externalId = element.attributeValue("externalId");
                String abbv = element.attributeValue("abbreviation");
                String name = element.attributeValue("name");
                
                StudentAccomodation accomodation = null;
                if (externalId != null)
                	accomodation = id2accomodation.remove(externalId);
                if (accomodation == null)
                	accomodation = code2accomodation.get(abbv);
                
                if (accomodation == null) {
                	accomodation = new StudentAccomodation();
                	accomodation.setSession(session);
                	info("Accomodation " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " created.");
                } else {
                	info("Accomodation " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
                }
                
                accomodation.setExternalUniqueId(externalId);
                accomodation.setAbbreviation(abbv);
                accomodation.setName(name);
                
                getHibSession().saveOrUpdate(accomodation);
            }
            
            for (StudentAccomodation accomodation: id2accomodation.values()) {
            	info("Accomodation " + accomodation.getAbbreviation() + " (" + accomodation.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(accomodation);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
