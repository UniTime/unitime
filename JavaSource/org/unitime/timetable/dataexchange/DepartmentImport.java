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

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;

/**
 * 
 * @author Timothy Almon, Tomas Muller, Stephanie Schluttenhofer
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
                    department.setAllowStudentScheduling(true);
                    department.setInheritInstructorPreferences(true);
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
                department.setInheritInstructorPreferences("true".equals(element.attributeValue("instructorPrefs", department.isInheritInstructorPreferences() ? "true" : "false")));
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
