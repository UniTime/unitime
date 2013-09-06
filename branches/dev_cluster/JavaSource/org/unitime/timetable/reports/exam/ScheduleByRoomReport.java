/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
        setHeader(new String[] {
                "Bldg  Room  Capacity  ExCap Period Date And Time                          Subj Crsnbr "+(iItype?iExternal?"ExtnID ":"InsTyp ":"")+"Sect   Enrl",
                "----- ----- -------- ------ ------ -------------------------------------- ---- ------ "+(iItype?"------ ":"")+"----- -----"});
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
                            if (lastPeriod!=null && !lastPeriod.getDateOffset().equals(period.getDateOffset()) && !iNewPage) println("");
                            lastPeriod = period;
                            println((iPeriodPrinted?rpad("",11):formatRoom(room.getName()))+" "+
                                    lpad(iPeriodPrinted?"":String.valueOf(room.getCapacity()),8)+" "+
                                    lpad(iPeriodPrinted?"":String.valueOf(room.getExamCapacity()),6)+" "+
                                    lpad(iStudentPrinted?"":String.valueOf(periods.indexOf(period)+1),6)+" "+
                                    rpad(iStudentPrinted?"":formatPeriod(section.getExamAssignment()),38)+" "+
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iITypePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(section.getSection(),5)+" "+
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
                                lpad(iPeriodPrinted?"":String.valueOf(room.getCapacity()),8)+" "+
                                lpad(iPeriodPrinted?"":String.valueOf(room.getExamCapacity()),6)+" "+
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
