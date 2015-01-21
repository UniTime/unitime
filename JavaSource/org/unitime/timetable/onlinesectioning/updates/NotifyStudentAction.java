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

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.ServerCallback;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class NotifyStudentAction implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private Long iStudentId;
	private XOffering iOldOffering;
	private XEnrollment iOldEnrollment;
	private XStudent iOldStudent;
	
	public NotifyStudentAction forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public NotifyStudentAction oldEnrollment(XOffering oldOffering, XEnrollment oldEnrollment) {
		iOldOffering = oldOffering;
		iOldEnrollment = oldEnrollment;
		return this;
	}
	
	public NotifyStudentAction oldStudent(XStudent oldStudent) {
		iOldStudent = oldStudent;
		return this;
	}
	
	public Long getStudentId() { return iStudentId; }

	@Override
	public Boolean execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		XStudent student = server.getStudent(getStudentId());
		if (student != null) {
			if (iOldOffering != null) {
				if (iOldEnrollment != null && !iOldOffering.getOfferingId().equals(iOldEnrollment.getOfferingId()))
					iOldOffering = server.getOffering(iOldEnrollment.getOfferingId());
				String message = "Student " + student.getName() + " (" + student.getStudentId() + ") changed.";
				String courseName = (iOldEnrollment == null ? iOldOffering.getName() : iOldOffering.getCourse(iOldEnrollment.getCourseId()).getCourseName());
				XCourseRequest request = null;
				for (XRequest r: student.getRequests()) {
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						XCourseId id = cr.getCourseIdByOfferingId(iOldOffering.getOfferingId());
						if (id != null) { courseName = id.getCourseName(); request = cr; break; }
					}
				}
				message += "\n  Previous assignment:";
				if (iOldEnrollment != null) {
					message += "\n    " + (request == null ? iOldOffering.toString() : request.toString());
					if (iOldEnrollment.getApproval() != null)
						message += " (approved by " + iOldEnrollment.getApproval().getName() + ")";
					for (XSection section: iOldOffering.getSections(iOldEnrollment))
						message += "\n      " + courseName + " " + section.toString(iOldEnrollment.getCourseId());
				} else {
					message += "\n    " + (request == null ? iOldOffering.toString() : request.toString()) + " NOT ASSIGNED";
				}
				message += "\n  New assignment:";
				XOffering offering = server.getOffering(iOldOffering.getOfferingId());
				if (offering == null || request == null || request.getEnrollment() == null) {
					message += "\n    " + (request == null ? iOldOffering.toString() : request.toString()) + " NOT ASSIGNED";
				} else {
					message += "\n    " + request;
					if (request.getEnrollment().getApproval() != null)
						message += " (approved by " + request.getEnrollment().getApproval().getName() + ")";
					for (XSection section: offering.getSections(request.getEnrollment())) {
						message += "\n      " + courseName + " " + section.toString(request.getEnrollment().getCourseId());
					}
				}
				helper.debug(message);
				if (server.getAcademicSession().isSectioningEnabled() && ApplicationProperty.OnlineSchedulingEmailConfirmation.isTrue()) {
					server.execute(server.createAction(StudentEmail.class).forStudent(getStudentId()).oldEnrollment(iOldOffering, iOldEnrollment), helper.getUser(), new ServerCallback<Boolean>() {
						@Override
						public void onFailure(Throwable exception) {
							helper.error("Failed to notify student: " + exception.getMessage(), exception);
						}
						@Override
						public void onSuccess(Boolean result) {
						}
					});
				}
				return true;
			} else {
				String message = "Student " + student.getName() + " (" + student.getStudentId() + ") changed.";
				if (iOldStudent != null) {
					message += "\n  Previous schedule:";
					for (XRequest r: iOldStudent.getRequests()) {
						message += "\n    " + r;
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							if (cr.getEnrollment() == null)
								message += " NOT ASSIGNED";
							else {
								if (cr.getEnrollment().getApproval() != null)
									message += " (approved by " + cr.getEnrollment().getApproval().getName() + ")";
								XOffering offering = server.getOffering(cr.getEnrollment().getOfferingId());
								if (offering != null) {
									XCourse course = offering.getCourse(cr.getEnrollment().getCourseId());
									for (XSection section: offering.getSections(cr.getEnrollment()))
										message += "\n      " + (course == null ? offering.getName() : course.getCourseName()) + " " + section.toString(cr.getEnrollment().getCourseId());
								}
							}
						}
					}
				}
				message += "\n  New schedule:";
				for (XRequest r: student.getRequests()) {
					message += "\n    " + r;
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						if (cr.getEnrollment() == null)
							message += " NOT ASSIGNED";
						else {
							if (cr.getEnrollment().getApproval() != null)
								message += " (approved by " + cr.getEnrollment().getApproval().getName() + ")";
							XOffering offering = server.getOffering(cr.getEnrollment().getOfferingId());
							if (offering != null) {
								XCourse course = offering.getCourse(cr.getEnrollment().getCourseId());
								for (XSection section: offering.getSections(cr.getEnrollment()))
									message += "\n      " + (course == null ? offering.getName() : course.getCourseName()) + " " + section.toString(cr.getEnrollment().getCourseId());
							}
						}
					}
				}
				helper.debug(message);
				if (server.getAcademicSession().isSectioningEnabled() && ApplicationProperty.OnlineSchedulingEmailConfirmation.isTrue()) {
					server.execute(server.createAction(StudentEmail.class).forStudent(getStudentId()).oldStudent(iOldStudent), helper.getUser(), new ServerCallback<Boolean>() {
						@Override
						public void onFailure(Throwable exception) {
							helper.error("Failed to notify student: " + exception.getMessage(), exception);
						}
						@Override
						public void onSuccess(Boolean result) {
						}
					});
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String name() {
		return "notify";
	}

}
