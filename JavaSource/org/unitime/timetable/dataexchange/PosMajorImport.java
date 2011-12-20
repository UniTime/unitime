/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
 * @author Timothy Almon
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
                String code = element.attributeValue("code");
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
                major.setName(element.attributeValue("name"));
                
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
