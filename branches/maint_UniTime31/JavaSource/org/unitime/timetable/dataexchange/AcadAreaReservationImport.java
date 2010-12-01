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

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon
 *
 */
public class AcadAreaReservationImport extends BaseImport {

	
    public void loadXml(Element root) throws Exception{
        if (!root.getName().equalsIgnoreCase("academicAreaReservations")) {
            throw new Exception("Given XML file is not an Academic Area Reservation load file.");
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

            for ( Iterator iter = root.elementIterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                String subject = element.attributeValue("subject");
                String course  = element.attributeValue("courseNumber");
                for (Iterator it = element.elementIterator(); it.hasNext();) {
                    Element el = (Element) it.next();
                    String academicArea = el.attributeValue("academicArea");
                    if (academicArea == null) {
                        error("Academic area not provided for "+subject+" "+course+".");
                        continue;
                    }
                    String reservationType = el.attributeValue("reservationType");
                    if(reservationType == null) {
                        error("Reservation type not provided for "+subject+" "+course+".");
                        continue;
                    }
                    AcademicArea area = fetchAcademicArea(academicArea, session.getSessionId());
                    if (area == null) continue;
                    String priority = el.attributeValue("priority");
                    if (priority == null) priority = "1";
        
                    AcadAreaReservation reservation = new AcadAreaReservation();
                    
                    CourseOffering offer = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subject, course);
                    if(offer == null) continue;
                    reservation.setOwner(offer.getUniqueId());
        
                    reservation.setReservationType(fetchReservationType(reservationType));
                    reservation.setAcademicClassification(null);
                    reservation.setAcademicArea(area);
                    reservation.setPriority(Integer.decode(priority));
                    reservation.setReserved(Integer.decode(el.attributeValue("request")));
                    reservation.setPriorEnrollment(Integer.decode(el.attributeValue("priorEnrollment")));
                    reservation.setProjectedEnrollment(Integer.decode(el.attributeValue("projectedEnrollment")));
                    reservation.setOwnerClassId("U");
                    reservation.setRequested(reservation.getReserved());
                    getHibSession().saveOrUpdate(reservation);
                }
                flushIfNeeded(false);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

	AcademicArea fetchAcademicArea(String academicArea, Long sessionId) {
		return (AcademicArea) 
		getHibSession().
		createQuery("select distinct a from AcademicArea as a where a.academicAreaAbbreviation=:academicArea and a.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("academicArea", academicArea).
		setCacheable(true).
		uniqueResult();
	}

	ReservationType fetchReservationType(String type) {
		return (ReservationType)
		getHibSession().
		createQuery("select distinct a from ReservationType as a where a.reference=:ref").
		setString("ref", type).
		setCacheable(true).
		uniqueResult();
	}
}
