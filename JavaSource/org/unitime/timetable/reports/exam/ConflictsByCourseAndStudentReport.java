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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class ConflictsByCourseAndStudentReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ConflictsByCourseAndStudentReport.class);
    Hashtable<Long,String> iStudentNames = new Hashtable();
    
    public ConflictsByCourseAndStudentReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "CONFLICTS BY COURSE AND STUDENT", session, examType, subjectAreas, exams);
        sLog.debug("  Loading students ...");
        for (Iterator i=new StudentDAO().getSession().createQuery("select s.uniqueId, s.externalUniqueId, s.lastName, s.firstName, s.middleName from Student s where s.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).iterate();i.hasNext();) {
            Object[] o = (Object[])i.next();
            if (o[2]!=null)
                iStudentNames.put((Long)o[0], (String)o[2]+(o[3]==null?"":" "+(String)o[3])+(o[4]==null?"":" "+(String)o[4]));
            else if (o[1]!=null)
                iStudentNames.put((Long)o[0], (String)o[1]);
            else
                iStudentNames.put((Long)o[0], "N/A");
        }
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
                Vector<Long> students = new Vector<Long>(section.getStudentIds());
                Collections.sort(students,new Comparator<Long>() {
                    public int compare(Long s1, Long s2) {
                        int cmp = iStudentNames.get(s1).compareTo(iStudentNames.get(s2));
                        if (cmp!=0) return cmp;
                        return s1.compareTo(s2);
                    }
                });
                for (Long studentId : students) {
                    iStudentPrinted = false;
                    if (iDirect) for (DirectConflict conflict : exam.getDirectConflicts()) {
                        if (!conflict.getStudents().contains(studentId)) continue;
                        iPeriodPrinted = false;
                        if (conflict.getOtherExam()!=null) {
                            for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                                if (!other.getStudentIds().contains(studentId)) continue;
                                println(
                                        rpad(iSubjectPrinted?"":subject,7)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                        rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                        rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
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
                                        rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
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
                                        rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
                                        rpad(iPeriodPrinted?"":"EVENT",6)+" "+
                                        rpad(conflict.getOtherEventName(),(iItype?33:26))+" "+
                                        getMeetingTime(conflict.getOtherEventTime())
                                        );
                            }
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }
                    if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                        if (!conflict.getStudents().contains(studentId)) continue;
                        iPeriodPrinted = false;
                        for (ExamAssignment otherExam : conflict.getOtherExams()) {
                            for (ExamSectionInfo other : otherExam.getSectionsIncludeCrosslistedDummies()) {
                                if (!other.getStudentIds().contains(studentId)) continue;
                                println(
                                        rpad(iSubjectPrinted?"":subject,7)+" "+
                                        rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                        (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                        lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                        rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                        rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
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
                    if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                        if (!conflict.getStudents().contains(studentId)) continue;
                        iPeriodPrinted = false;
                        for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            println(
                                    rpad(iSubjectPrinted?"":subject,7)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),9)+" "+
                                    rpad(iCoursePrinted?"":formatShortPeriodNoEndTime(exam),14)+" "+
                                    rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
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
