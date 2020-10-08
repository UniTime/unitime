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
package org.unitime.timetable.onlinesectioning.advisors;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestPriority;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.model.AdvisorClassPref;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorInstrMthPref;
import org.unitime.timetable.model.AdvisorSectioningPref;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.StudentHoldsCheckProvider;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XGroupReservation;
import org.unitime.timetable.onlinesectioning.model.XIndividualReservation;
import org.unitime.timetable.onlinesectioning.model.XLearningCommunityReservation;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest.XPreference;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.Formats.Format;

/**
 * @author Tomas Muller
 */
public class AdvisorGetCourseRequests implements OnlineSectioningAction<CourseRequestInterface> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private boolean iCheckExistingDemands = false;
	private boolean iCheckHolds = false;
	
	public AdvisorGetCourseRequests forStudent(Long id) {
		iStudentId = id;
		return this;
	}
	
	public AdvisorGetCourseRequests checkDemands(boolean check) {
		iCheckExistingDemands = check;
		return this;
	}
	
	public AdvisorGetCourseRequests checkHolds(boolean check) {
		iCheckHolds = check;
		return this;
	}

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (server instanceof DatabaseServer) return executeDB(server, helper);
		
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
		
		XStudent student = server.getStudent(iStudentId);
		if (student != null) {
			action.getStudentBuilder().setExternalId(student.getExternalId());
			action.getStudentBuilder().setName(student.getName());
			if (student.getStatus() != null)
				action.addOptionBuilder().setKey("status").setValue(student.getStatus());

		}
		
		CourseRequestInterface request = new CourseRequestInterface();
		request.setStudentId(iStudentId);
		request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
		
		if (student != null && iCheckExistingDemands) {
			Format<Date> ts = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
			
			for (XRequest xr: student.getRequests()) {
				CourseRequestInterface.Request r = null;
				if (xr instanceof XCourseRequest) {
					XCourseRequest xcr = (XCourseRequest)xr;
					r = new CourseRequestInterface.Request();
					boolean enrolled = false, reserved = false;
					for (XCourseId course: xcr.getCourseIds()) {
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(course.getCourseId());
						rc.setCourseName(course.getCourseName() + (!CONST.showCourseTitle() || course.getTitle() == null || course.getTitle().isEmpty() ? "" : " - " + course.getTitle()));
						rc.setCourseTitle(course.getTitle());
						XCourse xcourse = server.getCourse(course.getCourseId());
						if (xcourse != null) {
							rc.setCredit(xcourse.getMinCredit(), xcourse.getMaxCredit());
						}
						Date hasEnrollments = null;
						if (xcr.getEnrollment() != null && xcr.getEnrollment().equals(course))
							hasEnrollments = xcr.getEnrollment().getTimeStamp();
						rc.setReadOnly(hasEnrollments != null);
						rc.setCanDelete(hasEnrollments == null);
						if (hasEnrollments != null) {
							enrolled = true;
							r.addAdvisorNote(MSG.noteEnrolled(course.getCourseName(), ts.format(hasEnrollments)));
						}
						XOffering offering = server.getOffering(course.getOfferingId());
						if (offering != null && offering.hasReservations()) {
							for (XReservation reservation: offering.getReservations()) {
								if (reservation instanceof XIndividualReservation || reservation instanceof XGroupReservation || reservation instanceof XLearningCommunityReservation) {
									if (reservation.mustBeUsed() && reservation.isApplicable(student, course)) {
										if (reservation instanceof XGroupReservation)
											r.addAdvisorNote(MSG.noteHasGroupReservation(((XGroupReservation)reservation).getGroup().getTitle()));
										else if (reservation instanceof XLearningCommunityReservation)
											r.addAdvisorNote(MSG.noteHasGroupReservation(((XLearningCommunityReservation)reservation).getGroup().getTitle()));
										else
											r.addAdvisorNote(MSG.noteHasIndividualReservation());
										reserved = true;
										break;
									}
								}
							}
						}
						xcr.fillPreferencesIn(rc, course);
						r.addRequestedCourse(rc);
					}
					if (r.hasRequestedCourse() && (enrolled || reserved) && !student.hasAdvisorRequests()) {
						if (xr.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					r.setWaitList(xcr.isWaitlist());
					r.setCritical(xcr.getCritical());
					r.setTimeStamp(xcr.getTimeStamp());
				}
			}
		}
	
		
		if (student.hasAdvisorRequests())
			fillCourseRequests(request, student.getAdvisorRequests(), server);
		
		for (OnlineSectioningLog.Request log: OnlineSectioningHelper.toProto(request))
			action.addRequest(log);
		
		request.setPin(student.getPin());
		request.setPinReleased(student.isPinReleased());
		
		if (iCheckHolds && Customization.StudentHoldsCheckProvider.hasProvider()) {
			try {
				StudentHoldsCheckProvider provider = Customization.StudentHoldsCheckProvider.getProvider();
				request.setErrorMessage(provider.getStudentHoldError(server, helper, student));
				for (OnlineSectioningLog.Property p: helper.getAction().getOptionList())
					if ("PIN".equals(p.getKey())) 
						request.setPin(p.getValue());
			} catch (Exception e) {
				helper.warn(MSG.exceptionFailedEligibilityCheck(e.getMessage()), e);
			}
		}
		
		// has pin but was not advised yet >> set the pin released default to true
		if (request.hasPin() && !student.hasAdvisorRequests()) request.setPinReleased(true);

		return request;

	}
	
	protected CourseRequestInterface executeDB(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));

		Student student = StudentDAO.getInstance().get(iStudentId, helper.getHibSession());
		if (student != null) {
			action.getStudentBuilder().setExternalId(student.getExternalUniqueId());
			action.getStudentBuilder().setName(NameFormat.LAST_FIRST_MIDDLE.format(student));
			if (student.getSectioningStatus() != null)
				action.addOptionBuilder().setKey("status").setValue(student.getSectioningStatus().getReference());
		}

		CourseRequestInterface request = new CourseRequestInterface();
		request.setStudentId(iStudentId);
		request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
		
		List<AdvisorCourseRequest> acrs = helper.getHibSession().createQuery(
				"from AdvisorCourseRequest where student = :studentId order by priority, alternative"
				).setLong("studentId", iStudentId).list();
		
		if (student != null && iCheckExistingDemands) {
			Format<Date> ts = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);

			TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
				public int compare(CourseDemand d1, CourseDemand d2) {
					if (d1.isAlternative() && !d2.isAlternative()) return 1;
					if (!d1.isAlternative() && d2.isAlternative()) return -1;
					int cmp = d1.getPriority().compareTo(d2.getPriority());
					if (cmp != 0) return cmp;
					return d1.getUniqueId().compareTo(d2.getUniqueId());
				}
			});
			demands.addAll(student.getCourseDemands());
			
			for (CourseDemand cd: demands) {
				CourseRequestInterface.Request r = null;
				if (!cd.getCourseRequests().isEmpty()) {
					r = new CourseRequestInterface.Request();
					boolean enrolled = false, reserved = false;
					for (CourseRequest course: new TreeSet<CourseRequest>(cd.getCourseRequests())) {
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(course.getCourseOffering().getUniqueId());
						rc.setCourseName(course.getCourseOffering().getSubjectAreaAbbv() + " " + course.getCourseOffering().getCourseNbr() + (!CONST.showCourseTitle() ? "" : " - " + course.getCourseOffering().getTitle()));
						rc.setCourseTitle(course.getCourseOffering().getTitle());
						CourseCreditUnitConfig credit = course.getCourseOffering().getCredit(); 
						if (credit != null) rc.setCredit(credit.getMinCredit(), credit.getMaxCredit());
						Date hasEnrollments = null;
						for (StudentClassEnrollment e: course.getClassEnrollments())
							if (hasEnrollments == null || hasEnrollments.before(e.getTimestamp()))
								hasEnrollments = e.getTimestamp();
						rc.setReadOnly(hasEnrollments != null);
						rc.setCanDelete(hasEnrollments == null);
						if (hasEnrollments != null) {
							rc.setStatus(RequestedCourseStatus.ENROLLED);
							enrolled = true;
						} else
							rc.setStatus(RequestedCourseStatus.SAVED);
						if (hasEnrollments != null) {
							rc.setCanChangeAlternatives(false);
							rc.setCanChangePriority(false);
							r.addAdvisorNote(MSG.noteEnrolled(course.getCourseOffering().getSubjectAreaAbbv() + " " + course.getCourseOffering().getCourseNbr(), ts.format(hasEnrollments)));
						}
						for (Reservation reservation: course.getCourseOffering().getInstructionalOffering().getReservations()) {
							if (reservation instanceof IndividualReservation || reservation instanceof StudentGroupReservation || reservation instanceof LearningCommunityReservation) {
								if (reservation.isMustBeUsed() && reservation.isApplicable(student, course)) { // !reservation.isExpired() && 
									rc.setReadOnly(true);
									rc.setCanDelete(false);
									rc.setCanChangeAlternatives(false);
									rc.setCanChangePriority(false);
									if (reservation instanceof StudentGroupReservation)
										r.addAdvisorNote(MSG.noteHasGroupReservation(((StudentGroupReservation)reservation).getGroup().getGroupName()));
									else if (reservation instanceof LearningCommunityReservation)
										r.addAdvisorNote(MSG.noteHasGroupReservation(((LearningCommunityReservation)reservation).getGroup().getGroupName()));
									else
										r.addAdvisorNote(MSG.noteHasIndividualReservation());
									reserved = true;
									break;
								}
							}
						}
						if (course.getPreferences() != null)
							for (StudentSectioningPref ssp: course.getPreferences()) {
								if (ssp instanceof StudentClassPref) {
									StudentClassPref scp = (StudentClassPref)ssp;
									rc.setSelectedClass(scp.getClazz().getUniqueId(), scp.getClazz().getClassPrefLabel(course.getCourseOffering()), scp.isRequired(), true);
								} else if (ssp instanceof StudentInstrMthPref) {
									StudentInstrMthPref imp = (StudentInstrMthPref)ssp;
									rc.setSelectedIntructionalMethod(imp.getInstructionalMethod().getUniqueId(), imp.getInstructionalMethod().getLabel(), imp.isRequired(), true);
								}
							}
						r.addRequestedCourse(rc);
					}
					if (r.hasRequestedCourse() && (enrolled || reserved) && acrs.isEmpty()) {
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					r.setWaitList(cd.getWaitlist());
					if (cd.getCriticalOverride() != null)
						r.setCritical(cd.getCriticalOverride());
					else
						r.setCritical(cd.getCritical());
					r.setTimeStamp(cd.getTimestamp());
				}
			}
		}
	
		
		fillCourseRequests(request, acrs);
		
		for (OnlineSectioningLog.Request log: OnlineSectioningHelper.toProto(request))
			action.addRequest(log);
		
		request.setPin(student.getPin());
		request.setPinReleased(student.isPinReleased() != null && student.isPinReleased().booleanValue());
		
		if (iCheckHolds && Customization.StudentHoldsCheckProvider.hasProvider()) {
			try {
				StudentHoldsCheckProvider provider = Customization.StudentHoldsCheckProvider.getProvider();
				request.setErrorMessage(provider.getStudentHoldError(server, helper, new XStudentId(student, helper)));
				for (OnlineSectioningLog.Property p: helper.getAction().getOptionList())
					if ("PIN".equals(p.getKey())) 
						request.setPin(p.getValue());
			} catch (Exception e) {
				helper.warn(MSG.exceptionFailedEligibilityCheck(e.getMessage()), e);
			}
		}
		
		// has pin but was not advised yet >> set the pin released default to true
		if (request.hasPin() && acrs.isEmpty()) request.setPinReleased(true);
		
		return request;
	}
	
	protected static void fillCourseRequests(CourseRequestInterface request, List<AdvisorCourseRequest> acrs) {
		int last = -1;
		CourseRequestInterface.Request r = null;
		Set<Integer> skip = new HashSet<Integer>();
		String note = null;
		for (AdvisorCourseRequest acr: acrs) {
			if (acr.getAlternative() == 0 || acr.getNotes() != null)
				note = acr.getNotes();
			if (acr.getCourseOffering() != null) {
				RequestPriority rp = request.getRequestPriority(new RequestedCourse(acr.getCourseOffering().getUniqueId(), acr.getCourseOffering().getCourseName()));
				if (rp != null) {
					if (note != null) rp.getRequest().addAdvisorNote(note);
					skip.add(acr.getPriority());
				}
			}
		}
		for (AdvisorCourseRequest acr: acrs) {
			if (skip.contains(acr.getPriority())) continue;
			if (acr.getPriority() == -1) {
				request.setCreditNote(acr.getNotes());
				continue;
			}
			if (r == null || last != acr.getPriority()) {
				r = new CourseRequestInterface.Request();
				if (acr.isSubstitute())
					request.getAlternatives().add(r);
				else
					request.getCourses().add(r);
				last = acr.getPriority();
			}
			if (acr.getCourseOffering() != null) {
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseId(acr.getCourseOffering().getUniqueId());
				rc.setCourseName(acr.getCourseOffering().getSubjectAreaAbbv() + " " + acr.getCourseOffering().getCourseNbr() + (!CONST.showCourseTitle() ? "" : " - " + acr.getCourseOffering().getTitle()));
				rc.setCourseTitle(acr.getCourseOffering().getTitle());
				CourseCreditUnitConfig credit = acr.getCourseOffering().getCredit(); 
				if (credit != null) rc.setCredit(credit.getMinCredit(), credit.getMaxCredit());
				if (acr.getPreferences() != null)
					for (AdvisorSectioningPref ssp: acr.getPreferences()) {
						if (ssp instanceof AdvisorClassPref) {
							AdvisorClassPref scp = (AdvisorClassPref)ssp;
							rc.setSelectedClass(scp.getClazz().getUniqueId(), scp.getClazz().getClassPrefLabel(acr.getCourseOffering()), scp.isRequired(), true);
						} else if (ssp instanceof AdvisorInstrMthPref) {
							AdvisorInstrMthPref imp = (AdvisorInstrMthPref)ssp;
							rc.setSelectedIntructionalMethod(imp.getInstructionalMethod().getUniqueId(), imp.getInstructionalMethod().getLabel(), imp.isRequired(), true);
						}
					}
				r.addRequestedCourse(rc);
				if (acr.getAlternative() == 0)
					r.setCritical(acr.getCritical());
			} else if (acr.getFreeTime() != null) {
				CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
				ft.setStart(acr.getFreeTime().getStartSlot());
				ft.setLength(acr.getFreeTime().getLength());
				for (DayCode day : DayCode.toDayCodes(acr.getFreeTime().getDayCode()))
					ft.addDay(day.getIndex());	
				if (!r.hasRequestedCourse()) r.addRequestedCourse(new RequestedCourse());
				r.getRequestedCourse(0).addFreeTime(ft);
			} else if (acr.getCourse() != null) {
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseName(acr.getCourse());
				r.addRequestedCourse(rc);
			}
			if (acr.getCredit() != null)
				r.setAdvisorCredit(acr.getCredit());
			if (acr.getNotes() != null)
				r.setAdvisorNote(acr.getNotes());
		}
	}
	
	protected static void fillCourseRequests(CourseRequestInterface request, List<XAdvisorRequest> acrs, OnlineSectioningServer server) {
		int last = -1;
		CourseRequestInterface.Request r = null;
		Set<Integer> skip = new HashSet<Integer>();
		String note = null;
		for (XAdvisorRequest acr: acrs) {
			if (acr.getAlternative() == 0 || acr.getNote() != null)
				note = acr.getNote();
			if (acr.getCourseId() != null) {
				RequestPriority rp = request.getRequestPriority(new RequestedCourse(acr.getCourseId().getCourseId(), acr.getCourseId().getCourseName()));
				if (rp != null) {
					if (note != null) rp.getRequest().addAdvisorNote(note);
					skip.add(acr.getPriority());
				}
			}
		}
		for (XAdvisorRequest acr: acrs) {
			if (skip.contains(acr.getPriority())) continue;
			if (acr.getPriority() == -1) {
				request.setCreditNote(acr.getNote());
				continue;
			}
			if (r == null || last != acr.getPriority()) {
				r = new CourseRequestInterface.Request();
				if (acr.isSubstitute())
					request.getAlternatives().add(r);
				else
					request.getCourses().add(r);
				last = acr.getPriority();
			}
			if (acr.getCourseId() != null) {
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseId(acr.getCourseId().getCourseId());
				rc.setCourseName(acr.getCourseId().getCourseName() + (!CONST.showCourseTitle() || acr.getCourseId().getTitle() == null || acr.getCourseId().getTitle().isEmpty() ? "" : " - " + acr.getCourseId().getTitle()));
				rc.setCourseTitle(acr.getCourseId().getTitle());
				XCourse course = server.getCourse(acr.getCourseId().getCourseId());
				if (course != null) {
					rc.setCredit(course.getMinCredit(), course.getMaxCredit());
				}
				if (acr.hasPreferences())
					for (XPreference ssp: acr.getPreferences()) {
						switch (ssp.getType()) {
						case SECTION:
							rc.setSelectedClass(ssp.getUniqueId(), ssp.getLabel(), ssp.isRequired(), true);
							break;
						case INSTR_METHOD:
							rc.setSelectedIntructionalMethod(ssp.getUniqueId(), ssp.getLabel(), ssp.isRequired(), true);
							break;
						}
					}
				r.addRequestedCourse(rc);
				if (acr.getAlternative() == 0)
					r.setCritical(acr.getCritical());
			} else if (acr.getFreeTime() != null) {
				CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
				ft.setStart(acr.getFreeTime().getSlot());
				ft.setLength(acr.getFreeTime().getLength());
				for (DayCode day : DayCode.toDayCodes(acr.getFreeTime().getDays()))
					ft.addDay(day.getIndex());	
				if (!r.hasRequestedCourse()) r.addRequestedCourse(new RequestedCourse());
				r.getRequestedCourse(0).addFreeTime(ft);
			} else if (acr.getCourseName() != null) {
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseName(acr.getCourseName());
				r.addRequestedCourse(rc);
			}
			if (acr.getCredit() != null)
				r.setAdvisorCredit(acr.getCredit());
			if (acr.getNote() != null)
				r.setAdvisorNote(acr.getNote());
		}
	}
	
	public static CourseRequestInterface getRequest(Student student, org.hibernate.Session hibSession) {
		CourseRequestInterface request = getRequest(student.getUniqueId(), hibSession);
		request.setPin(student.getPin());
		request.setPinReleased(student.isPinReleased() != null && student.isPinReleased().booleanValue());
		return request;
	}
	
	public static CourseRequestInterface getRequest(Long studentId, org.hibernate.Session hibSession) {
		CourseRequestInterface request = new CourseRequestInterface();
		request.setStudentId(studentId);
		
		List<AdvisorCourseRequest> acrs = hibSession.createQuery(
				"from AdvisorCourseRequest where student = :studentId order by priority, alternative"
				).setLong("studentId", studentId).list();
		
		fillCourseRequests(request, acrs);
		
		return request;
	}
	
	public static CourseRequestInterface getRequest(XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (server instanceof StudentSolver)
			return getRequest(student.getStudentId(), helper.getHibSession());
		
		if (!student.hasAdvisorRequests()) return null;
		
		CourseRequestInterface request = new CourseRequestInterface();
		request.setStudentId(student.getStudentId());
		request.setPin(student.getPin());
		request.setPinReleased(student.isPinReleased());
		
		fillCourseRequests(request, student.getAdvisorRequests(), server);
		
		return request;
	}
	
	@Override
	public String name() {
		return "advisor-requests";
	}

}
