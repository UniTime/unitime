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

import java.util.Collection;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class RejectEnrollmentsAction implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iOfferingId;
	private Collection<Long> iStudentIds;
	private Collection<Long> iCourseIdsCanApprove;
	private String iApproval;
	
	public RejectEnrollmentsAction withParams(Long offeringId, Collection<Long> studentIds, Collection<Long> courseIdsCanApprove, String approval) {
		iOfferingId = offeringId;
		iStudentIds = studentIds;
		iCourseIdsCanApprove = courseIdsCanApprove;
		iApproval = approval;
		return this;
	}
	
	public Long getOfferingId() { return iOfferingId; }
	public Collection<Long> getStudentIds() { return iStudentIds; }
	public String getApproval() { return iApproval; }
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.lockOffering(getOfferingId(), getStudentIds(), name());
		try {
			helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getOfferingId())
					.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));

			String[] approval = getApproval().split(":");
			
			helper.beginTransaction();
			try {
				
				XOffering offering = server.getOffering(getOfferingId());
				if (offering == null) 
					throw new SectioningException(MSG.exceptionBadOffering());
				XEnrollments enrollments = server.getEnrollments(getOfferingId());
				
				for (XCourseRequest request: enrollments.getRequests()) {
					XEnrollment enrollment = request.getEnrollment();
					if (enrollment == null || !enrollment.getOfferingId().equals(getOfferingId())) continue;
					if (getStudentIds().contains(enrollment.getStudentId()) && iCourseIdsCanApprove.contains(enrollment.getCourseId())) {
						
						XStudent student = server.getStudent(enrollment.getStudentId());
						
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						action.setStudent(
								OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(enrollment.getStudentId())
								.setExternalId(student.getExternalId())
								.setName(student.getName()));
						action.addRequest(OnlineSectioningHelper.toProto(request));
						OnlineSectioningLog.Enrollment.Builder enrl = OnlineSectioningLog.Enrollment.newBuilder();
						enrl.setType(OnlineSectioningLog.Enrollment.EnrollmentType.REJECTED);
						for (XSection section: offering.getSections(enrollment))
							enrl.addSection(OnlineSectioningHelper.toProto(section, enrollment));
						action.addOther(OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(offering.getOfferingId())
								.setName(offering.getName())
								.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
						action.addOther(OnlineSectioningLog.Entity.newBuilder()
								.setName(approval[2])
								.setExternalId(approval[1])
								.setType(OnlineSectioningLog.Entity.EntityType.MANAGER));
						action.addEnrollment(enrl);
						
						CourseRequest cr = null;
						for (StudentClassEnrollment e: (List<StudentClassEnrollment>)helper.getHibSession().createQuery(
								"from StudentClassEnrollment e where e.student.uniqueId = :studentId and e.courseOffering.instructionalOffering = :offeringId")
								.setLong("studentId", enrollment.getStudentId())
								.setLong("offeringId", getOfferingId())
								.list()) {
							if (cr == null) cr = e.getCourseRequest();
							helper.getHibSession().delete(e);
						}
						if (cr != null)
							helper.getHibSession().delete(cr.getCourseDemand());
						student.getRequests().remove(request);
						server.update(student, true);
						
						server.execute(server.createAction(NotifyStudentAction.class).forStudent(enrollment.getStudentId()).oldEnrollment(offering, offering.getCourse(enrollment.getCourseId()), enrollment), helper.getUser());
					}
					
				}

				helper.commitTransaction();
				
				return true;			
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() { return "reject-enrollments"; }

}
