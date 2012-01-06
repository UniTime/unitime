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

public class StudentExport extends BaseExport {
	
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("students");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        
	        for (Student student: (List<Student>)getHibSession().createQuery(
	        		"select s from Student s where s.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	
	        	Element studentEl = root.addElement("student");
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
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}


}
