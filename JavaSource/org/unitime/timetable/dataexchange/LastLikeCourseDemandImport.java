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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

/**
 * 
 * @author Timothy Almon
 *
 */

public class LastLikeCourseDemandImport extends BaseImport {

	private HashMap<String, SubjectArea> subjectAreas = new HashMap<String, SubjectArea>();
	private HashMap<String, String> courseOfferings = new HashMap<String, String>();
	
	public LastLikeCourseDemandImport() {
		super();
	}

	public void loadXml(Element root) throws Exception {
		try {
	        String campus = root.attributeValue("campus");
	        String year   = root.attributeValue("year");
	        String term   = root.attributeValue("term");

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }

	        loadSubjectAreas(session.getSessionId());
	        loadCourseOfferings(session.getSessionId());

			beginTransaction();
            
            getHibSession().createQuery("delete LastLikeCourseDemand ll where ll.student.uniqueId in " +
                    "(select s.uniqueId from Student s where s.session.uniqueId=:sessionId)").
                    setLong("sessionId", session.getUniqueId()).executeUpdate();
            
            flush(true);
            
	        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
	            Element element = (Element) it.next();
	            String externalId = element.attributeValue("externalId");
	            Student student = fetchStudent(externalId, session.getSessionId());
	            if(student == null) continue;
	            loadCourses(element, student, session);
	            flushIfNeeded(true);
	        }
            
            commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}

	Student fetchStudent(String externalId, Long sessionId) {
		return (Student) this.
		getHibSession().
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
			SubjectArea area = subjectAreas.get(subject);
			if(area == null) {
				System.out.println("Subject area " + subject + " not found");
				continue;
			}

	        LastLikeCourseDemand demand = new LastLikeCourseDemand();

			demand.setCoursePermId(courseOfferings.get(courseNumber + area.getUniqueId().toString()));

			demand.setCourseNbr(courseNumber);
	        demand.setStudent(student);
	        demand.setSubjectArea(area);
	        demand.setPriority(Integer.decode(el.attributeValue("priority")));
	        getHibSession().save(demand);
		}
	}

	private void loadSubjectAreas(Long sessionId) {
		List areas = new ArrayList();
		areas = new SubjectAreaDAO().
			getSession().
			createQuery("select distinct a from SubjectArea as a where a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (Iterator it = areas.iterator(); it.hasNext();) {
			SubjectArea area = (SubjectArea) it.next();
			subjectAreas.put(area.getSubjectAreaAbbreviation(), area);
		}
	}

	private void loadCourseOfferings(Long sessionId) {
		for (Iterator it = CourseOffering.findAll(sessionId).iterator(); it.hasNext();) {
			CourseOffering offer = (CourseOffering) it.next();
			courseOfferings.put(offer.getCourseNbr() + offer.getSubjectArea().getUniqueId().toString(), offer.getPermId());

		}
	}
}