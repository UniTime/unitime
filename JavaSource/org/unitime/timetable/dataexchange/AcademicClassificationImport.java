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
