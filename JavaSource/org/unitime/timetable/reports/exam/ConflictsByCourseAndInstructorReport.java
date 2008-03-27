package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ConflictsByCourseAndInstructorReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ConflictsByCourseAndStudentReport.class);
    Hashtable<Long,String> iStudentNames = new Hashtable();
    
    public ConflictsByCourseAndInstructorReport(File file, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "CONFLICTS BY COURSE AND INSTRUCTOR", session, examType, exams);
    }
    
    public void printReport() throws DocumentException {
        printReport(true, (getExamType()==Exam.sExamTypeFinal), (getExamType()==Exam.sExamTypeEvening));
    }

    public void printReport(boolean direct, boolean m2d, boolean btb) throws DocumentException {
        sLog.debug("  Sorting sections ...");
        Hashtable<String,TreeSet<ExamSectionInfo>> subject2courseSections = new Hashtable();
        for (ExamInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSections()) {
                TreeSet<ExamSectionInfo> sections = subject2courseSections.get(section.getSubject());
                if (sections==null) {
                    sections = new TreeSet();
                    subject2courseSections.put(section.getSubject(), sections);
                }
                sections.add(section);
            }
        }
        setHeader(new String[] {
                "Subj Crsnbr InsTyp Sect Date And Time                Name       Type   Subj Crsnbr InsTyp Sect Time",
                "---- ------ ------ ---- ---------------------------- ---------- ------ ---- ------ ------ ---- ---------------------"});
        printHeader();
        for (Iterator<String> i = new TreeSet<String>(subject2courseSections.keySet()).iterator(); i.hasNext();) {
            String subject = i.next();
            TreeSet<ExamSectionInfo> sections = subject2courseSections.get(subject);
            if (iSubjectPrinted) newPage();
            setPageName(subject); setCont(subject);
            iSubjectPrinted = false;
            for (Iterator<ExamSectionInfo> j = sections.iterator(); j.hasNext();) {
                ExamSectionInfo section = j.next();
                ExamAssignmentInfo exam = section.getExamAssignmentInfo();
                if (exam==null || exam.getPeriod()==null) continue;
                ExamPeriod period = exam.getPeriod();
                iCoursePrinted = false;
                for (ExamInstructorInfo instructor : exam.getInstructors()) {
                    iStudentPrinted = false;
                    if (direct) for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        if (conflict.getOtherExam()!=null) {
                            for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                                if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                                println(
                                        rpad(iSubjectPrinted?"":subject,4)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                        rpad(iCoursePrinted?"":section.getItype(), 6)+" "+
                                        lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                        rpad(iCoursePrinted?"":exam.getPeriodName(),28)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),10)+" "+
                                        rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                        rpad(other.getSubject(),4)+" "+
                                        rpad(other.getCourseNbr(),6)+" "+
                                        rpad(other.getItype(),6)+" "+
                                        lpad(other.getSection(),4)+" "+
                                        other.getExamAssignment().getTime(false)
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        } else if (conflict.getOtherAssignment()!=null) {
                            String dpat = "";
                            DatePattern dp = conflict.getOtherAssignment().getDatePattern();
                            if (dp!=null && !dp.isDefault()) {
                                if (dp.getType().intValue()==DatePattern.sTypeAlternate)
                                    dpat = " "+dp.getName();
                                else {
                                    SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
                                    dpat = ", "+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate());
                                }
                            }
                            TimeLocation t = conflict.getOtherAssignment().getTimeLocation();
                            String meetingTime = t.getDayHeader()+" "+t.getStartTimeHeader()+" - "+t.getEndTimeHeader()+dpat;
                            println(
                                    rpad(iSubjectPrinted?"":subject,4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    rpad(iCoursePrinted?"":section.getItype(), 6)+" "+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodName(),28)+" "+
                                    rpad(iStudentPrinted?"":instructor.getName(),10)+" "+
                                    rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                    rpad(conflict.getOtherAssignment().getClazz().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),4)+" "+
                                    rpad(conflict.getOtherAssignment().getClazz().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),6)+" "+
                                    rpad(conflict.getOtherAssignment().getClazz().getSchedulingSubpart().getItypeDesc(),6)+" "+
                                    lpad(conflict.getOtherAssignment().getClazz().getSectionNumberString(),4)+" "+
                                    meetingTime
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }
                    if (m2d) for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        for (ExamAssignment otherExam : conflict.getOtherExams()) {
                            if (!otherExam.getInstructors().contains(instructor)) continue;
                            for (ExamSectionInfo other : otherExam.getSections()) {
                                println(
                                        rpad(iSubjectPrinted?"":subject,4)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                        rpad(iCoursePrinted?"":section.getItype(), 6)+" "+
                                        lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                        rpad(iCoursePrinted?"":exam.getPeriodName(),28)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),10)+" "+
                                        rpad(iPeriodPrinted?"":">2-DAY",6)+" "+
                                        rpad(other.getSubject(),4)+" "+
                                        rpad(other.getCourseNbr(),6)+" "+
                                        rpad(other.getItype(),6)+" "+
                                        lpad(other.getSection(),4)+" "+
                                        other.getExamAssignment().getTime(false)
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        }
                    }
                    if (btb) for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                            if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                            println(
                                    rpad(iSubjectPrinted?"":subject,4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    rpad(iCoursePrinted?"":section.getItype(), 6)+" "+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodName(),28)+" "+
                                    rpad(iStudentPrinted?"":instructor.getName(),10)+" "+
                                    rpad(iPeriodPrinted?"":"BTB",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    rpad(other.getItype(),6)+" "+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTime(false)
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }                    
                }
            }
            setCont(null);
        }
        if (iSubjectPrinted) lastPage();
    }
}
