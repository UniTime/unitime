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
package org.unitime.timetable.action;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamAssignmentReportForm;
import org.unitime.timetable.form.ExamAssignmentReportForm.ExamReport;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DistributionConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.Parameters;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Action(value = "examAssignmentReport", results = {
		@Result(name = "show", type = "tiles", location = "examAssignmentReport.tiles")
	})
@TilesDefinition(name = "examAssignmentReport.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Reports"),
		@TilesPutAttribute(name = "body", value = "/exam/assignmentReport.jsp"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamAssignmentReportAction extends UniTimeAction<ExamAssignmentReportForm> {
	private static final long serialVersionUID = -5536751983250422597L;
	protected static final ConstantsMessages CONST = Localization.create(ConstantsMessages.class);
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);

	public String execute() throws Exception {
	    // Check Access
	    sessionContext.checkPermission(Right.ExaminationReports);
	    
    	ExamSolverProxy solver = getExaminationSolverService().getSolver();

    	if (form == null) {
	    	form = new ExamAssignmentReportForm();
	    	form.reset();
	    	if (solver != null) form.setExamType(solver.getExamTypeId());
	    }
	    
    	if (form.getOp() != null) op = form.getOp();

        if (MSG.actionExportPdf().equals(op) || MSG.actionExportCsv().equals(op) || MSG.buttonApply().equals(op)) {
            form.save(sessionContext);
        } else if (MSG.buttonRefresh().equals(op)) {
            form.reset();
            if (solver != null) form.setExamType(solver.getExamTypeId());
        }
        
        form.setCanSeeAll(sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent));
        
        Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
        RoomAvailability.setAvailabilityWarning(request, session, form.getExamType(), true, false);
        
        form.load(sessionContext);
        
        Collection<ExamAssignmentInfo> assignedExams = null;
        if (form.getSubjectArea()!=null && form.getSubjectArea()!=0 && form.getExamType() != null) {
            if (solver!=null && solver.getExamTypeId().equals(form.getExamType()))
                assignedExams = solver.getAssignedExams(form.getSubjectArea());
            else {
                if (ApplicationProperty.ExaminationCacheConflicts.isTrue() && form.getSubjectArea()!=null && form.getSubjectArea()>0)
                    assignedExams = Exam.findAssignedExams(session.getUniqueId(),form.getSubjectArea(),form.getExamType());
                else
                    assignedExams = findAssignedExams(session.getUniqueId(),form.getSubjectArea(),form.getExamType());
            }
        }
        
        WebTable.setOrder(sessionContext,"examAssignmentReport["+form.getReport()+"].ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(session.getUniqueId(), true, form, assignedExams);
        
        if (MSG.actionExportPdf().equals(op) && table!=null) {
        	ExportUtils.exportPDF(
        			getTable(session.getUniqueId(), false, form, assignedExams),
        			WebTable.getOrder(sessionContext,"examAssignmentReport["+form.getReport()+"].ord"),
        			response, "xreport");
        	return null;
        }
        
        if (MSG.actionExportCsv().equals(op) && table!=null) {
        	ExportUtils.exportCSV(
        			getTable(session.getUniqueId(), false, form, assignedExams),
        			WebTable.getOrder(sessionContext,"examAssignmentReport["+form.getReport()+"].ord"),
        			response, "xreport");
        	return null;
        }

        if (table!=null) {
            form.setTable(table.printTable(WebTable.getOrder(sessionContext,"examAssignmentReport["+form.getReport()+"].ord")), table.getNrColumns(), assignedExams.size());
            BackTracker.markForBack(
                    request, 
                    "examAssignmentReport.action?form.op=" + MSG.buttonApply() + "&form.examType="+getForm().getExamType()+"&form.subjectArea="+getForm().getSubjectArea()
                    	+"&form.showSections="+getForm().getShowSections()
                    	+"&form.report="+getForm().getReport()
                    	+"&form.filter="+URLEncoder.encode(getForm().getFilter(), "utf-8"),
                    MSG.backExaminationReports(form.getReportName(), form.getSubjectArea() == -1l ? CONST.all() : form.getSubjectAreaAbbv()), 
                    true, true);
        }
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable);

        return "show";
	}
	
	public static class CacheAssignedExams {
		Long sessionId, examTypeId;
        Hashtable<Long, Exam> exams = new Hashtable();
        Hashtable<Long,Set<Long>> owner2students = new Hashtable();
        Hashtable<Long,Set<Exam>> student2exams = new Hashtable();
        Hashtable<Long, Set<Meeting>> period2meetings = new Hashtable();
        Hashtable<Long,Hashtable<Long,Set<Long>>> owner2course2students = new Hashtable();
        Parameters p;
		
        public CacheAssignedExams(Long sessionId, Long examTypeId) {
			this.sessionId = sessionId;
			this.examTypeId = examTypeId;
	        for (Iterator i=ExamDAO.getInstance().getSession().createQuery(
	                "select x from Exam x where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId", Exam.class
	                ).setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
	            Exam exam = (Exam)i.next();
	            exams.put(exam.getUniqueId(), exam);
	        }
	        ExamDAO.getInstance().getSession().createQuery(
	                "select c from Class_ c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:classType and c.uniqueId=o.ownerId", Class_.class)
	                .setParameter("sessionId", sessionId)
	                .setParameter("examTypeId", examTypeId)
	                .setParameter("classType", ExamOwner.sOwnerTypeClass).setCacheable(true).list();
	        ExamDAO.getInstance().getSession().createQuery(
	                "select c from InstrOfferingConfig c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:configType and c.uniqueId=o.ownerId", InstrOfferingConfig.class)
	                .setParameter("sessionId", sessionId)
	                .setParameter("examTypeId", examTypeId)
	                .setParameter("configType", ExamOwner.sOwnerTypeConfig).setCacheable(true).list();
	        ExamDAO.getInstance().getSession().createQuery(
	                "select c from CourseOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:courseType and c.uniqueId=o.ownerId", CourseOffering.class)
	                .setParameter("sessionId", sessionId)
	                .setParameter("examTypeId", examTypeId)
	                .setParameter("courseType", ExamOwner.sOwnerTypeCourse).setCacheable(true).list();
	        ExamDAO.getInstance().getSession().createQuery(
	                "select c from InstructionalOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:offeringType and c.uniqueId=o.ownerId", InstructionalOffering.class)
	                .setParameter("sessionId", sessionId)
	                .setParameter("examTypeId", examTypeId)
	                .setParameter("offeringType", ExamOwner.sOwnerTypeOffering).setCacheable(true).list();
	        
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
	                "Exam x inner join x.owners o, "+
	                "StudentClassEnrollment e inner join e.clazz c "+
	                "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
	                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeClass+" and "+
	                "o.ownerId=c.uniqueId", Object[].class)
	                .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
                Long examId = (Long)o[0];
                Long ownerId = (Long)o[1];
                Long studentId = (Long)o[2];
                Set<Long> studentsOfOwner = owner2students.get(ownerId);
                if (studentsOfOwner==null) {
                    studentsOfOwner = new HashSet<Long>();
                    owner2students.put(ownerId, studentsOfOwner);
                }
                studentsOfOwner.add(studentId);
                Set<Exam> examsOfStudent = student2exams.get(studentId);
                if (examsOfStudent==null) { 
                    examsOfStudent = new HashSet<Exam>();
                    student2exams.put(studentId, examsOfStudent);
                }
                examsOfStudent.add(exams.get(examId));
                Long courseId = (Long)o[3];
                Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
                if (course2students == null) {
                	course2students = new Hashtable<Long, Set<Long>>();
                	owner2course2students.put(ownerId, course2students);
                }
                Set<Long> studentsOfCourse = course2students.get(courseId);
                if (studentsOfCourse == null) {
                	studentsOfCourse = new HashSet<Long>();
                	course2students.put(courseId, studentsOfCourse);
                }
                studentsOfCourse.add(studentId);
            }
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                        "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
	                        "Exam x inner join x.owners o, "+
	                        "StudentClassEnrollment e inner join e.clazz c " +
	                        "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
	                        "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
	                        "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeConfig+" and "+
	                        "o.ownerId=ioc.uniqueId", Object[].class)
	                		.setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
                Long examId = (Long)o[0];
                Long ownerId = (Long)o[1];
                Long studentId = (Long)o[2];
                Set<Long> studentsOfOwner = owner2students.get(ownerId);
                if (studentsOfOwner==null) {
                    studentsOfOwner = new HashSet<Long>();
                    owner2students.put(ownerId, studentsOfOwner);
                }
                studentsOfOwner.add(studentId);
                Set<Exam> examsOfStudent = student2exams.get(studentId);
                if (examsOfStudent==null) { 
                    examsOfStudent = new HashSet<Exam>();
                    student2exams.put(studentId, examsOfStudent);
                }
                examsOfStudent.add(exams.get(examId));
                Long courseId = (Long)o[3];
                Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
                if (course2students == null) {
                	course2students = new Hashtable<Long, Set<Long>>();
                	owner2course2students.put(ownerId, course2students);
                }
                Set<Long> studentsOfCourse = course2students.get(courseId);
                if (studentsOfCourse == null) {
                	studentsOfCourse = new HashSet<Long>();
                	course2students.put(courseId, studentsOfCourse);
                }
                studentsOfCourse.add(studentId);
            }
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                        "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
	                        "Exam x inner join x.owners o, "+
	                        "StudentClassEnrollment e inner join e.courseOffering co " +
	                        "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
	                        "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeCourse+" and "+
	                        "o.ownerId=co.uniqueId", Object[].class)
	                		.setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
                Long examId = (Long)o[0];
                Long ownerId = (Long)o[1];
                Long studentId = (Long)o[2];
                Set<Long> studentsOfOwner = owner2students.get(ownerId);
                if (studentsOfOwner==null) {
                    studentsOfOwner = new HashSet<Long>();
                    owner2students.put(ownerId, studentsOfOwner);
                }
                studentsOfOwner.add(studentId);
                Set<Exam> examsOfStudent = student2exams.get(studentId);
                if (examsOfStudent==null) { 
                    examsOfStudent = new HashSet<Exam>();
                    student2exams.put(studentId, examsOfStudent);
                }
                examsOfStudent.add(exams.get(examId));
                Long courseId = (Long)o[3];
                Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
                if (course2students == null) {
                	course2students = new Hashtable<Long, Set<Long>>();
                	owner2course2students.put(ownerId, course2students);
                }
                Set<Long> studentsOfCourse = course2students.get(courseId);
                if (studentsOfCourse == null) {
                	studentsOfCourse = new HashSet<Long>();
                	course2students.put(courseId, studentsOfCourse);
                }
                studentsOfCourse.add(studentId);
            }
            
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                        "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
	                        "Exam x inner join x.owners o, "+
	                        "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
	                        "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
	                        "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeOffering+" and "+
	                        "o.ownerId=io.uniqueId", Object[].class)
	                		.setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
                Long examId = (Long)o[0];
                Long ownerId = (Long)o[1];
                Long studentId = (Long)o[2];
                Set<Long> studentsOfOwner = owner2students.get(ownerId);
                if (studentsOfOwner==null) {
                    studentsOfOwner = new HashSet<Long>();
                    owner2students.put(ownerId, studentsOfOwner);
                }
                studentsOfOwner.add(studentId);
                Set<Exam> examsOfStudent = student2exams.get(studentId);
                if (examsOfStudent==null) { 
                    examsOfStudent = new HashSet<Exam>();
                    student2exams.put(studentId, examsOfStudent);
                }
                examsOfStudent.add(exams.get(examId));
                Long courseId = (Long)o[3];
                Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
                if (course2students == null) {
                	course2students = new Hashtable<Long, Set<Long>>();
                	owner2course2students.put(ownerId, course2students);
                }
                Set<Long> studentsOfCourse = course2students.get(courseId);
                if (studentsOfCourse == null) {
                	studentsOfCourse = new HashSet<Long>();
                	course2students.put(courseId, studentsOfCourse);
                }
                studentsOfCourse.add(studentId);
            }
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                    "select p.uniqueId, m from ClassEvent ce inner join ce.meetings m, ExamPeriod p " +
	                    "where p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
	                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId", Object[].class)
	                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeClass.intValue())
	                    .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId)
	                    .setCacheable(true).list()) {
                Long periodId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Meeting> meetings  = period2meetings.get(periodId);
                if (meetings==null) {
                    meetings = new HashSet(); period2meetings.put(periodId, meetings);
                }
                meetings.add(meeting);
            }
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                    "select p.uniqueId, m from CourseEvent ce inner join ce.meetings m, ExamPeriod p " +
	                    "where ce.reqAttendance=true and m.approvalStatus = 1 and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
	                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId", Object[].class)
	                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeCourse.intValue())
	                    .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId)
	                    .setCacheable(true).list()) {
                Long periodId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Meeting> meetings  = period2meetings.get(periodId);
                if (meetings==null) {
                    meetings = new HashSet(); period2meetings.put(periodId, meetings);
                }
                meetings.add(meeting);
            }
            for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
	                    "select p.uniqueId, m from ExamEvent ce inner join ce.meetings m, ExamPeriod p " +
	                    "where ce.exam.examType.uniqueId != :examTypeId and m.approvalStatus = 1 and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
	                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId", Object[].class)
	                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeCourse.intValue())
	                    .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId)
	                    .setCacheable(true).list()) {
                Long periodId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Meeting> meetings  = period2meetings.get(periodId);
                if (meetings==null) {
                    meetings = new HashSet(); period2meetings.put(periodId, meetings);
                }
                meetings.add(meeting);
            }
            p = new Parameters(sessionId, examTypeId);			
		}
		
	
        public TreeSet<ExamAssignmentInfo> getAssignedExams() {
			TreeSet<ExamAssignmentInfo> ret = new TreeSet();
            for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                    "select x from Exam x where " +
                    "x.examType.uniqueId=:examTypeId and "+
                    "x.session.uniqueId=:sessionId and x.assignedPeriod is not null", Exam.class).
                    setParameter("sessionId", sessionId).
                    setParameter("examTypeId", examTypeId).
                    setCacheable(true).list()) {
            	ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
                ret.add(info);
            }
            return ret;
		}
		
		public TreeSet<ExamAssignmentInfo> getAssignedExamsOfSubjectArea(Long subjectAreaId) {
			TreeSet<ExamAssignmentInfo> ret = new TreeSet();
            for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                    "select distinct x from Exam x inner join x.owners o where " +
                    "o.course.subjectArea.uniqueId=:subjectAreaId and "+
                    "x.examType.uniqueId=:examTypeId and "+
                    "x.session.uniqueId=:sessionId and x.assignedPeriod is not null", Exam.class).
                    setParameter("sessionId", sessionId).
                    setParameter("examTypeId", examTypeId).
                    setParameter("subjectAreaId", subjectAreaId).
                    setCacheable(true).list()) {
                ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
                ret.add(info);
            }
            return ret;
        }
		
		public TreeSet<ExamAssignmentInfo> getAssignedExamsOfLocation(Long locationId) throws Exception {
			TreeSet<ExamAssignmentInfo> ret = new TreeSet();
	        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
	                "select distinct x from Exam x inner join x.assignedRooms r where " +
	                "r.uniqueId=:locationId and x.assignedPeriod is not null and "+
	                "x.examType.uniqueId=:examTypeId", Exam.class).
	                setParameter("locationId", locationId).
	                setParameter("examTypeId", examTypeId).
	                setCacheable(true).list()) {
                ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
                ret.add(info);
	        } 
	        return ret;
	    }
		
		public TreeSet<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId) throws Exception {
			TreeSet<ExamAssignmentInfo> ret = new TreeSet();
	        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
	                "select distinct x from Exam x inner join x.instructors i where " +
	                "i.uniqueId=:instructorId and x.assignedPeriod is not null and "+
	                "x.examType.uniqueId=:examTypeId", Exam.class).
	                setParameter("instructorId", instructorId).
	                setParameter("examTypeId", examTypeId).
	                setCacheable(true).list()) {
	        	ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
                ret.add(info);
	        } 
	        return ret;
	    }
	}
	
    public static TreeSet<ExamAssignmentInfo> findAssignedExams(Long sessionId, Long subjectAreaId, Long examTypeId) throws Exception {
    	CacheAssignedExams cache = new CacheAssignedExams(sessionId, examTypeId);
        if (subjectAreaId==null || subjectAreaId<0) {
        	return cache.getAssignedExams();
        } else {
        	return cache.getAssignedExamsOfSubjectArea(subjectAreaId);
        }
    }
	
	public boolean match(ExamAssignmentReportForm form, String name) {
	    if (form.getFilter()==null || form.getFilter().trim().length()==0) return true;
	    String n = (name==null?"":name).toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(form.getFilter().toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*")+"\\s";
                        if (!Pattern.compile(tokenRegExp).matcher(" "+n+" ").find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;
	}
	
	public boolean match(ExamAssignmentReportForm form, ExamAssignment exam) {
	    if (exam==null) return false;
	    if (form.getShowSections()) {
	        for (ExamSectionInfo section : exam.getSections())
	            if (match(form, section.getName())) return true;
	        return false;
	    } else {
	        return match(form, exam.getExamName());
	    }
	}
	
	public PdfWebTable getTable(Long sessionId, boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        if (exams==null || exams.isEmpty()) return null;
        try {
            switch (ExamReport.valueOf(form.getReport())) {
            case ExamAssignmentReport:
    		    return generateAssignmentReport(html, form, exams);
            case RoomAssignmentReport:
    		    return generateRoomReport(html, form, exams);
            case PeriodUsage:
                return generatePeriodUsageReport(html, form, exams, sessionId);
            case NrExamsADay:
    		    return generateNrExamsADayReport(html, form, exams);
            case RoomSplits:
    		    return generateRoomSplitReport(html, form, exams);
            case ViolatedDistributions:
    		    return generateViolatedDistributionsReport(html, form, exams);
            case IndividualStudentConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, true, true, true, true, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualDirectStudentConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, true, true, false, false, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualBackToBackStudentConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, true, false, false, true, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualMore2ADayStudentConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, true, false, true, false, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualInstructorConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, false, true, true, true, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualDirectInstructorConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, false, true, false, false, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualBackToBackInstructorConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, false, false, false, true, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualMore2ADayInstructorConflicts:
                return generateIndividualConflictsReport(html, sessionId, form, exams, false, false, true, false, UserProperty.NameFormat.get(sessionContext.getUser()));
            case DirectStudentConflicts:
                return generateDirectConflictsReport(html, form, exams, true);
            case BackToBackStudentConflicts:
                return generateBackToBackConflictsReport(html, form, exams, true);
            case More2ADayStudentConflicts:
                return generate2MoreADayConflictsReport(html, form, exams, true);
            case DirectInstructorConflicts:
                return generateDirectConflictsReport(html, form, exams, false);
            case BackToBackInstructorConflicts:
                return generateBackToBackConflictsReport(html, form, exams, false);
            case More2ADayInstructorConflicts:
                return generate2MoreADayConflictsReport(html, form, exams, false);
            case IndividualStudentSchedule:
                return generateIndividualAssignmentReport(html, sessionId, form, exams, true, UserProperty.NameFormat.get(sessionContext.getUser()));
            case IndividualInstructorSchedule:
                return generateIndividualAssignmentReport(html, sessionId, form, exams, false, UserProperty.NameFormat.get(sessionContext.getUser()));
            case Statistics:
                return generateStatisticsReport(html, sessionId, form, exams);
            default:
            	return null;
            }
        } catch (IllegalArgumentException e) {
        	return null;
        }
    }
	
	private PdfWebTable generateAssignmentReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
	    String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 10,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.colEnrollment(),
                    MSG.colSeatingType().replace("\n",nl),
                    MSG.colDate(),
                    MSG.colTime(),
                    MSG.colRoom(),
                    MSG.colRoomCapacity(),
                    MSG.colInstructor(),
                    MSG.colStudentConflicts().replace("\n",nl),
                    MSG.colInstructorConflicts().replace("\n",nl)},
                new String[] {"left", "right", "center", "left", "left", "left", "right", "left", "center", "center"},
                new boolean[] {true, true, true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        for (ExamAssignmentInfo exam : exams) {
            if (form.getShowSections()) {
                boolean firstSection = true; 
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!match(form, section.getName())) continue;
                    int idc = exam.getNrInstructorDirectConflicts(section);
                    String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                    int im2d = exam.getNrInstructorMoreThanTwoConflicts(section);
                    String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                    int ibtb = exam.getNrInstructorBackToBackConflicts(section);
                    int idbtb = exam.getNrInstructorDistanceBackToBackConflicts(section);
                    String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                    int dc = exam.getNrDirectConflicts(section);
                    String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                    int m2d = exam.getNrMoreThanTwoConflicts(section);
                    String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                    int btb = exam.getNrBackToBackConflicts(section);
                    int dbtb = exam.getNrDistanceBackToBackConflicts(section);
                    String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));

                    table.addLine(
                            "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                            new String[] {
                                (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                String.valueOf(section.getNrStudents()),
                                exam.getSeatingTypeLabel(),
                                exam.getDate(html),
                                exam.getTime(html),
                                exam.getRoomsName(html,", "),
                                exam.getRoomsCapacity(html, ", "),
                                exam.getInstructorName("; "),
                                (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                            },
                            new Comparable[] {
                                new MultiComparable(section.getName(), exam),
                                new MultiComparable(-section.getNrStudents(), section.getName(), exam),
                                new MultiComparable(exam.getSeatingType(), section.getName(), exam),
                                new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                                new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), section.getName(), exam),
                                new MultiComparable(exam.getRoomsName(":"), section.getName(), exam),
                                new MultiComparable(-exam.getRoomsCapacity(), section.getName(), exam),
                                new MultiComparable(exam.getInstructorName(":"), section.getName(), exam),
                                new MultiComparable(-dc,-m2d,-btb,-dbtb,section.getName(),exam),
                                new MultiComparable(-idc,-im2d,-ibtb,-idbtb,section.getName(),exam)
                            },
                            (firstSection?exam.getExamId().toString():null));
                    firstSection = false;
                }
            } else {
                if (!match(form, exam.getExamName())) continue;
                int idc = exam.getNrInstructorDirectConflicts();
                String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                int im2d = exam.getNrInstructorMoreThanTwoConflicts();
                String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                int ibtb = exam.getNrInstructorBackToBackConflicts();
                int idbtb = exam.getNrInstructorDistanceBackToBackConflicts();
                String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                int dc = exam.getNrDirectConflicts();
                String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                int m2d = exam.getNrMoreThanTwoConflicts();
                String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                int btb = exam.getNrBackToBackConflicts();
                int dbtb = exam.getNrDistanceBackToBackConflicts();
                String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                
                table.addLine(
                        "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                        new String[] {
                            (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                            String.valueOf(exam.getNrStudents()),
                            exam.getSeatingTypeLabel(),
                            exam.getDate(html),
                            exam.getTime(html),
                            exam.getRoomsName(html,", "),
                            exam.getRoomsCapacity(html, ", "),
                            exam.getInstructorName("; "),
                            (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                            (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                        },
                        new Comparable[] {
                            exam,
                            new MultiComparable(exam.getNrStudents(), exam),
                            new MultiComparable(exam.getSeatingType(), exam),
                            new MultiComparable(exam.getPeriodOrd(), exam),
                            new MultiComparable(exam.getPeriod().getStartSlot(), exam),
                            new MultiComparable(exam.getRoomsName(":"), exam),
                            new MultiComparable(-exam.getRoomsCapacity(), exam),
                            new MultiComparable(exam.getInstructorName(":"), exam),
                            new MultiComparable(-dc,-m2d,-btb,-dbtb,exam),
                            new MultiComparable(-idc,-im2d,-ibtb,-idbtb,exam)
                        },
                        exam.getExamId().toString());
            }
        }
        return table;	    
	}
	
	private PdfWebTable generateRoomReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 11,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    MSG.colRoom(),
                    MSG.colRoomCapacity(),
                    MSG.colExamCapacity().replace("\n",nl),
                    MSG.colDate(),
                    MSG.colTime(),
                    (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.colEnrollment(),
                    MSG.colSeatingType().replace("\n",nl),
                    MSG.colInstructor(),
                    MSG.colStudentConflicts().replace("\n",nl),
                    MSG.colInstructorConflicts().replace("\n",nl)},
                new String[] {"left", "right", "right", "left", "left", "left", "right", "center", "left", "center", "center"},
                new boolean[] {true, true, true, true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        for (ExamAssignmentInfo exam : exams) {
            boolean match = false;
            for (ExamRoomInfo room : exam.getRooms()) {
                if (match(form,room.getName())) { match = true; break; }
            }
            if (!match) continue;
            if (form.getShowSections()) {
                for (ExamSectionInfo section : exam.getSections()) {
                    int idc = exam.getNrInstructorDirectConflicts(section);
                    String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                    int im2d = exam.getNrInstructorMoreThanTwoConflicts(section);
                    String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                    int ibtb = exam.getNrInstructorBackToBackConflicts(section);
                    int idbtb = exam.getNrInstructorDistanceBackToBackConflicts(section);
                    String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                    int dc = exam.getNrDirectConflicts(section);
                    String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                    int m2d = exam.getNrMoreThanTwoConflicts(section);
                    String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                    int btb = exam.getNrBackToBackConflicts(section);
                    int dbtb = exam.getNrDistanceBackToBackConflicts(section);
                    String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                    
                    boolean firstRoom = true;
                    for (ExamRoomInfo room : exam.getRooms()) {
                        if (!match(form,room.getName())) continue;
                        table.addLine(
                                "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    room.getName(),
                                    String.valueOf(room.getCapacity()),
                                    String.valueOf(room.getExamCapacity()),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                    String.valueOf(section.getNrStudents()),
                                    exam.getSeatingTypeLabel(),
                                    exam.getInstructorName("; "),
                                    (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                    (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                                },
                                new Comparable[] {
                                    new MultiComparable(room.getName()),
                                    new MultiComparable(-room.getCapacity(), room.getName()),
                                    new MultiComparable(-room.getExamCapacity(), room.getName()),
                                    new MultiComparable(room.getName(), exam.getPeriodOrd(), section.getName(), exam),
                                    new MultiComparable(room.getName(), exam.getPeriod().getStartSlot(), section.getName(), exam),
                                    new MultiComparable(room.getName(), section.getName(), exam),
                                    new MultiComparable(room.getName(), -section.getNrStudents(), section.getName(), exam),
                                    new MultiComparable(room.getName(), exam.getSeatingType(), section.getName(), exam),
                                    new MultiComparable(room.getName(), exam.getInstructorName(":"), section.getName(), exam),
                                    new MultiComparable(room.getName(), -dc,-m2d,-btb,-dbtb,section.getName(),exam),
                                    new MultiComparable(room.getName(), -idc,-im2d,-ibtb,-idbtb,section.getName(),exam)
                                },
                                (firstRoom?exam.getExamId().toString():null));
                        firstRoom = false;
                    }
                }
            } else {
                int idc = exam.getNrInstructorDirectConflicts();
                String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                int im2d = exam.getNrInstructorMoreThanTwoConflicts();
                String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                int ibtb = exam.getNrInstructorBackToBackConflicts();
                int idbtb = exam.getNrInstructorDistanceBackToBackConflicts();
                String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                int dc = exam.getNrDirectConflicts();
                String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                int m2d = exam.getNrMoreThanTwoConflicts();
                String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                int btb = exam.getNrBackToBackConflicts();
                int dbtb = exam.getNrDistanceBackToBackConflicts();
                String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                
                boolean firstRoom = true;
                for (ExamRoomInfo room : exam.getRooms()) {
                    if (!match(form,room.getName())) continue;
                    table.addLine(
                            "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                            new String[] {
                                room.getName(),
                                String.valueOf(room.getCapacity()),
                                String.valueOf(room.getExamCapacity()),
                                exam.getDate(html),
                                exam.getTime(html),
                                (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                                String.valueOf(exam.getNrStudents()),
                                exam.getSeatingTypeLabel(),
                                exam.getInstructorName("; "),
                                (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                            },
                            new Comparable[] {
                                new MultiComparable(room.getName()),
                                new MultiComparable(-room.getCapacity(), room.getName()),
                                new MultiComparable(-room.getExamCapacity(), room.getName()),
                                new MultiComparable(room.getName(), exam.getPeriodOrd(), exam),
                                new MultiComparable(room.getName(), exam.getPeriod().getStartSlot(), exam),
                                new MultiComparable(room.getName(), exam),
                                new MultiComparable(room.getName(), -exam.getNrStudents(), exam),
                                new MultiComparable(room.getName(), exam.getSeatingType(), exam),
                                new MultiComparable(room.getName(), exam.getInstructorName(":"), exam),
                                new MultiComparable(room.getName(), -dc,-m2d,-btb,-dbtb,exam),
                                new MultiComparable(room.getName(), -idc,-im2d,-ibtb,-idbtb,exam)
                            },
                            (firstRoom?exam.getExamId().toString():null));
                    firstRoom = false;
                }
            }
        }
        return table;	    
	}
	
	private PdfWebTable generatePeriodUsageReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams, Long sessionId) {
	    String nl = (html?"<br>":"\n");
	    PdfWebTable table = new PdfWebTable( 8,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    MSG.colDate(),
                    MSG.colTime(),
                    (form.getShowSections()?MSG.colOwners():MSG.colExaminations()),
                    MSG.colTotalEnrollment(),
                    (form.getShowSections()?MSG.colOwners():MSG.colExaminations())+nl+MSG.withNOrMoreStudents(10),
                    (form.getShowSections()?MSG.colOwners():MSG.colExaminations())+nl+MSG.withNOrMoreStudents(50),
                    (form.getShowSections()?MSG.colOwners():MSG.colExaminations())+nl+MSG.withNOrMoreStudents(100),
                    (form.getShowSections()?MSG.colOwners():MSG.colExaminations())+nl+MSG.withNOrMoreStudents(500)
                    },
                new String[] {"left","left","right","right","right","right","right","right"},
                new boolean[] {true, true, true, true, true, true, true, true} );
        int tnrExams = 0, tnrStudents = 0, tnrExams10=0, tnrExams50=0, tnrExams100=0, tnrExams500=0;
        table.setRowStyle("white-space:nowrap");
        for (Iterator i=ExamPeriod.findAll(sessionId, form.getExamType()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            String periodDate = period.getStartDateLabel();
            String periodTime = period.getStartTimeLabel()+" - "+period.getEndTimeLabel();
            if (html && period.getPrefLevel()!=null && !PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog())) {
                periodDate = "<font color='"+PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog())+"'>"+periodDate+"</font>";
                periodTime = "<font color='"+PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog())+"'>"+periodTime+"</font>";
            }
            int nrExams = 0, nrStudents = 0, nrExams10=0, nrExams50=0, nrExams100=0, nrExams500=0;
            for (ExamAssignmentInfo exam : exams) {
                if (!period.getUniqueId().equals(exam.getPeriodId())) continue;
                if (form.getShowSections()) {
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!match(form,section.getName())) continue;
                        nrExams++;
                        nrStudents+=section.getNrStudents();
                        if (section.getNrStudents()>=10) nrExams10++;
                        if (section.getNrStudents()>=50) nrExams50++;
                        if (section.getNrStudents()>=100) nrExams100++;
                        if (section.getNrStudents()>=500) nrExams500++;
                    }
                } else {
                    if (!match(form,exam.getExamName())) continue;
                    nrExams++;
                    int nrStudentsThisExam = exam.getStudentIds().size(); 
                    nrStudents+=nrStudentsThisExam;
                    if (nrStudentsThisExam>=10) nrExams10++;
                    if (nrStudentsThisExam>=50) nrExams50++;
                    if (nrStudentsThisExam>=100) nrExams100++;
                    if (nrStudentsThisExam>=500) nrExams500++;
                }
            }
            if (nrExams==0) continue;
            table.addLine(
                    new String[] {
                            periodDate,
                            periodTime,
                            String.valueOf(nrExams),
                            String.valueOf(nrStudents),
                            String.valueOf(nrExams10),
                            String.valueOf(nrExams50),
                            String.valueOf(nrExams100),
                            String.valueOf(nrExams500)},
                    new Comparable[] {
                            new MultiComparable(0,period),
                            new MultiComparable(0,period.getStartSlot(), period.getDateOffset(), period),
                            new MultiComparable(0,nrExams),
                            new MultiComparable(0,nrStudents),
                            new MultiComparable(0,nrExams10),
                            new MultiComparable(0,nrExams50),
                            new MultiComparable(0,nrExams100),
                            new MultiComparable(0,nrExams500)
                    });
            tnrExams += nrExams;
            tnrExams10 += nrExams10;
            tnrExams50 += nrExams50;
            tnrExams100 += nrExams100;
            tnrExams500 += nrExams500;
            tnrStudents += nrStudents;
        }
        table.addLine(
                new String[] {
                        (html?"<b>"+MSG.colTotals()+"</b>":MSG.colTotals()),
                        "",
                        (html?"<b>"+tnrExams+"</b>":String.valueOf(tnrExams)),
                        (html?"<b>"+tnrStudents+"</b>":String.valueOf(tnrStudents)),
                        (html?"<b>"+tnrExams10+"</b>":String.valueOf(tnrExams10)),
                        (html?"<b>"+tnrExams50+"</b>":String.valueOf(tnrExams50)),
                        (html?"<b>"+tnrExams100+"</b>":String.valueOf(tnrExams100)),
                        (html?"<b>"+tnrExams500+"</b>":String.valueOf(tnrExams500))},
                new Comparable[] {
                        new MultiComparable(1,null),
                        new MultiComparable(1,0,0, null),
                        new MultiComparable(1,tnrExams),
                        new MultiComparable(1,tnrStudents),
                        new MultiComparable(1,tnrExams10),
                        new MultiComparable(1,tnrExams50),
                        new MultiComparable(1,tnrExams100),
                        new MultiComparable(1,tnrExams500)
                });
        return table;	    
	}
	
	private PdfWebTable generateNrExamsADayReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    MSG.colDate(),
                    MSG.colStudentsWithNoExam().replace("\n",nl),
                    MSG.colStudentsWithOneExam().replace("\n",nl),
                    MSG.colStudentsWithTwoExams().replace("\n",nl),
                    MSG.colStudentsWithThreeExams().replace("\n",nl),
                    MSG.colStudentsWithFourOrMoreExams().replace("\n",nl),
                    MSG.colStudentBTBExams().replace("\n",nl),
                    MSG.colStudentDistanceBTBExams().replace("\n",nl)},
                new String[] {"left", "right", "right", "right", "right", "right", "right", "right", "right"},
                new boolean[] {true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        HashSet<Long> studentIds = new HashSet<Long>();
        Hashtable<Date,Hashtable<Long,Integer>> date2students = new Hashtable();
        Hashtable<Date,Integer> date2btb = new Hashtable();
        Hashtable<Date,Integer> date2dbtb = new Hashtable();
        for (ExamAssignmentInfo exam : exams) {
            if (!form.getShowSections() && !match(form,exam.getExamName())) continue;
            Hashtable<Long,Integer> students = date2students.get(exam.getPeriod().getStartDate());
            if (students==null) {
                students = new Hashtable<Long, Integer>(); date2students.put(exam.getPeriod().getStartDate(),students);
            }
            for (ExamSectionInfo section : exam.getSections()) {
                if (form.getShowSections() && !match(form,section.getName())) continue;
                studentIds.addAll(section.getStudentIds());
                for (Long studentId : section.getStudentIds()) {
                    Integer nrExamsThisDay = students.get(studentId);
                    students.put(studentId, 1+(nrExamsThisDay==null?0:nrExamsThisDay));
                }
                int btb = 0, dbtb = 0;
                for (Iterator i=exam.getBackToBackConflicts().iterator();i.hasNext();) {
                    BackToBackConflict conf = (BackToBackConflict)i.next();
                    if (exam.getPeriod().compareTo(conf.getOtherExam().getPeriod())>=0) continue;
                    if (form.getShowSections() && form.getFilter()!=null && form.getFilter().trim().length()>0) {
                        for (Enumeration e=conf.getStudents().elements();e.hasMoreElements();) {
                            Long studentId = (Long)e.nextElement();
                            if (section.getStudentIds().contains(studentId)) {
                                btb++;
                                if (conf.isDistance()) dbtb++;
                            }
                        }
                    } else {
                        btb += conf.getNrStudents();
                        if (conf.isDistance()) dbtb += conf.getNrStudents(); 
                    }
                }
                if (btb>0)
                    date2btb.put(exam.getPeriod().getStartDate(), btb + (date2btb.get(exam.getPeriod().getStartDate())==null?0:date2btb.get(exam.getPeriod().getStartDate())));
                if (dbtb>0)
                    date2dbtb.put(exam.getPeriod().getStartDate(), dbtb + (date2dbtb.get(exam.getPeriod().getStartDate())==null?0:date2dbtb.get(exam.getPeriod().getStartDate())));
            }
        }
        int tNoExam = 0, tOneExam = 0, tTwoExams = 0, tThreeExams = 0, tFourExams = 0, tBtb = 0, tDistBtb = 0;
        for (Map.Entry<Date,Hashtable<Long,Integer>> entry : date2students.entrySet()) {
            int noExam = 0, oneExam = 0, twoExams = 0, threeExams = 0, fourExams = 0, btb = 0, dbtb = 0;
            for (Map.Entry<Long, Integer> student : entry.getValue().entrySet()) {
                if (student.getValue()==1) oneExam ++;
                else if (student.getValue()==2) twoExams ++;
                else if (student.getValue()==3) threeExams ++;
                else if (student.getValue()>=4) fourExams ++;
            }
            noExam = studentIds.size() - oneExam - twoExams - threeExams - fourExams;
            btb = (date2btb.get(entry.getKey())==null?0:date2btb.get(entry.getKey()));
            dbtb = (date2dbtb.get(entry.getKey())==null?0:date2dbtb.get(entry.getKey()));
            table.addLine(
                    new String[] {
                            df.format(entry.getKey()),
                            String.valueOf(noExam),
                            String.valueOf(oneExam),
                            String.valueOf(twoExams),
                            String.valueOf(threeExams),
                            String.valueOf(fourExams),
                            String.valueOf(btb),
                            String.valueOf(dbtb)},
                    new Comparable[] {
                            new MultiComparable(0,entry.getKey()),
                            new MultiComparable(0,noExam),
                            new MultiComparable(0,oneExam),
                            new MultiComparable(0,twoExams),
                            new MultiComparable(0,threeExams),
                            new MultiComparable(0,fourExams),
                            new MultiComparable(0,btb),
                            new MultiComparable(0,dbtb)
                    });
            tNoExam += noExam;
            tOneExam += oneExam;
            tTwoExams += twoExams;
            tThreeExams += threeExams;
            tFourExams += fourExams;
            tBtb += btb;
            tDistBtb += dbtb;
        }
        table.addLine(
                new String[] {
                        (html?"<b>"+MSG.colTotals()+"</b>":MSG.colTotals()),
                        (html?"<b>"+tNoExam+"</b>":String.valueOf(tNoExam)),
                        (html?"<b>"+tOneExam+"</b>":String.valueOf(tOneExam)),
                        (html?"<b>"+tTwoExams+"</b>":String.valueOf(tTwoExams)),
                        (html?"<b>"+tThreeExams+"</b>":String.valueOf(tThreeExams)),
                        (html?"<b>"+tFourExams+"</b>":String.valueOf(tFourExams)),
                        (html?"<b>"+tBtb+"</b>":String.valueOf(tBtb)),
                        (html?"<b>"+tDistBtb+"</b>":String.valueOf(tDistBtb))},
                new Comparable[] {
                        new MultiComparable(1,null),
                        new MultiComparable(1,tNoExam),
                        new MultiComparable(1,tOneExam),
                        new MultiComparable(1,tTwoExams),
                        new MultiComparable(1,tThreeExams),
                        new MultiComparable(1,tFourExams),
                        new MultiComparable(1,tBtb),
                        new MultiComparable(1,tDistBtb)
                });
        return table;	    
	}
	
	private PdfWebTable generateRoomSplitReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        String nl = (html?"<br>":"\n");
        List<String> colNames = new ArrayList<String>(
        		Arrays.asList(new String[] {
                        (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                        MSG.colEnrollment(),
                        MSG.colSeatingType().replace("\n",nl),
                        MSG.colDate(),
                        MSG.colTime(),
                        MSG.colAverageDistance().replace("\n",nl)}));
        List<String> colCmp = new ArrayList<String>(
        		Arrays.asList(new String[] {"left","left","center","left","left","left"}));
        List<Boolean> colOrd = new ArrayList<Boolean>(
        		Arrays.asList(new Boolean[] {true, true, true, true, true, true}));
        int maxSplits = 2;
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getRooms() !=null && exam.getRooms().size() > maxSplits)
            	maxSplits = exam.getRooms().size();
        }
        for (int i = 1; i <= maxSplits; i++) {
        	String pos;
        	switch (i) {
        		case 1: pos = MSG.col1stExam(); break;
        		case 2: pos = MSG.col2ndExam(); break;
        		case 3: pos = MSG.col3rdExam(); break;
        		default: pos = MSG.colNthExam(i); break;
        	}
        	colNames.add(pos + " " + MSG.colRoom());
        	colNames.add(pos + " " + MSG.colRoom() + nl + MSG.colRoomCapacity());
        	colCmp.add("left"); colCmp.add("left");
        	colOrd.add(true); colOrd.add(true);
        }
        boolean[] ord = new boolean[colOrd.size()];
        for (int i = 0; i < colOrd.size(); i++) ord[i] = colOrd.get(i);
        PdfWebTable table = new PdfWebTable( colNames.size(),
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                colNames.toArray(new String[colNames.size()]), colCmp.toArray(new String[colCmp.size()]), ord);
        table.setRowStyle("white-space:nowrap");
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getRooms()==null || exam.getRooms().size()<=1) continue;
            if (form.getShowSections()) {
                boolean firstSection = true; 
                for (ExamSectionInfo section : exam.getSections()) {
                    if (!match(form, section.getName())) continue;
                    double distance = 0;
                    for (ExamRoomInfo r1 : exam.getRooms())
                        for (ExamRoomInfo r2 : exam.getRooms())
                            if (r1.getLocationId().compareTo(r2.getLocationId())<0) distance += r1.getDistance(r2);
                    distance /= exam.getRooms().size() * (exam.getRooms().size() - 1) / 2;
                    List<String> line = new ArrayList<String>(colNames.size());
                    line.add((html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""));
                    line.add(String.valueOf(section.getNrStudents()));
                    line.add(exam.getSeatingTypeLabel());
                    line.add(exam.getDate(html));
                    line.add(exam.getTime(html));
                    line.add(((int)(distance*10.0)==0?"":(int)(distance*10.0)+" m"));
                    List<Comparable> cmp = new ArrayList<Comparable>(colNames.size());
                    cmp.add(new MultiComparable(section.getName(), exam));
                    cmp.add(new MultiComparable(exam.getNrStudents(), section.getName(), exam));
                    cmp.add(new MultiComparable(exam.getSeatingType(), section.getName(), exam));
                    cmp.add(new MultiComparable(exam.getPeriodOrd(), section.getName(), exam));
                    cmp.add(new MultiComparable(exam.getPeriod().getStartSlot(), section.getName(), exam));
                    cmp.add(new MultiComparable(-distance, section.getName(), exam));
                    for (ExamRoomInfo room: exam.getRooms()) {
                    	line.add(html?room.toString():room.getName());
                    	line.add(html?"<font color='"+PreferenceLevel.int2color(room.getPreference())+"'>"+room.getCapacity(exam)+"</font>":String.valueOf(room.getCapacity(exam)));
                    	cmp.add(new MultiComparable(room.getName(), section.getName(), exam));
                    	cmp.add(new MultiComparable(room.getCapacity(exam), section.getName(), exam));
                    }
                    for (int i = exam.getRooms().size(); i < maxSplits; i++) {
                    	line.add(""); line.add("");
                    	cmp.add(new MultiComparable("", section.getName(), exam));
                    	cmp.add(new MultiComparable(0, section.getName(), exam));
                    }
                    table.addLine(
                            "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                            line.toArray(new String[line.size()]),
                            cmp.toArray(new Comparable[cmp.size()]),
                            (firstSection?exam.getExamId().toString():null));
                    firstSection = false;
                }
            } else {
                if (!match(form, exam.getExamName())) continue;
                double distance = 0;
                for (ExamRoomInfo r1 : exam.getRooms())
                    for (ExamRoomInfo r2 : exam.getRooms())
                        if (r1.getLocationId().compareTo(r2.getLocationId())<0) distance += r1.getDistance(r2);
                distance /= exam.getRooms().size() * (exam.getRooms().size() - 1) / 2;
                List<String> line = new ArrayList<String>(colNames.size());
                line.add((html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""));
                line.add(String.valueOf(exam.getNrStudents()));
                line.add(exam.getSeatingTypeLabel());
                line.add(exam.getDate(html));
                line.add(exam.getTime(html));
                line.add(((int)(distance*10.0)==0?"":(int)(distance*10.0)+" m"));
                List<Comparable> cmp = new ArrayList<Comparable>(colNames.size());
                cmp.add(new MultiComparable(exam));
                cmp.add(new MultiComparable(exam.getNrStudents(), exam));
                cmp.add(new MultiComparable(exam.getSeatingType(), exam));
                cmp.add(new MultiComparable(exam.getPeriodOrd(), exam));
                cmp.add(new MultiComparable(exam.getPeriod().getStartSlot(), exam));
                cmp.add(new MultiComparable(-distance, exam));
                for (ExamRoomInfo room: exam.getRooms()) {
                	line.add(html?room.toString():room.getName());
                	line.add(html?"<font color='"+PreferenceLevel.int2color(room.getPreference())+"'>"+room.getCapacity(exam)+"</font>":String.valueOf(room.getCapacity(exam)));
                	cmp.add(new MultiComparable(room.getName(), exam));
                	cmp.add(new MultiComparable(room.getCapacity(exam), exam));
                }
                for (int i = exam.getRooms().size(); i < maxSplits; i++) {
                	line.add(""); line.add("");
                	cmp.add(new MultiComparable("", exam));
                	cmp.add(new MultiComparable(0, exam));
                }
                table.addLine(
                        "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                        line.toArray(new String[line.size()]),
                        cmp.toArray(new Comparable[cmp.size()]),
                        exam.getExamId().toString());
            }
        }
        return table;	    
	}
	
	private PdfWebTable generateViolatedDistributionsReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    MSG.colPreference(),
                    MSG.colDistribution(),
                    (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.colEnrollment(),
                    MSG.colSeatingType().replace("\n",nl),
                    MSG.colDate(),
                    MSG.colTime(),
                    MSG.colRoom()},
                new String[] {"left","left","left","right","center","left","left","left"},
                new boolean[] {true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        HashSet<DistributionConflict> conflicts = new HashSet();
        for (ExamAssignmentInfo exam : exams) {
            if (!match(form, exam)) continue;
            for (DistributionConflict conf : exam.getDistributionConflicts()) {
                if (conflicts.contains(conf)) continue;
                conf.getOtherExams().add(exam); conflicts.add(conf);
            }
        }
        for (DistributionConflict conf : conflicts) {
            String classes = "", enrollment = "", seating = "";
            String date = "", time = "", room = "";
            int idx = 0;
            Date[] dates = new Date[conf.getOtherExams().size()]; 
            Integer[] times = new Integer[conf.getOtherExams().size()];
            int enrl = 0;
            for (ExamInfo exam:conf.getOtherExams()) {
                enrl += exam.getNrStudents();
                if (form.getShowSections()) {
                    if (exam instanceof ExamAssignment) {
                        ExamAssignment ea = (ExamAssignment)exam;
                        dates[idx] = ea.getPeriod().getStartTime();
                        times[idx] = ea.getPeriod().getStartSlot();
                    } else {
                        dates[idx] = new Date(0);
                        times[idx] = -1;
                    }
                    idx++;
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl;
                            date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += section.getNrStudents();
                        if (firstSection) {
                            seating += exam.getSeatingTypeLabel();
                            if (exam instanceof ExamAssignment) {
                                ExamAssignment ea = (ExamAssignment)exam;
                                date += ea.getDate(html);
                                time += ea.getTime(html);
                                room += ea.getRoomsName(html, ", ");
                            }
                        }
                        firstSection = false;
                    }
                } else {
                    if (classes.length()>0) {
                        classes += nl; enrollment += nl; seating += nl;
                        date += nl; time += nl; room += nl;
                    }
                    classes += exam.getExamName();
                    enrollment += exam.getNrStudents();
                    seating += exam.getSeatingTypeLabel();
                    if (exam instanceof ExamAssignment) {
                        ExamAssignment ea = (ExamAssignment)exam;
                        date += ea.getDate(html);
                        time += ea.getTime(html);
                        room += ea.getRoomsName(html, ", ");
                    }
                }
            }
            table.addLine(
                    new String[] {
                        (html?"<font color='"+PreferenceLevel.prolog2color(conf.getPreference())+"'>":"")+PreferenceLevel.prolog2string(conf.getPreference())+(html?"</font>":""),
                        (html?"<font color='"+PreferenceLevel.prolog2color(conf.getPreference())+"'>":"")+conf.getType()+(html?"</font>":""),
                        classes,
                        enrollment,
                        seating,
                        date,
                        time,
                        room
                    },
                    new Comparable[] {
                        new MultiComparable(conf),
                        new MultiComparable(conf.getType(), conf),
                        new MultiComparable(classes, conf),
                        new MultiComparable(-enrl, conf),
                        new MultiComparable(seating, conf),
                        new MultiComparable(dates),
                        new MultiComparable(times),
                        new MultiComparable(room, conf)
                    });
        }
        return table;
	}
	
	private PdfWebTable generateIndividualConflictsReport(boolean html, Long sessionId, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams, boolean studentConf, boolean direct, boolean m2d, boolean btb, String nameFormat) {
	    Hashtable<Long, Student> students = new Hashtable();
	    if (studentConf) {
            HashSet<Long> allStudentIds = new HashSet();
            for (ExamAssignmentInfo exam : exams) {
                if (direct) for (DirectConflict conflict : exam.getDirectConflicts()) {
                    allStudentIds.addAll(conflict.getStudents());
                }
                if (btb) for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    allStudentIds.addAll(conflict.getStudents());
                }
                if (m2d) for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                    allStudentIds.addAll(conflict.getStudents());
                }
            }
            String inSet = null; int idx = 0;
            for (Iterator i=allStudentIds.iterator();i.hasNext();idx++) {
                if (idx==1000) {
                    for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                        students.put(s.getUniqueId(), s);
                    }
                    idx = 0; inSet = null;
                }
                if (inSet==null)
                    inSet = i.next().toString();
                else
                    inSet += ","+i.next();
            }
            if (inSet!=null) {
            	for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                    students.put(s.getUniqueId(), s);
                }
            }
	    }
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 10,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    (studentConf?MSG.colStudentId():MSG.colInstructorId()),
                    MSG.colStudentOrInstructorName(),
                    MSG.colType(),
                    (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.colEnrollment(),
                    MSG.colSeatingType().replace("\n",nl),
                    MSG.colDate(),
                    MSG.colTime(),
                    MSG.colRoom(),
                    MSG.colDistance()},
                new String[] {"left","left","left","left","right","center","left","left","left","left"},
                new boolean[] {true, true, true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        for (ExamAssignmentInfo exam : exams) {
            if (direct)
                for (DirectConflict conflict : (studentConf?exam.getDirectConflicts():exam.getInstructorDirectConflicts())) {
                    if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                    for (Long studentId : conflict.getStudents()) {
                        String id = "", name = "";
                        if (studentConf) {
                            Student student = students.get(studentId);
                            id = student.getExternalUniqueId();
                            name = student.getName(nameFormat);
                        } else {
                            DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(studentId);
                            id = instructor.getExternalUniqueId();
                            name = instructor.getName(nameFormat);
                        }
                        if (!match(form,id) && !match(form,name)) continue;
                        if (form.getShowSections()) {
                            String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                            boolean firstSection = true;
                            for (ExamSectionInfo section : exam.getSections()) {
                                if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                                if (classes.length()>0) {
                                    classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                }
                                classes += section.getName();
                                enrollment += String.valueOf(section.getNrStudents());
                                if (firstSection) {
                                    seating += exam.getSeatingTypeLabel();
                                    date += exam.getDate(html);
                                    time += exam.getTime(html);
                                    room += exam.getRoomsName(html, ", ");
                                }
                                firstSection = false;
                            }
                            firstSection = true;
                            if (conflict.getOtherExam()!=null) {
                                for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                                    if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                                    if (classes.length()>0) {
                                        classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                    }
                                    classes += section.getName();
                                    enrollment += String.valueOf(section.getNrStudents());
                                    if (firstSection) {
                                        seating += conflict.getOtherExam().getSeatingTypeLabel();
                                        room += conflict.getOtherExam().getRoomsName(html, ", ");
                                        if (!exam.getPeriod().equals(conflict.getOtherExam().getPeriod()) || exam.getLength() != conflict.getOtherExam().getLength()) {
                                            time += conflict.getOtherExam().getTime(html);
                                        }
                                    }
                                    firstSection = false;
                                }
                            } else if (conflict.getOtherEventId()!=null) {
                                classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                classes += conflict.getOtherEventName();
                                enrollment += conflict.getOtherEventSize();
                                seating += (conflict.isOtherClass()?MSG.typeClass():MSG.typeEvent());
                                room += conflict.getOtherEventRoom();
                                //date += conflict.getOtherEventDate();
                                time += conflict.getOtherEventTime(); 
                            }
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        id,
                                        name,
                                        (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+MSG.conflictDirect()+(html?"</font>":""),
                                        classes,
                                        enrollment,
                                        seating,
                                        date,
                                        time,
                                        room,
                                        ""
                                    }, new Comparable[] {
                                        new MultiComparable(id, exam, 0),
                                        new MultiComparable(name,id, exam, 0),
                                        new MultiComparable(0, exam, 0),
                                        new MultiComparable(exam, exam, 0),
                                        new MultiComparable(-exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                                        new MultiComparable(exam.getSeatingType(), exam, 0),
                                        new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                        new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                        new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                        new MultiComparable(-1.0, exam, 0)
                                    },
                                    exam.getExamId().toString());
                        } else {
                            if (conflict.getOtherExam()!=null) {
                                table.addLine(
                                        "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                        new String[] {
                                            id,
                                            name,
                                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+MSG.conflictDirect()+(html?"</font>":""),
                                            exam.getExamName()+nl+conflict.getOtherExam().getExamName(),
                                            exam.getNrStudents()+nl+conflict.getOtherExam().getNrStudents(),
                                            exam.getSeatingTypeLabel()+nl+conflict.getOtherExam().getSeatingTypeLabel(),
                                            exam.getDate(html)+nl,
                                            exam.getTime(html)+nl+(exam.getPeriod().equals(conflict.getOtherExam().getPeriod()) && exam.getLength() == conflict.getOtherExam().getLength() ? "" : conflict.getOtherExam().getTime(html)),
                                            exam.getRoomsName(html, ", ")+nl+conflict.getOtherExam().getRoomsName(html, ", "),
                                            ""
                                        }, new Comparable[] {
                                            new MultiComparable(id, exam, 0),
                                            new MultiComparable(name, id, exam, 0),
                                            new MultiComparable(0, exam, 0),
                                            new MultiComparable(exam, exam, 0),
                                            new MultiComparable(-exam.getNrStudents()-conflict.getOtherExam().getNrStudents(), exam, 0),
                                            new MultiComparable(exam.getSeatingType(), exam, 0),
                                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                            new MultiComparable(-1.0, exam, 0)
                                        },
                                        exam.getExamId().toString());                                
                            } else if (conflict.getOtherEventId()!=null) {
                                table.addLine(
                                        "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                        new String[] {
                                            id,
                                            name,
                                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+MSG.conflictDirect()+(html?"</font>":""),
                                            exam.getExamName()+nl+conflict.getOtherEventName(),
                                            String.valueOf(exam.getNrStudents())+nl+conflict.getOtherEventSize(),
                                            exam.getSeatingTypeLabel()+nl+(conflict.isOtherClass()?MSG.typeClass():MSG.typeEvent()),
                                            exam.getDate(html)+nl,//+conflict.getOtherEventDate(),
                                            exam.getTime(html)+nl+conflict.getOtherEventTime(),
                                            exam.getRoomsName(html, ", ")+nl+conflict.getOtherEventRoom(),
                                            ""
                                        }, new Comparable[] {
                                            new MultiComparable(id, exam, 0),
                                            new MultiComparable(name, id, exam, 0),
                                            new MultiComparable(0, exam, 0),
                                            new MultiComparable(exam, exam, 0),
                                            new MultiComparable(-exam.getNrStudents()-conflict.getOtherEventSize(), exam, 0),
                                            new MultiComparable(exam.getSeatingType(), exam, 0),
                                            new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                            new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                            new MultiComparable(-1.0, exam, 0)
                                        },
                                        exam.getExamId().toString());                                
                            }

                        }
                    }
                }
            if (btb)
                for (BackToBackConflict conflict : (studentConf?exam.getBackToBackConflicts():exam.getInstructorBackToBackConflicts())) {
                    if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                    for (Long studentId : conflict.getStudents()) {
                        String id = "", name = "";
                        if (studentConf) {
                            Student student = students.get(studentId);
                            id = student.getExternalUniqueId();
                            name = student.getName(nameFormat);
                        } else {
                            DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(studentId);
                            id = instructor.getExternalUniqueId();
                            name = instructor.getName(nameFormat);
                        }
                        if (!match(form,id) && !match(form,name)) continue;
                        if (form.getShowSections()) {
                            String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                            boolean firstSection = true;
                            for (ExamSectionInfo section : exam.getSections()) {
                                if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                                if (classes.length()>0) {
                                    classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                }
                                classes += section.getName();
                                enrollment += String.valueOf(section.getNrStudents());
                                if (firstSection) {
                                    seating += exam.getSeatingTypeLabel();
                                    date += exam.getDate(html);
                                    time += exam.getTime(html);
                                    room += exam.getRoomsName(html, ", ");
                                }
                                firstSection = false;
                            }
                            firstSection = true;
                            for (ExamSectionInfo section : conflict.getOtherExam().getSections()) {
                                if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                                if (classes.length()>0) {
                                    classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                }
                                classes += section.getName();
                                enrollment += String.valueOf(section.getNrStudents());
                                if (firstSection) {
                                    seating += exam.getSeatingTypeLabel();
                                    time += conflict.getOtherExam().getTime(html);
                                    room += conflict.getOtherExam().getRoomsName(html, ", ");
                                }
                                firstSection = false;
                            }
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        id,
                                        name,
                                        (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+MSG.conflictBackToBack()+(html?"</font>":""),
                                        classes,
                                        enrollment,
                                        seating,
                                        date,
                                        time,
                                        room,
                                        (int)(conflict.getDistance()*10.0)+" m"
                                    }, new Comparable[] {
                                        new MultiComparable(id, exam, 0),
                                        new MultiComparable(name, id, exam, 0),
                                        new MultiComparable(2, exam, 0),
                                        new MultiComparable(exam, exam, 0),
                                        new MultiComparable(-exam.getNrStudents()-conflict.getOtherExam().getNrStudents(), exam, 0),
                                        new MultiComparable(exam.getSeatingType(), exam, 0),
                                        new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                        new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                        new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                        new MultiComparable(conflict.getDistance(), exam, 0)
                                    },
                                    exam.getExamId().toString());
                        } else {
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        id,
                                        name,
                                        (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+MSG.conflictBackToBack()+(html?"</font>":""),
                                        exam.getExamName()+nl+conflict.getOtherExam().getExamName(),
                                        exam.getNrStudents()+nl+conflict.getOtherExam().getNrStudents(),
                                        exam.getSeatingTypeLabel()+nl+conflict.getOtherExam().getSeatingTypeLabel(),
                                        exam.getDate(html)+nl,
                                        exam.getTime(html)+nl+conflict.getOtherExam().getTime(html),
                                        exam.getRoomsName(html, ", ")+nl+conflict.getOtherExam().getRoomsName(html, ", "),
                                        (int)(conflict.getDistance()*10.0)+" m"
                                    }, new Comparable[] {
                                        new MultiComparable(id, exam, 0),
                                        new MultiComparable(name, id, exam, 0),
                                        new MultiComparable(2, exam, 0),
                                        new MultiComparable(exam, exam, 0),
                                        new MultiComparable(-exam.getNrStudents()-conflict.getOtherExam().getNrStudents(), exam, 0),
                                        new MultiComparable(exam.getSeatingType(), exam, 0),
                                        new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                        new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                        new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                        new MultiComparable(conflict.getDistance(), exam, 0)
                                    },
                                    exam.getExamId().toString());
                        }
                    }
                }
            if (m2d)
                conflicts: for (MoreThanTwoADayConflict conflict : (studentConf?exam.getMoreThanTwoADaysConflicts():exam.getInstructorMoreThanTwoADaysConflicts())) {
                    for (ExamAssignment other : conflict.getOtherExams())
                        if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                    for (Long studentId : conflict.getStudents()) {
                        String id = "", name = "";
                        if (studentConf) {
                            Student student = students.get(studentId);
                            id = student.getExternalUniqueId();
                            name = student.getName(nameFormat);
                        } else {
                            DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(studentId);
                            id = instructor.getExternalUniqueId();
                            name = instructor.getName(nameFormat);
                        }
                        if (!match(form,id) && !match(form,name)) continue;
                        if (form.getShowSections()) {
                            String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                            int nrStudents = exam.getNrStudents();
                            boolean firstSection = true;
                            for (ExamSectionInfo section : exam.getSections()) {
                                if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                                if (classes.length()>0) {
                                    classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                }
                                classes += section.getName();
                                enrollment += String.valueOf(section.getNrStudents());
                                if (firstSection) {
                                    seating += exam.getSeatingTypeLabel();
                                    date += exam.getDate(html);
                                    time += exam.getTime(html);
                                    room += exam.getRoomsName(html, ", ");
                                }
                                firstSection = false;
                            }
                            for (ExamAssignment other : conflict.getOtherExams()) {
                                firstSection = true;
                                nrStudents += other.getNrStudents();
                                for (ExamSectionInfo section : other.getSections()) {
                                    if (studentConf && !section.getStudentIds().contains(studentId)) continue;
                                    if (classes.length()>0) {
                                        classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                                    }
                                    classes += section.getName();
                                    enrollment += String.valueOf(section.getNrStudents());
                                    if (firstSection) {
                                        seating += other.getSeatingTypeLabel();
                                        time += other.getTime(html);
                                        room += other.getRoomsName(html, ", ");
                                    }
                                    firstSection = false;
                                }
                            }
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        id,
                                        name,
                                        (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?"&gt;":"")+"2 A Day"+(html?"</font>":""),
                                        classes,
                                        enrollment,
                                        seating,
                                        date,
                                        time,
                                        room,
                                        ""
                                    }, new Comparable[] {
                                        new MultiComparable(id, exam, 0),
                                        new MultiComparable(name, id, exam, 0),
                                        new MultiComparable(1, exam, 0),
                                        new MultiComparable(exam, exam, 0),
                                        new MultiComparable(-nrStudents, exam, 0),
                                        new MultiComparable(exam.getSeatingType(), exam, 0),
                                        new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                        new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                        new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                        new MultiComparable(-1.0, exam, 0)
                                    },
                                    exam.getExamId().toString());
                        } else {
                            String classes = exam.getExamName(), enrollment = ""+exam.getNrStudents(), seating = exam.getSeatingTypeLabel();
                            String date = exam.getDate(html), time = exam.getTime(html), room = exam.getRoomsName(html, ", ");
                            int nrStudents = exam.getNrStudents();
                            for (ExamAssignment other : conflict.getOtherExams()) {
                                classes += nl+other.getExamName();
                                enrollment += nl+other.getNrStudents();
                                seating += nl+other.getSeatingTypeLabel();
                                time += nl+other.getTime(html);
                                room += nl+other.getRoomsName(html, ", ");
                            }
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        id,
                                        name,
                                        (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?"&gt;":"")+"2 A Day"+(html?"</font>":""),
                                        classes,
                                        enrollment,
                                        seating,
                                        date,
                                        time,
                                        room,
                                        ""
                                    }, new Comparable[] {
                                        new MultiComparable(id, exam, 0),
                                        new MultiComparable(name, id, exam, 0),
                                        new MultiComparable(1, exam, 0),
                                        new MultiComparable(exam, exam, 0),
                                        new MultiComparable(-nrStudents, exam, 0),
                                        new MultiComparable(exam.getSeatingType(), exam, 0),
                                        new MultiComparable(exam.getPeriodOrd(), exam, 0),
                                        new MultiComparable(exam.getPeriod().getStartSlot(), exam, 0),
                                        new MultiComparable(exam.getRoomsName(":"), exam, 0),
                                        new MultiComparable(-1.0, exam, 0)
                                    },
                                    exam.getExamId().toString());
                        }
                    }
                }
        }
        
        return table;	    
	}
	
	private PdfWebTable generateDirectConflictsReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams, boolean studentConf) {
        String nl = (html?"<br>":"\n");
        DecimalFormat df = new DecimalFormat("0.0");
        PdfWebTable table = new PdfWebTable( 10,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    MSG.col1stExam() + " " + (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.col1stExam() + " " + MSG.colEnrollment(),
                    MSG.col1stExam() + " " + MSG.colSeatingType().replace("\n", nl),
                    MSG.col2ndExam() + " " + (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.col2ndExam() + " " + MSG.colEnrollment(),
                    MSG.col2ndExam() + " " + MSG.colSeatingType().replace("\n", nl),
                    MSG.colDate(),
                    MSG.colTime(),
                    MSG.conflictDirect(),
                    MSG.colDirectPercent()},
                new String[] {"left","right","center","left","right","center","left","left","right","right"},
                new boolean[] {true, true, true, true, true, true, true, true, true, true} );
        
        table.setRowStyle("white-space:nowrap");
        for (ExamAssignmentInfo exam : exams) {
            if (!match(form, exam)) continue;
            for (DirectConflict conflict : (studentConf?exam.getDirectConflicts():exam.getInstructorDirectConflicts())) {
                if (match(form, conflict.getOtherExam()) && exam.compareTo(conflict.getOtherExam())>=0) continue;
                ExamAssignment other = conflict.getOtherExam();
                if (form.getShowSections()) {
                    for (ExamSectionInfo section1 : exam.getSections()) {
                        if (other!=null) {
                            for (ExamSectionInfo section2 : conflict.getOtherExam().getSections()) {
                                if (!match(form, section1.getName()) && !match(form, section2.getName())) continue;
                                int nrStudents = 0;
                                if (studentConf) for (Long studentId : section1.getStudentIds()) {
                                    if (section2.getStudentIds().contains(studentId)) nrStudents++;
                                } else nrStudents = conflict.getNrStudents();
                                if (nrStudents==0) continue;
                                table.addLine(
                                        "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                        new String[] {
                                            section1.getName(),
                                            String.valueOf(section1.getNrStudents()),
                                            exam.getSeatingTypeLabel(),
                                            section2.getName(),
                                            String.valueOf(section2.getNrStudents()),
                                            conflict.getOtherExam().getSeatingTypeLabel(),
                                            exam.getDate(html),
                                            exam.getTime(html),
                                            String.valueOf(nrStudents),
                                            df.format(100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents()))
                                        }, new Comparable[] {
                                            new MultiComparable(section1.getName(), section2.getName(), exam, other),
                                            new MultiComparable(-section1.getNrStudents(), -section2.getNrStudents(), section1.getName(), section2.getName(), exam, other),
                                            new MultiComparable(exam.getSeatingType(), other.getSeatingType(), section1.getName(), section2.getName(), exam, other),
                                            new MultiComparable(section2.getName(), section1.getName(), other, exam),
                                            new MultiComparable(-section2.getNrStudents(), -section1.getNrStudents(), section2.getName(), section1.getName(), other, exam),
                                            new MultiComparable(other.getSeatingType(), exam.getSeatingType(), section2.getName(), section1.getName(), other, exam),
                                            new MultiComparable(exam.getPeriodOrd(), exam, other),
                                            new MultiComparable(exam.getPeriod().getStartSlot(), exam, other),
                                            new MultiComparable(-nrStudents, exam, other),
                                            new MultiComparable(-100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents()), exam, other)
                                        },
                                        exam.getExamId().toString());
                            }                        
                        } else if (conflict.getOtherEventId()!=null) {
                            if (!match(form, section1.getName())) continue;
                            int nrStudents = 0;
                            for (Long studentId : section1.getStudentIds())
                                if (conflict.getStudents().contains(studentId)) nrStudents++;
                            if (nrStudents==0) continue;
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        section1.getName(),
                                        String.valueOf(section1.getNrStudents()),
                                        exam.getSeatingTypeLabel(),
                                        conflict.getOtherEventName(),
                                        String.valueOf(conflict.getOtherEventSize()),
                                        (conflict.isOtherClass()?MSG.typeClass():MSG.typeEvent()),
                                        exam.getDate(html),
                                        exam.getTime(html),
                                        String.valueOf(nrStudents),
                                        df.format(100.0*nrStudents/Math.min(section1.getNrStudents(), conflict.getOtherEventSize()))
                                    }, new Comparable[] {
                                        new MultiComparable(section1.getName(), conflict.getOtherEventName(), exam, other),
                                        new MultiComparable(-section1.getNrStudents(), -conflict.getOtherEventSize(), section1.getName(), conflict.getOtherEventName(), exam, other),
                                        new MultiComparable(exam.getSeatingType(), -1, section1.getName(), conflict.getOtherEventName(), exam, other),
                                        new MultiComparable(conflict.getOtherEventName(), section1.getName(), other, exam),
                                        new MultiComparable(-conflict.getOtherEventSize(), -section1.getNrStudents(), conflict.getOtherEventName(), section1.getName(), other, exam),
                                        new MultiComparable(-1, exam.getSeatingType(), conflict.getOtherEventName(), section1.getName(), other, exam),
                                        new MultiComparable(exam.getPeriodOrd(), exam, other),
                                        new MultiComparable(exam.getPeriod().getStartSlot(), exam, other),
                                        new MultiComparable(-nrStudents, exam, other),
                                        new MultiComparable(-100.0*nrStudents/Math.min(section1.getNrStudents(), conflict.getOtherEventSize()), exam, other)
                                    },
                                    exam.getExamId().toString());                            
                        }
                    }
                } else {
                    if (other!=null) {
                        table.addLine(
                                "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    exam.getExamName(),
                                    String.valueOf(exam.getNrStudents()),
                                    exam.getSeatingTypeLabel(),
                                    other.getExamName(),
                                    String.valueOf(other.getNrStudents()),
                                    conflict.getOtherExam().getSeatingTypeLabel(),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    String.valueOf(conflict.getNrStudents()),
                                    df.format(100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents()))
                                }, new Comparable[] {
                                    new MultiComparable(exam, other),
                                    new MultiComparable(-exam.getNrStudents(), -other.getNrStudents(), exam, other),
                                    new MultiComparable(exam.getSeatingType(), other.getSeatingType(), exam, other),
                                    new MultiComparable(other, exam),
                                    new MultiComparable(-other.getNrStudents(), -exam.getNrStudents(), other, exam),
                                    new MultiComparable(other.getSeatingType(), exam.getSeatingType(), other, exam),
                                    new MultiComparable(exam.getPeriodOrd(), exam, other),
                                    new MultiComparable(exam.getPeriod().getStartSlot(), exam, other),
                                    new MultiComparable(-conflict.getNrStudents(), exam, other),
                                    new MultiComparable(-100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents()), exam, other)
                                },
                                exam.getExamId().toString());
                    } else if (conflict.getOtherEventId()!=null) {
                        table.addLine(
                                "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    exam.getExamName(),
                                    String.valueOf(exam.getNrStudents()),
                                    exam.getSeatingTypeLabel(),
                                    conflict.getOtherEventName(),
                                    String.valueOf(conflict.getOtherEventSize()),
                                    (conflict.isOtherClass()?MSG.typeClass():MSG.typeEvent()),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    String.valueOf(conflict.getNrStudents()),
                                    df.format(100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), conflict.getOtherEventSize()))
                                }, new Comparable[] {
                                    new MultiComparable(exam, other),
                                    new MultiComparable(-exam.getNrStudents(), -conflict.getOtherEventSize(), exam, other),
                                    new MultiComparable(exam.getSeatingType(), -1, exam, other),
                                    new MultiComparable(other, exam),
                                    new MultiComparable(-conflict.getOtherEventSize(), -exam.getNrStudents(), other, exam),
                                    new MultiComparable(-1, exam.getSeatingType(), other, exam),
                                    new MultiComparable(exam.getPeriodOrd(), exam, other),
                                    new MultiComparable(exam.getPeriod().getStartSlot(), exam, other),
                                    new MultiComparable(-conflict.getNrStudents(), exam, other),
                                    new MultiComparable(-100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), conflict.getOtherEventSize()), exam, other)
                                },
                                exam.getExamId().toString());
                    }
                }
            }
        }
        return table;
	}
    
    private PdfWebTable generateBackToBackConflictsReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams, boolean studentConf) {
         String nl = (html?"<br>":"\n");
         DecimalFormat df = new DecimalFormat("0.0");
         PdfWebTable table = new PdfWebTable( 11,
                 form.getReportName(), "examAssignmentReport.action?ord=%%",
                 new String[] {
                     MSG.col1stExam() + " " + (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                     MSG.col1stExam() + " " + MSG.colEnrollment(),
                     MSG.col1stExam() + " " + MSG.colSeatingType().replace("\n", nl),
                     MSG.col2ndExam() + " " + (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                     MSG.col2ndExam() + " " + MSG.colEnrollment(),
                     MSG.col2ndExam() + " " + MSG.colSeatingType().replace("\n", nl),
                     MSG.colDate(),
                     MSG.colTime(),
                     MSG.conflictBackToBack(),
                     MSG.colBackToBackPercent(),
                     MSG.colDistanceMeters()},
                 new String[] {"left","right","center","left","right","center","left","left","right","right","right"},
                 new boolean[] {true, true, true, true, true, true, true, true, true, true, true} );
         
         table.setRowStyle("white-space:nowrap");
         for (ExamAssignmentInfo exam : exams) {
             if (!match(form, exam)) continue;
             for (BackToBackConflict conflict : (studentConf?exam.getBackToBackConflicts():exam.getInstructorBackToBackConflicts())) {
                 if (match(form, conflict.getOtherExam()) && exam.getPeriod().compareTo(conflict.getOtherExam().getPeriod())>=0) continue;
                 ExamAssignment other = conflict.getOtherExam();
                 if (form.getShowSections()) {
                     for (ExamSectionInfo section1 : exam.getSections()) {
                         for (ExamSectionInfo section2 : conflict.getOtherExam().getSections()) {
                             int nrStudents = 0;
                             if (!match(form, section1.getName()) && !match(form, section2.getName())) continue;
                             if (studentConf) for (Long studentId : section1.getStudentIds()) {
                                 if (section2.getStudentIds().contains(studentId)) nrStudents++;
                             } else
                                 nrStudents = conflict.getNrStudents();
                             if (nrStudents==0) continue;
                             table.addLine(
                                     "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                     new String[] {
                                         section1.getName(),
                                         String.valueOf(section1.getNrStudents()),
                                         exam.getSeatingTypeLabel(),
                                         section2.getName(),
                                         String.valueOf(section2.getNrStudents()),
                                         conflict.getOtherExam().getSeatingTypeLabel(),
                                         exam.getDate(html),
                                         exam.getTime(html),
                                         String.valueOf(nrStudents),
                                         df.format(100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents())),
                                         String.valueOf((int)(10.0*conflict.getDistance()))
                                     }, new Comparable[] {
                                         new MultiComparable(section1.getName(), section2.getName(), exam, other),
                                         new MultiComparable(-section1.getNrStudents(), -section2.getNrStudents(), section1.getName(), section2.getName(), exam, other),
                                         new MultiComparable(exam.getSeatingType(), other.getSeatingType(), section1.getName(), section2.getName(), exam, other),
                                         new MultiComparable(section2.getName(), section1.getName(), other, exam),
                                         new MultiComparable(-section2.getNrStudents(), -section1.getNrStudents(), section2.getName(), section1.getName(), other, exam),
                                         new MultiComparable(other.getSeatingType(), exam.getSeatingType(), section2.getName(), section1.getName(), other, exam),
                                         new MultiComparable(exam.getPeriodOrd(), exam, other),
                                         new MultiComparable(exam.getPeriod().getStartSlot(), exam, other),
                                         new MultiComparable(-nrStudents, exam, other),
                                         new MultiComparable(-100.0*nrStudents/Math.min(section1.getNrStudents(), section2.getNrStudents()), exam, other),
                                         new MultiComparable(-conflict.getDistance(), exam, other)
                                     },
                                     exam.getExamId().toString());
                         }
                     }
                 } else {
                     table.addLine(
                             "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                             new String[] {
                                 exam.getExamName(),
                                 String.valueOf(exam.getNrStudents()),
                                 exam.getSeatingTypeLabel(),
                                 other.getExamName(),
                                 String.valueOf(other.getNrStudents()),
                                 conflict.getOtherExam().getSeatingTypeLabel(),
                                 exam.getDate(html),
                                 exam.getTime(html),
                                 String.valueOf(conflict.getNrStudents()),
                                 df.format(100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents())),
                                 String.valueOf((int)(10.0*conflict.getDistance()))
                             }, new Comparable[] {
                                 new MultiComparable(exam, other),
                                 new MultiComparable(-exam.getNrStudents(), -other.getNrStudents(), exam, other),
                                 new MultiComparable(exam.getSeatingType(), other.getSeatingType(), exam, other),
                                 new MultiComparable(other, exam),
                                 new MultiComparable(-other.getNrStudents(), -exam.getNrStudents(), other, exam),
                                 new MultiComparable(other.getSeatingType(), exam.getSeatingType(), other, exam),
                                 new MultiComparable(exam.getPeriodOrd(), exam, other),
                                 new MultiComparable(exam.getPeriod().getStartSlot(), exam, other),
                                 new MultiComparable(-conflict.getNrStudents(), exam, other),
                                 new MultiComparable(-100.0*conflict.getNrStudents()/Math.min(exam.getNrStudents(), other.getNrStudents()), exam, other),
                                 new MultiComparable(-conflict.getDistance(), exam, other)
                             },
                             exam.getExamId().toString());
                 }
             }
         }
         return table;
    }
    
    private PdfWebTable generate2MoreADayConflictsReport(boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams, boolean studentConf) {
        DecimalFormat df = new DecimalFormat("0.0");
        int max = 0;
        for (ExamAssignmentInfo exam : exams) {
            if (!match(form, exam)) continue;
            conflicts: for (MoreThanTwoADayConflict conflict : (studentConf?exam.getMoreThanTwoADaysConflicts():exam.getInstructorMoreThanTwoADaysConflicts())) {
                for (ExamAssignment other : conflict.getOtherExams()) 
                    if (match(form, other) && exam.compareTo(other)>=0) continue conflicts;
                max = Math.max(max,conflict.getOtherExams().size()+1);
            }
        }
        if (max<=2) return null;
        
        String[] colName = new String[3+3*max];
        String[] colAlign = new String[3+3*max];
        boolean[] colOrd = new boolean[3+3*max];
        int idx = 0;
        colName[idx] = MSG.colDate(); colAlign[idx] = "left"; colOrd[idx++] = true;
        for (int i=0;i<max;i++) {
            String th = (i==0?MSG.col1stExam():i==1?MSG.col2ndExam():i==2?MSG.col3rdExam():MSG.colNthExam(i + 1));
            colName[idx] = th+" "+(form.getShowSections()?MSG.colOwner():MSG.colExamination()); colAlign[idx] = "left"; colOrd[idx++] = true;
            colName[idx] = th+" " + MSG.colEnrollment(); colAlign[idx] = "right"; colOrd[idx++] = true;
            colName[idx] = th+" " + MSG.colTime(); colAlign[idx] = "left"; colOrd[idx++] = true;
        }
        colName[idx] = (html ? MSG.conflictMoreThanTwoADay().replace(">", "&gt;") : MSG.conflictMoreThanTwoADay()); colAlign[idx] = "left"; colOrd[idx++] = true;
        colName[idx] = (html ? MSG.colMoreThanTwoADayPercent().replace(">", "&gt;") : MSG.colMoreThanTwoADayPercent()); colAlign[idx] = "left"; colOrd[idx++] = true;
        PdfWebTable table = new PdfWebTable( 3+3*max, form.getReportName(), "examAssignmentReport.action?ord=%%", colName, colAlign, colOrd);
        table.setRowStyle("white-space:nowrap");
        for (ExamAssignmentInfo exam : exams) {
            if (!match(form, exam)) continue;
            conflicts: for (MoreThanTwoADayConflict conflict : (studentConf?exam.getMoreThanTwoADaysConflicts():exam.getInstructorMoreThanTwoADaysConflicts())) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (match(form, other) && exam.compareTo(other)>=0) continue conflicts;
                Vector<ExamAssignment> examsThisConf = new Vector<ExamAssignment>(max);
                examsThisConf.add(exam);
                examsThisConf.addAll(conflict.getOtherExams());
                Collections.sort(examsThisConf, new Comparator<ExamAssignment>() {
                    public int compare(ExamAssignment a1, ExamAssignment a2) {
                        //int cmp = a1.getPeriod().compareTo(a2.getPeriod());
                        //if (cmp!=0) return cmp;
                        return a1.compareTo(a2);
                    }
                });
                if (form.getShowSections()) {
                    idx = 0;
                    String[] line = new String[3+3*max];
                    Comparable[] cmp = new Comparable[3+3*max];
                    line[idx] = exam.getDate(false); cmp[idx] = new MultiComparable(exam.getPeriodOrd(), new MultiComparable(examsThisConf));
                    m2dReportAddLines(form, html, studentConf, table, max, examsThisConf, 0, line, cmp, 1, exam.getNrStudents(), null, false);  
                } else {
                    idx = 0;
                    String[] line = new String[3+3*max];
                    Comparable[] cmp = new Comparable[3+3*max];
                    line[idx] = exam.getDate(false); cmp[idx++] = new MultiComparable(exam.getPeriodOrd(), new MultiComparable(examsThisConf));
                    int minStudents = exam.getNrStudents();
                    for (ExamAssignment x : examsThisConf) {
                        line[idx] = x.getExamName();
                        cmp[idx++] = new MultiComparable(x, new MultiComparable(examsThisConf));
                        line[idx] = String.valueOf(x.getNrStudents());
                        cmp[idx++] = new MultiComparable(-x.getNrStudents(), x, new MultiComparable(examsThisConf));
                        line[idx] = x.getTime(html);
                        cmp[idx++] = new MultiComparable(x.getPeriod().getStartSlot(), x, new MultiComparable(examsThisConf));
                        minStudents = Math.min(minStudents, x.getNrStudents());
                    }
                    for (int i=examsThisConf.size();i<max;i++) {
                        line[idx] = "";
                        cmp[idx++] = new MultiComparable(null, new MultiComparable(examsThisConf));
                        line[idx] = "";
                        cmp[idx++] = new MultiComparable(1, null, new MultiComparable(examsThisConf));
                        line[idx] = "";
                        cmp[idx++] = new MultiComparable(-1, null, new MultiComparable(examsThisConf));
                    }
                    line[idx] = String.valueOf(conflict.getNrStudents());
                    cmp[idx++] = new MultiComparable(-conflict.getNrStudents(), new MultiComparable(examsThisConf));
                    line[idx] = df.format(100.0*conflict.getNrStudents()/minStudents);
                    cmp[idx++] = new MultiComparable(-100.0*conflict.getNrStudents()/minStudents, new MultiComparable(examsThisConf));
                    table.addLine(
                            "onClick=\"document.location='examDetail.action?examId="+examsThisConf.firstElement().getExamId()+"';\"",
                            line, cmp, examsThisConf.firstElement().getExamId().toString());
                }
            }
        }
        return table;
    }
    
    private void m2dReportAddLines(ExamAssignmentReportForm form, boolean html, boolean studentConf, PdfWebTable table, int max, Vector<ExamAssignment> exams, int pos, String[] line, Comparable[] cmp, int idx, int minStudents, Set<Long> students, boolean match) {
        if (students!=null && students.isEmpty()) return;
        if (pos==max) {
            if (!match) return;
            line[idx] = String.valueOf(students.size());
            cmp[idx++] = new MultiComparable(-students.size(), new MultiComparable(exams));
            line[idx] = new DecimalFormat("0.0").format(100.0*students.size()/minStudents);
            cmp[idx++] = new MultiComparable(-100.0*students.size()/minStudents, new MultiComparable(exams));
            
            table.addLine("onClick=\"document.location='examDetail.action?examId="+exams.firstElement().getExamId()+"';\"", (String[])line.clone(), (Comparable[])cmp.clone(), exams.firstElement().getExamId().toString());
            return;
        }
        if (pos<exams.size()) {
            ExamAssignment exam = exams.elementAt(pos);
            Set<Long> newStudents = null;
            for (ExamSectionInfo section : exam.getSections()) {
                if (students == null) {
                	if (studentConf)
                		newStudents = new HashSet(section.getStudentIds());
                	else {
                		newStudents = new HashSet();
                		for (ExamInstructorInfo i: section.getExam().getInstructors()) newStudents.add(i.getId());
                	}
                } else {
                    newStudents = new HashSet<Long>();
                    for (Long studentId : students) {
                        if (studentConf && section.getStudentIds().contains(studentId)) newStudents.add(studentId);
                        if (!studentConf && section.getExam().hasInstructor(studentId)) newStudents.add(studentId);
                    }
                }
                if (newStudents.isEmpty()) continue;
                line[idx] = section.getName();
                cmp[idx] = new MultiComparable(section.getName(), new MultiComparable(exams));
                line[idx+1] = String.valueOf(section.getNrStudents());
                cmp[idx+1] = new MultiComparable(-section.getNrStudents(), section.getName(), new MultiComparable(exams));
                line[idx+2] = exam.getTime(html);
                cmp[idx+2] = new MultiComparable(exam.getPeriod().getStartSlot(), section.getName(), new MultiComparable(exams));
                m2dReportAddLines(form, html, studentConf, table, max, exams, pos+1, line, cmp, idx+3, Math.min(section.getNrStudents(),minStudents), newStudents, match || match(form,section.getName()));
            }
        } else {
            line[idx] = "";
            cmp[idx++] = new MultiComparable(null, new MultiComparable(exams));
            line[idx] = "";
            cmp[idx++] = new MultiComparable(1, null, new MultiComparable(exams));
            line[idx] = "";
            cmp[idx++] = new MultiComparable(-1, null, new MultiComparable(exams));
            m2dReportAddLines(form, html, studentConf, table, max, exams, pos+1, line, cmp, idx, minStudents, students, match);
        }
    }
    
    private PdfWebTable generateIndividualAssignmentReport(boolean html, Long sessionId, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams, boolean student, String nameFormat) {
        Hashtable<Long, Student> students = new Hashtable();
        if (student) {
            HashSet<Long> allStudentIds = new HashSet();
            for (ExamAssignmentInfo exam : exams)
                for (ExamSectionInfo section : exam.getSections())
                    allStudentIds.addAll(section.getStudentIds());
            String inSet = null; int idx = 0;
            for (Iterator i=allStudentIds.iterator();i.hasNext();idx++) {
                if (idx==1000) {
                    for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                        students.put(s.getUniqueId(), s);
                    }
                    idx = 0; inSet = null;
                }
                if (inSet==null)
                    inSet = i.next().toString();
                else
                    inSet += ","+i.next();
            }
            if (inSet!=null) {
            	for (Student s: StudentDAO.getInstance().getSession().createQuery("select s from Student s where s.uniqueId in ("+inSet+")", Student.class).list()) {
                    students.put(s.getUniqueId(), s);
                }
            }
        }
        PdfWebTable table =
            (student?
            new PdfWebTable( 7,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {
                    (student?MSG.colStudentId():MSG.colInstructorId()),
                    MSG.colStudentOrInstructorName(),
                    (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    MSG.colDate(),
                    MSG.colTime(),
                    MSG.colRoom(),
                    MSG.colInstructor()},
                new String[] {"left","left","left","left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true, true} ):
           new PdfWebTable( 6,
                   form.getReportName(), "examAssignmentReport.action?ord=%%",
                   new String[] {
                       (student?MSG.colStudentId():MSG.colInstructorId()),
                       MSG.colStudentOrInstructorName(),
                       (form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                       MSG.colDate(),
                       MSG.colTime(),
                       MSG.colRoom()},
                       new String[] {"left","left","left","left", "left", "left"},
                       new boolean[] {true, true, true, true, true, true} ));
                    
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        for (ExamAssignmentInfo exam : exams) {
            if (form.getShowSections()) {
                for (ExamSectionInfo section : exam.getSections()) {
                    if (student) {
                        for (Long studentId : section.getStudentIds()) {
                            Student s = students.get(studentId);
                            if (s==null) continue;
                            if (!match(form, s.getExternalUniqueId()) && !match(form, s.getName(nameFormat))) continue;
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        s.getExternalUniqueId(),
                                        s.getName(nameFormat),
                                        (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                        exam.getDate(html),
                                        exam.getTime(html),
                                        exam.getRoomsName(html,", "),
                                        exam.getInstructorName("; ")
                                    },
                                    new Comparable[] {
                                        new MultiComparable(s.getExternalUniqueId(), section.getName(), exam),
                                        new MultiComparable(s.getName(nameFormat), s.getExternalUniqueId(), section.getName(), exam),
                                        new MultiComparable(section.getName(), exam),
                                        new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                                        new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), section.getName(), exam),
                                        new MultiComparable(exam.getRoomsName(":"), section.getName(), exam),
                                        new MultiComparable(exam.getInstructorName(":"), section.getName(), exam)
                                    });
                        }
                    } else {
                        for (ExamInstructorInfo instructor : section.getExam().getInstructors()) {
                            if (!match(form, instructor.getExternalUniqueId()) && !match(form, instructor.getName())) continue;
                            table.addLine(
                                    "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        instructor.getExternalUniqueId(),
                                        instructor.getName(),
                                        (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                        exam.getDate(html),
                                        exam.getTime(html),
                                        exam.getRoomsName(html,", ")
                                    },
                                    new Comparable[] {
                                        new MultiComparable(instructor.getExternalUniqueId(), section.getName(), exam),
                                        new MultiComparable(instructor.getName(), instructor.getExternalUniqueId(), section.getName(), exam),
                                        new MultiComparable(section.getName(), exam),
                                        new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                                        new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), section.getName(), exam),
                                        new MultiComparable(exam.getRoomsName(":"), section.getName(), exam)
                                    });
                        }
                    }
                }
            } else {
                if (student) {
                    HashSet<Long> studentIds = new HashSet();
                    for (ExamSectionInfo section : exam.getSections()) {
                        studentIds.addAll(section.getStudentIds());
                    }
                    for (Long studentId : studentIds) {
                        Student s = students.get(studentId);
                        if (s==null) continue;
                        if (!match(form, s.getExternalUniqueId()) && !match(form, s.getName(nameFormat))) continue;
                        table.addLine(
                                "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    s.getExternalUniqueId(),
                                    s.getName(nameFormat),
                                    (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    exam.getRoomsName(html,", "),
                                    exam.getInstructorName("; ")
                                },
                                new Comparable[] {
                                    new MultiComparable(s.getExternalUniqueId(), exam),
                                    new MultiComparable(s.getName(nameFormat), s.getExternalUniqueId(), exam),
                                    new MultiComparable(exam),
                                    new MultiComparable(exam.getPeriodOrd(), exam),
                                    new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), exam),
                                    new MultiComparable(exam.getRoomsName(":"), exam),
                                    new MultiComparable(exam.getInstructorName(":"), exam)
                                });
                    }                        
                } else {
                    for (ExamInstructorInfo instructor : exam.getInstructors()) {
                        if (!match(form, instructor.getExternalUniqueId()) && !match(form, instructor.getName())) continue;
                        table.addLine(
                                "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    instructor.getExternalUniqueId(),
                                    instructor.getName(),
                                    (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    exam.getRoomsName(html,", ")
                                },
                                new Comparable[] {
                                    new MultiComparable(instructor.getExternalUniqueId(), exam),
                                    new MultiComparable(instructor.getName(), instructor.getExternalUniqueId(), exam),
                                    new MultiComparable(exam),
                                    new MultiComparable(exam.getPeriodOrd(), exam),
                                    new MultiComparable(exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), exam),
                                    new MultiComparable(exam.getRoomsName(":"), exam)
                                });
                    }
                }
            }
        }
        return table;       
    }
    
    private PdfWebTable generateStatisticsReport(boolean html, long sessionId, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        String sp = (html?"&nbsp;":" ");
        String indent = (html?"&nbsp;&nbsp;&nbsp;&nbsp;":"    ");
        PdfWebTable table = new PdfWebTable( 2,
                form.getReportName(), "examAssignmentReport.action?ord=%%",
                new String[] {MSG.colName(),MSG.colValue()},
                new String[] {"left", "right"},
                new boolean[] {true, true} );
        table.setRowStyle("white-space:nowrap");
        int row=0;
        
        int sdc=0,sdcna=0,sbtb=0,sdbtb=0,sm2d=0;
        int idc=0,idcna=0,ibtb=0,idbtb=0,im2d=0;
        HashSet<Long>[] sct = new HashSet[] {new HashSet(),new HashSet(),new HashSet(),new HashSet()};
        HashSet<Long> students = new HashSet();
        int studentExams = 0;
        DecimalFormat df1 = new DecimalFormat("0.00");
        DecimalFormat df2 = new DecimalFormat("#,##0");
        int instructorExams = 0;
        HashSet<Long> instructors = new HashSet();
        
        for (ExamAssignmentInfo exam:exams) {
            for (ExamSectionInfo section:exam.getSections()) {
                sct[section.getOwnerType()].add(section.getOwnerId());
                students.addAll(section.getStudentIds());
            }
            studentExams += exam.getStudentIds().size();
            instructorExams += exam.getInstructors().size(); 
            for (DirectConflict dc : exam.getDirectConflicts()) {
                if (dc.getOtherExam()!=null && exam.compareTo(dc.getOtherExam())>=0 && exams.contains(dc.getOtherExam())) continue;
                sdc+=dc.getNrStudents(); 
                if (dc.getOtherExam()==null) sdcna+=dc.getNrStudents();
            }
            for (DirectConflict dc : exam.getInstructorDirectConflicts()) {
                if (dc.getOtherExam()!=null && exam.compareTo(dc.getOtherExam())>=0 && exams.contains(dc.getOtherExam())) continue;
                idc+=dc.getNrStudents();
                if (dc.getOtherExam()==null) idcna+=dc.getNrStudents();
            }
            for (BackToBackConflict btb : exam.getBackToBackConflicts()) {
                if (btb.getOtherExam()!=null && exam.compareTo(btb.getOtherExam())>=0 && exams.contains(btb.getOtherExam())) continue;
                sbtb+=btb.getNrStudents();
                if (btb.isDistance()) sdbtb+=btb.getNrStudents();
            }
            for (BackToBackConflict btb : exam.getInstructorBackToBackConflicts()) {
                if (btb.getOtherExam()!=null && exam.compareTo(btb.getOtherExam())>=0 && exams.contains(btb.getOtherExam())) continue;
                ibtb+=btb.getNrStudents();
                if (btb.isDistance()) idbtb+=btb.getNrStudents();
            }
            m2d: for (MoreThanTwoADayConflict m2d: exam.getMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : m2d.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue m2d;
                sm2d+=m2d.getNrStudents();
            }
            m2d: for (MoreThanTwoADayConflict m2d: exam.getInstructorMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : m2d.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue m2d;
                im2d+=m2d.getNrStudents();
            }
        }
        table.addLine(new String[] {
                MSG.propNumberOfExams(), df2.format(exams.size())
                }, new Comparable[] {row++,null,null});
        for (int i=0;i<ExamOwner.sOwnerTypes.length;i++)
            if (!sct[i].isEmpty()) 
                table.addLine(new String[] {
                        indent+
                        MSG.propOwnersWithAnExam(i==ExamOwner.sOwnerTypeClass?MSG.typeClasses():i==ExamOwner.sOwnerTypeConfig?MSG.typeConfigs():i==ExamOwner.sOwnerTypeCourse?MSG.typeCourses():MSG.typeOfferings()), df2.format(sct[i].size())
                        }, new Comparable[] {row++,null,null});
        
        table.addLine(new String[] {sp,""}, new Comparable[] {row++,null,null});

        /*
        table.addLine(new String[] {
                "Registered students", 
                df2.format(StudentDAO.getInstance().getSession().createQuery("select count(s) from Student s where s.session.uniqueId=:sessionId")
                .setParameter("sessionId", sessionId).uniqueResult())
                }, new Comparable[] {row++,null,null});
                */
        table.addLine(new String[] {
                indent+MSG.propStudentsEnrolledInClasses(), 
                df2.format(StudentDAO.getInstance().getSession().createQuery("select count(distinct s) from Student s inner join s.classEnrollments c where s.session.uniqueId=:sessionId", Number.class)
                .setParameter("sessionId", sessionId).uniqueResult())
                }, new Comparable[] {row++,null,null});
        table.addLine(new String[] {
                indent+MSG.propStudentsHavingAnExam(), df2.format(students.size())
                }, new Comparable[] {row++,null,null});
        table.addLine(new String[] {
                indent+MSG.propStudentExamEnrollments(), df2.format(studentExams)
                }, new Comparable[] {row++,null,null});

        table.addLine(new String[] {sp,""}, new Comparable[] {row++,null,null});
                                    
        if (!instructors.isEmpty()) {
            table.addLine(new String[] {
                    MSG.propRegisteredInstructors(), 
                    df2.format(StudentDAO.getInstance().getSession().createQuery("select count(i.externalUniqueId) from DepartmentalInstructor i where i.department.session.uniqueId=:sessionId", Number.class)
                    .setParameter("sessionId", sessionId).uniqueResult())
                    
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {
                    indent+MSG.propInstructorsHavingAnExam(), df2.format(instructors.size())
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {
                    indent+MSG.propInstructorExamEnrollments(), df2.format(instructorExams)
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {sp,""}, new Comparable[] {row++,null,null});
        }

        if (sdc>0)
            table.addLine(new String[] {
                    MSG.propDirectStudentConflicts(), df2.format(sdc)
                    }, new Comparable[] {row++,null,null});
        if (sdcna>0) {
            table.addLine(new String[] {
                    indent+MSG.propConflictWithOtherExam(), df2.format(sdc-sdcna)
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {
                    indent+MSG.propStudentNotAvailable(), df2.format(sdcna)
                    }, new Comparable[] {row++,null,null});
        }
        if (sm2d>0)
            table.addLine(new String[] {
                    MSG.propStudentMoreThanTwoExamsADayConflicts(), ""+df2.format(sm2d)
                    }, new Comparable[] {row++,null,null});
        if (sbtb>0)
            table.addLine(new String[] {
                    MSG.propStudentBackToBackConflicts(), df2.format(sbtb)
                    }, new Comparable[] {row++,null,null});
        if (sdbtb>0)
            table.addLine(new String[] {
                    indent+MSG.propStudentDistanceBackToBackConflicts(), df2.format(sdbtb)
                    }, new Comparable[] {row++,null,null});
        
        if (idc>0 || im2d>0 || ibtb>0) 
            table.addLine(new String[] {sp,""}, new Comparable[] {row++,null,null});

        if (idc>0)
            table.addLine(new String[] {
                    MSG.propDirectInstructorConflicts(), df2.format(idc)
                    }, new Comparable[] {row++,null,null});
        if (idcna>0) {
            table.addLine(new String[] {
                    indent+MSG.propConflictWithOtherExam(), df2.format(idc-idcna)
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {
                    indent+MSG.propInstructorNotAvailable(), df2.format(idcna)
                    }, new Comparable[] {row++,null,null});
        }
        if (im2d>0)
            table.addLine(new String[] {
                    MSG.propInstructorMoreThanTwoExamsADayConflicts(), df2.format(im2d)
                    }, new Comparable[] {row++,null,null});
        if (ibtb>0)
            table.addLine(new String[] {
                    MSG.propInstructorBackToBackConflicts(), df2.format(ibtb)
                    }, new Comparable[] {row++,null,null});
        if (idbtb>0)
            table.addLine(new String[] {
                    indent+MSG.propInstructorDistanceBackToBackConflicts(), df2.format(idbtb)
                    }, new Comparable[] {row++,null,null});
        
        table.addLine(new String[] {sp,""}, new Comparable[] {row++,null,null});

        if (sdc>0)
            table.addLine(new String[] {
                    MSG.propDirectStudentConflicts(), df1.format(100.0*sdc/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        if (sdcna>0) {
            table.addLine(new String[] {
                    indent+MSG.propConflictWithOtherExam(), df1.format(100.0*(sdc-sdcna)/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {
                    indent+MSG.propStudentNotAvailable(), df1.format(100.0*sdcna/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        }
        if (sm2d>0)
            table.addLine(new String[] {
                    MSG.propStudentMoreThanTwoExamsADayConflicts(), ""+df1.format(100.0*sm2d/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        if (sbtb>0)
            table.addLine(new String[] {
            		MSG.propStudentBackToBackConflicts(), df1.format(100.0*sbtb/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        if (sdbtb>0)
            table.addLine(new String[] {
                    indent+MSG.propStudentDistanceBackToBackConflicts(), df1.format(100.0*sdbtb/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        
        if (idc>0 || im2d>0 || ibtb>0) 
            table.addLine(new String[] {sp,""}, new Comparable[] {row++,null,null});

        if (idc>0)
            table.addLine(new String[] {
            		MSG.propDirectInstructorConflicts(), df1.format(100.0*idc/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        if (idcna>0) {
            table.addLine(new String[] {
                    indent+MSG.propConflictWithOtherExam(), df1.format(100.0*(idc-idcna)/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
            table.addLine(new String[] {
                    indent+MSG.propInstructorNotAvailable(), df1.format(100.0*idcna/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        }
        if (im2d>0)
            table.addLine(new String[] {
                    MSG.propInstructorMoreThanTwoExamsADayConflicts(), df1.format(100.0*im2d/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        if (ibtb>0)
            table.addLine(new String[] {
            		MSG.propInstructorBackToBackConflicts(), df1.format(100.0*ibtb/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});
        if (idbtb>0)
            table.addLine(new String[] {
                    indent+MSG.propInstructorDistanceBackToBackConflicts(), df1.format(100.0*idbtb/studentExams)+"%"
                    }, new Comparable[] {row++,null,null});

        table.setRowStyle("white-space:nowrap");

        return table;       
    }
}

