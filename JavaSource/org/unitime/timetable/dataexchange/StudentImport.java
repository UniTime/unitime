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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentSectioningQueue;

/**
 * @author Tomas Muller, Timothy Almon
 */
public class StudentImport extends BaseImport {

	public StudentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		try {
			boolean trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
			
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        boolean incremental = "true".equals(rootElement.attributeValue("incremental", "false"));

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null)
	           	throw new Exception("No session found for the given campus, year, and term.");
	        
	        if (incremental) {
	        	info("Incremental mode enabled: only included students will be updated.");
	        } else {
	        	info("Incremental mode disabled: students not included in this file will be deleted.");
	        }

			beginTransaction();
            
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
	        
	        Set<Long> updatedStudents = new HashSet<Long>(); 
	        
	        for (Iterator i1 = rootElement.elementIterator(); i1.hasNext(); ) {
	            Element element = (Element) i1.next();
	            
	            String externalId = element.attributeValue("externalId");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
	            
            	String fName = element.attributeValue("firstName", "Name");
            	String mName = element.attributeValue("middleName");
            	String lName = element.attributeValue("lastName", "Unknown");
            	String email = element.attributeValue("email");

            	Student student = students.remove(externalId);
            	if (student == null) {
            		student = new Student();
	                student.setSession(session);
		            student.setExternalUniqueId(externalId);
		            student.setFreeTimeCategory(0);
		            student.setSchedulePreference(0);
		            student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
		            student.setCourseDemands(new HashSet<CourseDemand>());
		            student.setFirstName(fName);
		            student.setMiddleName(mName);
		            student.setLastName(lName);
		            student.setEmail(email);
		            student.setAcademicAreaClassifications(new HashSet<AcademicAreaClassification>());
		            student.setPosMajors(new HashSet<PosMajor>());
		            student.setPosMinors(new HashSet<PosMinor>());
		            student.setGroups(new HashSet<StudentGroup>());
		            student.setAccomodations(new HashSet<StudentAccomodation>());
            	} else {
                	if (!eq(fName, student.getFirstName())) {
                		student.setFirstName(fName);
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
                	if (!eq(email, student.getEmail())) {
                		student.setEmail(email);
                		updatedStudents.add(student.getUniqueId());
                	}
            	}
            	
            	if (element.element("studentAcadAreaClass") != null) {
                	Map<String, AcademicAreaClassification> sAreaClasf = new Hashtable<String, AcademicAreaClassification>();
                	for (AcademicAreaClassification aac: student.getAcademicAreaClassifications())
                		sAreaClasf.put(aac.getAcademicArea().getAcademicAreaAbbreviation() + ":" + aac.getAcademicClassification().getCode(), aac);
                	for (Iterator i2 = element.element("studentAcadAreaClass").elementIterator("acadAreaClass"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String area = e.attributeValue("academicArea");
    	    			String clasf = e.attributeValue("academicClass");
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
    	            }
                	for (AcademicAreaClassification aac: sAreaClasf.values()) {
                		student.getAcademicAreaClassifications().remove(aac);
                		getHibSession().delete(aac);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}            		
            	}
            	
            	if (element.element("studentMajors") != null) {
                	Map<String, PosMajor> sMajors = new Hashtable<String, PosMajor>();
                	for (PosMajor major: student.getPosMajors())
                		for (AcademicArea area: major.getAcademicAreas())
                			sMajors.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);
                	for (Iterator i2 = element.element("studentMajors").elementIterator("major"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String area = e.attributeValue("academicArea");
    	    			String code = e.attributeValue("code");
    	    			if (sMajors.remove(area + ":" + code) == null) {
    	    				PosMajor major = code2major.get(area + ":" + code);
    	    				if (major == null) {
    	    					warn("Major" + area + " " + code + " not known.");
    	    					continue;
    	    				}
    	    				student.getPosMajors().add(major);
                    		if (student.getUniqueId() != null)
                    			updatedStudents.add(student.getUniqueId());
    	    			}
                	}
                	for (PosMajor major: sMajors.values()) {
                		student.getPosMajors().remove(major);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}            		
            	}
            	
            	if (element.element("studentMinors") != null) {
                	Map<String, PosMinor> sMinors = new Hashtable<String, PosMinor>();
                	for (PosMinor minor: student.getPosMinors())
                		for (AcademicArea area: minor.getAcademicAreas())
                			sMinors.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
                	for (Iterator i2 = element.element("studentMinors").elementIterator("minor"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String area = e.attributeValue("academicArea");
    	    			String code = e.attributeValue("code");
    	    			if (sMinors.remove(area + ":" + code) == null) {
    	    				PosMinor minor = code2minor.get(area + ":" + code);
    	    				if (minor == null) {
    	    					warn("Minor" + area + " " + code + " not known.");
    	    					continue;
    	    				}
    	    				student.getPosMinors().add(minor);
                    		if (student.getUniqueId() != null)
                    			updatedStudents.add(student.getUniqueId());
    	    			}
                	}
                	for (PosMinor minor: sMinors.values()) {
                		student.getPosMinors().remove(minor);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
                	}            		
            	}
            	
            	if (element.element("studentGroups") != null) {
            		Map<String, StudentGroup> sGroups = new Hashtable<String, StudentGroup>();
            		for (StudentGroup group: student.getGroups())
            			sGroups.put(group.getGroupAbbreviation(), group);
            		for (Iterator i2 = element.element("studentGroups").elementIterator("studentGroup"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("group");
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
            	}
            	
            	if (element.element("studentAccomodations") != null) {
            		Map<String, StudentAccomodation> sAccomodations = new Hashtable<String, StudentAccomodation>();
            		for (StudentAccomodation accomodation: student.getAccomodations())
            			sAccomodations.put(accomodation.getAbbreviation(), accomodation);
            		for (Iterator i2 = element.element("studentAccomodations").elementIterator("studentAccomodation"); i2.hasNext();) {
    	    			Element e = (Element) i2.next();
    	    			String code = e.attributeValue("accomodation");
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
	        }

	        if (!incremental)
	 	        for (Student student: students.values()) {
	        		updatedStudents.add(student.getUniqueId());
	        		getHibSession().delete(student);
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
	}
	
	private boolean eq(String a, String b) {
		return (a == null ? b == null : a.equals(b));
	}
}
