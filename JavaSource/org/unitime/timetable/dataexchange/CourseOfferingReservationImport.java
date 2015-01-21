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
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;


/**
 * 
 * @author Timothy Almon, Tomas Muller
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
