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

import java.text.ParseException;
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
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.AdvisorClassPref;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorInstrMthPref;
import org.unitime.timetable.model.AdvisorSectioningPref;
import org.unitime.timetable.model.Campus;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Degree;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Program;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * 
 * @author Tomas Muller
 *
 */
public class StudentSectioningImport extends BaseImport {
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
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
	            	Element addCoursesElement = studentElement.element("addCourseRequests");
	            	if (addCoursesElement != null && "true".equals(addCoursesElement.attributeValue("commit", "true")))
	            		for (Iterator j = addCoursesElement.elementIterator("courseOffering"); j.hasNext(); ) {
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
            for (Student student: getHibSession().createQuery(
            		"from Student s where s.session.uniqueId=:sessionId and s.externalUniqueId is not null", Student.class).
                    setParameter("sessionId", session.getUniqueId()).list()) { 
            	students.put(student.getExternalUniqueId(), student);
            }
            
            Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
            for (AcademicArea area: getHibSession().createQuery(
            		"from AcademicArea where session.uniqueId=:sessionId", AcademicArea.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
            }

            Map<String, AcademicClassification> code2clasf = new Hashtable<String, AcademicClassification>();
            for (AcademicClassification clasf: getHibSession().createQuery(
            		"from AcademicClassification where session.uniqueId=:sessionId", AcademicClassification.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	code2clasf.put(clasf.getCode(), clasf);
            }
            
            Map<String, PosMajor> code2major = new Hashtable<String, PosMajor>();
            for (PosMajor major: getHibSession().createQuery(
            		"from PosMajor where session.uniqueId=:sessionId", PosMajor.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	for (AcademicArea area: major.getAcademicAreas())
            		code2major.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);
            }
            
            Map<String, PosMajorConcentration> code2concentration = new Hashtable<String, PosMajorConcentration>();
            for (PosMajorConcentration conc: getHibSession().createQuery(
            		"from PosMajorConcentration where major.session.uniqueId=:sessionId", PosMajorConcentration.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	for (AcademicArea area: conc.getMajor().getAcademicAreas())
            		code2concentration.put(area.getAcademicAreaAbbreviation() + ":" + conc.getMajor().getCode() + ":" + conc.getCode(), conc);
            }
            
            Map<String, Degree> code2degree = new Hashtable<String, Degree>();
            for (Degree deg: getHibSession().createQuery(
            		"from Degree where session.uniqueId=:sessionId", Degree.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	code2degree.put(deg.getReference(), deg);
            }
            
            Map<String, Program> code2program = new Hashtable<String, Program>();
            for (Program prog: getHibSession().createQuery(
            		"from Program where session.uniqueId=:sessionId", Program.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	code2program.put(prog.getReference(), prog);
            }
            
            Map<String, Campus> code2campus = new Hashtable<String, Campus>();
            for (Campus camp: getHibSession().createQuery(
            		"from Campus where session.uniqueId=:sessionId", Campus.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	code2campus.put(camp.getReference(), camp);
            }
            
            Map<String, PosMinor> code2minor = new Hashtable<String, PosMinor>();
            for (PosMinor minor: getHibSession().createQuery(
            		"from PosMinor where session.uniqueId=:sessionId", PosMinor.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	for (AcademicArea area: minor.getAcademicAreas())
            		code2minor.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
            }

            Map<String, StudentGroup> code2group = new Hashtable<String, StudentGroup>();
            for (StudentGroup group: getHibSession().createQuery(
            		"from StudentGroup where session.uniqueId=:sessionId", StudentGroup.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	code2group.put(group.getGroupAbbreviation(), group);
            }
            
            Map<String, StudentAccomodation> code2accomodation = new Hashtable<String, StudentAccomodation>();
            for (StudentAccomodation accomodation: getHibSession().createQuery(
            		"from StudentAccomodation where session.uniqueId=:sessionId", StudentAccomodation.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	code2accomodation.put(accomodation.getAbbreviation(), accomodation);
            }
            
            Map<String, CourseOffering> name2course = new Hashtable<String, CourseOffering>();
            for (CourseOffering course: getHibSession().createQuery(
            		"from CourseOffering where subjectArea.session.uniqueId=:sessionId", CourseOffering.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	name2course.put(course.getCourseName(), course);
            }
            
	    	HashMap<Long, Map<String, Set<Class_>>> course2extId2class = new HashMap<Long, Map<String, Set<Class_>>>();
	    	HashMap<Long, Map<String, Set<Class_>>> course2name2class = new HashMap<Long, Map<String, Set<Class_>>>();
	    	info("Loading classes...");
	 		for (Object[] o: getHibSession().createQuery(
	 				"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
    				"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId", Object[].class)
    				.setParameter("sessionId", session.getUniqueId()).list()) {
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
				String name = clazz.getSchedulingSubpart().getItypeDesc().trim() + " " + getClassSuffix(clazz);
				Set<Class_> sameNameClasses = name2class.get(name);
				if (sameNameClasses == null) {
					sameNameClasses = new HashSet<Class_>();
					name2class.put(name, sameNameClasses);
				}
				sameNameClasses.add(clazz);
			}
	 		
	 		Map<String, InstructionalMethod> ref2im = new HashMap<String, InstructionalMethod>();
	 		Map<String, InstructionalMethod> name2im = new HashMap<String, InstructionalMethod>();
	 		for (InstructionalMethod meth: InstructionalMethodDAO.getInstance().findAll(getHibSession())) {
	 			ref2im.put(meth.getReference(), meth);
	 			name2im.put(meth.getLabel(), meth);
	 		}
	 		
	 		Map<String, StudentSectioningStatus> ref2status = new HashMap<String, StudentSectioningStatus>();
	 		for (StudentSectioningStatus status: StudentSectioningStatus.findAll(getHibSession(), session.getUniqueId()))
	 			ref2status.put(status.getReference(), status);
            
            Set<Long> updatedStudents = new HashSet<Long>();
            List<Student> createdStudents = new ArrayList<Student>();
            
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
            				getHibSession().remove(cd.getFreeTime());
            			for (CourseRequest cr: cd.getCourseRequests())
            				getHibSession().remove(cr);
            			i.remove();
            			getHibSession().remove(cd);
            			updatedStudents.add(student.getUniqueId());
            		}
            		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
	        			StudentClassEnrollment enrollment = i.next();
	        			getHibSession().remove(enrollment);
	        			i.remove();
	     	        	updatedStudents.add(student.getUniqueId());
	        		}

            		boolean delete = "true".equals(cancelElement.attributeValue("delete", "false"));
	            	if (delete) {
	            		updatedStudents.add(student.getUniqueId());
	            		getHibSession().remove(student);
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
		            student.setAdvisorCourseRequests(new HashSet<AdvisorCourseRequest>());
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
		            Element pin = demographicsElement.element("pin");
		            if (pin != null) {
		            	student.setPin(pin.attributeValue("value"));
		            	String pinReleased = pin.attributeValue("released");
		            	if ("true".equalsIgnoreCase(pinReleased))
		                	student.setPinReleased(true);
		                else if ("false".equalsIgnoreCase(pinReleased))
		                	student.setPinReleased(false);
		            }
		            student.setAreaClasfMajors(new HashSet<StudentAreaClassificationMajor>());
		            student.setAreaClasfMinors(new HashSet<StudentAreaClassificationMinor>());
		            student.setGroups(new HashSet<StudentGroup>());
		            student.setAccomodations(new HashSet<StudentAccomodation>());
		        	String minCred = demographicsElement.attributeValue("minCredit");
		        	student.setMinCredit(minCred == null ? null : Float.valueOf(minCred));
		            String maxCred = demographicsElement.attributeValue("maxCredit");
		            student.setMaxCredit(maxCred == null ? null : Float.valueOf(maxCred));
		            getHibSession().persist(student);
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
		        	String minCred = demographicsElement.attributeValue("minCredit");
		            if (!eq(minCred == null ? null : Float.valueOf(minCred), student.getMinCredit())) {
		        		student.setMinCredit(minCred == null ? null : Float.valueOf(minCred));
		        		updatedStudents.add(student.getUniqueId());
		        	}
		            String maxCred = demographicsElement.attributeValue("maxCredit");
		        	if (!eq(maxCred == null ? null : Float.valueOf(maxCred), student.getMaxCredit())) {
		        		student.setMaxCredit(maxCred == null ? null : Float.valueOf(maxCred));
		        		updatedStudents.add(student.getUniqueId());
		        	}
		        	Element pin = demographicsElement.element("pin");
		            if (pin != null) {
		            	if (!eq(pin.attributeValue("value"), student.getPin())) {
		            		student.setPin(pin.attributeValue("value"));
		            		updatedStudents.add(student.getUniqueId());
		            	}
		            	String pinReleased = pin.attributeValue("released");
		            	if (!eq(pinReleased, student.getPinReleased() == null ? null : Boolean.TRUE.equals(student.getPinReleased()) ? "true" : "false")) {
		            		if ("true".equalsIgnoreCase(pinReleased))
		                    	student.setPinReleased(true);
		                    else if ("false".equalsIgnoreCase(pinReleased))
		                    	student.setPinReleased(false);
		                    else
		                    	student.setPinReleased(null);
		            		updatedStudents.add(student.getUniqueId());
		            	}
		            }
            	}
            	if (demographicsElement != null) {
                	Map<String, StudentAreaClassificationMajor> sMajors = new Hashtable<String, StudentAreaClassificationMajor>();
                	for (StudentAreaClassificationMajor major: student.getAreaClasfMajors())
                		sMajors.put(major.getAcademicArea().getAcademicAreaAbbreviation() + ":" + major.getAcademicClassification().getCode() + ":" + major.getMajor().getCode()
                				+ (major.getConcentration() == null ? "" : ":" + major.getConcentration().getCode()), major);

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
        	    			String concentration = g.attributeValue("concentration");
        	    			String degree = g.attributeValue("degree");
        	    			String program = g.attributeValue("program");
        	    			String camp = g.attributeValue("campus");
        	    			Double weight = Double.valueOf(g.attributeValue("weight", "1.0"));
        	    			StudentAreaClassificationMajor acm = sMajors.remove(area + ":" + clasf + ":" + code + (concentration == null ? "" : ":" + concentration));
        	    			if (acm != null) {
        	    				acm.setConcentration(concentration == null ? null : code2concentration.get(area + ":" + code + ":" + concentration));
    	        				acm.setDegree(degree == null ? null : code2degree.get(degree));
    	        				acm.setProgram(program == null ? null : code2program.get(program));
    	        				acm.setCampus(camp == null ? null : code2campus.get(camp));
    	        				acm.setWeight(weight);
    	        				if (student.getUniqueId() != null)
                        			updatedStudents.add(student.getUniqueId());
        	    			} else {
        	    				PosMajor m = code2major.get(area + ":" + code);
        	    				if (m == null) {
        	    					warn("Major " + area + " " + code + " not known.");
        	    					continue;
        	    				}
        	    				acm = new StudentAreaClassificationMajor();
    	        				acm.setAcademicArea(a);
    	        				acm.setAcademicClassification(f);
    	        				acm.setMajor(m);
    	        				acm.setStudent(student);
    	        				acm.setConcentration(concentration == null ? null : code2concentration.get(area + ":" + code + ":" + concentration));
    	        				acm.setDegree(degree == null ? null : code2degree.get(degree));
    	        				acm.setProgram(program == null ? null : code2program.get(program));
    	        				acm.setCampus(camp == null ? null : code2campus.get(camp));
    	        				acm.setWeight(weight);
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
                        				m.addToAcademicAreas(a);
                        				m.setExternalUniqueId("-");
                        				m.setCode("-");
                        				m.setName("No Major");
                        				m.setSession(a.getSession());
                        				a.addToPosMajors(m);
                        				getHibSession().persist(m);
                        				code2major.put(area + ":" + code, m);
                        			}
            	    				StudentAreaClassificationMajor acm = new StudentAreaClassificationMajor();
        	        				acm.setAcademicArea(a);
        	        				acm.setAcademicClassification(f);
        	        				acm.setMajor(m);
        	        				acm.setStudent(student);
        	        				acm.setWeight(1.0);
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
    	        				acm.setWeight(1.0);
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
                		getHibSession().remove(major);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}
                	for (StudentAreaClassificationMinor minor: sMinors.values()) {
                		student.getAreaClasfMinors().remove(minor);
                		getHibSession().remove(minor);
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
    	    				getHibSession().merge(group);
                    		if (student.getUniqueId() != null)
                    			updatedStudents.add(student.getUniqueId());
    	    			}
            		}
                	for (StudentGroup group: sGroups.values()) {
                		if (group.getExternalUniqueId() == null) continue;
                		student.getGroups().remove(group);
                		group.getStudents().remove(student);
                		getHibSession().merge(group);
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
            	
	            String status = studentElement.attributeValue("status");
	            if (status != null) {
	            	if (status.isEmpty())
	            		student.setSectioningStatus(null);
	            	else {
	            		StudentSectioningStatus s = ref2status.get(status);
	            		if (s != null)
	            			student.setSectioningStatus(s);
	            		else
	            			warn("Student sectioning status " + status + " not found.");
	            	}
	            }
            	
            	if (student.getUniqueId() == null) {
            		createdStudents.add(student);
            		getHibSession().persist(student);
            	} else {
            		getHibSession().merge(student);
            	}
            	
            	Element reqCoursesElement = studentElement.element("updateCourseRequests");
            	Element delCoursesElement = null;
            	boolean updateMode = (reqCoursesElement != null);
            	if (!updateMode) {
            		reqCoursesElement = studentElement.element("addCourseRequests");
            		delCoursesElement = studentElement.element("dropCourseRequests");
            	}
            	if ((reqCoursesElement != null && "true".equals(reqCoursesElement.attributeValue("commit", "true"))) || delCoursesElement != null) {
                	Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
                	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
                		enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
                	}
                	
            		Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
            		int priority = (updateMode ? 0 : remaining.size());
            		Date ts = new Date();
            		Map<Long, CourseRequest> course2request = new HashMap<Long, CourseRequest>();
            		List<CourseRequest> unusedRequests = new ArrayList<CourseRequest>();
            		
            		if (reqCoursesElement != null) for (Iterator i = reqCoursesElement.elementIterator(); i.hasNext(); priority++) {
                        Element requestElement = (Element)i.next();
                        String waitList = requestElement.attributeValue("waitlist");
                        String noSub = requestElement.attributeValue("nosub");
                        String alternative = requestElement.attributeValue("alternative");
                        String critical = requestElement.attributeValue("critical");
                        String criticalOverride = requestElement.attributeValue("criticalOverride");
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
            					String requested = requestElement.attributeValue("requested");
        						if (requested != null) {
        							try {
        								cd.setTimestamp(sDateFormat.parse(requested)); 
        							} catch (ParseException e) {
        								cd.setTimestamp(ts);
        							}
        						} else {
        							cd.setTimestamp(ts);
        						}
            					cd.setCourseRequests(new HashSet<CourseRequest>());
            					cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
            					cd.setStudent(student);
            					student.getCourseDemands().add(cd);
            				}
        					cd.setAlternative("true".equals(alternative));
        					cd.setPriority(priority);
        					if ("true".equals(waitList) && !Boolean.TRUE.equals(cd.getWaitlist())) {
        						String waitlisted = requestElement.attributeValue("waitlisted");
        						if (waitlisted != null) {
        							try {
        								cd.setWaitlistedTimeStamp(sDateFormat.parse(waitlisted)); 
        							} catch (ParseException e) {
        								cd.setWaitlistedTimeStamp(ts);
        							}
        						} else {
        							cd.setWaitlistedTimeStamp(ts);
        						}
        					}
        					cd.setWaitlist("true".equals(waitList));
        					cd.setNoSub("true".equals(noSub));
        					if (critical == null)
        						cd.setCritical(null);
        					else if ("true".equals(critical))
        						cd.setCritical(CourseDemand.Critical.CRITICAL.ordinal());
        					else if ("false".equals(critical))
        						cd.setCritical(CourseDemand.Critical.NORMAL.ordinal());
        					else {
        						for (CourseDemand.Critical c: CourseDemand.Critical.values()) {
        							if (c.name().equalsIgnoreCase(critical) || String.valueOf(c.ordinal()).equals(critical)) {
        								cd.setCritical(c.ordinal());
        								break;
        							}        								
        						}
        					}
        					if (criticalOverride == null)
        						cd.setCriticalOverride(null);
        					else if ("true".equals(criticalOverride))
        						cd.setCriticalOverride(CourseDemand.Critical.CRITICAL.ordinal());
        					else if ("false".equals(criticalOverride))
        						cd.setCriticalOverride(CourseDemand.Critical.NORMAL.ordinal());
        					else {
        						for (CourseDemand.Critical c: CourseDemand.Critical.values()) {
        							if (c.name().equalsIgnoreCase(criticalOverride) || String.valueOf(c.ordinal()).equals(criticalOverride)) {
        								cd.setCriticalOverride(c.ordinal());
        								break;
        							}        								
        						}
        					}
            				Iterator<CourseRequest> requests = new TreeSet<CourseRequest>(cd.getCourseRequests()).iterator();
            				int order = 0;
            				for (CourseOffering co: courses) {
            					CourseRequest cr = null;
            					if (requests.hasNext()) {
            						cr = requests.next();
            						if (cr.getCourseRequestOptions() != null) {
            							for (Iterator<CourseRequestOption> j = cr.getCourseRequestOptions().iterator(); j.hasNext(); )
            								getHibSession().remove(j.next());
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
            					cr.setCourseOffering(co);
            					importPreferences(cr, elements.get(order), co, course2extId2class.get(co.getUniqueId()), course2name2class.get(co.getUniqueId()), ref2im, name2im);
            					cr.setOrder(order++);
            					course2request.put(co.getUniqueId(), cr);
            				}
            				while (requests.hasNext()) {
            					unusedRequests.add(requests.next());
            					requests.remove();
            				}
            				if (cd.getUniqueId() == null)
            					getHibSession().persist(cd);
            				else
            					getHibSession().merge(cd);
            				
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
                                		if (classExternalId != null && extId2class != null) {
                                			classes = extId2class.get(classExternalId);
                                			if (classes == null)
                                				classes = name2class.get(classExternalId);
                                		}
                                		
                                		if (classes == null && name2class != null) {
                                    		String type = classElement.attributeValue("type");
                                    		String suffix = classElement.attributeValue("suffix");
                                    		if (type != null && suffix != null)
                                    			classes = name2class.get(type.trim() + " " + suffix);
                                		}
                                		
                                		if (classes == null && co != null) {
                                			warn(co.getCourseName() + ": Class " + (classExternalId != null ? classExternalId : classElement.attributeValue("type") + " " + classElement.attributeValue("suffix")) + " not found.");
                                			continue;
                                		}
                                		
                                		CourseRequest request = course2request.get(co.getUniqueId());
                                		if (request != null)
                                			for (Iterator<StudentEnrollmentMessage> l = request.getCourseDemand().getEnrollmentMessages().iterator(); l.hasNext(); ) {
                                				StudentEnrollmentMessage message = l.next();
                                				getHibSession().remove(message);
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
                                    		String enrolled = classElement.attributeValue("enrolled");
                    						if (enrolled != null) {
                    							try {
                    								enrollment.setTimestamp(sDateFormat.parse(enrolled)); 
                    							} catch (ParseException e) {
                    								enrollment.setTimestamp(ts);
                    							}
                    						} else {
                    							enrollment.setTimestamp(ts);
                    						}
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
        						if (!updateMode) {
        							TimeLocation free = new TimeLocation(adept.getFreeTime().getDayCode(), adept.getFreeTime().getStartSlot(), adept.getFreeTime().getLength(),
											0, 0.0, 0, null, null, student.getSession().getDefaultDatePattern().getPatternBitSet(), 0);
        							if (!free.hasIntersection(time)) continue;
        						}
        						cd = adept; j.remove(); break;
        					}
        					if (cd == null) {
        						cd = new CourseDemand();
        						String requested = requestElement.attributeValue("requested");
        						if (requested != null) {
        							try {
        								cd.setTimestamp(sDateFormat.parse(requested)); 
        							} catch (ParseException e) {
        								cd.setTimestamp(ts);
        							}
        						} else {
        							cd.setTimestamp(ts);
        						}
        						student.getCourseDemands().add(cd);
        						cd.setStudent(student);
        					}
        					
        					cd.setAlternative("true".equals(alternative));
        					cd.setPriority(priority);
        					if ("true".equals(waitList) && !Boolean.TRUE.equals(cd.getWaitlist())) {
        						String waitlisted = requestElement.attributeValue("waitlisted");
        						if (waitlisted != null) {
        							try {
        								cd.setWaitlistedTimeStamp(sDateFormat.parse(waitlisted)); 
        							} catch (ParseException e) {
        								cd.setWaitlistedTimeStamp(ts);
        							}
        						} else {
        							cd.setWaitlistedTimeStamp(ts);
        						}
        					}
        					cd.setWaitlist("true".equals(waitList));
        					cd.setNoSub("true".equals(noSub));
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
        					if (free.getUniqueId() == null)
            					getHibSession().persist(free);
            				else
            					getHibSession().merge(free);
        					if (cd.getUniqueId() == null)
            					getHibSession().persist(cd);
            				else
            					getHibSession().merge(cd);
                        } else warn("Request element "+requestElement.getName()+" not recognized.");
                    }
            		
        			if (delCoursesElement != null) for (Iterator i = delCoursesElement.elementIterator(); i.hasNext(); priority++) {
                        Element requestElement = (Element)i.next();
                        if (requestElement.getName().equals("courseOffering")) {
                        	
                        	CourseOffering course = name2course.get(requestElement.attributeValue("subjectArea") + " " + requestElement.attributeValue("courseNumber"));
                            if (course == null) {
                                warn("Course " + requestElement.attributeValue("subjectArea") + " " + requestElement.attributeValue("courseNumber") + " not found.");
                                continue;
                            }
                            
            				CourseDemand cd = null;
            				adepts: for (Iterator<CourseDemand> j = remaining.iterator(); j.hasNext(); ) {
            					CourseDemand adept = j.next();
            					if (adept.getFreeTime() != null) continue;
            					for (CourseRequest cr: adept.getCourseRequests())
            						if (cr.getCourseOffering().getUniqueId().equals(course.getUniqueId())) {
            							cd = adept; j.remove();  break adepts;
            						}
            				}
            				if (cd == null) continue;
            				for (CourseRequest cr: cd.getCourseRequests())
            					unusedRequests.add(cr);
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
        						if (!updateMode) {
        							TimeLocation free = new TimeLocation(adept.getFreeTime().getDayCode(), adept.getFreeTime().getStartSlot(), adept.getFreeTime().getLength(),
											0, 0.0, 0, null, null, student.getSession().getDefaultDatePattern().getPatternBitSet(), 0);
        							if (!free.hasIntersection(time)) continue;
        						}
        						cd = adept; j.remove(); break;
        					}
        					
        					if (cd == null) continue;
        					getHibSession().remove(cd.getFreeTime());
                			student.getCourseDemands().remove(cd);
                			getHibSession().remove(cd);
                        } else warn("Request element "+requestElement.getName()+" not recognized.");
                    }
        			
        			if (!updateMode) {
        				for (CourseDemand cd: remaining) {
        					for (CourseRequest cr: cd.getCourseRequests()) {
        						if (course2request.containsKey(cr.getCourseOffering().getUniqueId())) {
        							unusedRequests.add(cr);
        							continue;
        						}
        						course2request.put(cr.getCourseOffering().getUniqueId(), cr);
        						if (mode == EnrollmentMode.IMPORT)
        							for (StudentClassEnrollment e: student.getClassEnrollments())
        								if (e.getCourseOffering().equals(cr.getCourseOffering()))
        									enrollments.remove(new Pair(e.getCourseOffering().getUniqueId(), e.getClazz().getUniqueId()));
        					}
        				}
        				remaining.clear();
        			}
        			
        		    if (mode == EnrollmentMode.DELETE || mode == EnrollmentMode.IMPORT) {
                    	for (StudentClassEnrollment enrl: enrollments.values()) {
                			student.getClassEnrollments().remove(enrl);
                			enrl.getClazz().getStudentEnrollments().remove(enrl);
                			getHibSession().remove(enrl);
                		}
                    } else {
                        for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
                			StudentClassEnrollment enrl = i.next();
            				CourseRequest cr = course2request.get(enrl.getCourseOffering().getUniqueId());
            				if (cr == null) {
            					if (mode == EnrollmentMode.NOCHANGE) {
                					enrl.setCourseRequest(null);
                					getHibSession().merge(enrl);
            					} else {
                    				enrl.getClazz().getStudentEnrollments().remove(enrl);
                    				getHibSession().remove(enrl);
                    				i.remove();
            					}
            				} else {
            					enrl.setCourseRequest(cr);
            					getHibSession().merge(enrl);
            				}
                		}
                    }
            		
            		for (CourseRequest cr: unusedRequests) {
            			CourseDemand cd = cr.getCourseDemand();
            			cd.getCourseRequests().remove(cr);
            			getHibSession().remove(cr);
            			if (cd.getCourseRequests().isEmpty()) {
            				student.getCourseDemands().remove(cd);
                			getHibSession().remove(cd);
            			}
            		}
            		
            		for (CourseDemand cd: remaining) {
            			if (cd.getFreeTime() != null)
            				getHibSession().remove(cd.getFreeTime());
            			for (CourseRequest cr: cd.getCourseRequests())
            				getHibSession().remove(cr);
            			student.getCourseDemands().remove(cd);
            			getHibSession().remove(cd);
            		}
            		
            		if (!updateMode) {
            			priority = 0;
                		for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
                			cd.setPriority(priority++);
                			getHibSession().merge(cd);
                		}
            		}

            		updatedStudents.add(student.getUniqueId());
            		
            		if (student.getWaitListMode() == WaitListMode.WaitList) {
                		student.resetWaitLists(
                				WaitList.WaitListType.XML_IMPORT,
                				StudentClassEnrollment.SystemChange.IMPORT.toString(),
                				ts,
                				getHibSession());
            		}
            	}
            	
            	Element recommendationsEl = studentElement.element("updateAdvisorRecommendations");
            	if (recommendationsEl != null) {
            		Date ts = new Date();
            		Set<AdvisorCourseRequest> remaining = new TreeSet<AdvisorCourseRequest>(student.getAdvisorCourseRequests());
            		String notes = recommendationsEl.attributeValue("notes");
            		if (notes != null) {
            			AdvisorCourseRequest acr = null;
            			for (Iterator<AdvisorCourseRequest> i = remaining.iterator(); i.hasNext(); ) { 
            				AdvisorCourseRequest adept = i.next();
            				if (adept.getPriority() == -1) {
            					acr = adept; i.remove();
            				}
            			}
            			if (acr == null) {
            				acr = new AdvisorCourseRequest();
            				acr.setStudent(student);
            				acr.setPriority(-1);
            				acr.setAlternative(0);
            				acr.setSubstitute(false);
            				String recommended = recommendationsEl.attributeValue("recommended");
    						if (recommended != null) {
    							try {
    								acr.setTimestamp(sDateFormat.parse(recommended)); 
    							} catch (ParseException e) {
    								acr.setTimestamp(ts);
    							}
    						} else {
    							acr.setTimestamp(ts);
    						}
            				acr.setCredit(null);
            				acr.setCritical(0);
            				acr.setChangedBy(StudentClassEnrollment.SystemChange.IMPORT.toString());
            				student.getAdvisorCourseRequests().add(acr);
            			}
            			acr.setNotes(notes);
    					if (acr.getUniqueId() == null)
        					getHibSession().persist(acr);
        				else
        					getHibSession().merge(acr);
            		}
                	int priority = 0;
                	for (Iterator i = recommendationsEl.elementIterator("recommendation"); i.hasNext(); ) { 
                		Element recEl = (Element)i.next();
                		AdvisorCourseRequest acr = null;
            			for (Iterator<AdvisorCourseRequest> j = remaining.iterator(); j.hasNext(); ) { 
            				AdvisorCourseRequest adept = j.next();
            				if (adept.getPriority() == priority && adept.getAlternative() == 0) {
            					acr = adept; j.remove();
            				}
            			}
            			if (acr == null) {
            				acr = new AdvisorCourseRequest();
							acr.setStudent(student);
							acr.setChangedBy(StudentClassEnrollment.SystemChange.IMPORT.toString());
            				String recommended = recEl.attributeValue("recommended");
    						if (recommended != null) {
    							try {
    								acr.setTimestamp(sDateFormat.parse(recommended)); 
    							} catch (ParseException e) {
    								acr.setTimestamp(ts);
    							}
    						} else {
    							acr.setTimestamp(ts);
    						}
							acr.setPriority(priority);
            				acr.setAlternative(0);
            				student.getAdvisorCourseRequests().add(acr);
            			}
            			acr.setSubstitute("true".equalsIgnoreCase(recEl.attributeValue("substitute", "false")));
            			acr.setCredit(recEl.attributeValue("credit"));
            			String wl = recEl.attributeValue("waitlist");
            			if (wl == null)
            				acr.setWaitlist(null);
            			else
            				acr.setWaitlist("true".equalsIgnoreCase(wl));
            			String ns = recEl.attributeValue("nosub");
            			if (ns == null)
            				acr.setNoSub(null);
            			else
            				acr.setNoSub("true".equalsIgnoreCase(ns));
						acr.setNotes(recEl.attributeValue("notes"));
						acr.setCourse(recEl.attributeValue("course"));
						if (recEl.attributeValue("subjectArea") != null) {
							CourseOffering course = name2course.get(recEl.attributeValue("subjectArea") + " " + recEl.attributeValue("courseNumber"));
                            if (course == null)
                                warn("Course " + recEl.attributeValue("subjectArea") + " " + recEl.attributeValue("courseNumber") + " not found.");
                            else {
                            	acr.setCourseOffering(course);
                            	importPreferences(acr, recEl, course, course2extId2class.get(course.getUniqueId()), course2name2class.get(course.getUniqueId()), ref2im, name2im);
                            }
						} else {
							acr.setCourseOffering(null);
							if (acr.getPreferences() != null && !acr.getPreferences().isEmpty())
								acr.getPreferences().clear();
						}
						Element ftEl = recEl.element("freeTime");
						if (ftEl != null) {
							String days = ftEl.attributeValue("days");
                            String startTime = ftEl.attributeValue("startTime");
                            String length = ftEl.attributeValue("length");
                            String endTime = ftEl.attributeValue("endTime");
                            TimeLocation time = makeTime(student.getSession().getDefaultDatePattern(), days, startTime, endTime, length);
                            FreeTime free = acr.getFreeTime();
                            if (free == null) {
                            	free = new FreeTime();
        						acr.setFreeTime(free);
        					}
        					free.setCategory(time.getBreakTime());
        					free.setDayCode(time.getDayCode());
        					free.setStartSlot(time.getStartSlot());
        					free.setLength(time.getLength());
        					free.setSession(student.getSession());
        					free.setName(time.getLongName(true));
        					if (free.getUniqueId() == null)
            					getHibSession().persist(free);
            				else
            					getHibSession().merge(free);

						} else {
							if (acr.getFreeTime() != null) {
								getHibSession().remove(acr.getFreeTime());
								acr.setFreeTime(null);
							}
						}
						String critical = recEl.attributeValue("critical");
						if (critical == null)
    						acr.setCritical(null);
    					else if ("true".equals(critical))
    						acr.setCritical(CourseDemand.Critical.CRITICAL.ordinal());
    					else if ("false".equals(critical))
    						acr.setCritical(CourseDemand.Critical.NORMAL.ordinal());
    					else {
    						for (CourseDemand.Critical c: CourseDemand.Critical.values()) {
    							if (c.name().equalsIgnoreCase(critical) || String.valueOf(c.ordinal()).equals(critical)) {
    								acr.setCritical(c.ordinal());
    								break;
    							}        								
    						}
    					}
    					if (acr.getUniqueId() == null)
        					getHibSession().persist(acr);
        				else
        					getHibSession().merge(acr);
						int alterantive = 1;
						AdvisorCourseRequest parent = acr;
						for (Iterator j = recEl.elementIterator("alternative"); j.hasNext(); ) { 
	                		Element acrEl = (Element)j.next();
	                		acr = null;
	            			for (Iterator<AdvisorCourseRequest> k = remaining.iterator(); k.hasNext(); ) { 
	            				AdvisorCourseRequest adept = k.next();
	            				if (adept.getPriority() == priority && adept.getAlternative() == alterantive) {
	            					acr = adept; k.remove();
	            				}
	            			}
	            			if (acr == null) {
	            				acr = new AdvisorCourseRequest();
								acr.setStudent(student);
								acr.setChangedBy(StudentClassEnrollment.SystemChange.IMPORT.toString());
								String recommended = acrEl.attributeValue("recommended");
	    						if (recommended != null) {
	    							try {
	    								acr.setTimestamp(sDateFormat.parse(recommended)); 
	    							} catch (ParseException e) {
	    								acr.setTimestamp(ts);
	    							}
	    						} else {
	    							acr.setTimestamp(ts);
	    						}
								acr.setPriority(priority);
	            				acr.setAlternative(alterantive);
	            				student.getAdvisorCourseRequests().add(acr);
	            			}
	            			acr.setSubstitute(parent.isSubstitute());
	            			acr.setCredit(null);
							acr.setNotes(null);
							acr.setWaitlist(null);
							acr.setCourse(acrEl.attributeValue("course"));
							if (acrEl.attributeValue("subjectArea") != null) {
								CourseOffering course = name2course.get(acrEl.attributeValue("subjectArea") + " " + acrEl.attributeValue("courseNumber"));
	                            if (course == null)
	                                warn("Course " + acrEl.attributeValue("subjectArea") + " " + acrEl.attributeValue("courseNumber") + " not found.");
	                            else {
	                            	acr.setCourseOffering(course);
	                            	importPreferences(acr, acrEl, course, course2extId2class.get(course.getUniqueId()), course2name2class.get(course.getUniqueId()), ref2im, name2im);
	                            }
							} else {
								acr.setCourseOffering(null);
								if (acr.getPreferences() != null && !acr.getPreferences().isEmpty())
									acr.getPreferences().clear();
							}
							ftEl = acrEl.element("freeTime");
							if (ftEl != null) {
								String days = ftEl.attributeValue("days");
	                            String startTime = ftEl.attributeValue("startTime");
	                            String length = ftEl.attributeValue("length");
	                            String endTime = ftEl.attributeValue("endTime");
	                            TimeLocation time = makeTime(student.getSession().getDefaultDatePattern(), days, startTime, endTime, length);
	                            FreeTime free = acr.getFreeTime();
	                            if (free == null) {
	                            	free = new FreeTime();
	        						acr.setFreeTime(free);
	        					}
	        					free.setCategory(time.getBreakTime());
	        					free.setDayCode(time.getDayCode());
	        					free.setStartSlot(time.getStartSlot());
	        					free.setLength(time.getLength());
	        					free.setSession(student.getSession());
	        					free.setName(time.getLongName(true));
	        					if (free.getUniqueId() == null)
	            					getHibSession().persist(free);
	            				else
	            					getHibSession().merge(free);
	
							} else {
								if (acr.getFreeTime() != null) {
									getHibSession().remove(acr.getFreeTime());
									acr.setFreeTime(null);
								}
							}
							critical = acrEl.attributeValue("critical");
							if (critical == null)
	    						acr.setCritical(null);
	    					else if ("true".equals(critical))
	    						acr.setCritical(CourseDemand.Critical.CRITICAL.ordinal());
	    					else if ("false".equals(critical))
	    						acr.setCritical(CourseDemand.Critical.NORMAL.ordinal());
	    					else {
	    						for (CourseDemand.Critical c: CourseDemand.Critical.values()) {
	    							if (c.name().equalsIgnoreCase(critical) || String.valueOf(c.ordinal()).equals(critical)) {
	    								acr.setCritical(c.ordinal());
	    								break;
	    							}        								
	    						}
	    					}
	            			alterantive ++;
	    					if (acr.getUniqueId() == null)
	        					getHibSession().persist(acr);
	        				else
	        					getHibSession().merge(acr);
						}
            			priority ++;
                	}
                	for (AdvisorCourseRequest acr: remaining) {
            			if (acr.getFreeTime() != null)
            				getHibSession().remove(acr.getFreeTime());
            			student.getAdvisorCourseRequests().remove(acr);
            			getHibSession().remove(acr);
            		}
            	}
            	
            	
            	getHibSession().merge(student);
	        }
	            
            getHibSession().flush();
    		if (!createdStudents.isEmpty())
    			for (Student s: createdStudents)
    				updatedStudents.add(s.getUniqueId());
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
            nrSlots = (((Integer.parseInt(endTime)/100)*60 + Integer.parseInt(endTime)%100) - ((Integer.parseInt(startTime)/100)*60 + Integer.parseInt(startTime)%100)) / Constants.SLOT_LENGTH_MIN;
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
	
	private boolean eq(Float a, Float b) {
		return (a == null ? b == null : a.equals(b));
	}
	
	protected void importPreferences(CourseRequest cr, Element requestEl, CourseOffering course, Map<String, Set<Class_>> extId2class, Map<String, Set<Class_>> name2class, Map<String, InstructionalMethod> ref2im, Map<String, InstructionalMethod> name2im) {
		Element prefEl = requestEl.element("preferences");
		if (cr.getPreferences() == null) {
			cr.setPreferences(new HashSet<StudentSectioningPref>());
		} else {
			for (Iterator<StudentSectioningPref> i = cr.getPreferences().iterator(); i.hasNext(); ) {
				iHibSession.remove(i.next());
				i.remove();
			}
		}
		if (prefEl == null) return;
		Set<Class_> preferredClasses = new HashSet<Class_>();
		Set<Class_> requiredClasses = new HashSet<Class_>();
		for (Iterator i = prefEl.elementIterator("class"); i.hasNext(); ) {
			Element classElement = (Element)i.next();
			Set<Class_> classes = null;
            
    		String classExternalId  = classElement.attributeValue("externalId");
    		if (classExternalId != null && extId2class != null) {
    			classes = extId2class.get(classExternalId);
    			if (classes == null)
    				classes = name2class.get(classExternalId);
    		}
    		
    		if (classes == null && name2class != null) {
        		String type = classElement.attributeValue("type");
        		String suffix = classElement.attributeValue("suffix");
        		if (type != null && suffix != null)
        			classes = name2class.get(type.trim() + " " + suffix);
    		}
    		
    		if (classes == null && course != null) {
    			warn(course.getCourseName() + ": Class " + (classExternalId != null ? classExternalId : classElement.attributeValue("type") + " " + classElement.attributeValue("suffix")) + " not found.");
    			continue;
    		}
    		
    		if ("true".equalsIgnoreCase(classElement.attributeValue("required", "false")))
    			requiredClasses.addAll(classes);
    		else
    			preferredClasses.addAll(classes);
		}
		for (Class_ clazz: preferredClasses) {
			StudentClassPref scp = new StudentClassPref();
			scp.setCourseRequest(cr);
			scp.setClazz(clazz);
			scp.setLabel(clazz.getClassPrefLabel(cr.getCourseOffering()));
			scp.setRequired(false);
			cr.getPreferences().add(scp);
		}
		for (Class_ clazz: requiredClasses) {
			StudentClassPref scp = new StudentClassPref();
			scp.setCourseRequest(cr);
			scp.setClazz(clazz);
			scp.setLabel(clazz.getClassPrefLabel(cr.getCourseOffering()));
			scp.setRequired(true);
			cr.getPreferences().add(scp);
		}
		Set<InstructionalMethod> preferredIMs = new HashSet<InstructionalMethod>();
		Set<InstructionalMethod> requiredIMs = new HashSet<InstructionalMethod>();
		for (Iterator i = prefEl.elementIterator("instructional-method"); i.hasNext(); ) {
			Element imElement = (Element)i.next();
			
			InstructionalMethod meth = null;
			
			String imExternalId = imElement.attributeValue("externalId", imElement.attributeValue("id"));
    		if (imExternalId != null)
    			meth = ref2im.get(imExternalId);
    		
    		if (meth == null) {
    			String imName = imElement.attributeValue("name");
    			if (imName != null)
    				meth = name2im.get(imName);
    		}
    		
    		if (meth == null) {
    			warn(course.getCourseName() + ": Instructional Method " + (imExternalId != null ? imExternalId : imElement.attributeValue("name")) + " not found.");
    			continue;
    		}
    		if ("true".equalsIgnoreCase(imElement.attributeValue("required", "false")))
    			requiredIMs.add(meth);
    		else
    			preferredIMs.add(meth);
		}
		for (InstructionalMethod meth: preferredIMs) {
			StudentInstrMthPref imp = new StudentInstrMthPref();
			imp.setCourseRequest(cr);
			imp.setRequired(false);
			imp.setInstructionalMethod(meth);
			imp.setLabel(meth.getLabel());
			cr.getPreferences().add(imp);
		}
		for (InstructionalMethod meth: requiredIMs) {
			StudentInstrMthPref imp = new StudentInstrMthPref();
			imp.setCourseRequest(cr);
			imp.setRequired(true);
			imp.setInstructionalMethod(meth);
			imp.setLabel(meth.getLabel());
			cr.getPreferences().add(imp);
		}
	}
	
	protected void importPreferences(AdvisorCourseRequest cr, Element requestEl, CourseOffering course, Map<String, Set<Class_>> extId2class, Map<String, Set<Class_>> name2class, Map<String, InstructionalMethod> ref2im, Map<String, InstructionalMethod> name2im) {
		Element prefEl = requestEl.element("preferences");
		if (cr.getPreferences() == null) {
			cr.setPreferences(new HashSet<AdvisorSectioningPref>());
		} else {
			for (Iterator<AdvisorSectioningPref> i = cr.getPreferences().iterator(); i.hasNext(); ) {
				iHibSession.remove(i.next());
				i.remove();
			}
		}
		if (prefEl == null) return;
		Set<Class_> preferredClasses = new HashSet<Class_>();
		Set<Class_> requiredClasses = new HashSet<Class_>();
		for (Iterator i = prefEl.elementIterator("class"); i.hasNext(); ) {
			Element classElement = (Element)i.next();
			Set<Class_> classes = null;
            
    		String classExternalId  = classElement.attributeValue("externalId");
    		if (classExternalId != null && extId2class != null) {
    			classes = extId2class.get(classExternalId);
    			if (classes == null)
    				classes = name2class.get(classExternalId);
    		}
    		
    		if (classes == null && name2class != null) {
        		String type = classElement.attributeValue("type");
        		String suffix = classElement.attributeValue("suffix");
        		if (type != null && suffix != null)
        			classes = name2class.get(type.trim() + " " + suffix);
    		}
    		
    		if (classes == null && course != null) {
    			warn(course.getCourseName() + ": Class " + (classExternalId != null ? classExternalId : classElement.attributeValue("type") + " " + classElement.attributeValue("suffix")) + " not found.");
    			continue;
    		}
    		
    		if ("true".equalsIgnoreCase(classElement.attributeValue("required", "false")))
    			requiredClasses.addAll(classes);
    		else
    			preferredClasses.addAll(classes);
		}
		for (Class_ clazz: preferredClasses) {
			AdvisorClassPref scp = new AdvisorClassPref();
			scp.setCourseRequest(cr);
			scp.setClazz(clazz);
			scp.setLabel(clazz.getClassPrefLabel(cr.getCourseOffering()));
			scp.setRequired(false);
			cr.getPreferences().add(scp);
		}
		for (Class_ clazz: requiredClasses) {
			AdvisorClassPref scp = new AdvisorClassPref();
			scp.setCourseRequest(cr);
			scp.setClazz(clazz);
			scp.setLabel(clazz.getClassPrefLabel(cr.getCourseOffering()));
			scp.setRequired(true);
			cr.getPreferences().add(scp);
		}
		Set<InstructionalMethod> preferredIMs = new HashSet<InstructionalMethod>();
		Set<InstructionalMethod> requiredIMs = new HashSet<InstructionalMethod>();
		for (Iterator i = prefEl.elementIterator("instructional-method"); i.hasNext(); ) {
			Element imElement = (Element)i.next();
			
			InstructionalMethod meth = null;
			
			String imExternalId = imElement.attributeValue("externalId", imElement.attributeValue("id"));
    		if (imExternalId != null)
    			meth = ref2im.get(imExternalId);
    		
    		if (meth == null) {
    			String imName = imElement.attributeValue("name");
    			if (imName != null)
    				meth = name2im.get(imName);
    		}
    		
    		if (meth == null) {
    			warn(course.getCourseName() + ": Instructional Method " + (imExternalId != null ? imExternalId : imElement.attributeValue("name")) + " not found.");
    			continue;
    		}
    		if ("true".equalsIgnoreCase(imElement.attributeValue("required", "false")))
    			requiredIMs.add(meth);
    		else
    			preferredIMs.add(meth);
		}
		for (InstructionalMethod meth: preferredIMs) {
			AdvisorInstrMthPref imp = new AdvisorInstrMthPref();
			imp.setCourseRequest(cr);
			imp.setRequired(false);
			imp.setInstructionalMethod(meth);
			imp.setLabel(meth.getLabel());
			cr.getPreferences().add(imp);
		}
		for (InstructionalMethod meth: requiredIMs) {
			AdvisorInstrMthPref imp = new AdvisorInstrMthPref();
			imp.setCourseRequest(cr);
			imp.setRequired(true);
			imp.setInstructionalMethod(meth);
			imp.setLabel(meth.getLabel());
			cr.getPreferences().add(imp);
		}
	}
}
