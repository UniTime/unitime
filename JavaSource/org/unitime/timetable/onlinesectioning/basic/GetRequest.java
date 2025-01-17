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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.constraint.HardDistanceConflicts;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorGetCourseRequests;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsValidationHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.WaitListValidationProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.updates.WaitlistedOnlineSectioningAction;
import org.unitime.timetable.solver.studentsct.StudentSolver;

/**
 * @author Tomas Muller
 */
public class GetRequest extends WaitlistedOnlineSectioningAction<CourseRequestInterface> {
	protected static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static final long serialVersionUID = 1L;
	
	private Long iStudentId;
	private boolean iSectioning;
	private boolean iCustomValidation = false;
	private boolean iWaitListValidation = false;
	private boolean iCustomRequests = true;
	private boolean iAdvisorRequests = true;
	private CourseMatcher iMatcher = null;
	private WaitListMode iWaitListMode = null;
	
	public GetRequest forStudent(Long studentId, boolean sectioning) {
		iStudentId = studentId;
		iSectioning = sectioning;
		return this;
	}
	
	public GetRequest forStudent(Long studentId) {
		return forStudent(studentId, true);
	}
	
	public GetRequest withCustomValidation(boolean validation) {
		iCustomValidation = validation; return this;
	}
	
	public GetRequest withWaitListValidation(boolean validation) {
		iWaitListValidation = validation; return this;
	}
	
	public GetRequest withCustomRequest(boolean request) {
		iCustomRequests = request; return this;
	}
	
	public GetRequest withAdvisorRequests(boolean adv) {
		iAdvisorRequests = adv; return this;
	}
	
	public GetRequest withCourseMatcher(CourseMatcher matcher) {
		iMatcher = matcher; return this;
	}
	
