/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.util.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Email;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.reports.exam.StudentExamReport;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * 
 * @author Tomas Muller
 *
 */
public class PdfExamReportQueueItem extends QueueItem {
	public static String TYPE = "PDF Exam Report";

	private ExamPdfReportForm iForm;
	private String iUrl = null;
	private ExamSolverProxy iExamSolver;
	private String iName = null;
	private double iProgress = 0;
	private boolean iSubjectIndependent = false;
	
	public PdfExamReportQueueItem(Session session, UserContext owner, ExamPdfReportForm form, HttpServletRequest request, ExamSolverProxy examSolver) {
		super(session, owner);
		iForm = form;
		iUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		iExamSolver = examSolver;
		iName = ExamTypeDAO.getInstance().get(iForm.getExamType()).getLabel() + " ";
        for (int i=0;i<iForm.getReports().length;i++) {
        	if (i > 0) iName += ", ";
        	iName += iForm.getReports()[i];
        }
        if (!iForm.getAll()) {
        	iName += " (";
            for (int i=0;i<iForm.getSubjects().length;i++) {
                SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(iForm.getSubjects()[i]));
                if (i > 0) iName += ", ";
                iName += subject.getSubjectAreaAbbreviation();
            }
            iName += ")";
        }
        iSubjectIndependent = (owner == null || owner.getCurrentAuthority() == null ? false : owner.getCurrentAuthority().hasRight(Right.DepartmentIndependent));
        iForm.setSubjectAreas(SubjectArea.getUserSubjectAreas(owner, false));
	}

	@Override
	public void execute() {
        /*
        Logger repLog = Logger.getLogger("org.unitime.timetable.reports.exam");
        Appender myAppender = new AppenderSkeleton() {
			
			@Override
			public boolean requiresLayout() {
				return false;
			}
			
			@Override
			public void close() {
			}
			
			@Override
			protected void append(LoggingEvent event) {
				if (event.getMessage() == null) return;
				if (event.getLevel().toInt() >= Priority.ERROR_INT) {
					error(event.getMessage().toString());
				} else if (event.getLevel().toInt() >= Priority.WARN_INT) {
					warn(event.getMessage().toString());
				} else
					log(event.getMessage().toString());
			}
		};
		repLog.addAppender(myAppender);
		*/
		org.hibernate.Session hibSession = ExamDAO.getInstance().getSession();
		createReports(hibSession);
		if (hibSession.isOpen()) hibSession.close();
		//repLog.removeAppender(myAppender);
	}
	
	private void createReports(org.hibernate.Session hibSession) {
        try {
        	iProgress = 0;
            setStatus("Loading exams...");
            TreeSet<ExamAssignmentInfo> exams = null;
            if (iExamSolver!=null && iExamSolver.getExamTypeId().equals(iForm.getExamType()) && ApplicationProperty.ExaminationPdfReportsCanUseSolution.isTrue()) {
                    exams = new TreeSet(iExamSolver.getAssignedExams());
                    if (iForm.getIgnoreEmptyExams()) for (Iterator<ExamAssignmentInfo> i=exams.iterator();i.hasNext();) {
                        if (i.next().getStudentIds().isEmpty()) i.remove();
                    }
                    if (ApplicationProperty.ExaminationPdfReportsPreloadCrosslistedExams.isTrue()) {
                		setStatus("  Fetching exams...");
                		hibSession.createQuery(
                                "select o from Exam x inner join x.owners o where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId"
                                ).setLong("sessionId", iExamSolver.getSessionId()).setLong("examTypeId", iExamSolver.getExamTypeId()).setCacheable(true).list();
                		setStatus("  Fetching related objects (class)...");
                        hibSession.createQuery(
                                "select c from Class_ c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:classType and c.uniqueId=o.ownerId")
                                .setLong("sessionId", iExamSolver.getSessionId())
                                .setLong("examTypeId", iExamSolver.getExamTypeId())
                                .setInteger("classType", ExamOwner.sOwnerTypeClass).setCacheable(true).list();
                        setStatus("  Fetching related objects (config)...");
                        hibSession.createQuery(
                                "select c from InstrOfferingConfig c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:configType and c.uniqueId=o.ownerId")
                                .setLong("sessionId", iExamSolver.getSessionId())
                                .setLong("examTypeId", iExamSolver.getExamTypeId())
                                .setInteger("configType", ExamOwner.sOwnerTypeConfig).setCacheable(true).list();
                        setStatus("  Fetching related objects (course)...");
                        hibSession.createQuery(
                                "select c from CourseOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:courseType and c.uniqueId=o.ownerId")
                                .setLong("sessionId", iExamSolver.getSessionId())
                                .setLong("examTypeId", iExamSolver.getExamTypeId())
                                .setInteger("courseType", ExamOwner.sOwnerTypeCourse).setCacheable(true).list();
                        setStatus("  Fetching related objects (offering)...");
                        hibSession.createQuery(
                                "select c from InstructionalOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:offeringType and c.uniqueId=o.ownerId")
                                .setLong("sessionId", iExamSolver.getSessionId())
                                .setLong("examTypeId", iExamSolver.getExamTypeId())
                                .setInteger("offeringType", ExamOwner.sOwnerTypeOffering).setCacheable(true).list();
                        Hashtable<Long,Hashtable<Long,Set<Long>>> owner2course2students = new Hashtable();
                        setStatus("  Loading students (class)...");
                        for (Iterator i=
                            hibSession.createQuery(
                            "select o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                            "Exam x inner join x.owners o, "+
                            "StudentClassEnrollment e inner join e.clazz c "+
                            "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                            "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeClass+" and "+
                            "o.ownerId=c.uniqueId").setLong("sessionId", iExamSolver.getSessionId()).setLong("examTypeId", iExamSolver.getExamTypeId()).setCacheable(true).list().iterator();i.hasNext();) {
                                Object[] o = (Object[])i.next();
                                Long ownerId = (Long)o[0];
                                Long studentId = (Long)o[1];
                                Long courseId = (Long)o[2];
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
                        setStatus("  Loading students (config)...");
                        for (Iterator i=
                            hibSession.createQuery(
                                    "select o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                                    "Exam x inner join x.owners o, "+
                                    "StudentClassEnrollment e inner join e.clazz c " +
                                    "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                                    "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                                    "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeConfig+" and "+
                                    "o.ownerId=ioc.uniqueId").setLong("sessionId", iExamSolver.getSessionId()).setLong("examTypeId", iExamSolver.getExamTypeId()).setCacheable(true).list().iterator();i.hasNext();) {
                            Object[] o = (Object[])i.next();
                            Long ownerId = (Long)o[0];
                            Long studentId = (Long)o[1];
                            Long courseId = (Long)o[2];
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
                        setStatus("  Loading students (course)...");
                        for (Iterator i=
                            hibSession.createQuery(
                                    "select o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                                    "Exam x inner join x.owners o, "+
                                    "StudentClassEnrollment e inner join e.courseOffering co " +
                                    "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                                    "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeCourse+" and "+
                                    "o.ownerId=co.uniqueId").setLong("sessionId", iExamSolver.getSessionId()).setLong("examTypeId", iExamSolver.getExamTypeId()).setCacheable(true).list().iterator();i.hasNext();) {
                            Object[] o = (Object[])i.next();
                            Long ownerId = (Long)o[0];
                            Long studentId = (Long)o[1];
                            Long courseId = (Long)o[2];
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
                        setStatus("  Loading students (offering)...");
                        for (Iterator i=
                            hibSession.createQuery(
                                    "select o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                                    "Exam x inner join x.owners o, "+
                                    "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
                                    "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                                    "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeOffering+" and "+
                                    "o.ownerId=io.uniqueId").setLong("sessionId", iExamSolver.getSessionId()).setLong("examTypeId", iExamSolver.getExamTypeId()).setCacheable(true).list().iterator();i.hasNext();) {
                            Object[] o = (Object[])i.next();
                            Long ownerId = (Long)o[0];
                            Long studentId = (Long)o[1];
                            Long courseId = (Long)o[2];
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
                        for (ExamAssignmentInfo exam: exams) {
                        	exam.createSectionsIncludeCrosslistedDummies(owner2course2students);
                        }
                    }
            } else {
                    exams = PdfLegacyExamReport.loadExams(getSessionId(), iForm.getExamType(), true, iForm.getIgnoreEmptyExams(), true);
            }
        	iProgress = 0.1;
            /*
            if (iForm.getAll()) {
                for (Iterator i=Exam.findAll(session.getUniqueId(), iForm.getExamType()).iterator();i.hasNext();) {
                    exams.add(new ExamAssignmentInfo((Exam)i.next()));
                }
            } else {
                for (int i=0;i<iForm.getSubjects().length;i++) {
                    SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(iForm.getSubjects()[i]));
                    TreeSet<ExamAssignmentInfo> examsThisSubject = new TreeSet();
                    for (Iterator j=Exam.findExamsOfSubjectArea(subject.getUniqueId(), iForm.getExamType()).iterator();j.hasNext();) {
                        examsThisSubject.add(new ExamAssignmentInfo((Exam)j.next()));
                    }
                    examsPerSubject.put(subject, examsThisSubject);
                }
            }
            */
            Hashtable<String,File> output = new Hashtable();
            Hashtable<SubjectArea,Hashtable<String,File>> outputPerSubject = new Hashtable();
            Hashtable<ExamInstructorInfo,File> ireports = null;
            Hashtable<Student,File> sreports = null;
            Session session = getSession();
            for (int i=0;i<iForm.getReports().length;i++) {
            	iProgress = 0.1 + (0.8 / iForm.getReports().length) * i;
                setStatus("Generating "+iForm.getReports()[i]+"...");
                Class reportClass = ExamPdfReportForm.sRegisteredReports.get(iForm.getReports()[i]);
                String reportName = null;
                for (Map.Entry<String, Class> entry : PdfLegacyExamReport.sRegisteredReports.entrySet())
                    if (entry.getValue().equals(reportClass)) reportName = entry.getKey();
                if (reportName==null) reportName = "r"+(i+1);
                String name = session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference()+"_"+reportName;
                if (iForm.getAll()) {
                    File file = ApplicationProperties.getTempFile(name, (iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"));
                    log("&nbsp;&nbsp;Writing <a href='temp/"+file.getName()+"'>"+reportName+"."+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf")+"</a>... " + (iSubjectIndependent ? " ("+exams.size()+" exams)" : ""));
                    PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.
                        getConstructor(int.class, File.class, Session.class, ExamType.class, Collection.class, Collection.class).
                        newInstance(iForm.getModeIdx(), file, new SessionDAO().get(session.getUniqueId()), ExamTypeDAO.getInstance().get(iForm.getExamType()), iSubjectIndependent ? null : iForm.getSubjectAreas(), exams);
                    report.setDirect(iForm.getDirect());
                    report.setM2d(iForm.getM2d());
                    report.setBtb(iForm.getBtb());
                    report.setDispRooms(iForm.getDispRooms());
                    report.setNoRoom(iForm.getNoRoom());
                    report.setTotals(iForm.getTotals());
                    report.setLimit(iForm.getLimit()==null || iForm.getLimit().length()==0?-1:Integer.parseInt(iForm.getLimit()));
                    report.setRoomCode(iForm.getRoomCodes());
                    report.setDispLimits(iForm.getDispLimit());
                    report.setSince(iForm.getSince()==null || iForm.getSince().length()==0?null:Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).parse(iForm.getSince()));
                    report.setItype(iForm.getItype());
                    report.setClassSchedule(iForm.getClassSchedule());
                    report.printReport();
                    report.close();
                    output.put(reportName+"."+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                    if (report instanceof InstructorExamReport && iForm.getEmailInstructors()) {
                        ireports = ((InstructorExamReport)report).printInstructorReports(iForm.getModeIdx(), name, new FileGenerator(name));
                    } else if (report instanceof StudentExamReport && iForm.getEmailStudents()) {
                        sreports = ((StudentExamReport)report).printStudentReports(iForm.getModeIdx(), name, new FileGenerator(name));
                    }
                } else {
                    for (int j=0;j<iForm.getSubjects().length;j++) {
                        SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(iForm.getSubjects()[j]));
                        File file = ApplicationProperties.getTempFile(name+"_"+subject.getSubjectAreaAbbreviation(), (iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"));
                        int nrExams = 0;
                        for (ExamAssignmentInfo exam : exams) {
                            if (exam.isOfSubjectArea(subject)) nrExams++;
                        }
                        log("&nbsp;&nbsp;Writing <a href='temp/"+file.getName()+"'>"+subject.getSubjectAreaAbbreviation()+"_"+reportName+"."+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf")+"</a>... ("+nrExams+" exams)");
                        List<SubjectArea> subjects = new ArrayList<SubjectArea>(); subjects.add(subject);
                        PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.
                            getConstructor(int.class, File.class, Session.class, ExamType.class, Collection.class, Collection.class).
                            newInstance(iForm.getModeIdx(), file, new SessionDAO().get(session.getUniqueId()), ExamTypeDAO.getInstance().get(iForm.getExamType()), subjects, exams);
                        report.setDirect(iForm.getDirect());
                        report.setM2d(iForm.getM2d());
                        report.setBtb(iForm.getBtb());
                        report.setDispRooms(iForm.getDispRooms());
                        report.setNoRoom(iForm.getNoRoom());
                        report.setTotals(iForm.getTotals());
                        report.setLimit(iForm.getLimit()==null || iForm.getLimit().length()==0?-1:Integer.parseInt(iForm.getLimit()));
                        report.setRoomCode(iForm.getRoomCodes());
                        report.setDispLimits(iForm.getDispLimit());
                        report.setItype(iForm.getItype());
                        report.setClassSchedule(iForm.getClassSchedule());
                        report.printReport();
                        report.close();
                        output.put(subject.getSubjectAreaAbbreviation()+"_"+reportName+"."+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                        Hashtable<String,File> files = outputPerSubject.get(subject);
                        if (files==null) {
                            files = new Hashtable(); outputPerSubject.put(subject,files);
                        }
                        files.put(subject.getSubjectAreaAbbreviation()+"_"+reportName+"."+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                        if (report instanceof InstructorExamReport && iForm.getEmailInstructors()) {
                            ireports = ((InstructorExamReport)report).printInstructorReports(iForm.getModeIdx(), name, new FileGenerator(name));
                        } else if (report instanceof StudentExamReport && iForm.getEmailStudents()) {
                            sreports = ((StudentExamReport)report).printStudentReports(iForm.getModeIdx(), name, new FileGenerator(name));
                        }
                    }
                }
            }
        	iProgress = 0.9;
            byte[] buffer = new byte[32*1024];
            int len = 0;
            if (output.isEmpty())
                log("<font color='orange'>No report generated.</font>");
            else if (iForm.getEmail()) {
                setStatus("Sending email(s)...");
                if (iForm.getEmailDeputies()) {
                    Hashtable<TimetableManager,Hashtable<String,File>> files2send = new Hashtable();
                    for (Map.Entry<SubjectArea, Hashtable<String,File>> entry : outputPerSubject.entrySet()) {
                        if (entry.getKey().getDepartment().getTimetableManagers().isEmpty())
                            log("<font color='orange'>&nbsp;&nbsp;No manager associated with subject area "+entry.getKey().getSubjectAreaAbbreviation()+
                                " ("+entry.getKey().getDepartment().getLabel()+")</font>");
                        for (Iterator i=entry.getKey().getDepartment().getTimetableManagers().iterator();i.hasNext();) {
                            TimetableManager g = (TimetableManager)i.next();
                            boolean receiveEmail = true;
                            for (ManagerRole mr : (Set<ManagerRole>)g.getManagerRoles()){
                            	if (!mr.getRole().hasRight(Right.DepartmentIndependent)) {
                            		receiveEmail = mr.isReceiveEmails() == null?false:mr.isReceiveEmails().booleanValue();
                            		break;
                            	}
                            }
                            if (receiveEmail){
                                if (g.getEmailAddress()==null || g.getEmailAddress().length()==0) {
                                    log("<font color='orange'>&nbsp;&nbsp;Manager "+g.getName()+" has no email address.</font>");
                                } else {
                                    Hashtable<String,File> files = files2send.get(g);
                                    if (files==null) { files = new Hashtable<String,File>(); files2send.put(g, files); }
                                    files.putAll(entry.getValue());
                                }
                            }
                        }
                    }
                    if (files2send.isEmpty()) {
                        log("<font color='red'>Nothing to send.</font>");
                    } else {
                        Set<TimetableManager> managers = files2send.keySet();
                        while (!managers.isEmpty()) {
                            TimetableManager manager = managers.iterator().next();
                            Hashtable<String,File> files = files2send.get(manager);
                            managers.remove(manager);
                            log("Sending email to "+manager.getName()+" ("+manager.getEmailAddress()+")...");
                            try {
                                Email mail = Email.createEmail();
                                mail.setSubject(iForm.getSubject()==null?"Examination Report":iForm.getSubject());
                                mail.setText((iForm.getMessage()==null?"":iForm.getMessage()+"\r\n\r\n")+
                                        "For an up-to-date examination report, please visit "+
                                        iUrl+"/\r\n\r\n"+
                                        "This email was automatically generated by "+
                                        "UniTime "+Constants.getVersion()+
                                        " (Univesity Timetabling Application, http://www.unitime.org).");
                                mail.addRecipient(manager.getEmailAddress(), manager.getName());
                                for (Iterator<TimetableManager> i=managers.iterator();i.hasNext();) {
                                    TimetableManager m = (TimetableManager)i.next();
                                    if (files.equals(files2send.get(m))) {
                                        log("&nbsp;&nbsp;Including "+m.getName()+" ("+m.getEmailAddress()+")");
                                        mail.addRecipient(m.getEmailAddress(),m.getName());
                                        i.remove();
                                    }
                                }
                                if (iForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                                    mail.addRecipient(s.nextToken(), null);
                                if (iForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                                    mail.addRecipientCC(s.nextToken(), null);
                                if (iForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                                    mail.addRecipientBCC(s.nextToken(), null);
                                for (Map.Entry<String, File> entry : files.entrySet()) {
                                	mail.addAttachement(entry.getValue(), session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference()+"_"+entry.getKey());
                                    log("&nbsp;&nbsp;Attaching <a href='temp/"+entry.getValue().getName()+"'>"+entry.getKey()+"</a>");
                                }
                                mail.send();
                                log("Email sent.");
                            } catch (Exception e) {
                                log("<font color='red'>Unable to send email: "+e.getMessage()+"</font>");
                                setError(e);
                            }
                        }
                    }
                } else {
                    try {
                    	Email mail = Email.createEmail();
                        mail.setSubject(iForm.getSubject()==null?"Examination Report":iForm.getSubject());
                        mail.setText((iForm.getMessage()==null?"":iForm.getMessage()+"\r\n\r\n")+
                                "For an up-to-date examination report, please visit "+
                                iUrl+"/\r\n\r\n"+
                                "This email was automatically generated by "+
                                "UniTime "+Constants.getVersion()+
                                " (Univesity Timetabling Application, http://www.unitime.org).");
                        if (iForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipient(s.nextToken(), null);
                        if (iForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipientCC(s.nextToken(), null);
                        if (iForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipientBCC(s.nextToken(), null);
                        for (Map.Entry<String, File> entry : output.entrySet()) {
                        	mail.addAttachement(entry.getValue(), session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference()+"_"+entry.getKey());
                        }
                        mail.send();
                        log("Email sent.");
                    } catch (Exception e) {
                        log("<font color='red'>Unable to send email: "+e.getMessage()+"</font>");
                        setError(e);
                    }
                }
                if (iForm.getEmailInstructors() && ireports!=null && !ireports.isEmpty()) {
                    setStatus("Emailing instructors...");
                    for (ExamInstructorInfo instructor : new TreeSet<ExamInstructorInfo>(ireports.keySet())) {
                        File report = ireports.get(instructor);
                        String email = instructor.getInstructor().getEmail();
                        if (email==null || email.length()==0) {
                            log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+instructor.getName()+"</a> -- instructor has no email address.</font>");
                            continue;
                        }
                        try {
                        	Email mail = Email.createEmail();
                            mail.setSubject(iForm.getSubject()==null?"Examination Report":iForm.getSubject());
                            mail.setText((iForm.getMessage()==null?"":iForm.getMessage()+"\r\n\r\n")+
                                    "For an up-to-date examination report, please visit "+
                                    iUrl+"/exams.do\r\n\r\n"+
                                    "This email was automatically generated by "+
                                    "UniTime "+Constants.getVersion()+
                                    " (Univesity Timetabling Application, http://www.unitime.org).");
                            mail.addRecipient(email, null);
                            if (iForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                                mail.addRecipientCC(s.nextToken(), null);
                            if (iForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                                mail.addRecipientBCC(s.nextToken(), null);
                            mail.addAttachement(report, session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference()+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?".txt":".pdf"));
                            mail.send();
                            log("&nbsp;&nbsp;An email was sent to <a href='temp/"+report.getName()+"'>"+instructor.getName()+"</a>.");
                        } catch (Exception e) {
                            log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+instructor.getName()+"</a> -- "+e.getMessage()+".</font>");
                            setError(e);
                        }
                    }
                    log("Emails sent.");
                }
                if (iForm.getEmailStudents() && sreports!=null && !sreports.isEmpty()) {
                    setStatus("Emailing students...");
                    for (Student student : new TreeSet<Student>(sreports.keySet())) {
                        File report = sreports.get(student);
                        String email = student.getEmail();
                        if (email==null || email.length()==0) {
                            log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+student.getName(DepartmentalInstructor.sNameFormatLastFist)+"</a> -- student has no email address.</font>");
                            continue;
                        }
                        try {
                            Email mail = Email.createEmail();
                            mail.setSubject(iForm.getSubject()==null?"Examination Report":iForm.getSubject());
                            mail.setText((iForm.getMessage()==null?"":iForm.getMessage()+"\r\n\r\n")+
                                    "For an up-to-date examination report, please visit "+
                                    iUrl+"/exams.do\r\n\r\n"+
                                    "This email was automatically generated by "+
                                    "UniTime "+Constants.getVersion()+
                                    " (Univesity Timetabling Application, http://www.unitime.org).");
                            mail.addRecipient(email, null);
                            if (iForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                                mail.addRecipientCC(s.nextToken(), null);
                            if (iForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                                mail.addRecipientBCC(s.nextToken(), null);
                            mail.addAttachement(report, session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference()+(iForm.getModeIdx()==PdfLegacyExamReport.sModeText?".txt":".pdf"));
                            mail.send();
                            log("&nbsp;&nbsp;An email was sent to <a href='temp/"+report.getName()+"'>"+student.getName(DepartmentalInstructor.sNameFormatLastFist)+"</a>.");
                        } catch (Exception e) {
                            log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+student.getName(DepartmentalInstructor.sNameFormatLastFist)+"</a> -- "+e.getMessage()+".</font>");
                            setError(e);
                        }
                    }
                    log("Emails sent.");
                }
            }
            if (output.isEmpty()) {
                throw new Exception("Nothing generated.");
            } else if (output.size()==1) {
            	setOutput(output.elements().nextElement());
            } else {
                FileInputStream fis = null;
                ZipOutputStream zip = null;
                try {
                    File zipFile = ApplicationProperties.getTempFile(session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference(), "zip");
                    log("Writing <a href='temp/"+zipFile.getName()+"'>"+session.getAcademicTerm()+session.getSessionStartYear()+ExamTypeDAO.getInstance().get(iForm.getExamType()).getReference()+".zip</a>...");
                    zip = new ZipOutputStream(new FileOutputStream(zipFile));
                    for (Map.Entry<String, File> entry : output.entrySet()) {
                        zip.putNextEntry(new ZipEntry(entry.getKey()));
                        fis = new FileInputStream(entry.getValue());
                        while ((len=fis.read(buffer))>0) zip.write(buffer, 0, len);
                        fis.close(); fis = null;
                        zip.closeEntry();
                    }
                    zip.flush(); zip.close();
                    setOutput(zipFile);
                } catch (IOException e) {
                    if (fis!=null) fis.close();
                    if (zip!=null) zip.close();
                    setError(e);
                }
            }
        	iProgress = 1.0;
            setStatus("All done.");
        } catch (Exception e) {
            log("<font color='red'>Process failed: "+e.getMessage()+" (exception "+e.getClass().getName()+")</font>");
            sLog.error(e.getMessage(),e);
            setError(e);
        }
	}

	@Override
	public String name() {
		return iName;
	}

	@Override
	public double progress() {
		return iProgress;
	}
	
	@Override
	public String type() {
		return TYPE;
	}
	
	public static class FileGenerator implements InstructorExamReport.FileGenerator {
        String iName;
        public FileGenerator(String name) {
            iName = name;
        }
        public File generate(String prefix, String ext) {
            return ApplicationProperties.getTempFile(iName+"_"+prefix, ext);
        }
    }

}
