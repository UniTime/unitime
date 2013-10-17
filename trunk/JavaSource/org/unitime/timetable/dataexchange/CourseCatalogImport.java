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

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon, Tomas Muller
 *
 */
public class CourseCatalogImport extends BaseImport {

	private static final int MIN_CREDIT = 0; 
	private static final int MAX_CREDIT = 16;
	
    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("courseCatalog")) {
        	throw new Exception("Given XML file is not a Course Catalog load file.");
        }
        try {
            beginTransaction();

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
                catalog.setCourseNumber(element.attributeValue("courseNumber"));
                catalog.setApprovalType(element.attributeValue("approvalType"));
                catalog.setExternalUniqueId(element.attributeValue("externalId"));
                catalog.setFractionalCreditAllowed(Boolean.valueOf(element.attributeValue("fractionalCreditAllowed")));
                catalog.setPermanentId(element.attributeValue("permanentId"));
                catalog.setPreviousCourseNumber(element.attributeValue("previousCourseNumber"));
                catalog.setPreviousSubject(element.attributeValue("previousSubject"));
                catalog.setSession(session);
                catalog.setSubject(element.attributeValue("subject"));
                catalog.setTitle(element.attributeValue("title"));

                Element credit = element.element("courseCredit");
                if (credit == null) {
                    error("Course credit not provided for "+catalog.getSubject()+" "+catalog.getCourseNumber()+".");
                    continue;
                }
                catalog.setCreditFormat(credit.attributeValue("creditFormat"));
                catalog.setCreditType(credit.attributeValue("creditType"));
                catalog.setCreditUnitType(credit.attributeValue("creditUnitType"));
                String minCredit = credit.attributeValue("fixedCredit");
                if(minCredit != null) 
                    catalog.setFixedMinimumCredit(Float.parseFloat(minCredit));
                else {
                    minCredit = credit.attributeValue("minimumCredit");
                    if(minCredit != null) 
                        catalog.setFixedMinimumCredit(Float.parseFloat(minCredit));
                    else
                        catalog.setFixedMinimumCredit(new Float(MIN_CREDIT));
                    String maxCredit = credit.attributeValue("maximumCredit");
                    if(maxCredit != null)
                        catalog.setMaximumCredit(Float.parseFloat(maxCredit));
                    else
                        catalog.setMaximumCredit(new Float(MAX_CREDIT));
                }

                getHibSession().saveOrUpdate(catalog);  // to set the uniqueId
                loadCredits(element, catalog);
                getHibSession().saveOrUpdate(catalog);  // to save the subparts
                flushIfNeeded(false);
            }

            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
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
