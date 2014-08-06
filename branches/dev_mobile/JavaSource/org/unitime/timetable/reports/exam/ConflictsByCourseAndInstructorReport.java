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
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class ConflictsByCourseAndInstructorReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ConflictsByCourseAndStudentReport.class);
    Hashtable<Long,String> iStudentNames = new Hashtable();
    
    public ConflictsByCourseAndInstructorReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "CONFLICTS BY COURSE AND INSTRUCTOR", session, examType, subjectAreas, exams);
    }
    
    public void printReport() throws DocumentException {
        sLog.debug("  Sorting sections...");
        Hashtable<String,TreeSet<ExamSectionInfo>> subject2courseSections = new Hashtable();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
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
        		"Subject Course   "+(iItype?iExternal?"ExtnID ":"Type   ":"")+"Section   Date    Time   Name                      Type   Subject Course   "+(iItype?iExternal?"ExtnID ":"Type   ":"")+"Section   Time           ",
        		"------- -------- "+(iItype?"------ ":"")+"--------- ------- ------ ------------------------- ------ ------- -------- "+(iItype?"------ ":"")+"--------- ---------------"});
        printHeader();
        for (Iterator<String> i = new TreeSet<String>(subject2courseSections.keySet()).iterator(); i.hasNext();) {
            String subject = i.next();
            TreeSet<ExamSectionInfo> sections = subject2courseSections.get(subject);
            if (iSubjectPrinted) newPage();
            setPageName(subject); setCont(subject);
            iSubjectPrinted = false;
            for (Iterator<ExamSectionInfo> j = sections.iterator(); j.hasNext();) {
                ExamSectionInfo section = j.next();
                ExamAssignmentInfo exam = section.getExamAssignmentInfo();
                if (exam==null || exam.getPeriod()==null) continue;
                iCoursePrinted = false;
                for (ExamInstructorInfo instructor : exam.getInstructors()) {
                    iStudentPrinted = false;
                    if (iDirect) for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        if (conflict.getOtherExam()!=null) {
                            for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                                if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                                println(
                                        rpad(iSubjectPrinted?"":subject,7)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                        rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                        rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                        rpad(other.getSubject(),7)+" "+
                                        rpad(other.getCourseNbr(),8)+" "+
                                        (iItype?rpad(other.getItype(),6)+" ":"")+
                                        lpad(other.getSection(),9)+" "+
                                        other.getExamAssignment().getTimeFixedLength()
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        } else if (conflict.getOtherEventId()!=null) {
                            if (conflict.isOtherClass()) {
                                println(
                                    rpad(iSubjectPrinted?"":subject,7)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                    rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                    rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                    rpad(iPeriodPrinted?"":"CLASS",6)+" "+
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),7)+" "+
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),8)+" "+
                                    (iItype?rpad(iExternal?conflict.getOtherClass().getExternalUniqueId():conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6)+" ":"")+
                                    lpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(),9)+" "+
                                    getMeetingTime(conflict.getOtherEventTime())
                                    );
                            } else {
                                println(
                                        rpad(iSubjectPrinted?"":subject,7)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                        rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                        rpad(iPeriodPrinted?"":"EVENT",6)+" "+
                                        rpad(conflict.getOtherEventName(),(iItype?33:26))+" "+
                                        getMeetingTime(conflict.getOtherEventTime())
                                        );
                            }
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }
                    if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        for (ExamAssignment otherExam : conflict.getOtherExams()) {
                            if (!otherExam.getInstructors().contains(instructor)) continue;
                            for (ExamSectionInfo other : otherExam.getSectionsIncludeCrosslistedDummies()) {
                                println(
                                        rpad(iSubjectPrinted?"":subject,7)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                        rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                        rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                        rpad(iPeriodPrinted?"":">2-DAY",6)+" "+
                                        rpad(other.getSubject(),7)+" "+
                                        rpad(other.getCourseNbr(),8)+" "+
                                        (iItype?rpad(other.getItype(),6)+" ":"")+
                                        lpad(other.getSection(),9)+" "+
                                        other.getExamAssignment().getTimeFixedLength()
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        }
                    }
                    if (iBtb) for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                        if (!conflict.getStudents().contains(instructor.getId())) continue;
                        iPeriodPrinted = false;
                        for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                            if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                            println(
                                    rpad(iSubjectPrinted?"":subject,7)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                    rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                    rpad(iStudentPrinted?"":instructor.getName(),25)+" "+
                                    rpad(iPeriodPrinted?"":"BTB",6)+" "+
                                    rpad(other.getSubject(),7)+" "+
                                    rpad(other.getCourseNbr(),8)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),9)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }                    
                }
            }
            setCont(null);
        }
        if (iSubjectPrinted) lastPage();
    }
}
