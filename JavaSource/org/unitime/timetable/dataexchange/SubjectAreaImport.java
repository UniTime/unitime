/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
 * @author Timothy Almon
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
                subjectArea.setLongTitle(element.attributeValue("longTitle"));
                subjectArea.setShortTitle(element.attributeValue("shortTitle"));
                subjectArea.setExternalUniqueId(externalId);
                subjectArea.setScheduleBookOnly(new Boolean(element.attributeValue("schedBookOnly").equalsIgnoreCase("T")));
                subjectArea.setPseudoSubjectArea(new Boolean(element.attributeValue("pseudoSubjArea").equalsIgnoreCase("T")));

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