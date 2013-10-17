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
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon, Tomas Muller
 *
 */
public class AcademicClassificationImport extends BaseImport {


    public void loadXml(Element root) throws Exception{
        if (!root.getName().equalsIgnoreCase("academicClassifications")) {
            throw new Exception("Given XML file is not academic classifications load file.");
        }
        try {
            beginTransaction();
            
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (session == null)
                throw new Exception("No session found for the given campus, year, and term.");
            
            Map<String, AcademicClassification> id2clasf = new Hashtable<String, AcademicClassification>();
            Map<String, AcademicClassification> code2clasf = new Hashtable<String, AcademicClassification>();
            for (AcademicClassification clasf: (List<AcademicClassification>)getHibSession().createQuery(
            		"from AcademicClassification where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (clasf.getExternalUniqueId() != null)
            		id2clasf.put(clasf.getExternalUniqueId(), clasf);
            	code2clasf.put(clasf.getCode(), clasf);
            }
            
            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                
                String externalId = element.attributeValue("externalId");
                String code = element.attributeValue("code");
                
                AcademicClassification clasf = null;
                if (externalId != null)
                	clasf = id2clasf.remove(externalId);
                if (clasf == null)
                	clasf = code2clasf.get(code);
                
                if (clasf == null) {
                	clasf = new AcademicClassification();
                	clasf.setSession(session);
                	info("Academic classification " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
                } else {
                	info("Academic classification " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
                }
                
                clasf.setExternalUniqueId(externalId);
                clasf.setCode(code);
                clasf.setName(element.attributeValue("name"));
                
                getHibSession().saveOrUpdate(clasf);
            }
            
            for (AcademicClassification clasf: id2clasf.values()) {
            	info("Academic classification " + clasf.getCode() + " (" + clasf.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(clasf);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
