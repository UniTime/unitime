package org.unitime.timetable.dataexchange;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;

public class StudentImport extends BaseImport {

	public StudentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }

			beginTransaction();
            
            /* 
             * If some records of a table related to students need to be explicitly deleted, 
             * hibernate can also be used to delete them. For instance, the following query 
             * deletes all last-like course demands for given academic session:
             *   
             * delete LastLikeCourseDemand ll where ll.student.uniqueId in
             *      (select s.uniqueId from Student s where s.session.uniqueId=:sessionId)
             */
            
            getHibSession().createQuery("delete Student s where s.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).executeUpdate();
            
            flush(true);
            
	        for ( Iterator it = rootElement.elementIterator(); it.hasNext(); ) {
	            Element element = (Element) it.next();
            	Student student = new Student();
                student.setSession(session);
	            student.setFirstName(element.attributeValue("firstName"));
	            student.setMiddleName(element.attributeValue("middleName"));
	            student.setLastName(element.attributeValue("lastName"));
	            student.setEmail(element.attributeValue("email"));
	            student.setExternalUniqueId(element.attributeValue("externalId"));
	            student.setFreeTimeCategory(new Integer(0));
	            student.setSchedulePreference(new Integer(0));
	            
	            loadAcadAreaClassifications(element, student, session);
	            if(!loadMajors(element, student, session)) continue;
	            loadMinors(element, student, session);
                
                getHibSession().save(student);

	            flushIfNeeded(true);
	        }
            
            commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}
	private boolean loadMajors(Element element, Student student, Session session) throws Exception {
		if(element.element("studentMajors") == null) return false;

		for (Iterator it = element.element("studentMajors").elementIterator("major"); it.hasNext();) {
			Element el = (Element) it.next();
			String code = el.attributeValue("code");
			if(code == null) {
				throw new Exception("Major Code is required.");
			}
			String academicArea = el.attributeValue("academicArea");
			PosMajor major = null;
			if(academicArea != null) {
				major = PosMajor.findByCodeAcadAreaAbbv(session.getSessionId(), code, academicArea);
			}
			else {
				major = PosMajor.findByCode(session.getSessionId(), code);
			}
			if(major != null) {
				student.addToPosMajors(major);
			}
		}
		if(student.getPosMajors() != null) {
			return student.getPosMajors().size() > 0;
		} else
			return false;
		}

	private void loadMinors(Element element, Student student, Session session) throws Exception {
		if(element.element("studentMinors") == null) return;

		for (Iterator it = element.element("studentMinors").elementIterator("minor"); it.hasNext();) {
			Element el = (Element) it.next();
			String code = el.attributeValue("code");
			if(code == null) {
				throw new Exception("Minor Code is required.");
			}
			String academicArea = el.attributeValue("academicArea");
			PosMinor minor = null;
			if(academicArea != null) {
				minor = PosMinor.findByCodeAcadAreaAbbv(session.getSessionId(), code, academicArea);
			}
			else {
				minor = PosMinor.findByCode(session.getSessionId(), code);
			}
			if(minor != null) {
				student.addToPosMinors(minor);
			}
		}
	}

	private void loadAcadAreaClassifications(Element element, Student student, Session session) throws Exception {

		AcademicClassificationDAO acadClassDAO = new AcademicClassificationDAO();

		if(element.element("studentAcadAreaClass") == null) return;

		for (Iterator it = element.element("studentAcadAreaClass").elementIterator("acadAreaClass"); it.hasNext();) {
			Element el = (Element) it.next();
			String abbv = el.attributeValue("academicArea");
			if(abbv == null) {
				throw new Exception("Academic Area is required.");
			}
			AcademicArea acadArea = null;
			acadArea = AcademicArea.findByAbbv(session.getSessionId(), abbv);
			if(acadArea == null) {
				throw new Exception("Academic Area " + abbv + " was not found.");
			}
			String code = el.attributeValue("academicClass");
			if(code == null) {
				throw new Exception("Academic Classification is required.");
			}
			AcademicClassification acadClass = null;
			acadClass = findAcadClass(acadClassDAO, code, session.getSessionId());
			if(acadClass == null) {
				acadClass = findAcadClass(acadClassDAO, "00", session.getSessionId());
				//throw new Exception("Academic Classification " + code + " was not found.");
			}
			AcademicAreaClassification acadAreaClass = new AcademicAreaClassification();
			acadAreaClass.setStudent(student);
			acadAreaClass.setAcademicArea(acadArea);
			acadAreaClass.setAcademicClassification(acadClass);
			student.addToacademicAreaClassifications(acadAreaClass);
		}
	}

	private AcademicClassification findAcadClass(AcademicClassificationDAO acadClassDAO, String code, Long sessionId) {
		List results = acadClassDAO.
			getSession().
			createQuery("select distinct a from AcademicClassification as a where a.code=:code and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("code", code).
			setCacheable(true).list();
		if(results.size() > 0 ) {
			return (AcademicClassification) results.get(0);
		} else {
			return null;
		}
	}

}
