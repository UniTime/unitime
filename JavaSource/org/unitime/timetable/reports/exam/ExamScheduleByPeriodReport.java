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
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamPeriod;
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
public class ExamScheduleByPeriodReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ExamScheduleByPeriodReport.class);
    
    public ExamScheduleByPeriodReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY PERIOD", session, examType, subjectAreas, exams);
    }

    
    public void printReport() throws DocumentException {
        setHeader(new String[] {
                "Date And Time                  Subject Course   "+(iItype?iExternal?"ExtnID ":"Type   ":"")+"Section     Meeting Times                         Enrl"+(iDispRooms?"  Room         Cap ExCap":""),
                "------------------------------ ------- -------- "+(iItype?"------ ":"")+"--------- -------------------------------------- -----"+(iDispRooms?" ----------- ----- -----":"")});
        printHeader();
        sLog.debug("  Sorting exams...");
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
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
                for (Iterator<ExamSectionInfo> j = exam.getSectionsIncludeCrosslistedDummies().iterator(); j.hasNext();) {
                    ExamSectionInfo  section = j.next();
                    if (!hasSubjectArea(section)) continue;
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
                            rpad(iPeriodPrinted?"":formatPeriod(period),30)+" "+
                            rpad(iSubjectPrinted?"":section.getSubject(),7)+" "+
                            rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                            (iItype?rpad(iStudentPrinted?"":section.getItype(), 6)+" ":"")+
                            lpad(section.getSection(),9)+" "+
                            rpad(getMeetingTime(section),38)+" "+
                            lpad(String.valueOf(section.getNrStudents()),5)
                            );
                        iPeriodPrinted = !iNewPage;
                    } else {
                        if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                            println(
                                    rpad(iPeriodPrinted?"":formatPeriod(period),30)+" "+
                                    rpad(iSubjectPrinted?"":section.getSubject(),7)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                    (iItype?rpad(iStudentPrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(section.getSection(),9)+" "+
                                    rpad(getMeetingTime(section),38)+" "+
                                    lpad(String.valueOf(section.getNrStudents()),5)+" "+iNoRoom
                                    );
                            iPeriodPrinted = !iNewPage;
                        } else {
                            if (getLineNumber()+section.getExamAssignment().getRooms().size()>iNrLines) newPage();
                            boolean firstRoom = true;
                            for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                                println(
                                        rpad(!firstRoom || iPeriodPrinted?"":formatPeriod(period),30)+" "+
                                        rpad(!firstRoom || iSubjectPrinted?"":section.getSubject(),7)+" "+
                                        rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                        (iItype?rpad(!firstRoom || iStudentPrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(!firstRoom?"":section.getSection(),9)+" "+
                                        rpad(!firstRoom?"":getMeetingTime(section),38)+" "+
                                        lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5)+" "+
                                        formatRoom(room)+" "+
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
