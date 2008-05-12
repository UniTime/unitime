package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.reports.exam.InstructorExamReport.FileGenerator;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class StudentExamReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(InstructorExamReport.class);
    Hashtable<Long,Student> iStudents = new Hashtable();
    
    public StudentExamReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "STUDENT EXAMINATION SCHEDULE", session, examType, subjectArea, exams);
        for (Iterator i=new StudentDAO().getSession().createQuery("select s from Student s where s.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).iterate();i.hasNext();) {
            Student s = (Student)i.next();
            iStudents.put(s.getUniqueId(), s);
        }
    }
    
    public void printReport() throws DocumentException {
        Hashtable<Student,TreeSet<ExamSectionInfo>> sections = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            for (ExamSectionInfo section:exam.getSections()) {
                for (Long studentId : section.getStudentIds()) {
                    Student student = iStudents.get(studentId);
                    TreeSet<ExamSectionInfo> sectionsThisStudent = sections.get(student);
                    if (sectionsThisStudent==null) {
                        sectionsThisStudent = new TreeSet<ExamSectionInfo>();
                        sections.put(student, sectionsThisStudent);
                    }
                    sectionsThisStudent.add(section);
                }
            }
        }
        boolean firstStudent = true;
        printHeader();
        for (Student student : new TreeSet<Student>(sections.keySet())) {
            if (iSince!=null) {
                ChangeLog last = getLastChange(sections.get(student));
                if (last==null || iSince.compareTo(last.getTimeStamp())>0) {
                    sLog.debug("No change found for "+student.getName(DepartmentalInstructor.sNameFormatLastFist));
                    continue;
                }
            }
            if (!firstStudent) newPage();
            printReport(student, sections.get(student));
            firstStudent = false;
        }
        lastPage();
    }
    
    public void printReport(Long studentId) throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        for (ExamAssignmentInfo exam : getExams())
            for (ExamSectionInfo section : exam.getSections())
                if (section.getStudentIds().contains(studentId)) sections.add(section);
        if (sections.isEmpty()) return;
        Student student = new StudentDAO().get(studentId);
        printHeader();
        printReport(student, sections);
        lastPage();
    }

    public void printReport(Student student, TreeSet<ExamSectionInfo> sections) throws DocumentException {
        String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
        String shortName = student.getName(DepartmentalInstructor.sNameFormatLastInitial).toUpperCase();
        setPageName(shortName);
        setCont(shortName);
        println("Name:  "+name);
        if (student.getEmail()!=null)
            println("Email:       "+student.getEmail());
        Date lastChange = null;
        String changeObject = null;
        if (iClassSchedule) {
            TreeSet<Class_> allClasses = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
            for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                allClasses.add(sce.getClazz());
            }
            if (!allClasses.isEmpty()) {
                println("");
                setHeader(new String[]{
                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect  Dates                     Time            Room        Instructor",
                        "---- ------ "+(iItype?"------ ":"")+"---- ------------------------- --------------- ----------- -------------------------"});
                println(mpad("~ ~ ~ ~ ~ CLASS SECHEDULE ~ ~ ~ ~ ~",iNrChars));
                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                for (Class_ clazz : allClasses) {
                    String instructor = "";
                    if (clazz.isDisplayInstructor()) {
                        for (Iterator i=new TreeSet(clazz.getClassInstructors()).iterator();i.hasNext();) {
                            ClassInstructor ci = (ClassInstructor)i.next();
                            if (instructor.length()>0) instructor+=", ";
                            instructor += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatLastInitial);
                        }
                    }
                    String subject = clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(); 
                    String course = clazz.getSchedulingSubpart().getControllingCourseOffering().getCourseNbr();
                    String itype =  getItype(clazz);
                    String section = (iUseClassSuffix && clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString());
                    ClassEvent event = clazz.getEvent();
                    if (event==null || event.getMeetings().isEmpty()) {
                        println(
                                rpad(subject,4)+" "+
                                rpad(course,6)+" "+
                                (iItype?rpad(itype,6)+" ":"")+
                                lpad(section,4)+" "+
                                rpad("ARRANGED HOURS",54)+
                                rpad(instructor,55)
                                );
                    } else {
                        MultiMeeting last = null;
                        String lastTime = null, lastDate = null;
                        String lastLoc = null;
                        for (MultiMeeting meeting : event.getMultiMeetings()) {
                            String line;
                            if (last==null) {
                                line = rpad(subject,4)+" "+
                                rpad(course,6)+" "+
                                (iItype?rpad(itype,6)+" ":"")+
                                lpad(section,4)+" ";
                            } else {
                                line = rpad("",17+(iItype?7:0));
                            }
                            String date = getMeetingDate(meeting);
                            String time = getMeetingTime(meeting.getMeetings().first());
                            if (last==null || !time.equals(lastTime) || !date.equals(lastDate)) {
                                line += rpad(date.equals(lastDate)?"":date,25)+" "+
                                        rpad(time.equals(lastTime)?"":time,15)+" ";
                            } else {
                                line += rpad("",39);
                            }
                            Location location = meeting.getMeetings().first().getLocation();
                            String loc = (location==null?"":formatRoom(location.getLabel()));
                            if (last==null || !loc.equals(lastLoc)) {
                                line += loc + " ";
                            } else {
                                line += rpad("",12);
                            }
                            if (last==null)
                                line += rpad(instructor,55);
                            lastLoc = loc;
                            lastTime = time; lastDate = date;
                            last = meeting;
                            println(line);
                            if (iNewPage) { last=null; lastTime = null; lastDate = null; lastLoc = null; }
                        }
                    }
                }
            }
        }
        println("");
        setHeader(new String[]{
                "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect   Meeting Times                          Date And Time                   Room      ",
                "---- ------ "+(iItype?"------ ":"")+"---- -------------------------------------- -------------------------------- -----------"});
        println(mpad("~ ~ ~ ~ ~ EXAMINATION SECHEDULE ~ ~ ~ ~ ~",iNrChars));
        for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
        iSubjectPrinted = false; String lastSubject = null;
        iCoursePrinted = false; String lastCourse = null;
        iITypePrinted = false; String lastItype = null;
        iPeriodPrinted = false; String lastSection = null;
        for (ExamSectionInfo section : sections) {
            if (iSubjectPrinted && !section.getSubject().equals(lastSubject)) { iSubjectPrinted = false; iCoursePrinted = false; iITypePrinted = false; iPeriodPrinted = false; }
            if (iCoursePrinted && !section.getCourseNbr().equals(lastCourse)) { iCoursePrinted = false; iITypePrinted = false; iPeriodPrinted = false; }
            if (iITypePrinted && !section.getItype().equals(lastItype)) { iITypePrinted = false; iPeriodPrinted = false; }
            if (iPeriodPrinted && !section.getSection().equals(lastSection)) { iPeriodPrinted = false; }
            if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                println(
                        rpad(iSubjectPrinted?"":section.getSubject(), 4)+" "+
                        rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                        (iItype?rpad(iITypePrinted?"":section.getItype(), 6)+" ":"")+
                        lpad(iPeriodPrinted?"":section.getSection(), 4)+" "+
                        rpad(getMeetingTime(section),38)+" "+
                        rpad((section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                        (section.getExamAssignment()==null?"":iNoRoom)
                        );
            } else {
                if (getLineNumber()+section.getExamAssignment().getRooms().size()>iNrLines) newPage();
                boolean firstRoom = true;
                for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                    println(
                            rpad(!firstRoom || iSubjectPrinted?"":section.getSubject(), 4)+" "+
                            rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                            (iItype?rpad(!firstRoom || iITypePrinted?"":section.getItype(), 6)+" ":"")+
                            lpad(!firstRoom || iPeriodPrinted?"":section.getSection(), 4)+" "+
                            rpad(!firstRoom?"":getMeetingTime(section),38)+" "+
                            rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                            formatRoom(room.getName()));
                    firstRoom = false;
                }
            }
            if (iNewPage) {
                iSubjectPrinted = iITypePrinted = iCoursePrinted = iPeriodPrinted = false;
                lastSubject = lastItype = lastCourse = lastSection = null;
            } else {
                iSubjectPrinted = iITypePrinted = iCoursePrinted = iPeriodPrinted = true;
                lastSubject = section.getSubject();
                lastItype = section.getItype();
                lastCourse = section.getCourseNbr();
                lastSection = section.getSection();
            }
        }
        
        boolean headerPrinted = false;
        lastSubject = null;
        for (ExamSectionInfo section : sections) {
            iSubjectPrinted = (!iNewPage && lastSubject!=null && lastSubject.equals(section.getSubject()));
            ExamAssignmentInfo exam = section.getExamAssignmentInfo();
            if (exam==null || exam.getPeriod()==null) continue;
            ExamPeriod period = exam.getPeriod();
            iCoursePrinted = false;
                if (iDirect) for (DirectConflict conflict : exam.getDirectConflicts()) {
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    iPeriodPrinted = false;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                            if (!other.getStudentIds().contains(student.getUniqueId())) continue;
                            if (!headerPrinted) {
                                if (!iNewPage) println("");
                                setHeader(null);
                                if (getLineNumber()+5>=iNrLines) newPage();
                                setHeader(new String[] {
                                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                        "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                                println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                                setCont(shortName+"  STUDENT CONFLICTS");
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        if (!headerPrinted) {
                            if (!iNewPage) println("");
                            setHeader(null);
                            if (getLineNumber()+5>=iNrLines) newPage();
                            setHeader(new String[] {
                                    "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                    "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                            println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                            for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                            setCont(shortName+"  STUDENT CONFLICTS");
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                rpad(iPeriodPrinted?"":"CLASS",6)+" "+
                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),4)+" "+
                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),6)+" "+
                                (iItype?rpad(conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6)+" ":"")+
                                lpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(),4)+" "+
                                getMeetingTime(conflict.getOtherEventTime())
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
                }
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        for (ExamSectionInfo other : otherExam.getSections()) {
                            if (!other.getStudentIds().contains(student.getUniqueId())) continue;
                            if (!headerPrinted) {
                                if (!iNewPage) println("");
                                setHeader(null);
                                if (getLineNumber()+5>=iNrLines) newPage();
                                setHeader(new String[] {
                                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                        "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                                println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                                setCont(shortName+"  STUDENT CONFLICTS");
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iPeriodPrinted?"":">2-DAY",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    }
                }
                if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                        if (!other.getStudentIds().contains(student.getUniqueId())) continue;
                        if (!headerPrinted) {
                            if (!iNewPage) println("");
                            setHeader(null);
                            if (getLineNumber()+5>=iNrLines) newPage();
                            setHeader(new String[] {
                                    "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                    "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                            println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                            for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                            setCont(shortName+"  STUDENT CONFLICTS");
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                rpad(iPeriodPrinted?"":"BTB",6)+" "+
                                rpad(other.getSubject(),4)+" "+
                                rpad(other.getCourseNbr(),6)+" "+
                                (iItype?rpad(other.getItype(),6)+" ":"")+
                                lpad(other.getSection(),4)+" "+
                                other.getExamAssignment().getTimeFixedLength()
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
            }
        }
        
        setHeader(null);
        setCont(null);
    }
    
    public ChangeLog getLastChange(TreeSet<ExamSectionInfo> sections) {
        ChangeLog lastChange = null;
        for (ExamSectionInfo section : sections) {
            ChangeLog c = ChangeLog.findLastChange(section.getExam().getExam());
            if (c!=null && (lastChange==null || lastChange.getTimeStamp().compareTo(c.getTimeStamp())<0)) {
                lastChange = c;
            }
        }
        return lastChange;
    }
    
    public Hashtable<Student,File> printStudentReports(int mode, String filePrefix, FileGenerator gen, StudentFilter filter) throws DocumentException, IOException {
        Hashtable<Student,File> files = new Hashtable();
        Hashtable<Student,TreeSet<ExamSectionInfo>> sections = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            for (ExamSectionInfo section:exam.getSections()) {
                for (Long studentId : section.getStudentIds()) {
                    Student student = iStudents.get(studentId);
                    TreeSet<ExamSectionInfo> sectionsThisStudent = sections.get(student);
                    if (sectionsThisStudent==null) {
                        sectionsThisStudent = new TreeSet<ExamSectionInfo>();
                        sections.put(student, sectionsThisStudent);
                    }
                    sectionsThisStudent.add(section);
                }
            }
        }
        for (Student student : new TreeSet<Student>(sections.keySet())) {
            if (!filter.generate(student, sections.get(student))) continue;
            if (iSince!=null) {
                ChangeLog last = getLastChange(sections.get(student));
                if (last==null || iSince.compareTo(last.getTimeStamp())>0) {
                    sLog.debug("No change found for "+student.getName(DepartmentalInstructor.sNameFormatLastFist));
                    continue;
                }
            }
            sLog.debug("Generating file for "+student.getName(DepartmentalInstructor.sNameFormatLastFist));
            File file = gen.generate(filePrefix+"_"+
                    (student.getExternalUniqueId()!=null?student.getExternalUniqueId():student.getLastName()),
                    (mode==sModeText?"txt":"pdf")); 
                //ApplicationProperties.getTempFile(filePrefix+"_"+(instructor.getExternalUniqueId()!=null?instructor.getExternalUniqueId():instructor.getInstructor().getLastName()), (mode==sModeText?"txt":"pdf"));
            open(file, mode);
            printHeader();
            printReport(student, sections.get(student));
            lastPage();
            close();
            files.put(student,file);
        }
        return files;
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
            int examType = (ApplicationProperties.getProperty("type","final").equalsIgnoreCase("final")?Exam.sExamTypeFinal:Exam.sExamTypeMidterm);
            int mode = sModeNormal;
            if ("text".equals(System.getProperty("mode"))) mode = sModeText;
            if ("ledger".equals(System.getProperty("mode"))) mode = sModeLedger;
            sLog.info("Exam type: "+Exam.sExamTypes[examType]);
            sLog.info("Loading exams...");
            Vector<ExamAssignmentInfo> exams = new Vector<ExamAssignmentInfo>();
            Hashtable<SubjectArea,Vector<ExamAssignmentInfo>> examsPerSubj = new Hashtable();
            for (Iterator i=Exam.findAll(session.getUniqueId(),examType).iterator();i.hasNext();) {
                ExamAssignmentInfo exam = new ExamAssignmentInfo((Exam)i.next());
                exams.add(exam);
            }
            Date since = null;
            if (System.getProperty("since")!=null) {
                since = new SimpleDateFormat(System.getProperty("sinceFormat","MM/dd/yy")).parse(System.getProperty("since"));
                sLog.info("Since: "+since);
            }
            StudentExamReport report = new StudentExamReport(mode, null, session, examType, null, exams);
            report.printStudentReports(mode, session.getAcademicTerm()+session.getYear()+(examType==Exam.sExamTypeMidterm?"evn":"fin"), new FileGenerator() {
                public File generate(String prefix, String ext) {
                    int idx = 0;
                    File file = new File(prefix+"."+ext);
                    while (file.exists()) {
                        idx++;
                        file = new File(prefix+"_"+idx+"."+ext);
                    }
                    return file;
                }
            }, new StudentFilter() {
                public boolean generate(Student student, TreeSet<ExamSectionInfo> sections) { return true; }
            });
            sLog.info("Done.");
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
        }
    }
    
    public static interface StudentFilter {
        public boolean generate(Student student, TreeSet<ExamSectionInfo> sections);
    }

}
