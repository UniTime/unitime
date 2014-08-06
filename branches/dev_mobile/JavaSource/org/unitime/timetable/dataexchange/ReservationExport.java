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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;

/**
 * @author Tomas Muller
 */
public class ReservationExport extends BaseExport {
	public static String sDateFormat = "MM/dd/yyyy";

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("reservations");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        root.addAttribute("dateFormat", sDateFormat);
	        root.addAttribute("created", new Date().toString());

	        List<Reservation> reservations = (List<Reservation>)getHibSession().createQuery(
    			"select r from Reservation r where r.instructionalOffering.session.uniqueId = :sessionId")
    			.setLong("sessionId", session.getUniqueId()).list();
	        Collections.sort(reservations, new Comparator<Reservation>() {
				@Override
				public int compare(Reservation r1, Reservation r2) {
					int cmp = r1.getInstructionalOffering().getControllingCourseOffering().compareTo(r2.getInstructionalOffering().getControllingCourseOffering());
					if (cmp != 0) return cmp;
					cmp = r1.getClass().getName().compareTo(r2.getClass().getName());
					if (cmp != 0) return cmp;
					return r1.getUniqueId().compareTo(r2.getUniqueId());
				}
			});
	        
	        SimpleDateFormat df = new SimpleDateFormat(sDateFormat, Locale.US);
	        
	        for (Reservation reservation: (List<Reservation>)getHibSession().createQuery(
	        		"select r from Reservation r where r.instructionalOffering.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	Element reservationEl = root.addElement("reservation");
	        	CourseOffering course = reservation.getInstructionalOffering().getControllingCourseOffering();
	        	if (reservation instanceof CourseReservation)
	        		course = ((CourseReservation)reservation).getCourse();
	        	reservationEl.addAttribute("subject", course.getSubjectAreaAbbv());
	        	reservationEl.addAttribute("courseNbr", course.getCourseNbr());
	        	if (reservation.getLimit() != null && !(reservation instanceof IndividualReservation))
	        		reservationEl.addAttribute("limit", reservation.getLimit().toString());
	        	if (reservation.getExpirationDate() != null)
	        		reservationEl.addAttribute("expire", df.format(reservation.getExpirationDate()));
	        	
	        	for (InstrOfferingConfig config: reservation.getConfigurations()) {
	        		reservationEl.addElement("configuration").addAttribute("name", config.getName());
	        	}
	        	for (Class_ clazz: reservation.getClasses()) {
	        		Element classEl = reservationEl.addElement("class");
	        		if (clazz.getExternalUniqueId() != null)
	        			classEl.addAttribute("externalId", clazz.getExternalUniqueId());
	        		classEl.addAttribute("type", clazz.getItypeDesc().trim());
	        		classEl.addAttribute("suffix", clazz.getSectionNumberString(getHibSession()));
	        	}
	        	
	        	if (reservation instanceof IndividualReservation) {
	        		reservationEl.addAttribute("type", "individual");
	        		for (Student student: ((IndividualReservation)reservation).getStudents()) {
	        			reservationEl.addElement("student").addAttribute("externalId", student.getExternalUniqueId());
	        		}
	        	} else if (reservation instanceof StudentGroupReservation) {
	        		reservationEl.addAttribute("type", "group");
	        		StudentGroup group = ((StudentGroupReservation)reservation).getGroup();
	        		Element groupEl = reservationEl.addElement("studentGroup");
	        		if (group.getExternalUniqueId() != null)
	        			groupEl.addAttribute("externalId", group.getExternalUniqueId());
	        		groupEl.addAttribute("code", group.getGroupAbbreviation());
	        	} else if (reservation instanceof CurriculumReservation) {
	        		reservationEl.addAttribute("type", "curriculum");
	        		CurriculumReservation curRes = (CurriculumReservation)reservation;
	        		Element acadAreaEl = reservationEl.addElement("academicArea");
	        		if (curRes.getArea().getExternalUniqueId() != null)
	        			acadAreaEl.addAttribute("externalId", curRes.getArea().getExternalUniqueId());
	        		acadAreaEl.addAttribute("abbreviation", curRes.getArea().getAcademicAreaAbbreviation());
	        		for (AcademicClassification clasf: curRes.getClassifications()) {
	        			Element clasfEl = reservationEl.addElement("academicClassification");
		        		if (clasf.getExternalUniqueId() != null)
		        			clasfEl.addAttribute("externalId", clasf.getExternalUniqueId());
		        		clasfEl.addAttribute("code", clasf.getCode());
	        		}
	        		for (PosMajor major: curRes.getMajors()) {
	        			Element majorEl = reservationEl.addElement("major");
		        		if (major.getExternalUniqueId() != null)
		        			majorEl.addAttribute("externalId", major.getExternalUniqueId());
		        		majorEl.addAttribute("code", major.getCode());
	        		}
	        	} else if (reservation instanceof CourseReservation) {
	        		if (course.getReservation() != null)
	        			reservationEl.addAttribute("limit", course.getReservation().toString());
	        		reservationEl.addAttribute("type", "course");
	        	} else {
	        		reservationEl.addAttribute("type", "unknown");
	        	}
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}

}
