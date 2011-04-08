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

public class StudentEnrollmentExport extends BaseExport {

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("studentEnrollments");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        
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
