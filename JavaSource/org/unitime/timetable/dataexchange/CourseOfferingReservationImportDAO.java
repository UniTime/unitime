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
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingReservationDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class CourseOfferingReservationImportDAO extends CourseOfferingReservationDAO {

	public CourseOfferingReservationImportDAO() {
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

        if (!root.getName().equalsIgnoreCase("courseOfferingReservations")) {
        	throw new Exception("Given XML file is not a Course Offering Reservations load file.");
        }

        String campus = root.attributeValue("campus");
        String year   = root.attributeValue("year");
        String term   = root.attributeValue("term");

        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
        if(session == null) {
           	throw new Exception("No session found for the given campus, year, and term.");
        }

        ReservationType type = fetchReservationType();
        
        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            SubjectArea subject = this.fetchSubjectArea(element.attributeValue("subject"), session.getSessionId());
            CourseOffering course = this.fetchCourseOffering(element.attributeValue("courseNumber"), subject.getUniqueId());
            CourseOfferingReservation res = new CourseOfferingReservation();
           	res.setOwner(course.getInstructionalOffering().getUniqueId());
           	res.setCourseOffering(course);
           	res.setPriority(new Integer(1));
            res.setReserved(new Integer(Integer.parseInt(element.attributeValue("reservation"))));
            res.setOwnerClassId("I");
            res.setPriorEnrollment(new Integer(Integer.parseInt(element.attributeValue("priorEnrollment"))));
            res.setProjectedEnrollment(new Integer(Integer.parseInt(element.attributeValue("projectedEnrollment"))));
            res.setReservationType(type);
            saveOrUpdate(res);
        }
        return;
	}

	SubjectArea fetchSubjectArea(String subject, Long sessionId) {
		return (SubjectArea) this.
			getSession().
			createQuery("select distinct a from SUBJECT_AREA as a where a.subjectAreaAbbreviation=:subject and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("subject", subject).
			setCacheable(true).
			uniqueResult();
	}

	CourseOffering fetchCourseOffering(String courseNumber, Long subjectAreaId) {
		return (CourseOffering) this.
			getSession().
			createQuery("select distinct a from COURSE_OFFERING as a where a.courseNumber=:courseNumber and a.subjectArea=:subjectArea").
			setLong("subjectArea", subjectAreaId.longValue()).
			setString("courseNumber", courseNumber).
			setCacheable(true).
			uniqueResult();
	}

	ReservationType fetchReservationType() {
		return (ReservationType) this.
		getSession().
		createQuery("select distinct a from RESERVATION_TYPE as a where a.reference=:ref").
		setString("ref", "perm").
		setCacheable(true).
		uniqueResult();
	}
}