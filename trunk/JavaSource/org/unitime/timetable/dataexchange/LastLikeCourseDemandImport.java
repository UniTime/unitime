/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

/**
 * 
 * @author Timothy Almon, Stephanie Schluttenhofer, Tomas Muller
 *
 */

public class LastLikeCourseDemandImport extends BaseImport {

	private HashMap<String, SubjectArea> subjectAreas = new HashMap<String, SubjectArea>();
	private HashMap<String, String> courseOfferings = new HashMap<String, String>();
	private HashMap<String, String> externalIdCoursePermId = new HashMap<String, String>();
	private HashMap<String, String> externalIdCourseNumber = new HashMap<String, String>();
	private HashMap<String, SubjectArea> externalIdSubjectArea = new HashMap<String, SubjectArea>();
	protected TimetableManager manager = null;
	protected boolean trimLeadingZerosFromExternalId = false;
	
	public LastLikeCourseDemandImport() {
		super();
	}

	public void loadXml(Element root) throws Exception {
		String trimLeadingZeros =
	        ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false");
		if (trimLeadingZeros.equals("true")){
			trimLeadingZerosFromExternalId = true;
		}
		try {
			String rootElementName = "lastLikeCourseDemand";
	        if (!root.getName().equalsIgnoreCase(rootElementName)) {
	        	throw new Exception("Given XML file is not a Course Offerings load file.");
	        }

	        String campus = root.attributeValue("campus");
	        String year   = root.attributeValue("year");
	        String term   = root.attributeValue("term");
	        String created = getOptionalStringAttribute(root, "created");
	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
	        loadSubjectAreas(session.getSessionId());
	        loadCourseOfferings(session.getSessionId());

			beginTransaction();
	        if (created != null) {
				ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_LASTLIKE_DEMAND, ChangeLog.Operation.UPDATE, null, null);
	        }
           
            getHibSession().createQuery("delete LastLikeCourseDemand ll where ll.subjectArea.uniqueId in " +
                    "(select s.uniqueId from SubjectArea s where s.session.uniqueId=:sessionId)").
                    setLong("sessionId", session.getUniqueId()).executeUpdate();
            
            flush(true);
            
	        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
	            Element element = (Element) it.next();
	            String externalId = element.attributeValue("externalId");
	            if (trimLeadingZerosFromExternalId){
	            	try {
	            		Integer num = new Integer(externalId);
	            		externalId = num.toString();
					} catch (Exception e) {
						// do nothing
					}
	            }
	            Student student = fetchStudent(externalId, session.getSessionId());
	            if (student == null){
	            	student = new Student();
	            	student.setFirstName("Unknown");
	            	student.setLastName("Student");
	            	student.setExternalUniqueId(externalId);
	            	student.setFreeTimeCategory(new Integer(0));
	            	student.setSchedulePreference(new Integer(0));
	            	student.setSession(session);
	            	getHibSession().save(student);
	            	getHibSession().flush();
	            	getHibSession().refresh(student);
	            }
	            loadCourses(element, student, session);
	            flushIfNeeded(true);
	        }
	        
	        flush(true);
	        
            getHibSession().createQuery("update CourseOffering c set c.demand="+
                    "(select count(distinct d.student) from LastLikeCourseDemand d where "+
                    "(c.subjectArea=d.subjectArea and c.courseNbr=d.courseNbr)) where "+
                    "c.permId is null and c.subjectArea.uniqueId in (select sa.uniqueId from SubjectArea sa where sa.session.uniqueId=:sessionId)").
                    setLong("sessionId", session.getUniqueId()).executeUpdate();

            getHibSession().createQuery("update CourseOffering c set c.demand="+
	                "(select count(distinct d.student) from LastLikeCourseDemand d where "+
	                "d.student.session=c.subjectArea.session and c.permId=d.coursePermId) where "+
	                "c.permId is not null and c.subjectArea.uniqueId in (select sa.uniqueId from SubjectArea sa where sa.session.uniqueId=:sessionId)").
	                setLong("sessionId", session.getUniqueId()).executeUpdate();
            
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
		int row = 0;
		for (Iterator it = studentEl.elementIterator(); it.hasNext();) {
			Element el = (Element) it.next();
			String subject = getOptionalStringAttribute(el, "subject");
			String courseNumber = getOptionalStringAttribute(el, "courseNumber");
			String externalIdStr = getOptionalStringAttribute(el, "externalId");
			if (externalIdStr == null){
				if (subject == null || courseNumber == null){
					throw new Exception("Either a Subject and Course Number is required or an External Id is required.");
				}
			}
			SubjectArea area = null;
			String permId = null;
			if (externalIdStr != null){
				area = externalIdSubjectArea.get(externalIdStr);
				courseNumber = externalIdCourseNumber.get(externalIdStr);
				permId = externalIdCoursePermId.get(externalIdStr);
				if(area == null) {
					System.out.println("Course not found " + externalIdStr + " not found");
					continue;
				}
			} else {
				area = subjectAreas.get(subject);
				if(area == null) {
					System.out.println("Subject area " + subject + " not found");
					continue;
				}
				permId = courseOfferings.get(courseNumber + area.getUniqueId().toString());
			}

	        LastLikeCourseDemand demand = new LastLikeCourseDemand();

			demand.setCoursePermId(permId);

			demand.setCourseNbr(courseNumber);
	        demand.setStudent(student);
	        demand.setSubjectArea(area);
	        demand.setPriority(Integer.decode(el.attributeValue("priority", String.valueOf(row++))));
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
			if (offer.getPermId()!=null){
			    courseOfferings.put(offer.getCourseNbr() + offer.getSubjectArea().getUniqueId().toString(), offer.getPermId());
			}
		    externalIdSubjectArea.put(offer.getExternalUniqueId(), offer.getSubjectArea());
		    externalIdCourseNumber.put(offer.getExternalUniqueId(), offer.getCourseNbr());
		    externalIdCoursePermId.put(offer.getExternalUniqueId(), offer.getPermId());
		}
	}
}
