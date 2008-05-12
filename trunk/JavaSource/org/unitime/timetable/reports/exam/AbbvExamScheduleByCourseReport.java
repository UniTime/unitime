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
                    boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                    if ((lx%n)!=0 && last!=null) {
                        if (last.getSubject().equals(section.getSubject())) { 
                            sameSubj = true;
                            if (last.getCourseNbr().equals(section.getCourseNbr())) {
                                sameCrs = true;
                                if (last.getSection().equals(section.getSection()))
                                    sameSct = true;
                                if (last.getItype().equals(section.getItype()))
                                    sameItype = true;
                            }
                        } 
                    }
                    last = section; lx++;
                    if (firstSection) {
                        if (iItype) {
                            lines.add(
                                 rpad(sameSubj?"":section.getSubject(),4)+" "+
                                 rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                 rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                 lpad(sameSct?"":section.getSection(),3)+" "+
                                formatShortPeriod(section.getExamAssignment().getPeriod()));
                        } else {
                            lines.add(
                                    rpad(sameSubj?"":section.getSubject(),4)+" "+
                                    rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                    rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3)+"  "+
                                    formatPeriod(exam.getPeriod()));
                        }
                    } else {
                        if (iItype) {
                            String w = "w/"+(sameCrs?"":section.getSubject()+" ")+
                                (sameCrs?"":section.getCourseNbr()+" ")+
                                (sameItype?"":(section.getItype().length()==0?"ALL":section.getItype())+" ")+
                                (sameSct?"":section.getSection()); 
                            if (w.length()<20) w = lpad(w,20);
                            lines.add(w);
                        } else {
                            String w = "w/"+
                            (sameCrs?"":section.getSubject()+" ")+
                            (sameCrs?"":section.getCourseNbr()+" ")+
                            (sameSct?"":section.getSection().length()==0?"ALL":section.getSection());
                            if (w.length()<14) w = lpad(w, 14);
                            lines.add(w);
                        }
                    }
                    firstSection = false;
                }
            }
            if (iItype) {
                setHeader(new String[] {
                        "Subj CrsNr InsTp Sct Date    Time          | Subj CrsNr InsTp Sct Date    Time          | Subj CrsNr InsTp Sct Date    Time         ",
                        "---- ----- ----- --- ------- ------------- | ---- ----- ----- --- ------- ------------- | ---- ----- ----- --- ------- -------------"});
            } else {
                setHeader(new String[] {
                    "  Subj CrsNr Sct  Date      Time            | Subj CrsNr Sct  Date      Time            | Subj CrsNr Sct  Date      Time           ",
                    "  ---- ----- ---  --------- --------------- | ---- ----- ---  --------- --------------- | ---- ----- ---  --------- ---------------"});
            }
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=3*n) {
                for (int i=0;i<n;i++) {
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                    String c = (i+idx+2*n<lines.size()?lines.elementAt(i+idx+2*n):"");
                    if (iItype)
                        println(rpad(a,42)+" | "+rpad(b,42)+" | "+c);
                    else
                        println("  "+rpad(a,41)+" | "+rpad(b,41)+" | "+c);
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
                    boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                    if ((lx%n)!=0 && last!=null) {
                        if (section!=null && last.getSubject().equals(section.getSubject())) { 
                            sameSubj = true;
                            if (last.getCourseNbr().equals(section.getCourseNbr())) {
                                sameCrs = true;
                                if (last.getSection().equals(section.getSection()))
                                    sameSct = true;
                                if (last.getItype().equals(section.getItype()))
                                    sameItype = true;
                            }
                        } 
                    }
                    if (i==0) {
                        if (iItype) {
                            lines.add(
                                     rpad(sameSubj?"":section.getSubject(),4)+" "+
                                     rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                     rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                     lpad(sameSct?"":section.getSection(),3)+" "+
                                    formatShortPeriod(section.getExamAssignment().getPeriod())+" "+a+" "+b);
                            } else {
                                lines.add(
                                        (iItype?rpad(section.getName(),14):
                                            rpad(sameSubj?"":section.getSubject(),4)+" "+
                                            rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                            rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3))+"  "+
                                        formatPeriod(section.getExamAssignment().getPeriod())+" "+a+" "+b);
                            }
                    } else if (section!=null) {
                        if (iItype) {
                            String w = "w/"+(sameCrs?"":section.getSubject()+" ")+
                                (sameCrs?"":section.getCourseNbr()+" ")+
                                (sameItype?"":(section.getItype().length()==0?"ALL":section.getItype())+" ")+
                                (sameSct?"":section.getSection()); 
                            if (w.length()<20) w = lpad(w,20);
                            lines.add(rpad(w,43)+a+" "+b);
                        } else {
                            String w = "w/"+
                            (sameCrs?"":section.getSubject()+" ")+
                            (sameCrs?"":section.getCourseNbr()+" ")+
                            (sameSct?"":section.getSection().length()==0?"ALL":section.getSection());
                            if (w.length()<14) w = lpad(w, 14);
                            lines.add(rpad(w,42)+a+" "+b);
                        }
                    } else {
                        lines.add(rpad("",(iItype?43:42))+a+" "+b);
                    }
                    lx++;
                }
            }
            if (iItype) {
                setHeader(new String[] {
                    "Subj CrsNr InsTp Sct Date    Time          Bldg  Room  Bldg  Room | Subj CrsNr InsTp Sct Date    Time          Bldg  Room  Bldg  Room",
                    "---- ----- ----- --- ------- ------------- ----- ----- ----- ---- | ---- ----- ----- --- ------- ------------- ----- ----- ----- ----"});
            } else {
                setHeader(new String[] {
                    "Subj CrsNr Sct  Date      Time            Bldg  Room  Bldg  Room  | Subj CrsNr Sct  Date      Time            Bldg  Room  Bldg  Room ",
                    "---- ----- ---  --------- --------------- ----- ----- ----- ----- | ---- ----- ---  --------- --------------- ----- ----- ----- -----"});
            }
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=2*n) {
                for (int i=0;i<n;i++) {
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):rpad("",65));
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):rpad("",65));
                    println(rpad(a,66)+"|"+b);
                }
            }
        }
    }
}
