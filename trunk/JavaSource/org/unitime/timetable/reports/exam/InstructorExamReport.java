package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

public class InstructorExamReport extends PdfLegacyExamReport {
    protected static Logger sLog = Logger.getLogger(InstructorExamReport.class);
    Hashtable<Long,String> iStudentNames = new Hashtable();
    
    public InstructorExamReport(int mode, File file, Session session, int examType, SubjectArea subjectArea, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, "INSTRUCTOR EXAMINATION SCHEDULE", session, examType, subjectArea, exams);
        sLog.debug("  Loading students ...");
        for (Iterator i=new StudentDAO().getSession().createQuery("select s.uniqueId, s.externalUniqueId, s.lastName, s.firstName, s.middleName from Student s where s.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).iterate();i.hasNext();) {
            Object[] o = (Object[])i.next();
            iStudentNames.put((Long)o[0], (String)o[2]+(o[3]==null?"":" "+((String)o[3]).substring(0,1))+(o[4]==null?"":" "+((String)o[4]).substring(0,1)));
        }
    }
    
    public void printReport() throws DocumentException {
        Hashtable<ExamInstructorInfo,TreeSet<ExamAssignmentInfo>> exams = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            for (ExamInstructorInfo instructor:exam.getInstructors()) {
                TreeSet<ExamAssignmentInfo> examsThisInstructor = exams.get(instructor);
                if (examsThisInstructor==null) {
                    examsThisInstructor = new TreeSet<ExamAssignmentInfo>();
                    exams.put(instructor, examsThisInstructor);
                }
                examsThisInstructor.add(exam);
            }
        }
        boolean firstInstructor = true;
        printHeader();
        for (ExamInstructorInfo instructor : new TreeSet<ExamInstructorInfo>(exams.keySet())) {
            if (!firstInstructor) newPage();
            printReport(instructor, exams.get(instructor));
            firstInstructor = false;
        }
        lastPage();
    }
    
    public Hashtable<ExamInstructorInfo,File> printInstructorReports(int mode, String filePrefix, Date since, FileGenerator gen) throws DocumentException, IOException {
        Hashtable<ExamInstructorInfo,File> files = new Hashtable();
        Hashtable<ExamInstructorInfo,TreeSet<ExamAssignmentInfo>> exams = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            for (ExamInstructorInfo instructor:exam.getInstructors()) {
                TreeSet<ExamAssignmentInfo> examsThisInstructor = exams.get(instructor);
                if (examsThisInstructor==null) {
                    examsThisInstructor = new TreeSet<ExamAssignmentInfo>();
                    exams.put(instructor, examsThisInstructor);
                }
                examsThisInstructor.add(exam);
            }
        }
        for (ExamInstructorInfo instructor : new TreeSet<ExamInstructorInfo>(exams.keySet())) {
            if (since!=null) {
                ChangeLog last = getLastChange(instructor, exams.get(instructor));
                if (last==null || since.compareTo(last.getTimeStamp())>0) {
                    sLog.debug("No change found for "+instructor.getName());
                    continue;
                }
            }
            sLog.debug("Generating file for "+instructor.getName());
            File file = gen.generate(filePrefix+"_"+
                    (instructor.getExternalUniqueId()!=null?instructor.getExternalUniqueId():instructor.getInstructor().getLastName()),
                    (mode==sModeText?"txt":"pdf")); 
                //ApplicationProperties.getTempFile(filePrefix+"_"+(instructor.getExternalUniqueId()!=null?instructor.getExternalUniqueId():instructor.getInstructor().getLastName()), (mode==sModeText?"txt":"pdf"));
            open(file, mode);
            printHeader();
            printReport(instructor, exams.get(instructor));
            lastPage();
            close();
            files.put(instructor,file);
        }
        return files;
    }
    
    public void printReport(ExamInstructorInfo instructor) throws DocumentException {
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam:getExams()) {
            if (exam.getInstructors().contains(instructor));
        }
        if (exams.isEmpty()) return;
        printHeader();
        printReport(instructor, exams);
        lastPage();
    }
    
    public ChangeLog getLastChange(ExamInstructorInfo instructor, TreeSet<ExamAssignmentInfo> exams) {
        ChangeLog lastChange = ChangeLog.findLastChange(instructor.getInstructor());
        for (ExamAssignmentInfo exam : exams) {
            ChangeLog c = ChangeLog.findLastChange(exam.getExam());
            if (c!=null && (lastChange==null || lastChange.getTimeStamp().compareTo(c.getTimeStamp())<0)) {
                lastChange = c;
            }
        }
        return lastChange;
    }
    
    public void printReport(ExamInstructorInfo instructor, TreeSet<ExamAssignmentInfo> exams) throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>();
        for (ExamAssignmentInfo exam : exams) sections.addAll(exam.getSections());
        setFooter(instructor.getName());//+" ("+instructor.getInstructor().getExternalUniqueId()+")");
        setCont(instructor.getName());
        println("Instructor:  "+instructor.getName());
        if (instructor.getInstructor().getEmail()!=null)
            println("Email:       "+instructor.getInstructor().getEmail());
        Date lastChange = null;
        String changeObject = null;
        ChangeLog c = ChangeLog.findLastChange(instructor.getInstructor());
        if (c!=null && (lastChange==null || lastChange.compareTo(c.getTimeStamp())<0)) {
            lastChange = c.getTimeStamp();
        }
        for (ExamAssignmentInfo exam : exams) {
            c = ChangeLog.findLastChange(exam.getExam());
            if (c!=null && (lastChange==null || lastChange.compareTo(c.getTimeStamp())<0)) {
                lastChange = c.getTimeStamp();
                changeObject = c.getObjectTitle().replaceAll("&rarr;", "->");
            }
        }
        if (lastChange!=null)
            println("Last Change: "+new SimpleDateFormat("EEE, MM/dd/yyyy hh:mmaa").format(lastChange)+(changeObject==null?"":" "+changeObject));
        println("");
        setHeader(new String[]{
                "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect   Meeting Times                         Enrl    Date And Time                   Room         Cap ExCap",
                "---- ------ "+(iItype?"------ ":"")+"---- -------------------------------------- -----  -------------------------------- ----------- ----- -----"});
        println(mpad("~ ~ ~ ~ ~ EXAMINATION SECHEDULE ~ ~ ~ ~ ~",iNrChars));
        for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
        iSubjectPrinted = false; String lastSubject = null;
        iCoursePrinted = false; String lastCourse = null;
        iITypePrinted = false; String lastItype = null;
        iPeriodPrinted = false; String lastSection = null;
        for (ExamAssignmentInfo exam : exams) {
            for (Iterator<ExamSectionInfo> j = exam.getSections().iterator(); j.hasNext();) {
                ExamSectionInfo  section = j.next();
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
                            lpad(String.valueOf(section.getNrStudents()),5)+"  "+
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
                                lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5)+"  "+
                                rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),32)+" "+
                                formatRoom(room.getName())+" "+
                                lpad(""+room.getCapacity(),5)+" "+
                                lpad(""+room.getExamCapacity(),5)
                                );
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
            println("");
        }
        
        boolean headerPrinted = false;
        lastSubject = null;
        for (ExamSectionInfo section : sections) {
            iSubjectPrinted = (!iNewPage && lastSubject!=null && lastSubject.equals(section.getSubject()));
            lastSubject = section.getSubject();
            ExamAssignmentInfo exam = section.getExamAssignmentInfo();
            if (exam==null || exam.getPeriod()==null) continue;
            ExamPeriod period = exam.getPeriod();
            iCoursePrinted = false;
                if (iDirect) for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                    if (!conflict.getStudents().contains(instructor.getId())) continue;
                    iPeriodPrinted = false;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                            if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                            if (!headerPrinted) {
                                if (!iNewPage) println("");
                                setHeader(null);
                                if (getLineNumber()+5>=iNrLines) newPage();
                                setHeader(new String[] {
                                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                        "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                                println(mpad("~ ~ ~ ~ ~ INSTRUCTOR CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                                setCont(instructor.getName()+"  INSTRUCTOR CONFLICTS");
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
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        if (!headerPrinted) {
                            if (!iNewPage) println("");
                            setHeader(null);
                            if (getLineNumber()+5>=iNrLines) newPage();
                            setHeader(new String[] {
                                    "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                    "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                            println(mpad("~ ~ ~ ~ ~ INSTRUCTOR CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                            for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                            setCont(instructor.getName()+"  INSTRUCTOR CONFLICTS");
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
                    }
                }
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(instructor.getId())) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        if (!otherExam.getInstructors().contains(instructor)) continue;
                        for (ExamSectionInfo other : otherExam.getSections()) {
                            if (!headerPrinted) {
                                if (!iNewPage) println("");
                                setHeader(null);
                                if (getLineNumber()+5>=iNrLines) newPage();
                                setHeader(new String[] {
                                        "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                        "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                                println(mpad("~ ~ ~ ~ ~ INSTRUCTOR CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                                for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                                setCont(instructor.getName()+"  INSTRUCTOR CONFLICTS");
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
                        }
                    }
                }
                if (iBtb) for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(instructor.getId())) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                        if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                        if (!headerPrinted) {
                            if (!iNewPage) println("");
                            setHeader(null);
                            if (getLineNumber()+5>=iNrLines) newPage();
                            setHeader(new String[] {
                                    "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                                    "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
                            println(mpad("~ ~ ~ ~ ~ INSTRUCTOR CONFLICTS ~ ~ ~ ~ ~",iNrChars));
                            for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
                            setCont(instructor.getName()+"  INSTRUCTOR CONFLICTS");
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
                    }
            }
        }
        if (headerPrinted && !iNewPage) println("");
        
        if (!iNewPage) println("");
        setHeader(null);
        if (getLineNumber()+5>=iNrLines) newPage();
        setHeader(new String[] {
                "Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Date And Time                Name                      Type   Subj Crsnbr "+(iItype?"InsTyp ":"")+"Sect Time                 ",
                "---- ------ "+(iItype?"------ ":"")+"---- ---------------------------- ------------------------- ------ ---- ------ "+(iItype?"------ ":"")+"---- ---------------------"});
        println(mpad("~ ~ ~ ~ ~ STUDENT CONFLICTS ~ ~ ~ ~ ~",iNrChars));
        for (int i=0;i<getHeader().length;i++) println(getHeader()[i]);
        setCont(instructor.getName()+"  STUDENT CONFLICTS");

        lastSubject = null;
        for (ExamSectionInfo section : sections) {
            iSubjectPrinted = (!iNewPage && lastSubject!=null && lastSubject.equals(section.getSubject()));
            lastSubject = section.getSubject();
            ExamAssignmentInfo exam = section.getExamAssignmentInfo();
            if (exam==null || exam.getPeriod()==null) continue;
            ExamPeriod period = exam.getPeriod();
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
                        for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
                                    rpad(iPeriodPrinted?"":"DIRECT",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
                                rpad(iPeriodPrinted?"":"CLASS",6)+" "+
                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),4)+" "+
                                rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),6)+" "+
                                (iItype?rpad(conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6)+" ":"")+
                                lpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(),4)+" "+
                                getMeetingTime(conflict.getOtherEventTime())
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                    }
                }
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        for (ExamSectionInfo other : otherExam.getSections()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                    lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                    rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
                                    rpad(iPeriodPrinted?"":">2-DAY",6)+" "+
                                    rpad(other.getSubject(),4)+" "+
                                    rpad(other.getCourseNbr(),6)+" "+
                                    (iItype?rpad(other.getItype(),6)+" ":"")+
                                    lpad(other.getSection(),4)+" "+
                                    other.getExamAssignment().getTimeFixedLength()
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        }
                    }
                }
                if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSections()) {
                        if (!other.getStudentIds().contains(studentId)) continue;
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),4)+" "+
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 6)+" "+
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6)+" ":"")+
                                lpad(iCoursePrinted?"":section.getSection(),4)+" "+
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),28)+" "+
                                rpad(iStudentPrinted?"":iStudentNames.get(studentId),25)+" "+
                                rpad(iPeriodPrinted?"":"BTB",6)+" "+
                                rpad(other.getSubject(),4)+" "+
                                rpad(other.getCourseNbr(),6)+" "+
                                (iItype?rpad(other.getItype(),6)+" ":"")+
                                lpad(other.getSection(),4)+" "+
                                other.getExamAssignment().getTimeFixedLength()
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                    }
                }                    
            }
        }
        setHeader(null);
        setCont(null);
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
            InstructorExamReport report = new InstructorExamReport(mode, null, session, examType, null, exams);
            report.printInstructorReports(mode, session.getAcademicTerm()+session.getYear()+(examType==Exam.sExamTypeEvening?"evn":"fin"), since, new FileGenerator() {
                public File generate(String prefix, String ext) {
                    int idx = 0;
                    File file = new File(prefix+"."+ext);
                    while (file.exists()) {
                        idx++;
                        file = new File(prefix+"_"+idx+"."+ext);
                    }
                    return file;
                }
            });
            sLog.info("Done.");
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
        }
    }
    
    public static interface FileGenerator {
        public File generate(String prefix, String ext);
    }
}
