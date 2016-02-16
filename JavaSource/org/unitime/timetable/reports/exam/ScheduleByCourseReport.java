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
                "Subject Course   "+(iItype?iExternal?"ExtnID ":"Type   ":"")+"Section     Meeting Times                       Enrl    Date And Time                 Room         Cap ExCap",
                "------- -------- "+(iItype?"------ ":"")+"--------- ------------------------------------ -----  ------------------------------ ----------- ----- -----"});
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
                Iterator<ExamSectionInfo> ch = (section.hasDifferentSubjectChildren() ? section.getDifferentSubjectChildren().iterator() : null);
                if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                	if (getLineNumber()+1+(section.hasDifferentSubjectChildren()?section.getDifferentSubjectChildren().size():0)>iNrLines) newPage();
                    println(
                            rpad(iSubjectPrinted?"":subject, 7)+" "+
                            rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                            (iItype?rpad(iITypePrinted?"":section.getItype(), 6)+" ":"")+
                            lpad(section.getSection(), 9)+" "+
                            rpad(getMeetingTime(section),36)+" "+
                            lpad(String.valueOf(section.getNrStudents()),5)+"  "+
                            rpad((section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),30)+" "+
                            (section.getExamAssignment()==null?"":iNoRoom)
                            );
                } else {
                    if (getLineNumber()+Math.max(section.getExamAssignment().getRooms().size(), section.hasDifferentSubjectChildren() ? section.getDifferentSubjectChildren().size() : 0)>iNrLines) newPage();
                    boolean firstRoom = true;
                    for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                    	ExamSectionInfo child = (firstRoom || ch == null || !ch.hasNext() ? null : ch.next());
                    	if (child != null) {
                    		println(
                                    "w/" + rpad(child.getSubject(), 6)+
                                    rpad(child.getCourseNbr(), 8)+" "+
                                    (iItype?rpad(child.getItype(), 6)+" ":"")+
                                    lpad(child.getSection(), 9)+" "+
                                    rpad("",36)+" "+
                                    lpad(String.valueOf(child.getNrStudents()),5)+"  "+
                                    rpad("",30)+" "+
                                    formatRoom(room.getName())+" "+
                                    lpad(""+room.getCapacity(),5)+" "+
                                    lpad(""+room.getExamCapacity(),5)
                                    );
                    	} else {
                            println(
                                    rpad(!firstRoom || iSubjectPrinted?"":subject, 7)+" "+
                                    rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                    (iItype?rpad(!firstRoom || iITypePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(!firstRoom?"":section.getSection(), 9)+" "+
                                    rpad(!firstRoom?"":getMeetingTime(section),36)+" "+
                                    lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5)+"  "+
                                    rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),30)+" "+
                                    formatRoom(room.getName())+" "+
                                    lpad(""+room.getCapacity(),5)+" "+
                                    lpad(""+room.getExamCapacity(),5)
                                    );
                    	}
                        firstRoom = false;
                    }
                }
                while (ch != null && ch.hasNext()) {
                	ExamSectionInfo child = ch.next();
                	println(
                            "w/" + rpad(child.getSubject(), 6)+
                            rpad(child.getCourseNbr(), 8)+" "+
                            (iItype?rpad(child.getItype(), 6)+" ":"")+
                            lpad(child.getSection(), 9)+" "+
                            rpad("",36)+" "+
                            lpad(String.valueOf(child.getNrStudents()),5)
                            );
            	}
                if (iNewPage || section.hasDifferentSubjectChildren()) {
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
