/*
 * UniTime 3.1 (University Timetabling Application)
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
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.PersonalizedExamReportForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
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
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PersonalizedExamReportForm myForm = (PersonalizedExamReportForm) form;
        
        User user = Web.getUser(request.getSession());
        if (user==null) {
            request.setAttribute("message", "Login is required.");
            return mapping.findForward("back");
        }
        if (user.getRole()!=null) return mapping.findForward("main");
        String externalId = user.getId();
        if (externalId==null || externalId.length()==0) {
            request.setAttribute("message", "No user id provided.");
            return mapping.findForward("back");
        }
        
        if ("Log Out".equals(myForm.getOp())) {
            request.getSession().invalidate();
            return mapping.findForward("back");
        }
        
        Long sessionId = (Long)request.getAttribute("PersonalizedExamReport.SessionId");
        if (request.getParameter("session")!=null) {
            sessionId = Long.valueOf(request.getParameter("session"));
            request.setAttribute("PersonalizedExamReport.SessionId", sessionId);
        }
        
        HashSet<Session> sessions = new HashSet();
        DepartmentalInstructor instructor = null;
        for (Iterator i=new DepartmentalInstructorDAO().
                getSession().
                createQuery("select i from DepartmentalInstructor i where i.externalUniqueId=:externalId").
                setString("externalId",externalId).
                setCacheable(true).list().iterator();i.hasNext();) {
            DepartmentalInstructor s = (DepartmentalInstructor)i.next();
            if (s.getDepartment().getSession().getStatusType()==null || !s.getDepartment().getSession().getStatusType().canNoRoleReport() || !Exam.hasTimetable(s.getDepartment().getSession().getUniqueId())) continue;
            sessions.add(s.getDepartment().getSession());
            if (sessionId==null) {
                if (instructor==null || instructor.getDepartment().getSession().compareTo(s.getDepartment().getSession())>0) instructor = s;
            } else if (sessionId.equals(s.getDepartment().getSession().getUniqueId())) {
                instructor = s;
            }
        }
        
        Student student = null;
        for (Iterator i=new StudentDAO().
                getSession().
                createQuery("select s from Student s where s.externalUniqueId=:externalId").
                setString("externalId",externalId).
                setCacheable(true).list().iterator();i.hasNext();) {
            Student s = (Student)i.next();
            if (s.getSession().getStatusType()==null || !s.getSession().getStatusType().canNoRoleReport() || !Exam.hasTimetable(s.getSession().getUniqueId())) continue;
            sessions.add(s.getSession());
            if (sessionId==null) {
                if (student==null || student.getSession().compareTo(s.getSession())>0) student = s;
            } else if (sessionId.equals(s.getSession().getUniqueId()))
                student = s;
        }
        
        if (instructor==null && student==null) {
            request.setAttribute("message", "No examinations found.");
            return mapping.findForward("back");
        }
        
        myForm.setCanExport(false);
        
        if (instructor!=null && student!=null && !instructor.getDepartment().getSession().equals(student.getSession())) {
            if (instructor.getDepartment().getSession().compareTo(student.getSession())<0)
                instructor = null;
            else
                student = null;
        }
        
        HashSet<Exam> studentExams = new HashSet<Exam>();
        if (student!=null) for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            studentExams.addAll(Exam.findAllRelated("Class_", sce.getClazz().getUniqueId()));
        }
        
        HashSet<Exam> instructorExams = new HashSet<Exam>();
        if (instructor!=null) {
            instructorExams.addAll(instructor.getExams(Exam.sExamTypeMidterm));
            instructorExams.addAll(instructor.getExams(Exam.sExamTypeFinal));
        }
        
        if (instructorExams.isEmpty() && studentExams.isEmpty()) {
            request.setAttribute("message", "No examinations found.");
            return mapping.findForward("back");
        }
        
        WebTable.setOrder(request.getSession(),"exams.o0",request.getParameter("o0"),1);
        WebTable.setOrder(request.getSession(),"exams.o1",request.getParameter("o1"),1);
        WebTable.setOrder(request.getSession(),"exams.o2",request.getParameter("o2"),1);
        WebTable.setOrder(request.getSession(),"exams.o3",request.getParameter("o3"),1);
        WebTable.setOrder(request.getSession(),"exams.o4",request.getParameter("o4"),1);
        WebTable.setOrder(request.getSession(),"exams.o5",request.getParameter("o5"),1);
        WebTable.setOrder(request.getSession(),"exams.o6",request.getParameter("o6"),1);
        WebTable.setOrder(request.getSession(),"exams.o7",request.getParameter("o7"),1);
        
        if ("Export PDF".equals(myForm.getOp())) {
            FileOutputStream out = null;
            try {
                File file = ApplicationProperties.getTempFile("exams", "pdf");
                
                if (!instructorExams.isEmpty()) {
                    TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
                    for (Exam exam : instructorExams) exams.add(new ExamAssignmentInfo(exam));

                    InstructorExamReport ir = new InstructorExamReport(
                            InstructorExamReport.sModeNormal, file, instructor.getDepartment().getSession(),
                            -1, null, exams);
                    ir.setM2d(true); ir.setDirect(true);
                    ir.printHeader();
                    ir.printReport(ExamInfo.createInstructorInfo(instructor), exams);
                    ir.lastPage();
                    ir.close();
                } else {
                    TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
                    TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>();
                    for (Exam exam : studentExams) {
                        ExamAssignmentInfo x = new ExamAssignmentInfo(exam);
                        exams.add(x);
                        sections.addAll(x.getSections());
                    }

                    StudentExamReport sr = new StudentExamReport(
                            StudentExamReport.sModeNormal, file, student.getSession(),
                            -1, null, exams);
                    sr.setM2d(true); sr.setBtb(true); sr.setDirect(true);
                    sr.printHeader();
                    sr.printReport(student, sections);
                    sr.lastPage();
                    sr.close();
                }
                
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out!=null) out.close();
                } catch (IOException e) {}
            }
        }
        
        if (instructor!=null && sessions.size()>1) {
            PdfWebTable table = getSessions(true, sessions, instructor.getName(DepartmentalInstructor.sNameFormatLastFist), instructor.getDepartment().getSession().getUniqueId());
            request.setAttribute("sessions", table.printTable(WebTable.getOrder(request.getSession(),"exams.o0")));
        } else if (student!=null && sessions.size()>1) {
            PdfWebTable table = getSessions(true, sessions, student.getName(DepartmentalInstructor.sNameFormatLastFist), student.getSession().getUniqueId());
            request.setAttribute("sessions", table.printTable(WebTable.getOrder(request.getSession(),"exams.o0")));
        }
        
        if (student!=null && !student.getClassEnrollments().isEmpty()) {
            PdfWebTable table =  getStudentClassSchedule(true, student);
            if (!table.getLines().isEmpty()) {
                request.setAttribute("clsschd", table.printTable(WebTable.getOrder(request.getSession(),"exams.o6")));
            }
        }
        
        if (instructor!=null) {
            PdfWebTable table = getInstructorClassSchedule(true, instructor);
            if (!table.getLines().isEmpty()) {
                request.setAttribute("iclsschd", table.printTable(WebTable.getOrder(request.getSession(),"exams.o7")));
            }
        }
        
        if (!studentExams.isEmpty()) {
            myForm.setCanExport(true);
            TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
            for (Exam exam : studentExams) exams.add(new ExamAssignmentInfo(exam));
            
            PdfWebTable table = getStudentExamSchedule(true, exams, student);
            request.setAttribute("schedule", table.printTable(WebTable.getOrder(request.getSession(),"exams.o1")));
            
            table = getStudentConflits(true, exams, student);
            if (!table.getLines().isEmpty())
                request.setAttribute("conf", table.printTable(WebTable.getOrder(request.getSession(),"exams.o3")));
        }
        
        if (!instructorExams.isEmpty()) {
            myForm.setCanExport(true);
            TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
            for (Exam exam : instructorExams) exams.add(new ExamAssignmentInfo(exam));
            
            PdfWebTable table = getInstructorExamSchedule(true, exams, instructor);
            request.setAttribute("ischedule", table.printTable(WebTable.getOrder(request.getSession(),"exams.o2")));
            
            table = getInstructorConflits(true, exams, instructor);
            if (!table.getLines().isEmpty())
                request.setAttribute("iconf", table.printTable(WebTable.getOrder(request.getSession(),"exams.o4")));

            table = getStudentConflits(true, exams, instructor);
            if (!table.getLines().isEmpty())
                request.setAttribute("sconf", table.printTable(WebTable.getOrder(request.getSession(),"exams.o5")));
        }

        return mapping.findForward("show");
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
                "exams.do?o0=%%",
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
                    "onClick=\"document.location='exams.do?session="+session.getUniqueId()+"';\"",
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
                "exams.do?o1=%%",
                new String[] {
                    "Class / Course",
                    "Meeting Time",
                    "Date",
                    "Time",
                    "Room"},
                new String[] {"left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        for (ExamAssignmentInfo exam : exams) {
            for (ExamSectionInfo section : exam.getSections()) {
                if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                table.addLine(
                        new String[] {
                                section.getName(),
                                getMeetingTime(section),
                                exam.getDate(false),
                                exam.getTime(false),
                                exam.getRoomsName(false,", ")
                        },
                        new Comparable[] {
                            new MultiComparable(section.getName(), exam),
                            new MultiComparable(getMeetingComparable(section), section.getName(), exam),
                            new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                            new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), section.getName(), exam),
                            new MultiComparable(exam.getRoomsName(":"), section.getName(), exam)
                        });
            }
        }
        return table;
    }
    
    public PdfWebTable getStudentConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, Student student) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
                student.getSession().getLabel()+" Examination Conflicts for "+student.getName(DepartmentalInstructor.sNameFormatLastFist),
                "exams.do?o3=%%",
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
                    classes += section.getName();
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += exam.getRoomsName(false, ", ");
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
                        classes += section.getName();
                        if (firstSection) {
                            room += conflict.getOtherExam().getRoomsName(false, ", ");
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
                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+(conflict.getOtherEventId()!=null?"Class":"Direct")+(html?"</font>":""),
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
                    classes += section.getName();
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += exam.getRoomsName(false, ", ");
                    }
                    firstSection = false;
                }
                firstSection = true;
                for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        blank+=nl; classes += nl; date += nl; time += nl; room += nl; distance += nl;
                    }
                    classes += section.getName();
                    if (firstSection) {
                        time += conflict.getOtherExam().getTime(false);
                        room += conflict.getOtherExam().getRoomsName(false, ", ");
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
                    classes += section.getName();
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += exam.getRoomsName(false, ", ");
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
                        classes += section.getName();
                        if (firstSection) {
                            time += other.getTime(false);
                            room += other.getRoomsName(false, ", ");
                        }
                        firstSection = false;
                    }
                }
                table.addLine(
                        "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
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
                "exams.do?o2=%%",
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
        for (ExamAssignmentInfo exam : exams) {
            for (ExamSectionInfo section : exam.getSections()) {
                table.addLine(
                        new String[] {
                                section.getName(),
                                String.valueOf(section.getNrStudents()),
                                Exam.sSeatingTypes[exam.getSeatingType()],
                                getMeetingTime(section),
                                exam.getDate(false),
                                exam.getTime(false),
                                exam.getRoomsName(false,", "),
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
    
    public PdfWebTable getInstructorConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
                instructor.getDepartment().getSession().getLabel()+" Examination Instructor Conflicts for "+instructor.getName(DepartmentalInstructor.sNameFormatLastFist),
                "exams.do?o4=%%",
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
                        seating += Exam.sExamTypes[exam.getExamType()];
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += exam.getRoomsName(false, ", ");
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
                            seating += Exam.sExamTypes[conflict.getOtherExam().getExamType()];
                            room += conflict.getOtherExam().getRoomsName(false, ", ");
                        }
                        firstSection = false;
                    }
                } else if (conflict.getOtherEventId()!=null) {
                    blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                    classes += conflict.getOtherEventName();
                    enrollment += conflict.getOtherEventSize();
                    seating += "Class";
                    room += conflict.getOtherEventRoom();
                    //date += conflict.getOtherEventDate();
                    time += conflict.getOtherEventTime(); 
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+(conflict.getOtherEventId()!=null?"Class":"Direct")+(html?"</font>":""),
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
                        seating += Exam.sExamTypes[exam.getExamType()];
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += exam.getRoomsName(false, ", ");
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
                        seating += Exam.sExamTypes[exam.getExamType()];
                        time += conflict.getOtherExam().getTime(false);
                        room += conflict.getOtherExam().getRoomsName(false, ", ");
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
                        seating += Exam.sExamTypes[exam.getExamType()];
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += exam.getRoomsName(false, ", ");
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
                            seating += Exam.sExamTypes[other.getExamType()];
                            time += other.getTime(false);
                            room += other.getRoomsName(false, ", ");
                        }
                        firstSection = false;
                    }
                }
                table.addLine(
                        "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
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
                "exams.do?o5=%%",
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
                            seating += Exam.sExamTypes[exam.getExamType()];
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += exam.getRoomsName(false, ", ");
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
                                seating += Exam.sExamTypes[conflict.getOtherExam().getExamType()];
                                room += conflict.getOtherExam().getRoomsName(false, ", ");
                            }
                            firstSection = false;
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        classes += conflict.getOtherEventName();
                        enrollment += conflict.getOtherEventSize();
                        seating += "Class";
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
                            seating += Exam.sExamTypes[exam.getExamType()];
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += exam.getRoomsName(false, ", ");
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
                            seating += Exam.sExamTypes[exam.getExamType()];
                            time += conflict.getOtherExam().getTime(false);
                            room += conflict.getOtherExam().getRoomsName(false, ", ");
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
                            seating += Exam.sExamTypes[exam.getExamType()];
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += exam.getRoomsName(false, ", ");
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
                                seating += Exam.sExamTypes[other.getExamType()];
                                time += other.getTime(false);
                                room += other.getRoomsName(false, ", ");
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
        } else {
            meetingTime += "Arr Hrs";
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
                "exams.do?o6=%%",
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
                "exams.do?o7=%%",
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
}