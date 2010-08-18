package org.unitime.timetable.dataexchange;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;

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
            
            List classes = getHibSession().createQuery(
                    "select distinct c from Class_ as c where " +
                    "c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId").
                    setLong("sessionId",session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
            
            for (Iterator i=classes.iterator();i.hasNext();)
                exportClass(root.addElement("class"), (Class_)i.next(), session);
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    protected void exportClass(Element classElement, Class_ clazz, Session session) {
        classElement.addAttribute("id", clazz.getUniqueId().toString());
        CourseOffering course = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
        classElement.addAttribute("subject", course.getSubjectAreaAbbv());
        classElement.addAttribute("courseNbr", course.getCourseNbr());
        classElement.addAttribute("type", clazz.getItypeDesc().trim());
        classElement.addAttribute("suffix", clazz.getSectionNumberString());
        for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
            Class_ childClazz = (Class_)i.next();
            exportClass(classElement.addElement("class"), childClazz, session);
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
        exportDatePattern(classElement, clazz, session);
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
