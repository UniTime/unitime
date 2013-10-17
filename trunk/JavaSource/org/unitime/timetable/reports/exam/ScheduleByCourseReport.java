/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
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
public class ScheduleByCourseReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    
    public ScheduleByCourseReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY COURSE", session, examType, subjectAreas, exams);
    }
    
    public void printReport() throws DocumentException {
        sLog.debug("  Sorting sections...");
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
        sLog.debug("  Printing report...");
        setHeader(new String[] {
                "Subj Crsnbr "+(iItype?iExternal?"ExtnID ":"InsTyp ":"")+"Sect    Meeting Times                         Enrl    Date And Time                   Room         Cap ExCap ",
                "---- ------ "+(iItype?"------ ":"")+"----- -------------------------------------- -----  -------------------------------- ----------- ----- -----"});
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
                if (iCoursePrinted && !section.getCourseNbr().equals(lastCourse)) { iCoursePrinted = false; iITypePrinted = false; }
                if (iITypePrinted && !section.getItype().equals(lastItype)) iITypePrinted = false;
                if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                    println(
                            rpad(iSubjectPrinted?"":subject, 4)+" "+
                            rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                            (iItype?rpad(iITypePrinted?"":section.getItype(), 6)+" ":"")+
                            lpad(section.getSection(), 5)+" "+
                            rpad(getMeetingTime(section),38)+" "+
                            lpad(String.valueOf(section.getNrStudents()),5)+"  "+
                            rpad((section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                            (section.getExamAssignment()==null?"":iNoRoom)
                            );
                } else {
                    if (getLineNumber()+section.getExamAssignment().getRooms().size()>iNrLines) newPage();
                    boolean firstRoom = true;
                    for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                        println(
                                rpad(!firstRoom || iSubjectPrinted?"":subject, 4)+" "+
                                rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(!firstRoom || iITypePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(!firstRoom?"":section.getSection(), 5)+" "+
                                rpad(!firstRoom?"":getMeetingTime(section),38)+" "+
                                lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5)+"  "+
                                rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                                formatRoom(room.getName())+" "+
                                lpad(""+room.getCapacity(),5)+" "+
                                lpad(""+room.getExamCapacity(),5)
                                );
                        firstRoom = false;
                    }
                }
                if (iNewPage) {
                    iSubjectPrinted = iITypePrinted = iCoursePrinted = false;
                    lastItype = lastCourse = null;
                } else {
                    iSubjectPrinted = iITypePrinted = iCoursePrinted = true;
                    lastItype = section.getItype();
                    lastCourse = section.getCourseNbr();
                }
                if (j.hasNext()) { 
                    if (!iNewPage) println(""); 
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
