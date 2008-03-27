package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.reports.PdfLegacyReport;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;

import com.lowagie.text.DocumentException;

public abstract class PdfLegacyExamReport extends PdfLegacyReport {
    protected static Logger sLog = Logger.getLogger(PdfLegacyExamReport.class);
    
    public static Hashtable<String,Class> sRegisteredReports = new Hashtable();
    public static String sAllRegisteredReports = "";
    private Collection<ExamAssignmentInfo> iExams = null;
    private Session iSession = null;
    private int iExamType = -1;
    
    static {
        sRegisteredReports.put("crsn", ScheduleByCourseReport.class);
        sRegisteredReports.put("conf", ConflictsByCourseAndStudentReport.class);
        sRegisteredReports.put("iconf", ConflictsByCourseAndInstructorReport.class);
        sRegisteredReports.put("pern", ScheduleByPeriodReport.class);
        sRegisteredReports.put("xpern", ExamScheduleByPeriodReport.class);
        sRegisteredReports.put("room", ScheduleByRoomReport.class);
        sRegisteredReports.put("chart", PeriodChartReport.class);
        for (String report : sRegisteredReports.keySet())
            sAllRegisteredReports += (sAllRegisteredReports.length()>0?",":"") + report;
    }
    
    public PdfLegacyExamReport(File file, String title, Session session, int examType, Collection<ExamAssignmentInfo> exams) throws DocumentException, IOException {
        super(file, title, (examType==Exam.sExamTypeFinal?"FINAL":"EVENING")+" EXAMINATIONS", title + " -- " + session.getLabel(), session.getLabel());
        iExams = exams;
        iSession = session;
        iExamType = examType;
    }
    
    public Collection<ExamAssignmentInfo> getExams() {
        return iExams;
    }
    
    public Session getSession() {
        return iSession; 
    }
    
    public int getExamType() {
        return iExamType;
    }
    
    public abstract void printReport() throws DocumentException; 
    
    protected boolean iSubjectPrinted = false;
    protected boolean iITypePrinted = false;
    protected boolean iCoursePrinted = false;
    protected boolean iStudentPrinted = false;
    protected boolean iPeriodPrinted = false;
    protected boolean iNewPage = false;
    
    protected void headerPrinted() {
        iSubjectPrinted = false;
        iCoursePrinted = false;
        iStudentPrinted = false;
        iPeriodPrinted = false;
        iITypePrinted = false;
        iNewPage = true;
    }
    
    protected void println(String text) throws DocumentException {
        iNewPage = false;
        super.println(text);
    }
    
    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "puWestLafayetteTrdtn"),
                    ApplicationProperties.getProperty("year","2008"),
                    ApplicationProperties.getProperty("term","Spr")
                    );
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            int examType = (ApplicationProperties.getProperty("type","final").equalsIgnoreCase("final")?Exam.sExamTypeFinal:Exam.sExamTypeEvening);
            sLog.info("Exam type: "+Exam.sExamTypes[examType]);
            sLog.info("Loading exams...");
            Vector<ExamAssignmentInfo> exams = new Vector<ExamAssignmentInfo>();
            for (Iterator i=Exam.findAll(session.getUniqueId(),examType).iterator();i.hasNext();)
                exams.add(new ExamAssignmentInfo((Exam)i.next()));
            for (StringTokenizer stk=new StringTokenizer(ApplicationProperties.getProperty("report",sAllRegisteredReports),",");stk.hasMoreTokens();) {
                String reportName = stk.nextToken();
                Class reportClass = sRegisteredReports.get(reportName);
                if (reportClass==null) continue;
                sLog.info("Report: "+reportClass.getName().substring(reportClass.getName().lastIndexOf('.')+1));
                File file = new File(new File(ApplicationProperties.getProperty("output",".")),
                        session.getAcademicTerm()+session.getYear()+(examType==Exam.sExamTypeEvening?"evn":"fin")+"_"+reportName+".pdf");
                sLog.info("Generating report "+file+" ...");
                PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.getConstructor(new Class[] {File.class, Session.class, int.class, Collection.class}).newInstance(file, session, examType, exams);
                report.printReport();
                report.close();
            }
            sLog.info("Done.");
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
        }
    }

}
