/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon
 *
 */
public class SessionImport extends BaseImport {

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("session")) {
        	throw new Exception("Given XML file is not a Session load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            DepartmentStatusType statusType = DepartmentStatusType.findByRef("initial");

            for ( Iterator iter = root.elementIterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                String beginDate = element.attributeValue("beginDate");
                if(beginDate == null) {
                    error("Begin date not provided.");
                    continue;
                }
                String endDate = element.attributeValue("endDate");
                if(endDate == null) {
                    error("End date not provided.");
                    continue;
                }
                String classesEnd = element.attributeValue("classesEnd");
                if(classesEnd == null) {
                    error("Classes end date not provided.");
                    continue;
                }
                String examBegin = element.attributeValue("examBegin");
                if (examBegin == null) {
                	examBegin = classesEnd;
                }
                String eventBegin = element.attributeValue("eventBegin");
                if (eventBegin == null) {
                	examBegin = beginDate;
                }
                String eventEnd = element.attributeValue("eventEnd");
                if (eventEnd == null) {
                	eventEnd = endDate;
                }
                Session session = new Session();
                session.setAcademicInitiative(campus);
                session.setAcademicYear(year);
                session.setAcademicTerm(term);
                DateFormat df = new SimpleDateFormat("M/d/y k:m:s");
                session.setSessionBeginDateTime(df.parse(beginDate));
                session.setSessionEndDateTime(df.parse(endDate));
                session.setClassesEndDateTime(df.parse(classesEnd));
                session.setExamBeginDate(df.parse(examBegin));
                session.setEventBeginDate(df.parse(eventBegin));
                session.setEventEndDate(df.parse(eventEnd));
                session.setStatusType(statusType);
                getHibSession().saveOrUpdate(session);
                
                flushIfNeeded(false);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
}
