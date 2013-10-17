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
