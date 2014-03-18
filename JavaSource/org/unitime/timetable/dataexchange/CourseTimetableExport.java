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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public class CourseTimetableExport extends CourseOfferingExport {

    public void saveXml(Document document, Session session, Properties parameters) throws Exception {
        try {
            beginTransaction();
            
            document.addDocType("timetable","-//UniTime//DTD University Course Timetabling/EN","http://www.unitime.org/interface/CourseTimetable.dtd");

            Element root = document.addElement("timetable");
            root.addAttribute("campus", session.getAcademicInitiative());
            root.addAttribute("year", session.getAcademicYear());
            root.addAttribute("term", session.getAcademicTerm());
            root.addAttribute("action", "update");
            root.addAttribute("dateFormat", sDateFormat.toPattern());
            root.addAttribute("timeFormat", sTimeFormat.toPattern());
            root.addAttribute("created", new Date().toString());
            
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
        classElement.addAttribute("suffix", (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
        if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment())
            classElement.addAttribute("limit", "inf");
        else
            classElement.addAttribute("limit", String.valueOf(clazz.getClassLimit()));
        if (clazz.getSchedulePrintNote()!=null)
            classElement.addAttribute("scheduleNote", clazz.getSchedulePrintNote());
        classElement.addAttribute("studentScheduling", clazz.isEnabledForStudentScheduling()?"true":"false");
        classElement.addAttribute("displayInScheduleBook", clazz.isEnabledForStudentScheduling()?"true":"false");
        classElement.addAttribute("controlling", course.isIsControl()?"true":"false");
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
