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
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.IndividualOverrideReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.OverrideReservation;
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
	        	if (reservation instanceof LearningCommunityReservation)
	        		course = ((LearningCommunityReservation)reservation).getCourse();
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
	        		classEl.addAttribute("suffix", getClassSuffix(clazz));
	        	}
	        	
	        	if (reservation instanceof OverrideReservation) {
	        		reservationEl.addAttribute("type", ((OverrideReservation)reservation).getOverrideType().getReference());
	        		for (Student student: ((OverrideReservation)reservation).getStudents()) {
	        			reservationEl.addElement("student").addAttribute("externalId", student.getExternalUniqueId());
	        		}
	        	} else if (reservation instanceof IndividualReservation) {
	        		reservationEl.addAttribute("type", "individual");
	        		for (Student student: ((IndividualReservation)reservation).getStudents()) {
	        			reservationEl.addElement("student").addAttribute("externalId", student.getExternalUniqueId());
	        		}
	        		if (reservation instanceof IndividualOverrideReservation) {
	        			IndividualOverrideReservation override = (IndividualOverrideReservation)reservation;
	        			reservationEl.addElement("override", "true");
	        			reservationEl.addElement("expired", override.isAlwaysExpired() ? "true" : "false");
	        			reservationEl.addElement("allowOverlap", override.isAllowOverlap() ? "true" : "false");
	        			reservationEl.addElement("overLimit", override.isCanAssignOverLimit() ? "true" : "false");
	        			reservationEl.addElement("mustBeUsed", override.isMustBeUsed() ? "true" : "false");
	        		}
	        	} else if (reservation instanceof LearningCommunityReservation) {
	        		reservationEl.addAttribute("type", "lc");
	        		StudentGroup group = ((LearningCommunityReservation)reservation).getGroup();
	        		Element groupEl = reservationEl.addElement("studentGroup");
	        		if (group.getExternalUniqueId() != null)
	        			groupEl.addAttribute("externalId", group.getExternalUniqueId());
	        		groupEl.addAttribute("code", group.getGroupAbbreviation());
	        	} else if (reservation instanceof StudentGroupReservation) {
	        		reservationEl.addAttribute("type", "group");
	        		StudentGroup group = ((StudentGroupReservation)reservation).getGroup();
	        		Element groupEl = reservationEl.addElement("studentGroup");
	        		if (group.getExternalUniqueId() != null)
	        			groupEl.addAttribute("externalId", group.getExternalUniqueId());
	        		groupEl.addAttribute("code", group.getGroupAbbreviation());
	        		if (reservation instanceof GroupOverrideReservation) {
	        			GroupOverrideReservation override = (GroupOverrideReservation)reservation;
	        			reservationEl.addElement("override", "true");
	        			reservationEl.addElement("expired", override.isAlwaysExpired() ? "true" : "false");
	        			reservationEl.addElement("allowOverlap", override.isAllowOverlap() ? "true" : "false");
	        			reservationEl.addElement("overLimit", override.isCanAssignOverLimit() ? "true" : "false");
	        			reservationEl.addElement("mustBeUsed", override.isMustBeUsed() ? "true" : "false");
	        		}
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
	        	} else if (reservation instanceof OverrideReservation) {
	        		OverrideReservation ovRes = (OverrideReservation)reservation;
	        		reservationEl.addAttribute("type", ovRes.getOverrideType().getReference());
	        		for (Student student: ovRes.getStudents()) {
	        			reservationEl.addElement("student").addAttribute("externalId", student.getExternalUniqueId());
	        		}
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
