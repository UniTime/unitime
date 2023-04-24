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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Element;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Campus;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.Degree;
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
	        for (Student student: getHibSession().createQuery(
	        		"from Student s where s.session.uniqueId=:sessionId and s.externalUniqueId is not null", Student.class).
                    setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) { 
	        	students.put(student.getExternalUniqueId(), student);
	        }
	        
            Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
            for (AcademicArea area: getHibSession().createQuery(
            		"from AcademicArea where session.uniqueId=:sessionId", AcademicArea.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
            }

            Map<String, AcademicClassification> code2clasf = new Hashtable<String, AcademicClassification>();
            for (AcademicClassification clasf: getHibSession().createQuery(
            		"from AcademicClassification where session.uniqueId=:sessionId", AcademicClassification.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	code2clasf.put(clasf.getCode(), clasf);
            }
            
            Map<String, PosMajor> code2major = new Hashtable<String, PosMajor>();
            for (PosMajor major: getHibSession().createQuery(
            		"from PosMajor where session.uniqueId=:sessionId", PosMajor.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	for (AcademicArea area: major.getAcademicAreas())
            		code2major.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);
            }
            
            Map<String, PosMajorConcentration> code2concentration = new Hashtable<String, PosMajorConcentration>();
            for (PosMajorConcentration conc: getHibSession().createQuery(
            		"from PosMajorConcentration where major.session.uniqueId=:sessionId", PosMajorConcentration.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	for (AcademicArea area: conc.getMajor().getAcademicAreas())
            		code2concentration.put(area.getAcademicAreaAbbreviation() + ":" + conc.getMajor().getCode() + ":" + conc.getCode(), conc);
            }
            
            Map<String, Degree> code2degree = new Hashtable<String, Degree>();
            for (Degree deg: getHibSession().createQuery(
            		"from Degree where session.uniqueId=:sessionId", Degree.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	code2degree.put(deg.getReference(), deg);
            }
            
            Map<String, Program> code2program = new Hashtable<String, Program>();
            for (Program prog: getHibSession().createQuery(
            		"from Program where session.uniqueId=:sessionId", Program.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	code2program.put(prog.getReference(), prog);
            }
            
            Map<String, Campus> code2campus = new Hashtable<String, Campus>();
            for (Campus camp: getHibSession().createQuery(
            		"from Campus where session.uniqueId=:sessionId", Campus.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	code2campus.put(camp.getReference(), camp);
            }
            
            Map<String, PosMinor> code2minor = new Hashtable<String, PosMinor>();
            for (PosMinor minor: getHibSession().createQuery(
            		"from PosMinor where session.uniqueId=:sessionId", PosMinor.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	for (AcademicArea area: minor.getAcademicAreas())
            		code2minor.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
            }

            Map<String, StudentGroup> code2group = new Hashtable<String, StudentGroup>();
            for (StudentGroup group: getHibSession().createQuery(
            		"from StudentGroup where session.uniqueId=:sessionId", StudentGroup.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	code2group.put(group.getGroupAbbreviation(), group);
            }
            
            Map<String, StudentAccomodation> code2accomodation = new Hashtable<String, StudentAccomodation>();
            for (StudentAccomodation accomodation: getHibSession().createQuery(
            		"from StudentAccomodation where session.uniqueId=:sessionId", StudentAccomodation.class)
            		.setParameter("sessionId", session.getUniqueId(), org.hibernate.type.LongType.INSTANCE).list()) {
            	code2accomodation.put(accomodation.getAbbreviation(), accomodation);
            }
	        
	        Set<Long> updatedStudents = new HashSet<Long>(); 
	        
	        for (Iterator i1 = rootElement.elementIterator(); i1.hasNext(); ) {
	            Element element = (Element) i1.next();

	            String externalId = element.attributeValue("externalId");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);

	            importStudent(element, externalId, students, session, updatedStudents,
	            		abbv2area, code2clasf, code2major, code2minor, code2group, code2accomodation, code2concentration, code2degree, code2program, code2campus);
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
	
	protected Student importStudent(Element element, String externalId, Hashtable<String, Student> students, Session session, Set<Long> updatedStudents,
			Map<String, AcademicArea> abbv2area, Map<String, AcademicClassification> code2clasf, Map<String, PosMajor> code2major, Map<String, PosMinor> code2minor,
			Map<String, StudentGroup> code2group, Map<String, StudentAccomodation> code2accomodation, Map<String, PosMajorConcentration> code2conc,
			Map<String, Degree> code2degree, Map<String, Program> code2program, Map<String, Campus> code2campus) {
		
        Student student = updateStudentInfo(element, externalId, students, session, updatedStudents);
		
		updateStudentMajors(element, student, updatedStudents, abbv2area, code2clasf, code2major, code2conc, code2degree, code2program, code2campus);
		
		updateStudentMinors(element, student, updatedStudents, abbv2area, code2clasf, code2minor);

		updateStudentGroups(element, student, updatedStudents, code2group);
    	
    	updateStudentAccomodations(element, student, updatedStudents, code2accomodation);
    	
    	saveOrUpdateStudent(student, updatedStudents);
    	
    	return student;
	}
	
	protected Student updateStudentInfo(Element element, String externalId, Hashtable<String, Student> students, Session session, Set<Long> updatedStudents) {
    	String fName = element.attributeValue("firstName", "Name");
    	String mName = element.attributeValue("middleName");
    	String lName = element.attributeValue("lastName", "Unknown");
    	String email = element.attributeValue("email");
    	String maxCred = element.attributeValue("maxCredit");
    	String minCred = element.attributeValue("minCredit");

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
            student.setAreaClasfMajors(new HashSet<StudentAreaClassificationMajor>());
            student.setAreaClasfMinors(new HashSet<StudentAreaClassificationMinor>());
            student.setGroups(new HashSet<StudentGroup>());
            student.setAccomodations(new HashSet<StudentAccomodation>());
            student.setMinCredit(minCred == null ? null : Float.valueOf(minCred));
            student.setMaxCredit(maxCred == null ? null : Float.valueOf(maxCred));
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
        	if (!eq(minCred == null ? null : Float.valueOf(minCred), student.getMinCredit())) {
        		student.setMinCredit(minCred == null ? null : Float.valueOf(minCred));
        		updatedStudents.add(student.getUniqueId());
        	}
        	if (!eq(maxCred == null ? null : Float.valueOf(maxCred), student.getMaxCredit())) {
        		student.setMaxCredit(maxCred == null ? null : Float.valueOf(maxCred));
        		updatedStudents.add(student.getUniqueId());
        	}
    	}
    	
    	return student;
	}
	
	protected void updateStudentMajors(Element element, Student student, Set<Long> updatedStudents,
			Map<String, AcademicArea> abbv2area, Map<String, AcademicClassification> code2clasf, Map<String, PosMajor> code2major,
			Map<String, PosMajorConcentration> code2conc, Map<String, Degree> code2degree, Map<String, Program> code2program, Map<String, Campus> code2campus) {
		Map<String, Set<String>> area2classifications = new HashMap<String, Set<String>>();
		if (element.element("studentAcadAreaClass") != null) {
			for (Iterator i2 = element.element("studentAcadAreaClass").elementIterator("acadAreaClass"); i2.hasNext();) {
    			Element e = (Element) i2.next();
    			String area = e.attributeValue("academicArea");
    			String clasf = e.attributeValue("academicClass");
    			Set<String> classifications = area2classifications.get(area);
    			if (classifications == null) {
    				classifications = new TreeSet<String>();
    				area2classifications.put(area, classifications);
    			}
    			classifications.add(clasf);
			}
		}
    	Map<String, StudentAreaClassificationMajor> table = new Hashtable<String, StudentAreaClassificationMajor>();
    	for (StudentAreaClassificationMajor acm: student.getAreaClasfMajors())
    		table.put(acm.getAcademicArea().getAcademicAreaAbbreviation() + "|" + acm.getAcademicClassification().getCode() + "|" + acm.getMajor().getCode() + "|" + (acm.getConcentration() == null ? "" : acm.getConcentration().getCode()), acm);
    	if (element.element("studentMajors") != null) {
        	for (Iterator i2 = element.element("studentMajors").elementIterator("major"); i2.hasNext();) {
    			Element e = (Element) i2.next();
    			
    			String area = e.attributeValue("academicArea");
    			AcademicArea a = abbv2area.get(area);
    			if (a == null) {
    				warn("Academic area " + area + " not known.");
					continue;
    			}

    			String code = e.attributeValue("code");
				PosMajor m = code2major.get(area + ":" + code);
				if (m == null) {
					warn("Major " + area + " " + code + " not known.");
					continue;
				}
				
				String concentration = e.attributeValue("concentration");
				String degree = e.attributeValue("degree");
				String program = e.attributeValue("program");
				String campus = e.attributeValue("campus");
				Double weight = Double.valueOf(e.attributeValue("weight", "1.0"));
				
    			String clasf = e.attributeValue("academicClass");
    			if (clasf == null) {
    				Set<String> classifications = area2classifications.get(area);
    				if (classifications != null)
    					for (String cf: classifications) {
    						StudentAreaClassificationMajor acm = table.remove(area + "|" + cf + "|" + code + "|" + (concentration == null ? "" : concentration));
    						if (acm == null) {
    			    			AcademicClassification f = code2clasf.get(cf);
    			    			if (f == null) {
    								warn("Academic classification " + clasf + " not known.");
    								continue;
    							}
    	        				acm = new StudentAreaClassificationMajor();
    	        				acm.setAcademicArea(a);
    	        				acm.setAcademicClassification(f);
    	        				acm.setMajor(m);
    	        				acm.setStudent(student);
    	        				acm.setConcentration(concentration == null ? null : code2conc.get(area + ":" + code + ":" + concentration));
    	        				acm.setDegree(degree == null ? null : code2degree.get(degree));
    	        				acm.setProgram(program == null ? null : code2program.get(program));
    	        				acm.setCampus(campus == null ? null : code2campus.get(campus));
    	        				acm.setWeight(weight);
    	        				student.getAreaClasfMajors().add(acm);
    	                		if (student.getUniqueId() != null)
    	                			updatedStudents.add(student.getUniqueId());
    	    				} else if (!ToolBox.equals(concentration, acm.getConcentration() == null ? null : acm.getConcentration().getCode())
    	    						|| !ToolBox.equals(degree, acm.getDegree() == null ? null : acm.getDegree().getReference())
    	    						|| !ToolBox.equals(program, acm.getProgram() == null ? null : acm.getProgram().getReference())
    	    						|| !ToolBox.equals(campus, acm.getCampus() == null ? null : acm.getCampus().getReference())) {
    	    					acm.setConcentration(concentration == null ? null : code2conc.get(area + ":" + code + ":" + concentration));
    	    					acm.setDegree(degree == null ? null : code2degree.get(degree));
    	    					acm.setProgram(program == null ? null : code2program.get(program));
    	    					acm.setCampus(campus == null ? null : code2campus.get(campus));
    	    					acm.setWeight(weight);
    	    					iHibSession.update(acm);
    	    					if (student.getUniqueId() != null)
    	                			updatedStudents.add(student.getUniqueId());
    	    				} else if (!ToolBox.equals(acm.getWeight(), weight)) {
    	    					acm.setWeight(weight);
    	    					iHibSession.update(acm);
    	    					if (student.getUniqueId() != null)
    	                			updatedStudents.add(student.getUniqueId());
    	    				}
    					}
    			} else {
    				StudentAreaClassificationMajor acm = table.remove(area + "|" + clasf + "|" + code + "|" + (concentration == null ? "" : concentration));
    				if (acm == null) {
		    			AcademicClassification f = code2clasf.get(clasf);
		    			if (f == null) {
							warn("Academic classification " + clasf + " not known.");
							continue;
						}
        				acm = new StudentAreaClassificationMajor();
        				acm.setAcademicArea(a);
        				acm.setAcademicClassification(f);
        				acm.setMajor(m);
        				acm.setStudent(student);
        				acm.setConcentration(concentration == null ? null : code2conc.get(area + ":" + code + ":" + concentration));
        				acm.setDegree(degree == null ? null : code2degree.get(degree));
        				acm.setProgram(program == null ? null : code2program.get(program));
        				acm.setCampus(campus == null ? null : code2campus.get(campus));
        				acm.setWeight(weight);
        				student.getAreaClasfMajors().add(acm);
                		if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
    				} else if (!ToolBox.equals(concentration, acm.getConcentration() == null ? null : acm.getConcentration().getCode())
    						|| !ToolBox.equals(degree, acm.getDegree() == null ? null : acm.getDegree().getReference())
    						|| !ToolBox.equals(program, acm.getProgram() == null ? null : acm.getProgram().getReference())
    						|| !ToolBox.equals(campus, acm.getCampus() == null ? null : acm.getCampus().getReference())) {
    					acm.setConcentration(concentration == null ? null : code2conc.get(area + ":" + code + ":" + concentration));
    					acm.setDegree(degree == null ? null : code2degree.get(degree));
    					acm.setProgram(program == null ? null : code2program.get(program));
    					acm.setCampus(campus == null ? null : code2campus.get(campus));
    					acm.setWeight(weight);
    					iHibSession.update(acm);
    					if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
    				} else if (!ToolBox.equals(acm.getWeight(), weight)) {
    					acm.setWeight(weight);
    					iHibSession.update(acm);
    					if (student.getUniqueId() != null)
                			updatedStudents.add(student.getUniqueId());
    				}
    			}
        	}
    	}
    	for (StudentAreaClassificationMajor acm: student.getAreaClasfMajors()) {
    		Set<String> classifications = area2classifications.get(acm.getAcademicArea().getAcademicAreaAbbreviation());
    		if (classifications != null && !table.containsKey(acm.getAcademicArea().getAcademicAreaAbbreviation() + "|" + acm.getAcademicClassification().getCode() + "|" + acm.getMajor().getCode() + "|" + (acm.getConcentration() == null ? "" : acm.getConcentration().getCode())))
    			classifications.remove(acm.getAcademicClassification().getCode());
    	}
    	if (element.element("studentMinors") != null)
        	for (Iterator i2 = element.element("studentMinors").elementIterator("minor"); i2.hasNext();) {
    			Element e = (Element) i2.next();
    			String area = e.attributeValue("academicArea");
    			String clasf = e.attributeValue("academicClass");
    			if (clasf == null) continue;
    			Set<String> classifications = area2classifications.get(area);
    			if (classifications != null) classifications.remove(clasf);
        	}
    	for (Map.Entry<String, Set<String>> a2c: area2classifications.entrySet()) {
    		String area = a2c.getKey();
    		if (a2c.getValue().isEmpty()) continue;
			AcademicArea a = abbv2area.get(area);
			if (a == null) {
				warn("Academic area " + area + " not known.");
				continue;
			}
    		for (String clasf: a2c.getValue()) {
    			AcademicClassification f = code2clasf.get(clasf);
    			if (f == null) {
					warn("Academic classification " + clasf + " not known.");
					continue;
				}
    			String major = "-";
    			PosMajor m = code2major.get(area + ":" + major);
    			if (m == null) {
    				m = new PosMajor();
    				m.addToacademicAreas(a);
    				m.setExternalUniqueId("-");
    				m.setCode("-");
    				m.setName("No Major");
    				m.setSession(a.getSession());
    				a.addToposMajors(m);
    				getHibSession().saveOrUpdate(m);
    				code2major.put(area + ":" + major, m);
    			}
    			if (table.remove(area + "|" + clasf + "|" + major + "|") == null) {
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
    	for (StudentAreaClassificationMajor acm: table.values()) {
    		student.getAreaClasfMajors().remove(acm);
    		getHibSession().delete(acm);
    		if (student.getUniqueId() != null)
    			updatedStudents.add(student.getUniqueId());
    	}
	}
	
	protected void updateStudentMinors(Element element, Student student, Set<Long> updatedStudents, Map<String, AcademicArea> abbv2area, Map<String, AcademicClassification> code2clasf, Map<String, PosMinor> code2minor) {
		Map<String, Set<String>> area2classifications = new HashMap<String, Set<String>>();
		if (element.element("studentAcadAreaClass") != null) {
			for (Iterator i2 = element.element("studentAcadAreaClass").elementIterator("acadAreaClass"); i2.hasNext();) {
    			Element e = (Element) i2.next();
    			String area = e.attributeValue("academicArea");
    			String clasf = e.attributeValue("academicClass");
    			Set<String> classifications = area2classifications.get(area);
    			if (classifications == null) {
    				classifications = new TreeSet<String>();
    				area2classifications.put(area, classifications);
    			}
    			classifications.add(clasf);
			}
		}
    	Map<String, StudentAreaClassificationMinor> table = new Hashtable<String, StudentAreaClassificationMinor>();
    	for (StudentAreaClassificationMinor acm: student.getAreaClasfMinors())
    		table.put(acm.getAcademicArea().getAcademicAreaAbbreviation() + "|" + acm.getAcademicClassification().getCode() + "|" + acm.getMinor().getCode(), acm);
    	if (element.element("studentMinors") != null) {
        	for (Iterator i2 = element.element("studentMinors").elementIterator("minor"); i2.hasNext();) {
    			Element e = (Element) i2.next();
    			
    			String area = e.attributeValue("academicArea");
    			AcademicArea a = abbv2area.get(area);
    			if (a == null) {
    				warn("Academic area " + area + " not known.");
					continue;
    			}

    			String code = e.attributeValue("code");
				PosMinor m = code2minor.get(area + ":" + code);
				if (m == null) {
					warn("Minor " + area + " " + code + " not known.");
					continue;
				}
				
    			String clasf = e.attributeValue("academicClass");
    			if (clasf == null) {
    				Set<String> classifications = area2classifications.get(area);
    				if (classifications != null)
    					for (String cf: classifications) {
    						if (table.remove(area + "|" + cf + "|" + code) == null) {
    			    			AcademicClassification f = code2clasf.get(cf);
    			    			if (f == null) {
    								warn("Academic classification " + clasf + " not known.");
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
    			} else {
    				if (table.remove(area + "|" + clasf + "|" + code) == null) {
		    			AcademicClassification f = code2clasf.get(clasf);
		    			if (f == null) {
							warn("Academic classification " + clasf + " not known.");
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
    	}
    	for (StudentAreaClassificationMinor acm: table.values()) {
    		student.getAreaClasfMinors().remove(acm);
    		getHibSession().delete(acm);
    		if (student.getUniqueId() != null)
    			updatedStudents.add(student.getUniqueId());
    	}
	}
	
	protected void updateStudentGroups(Element element, Student student, Set<Long> updatedStudents, Map<String, StudentGroup> code2group) {
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
	}
	
	protected void updateStudentAccomodations(Element element, Student student, Set<Long> updatedStudents, Map<String, StudentAccomodation> code2accomodation) {
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
	}
	
	protected void saveOrUpdateStudent(Student student, Set<Long> updatedStudents) {
    	if (student.getUniqueId() == null) {
    		updatedStudents.add((Long)getHibSession().save(student));
    	} else {
    		getHibSession().update(student);
    	}		
	}
	
	protected boolean eq(String a, String b) {
		return (a == null ? b == null : a.equals(b));
	}
	
	protected boolean eq(Float a, Float b) {
		return (a == null ? b == null : a.equals(b));
	}
}
