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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.dao.SessionDAO;
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

/**
 * @author Tomas Muller
 */
public class StudentExamReport extends PdfLegacyExamReport {
    protected static Log sLog = LogFactory.getLog(StudentExamReport.class);
    Hashtable<Long,Student> iStudents = null;
    Hashtable<Long,ClassEvent> iClass2event = null;
    Hashtable<Long,Location> iLocations = null;
    
    public StudentExamReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legactReportStudentExaminationSchedule(), session, examType, subjectAreas, exams);
    }
    
    public StudentExamReport(int mode, OutputStream out, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, out, MSG.legactReportStudentExaminationSchedule(), session, examType, subjectAreas, exams);
    }

    private void generateCache() {
        if (iStudents==null) {
            sLog.info(MSG.statusLoadingStudents());
            iStudents = new Hashtable();
            for (Iterator i=new StudentDAO().getSession().createQuery("select s from Student s where s.session.uniqueId=:sessionId").setParameter("sessionId", getSession().getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list().iterator();i.hasNext();) {
                Student s = (Student)i.next();
                iStudents.put(s.getUniqueId(), s);
            }
        }
        if (iClass2event==null) {
            sLog.info(MSG.statusLoadingClassEvents());
            iClass2event = new Hashtable();
            if (hasSubjectAreas()) {
            	for (SubjectArea subject: getSubjectAreas()) {
                    for (Iterator i=new SessionDAO().getSession().createQuery(
                            "select c.uniqueId, e from ClassEvent e inner join e.clazz c left join fetch e.meetings m "+
                            "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where "+
                            "co.subjectArea.uniqueId=:subjectAreaId").
                            setParameter("subjectAreaId", subject.getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list().iterator();i.hasNext();) {
                        Object[] o = (Object[])i.next();
                        iClass2event.put((Long)o[0], (ClassEvent)o[1]);
                    }
            	}
            } else {
                for (Iterator i=new SessionDAO().getSession().createQuery(
                        "select c.uniqueId, e from ClassEvent e inner join e.clazz c left join fetch e.meetings m "+
                        "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where "+
                        "co.subjectArea.session.uniqueId=:sessionId").
                        setParameter("sessionId", getSession().getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    iClass2event.put((Long)o[0], (ClassEvent)o[1]);
                }
            }
        }
        if (iLocations==null) {
            sLog.info(MSG.statusLoadingLocations());
            iLocations = new Hashtable();
            for (Iterator i=new SessionDAO().getSession().createQuery(
                    "select r from Room r where r.session.uniqueId=:sessionId and r.permanentId!=null").
                    setParameter("sessionId", getSession().getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list().iterator();i.hasNext();) {
                Location location = (Location)i.next();
                iLocations.put(location.getPermanentId(), location);
            }
            for (Iterator i=new SessionDAO().getSession().createQuery(
                    "select r from NonUniversityLocation r where r.session.uniqueId=:sessionId and r.permanentId!=null").
                    setParameter("sessionId", getSession().getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list().iterator();i.hasNext();) {
                Location location = (Location)i.next();
                iLocations.put(location.getPermanentId(), location);
            }
        }
    }
    
    public boolean isOfSubjectArea(TreeSet<ExamSectionInfo> sections) {
        for (ExamSectionInfo section : sections)
        	if (hasSubjectArea(section)) return true;
        return false;
    }

    public void printReport() throws DocumentException {
        generateCache();
        sLog.info(MSG.statusPrintingReport());
        Hashtable<Student,TreeSet<ExamSectionInfo>> sections = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section:exam.getSectionsIncludeCrosslistedDummies()) {
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
        printHeader();
        int index = 0;
        for (Student student : new TreeSet<Student>(sections.keySet())) {
            TreeSet<ExamSectionInfo> sectionsThisStudent = sections.get(student);
            if (!isOfSubjectArea(sectionsThisStudent)) continue;
            if (iSince!=null) {
                ChangeLog last = getLastChange(sectionsThisStudent);
                if (last==null || iSince.compareTo(last.getTimeStamp())>0) {
                    sLog.debug(MSG.logNoChangesFoundFor(student.getName(DepartmentalInstructor.sNameFormatLastFist)));
                    continue;
                }
            }
            if (index>0) newPage();
            printReport(student, sectionsThisStudent);
            index++;
            if ((index%100)==0) sLog.debug("  "+index+" students printed");
        }
        lastPage();
    }
    
    public void printReport(Long studentId) throws DocumentException {
        TreeSet<ExamSectionInfo> sections = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies())
                if (section.getStudentIds().contains(studentId)) sections.add(section);
        }
        if (sections.isEmpty()) return;
        Student student = new StudentDAO().get(studentId);
        printHeader();
        printReport(student, sections);
        lastPage();
    }
    
    public static class StudentClassComparator implements Comparator<Class_> {
    	private Student iStudent;
    	public StudentClassComparator(Student student) {
    		iStudent = student;
    	}
    	public CourseOffering getCourse(Class_ clazz) {
        	CourseOffering correctedCourse = clazz.getSchedulingSubpart().getControllingCourseOffering();
        	for (Iterator i=iStudent.getClassEnrollments().iterator();i.hasNext();) {
        		StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
        		if (sce.getCourseOffering().getInstructionalOffering().equals(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering())) {
        			correctedCourse = sce.getCourseOffering();
        			break;
        		}
        	}
        	return correctedCourse;
    	}
    	public int compare(Class_ c1, Class_ c2) {
    		CourseOffering co1 = getCourse(c1);
    		CourseOffering co2 = getCourse(c2);
    		int cmp = co1.getSubjectAreaAbbv().compareTo(co2.getSubjectAreaAbbv());
    		if (cmp!=0) return cmp;
    		cmp = co1.getCourseNbr().compareTo(co2.getCourseNbr());
    		if (cmp!=0) return cmp;
    		if (isParentOf(c1, c2)) return -1;
    		if (isParentOf(c2, c1)) return 1;
    		if (cmp!=0) return cmp;
    		cmp = c1.getSchedulingSubpart().getItype().compareTo(c2.getSchedulingSubpart().getItype());
    		if (cmp!=0) return cmp;
    		return c1.getUniqueId().compareTo(c2.getUniqueId());
    	}
    	public boolean isParentOf(Class_ c1, Class_ c2) {
    		if (c2.getParentClass()!=null) {
    			if (c2.getParentClass().equals(c1)) return true;
    			return isParentOf(c1, c2.getParentClass());
    		}
    		return false;
    	}
    }

    public void printReport(Student student, TreeSet<ExamSectionInfo> sections) throws DocumentException {
        String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
        String shortName = student.getName(DepartmentalInstructor.sNameFormatLastInitial).toUpperCase();
        setPageName(shortName);
        setCont(shortName);
        println(rpad(MSG.lrPropStudent(), 12), new Cell(name).withColSpan(9));
        if (student.getEmail()!=null)
        	println(rpad(MSG.lrPropEmail(), 12), new Cell(student.getEmail()).withColSpan(9));
        if (iClassSchedule) {
        	StudentClassComparator scc = new StudentClassComparator(student);
            TreeSet<Class_> allClasses = new TreeSet(scc);
            for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                allClasses.add(sce.getClazz());
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
                				rpad(MSG.lrInstructor(), 45)
                		), new Line(
                				lpad("", '-', 7),
                				lpad("", '-', 8),
                				(iItype ? lpad("", '-', 6) : NULL),
                				lpad("", '-', 9),
                				lpad("", '-', 25),
                				lpad("", '-', 15),
                				lpad("", '-', 11),
                				lpad("", '-', 45)
                		));
                println();
                println(mpad(MSG.lrSectClassSchedule(),getNrCharsPerLine()).withColSpan(10));
                printHeader(false);
                for (Class_ clazz : allClasses) {
                    String instructor = "";
                    if (clazz.isDisplayInstructor()) {
                        for (Iterator i=new TreeSet(clazz.getClassInstructors()).iterator();i.hasNext();) {
                            ClassInstructor ci = (ClassInstructor)i.next();
                            if (instructor.length()>0) instructor+=", ";
                            instructor += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatLastInitial);
                        }
                    }
                	CourseOffering correctedCourse = scc.getCourse(clazz);
                    String subject = correctedCourse.getSubjectAreaAbbv(); 
                    String course = correctedCourse.getCourseNbr();
                    String itype =  getItype(correctedCourse, clazz);
                    String section = (iUseClassSuffix && clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString());
                    ClassEvent event = (iClass2event==null?clazz.getEvent():iClass2event.get(clazz.getUniqueId()));
                    if (event==null && iClass2event!=null && !hasSubjectArea(subject))
                    	event = clazz.getEvent();
                    if (event==null || event.getMeetings().isEmpty()) {
                        println(
                                rpad(subject,7),
                                rpad(course,8),
                                (iItype?rpad(itype,6):NULL),
                                rpad(section,9),
                                rpad(MSG.lrArrangedHours(),53).withColSpan(3),
                                rpad(instructor,45)
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
                            if (last==null)
                                line.add(rpad(instructor,45));
                            else
                            	line.add(rpad("",45));
                            lastLoc = loc;
                            lastTime = time; lastDate = date;
                            last = meeting;
                            println(line.toArray(new Cell[line.size()]));
                            if (iNewPage) { last=null; lastTime = null; lastDate = null; lastLoc = null; }
                        }
                    }
                }
            }
        }
        setHeaderLine();
        if (getLineNumber()+5>=getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
        setHeaderLine(
        		new Line(
        				rpad(MSG.lrSubject(), 7),
        				rpad(MSG.lrCourse(), 8),
        				(iItype ? rpad(iExternal ? MSG.lrExtnId() : MSG.lrType(), 6) : NULL),
        				rpad(MSG.lrSection(), 9),
        				rpad(MSG.lrMeetingTimes(), 36),
        				rpad(MSG.lrDateAndTime(), 30),
        				rpad(MSG.lrRoom(), 11)
        		), new Line(
        				lpad("", '-', 7),
        				lpad("", '-', 8),
        				(iItype ? lpad("", '-', 6) : NULL),
        				lpad("", '-', 9),
        				lpad("", '-', 36),
        				lpad("", '-', 30),
        				lpad("", '-', 11)
        		));
        println();
        println(mpad(MSG.lrSectExaminationSchedule(),getNrCharsPerLine()).withColSpan(10));
        printHeader(false);
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
                        rpad(iSubjectPrinted?"":section.getSubject(), 7),
                        rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                        (iItype?rpad(iITypePrinted?"":section.getItype(), 6):NULL),
                        rpad(iPeriodPrinted?"":section.getSection(), 9),
                        rpad(getMeetingTime(section),36),
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
                            rpad(!firstRoom?"":(section.getExamAssignment()==null?"":section.getExamAssignment().getPeriodNameFixedLength()),30),
                            formatRoom(room));
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
            iCoursePrinted = false;
                if (iDirect) for (DirectConflict conflict : exam.getDirectConflicts()) {
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    iPeriodPrinted = false;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                            if (!other.getStudentIds().contains(student.getUniqueId())) continue;
                            if (!headerPrinted) {
                                setHeaderLine();
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
                                if (!iNewPage) println();
                                println(mpad(MSG.lrSectExaminationConflicts(),getNrCharsPerLine()).withColSpan(10));
                                printHeader(false);
                                setCont(MSG.lrExaminationConflicts(shortName));
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
                        	if (!iNewPage) println();
                            println(mpad(MSG.lrSectExaminationConflicts(),getNrCharsPerLine()).withColSpan(10));
                            printHeader(false);
                            setCont(MSG.lrExaminationConflicts(shortName));
                            headerPrinted = true;
                        }
                        if (conflict.isOtherClass()) {
                            println(
                                    rpad(iSubjectPrinted?"":section.getSubject(), 7),
                                    rpad(iCoursePrinted?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iCoursePrinted?"":section.getItype(), 6):NULL),
                                    rpad(iCoursePrinted?"":section.getSection(), 9),
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
                if (iM2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    iPeriodPrinted = false;
                    for (ExamAssignment otherExam : conflict.getOtherExams()) {
                        for (ExamSectionInfo other : otherExam.getSectionsIncludeCrosslistedDummies()) {
                            if (!other.getStudentIds().contains(student.getUniqueId())) continue;
                            if (!headerPrinted) {
                                setHeaderLine();
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
                            	if (!iNewPage) println();
                                println(mpad(MSG.lrSectExaminationConflicts(),getNrCharsPerLine()).withColSpan(10));
                                printHeader(false);
                                setCont(MSG.lrExaminationConflicts(shortName));
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
                if (iBtb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    iPeriodPrinted = false;
                    for (ExamSectionInfo other : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (!other.getStudentIds().contains(student.getUniqueId())) continue;
                        if (!headerPrinted) {
                            setHeaderLine();
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
                            if (!iNewPage) println();
                            println(mpad(MSG.lrSectExaminationConflicts(),getNrCharsPerLine()).withColSpan(10));
                            printHeader(false);
                            setCont(MSG.lrExaminationConflicts(shortName));
                            headerPrinted = true;
                        }
                        println(
                                rpad(iSubjectPrinted?"":section.getSubject(), 7),
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
    
    public Hashtable<Student,File> printStudentReports(String filePrefix, FileGenerator gen) throws DocumentException, IOException {
        generateCache();
        sLog.info(MSG.statusPrintingIndividualStudentReports());
        Hashtable<Student,File> files = new Hashtable();
        Hashtable<Student,TreeSet<ExamSectionInfo>> sections = new Hashtable();
        for (ExamAssignmentInfo exam:getExams()) {
            for (ExamSectionInfo section:exam.getSectionsIncludeCrosslistedDummies()) {
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
            TreeSet<ExamSectionInfo> sectionsThisStudent = sections.get(student);
            if (!isOfSubjectArea(sectionsThisStudent)) continue;
            if (iSince!=null) {
                ChangeLog last = getLastChange(sectionsThisStudent);
                if (last==null || iSince.compareTo(last.getTimeStamp())>0) {
                    sLog.debug(MSG.logNoChangesFoundFor(student.getName(DepartmentalInstructor.sNameFormatLastFist)));
                    continue;
                }
            }
            sLog.debug(MSG.logGeneratingFileFor(student.getName(DepartmentalInstructor.sNameFormatLastFist)));
            File file = gen.generate(filePrefix+"_"+
                    (student.getExternalUniqueId()!=null?student.getExternalUniqueId():student.getLastName()),
                    getExtension()); 
                //ApplicationProperties.getTempFile(filePrefix+"_"+(instructor.getExternalUniqueId()!=null?instructor.getExternalUniqueId():instructor.getInstructor().getLastName()), (mode==sModeText?"txt":"pdf"));
            open(file);
            printHeader();
            printReport(student, sectionsThisStudent);
            lastPage();
            close();
            files.put(student,file);
        }
        return files;
    }
}
