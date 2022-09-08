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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    protected static Log sLog = LogFactory.getLog(ConflictsByCourseAndStudentReport.class);
    Hashtable<Long,String> iStudentNames = new Hashtable();
    
    public ConflictsByCourseAndStudentReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legacyReportConflictsByCourseAndStudent(), session, examType, subjectAreas, exams);
        sLog.debug(MSG.statusLoadingStudents());
        for (Iterator i=new StudentDAO().getSession().createQuery("select s.uniqueId, s.externalUniqueId, s.lastName, s.firstName, s.middleName from Student s where s.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).iterate();i.hasNext();) {
            Object[] o = (Object[])i.next();
            if (o[2]!=null)
                iStudentNames.put((Long)o[0], (String)o[2]+(o[3]==null?"":" "+(String)o[3])+(o[4]==null?"":" "+(String)o[4]));
            else if (o[1]!=null)
                iStudentNames.put((Long)o[0], (String)o[1]);
            else
                iStudentNames.put((Long)o[0], MSG.lrNA());
        }
    }

    public void printReport() throws DocumentException {
        sLog.debug(MSG.statusSortingSections());
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
        sLog.debug(MSG.statusPrintingReport());
        setHeaderLine(
    			new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), (iItype?rpad(iExternal?MSG.lrExtnID():MSG.lrType(), 6):NULL), rpad(MSG.lrSection(), 10),
    					rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6), rpad(MSG.lrName(), 25), rpad(MSG.lrType(), 6),
    					rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), (iItype?rpad(iExternal?MSG.lrExtnID():MSG.lrType(), 6):NULL), rpad(MSG.lrSection(), 10), rpad(MSG.lrTime(), 15)),
    			new Line(lpad("", '-', 7), lpad("", '-', 8), (iItype?lpad("", '-', 6):NULL), lpad("", '-', 10),
    					lpad("", '-', 7), lpad("", '-', 6), lpad("", '-', 25), lpad("", '-', 6),
    					lpad("", '-', 7), lpad("", '-', 8), (iItype?lpad("", '-', 6):NULL), lpad("", '-', 10), lpad("", '-', 15))
    			);
        printHeader();
        boolean dirtyPage = false;
        for (Iterator<String> i = new TreeSet<String>(subject2courseSections.keySet()).iterator(); i.hasNext();) {
            String subject = i.next();
            TreeSet<ExamSectionInfo> sections = subject2courseSections.get(subject);
            if (iSubjectPrinted || dirtyPage) newPage();
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
                                        rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                        rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                        (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                        formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam)),
                                        rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                        rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrDIRECT(),6),
                                        rpad(other.getSubject(),7),
                                        rpad(other.getCourseNbr(),8),
                                        (iItype?rpad(other.getItype(),6):NULL),
                                        formatSection10(other.getSection()),
                                        new Cell(other.getExamAssignment().getTimeFixedLength())
                                        );
                                iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            }
                        } else if (conflict.getOtherEventId()!=null) {
                            if (conflict.isOtherClass()) {
                                println(
                                        rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                        rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                        (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                        formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam)),
                                        rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                        rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrCLASS(),6),
                                        rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),7),
                                        rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),8),
                                        (iItype?rpad(iExternal?conflict.getOtherClass().getExternalUniqueId():conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6):NULL),
                                        formatSection10(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString()),
                                        new Cell(getMeetingTime(conflict.getOtherEventTime()))
                                        );
                            } else {
                                println(
                                        rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                        rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                        (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                        formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam)),
                                        rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                        rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrEVENT(),6),
                                        rpad(conflict.getOtherEventName(),(iItype?34:27)).withColSpan(iItype?4:3),
                                        new Cell(getMeetingTime(conflict.getOtherEventTime()))
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
                                        rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                        rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                        (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                        formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                        (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam)),
                                        rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                        rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrMore2DAY(),6),
                                        rpad(other.getSubject(),7),
                                        rpad(other.getCourseNbr(),8),
                                        (iItype?rpad(other.getItype(),6):NULL),
                                        formatSection10(other.getSection()),
                                        new Cell(other.getExamAssignment().getTimeFixedLength())
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
                                    rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                    rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                    formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                    (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                    (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam)),
                                    rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                    rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrBTB(),6),
                                    rpad(other.getSubject(),7),
                                    rpad(other.getCourseNbr(),8),
                                    (iItype?rpad(other.getItype(),6):NULL),
                                    formatSection10(other.getSection()),
                                    new Cell(other.getExamAssignment().getTimeFixedLength())
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }
                }
                boolean needReprintSubject = false;
                if (section.hasDifferentSubjectChildren()) {
                	for (ExamSectionInfo child: section.getDifferentSubjectChildren()) {
                		boolean diffSubjectPrinted = false;
                        students = new Vector<Long>(child.getStudentIds());
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
                                    	if (!iCoursePrinted) {
                                    		println(
                                                    rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                                    rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                                    (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                                    formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                                    (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                                    (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam))
                                                    );
                                            iSubjectPrinted = iCoursePrinted = !iNewPage;
                                    	}
                                        println(
                                        	rpad(diffSubjectPrinted ? "" : MSG.lrWith() + child.getSubject(), 8).withSeparator(""),
                                        	rpad(child.getCourseNbr(), 8),
                                        	(iItype?rpad(child.getItype(), 6):NULL),
                                        	formatSection10(child.getSection()),
                                        	formatShortPeriodNoEndTimeDate(exam), formatShortPeriodNoEndTimeTime(exam),
                                        	rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                        	rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrDIRECT(),6),
                                        	rpad(other.getSubject(),7),
                                        	rpad(other.getCourseNbr(),8),
                                        	(iItype?rpad(other.getItype(),6):NULL),
                                        	formatSection10(other.getSection()),
                                        	new Cell(other.getExamAssignment().getTimeFixedLength())
                                        	);
                                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = diffSubjectPrinted = needReprintSubject = !iNewPage;
                                    }
                                } else if (conflict.getOtherEventId()!=null) {
                                	if (!iCoursePrinted) {
                                		println(
                                                rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                                rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                                (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                                formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                                (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                                (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam))
                                                );
                                        iSubjectPrinted = iCoursePrinted = !iNewPage;
                                	}
                                    if (conflict.isOtherClass()) {
                                        println(
                                        		rpad(diffSubjectPrinted ? "" : MSG.lrWith() + child.getSubject(), 8).withSeparator(""),
                                        		rpad(child.getCourseNbr(), 8),
                                        		(iItype?rpad(child.getItype(), 6):NULL),
                                        		formatSection10(child.getSection()),
                                        		formatShortPeriodNoEndTimeDate(exam), formatShortPeriodNoEndTimeTime(exam),
                                                rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                                rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrCLASS(),6),
                                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),7),
                                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),8),
                                                (iItype?rpad(iExternal?conflict.getOtherClass().getExternalUniqueId():conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6):NULL),
                                                formatSection10(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString()),
                                                new Cell(getMeetingTime(conflict.getOtherEventTime()))
                                                );
                                    } else {
                                        println(
                                        		rpad(diffSubjectPrinted ? "" : MSG.lrWith() + child.getSubject(), 8).withSeparator(""),
                                        		rpad(child.getCourseNbr(), 8),
                                        		(iItype?rpad(child.getItype(), 6):NULL),
                                        		formatSection10(child.getSection()),
                                        		formatShortPeriodNoEndTimeDate(exam), formatShortPeriodNoEndTimeTime(exam),
                                        		rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                                rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrEVENT(),6),
                                                rpad(conflict.getOtherEventName(),(iItype?34:27)).withColSpan(iItype?4:3),
                                                new Cell(getMeetingTime(conflict.getOtherEventTime()))
                                                );
                                    }
                                    iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = diffSubjectPrinted = needReprintSubject = !iNewPage;
                                }
                            }
                            if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                                if (!conflict.getStudents().contains(studentId)) continue;
                                iPeriodPrinted = false;
                                for (ExamAssignment otherExam : conflict.getOtherExams()) {
                                    for (ExamSectionInfo other : otherExam.getSectionsIncludeCrosslistedDummies()) {
                                        if (!other.getStudentIds().contains(studentId)) continue;
                                    	if (!iCoursePrinted) {
                                    		println(
                                                    rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                                    rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                                    (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                                    formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                                    (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                                    (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam))
                                                    );
                                            iSubjectPrinted = iCoursePrinted = !iNewPage;
                                    	}
                                        println(
                                        		rpad(diffSubjectPrinted ? "" : MSG.lrWith() + child.getSubject(), 8).withSeparator(""),
                                        		rpad(child.getCourseNbr(), 8),
                                        		(iItype?rpad(child.getItype(), 6):NULL),
                                        		formatSection10(child.getSection()),
                                        		formatShortPeriodNoEndTimeDate(exam), formatShortPeriodNoEndTimeTime(exam),
                                                rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                                rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrMore2DAY(),6),
                                                rpad(other.getSubject(),7),
                                                rpad(other.getCourseNbr(),8),
                                                (iItype?rpad(other.getItype(),6):NULL),
                                                formatSection10(other.getSection()),
                                                new Cell(other.getExamAssignment().getTimeFixedLength())
                                                );
                                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = diffSubjectPrinted = needReprintSubject = !iNewPage;
                                    }
                                }
                            }
                            if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                                if (!conflict.getStudents().contains(studentId)) continue;
                                iPeriodPrinted = false;
                                for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                                    if (!other.getStudentIds().contains(studentId)) continue;
                                	if (!iCoursePrinted) {
                                		println(
                                                rpad(iSubjectPrinted && isSkipRepeating()?"":subject,7),
                                                rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                                (iItype?rpad(iCoursePrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                                formatSection10(iCoursePrinted && isSkipRepeating()?"":section.getSection()),
                                                (iCoursePrinted && isSkipRepeating()?rpad("",7):formatShortPeriodNoEndTimeDate(exam)),
                                                (iCoursePrinted && isSkipRepeating()?rpad("",6):formatShortPeriodNoEndTimeTime(exam))
                                                );
                                        iSubjectPrinted = iCoursePrinted = !iNewPage;
                                	}
                                    println(
                                    		rpad(diffSubjectPrinted ? "" : MSG.lrWith() + child.getSubject(), 8).withSeparator(""),
                                    		rpad(child.getCourseNbr(), 8),
                                    		(iItype?rpad(child.getItype(), 6):NULL),
                                    		formatSection10(child.getSection()),
                                    		formatShortPeriodNoEndTimeDate(exam), formatShortPeriodNoEndTimeTime(exam),
                                    		rpad(iStudentPrinted && isSkipRepeating()?"":iStudentNames.get(studentId),25),
                                            rpad(iPeriodPrinted && isSkipRepeating()?"":MSG.lrBTB(),6),
                                            rpad(other.getSubject(),7),
                                            rpad(other.getCourseNbr(),8),
                                            (iItype?rpad(other.getItype(),6):NULL),
                                            formatSection10(other.getSection()),
                                            new Cell(other.getExamAssignment().getTimeFixedLength())
                                            );
                                    iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = diffSubjectPrinted = needReprintSubject = !iNewPage;
                                }
                            }                    
                        }
                	}                	
                }
                if (needReprintSubject) {
                	iSubjectPrinted = false;
                	dirtyPage = true;
                }
            }
            setCont(null);
        }
        if (iSubjectPrinted) lastPage();
    }
}
