package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
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
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ScheduleByPeriodReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByPeriodReport.class);
    
    public ScheduleByPeriodReport(File file, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "SCHEDULE BY PERIOD", session, examType, exams);
    }

    public void printReport() throws DocumentException {
        sLog.debug("  Sorting sections ...");
        Hashtable<String,TreeSet<ExamSectionInfo>> subject2courseSections = new Hashtable();
        for (ExamInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSections()) {
                TreeSet<ExamSectionInfo> sections = subject2courseSections.get(section.getSubject());
                if (sections==null) {
                    sections = new TreeSet(new Comparator<ExamSectionInfo>() {
                        public int compare(ExamSectionInfo s1, ExamSectionInfo s2) {
                            return s1.getOwner().compareTo(s2.getOwner());
                        }
                    });
                    subject2courseSections.put(section.getSubject(), sections);
                }
                sections.add(section);
            }
        }
        setHeader(new String[] {
                "Date And Time                          Subj Crsnbr InsTyp Sect   Meeting Times                         Enrl",
                "-------------------------------------- ---- ------ ------ ---- -------------------------------------- -----"});
        printHeader();
        for (Iterator p=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();p.hasNext();) {
            ExamPeriod period = (ExamPeriod)p.next();
            iPeriodPrinted = false;
            setPageName(period.getName());
            setCont(period.getName());
            for (Iterator<String> i = new TreeSet<String>(subject2courseSections.keySet()).iterator(); i.hasNext();) {
                String subject = i.next();
                TreeSet<ExamSectionInfo> sections = subject2courseSections.get(subject);
                iSubjectPrinted = false;
                for (Iterator<ExamSectionInfo> j = sections.iterator(); j.hasNext();) {
                    ExamSectionInfo  section = j.next();
                    if (!period.equals(section.getExamAssignment().getPeriod())) continue;
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
                            rpad(iSubjectPrinted?"":subject,4)+" "+
                            rpad(section.getCourseNbr(), 6)+" "+
                            rpad(section.getItype(), 6)+" "+
                            lpad(section.getSection(),4)+" "+
                            rpad(meetingTime,38)+" "+
                            lpad(String.valueOf(section.getNrStudents()),5)
                            );
                    iPeriodPrinted = iSubjectPrinted = true;
                    iSubjectPrinted = iCoursePrinted = iStudentPrinted = true;
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
