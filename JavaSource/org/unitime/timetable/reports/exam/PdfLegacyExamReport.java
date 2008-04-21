package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.reports.PdfLegacyReport;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Constants;

import com.lowagie.text.DocumentException;

public abstract class PdfLegacyExamReport extends PdfLegacyReport {
    protected static Logger sLog = Logger.getLogger(PdfLegacyExamReport.class);
    
    public static Hashtable<String,Class> sRegisteredReports = new Hashtable();
    public static String sAllRegisteredReports = "";
    private Collection<ExamAssignmentInfo> iExams = null;
    private Session iSession = null;
    private SubjectArea iSubjectArea = null;
    private int iExamType = -1;
    
    protected boolean iDispRooms = true;
    protected String iNoRoom = "";
    protected boolean iDirect = true;
    protected boolean iM2d = true;
    protected boolean iBtb = false;
    protected int iLimit = -1;
    protected boolean iItype = false;
    
    static {
        sRegisteredReports.put("crsn", ScheduleByCourseReport.class);
        sRegisteredReports.put("conf", ConflictsByCourseAndStudentReport.class);
        sRegisteredReports.put("iconf", ConflictsByCourseAndInstructorReport.class);
        sRegisteredReports.put("pern", ScheduleByPeriodReport.class);
        sRegisteredReports.put("xpern", ExamScheduleByPeriodReport.class);
        sRegisteredReports.put("room", ScheduleByRoomReport.class);
        sRegisteredReports.put("chart", PeriodChartReport.class);
        sRegisteredReports.put("ver", ExamVerificationReport.class);
        for (String report : sRegisteredReports.keySet())
            sAllRegisteredReports += (sAllRegisteredReports.length()>0?",":"") + report;
    }
    
    public PdfLegacyExamReport(File file, String title, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws DocumentException, IOException {
        super(file, title, (examType==Exam.sExamTypeFinal?"FINAL":"EVENING")+" EXAMINATIONS", title + " -- " + session.getLabel(), session.getLabel());
        if (subjectArea!=null) setFooter(subjectArea.getSubjectAreaAbbreviation());
        iExams = exams;
        iSession = session;
        iExamType = examType;
        iSubjectArea = subjectArea;
        iDispRooms = "true".equals(System.getProperty("room","true"));
        iNoRoom = System.getProperty("noroom","INSTR OFFC");
        iDirect = "true".equals(System.getProperty("direct","true"));
        iM2d = "true".equals(System.getProperty("m2d",(examType==Exam.sExamTypeFinal?"true":"false")));
        iBtb = "true".equals(System.getProperty("btb","false"));
        iLimit = Integer.parseInt(System.getProperty("limit", "-1"));
        iItype = "true".equals(System.getProperty("itype","false"));
    }
    
    public void setDispRooms(boolean dispRooms) { iDispRooms = dispRooms; }
    public void setNoRoom(String noRoom) { iNoRoom = noRoom; }
    public void setDirect(boolean direct) { iDirect = direct; }
    public void setM2d(boolean m2d) { iM2d = m2d; }
    public void setBtb(boolean btb) { iBtb = btb; }
    public void setLimit(int limit) { iLimit = limit; }
    public void setItype(boolean itype) { iItype = itype; }

    public Collection<ExamAssignmentInfo> getExams() {
        return iExams;
    }
    
    public Session getSession() {
        return iSession; 
    }
    
    public int getExamType() {
        return iExamType;
    }
    
    public SubjectArea getSubjectArea() {
        return iSubjectArea;
    }
    
    public abstract void printReport() throws DocumentException; 
    
    protected boolean iSubjectPrinted = false;
    protected boolean iITypePrinted = false;
    protected boolean iConfigPrinted = false;
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
        iConfigPrinted = false;
        iNewPage = true;
    }
    
    protected void println(String text) throws DocumentException {
        iNewPage = false;
        super.println(text);
    }
    
