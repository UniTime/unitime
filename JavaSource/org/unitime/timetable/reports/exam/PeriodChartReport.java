package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class PeriodChartReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    protected Hashtable<String,String> iRoomCodes = new Hashtable();
    protected boolean iTotals = true;
    
    public PeriodChartReport(File file, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "PERIOD ASSIGNMENT", session, examType, exams);
        if (iLimit>=0) setFooter("limit="+iLimit);
        setRoomCode(System.getProperty("roomcode"));
        iTotals = "true".equals(System.getProperty("totals","true"));
    }
    
    public void setRoomCode(String roomCode) {
        if (roomCode==null || roomCode.length()==0)
            iRoomCodes = null;
        else {
            iRoomCodes = new Hashtable<String, String>();
            String codes = "";
            for (StringTokenizer s = new StringTokenizer(roomCode,":;,=");s.hasMoreTokens();) {
                String room = s.nextToken(), code = s.nextToken();
                iRoomCodes.put(room, code);
                if (codes.length()>0) codes += ", ";
                codes += code+":"+room;
            }
            if (codes.length()>0) setFooter(codes+(iLimit>=0?" (limit="+iLimit+")":""));
            System.out.println("  Room codes:"+codes);
        }
    }
    
    public void printReport() throws DocumentException {
        Hashtable<ExamPeriod,TreeSet<ExamSectionInfo>> period2courseSections = new Hashtable();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
            TreeSet<ExamSectionInfo> sections = period2courseSections.get(exam.getPeriod());
            if (sections==null) {
                sections = new TreeSet();
                period2courseSections.put(exam.getPeriod(),sections);
            }
            sections.addAll(exam.getSections());
        }
        Hashtable<Integer,String> times = new Hashtable();
        Hashtable<Integer,String> fixedTimes = new Hashtable();
        Hashtable<Integer,String> days = new Hashtable();
        TreeSet weeks = new TreeSet();
        for (Iterator i=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            times.put(period.getStartSlot(), period.getStartTimeLabel());
            days.put(period.getDateOffset(), period.getStartDateLabel());
            fixedTimes.put(period.getStartSlot(), lpad(period.getStartTimeLabel(),'0',6)+" - "+lpad(period.getEndTimeLabel(),'0',6));
        }
        boolean headerPrinted = false;
        
        Hashtable totalADay = new Hashtable();
        String timesThisPage = null;
        int nrCols = 0;
        if (!iTotals) {
            setHeader(new String[] {
                "Time            Exam            Enrl  Exam            Enrl  Exam            Enrl  Exam            Enrl  Exam            Enrl",
                "--------------- --------------- ----  --------------- ----  --------------- ----  --------------- ----  --------------- ----"
            });
            printHeader();
        }
        for (int dIdx = 0; dIdx < days.size(); dIdx+=nrCols) {
            for (Enumeration e=ToolBox.sortEnumeration(times.keys());e.hasMoreElements();) {
                int time = ((Integer)e.nextElement()).intValue();
                String timeStr = (String)times.get(new Integer(time));
                String header1 = "";
                String header2 = "";
                String header3 = "";
                Vector periods = new Vector();
                int idx = 0;
                String firstDay = null; int firstDayOffset = 0;
                String lastDay = null;
                nrCols = 0;
                for (Enumeration f=ToolBox.sortEnumeration(days.keys());f.hasMoreElements();idx++) {
                    int day = ((Integer)f.nextElement()).intValue();
                    String dayStr = days.get(day);
                    if (idx<dIdx || (firstDay!=null && (dayStr.startsWith("Mon") || day>=firstDayOffset+7)) || nrCols==(iTotals?6:5)) continue;
                    if (firstDay==null) {firstDay = dayStr; firstDayOffset = day; } 
                    lastDay = dayStr;
                    header1 += mpad(dayStr,20)+"  "; 
                    header2 += "Exam            Enrl  ";
                    header3 += "=============== ====  ";
                    ExamPeriod period = null;
                    nrCols++;
                    for (Iterator i=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
                        ExamPeriod p = (ExamPeriod)i.next();
                        if (time!=p.getStartSlot() || day!=p.getDateOffset()) continue;
                        period = p; break;
                    }
                    periods.add(period);
                }
                if (iTotals) setHeader(new String[] {timeStr,header1,header2,header3});
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
                if (nextLines==0) continue;
                if (iTotals) {
                    if (!headerPrinted) {
                        printHeader();
                        setPageName(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        setCont(timeStr+(days.size()>nrCols?" ("+firstDay+" - "+lastDay+")":""));
                        timesThisPage = timeStr;
                    } else if (timesThisPage!=null && getLineNumber()+nextLines<=sNrLines) {
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
                        linesThisPeriod.add(lpad("0",20));
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
                        if (iItype)
                            linesThisPeriod.add(
                                    rpad(section.getName(),15)+(code==null||code.length()==0?' ':code.charAt(0))+
                                    lpad(String.valueOf(section.getNrStudents()),4));
                            else
                                linesThisPeriod.add(
                                    rpad(section.getSubject(),4)+" "+
                                    rpad(section.getCourseNbr(),5)+" "+
                                    rpad(section.getSection(),3)+" "+
                                    (code==null||code.length()==0?' ':code.charAt(0))+
                                    lpad(String.valueOf(section.getNrStudents()),4));
                    }
                    if (iTotals) {
                        if (totalListed!=total)
                            linesThisPeriod.insertElementAt(mpad("("+totalListed+")",13)+" "+lpad(""+total,6), 0);
                        else
                            linesThisPeriod.insertElementAt(lpad(""+total,20), 0);
                    } else {
                        linesThisPeriod.insertElementAt(rpad(period.getStartDateLabel(),13)+" "+lpad(""+total,6), 0);
                    }
                    max = Math.max(max, linesThisPeriod.size());
                    Integer td = (Integer)totalADay.get(period.getDateOffset());
                    totalADay.put(period.getDateOffset(),new Integer(total+(td==null?0:td.intValue())));
                    lines.add(linesThisPeriod);
                }
                for (int i=0;i<max;i++) {
                    String line = "";
                    if (!iTotals) {
                        if (i==0 || iNewPage)
                            line += fixedTimes.get(time) + " ";
                        else
                            line += rpad("",16);
                    }
                    for (Enumeration f=lines.elements();f.hasMoreElements();) {
                        Vector linesThisPeriod = (Vector)f.nextElement();
                        if (i<linesThisPeriod.size())
                            line += (String)linesThisPeriod.elementAt(i);
                        else
                            line += rpad("",20);
                        if (f.hasMoreElements()) line += "  ";
                    }
                    println(line);
                }
                if (!iTotals && !iNewPage) {
                    if (e.hasMoreElements())
                        println("                --------------- ----  --------------- ----  --------------- ----  --------------- ----  --------------- ----");
                    else
                        println("--------------- --------------- ----  --------------- ----  --------------- ----  --------------- ----  --------------- ----");
                }
                setCont(null);
            }
            if (iTotals) {
                if (getLineNumber()+5>sNrLines) {
                    newPage();
                    setPageName("Totals");
                } else 
                    println("");
                println("Total Student Exams");
                String line1 = "", line2 = "", line3 = "";
                int idx = 0;
                for (Enumeration f=ToolBox.sortEnumeration(days.keys());f.hasMoreElements();idx++) {
                    Integer day = (Integer)f.nextElement();
                    if (idx<dIdx || idx>=dIdx+nrCols) continue;
                    line1 += mpad((String)days.get(day),20)+"  ";
                    line2 += "=============== ====  ";
                    line3 += lpad(totalADay.get(day)==null?"":totalADay.get(day).toString(),20)+"  ";
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
