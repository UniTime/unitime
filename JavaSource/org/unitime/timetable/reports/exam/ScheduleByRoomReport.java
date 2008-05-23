package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class ScheduleByRoomReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    
    public ScheduleByRoomReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY ROOM", session, examType, subjectArea, exams);
    }

    public void printReport() throws DocumentException {
        setHeader(new String[] {
                "Bldg  Room  Capacity  ExCap Period Date And Time                          Subj Crsnbr "+(iItype?iExternal?"ExtnID ":"InsTyp ":"")+"Sect  Enrl",
                "----- ----- -------- ------ ------ -------------------------------------- ---- ------ "+(iItype?"------ ":"")+"---- -----"});
        printHeader();
        Vector periods = new Vector(ExamPeriod.findAll(getSession().getUniqueId(), getExamType()));
        for (Iterator i = Location.findAllExamLocations(getSession().getUniqueId(), getExamType()).iterator(); i.hasNext();) {
            Location location = (Location)i.next();
            Room room = (location instanceof Room ? (Room)location : null);
            iPeriodPrinted = false;
            setPageName(location.getLabel());
            setCont(location.getLabel());
            ExamPeriod lastPeriod = null;
            for (Iterator j=periods.iterator();j.hasNext();) {
                ExamPeriod period = (ExamPeriod)j.next();
                if (lastPeriod!=null && !lastPeriod.getDateOffset().equals(period.getDateOffset()) && !iNewPage) println("");
                iStudentPrinted = false;
                TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>();
                for (ExamAssignmentInfo exam : getExams()) {
                    if (exam.hasRoom(location.getUniqueId()) && period.equals(exam.getPeriod())) {
                        iSubjectPrinted = iCoursePrinted = iITypePrinted = false;
                        ExamSectionInfo lastSection = null;
                        for (ExamSectionInfo section : exam.getSections()) {
                            if (getSubjectArea()!=null && !getSubjectArea().getSubjectAreaAbbreviation().equals(section.getSubject())) continue;
                            if (lastSection!=null && iSubjectPrinted) {
                                if (section.getSubject().equals(lastSection.getSubject())) {
                                    iSubjectPrinted = true;
                                    if (section.getCourseNbr().equals(lastSection.getCourseNbr())) {
                                        iCoursePrinted = true;
                                        if (section.getItype().equals(lastSection.getItype())) {
                                            iITypePrinted = true;
                                        }
                                    }
                                }
                            }
                            println(
                                    (room!=null?
                                            rpad(iPeriodPrinted?"":room.getBuildingAbbv(),5)+" "+
                                            rpad(iPeriodPrinted?"":room.getRoomNumber(),5)+" "
                                            :
                                            rpad(iPeriodPrinted?"":location.getLabel(),9)
                                    )+
                                    lpad(iPeriodPrinted?"":String.valueOf(location.getCapacity()),8)+" "+
                                    lpad(iPeriodPrinted?"":String.valueOf(location.getExamCapacity()),6)+" "+
                                    lpad(iStudentPrinted?"":String.valueOf(periods.indexOf(period)+1),6)+" "+
                                    rpad(iStudentPrinted?"":formatPeriod(period),38)+" "+
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iITypePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(section.getSection(),4)+" "+
                                    lpad(String.valueOf(section.getNrStudents()),5)
                                    
                                    );
                            iPeriodPrinted = iStudentPrinted = iSubjectPrinted = iCoursePrinted = iITypePrinted = !iNewPage;
                            lastSection = section;
                        }
                        //println("");
                    }
                }
                if (!iStudentPrinted) {
                    println(
                            (room!=null?
                                    rpad(iPeriodPrinted?"":room.getBuildingAbbv(),5)+" "+
                                    rpad(iPeriodPrinted?"":room.getRoomNumber(),5)+" "
                                    :
                                    rpad(iPeriodPrinted?"":location.getLabel(),11)+" "
                            )+
                            lpad(iPeriodPrinted?"":String.valueOf(location.getCapacity()),8)+" "+
                            lpad(iPeriodPrinted?"":String.valueOf(location.getExamCapacity()),6)+" "+
                            lpad(String.valueOf(periods.indexOf(period)+1),6)+" "+
                            rpad(formatPeriod(period),38)
                            );
                    iPeriodPrinted = !iNewPage;
                    //println("");
                }
                lastPeriod = period;
            }
            setCont(null);
            if (i.hasNext()) {
                newPage();
            }
        }
        lastPage();        
    }
}
