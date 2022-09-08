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
import java.util.TreeSet;
import java.util.Vector;

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
public class AbbvScheduleByCourseReport extends PdfLegacyExamReport {
    public AbbvScheduleByCourseReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legacyReportScheduleByCourse(), session, examType, subjectAreas, exams);
    }

    public void printReport() throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (!hasSubjectArea(section)) continue;
                sections.add(section);
            }
        }
        Vector<Line> lines = new Vector<Line>();
        int split = 9;
        int n = 9;
        if (getNrLinesPerPage() > 0)
        	n = getNrLinesPerPage() - 1 - getSeparatorNrLines() - (getSeparatorNrLines() == 0 ? 0 : (getNrLinesPerPage() - 2) / (split + 1));
        else {
        	int rows = 0;
        	if (!iDispRooms) {
        		rows = sections.size();
        		if (iItype)
        			n = (rows + 1) / 2;
        		else
        			n = (rows + 2) / 3;
        	} else {
        		for (ExamSectionInfo section : sections)
        			rows += Math.max(1, section.getExamAssignment().getNrRooms());
        		n = (rows + 1) / 2;
        	}
        	split = rows;
        }
        if (!iDispRooms) {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamSectionInfo section : sections) {
                boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                if ((lx%n)!=0 && ((lx%n)%split)!=0 && last!=null) {
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
                if (iItype) {
                    lines.add(new Line(
                    	rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                        rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                        rpad(sameItype && isSkipRepeating()?"":section.getItype().length()==0?MSG.lrALL():section.getItype(),5),
                        formatSection10(sameSct && isSkipRepeating()?"":section.getSection()),
                        formatPeriodDate(section.getExamAssignment()), formatPeriodTime(section.getExamAssignment())
                        ));
                } else {
                    lines.add(new Line(
                    		rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                            rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                            formatSection10(sameSct && isSkipRepeating()?"":section.getSection().length()==0?MSG.lrALL():section.getSection()),
                            formatShortPeriodNoEndTimeDate(section.getExamAssignment()), formatShortPeriodNoEndTimeTime(section.getExamAssignment())
                            ));
                }
            }
            if (iItype) {
                if (iExternal) {
                	setHeaderLine(
                			new Line(
                					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15)).withLength(60),
                					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15))
                			), new Line(
                        			new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15)).withLength(60),
                        			new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15))
                			));
                } else {
                	setHeaderLine(
                			new Line(
                					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15)).withLength(60),
                					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15))
                			), new Line(
                        			new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15)).withLength(60),
                        			new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 9), rpad("", '-', 15))
                			));
                }
            } else {
            	setHeaderLine(
            			new Line(
            					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6)).withLength(43),
            					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6)).withLength(43),
            					new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9).withSeparator("  "), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 6))
            			), new Line(
            					new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 7), rpad("", '-', 6)).withLength(43),
            					new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 7), rpad("", '-', 6)).withLength(43),
            					new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9).withSeparator("  "), rpad("", '-', 7), rpad("", '-', 6))
            			));
            }
            printHeader();
            if (iItype) {
                for (int idx=0; idx<lines.size(); idx+=2*n) {
                    for (int i=0;i<n;i++) {
                        Line a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):new Line(new Cell("").withColSpan(6)));
                        Line b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):new Line(new Cell("").withColSpan(6)));
                        println(a.withLength(60), b);
                        if ((i%split)==split-1 && idx<lines.size()) printSeparator();
                    }
                }
            } else {
                for (int idx=0; idx<lines.size(); idx+=3*n) {
                    for (int i=0;i<n;i++) {
                        Line a = (i+idx+0*n<lines.size()?lines.elementAt(i+idx+0*n):new Line(new Cell("").withColSpan(5)));
                        Line b = (i+idx+1*n<lines.size()?lines.elementAt(i+idx+1*n):new Line(new Cell("").withColSpan(5)));
                        Line c = (i+idx+2*n<lines.size()?lines.elementAt(i+idx+2*n):new Line(new Cell("").withColSpan(5)));
                        println(a.withLength(43), b.withLength(43), c);
                        if ((i%split)==split-1 && idx<lines.size()) printSeparator();
                    }
                }
            }
        } else {
            ExamSectionInfo last = null; int lx = 0;
            for (ExamSectionInfo section : sections) {
                boolean sameSubj = false, sameCrs = false, sameSct = false, sameItype = false;
                if ((lx%n)!=0 && ((lx%n)%split)!=0 && last!=null) {
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
                if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                    if (iItype) {
                        lines.add(new Line(
                                 rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                 rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                 rpad(sameItype && isSkipRepeating()?"":section.getItype().length()==0?MSG.lrALL():section.getItype(),5),
                                 formatSection10(sameSct && isSkipRepeating()?"":section.getSection()).withSeparator(""),
                                 formatShortPeriodDate(section.getExamAssignment()), formatShortPeriodTime(section.getExamAssignment()),
                                 rpad(iNoRoom,23)
                                 ));
                        } else {
                            lines.add(new Line(
                            		rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                            		rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                            		formatSection10(sameSct && isSkipRepeating()?"":section.getSection().length()==0?MSG.lrALL():section.getSection()).withSeparator(""),
                            		formatPeriodDate(section.getExamAssignment()), formatPeriodTime(section.getExamAssignment()),
                            		rpad(iNoRoom,23)
                            		));
                        }
                } else {
                    Vector<ExamRoomInfo> rooms = new Vector(section.getExamAssignment().getRooms());
                    for (int i=0;i<rooms.size();i++) {
                        ExamRoomInfo a = rooms.elementAt(i);
                        if (i==0) {
                            if (iItype) {
                                lines.add(new Line(
                                		rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                		rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                		rpad(sameItype && isSkipRepeating()?"":section.getItype().length()==0?MSG.lrALL():section.getItype(),5),
                                		formatSection10(sameSct && isSkipRepeating()?"":section.getSection()).withSeparator(""),
                                		formatShortPeriodDate(section.getExamAssignment()), formatShortPeriodTime(section.getExamAssignment()),
                                		formatRoom(a)
                                		));
                                } else {
                                    lines.add(new Line(
                                    		rpad(sameSubj && isSkipRepeating()?"":section.getSubject(),7),
                                            rpad(sameCrs && isSkipRepeating()?"":section.getCourseNbr(),8),
                                            formatSection10(sameSct && isSkipRepeating()?"":section.getSection().length()==0?MSG.lrALL():section.getSection()).withSeparator(""),
                                            formatPeriodDate(section.getExamAssignment()), formatPeriodTime(section.getExamAssignment()),
                                            formatRoom(a)
                                            ));
                                }
                        } else {
                            lines.add(new Line(
                                    rpad("",(iItype?54:52)).withColSpan(iItype ? 6 : 5),
                                    formatRoom(a)
                                    ));
                            lx++;
                        }
                    }
                }
            }
            if (iItype) {
                if (iExternal) {
                    setHeaderLine(
                    		new Line(
                    				new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5)).withLength(66).withLineSeparator("|"),
                    				new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrExtID(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5))
                    		), new Line(
                    				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5)).withLength(66).withLineSeparator("|"),
                    				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5))
                    		));
                } else {
                    setHeaderLine(
                    		new Line(
                    				new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5)).withLength(66).withLineSeparator("|"),
                    				new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrType(), 5), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 7), rpad(MSG.lrTime(), 13), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5))
                    		), new Line(
                    				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5)).withLength(66).withLineSeparator("|"),
                    				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 5), rpad("", '-', 9), rpad("", '-', 7), rpad("", '-', 13), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5))
                    		));
                }
            } else {
                setHeaderLine(
                		new Line(
                				new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5)).withLength(65),
                				new Line(rpad(MSG.lrSubject(), 7), rpad(MSG.lrCourse(), 8), rpad(MSG.lrSection(), 9), rpad(MSG.lrDate(), 9), rpad(MSG.lrTime(), 15), rpad(MSG.lrBldg(), 5).withColSpan(0), rpad(MSG.lrRoom(), 5))
                		), new Line(
                				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9), rpad("", '-', 9), rpad("", '-', 15), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5)).withLength(65),
                				new Line(rpad("", '-', 7), rpad("", '-', 8), rpad("", '-', 9), rpad("", '-', 9), rpad("", '-', 15), rpad("", '-', 5).withColSpan(0), rpad("", '-', 5))
                		));
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
                    if ((i%split)==split-1 && idx<lines.size()) printSeparator();
                }
            }
        }
    }
}
