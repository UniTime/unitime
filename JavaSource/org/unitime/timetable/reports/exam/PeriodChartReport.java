package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.log4j.Logger;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class PeriodChartReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(ScheduleByCourseReport.class);
    
    public PeriodChartReport(File file, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(file, "PERIOD ASSIGNMENT", session, examType, exams);
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
        SimpleDateFormat ds = new SimpleDateFormat("MM/dd");
        SimpleDateFormat ts = new SimpleDateFormat("hh:mmaa");
        Hashtable times = new Hashtable();
        Hashtable days = new Hashtable();
        for (Iterator i=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            times.put(new Integer(period.getStartSlot()), ts.format(period.getStartTime()));
            days.put(new Integer(period.getDateOffset()), ds.format(period.getStartDate()));
        }
        boolean headerPrinted = false;
        int nrCols = 6;
        Hashtable totalADay = new Hashtable();
        for (int dIdx = 0; dIdx < days.size(); dIdx += nrCols) {
            for (Enumeration e=ToolBox.sortEnumeration(times.keys());e.hasMoreElements();) {
                int time = ((Integer)e.nextElement()).intValue();
                String timeStr = (String)times.get(new Integer(time));
                String header1 = "";
                String header2 = "";
                String header3 = "";
                Vector periods = new Vector();
                int idx = 0;
                for (Enumeration f=ToolBox.sortEnumeration(days.keys());f.hasMoreElements();idx++) {
                    int day = ((Integer)f.nextElement()).intValue();
                    if (idx<dIdx || idx>=dIdx+nrCols) continue;
                    String dayStr = (String)days.get(new Integer(day));
                    header1 += mpad(dayStr,17)+"   "; 
                    header2 += "Exam         Enrl   ";
                    header3 += "============ ====   ";
                    ExamPeriod period = null;
                    for (Iterator i=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
                        ExamPeriod p = (ExamPeriod)i.next();
                        if (time!=p.getStartSlot() || day!=p.getDateOffset()) continue;
                        period = p; break;
                    }
                    periods.add(period);
                }
                setHeader(new String[] {timeStr,header1,header2,header3});
                if (!headerPrinted)
                    printHeader();
                else
                    newPage();
                setPageName(timeStr);
                setCont(timeStr); 
                headerPrinted = true;
                int max = 0;
                Vector lines = new Vector();
                for (Enumeration f=periods.elements();f.hasMoreElements();) {
                    ExamPeriod period = (ExamPeriod)f.nextElement();
                    if (period==null) {
                        Vector linesThisPeriod = new Vector();
                        linesThisPeriod.add(lpad("0",17));
                        lines.add(linesThisPeriod);
                        continue;
                    }
                    TreeSet<ExamSectionInfo> sections = period2courseSections.get(period);
                    if (sections==null) sections = new TreeSet();
                    Vector linesThisPeriod = new Vector();
                    int total = 0;
                    for (ExamSectionInfo section : sections) {
                        total += section.getNrStudents();
                        linesThisPeriod.add(
                                rpad(section.getName(), 12)+" "+
                                lpad(String.valueOf(section.getNrStudents()),4));
                    }
                    linesThisPeriod.insertElementAt(lpad(""+total,17), 0);
                    max = Math.max(max, linesThisPeriod.size());
                    Integer td = (Integer)totalADay.get(period.getDateOffset());
                    totalADay.put(period.getDateOffset(),new Integer(total+(td==null?0:td.intValue())));
                    lines.add(linesThisPeriod);
                }
                for (int i=0;i<max;i++) {
                    String line = "";
                    for (Enumeration f=lines.elements();f.hasMoreElements();) {
                        Vector linesThisPeriod = (Vector)f.nextElement();
                        if (i<linesThisPeriod.size())
                            line += (String)linesThisPeriod.elementAt(i);
                        else
                            line += rpad("",17);
                        if (f.hasMoreElements()) line += "   ";
                    }
                    println(line);
                }
                setCont(null);
            }
            println("");
            if (getLineNumber()+4>sNrLines) {
                newPage();
                setPageName("Totals");
                println("");
            }
            println("Total Student Exams");
            String line1 = "", line2 = "", line3 = "";
            int idx = 0;
            for (Enumeration f=ToolBox.sortEnumeration(days.keys());f.hasMoreElements();idx++) {
                Integer day = (Integer)f.nextElement();
                if (idx<dIdx || idx>=dIdx+nrCols) continue;
                line1 += mpad((String)days.get(day),17)+"   ";
                line2 += "============ ====   ";
                line3 += lpad(totalADay.get(day)==null?"":totalADay.get(day).toString(),17)+"   ";
            }
            println(line1);
            println(line2);
            println(line3);
        }
        lastPage();
    }
}
