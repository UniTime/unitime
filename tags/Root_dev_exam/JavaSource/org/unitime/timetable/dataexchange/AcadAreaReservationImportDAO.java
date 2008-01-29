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

import java.io.FileInputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.AcadAreaReservationDAO;
import org.unitime.timetable.model.dao.AcademicAreaDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class AcadAreaReservationImportDAO extends AcadAreaReservationDAO {

	public AcadAreaReservationImportDAO() {
		super();
	}

	public void loadFromXML(String filename) throws Exception {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);
			loadFromStream(fis);
		} finally {
			if (fis != null) fis.close();
		}
		return;
	}

	public void loadFromStream(FileInputStream fis) throws Exception {

		Document document = (new SAXReader()).read(fis);
        Element root = document.getRootElement();
        
        loadFromXML(root);
    }
    
    public void loadFromXML(Element root) throws Exception {

        if (!root.getName().equalsIgnoreCase("academicAreaReservations")) {
        	throw new Exception("Given XML file is not an Academic Area Reservation load file.");
        }

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
				if(academicArea == null) {
					throw new Exception("Academic Area is required.");
				}
				String reservationType = el.attributeValue("reservationType");
				if(reservationType == null) {
					throw new Exception("Reservation Type is required.");
				}
				AcademicArea area = fetchAcademicArea(academicArea, session.getSessionId());
				if(area == null) continue;
				String priority = el.attributeValue("priority");
				if(priority == null) {
					priority = "1";
				}
	
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
		        saveOrUpdate(reservation);
			}
        }
	}

	AcademicArea fetchAcademicArea(String academicArea, Long sessionId) {
		return (AcademicArea) new AcademicAreaDAO().
		getSession().
		createQuery("select distinct a from AcademicArea as a where a.academicAreaAbbreviation=:academicArea and a.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("academicArea", academicArea).
		setCacheable(true).
		uniqueResult();
	}

	ReservationType fetchReservationType(String type) {
		return (ReservationType) this.
		getSession().
		createQuery("select distinct a from ReservationType as a where a.reference=:ref").
		setString("ref", type).
		setCacheable(true).
		uniqueResult();
	}
}