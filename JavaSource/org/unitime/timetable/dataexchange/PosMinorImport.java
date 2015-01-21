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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon, Tomas Muller
 *
 */
public class PosMinorImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("posMinors")) {
        	throw new Exception("Given XML file is not pos minors load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if(session == null)
                throw new Exception("No session found for the given campus, year, and term.");
            
            Map<String, PosMinor> id2minor = new Hashtable<String, PosMinor>();
            Map<String, PosMinor> code2minor = new Hashtable<String, PosMinor>();
            for (PosMinor minor: (List<PosMinor>)getHibSession().createQuery(
            		"from PosMinor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (minor.getExternalUniqueId() != null)
            		id2minor.put(minor.getExternalUniqueId(), minor);
            	for (AcademicArea area: minor.getAcademicAreas())
            		code2minor.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
            }
            
            Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
            for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
            		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
            }
            
            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                
                String externalId = element.attributeValue("externalId");
                String code = element.attributeValue("code");
                AcademicArea area = abbv2area.get(element.attributeValue("academicArea"));
                
                if (area == null) {
                	warn("Unknown academic area " + element.attributeValue("academicArea"));
                	continue;
                }
                
                PosMinor minor = null;
                if (externalId != null)
                	minor = id2minor.remove(externalId);
                if (minor == null)
                	minor = code2minor.get(area.getAcademicAreaAbbreviation() + ":" + code);
                
                if (minor == null) {
                	minor = new PosMinor();
                	minor.setSession(session);
                	minor.setAcademicAreas(new HashSet<AcademicArea>());
                	info("Minor " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
                } else {
                	info("Minor " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
                }
                
                minor.setExternalUniqueId(externalId);
                minor.setCode(code);
                minor.setName(element.attributeValue("name"));
                
                minor.getAcademicAreas().clear();
                minor.getAcademicAreas().add(area);
                area.getPosMinors().add(minor);
                
                getHibSession().saveOrUpdate(minor);
            }
            
            for (PosMinor minor: id2minor.values()) {
            	String abbv = null;
            	for (AcademicArea area: minor.getAcademicAreas()) {
            		area.getPosMinors().remove(minor);
            		abbv = area.getAcademicAreaAbbreviation();
            	}
            	info("Minor " + abbv + " " + minor.getCode() + " (" + minor.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(minor);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
