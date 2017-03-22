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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.TimeLocation;
import org.dom4j.Element;
import org.unitime.timetable.dataexchange.StudentEnrollmentImport.Pair;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
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
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
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
            
	    	HashMap<Long, Map<String, Set<Class_>>> course2extId2class = new HashMap<Long, Map<String, Set<Class_>>>();
	    	HashMap<Long, Map<String, Set<Class_>>> course2name2class = new HashMap<Long, Map<String, Set<Class_>>>();
	    	info("Loading classes...");
	 		for (Object[] o: (List<Object[]>)getHibSession().createQuery(
	 				"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
    				"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
    				.setLong("sessionId", session.getUniqueId()).list()) {
	 			Class_ clazz = (Class_)o[0];
	 			CourseOffering course = (CourseOffering)o[1];
	 			
	 			Map<String, Set<Class_>> extId2class = course2extId2class.get(course.getUniqueId());
	 			if (extId2class == null) {
	 				extId2class = new HashMap<String, Set<Class_>>();
	 				course2extId2class.put(course.getUniqueId(), extId2class);
	 			}
	 			Map<String, Set<Class_>> name2class = course2name2class.get(course.getUniqueId());
	 			if (name2class == null) {
	 				name2class = new HashMap<String, Set<Class_>>();
	 				course2name2class.put(course.getUniqueId(), name2class);
	 			}
				String extId = clazz.getExternalId(course);
				if (extId != null && !extId.isEmpty()) {
					Set<Class_> sameExtIdClasses = extId2class.get(extId);
					if (sameExtIdClasses == null) {
						sameExtIdClasses = new HashSet<Class_>();
						extId2class.put(extId, sameExtIdClasses);
					}
					sameExtIdClasses.add(clazz);
				}
				String name = clazz.getSchedulingSubpart().getItypeDesc().trim() + " " + clazz.getSectionNumberString();
				Set<Class_> sameNameClasses = name2class.get(name);
				if (sameNameClasses == null) {
					sameNameClasses = new HashSet<Class_>();
					name2class.put(name, sameNameClasses);
				}
				sameNameClasses.add(clazz);
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
		            student.setAreaClasfMajors(new HashSet<StudentAreaClassificationMajor>());
		            student.setAreaClasfMinors(new HashSet<StudentAreaClassificationMinor>());
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
                	Map<String, StudentAreaClassificationMajor> sMajors = new Hashtable<String, StudentAreaClassificationMajor>();
                	for (StudentAreaClassificationMajor major: student.getAreaClasfMajors())
                		sMajors.put(major.getAcademicArea().getAcademicAreaAbbreviation() + ":" + major.getAcademicClassification().getCode() + ":" + major.getMajor().getCode(), major);

                	Map<String, StudentAreaClassificationMinor> sMinors = new Hashtable<String, StudentAreaClassificationMinor>();
                	for (StudentAreaClassificationMinor minor: student.getAreaClasfMinors())
                		sMinors.put(minor.getAcademicArea().getAcademicAreaAbbreviation() + ":" + minor.getAcademicClassification().getCode() + ":" + minor.getMinor().getCode(), minor);
                	
        			for (Iterator i2 = demographicsElement.elementIterator("acadArea"); i2.hasNext();) {
            			Element e = (Element) i2.next();
            			
            			String area = e.attributeValue("abbv");
            			AcademicArea a = abbv2area.get(area);
            			if (a == null) {
            				warn("Academic area " + area + " not known.");
	    					continue;
            			}
            			
    	    			String clasf = e.attributeValue("classification");
    	    			AcademicClassification f = code2clasf.get(clasf);
    	    			if (f == null) {
    	    				warn("Academic classification " + clasf + " not known.");
	    					continue;
    	    			}
    	    			
                    	for (Iterator i3 = e.elementIterator("major"); i3.hasNext();) {
        	    			Element g = (Element) i3.next();
        	    			String code = g.attributeValue("code");
        	    			if (sMajors.remove(area + ":" + clasf + ":" + code) == null) {
        	    				PosMajor m = code2major.get(area + ":" + code);
        	    				if (m == null) {
        	    					warn("Major " + area + " " + code + " not known.");
        	    					continue;
        	    				}
        	    				StudentAreaClassificationMajor acm = new StudentAreaClassificationMajor();
    	        				acm.setAcademicArea(a);
    	        				acm.setAcademicClassification(f);
    	        				acm.setMajor(m);
    	        				acm.setStudent(student);
    	        				student.getAreaClasfMajors().add(acm);
    	        				if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
                    	}
                    	if (e.element("major") == null && e.element("minor") == null) {
                    		boolean noMajor = true;
                        	for (Iterator i3 = demographicsElement.elementIterator("major"); i3.hasNext();) {
            	    			String code = ((Element) i3.next()).attributeValue("code");
            	    			if (code2major.get(area + ":" + code) != null) { noMajor = false; break; }
                        	}
                        	if (noMajor) {
                        		String code = "-";
                        		if (sMajors.remove(area + ":" + clasf + ":" + code) == null) {
                            		PosMajor m = code2major.get(area + ":" + code);
                        			if (m == null) {
                        				m = new PosMajor();
                        				m.addToacademicAreas(a);
                        				m.setExternalUniqueId("-");
                        				m.setCode("-");
                        				m.setName("No Major");
                        				m.setSession(a.getSession());
                        				a.addToposMajors(m);
                        				getHibSession().saveOrUpdate(m);
                        				code2major.put(area + ":" + code, m);
                        			}
            	    				StudentAreaClassificationMajor acm = new StudentAreaClassificationMajor();
        	        				acm.setAcademicArea(a);
        	        				acm.setAcademicClassification(f);
        	        				acm.setMajor(m);
        	        				acm.setStudent(student);
        	        				student.getAreaClasfMajors().add(acm);
        	        				if (student.getUniqueId() != null)
                            			updatedStudents.add(student.getUniqueId());
                        		}
                        	}
                    	}
                    	
                    	for (Iterator i3 = e.elementIterator("minor"); i3.hasNext();) {
        	    			Element g = (Element) i3.next();
        	    			String code = g.attributeValue("code");
        	    			if (sMinors.remove(area + ":" + clasf + ":" + code) == null) {
        	    				PosMinor m = code2minor.get(area + ":" + code);
        	    				if (m == null) {
        	    					warn("Minor " + area + " " + code + " not known.");
        	    					continue;
        	    				}
        	    				StudentAreaClassificationMinor acm = new StudentAreaClassificationMinor();
    	        				acm.setAcademicArea(a);
    	        				acm.setAcademicClassification(f);
    	        				acm.setMinor(m);
    	        				acm.setStudent(student);
    	        				student.getAreaClasfMinors().add(acm);
    	        				if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
                    	}
        			}
                	for (Iterator i2 = demographicsElement.elementIterator("major"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("code");
    	    			for (Iterator i3 = demographicsElement.elementIterator("acadArea"); i3.hasNext();) {
    	    				Element g = (Element) i3.next();
        	    			String area = g.attributeValue("abbv");
        	    			PosMajor m = code2major.get(area + ":" + code);
        	    			if (m == null) continue;
                			AcademicArea a = abbv2area.get(area);
                			if (a == null) continue;
        	    			String clasf = g.attributeValue("classification");
        	    			AcademicClassification f = code2clasf.get(clasf);
        	    			if (f == null) continue;
        	    			if (sMajors.remove(area + ":" + clasf + ":" + code) == null) {
        	    				StudentAreaClassificationMajor acm = new StudentAreaClassificationMajor();
    	        				acm.setAcademicArea(a);
    	        				acm.setAcademicClassification(f);
    	        				acm.setMajor(m);
    	        				acm.setStudent(student);
    	        				student.getAreaClasfMajors().add(acm);
    	        				if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
    	    			}
                	}
                	for (Iterator i2 = demographicsElement.elementIterator("minor"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("code");
    	    			for (Iterator i3 = demographicsElement.elementIterator("acadArea"); i3.hasNext();) {
    	    				Element g = (Element) i3.next();
        	    			String area = g.attributeValue("abbv");
        	    			PosMinor m = code2minor.get(area + ":" + code);
        	    			if (m == null) continue;
                			AcademicArea a = abbv2area.get(area);
                			if (a == null) continue;
        	    			String clasf = g.attributeValue("classification");
        	    			AcademicClassification f = code2clasf.get(clasf);
        	    			if (f == null) continue;
        	    			if (sMinors.remove(area + ":" + clasf + ":" + code) == null) {
        	    				StudentAreaClassificationMinor acm = new StudentAreaClassificationMinor();
    	        				acm.setAcademicArea(a);
    	        				acm.setAcademicClassification(f);
    	        				acm.setMinor(m);
    	        				acm.setStudent(student);
    	        				student.getAreaClasfMinors().add(acm);
    	        				if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			}
    	    			}
                	}
                	for (StudentAreaClassificationMajor major: sMajors.values()) {
                		student.getAreaClasfMajors().remove(major);
                		getHibSession().delete(major);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}
                	for (StudentAreaClassificationMinor minor: sMinors.values()) {
                		student.getAreaClasfMinors().remove(minor);
                		getHibSession().delete(minor);
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
                            
                            Queue<Element> queue = new LinkedList<Element>(); queue.add(requestElement);
                            Element requestOrAlternativeElement = null; 
                            while ((requestOrAlternativeElement = queue.poll()) != null) {
                                for (Iterator j = requestOrAlternativeElement.elementIterator("alternative"); j.hasNext(); ) {
                                    Element altElement = (Element)j.next();
                                    
                                    CourseOffering altCourse = name2course.get(altElement.attributeValue("subjectArea") + " " + altElement.attributeValue("courseNumber"));
                                    if (altCourse == null)
                                        warn("Course " + altElement.attributeValue("subjectArea") + " " + altElement.attributeValue("courseNumber") + " not found.");
                                    else {
                                    	courses.add(altCourse);
                                    	credits.add(Integer.valueOf(altElement.attributeValue("credit", "0")));
                                    	elements.add(altElement);
                                    }
                                    
                                    queue.add(altElement);
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
            					cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
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
                					
                    	 			Map<String, Set<Class_>> extId2class = course2extId2class.get(co.getUniqueId());
                    	 			Map<String, Set<Class_>> name2class = course2name2class.get(co.getUniqueId());
                    	 			Set<Long> imported = new HashSet<Long>();
                                    for (Iterator k = reqEl.elementIterator("class"); k.hasNext(); ) {
                                        Element classElement = (Element)k.next();
                                        Set<Class_> classes = null;
                                        
                                		String classExternalId  = classElement.attributeValue("externalId");
                                		if (classExternalId != null) {
                                			classes = extId2class.get(classExternalId);
                                			if (classes == null)
                                				classes = name2class.get(classExternalId);
                                		}
                                		
                                		if (classes == null) {
                                    		String type = classElement.attributeValue("type");
                                    		String suffix = classElement.attributeValue("suffix");
                                    		if (type != null && suffix != null)
                                    			classes = name2class.get(type.trim() + " " + suffix);
                                		}
                                		
                                		if (classes == null) {
                                			warn(co.getCourseName() + ": Class " + (classExternalId != null ? classExternalId : classElement.attributeValue("type") + " " + classElement.attributeValue("suffix")) + " not found.");
                                			continue;
                                		}
                                		
                                		CourseRequest request = course2request.get(co.getUniqueId());
                                		if (request != null)
                                			for (Iterator<StudentEnrollmentMessage> l = request.getCourseDemand().getEnrollmentMessages().iterator(); l.hasNext(); ) {
                                				StudentEnrollmentMessage message = l.next();
                                				getHibSession().delete(message);
                                				l.remove();
                                			}

                                		for (Class_ clazz: classes) {
                                			if (!imported.add(clazz.getUniqueId())) continue; // avoid duplicates
                                    		StudentClassEnrollment enrollment = enrollments.remove(new Pair(co.getUniqueId(), clazz.getUniqueId()));
                                    		if (enrollment != null) continue; // enrollment already exists
                                    		
                                    		enrollment = new StudentClassEnrollment();
                                    		enrollment.setStudent(student);
                                    		enrollment.setClazz(clazz);
                                    		enrollment.setCourseOffering(co);
                                    		enrollment.setTimestamp(ts);
                                    		enrollment.setChangedBy(StudentClassEnrollment.SystemChange.IMPORT.toString());
                                    		enrollment.setCourseRequest(request);
                                    		student.getClassEnrollments().add(enrollment);
                                		}
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
            		
            		for (CourseRequest cr: unusedRequests) {
            			cr.getCourseDemand().getCourseRequests().remove(cr);
            			getHibSession().delete(cr);
            		}
            		
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
        int dayCode = 0;
        if (days.contains("Th")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_THU];
        	days = days.replace("Th", "..");
        }
        if (days.contains("R")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_THU];
        	days = days.replace("R", ".");
        }
        if (days.contains("Su")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_SUN];
        	days = days.replace("Su", "..");
        }
        if (days.contains("U")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_SUN];
        	days = days.replace("U", ".");
        }
        if (days.contains("M")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_MON];
        	days = days.replace("M", ".");
        }
        if (days.contains("T")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_TUE];
        	days = days.replace("T", ".");
        }
        if (days.contains("W")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_WED];
        	days = days.replace("W", ".");
        }
        if (days.contains("F")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_FRI];
        	days = days.replace("F", ".");
        }
        if (days.contains("S")) {
        	dayCode += Constants.DAY_CODES[Constants.DAY_SAT];
        	days = days.replace("S", ".");
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
