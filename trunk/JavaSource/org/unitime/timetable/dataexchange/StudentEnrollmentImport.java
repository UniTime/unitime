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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.test.UpdateExamConflicts;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class StudentEnrollmentImport extends BaseImport {

	public StudentEnrollmentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		boolean trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();

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

	    	HashMap<String, Class_> extId2class = new HashMap<String, Class_>();
	    	HashMap<String, Class_> name2class = new HashMap<String, Class_>();
	    	HashMap<String, CourseOffering> extId2course = new HashMap<String, CourseOffering>();
	    	HashMap<String, CourseOffering> name2course = new HashMap<String, CourseOffering>();
	    	HashMap<String, CourseOffering> cextId2course = new HashMap<String, CourseOffering>();
	    	HashMap<String, CourseOffering> cname2course = new HashMap<String, CourseOffering>();
	    	HashMap<Long, Set<CourseOffering>> class2courses = new HashMap<Long, Set<CourseOffering>>();
	    	
	    	info("Loading classes...");
	 		for (Object[] o: (List<Object[]>)getHibSession().createQuery(
	 				"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
    				"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
    				.setLong("sessionId", session.getUniqueId()).list()) {
	 			Class_ clazz = (Class_)o[0];
	 			CourseOffering course = (CourseOffering)o[1];
				String extId = clazz.getExternalId(course);
				if (extId != null && !extId.isEmpty())
					extId2class.put(extId, clazz);
				String name = clazz.getClassLabel(course);
				name2class.put(name, clazz);
				name2course.put(name, course);
				if (!extId2course.containsKey(extId) || course.isIsControl())
					extId2course.put(extId, course);
				Set<CourseOffering> courses = class2courses.get(clazz.getUniqueId());
				if (course.getExternalUniqueId() != null && !course.getExternalUniqueId().isEmpty())
					cextId2course.put(course.getExternalUniqueId(), course);
				cname2course.put(course.getCourseName(), course);
				if (courses == null) {
					courses = new HashSet<CourseOffering>();
					class2courses.put(clazz.getUniqueId(), courses);
				}
				courses.add(course);
			}
	        
	        if (created != null)
				ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_STUDENT_ENROLLMENTS, ChangeLog.Operation.UPDATE, null, null);
         
	        info("Loading students...");
	        Hashtable<String, Student> students = new Hashtable<String, Student>();
	        for (Student student: (List<Student>)getHibSession().createQuery(
                    "select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "where s.session.uniqueId=:sessionId and s.externalUniqueId is not null").
                    setLong("sessionId",session.getUniqueId()).list()) { 
	        	students.put(student.getExternalUniqueId(), student);
	        }
	        
	        info("Importing enrollments...");
	        Date ts = new Date();
	        for (Iterator i = rootElement.elementIterator("student"); i.hasNext(); ) {
	            Element studentElement = (Element) i.next();
	            
	            String externalId = studentElement.attributeValue("externalId");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
	            
	            boolean fixCourseDemands = false;
	 	        
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
		            student.setCourseDemands(new HashSet<CourseDemand>());
            	}
            	
            	Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
            	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
            		enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
            	}
            	int nextPriority = 0;
            	for (CourseDemand cd: student.getCourseDemands())
            		if (!cd.isAlternative() && cd.getPriority() >= nextPriority)
            			nextPriority = cd.getPriority() + 1;
            	Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
            	
            	for (Iterator j = studentElement.elementIterator("class"); j.hasNext(); ) {
            		Element classElement = (Element) j.next();
            		
            		Class_ clazz = null;
            		CourseOffering course = null;

            		String classExternalId  = classElement.attributeValue("externalId");
            		if (classExternalId != null) {
            			clazz = extId2class.get(classExternalId);
            			course = extId2course.get(classExternalId);
            			if (clazz == null) {
                			clazz = name2class.get(classExternalId);
                			course = name2course.get(classExternalId);
            			}
            		}
            		
            		if (clazz == null && classElement.attributeValue("name") != null) {
            			String className = classElement.attributeValue("name");
            			clazz = name2class.get(className);
            			course = name2course.get(className);
            		}
            		
            		String courseName = classElement.attributeValue("course");
            		if (courseName != null) {
            			course = cname2course.get(courseName);
            		} else {
                		String subject = classElement.attributeValue("subject");
                		String courseNbr = classElement.attributeValue("courseNbr");
                		if (subject != null && courseNbr != null)
                			course = cname2course.get(subject + " " + courseNbr);
            		}
            		
            		if (course != null  && clazz == null) {
                		String type = classElement.attributeValue("type");
                		String suffix = classElement.attributeValue("suffix");
                		if (type != null && suffix != null)
                			clazz = name2class.get(course.getCourseName() + " " + type.trim() + " " + suffix);
            		}
            		
            		
            		if (clazz == null) {
            			warn("Class " + (classExternalId != null ? classExternalId : classElement.attributeValue("name",
            					classElement.attributeValue("course", classElement.attributeValue("subject") + " " + classElement.attributeValue("courseNbr")) + " " +
            					classElement.attributeValue("type") + " " + classElement.attributeValue("suffix"))) + " not found.");
            			continue;
            		}
            		
            		Set<CourseOffering> courses = class2courses.get(clazz.getUniqueId());
            		if (course == null || !courses.contains(course)) {
            			for (CourseOffering co: courses)
            				if (co.isIsControl())
            					{ course = co; break; }
            		}
            		
            		StudentClassEnrollment enrollment = enrollments.remove(new Pair(course.getUniqueId(), clazz.getUniqueId()));
            		if (enrollment != null) continue; // enrollment already exists
            		
            		enrollment = new StudentClassEnrollment();
            		enrollment.setStudent(student);
            		enrollment.setClazz(clazz);
            		enrollment.setCourseOffering(course);
            		enrollment.setTimestamp(ts);
            		enrollment.setChangedBy(StudentClassEnrollment.SystemChange.IMPORT.toString());
            		student.getClassEnrollments().add(enrollment);
            		
            		demands: for (CourseDemand d: student.getCourseDemands()) {
            			for (CourseRequest r: d.getCourseRequests()) {
            				if (r.getCourseOffering().equals(course)) {
            					enrollment.setCourseRequest(r);
            					break demands;
            				}
            			}
            		}
            		
            		if (enrollment.getCourseRequest() != null) {
            			remaining.remove(enrollment.getCourseRequest().getCourseDemand());
            		} else {
            			CourseDemand cd = new CourseDemand();
    					cd.setTimestamp(ts);
    					cd.setCourseRequests(new HashSet<CourseRequest>());
    					cd.setStudent(student);
    					student.getCourseDemands().add(cd);
    					cd.setAlternative(false);
    					cd.setPriority(nextPriority++);
    					cd.setWaitlist(false);
    					CourseRequest cr = new CourseRequest();
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(0);
						cr.setCourseOffering(enrollment.getCourseOffering());
						enrollment.setCourseRequest(cr);
						cr.getClassEnrollments().add(enrollment);
						fixCourseDemands = true;
            		}
            		
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
            	
            	if (fixCourseDemands) {
            		// removed unused course demands
            		for (CourseDemand cd: remaining) {
            			if (cd.getFreeTime() != null)
            				getHibSession().delete(cd.getFreeTime());
            			for (CourseRequest cr: cd.getCourseRequests())
            				getHibSession().delete(cr);
            			student.getCourseDemands().remove(cd);
            			getHibSession().delete(cd);
            		}
            		int priority = 0;
            		for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
            			cd.setPriority(priority++);
            			getHibSession().saveOrUpdate(cd);
            		}
            	}

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
 	        
            info(updatedStudents.size() + " students changed");

 	        if (!updatedStudents.isEmpty())
 	 	        StudentSectioningQueue.studentChanged(getHibSession(), null, session.getUniqueId(), updatedStudents);
            
            commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
		
        if (session!=null && ApplicationProperty.DataExchangeUpdateStudentConflictsFinal.isTrue()) {
            try {
                beginTransaction();
                for (ExamType type: ExamType.findAllOfType(ExamType.sExamTypeFinal))
                	new UpdateExamConflicts(this).update(session.getUniqueId(), type.getUniqueId(), getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }

        if (session!=null && ApplicationProperty.DataExchangeUpdateStudentConflictsMidterm.isTrue()) {
            try {
                beginTransaction();
                for (ExamType type: ExamType.findAllOfType(ExamType.sExamTypeMidterm))
                	new UpdateExamConflicts(this).update(session.getUniqueId(), type.getUniqueId(), getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }
        
        /*
        if (session != null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.class.updateEnrollments","true"))){
        	org.hibernate.Session hibSession = new _RootDAO().createNewSession();
            try {
                info("  Updating class enrollments...");
                Class_.updateClassEnrollmentForSession(session, hibSession);
                info("  Updating course offering enrollments...");
                CourseOffering.updateCourseOfferingEnrollmentForSession(session, hibSession);
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
            } finally {
            	hibSession.close();
            }
        }
        */
	}
	
	public static class Pair {
		private Long iCourseId, iClassId;
		public Pair(Long courseId, Long classId) {
			iCourseId = courseId; iClassId = classId;
		}
		public Long getCourseId() { return iCourseId; }
		public Long getClassId() { return iClassId; }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Pair)) return false;
			Pair p = (Pair)o;
			return getCourseId().equals(p.getCourseId()) && getClassId().equals(p.getClassId());
		}
		public int hashCode() {
			return getCourseId().hashCode() ^ getClassId().hashCode();
		}
	}
	
}
