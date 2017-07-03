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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public class CourseTimetableExport extends CourseOfferingExport {

    public void saveXml(Document document, Session session, Properties parameters) throws Exception {
        try {
            beginTransaction();
            
            iExportGroupInfos = ApplicationProperty.DataExchangeIncludeStudentGroups.isTrue();
            
            document.addDocType("timetable","-//UniTime//DTD University Course Timetabling/EN","http://www.unitime.org/interface/CourseTimetable.dtd");

            Element root = document.addElement("timetable");
            root.addAttribute("campus", session.getAcademicInitiative());
            root.addAttribute("year", session.getAcademicYear());
            root.addAttribute("term", session.getAcademicTerm());
            root.addAttribute("action", "update");
            root.addAttribute("dateFormat", sDateFormat.toPattern());
            root.addAttribute("timeFormat", sTimeFormat.toPattern());
            root.addAttribute("created", new Date().toString());
            
            if (ApplicationProperty.DataExchangeIncludeMeetings.isTrue()) {
            	iClassEvents = new HashMap<Long, ClassEvent>();
            	for (ClassEvent e: (List<ClassEvent>)getHibSession().createQuery("from ClassEvent e where e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
            			.setLong("sessionId", session.getUniqueId()).list()) {
            		iClassEvents.put(e.getClazz().getUniqueId(), e);
            	}
            	iMeetingLocations = new HashMap<Long, Location>();
                for (Location l: (List<Location>)getHibSession().createQuery("from Location l where l.session.uniqueId = :sessionId")
                		.setLong("sessionId", session.getUniqueId()).list()) {
                	iMeetingLocations.put(l.getPermanentId(), l);
            	}
            }
            
            List<CourseOffering> courses = (List<CourseOffering>)getHibSession().createQuery(
                    "select c from CourseOffering as c where " +
                    "c.subjectArea.session.uniqueId=:sessionId " + 
                    "order by c.subjectArea.subjectAreaAbbreviation, c.courseNbr").
                    setLong("sessionId",session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
            
            for (CourseOffering course: courses) {
            	for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
            		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
            			if (subpart.getParentSubpart() != null) continue;
            			for (Class_ clazz: subpart.getClasses()) {
                            exportClass(root.addElement("class"), clazz, course, session);
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
    
    protected void exportClass(Element classElement, Class_ clazz, CourseOffering course, Session session) {
        classElement.addAttribute("id", clazz.getUniqueId().toString());
        classElement.addAttribute("subject", course.getSubjectArea().getSubjectAreaAbbreviation());
        classElement.addAttribute("courseNbr", course.getCourseNbr());
        classElement.addAttribute("type", clazz.getItypeDesc().trim());
        classElement.addAttribute("suffix", getClassSuffix(clazz));
        if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment())
            classElement.addAttribute("limit", "inf");
        else
            classElement.addAttribute("limit", String.valueOf(clazz.getClassLimit()));
        if (clazz.getSchedulePrintNote()!=null)
            classElement.addAttribute("scheduleNote", clazz.getSchedulePrintNote());
        classElement.addAttribute("studentScheduling", clazz.isEnabledForStudentScheduling()?"true":"false");
        classElement.addAttribute("displayInScheduleBook", clazz.isEnabledForStudentScheduling()?"true":"false");
        classElement.addAttribute("controlling", course.isIsControl()?"true":"false");
        classElement.addAttribute("cancelled", clazz.isCancelled() ? "true" : "false");
        if (clazz.getManagingDept().getSolverGroup() != null)
        	classElement.addAttribute("solver", clazz.getManagingDept().getSolverGroup().getAbbv());
        for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
            Class_ childClazz = (Class_)i.next();
            exportClass(classElement.addElement("class"), childClazz, course, session);
        }
        if (clazz.getCommittedAssignment()!=null)
            exportAssignment(classElement, clazz.getCommittedAssignment(), session);
        else if (clazz.getManagingDept().getSolverGroup()!=null && clazz.getManagingDept().getSolverGroup().getCommittedSolution()!=null) {
            exportArrHours(classElement, clazz, session);
        }
        if (clazz.isDisplayInstructor())
            for (Iterator i=clazz.getClassInstructors().iterator();i.hasNext();) {
                ClassInstructor instructor = (ClassInstructor)i.next(); 
                if (instructor.getInstructor().getExternalUniqueId()!=null)
                    exportInstructor(classElement.addElement("instructor"), instructor, session);
            }
    }

    protected void exportArrHours(Element classElement, Class_ clazz, Session session) {
        exportDatePattern(classElement, clazz.effectiveDatePattern(), session);
        Element arrangeTimeEl = classElement.addElement("arrangeTime");
        if (clazz.getSchedulingSubpart().getMinutesPerWk()!=null && clazz.getSchedulingSubpart().getMinutesPerWk()>0)
            arrangeTimeEl.addAttribute("minPerWeek", clazz.getSchedulingSubpart().getMinutesPerWk().toString());
        exportRequiredRooms(classElement, clazz, session);
    }
    
    public static void main(String[] args) {
        try {
            if (args.length==0)
                args = new String[] {
                    "c:\\test\\timetable.xml",
                    "puWestLafayetteTrdtn",
                    "2007",
                    "Fal"};

            ToolBox.configureLogging();
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
            
            Session session = Session.getSessionUsingInitiativeYearTerm(args[1], args[2], args[3]);
            
            if (session==null) throw new Exception("Session "+args[1]+" "+args[2]+args[3]+" not found!");
            
            new CourseTimetableExport().saveXml(args[0], session, ApplicationProperties.getProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
