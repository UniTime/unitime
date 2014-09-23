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

import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;

/**
 * @author Tomas Muller
 */
public class StudentExport extends BaseExport {
	
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("students");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        
	        document.addDocType("students", "-//UniTime//UniTime Students DTD/EN", "http://www.unitime.org/interface/Student.dtd");
	        
	        for (Student student: (List<Student>)getHibSession().createQuery(
	        		"select s from Student s where s.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	
	        	Element studentEl = root.addElement("student");
	        	exportStudent(studentEl, student);
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}

	protected void exportStudent(Element studentEl, Student student) {
    	studentEl.addAttribute("externalId",
    			student.getExternalUniqueId() == null || student.getExternalUniqueId().isEmpty() ? student.getUniqueId().toString() : student.getExternalUniqueId());
    	
    	if (student.getFirstName() != null)
    		studentEl.addAttribute("firstName", student.getFirstName());
    	if (student.getMiddleName() != null)
    		studentEl.addAttribute("middleName", student.getMiddleName());
    	if (student.getLastName() != null)
    		studentEl.addAttribute("lastName", student.getLastName());
    	if (student.getEmail() != null)
    		studentEl.addAttribute("email", student.getEmail());
    	
    	if (!student.getAcademicAreaClassifications().isEmpty()) {
    		Element e = studentEl.addElement("studentAcadAreaClass");
        	for (AcademicAreaClassification aac: student.getAcademicAreaClassifications()) {
        		e.addElement("acadAreaClass")
        			.addAttribute("academicArea", aac.getAcademicArea().getAcademicAreaAbbreviation())
        			.addAttribute("academicClass", aac.getAcademicClassification().getCode());
        	}
    	}
    	
    	if (!student.getPosMajors().isEmpty()) {
    		Element e = studentEl.addElement("studentMajors");
        	for (PosMajor major: student.getPosMajors()) {
        		for (AcademicArea area: major.getAcademicAreas()) {
	        		e.addElement("major").addAttribute("academicArea", area.getAcademicAreaAbbreviation()).addAttribute("code", major.getCode());
        		}
        	}
    	}
    	
    	if (!student.getPosMinors().isEmpty()) {
    		Element e = studentEl.addElement("studentMinors");
        	for (PosMinor minor: student.getPosMinors()) {
        		for (AcademicArea area: minor.getAcademicAreas()) {
	        		e.addElement("minor").addAttribute("academicArea", area.getAcademicAreaAbbreviation()).addAttribute("code", minor.getCode());
        		}
        	}
    	}
    	
    	if (!student.getGroups().isEmpty()) {
    		Element e = studentEl.addElement("studentGroups");
        	for (StudentGroup group: student.getGroups()) {
        		e.addElement("studentGroup").addAttribute("group", group.getGroupAbbreviation());
        	}
    	}
    	
    	if (!student.getAccomodations().isEmpty()) {
    		Element e = studentEl.addElement("studentAccomodations");
        	for (StudentAccomodation accomodation: student.getAccomodations()) {
        		e.addElement("studentAccomodation").addAttribute("accomodation", accomodation.getAbbreviation());
        	}
    	}		
	}

}
