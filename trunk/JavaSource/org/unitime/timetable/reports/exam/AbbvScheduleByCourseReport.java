package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class AbbvScheduleByCourseReport extends PdfLegacyExamReport {
    public AbbvScheduleByCourseReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY COURSE", session, examType, subjectArea, exams);
    }

    public void printReport() throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSections()) {
                if (getSubjectArea()!=null && !getSubjectArea().getSubjectAreaAbbreviation().equals(section.getSubject())) continue;
                sections.add(section);
            }
        }
        Vector<String> lines = new Vector();
        int n = iNrLines - 2;
        if (!iDispRooms) {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamSectionInfo section : sections) {
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
                if (iItype) {
                    lines.add(
                        (rpad(sameSubj?"":section.getSubject(),4)+" "+
                         rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                         rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                         lpad(sameSct?"":section.getSection(),3))+" "+
                        formatShortPeriod(section.getExamAssignment().getPeriod()));
                } else {
                    lines.add(rpad(sameSubj?"":section.getSubject(),4)+" "+
                            rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                            lpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3)+"  "+
                            formatPeriod(section.getExamAssignment().getPeriod()));
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
            for (ExamSectionInfo section : sections) {
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
                if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                    if (iItype) {
                        lines.add(
                                 rpad(sameSubj?"":section.getSubject(),4)+" "+
                                 rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                 rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                 lpad(sameSct?"":section.getSection(),3)+" "+
                                formatShortPeriod(section.getExamAssignment().getPeriod())+" "+
                                rpad(iNoRoom,23));
                        } else {
                            lines.add(
                                        rpad(sameSubj?"":section.getSubject(),4)+" "+
                                        rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                        rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3)+"  "+
                                    formatPeriod(section.getExamAssignment().getPeriod())+" "+
                                    rpad(iNoRoom,23));
                        }
                } else {
                    Vector<ExamRoomInfo> rooms = new Vector(section.getExamAssignment().getRooms());
                    for (int i=0;i<rooms.size();i+=2) {
                        ExamRoomInfo a = rooms.elementAt(i);
                        ExamRoomInfo b = (i+1<rooms.size()?rooms.elementAt(i+1):null);
                        if (i==0) {
                            if (iItype) {
                                lines.add(
                                         rpad(sameSubj?"":section.getSubject(),4)+" "+
                                         rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                         rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                         lpad(sameSct?"":section.getSection(),3)+" "+
                                        formatShortPeriod(section.getExamAssignment().getPeriod())+" "+
                                        formatRoom(a.getName())+" "+formatRoom(b==null?"":b.getName()));
                                } else {
                                    lines.add(
                                            (iItype?rpad(section.getName(),14):
                                                rpad(sameSubj?"":section.getSubject(),4)+" "+
                                                rpad(sameCrs?"":section.getCourseNbr(),5)+" "+
                                                rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),3))+"  "+
                                            formatPeriod(section.getExamAssignment().getPeriod())+" "+
                                            formatRoom(a.getName())+" "+formatRoom(b==null?"":b.getName()));
                                }
                        } else {
                            lines.add(
                                    rpad("",(iItype?43:42))+
                                    formatRoom(a.getName())+" "+formatRoom(b==null?"":b.getName()));
                            lx++;
                        }
                    }
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
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                    println(rpad(a,66)+"|"+b);
                }
            }
        }
    }
}
