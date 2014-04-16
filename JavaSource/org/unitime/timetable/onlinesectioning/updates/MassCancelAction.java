/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class MassCancelAction implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private List<Long> iStudentIds;
	private StudentSectioningStatus iStatus;
	private boolean iEmail = false;
	private String iSubject;
	private String iMessage;
	private String iCC;
	
	public MassCancelAction forStudents(List<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	public MassCancelAction withStatus(StudentSectioningStatus status) {
		iStatus = status;
		return this;
	}
	
	public MassCancelAction withEmail(String subject, String message, String cc) {
		iEmail = true;
		iSubject = subject;
		iMessage = message;
		iCC = cc;
		return this;
	}

	public List<Long> getStudentIds() { return iStudentIds; }
	public StudentSectioningStatus getStatus() { return iStatus; }
	public String getSubject() { return iSubject; }
	public String getMessage() { return iMessage; }
	public String getCC() { return iCC; }

	@Override
	public Boolean execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		Exception caughtException = null;
		Set<Long> offeringsToCheck = new HashSet<Long>();
		
		OnlineSectioningServer.ServerCallback<Boolean> emailSent = new OnlineSectioningServer.ServerCallback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Student email failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, true);
			try {
				helper.beginTransaction();
				try {
					Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					if (student != null) {
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student.getUniqueId())
							.setExternalId(student.getExternalUniqueId())
							.setName(student.getName(DepartmentalInstructor.sNameFormatFirstMiddleLast)));
						
						for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
							StudentClassEnrollment enrl = i.next();
							enrl.getClazz().getStudentEnrollments().remove(enrl);
							/*
							if (enrl.getCourseRequest() != null)
								enrl.getCourseRequest().getClassEnrollments().remove(enrl);
								*/
							helper.getHibSession().delete(enrl);
							i.remove();
						}

						for (Iterator<CourseDemand> i = student.getCourseDemands().iterator(); i.hasNext(); ) {
							CourseDemand cd = i.next();
							if (cd.getFreeTime() != null)
								helper.getHibSession().delete(cd.getFreeTime());
							for (Iterator<CourseRequest> j = cd.getCourseRequests().iterator(); j.hasNext(); ) {
								CourseRequest cr = j.next();
								for (Iterator<ClassWaitList> k = cr.getClassWaitLists().iterator(); k.hasNext(); ) {
									helper.getHibSession().delete(k.next());
									k.remove();
								}
								helper.getHibSession().delete(cr);
								j.remove();
							}
							helper.getHibSession().delete(cd);
							i.remove();
						}
						
						student.setSectioningStatus(getStatus());
						
						helper.getHibSession().saveOrUpdate(student);
						helper.getHibSession().flush();
						
						XStudent oldStudent = server.getStudent(studentId);
						XStudent newStudent = null;
						try {
							newStudent = ReloadAllData.loadStudent(student, null, server, helper);
							server.update(newStudent, true);
						} catch (Exception e) {
							if (e instanceof RuntimeException)
								throw (RuntimeException)e;
							throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
						}
						
						if (oldStudent != null) {
							for (XRequest oldRequest: oldStudent.getRequests()) {
								XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
								if (oldEnrollment == null) continue; // free time or not assigned
								offeringsToCheck.add(oldEnrollment.getOfferingId());
								XOffering offering = server.getOffering(oldEnrollment.getOfferingId());
								EnrollStudent.updateSpace(server,
										null,
										oldEnrollment == null ? null : SectioningRequest.convert(oldStudent, (XCourseRequest)oldRequest, server, offering, oldEnrollment),
										offering);
							}
							OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
							enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
							for (XRequest oldRequest: oldStudent.getRequests()) {
								XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
								if (oldEnrollment != null)
									for (XSection section: server.getOffering(oldEnrollment.getOfferingId()).getSections(oldEnrollment))
										enrollment.addSection(OnlineSectioningHelper.toProto(section, oldEnrollment));
							}
							action.addEnrollment(enrollment);
						}
						
						if (iEmail && "true".equals(ApplicationProperties.getProperty("unitime.enrollment.email", "true"))) {
							StudentEmail email = server.createAction(StudentEmail.class).forStudent(studentId).oldStudent(oldStudent);
							email.setCC(getCC());
							email.setEmailSubject(getSubject() == null || getSubject().isEmpty() ? MSG.defaulSubjectMassCancel() : getSubject());
							email.setMessage(getMessage());
							server.execute(email, helper.getUser(), emailSent);
						}
					}
					helper.commitTransaction();
				} catch (Exception e) {
					helper.rollbackTransaction();
					caughtException = e;
				}
			} finally {
				lock.release();
			}
		}
		
		OnlineSectioningServer.ServerCallback<Boolean> offeringChecked = new OnlineSectioningServer.ServerCallback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Offering check failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		
		for (Long offeringId: offeringsToCheck) {
			server.persistExpectedSpaces(offeringId);
			server.execute(server.createAction(CheckOfferingAction.class).forOfferings(offeringId), helper.getUser(), offeringChecked);
		}
		
		if (caughtException != null) {
			if (caughtException instanceof SectioningException)
				throw (SectioningException)caughtException;
			throw new SectioningException(MSG.exceptionUnknown(caughtException.getMessage()), caughtException);
		}
		
		return true;
	}

	@Override
	public String name() {
		return "mass-cancel";
	}
}
