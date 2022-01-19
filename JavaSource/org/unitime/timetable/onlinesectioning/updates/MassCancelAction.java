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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeModes;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentFailure;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentRequest;
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
	private String iStatus;
	private boolean iEmail = false;
	private String iSubject;
	private String iMessage;
	private String iCC;
	
	public MassCancelAction forStudents(List<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	public MassCancelAction withStatus(String status) {
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
	public String getStatus() { return iStatus; }
	public boolean hasStatus() { return iStatus != null && !iStatus.isEmpty(); }
	public boolean changeStatus() { return !"-".equals(iStatus); }
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
		
		StudentSectioningStatus status = (changeStatus() && hasStatus() ? (StudentSectioningStatus)helper.getHibSession().createQuery(
				"from StudentSectioningStatus where reference = :ref and (session is null or session = :sessionId)").setString("ref", getStatus()).setLong("sessionId", server.getAcademicSession().getUniqueId()).uniqueResult() : null);
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, name());
			try {
				helper.beginTransaction();
				try {
					Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					if (student != null) {
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession(), true);
						WaitListMode wlMode = student.getWaitListMode();
						
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student.getUniqueId())
							.setExternalId(student.getExternalUniqueId())
							.setName(helper.getStudentNameFormat().format(student)));
						
						if (status != null) {
							action.addOther(OnlineSectioningLog.Entity.newBuilder()
									.setUniqueId(status.getUniqueId())
									.setName(status.getLabel())
									.setExternalId(status.getReference())
									.setType(OnlineSectioningLog.Entity.EntityType.OTHER));
						}
						
						XStudent oldStudent = server.getStudent(studentId);
						if (oldStudent != null) {
							for (XRequest oldRequest: oldStudent.getRequests()) {
								XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
								if (oldEnrollment == null) continue; // free time or not assigned
								offeringsToCheck.add(oldEnrollment.getOfferingId());
								XOffering offering = server.getOffering(oldEnrollment.getOfferingId());
								EnrollStudent.updateSpace(server,
										null,
										oldEnrollment == null ? null : SectioningRequest.convert(oldStudent, (XCourseRequest)oldRequest, server, offering, oldEnrollment, wlMode),
										offering);
							}
							OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
							enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
							for (XRequest oldRequest: oldStudent.getRequests()) {
								XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
								if (oldEnrollment != null)
									for (XSection section: server.getOffering(oldEnrollment.getOfferingId()).getSections(oldEnrollment))
										enrollment.addSection(OnlineSectioningHelper.toProto(section, oldEnrollment));
							}
							action.addEnrollment(enrollment);
						}
						
						Map<Long, Set<Long>> enrollmentsToKeep = null;
						if (CustomStudentEnrollmentHolder.hasProvider()) {
							Set<Long> lockedCourses = new HashSet<Long>();
							List<EnrollmentRequest> enrollments = new ArrayList<EnrollmentRequest>();
							GradeModes gradeModes = new GradeModes();
							boolean hasWaitListedCourses = false;
							List<EnrollmentFailure> failures = CustomStudentEnrollmentHolder.getProvider().enroll(server, helper, oldStudent, enrollments, lockedCourses, gradeModes, hasWaitListedCourses);
							for (EnrollmentFailure f: failures) {
								if (!f.isEnrolled()) continue;
								if (enrollmentsToKeep == null) enrollmentsToKeep = new HashMap<Long, Set<Long>>();
								Set<Long> sectionIds = enrollmentsToKeep.get(f.getCourse().getCourseId());
								if (sectionIds == null) {
									sectionIds = new HashSet<Long>();
									enrollmentsToKeep.put(f.getCourse().getCourseId(), sectionIds);
								}
								sectionIds.add(f.getSection().getSectionId());
							}
						}
						
						for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
							StudentClassEnrollment enrl = i.next();
							if (enrollmentsToKeep != null && enrollmentsToKeep.containsKey(enrl.getCourseOffering().getUniqueId()) && enrollmentsToKeep.get(enrl.getCourseOffering().getUniqueId()).contains(enrl.getClazz().getUniqueId())) continue;
							enrl.getClazz().getStudentEnrollments().remove(enrl);
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
								if (enrollmentsToKeep != null && enrollmentsToKeep.containsKey(cr.getCourseOffering().getUniqueId())) {
									cr.setOrder(0);
									helper.getHibSession().update(cr);
								} else {
									helper.getHibSession().delete(cr);
									j.remove();
								}
							}
							if (cd.getCourseRequests().isEmpty()) {
								helper.getHibSession().delete(cd);
								i.remove();
							} else {
								cd.setAlternative(false);
								cd.setWaitlist(false);
							}
						}
						
						if (!student.getCourseDemands().isEmpty()) {
							int priority = 0;
							for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
								cd.setPriority(priority ++);
								helper.getHibSession().update(cd);
							}
						}
						
						if (changeStatus())
							student.setSectioningStatus(status);
						
						helper.getHibSession().saveOrUpdate(student);
						helper.getHibSession().flush();
						
						XStudent newStudent = null;
						try {
							newStudent = ReloadAllData.loadStudent(student, null, server, helper, WaitList.WaitListType.MASS_CANCEL);
							if (newStudent != null) {
								server.update(newStudent, true);
								OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
								enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
								for (XRequest newRequest: newStudent.getRequests()) {
									action.addRequest(OnlineSectioningHelper.toProto(newRequest));
									if (newRequest instanceof XCourseRequest && ((XCourseRequest)newRequest).getEnrollment() != null) {
										XEnrollment enrl = ((XCourseRequest)newRequest).getEnrollment();
										XOffering offering = server.getOffering(enrl.getOfferingId());
										for (XSection section: offering.getSections(enrl))
											enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
									}
								}
								action.addEnrollment(enrollment);
							}
						} catch (Exception e) {
							if (e instanceof RuntimeException)
								throw (RuntimeException)e;
							throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
						}
						
						if (iEmail) {
							StudentEmail email = server.createAction(StudentEmail.class).forStudent(studentId).fromAction(name()).oldStudent(oldStudent);
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
