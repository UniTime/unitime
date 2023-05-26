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
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class AbbvExamScheduleByCourseReport extends PdfLegacyExamReport {
    public AbbvExamScheduleByCourseReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legacyReportScheduleByCourse(), session, examType, subjectAreas, exams);
    }

    public void printReport() throws DocumentException {
        Vector<Line> lines = new Vector<Line>();
        int n = getNrLinesPerPage() - 1 - getSeparatorNrLines();
        if (n < 0) {
        	if (iDispRooms) {
        		int sections = 0;
        		for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                    if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
                    sections += Math.max(exam.getSectionsIncludeCrosslistedDummies().size(), exam.getNrRooms());
        		}
        		n = (sections + 1) / 2;
        	} else {
        		int sections = 0;
        		for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                    if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
                    sections += exam.getSectionsIncludeCrosslistedDummies().size();
        		}
        		if (iItype)
        			n = (sections + 1) / 2;
        		else
        			n = (sections + 2) / 3;
        	}
        }
        if (!iDispRooms) {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
                boolean firstSection = true;
                TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>(new Comparator<ExamSectionInfo>() {
                    public int compare(ExamSectionInfo s1, ExamSectionInfo s2) {
                        if (!hasSubjectAreas()) return s1.compareTo(s2);
                        if (hasSubjectArea(s1.getOwner().getCourse().getSubjectArea())) {
                            if (!hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return -1;
                        } else if (hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return 1;
                        return s1.compareTo(s2);
                    }
                 });
                 sections.addAll(exam.getSectionsIncludeCrosslistedDummies());
                for (ExamSectionInfo section : sections) {
                    boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                    if ((lx%n)!=0 && last!=null) {
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
                    if (firstSection) {
                        if (iItype) {
                            lines.add(new Line(
                                 rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                 rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                 rpad(sameItype && isSkipRepeating()?"":section.getItype().length()==0?MSG.lrALL():section.getItype(),5),
                                 formatSection10(sameSct && isSkipRepeating()?"":section.getSection()),
                                 formatPeriodDate(exam), formatPeriodTime(exam)
                                 ));
                        } else {
                            lines.add(new Line(
                                    rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                    rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                    formatSection10(sameSct?"":section.getSection().length()==0?MSG.lrALL():section.getSection()),
                                    formatShortPeriodNoEndTimeDate(exam), formatShortPeriodNoEndTimeTime(exam)));
                        }
                    } else {
                        if (iItype) {
                            String w = MSG.lrWith()+(sameCrs?"":section.getSubject()+" ")+
                                (sameCrs && isSkipRepeating()?"":section.getCourseNbr()+" ")+
                                (sameItype && isSkipRepeating()?"":(section.getItype().length()==0?MSG.lrALL():section.getItype())+" ")+
                                (sameSct && isSkipRepeating()?"":section.getSection());
                            lines.add(new Line(lpad(w, 32).withColSpan(4), rpad("", 25).withColSpan(2)));
                        } else {
                            String w = MSG.lrWith()+
                            (sameCrs && isSkipRepeating()?"":section.getSubject()+" ")+
                            (sameCrs && isSkipRepeating()?"":section.getCourseNbr()+" ")+
                            (sameSct && isSkipRepeating()?"":section.getSection().length()==0?MSG.lrALL():section.getSection());
                            lines.add(new Line(lpad(w, 26).withColSpan(3), rpad("", 14).withColSpan(2)));
                        }
                    }
                    firstSection = false;
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeaderLine(new Line(new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15)).withLength(60),
                    		new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15))),
                    		new Line(new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15)).withLength(60),
                    		new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15))));
                    		
                } else {
                    setHeaderLine(new Line(new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15)).withLength(60),
                    		new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15))),
                    		new Line(new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15)).withLength(60),
                    		new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15))));
                }
            } else {
                setHeaderLine(new Line(new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6)).withLength(43),
                			new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6)).withLength(43),
                			new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6))),
                		new Line(new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 7), rpad("", '-', 6)).withLength(43),
                				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 7), rpad("", '-', 6)).withLength(43),
                				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 7), rpad("", '-', 6))));
            }
            printHeader();
            if (iItype) {
                for (int idx=0; idx<lines.size(); idx+=2*n) {
                    for (int i=0;i<n;i++) {
                        Line a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):new Line(new Cell("").withColSpan(6)));
                        Line b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):new Line(new Cell("").withColSpan(6)));
                        println(new Line(a.withLength(60), b));
                    }
                }            	
            } else {
                for (int idx=0; idx<lines.size(); idx+=3*n) {
                    for (int i=0;i<n;i++) {
                    	Line a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):new Line(new Cell("").withColSpan(5)));
                        Line b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):new Line(new Cell("").withColSpan(5)));
                        Line c = (i+idx+2*n<lines.size()?lines.elementAt(i+idx+2*n):new Line(new Cell("").withColSpan(5)));
                        println(new Line(a.withLength(43), b.withLength(43), c));
                    }
                }
            }
        } else {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamAssignment exam : new TreeSet<ExamAssignment>(getExams())) {
                if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
                Vector<Cell> rooms = new Vector<Cell>();
                if (exam.getRooms()==null || exam.getRooms().isEmpty()) {
                    rooms.add(formatRoom(iNoRoom));
                } else for (ExamRoomInfo room : exam.getRooms()) {
                    rooms.add(formatRoom(room));
                }
                Vector<ExamSectionInfo> sections = new Vector(exam.getSectionsIncludeCrosslistedDummies());
                Collections.sort(sections, new Comparator<ExamSectionInfo>() {
                    public int compare(ExamSectionInfo s1, ExamSectionInfo s2) {
                        if (!hasSubjectAreas()) return s1.compareTo(s2);
                        if (hasSubjectArea(s1.getOwner().getCourse().getSubjectArea())) {
                            if (!hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return -1;
                        } else if (hasSubjectArea(s2.getOwner().getCourse().getSubjectArea())) return 1;
                        return s1.compareTo(s2);
                    }
                 });
                for (int i=0;i<Math.max(rooms.size(),sections.size());i++) {
                    Cell a = (i<rooms.size()?rooms.elementAt(i):rpad("",11));
                    ExamSectionInfo section = (i<sections.size()?sections.elementAt(i):null);
                    boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                    if ((lx%n)!=0 && last!=null) {
                        if (section!=null && last.getSubject().equals(section.getSubject())) { 
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
                    if (i==0) {
                        if (iItype) {
                            lines.add(new Line(
                                     rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                     rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                     rpad(sameItype && isSkipRepeating()?"":section.getItype().length()==0?MSG.lrALL():section.getItype(),5),
                                     formatSection10(sameSct && isSkipRepeating()?"":section.getSection()).withSeparator(""),
                                     formatShortPeriodDate(section.getExamAssignment()), formatShortPeriodTime(section.getExamAssignment()),
                                    a));
                            } else {
                                lines.add(new Line(
                                		rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                		rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                		formatSection10(sameSct && isSkipRepeating()?"":section.getSection().length()==0?MSG.lrALL():section.getSection()).withSeparator(""),
                                        formatPeriodDate(section.getExamAssignment()), formatPeriodTime(section.getExamAssignment()),
                                        a));
                            }
                    } else if (section!=null) {
                        if (iItype) {
                            String w = MSG.lrWith()+(sameCrs?"":section.getSubject()+" ")+
                                (sameCrs && isSkipRepeating()?"":section.getCourseNbr()+" ")+
                                (sameItype && isSkipRepeating()?"":(section.getItype().length()==0?MSG.lrALL():section.getItype())+" ")+
                                (sameSct && isSkipRepeating()?"":section.getSection()); 
                            lines.add(new Line(lpad(w, 32).withColSpan(4), rpad("", 33).withColSpan(3)));
                        } else {
                            String w = MSG.lrWith()+
                            (sameCrs && isSkipRepeating()?"":section.getSubject()+" ")+
                            (sameCrs && isSkipRepeating()?"":section.getCourseNbr()+" ")+
                            (sameSct && isSkipRepeating()?"":section.getSection().length()==0?MSG.lrALL():section.getSection());
                            lines.add(new Line(lpad(w, 26).withColSpan(3), rpad("", 37).withColSpan(3)));
                        }
                    } else {
                        lines.add(new Line(rpad("",(iItype?54:52)).withColSpan(iItype?6:5),a));
                    }
                    lx++;
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeaderLine(new Line(new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5)).withLength(66).withLineSeparator("|"),
                    		new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5))),
                    		new Line(new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5)).withLength(66).withLineSeparator("|"),
                    		new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5))));
                } else {
                    setHeaderLine(new Line(new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5)).withLength(66).withLineSeparator("|"),
                    		new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5))),
                    		new Line(new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5)).withLength(66).withLineSeparator("|"),
                    		new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5))));
                }
            } else {
                setHeaderLine(new Line(new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5)).withLength(65),
                		new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5))),
                		new Line(new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9), rpad("", '-', 9), rpad("", '-', 15), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5)).withLength(65),
                		new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9), rpad("", '-', 9), rpad("", '-', 15), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5))));
            }
            printHeader();
            for (int idx=0; idx<lines.size(); idx+=2*n) {
                for (int i=0;i<n;i++) {
                    Line a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):new Line(new Cell("").withColSpan(iItype?7:6)));
                    Line b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):new Line(new Cell("").withColSpan(iItype?7:6)));
                    if (iItype)
                    	println(a.withLength(66).withLineSeparator("|"), b);
                    else
                    	println(a.withLength(65), b);
                }
            }
        }
    }
}
