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