	public GetRequest withWaitListMode(WaitListMode mode) {
		iWaitListMode = mode; return this;
	}

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iStudentId == null) {
			if (CustomCourseRequestsHolder.hasProvider() && iCustomRequests) {
				if (iMatcher != null) iMatcher.setServer(server);
				CourseRequestInterface request = CustomCourseRequestsHolder.getProvider().getCourseRequests(
						server, helper, new XStudent(null, helper.getStudentExternalId(), helper.getUser().getName()), iMatcher);
				if (request != null) return request;
			}
			throw new SectioningException(MSG.exceptionNoStudent());
		}
		CourseRequestInterface request = null;
		Lock lock = server.readLock();
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
			XStudent student = server.getStudent(iStudentId);
			if (student == null) return null;
			action.getStudentBuilder().setExternalId(student.getExternalId());
			action.getStudentBuilder().setName(student.getName());
			if (student.getRequests().isEmpty() && CustomCourseRequestsHolder.hasProvider() && iCustomRequests) {
				if (iMatcher != null) iMatcher.setServer(server);
				request = CustomCourseRequestsHolder.getProvider().getCourseRequests(server, helper, student, iMatcher);
				if (request != null && !request.isEmpty()) return request;
			}
			
			request = new CourseRequestInterface();
			request.setStudentId(iStudentId);
			request.setSaved(true);
			request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
			request.setMaxCredit(student.getMaxCredit());
			if (iWaitListMode == null)
				request.setWaitListMode(student.getWaitListMode(helper));
			else
				request.setWaitListMode(iWaitListMode);
			if (student.getMaxCreditOverride() != null) {
				request.setMaxCreditOverride(student.getMaxCreditOverride().getValue());
				request.setMaxCreditOverrideExternalId(student.getMaxCreditOverride().getExternalId());
				request.setMaxCreditOverrideTimeStamp(student.getMaxCreditOverride().getTimeStamp());
				Integer status = student.getMaxCreditOverride().getStatus();
				if (status == null)
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_CHECKED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_NEEDED);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.NOT_NEEDED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_NOT_NEEDED);
				else
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
			}
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			boolean hasEnrollments = false;
			for (XRequest cd: student.getRequests()) {
				if (cd instanceof XCourseRequest && ((XCourseRequest)cd).getEnrollment() != null) {
					hasEnrollments = true; break;
				}
			}
			boolean setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnly.isTrue();
			boolean setReadOnlyWhenReserved = ApplicationProperty.OnlineSchedulingMakeReservedRequestReadOnly.isTrue();
			boolean setInactive = ApplicationProperty.OnlineSchedulingMakeUnassignedRequestsInactive.isTrue();
			if (helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER) {
				setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnlyIfAdmin.isTrue();
				setReadOnlyWhenReserved = ApplicationProperty.OnlineSchedulingMakeReservedRequestReadOnlyIfAdmin.isTrue();
				setInactive = ApplicationProperty.OnlineSchedulingMakeUnassignedRequestsInactiveIfAdmin.isTrue();
			}
			boolean reservedNoPriority = ApplicationProperty.OnlineSchedulingReservedRequestNoPriorityChanges.isTrue();
			boolean reservedNoAlternatives = ApplicationProperty.OnlineSchedulingReservedRequestNoAlternativeChanges.isTrue();
			boolean enrolledNoPriority = ApplicationProperty.OnlineSchedulingAssignedRequestNoPriorityChanges.isTrue();
			boolean enrolledNoAlternatives = ApplicationProperty.OnlineSchedulingAssignedRequestNoAlternativeChanges.isTrue();
			if (setInactive && !hasEnrollments) setInactive = false;
			if (setInactive && server instanceof StudentSolver)
				setInactive = false;
			boolean showWaitListPosition = ApplicationProperty.OnlineSchedulingShowWaitListPosition.isTrue();
			DistanceMetric m = server.getDistanceMetric();
			StudentQuality sq = new StudentQuality(m, server.getConfig());
			
			for (XRequest cd: student.getRequests()) {
				CourseRequestInterface.Request r = null;
				if (cd instanceof XFreeTimeRequest) {
					XFreeTimeRequest ftr = (XFreeTimeRequest)cd;
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setStart(ftr.getTime().getSlot());
					ft.setLength(ftr.getTime().getLength());
					for (DayCode day : DayCode.toDayCodes(ftr.getTime().getDays()))
						ft.addDay(day.getIndex());
					if (lastRequest != null && lastRequestPriority == cd.getPriority() && lastRequest.hasRequestedCourse() && lastRequest.getRequestedCourse(0).isFreeTime()) {
						lastRequest.getRequestedCourse(0).addFreeTime(ft);
					} else {
						r = new CourseRequestInterface.Request();
						RequestedCourse rc = new RequestedCourse();
						r.addRequestedCourse(rc);
						rc.addFreeTime(ft);
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
						lastRequest = r;
						lastRequestPriority = cd.getPriority();
						rc.setStatus(RequestedCourseStatus.SAVED);
					}
				} else if (cd instanceof XCourseRequest) {
					r = new CourseRequestInterface.Request();
					for (XCourseId courseId: ((XCourseRequest)cd).getCourseIds()) {
						XCourse c = server.getCourse(courseId.getCourseId());
						if (c == null) continue;
						XOffering offering = server.getOffering(c.getOfferingId());
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(c.getCourseId());
						rc.setCourseName(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() && !CONSTANTS.showCourseTitle() ? "" : " - " + c.getTitle()));
						rc.setCourseTitle(c.getTitle());
						rc.setCredit(c.getMinCredit(), c.getMaxCredit());
						boolean isEnrolled = ((XCourseRequest)cd).getEnrollment() != null && c.getCourseId().equals(((XCourseRequest)cd).getEnrollment().getCourseId());
						boolean isWaitListed = !isEnrolled && offering.isWaitList() && ((XCourseRequest)cd).isWaitlist(request.getWaitListMode()); 
						if (setReadOnly && isEnrolled)
							rc.setReadOnly(true);
						if (iSectioning && setInactive && !isEnrolled && !isWaitListed)
							rc.setInactive(true);
						if (!iSectioning && isEnrolled) {
							rc.setReadOnly(true);
							rc.setCanDelete(false);
							if (enrolledNoAlternatives) rc.setCanChangeAlternatives(false);
							if (enrolledNoPriority) rc.setCanChangePriority(false);
						}
						if (!iSectioning && setReadOnlyWhenReserved) {
							if (offering != null && (offering.hasIndividualReservation(student, c) || offering.hasGroupReservation(student, c))) {
								rc.setReadOnly(true);
								rc.setCanDelete(false);
								if (reservedNoAlternatives) rc.setCanChangeAlternatives(false);
								if (reservedNoPriority) rc.setCanChangePriority(false);
							}
						}
						rc.setCanWaitList(offering != null && offering.isWaitList());
						if (isEnrolled)
							rc.setStatus(RequestedCourseStatus.ENROLLED);
						else {
							Integer status = ((XCourseRequest)cd).getOverrideStatus(courseId);
							if (status == null)
								rc.setStatus(RequestedCourseStatus.SAVED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.NOT_CHECKED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_NEEDED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.NOT_NEEDED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_NOT_NEEDED);
							else
								rc.setStatus(RequestedCourseStatus.OVERRIDE_PENDING);
						}
						rc.setOverrideExternalId(((XCourseRequest)cd).getOverrideExternalId(courseId));
						rc.setOverrideTimeStamp(((XCourseRequest)cd).getOverrideTimeStamp(courseId));
						((XCourseRequest)cd).fillPreferencesIn(rc, courseId);
						r.addRequestedCourse(rc);
						if (showWaitListPosition && rc.isCanWaitList() && ((XCourseRequest)cd).isWaitlist()) {
							rc.setWaitListPosition(getWaitListPosition(offering, student, (XCourseRequest)cd, courseId, server, helper));
						}
					}
					r.setWaitList(((XCourseRequest)cd).isWaitlist());
					r.setNoSub(((XCourseRequest)cd).isNoSub());
					r.setCritical(((XCourseRequest)cd).getCritical());
					r.setTimeStamp(((XCourseRequest)cd).getTimeStamp());
					if (r.isWaitList())
						r.setWaitListedTimeStamp(((XCourseRequest)cd).getWaitListedTimeStamp());
					r.setWaitListSwapWithCourseOfferingId(((XCourseRequest)cd).getWaitListSwapWithCourseOffering() == null ? null : ((XCourseRequest)cd).getWaitListSwapWithCourseOffering().getCourseId());
					if (r.hasRequestedCourse()) {
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					if (r.isWaitList() && ((XCourseRequest)cd).getEnrollment() == null) {
						Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
						org.cpsolver.studentsct.model.CourseRequest courseRequest = SectioningRequest.convert(assignment, (XCourseRequest)cd, server, request.getWaitListMode(), helper);
						Collection<Enrollment> enrls = courseRequest.getEnrollmentsSkipSameTime(assignment);
						rc: for (RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.getCourseId() == null) continue;
							for (XCourseId cid: ((XCourseRequest)cd).getCourseIds())
								if (cid.getCourseId().equals(rc.getCourseId())) {
									XOffering off = server.getOffering(cid.getOfferingId());
									if (off == null || !off.isWaitList()) continue rc;
								}
							TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
								@Override
								public int compare(Enrollment o1, Enrollment o2) {
									return o1.getRequest().compareTo(o2.getRequest());
								}
							});
							TreeSet<String> other = new TreeSet<String>();
							Hashtable<org.cpsolver.studentsct.model.CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<org.cpsolver.studentsct.model.CourseRequest, TreeSet<Section>>();
							Enrollment noConfEnrl = null;
							int nbrEnrl = 0;
							for (Iterator<Enrollment> e = enrls.iterator(); e.hasNext();) {
								Enrollment enrl = e.next();
								if (!rc.getCourseId().equals(enrl.getCourse().getId())) continue;
								nbrEnrl ++;
								boolean overlaps = false;
								for (Request q: enrl.getStudent().getRequests()) {
									if (q.equals(request)) continue;
									Enrollment x = assignment.getValue(q);
									if (q instanceof FreeTimeRequest) {
										if (GetAssignment.isFreeTimeOverlapping((FreeTimeRequest)q, enrl)) {
											overlaps = true;
											overlap.add(((FreeTimeRequest)q).createEnrollment());
										}
									} else if (x != null && x.getAssignments() != null && !x.getAssignments().isEmpty()) {
										for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
								        	SctAssignment a = i.next();
											if (a.isOverlapping(enrl.getAssignments()) || HardDistanceConflicts.inConflict(sq, a, enrl)) {
												overlaps = true;
												overlap.add(x);
												if (x.getRequest() instanceof org.cpsolver.studentsct.model.CourseRequest) {
													org.cpsolver.studentsct.model.CourseRequest cr = (org.cpsolver.studentsct.model.CourseRequest)x.getRequest();
													TreeSet<Section> ss = overlapingSections.get(cr);
													if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
													ss.add((Section)a);
												}
											}
								        }
									}
								}
								unavailabilities: for (Unavailability unavailability: courseRequest.getStudent().getUnavailabilities()) {
									for (SctAssignment section: enrl.getAssignments()) {
										if (HardDistanceConflicts.inConflict(sq, (Section)section, unavailability)) {
											overlaps = true;
											String ov = MSG.teachingAssignment(unavailability.getSection().getName());
											other.add(ov);
											continue unavailabilities;
										}
									}
								}
								if (!overlaps && noConfEnrl == null)
									noConfEnrl = enrl;
							}
							if (noConfEnrl == null) {
								Set<String> overlaps = new TreeSet<String>();
								for (Enrollment q: overlap) {
									if (q.getRequest() instanceof FreeTimeRequest) {
										overlaps.add(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
									} else {
										org.cpsolver.studentsct.model.CourseRequest cr = (org.cpsolver.studentsct.model.CourseRequest)q.getRequest();
										Course o = q.getCourse();
										String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
										if (overlapingSections.get(cr).size() == 1)
											for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
												Section s = i.next();
												ov += " " + s.getSubpart().getName();
												if (i.hasNext()) ov += ",";
											}
										overlaps.add(ov);
									}
								}
								if (nbrEnrl == 0) {
									unavailabilities: for (Unavailability unavailability: courseRequest.getStudent().getUnavailabilities()) {
										for (Config config: courseRequest.getCourse(rc.getCourseId()).getOffering().getConfigs())
											for (Subpart subpart: config.getSubparts())
												for (Section section: subpart.getSections()) {
													if (unavailability.isOverlapping(section)) {
														overlaps.add(MSG.teachingAssignment(unavailability.getSection().getName()));
														continue unavailabilities;
													}
												}
									}
								}
								if (overlaps != null && !overlaps.isEmpty()) {
									String message = null;
									for (Iterator<String> i = overlaps.iterator(); i.hasNext();) {
										String ov = i.next();
										if (message == null)
											message = MSG.conflictWithFirst(ov);
										else if (i.hasNext())
											message += MSG.conflictWithMiddle(ov);
										else
											message += MSG.conflictWithLast(ov);
									}
									request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "WL-OVERLAP", message + ".", 0);
								}
							}
				
						}
					}
					if (r.isWaitList() && r.getWaitListSwapWithCourseOfferingId() != null && r.hasRequestedCourse()) {
						XEnrollment enrollment = ((XCourseRequest)cd).getEnrollment();
						if (enrollment != null && enrollment.getCourseId().equals(r.getWaitListSwapWithCourseOfferingId())) {
							boolean before = true;
							for (RequestedCourse rc: r.getRequestedCourse()) {
								if (r.getWaitListSwapWithCourseOfferingId().equals(rc.getCourseId())) {
									if (((XCourseRequest)cd).isRequired(enrollment, server.getOffering(enrollment.getOfferingId()))) {
										rc.setStatus(RequestedCourseStatus.WAITLIST_INACTIVE);
										request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "WL-INACTIVE", MSG.waitListRequirementsMet(), 0);
									}
									before = false;
								} else if (!before) {
									rc.setStatus(RequestedCourseStatus.WAITLIST_INACTIVE);
									request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "WL-INACTIVE", MSG.waitListLowPriority(), 0);
								}
							}
						}
					}
					lastRequest = r;
					lastRequestPriority = cd.getPriority();
				}
				action.addRequest(OnlineSectioningHelper.toProto(cd));
			}

			if (student.getLastStudentChange() == null && !(server instanceof StudentSolver) && iAdvisorRequests && (!iSectioning || !hasEnrollments)) {
				if (request.applyAdvisorRequests(AdvisorGetCourseRequests.getRequest(student, server, helper)))
					request.setPopupMessage(ApplicationProperty.PopupMessageCourseRequestsPrepopulatedWithAdvisorRecommendations.value());
			}
		} finally {
			lock.release();
		}

		if (iCustomValidation && CustomCourseRequestsValidationHolder.hasProvider())
			CustomCourseRequestsValidationHolder.getProvider().check(server, helper, request);

		if (iWaitListValidation && request.getWaitListMode() == WaitListMode.WaitList && Customization.WaitListValidationProvider.hasProvider()) {
			WaitListValidationProvider wp = Customization.WaitListValidationProvider.getProvider();
			wp.check(server, helper, request);
		}

		return request;
	}
	
	@Override
	public String name() {
		return "get-request";
	}
}
