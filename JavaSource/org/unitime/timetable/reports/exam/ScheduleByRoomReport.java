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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

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
public class ScheduleByRoomReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByRoomReport.class);
    
    public ScheduleByRoomReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "SCHEDULE BY ROOM", session, examType, subjectAreas, exams);
    }

    public void printReport() throws DocumentException {
        sLog.info("  Computing room table...");
        Hashtable <ExamRoomInfo,Hashtable<ExamPeriod,List<ExamAssignmentInfo>>> table = new Hashtable();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
            for (ExamRoomInfo room : exam.getRooms()) {
                Hashtable<ExamPeriod,List<ExamAssignmentInfo>> roomAssignments = table.get(room);
                if (roomAssignments==null) {
                    roomAssignments = new Hashtable();
                    table.put(room,roomAssignments);
                }
                List<ExamAssignmentInfo> exams = roomAssignments.get(exam.getPeriod());
                if (exams == null) {
                	exams = new ArrayList<ExamAssignmentInfo>();
                	roomAssignments.put(exam.getPeriod(), exams);	
                }
                exams.add(exam);
            }
        }
        TreeSet<ExamRoomInfo> rooms = new TreeSet(new Comparator<ExamRoomInfo>() {
           public int compare(ExamRoomInfo r1, ExamRoomInfo r2) {
               int cmp = r1.getName().compareTo(r2.getName());
               if (cmp!=0) return cmp;
               return r1.getLocationId().compareTo(r2.getLocationId());
           }
        });
        rooms.addAll(table.keySet());
        Vector periods = new Vector(ExamPeriod.findAll(getSession().getUniqueId(), getExamType()));
        sLog.info("  Printing report...");
        setHeaderLine(
        		new Line(
        				rpad("Bldg", 5).withColSpan(0), rpad("Room", 5),
        				lpad("Capacity", 8),
        				lpad("ExCap", 6),
        				lpad("Period", 6),
        				rpad("Date And Time", 38),
        				rpad("Subject", 7),
        				rpad("Course", 8),
        				(iItype ? rpad(iExternal ? "ExtnId" : "Type", 6) : NULL),
        				rpad("Section", 10),
        				lpad("Enrl", 5)
        			), new Line(
        				lpad("", '-', 5).withColSpan(0), lpad("", '-', 5),
            			lpad("", '-', 8),
            			lpad("", '-', 6),
            			lpad("", '-', 6),
            			lpad("", '-', 38),
            			lpad("", '-', 7),
            			lpad("", '-', 8),
            			(iItype ? lpad("", '-', 6) : NULL),
            			lpad("", '-', 10),
            			lpad("", '-', 5)
            		));
        printHeader();
        for (Iterator<ExamRoomInfo> i = rooms.iterator();i.hasNext();) {
            ExamRoomInfo room = i.next();
            iPeriodPrinted = false;
            setPageName(room.getName());
            setCont(room.getName());
            Hashtable<ExamPeriod,List<ExamAssignmentInfo>> roomAssignments = table.get(room);
            ExamPeriod lastPeriod = null;
            boolean somethingPrinted = false;
            for (Iterator j=periods.iterator();j.hasNext();) {
                ExamPeriod period = (ExamPeriod)j.next();
                iStudentPrinted = false;
                List<ExamAssignmentInfo> exams = roomAssignments.get(period);
                if (exams!=null) {
                	for (ExamAssignmentInfo exam: exams) {
                        ExamSectionInfo lastSection = null;
                        iSubjectPrinted = iCoursePrinted = iITypePrinted = false;
                        for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                            if (!hasSubjectArea(section)) continue;
                            if (lastSection!=null && iSubjectPrinted) {
                                iSubjectPrinted = iCoursePrinted = iITypePrinted = false;
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
                            if (lastPeriod!=null && !lastPeriod.getDateOffset().equals(period.getDateOffset()) && !iNewPage) println(new Line());
                            lastPeriod = period;
                            println((iPeriodPrinted && isSkipRepeating()?rpad("",11):formatRoom(room)),
                                    lpad(iPeriodPrinted && isSkipRepeating()?"":String.valueOf(room.getCapacity()),8),
                                    lpad(iPeriodPrinted && isSkipRepeating()?"":String.valueOf(room.getExamCapacity()),6),
                                    lpad(iStudentPrinted && isSkipRepeating()?"":String.valueOf(periods.indexOf(period)+1),6),
                                    rpad(iStudentPrinted && isSkipRepeating()?"":formatPeriod(section.getExamAssignment()),38),
                                    rpad(iSubjectPrinted && isSkipRepeating()?"":section.getSubject(), 7),
                                    rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iITypePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                    formatSection10(section.getSection()),
                                    lpad(String.valueOf(section.getNrStudents()),5)
                                    );
                            iPeriodPrinted = iStudentPrinted = iSubjectPrinted = iCoursePrinted = iITypePrinted = !iNewPage;
                            lastSection = section;
                            somethingPrinted = true;
                        }
                    }
                    /*} else {
                        if (lastPeriod!=null && !lastPeriod.getDateOffset().equals(period.getDateOffset()) && !iNewPage) println("");
                        lastPeriod = period;
                        println((iPeriodPrinted?rpad("",11):formatRoom(room.getName()))+" "+
                                lpad(iPeriodPrinted && isSkipRepeating()?"":String.valueOf(room.getCapacity()),8)+" "+
                                lpad(iPeriodPrinted && isSkipRepeating()?"":String.valueOf(room.getExamCapacity()),6)+" "+
                                lpad(String.valueOf(periods.indexOf(period)+1),6)+" "+
                                rpad(formatPeriod(period),38)
                                );
                        iPeriodPrinted = !iNewPage;
                        //println("");
                    }*/                		
                }
            }
            setCont(null);
            if (somethingPrinted && i.hasNext()) {
                newPage();
            }
        }
        lastPage();        
    }
}
