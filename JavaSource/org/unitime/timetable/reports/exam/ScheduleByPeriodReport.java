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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class ScheduleByPeriodReport extends PdfLegacyExamReport {
    protected static Log sLog = LogFactory.getLog(ScheduleByPeriodReport.class);
    
    public ScheduleByPeriodReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legacyReportScheduleByPeriod(), session, examType, subjectAreas, exams);
    }

    public void printReport() throws DocumentException {
        sLog.debug(MSG.statusSortingSections());
        Hashtable<String,TreeSet<ExamSectionInfo>> subject2courseSections = new Hashtable();
        for (ExamInfo exam : getExams()) {
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (!hasSubjectArea(section)) continue;
                TreeSet<ExamSectionInfo> sections = subject2courseSections.get(section.getSubject());
                if (sections==null) {
                    sections = new TreeSet();
                    subject2courseSections.put(section.getSubject(), sections);
                }
                sections.add(section);
            }
        }
        setHeaderLine(
        		new Line(
        				rpad(MSG.lrDateAndTime(), 30),
        				rpad(MSG.lrSubject(), 7),
        				rpad(MSG.lrCourse(), 8),
        				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
        				rpad(MSG.lrSection(), 10),
        				rpad(MSG.lrMeetingTimes(), 35),
        				lpad(MSG.lrEnrl(), 5),
        				(iDispRooms ? rpad(MSG.lrRoom(), 11): NULL),
        				(iDispRooms ? lpad(MSG.lrCap(), 5): NULL),
        				(iDispRooms ? lpad(MSG.lrExCap(), 6): NULL)
        		), new Line(
        				lpad("", '-', 30),
        				lpad("", '-', 7),
        				lpad("", '-', 8),
        				(iItype ? lpad("", '-', 6) : NULL),
        				lpad("", '-', 10),
        				lpad("", '-', 35),
        				lpad("", '-', 5),
        				(iDispRooms ? lpad("", '-', 11): NULL),
        				(iDispRooms ? lpad("", '-', 5): NULL),
        				(iDispRooms ? lpad("", '-', 6): NULL)
        		));
        sLog.debug(MSG.statusPrintingReport());
        printHeader();
        for (Iterator p=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();p.hasNext();) {
            ExamPeriod period = (ExamPeriod)p.next();
            iPeriodPrinted = false;
            setPageName(formatPeriod(period));
            setCont(formatPeriod(period));
            for (Iterator<String> i = new TreeSet<String>(subject2courseSections.keySet()).iterator(); i.hasNext();) {
                String subject = i.next();
                TreeSet<ExamSectionInfo> sections = subject2courseSections.get(subject);
                iSubjectPrinted = false;
                for (Iterator<ExamSectionInfo> j = sections.iterator(); j.hasNext();) {
                    ExamSectionInfo  section = j.next();
                    if (!period.equals(section.getExamAssignment().getPeriod())) continue;
                    if (!iDispRooms) {
                        println(
                            rpad(iPeriodPrinted && isSkipRepeating()?"":formatPeriod(period),30),
                            rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                            rpad(section.getCourseNbr(), 8),
                            (iItype?rpad(section.getItype(), 6):NULL),
                            formatSection10(section.getSection()),
                            rpad(getMeetingTime(section),35),
                            lpad(String.valueOf(section.getNrStudents()),5)
                            );
                        iPeriodPrinted = iSubjectPrinted = !iNewPage;
                    } else {
                        if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                            println(
                                    rpad(iPeriodPrinted && isSkipRepeating()?"":formatPeriod(period),30),
                                    rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                    rpad(section.getCourseNbr(), 8),
                                    (iItype?rpad(section.getItype(), 6):NULL),
                                    formatSection10(section.getSection()),
                                    rpad(getMeetingTime(section),35),
                                    lpad(String.valueOf(section.getNrStudents()),5),
                                    new Cell(iNoRoom).withColSpan(3)
                                    );
                            iPeriodPrinted = iSubjectPrinted = !iNewPage;
                        } else {
                            if (getLineNumber()+section.getExamAssignment().getRooms().size()>getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                            boolean firstRoom = true;
                            for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                                println(
                                        rpad(!firstRoom || (iPeriodPrinted && isSkipRepeating())?"":formatPeriod(period),30),
                                        rpad(!firstRoom || (iSubjectPrinted && isSkipRepeating())?"":subject,7),
                                        rpad(!firstRoom?"":section.getCourseNbr(), 8),
                                        (iItype?rpad(!firstRoom?"":section.getItype(), 6):NULL),
                                        formatSection10(!firstRoom?"":section.getSection()),
                                        (!firstRoom?rpad("", 35):getMeetingTime(section)),
                                        lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5),
                                        formatRoom(room),
                                        lpad(""+room.getCapacity(),5),
                                        lpad(""+room.getExamCapacity(),6)
                                        );
                                firstRoom = false;
                            }
                            iPeriodPrinted = iSubjectPrinted = !iNewPage;
                        }
                    }
                }
            }
            setCont(null);
            if (iPeriodPrinted && p.hasNext()) newPage();
        }
        if (iPeriodPrinted) lastPage();
    }
}
