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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.TimeLocation;
import org.dom4j.Element;
import org.unitime.timetable.dataexchange.StudentEnrollmentImport.Pair;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.util.Constants;

/**
 * 
 * @author Tomas Muller
 *
 */
public class StudentSectioningImport extends BaseImport {
    public StudentSectioningImport() {}
    
    public static enum EnrollmentMode {
    	DELETE("Student enrollments will be deleted."),
    	IMPORT("Student enrollments will be imported."),
    	NOCHANGE("Student enrollments will be left unchanged"),
    	UPDATE("Student enrollments will be updated (only enrollments that are no longer requested will be deleted)"),
    	;
    	private String iText;
    	EnrollmentMode(String text) { iText = text; }
    	
    	@Override
    	public String toString() { return iText; }
    }
    
    public void loadXml(Element rootElement) {
        try {
            beginTransaction();
            
            boolean trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
            
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        
	        EnrollmentMode mode = null;
	        if (rootElement.attributeValue("enrollments") != null) { 
	        	mode = EnrollmentMode.valueOf(rootElement.attributeValue("enrollments").toUpperCase());
	        } else {
	        	mode = EnrollmentMode.UPDATE;
	        	if ("true".equals(rootElement.attributeValue("keepEnrollments", "false")))
	        		mode = EnrollmentMode.NOCHANGE;
	        	mode: for (Iterator i = rootElement.elementIterator("student"); i.hasNext(); ) {
	        		Element studentElement = (Element)i.next();
	        		Element reqCoursesElement = studentElement.element("updateCourseRequests");
	            	if (reqCoursesElement != null && "true".equals(reqCoursesElement.attributeValue("commit", "true")))
	            		for (Iterator j = reqCoursesElement.elementIterator("courseOffering"); j.hasNext(); ) {
	            			Element requestElement = (Element)j.next();
	            			if (requestElement.element("class") != null) { mode = EnrollmentMode.IMPORT; break mode; }
	            			for (Iterator k = requestElement.elementIterator("alternative"); k.hasNext(); ) {
                                Element altElement = (Element)k.next();
                                if (altElement.element("class") != null) { mode = EnrollmentMode.IMPORT; break mode; }
	            			}
	            		}
	        	}
	        }
	        info("Enrollment mode set to " + mode.name() + ": " + mode.toString()); 

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null)
	           	throw new Exception("No session found for the given campus, year, and term.");

            Hashtable<String, Student> students = new Hashtable<String, Student>();
            for (Student student: (List<Student>)getHibSession().createQuery(
            		"from Student s where s.session.uniqueId=:sessionId and s.externalUniqueId is not null").
                    setLong("sessionId",session.getUniqueId()).list()) { 
            	students.put(student.getExternalUniqueId(), student);
            }
            
            Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
            for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
            		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
            }

