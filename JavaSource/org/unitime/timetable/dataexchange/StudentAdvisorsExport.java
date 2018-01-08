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

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;

/**
 * @author Tomas Muller
 */
public class StudentAdvisorsExport extends BaseExport {

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("studentAdvisors");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        root.addAttribute("created", new Date().toString());
	        
	        document.addDocType("studentAdvisors", "-//UniTime//UniTime Student Advisors DTD/EN", "http://www.unitime.org/interface/StudentAdvisors.dtd");
	        
	        for (Advisor advisor: (List<Advisor>)getHibSession().createQuery(
	        		"from Advisor a where a.session.uniqueId = :sessionId order by a.lastName, a.firstName, a.externalUniqueId").setLong("sessionId", session.getUniqueId()).list()) {
	        	Element advisorEl = root.addElement("studentAdvisor");
	        	advisorEl.addAttribute("externalId", advisor.getExternalUniqueId());
	        	if (advisor.getFirstName() != null)
	        		advisorEl.addAttribute("firstName", advisor.getFirstName());
	        	if (advisor.getMiddleName() != null)
	        		advisorEl.addAttribute("middleName", advisor.getMiddleName());
	        	if (advisor.getLastName() != null)
	        		advisorEl.addAttribute("lastName", advisor.getLastName());
	        	if (advisor.getAcademicTitle() != null)
	        		advisorEl.addAttribute("acadTitle", advisor.getAcademicTitle());
	        	if (advisor.getEmail() != null)
	        		advisorEl.addAttribute("email", advisor.getEmail());
	        	if (advisor.getRole() != null)
	        		advisorEl.addAttribute("role", advisor.getRole().getReference());

	        	Element updateStudentsEl = advisorEl.addElement("updateStudents");
	        	if (advisor.getStudents() != null)
	        		for (Student student: advisor.getStudents())
	        			if (student.getExternalUniqueId() != null)
	        				updateStudentsEl.addElement("student").addAttribute("externalId", student.getExternalUniqueId());
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}
}
