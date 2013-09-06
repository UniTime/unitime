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

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;

/**
 * 
 * @author Timothy Almon
 *
 */
public class DepartmentImport extends BaseImport {
    
    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("departments")) {
        	throw new Exception("Given XML file is not an Department load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            String created = root.attributeValue("created");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if(session == null) {
                throw new Exception("No session found for the given campus, year, and term.");
            }
            if (created != null) {
                ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_DEPARTMENTS, ChangeLog.Operation.UPDATE, null, null);
            }
           
            for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                String externalId = element.attributeValue("externalId");
                Department department = null;
                if(externalId != null && externalId.length() > 0) {
                    department = findByExternalId(externalId, session.getSessionId());
                    if (department == null){
                    	department = Department.findByDeptCode(element.attributeValue("deptCode"), session.getSessionId());
                    	if (department != null){
                    		warn("Department:  " + element.attributeValue("deptCode") + " not loaded because a manually created department with the same department code already exists.");
                    		continue;
                    	}
                    }
                }
                else {
                    department = Department.findByDeptCode(element.attributeValue("deptCode"), session.getSessionId());
                }
                if(department == null) {
                    department = new Department();
                    department.setSession(session);
                    department.setAllowReqTime(false);
                    department.setAllowReqRoom(false);
                    department.setAllowReqDistribution(false);
                    department.setExternalManager(false);
                    department.setAllowEvents(false);
                    department.setDistributionPrefPriority(new Integer(0));
                } else {
                    if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
                        getHibSession().delete(department);
                        continue;
                    }
                }
                department.setAbbreviation(element.attributeValue("abbreviation"));
                department.setName(element.attributeValue("name"));
                department.setDeptCode(element.attributeValue("deptCode"));
                department.setExternalUniqueId(externalId);
                department.setAllowEvents("true".equals(element.attributeValue("allowEvents", department.isAllowEvents() ? "true" : "false")));
                getHibSession().saveOrUpdate(department);
                flushIfNeeded(false);
            }
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

	private Department findByExternalId(String externalId, Long sessionId) {
		return (Department) this.
			getHibSession().
			createQuery("select distinct a from Department as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}
}
