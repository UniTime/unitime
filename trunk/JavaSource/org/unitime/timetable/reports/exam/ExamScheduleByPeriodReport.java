package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ExamScheduleByPeriodReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ExamScheduleByPeriodReport.class);
    
    public ExamScheduleByPeriodReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY PERIOD", session, examType, subjectArea, exams);
    }

    
    public void printReport() throws DocumentException {
        setHeader(new String[] {
                "Date And Time                          Subj Crsnbr "+(iItype?iExternal?"ExtnID ":"InsTyp ":"")+"Sect    Meeting Times                         Enrl"+(iDispRooms?"  Room         Cap ExCap":""),
                "-------------------------------------- ---- ------ "+(iItype?"------ ":"")+"----- -------------------------------------- -----"+(iDispRooms?" ----------- ----- -----":"")});
        printHeader();
        sLog.debug("  Sorting exams...");
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null || !exam.isOfSubjectArea(getSubjectArea())) continue;
            exams.add(exam);
        }
        sLog.debug("  Printing report...");
        for (Iterator p=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();p.hasNext();) {
            ExamPeriod period = (ExamPeriod)p.next();
            iPeriodPrinted = false;
            setPageName(formatPeriod(period));
            setCont(formatPeriod(period));
            for (Iterator<ExamAssignmentInfo> i = exams.iterator(); i.hasNext();) {
                ExamAssignmentInfo exam = i.next();
                if (!period.equals(exam.getPeriod())) continue;
                if (iPeriodPrinted) {
                    if (!iNewPage) println("");
                }
                ExamSectionInfo lastSection = null;
                for (Iterator<ExamSectionInfo> j = exam.getSections().iterator(); j.hasNext();) {
                    ExamSectionInfo  section = j.next();
                    if (getSubjectArea()!=null && !getSubjectArea().getSubjectAreaAbbreviation().equals(section.getSubject())) continue;
                    iSubjectPrinted = iCoursePrinted = iStudentPrinted = false;
                    if (lastSection!=null) {
                        if (section.getSubject().equals(lastSection.getSubject())) {
                            iSubjectPrinted = true;
                            if (section.getCourseNbr().equals(lastSection.getCourseNbr())) {
                                iCoursePrinted = true;
                                if (section.getItype().equals(lastSection.getItype())) {
                                    iStudentPrinted = true;
                                }
                            }
                        }
                    } 
                    lastSection = section;

                    if (!iDispRooms) {
                        println(
                            rpad(iPeriodPrinted?"":formatPeriod(period),38)+" "+
                            rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                            rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                            (iItype?rpad(iStudentPrinted?"":section.getItype(), 6)+" ":"")+
                            lpad(section.getSection(),5)+" "+
                            rpad(getMeetingTime(section),38)+" "+
                            lpad(String.valueOf(section.getNrStudents()),5)
                            );
                        iPeriodPrinted = !iNewPage;
                    } else {
                        if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                            println(
                                    rpad(iPeriodPrinted?"":formatPeriod(period),38)+" "+
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iStudentPrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(section.getSection(),5)+" "+
                                    rpad(getMeetingTime(section),38)+" "+
                                    lpad(String.valueOf(section.getNrStudents()),5)+" "+iNoRoom
                                    );
                            iPeriodPrinted = !iNewPage;
                        } else {
                            if (getLineNumber()+section.getExamAssignment().getRooms().size()>iNrLines) newPage();
                            boolean firstRoom = true;
                            for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                                println(
                                        rpad(!firstRoom || iPeriodPrinted?"":formatPeriod(period),38)+" "+
                                        rpad(!firstRoom || iSubjectPrinted?"":section.getSubject(),4)+" "+
                                        rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                        (iItype?rpad(!firstRoom || iStudentPrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(!firstRoom?"":section.getSection(),5)+" "+
                                        rpad(!firstRoom?"":getMeetingTime(section),38)+" "+
                                        lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5)+" "+
                                        formatRoom(room.getName())+" "+
                                        lpad(""+room.getCapacity(),5)+" "+
                                        lpad(""+room.getExamCapacity(),5)
                                        );
                                firstRoom = false;
                            }
                            iPeriodPrinted = !iNewPage;
                        }
                    }
                    
                }
            }
            setCont(null);
            if (iPeriodPrinted && p.hasNext()) {
                newPage();
            }
        }
        if (iPeriodPrinted) lastPage();
    }
}
