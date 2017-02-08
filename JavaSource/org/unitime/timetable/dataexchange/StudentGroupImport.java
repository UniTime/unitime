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
import org.unitime.timetable.model.StudentGroup;


/**
 * @author Tomas Muller
 */
public class StudentGroupImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("studentGroups")) {
        	throw new Exception("Given XML file is not student groups load file.");
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
            
            Map<String, StudentGroup> id2group = new Hashtable<String, StudentGroup>();
            Map<String, StudentGroup> code2group = new Hashtable<String, StudentGroup>();
            for (StudentGroup group: (List<StudentGroup>)getHibSession().createQuery(
            		"from StudentGroup where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (group.getExternalUniqueId() != null)
            		id2group.put(group.getExternalUniqueId(), group);
            	code2group.put(group.getGroupAbbreviation(), group);
            }
            
            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element)it.next();
                
                String externalId = element.attributeValue("externalId");
                String code = element.attributeValue("code");
                String name = element.attributeValue("name");
                String size = element.attributeValue("size");
                
                StudentGroup group = null;
                if (externalId != null)
                	group = id2group.remove(externalId);
                if (group == null)
                	group = code2group.get(code);
                
                if (group == null) {
                	group = new StudentGroup();
                	group.setSession(session);
                	info("Group " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
                } else {
                	info("Group " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
                }
                
                group.setExternalUniqueId(externalId);
                group.setGroupAbbreviation(code);
                group.setGroupName(name);
                try {
        			group.setExpectedSize(size == null || size.isEmpty() ? null : Integer.valueOf(size));
        		} catch (NumberFormatException e) {
        			group.setExpectedSize(null);
        		}
                
                getHibSession().saveOrUpdate(group);
            }
            
            for (StudentGroup group: id2group.values()) {
            	info("Group " + group.getGroupAbbreviation() + " (" + group.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(group);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
