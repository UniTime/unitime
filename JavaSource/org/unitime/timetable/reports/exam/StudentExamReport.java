package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class StudentExamReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(InstructorExamReport.class);
    Hashtable<Long,String> iStudentNames = new Hashtable();
    
    public StudentExamReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "STUDENT EXAMINATION SCHEDULE", session, examType, subjectArea, exams);
    }
    
    public void printReport() throws DocumentException {
        Hashtable<Long,TreeSet<ExamSectionInfo>> sections = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            for (ExamSectionInfo section:exam.getSections()) {
                for (Long studentId : section.getStudentIds()) {
                    TreeSet<ExamSectionInfo> sectionsThisStudent = sections.get(studentId);
                    if (sectionsThisStudent==null) {
                        sectionsThisStudent = new TreeSet<ExamSectionInfo>();
                        sections.put(studentId, sectionsThisStudent);
                    }
                    sectionsThisStudent.add(section);
                }
            }
        }
        boolean firstStudent = true;
        printHeader();
        TreeSet<Long> students = new TreeSet<Long>(new Comparator<Long>() {
            public int compare(Long s1, Long s2) {
                return new StudentDAO().get(s1).compareTo(new StudentDAO().get(s2));
            }
        });
        students.addAll(sections.keySet());
        for (Long studentId : students) {
            if (!firstStudent) newPage();
            printReport(studentId, sections.get(studentId));
            firstStudent = false;
        }
        lastPage();
    }
    
    public void printReport(Long studentId) throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        for (ExamAssignmentInfo exam : getExams())
            for (ExamSectionInfo section : exam.getSections())
                if (section.getStudentIds().contains(studentId)) sections.add(section);
        if (sections.isEmpty()) return;
        printHeader();
        printReport(studentId, sections);
        lastPage();
    }

    public void printReport(Long studentId, TreeSet<ExamSectionInfo> sections) throws DocumentException {
        Student student = new StudentDAO().get(studentId);
        String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
        String shortName = student.getName(DepartmentalInstructor.sNameFormatLastInitial).toUpperCase();
        setPageName(shortName);
        setCont(shortName);
        println("Name:  "+name);
        if (student.getEmail()!=null)
            println("Email:       "+student.getEmail());
        Date lastChange = null;
        String changeObject = null;
        println("");
        setHeader(new String[]{
                "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect   Meeting Times                          Date And Time                   Room      ",
                "---- ------ "+(iItype?"------ ":"")+"---- -------------------------------------- -------------------------------- -----------"});
        println(mpad("~ ~ ~ ~ ~ EXAMINATION SECHEDULE ~ ~ ~ ~ ~",iNrChars));
        for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
        iSubjectPrinted = false; String lastSubject = null;
        iCoursePrinted = false; String lastCourse = null;
        iITypePrinted = false; String lastItype = null;
        iPeriodPrinted = false; String lastSection = null;
        for (ExamSectionInfo section : sections) {
            if (iSubjectPrinted && !section.getSubject().equals(lastSubject)) { iSubjectPrinted = false; iCoursePrinted = false; iITypePrinted = false; iPeriodPrinted = false; }
            if (iCoursePrinted && !section.getCourseNbr().equals(lastCourse)) { iCoursePrinted = false; iITypePrinted = false; iPeriodPrinted = false; }
            if (iITypePrinted && !section.getItype().equals(lastItype)) { iITypePrinted = false; iPeriodPrinted = false; }
            if (iPeriodPrinted && !section.getSection().equals(lastSection)) { iPeriodPrinted = false; }
            if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                println(
                        rpad(iSubjectPrinted?"":section.getSubject(), 4)+" "+
                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                        (iItype?rpad(iITypePrinted?"":section.getItype(), 6)+" ":"")+
                        lpad(iPeriodPrinted?"":section.getSection(), 4)+" "+
                        rpad(getMeetingTime(section),38)+" "+
                        rpad((section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                        (section.getExamAssignment()==null?"":iNoRoom)
                        );
            } else {
                if (getLineNumber()+section.getExamAssignment().getRooms().size()>iNrLines) newPage();
                boolean firstRoom = true;
                for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                    println(
                            rpad(!firstRoom || iSubjectPrinted?"":section.getSubject(), 4)+" "+
                            rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                            (iItype?rpad(!firstRoom || iITypePrinted?"":section.getItype(), 6)+" ":"")+
                            lpad(!firstRoom || iPeriodPrinted?"":section.getSection(), 4)+" "+
                            rpad(!firstRoom?"":getMeetingTime(section),38)+" "+
                            rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                            formatRoom(room.getName()));
                    firstRoom = false;
                }
            }
            if (iNewPage) {
                iSubjectPrinted = iITypePrinted = iCoursePrinted = iPeriodPrinted = false;
                lastSubject = lastItype = lastCourse = lastSection = null;
            } else {
                iSubjectPrinted = iITypePrinted = iCoursePrinted = iPeriodPrinted = true;
                lastSubject = section.getSubject();
                lastItype = section.getItype();
                lastCourse = section.getCourseNbr();
                lastSection = section.getSection();
            }
        }
        println("");
        
        boolean headerPrinted = false;
        lastSubject = null;
        for (ExamSectionInfo section : sections) {
            iSubjectPrinted = (!iNewPage && lastSubject!=null && lastSubject.equals(section.getSubject()));
            ExamAssignmentInfo exam = section.getExamAssignmentInfo();
            if (exam==null || exam.getPeriod()==null) continue;
            ExamPeriod period = exam.getPeriod();
            iCoursePrinted = false;
                if (iDirect) for (DirectConflict conflict : exam.getDirectConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            if (!headerPrinted) {
                                if (!iNewPage) println("");
                                setHeader(null);
                                if (getLineNumber()+5>=iNrLines) newPage();
                                setHeader(new String[] {
                                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                        "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                                println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                                setCont(shortName+"  STUDENT CONFLICTS");
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        if (!headerPrinted) {
                            if (!iNewPage) println("");
                            setHeader(null);
                            if (getLineNumber()+5>=iNrLines) newPage();
                            setHeader(new String[] {
                                    "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                    "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                            println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                            for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                            setCont(shortName+"  STUDENT CONFLICTS");
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                rpad(iPeriodPrinted?"":"CLASS",6)+" "+
                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),4)+" "+
                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),6)+" "+
                                (iItype?rpad(conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6)+" ":"")+
                                lpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(),4)+" "+
                                getMeetingTime(conflict.getOtherEventTime())
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
                }
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        for (ExamSectionInfo other : otherExam.getSections()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            if (!headerPrinted) {
                                if (!iNewPage) println("");
                                setHeader(null);
                                if (getLineNumber()+5>=iNrLines) newPage();
                                setHeader(new String[] {
                                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                        "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                                println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                                setCont(shortName+"  STUDENT CONFLICTS");
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iPeriodPrinted?"":">2-DAY",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    }
                }
                if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                        if (!other.getStudentIds().contains(studentId)) continue;
                        if (!headerPrinted) {
                            if (!iNewPage) println("");
                            setHeader(null);
                            if (getLineNumber()+5>=iNrLines) newPage();
                            setHeader(new String[] {
                                    "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                    "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                            println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                            for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                            setCont(shortName+"  STUDENT CONFLICTS");
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                rpad(iPeriodPrinted?"":"BTB",6)+" "+
                                rpad(other.getSubject(),4)+" "+
                                rpad(other.getCourseNbr(),6)+" "+
                                (iItype?rpad(other.getItype(),6)+" ":"")+
                                lpad(other.getSection(),4)+" "+
                                other.getExamAssignment().getTimeFixedLength()
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
            }
        }
        
        setHeader(null);
        setCont(null);
    }
}
