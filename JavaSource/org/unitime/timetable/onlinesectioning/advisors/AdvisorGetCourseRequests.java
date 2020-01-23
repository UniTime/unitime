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
	
	public AdvisorGetCourseRequests forStudent(Long id) {
		iStudentId = id;
		return this;
	}

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
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
		
		Format<Date> ts = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
		
		if (student != null) {

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
					if (cd.isCriticalOverride() != null)
						r.setCritical(cd.isCriticalOverride());
					else
						r.setCritical(cd.isCritical());
					r.setTimeStamp(cd.getTimestamp());
				}
			}
		}
	
		
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
		
		for (OnlineSectioningLog.Request log: OnlineSectioningHelper.toProto(request))
			action.addRequest(log);

		return request;
	}
	
	@Override
	public String name() {
		return "advisor-requests";
	}

}
