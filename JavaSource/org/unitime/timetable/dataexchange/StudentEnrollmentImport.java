/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008-2009, UniTime LLC, and individual contributors
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.SectioningServer;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.test.UpdateExamConflicts;

public class StudentEnrollmentImport extends BaseImport {

	public StudentEnrollmentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		boolean trimLeadingZerosFromExternalId = "true".equals(ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false"));

        if (!rootElement.getName().equalsIgnoreCase("studentEnrollments"))
        	throw new Exception("Given XML file is not a Student Enrollments load file.");
        
        Session session = null;
        
        Set<Long> updatedStudents = new HashSet<Long>(); 
        
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        String created = rootElement.attributeValue("created");
			
	        beginTransaction();
	        
	        session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        
	        if(session == null)
	           	throw new Exception("No session found for the given campus, year, and term.");

	    	HashMap<String, Class_> classes = new HashMap<String, Class_>();
	    	HashMap<String, CourseOffering> controllingCourses = new HashMap<String, CourseOffering>();
	 		for (Iterator it = Class_.findAll(getHibSession(), session.getUniqueId()).iterator(); it.hasNext();) {
				Class_ c = (Class_) it.next();
				if (c.getExternalUniqueId() != null && !c.getExternalUniqueId().isEmpty()) {
					classes.put(c.getExternalUniqueId(), c);
					controllingCourses.put(c.getExternalUniqueId(), c.getSchedulingSubpart().getControllingCourseOffering());
				} else {
					classes.put(c.getClassLabel(), c);
					controllingCourses.put(c.getClassLabel(), c.getSchedulingSubpart().getControllingCourseOffering());
				}
			}
	        debug("classes loaded");
	        
	        TimetableManager manger = getManager();
	        if (manger == null)
	        	manger = findDefaultManager();

	        if (created != null)
				ChangeLog.addChange(getHibSession(), manger, session, session, created, ChangeLog.Source.DATA_IMPORT_STUDENT_ENROLLMENTS, ChangeLog.Operation.UPDATE, null, null);
         
	        Hashtable<String, Student> students = new Hashtable<String, Student>();
	        for (Student student: StudentDAO.getInstance().findBySession(getHibSession(), session.getUniqueId())) {
	        	if (student.getExternalUniqueId() != null)
	        		students.put(student.getExternalUniqueId(), student);
	        }
	        
 	        for (Iterator i = rootElement.elementIterator("student"); i.hasNext(); ) {
	            Element studentElement = (Element) i.next();
	            
	            String externalId = studentElement.attributeValue("externalId");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
	            
            	Student student = students.remove(externalId);
            	if (student == null) {
            		student = new Student();
	                student.setSession(session);
		            student.setFirstName(studentElement.attributeValue("firstName", "Name"));
		            student.setMiddleName(studentElement.attributeValue("middleName"));
		            student.setLastName(studentElement.attributeValue("lastName", "Unknown"));
		            student.setEmail(studentElement.attributeValue("email"));
		            student.setExternalUniqueId(externalId);
		            student.setFreeTimeCategory(0);
		            student.setSchedulePreference(0);
		            student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
            	}
            	
            	Hashtable<String, StudentClassEnrollment> enrollments = new Hashtable<String, StudentClassEnrollment>();
            	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
            		enrollments.put(enrollment.getClazz().getExternalUniqueId() == null || enrollment.getClazz().getExternalUniqueId().isEmpty() ? enrollment.getClazz().getClassLabel() : enrollment.getClazz().getExternalUniqueId(), enrollment);
            	}
            	
            	for (Iterator j = studentElement.elementIterator("class"); j.hasNext(); ) {
            		Element classElement = (Element) j.next();
            		
            		String classExternalId  = classElement.attributeValue("externalId");
            		Class_ clazz = (classExternalId == null ? null : classes.get(classExternalId));
            		if (clazz == null) {
            			warn("Class " + externalId + " not found.");
            			continue;
            		}
            		
            		StudentClassEnrollment enrollment = enrollments.remove(classExternalId);
            		if (enrollment != null) continue; // enrollment already exists
            		
            		enrollment = new StudentClassEnrollment();
            		enrollment.setStudent(student);
            		enrollment.setClazz(clazz);
            		enrollment.setCourseOffering(controllingCourses.get(classExternalId));
            		enrollment.setTimestamp(new java.util.Date());
            		student.getClassEnrollments().add(enrollment);
            		
            		if (student.getUniqueId() != null) updatedStudents.add(student.getUniqueId());
            	}
            	
            	if (!enrollments.isEmpty()) {
            		for (StudentClassEnrollment enrollment: enrollments.values()) {
            			student.getClassEnrollments().remove(enrollment);
            			getHibSession().delete(enrollment);
                		updatedStudents.add(student.getUniqueId());
            		}
            	}

            	if (student.getUniqueId() == null) {
            		updatedStudents.add((Long)getHibSession().save(student));
            	} else {
            		getHibSession().update(student);
            	}

	            flushIfNeeded(true);
	        }
 	        
 	        for (Student student: students.values()) {
        		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
        			StudentClassEnrollment enrollment = i.next();
        			getHibSession().delete(enrollment);
        			i.remove();
     	        	updatedStudents.add(student.getUniqueId());
        		}
        		getHibSession().update(student);
 	        }
            
            commitTransaction();
            
            debug(updatedStudents.size() + " students changed");
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
		
		if (session != null && !updatedStudents.isEmpty())
            SectioningServer.studentChanged(session.getUniqueId(), updatedStudents);
		
        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.finalExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                new UpdateExamConflicts(this).update(session.getUniqueId(), Exam.sExamTypeFinal, getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }

        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.midtermExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                new UpdateExamConflicts(this).update(session.getUniqueId(), Exam.sExamTypeMidterm, getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }

        if (session != null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.class.updateEnrollments","true"))){
            try {
                info("  Updating class enrollments...");
                Class_.updateClassEnrollmentForSession(session, getHibSession());
                info("  Updating course offering enrollments...");
                CourseOffering.updateCourseOfferingEnrollmentForSession(session, getHibSession());
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
            } 
        }
	}
	
}
