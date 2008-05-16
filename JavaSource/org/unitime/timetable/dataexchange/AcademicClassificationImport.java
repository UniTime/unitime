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
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon
 *
 */
public class AcademicClassificationImport extends BaseImport {


    public void loadXml(Element root) throws Exception{
        if (!root.getName().equalsIgnoreCase("academicClassifications")) {
            throw new Exception("Given XML file is not a AcademicClassification load file.");
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
            
            for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                String externalId = element.attributeValue("externalId");
                AcademicClassification academicClassification = null;
                if(externalId != null && externalId.length() > 0) {
                    academicClassification = findByExternalId(externalId, session.getSessionId());
                }
                if(academicClassification == null) {
                    academicClassification = new AcademicClassification();
                    academicClassification.setSession(session);
                }
                else if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
                    getHibSession().delete(academicClassification);
                    continue;
                }
                academicClassification.setCode(element.attributeValue("code"));
                academicClassification.setExternalUniqueId(element.attributeValue("externalId"));
                academicClassification.setName(element.attributeValue("name"));

                getHibSession().saveOrUpdate(academicClassification);
                flushIfNeeded(false);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

	private AcademicClassification findByExternalId(String externalId, Long sessionId) {
		return (AcademicClassification) getHibSession().
			createQuery("select distinct a from AcademicClassification as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}
}