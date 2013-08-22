/*
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class RejectEnrollmentsAction implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iOfferingId;
	private Collection<Long> iStudentIds;
	private Collection<Long> iCourseIdsCanApprove;
	private String iApproval;
	
	public RejectEnrollmentsAction(Long offeringId, Collection<Long> studentIds, Collection<Long> courseIdsCanApprove, String approval) {
		iOfferingId = offeringId;
		iStudentIds = studentIds;
		iCourseIdsCanApprove = courseIdsCanApprove;
		iApproval = approval;
	}
	
	public Long getOfferingId() { return iOfferingId; }
	public Collection<Long> getStudentIds() { return iStudentIds; }
	public String getApproval() { return iApproval; }
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		try {
			helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getOfferingId())
					.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));

			String[] approval = getApproval().split(":");
			
			Lock lock = server.lockOffering(getOfferingId(), getStudentIds(), false);
			try {
				
				Offering offering = server.getOffering(getOfferingId());
				if (offering == null) 
					throw new SectioningException(MSG.exceptionBadOffering());
				
				for (Config config: offering.getConfigs())
					for (Enrollment enrollment: new ArrayList<Enrollment>(config.getEnrollments())) {
						if (getStudentIds().contains(enrollment.getStudent().getId()) && iCourseIdsCanApprove.contains(enrollment.getCourse().getId())) {
							
							OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
							action.setStudent(
									OnlineSectioningLog.Entity.newBuilder()
									.setUniqueId(enrollment.getStudent().getId())
									.setExternalId(enrollment.getStudent().getExternalId()));
							action.addRequest(OnlineSectioningHelper.toProto(enrollment.getRequest()));
							OnlineSectioningLog.Enrollment.Builder enrl = OnlineSectioningLog.Enrollment.newBuilder();
							enrl.setType(OnlineSectioningLog.Enrollment.EnrollmentType.REJECTED);
							for (Assignment assignment: enrollment.getAssignments())
								enrl.addSection(OnlineSectioningHelper.toProto(assignment, enrollment));
							action.addOther(OnlineSectioningLog.Entity.newBuilder()
									.setUniqueId(offering.getId())
									.setName(offering.getName())
									.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
							action.addOther(OnlineSectioningLog.Entity.newBuilder()
									.setName(approval[2])
									.setExternalId(approval[1])
									.setType(OnlineSectioningLog.Entity.EntityType.MANAGER));
							action.addEnrollment(enrl);
							
							org.unitime.timetable.model.CourseRequest request = null;
							for (StudentClassEnrollment e: (List<StudentClassEnrollment>)helper.getHibSession().createQuery(
									"from StudentClassEnrollment e where e.student.uniqueId = :studentId and e.courseOffering.instructionalOffering = :offeringId")
									.setLong("studentId", enrollment.getStudent().getId())
									.setLong("offeringId", getOfferingId())
									.list()) {
								if (request == null)
									request = e.getCourseRequest();
								helper.getHibSession().delete(e);
							}
							if (request != null)
								helper.getHibSession().delete(request.getCourseDemand());
							CourseRequest cr = (CourseRequest)enrollment.getRequest();
							enrollment.getRequest().unassign(0);
							for (Course course: cr.getCourses())
								course.getRequests().remove(cr);
							enrollment.getStudent().getRequests().remove(cr);
							cr.setInitialAssignment(null);
							server.notifyStudentChanged(enrollment.getStudent().getId(), cr, enrollment, helper.getUser());
						}
					}
			} finally {
				lock.release();
			}

			helper.commitTransaction();
			
			/*
			server.execute(new UpdateEnrollmentCountsAction(getOfferingId()), helper.getUser(), new OnlineSectioningServer.ServerCallback<Boolean>() {
				@Override
				public void onFailure(Throwable exception) {
				}
				@Override
				public void onSuccess(Boolean result) {
				}
			});
			*/
			
			return true;			
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}

	@Override
	public String name() { return "reject-enrollments"; }

}
