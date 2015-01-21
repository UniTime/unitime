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
package org.unitime.timetable.test;

import java.util.Date;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.studentsct.StudentSectioningSaver;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.hibernate.Transaction;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao.SessionDAO;


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
    private Hashtable<Long,org.unitime.timetable.model.Student> iStudents = null;
    private Hashtable<Long,CourseOffering> iCourses = null;
    private Hashtable<Long,Class_> iClasses = null;
    private Hashtable<String,org.unitime.timetable.model.CourseRequest> iRequests = null;
    
    private int iInsert = 0;

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
    
    public void flushIfNeeded(org.hibernate.Session hibSession) {
        iInsert++;
        if ((iInsert%1000)==0) {
            hibSession.flush(); hibSession.clear();
        }
    }
    
    public void flush(org.hibernate.Session hibSession) {
        hibSession.flush(); hibSession.clear();
        iInsert=0;
    }

    
    public void saveStudent(org.hibernate.Session hibSession, Student student) {
        org.unitime.timetable.model.Student s = iStudents.get(student.getId());
        if (s==null) {
            sLog.warn("Student "+student.getId()+" not found.");
            return;
        }
        for (Iterator i=s.getClassEnrollments().iterator();i.hasNext();) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            hibSession.delete(sce); i.remove();
        }
        for (Iterator i=s.getWaitlists().iterator();i.hasNext();) {
            WaitList wl = (WaitList)i.next();
            hibSession.delete(wl); i.remove();
        }
        for (Iterator e=student.getRequests().iterator();e.hasNext();) {
            Request request = (Request)e.next();
            Enrollment enrollment = (Enrollment)getAssignment().getValue(request);
            if (request instanceof CourseRequest) {
                CourseRequest courseRequest = (CourseRequest)request;
                if (enrollment==null) {
                    if (courseRequest.isWaitlist() && student.canAssign(getAssignment(), courseRequest)) {
                        WaitList wl = new WaitList();
                        wl.setStudent(s);
                        wl.setCourseOffering(iCourses.get(((Course)courseRequest.getCourses().get(0)).getId()));
                        wl.setTimestamp(new Date());
                        wl.setType(new Integer(0));
                        hibSession.save(wl);
                    }
                } else {
                    org.unitime.timetable.model.CourseRequest cr = iRequests.get(request.getId()+":"+enrollment.getOffering().getId());
                    if (cr==null) continue;
                    for (Iterator i=enrollment.getAssignments().iterator();i.hasNext();) {
                        Section section = (Section)i.next();
                        StudentClassEnrollment sce = new StudentClassEnrollment();
                        sce.setStudent(s);
                        sce.setClazz(iClasses.get(section.getId()));
                        sce.setCourseRequest(cr);
                        sce.setCourseOffering(cr.getCourseOffering());
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
            iClasses = new Hashtable<Long, Class_>();
            for (Iterator i=Class_.findAll(session.getUniqueId()).iterator();i.hasNext();) {
                Class_ clazz = (Class_)i.next();
                iClasses.put(clazz.getUniqueId(),clazz);
            }
            if (iIncludeCourseDemands) {
                iStudents = new Hashtable();
                iCourses = new Hashtable();
                iRequests = new Hashtable();
                for (Iterator i=CourseDemand.findAll(session.getUniqueId()).iterator();i.hasNext();) {
                    CourseDemand demand = (CourseDemand)i.next();
                    iStudents.put(demand.getStudent().getUniqueId(), demand.getStudent());
                    for (Iterator j=demand.getCourseRequests().iterator();j.hasNext();) {
                        org.unitime.timetable.model.CourseRequest request = (org.unitime.timetable.model.CourseRequest)j.next();
                        iRequests.put(demand.getUniqueId()+":"+request.getCourseOffering().getInstructionalOffering().getUniqueId(), request);
                        iCourses.put(request.getCourseOffering().getUniqueId(), request.getCourseOffering());
                    }
                }
                for (Iterator e=getModel().getStudents().iterator();e.hasNext();) {
                    Student student = (Student)e.next();
                    if (student.isDummy()) continue;
                    saveStudent(hibSession, student);
                    flushIfNeeded(hibSession);
                }
                flush(hibSession);
            }
            
            if (iIncludeLastLikeStudents) {
                getModel().computeOnlineSectioningInfos(getAssignment());
                
            	Hashtable<Long, SectioningInfo> infoTable = new Hashtable<Long, SectioningInfo>();
            	List<SectioningInfo> infos = hibSession.createQuery(
            			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
            			.setLong("sessionId", session.getUniqueId())
            			.list();
            	for (SectioningInfo info : infos)
            		infoTable.put(info.getClazz().getUniqueId(), info);

                for (Iterator e=getModel().getOfferings().iterator();e.hasNext();) {
                    Offering offering = (Offering)e.next();
                    for (Iterator f=offering.getConfigs().iterator();f.hasNext();) {
                        Config config = (Config)f.next();
                        for (Iterator g=config.getSubparts().iterator();g.hasNext();) {
                            Subpart subpart = (Subpart)g.next();
                            for (Iterator h=subpart.getSections().iterator();h.hasNext();) {
                                Section section = (Section)h.next();
                                Class_ clazz = iClasses.get(section.getId());
                                if (clazz==null) continue;
                                SectioningInfo info = infoTable.get(section.getId());
                                if (info==null) {
                                    info = new SectioningInfo();
                                    info.setClazz(clazz);
                                }
                                sLog.debug("  -- "+info.getClazz().getClassLabel()+" expects "+info.getNbrExpectedStudents()+", holds "+info.getNbrHoldingStudents()+" of "+section.getLimit());
                                info.setNbrExpectedStudents(section.getSpaceExpected());
                                info.setNbrHoldingStudents(section.getSpaceHeld());
                                hibSession.saveOrUpdate(info);
                                flushIfNeeded(hibSession);
                            }
                        }
                    }
                }
            }

            // Update class enrollments
            /*
            for (Iterator e=getModel().getOfferings().iterator();e.hasNext();) {
                Offering offering = (Offering)e.next();
                for (Iterator f=offering.getConfigs().iterator();f.hasNext();) {
                    Config config = (Config)f.next();
                    for (Iterator g=config.getSubparts().iterator();g.hasNext();) {
                        Subpart subpart = (Subpart)g.next();
                        for (Iterator h=subpart.getSections().iterator();h.hasNext();) {
                            Section section = (Section)h.next();
                            Class_ clazz = iClasses.get(section.getId());
                            if (clazz==null) continue;
                            int enrl = 0;
                            for (Iterator i=section.getEnrollments().iterator();i.hasNext();) {
                            	Enrollment en = (Enrollment)i.next();
                            	if (!en.getStudent().isDummy()) enrl++;
                            }
                            clazz.setEnrollment(enrl);
                            sLog.debug("  -- "+clazz.getClassLabel()+" has an enrollment of "+enrl);
                            hibSession.saveOrUpdate(clazz);
                            flushIfNeeded(hibSession);
                        }
                    }
                }
            }
            */
            
            StudentSectioningQueue.allStudentsChanged(hibSession, null, session.getUniqueId());
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        } finally {
            hibSession.close();
        }        
    }

}
