/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
