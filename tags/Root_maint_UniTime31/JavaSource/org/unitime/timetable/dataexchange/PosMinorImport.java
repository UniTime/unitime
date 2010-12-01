/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon
 *
 */
public class PosMinorImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("posMinors")) {
        	throw new Exception("Given XML file is not a PosMinor load file.");
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
                PosMinor posMinor = null;
                if(externalId != null && externalId.length() > 0) {
                    posMinor = findByExternalId(externalId, session.getSessionId());
                }
                if(posMinor == null) {
                    posMinor = new PosMinor();
                    posMinor.setSession(session);
                    posMinor.setAcademicAreas(new HashSet());
                }
                else {
                    if("T".equalsIgnoreCase(element.attributeValue("delete"))) {
                        getHibSession().delete(posMinor);
                        continue;
                    }
                }
                posMinor.setName(element.attributeValue("name"));
                posMinor.setCode(element.attributeValue("code"));
                posMinor.setExternalUniqueId(externalId);

                AcademicArea acadArea = AcademicArea.findByAbbv(session.getSessionId(), element.attributeValue("academicArea"));
                if(acadArea == null) {
                    error("Unable to find academic area "+element.attributeValue("academicArea"));
                    continue;
                }
                boolean found = false;
                for (Iterator iter = posMinor.getAcademicAreas().iterator(); iter.hasNext();) {
                    AcademicArea area = (AcademicArea) iter.next();
                    if(area.getAcademicAreaAbbreviation().equals(element.attributeValue("academicArea"))) {
                        found = true;
                    }
                }
                if(!found) {
                    posMinor.getAcademicAreas().add(acadArea);
                    acadArea.getPosMinors().add(posMinor);
                }
                getHibSession().saveOrUpdate(posMinor);
                
                flushIfNeeded(false);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

	private PosMinor findByExternalId(String externalId, Long sessionId) {
		return (PosMinor) getHibSession().
			createQuery("select distinct a from PosMinor as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
	}
}
