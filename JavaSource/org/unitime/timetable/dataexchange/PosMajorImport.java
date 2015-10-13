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
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;

/**
 * 
 * @author Timothy Almon, Tomas Muller
 *
 */
public class PosMajorImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("posMajors")) {
        	throw new Exception("Given XML file is not pos majors load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (session == null)
                throw new Exception("No session found for the given campus, year, and term.");
            
            Map<String, PosMajor> id2major = new Hashtable<String, PosMajor>();
            Map<String, PosMajor> code2major = new Hashtable<String, PosMajor>();
            for (PosMajor major: (List<PosMajor>)getHibSession().createQuery(
            		"from PosMajor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (major.getExternalUniqueId() != null)
            		id2major.put(major.getExternalUniqueId(), major);
            	for (AcademicArea area: major.getAcademicAreas())
            		code2major.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);
            }
            
            Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
            for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
            		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
            }
            
            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                
                String externalId = element.attributeValue("externalId");
                String code = trim(element.attributeValue("code"), "code", 10);
                AcademicArea area = abbv2area.get(element.attributeValue("academicArea"));
                
                if (area == null) {
                	warn("Unknown academic area " + element.attributeValue("academicArea"));
                	continue;
                }
                
                PosMajor major = null;
                if (externalId != null)
                	major = id2major.remove(externalId);
                if (major == null)
                	major = code2major.get(area.getAcademicAreaAbbreviation() + ":" + code);
                
                if (major == null) {
                	major = new PosMajor();
                	major.setSession(session);
                	major.setAcademicAreas(new HashSet<AcademicArea>());
                	info("Major " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
                } else {
                	info("Major " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
                }
                
                major.setExternalUniqueId(externalId);
                major.setCode(code);
                major.setName(trim(element.attributeValue("name"), "name", 50));
                
                major.getAcademicAreas().clear();
                major.getAcademicAreas().add(area);
                area.getPosMajors().add(major);
                
                getHibSession().saveOrUpdate(major);
            }
            
            for (PosMajor major: id2major.values()) {
            	String abbv = null;
            	for (AcademicArea area: major.getAcademicAreas()) {
            		area.getPosMajors().remove(major);
            		abbv = area.getAcademicAreaAbbreviation();
            	}
            	info("Major " + abbv + " " + major.getCode() + " (" + major.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(major);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
