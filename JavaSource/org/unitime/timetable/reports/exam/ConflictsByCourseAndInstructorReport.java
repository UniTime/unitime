package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
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
    
    public ConflictsByCourseAndInstructorReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "CONFLICTS BY COURSE AND INSTRUCTOR", session, examType, subjectArea, exams);
    }
    
    public void printReport() throws DocumentException {
        sLog.debug("  Sorting sections ...");
        Hashtable<String,TreeSet<ExamSectionInfo>> subject2courseSections = new Hashtable();
        for (ExamInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSections()) {
                if (getSubjectArea()!=null && !getSubjectArea().getSubjectAreaAbbreviation().equals(section.getSubject())) continue;
                TreeSet<ExamSectionInfo> sections = subject2courseSections.get(section.getSubject());
                if (sections==null) {
                    sections = new TreeSet();
                    subject2courseSections.put(section.getSubject(), sections);
                }
                sections.add(section);
            }
        }
        setHeader(new String[] {
                "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Name                       Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time",
                "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
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
                    if (iDirect) for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        if (conflict.getOtherExam()!=null) {
                            for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                                if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                                println(
                                        rpad(iSubjectPrinted?"":subject,4)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                        rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                        rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                        rpad(other.getSubject(),4)+" "+
                                        rpad(other.getCourseNbr(),6)+" "+
                                        (iItype?rpad(other.getItype(),6)+" ":"")+
                                        lpad(other.getSection(),4)+" "+
                                        other.getExamAssignment().getTimeFixedLength()
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        } else if (conflict.getOtherEventId()!=null) {
                            println(
                                    rpad(iSubjectPrinted?"":subject,4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                    rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),4)+" "+
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),6)+" "+
                                    (iItype?rpad(conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6)+" ":"")+
                                    lpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(),4)+" "+
                                    getMeetingTime(conflict.getOtherEventTime())
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }
                    if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        for (ExamAssignment otherExam : conflict.getOtherExams()) {
                            if (!otherExam.getInstructors().contains(instructor)) continue;
                            for (ExamSectionInfo other : otherExam.getSections()) {
                                println(
                                        rpad(iSubjectPrinted?"":subject,4)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                        rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                        rpad(iPeriodPrinted?"":">2-DAY",6)+" "+
                                        rpad(other.getSubject(),4)+" "+
                                        rpad(other.getCourseNbr(),6)+" "+
                                        (iItype?rpad(other.getItype(),6)+" ":"")+
                                        lpad(other.getSection(),4)+" "+
                                        other.getExamAssignment().getTimeFixedLength()
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        }
                    }
                    if (iBtb) for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                            if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                            println(
                                    rpad(iSubjectPrinted?"":subject,4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                    rpad(iPeriodPrinted?"":"BTB",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
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
