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
package org.unitime.timetable.solver.studentsct;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.studentsct.StudentSectioningSaver;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.dao.SessionDAO;


/**
 * @author Tomas Muller
 */
public class StudentSectioningDatabaseSaver extends StudentSectioningSaver {
    private static Log sLog = LogFactory.getLog(StudentSectioningDatabaseSaver.class);
    private boolean iIncludeCourseDemands = true;
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private Hashtable<Long,org.unitime.timetable.model.Student> iStudents = null;
    private Hashtable<Long,CourseOffering> iCourses = null;
    private Hashtable<Long,Class_> iClasses = null;
    private Hashtable<String,org.unitime.timetable.model.CourseRequest> iRequests = null;
    private Date iTimeStamp = null;
    private StudentSectioningStatus iStatusToSet = null;
    private boolean iResetStatus = false;
    private boolean iUpdateCourseRequests = true;
    private String iOwnerId = null;
    
    private int iInsert = 0;
    
    private Progress iProgress = null;
	private boolean iProjections = false;

    public StudentSectioningDatabaseSaver(Solver solver) {
        super(solver);
        iIncludeCourseDemands = solver.getProperties().getPropertyBoolean("Load.IncludeCourseDemands", iIncludeCourseDemands);
        iInitiative = solver.getProperties().getProperty("Data.Initiative");
        iYear = solver.getProperties().getProperty("Data.Year");
        iTerm = solver.getProperties().getProperty("Data.Term");
        iProgress = Progress.getInstance(getModel());
        iProjections = "Projection".equals(solver.getProperties().getProperty("StudentSctBasic.Mode", "Initial"));
        iUpdateCourseRequests = solver.getProperties().getPropertyBoolean("Interactive.UpdateCourseRequests", true);
        iOwnerId = solver.getProperties().getProperty("General.OwnerPuid");
    }
    
    public void save() {
    	iProgress.setStatus("Saving solution ...");
        iTimeStamp = new Date();
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        try {
            hibSession = SessionDAO.getInstance().getSession();
            hibSession.setCacheMode(CacheMode.IGNORE);
            hibSession.setHibernateFlushMode(FlushMode.MANUAL);
            
            tx = hibSession.beginTransaction(); 

            Session session = Session.getSessionUsingInitiativeYearTerm(iInitiative, iYear, iTerm);
            
            if (session==null) throw new Exception("Session "+iInitiative+" "+iTerm+iYear+" not found!");
            ApplicationProperties.setSessionId(session.getUniqueId());
                        
            save(session, hibSession);
            
            StudentSectioningQueue.sessionStatusChanged(hibSession, null, session.getUniqueId(), true);
            
            hibSession.flush();
            
            tx.commit(); tx = null;
            
        } catch (Exception e) {
            iProgress.fatal("Unable to save student schedule, reason: "+e.getMessage(),e);
            sLog.error(e.getMessage(),e);
            if (tx != null) tx.rollback();
        } finally {
            // here we need to close the session since this code may run in a separate thread
            if (hibSession!=null && hibSession.isOpen()) hibSession.close();
        }
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
            iProgress.warn("Student "+student.getId()+" not found.");
            return;
        }
        
        if (iStatusToSet != null)
        	s.setSectioningStatus(iStatusToSet);
        else if (iResetStatus)
        	s.setSectioningStatus(null);
        
