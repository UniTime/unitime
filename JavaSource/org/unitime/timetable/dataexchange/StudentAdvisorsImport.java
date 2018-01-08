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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.StudentDAO;


/**
 * @author Tomas Muller
 */
public class StudentAdvisorsImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("studentAdvisors")) {
        	throw new Exception("Given XML file is not student groups load file.");
        }
        try {
            beginTransaction();
            
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            
            boolean incremental = "true".equalsIgnoreCase(root.attributeValue("incremental", "false"));
	        if (incremental)
	        	info("Incremental mode.");

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if(session == null) {
                throw new Exception("No session found for the given campus, year, and term.");
            }
            
            Map<String, Advisor> id2advisor = new Hashtable<String, Advisor>();
            for (Advisor advisor: (List<Advisor>)getHibSession().createQuery(
            		"from Advisor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	id2advisor.put(advisor.getExternalUniqueId(), advisor);
            }
            
            Map<String, Roles> ref2role = new Hashtable<String, Roles>();
            for (Roles role: Roles.findAll(true, getHibSession())) {
            	ref2role.put(role.getReference(), role);
            }
            
            Map<String, Student> id2student = new Hashtable<String, Student>();
            for (Student student: StudentDAO.getInstance().findBySession(getHibSession(), session.getUniqueId())) {
            	if (student.getExternalUniqueId() != null)
            		id2student.put(student.getExternalUniqueId(), student);
            }
            
            Set<Long> studentIds = new HashSet<Long>();
            
            for (Iterator i = root.elementIterator("studentAdvisor"); i.hasNext(); ) {
                Element element = (Element)i.next();
                
                String externalId = element.attributeValue("externalId");
                
                Advisor advisor = id2advisor.remove(externalId);
                
                if (advisor == null) {
                	advisor = new Advisor();
                	advisor.setSession(session);
                	advisor.setExternalUniqueId(externalId);
                }
                
            	advisor.setStudents(new HashSet<Student>());
                advisor.setFirstName(element.attributeValue("firstName"));
                advisor.setMiddleName(element.attributeValue("middleName"));
                advisor.setLastName(element.attributeValue("lastName"));
                advisor.setEmail(element.attributeValue("email"));
                advisor.setAcademicTitle(element.attributeValue("acadTitle"));
                advisor.setRole(ref2role.get(element.attributeValue("role", "Advisor")));
                if (advisor.getRole() == null) {
            		warn("Advisor role " + element.attributeValue("role", "Advisor") + " does not exist."); continue;
            	}
            	info("Advisor " + (advisor.hasName() ? advisor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle) + " (" + externalId + ")" : externalId) + (advisor.getUniqueId() == null ? " created." : " updated."));
                                
                Element updateStudentsEl = element.element("updateStudents");
                if (updateStudentsEl != null) {
                	Hashtable<String, Student> students = new Hashtable<String, Student>();
            		for (Student s: advisor.getStudents())
            			students.put(s.getExternalUniqueId(), s);
            		for (Iterator j = updateStudentsEl.elementIterator("student"); j.hasNext(); ) {
                        Element studentEl = (Element)j.next();
                        String extId = studentEl.attributeValue("externalId");
                        if (extId == null) {
                        	warn("A student has no external id.");
                        	continue;
                        }
                        if (students.remove(extId) != null) continue;
                        Student student = id2student.get(extId);
                        if (student == null) {
                        	warn("Student " + extId + " does not exist.");
                        	continue;
                        }
            			if (student != null) {
            				advisor.getStudents().add(student);
            				student.getAdvisors().add(advisor);
            				studentIds.add(student.getUniqueId());
            			}
            		}
            		if (!students.isEmpty()) {
            			for (Student student: students.values()) {
            				student.getAdvisors().remove(advisor);
            				studentIds.add(student.getUniqueId());
            			}
            			advisor.getStudents().removeAll(students.values());
            		}
                }
                getHibSession().saveOrUpdate(advisor);
            }
            
            if (!incremental)
                for (Advisor advisor: id2advisor.values()) {
                	info("Advisor " + (advisor.hasName() ? advisor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle) + " (" + advisor.getExternalUniqueId() + ")" : advisor.getExternalUniqueId()) + " deleted.");
                	if (advisor.getStudents() != null)
            			for (Student student: advisor.getStudents()) {
            				studentIds.add(student.getUniqueId());
            				student.getAdvisors().remove(advisor);
            			}
                	getHibSession().delete(advisor);
                }
            
            if (!studentIds.isEmpty()) {
            	StudentSectioningQueue.studentChanged(getHibSession(), null, session.getUniqueId(), studentIds);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
