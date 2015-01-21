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
import org.unitime.timetable.model.SubjectArea;


/**
 * 
 * @author Timothy Almon, Tomas Muller
 *
 */
public class SubjectAreaImport  extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("subjectAreas")) {
        	throw new Exception("Given XML file is not a SubjectArea load file.");
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
                ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_SUBJECT_AREAS, ChangeLog.Operation.UPDATE, null, null);
            }
            
            for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                String externalId = element.attributeValue("externalId");
                SubjectArea subjectArea = null;
                if(externalId != null && externalId.length() > 0) {
                    subjectArea = findByExternalId(externalId, session.getSessionId());
                }
                if(subjectArea == null) {
                    subjectArea = new SubjectArea();
                    subjectArea.setSession(session);
                }
                else {
                    if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
                        getHibSession().delete(subjectArea);
                        continue;
                    }
                }
                subjectArea.setSubjectAreaAbbreviation(element.attributeValue("abbreviation"));
                subjectArea.setTitle(element.attributeValue("title", element.attributeValue("longTitle")));
                subjectArea.setExternalUniqueId(externalId);

                String deptCode = element.attributeValue("department");
                Department department = findByDeptCode(deptCode, session.getSessionId());
                if (department == null) {
                    throw new Exception("No department found for " + deptCode);
                }
                subjectArea.setDepartment(department);

                getHibSession().saveOrUpdate(subjectArea);
                flushIfNeeded(false);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

	private SubjectArea findByExternalId(String externalId, Long sessionId) {
		return (SubjectArea) getHibSession().
			createQuery("select distinct a from SubjectArea as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}

	private Department findByDeptCode(String deptCode, Long sessionId) {
		return (Department) getHibSession().
			createQuery("select distinct a from Department as a where a.deptCode=:deptCode and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("deptCode", deptCode).
			setCacheable(true).
			uniqueResult();
	}
}
