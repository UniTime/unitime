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
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;

/**
 * @author Tomas Muller
 */
public class StudentEnrollmentExport extends BaseExport {

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("studentEnrollments");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        document.addDocType("studentEnrollments", "-//UniTime//UniTime Student Enrollments DTD/EN", "http://www.unitime.org/interface/StudentEnrollment.dtd");
	        
	        for (Student student: (List<Student>)getHibSession().createQuery(
	        		"select s from Student s where s.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	if (student.getClassEnrollments().isEmpty()) continue;
	        	Element studentEl = root.addElement("student");
	        	studentEl.addAttribute("externalId",
	        			student.getExternalUniqueId() == null || student.getExternalUniqueId().isEmpty() ? student.getUniqueId().toString() : student.getExternalUniqueId());
	        	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
	        		Element classEl = studentEl.addElement("class");
	        		Class_ clazz = enrollment.getClazz();
	        		CourseOffering course = enrollment.getCourseOffering();
	        		String extId = (course == null ? clazz.getExternalUniqueId() : clazz.getExternalId(course));
	        		if (extId != null && !extId.isEmpty())
	        			classEl.addAttribute("externalId", extId);
	        		if (course != null) {
	        			if (course.getExternalUniqueId() != null && !course.getExternalUniqueId().isEmpty())
	        				classEl.addAttribute("courseId", course.getExternalUniqueId());
	        			classEl.addAttribute("subject", course.getSubjectAreaAbbv());
	        			classEl.addAttribute("courseNbr", course.getCourseNbr());
	        		}
	        		classEl.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
	        		classEl.addAttribute("suffix", clazz.getSectionNumberString());
	        	}
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}

}
