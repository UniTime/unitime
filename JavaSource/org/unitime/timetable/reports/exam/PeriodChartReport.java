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
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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
public class PeriodChartReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    
    public PeriodChartReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "PERIOD ASSIGNMENT", session, examType, subjectAreas, exams);
    }
    
    public void printReport() throws DocumentException {
        if (iRC!=null && iRC.length()>0)
            setFooter(iRC+(iLimit>=0?" (limit="+iLimit+")":""));
        else if (iLimit>=0)
            setFooter("limit="+iLimit);
        Hashtable<ExamPeriod,TreeSet<ExamSectionInfo>> period2courseSections = new Hashtable();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
            TreeSet<ExamSectionInfo> sections = period2courseSections.get(exam.getPeriod());
            if (sections==null) {
                sections = new TreeSet();
                period2courseSections.put(exam.getPeriod(),sections);
            }
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (!hasSubjectArea(section)) continue;
                sections.add(section);
            }
        }
        HashMap<Integer,String> times = new HashMap<Integer, String>();
        HashMap<Integer,String> fixedTimes = new HashMap<Integer, String>();
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
                setHeader(new String[] {
                		"Start Time  Exam            Enrl  Exam            Enrl  Exam            Enrl  Exam            Enrl  Exam            Enrl",
                		"----------  --------------- ----  --------------- ----  --------------- ----  --------------- ----  --------------- ----"
                    });
        	} else {
                setHeader(new String[] {
                        "Start Time Exam                     Enrl  Exam                     Enrl  Exam                     Enrl  Exam                     Enrl",
                        "---------- ------------------------ ----  ------------------------ ----  ------------------------ ----  ------------------------ ----"
                     //  .........1.........2.........3.........4.........5.........6.........7.........8.........9........10........11........12........13...
                    });
        	}
            printHeader();
        }
        int lastDIdx = 0;
        boolean firstLine = true;
        for (int dIdx = 0; dIdx < days.size(); dIdx+=nrCols) {
            for (int time: new TreeSet<Integer>(times.keySet())) {
                int offset = 0;
                String timeStr = times.get(time);
                String header1 = "";
                String header2 = "";
                String header3 = "";
                Vector periods = new Vector();
                int idx = 0;
                String firstDay = null; int firstDayOffset = 0;
                String lastDay = null;
                nrCols = 0;
                for (Iterator<Integer> f = new TreeSet<Integer>(days.keySet()).iterator();f.hasNext();idx++) {
                	int day = f.next();
                    String dayStr = days.get(day);
                    if (idx<dIdx || (firstDay!=null && (dayStr.startsWith("Mon") || day>=firstDayOffset+7)) || nrCols==(iCompact?iTotals?6:5:4)) continue;
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
                    	header1 += mpad(dayStr,20)+"  ";
                    	header2 += "Exam            Enrl  ";
                    	header3 += "=============== ====  ";
                    } else {
                        header1 += mpad(dayStr,29)+"  "; 
                        header2 += "Exam                     Enrl  ";
                        header3 += "======================== ====  ";
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
                	setHeader(new String[] {timeStr,header1,header2,header3});
                else if (offset + periods.size() > (iCompact?iTotals?6:5:4))
                	offset = Math.max(0, (iCompact?iTotals?6:5:4) - periods.size());
                int nextLines = 0;
                for (Enumeration f=periods.elements();f.hasMoreElements();) {
                    ExamPeriod period = (ExamPeriod)f.nextElement();
                    if (period==null) continue;
                    TreeSet<ExamSectionInfo> sections = period2courseSections.get(period);
                    if (sections==null) continue;
                    int linesThisSections = 6;
                    for (ExamSectionInfo section : sections)
                        if (iLimit<0 || section.getNrStudents()>=iLimit) linesThisSections ++;
                    nextLines = Math.max(nextLines,linesThisSections);
                }
                if (iTotals) {
                    if (!headerPrinted) {
                        printHeader();
                        setPageName(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        setCont(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        timesThisPage = timeStr;
                    } else if (timesThisPage!=null && getLineNumber()+nextLines<=iNrLines) {
                        println("");
                        println(timeStr);
                        println(header1);
                        println(header2);
                        println(header3);
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
                        		println("----------  --------------- ----  --------------- ----  --------------- ----  --------------- ----  --------------- ----");
                        	else
                        		println("---------- ------------------------ ----  ------------------------ ----  ------------------------ ----  ------------------------ ----");
                            lastDIdx = dIdx;
                        } else {
                        	if (iCompact)
                        		println("            --------------- ----  --------------- ----  --------------- ----  --------------- ----  --------------- ----");
                        	else
                        		println("           ------------------------ ----  ------------------------ ----  ------------------------ ----  ------------------------ ----");
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
                        linesThisPeriod.add(lpad("0",iCompact ? 20 : 29));
                        lines.add(linesThisPeriod);
                        continue;
                    }
                    TreeSet<ExamSectionInfo> sections = period2courseSections.get(period);
                    if (sections==null) sections = new TreeSet();
                    Vector linesThisPeriod = new Vector();
                    int total = 0;
                    int totalListed = 0;
                    for (ExamSectionInfo section : sections) {
                        total += section.getNrStudents();
                        if (iLimit>=0 && section.getNrStudents()<iLimit) continue;
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
                                    rpad(section.getSubject(),7)+
                                    rpad(section.getCourseNbr(),8)+
                                    (code==null||code.length()==0?' ':code.charAt(0))+
                                    lpad(String.valueOf(section.getNrStudents()),4));
                        } else {
                            if (iItype) {
                                if (iExternal) {
                                    linesThisPeriod.add(
                                            rpad(section.getSubject(),7)+
                                            rpad(section.getCourseNbr(),8)+
                                            rpad(section.getItype(),9)+
                                            (code==null||code.length()==0?' ':code.charAt(0))+
                                            lpad(String.valueOf(section.getNrStudents()),4));
                                } else {
                                    linesThisPeriod.add(
                                            rpad(section.getName(),24)+(code==null||code.length()==0?' ':code.charAt(0))+
                                            lpad(String.valueOf(section.getNrStudents()),4));
                                }
                            } else {
                                linesThisPeriod.add(
                                        rpad(section.getSubject(),7)+
                                        rpad(section.getCourseNbr(),8)+
                                        rpad(section.getSection(),9)+
                                        (code==null||code.length()==0?' ':code.charAt(0))+
                                        lpad(String.valueOf(section.getNrStudents()),4));
                            }
                        }
                    }
                    if (iCompact) {
                    	if (iTotals) {
                            if (totalListed!=total)
                                linesThisPeriod.insertElementAt(mpad("("+totalListed+")",13)+" "+lpad(""+total,6), 0);
                            else
                                linesThisPeriod.insertElementAt(lpad(""+total,20), 0);
                        } else {
                            linesThisPeriod.insertElementAt(rpad(period.getStartDateLabel(),13)+" "+lpad(total==0?"":(""+total),6), 0);
                        }
                    } else {
                        if (iTotals) {
                            if (totalListed!=total)
                                linesThisPeriod.insertElementAt(mpad("("+totalListed+")",22)+" "+lpad(""+total,6), 0);
                            else
                                linesThisPeriod.insertElementAt(lpad(""+total,29), 0);
                        } else {
                            linesThisPeriod.insertElementAt(rpad(period.getStartDateLabel(),22)+" "+lpad(total==0?"":(""+total),6), 0);
                        }
                    }
                    max = Math.max(max, linesThisPeriod.size());
                    Integer td = (Integer)totalADay.get(period.getDateOffset());
                    totalADay.put(period.getDateOffset(),new Integer(total+(td==null?0:td.intValue())));
                    lines.add(linesThisPeriod);
                }
                for (int i=0;i<max;i++) {
                    String line = "";
                    if (!iTotals) {
                    	if (iCompact) {
                            if (i==0 || iNewPage)
                                line += rpad(fixedTimes.get(time),12)+rpad("",offset*22);
                            else
                                line += rpad("",12)+rpad("",offset*22);
                    	} else {
                            if (i==0 || iNewPage)
                                line += rpad(fixedTimes.get(time),11)+rpad("",offset*31);
                            else
                                line += rpad("",11)+rpad("",offset*31);
                    	}
                    }
                    for (Enumeration f=lines.elements();f.hasMoreElements();) {
                        Vector linesThisPeriod = (Vector)f.nextElement();
                        if (i<linesThisPeriod.size())
                            line += (String)linesThisPeriod.elementAt(i);
                        else
                            line += rpad("",iCompact ? 20 : 29);
                        if (f.hasMoreElements()) line += "  ";
                    }
                    println(line);
                }
                setCont(null);
            }
            if (iTotals) {
                if (getLineNumber()+5>iNrLines) {
                    newPage();
                    setPageName("Totals");
                } else 
                    println("");
                println("Total Student Exams");
                String line1 = "", line2 = "", line3 = "";
                int idx = 0;
                for (Iterator<Integer> f = new TreeSet<Integer>(days.keySet()).iterator(); f.hasNext(); idx++) {
                    Integer day = f.next();
                    if (idx<dIdx || idx>=dIdx+nrCols) continue;
                    if (iCompact) {
                        line1 += mpad((String)days.get(day),20)+"  ";
                        line2 += "=============== ====  ";
                        line3 += lpad(totalADay.get(day)==null?"":totalADay.get(day).toString(),20)+"  ";
                    } else {
                        line1 += mpad((String)days.get(day),29)+"  ";
                        line2 += "======================== ====  ";
                        line3 += lpad(totalADay.get(day)==null?"":totalADay.get(day).toString(),29)+"  ";
                    }
                }
                println(line1);
                println(line2);
                println(line3);
                timesThisPage = null;
            }
        }
        lastPage();
    }
}
