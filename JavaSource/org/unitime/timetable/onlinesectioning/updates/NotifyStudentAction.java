package org.unitime.timetable.onlinesectioning.updates;

import org.unitime.timetable.ApplicationProperties;
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

public class NotifyStudentAction implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private Long iStudentId;
	private XOffering iOldOffering;
	private XEnrollment iOldEnrollment;
	private XStudent iOldStudent;
	
	public NotifyStudentAction(Long studentId, XOffering oldOffering, XEnrollment oldEnrollment) {
		iStudentId = studentId;
		iOldOffering = oldOffering;
		iOldEnrollment = oldEnrollment;
	}
	
	public NotifyStudentAction(Long studentId, XStudent oldStudent) {
		iStudentId = studentId;
		iOldStudent = oldStudent;
	}
	
	public Long getStudentId() { return iStudentId; }

	@Override
	public Boolean execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		XStudent student = server.getStudent(getStudentId());
		if (student != null) {
			if (iOldOffering != null) {
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
					message += "\n    " + request;
					if (iOldEnrollment.getApproval() != null)
						message += " (approved by " + iOldEnrollment.getApproval().getName() + ")";
					for (XSection section: iOldOffering.getSections(iOldEnrollment))
						message += "\n      " + courseName + " " + section.toString(iOldEnrollment.getCourseId());
				} else {
					message += "\n    " + request + " NOT ASSIGNED";
				}
				message += "\n  New assignment:";
				XOffering offering = server.getOffering(iOldOffering.getOfferingId());
				if (offering == null || request == null || request.getEnrollment() == null) {
					message += "\n    " + request + " NOT ASSIGNED";
				} else {
					message += "\n    " + request;
					if (request.getEnrollment().getApproval() != null)
						message += " (approved by " + request.getEnrollment().getApproval().getName() + ")";
					for (XSection section: offering.getSections(request.getEnrollment())) {
						message += "\n      " + courseName + " " + section.toString(request.getEnrollment().getCourseId());
					}
				}
				helper.info(message);
				if (server.getAcademicSession().isSectioningEnabled() && "true".equals(ApplicationProperties.getProperty("unitime.enrollment.email", "true"))) {
					server.execute(new StudentEmail(getStudentId(), iOldOffering, iOldEnrollment), helper.getUser(), new ServerCallback<Boolean>() {
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
				helper.info(message);
				if (server.getAcademicSession().isSectioningEnabled() && "true".equals(ApplicationProperties.getProperty("unitime.enrollment.email", "true"))) {
					server.execute(new StudentEmail(getStudentId(), iOldStudent), helper.getUser(), new ServerCallback<Boolean>() {
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
