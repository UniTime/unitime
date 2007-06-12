/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.dataexchange;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.StudentDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class StudentImportDAO extends StudentDAO {

	public StudentImportDAO() {
		super();
	}

	public void loadFromXML(String filename) throws Exception {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);
			loadFromStream(fis);
		} finally {
			if (fis != null) fis.close();
		}
		return;
	}

	public void loadFromStream(FileInputStream fis) throws Exception {

		Document document = (new SAXReader()).read(fis);
        Element root = document.getRootElement();

        if (!root.getName().equalsIgnoreCase("students")) {
        	throw new Exception("Given XML file is not a Student load file.");
        }
        
        String campus = root.attributeValue("campus");
        String year   = root.attributeValue("year");
        String term   = root.attributeValue("term");

        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
        if(session == null) {
           	throw new Exception("No session found for the given campus, year, and term.");
        }

        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            String externalId = element.attributeValue("externalId");
            Student student = null;
            if(externalId != null && externalId.length() > 0) {
            	student = findByExternalId(externalId, session.getSessionId());
            }
            if(student == null) {
            	student = new Student();
                student.setSession(session);
            }
            student.setFirstName(element.attributeValue("firstName"));
            student.setMiddleName(element.attributeValue("middleName"));
            student.setLastName(element.attributeValue("lastName"));
            student.setEmail(element.attributeValue("email"));
            student.setExternalUniqueId(externalId);
            student.setFreeTimeCategory(new Integer(0));
            student.setSchedulePreference(new Integer(0));
            
            loadAcadAreaClassifications(element, student, session);
            loadMajors(element, student, session);
            loadMinors(element, student, session);

            saveOrUpdate(student);
        }
        return;
	}

	private void loadMajors(Element element, Student student, Session session) throws Exception {
		if(element.element("studentMajors") == null) return;

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
			if(major == null) {
				throw new Exception("Major " + code + " was not found.");
			}
			student.addToPosMajors(major);
		}
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
			if(minor == null) {
				throw new Exception("Minor " + code + " was not found.");
			}
			student.addToPosMinors(minor);
		}
	}

	private void loadAcadAreaClassifications(Element element, Student student, Session session) throws Exception {

		AcademicAreaDAO acadAreaDAO = new AcademicAreaDAO();
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

	private Student findByExternalId(String externalId, Long sessionId) {
		return (Student) this.
			getSession().
			createQuery("select distinct a from Student as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setString("externalId", externalId).
			setCacheable(true).
			uniqueResult();
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