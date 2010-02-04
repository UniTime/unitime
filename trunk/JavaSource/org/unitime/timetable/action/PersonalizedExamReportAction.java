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
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.PersonalizedExamReportForm;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.StudentExamReport;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;

public class PersonalizedExamReportAction extends Action {
    public static ExternalUidTranslation sTranslation;
    private static Log sLog = LogFactory.getLog(PersonalizedExamReportAction.class);
    
    static {
        if (ApplicationProperties.getProperty("tmtbl.externalUid.translation")!=null) {
            try {
                sTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperties.getProperty("tmtbl.externalUid.translation")).getConstructor().newInstance();
            } catch (Exception e) { Debug.error("Unable to instantiate external uid translation class, "+e.getMessage()); }
        }
    }
    
    public static String translate(String uid, Source target) {
        if (sTranslation==null || uid==null || target.equals(Source.User)) return uid;
        return sTranslation.translate(uid, Source.User, target);
    }
    
    public static boolean hasPersonalReport(User user) {
        //if (user.getRole()!=null) return false;
        HashSet<Session> sessions = new HashSet();
        DepartmentalInstructor instructor = null;
        for (Iterator i=new DepartmentalInstructorDAO().
                getSession().
                createQuery("select i from DepartmentalInstructor i where i.externalUniqueId=:externalId").
                setString("externalId",user.getId()).
                setCacheable(true).list().iterator();i.hasNext();) {
            DepartmentalInstructor s = (DepartmentalInstructor)i.next();
            if (!canDisplay(s.getDepartment().getSession())) continue;
            sessions.add(s.getDepartment().getSession());
            if (instructor==null || instructor.getDepartment().getSession().compareTo(s.getDepartment().getSession())<0) instructor = s;
        }
        
        Student student = null;
        for (Iterator i=new StudentDAO().
                getSession().
                createQuery("select s from Student s where s.externalUniqueId=:externalId").
                setString("externalId",user.getId()).
                setCacheable(true).list().iterator();i.hasNext();) {
            Student s = (Student)i.next();
            if (!canDisplay(s.getSession())) continue;
            sessions.add(s.getSession());
            if (student==null || student.getSession().compareTo(s.getSession())<0) student = s;
        }
        
        if (instructor==null && student==null) return false;

        return true;
    }
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PersonalizedExamReportForm myForm = (PersonalizedExamReportForm) form;
        
        String back = (String)request.getSession().getAttribute("loginPage");
        if (back==null) back = "back";
        
        User user = Web.getUser(request.getSession());
        if (user==null) {
            sLog.info("User not logged in, forwarding back.");
            request.setAttribute("message", "Login is required.");
            return mapping.findForward(back);
        }
        
        myForm.setAdmin(user.isAdmin());
        myForm.setLogout(!"back".equals(back));
        
        if (myForm.getAdmin() && myForm.getUid()!=null && myForm.getUid().length()>0) {
            user = new User();
            user.setId(myForm.getUid());
            user.setName(
                    (myForm.getLname()==null || myForm.getLname().length()==0?"":" "+Constants.toInitialCase(myForm.getLname()))+
                    (myForm.getFname()==null || myForm.getFname().length()==0?"":" "+myForm.getFname().substring(0,1).toUpperCase())+
                    (myForm.getMname()==null || myForm.getMname().length()==0?"":" "+myForm.getMname().substring(0,1).toUpperCase()));
        }
        /*
        if (user.getRole()!=null) {
            sLog.info("User "+user.getName()+" has role "+user.getRole()+", forwarding to main page.");
            return mapping.findForward("main");
        }
        */
        String externalId = user.getId();
        if (externalId==null || externalId.length()==0) {
            sLog.info("User "+user.getName()+" has no external id, forwarding to main page.");
            request.setAttribute("message", "No user id provided.");
            return mapping.findForward(back);
        }
        
        if ("Log Out".equals(myForm.getOp())) {
            sLog.info("Logging out user "+user.getName()+", forwarding to main page.");
            request.getSession().invalidate();
            return mapping.findForward(back);
        }
        
        Long sessionId = (Long)request.getAttribute("PersonalizedExamReport.SessionId");
        if (request.getParameter("session")!=null) {
            sessionId = Long.valueOf(request.getParameter("session"));
            request.setAttribute("PersonalizedExamReport.SessionId", sessionId);
        }
        if ("classes".equals(back)) {
            if (sessionId==null) {
                sessionId = (Long)request.getSession().getAttribute("Classes.session");
            } else {
                request.getSession().setAttribute("Classes.session", sessionId);
            }
        } else if ("exams".equals(back)) {
            if (sessionId==null) {
                sessionId = (Long)request.getSession().getAttribute("Exams.session");
            } else {
                request.getSession().setAttribute("Exams.session", sessionId);
            }
        }
        
        HashSet<Session> sessions = new HashSet();
        DepartmentalInstructor instructor = null;
        for (Iterator i=new DepartmentalInstructorDAO().
                getSession().
                createQuery("select i from DepartmentalInstructor i where i.externalUniqueId=:externalId").
                setString("externalId",translate(externalId,Source.Staff)).
                setCacheable(true).list().iterator();i.hasNext();) {
            DepartmentalInstructor s = (DepartmentalInstructor)i.next();
            if (!canDisplay(s.getDepartment().getSession())) continue;
            sessions.add(s.getDepartment().getSession());
            if (sessionId==null) {
                if (instructor==null || instructor.getDepartment().getSession().compareTo(s.getDepartment().getSession())<0) instructor = s;
            } else if (sessionId.equals(s.getDepartment().getSession().getUniqueId())) {
                instructor = s;
            }
        }
        
        Student student = null;
        for (Iterator i=new StudentDAO().
                getSession().
                createQuery("select s from Student s where s.externalUniqueId=:externalId").
                setString("externalId",translate(externalId,Source.Student)).
                setCacheable(true).list().iterator();i.hasNext();) {
            Student s = (Student)i.next();
            if (!canDisplay(s.getSession())) continue;
            sessions.add(s.getSession());
            if (sessionId==null) {
                if (student==null || student.getSession().compareTo(s.getSession())<0) student = s;
            } else if (sessionId.equals(s.getSession().getUniqueId()))
                student = s;
        }
        
        if (instructor==null && student==null && !myForm.getAdmin()) {
            if ("classes".equals(back))
                request.setAttribute("message", "No classes found.");
            else if ("exams".equals(back))
                request.setAttribute("message", "No examinations found.");
            else
                request.setAttribute("message", "No schedule found.");
            sLog.info("No matching instructor or student found for "+user.getName()+" ("+translate(externalId,Source.Student)+"), forwarding back ("+back+").");
            return mapping.findForward(back);
        }
        
        myForm.setCanExport(false);
        
        if (instructor!=null && student!=null && !instructor.getDepartment().getSession().equals(student.getSession())) {
            if (instructor.getDepartment().getSession().compareTo(student.getSession())<0)
                instructor = null;
            else
                student = null;
        }
        long t0 = System.currentTimeMillis();
        if (instructor!=null) {
            sLog.info("Requesting schedule for "+instructor.getName(DepartmentalInstructor.sNameFormatShort)+" (instructor)");
        } else if (student!=null) {
            sLog.info("Requesting schedule for "+student.getName(DepartmentalInstructor.sNameFormatShort)+" (student)");
        }
        
        HashSet<ExamOwner> studentExams = new HashSet<ExamOwner>();
        if (student!=null) {
        	/*
            for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                studentExams.addAll(Exam.findAllRelated("Class_", sce.getClazz().getUniqueId()));
            }
            */
            studentExams.addAll(
            		new ExamDAO().getSession().createQuery(
                            "select distinct o from Student s inner join s.classEnrollments ce, ExamOwner o inner join o.course co "+
                            "inner join co.instructionalOffering io "+
                            "inner join io.instrOfferingConfigs ioc " +
                            "inner join ioc.schedulingSubparts ss "+
                            "inner join ss.classes c where "+
                            "s.uniqueId=:studentId and c=ce.clazz and ("+
                            "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                            "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                            "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                            "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                            ")").
                            setLong("studentId", student.getUniqueId()).setCacheable(true).list());
            if (!student.getSession().getStatusType().canNoRoleReportExamFinal()) {
                for (Iterator<ExamOwner> i=studentExams.iterator();i.hasNext();) {
                    if (i.next().getExam().getExamType()==Exam.sExamTypeFinal) i.remove();
                }
            }
            if (!student.getSession().getStatusType().canNoRoleReportExamMidterm()) {
                for (Iterator<ExamOwner> i=studentExams.iterator();i.hasNext();) {
                    if (i.next().getExam().getExamType()==Exam.sExamTypeMidterm) i.remove();
                }
            }
        }
        
        HashSet<Exam> instructorExams = new HashSet<Exam>();
        if (instructor!=null) {
            if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportExamMidterm())
                instructorExams.addAll(instructor.getExams(Exam.sExamTypeMidterm));
            if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportExamFinal())
                instructorExams.addAll(instructor.getExams(Exam.sExamTypeFinal));
        }
        
        WebTable.setOrder(request.getSession(),"exams.o0",request.getParameter("o0"),1);
        WebTable.setOrder(request.getSession(),"exams.o1",request.getParameter("o1"),1);
        WebTable.setOrder(request.getSession(),"exams.o2",request.getParameter("o2"),1);
        WebTable.setOrder(request.getSession(),"exams.o3",request.getParameter("o3"),1);
        WebTable.setOrder(request.getSession(),"exams.o4",request.getParameter("o4"),1);
        WebTable.setOrder(request.getSession(),"exams.o5",request.getParameter("o5"),1);
        WebTable.setOrder(request.getSession(),"exams.o6",request.getParameter("o6"),1);
        WebTable.setOrder(request.getSession(),"exams.o7",request.getParameter("o7"),1);
        
        boolean hasClasses = false;
        if (student!=null && student.getSession().getStatusType().canNoRoleReportClass() && !student.getClassEnrollments().isEmpty()) {
            PdfWebTable table =  getStudentClassSchedule(true, student);
            if (!table.getLines().isEmpty()) {
                request.setAttribute("clsschd", table.printTable(WebTable.getOrder(request.getSession(),"exams.o6")));
                hasClasses = true;
                myForm.setCanExport(true);
            }
        }
        if (instructor!=null && instructor.getDepartment().getSession().getStatusType().canNoRoleReportClass()) {
            PdfWebTable table = getInstructorClassSchedule(true, instructor);
            if (!table.getLines().isEmpty()) {
                request.setAttribute("iclsschd", table.printTable(WebTable.getOrder(request.getSession(),"exams.o7")));
                hasClasses = true;
                myForm.setCanExport(true);
            }
        }
        
        if (!hasClasses && instructorExams.isEmpty() && studentExams.isEmpty()) {
            if ("classes".equals(back))
                myForm.setMessage("No classes found in "+(instructor!=null?instructor.getDepartment().getSession():student.getSession()).getLabel()+".");
            else if ("exams".equals(back))
                myForm.setMessage("No examinations found in "+(instructor!=null?instructor.getDepartment().getSession():student.getSession()).getLabel()+".");
            else if (student!=null || instructor!=null)
                myForm.setMessage("No classes or examinations found in "+(instructor!=null?instructor.getDepartment().getSession():student.getSession()).getLabel()+".");
            else if (user.isAdmin()) 
                myForm.setMessage("No classes or examinations found in "+Session.getCurrentAcadSession(user).getLabel()+".");
            else
                myForm.setMessage("No classes or examinations found for "+user.getName()+".");
            sLog.info("No classes or exams found for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student!=null?student.getName(DepartmentalInstructor.sNameFormatShort):user.getName()));
        }
        
        boolean useCache = "true".equals(ApplicationProperties.getProperty("tmtbl.exams.reports.conflicts.cache","true"));
        
        if ("Export PDF".equals(myForm.getOp())) {
            sLog.info("  Generating PDF for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student.getName(DepartmentalInstructor.sNameFormatShort)));
            FileOutputStream out = null;
            try {
                File file = ApplicationProperties.getTempFile("schedule", "pdf");
                
                if (!instructorExams.isEmpty()) {
                    TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
                    for (Exam exam : instructorExams) {
                        if (exam.getAssignedPeriod()==null) continue;
                        exams.add(new ExamAssignmentInfo(exam, useCache));
                    }

                    InstructorExamReport ir = new InstructorExamReport(
                            InstructorExamReport.sModeNormal, file, instructor.getDepartment().getSession(),
                            -1, null, exams);
                    ir.setM2d(true); ir.setDirect(true);
                    ir.setClassSchedule(instructor.getDepartment().getSession().getStatusType().canNoRoleReportClass());
                    ir.printHeader();
                    ir.printReport(ExamInfo.createInstructorInfo(instructor), exams);
                    ir.lastPage();
                    ir.close();
                } else if (!studentExams.isEmpty()) {
                    TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
                    TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>();
                    for (ExamOwner examOwner : studentExams) {
                        if (examOwner.getExam().getAssignedPeriod()==null) continue;
                        ExamAssignmentInfo x = new ExamAssignmentInfo(examOwner, student, studentExams);
                        exams.add(x);
                        sections.addAll(x.getSections());
                    }
                    
                    StudentExamReport sr = new StudentExamReport(
                            StudentExamReport.sModeNormal, file, student.getSession(),
                            -1, null, exams);
                    sr.setM2d(true); sr.setBtb(true); sr.setDirect(true);
                    sr.setClassSchedule(student.getSession().getStatusType().canNoRoleReportClass());
                    sr.printHeader();
                    sr.printReport(student, sections);
                    sr.lastPage();
                    sr.close();
                } else if (hasClasses) {
                    if (instructor!=null) {
                        InstructorExamReport ir = new InstructorExamReport(
                                InstructorExamReport.sModeNormal, file, instructor.getDepartment().getSession(),
                                -1, null, new TreeSet<ExamAssignmentInfo>());
                        ir.setM2d(true); ir.setDirect(true);
                        ir.setClassSchedule(instructor.getDepartment().getSession().getStatusType().canNoRoleReportClass());
                        ir.printHeader();
                        ir.printReport(ExamInfo.createInstructorInfo(instructor), new TreeSet<ExamAssignmentInfo>());
                        ir.lastPage();
                        ir.close();
                    } else if (student!=null) {
                        StudentExamReport sr = new StudentExamReport(
                                StudentExamReport.sModeNormal, file, student.getSession(),
                                -1, null, new TreeSet<ExamAssignmentInfo>());
                        sr.setM2d(true); sr.setBtb(true); sr.setDirect(true);
                        sr.setClassSchedule(student.getSession().getStatusType().canNoRoleReportClass());
                        sr.printHeader();
                        sr.printReport(student, new TreeSet<ExamSectionInfo>());
                        sr.lastPage();
                        sr.close();
                    }
                }
                
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
            } catch (Exception e) {
                sLog.error("Unable to generate PDF for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student.getName(DepartmentalInstructor.sNameFormatShort)),e);
            } finally {
                try {
                    if (out!=null) out.close();
                } catch (IOException e) {}
            }
        }
        
        if ("iCalendar".equals(myForm.getOp())) {
            sLog.info("  Generating calendar for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student.getName(DepartmentalInstructor.sNameFormatShort)));
            try {
                File file = ApplicationProperties.getTempFile("schedule", "ics");
                
                if (instructor!=null) {
                    printInstructorSchedule(file, instructor, instructorExams);
                } else {
                    printStudentSchedule(file, student, studentExams);
                }
                
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
            } catch (Exception e) {
                sLog.error("Unable to generate calendar for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student.getName(DepartmentalInstructor.sNameFormatShort)),e);
            }
        }
        
        if (instructor!=null && sessions.size()>1) {
            PdfWebTable table = getSessions(true, sessions, instructor.getName(DepartmentalInstructor.sNameFormatLastFist), instructor.getDepartment().getSession().getUniqueId());
            request.setAttribute("sessions", table.printTable(WebTable.getOrder(request.getSession(),"exams.o0")));
        } else if (student!=null && sessions.size()>1) {
            PdfWebTable table = getSessions(true, sessions, student.getName(DepartmentalInstructor.sNameFormatLastFist), student.getSession().getUniqueId());
            request.setAttribute("sessions", table.printTable(WebTable.getOrder(request.getSession(),"exams.o0")));
        }
        
        if (!studentExams.isEmpty()) {
            myForm.setCanExport(true);
            TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
            for (ExamOwner examOwner : studentExams) exams.add(new ExamAssignmentInfo(examOwner, student, studentExams));
            
            PdfWebTable table = getStudentExamSchedule(true, exams, student);
            request.setAttribute("schedule", table.printTable(WebTable.getOrder(request.getSession(),"exams.o1")));
            
            table = getStudentConflits(true, exams, student, user);
            if (!table.getLines().isEmpty())
                request.setAttribute("conf", table.printTable(WebTable.getOrder(request.getSession(),"exams.o3")));
        }
        
        if (!instructorExams.isEmpty()) {
            myForm.setCanExport(true);
            TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
            for (Exam exam : instructorExams) exams.add(new ExamAssignmentInfo(exam, useCache));
            
            PdfWebTable table = getInstructorExamSchedule(true, exams, instructor);
            request.setAttribute("ischedule", table.printTable(WebTable.getOrder(request.getSession(),"exams.o2")));
            
            table = getInstructorConflits(true, exams, instructor, user);
            if (!table.getLines().isEmpty())
                request.setAttribute("iconf", table.printTable(WebTable.getOrder(request.getSession(),"exams.o4")));

            table = getStudentConflits(true, exams, instructor);
            if (!table.getLines().isEmpty())
                request.setAttribute("sconf", table.printTable(WebTable.getOrder(request.getSession(),"exams.o5")));
        }
        long t1 = System.currentTimeMillis();
        sLog.info("Request processed in "+new DecimalFormat("0.00").format(((double)(t1-t0))/1000.0)+" s for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student!=null?student.getName(DepartmentalInstructor.sNameFormatShort):user.getName()));
        return mapping.findForward("show");
    }
    
    private static boolean canDisplay(Session session) {
        if (session.getStatusType()==null) return false;
        if (session.getStatusType().canNoRoleReportExamFinal() && Exam.hasTimetable(session.getUniqueId(),Exam.sExamTypeFinal)) return true;
        if (session.getStatusType().canNoRoleReportExamMidterm() && Exam.hasTimetable(session.getUniqueId(),Exam.sExamTypeMidterm)) return true;
        if (session.getStatusType().canNoRoleReportClass() && Solution.hasTimetable(session.getUniqueId())) return true;
        return false;
    }
    
    public int getDaysCode(Set meetings) {
        int daysCode = 0;
        for (Iterator i=meetings.iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            Calendar date = Calendar.getInstance(Locale.US);
            date.setTime(meeting.getMeetingDate());
            switch (date.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_MON]; break;
            case Calendar.TUESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_TUE]; break;
            case Calendar.WEDNESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_WED]; break;
            case Calendar.THURSDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_THU]; break;
            case Calendar.FRIDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_FRI]; break;
            case Calendar.SATURDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SAT]; break;
            case Calendar.SUNDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SUN]; break;
            }
        }
        return daysCode;
    }
    
    public static String DAY_NAMES_SHORT[] = new String[] {
        "M", "T", "W", "R", "F", "S", "U"
    };
    
    protected long getMeetingComparable(ExamSectionInfo section) {
        if (section.getOwner().getOwnerObject() instanceof Class_) {
            SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
            Class_ clazz = (Class_)section.getOwner().getOwnerObject();
            Assignment assignment = clazz.getCommittedAssignment();
            TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
            if (meetings!=null && !meetings.isEmpty()) {
                return ((Meeting)meetings.first()).getMeetingDate().getTime();
            } else if (assignment!=null) {
                return assignment.getTimeLocation().getStartSlot();
            }
        }
        return -1;
    }

    protected String getMeetingTime(ExamSectionInfo section) {
        String meetingTime = "";
        if (section.getOwner().getOwnerObject() instanceof Class_) {
            SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
            Class_ clazz = (Class_)section.getOwner().getOwnerObject();
            Assignment assignment = clazz.getCommittedAssignment();
            TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
            if (meetings!=null && !meetings.isEmpty()) {
                Date first = ((Meeting)meetings.first()).getMeetingDate();
                Date last = ((Meeting)meetings.last()).getMeetingDate();
                meetingTime += dpf.format(first)+" - "+dpf.format(last);
            } else if (assignment!=null && assignment.getDatePattern()!=null) {
                DatePattern dp = assignment.getDatePattern();
                if (dp!=null && !dp.isDefault()) {
                    if (dp.getType().intValue()==DatePattern.sTypeAlternate)
                        meetingTime += dp.getName();
                    else {
                        meetingTime += dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate());
                    }
                }
            }
            if (meetings!=null && !meetings.isEmpty()) {
                int dayCode = getDaysCode(meetings);
                String days = "";
                for (int i=0;i<Constants.DAY_CODES.length;i++)
                    if ((dayCode & Constants.DAY_CODES[i])!=0) days += Constants.DAY_NAMES_SHORT[i];
                meetingTime += " "+days;
                Meeting first = (Meeting)meetings.first();
                meetingTime += " "+first.startTime()+" - "+first.stopTime();
            } else if (assignment!=null) {
                TimeLocation t = assignment.getTimeLocation();
                meetingTime += " "+t.getDayHeader()+" "+t.getStartTimeHeader()+" - "+t.getEndTimeHeader();
            }
        }
        return meetingTime;
    }
    
    public PdfWebTable getSessions(boolean html, HashSet<Session> sessions, String name, Long sessionId) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 5,
                "Available Academic Sessions for "+name,
                "personalSchedule.do?o0=%%",
                new String[] {
                    "Term",
                    "Year",
                    "Campus"},
                new String[] {"left", "left", "left"},
                new boolean[] {true, true, true} );
        table.setRowStyle("white-space:nowrap");
        for (Session session : sessions) {
            String bgColor = null;
            if (sessionId.equals(session.getUniqueId())) bgColor = "rgb(168,187,225)";
            table.addLine(
                    "onClick=\"document.location='personalSchedule.do?session="+session.getUniqueId()+"';\"",
                    new String[] {
                        session.getAcademicTerm(),
                        session.getAcademicYear(),
                        session.getAcademicInitiative()
                    },
                    new Comparable[] {
                        new MultiComparable(session.getSessionBeginDateTime(),session.getAcademicInitiative()),
                        new MultiComparable(session.getSessionBeginDateTime(),session.getAcademicInitiative()),
                        new MultiComparable(session.getAcademicInitiative(),session.getSessionBeginDateTime())}).setBgColor(bgColor);
        }
        return table;
    }
    
    public PdfWebTable getStudentExamSchedule(boolean html, TreeSet<ExamAssignmentInfo> exams, Student student) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 5,
                student.getSession().getLabel()+" Examination Schedule for "+student.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o1=%%",
                new String[] {
                    "Class / Course",
                    "Meeting Time",
                    "Date",
                    "Time",
                    "Room"},
                new String[] {"left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom","");
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSections()) {
                if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                String sectionName = section.getNameForStudent(student);
                table.addLine(
                        new String[] {
                        		sectionName,
                                getMeetingTime(section),
                                exam.getDate(false),
                                exam.getTime(false),
                                (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "))
                        },
                        new Comparable[] {
                            new MultiComparable(sectionName, exam),
                            new MultiComparable(getMeetingComparable(section), sectionName, exam),
                            new MultiComparable(exam.getPeriodOrd(), sectionName, exam),
                            new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), sectionName, exam),
                            new MultiComparable(exam.getRoomsName(":"), sectionName, exam)
                        });
            }
        }
        return table;
    }
    
    public PdfWebTable getStudentConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, Student student, User user) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
                student.getSession().getLabel()+" Examination Conflicts and/or Back-To-Back Examinations for "+student.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o3=%%",
                new String[] {
                    "Type",
                    "Class / Course",
                    "Date",
                    "Time",
                    "Room",
                    "Distance"},
                    new String[] {"left","left","left","left","left","left"},
                    new boolean[] {true,  true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom","");
        for (ExamAssignmentInfo exam : exams) {
            for (DirectConflict conflict : exam.getDirectConflicts()) {
                if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                String classes = "", date = "", time = "", room = "", distance = "", blank="";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getNameForStudent(student);
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                firstSection = true;
                if (conflict.getOtherExam()!=null) {
                    for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                        if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getNameForStudent(student);
                        if (firstSection) {
                            room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                } else if (conflict.getOtherEventId()!=null) {
                    blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                    classes += conflict.getOtherEventName();
                    room += conflict.getOtherEventRoom();
                    //date += conflict.getOtherEventDate();
                    time += conflict.getOtherEventTime(); 
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+(conflict.getOtherEventId()!=null?conflict.isOtherClass()?"Class":"Event":"Direct")+(html?"</font>":""),
                            classes,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(0, exam, 0),
                            new MultiComparable(exam, exam, 0),
                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-1.0, exam, 0)
                        });
            }
            for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                String classes = "", date = "", time = "", room = "", distance = "", blank="";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getNameForStudent(student);
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));;
                    }
                    firstSection = false;
                }
                firstSection = true;
                for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getNameForStudent(student);
                    if (firstSection) {
                        time += conflict.getOtherExam().getTime(false);
                        room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+"Back-To-Back"+(html?"</font>":""),
                            classes,
                            date,
                            time,
                            room,
                            (int)(conflict.getDistance()*10.0)+" m"
                        }, new Comparable[] {
                            new MultiComparable(2, exam, 0),
                            new MultiComparable(exam, exam, 0),
                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(conflict.getDistance(), exam, 0)
                        });
            }
            conflicts: for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                String classes = "", date = "", time = "", room = "", distance = "", blank="";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getNameForStudent(student);
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                for (ExamAssignment other : conflict.getOtherExams()) {
                    firstSection = true;
                    for (ExamSectionInfo section : other.getSections()) {
                        if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getNameForStudent(student);
                        if (firstSection) {
                            time += other.getTime(false);
                            room += (other.getNrRooms()==0?noRoom:other.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                }
                table.addLine(
                        (user.getRole() == null?"":"onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\""),
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?"&gt;":"")+"2 A Day"+(html?"</font>":""),
                            classes,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(1, exam, 0),
                            new MultiComparable(exam, exam, 0),
                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-1.0, exam, 0)
                        },
                        exam.getExamId().toString());
            }
        }
        return table;
    }

    
    public PdfWebTable getInstructorExamSchedule(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
                instructor.getDepartment().getSession().getLabel()+" Examination Instructor Schedule for "+instructor.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o2=%%",
                new String[] {
                    "Class / Course",
                    "Enrollment",
                    "Seating"+nl+"Type",
                    "Meeting Times",
                    "Date",
                    "Time",
                    "Room",
                    "Capacity"},
                new String[] {"left", "right", "center", "left", "left", "left", "left", "right"},
                new boolean[] {true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom","");
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSections()) {
                table.addLine(
                        new String[] {
                                section.getName(),
                                String.valueOf(section.getNrStudents()),
                                Exam.sSeatingTypes[exam.getSeatingType()],
                                getMeetingTime(section),
                                exam.getDate(false),
                                exam.getTime(false),
                                (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", ")),
                                exam.getRoomsCapacity(false, ", ")
                        },
                        new Comparable[] {
                            new MultiComparable(section.getName(), exam),
                            new MultiComparable(-exam.getNrStudents(), section.getName(), exam),
                            new MultiComparable(exam.getSeatingType(), section.getName(), exam),
                            new MultiComparable(getMeetingComparable(section), section.getName(), exam),
                            new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                            new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), section.getName(), exam),
                            new MultiComparable(exam.getRoomsName(":"), section.getName(), exam),
                            new MultiComparable(-exam.getRoomsCapacity(), section.getName(), exam),
                        });
            }
        }
        return table;
    }
    
    public PdfWebTable getInstructorConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor, User user) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
                instructor.getDepartment().getSession().getLabel()+" Examination Instructor Conflicts and/or Back-To-Back Examinations for "+instructor.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o4=%%",
                new String[] {
                    "Type",
                    "Class / Course",
                    "Enrollment",
                    "Seating"+nl+"Type",
                    "Date",
                    "Time",
                    "Room",
                    "Distance"},
                    new String[] {"left","left","right","center","left","left","left","left"},
                    new boolean[] {true,  true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom","");
        for (ExamAssignmentInfo exam : exams) {
            for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getName();
                    enrollment += String.valueOf(section.getNrStudents());
                    if (firstSection) {
                        seating += Exam.sSeatingTypes[exam.getSeatingType()];
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                firstSection = true;
                if (conflict.getOtherExam()!=null) {
                    for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.sSeatingTypes[conflict.getOtherExam().getSeatingType()];
                            room += (conflict.getOtherExam().getNrRooms()==0?conflict.getOtherExam():exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                } else if (conflict.getOtherEventId()!=null) {
                    blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                    classes += conflict.getOtherEventName();
                    enrollment += conflict.getOtherEventSize();
                    seating += conflict.isOtherClass()?"Class":"Event";
                    room += conflict.getOtherEventRoom();
                    //date += conflict.getOtherEventDate();
                    time += conflict.getOtherEventTime(); 
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+(conflict.getOtherEventId()!=null?conflict.isOtherClass()?"Class":"Event":"Direct")+(html?"</font>":""),
                            classes,
                            enrollment,
                            seating,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(0, exam, 0),
                            new MultiComparable(exam, exam, 0),
                            new MultiComparable(-exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                            new MultiComparable(exam.getExamType(), exam, 0),
                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-1.0, exam, 0)
                        });
            }
            for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getName();
                    enrollment += String.valueOf(section.getNrStudents());
                    if (firstSection) {
                        seating += Exam.sSeatingTypes[exam.getSeatingType()];
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                firstSection = true;
                for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getName();
                    enrollment += String.valueOf(section.getNrStudents());
                    if (firstSection) {
                        seating += Exam.sSeatingTypes[conflict.getOtherExam().getSeatingType()];
                        time += conflict.getOtherExam().getTime(false);
                        room += (conflict.getOtherExam().getNrRooms()==0?conflict.getOtherExam():exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+"Back-To-Back"+(html?"</font>":""),
                            classes,
                            enrollment,
                            seating,
                            date,
                            time,
                            room,
                            (int)(conflict.getDistance()*10.0)+" m"
                        }, new Comparable[] {
                            new MultiComparable(2, exam, 0),
                            new MultiComparable(exam, exam, 0),
                            new MultiComparable(-exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                            new MultiComparable(exam.getExamType(), exam, 0),
                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(conflict.getDistance(), exam, 0)
                        });
            }
            conflicts: for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                int nrStudents = exam.getNrStudents();
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getName();
                    enrollment += String.valueOf(section.getNrStudents());
                    if (firstSection) {
                        seating += Exam.sSeatingTypes[exam.getSeatingType()];
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                for (ExamAssignment other : conflict.getOtherExams()) {
                    firstSection = true;
                    nrStudents += other.getNrStudents();
                    for (ExamSectionInfo section : other.getSections()) {
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.sSeatingTypes[exam.getSeatingType()];
                            time += other.getTime(false);
                            room += (other.getNrRooms()==0?noRoom:other.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                }
                table.addLine(
                		(user.getRole() == null?"":"onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\""),
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?"&gt;":"")+"2 A Day"+(html?"</font>":""),
                            classes,
                            enrollment,
                            seating,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(1, exam, 0),
                            new MultiComparable(exam, exam, 0),
                            new MultiComparable(-nrStudents, exam, 0),
                            new MultiComparable(exam.getExamType(), exam, 0),
                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-1.0, exam, 0)
                        },
                        exam.getExamId().toString());
            }
        }
        return table;
    }
    
    public PdfWebTable getStudentConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
                instructor.getDepartment().getSession().getLabel()+" Examination Conflicts for "+instructor.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o5=%%",
                new String[] {
                    "Name",
                    "Type",
                    "Class / Course",
                    "Enrollment",
                    "Seating"+nl+"Type",
                    "Date",
                    "Time",
                    "Room"},
                    new String[] {"left","left","left","right","center","left","left","left"},
                    new boolean[] {true, true,  true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom","");
        table.setBlankWhenSame(true);
        for (ExamAssignmentInfo exam : exams) {
            for (DirectConflict conflict : exam.getDirectConflicts()) {
                if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                for (Long studentId : conflict.getStudents()) {
                    Student student = new StudentDAO().get(studentId);
                    String id = student.getExternalUniqueId();
                    String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.sSeatingTypes[exam.getSeatingType()];
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    firstSection = true;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                            if (!section.getStudentIds().contains(studentId)) continue;
                            if (classes.length()>0) {
                                blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                            }
                            classes += section.getName();
                            enrollment += String.valueOf(section.getNrStudents());
                            if (firstSection) {
                                seating += Exam.sSeatingTypes[exam.getSeatingType()];
                                room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                            }
                            firstSection = false;
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        classes += conflict.getOtherEventName();
                        enrollment += conflict.getOtherEventSize();
                        seating += conflict.isOtherClass()?"Class":"Event";
                        room += conflict.getOtherEventRoom();
                        //date += conflict.getOtherEventDate();
                        time += conflict.getOtherEventTime(); 
                    }
                    table.addLine(
                            new String[] {
                                name,
                                (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+"Direct"+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room
                            }, new Comparable[] {
                                new MultiComparable(name,id, exam, 0),
                                new MultiComparable(0, exam, 0),
                                new MultiComparable(exam, exam, 0),
                                new MultiComparable(-exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                                new MultiComparable(exam.getExamType(), exam, 0),
                                new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(exam.getRoomsName(":"), exam, 0)
                            });
                }
            }
            /*
            for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                for (Long studentId : conflict.getStudents()) {
                    Student student = new StudentDAO().get(studentId);
                    String id = student.getExternalUniqueId();
                    String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.sSeatingTypes[exam.getSeatingType()];
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    firstSection = true;
                    for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.sSeatingTypes[exam.getSeatingType()];
                            time += conflict.getOtherExam().getTime(false);
                            room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    table.addLine(
                            new String[] {
                                name,
                                (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+"Back-To-Back"+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room
                            }, new Comparable[] {
                                new MultiComparable(name, id, exam, 0),
                                new MultiComparable(2, exam, 0),
                                new MultiComparable(exam, exam, 0),
                                new MultiComparable(-exam.getNrStudents()-conflict.getOtherExam().getNrStudents(), exam, 0),
                                new MultiComparable(exam.getExamType(), exam, 0),
                                new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(exam.getRoomsName(":"), exam, 0)
                            });
                }
            }
            */
            conflicts: for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                for (Long studentId : conflict.getStudents()) {
                    Student student = new StudentDAO().get(studentId);
                    String id = student.getExternalUniqueId();
                    String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                    int nrStudents = exam.getNrStudents();
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.sSeatingTypes[exam.getSeatingType()];
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    for (ExamAssignment other : conflict.getOtherExams()) {
                        firstSection = true;
                        nrStudents += other.getNrStudents();
                        for (ExamSectionInfo section : other.getSections()) {
                            if (!section.getStudentIds().contains(studentId)) continue;
                            if (classes.length()>0) {
                                blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                            }
                            classes += section.getName();
                            enrollment += String.valueOf(section.getNrStudents());
                            if (firstSection) {
                                seating += Exam.sSeatingTypes[exam.getSeatingType()];
                                time += other.getTime(false);
                                room += (other.getNrRooms()==0?noRoom:other.getRoomsName(false,", "));
                            }
                            firstSection = false;
                        }
                    }
                    table.addLine(
                            new String[] {
                                name,
                                (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?"&gt;":"")+"2 A Day"+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room
                            }, new Comparable[] {
                                new MultiComparable(name, id, exam, 0),
                                new MultiComparable(1, exam, 0),
                                new MultiComparable(exam, exam, 0),
                                new MultiComparable(-nrStudents, exam, 0),
                                new MultiComparable(exam.getExamType(), exam, 0),
                                new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(exam.getRoomsName(":"), exam, 0),
                            });
                }
            }
        }
        return table;       
    }
    
    protected String getMeetingTime(Class_ clazz) {
        String meetingTime = "";
        SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        DatePattern dp = (assignment==null?null:assignment.getDatePattern());
        if (meetings!=null && !meetings.isEmpty()) {
            int dayCode = getDaysCode(meetings);
            String days = "";
            for (int i=0;i<Constants.DAY_CODES.length;i++)
                if ((dayCode & Constants.DAY_CODES[i])!=0) days += Constants.DAY_NAMES_SHORT[i];
            meetingTime += days;
            Meeting first = (Meeting)meetings.first();
            meetingTime += " "+first.startTime()+" - "+first.stopTime();
        } else if (assignment!=null) {
            TimeLocation t = assignment.getTimeLocation();
            meetingTime += t.getDayHeader()+" "+t.getStartTimeHeader()+" - "+t.getEndTimeHeader();
        } else {
            meetingTime += "Arr Hrs";
        }
        if (meetings!=null && !meetings.isEmpty()) {
            if (dp==null || !dp.isDefault()) {
                Date first = ((Meeting)meetings.first()).getMeetingDate();
                Date last = ((Meeting)meetings.last()).getMeetingDate();
                if (dp!=null && dp.getType()==DatePattern.sTypeAlternate) 
                    meetingTime += " ("+dpf.format(first)+" - "+dpf.format(last)+" "+dp.getName()+")";
                else
                    meetingTime += " ("+dpf.format(first)+" - "+dpf.format(last)+")";
            }
        } else if (dp!=null && !dp.isDefault()) {
            if (dp.getType()==DatePattern.sTypeAlternate) 
                meetingTime += " ("+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate())+" "+dp.getName()+")";
            else
                meetingTime += " ("+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate())+")";
        }
        return meetingTime;
    }
    
    protected String getMeetingRooms(Class_ clazz) {
        String meetingRooms = "";
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet<Meeting> meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        TreeSet<Location> locations = new TreeSet<Location>();
        if (meetings!=null && !meetings.isEmpty()) {
            for (Meeting meeting : meetings)
                if (meeting.getLocation()!=null) locations.add(meeting.getLocation());
        } else if (assignment!=null && assignment.getDatePattern()!=null) {
            for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
                locations.add((Location)i.next());
            }
        }
        for (Location location: locations) {
            if (meetingRooms.length()>0) meetingRooms+=", ";
            meetingRooms+=location.getLabel();
        }
        return meetingRooms;
    }
    
    protected long getMeetingComparable(Class_ clazz) {
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        if (meetings!=null && !meetings.isEmpty()) {
            return ((Meeting)meetings.first()).getMeetingDate().getTime();
        } else if (assignment!=null) {
            return assignment.getTimeLocation().getStartSlot();
        }
        return -1;
    }
    
    protected String getMeetingInstructor(Class_ clazz) {
        String meetingInstructor = "";
        if (!clazz.isDisplayInstructor()) return meetingInstructor;
        for (Iterator i=new TreeSet(clazz.getClassInstructors()).iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (meetingInstructor.length()>0) meetingInstructor+=", ";
            meetingInstructor += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatLastInitial);
        }
        return meetingInstructor;
    }
    
    public PdfWebTable getStudentClassSchedule(boolean html, Student student) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
                student.getSession().getLabel()+" Class Schedule for "+student.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o6=%%",
                new String[] {
                    "Course",
                    "Instruction"+nl+"Type",
                    "Section",
                    "Time",
                    "Room",
                    "Instructor"
                    },
                new String[] {"left", "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        boolean suffix = "true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.suffix","false"));
        for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            String course = sce.getCourseOffering().getCourseName();
            String itype =  sce.getClazz().getSchedulingSubpart().getItypeDesc();
            int itypeCmp = sce.getClazz().getSchedulingSubpart().getItype().getItype();
            String section = (suffix && sce.getClazz().getClassSuffix()!=null?sce.getClazz().getClassSuffix():sce.getClazz().getSectionNumberString());
            String time = getMeetingTime(sce.getClazz());
            long timeCmp = getMeetingComparable(sce.getClazz());
            String room = getMeetingRooms(sce.getClazz());
            String instr = getMeetingInstructor(sce.getClazz());
            table.addLine(
                    new String[] {
                            course,
                            itype,
                            section,
                            time,
                            room,
                            instr
                        },
                        new Comparable[] {
                            new MultiComparable(course, itypeCmp, section, timeCmp, room, instr),
                            new MultiComparable(itypeCmp, course, section, timeCmp, room, instr),
                            new MultiComparable(course, section, itypeCmp, timeCmp, room, instr),
                            new MultiComparable(timeCmp, room, course, itypeCmp, section, instr),
                            new MultiComparable(room, timeCmp, course, itypeCmp, section, instr),
                            new MultiComparable(instr, course, itypeCmp, section, timeCmp, room)
                        });
        }
        return table;        
    }
    
    public PdfWebTable getInstructorClassSchedule(boolean html, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
                instructor.getDepartment().getSession().getLabel()+" Class Schedule for "+instructor.getName(DepartmentalInstructor.sNameFormatLastFist),
                "personalSchedule.do?o7=%%",
                new String[] {
                    "Course",
                    "Instruction"+nl+"Type",
                    "Section",
                    "Time",
                    "Room",
                    "Share"
                    },
                new String[] {"left", "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true} );
        Set allClasses = new HashSet();
        for (Iterator i=DepartmentalInstructor.getAllForInstructor(instructor, instructor.getDepartment().getSession().getUniqueId()).iterator();i.hasNext();) {
            DepartmentalInstructor di = (DepartmentalInstructor)i.next();
            allClasses.addAll(di.getClasses());
        }
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        boolean suffix = "true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.suffix","false"));
        for (Iterator i=allClasses.iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            String course = ci.getClassInstructing().getSchedulingSubpart().getControllingCourseOffering().getCourseName();
            String itype =  ci.getClassInstructing().getSchedulingSubpart().getItypeDesc();
            int itypeCmp = ci.getClassInstructing().getSchedulingSubpart().getItype().getItype();
            String section = (suffix && ci.getClassInstructing().getClassSuffix()!=null?ci.getClassInstructing().getClassSuffix():ci.getClassInstructing().getSectionNumberString());
            String time = getMeetingTime(ci.getClassInstructing());
            long timeCmp = getMeetingComparable(ci.getClassInstructing());
            String room = getMeetingRooms(ci.getClassInstructing());
            String share = ci.getPercentShare()+"%";
            if (html && ci.isLead()) share = "<b>"+share+"</b>";
            table.addLine(
                    new String[] {
                            course,
                            itype,
                            section,
                            time,
                            room,
                            share
                        },
                        new Comparable[] {
                            new MultiComparable(course, itypeCmp, section, timeCmp, room),
                            new MultiComparable(itypeCmp, course, section, timeCmp, room),
                            new MultiComparable(course, section, itypeCmp, timeCmp, room),
                            new MultiComparable(timeCmp, room, course, itypeCmp, section),
                            new MultiComparable(room, timeCmp, course, itypeCmp, section),
                            new MultiComparable(-ci.getPercentShare(), course, itypeCmp, section, timeCmp, room)
                        });
        }
        return table;        
    }
    
    public void printStudentSchedule(File file, Student student, HashSet<ExamOwner> exams) throws Exception {
        PrintWriter out = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
            tf.setTimeZone(TimeZone.getTimeZone("UTC"));
            out = new PrintWriter(new FileWriter(file));
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:"+student.getName(DepartmentalInstructor.sNameFormatLastFist));
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+"/UniTime Personal Schedule//NONSGML v1.0//EN");
            if (student.getSession().getStatusType().canNoRoleReportClass()) {
                for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                    StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                    if (sce.getClazz().getEvent()!=null) {
                        for (Iterator k=sce.getClazz().getEvent().getMeetings().iterator();k.hasNext();) {
                            Meeting meeting = (Meeting)k.next();
                            out.println("BEGIN:VEVENT");
                            out.println("UID:m"+meeting.getUniqueId());
                            out.println("DTSTART:"+df.format(meeting.getStartTime())+"T"+tf.format(meeting.getStartTime())+"Z");
                            out.println("DTEND:"+df.format(meeting.getStopTime())+"T"+tf.format(meeting.getStopTime())+"Z");
                            out.println("SUMMARY:"+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")");
                            if (meeting.getLocation()!=null)
                                out.println("LOCATION:"+meeting.getLocation().getLabel());
                            out.println("END:VEVENT");
                        }
                        
                    }
                }
            }
            for (ExamOwner examOwner: exams) {
            	Exam exam = examOwner.getExam();
                if (exam.getAssignedPeriod()==null) continue;
                for (ExamSectionInfo section: new ExamAssignment(exam).getSections()) {
                    if (section.getStudentIds().contains(student.getUniqueId())) {
                        out.println("BEGIN:VEVENT");
                        out.println("UID:x"+exam.getUniqueId());
                        out.println("DTSTART:"+df.format(exam.getAssignedPeriod().getStartTime())+"T"+tf.format(exam.getAssignedPeriod().getStartTime())+"Z");
                        Calendar endTime = Calendar.getInstance(); endTime.setTime(exam.getAssignedPeriod().getStartTime());
                        endTime.add(Calendar.MINUTE, exam.getLength());
                        out.println("DTEND:"+df.format(endTime.getTime())+"T"+tf.format(endTime.getTime())+"Z");
                        out.println("SUMMARY:"+section.getNameForStudent(student)+" ("+ApplicationProperties.getProperty("tmtbl.exam.name.type."+Exam.sExamTypes[exam.getExamType()],Exam.sExamTypes[exam.getExamType()])+" Exam)");
                        //out.println("DESCRIPTION:"+exam.getExamName()+" ("+Exam.sExamTypes[exam.getExamType()]+" Exam)");
                        if (!exam.getAssignedRooms().isEmpty()) {
                            String rooms = "";
                            for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                                Location location = (Location)i.next();
                                if (rooms.length()>0) rooms+=", ";
                                rooms+=location.getLabel();
                            }
                            out.println("LOCATION:"+rooms);
                        }
                        out.println("END:VEVENT");
                    }
                }
            }
        } finally {
            if (out!=null) { out.flush(); out.close(); }
        }
    }
    
    public void printInstructorSchedule(File file, DepartmentalInstructor instructor, HashSet<Exam> exams) throws Exception {
        PrintWriter out = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
            tf.setTimeZone(TimeZone.getTimeZone("UTC"));
            out = new PrintWriter(new FileWriter(file));
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:"+instructor.getName(DepartmentalInstructor.sNameFormatLastFist));
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+"/UniTime Personal Schedule//NONSGML v1.0//EN");
            if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportClass()) {
                for (Iterator i=DepartmentalInstructor.getAllForInstructor(instructor, instructor.getDepartment().getSession().getUniqueId()).iterator();i.hasNext();) {
                    DepartmentalInstructor di = (DepartmentalInstructor)i.next();
                    for (Iterator j=di.getClasses().iterator();j.hasNext();) {
                        ClassInstructor ci = (ClassInstructor)j.next();
                        if (ci.getClassInstructing().getEvent()!=null) {
                            for (Iterator k=ci.getClassInstructing().getEvent().getMeetings().iterator();k.hasNext();) {
                                Meeting meeting = (Meeting)k.next();
                                out.println("BEGIN:VEVENT");
                                out.println("UID:m"+meeting.getUniqueId());
                                out.println("DTSTART:"+df.format(meeting.getStartTime())+"T"+tf.format(meeting.getStartTime())+"Z");
                                out.println("DTEND:"+df.format(meeting.getStopTime())+"T"+tf.format(meeting.getStopTime())+"Z");
                                out.println("SUMMARY:"+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")");
                                if (meeting.getLocation()!=null)
                                    out.println("LOCATION:"+meeting.getLocation().getLabel());
                                out.println("END:VEVENT");
                            }
                        }
                    }
                }
            }
            for (Exam exam: exams) {
                if (exam.getAssignedPeriod()==null) continue;
                out.println("BEGIN:VEVENT");
                out.println("UID:x"+exam.getUniqueId());
                out.println("DTSTART:"+df.format(exam.getAssignedPeriod().getStartTime())+"T"+tf.format(exam.getAssignedPeriod().getStartTime())+"Z");
                Calendar endTime = Calendar.getInstance(); endTime.setTime(exam.getAssignedPeriod().getStartTime());
                endTime.add(Calendar.MINUTE, exam.getLength());
                out.println("DTEND:"+df.format(endTime.getTime())+"T"+tf.format(endTime.getTime())+"Z");
                out.println("SUMMARY:"+exam.getLabel()+" ("+ApplicationProperties.getProperty("tmtbl.exam.name.type."+Exam.sExamTypes[exam.getExamType()],Exam.sExamTypes[exam.getExamType()])+" Exam)");
                //out.println("DESCRIPTION:"+exam.getExamName()+" ("+Exam.sExamTypes[exam.getExamType()]+" Exam)");
                if (!exam.getAssignedRooms().isEmpty()) {
                    String rooms = "";
                    for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                        Location location = (Location)i.next();
                        if (rooms.length()>0) rooms+=", ";
                        rooms+=location.getLabel();
                    }
                    out.println("LOCATION:"+rooms);
                }
                out.println("END:VEVENT");                
            }
        } finally {
            if (out!=null) { out.flush(); out.close(); }
        }
    }
}
