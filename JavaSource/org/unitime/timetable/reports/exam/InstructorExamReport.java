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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.SessionDAO;
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

/**
 * @author Tomas Muller
 */
public class InstructorExamReport extends PdfLegacyExamReport {
    protected static Log sLog = LogFactory.getLog(InstructorExamReport.class);
    Hashtable<Long,String> iStudentNames = null;
    Hashtable<Long,ClassEvent> iClass2event = null;
    Hashtable<Long,Location> iLocations = null;
    
    public InstructorExamReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legactReportInstructorExaminationSchedule(), session, examType, subjectAreas, exams);
    }
    
    public InstructorExamReport(int mode, OutputStream out, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, out, MSG.legactReportInstructorExaminationSchedule(), session, examType, subjectAreas, exams);
    }

    private void generateCache() {
        if (iStudentNames==null) {
            iStudentNames = new Hashtable();
            sLog.debug(MSG.statusLoadingStudents());
            for (Object[] o: StudentDAO.getInstance().getSession().createQuery(
            		"select s.uniqueId, s.externalUniqueId, s.lastName, s.firstName, s.middleName from Student s where s.session.uniqueId=:sessionId",
            		Object[].class)
            		.setParameter("sessionId", getSession().getUniqueId(), Long.class).setCacheable(true).list()) {
                iStudentNames.put((Long)o[0], (String)o[2]+(o[3]==null?"":" "+((String)o[3]).substring(0,1))+(o[4]==null?"":" "+((String)o[4]).substring(0,1)));
            }
        }
        if (iClass2event==null) {
            sLog.info(MSG.statusLoadingClassEvents());
            iClass2event = new Hashtable();
            if (hasSubjectAreas()) {
            	for (SubjectArea subject: getSubjectAreas()) {
                    for (Object[] o: SessionDAO.getInstance().getSession().createQuery(
                            "select c.uniqueId, e from ClassEvent e inner join e.clazz c left join fetch e.meetings m "+
                            "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where "+
                            "co.subjectArea.uniqueId=:subjectAreaId", Object[].class).
                            setParameter("subjectAreaId", subject.getUniqueId(), Long.class).setCacheable(true).list()) {
                        iClass2event.put((Long)o[0], (ClassEvent)o[1]);
                    }
            	}
            } else {
                for (Object[] o: SessionDAO.getInstance().getSession().createQuery(
                        "select c.uniqueId, e from ClassEvent e inner join e.clazz c left join fetch e.meetings m "+
                        "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where "+
                        "co.subjectArea.session.uniqueId=:sessionId", Object[].class).
                        setParameter("sessionId", getSession().getUniqueId(), Long.class).setCacheable(true).list()) {
                    iClass2event.put((Long)o[0], (ClassEvent)o[1]);
                }
            }
        }
        if (iLocations==null) {
            sLog.info(MSG.statusLoadingLocations());
            iLocations = new Hashtable();
            for (Location location: SessionDAO.getInstance().getSession().createQuery(
                    "select r from Room r where r.session.uniqueId=:sessionId and r.permanentId!=null", Location.class).
                    setParameter("sessionId", getSession().getUniqueId(), Long.class).setCacheable(true).list()) {
                iLocations.put(location.getPermanentId(), location);
            }
            for (Location location: SessionDAO.getInstance().getSession().createQuery(
                    "select r from NonUniversityLocation r where r.session.uniqueId=:sessionId and r.permanentId!=null", Location.class).
                    setParameter("sessionId", getSession().getUniqueId(), Long.class).setCacheable(true).list()) {
                iLocations.put(location.getPermanentId(), location);
            }
        }
    }
    
    public String getStudentName(Long studentId) {
        if (iStudentNames==null) iStudentNames = new Hashtable();
        String name = iStudentNames.get(studentId);
        if (name!=null) return name;
        Student student = StudentDAO.getInstance().get(studentId);
        name = (student==null?"":student.getName(DepartmentalInstructor.sNameFormatLastInitial));
        iStudentNames.put(studentId, name);
        return name;
    }
    
    public boolean isOfSubjectArea(TreeSet<ExamAssignmentInfo> exams) {
        for (ExamAssignmentInfo exam : exams)
        	if (hasSubjectArea(exam)) return true;
        return false;
    }
    
    public void printReport() throws DocumentException {
        generateCache();
        sLog.info(MSG.statusPrintingReport());
        Hashtable<ExamInstructorInfo,TreeSet<ExamAssignmentInfo>> exams = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            if (exam.getPeriod()==null) continue;
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
            TreeSet<ExamAssignmentInfo> examsThisInstructor = exams.get(instructor);
            if (!isOfSubjectArea(examsThisInstructor)) continue;
            if (iSince!=null) {
                ChangeLog last = getLastChange(instructor, examsThisInstructor);
                if (last==null || iSince.compareTo(last.getTimeStamp())>0) {
                    sLog.debug("    " + MSG.logNoChangesFoundFor(instructor.getName()));
                    continue;
                }
            }
            if (!firstInstructor) newPage();
            printReport(instructor, examsThisInstructor);
            firstInstructor = false;
        }
        lastPage();
    }
    
    public Hashtable<ExamInstructorInfo,File> printInstructorReports(String filePrefix, FileGenerator gen) throws DocumentException, IOException {
        generateCache();
        sLog.info(MSG.statusPrintingIndividualInstructorReports());
        Hashtable<ExamInstructorInfo,File> files = new Hashtable();
        Hashtable<ExamInstructorInfo,TreeSet<ExamAssignmentInfo>> exams = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            if (exam.getPeriod()==null) continue;
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
            TreeSet<ExamAssignmentInfo> examsThisInstructor = exams.get(instructor);
            if (!isOfSubjectArea(examsThisInstructor)) continue;
            if (iSince!=null) {
                ChangeLog last = getLastChange(instructor, examsThisInstructor);
                if (last==null || iSince.compareTo(last.getTimeStamp())>0) {
                    sLog.debug("    " + MSG.logNoChangesFoundFor(instructor.getName()));
                    continue;
                }
            }
            sLog.debug("  " + MSG.logGeneratingFileFor(instructor.getName()));
            File file = gen.generate(filePrefix+"_"+
                    (instructor.getExternalUniqueId()!=null?instructor.getExternalUniqueId():instructor.getInstructor().getLastName()), getExtension()); 
                //ApplicationProperties.getTempFile(filePrefix+"_"+(instructor.getExternalUniqueId()!=null?instructor.getExternalUniqueId():instructor.getInstructor().getLastName()), (mode==sModeText?"txt":"pdf"));
            open(file);
            printHeader();
            printReport(instructor, examsThisInstructor);
            lastPage();
            close();
            files.put(instructor,file);
        }
        return files;
    }
    
    public void printReport(ExamInstructorInfo instructor) throws DocumentException {
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam:getExams()) {
            if (exam.getPeriod()==null) continue;
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
        for (ExamAssignmentInfo exam : exams) sections.addAll(exam.getSectionsIncludeCrosslistedDummies());
        setFooter(instructor.getName());//+" ("+instructor.getInstructor().getExternalUniqueId()+")");
        setCont(instructor.getName());
        println(rpad(MSG.lrPropInstructor(), 12), new Cell(instructor.getName()).withColSpan(9));
        if (instructor.getInstructor().getEmail()!=null)
            println(rpad(MSG.lrPropEmail(), 12), new Cell(instructor.getInstructor().getEmail()).withColSpan(9));
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
        if (lastChange!=null && iSince!=null)
            println(rpad(MSG.lrPropLastChange(), 12), new Cell(new SimpleDateFormat(MSG.lrLastChangeDateFormat()).format(lastChange)+(changeObject==null?"":" "+changeObject)));
        if (iClassSchedule) {
            TreeSet<ClassInstructor> allClasses = new TreeSet(new Comparator<ClassInstructor>() {
                ClassComparator cc = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY); 
                public int compare(ClassInstructor c1, ClassInstructor c2) {
                    return cc.compare(c1.getClassInstructing(), c2.getClassInstructing());
                }
            });
            for (Iterator i=DepartmentalInstructor.getAllForInstructor(instructor.getInstructor(), getSession().getUniqueId()).iterator();i.hasNext();) {
                DepartmentalInstructor di = (DepartmentalInstructor)i.next();
                allClasses.addAll(di.getClasses());
            }
            if (!allClasses.isEmpty()) {
                setHeaderLine(
                		new Line(
                				rpad(MSG.lrSubject(), 7),
                				rpad(MSG.lrCourse(), 8),
                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                				rpad(MSG.lrSection(), 9),
                				rpad(MSG.lrDates(), 25),
                				rpad(MSG.lrTime(), 15),
                				rpad(MSG.lrRoom(), 11),
                				rpad(MSG.lrChk(), 3),
                				lpad(MSG.lrShare(), 5)
                		), new Line(
                				lpad("", '-', 7),
                				lpad("", '-', 8),
                				(iItype ? lpad("", '-', 6) : NULL),
                				lpad("", '-', 9),
                				lpad("", '-', 25),
                				lpad("", '-', 15),
                				lpad("", '-', 11),
                				lpad("", '-', 3),
                				lpad("", '-', 5)
                		));
                println();
                println(mpad(MSG.lrSectClassSchedule(),getNrCharsPerLine()).withColSpan(10));
                printHeader(false);
                for (Iterator i=allClasses.iterator();i.hasNext();) {
                    ClassInstructor ci = (ClassInstructor)i.next();
                    String subject = ci.getClassInstructing().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(); 
                    String course = ci.getClassInstructing().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr();
                    String itype =  getItype(ci.getClassInstructing().getSchedulingSubpart().getControllingCourseOffering(), ci.getClassInstructing());
                    String section = (iUseClassSuffix && ci.getClassInstructing().getClassSuffix()!=null?ci.getClassInstructing().getClassSuffix():ci.getClassInstructing().getSectionNumberString());
                    ClassEvent event = (iClass2event==null?ci.getClassInstructing().getEvent():iClass2event.get(ci.getClassInstructing().getUniqueId()));
                    if (event==null && iClass2event!=null && !hasSubjectArea(subject))
                    	event = ci.getClassInstructing().getEvent();
                    if (event==null || event.getMeetings().isEmpty()) {
                        println(
                                rpad(subject,7),
                                rpad(course,8),
                                (iItype?rpad(itype,6):NULL),
                                rpad(section,9),
                                rpad(MSG.lrArrangedHours(), 53).withColSpan(3),
                                rpad(ci.isLead()?MSG.lrYes():MSG.lrNo(),3),
                                lpad(ci.getPercentShare()+"%",5)
                                );
                    } else {
                        MultiMeeting last = null;
                        String lastTime = null, lastDate = null;
                        Cell lastLoc = null;
                        for (MultiMeeting meeting : event.getMultiMeetings()) {
                            List<Cell> line = new ArrayList<Cell>();
                            if (last==null) {
                                line.add(rpad(subject,7));
                                line.add(rpad(course,8));
                                line.add(iItype?rpad(itype,6):NULL);
                                line.add(rpad(section,9));
                            } else {
                            	line.add(rpad("",26+(iItype?7:0)).withColSpan(iItype?4:3));
                            }
                            String date = getMeetingDate(meeting);
                            String time = getMeetingTime(meeting.getMeetings().first());
                            if (last==null || !time.equals(lastTime) || !date.equals(lastDate)) {
                                line.add(rpad(date.equals(lastDate)?"":date,25));
                                line.add(rpad(time.equals(lastTime)?"":time,15));
                            } else {
                                line.add(rpad("",41).withColSpan(2));
                            }
                            Long permId = meeting.getMeetings().first().getLocationPermanentId();
                            Location location = (permId==null?null:(iLocations==null?meeting.getMeetings().first().getLocation():iLocations.get(permId)));
                            if (location==null && iLocations!=null && !hasSubjectArea(subject))
                            	location = meeting.getMeetings().first().getLocation();
                            Cell loc = (location==null?rpad("",11):formatRoom(location));
                            if (last==null || !loc.equals(lastLoc)) {
                                line.add(loc);
                            } else {
                                line.add(rpad("",11));
                            }
                            if (last==null) {
                                line.add(rpad(ci.isLead()?MSG.lrYes():MSG.lrNo(),3));
                                line.add(lpad(ci.getPercentShare()+"%",5));
                            } else {
                            	line.add(rpad("", 3));
                            	line.add(lpad("", 5));
                            }
                            lastLoc = loc;
                            lastTime = time; lastDate = date;
                            last = meeting;
                            println(line.toArray(new Cell[line.size()]));
                            if (iNewPage) { last=null; lastTime = null; lastDate = null; lastLoc = null; }
                        }
                    }
                    if (ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().size()>1) {
                        for (Iterator j=ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().iterator(); j.hasNext();) {
                        	CourseOffering co = (CourseOffering)j.next();
                        	if (co.isIsControl()) continue;
                            String xsubject = co.getSubjectAreaAbbv(); 
                            String xcourse = co.getCourseNbr();
                            println(rpad("  " + xsubject, 8).withSeparator(""), rpad(xcourse,8), (iItype?rpad(itype,6):NULL), rpad(section,9), new Cell(MSG.lrCrossList()).withColSpan(5));
                        }
                    }
                }
            }
        }
        setHeaderLine(
        		new Line(
        				rpad(MSG.lrSubject(), 7),
        				rpad(MSG.lrCourse(), 8),
        				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
        				rpad(MSG.lrSection(), 9),
        				rpad(MSG.lrMeetingTimes(), 36),
        				lpad(MSG.lrEnrl(), 5).withSeparator("  "),
        				rpad(MSG.lrDateAndTime(), 30),
        				rpad(MSG.lrRoom(), 11),
        				lpad(MSG.lrCap(), 5),
        				lpad(MSG.lrExCap(), 5)
        		), new Line(
        				lpad("", '-', 7),
        				lpad("", '-', 8),
        				(iItype ? lpad("", '-', 6) : NULL),
        				lpad("", '-', 9),
        				lpad("", '-', 36),
        				lpad("", '-', 5).withSeparator("  "),
        				lpad("", '-', 30),
        				lpad("", '-', 11),
        				lpad("", '-', 5),
        				lpad("", '-', 5)
        		));
        println();
        println(mpad(MSG.lrSectExaminationSchedule(),getNrCharsPerLine()).withColSpan(10));
        printHeader(false);
        iSubjectPrinted = false; String lastSubject = null;
        iCoursePrinted = false; String lastCourse = null;
        iITypePrinted = false; String lastItype = null;
        iPeriodPrinted = false; String lastSection = null;
        for (ExamAssignmentInfo exam: exams) {
            for (Iterator<ExamSectionInfo> j = exam.getSectionsIncludeCrosslistedDummies().iterator(); j.hasNext();) {
                ExamSectionInfo  section = j.next();
                if (iSubjectPrinted && !section.getSubject().equals(lastSubject)) { iSubjectPrinted = false; iCoursePrinted = false; iITypePrinted = false; iPeriodPrinted = false; }
                if (iCoursePrinted && !section.getCourseNbr().equals(lastCourse)) { iCoursePrinted = false; iITypePrinted = false; iPeriodPrinted = false; }
                if (iITypePrinted && !section.getItype().equals(lastItype)) { iITypePrinted = false; iPeriodPrinted = false; }
                if (iPeriodPrinted && !section.getSection().equals(lastSection)) { iPeriodPrinted = false; }
                if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                    println(
                            rpad(iSubjectPrinted?"":section.getSubject(), 7),
                            rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                            (iItype?rpad(iITypePrinted?"":section.getItype(), 6):NULL),
                            rpad(iPeriodPrinted?"":section.getSection(), 9),
                            rpad(getMeetingTime(section),36),
                            lpad(String.valueOf(section.getNrStudents()),5).withSeparator("  "),
                            rpad((section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),30),
                            new Cell(section.getExamAssignment()==null?"":iNoRoom)
                            );
                } else {
                    if (getLineNumber()+section.getExamAssignment().getRooms().size()>getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                    boolean firstRoom = true;
                    for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                        println(
                                rpad(!firstRoom || iSubjectPrinted?"":section.getSubject(), 7),
                                rpad(!firstRoom || iCoursePrinted?"":section.getCourseNbr(), 8),
                                (iItype?rpad(!firstRoom || iITypePrinted?"":section.getItype(), 6):NULL),
                                rpad(!firstRoom || iPeriodPrinted?"":section.getSection(), 9),
                                (!firstRoom?rpad("", 36):rpad(getMeetingTime(section), 36)),
                                lpad(!firstRoom?"":String.valueOf(section.getNrStudents()),5).withSeparator("  "),
                                rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),30),
                                formatRoom(room),
                                lpad(""+room.getCapacity(),5),
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
            println();
        }
        setHeaderLine();
        
        boolean headerPrinted = false;
        lastSubject = null;
        for (ExamSectionInfo section : sections) {
            iSubjectPrinted = (!iNewPage && lastSubject!=null && lastSubject.equals(section.getSubject()));
            ExamAssignmentInfo exam = section.getExamAssignmentInfo();
            if (exam==null || exam.getPeriod()==null) continue;
            iCoursePrinted = false;
                if (iDirect) for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                    if (!conflict.getStudents().contains(instructor.getId())) continue;
                    iPeriodPrinted = false;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                            if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                            if (!headerPrinted) {
                                setHeaderLine();
                                if (!iNewPage) println();
                                if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                                setHeaderLine(
                                		new Line(
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrDateAndTime(), 25),
                                				rpad(MSG.lrType(), 6),
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrTime(), 15)
                                		), new Line(
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 25),
                                				lpad("", '-', 6),
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 15)
                                		));
                                println(mpad(MSG.lrSectInstructorConflicts(),getNrCharsPerLine()).withColSpan(10));
                                printHeader(false);
                                setCont(MSG.lrInstructorConflicts(instructor.getName()));
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(),9),
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),25),
                                    rpad(iPeriodPrinted?"":MSG.lrDIRECT(),6),
                                    rpad(other.getSubject(),7),
                                    rpad(other.getCourseNbr(),8),
                                    (iItype?rpad(other.getItype(),6):NULL),
                                    rpad(other.getSection(),9),
                                    new Cell(other.getExamAssignment().getTimeFixedLength())
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        if (!headerPrinted) {
                            setHeaderLine();
                            if (!iNewPage) println();
                            if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                            setHeaderLine(
                            		new Line(
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrDateAndTime(), 25),
                            				rpad(MSG.lrType(), 6),
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrTime(), 15)
                            		), new Line(
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 25),
                            				lpad("", '-', 6),
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 15)
                            		));
                            println(mpad(MSG.lrSectInstructorConflicts(),getNrCharsPerLine()).withColSpan(10));
                            printHeader(false);
                            setCont(MSG.lrInstructorConflicts(instructor.getName()));
                            headerPrinted = true;
                        }
                        if (conflict.isOtherClass()) {
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(),9),
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),25),
                                    rpad(iPeriodPrinted?"":MSG.lrCLASS(),6),
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(),7),
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(),8),
                                    (iItype?rpad(iExternal?conflict.getOtherClass().getExternalUniqueId():conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(),6):NULL),
                                    rpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(),9),
                                    getMeetingTime(conflict.getOtherEventTime())
                                    );
                        } else {
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(),9),
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),25),
                                    rpad(iPeriodPrinted?"":MSG.lrEVENT(),6),
                                    rpad(conflict.getOtherEventName(),(iItype?33:26)),
                                    getMeetingTime(conflict.getOtherEventTime())
                                    );
                        }
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
                }
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(instructor.getId())) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        if (!otherExam.getInstructors().contains(instructor)) continue;
                        for (ExamSectionInfo other : otherExam.getSectionsIncludeCrosslistedDummies()) {
                            if (!headerPrinted) {
                                setHeaderLine();
                                if (!iNewPage) println();
                                if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                                setHeaderLine(
                                		new Line(
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrDateAndTime(), 25),
                                				rpad(MSG.lrType(), 6),
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrTime(), 15)
                                		), new Line(
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 25),
                                				lpad("", '-', 6),
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 15)
                                		));
                                println(mpad(MSG.lrSectInstructorConflicts(),getNrCharsPerLine()).withColSpan(10));
                                printHeader(false);
                                setCont(MSG.lrInstructorConflicts(instructor.getName()));
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(),7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(),9),
                                    rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),25),
                                    rpad(iPeriodPrinted?"":MSG.lrMore2DAY(),6),
                                    rpad(other.getSubject(),7),
                                    rpad(other.getCourseNbr(),8),
                                    (iItype?rpad(other.getItype(),6):NULL),
                                    rpad(other.getSection(),9),
                                    new Cell(other.getExamAssignment().getTimeFixedLength())
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    }
                }
                if (iBtb) for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(instructor.getId())) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (!conflict.getOtherExam().getInstructors().contains(instructor)) continue;
                        if (!headerPrinted) {
                            setHeaderLine();
                        	if (!iNewPage) println();
                            if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                            setHeaderLine(
                            		new Line(
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrDateAndTime(), 25),
                            				rpad(MSG.lrType(), 6),
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrTime(), 15)
                            		), new Line(
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 25),
                            				lpad("", '-', 6),
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 15)
                            		));
                            println(mpad(MSG.lrSectInstructorConflicts(),getNrCharsPerLine()).withColSpan(10));
                            printHeader(false);
                            setCont(MSG.lrInstructorConflicts(instructor.getName()));
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(),7),
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                rpad(iCoursePrinted?"":section.getSection(),9),
                                rpad(iCoursePrinted?"":exam.getPeriodNameFixedLength(),25),
                                rpad(iPeriodPrinted?"":MSG.lrBTB(),6),
                                rpad(other.getSubject(),7),
                                rpad(other.getCourseNbr(),8),
                                (iItype?rpad(other.getItype(),6):NULL),
                                rpad(other.getSection(),9),
                                new Cell(other.getExamAssignment().getTimeFixedLength())
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
            }
        }

        setHeaderLine();
        headerPrinted = false;
        lastSubject = null;
        for (ExamSectionInfo section : sections) {
            iSubjectPrinted = (!iNewPage && lastSubject!=null && lastSubject.equals(section.getSubject()));
            ExamAssignmentInfo exam = section.getExamAssignmentInfo();
            if (exam==null || exam.getPeriod()==null) continue;
            iCoursePrinted = false;
            Vector<Long> students = new Vector<Long>(section.getStudentIds());
            Collections.sort(students,new Comparator<Long>() {
                public int compare(Long s1, Long s2) {
                    int cmp = getStudentName(s1).compareTo(getStudentName(s2));
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
                        for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            if (!headerPrinted) {
                                setHeaderLine();
                            	if (!iNewPage) println();
                                if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                                setHeaderLine(
                                		new Line(
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrDate(), 7),
                                				rpad(MSG.lrTime(), 6),
                                				rpad(MSG.lrName(), 25),
                                				rpad(MSG.lrType(), 6),
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrTime(), 15)
                                		), new Line(
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 7),
                                				lpad("", '-', 6),
                                				lpad("", '-', 25),
                                				lpad("", '-', 6),
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 15)
                                		));
                                println(mpad(MSG.lrSectStudentConflicts(),getNrCharsPerLine()).withColSpan(10));
                                printHeader(false);
                                setCont(MSG.lrStudentConflicts(instructor.getName()));
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(), 7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(), 9),
                                    (iCoursePrinted?rpad("", 7):formatShortPeriodNoEndTimeDate(exam)),
                                    (iCoursePrinted?rpad("", 6):formatShortPeriodNoEndTimeTime(exam)),
                                    rpad(iStudentPrinted?"":getStudentName(studentId),25),
                                    rpad(iPeriodPrinted?"":MSG.lrDIRECT(),6),
                                    rpad(other.getSubject(), 7),
                                    rpad(other.getCourseNbr(), 8),
                                    (iItype?rpad(other.getItype(), 6):NULL),
                                    rpad(other.getSection(), 9),
                                    new Cell(other.getExamAssignment().getTimeFixedLength())
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        if (!headerPrinted) {
                            setHeaderLine();
                        	if (!iNewPage) println();
                            if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                            setHeaderLine(
                            		new Line(
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrDate(), 7),
                            				rpad(MSG.lrTime(), 6),
                            				rpad(MSG.lrName(), 25),
                            				rpad(MSG.lrType(), 6),
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrTime(), 15)
                            		), new Line(
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 7),
                            				lpad("", '-', 6),
                            				lpad("", '-', 25),
                            				lpad("", '-', 6),
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 15)
                            		));
                            println(mpad(MSG.lrSectStudentConflicts(),getNrCharsPerLine()).withColSpan(10));
                            printHeader(false);
                            setCont(MSG.lrStudentConflicts(instructor.getName()));
                            headerPrinted = true;
                        }
                        if (conflict.isOtherClass()) {
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(), 7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(), 9),
                                    (iCoursePrinted?rpad("", 7):formatShortPeriodNoEndTimeDate(exam)),
                                    (iCoursePrinted?rpad("", 6):formatShortPeriodNoEndTimeTime(exam)),
                                    rpad(iStudentPrinted?"":getStudentName(studentId),25),
                                    rpad(iPeriodPrinted?"":MSG.lrCLASS(),6),
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv(), 7),
                                    rpad(conflict.getOtherClass().getSchedulingSubpart().getControllingCourseOffering().getCourseNbr(), 8),
                                    (iItype?rpad(iExternal?conflict.getOtherClass().getExternalUniqueId():conflict.getOtherClass().getSchedulingSubpart().getItypeDesc(), 6):NULL),
                                    rpad(iUseClassSuffix && conflict.getOtherClass().getClassSuffix()!=null?conflict.getOtherClass().getClassSuffix():conflict.getOtherClass().getSectionNumberString(), 9),
                                    getMeetingTime(conflict.getOtherEventTime())
                                    );
                        } else {
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(), 7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(), 9),
                                    (iCoursePrinted?rpad("", 7):formatShortPeriodNoEndTimeDate(exam)),
                                    (iCoursePrinted?rpad("", 6):formatShortPeriodNoEndTimeTime(exam)),
                                    rpad(iStudentPrinted?"":getStudentName(studentId),25),
                                    rpad(iPeriodPrinted?"":MSG.lrEVENT(),6),
                                    rpad(conflict.getOtherEventName(),(iItype?33:26)),
                                    getMeetingTime(conflict.getOtherEventTime())
                                    );
                        }
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
                }
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        for (ExamSectionInfo other : otherExam.getSectionsIncludeCrosslistedDummies()) {
                            if (!other.getStudentIds().contains(studentId)) continue;
                            if (!headerPrinted) {
                            	if (!iNewPage) println();
                                setHeaderLine();
                                if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                                setHeaderLine(
                                		new Line(
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrDate(), 7),
                                				rpad(MSG.lrTime(), 6),
                                				rpad(MSG.lrName(), 25),
                                				rpad(MSG.lrType(), 6),
                                				rpad(MSG.lrSubject(), 7),
                                				rpad(MSG.lrCourse(), 8),
                                				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                                				rpad(MSG.lrSection(), 9),
                                				rpad(MSG.lrTime(), 15)
                                		), new Line(
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 7),
                                				lpad("", '-', 6),
                                				lpad("", '-', 25),
                                				lpad("", '-', 6),
                                				lpad("", '-', 7),
                                				lpad("", '-', 8),
                                				(iItype ? lpad("", '-', 6) : NULL),
                                				lpad("", '-', 9),
                                				lpad("", '-', 15)
                                		));
                                println(mpad(MSG.lrSectStudentConflicts(),getNrCharsPerLine()).withColSpan(10));
                                printHeader(false);
                                setCont(MSG.lrStudentConflicts(instructor.getName()));
                                headerPrinted = true;
                            }
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(), 7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(), 9),
                                    (iCoursePrinted?rpad("", 7):formatShortPeriodNoEndTimeDate(exam)),
                                    (iCoursePrinted?rpad("", 6):formatShortPeriodNoEndTimeTime(exam)),
                                    rpad(iStudentPrinted?"":getStudentName(studentId),25),
                                    rpad(iPeriodPrinted?"":MSG.lrMore2DAY(),6),
                                    rpad(other.getSubject(), 7),
                                    rpad(other.getCourseNbr(), 8),
                                    (iItype?rpad(other.getItype(), 6):NULL),
                                    rpad(other.getSection(), 9),
                                    new Cell(other.getExamAssignment().getTimeFixedLength())
                                    );
                            iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                            lastSubject = section.getSubject();
                        }
                    }
                }
                if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(studentId)) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (!other.getStudentIds().contains(studentId)) continue;
                        if (!headerPrinted) {
                            setHeaderLine();
                        	if (!iNewPage) println();
                            if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                            setHeaderLine(
                            		new Line(
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrDate(), 7),
                            				rpad(MSG.lrTime(), 6),
                            				rpad(MSG.lrName(), 25),
                            				rpad(MSG.lrType(), 6),
                            				rpad(MSG.lrSubject(), 7),
                            				rpad(MSG.lrCourse(), 8),
                            				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
                            				rpad(MSG.lrSection(), 9),
                            				rpad(MSG.lrTime(), 15)
                            		), new Line(
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 7),
                            				lpad("", '-', 6),
                            				lpad("", '-', 25),
                            				lpad("", '-', 6),
                            				lpad("", '-', 7),
                            				lpad("", '-', 8),
                            				(iItype ? lpad("", '-', 6) : NULL),
                            				lpad("", '-', 9),
                            				lpad("", '-', 15)
                            		));
                            println(mpad(MSG.lrSectStudentConflicts(),getNrCharsPerLine()).withColSpan(10));
                            printHeader(false);
                            setCont(MSG.lrStudentConflicts(instructor.getName()));
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(), 7),
                                rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                rpad(iCoursePrinted?"":section.getSection(),9),
                                (iCoursePrinted?rpad("", 7):formatShortPeriodNoEndTimeDate(exam)),
                                (iCoursePrinted?rpad("", 6):formatShortPeriodNoEndTimeTime(exam)),
                                rpad(iStudentPrinted?"":getStudentName(studentId),25),
                                rpad(iPeriodPrinted?"":MSG.lrBTB(),6),
                                rpad(other.getSubject(),7),
                                rpad(other.getCourseNbr(),8),
                                (iItype?rpad(other.getItype(),6):NULL),
                                rpad(other.getSection(),9),
                                new Cell(other.getExamAssignment().getTimeFixedLength())
                                );
                        iSubjectPrinted = iCoursePrinted = iStudentPrinted = iPeriodPrinted = !iNewPage;
                        lastSubject = section.getSubject();
                    }
                }                    
            }
        }
        setHeaderLine();
        setCont(null);
    }
    
    public static interface FileGenerator {
        public File generate(String prefix, String ext);
    }
}
