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
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
public class ExamPeriodChartReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    
    public ExamPeriodChartReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "PERIOD ASSIGNMENT", session, examType, subjectAreas, exams);
    }
    
    public void printReport() throws DocumentException {
        if (iRC!=null && iRC.length()>0)
            setFooter(iRC+(iLimit>=0?" (limit="+iLimit+")":""));
        else if (iLimit>=0)
            setFooter("limit="+iLimit);
        Hashtable<ExamPeriod,TreeSet<ExamAssignmentInfo>> period2exams = new Hashtable();
        for (ExamAssignmentInfo exam : getExams()) {
        	if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
            TreeSet<ExamAssignmentInfo> exams = period2exams.get(exam.getPeriod());
            if (exams==null) {
                exams = new TreeSet();
                period2exams.put(exam.getPeriod(),exams);
            }
            exams.add(exam);
        }
        HashMap<Integer,String> times = new HashMap<Integer, String>();
        HashMap<Integer,Cell> fixedTimes = new HashMap<Integer, Cell>();
        HashMap<Integer,String> days = new HashMap<Integer, String>();
        for (Iterator i=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            times.put(period.getStartSlot(), period.getStartTimeLabel());
            days.put(period.getDateOffset(), period.getStartDateLabel());
            fixedTimes.put(period.getStartSlot(), lpad(period.getStartTimeLabel(),'0',6));
        }
        boolean headerPrinted = false;
        
        Hashtable totalADay = new Hashtable();
        String timesThisPage = null;
        int nrCols = 0;
        if (!iTotals) {
        	if (iCompact) {
        		setHeaderLine(new Line(
        				rpad("Start Time", 10).withSeparator("| "),
        				rpad("Exam", 15).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 15).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 15).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 15).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 15).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4)),
        				new Line(rpad("", '-', 10).withSeparator("| "),
                		rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 15).withColSpan(2), rpad("", '-', 4)));
        	} else {
        		setHeaderLine(new Line(
        				rpad("Start Time", 10).withSeparator("|"),
        				rpad("Exam", 24).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 24).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 24).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4).withSeparator("| "),
        				rpad("Exam", 24).withSeparator(""), rpad(" ", 1).withSeparator(""), rpad("Enrl", 4)),
        				new Line(rpad("", '-', 10).withSeparator("|"),
        				rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4)));
        	}
            printHeader();
        }
        int lastDIdx = 0;
        boolean firstLine = true;
        for (int dIdx = 0; dIdx < days.size(); dIdx+=nrCols) {
            for (int time: new TreeSet<Integer>(times.keySet())) {
                int offset = 0;
                String timeStr = times.get(time);
                List<Cell> header1 = new ArrayList<Cell>();
                List<Cell> header2 = new ArrayList<Cell>();
                List<Cell> header3 = new ArrayList<Cell>();
                Vector periods = new Vector();
                int idx = 0;
                String firstDay = null; int firstDayOffset = 0;
                String lastDay = null;
                nrCols = 0;
                for (Iterator<Integer> f = new TreeSet<Integer>(days.keySet()).iterator(); f.hasNext(); idx++) {
                    int day =  f.next();
                    String dayStr = days.get(day);
                    if (idx<dIdx || nrCols==(iCompact?iTotals?6:5:4)) continue;
                    if (firstDay!=null && (dayStr.startsWith("Mon") || day>=firstDayOffset+7)) break;
                    if (firstDay==null) {
                        firstDay = dayStr; firstDayOffset = day;
                        Calendar c = Calendar.getInstance(Locale.US);
                        c.setTime(getSession().getExamBeginDate());
                        c.add(Calendar.DAY_OF_YEAR, day);
                        if (!iTotals) {
                            offset = (c.get(Calendar.DAY_OF_WEEK)+5)%7;
                            firstDayOffset -= offset;
                        }
                    } 
                    lastDay = dayStr;
                    if (iCompact) {
                    	header1.add(mpad(dayStr,20).withSeparator("| ").withColSpan(3));
                    	header2.add(rpad("Exam", 15).withSeparator(""));
                    	header2.add(rpad(" ", 1).withSeparator(""));
                    	header2.add(rpad("Enrl", 4).withSeparator("| "));
                    	header3.add(lpad("", '=', 15).withColSpan(2)); header3.add(lpad("", '=', 4).withSeparator("| "));
                    } else {
                    	header1.add(mpad(dayStr,29).withSeparator("| ").withColSpan(3)); 
                    	header2.add(rpad("Exam", 24).withSeparator(""));
                    	header2.add(rpad(" ", 1).withSeparator(""));
                    	header2.add(rpad("Enrl", 4).withSeparator("| "));
                    	header3.add(lpad("", '=', 24).withColSpan(2)); header3.add(lpad("", '=', 4).withSeparator("| "));
                    }
                    ExamPeriod period = null;
                    nrCols++;
                    for (Iterator i=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
                        ExamPeriod p = (ExamPeriod)i.next();
                        if (time!=p.getStartSlot() || day!=p.getDateOffset()) continue;
                        period = p; break;
                    }
                    periods.add(period);
                }
                if (iTotals)
                	setHeaderLine(
                			new Line(new Cell(timeStr).withColSpan(header2.size())),
                			new Line(header1.toArray(new Cell[header1.size()])),
                			new Line(header2.toArray(new Cell[header2.size()])),
                			new Line(header3.toArray(new Cell[header3.size()])));
                else if (offset + periods.size() > (iCompact?iTotals?6:5:4))
                	offset = Math.max(0, (iCompact?iTotals?6:5:4) - periods.size());
                int nextLines = 0;
                for (Enumeration f=periods.elements();f.hasMoreElements();) {
                    ExamPeriod period = (ExamPeriod)f.nextElement();
                    if (period==null) continue;
                    TreeSet<ExamAssignmentInfo> exams = period2exams.get(period);
                    if (exams==null) continue;
                    int linesThisSections = 6;
                    for (ExamAssignmentInfo exam : exams) {
                        int size = 0;
                        for (ExamSectionInfo section: exam.getSectionsIncludeCrosslistedDummies()) size+= section.getNrStudents();
                        if (iLimit<0 || size>=iLimit) {
                            for (ExamSectionInfo section: exam.getSectionsIncludeCrosslistedDummies())
                                if (hasSubjectArea(section)) linesThisSections++;
                        }
                    }
                    nextLines = Math.max(nextLines,linesThisSections);
                }
                if (iTotals) {
                    if (!headerPrinted) {
                        printHeader();
                        setPageName(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        setCont(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        timesThisPage = timeStr;
                    } else if (timesThisPage!=null && (getNrLinesPerPage() == 0 || getLineNumber()+nextLines<=getNrLinesPerPage())) {
                        println(new Line());
                        printHeader(false);
                        timesThisPage += ", "+timeStr;
                        setPageName(timesThisPage+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        setCont(timesThisPage+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                    } else {
                        newPage();
                        timesThisPage = timeStr;
                        setPageName(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        setCont(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                    }
                } else {
                    if (nextLines==0) continue;
                    if (!iNewPage && !firstLine) {
                    	if (lastDIdx!=dIdx) {
                        	if (iCompact)
                        		printSeparator(rpad("", '-', 10).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4));
                        	else
                        		printSeparator(rpad("", '-', 10),
                        				rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4));
                            lastDIdx = dIdx;
                        } else {
                        	if (iCompact)
                        		printSeparator(lpad("", ' ', 10).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                        				rpad("", '-', 15).withColSpan(2), rpad("", '-', 4));
                        	else
                        		printSeparator(lpad("", ' ', 10),
                        				rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4).withSeparator("| "),
                                		rpad("", '-', 24).withColSpan(2), rpad("", '-', 4));
                        }
                    }
                    firstLine = false;
                    setCont(firstDay+" - "+lastDay+" "+fixedTimes.get(time));
                    setPageName(firstDay+" - "+lastDay+" "+fixedTimes.get(time));
                }
                headerPrinted = true;
                int max = 0;
                Vector lines = new Vector();
                for (Enumeration f=periods.elements();f.hasMoreElements();) {
                    ExamPeriod period = (ExamPeriod)f.nextElement();
                    if (period==null) {
                        Vector linesThisPeriod = new Vector();
                        linesThisPeriod.add(new Cell[] {lpad("", iCompact ? 15 : 24).withColSpan(2), lpad("0",5)});
                        lines.add(linesThisPeriod);
                        continue;
                    }
                    TreeSet<ExamAssignmentInfo> exams = period2exams.get(period);
                    if (exams==null) exams = new TreeSet();
                    Vector<Cell[]> linesThisPeriod = new Vector<Cell[]>();
                    int total = 0;
                    int totalListed = 0;
                    for (ExamAssignmentInfo exam : exams) {
                        boolean sizePrinted = false;
                        int size = 0;
                        for (ExamSectionInfo section: exam.getSectionsIncludeCrosslistedDummies()) size+= section.getNrStudents();
                        for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                            if (!hasSubjectArea(section)) continue;
                            total += section.getNrStudents();
                            if (iLimit>=0 && size<iLimit) continue;
                            totalListed += section.getNrStudents();
                            String code = null;
                            if (iRoomCodes!=null && !iRoomCodes.isEmpty()) {
                                for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                                    String c = iRoomCodes.get(room.getName());
                                    if (c!=null) code = c; break;
                                }
                            }
                            if (iCompact) {
                            	linesThisPeriod.add(
                            			new Cell[] {
                            					new Cell(
                            							rpad(section.getSubject(),7).withSeparator(""),
                            							rpad(section.getCourseNbr(),8).withSeparator("")).withSeparator(""),
                            					new Cell(String.valueOf(sizePrinted||code==null||code.length()==0?' ':code.charAt(0))).withSeparator(""),
                            					lpad(sizePrinted?"":String.valueOf(size),4)
                            			});
                            } else {
                                if (iItype) {
                                    if (iExternal) {
                                    	linesThisPeriod.add(
                                    			new Cell[] {
                                    					new Cell(
                                    							rpad(section.getSubject(),7).withSeparator(""),
                                    							rpad(section.getCourseNbr(),8).withSeparator(""),
                                    							rpad(section.getItype(),9).withSeparator("")).withSeparator(""),
                                    					new Cell(String.valueOf(sizePrinted||code==null||code.length()==0?' ':code.charAt(0))).withSeparator(""),
                                    					lpad(sizePrinted?"":String.valueOf(size),4)
                                    			});
                                    } else {
                                        linesThisPeriod.add(
                                        		new Cell[] {
                                        				rpad(section.getName(),24).withSeparator(""),
                                        				new Cell(String.valueOf(sizePrinted||code==null||code.length()==0?' ':code.charAt(0))).withSeparator(""),
                                        				lpad(sizePrinted?"":String.valueOf(size),4)
                                        		});
                                    }
                                } else {
                                    linesThisPeriod.add(
                                    		new Cell[] {
                                    				new Cell(
                                    						rpad(section.getSubject(),7).withSeparator(""),
                                							rpad(section.getCourseNbr(),8).withSeparator(""),
                                							rpad(section.getItype(),9).withSeparator("")).withSeparator(""),
                                    				new Cell(String.valueOf(sizePrinted||code==null||code.length()==0?' ':code.charAt(0))).withSeparator(""),
                                    				lpad(sizePrinted?"":String.valueOf(size),4)
                                    		});
                                }
                            }
                            sizePrinted = true;
                        }
                    }
                    if (iCompact) {
                    	if (iTotals) {
                            if (totalListed!=total)
                            	linesThisPeriod.insertElementAt(new Cell[] {mpad("("+totalListed+")",14).withColSpan(2), lpad(""+total,6)}, 0);
                            else
                            	linesThisPeriod.insertElementAt(new Cell[] {lpad(""+total,20).withColSpan(3)}, 0);
                        } else {
                        	linesThisPeriod.insertElementAt(new Cell[] {rpad(period.getStartDateLabel(),14).withColSpan(2), lpad(total==0?"":(""+total),6)}, 0);
                        }
                    } else {
                    	if (iTotals) {
                            if (totalListed!=total)
                                linesThisPeriod.insertElementAt(new Cell[] {mpad("("+totalListed+")",23).withColSpan(2), lpad(""+total,6)}, 0);
                            else
                                linesThisPeriod.insertElementAt(new Cell[] {lpad(""+total,29).withColSpan(3)}, 0);
                        } else {
                            linesThisPeriod.insertElementAt(new Cell[] {rpad(period.getStartDateLabel(),23).withColSpan(2), lpad(total==0?"":(""+total),6)}, 0);
                        }
                    }
                    max = Math.max(max, linesThisPeriod.size());
                    Integer td = (Integer)totalADay.get(period.getDateOffset());
                    totalADay.put(period.getDateOffset(),new Integer(total+(td==null?0:td.intValue())));
                    lines.add(linesThisPeriod);
                }
                for (int i=0;i<max;i++) {
                    List<Cell> line = new ArrayList<Cell>();
                    if (!iTotals) {
                    	if (iCompact) {
                            if (i==0 || iNewPage) {
                            	line.add(rpad(fixedTimes.get(time),10).withSeparator("| "));
                            	for (int c = 0; c < offset; c++)
                            		line.add(rpad("",20).withSeparator("| ").withColSpan(3));
                            } else {
                            	line.add(rpad("",10).withSeparator("| "));
                            	for (int c = 0; c < offset; c++)
                            		line.add(rpad("",20).withSeparator("| ").withColSpan(3));
                            }
                    	} else {
                            if (i==0 || iNewPage) {
                            	line.add(rpad(fixedTimes.get(time),10).withSeparator("|"));
                            	for (int c = 0; c < offset; c++)
                            		line.add(rpad("",29).withSeparator("| ").withColSpan(3));
                            } else {
                            	line.add(rpad("",10).withSeparator("|"));
                            	for (int c = 0; c < offset; c++)
                            		line.add(rpad("",29).withSeparator("| ").withColSpan(3));
                            }
                    	}
                    }
                    for (Enumeration f=lines.elements();f.hasMoreElements();) {
                        Vector linesThisPeriod = (Vector)f.nextElement();
                        if (i < linesThisPeriod.size()) {
                        	Cell[] c = (Cell[])linesThisPeriod.elementAt(i);
                        	for (int j = 0; j < c.length; j++)
                        		line.add(c[j].withSeparator(j + 1 == c.length ? "| " : ""));
                        } else {
                        	line.add(rpad("",iCompact ? 20 : 29).withColSpan(3).withSeparator("| "));
                        }
                    }
                    if (!iTotals)
                    	for (int c = offset + lines.size(); c < (iCompact ? 5 : 4); c++) {
                    		if (iCompact) {
                    			line.add(rpad("",20).withSeparator("| ").withColSpan(3));
                    		} else {
                    			line.add(rpad("",29).withSeparator("|").withColSpan(3));
                    		}
                    	}
                    println(line.toArray(new Cell[line.size()]));
                }
                setCont(null);
            }
            if (iTotals) {
            	setHeaderLine();
                if (getLineNumber()+5>getNrLinesPerPage() && getNrLinesPerPage() > 0) {
                    newPage();
                    setPageName("Totals");
                } else 
                    println(new Line());
                List<Cell> line1 = new ArrayList<Cell>();
                List<Cell> line2 = new ArrayList<Cell>();
                List<Cell> line3 = new ArrayList<Cell>();
                int idx = 0;
                for (Iterator<Integer> f = new TreeSet<Integer>(days.keySet()).iterator(); f.hasNext(); idx++) {
                    int day =  f.next();
                    if (idx<dIdx || idx>=dIdx+nrCols) continue;
                    if (iCompact) {
                    	line1.add(mpad((String)days.get(day),20).withSeparator("| ").withColSpan(3));
                    	line2.add(lpad("", '=', 15)); line2.add(lpad("", '=', 4).withSeparator("| "));
                    	line2.add(lpad("", '=', 15).withSeparator("")); line2.add(lpad("", ' ', 1).withSeparator(""));  line2.add(lpad("", '=', 4).withSeparator("| "));
                    	line3.add(lpad(totalADay.get(day)==null?"":totalADay.get(day).toString(),20).withColSpan(3).withSeparator("| "));
                    } else {
                    	line1.add(mpad((String)days.get(day),29).withSeparator("| ").withColSpan(3));
                    	line2.add(lpad("", '=', 24).withSeparator("")); line2.add(lpad("", ' ', 1).withSeparator(""));  line2.add(lpad("", '=', 4).withSeparator("| "));
                    	line3.add(lpad(totalADay.get(day)==null?"":totalADay.get(day).toString(),29).withColSpan(3).withSeparator("| "));
                    }
                }
                setHeaderLine(
                		new Line(new Cell("Total Student Exams").withColSpan(iCompact ? 15 : 12)),
                		new Line(line1.toArray(new Cell[line1.size()])),
                		new Line(line2.toArray(new Cell[line2.size()])));
                printHeader(false);
                println(line3.toArray(new Cell[line3.size()]));
                timesThisPage = null;
            }
        }
        lastPage();
    }
}
