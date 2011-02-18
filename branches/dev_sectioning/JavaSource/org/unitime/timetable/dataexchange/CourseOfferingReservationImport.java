/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;


/**
 * 
 * @author Timothy Almon
 *
 */
public class CourseOfferingReservationImport extends BaseImport {


    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("courseOfferingReservations")) {
            throw new Exception("Given XML file is not a Course Offering Reservations load file.");
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
                SubjectArea subject = this.fetchSubjectArea(element.attributeValue("subject"), session.getSessionId());
                CourseOffering course = this.fetchCourseOffering(element.attributeValue("courseNumber"), subject.getUniqueId());
                String r = element.attributeValue("reservation");
                course.setReservation(r == null ? null : Integer.valueOf(r));
                getHibSession().saveOrUpdate(course);
                
                flushIfNeeded(false);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

	SubjectArea fetchSubjectArea(String subject, Long sessionId) {
		return (SubjectArea)getHibSession().
			createQuery("select distinct a from SUBJECT_AREA as a where a.subjectAreaAbbreviation=:subject and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("subject", subject).
			setCacheable(true).
			uniqueResult();
	}

	CourseOffering fetchCourseOffering(String courseNumber, Long subjectAreaId) {
		return (CourseOffering) getHibSession().
			createQuery("select distinct a from COURSE_OFFERING as a where a.courseNumber=:courseNumber and a.subjectArea=:subjectArea").
			setLong("subjectArea", subjectAreaId.longValue()).
			setString("courseNumber", courseNumber).
			setCacheable(true).
			uniqueResult();
	}
}
