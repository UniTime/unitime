package org.unitime.timetable.dataexchange;

import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

public class StudentSectioningExport extends BaseExport {
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
	
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("request");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        
	        document.addDocType("request", "-//UniTime//UniTime Student Sectioning DTD/EN", "http://www.unitime.org/interface/StudentSectioning.dtd");
	        
	        for (Student student: (List<Student>)getHibSession().createQuery(
	        		"select s from Student s where s.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	Element studentEl = root.addElement("student");
	        	studentEl.addAttribute("key", student.getExternalUniqueId() == null || student.getExternalUniqueId().isEmpty() ? student.getUniqueId().toString() : student.getExternalUniqueId());
	        	
	        	// Student demographics
	        	Element demographicsEl = studentEl.addElement("updateDemographics");
	        	Element nameEl = demographicsEl.addElement("name");
	        	if (student.getFirstName() != null)
	        		nameEl.addAttribute("first", student.getFirstName());
	        	if (student.getMiddleName() != null)
	        		nameEl.addAttribute("middle", student.getMiddleName());
	        	if (student.getLastName() != null)
	        		nameEl.addAttribute("last", student.getLastName());
	        	for (AcademicAreaClassification aac: student.getAcademicAreaClassifications()) {
	        		Element acadAreaEl = demographicsEl.addElement("acadArea");
	        		acadAreaEl.addAttribute("abbv", aac.getAcademicArea().getAcademicAreaAbbreviation());
	        		acadAreaEl.addAttribute("classification", aac.getAcademicClassification().getCode());
	        		for (PosMajor m: student.getPosMajors()) {
	        			if (m.getAcademicAreas().contains(aac.getAcademicArea()))
	        				acadAreaEl.addElement("major").addAttribute("code", m.getCode());
	        		}
	        		for (PosMinor m: student.getPosMinors()) {
	        			if (m.getAcademicAreas().contains(aac.getAcademicArea()))
	        				acadAreaEl.addElement("minor").addAttribute("code", m.getCode());
	        		}
	        	}
        		for (PosMajor m: student.getPosMajors()) {
        			if (m.getAcademicAreas().isEmpty())
        				demographicsEl.addElement("major").addAttribute("code", m.getCode());
        		}
        		for (PosMinor m: student.getPosMinors()) {
        			if (m.getAcademicAreas().isEmpty())
        				demographicsEl.addElement("minor").addAttribute("code", m.getCode());
        		}
	        	for (StudentGroup group: student.getGroups())
	        		demographicsEl.addElement("groupAffiliation").addAttribute("code", group.getGroupAbbreviation());
	        	for (StudentAccomodation acc: student.getAccomodations())
	        		demographicsEl.addElement("disability").addAttribute("code", acc.getAbbreviation());
	        	
	        	// Course requests
	        	Element requestsEl = studentEl.addElement("updateCourseRequests").addAttribute("commit", "true");
	        	for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
	        		if (cd.getFreeTime() != null) {
	        			Element freeTimeEl = requestsEl.addElement("freeTime");
	        			String days = "";
	        	        for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++) {
	        	        	if ((cd.getFreeTime().getDayCode() & Constants.DAY_CODES[i]) != 0) {
	        	        		days += Constants.DAY_NAMES_SHORT[i];
	        	        	}
	        	        }
	        	        freeTimeEl.addAttribute("days", days);
	        	        freeTimeEl.addAttribute("startTime", startSlot2startTime(cd.getFreeTime().getStartSlot()));
	        	        freeTimeEl.addAttribute("endTime", startSlot2startTime(cd.getFreeTime().getStartSlot() + Constants.SLOT_LENGTH_MIN * cd.getFreeTime().getLength()));
	        	        freeTimeEl.addAttribute("length", String.valueOf(Constants.SLOT_LENGTH_MIN * cd.getFreeTime().getLength()));
	        		}
	        		if (!cd.getCourseRequests().isEmpty()) {
	        			Element courseOfferingEl = null;
	        			boolean first = true;
	        			for (CourseRequest cr: new TreeSet<CourseRequest>(cd.getCourseRequests())) {
	        				courseOfferingEl = (courseOfferingEl == null ? requestsEl.addElement("courseOffering") : courseOfferingEl.addElement("alternative"));
	        				courseOfferingEl.addAttribute("subjectArea", cr.getCourseOffering().getSubjectAreaAbbv());
	        				courseOfferingEl.addAttribute("courseNumber", cr.getCourseOffering().getCourseNbr());
	        				if (first && cd.isWaitlist())
	        					courseOfferingEl.addAttribute("waitlist", "true");
	        				if (first && cd.isAlternative())
	        					courseOfferingEl.addAttribute("alternative", "true");
	        				if (cr.getCredit() != null && cr.getCredit() != 0)
	        					courseOfferingEl.addAttribute("credit", String.valueOf(cr.getCredit()));
	        				for (StudentClassEnrollment enrollment: cr.getClassEnrollments()) {
	        	        		Element classEl = courseOfferingEl.addElement("class");
	        	        		Class_ clazz = enrollment.getClazz();
	        	        		String extId = clazz.getExternalId(cr.getCourseOffering());
	        	        		if (extId != null && !extId.isEmpty())
	        	        			classEl.addAttribute("externalId", extId);
	        	        		classEl.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
	        	        		classEl.addAttribute("suffix", clazz.getSectionNumberString());
	        				}
	        				first = false;
	        			}
	        		}
	        	}
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}
	
    private static String startSlot2startTime(int startSlot) {
        int minHrs = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }

}