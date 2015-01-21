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

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public class LastLikeCourseDemandExport extends BaseExport {

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("lastLikeCourseDemand");
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        root.addAttribute("created", new Date().toString());
	        
	        document.addDocType("lastLikeCourseDemand", "-//UniTime//DTD University Course Timetabling/EN", "http://www.unitime.org/interface/StudentCourse.dtd");
	        
	        String lastExternalId = null;
	        Element studentEl = null;
	        Map<String, CourseOffering> permId2course = new Hashtable<String, CourseOffering>();
	        for (CourseOffering course: (List<CourseOffering>)getHibSession().createQuery(
	        		"from CourseOffering co where co.subjectArea.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	if (course.getPermId() != null)
	        		permId2course.put(course.getPermId(), course);
	        }
	        for (LastLikeCourseDemand demand : (List<LastLikeCourseDemand>)getHibSession().createQuery(
	        		"from LastLikeCourseDemand d where d.subjectArea.session.uniqueId = :sessionId " +
	        		"order by d.student.externalUniqueId, d.priority, d.subjectArea.subjectAreaAbbreviation, d.courseNbr")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
	        	if (!demand.getStudent().getExternalUniqueId().equals(lastExternalId)) {
	        		lastExternalId = demand.getStudent().getExternalUniqueId();
	        		studentEl = root.addElement("student");
	        		studentEl.addAttribute("externalId"	, lastExternalId);
	        	}
	        	Element demandEl = studentEl.addElement("studentCourse");
	        	CourseOffering course = (demand.getCoursePermId() == null ? null : permId2course.get(demand.getCoursePermId()));
	        	if (course == null) {
	        		demandEl.addAttribute("subject", demand.getSubjectArea().getSubjectAreaAbbreviation());
	        		demandEl.addAttribute("courseNumber", demand.getCourseNbr());
	        	} else {
	        		demandEl.addAttribute("subject", course.getSubjectAreaAbbv());
	        		demandEl.addAttribute("courseNumber", course.getCourseNbr());
	        	}
	        }
	        
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}

}
