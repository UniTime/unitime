package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ExamScheduleByPeriodReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByPeriodReport.class);
    
    public ExamScheduleByPeriodReport(File file, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "SCHEDULE BY PERIOD", session, examType, exams);
    }

    public void printReport() throws DocumentException {
        setHeader(new String[] {
                "Date And Time                          Subj Crsnbr InsTyp Sect   Meeting Times                         Enrl",
                "-------------------------------------- ---- ------ ------ ---- -------------------------------------- -----"});
        printHeader();
        TreeSet<ExamAssignmentInfo> exams = new TreeSet(getExams());
        for (Iterator p=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();p.hasNext();) {
            ExamPeriod period = (ExamPeriod)p.next();
            iPeriodPrinted = false;
            setPageName(period.getName());
            setCont(period.getName());
            for (Iterator<ExamAssignmentInfo> i = exams.iterator(); i.hasNext();) {
                ExamAssignmentInfo exam = i.next();
                if (!period.equals(exam.getPeriod())) continue;
                if (iPeriodPrinted) {
                    if (!iNewPage) println("");
                }
                ExamSectionInfo lastSection = null;
                for (Iterator<ExamSectionInfo> j = exam.getSections().iterator(); j.hasNext();) {
                    ExamSectionInfo  section = j.next();
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

                    String meetingTime = "";
                    if (section.getOwner().getOwnerObject() instanceof Class_) {
                        Assignment assignment = ((Class_)section.getOwner().getOwnerObject()).getCommittedAssignment();
                        if (assignment!=null) {
                            String dpat = "";
                            DatePattern dp = assignment.getDatePattern();
                            if (dp!=null && !dp.isDefault()) {
                                if (dp.getType().intValue()==DatePattern.sTypeAlternate)
                                    dpat = " "+dp.getName();
                                else {
                                    SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
                                    dpat = ", "+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate());
                                }
                            }
                            TimeLocation t = assignment.getTimeLocation();
                            meetingTime = t.getDayHeader()+" "+t.getStartTimeHeader()+" - "+t.getEndTimeHeader()+dpat;
                        }
                    }
                    
                    println(
                            rpad(iPeriodPrinted?"":period.getName(),38)+" "+
                            rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                            rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                            rpad(iStudentPrinted?"":section.getItype(), 6)+" "+
                            lpad(section.getSection(),4)+" "+
                            rpad(meetingTime,38)+" "+
                            lpad(String.valueOf(section.getNrStudents()),5)
                            );
                    iPeriodPrinted = !iNewPage;
                }
            }
            setCont(null);
            if (p.hasNext()) {
                newPage();
            }
        }
        lastPage();
    }
}