        for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
            StudentClassEnrollment sce = i.next();
            sce.getClazz().getStudentEnrollments().remove(sce);
            hibSession.remove(sce); i.remove();
        }
        
        if (iUpdateCourseRequests && BatchEnrollStudent.sRequestsChangedStatus.equals(student.getStatus())) {
        	StudentSectioningStatus status = s.getEffectiveStatus();
        	Set<CourseDemand> remaining = new TreeSet<CourseDemand>(s.getCourseDemands());
        	Date ts = new Date();
        	for (Request request: student.getRequests()) {
        		CourseDemand cd = null;
        		for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
        			CourseDemand adept = i.next();
        			if (adept.getUniqueId().equals(request.getId())) {
        				cd = adept; i.remove(); break;
        			}
        		}
        		if (cd != null) {
        			cd.setPriority(request.getPriority());
        			if (status == null || status.hasOption(Option.waitlist)) {
        				if (request instanceof CourseRequest && ((CourseRequest)request).isWaitlist() && !Boolean.TRUE.equals(cd.getWaitlist())) {
        					cd.setWaitlistedTimeStamp(ts);
        					cd.setWaitListSwapWithCourseOffering(null);
        				}
        				cd.setWaitlist(request instanceof CourseRequest && ((CourseRequest)request).isWaitlist());
        				cd.setNoSub(false);
        			} else if (status.hasOption(Option.nosubs)) {
        				cd.setWaitlist(false);
        				cd.setNoSub(request instanceof CourseRequest && ((CourseRequest)request).isWaitlist());
        			} else {
        				cd.setWaitlist(false);
        				cd.setNoSub(false);
        			}
        			cd.setCritical(CourseDemand.Critical.fromRequestPriority(request.getRequestPriority()).ordinal());
        			if (request instanceof CourseRequest)
        				cd.updatePreferences((CourseRequest)request, hibSession);
        			hibSession.merge(cd);
        		} else {
    				cd = new CourseDemand();
					cd.setTimestamp(ts);
					cd.setChangedBy(iOwnerId);
					s.getCourseDemands().add(cd);
					cd.setStudent(s);
					cd.setAlternative(request.isAlternative());
					cd.setCritical(CourseDemand.Critical.fromRequestPriority(request.getRequestPriority()).ordinal());
					cd.setPriority(request.getPriority());
        			if (request instanceof FreeTimeRequest) {
        				FreeTimeRequest ft = (FreeTimeRequest)request;
    					cd.setWaitlist(false);
    					cd.setNoSub(false);
    					FreeTime free = new FreeTime();
    					cd.setFreeTime(free);
    					free.setCategory(0);
    					free.setDayCode(ft.getTime().getDayCode());
    					free.setStartSlot(ft.getTime().getStartSlot());
    					free.setLength(ft.getTime().getLength());
    					free.setSession(s.getSession());
    					free.setName("Free " + ft.getTime().getDayHeader() + " " + ft.getTime().getStartTimeHeader(true) + " - " + ft.getTime().getEndTimeHeader(true));
    					hibSession.persist(free);
        			} else {
        				CourseRequest cr = (CourseRequest)request;
        				if (status == null || status.hasOption(Option.waitlist)) {
        					if (cr.isWaitlist() && !Boolean.TRUE.equals(cd.getWaitlist())) {
        						cd.setWaitlistedTimeStamp(ts);
        						cd.setWaitListSwapWithCourseOffering(null);
        					}
        					cd.setWaitlist(cr.isWaitlist());
        					cd.setNoSub(false);
        				} else if (status.hasOption(Option.nosubs)) {
        					cd.setNoSub(cr.isWaitlist());
        					cd.setWaitlist(false);
        				} else {
        					cd.setWaitlist(false);
        					cd.setNoSub(false);
        				}
        				cd.setCourseRequests(new HashSet<org.unitime.timetable.model.CourseRequest>());
        				cd.setTimestamp(new Date(cr.getTimeStamp()));
        				int order = 0;
        				for (Course course: cr.getCourses()) {
        					CourseOffering co = iCourses.get(course.getId());
        					if (co == null) continue;
        					org.unitime.timetable.model.CourseRequest crq = new org.unitime.timetable.model.CourseRequest();
        					cd.getCourseRequests().add(crq);
        					crq.setCourseDemand(cd);
        					crq.setAllowOverlap(false);
        					crq.setCredit(0);
        					crq.setOrder(order++);
        					crq.setCourseOffering(co);
						}
        				cd.updatePreferences((CourseRequest)request, hibSession);
        			}
        			hibSession.persist(cd);
        			if (cd.getUniqueId() == null) hibSession.flush();
					Long demandId = cd.getUniqueId();
					for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests()) {
	                    iRequests.put(demandId+":"+cr.getCourseOffering().getInstructionalOffering().getUniqueId(), cr);
	                }
        		}
        	}
        	
        	for (CourseDemand cd: remaining) {
				if (cd.getFreeTime() != null)
					hibSession.remove(cd.getFreeTime());
				for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests()) {
					iRequests.remove(cd.getUniqueId() + ":" + cr.getCourseOffering().getInstructionalOffering().getUniqueId());
					hibSession.remove(cr);
				}
				s.getCourseDemands().remove(cd);
				hibSession.remove(cd);
			}
        }
        
        for (Iterator e=student.getRequests().iterator();e.hasNext();) {
            Request request = (Request)e.next();
            Enrollment enrollment = (Enrollment)getAssignment().getValue(request);
            if (enrollment != null && request instanceof CourseRequest) {
                org.unitime.timetable.model.CourseRequest cr = iRequests.get(request.getId()+":"+enrollment.getOffering().getId());
                for (Iterator j=enrollment.getAssignments().iterator();j.hasNext();) {
                    Section section = (Section)j.next();
                    Class_ clazz = iClasses.get(section.getId());
                    if (clazz == null) {
                    	iProgress.warn("Class " + section.getId() + " not found.");
                    	continue;
                    }
                    StudentClassEnrollment sce = new StudentClassEnrollment();
                    sce.setChangedBy(StudentClassEnrollment.SystemChange.BATCH.toString());
                    sce.setStudent(s);
                    sce.setClazz(clazz);
                    if (cr == null) {
                    	CourseOffering co = iCourses.get(enrollment.getCourse().getId());
                    	if (co == null)
                    		co = clazz.getSchedulingSubpart().getControllingCourseOffering();
                    	sce.setCourseOffering(co);
                    } else {
                        sce.setCourseRequest(cr);
                        sce.setCourseOffering(cr.getCourseOffering());
                    }
                    sce.setTimestamp(iTimeStamp);
                    s.getClassEnrollments().add(sce);
                    hibSession.persist(sce);
                }
                if (cr != null)
                	hibSession.merge(cr);
            }
        }
        hibSession.merge(s);
        
        if (s.getWaitListMode() == WaitListMode.WaitList)
            s.resetWaitLists(
        			WaitList.WaitListType.BATCH_SOLVER,
        			iOwnerId,
        			iTimeStamp,
        			hibSession);
    }    
    
    public void save(Session session, org.hibernate.Session hibSession) {
        iClasses = new Hashtable<Long, Class_>();
        setPhase("Loading classes...", 1);
        for (Class_ clazz: hibSession.createQuery(
        		"select distinct c from Class_ c where " +
        		"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId", Class_.class)
        		.setParameter("sessionId", session.getUniqueId()).list()) {
            iClasses.put(clazz.getUniqueId(),clazz);
        }
        incProgress();
        
        if (iIncludeCourseDemands && !iProjections) {
            iCourses = new Hashtable<Long, CourseOffering>();
            setPhase("Loading courses...", 1);
            for (CourseOffering course: hibSession.createQuery(
            		"select distinct c from CourseOffering c where c.subjectArea.session.uniqueId = :sessionId", CourseOffering.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
                iCourses.put(course.getUniqueId(), course);
            }
            incProgress();

            iStudents = new Hashtable<Long, org.unitime.timetable.model.Student>();
            setPhase("Loading students...", 1);
            for (org.unitime.timetable.model.Student student: hibSession.createQuery(
            		"select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd "+
                    "left join fetch cd.courseRequests as cr "+
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch s.waitlists as w " +
            		"where s.session.uniqueId = :sessionId", org.unitime.timetable.model.Student.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	iStudents.put(student.getUniqueId(), student);
            }
            incProgress();
            
            
            iRequests = new Hashtable<String, org.unitime.timetable.model.CourseRequest>();
            setPhase("Loading course demands...", 1);
            for (CourseDemand demand: hibSession.createQuery(
            		"select distinct c from CourseDemand c " +
            		"left join fetch c.courseRequests r " +
            		"left join fetch r.courseOffering as co " +
            		"left join fetch co.instructionalOffering as io " +
            		"where c.student.session.uniqueId=:sessionId", CourseDemand.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
                for (org.unitime.timetable.model.CourseRequest request: demand.getCourseRequests()) {
                    iRequests.put(demand.getUniqueId()+":"+request.getCourseOffering().getInstructionalOffering().getUniqueId(), request);
                }
            }
            incProgress();
            
            setPhase("Saving student enrollments...", getModel().getStudents().size());
            String statusToSet = getSolver().getProperties().getProperty("Save.StudentSectioningStatusToSet");
            if ("Default".equalsIgnoreCase(statusToSet)) {
            	iStatusToSet = null; iResetStatus = true;
            	iProgress.info("Setting student sectioning status to " + (session.getDefaultSectioningStatus() == null ? "System Default (All Enabled)" : "Session Default (" + session.getDefaultSectioningStatus().getLabel() + ")") + ".");
            } else if (statusToSet != null && !statusToSet.isEmpty() && !statusToSet.equals("N/A")) {
            	iStatusToSet = StudentSectioningStatus.getStatus(statusToSet, session.getUniqueId(), hibSession);
            	if (iStatusToSet == null)
            		iProgress.warn("Student sectioning status " + statusToSet + " does not exist.");
            	else
            		iProgress.info("Setting student sectioning status to " + iStatusToSet.getLabel());
            }
            if (iStatusToSet == null && !iResetStatus)
            	iProgress.info("Keeping student sectioning status unchanged.");
            for (Iterator e=getModel().getStudents().iterator();e.hasNext();) {
                Student student = (Student)e.next(); incProgress();
                if (student.isDummy()) continue;
                saveStudent(hibSession, student);
            }
            flush(hibSession);
        }
        
        if (getModel().getNrLastLikeRequests(false) > 0 || iProjections) {
            setPhase("Computing expected/held space for online sectioning...", 0);
            getModel().computeOnlineSectioningInfos(getAssignment());
            incProgress();
            
        	Hashtable<Long, SectioningInfo> infoTable = new Hashtable<Long, SectioningInfo>();
        	List<SectioningInfo> infos = hibSession.createQuery(
        			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId", SectioningInfo.class)
        			.setParameter("sessionId", session.getUniqueId())
        			.list();
        	for (SectioningInfo info : infos)
        		infoTable.put(info.getClazz().getUniqueId(), info);
            
            setPhase("Saving expected/held space for online sectioning...", getModel().getOfferings().size());
            for (Iterator e=getModel().getOfferings().iterator();e.hasNext();) {
                Offering offering = (Offering)e.next(); incProgress();
                if (offering.isDummy()) continue;
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
                            info.setNbrExpectedStudents(section.getSpaceExpected());
                            info.setNbrHoldingStudents(section.getSpaceHeld());
                            if (info.getUniqueId() == null)
                            	hibSession.persist(info);
                            else
                            	hibSession.merge(info);
                            flushIfNeeded(hibSession);
                        }
                    }
                }
            }
        }
        
        // Update class enrollments
        /*
        if (!iProjections) {
            setPhase("Updating enrollment counts...", getModel().getOfferings().size());
            for (Offering offering: getModel().getOfferings()) {
                incProgress();
                for (Config config: offering.getConfigs()) {
                    for (Subpart subpart: config.getSubparts()) {
                        for (Section section: subpart.getSections()) {
                            Class_ clazz = iClasses.get(section.getId());
                            if (clazz==null) continue;
                            int enrl = 0;
                            for (Enrollment en: section.getEnrollments())
                            	if (!en.getStudent().isDummy()) enrl++;
                            clazz.setEnrollment(enrl);
                            hibSession.saveOrUpdate(clazz);
                            flushIfNeeded(hibSession);
                        }
                    }
                }
                for (Course course: offering.getCourses()) {
                	CourseOffering co = iCourses.get(course.getId());
                	if (co == null) continue;
                    int enrl = 0;
                    for (Enrollment en: course.getEnrollments())
                    	if (!en.getStudent().isDummy()) enrl++;
                    co.setEnrollment(enrl);
                    hibSession.saveOrUpdate(co);
                    flushIfNeeded(hibSession);
                }
            }
        }
        */
        
        flush(hibSession);
        
        setPhase("Done",1);incProgress();
    }
    
    protected void checkTermination() {
    	if (getTerminationCondition() != null && !getTerminationCondition().canContinue(getSolution()))
    		throw new RuntimeException("The save was interrupted.");
    }
    
    protected void setPhase(String phase, long progressMax) {
    	checkTermination();
    	iProgress.setPhase(phase, progressMax);
    }
    
    protected void incProgress() {
    	checkTermination();
    	iProgress.incProgress();
    }

}
