/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
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
 * @author Timothy Almon, Tomas Muller
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
                String examBegin = element.attributeValue("examBegin", classesEnd);
                String eventBegin = element.attributeValue("eventBegin", beginDate);
                String eventEnd = element.attributeValue("eventEnd", endDate);
                Session session = new Session();
                session.setAcademicInitiative(campus);
                session.setAcademicYear(year);
                session.setAcademicTerm(term);
                DateFormat df = new SimpleDateFormat(root.attributeValue("dateFormat","M/d/y"));
                session.setSessionBeginDateTime(df.parse(beginDate));
                session.setSessionEndDateTime(df.parse(endDate));
                session.setClassesEndDateTime(df.parse(classesEnd));
                session.setExamBeginDate(df.parse(examBegin));
                session.setEventBeginDate(df.parse(eventBegin));
                session.setEventEndDate(df.parse(eventEnd));
                session.setStatusType(statusType);
                session.setLastWeekToEnroll(Integer.valueOf(element.attributeValue("lastWeekToEnroll", "1")));
                session.setLastWeekToChange(Integer.valueOf(element.attributeValue("lastWeekToChange", "1")));
                session.setLastWeekToDrop(Integer.valueOf(element.attributeValue("lastWeekToDrop", "4")));
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