    public int getDaysCode(Set meetings) {
        int daysCode = 0;
        for (Iterator i=meetings.iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            Calendar date = Calendar.getInstance(Locale.US);
            date.setTime(meeting.getMeetingDate());
            switch (date.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_MON]; break;
            case Calendar.TUESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_TUE]; break;
            case Calendar.WEDNESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_WED]; break;
            case Calendar.THURSDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_THU]; break;
            case Calendar.FRIDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_FRI]; break;
            case Calendar.SATURDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SAT]; break;
            case Calendar.SUNDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SUN]; break;
            }
        }
        return daysCode;
    }
    
    public static String DAY_NAMES_SHORT[] = new String[] {
        "M", "T", "W", "R", "F", "S", "U"
    }; 
    
    protected String getMeetingTime(ExamSectionInfo section) {
        String meetingTime = "";
        if (section.getOwner().getOwnerObject() instanceof Class_) {
            SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
            Class_ clazz = (Class_)section.getOwner().getOwnerObject();
            Assignment assignment = clazz.getCommittedAssignment();
            Event event = (assignment==null || assignment.getEvent()==null?Event.findClassEvent(clazz.getUniqueId()):assignment.getEvent());
            TreeSet meetings = (event==null?null:new TreeSet(event.getMeetings()));
            if (meetings!=null && !meetings.isEmpty()) {
                Date first = ((Meeting)meetings.first()).getMeetingDate();
                Date last = ((Meeting)meetings.last()).getMeetingDate();
                meetingTime += dpf.format(first)+" - "+dpf.format(last);
            } else if (assignment!=null && assignment.getDatePattern()!=null) {
                DatePattern dp = assignment.getDatePattern();
                if (dp!=null && !dp.isDefault()) {
                    if (dp.getType().intValue()==DatePattern.sTypeAlternate)
                        meetingTime += rpad(dp.getName(),13);
                    else {
                        meetingTime += dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate());
                    }
                }
            } else {
                meetingTime = rpad("",13);
            }
            if (meetings!=null && !meetings.isEmpty()) {
                int dayCode = getDaysCode(meetings);
                String days = "";
                for (int i=0;i<Constants.DAY_CODES.length;i++)
                    if ((dayCode & Constants.DAY_CODES[i])!=0) days += DAY_NAMES_SHORT[i];
                meetingTime += " "+rpad(days,5);
                Meeting first = (Meeting)meetings.first();
                meetingTime += " "+lpad(first.startTime(),6)+" - "+lpad(first.stopTime(),6);
            } else if (assignment!=null) {
                TimeLocation t = assignment.getTimeLocation();
                meetingTime += " "+rpad(t.getDayHeader(),5)+" "+lpad(t.getStartTimeHeader(),6)+" - "+lpad(t.getEndTimeHeader(),6);
            }
        }
        return meetingTime;
    }
    
    protected String getMeetingTime(Meeting meeting) {
        return lpad(meeting.startTime(),6)+" - "+lpad(meeting.stopTime(),6);
    }
    
    protected String getMeetingTime(String time) {
        int idx = time.indexOf('-');
        if (idx<0) return lpad(time,15);
        String start = time.substring(0,idx).trim();
        String stop = time.substring(idx+1).trim();
        return lpad(start,'0',6)+" - "+lpad(stop,'0',6);
    }

    public String formatRoom(String room) {
        String r = room.trim();
        int idx = r.lastIndexOf(' '); 
        if (idx>=0 && idx<=5 && r.length()-idx-1<=5)
            return rpad(r.substring(0, idx),5)+" "+rpad(room.substring(idx+1),5);
        return rpad(room,11);
    }
    
    public String formatPeriod(ExamPeriod period) {
        return period.getStartDateLabel()+" "+lpad(period.getStartTimeLabel(),6)+" - "+lpad(period.getEndTimeLabel(),6);
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
            boolean assgn = "true".equals(System.getProperty("assgn","true"));
            sLog.info("Exam type: "+Exam.sExamTypes[examType]);
            sLog.info("Loading exams...");
            boolean perSubject = "true".equals(System.getProperty("persubject","false"));
            HashSet<SubjectArea> subjects = null;
            if (System.getProperty("subject")!=null) {
                perSubject = true;
                subjects = new HashSet();
                String inSubjects = "";
                for (StringTokenizer s=new StringTokenizer(System.getProperty("subject"),",");s.hasMoreTokens();)
                    inSubjects += "'"+s.nextToken()+"'"+(s.hasMoreTokens()?",":"");
                subjects.addAll(new _RootDAO().getSession().createQuery(
                        "select sa from SubjectArea sa where sa.session.uniqueId=:sessionId and sa.subjectAreaAbbreviation in ("+inSubjects+")"
                        ).setLong("sessionId", session.getUniqueId()).list());
            }
            Vector<ExamAssignmentInfo> exams = new Vector<ExamAssignmentInfo>();
            Hashtable<SubjectArea,Vector<ExamAssignmentInfo>> examsPerSubj = new Hashtable();
            if (subjects==null) {
                for (Iterator i=Exam.findAll(session.getUniqueId(),examType).iterator();i.hasNext();) {
                    ExamAssignmentInfo exam = (assgn?new ExamAssignmentInfo((Exam)i.next()):new ExamAssignmentInfo((Exam)i.next(),null,null,null,null));
                    exams.add(exam);
                    if (perSubject) {
                        HashSet<SubjectArea> sas = new HashSet<SubjectArea>();
                        for (Iterator j=exam.getExam().getOwners().iterator();j.hasNext();) {
                            ExamOwner owner = (ExamOwner)j.next();
                            SubjectArea sa = owner.getCourse().getSubjectArea();
                            if (!sas.add(sa)) continue;
                            Vector<ExamAssignmentInfo> x = examsPerSubj.get(sa);
                            if (x==null) { x = new Vector(); examsPerSubj.put(sa,x); }
                            x.add(exam);
                        }
                    }
                }
            } else for (SubjectArea subject : subjects) {
                Vector<ExamAssignmentInfo> examsOfThisSubject = new Vector();
                for (Iterator i=Exam.findExamsOfSubjectArea(subject.getUniqueId(),examType).iterator();i.hasNext();) {
                    ExamAssignmentInfo exam = (assgn?new ExamAssignmentInfo((Exam)i.next()):new ExamAssignmentInfo((Exam)i.next(),null,null,null,null)); 
                    exams.add(exam);
                    examsOfThisSubject.add(exam);
                }
                examsPerSubj.put(subject, examsOfThisSubject);
            }
            for (StringTokenizer stk=new StringTokenizer(ApplicationProperties.getProperty("report",sAllRegisteredReports),",");stk.hasMoreTokens();) {
                String reportName = stk.nextToken();
                Class reportClass = sRegisteredReports.get(reportName);
                if (reportClass==null) continue;
                sLog.info("Report: "+reportClass.getName().substring(reportClass.getName().lastIndexOf('.')+1));
                if (perSubject) {
                    for (Map.Entry<SubjectArea,Vector<ExamAssignmentInfo>> entry : examsPerSubj.entrySet()) {
                        File file = new File(new File(ApplicationProperties.getProperty("output",".")),
                            session.getAcademicTerm()+session.getYear()+(examType==Exam.sExamTypeEvening?"evn":"fin")+"_"+reportName+"_"+entry.getKey().getSubjectAreaAbbreviation()+".pdf");
                        sLog.info("Generating report "+file+" ("+entry.getKey().getSubjectAreaAbbreviation()+") ...");
                        PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.getConstructor(File.class, Session.class, int.class, SubjectArea.class, Collection.class).newInstance(file, session, examType, entry.getKey(), entry.getValue());
                        report.printReport();
                        report.close();
                    }
                } else {
                    File file = new File(new File(ApplicationProperties.getProperty("output",".")),
                            session.getAcademicTerm()+session.getYear()+(examType==Exam.sExamTypeEvening?"evn":"fin")+"_"+reportName+".pdf");
                        sLog.info("Generating report "+file+" ...");
                        PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.getConstructor(File.class, Session.class, int.class, SubjectArea.class, Collection.class).newInstance(file, session, examType, null, exams);
                        report.printReport();
                        report.close();
                }
            }
            sLog.info("Done.");
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
        }
    }

}
