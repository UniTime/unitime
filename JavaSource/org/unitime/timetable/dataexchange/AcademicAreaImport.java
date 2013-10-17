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
