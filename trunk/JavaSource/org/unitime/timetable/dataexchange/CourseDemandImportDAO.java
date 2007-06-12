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
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.LastLikeCourseDemandDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class CourseDemandImportDAO extends LastLikeCourseDemandDAO {

	public CourseDemandImportDAO() {
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

        if (!root.getName().equalsIgnoreCase("lastLikeCourseDemand")) {
        	throw new Exception("Given XML file is not a Last Like Course Demand load file.");
        }

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
            Student student = fetchStudent(externalId, session.getSessionId());
            if(student == null) {
            	throw new Exception("Student with external Id = " + externalId + " was not found");
            }
            loadCourses(element, student, session);
        }
        return;
	}

	Student fetchStudent(String externalId, Long sessionId) {
		return (Student) this.
		getSession().
		createQuery("select distinct a from Student as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("externalId", externalId).
		setCacheable(true).
		uniqueResult();
	}

	private void loadCourses(Element studentEl, Student student, Session session) throws Exception {
		for (Iterator it = studentEl.elementIterator(); it.hasNext();) {
			Element el = (Element) it.next();
			String subject = el.attributeValue("subject");
			if(subject == null) {
				throw new Exception("Subject is required.");
			}
			String courseNumber = el.attributeValue("courseNumber");
			if(courseNumber == null) {
				throw new Exception("Course Number is required.");
			}
			SubjectArea area = fetchSubjectArea(subject, session.getSessionId());
			if(area == null) {
				continue;
			}
	        LastLikeCourseDemand demand = new LastLikeCourseDemand();

	        CourseOffering courseOffering = fetchCourseOffering(courseNumber, area.getUniqueId());
			if(courseOffering == null) {
		        demand.setCoursePermId(null);
			} else {
		        demand.setCoursePermId(courseOffering.getPermId());
			}
	        demand.setCourseNbr(courseNumber);
	        demand.setStudent(student);
	        demand.setSubjectArea(area);
	        demand.setPriority(Integer.decode(el.attributeValue("priority")));
	        saveOrUpdate(demand);
		}
	}

	SubjectArea fetchSubjectArea(String subjectAreaAbbv, Long sessionId) {
		return (SubjectArea) new SubjectAreaDAO().
		getSession().
		createQuery("select distinct a from SubjectArea as a where a.subjectAreaAbbreviation=:subjectAreaAbbv and a.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("subjectAreaAbbv", subjectAreaAbbv).
		setCacheable(true).
		uniqueResult();
	}

	CourseOffering fetchCourseOffering(String courseNbr, Long subjectAreaId) {
		return (CourseOffering) new CourseOfferingDAO().
		getSession().
		createQuery("select distinct a from CourseOffering as a where a.courseNbr=:courseNbr and a.subjectArea=:subjectAreaId").
		setLong("subjectAreaId", subjectAreaId.longValue()).
		setString("courseNbr", courseNbr).
		setCacheable(true).
		uniqueResult();
	}
}