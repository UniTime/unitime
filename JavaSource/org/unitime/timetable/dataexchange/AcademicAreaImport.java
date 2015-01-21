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
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon, Tomas Muller
 *
 */
public class AcademicAreaImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("academicAreas")) {
        	throw new Exception("Given XML file is not acedemic areas load file.");
        }
        try {
            beginTransaction();
            
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (session == null)
                throw new Exception("No session found for the given campus, year, and term.");
            
            Map<String, AcademicArea> id2area = new Hashtable<String, AcademicArea>();
            Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
            for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
            		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (area.getExternalUniqueId() != null)
            		id2area.put(area.getExternalUniqueId(), area);
            	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
            }

            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                
                String externalId = element.attributeValue("externalId");
                String abbv = element.attributeValue("abbreviation");
                
                AcademicArea area = null;
                if (externalId != null)
                	area = id2area.remove(externalId);
                if (area == null)
                	area = abbv2area.get(abbv);
                
                if (area == null) {
                	area = new AcademicArea();
                	area.setSession(session);
                	info("Academic area " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " created.");
                } else {
                	info("Academic area " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
                }
                area.setExternalUniqueId(externalId);
                area.setAcademicAreaAbbreviation(abbv);
                area.setTitle(element.attributeValue("title", element.attributeValue("longTitle")));

                getHibSession().saveOrUpdate(area);
            }
            
            for (AcademicArea area: id2area.values()) {
            	info("Academic area " + area.getAcademicAreaAbbreviation() + " (" + area.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(area);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
