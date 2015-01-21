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
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class AbbvScheduleByCourseReport extends PdfLegacyExamReport {
    public AbbvScheduleByCourseReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY COURSE", session, examType, subjectAreas, exams);
    }

    public void printReport() throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (!hasSubjectArea(section)) continue;
                sections.add(section);
            }
        }
        Vector<String> lines = new Vector();
        String separator = null;
        int split = 9;
        int n = iNrLines - 2 - ((iNrLines - 2) / (split + 1));
        if (!iDispRooms) {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamSectionInfo section : sections) {
                boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                if ((lx%n)!=0 && ((lx%n)%split)!=0 && last!=null) {
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
                        (rpad(sameSubj?"":section.getSubject(),7)+" "+
                         rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                         rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                         lpad(sameSct?"":section.getSection(),9))+"  "+
                         formatPeriod(section.getExamAssignment()));
                } else {
                    lines.add(rpad(sameSubj?"":section.getSubject(),7)+" "+
                            rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                            lpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),9)+"  "+
                            formatShortPeriodNoEndTime(section.getExamAssignment()));
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeader(new String[] {
                    		"Subject Course   ExtID Section    Date      Time            | Subject Course   ExtID Section    Date      Time           ",
                    		"------- -------- ----- ---------  --------- --------------- | ------- -------- ----- ---------  --------- ---------------"});
                    separator = "------- -------- ----- ---------  --------- --------------- | ------- -------- ----- ---------  --------- ---------------";
                } else {
                    setHeader(new String[] {
                            "Subject Course   Type  Section    Date      Time            | Subject Course   Type  Section    Date      Time           ",
                            "------- -------- ----- ---------  --------- --------------- | ------- -------- ----- ---------  --------- ---------------"});
                    	//	".........1.........2.........3.........4.........5.........6"
                    separator = "------- -------- ----- ---------  --------- --------------- | ------- -------- ----- ---------  --------- ---------------";
                }
            } else {
                setHeader(new String[] {
                    "Subject Course   Section    Date    Time   | Subject Course   Section    Date    Time   | Subject Course   Section    Date    Time  ",
                    "------- -------- ---------  ------- ------ | ------- -------- ---------  ------- ------ | ------- -------- ---------  ------- ------"});
                //	".........1.........2.........3.........4123"
                separator = "------- -------- ---------  ------- ------ | ------- -------- ---------  ------- ------ | ------- -------- ---------  ------- ------";
            }
            printHeader();
            if (iItype) {
                for (int idx=0; idx<lines.size(); idx+=2*n) {
                    for (int i=0;i<n;i++) {
                        String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                        String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                        println(rpad(a,60)+"| "+b);
                        if ((i%split)==split-1) println(separator);
                    }
                }
            } else {
                for (int idx=0; idx<lines.size(); idx+=3*n) {
                    for (int i=0;i<n;i++) {
                        String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                        String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                        String c = (i+idx+2*n<lines.size()?lines.elementAt(i+idx+2*n):"");
                        println(rpad(a,43)+"| "+rpad(b,43)+"| "+c);
                        if ((i%split)==split-1) println(separator);
                    }
                }
            }
        } else {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamSectionInfo section : sections) {
                boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                if ((lx%n)!=0 && ((lx%n)%split)!=0 && last!=null) {
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
                                 rpad(sameSubj?"":section.getSubject(),7)+" "+
                                 rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                 rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                 lpad(sameSct?"":section.getSection(),9)+" "+
                                formatShortPeriod(section.getExamAssignment())+" "+
                                rpad(iNoRoom,23));
                        } else {
                            lines.add(
                                        rpad(sameSubj?"":section.getSubject(),7)+" "+
                                        rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                        rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),9)+"  "+
                                    formatPeriod(section.getExamAssignment())+" "+
                                    rpad(iNoRoom,23));
                        }
                } else {
                    Vector<ExamRoomInfo> rooms = new Vector(section.getExamAssignment().getRooms());
                    for (int i=0;i<rooms.size();i++) {
                        ExamRoomInfo a = rooms.elementAt(i);
                        if (i==0) {
                            if (iItype) {
                                lines.add(
                                         rpad(sameSubj?"":section.getSubject(),7)+" "+
                                         rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                         rpad(sameItype?"":section.getItype().length()==0?"ALL":section.getItype(),5)+" "+
                                         lpad(sameSct?"":section.getSection(),9)+" "+
                                        formatShortPeriod(section.getExamAssignment())+" "+
                                        formatRoom(a.getName()));
                                } else {
                                    lines.add(
                                            rpad(sameSubj?"":section.getSubject(),7)+" "+
                                            rpad(sameCrs?"":section.getCourseNbr(),8)+" "+
                                            rpad(sameSct?"":section.getSection().length()==0?"ALL":section.getSection(),9)+" "+
                                            formatPeriod(section.getExamAssignment())+" "+
                                            formatRoom(a.getName()));
                                }
                        } else {
                            lines.add(
                                    rpad("",(iItype?55:53))+
                                    formatRoom(a.getName()));
                            lx++;
                        }
                    }
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeader(new String[] {
                            "Subject Course   ExtID Section   Date    Time          Bldg  Room |Subject Course   ExtID Section   Date    Time          Bldg  Room",
                            "------- -------- ----- --------- ------- ------------- ----- -----|------- -------- ----- --------- ------- ------------- ----- -----"});
                    	//	".........1.........2.........3.........4.........5.........6123456"
                    separator = "------- -------- ----- --------- ------- ------------- ----- -----|------- -------- ----- --------- ------- ------------- ----- -----";
                } else {
                    setHeader(new String[] {
                    		"Subject Course   Type  Section   Date    Time          Bldg  Room |Subject Course   Type  Section   Date    Time          Bldg  Room",
                            "------- -------- ----- --------- ------- ------------- ----- -----|------- -------- ----- --------- ------- ------------- ----- -----"});
                    separator = "------- -------- ----- --------- ------- ------------- ----- -----|------- -------- ----- --------- ------- ------------- ----- -----";
                }
            } else {
                setHeader(new String[] {
                    "Subject Course   Section   Date      Time            Bldg  Room  | Subject Course   Section   Date      Time            Bldg  Room ",
                    "------- -------- --------- --------- --------------- ----- ----- | ------- -------- --------- --------- --------------- ----- -----"});
                //	".........1.........2.........3.........4.........5.........612345"
                separator = "------- -------- --------- --------- --------------- ----- ----- | ------- -------- --------- --------- --------------- ----- -----";
            }
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=2*n) {
                for (int i=0;i<n;i++) {
                    String a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):"");
                    String b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):"");
                    if (iItype)
                    	println(rpad(a,66)+"|"+rpad(b, 66));
                    else
                    	println(rpad(a,65)+"| "+rpad(b, 64));
                    if ((i%split)==split-1) println(separator);
                }
            }
        }
    }
}
