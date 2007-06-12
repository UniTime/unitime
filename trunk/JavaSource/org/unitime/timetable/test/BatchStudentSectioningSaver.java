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
package org.unitime.timetable.test;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseDemandDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;

import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.studentsct.StudentSectioningSaver;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

/**
 * @author Tomas Muller
 */
public class BatchStudentSectioningSaver extends StudentSectioningSaver {
    private static Log sLog = LogFactory.getLog(BatchStudentSectioningSaver.class);
    private boolean iIncludeCourseDemands = true;
    private boolean iIncludeLastLikeStudents = true;
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;

    public BatchStudentSectioningSaver(Solver solver) {
        super(solver);
        iIncludeCourseDemands = solver.getProperties().getPropertyBoolean("Load.IncludeCourseDemands", iIncludeCourseDemands);
        iIncludeLastLikeStudents = solver.getProperties().getPropertyBoolean("Load.IncludeLastLikeStudents", iIncludeLastLikeStudents);
        iInitiative = solver.getProperties().getProperty("Data.Initiative");
        iYear = solver.getProperties().getProperty("Data.Year");
        iTerm = solver.getProperties().getProperty("Data.Term");
    }
    
    public void save() throws Exception {
        Session session = Session.getSessionUsingInitiativeYearTerm(iInitiative, iYear, iTerm);
        
        if (session==null) throw new Exception("Session "+iInitiative+" "+iTerm+iYear+" not found!");
        
        save(session);
    }
    
    public static void saveStudent(org.hibernate.Session hibSession, Student student) {
        org.unitime.timetable.model.Student s = new StudentDAO().get(new Long(student.getId()));
        for (Iterator i=s.getClassEnrollments().iterator();i.hasNext();) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            hibSession.delete(sce); i.remove();
        }
        for (Iterator i=s.getWaitlists().iterator();i.hasNext();) {
            WaitList wl = (WaitList)i.next();
            hibSession.delete(wl); i.remove();
        }
        for (Enumeration e=student.getRequests().elements();e.hasMoreElements();) {
            Request request = (Request)e.nextElement();
            Enrollment enrollment = (Enrollment)request.getAssignment();
            if (request instanceof CourseRequest) {
                CourseRequest courseRequest = (CourseRequest)request;
                if (enrollment==null) {
                    if (courseRequest.isWaitlist() && student.canAssign(courseRequest)) {
                        WaitList wl = new WaitList();
                        wl.setStudent(s);
                        wl.setCourseOffering(new CourseOfferingDAO().get(new Long(((Course)courseRequest.getCourses().firstElement()).getId())));
                        wl.setTimestamp(new Date());
                        wl.setType(new Integer(0));
                        hibSession.save(wl);
                    }
                } else {
                    CourseDemand cd = new CourseDemandDAO().get(new Long(request.getId()));
                    org.unitime.timetable.model.CourseRequest cr = null;
                    if (cd!=null) {
                        for (Iterator j=cd.getCourseRequests().iterator();j.hasNext();) {
                            org.unitime.timetable.model.CourseRequest x = (org.unitime.timetable.model.CourseRequest)j.next();
                            if (enrollment.getOffering().getId()==x.getCourseOffering().getInstructionalOffering().getUniqueId().longValue()) {
                                cr = x; break;
                            }
                        }
                    }
                    for (Iterator i=enrollment.getAssignments().iterator();i.hasNext();) {
                        Section section = (Section)i.next();
                        StudentClassEnrollment sce = new StudentClassEnrollment();
                        sce.setStudent(s);
                        sce.setClazz(new Class_DAO().get(new Long(section.getId())));
                        sce.setCourseRequest(cr);
                        sce.setTimestamp(new Date());
                        hibSession.save(sce);
                    }
                }
            }
        }
    }    
    
    public void save(Session session) {
        org.hibernate.Session hibSession = new SessionDAO().getSession();
        Transaction tx = hibSession.beginTransaction();
        try {
            if (iIncludeCourseDemands) {
                for (Enumeration e=getModel().getStudents().elements();e.hasMoreElements();) {
                    Student student = (Student)e.nextElement();
                    if (student.isDummy()) continue;
                    saveStudent(hibSession, student);
                }
            }
            
            if (iIncludeLastLikeStudents) {
                getModel().computeOnlineSectioningInfos();
                
                for (Enumeration e=getModel().getOfferings().elements();e.hasMoreElements();) {
                    Offering offering = (Offering)e.nextElement();
                    for (Enumeration f=offering.getConfigs().elements();f.hasMoreElements();) {
                        Config config = (Config)f.nextElement();
                        for (Enumeration g=config.getSubparts().elements();g.hasMoreElements();) {
                            Subpart subpart = (Subpart)g.nextElement();
                            for (Enumeration h=subpart.getSections().elements();h.hasMoreElements();) {
                                Section section = (Section)h.nextElement();
                                Class_ clazz = new Class_DAO().get(new Long(section.getId()));
                                SectioningInfo info = clazz.getSectioningInfo();
                                if (info==null) {
                                    info = new SectioningInfo();
                                    info.setClazz(clazz);
                                }
                                info.setNbrExpectedStudents(section.getSpaceExpected());
                                info.setNbrHoldingStudents(section.getSpaceHeld());
                                sLog.debug("  -- "+info.getClazz().getClassLabel()+" expects "+info.getNbrExpectedStudents()+", holds "+info.getNbrHoldingStudents()+" of "+section.getLimit());
                                hibSession.saveOrUpdate(info);
                                
                            }
                        }
                    }
                }
            }
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        } finally {
            hibSession.close();
        }        
    }

}
