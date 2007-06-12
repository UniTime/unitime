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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.CourseCatalogDAO;


/**
 * 
 * @author Timothy Almon
 *
 */
public class CourseCatalogImportDAO extends CourseCatalogDAO {

	private static final int MIN_CREDIT = 0; 
	private static final int MAX_CREDIT = 16;
	
	public CourseCatalogImportDAO() {
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

        if (!root.getName().equalsIgnoreCase("courseCatalog")) {
        	throw new Exception("Given XML file is not a Course Catalog load file.");
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
            CourseCatalog catalog = new CourseCatalog();
            catalog.setApprovalType("");
            catalog.setCourseNumber(element.attributeValue("courseNumber"));
            catalog.setDesignatorRequired(Boolean.valueOf(element.attributeValue("designatorRequired")));
            catalog.setExternalUniqueId(element.attributeValue("externalId"));
            catalog.setFractionalCreditAllowed(Boolean.valueOf(element.attributeValue("fractionalCreditAllowed")));
            catalog.setPermanentId(element.attributeValue("permanentId"));
            catalog.setPreviousCourseNumber(element.attributeValue("previousCourseNumber"));
            catalog.setPreviousSubject(element.attributeValue("previousSubject"));
            catalog.setSession(session);
            catalog.setSubject(element.attributeValue("subject"));
            catalog.setTitle(element.attributeValue("title"));

            Element credit = element.element("courseCredit");
            if(credit == null) {
               	throw new Exception("Course credit is required.");
            }
            catalog.setCreditFormat(credit.attributeValue("creditFormat"));
            catalog.setCreditType(credit.attributeValue("creditType"));
            catalog.setCreditUnitType(credit.attributeValue("creditUnitType"));
            String minCredit = credit.attributeValue("fixedCredit");
            if(minCredit != null) {
            	catalog.setFixedMinimumCredit(Float.parseFloat(minCredit));
            }
            else {
                minCredit = credit.attributeValue("minimumCredit");
                if(minCredit != null) {
                	catalog.setFixedMinimumCredit(Float.parseFloat(minCredit));
                }
                else {
                	catalog.setFixedMinimumCredit(new Float(MIN_CREDIT));
                }
                String maxCredit = credit.attributeValue("maximumCredit");
                if(maxCredit != null) {
                	catalog.setMaximumCredit(Float.parseFloat(maxCredit));
                }
                else {
                	catalog.setMaximumCredit(new Float(MAX_CREDIT));
                }
            }

            saveOrUpdate(catalog);	// to set the uniqueId
            loadCredits(element, catalog);
            saveOrUpdate(catalog);	// to save the subparts
        }
        return;
	}

	private void loadCredits(Element course, CourseCatalog catalog) throws Exception {
        for ( Iterator it = course.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            if(element.getName().equals("courseCredit")) continue;
            CourseSubpartCredit credit = new CourseSubpartCredit();
            credit.setCourseCatalog(catalog);
            credit.setCreditFormat(element.attributeValue("creditFormat"));
            credit.setCreditType(element.attributeValue("creditType"));
            credit.setCreditUnitType(element.attributeValue("creditUnitType"));
            String minCredit = element.attributeValue("fixedCredit");
            if(minCredit != null) {
            	credit.setFixedMinimumCredit(Float.parseFloat(minCredit));
            }
            else {
                minCredit = element.attributeValue("minimumCredit");
                if(minCredit != null) {
                	credit.setFixedMinimumCredit(Float.parseFloat(minCredit));
                }
                else {
                	credit.setFixedMinimumCredit(new Float(MIN_CREDIT));
                }
                String maxCredit = element.attributeValue("maximumCredit");
                if(maxCredit != null) {
                	credit.setMaximumCredit(Float.parseFloat(maxCredit));
                }
                else {
                	credit.setMaximumCredit(new Float(MAX_CREDIT));
                }
            }
            credit.setFractionalCreditAllowed(Boolean.valueOf(element.attributeValue("fractionalCreditAllowed")));
            credit.setSubpartId(element.attributeValue("subpartId"));
            catalog.addTosubparts(credit);
        }
	}
}