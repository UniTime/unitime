package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class AbbvExamScheduleByCourseReport extends PdfLegacyExamReport {
    public AbbvExamScheduleByCourseReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY COURSE", session, examType, subjectArea, exams);
    }

    public void printReport() throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        Vector<String> lines = new Vector();
        int n = iNrLines - 2;
        if (!iDispRooms) {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSections()) {
                    boolean sameSubj = false, sameCrs = false, sameSct = false;
                    if ((lx%n)!=0 && last!=null) {
                        if (last.getSubject().equals(section.getSubject())) { 
                            sameSubj = true;
                            if (last.getCourseNbr().equals(section.getCourseNbr())) {
                                sameCrs = true;
                                if (last.getSection().equals(section.getSection()))
                                    sameSct = true;
                            }
                        } 
                    }
                    last = section; lx++;
                    if (firstSection)
                        lines.add(
                                (iItype?rpad(section.getName(),14):
                                    rpad(sameSubj?"":section.getSubject(),4)+" "+
                                    rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                    rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3))+"  "+
                                    formatPeriod(exam.getPeriod()));
                    else
                        lines.add(rpad(lpad("w/"+
                                (iItype?rpad(section.getName(),14):
                                    (sameCrs?"":section.getSubject()+" ")+
                                    (sameCrs?"":section.getCourseNbr()+" ")+
                                    (sameSct?"":section.getSection().length()==0?"ALL":section.getSection())),16),41));
                    firstSection = false;
                }
            }
            setHeader(new String[] {
                    "  Subj CrsNr Sct  Date      Time            | Subj CrsNr Sct  Date      Time            | Subj CrsNr Sct  Date      Time           ",
                    "  ---- ----- ---  --------- --------------- | ---- ----- ---  --------- --------------- | ---- ----- ---  --------- ---------------"});
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=3*n) {
                for (int i=0;i<n;i++) {
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):rpad("",41));
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):rpad("",41));
                    String c = (i+idx+2*n<lines.size()?lines.elementAt(i+idx+2*n):rpad("",41));
                    println("  "+a+" | "+b+" | "+c);
                }
            }
        } else {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                Vector<String> rooms = new Vector();
                if (exam.getRooms()==null || exam.getRooms().isEmpty()) {
                    rooms.add(rpad(iNoRoom,11));
                } else for (ExamRoomInfo room : exam.getRooms()) {
                    rooms.add(formatRoom(room.getName()));
                }
                for (int i=0;i<Math.max((rooms.size()+1)/2,exam.getSections().size());i++) {
                    String a = (2*i+0<rooms.size()?rooms.elementAt(2*i+0):rpad("",11));
                    String b = (2*i+1<rooms.size()?rooms.elementAt(2*i+1):rpad("",11));
                    ExamSectionInfo section = (i<exam.getSections().size()?exam.getSections().elementAt(i):null);
                    boolean sameSubj = false, sameCrs = false, sameSct = false;
                    if ((lx%n)!=0 && last!=null) {
                        if (section!=null && last.getSubject().equals(section.getSubject())) { 
                            sameSubj = true;
                            if (last.getCourseNbr().equals(section.getCourseNbr())) {
                                sameCrs = true;
                                if (last.getSection().equals(section.getSection()))
                                    sameSct = true;
                            }
                        } 
                    }
                    if (i==0) {
                        lines.add(
                                (iItype?rpad(section.getName(),14):
                                    rpad(sameSubj?"":section.getSubject(),4)+" "+
                                    rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                    rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3))+"  "+
                                formatPeriod(section.getExamAssignment().getPeriod())+" "+
                                a+" "+b);
                        last = section;
                    } else if (section!=null) {
                        lines.add(rpad(lpad("w/"+
                                (iItype?rpad(section.getName(),14):
                                    (sameCrs?"":section.getSubject()+" ")+
                                    (sameCrs?"":section.getCourseNbr()+" ")+
                                    (sameSct?"":section.getSection().length()==0?"ALL":section.getSection())),16),41)+" "+a+" "+b);
                    } else {
                        lines.add(rpad("",41)+" "+a+" "+b);
                    }
                    lx++;
                }
            }
            setHeader(new String[] {
                    "Subj CrsNr Sct  Date      Time            Bldg  Room  Bldg  Room  | Subj CrsNr Sct  Date      Time            Bldg  Room  Bldg  Room ",
                    "---- ----- ---  --------- --------------- ----- ----- ----- ----- | ---- ----- ---  --------- --------------- ----- ----- ----- ----- "});
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=2*n) {
                for (int i=0;i<n;i++) {
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):rpad("",65));
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):rpad("",65));
                    println(a+" | "+b);
                }
            }
        }
    }
}