            Map<String, AcademicClassification> code2clasf = new Hashtable<String, AcademicClassification>();
            for (AcademicClassification clasf: (List<AcademicClassification>)getHibSession().createQuery(
            		"from AcademicClassification where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	code2clasf.put(clasf.getCode(), clasf);
            }
            
            Map<String, PosMajor> code2major = new Hashtable<String, PosMajor>();
            for (PosMajor major: (List<PosMajor>)getHibSession().createQuery(
            		"from PosMajor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	for (AcademicArea area: major.getAcademicAreas())
            		code2major.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);
            }
            
            Map<String, PosMinor> code2minor = new Hashtable<String, PosMinor>();
            for (PosMinor minor: (List<PosMinor>)getHibSession().createQuery(
            		"from PosMinor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	for (AcademicArea area: minor.getAcademicAreas())
            		code2minor.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
            }

            Map<String, StudentGroup> code2group = new Hashtable<String, StudentGroup>();
            for (StudentGroup group: (List<StudentGroup>)getHibSession().createQuery(
            		"from StudentGroup where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	code2group.put(group.getGroupAbbreviation(), group);
            }
            
            Map<String, StudentAccomodation> code2accomodation = new Hashtable<String, StudentAccomodation>();
            for (StudentAccomodation accomodation: (List<StudentAccomodation>)getHibSession().createQuery(
            		"from StudentAccomodation where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	code2accomodation.put(accomodation.getAbbreviation(), accomodation);
            }
            
            Map<String, CourseOffering> name2course = new Hashtable<String, CourseOffering>();
            for (CourseOffering course: (List<CourseOffering>)getHibSession().createQuery(
            		"from CourseOffering where subjectArea.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	name2course.put(course.getCourseName(), course);
            }
            
	    	HashMap<Long, Map<String, Class_>> course2extId2class = new HashMap<Long, Map<String,Class_>>();
	    	HashMap<Long, Map<String, Class_>> course2name2class = new HashMap<Long, Map<String,Class_>>();
	    	info("Loading classes...");
	 		for (Object[] o: (List<Object[]>)getHibSession().createQuery(
	 				"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
    				"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
    				.setLong("sessionId", session.getUniqueId()).list()) {
	 			Class_ clazz = (Class_)o[0];
	 			CourseOffering course = (CourseOffering)o[1];
	 			
	 			Map<String, Class_> extId2class = course2extId2class.get(course.getUniqueId());
	 			if (extId2class == null) {
	 				extId2class = new HashMap<String, Class_>();
	 				course2extId2class.put(course.getUniqueId(), extId2class);
	 			}
	 			Map<String, Class_> name2class = course2name2class.get(course.getUniqueId());
	 			if (name2class == null) {
	 				name2class = new HashMap<String, Class_>();
	 				course2name2class.put(course.getUniqueId(), name2class);
	 			}
				String extId = clazz.getExternalId(course);
				if (extId != null && !extId.isEmpty())
					extId2class.put(extId, clazz);
				String name = clazz.getSchedulingSubpart().getItypeDesc().trim() + " " + clazz.getSectionNumberString();
				name2class.put(name, clazz);
			}
            
            Set<Long> updatedStudents = new HashSet<Long>();
            
            for (Iterator i1 = rootElement.elementIterator("student"); i1.hasNext(); ) {
                Element studentElement = (Element) i1.next();
                
	            String externalId = studentElement.attributeValue("key");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
	            
	            Element cancelElement = studentElement.element("cancelStudent");
	            if (cancelElement != null) {
	            	Student student = students.remove(externalId);
	            	if (student == null) continue;
	            	
            		for (Iterator<CourseDemand> i = student.getCourseDemands().iterator(); i.hasNext(); ) {
            			CourseDemand cd = i.next();
            			if (cd.getFreeTime() != null)
            				getHibSession().delete(cd.getFreeTime());
            			for (CourseRequest cr: cd.getCourseRequests())
            				getHibSession().delete(cr);
            			i.remove();
            			getHibSession().delete(cd);
            			updatedStudents.add(student.getUniqueId());
            		}
            		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
	        			StudentClassEnrollment enrollment = i.next();
	        			getHibSession().delete(enrollment);
	        			i.remove();
	     	        	updatedStudents.add(student.getUniqueId());
	        		}

            		boolean delete = "true".equals(cancelElement.attributeValue("delete", "false"));
	            	if (delete) {
	            		updatedStudents.add(student.getUniqueId());
	            		getHibSession().delete(student);
	            		continue;
	            	}
	            }
	            
	            Element demographicsElement = studentElement.element("updateDemographics");
	            
            	Student student = students.remove(externalId);
            	if (student == null) {
            		if (demographicsElement == null) {
            			error("Student "+externalId + " not found, but no demographics information provided.");
            			continue;
            		}
            	
            		student = new Student();
	                student.setSession(session);
		            student.setExternalUniqueId(externalId);
		            student.setFreeTimeCategory(0);
		            student.setSchedulePreference(0);
		            student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
		            student.setCourseDemands(new HashSet<CourseDemand>());
		            Element name = demographicsElement.element("name");
		            if (name != null) {
		                student.setFirstName(name.attributeValue("first"));
		                student.setMiddleName(name.attributeValue("middle"));
		                student.setLastName(name.attributeValue("last"));
		            } else {
		            	student.setFirstName("Name");
		                student.setLastName("Unknown");
		            }
		            Element email = demographicsElement.element("email");
		            if (email != null)
		                student.setEmail(email.attributeValue("value"));
		            student.setAcademicAreaClassifications(new HashSet<AcademicAreaClassification>());
		            student.setPosMajors(new HashSet<PosMajor>());
		            student.setPosMinors(new HashSet<PosMinor>());
		            student.setGroups(new HashSet<StudentGroup>());
		            student.setAccomodations(new HashSet<StudentAccomodation>());
            	} else if (demographicsElement != null) {
		            Element name = demographicsElement.element("name");
		            if (name!=null) {
		            	String fName = name.attributeValue("first");
		            	String mName = name.attributeValue("middle");
		            	String lName = name.attributeValue("last");
	                	if (!eq(fName, student.getFirstName())) {
	                		student.setFirstName(name.attributeValue("first"));
	                		updatedStudents.add(student.getUniqueId());
	                	}
	                	if (!eq(mName, student.getMiddleName())) {
	                		student.setMiddleName(mName);
	                		updatedStudents.add(student.getUniqueId());
	                	}
	                	if (!eq(lName, student.getLastName())) {
	                		student.setLastName(lName);
	                		updatedStudents.add(student.getUniqueId());
	                	}
		            }
		            Element email = demographicsElement.element("email");
		            if (email != null && !eq(email.attributeValue("value"), student.getEmail())) {
		            	student.setEmail(email.attributeValue("value"));
		            	updatedStudents.add(student.getUniqueId());
		            }
            	}
            	if (demographicsElement != null) {
                	Map<String, AcademicAreaClassification> sAreaClasf = new Hashtable<String, AcademicAreaClassification>();
                	for (AcademicAreaClassification aac: student.getAcademicAreaClassifications())
                		sAreaClasf.put(aac.getAcademicArea().getAcademicAreaAbbreviation() + ":" + aac.getAcademicClassification().getCode(), aac);
                	
                	Map<String, PosMajor> sMajors = new Hashtable<String, PosMajor>();
                	for (PosMajor major: student.getPosMajors())
                		for (AcademicArea area: major.getAcademicAreas())
                			sMajors.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);

                	Map<String, PosMinor> sMinors = new Hashtable<String, PosMinor>();
                	for (PosMinor minor: student.getPosMinors())
                		for (AcademicArea area: minor.getAcademicAreas())
                			sMinors.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
                	
                	for (Iterator i2 = demographicsElement.elementIterator("acadArea"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String area = e.attributeValue("abbv");
    	    			String clasf = e.attributeValue("classification");
    	    			if (sAreaClasf.remove(area + ":" + clasf) == null) {
    	    				AcademicAreaClassification aac = new AcademicAreaClassification();
    	    				if (abbv2area.get(area) == null) {
    	    					warn("Academic area " + area + " not known.");
    	    					continue;
    	    				}
    	    				aac.setAcademicArea(abbv2area.get(area));
    	    				if (code2clasf.get(clasf) == null) {
    	    					warn("Academic classification " + clasf + " not known.");
    	    					continue;
    	    				}
    	    				aac.setAcademicClassification(code2clasf.get(clasf));
    	    				aac.setStudent(student);
    	    				student.getAcademicAreaClassifications().add(aac);
                    		if (student.getUniqueId() != null)
                    			updatedStudents.add(student.getUniqueId());
    	    			}
    	    			
                    	for (Iterator i3 = e.elementIterator("major"); i3.hasNext();) {
        	    			Element f = (Element) i3.next();
        	    			String code = f.attributeValue("code");
        	    			if (sMajors.remove(area + ":" + code) == null) {
        	    				PosMajor major = code2major.get(area + ":" + code);
        	    				if (major == null) {
        	    					warn("Major " + area + " " + code + " not known.");
        	    					continue;
        	    				}
        	    				student.getPosMajors().add(major);
                        		if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
                    	}
                    	
                    	for (Iterator i3 = e.elementIterator("minor"); i3.hasNext();) {
        	    			Element f = (Element) i3.next();
        	    			String code = f.attributeValue("code");
        	    			if (sMinors.remove(area + ":" + code) == null) {
        	    				PosMinor minor = code2minor.get(area + ":" + code);
        	    				if (minor == null) {
        	    					warn("Minor " + area + " " + code + " not known.");
        	    					continue;
        	    				}
        	    				student.getPosMinors().add(minor);
                        		if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
                    	}
    	            }
                	for (Iterator i2 = demographicsElement.elementIterator("major"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("code");
    	    			for (Iterator i3 = demographicsElement.elementIterator("acadArea"); i3.hasNext();) {
    	    				Element f = (Element) i3.next();
        	    			String area = f.attributeValue("abbv");
        	    			PosMajor major = code2major.get(area + ":" + code);
        	    			if (major == null) continue;
        	    			if (sMajors.remove(area + ":" + code) == null) {
        	    				student.getPosMajors().add(major);
                        		if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
    	    			}
                	}
                	for (Iterator i2 = demographicsElement.elementIterator("minor"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("code");
    	    			for (Iterator i3 = demographicsElement.elementIterator("acadArea"); i3.hasNext();) {
    	    				Element f = (Element) i3.next();
        	    			String area = f.attributeValue("abbv");
        	    			PosMinor minor = code2minor.get(area + ":" + code);
        	    			if (minor == null) continue;
        	    			if (sMinors.remove(area + ":" + code) == null) {
        	    				student.getPosMinors().add(minor);
                        		if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
    	    			}
                	}

                	for (AcademicAreaClassification aac: sAreaClasf.values()) {
                		student.getAcademicAreaClassifications().remove(aac);
                		getHibSession().delete(aac);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}
                	for (PosMajor major: sMajors.values()) {
                		student.getPosMajors().remove(major);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}
                	for (PosMinor minor: sMinors.values()) {
                		student.getPosMinors().remove(minor);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}            		
                	
            		Map<String, StudentGroup> sGroups = new Hashtable<String, StudentGroup>();
            		for (StudentGroup group: student.getGroups())
            			sGroups.put(group.getGroupAbbreviation(), group);
            		for (Iterator i2 = demographicsElement.elementIterator("groupAffiliation"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("code");
    	    			if (sGroups.remove(code) == null) {
    	    				StudentGroup group = code2group.get(code);
    	    				if (group == null) {
    	    					warn("Student group " + code + " not known.");
    	    					continue;
    	    				}
    	    				student.getGroups().add(group);
    	    				group.getStudents().add(student);
    	    				getHibSession().saveOrUpdate(group);
                    		if (student.getUniqueId() != null)
                    			updatedStudents.add(student.getUniqueId());
    	    			}
            		}
                	for (StudentGroup group: sGroups.values()) {
                		if (group.getExternalUniqueId() == null) continue;
                		student.getGroups().remove(group);
                		group.getStudents().remove(student);
                		getHibSession().saveOrUpdate(group);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}
                	
                	
                	Map<String, StudentAccomodation> sAccomodations = new Hashtable<String, StudentAccomodation>();
            		for (StudentAccomodation accomodation: student.getAccomodations())
            			sAccomodations.put(accomodation.getAbbreviation(), accomodation);
            		for (Iterator i2 = demographicsElement.elementIterator("disability"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("code");
    	    			if (sAccomodations.remove(code) == null) {
    	    				StudentAccomodation accomodation = code2accomodation.get(code);
    	    				if (accomodation == null) {
    	    					warn("Student accomodation " + code + " not known.");
    	    					continue;
    	    				}
    	    				student.getAccomodations().add(accomodation);
    	    				accomodation.getStudents().add(student);
                    		if (student.getUniqueId() != null)
                    			updatedStudents.add(student.getUniqueId());
    	    			}
            		}
                	for (StudentAccomodation accomodation: sAccomodations.values()) {
                		student.getAccomodations().remove(accomodation);
                		accomodation.getStudents().remove(student);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}
            	}
            	
            	
            	if (student.getUniqueId() == null) {
            		updatedStudents.add((Long)getHibSession().save(student));
            	} else {
            		getHibSession().update(student);
            	}
            	
            	Element reqCoursesElement = studentElement.element("updateCourseRequests");
            	if (reqCoursesElement != null && "true".equals(reqCoursesElement.attributeValue("commit", "true"))) {
                	Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
                	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
                		enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
                	}
                	
            		Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
            		int priority = 0;
            		Date ts = new Date();
            		Map<Long, CourseRequest> course2request = new HashMap<Long, CourseRequest>();
            		List<CourseRequest> unusedRequests = new ArrayList<CourseRequest>();
            		
                    for (Iterator i = reqCoursesElement.elementIterator(); i.hasNext(); priority++) {
                        Element requestElement = (Element)i.next();
                        String waitList = requestElement.attributeValue("waitlist");
                        String alternative = requestElement.attributeValue("alternative");
                        if (requestElement.getName().equals("courseOffering")) {
                        	List<CourseOffering> courses = new ArrayList<CourseOffering>();
                        	List<Integer> credits = new ArrayList<Integer>();
                        	List<Element> elements = new ArrayList<Element>();
                        	
                        	CourseOffering course = name2course.get(requestElement.attributeValue("subjectArea") + " " + requestElement.attributeValue("courseNumber"));
                            if (course == null)
                                warn("Course " + requestElement.attributeValue("subjectArea") + " " + requestElement.attributeValue("courseNumber") + " not found.");
                            else {
                            	courses.add(course);
                            	credits.add(Integer.valueOf(requestElement.attributeValue("credit", "0")));
                            	elements.add(requestElement);
                            }
                            
                            for (Iterator j = requestElement.elementIterator("alternative"); j.hasNext(); ) {
                                Element altElement = (Element)j.next();
                                
                                CourseOffering altCourse = name2course.get(altElement.attributeValue("subjectArea") + " " + altElement.attributeValue("courseNumber"));
                                if (altCourse == null)
                                    warn("Course " + altElement.attributeValue("subjectArea") + " " + altElement.attributeValue("courseNumber") + " not found.");
                                else {
                                	courses.add(altCourse);
                                	credits.add(Integer.valueOf(altElement.attributeValue("credit", "0")));
                                	elements.add(altElement);
                                }
                            }
                            
                            if (courses.isEmpty()) continue;
                                        				
            				CourseDemand cd = null;
            				adepts: for (Iterator<CourseDemand> j = remaining.iterator(); j.hasNext(); ) {
            					CourseDemand adept = j.next();
            					if (adept.getFreeTime() != null) continue;
            					for (CourseRequest cr: adept.getCourseRequests())
            						if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getUniqueId())) {
            							cd = adept; j.remove();  break adepts;
            						}
            				}
            				if (cd == null) {
            					cd = new CourseDemand();
            					cd.setTimestamp(ts);
            					cd.setCourseRequests(new HashSet<CourseRequest>());
            					cd.setStudent(student);
            					student.getCourseDemands().add(cd);
            				}
        					cd.setAlternative("true".equals(alternative));
        					cd.setPriority(priority);
        					cd.setWaitlist("true".equals(waitList));
                            
            				Iterator<CourseRequest> requests = new TreeSet<CourseRequest>(cd.getCourseRequests()).iterator();
            				int order = 0;
            				for (CourseOffering co: courses) {
            					CourseRequest cr = null;
            					if (requests.hasNext()) {
            						cr = requests.next();
            						if (cr.getCourseRequestOptions() != null) {
            							for (Iterator<CourseRequestOption> j = cr.getCourseRequestOptions().iterator(); j.hasNext(); )
            								getHibSession().delete(j.next());
            							cr.getCourseRequestOptions().clear();
            						}
            					} else {
            						cr = new CourseRequest();
            						cd.getCourseRequests().add(cr);
            						cr.setCourseDemand(cd);
            						cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
            					}
            					cr.setAllowOverlap(false);
            					cr.setCredit(credits.get(order));
            					cr.setOrder(order++);
            					cr.setCourseOffering(co);
            					course2request.put(co.getUniqueId(), cr);
            				}
            				while (requests.hasNext()) {
            					unusedRequests.add(requests.next());
            					requests.remove();
            				}
            				getHibSession().saveOrUpdate(cd);
            				
            				if (mode == EnrollmentMode.IMPORT) {
                				for (int j = 0; j < courses.size(); j++) {
                					CourseOffering co = courses.get(j);
                					Element reqEl = elements.get(j);
                					
                    	 			Map<String, Class_> extId2class = course2extId2class.get(co.getUniqueId());
                    	 			Map<String, Class_> name2class = course2name2class.get(co.getUniqueId());
                                    for (Iterator k = reqEl.elementIterator("class"); k.hasNext(); ) {
                                        Element classElement = (Element)k.next();
                                        Class_ clazz = null;
                                        
                                		String classExternalId  = classElement.attributeValue("externalId");
                                		if (classExternalId != null) {
                                			clazz = extId2class.get(classExternalId);
                                			if (clazz == null)
                                    			clazz = name2class.get(classExternalId);
                                		}
                                		
                                		if (clazz == null) {
                                    		String type = classElement.attributeValue("type");
                                    		String suffix = classElement.attributeValue("suffix");
                                    		if (type != null && suffix != null)
                                    			clazz = name2class.get(type.trim() + " " + suffix);
                                		}
                                		
                                		if (clazz == null) {
                                			warn(co.getCourseName() + ": Class " + (classExternalId != null ? classExternalId : classElement.attributeValue("type") + " " + classElement.attributeValue("suffix")) + " not found.");
                                			continue;
                                		}

                                		StudentClassEnrollment enrollment = enrollments.remove(new Pair(co.getUniqueId(), clazz.getUniqueId()));
                                		if (enrollment != null) continue; // enrollment already exists
                                		
                                		enrollment = new StudentClassEnrollment();
                                		enrollment.setStudent(student);
                                		enrollment.setClazz(clazz);
                                		enrollment.setCourseOffering(co);
                                		enrollment.setTimestamp(ts);
                                		enrollment.setChangedBy(StudentClassEnrollment.SystemChange.IMPORT.toString());
                                		enrollment.setCourseRequest(course2request.get(co.getUniqueId()));
                                		student.getClassEnrollments().add(enrollment);
                                    }
                				}
            				}
                        } else if (requestElement.getName().equals("freeTime")) {
                            String days = requestElement.attributeValue("days");
                            String startTime = requestElement.attributeValue("startTime");
                            String length = requestElement.attributeValue("length");
                            String endTime = requestElement.attributeValue("endTime");
                            TimeLocation time = makeTime(student.getSession().getDefaultDatePattern(), days, startTime, endTime, length);
                            
        					CourseDemand cd = null;
        					for (Iterator<CourseDemand> j = remaining.iterator(); j.hasNext(); ) {
        						CourseDemand adept = j.next();
        						if (adept.getFreeTime() == null) continue;
        						cd = adept; j.remove(); break;
        					}
        					if (cd == null) {
        						cd = new CourseDemand();
        						cd.setTimestamp(ts);
        						student.getCourseDemands().add(cd);
        						cd.setStudent(student);
        					}
        					
        					cd.setAlternative("true".equals(alternative));
        					cd.setPriority(priority);
        					cd.setWaitlist("true".equals(waitList));
        					FreeTime free = cd.getFreeTime();
        					if (free == null) {
        						free = new FreeTime();
        						cd.setFreeTime(free);
        					}
        					free.setCategory(time.getBreakTime());
        					free.setDayCode(time.getDayCode());
        					free.setStartSlot(time.getStartSlot());
        					free.setLength(time.getLength());
        					free.setSession(student.getSession());
        					free.setName(time.getLongName(true));
        					getHibSession().saveOrUpdate(free);
        					getHibSession().saveOrUpdate(cd);
                        } else warn("Request element "+requestElement.getName()+" not recognized.");
                    }
                    
                    if (mode == EnrollmentMode.DELETE || mode == EnrollmentMode.IMPORT) {
                    	for (StudentClassEnrollment enrl: enrollments.values()) {
                			student.getClassEnrollments().remove(enrl);
                			enrl.getClazz().getStudentEnrollments().remove(enrl);
                			getHibSession().delete(enrl);
                		}
                    } else {
                        for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
                			StudentClassEnrollment enrl = i.next();
            				CourseRequest cr = course2request.get(enrl.getCourseOffering().getUniqueId());
            				if (cr == null) {
            					if (mode == EnrollmentMode.NOCHANGE) {
                					enrl.setCourseRequest(null);
                					getHibSession().saveOrUpdate(enrl);
            					} else {
                    				enrl.getClazz().getStudentEnrollments().remove(enrl);
                    				getHibSession().delete(enrl);
                    				i.remove();
            					}
            				} else {
            					enrl.setCourseRequest(cr);
            					getHibSession().saveOrUpdate(enrl);
            				}
                		}
                    }
            		
            		for (CourseRequest cr: unusedRequests)
            			getHibSession().delete(cr);
            		
            		for (CourseDemand cd: remaining) {
            			if (cd.getFreeTime() != null)
            				getHibSession().delete(cd.getFreeTime());
            			for (CourseRequest cr: cd.getCourseRequests())
            				getHibSession().delete(cr);
            			student.getCourseDemands().remove(cd);
            			getHibSession().delete(cd);
            		}

            		updatedStudents.add(student.getUniqueId());
            	}
            	
            	getHibSession().update(student);
	        }
	            
            info(updatedStudents.size() + " students changed");
            
 	        if (!updatedStudents.isEmpty())
 	 	        StudentSectioningQueue.studentChanged(getHibSession(), null, session.getUniqueId(), updatedStudents);
 	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    
    private TimeLocation makeTime(DatePattern dp, String days, String startTime, String endTime, String length) {
        int dayCode = 0, idx = 0;
        for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++) {
            if (days.startsWith(Constants.DAY_NAMES_SHORT[i], idx)) {
                dayCode += Constants.DAY_CODES[i];
                idx += Constants.DAY_NAMES_SHORT[i].length();
            }
        }
        int startSlot = (((Integer.parseInt(startTime)/100)*60 + Integer.parseInt(startTime)%100) - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
        int nrSlots = 0;
        if (length!=null) {
            nrSlots = Integer.parseInt(length) / Constants.SLOT_LENGTH_MIN; 
        } else {
            nrSlots = ((Integer.parseInt(endTime)/100)*60 + Integer.parseInt(endTime)%100) - ((Integer.parseInt(startTime)/100)*60 + Integer.parseInt(startTime)%100) / Constants.SLOT_LENGTH_MIN;
        }
        return new TimeLocation(
                dayCode,
                startSlot,
                nrSlots,
                0,
                0,
                dp.getUniqueId(),
                dp.getName(),
                dp.getPatternBitSet(),
                0);
    }
    
	private boolean eq(String a, String b) {
		return (a == null ? b == null : a.equals(b));
	}
}
