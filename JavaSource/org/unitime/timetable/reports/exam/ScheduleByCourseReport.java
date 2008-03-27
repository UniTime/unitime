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
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ScheduleByCourseReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    
    public ScheduleByCourseReport(File file, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "SCHEDULE BY COURSE", session, examType, exams);
    }

    public void printReport() throws DocumentException {
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
                "Subj Crsnbr InsTyp Sect   Meeting Times                         Enrl    Date And Time                   Room",
                "---- ------ ------ ---- -------------------------------------- -----  -------------------------------- ------------------------------"});
        printHeader();
        for (Iterator<String> i = new TreeSet<String>(subject2courseSections.keySet()).iterator(); i.hasNext();) {
            String subject = i.next();
            TreeSet<ExamSectionInfo> sections = subject2courseSections.get(subject);
            setPageName(subject); setCont(subject);
            iSubjectPrinted = false;
            iCoursePrinted = false; String lastCourse = null;
            iITypePrinted = false; String lastItype = null;
            for (Iterator<ExamSectionInfo> j = sections.iterator(); j.hasNext();) {
                ExamSectionInfo  section = j.next();
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
                if (iCoursePrinted && !section.getCourseNbr().equals(lastCourse)) { iCoursePrinted = false; iITypePrinted = false; }
                if (iITypePrinted && !section.getItype().equals(lastItype)) iITypePrinted = false;
                println(
                        rpad(iSubjectPrinted?"":subject, 4)+" "+
                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                        rpad(iITypePrinted?"":section.getItype(), 6)+" "+
                        lpad(section.getSection(), 4)+" "+
                        rpad(meetingTime,38)+" "+
                        lpad(String.valueOf(section.getNrStudents()),5)+"  "+
                        rpad((section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodName()),32)+" "+
                        (section.getExamAssignment()==null?"":section.getExamAssignment().getRoomsName(", "))
                        );
                if (iNewPage) {
                    iSubjectPrinted = iITypePrinted = iCoursePrinted = false;
                    lastItype = lastCourse = null;
                } else {
                    iSubjectPrinted = iITypePrinted = iCoursePrinted = true;
                    lastItype = section.getItype();
                    lastCourse = section.getCourseNbr();
                }
                if (j.hasNext()) { 
                    if (getLineNumber()<sNrLines) {
                        println(""); 
                    }
                }
            }
            setCont(null);
            if (i.hasNext()) {
                newPage();
            }
        }
        lastPage();        
    }
}
