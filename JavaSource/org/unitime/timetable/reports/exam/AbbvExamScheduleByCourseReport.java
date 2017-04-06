/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class AbbvExamScheduleByCourseReport extends PdfLegacyExamReport {
    public AbbvExamScheduleByCourseReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY COURSE", session, examType, subjectAreas, exams);
    }

    public void printReport() throws DocumentException {
        Vector<String> lines = new Vector();
        int n = iNrLines - 2;
        if (!iDispRooms) {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
                boolean firstSection = true;
                TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>(new Comparator<ExamSectionInfo>() {
                    public int compare(ExamSectionInfo s1, ExamSectionInfo s2) {
                        if (!hasSubjectAreas()) return s1.compareTo(s2);
                        if (hasSubjectArea(s1.getOwner().getCourse().getSubjectArea())) {
                            if (!hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return -1;
                        } else if (hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return 1;
                        return s1.compareTo(s2);
                    }
                 });
                 sections.addAll(exam.getSectionsIncludeCrosslistedDummies());
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
                    if (firstSection) {
                        if (iItype) {
                            lines.add(
                                 rpad(sameSubj?"":section.getSubject(),7)+" "+
                                 rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                 rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                 lpad(sameSct?"":section.getSection(),9)+"  "+
                                formatPeriod(exam));
                        } else {
                            lines.add(
                                    rpad(sameSubj?"":section.getSubject(),7)+" "+
                                    rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                    lpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),9)+"  "+
                                    formatShortPeriodNoEndTime(exam));
                        }
                    } else {
                        if (iItype) {
                            String w = "w/"+(sameCrs?"":section.getSubject()+" ")+
                                (sameCrs?"":section.getCourseNbr()+" ")+
                                (sameItype?"":(section.getItype().length()==0?"ALL":section.getItype())+" ")+
                                (sameSct?"":section.getSection()); 
                            if (w.length()<32) w = lpad(w,32);
                            lines.add(w);
                        } else {
                            String w = "w/"+
                            (sameCrs?"":section.getSubject()+" ")+
                            (sameCrs?"":section.getCourseNbr()+" ")+
                            (sameSct?"":section.getSection().length()==0?"ALL":section.getSection());
                            if (w.length()<26) w = lpad(w, 26);
                            lines.add(w);
                        }
                    }
                    firstSection = false;
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeader(new String[] {
                    		"Subject Course   ExtID Section    Date      Time            | Subject Course   ExtID Section    Date      Time           ",
                    		"------- -------- ----- ---------  --------- --------------- | ------- -------- ----- ---------  --------- ---------------"});
                } else {
                    setHeader(new String[] {
                            "Subject Course   Type  Section    Date      Time            | Subject Course   Type  Section    Date      Time           ",
                            "------- -------- ----- ---------  --------- --------------- | ------- -------- ----- ---------  --------- ---------------"});
                    	//	".........1.........2.........3.........4.........5.........6"
                }
            } else {
                setHeader(new String[] {
                    "Subject Course   Section    Date    Time   | Subject Course   Section    Date    Time   | Subject Course   Section    Date    Time  ",
                    "------- -------- ---------  ------- ------ | ------- -------- ---------  ------- ------ | ------- -------- ---------  ------- ------"});
                //	".........1.........2.........3.........4123"
            }
            printHeader();
            if (iItype) {
                for (int idx=0; idx<lines.size(); idx+=2*n) {
                    for (int i=0;i<n;i++) {
                        String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                        String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                        println(rpad(a,60)+"| "+b);
                    }
                }            	
            } else {
                for (int idx=0; idx<lines.size(); idx+=3*n) {
                    for (int i=0;i<n;i++) {
                        String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                        String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                        String c = (i+idx+2*n<lines.size()?lines.elementAt(i+idx+2*n):"");
                        println(rpad(a,43)+"| "+rpad(b,43)+"| "+c);
                    }
                }
            }
        } else {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
                Vector<String> rooms = new Vector();
                if (exam.getRooms()==null || exam.getRooms().isEmpty()) {
                    rooms.add(rpad(iNoRoom,11));
                } else for (ExamRoomInfo room : exam.getRooms()) {
                    rooms.add(formatRoom(room));
                }
                Vector<ExamSectionInfo> sections = new Vector(exam.getSectionsIncludeCrosslistedDummies());
                Collections.sort(sections, new Comparator<ExamSectionInfo>() {
                    public int compare(ExamSectionInfo s1, ExamSectionInfo s2) {
                        if (!hasSubjectAreas()) return s1.compareTo(s2);
                        if (hasSubjectArea(s1.getOwner().getCourse().getSubjectArea())) {
                            if (!hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return -1;
                        } else if (hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return 1;
                        return s1.compareTo(s2);
                    }
                 });
                for (int i=0;i<Math.max(rooms.size(),sections.size());i++) {
                    String a = (i<rooms.size()?rooms.elementAt(i):rpad("",11));
                    ExamSectionInfo section = (i<sections.size()?sections.elementAt(i):null);
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
                                     rpad(sameSubj?"":section.getSubject(),7)+" "+
                                     rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                     rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                     lpad(sameSct?"":section.getSection(),9)+" "+
                                    formatShortPeriod(section.getExamAssignment())+" "+a);
                            } else {
                                lines.add(
                                		rpad(sameSubj?"":section.getSubject(),7)+" "+
                                		rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                		lpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),9)+" "+
                                        formatPeriod(section.getExamAssignment())+" "+a);
                            }
                    } else if (section!=null) {
                        if (iItype) {
                            String w = "w/"+(sameCrs?"":section.getSubject()+" ")+
                                (sameCrs?"":section.getCourseNbr()+" ")+
                                (sameItype?"":(section.getItype().length()==0?"ALL":section.getItype())+" ")+
                                (sameSct?"":section.getSection()); 
                            if (w.length()<32) w = lpad(w,32);
                            lines.add(rpad(w,55)+a);
                        } else {
                            String w = "w/"+
                            (sameCrs?"":section.getSubject()+" ")+
                            (sameCrs?"":section.getCourseNbr()+" ")+
                            (sameSct?"":section.getSection().length()==0?"ALL":section.getSection());
                            if (w.length()<26) w = lpad(w, 26);
                            lines.add(rpad(w,53)+a);
                        }
                    } else {
                        lines.add(rpad("",(iItype?55:53))+a);
                    }
                    lx++;
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeader(new String[] {
                            "Subject Course   ExtID Section   Date    Time          Bldg  Room |Subject Course   ExtID Section   Date    Time          Bldg  Room",
                            "------- -------- ----- --------- ------- ------------- ----- -----|------- -------- ----- --------- ------- ------------- ----- -----"});
                    	//	".........1.........2.........3.........4.........5.........6123456"
                } else {
                    setHeader(new String[] {
                    		"Subject Course   Type  Section   Date    Time          Bldg  Room |Subject Course   Type  Section   Date    Time          Bldg  Room",
                            "------- -------- ----- --------- ------- ------------- ----- -----|------- -------- ----- --------- ------- ------------- ----- -----"});
                }
            } else {
                setHeader(new String[] {
                    "Subject Course   Section   Date      Time            Bldg  Room  | Subject Course   Section   Date      Time            Bldg  Room ",
                    "------- -------- --------- --------- --------------- ----- ----- | ------- -------- --------- --------- --------------- ----- -----"});
                //	".........1.........2.........3.........4.........5.........612345"
            }
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=2*n) {
                for (int i=0;i<n;i++) {
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):rpad("",65));
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):rpad("",65));
                    if (iItype)
                    	println(rpad(a,66)+"|"+rpad(b, 66));
                    else
                    	println(rpad(a,65)+"| "+rpad(b, 64));
                }
            }
        }
    }
}
