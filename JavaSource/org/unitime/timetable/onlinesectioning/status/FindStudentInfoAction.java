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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.model.Student.BackToBackPreference;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.joda.time.LocalDate;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.AdvisedInfoInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.CourseDemand.Critical;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment.CourseSection;
import org.unitime.timetable.onlinesectioning.match.AbstractStudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest.XPreference;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentInfoAction.FindEnrollmentInfoCourseMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseLookup;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseRequestMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class FindStudentInfoAction implements OnlineSectioningAction<List<StudentInfo>> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	protected Query iQuery;
	protected Integer iLimit = null;
	protected Set<Long> iCoursesIcoordinate, iCoursesIcanApprove, iMyStudents;
	protected Set<String> iSubjectAreas;
	protected boolean iCanShowExtIds = false, iCanRegister = false, iCanUseAssistant = false;
	protected boolean iIsAdmin = false, iIsAdvisor = false, iCanEditMyStudents = false, iCanEditOtherStudents = false, iCanSelect = false;
	
	public FindStudentInfoAction withParams(String query, Set<Long> coursesIcoordinage, Set<Long> coursesIcanApprove, Set<Long> myStudents, Set<String> subjects, boolean canShowExtIds, boolean canRegister, boolean canUseAssistant) {
		iQuery = new Query(query);
		iCoursesIcanApprove = coursesIcanApprove;
		iCoursesIcoordinate = coursesIcoordinage;
		iMyStudents = myStudents;
		iCanShowExtIds = canShowExtIds;
		iCanRegister = canRegister;
		iCanUseAssistant = canUseAssistant;
		iSubjectAreas = subjects;
		Matcher m = Pattern.compile("limit:[ ]?([0-9]*)", Pattern.CASE_INSENSITIVE).matcher(query);
		if (m.find()) {
			iLimit = Integer.parseInt(m.group(1));
		}
		return this;
	}
	
	public FindStudentInfoAction withPermissions(boolean isAdmin, boolean isAdvisor, boolean canEditMyStudents, boolean canEditOtherStudents, boolean canSelectOtherRole) {
		iIsAdmin = isAdmin; iIsAdvisor = isAdvisor;
		iCanEditMyStudents = canEditMyStudents; iCanEditOtherStudents = canEditOtherStudents;
		iCanSelect = canSelectOtherRole;
		return this;
	}
	
	protected SectioningStatusFilterRpcRequest iFilter = null;
	public FindStudentInfoAction withFilter(SectioningStatusFilterRpcRequest filter) {
		iFilter = filter;
		return this;
	}
	
	public Query query() { return iQuery; }
	
	public Integer limit() { return iLimit; }

	public boolean isConsentToDoCourse(XCourse course) {
		return iCoursesIcanApprove != null && course.getConsentLabel() != null && iCoursesIcanApprove.contains(course.getCourseId());
	}
	
	public boolean isCourseVisible(Long courseId) {
		return iCoursesIcoordinate == null || iCoursesIcoordinate.contains(courseId);
	}
	
	public boolean hasMatchingSubjectArea(String subject) {
		return iSubjectAreas == null || iSubjectAreas.contains(subject);
	}
	
	public boolean isMyStudent(XStudentId student) {
		return iMyStudents != null && iMyStudents.contains(student.getStudentId());
	}
	
	public boolean isCanSelect(XStudentId student) {
		if (iIsAdmin) return true;
		if (iIsAdvisor) {
			if (iCanEditOtherStudents || (iCanEditMyStudents && isMyStudent(student))) return true;
		} else {
			if (iCanSelect) return true;
		}
		return false;
	}
	
	@Override
	public List<StudentInfo> execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		Map<Long, StudentInfo> students = new HashMap<Long, StudentInfo>();
		Map<Long, List<CourseSection>> student2unavailabilities = new HashMap<Long, List<CourseSection>>();
		
		int gEnrl = 0, gWait = 0, gRes = 0, gUnasg = 0, gNoSub = 0, gSwap = 0;
		int gtEnrl = 0, gtWait = 0, gtRes = 0, gtUnasg = 0, gtNoSub = 0, gtSwap = 0;
		int gConNeed = 0, gtConNeed = 0, gOvrNeed = 0, gtOvrNeed = 0;
		int gDist = 0, gtDist = 0, gNrDC = 0, gtNrDC = 0, gShr = 0, gtShr = 0; 
		int gFre = 0, gtFre = 0, gPIM = 0, gtPIM = 0, gPSec = 0, gtPSec = 0;
		Set<Long> unassigned = new HashSet<Long>();
		Set<Long> assigned = new HashSet<Long>();
		AcademicSessionInfo session = server.getAcademicSession();
		CourseLookup lookup = new CourseLookup(session);
		Set<String> regStates = new HashSet<String>();
		Set<String> assStates = new HashSet<String>();
		Set<String> wlStates = new HashSet<String>();
		Set<String> noSubStates = new HashSet<String>();
		Session dbSession = SessionDAO.getInstance().get(session.getUniqueId());
		for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll(helper.getHibSession())) {
			if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.enabled)
				|| (iIsAdmin && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.admin))
				|| (iIsAdvisor && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.advisor))
				) assStates.add(status.getReference());
			if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.regenabled)
				|| (iIsAdmin && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.regadmin))
				|| (iIsAdvisor && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.regadvisor))
				) regStates.add(status.getReference());
			if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.waitlist))
				wlStates.add(status.getReference());
			else if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.nosubs))
				noSubStates.add(status.getReference());
		}
		WaitListMode defaultWL = null;
		if (server instanceof StudentSolver) defaultWL = WaitListMode.NoSubs;
		DistanceMetric dm = server.getDistanceMetric();
		DistanceMetric um = server.getUnavailabilityDistanceMetric();
		boolean solver = (server instanceof StudentSolver);
		Set<Long> studentIds = null;
		Map<Long, List<AdvisorCourseRequest>> acrs = null;
		if (!solver) {
			studentIds = (iFilter == null ? null : server.createAction(SectioningStatusFilterAction.class).forRequest(iFilter).getStudentIds(server, helper));
		} else if (iFilter != null) {
			acrs = server.createAction(SectioningStatusFilterAction.class).forRequest(iFilter).getAdvisorCourseRequests(server, helper);
		}
		boolean useAdvisorWaitLists = server.getConfig().getPropertyBoolean("Load.UseAdvisorWaitLists", false);
		boolean useAdvisorNoSubs = server.getConfig().getPropertyBoolean("Load.UseAdvisorNoSubs", false);
		for (XCourseId info: findCourses(server, helper, lookup)) {
			XOffering offering = server.getOffering(info.getOfferingId());
			if (offering == null) continue;
			XCourse course = offering.getCourse(info.getCourseId());
			XEnrollments enrollments = server.getEnrollments(info.getOfferingId());
			if (enrollments == null) continue;
			boolean isConsentToDoCourse = isConsentToDoCourse(course);
			
			for (XCourseRequest request: enrollments.getRequests()) {
				if (!request.hasCourse(info.getCourseId())) continue;
				if (request.getEnrollment() != null && !request.getEnrollment().getCourseId().equals(info.getCourseId())) continue;
				if (studentIds != null && !studentIds.contains(request.getStudentId())) continue;
				XStudent student = server.getStudent(request.getStudentId());
				if (student == null) continue;
				
				String status = (student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
				WaitListMode wl = WaitListMode.None;
				if (defaultWL != null)
					wl = defaultWL;
				else if (status == null || wlStates.contains(status))
					wl = WaitListMode.WaitList;
				else if (noSubStates.contains(status))
					wl = WaitListMode.NoSubs;

				if (acrs != null) student.setAdvisorRequests(acrs.get(student.getStudentId()), helper, server.getAcademicSession().getFreeTimePattern());
				CourseRequestMatcher m = new CourseRequestMatcher(session, course, student, offering, request, isConsentToDoCourse, isMyStudent(student), lookup, server, wl);
				if (query().match(m)) {
					StudentInfo s = students.get(request.getStudentId());
					if (s == null) {
						s = new StudentInfo();
						students.put(request.getStudentId(), s);
						ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
						st.setId(request.getStudentId());
						st.setSessionId(session.getUniqueId());
						st.setExternalId(student.getExternalId());
						st.setCanShowExternalId(iCanShowExtIds);
						st.setWaitListMode(wl);
						st.setCanRegister(iCanRegister && (status == null || regStates.contains(status)));
						st.setCanUseAssistant(iCanUseAssistant && (status == null || assStates.contains(status)));
						st.setCanSelect(isCanSelect(student));
						st.setName(student.getName());
						for (XAreaClassificationMajor acm: student.getMajors()) {
							st.addArea(acm.getArea(), acm.getAreaLabel());
							st.addClassification(acm.getClassification(), acm.getClassificationLabel());
							st.addMajor(acm.getMajor(), acm.getMajorLabel());
							st.addConcentration(acm.getConcentration(), acm.getConcentrationLabel());
							st.addDegree(acm.getDegree(), acm.getDegreeLabel());
							st.addProgram(acm.getProgram(), acm.getProgramLabel());
							st.addCampus(acm.getCampus(), acm.getCampusLabel());
						}
						st.setDefaultCampus(server.getAcademicSession().getCampus());
						for (XAreaClassificationMajor acm: student.getMinors()) {
							st.addMinor(acm.getMajor(), acm.getMajorLabel());
						}
						for (XStudent.XGroup gr: student.getGroups()) {
							st.addGroup(gr.getType(), gr.getAbbreviation(), gr.getTitle());
						}
						for (XStudent.XGroup acc: student.getAccomodations()) {
							st.addAccommodation(acc.getAbbreviation(), acc.getTitle());
						}
						for (XStudent.XAdvisor a: student.getAdvisors()) {
							if (a.getName() != null) st.addAdvisor(a.getName());
						}
						List<CourseSection> unavailabilities = null;
						if (server instanceof StudentSolver)
							unavailabilities = ((StudentSolver)server).getUnavailabilities(request.getStudentId());
						if (unavailabilities != null)
							student2unavailabilities.put(request.getStudentId(), unavailabilities);

						int tEnrl = 0, tWait = 0, tRes = 0, tConNeed = 0, tReq = 0, tUnasg = 0, tOvrNeed = 0, ovrNeed = 0, tNoSub = 0, tSwap =0;
						float tCred = 0f;
						int nrDisCnf = 0, maxDist = 0, share = 0; 
						int ftShare = 0;
						List<Float> minsTot = new ArrayList<Float>();
						List<Float> maxsTot = new ArrayList<Float>();
						List<Float> mins = new ArrayList<Float>();
						List<Float> maxs = new ArrayList<Float>();
						int nrCoursesTot = 0, nrCourses = 0;
						float studentMin = 0f, studentMax = 0f;
						float studentMinTot = 0f, studentMaxTot = 0f;
						Set<Long> advisorWaitListedCourseIds = student.getAdvisorWaitListedCourseIds(useAdvisorWaitLists, useAdvisorNoSubs);
						for (XRequest r: student.getRequests()) {
							if (r instanceof XCourseRequest) {
								XCourseRequest cr = (XCourseRequest)r;
								Float min = null, max = null;
								Float minTot = null, maxTot = null;
								for (XCourseId courseId: cr.getCourseIds()) {
									XCourse c = server.getCourse(courseId.getCourseId());
									if (c != null && c.hasCredit()) {
										if (minTot == null || minTot > c.getMinCredit()) minTot = c.getMinCredit();
										if (maxTot == null || maxTot < c.getMaxCredit()) maxTot = c.getMaxCredit();
									}
									if (cr.isOverridePending(c)) { gtOvrNeed ++; tOvrNeed ++; }
									if (query().match(new CourseRequestMatcher(session, c, student, server.getOffering(c.getOfferingId()), cr, isConsentToDoCourse(c), isMyStudent(student), lookup, server, wl))) {
										if (c != null && c.hasCredit()) { 
											if (min == null || min > c.getMinCredit()) min = c.getMinCredit();
											if (max == null || max < c.getMaxCredit()) max = c.getMaxCredit();
										}
										if (cr.isOverridePending(c)) { gOvrNeed ++; ovrNeed ++; }
									}
								}
								if (cr.isWaitListOrNoSub(wl, advisorWaitListedCourseIds)) {
									if (minTot != null) {
										studentMinTot += minTot; studentMaxTot += maxTot;
									}
									if (min != null) {
										studentMin += min; studentMax += max;
									}
								} else {
									if (minTot != null) {
										minsTot.add(minTot); maxsTot.add(maxTot); 
										if (!r.isAlternative()) nrCoursesTot ++;
									}
									if (min != null) {
										mins.add(min); maxs.add(max); 
										if (!r.isAlternative()) nrCourses ++;
									}									
								}
								if (!r.isAlternative()) tReq ++;
								if (cr.getEnrollment() == null) {
									if (student.canAssign(cr, st.getWaitListMode())) {
										tUnasg ++; gtUnasg ++;
										if (cr.isWaitlist(st.getWaitListMode())) {
											tWait ++; gtWait ++;
										}
										if (cr.isNoSub(st.getWaitListMode())) {
											tNoSub ++; gtNoSub ++;
										}
									}
								} else {
									tEnrl ++; gtEnrl ++;
									if (cr.getEnrollment().getReservation() != null) {
										tRes ++; gtRes ++;
									}
									if (cr.getEnrollment().getApproval() == null) {
										XCourse i = server.getCourse(cr.getEnrollment().getCourseId());
										if (i != null && i.getConsentLabel() != null) {
											tConNeed ++; gtConNeed ++;
										}
									}
									if (cr.isWaitlist(wl) && cr.getEnrollment().equals(cr.getWaitListSwapWithCourseOffering())) { tSwap ++; gtSwap ++; }
									XOffering o = server.getOffering(cr.getEnrollment().getOfferingId());
									XConfig g = (o == null ? null : o.getConfig(cr.getEnrollment().getConfigId()));
									if (g != null) {
										for (XSubpart xs: g.getSubparts()) {
											tCred += xs.getCreditValue(cr.getEnrollment().getCourseId());
											if (g.getInstructionalMethod() != null && !g.getInstructionalMethod().getReference().equals(session.getDefaultInstructionalMethod()))
												s.addIMTotalCredit(g.getInstructionalMethod().getReference(), xs.getCreditValue(cr.getEnrollment().getCourseId()));
										}
									}
									
									if (o != null)
										for (XSection section: o.getSections(cr.getEnrollment())) {
											if (section.getTime() == null) continue;
											for (XRequest q: student.getRequests()) {
												if (q instanceof XCourseRequest) {
													XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
													if (otherEnrollment == null) continue;
													XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
													for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
														if (otherSection.equals(section) || otherSection.getTime() == null) continue;
														if (otherSection.isDistanceConflict(student, section, dm)) {
															nrDisCnf ++; gtNrDC ++;
															int d = otherSection.getDistanceInMinutes(section, dm);
															if (d > maxDist) maxDist = d;
															if (d > gtDist) gtDist = d;
														}
														if (section.getTime().hasIntersection(otherSection.getTime()) && !section.isToIgnoreStudentConflictsWith(o.getDistributions(), otherSection.getSectionId()) && section.getSectionId() < otherSection.getSectionId()) {
															share += section.getTime().share(otherSection.getTime());
															gtShr += section.getTime().share(otherSection.getTime());
														}
													}
												} else if (q instanceof XFreeTimeRequest) {
													XFreeTimeRequest ft = (XFreeTimeRequest)q;
													if (section.getTime().hasIntersection(ft.getTime())) {
														ftShare += section.getTime().share(ft.getTime());
														gtFre += section.getTime().share(ft.getTime());
													}
												}
											}
											if (unavailabilities != null)
												for (CourseSection cs: unavailabilities) {
													if (section.getTime().hasIntersection(cs.getSection().getTime())) {
														share += section.getTime().share(cs.getSection().getTime());
														gtShr += section.getTime().share(cs.getSection().getTime());
													}
													if (section.isUnavailabilityDistanceConflict(student, cs.getSection(), um)) {
														nrDisCnf ++; gtNrDC ++;
														int d = section.getDistanceInMinutes(cs.getSection(), um);
														if (d > maxDist) maxDist = d;
														if (d > gtDist) gtDist = d;
														
													}
												}
										}
								}
							}
						}
						if (student.isMaxCreditOverridePending()) {
							if (nrCourses == nrCoursesTot) { gOvrNeed ++; ovrNeed ++; }
							gtOvrNeed ++; tOvrNeed ++;
						}

						Collections.sort(mins);
						Collections.sort(maxs);
						for (int i = 0; i < nrCourses; i++) {
							studentMin += mins.get(i);
							studentMax += maxs.get(maxs.size() - i - 1);
						}
						Collections.sort(minsTot);
						Collections.sort(maxsTot);
						for (int i = 0; i < nrCoursesTot; i++) {
							studentMinTot += minsTot.get(i);
							studentMaxTot += maxsTot.get(maxsTot.size() - i - 1);
						}
						s.setRequestCredit(studentMin, studentMax);
						s.setTotalRequestCredit(studentMinTot, studentMaxTot);
						s.setTotalEnrollment(tEnrl);
						s.setTotalReservation(tRes);
						s.setTotalWaitlist(tWait);
						s.setTotalNoSub(tNoSub);
						s.setTotalSwap(tSwap);
						s.setTotalUnassigned(tUnasg);
						s.setTotalConsentNeeded(tConNeed);
						s.setTotalOverrideNeeded(tOvrNeed);
						s.setEnrollment(0);
						s.setReservation(0);
						s.setNoSub(0);
						s.setWaitlist(0);
						s.setSwap(0);
						s.setUnassigned(0);
						s.setConsentNeeded(0);
						s.setOverrideNeeded(ovrNeed);
						s.setRequested(tReq);
						s.setStatus(student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
						s.setEmailDate(student.getEmailTimeStamp() == null ? null : student.getEmailTimeStamp());
						s.setCredit(0f);
						s.setTotalCredit(tCred);
						s.setNote(student.hasLastNote() ? student.getLastNote().getNote() : null);
						s.setNrDistanceConflicts(0);
						s.setLongestDistanceMinutes(0);
						s.setOverlappingMinutes(0);
						s.setTotalNrDistanceConflicts(nrDisCnf);
						s.setTotalLongestDistanceMinutes(maxDist);
						s.setTotalOverlappingMinutes(share);
						s.setFreeTimeOverlappingMins(0);
						s.setTotalFreeTimeOverlappingMins(ftShare);
						s.setPrefInstrMethConflict(0);
						s.setTotalPrefInstrMethConflict(0);
						s.setPrefSectionConflict(0);
						s.setTotalPrefSectionConflict(0);
						s.setMyStudent(isMyStudent(student));
		    			s.setAdvisedInfo(getAdvisedInfo(student, server, helper));
		    			s.setPreference(getStudentSchedulingPreference(student, server, helper));
						s.setPin(student.getPin());
						s.setPinReleased(student.isPinReleased());

					}
					if (m.enrollment() != null) {
						if (assigned.add(m.request().getRequestId())) {
							s.setEnrollment(s.getEnrollment() + 1); gEnrl ++;
							if (m.enrollment().getReservation() != null) { s.setReservation(s.getReservation() + 1); gRes ++; }
							if (course.getConsentLabel() != null && m.enrollment().getApproval() == null) {
								s.setConsentNeeded(s.getConsentNeeded() + 1); gConNeed ++;
							}
							if (m.request().isWaitlist(wl) && m.request().getEnrollment().equals(m.request().getWaitListSwapWithCourseOffering())) {
								s.setSwap(s.getSwap() + 1); gSwap ++;
							}
							if (m.enrollment().getTimeStamp() != null) {
								if (s.getEnrolledDate() == null)
									s.setEnrolledDate(m.enrollment().getTimeStamp());
								else if (m.enrollment().getTimeStamp().after(s.getEnrolledDate()))
									s.setEnrolledDate(m.enrollment().getTimeStamp());
							}
							if (m.enrollment().getApproval() != null) {
								if (s.getApprovedDate() == null)
									s.setApprovedDate(m.enrollment().getApproval().getTimeStamp());
								else if (m.enrollment().getApproval().getTimeStamp().after(s.getApprovedDate()))
									s.setApprovedDate(m.enrollment().getApproval().getTimeStamp());
							}
							XOffering o = server.getOffering(m.enrollment().getOfferingId());
							XConfig g = (o == null ? null : o.getConfig(m.enrollment().getConfigId()));
							if (g != null) {
								for (XSubpart xs: g.getSubparts()) {
									s.setCredit(s.getCredit() + xs.getCreditValue(m.enrollment().getCourseId()));
									if (g.getInstructionalMethod() != null && !g.getInstructionalMethod().getReference().equals(session.getDefaultInstructionalMethod()))
										s.addIMCredit(g.getInstructionalMethod().getReference(), xs.getCreditValue(m.enrollment().getCourseId()));
								}
								
								List<XPreference> pref = m.request().getPreferences(m.enrollment());
								if (pref != null) {
									boolean hasIm = false;
									boolean im = false;
									Set<String> allSubparts = new HashSet<String>();
									Set<String> selectedSubparts = new HashSet<String>();
									for (XPreference p: pref) {
										switch (p.getType()) {
										case INSTR_METHOD:
											hasIm = true;
											if (g.getInstructionalMethod() != null && g.getInstructionalMethod().getUniqueId().equals(p.getUniqueId()))
												im = true;
											break;
										case SECTION:
											XSection ps = o.getSection(p.getUniqueId());
											if (ps != null) {
												allSubparts.add(ps.getSubpartName());
												for (XSection section: o.getSections(m.enrollment())) {
													if (section.getSectionId().equals(p.getUniqueId()))
														selectedSubparts.add(section.getSubpartName());
												}
											}
											break;
										} 
									}
									if (hasIm) {
										s.setTotalPrefInstrMethConflict(s.getTotalPrefInstrMethConflict() + 1);
										gtPIM++;
										if (im) {
											s.setPrefInstrMethConflict(s.getPrefInstrMethConflict() + 1);
											gPIM++;
										}
									}
									if (!allSubparts.isEmpty()) {
										s.setTotalPrefSectionConflict(s.getTotalPrefSectionConflict() + allSubparts.size());
										gtPSec += allSubparts.size();
										s.setPrefSectionConflict(s.getPrefSectionConflict() + selectedSubparts.size());
										gPSec += selectedSubparts.size();
									}
								}
							}
							if (o != null)
								for (XSection section: o.getSections(m.enrollment())) {
									if (section.getTime() == null) continue;
									for (XRequest q: student.getRequests()) {
										if (q instanceof XCourseRequest) {
											XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
											if (otherEnrollment == null) continue;
											XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
											XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
											for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
												if (otherSection.equals(section) || otherSection.getTime() == null) continue;
												if (otherSection.isDistanceConflict(student, section, dm)) {
													s.setNrDistanceConflicts(s.getNrDistanceConflicts() + 1); gNrDC ++;
													int d = otherSection.getDistanceInMinutes(section, dm);
													if (d > s.getLongestDistanceMinutes()) s.setLongestDistanceMinutes(d);
													if (d > gDist) gDist = d;
												}
												if (section.getTime().hasIntersection(otherSection.getTime()) && !section.isToIgnoreStudentConflictsWith(o.getDistributions(), otherSection.getSectionId())) {
													if (section.getSectionId() < otherSection.getSectionId() || !query().match(new CourseRequestMatcher(session, otherCourse, student, otherOffering, (XCourseRequest)q, isConsentToDoCourse(otherCourse), isMyStudent(student), lookup, server, wl))) {
														s.setOverlappingMinutes(s.getOverlappingMinutes() + section.getTime().share(otherSection.getTime()));
														gShr += section.getTime().share(otherSection.getTime());
													}
												}
											}
										} else if (q instanceof XFreeTimeRequest) {
											XFreeTimeRequest ft = (XFreeTimeRequest)q;
											if (section.getTime().hasIntersection(ft.getTime())) {
												s.setFreeTimeOverlappingMins(s.getFreeTimeOverlappingMins() + section.getTime().share(ft.getTime()));
												gFre += section.getTime().share(ft.getTime());
											}
										}
									}
									List<CourseSection> unavailabilities = student2unavailabilities.get(request.getStudentId());
									if (unavailabilities != null)
										for (CourseSection cs: unavailabilities) {
											if (section.getTime().hasIntersection(cs.getSection().getTime())) {
												s.setOverlappingMinutes(s.getOverlappingMinutes() + section.getTime().share(cs.getSection().getTime()));
												gShr += section.getTime().share(cs.getSection().getTime());
											}
											if (section.isUnavailabilityDistanceConflict(student, cs.getSection(), um)) {
												s.setNrDistanceConflicts(s.getNrDistanceConflicts() + 1); gNrDC ++;
												int d = section.getDistanceInMinutes(cs.getSection(), um);
												if (d > s.getLongestDistanceMinutes()) s.setLongestDistanceMinutes(d);
												if (d > gtDist) gtDist = d;
											}
										}
								}
						}
					} else if (unassigned.add(m.request().getRequestId())) {
						if (m.student().canAssign(m.request(), s.getStudent().getWaitListMode())) {
							if (m.request().isWaitlist(s.getStudent().getWaitListMode())) {
								s.setWaitlist(s.getWaitlist() + 1); gWait ++;
								if (s.getTopWaitingPriority() == null)
									s.setTopWaitingPriority(1 + m.request().getPriority());
								else
									s.setTopWaitingPriority(Math.min(1 + m.request().getPriority(), s.getTopWaitingPriority()));
							}
							if (m.request().isNoSub(s.getStudent().getWaitListMode())) {
								s.setNoSub(s.getNoSub() + 1); gNoSub ++;
							}
							s.setUnassigned(s.getUnassigned() + 1); gUnasg ++;
						}
						for (XCourseId c: m.request().getCourseIds()) {
							List<XPreference> pref = m.request().getPreferences(c);
							if (pref != null) {
								XOffering o = server.getOffering(c.getOfferingId());
								boolean reqIm = false;
								Set<String> allSubparts = new HashSet<String>();
								for (XPreference p: pref) {
									if (!p.isRequired()) continue;
									switch (p.getType()) {
									case INSTR_METHOD:
										reqIm = true;
										break;
									case SECTION:
										XSection ps = (o == null ? null : o.getSection(p.getUniqueId()));
										if (ps != null)
											allSubparts.add(ps.getSubpartName());
										break;
									}
								}
								if (reqIm) {
									s.setTotalPrefInstrMethConflict(s.getTotalPrefInstrMethConflict() + 1);
									gtPIM++;
								}
								if (!allSubparts.isEmpty()) {
									s.setTotalPrefSectionConflict(s.getTotalPrefSectionConflict() + allSubparts.size());
									gtPSec += allSubparts.size();
								}
							}	
						}
					}
					if (m.request().getTimeStamp() != null) {
						if (s.getRequestedDate() == null)
							s.setRequestedDate(m.request().getTimeStamp());
						else if (m.request().getTimeStamp().after(s.getRequestedDate()))
							s.setRequestedDate(m.request().getTimeStamp());
					}
					if (m.request().getWaitListedTimeStamp() != null) {
						if (s.getWaitListedDate() == null)
							s.setWaitListedDate(m.request().getWaitListedTimeStamp());
						else if (m.request().getWaitListedTimeStamp().after(s.getWaitListedDate()))
							s.setWaitListedDate(m.request().getWaitListedTimeStamp());
					}
				}
			}
		}
		
		List<StudentInfo> ret = new ArrayList<StudentInfo>(students.values());
		
		if (iSubjectAreas == null && iCoursesIcoordinate == null) {
			if (studentIds != null && (studentIds.size() < 1000 || server instanceof DatabaseServer)) {
				FindStudentInfoMatcher m = new FindStudentInfoMatcher(session, query(), iMyStudents); m.setServer(server);
				for (Long id: studentIds) {
					if (students.containsKey(id)) continue;
					XStudent student = server.getStudent(id);
					if (student == null) continue;
					if (!m.match(student)) continue;
					StudentInfo s = new StudentInfo();
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
					st.setId(student.getStudentId());
					st.setSessionId(session.getUniqueId());
					st.setExternalId(student.getExternalId());
					st.setCanShowExternalId(iCanShowExtIds);
					String status = (student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
					if (defaultWL != null)
						st.setWaitListMode(defaultWL);
					else if (status == null || wlStates.contains(status))
						st.setWaitListMode(WaitListMode.WaitList);
					else if (noSubStates.contains(status))
						st.setWaitListMode(WaitListMode.NoSubs);
					else
						st.setWaitListMode(WaitListMode.None);
					st.setCanRegister(iCanRegister && (status == null || regStates.contains(status)));
					st.setCanUseAssistant(iCanUseAssistant && (status == null || assStates.contains(status)));
					st.setCanSelect(isCanSelect(student));
					st.setName(student.getName());
					for (XAreaClassificationMajor acm: student.getMajors()) {
						st.addArea(acm.getArea(), acm.getAreaLabel());
						st.addClassification(acm.getClassification(), acm.getClassificationLabel());
						st.addMajor(acm.getMajor(), acm.getMajorLabel());
						st.addConcentration(acm.getConcentration(), acm.getConcentrationLabel());
						st.addDegree(acm.getDegree(), acm.getDegreeLabel());
						st.addProgram(acm.getProgram(), acm.getProgramLabel());
						st.addCampus(acm.getCampus(), acm.getCampusLabel());
					}
					st.setDefaultCampus(server.getAcademicSession().getCampus());
					for (XAreaClassificationMajor acm: student.getMinors()) {
						st.addMinor(acm.getMajor(), acm.getMajorLabel());
					}
					for (XStudent.XGroup gr: student.getGroups()) {
						st.addGroup(gr.getType(), gr.getAbbreviation(), gr.getTitle());
					}
					for (XStudent.XGroup acc: student.getAccomodations()) {
						st.addAccommodation(acc.getAbbreviation(), acc.getTitle());
					}
					for (XStudent.XAdvisor a: student.getAdvisors()) {
						if (a.getName() != null) st.addAdvisor(a.getName());
					}
					s.setStatus(student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
					s.setEmailDate(student.getEmailTimeStamp() == null ? null : student.getEmailTimeStamp());
					s.setNote(student.hasLastNote() ? student.getLastNote().getNote() : null);
					s.setMyStudent(isMyStudent(student));
					s.setAdvisedInfo(getAdvisedInfo(student, server, helper));
					s.setPreference(getStudentSchedulingPreference(student, server, helper));
					s.setPin(student.getPin());
					s.setPinReleased(student.isPinReleased());

					ret.add(s);
				}
			} else {
				for (XStudentId id: server.findStudents(new FindStudentInfoMatcher(session, query(), iMyStudents))) {
					XStudent student = (id instanceof XStudent ? (XStudent)id : server.getStudent(id.getStudentId()));
					if (students.containsKey(id.getStudentId())) continue;
					StudentInfo s = new StudentInfo();
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
					st.setId(student.getStudentId());
					st.setSessionId(session.getUniqueId());
					st.setExternalId(student.getExternalId());
					st.setCanShowExternalId(iCanShowExtIds);
					String status = (student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
					if (defaultWL != null)
						st.setWaitListMode(defaultWL);
					else if (status == null || wlStates.contains(status))
						st.setWaitListMode(WaitListMode.WaitList);
					else if (noSubStates.contains(status))
						st.setWaitListMode(WaitListMode.NoSubs);
					else
						st.setWaitListMode(WaitListMode.None);
					st.setCanRegister(iCanRegister && (status == null || regStates.contains(status)));
					st.setCanUseAssistant(iCanUseAssistant && (status == null || assStates.contains(status)));
					st.setCanSelect(isCanSelect(student));
					st.setName(student.getName());
					for (XAreaClassificationMajor acm: student.getMajors()) {
						st.addArea(acm.getArea(), acm.getAreaLabel());
						st.addClassification(acm.getClassification(), acm.getClassificationLabel());
						st.addMajor(acm.getMajor(), acm.getMajorLabel());
						st.addConcentration(acm.getConcentration(), acm.getConcentrationLabel());
						st.addDegree(acm.getDegree(), acm.getDegreeLabel());
						st.addProgram(acm.getProgram(), acm.getProgramLabel());
						st.addCampus(acm.getCampus(), acm.getCampusLabel());
					}
					st.setDefaultCampus(server.getAcademicSession().getCampus());
					for (XAreaClassificationMajor acm: student.getMinors()) {
						st.addMinor(acm.getMajor(), acm.getMajorLabel());
					}
					for (XStudent.XGroup gr: student.getGroups()) {
						st.addGroup(gr.getType(), gr.getAbbreviation(), gr.getTitle());
					}
					for (XStudent.XGroup acc: student.getAccomodations()) {
						st.addAccommodation(acc.getAbbreviation(), acc.getTitle());
					}
					for (XStudent.XAdvisor a: student.getAdvisors()) {
						if (a.getName() != null) st.addAdvisor(a.getName());
					}
					s.setStatus(student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
					s.setEmailDate(student.getEmailTimeStamp() == null ? null : student.getEmailTimeStamp());
					s.setNote(student.hasLastNote() ? student.getLastNote().getNote() : null);
					s.setMyStudent(isMyStudent(student));
					s.setAdvisedInfo(getAdvisedInfo(student, server, helper));
					s.setPreference(getStudentSchedulingPreference(student, server, helper));
					s.setPin(student.getPin());
					s.setPinReleased(student.isPinReleased());
					ret.add(s);
				}
			}
		}
		
		Collections.sort(ret, new Comparator<StudentInfo>() {
			@Override
			public int compare(StudentInfo s1, StudentInfo s2) {
				int cmp = s1.getStudent().getName().compareTo(s2.getStudent().getName());
				if (cmp != 0) return cmp;
				return Long.valueOf(s1.getStudent().getId()).compareTo(s2.getStudent().getId());
			}
		});
		
		if (limit() != null && ret.size() >= limit()) {
			List<StudentInfo>  r = new ArrayList<StudentInfo>(limit());
			for (StudentInfo i: ret) {
				r.add(i);
				if (r.size() == limit()) break;
			}
			ret = r;
		}
		
		// if (students.size() > 0) {
		StudentInfo t = new StudentInfo();
		
		t.setEnrollment(gEnrl);
		t.setReservation(gRes);
		t.setWaitlist(gWait);
		t.setNoSub(gNoSub);
		t.setSwap(gSwap);
		t.setUnassigned(gUnasg);
		
		t.setTotalEnrollment(gtEnrl);
		t.setTotalReservation(gtRes);
		t.setTotalWaitlist(gtWait);
		t.setTotalNoSub(gtNoSub);
		t.setTotalSwap(gtSwap);
		t.setTotalUnassigned(gtUnasg);
		
		t.setConsentNeeded(gConNeed);
		t.setTotalConsentNeeded(gtConNeed);
		t.setOverrideNeeded(gOvrNeed);
		t.setTotalOverrideNeeded(gtOvrNeed);
		
		t.setNrDistanceConflicts(gNrDC);
		t.setTotalNrDistanceConflicts(gtNrDC);
		t.setLongestDistanceMinutes(gDist);
		t.setTotalLongestDistanceMinutes(gtDist);
		t.setOverlappingMinutes(gShr);
		t.setTotalOverlappingMinutes(gtShr);
		t.setFreeTimeOverlappingMins(gFre);
		t.setTotalFreeTimeOverlappingMins(gtFre);
		t.setPrefInstrMethConflict(gPIM);
		t.setTotalPrefInstrMethConflict(gtPIM);
		t.setPrefSectionConflict(gPSec);
		t.setTotalPrefSectionConflict(gtPSec);

		ret.add(t);
		
		return ret;
	}
	
	protected Collection<? extends XCourseId> findCourses(final OnlineSectioningServer server, final OnlineSectioningHelper helper, CourseLookup lookup) {
		if (iFilter != null && server instanceof DatabaseServer) {
			List<XCourseId> courses = new ArrayList<XCourseId>();
			FindEnrollmentInfoCourseMatcher m = new FindEnrollmentInfoCourseMatcher(iCoursesIcoordinate, iCoursesIcanApprove, iSubjectAreas, iQuery, lookup, server);
			for (XCourse course :server.createAction(SectioningStatusFilterAction.class).forRequest(iFilter).getCourses(server, helper))
				if (m.match(course)) courses.add(course);
			return courses;
		} else {
			 return server.findCourses(new FindEnrollmentInfoCourseMatcher(iCoursesIcoordinate, iCoursesIcanApprove, iSubjectAreas, iQuery, lookup, server));
		}
	}

	@Override
	public String name() {
		return "find-student-infos";
	}
	
	public static class FindStudentInfoMatcher extends AbstractStudentMatcher {
		private static final long serialVersionUID = 1L;
		protected Query iQuery;
		protected String iDefaultSectioningStatus;
		protected Set<Long> iMyStudents;
		
		public FindStudentInfoMatcher(AcademicSessionInfo session, Query query, Set<Long> myStudents) {
			iQuery = query;
			iDefaultSectioningStatus = session.getDefaultSectioningStatus();
			iMyStudents = myStudents;
		}
		
		public boolean isMyStudent(XStudentId student) {
			return iMyStudents != null && iMyStudents.contains(student.getStudentId());
		}

		@Override
		public boolean match(XStudentId id) {
			XStudent student = (id instanceof XStudent ? (XStudent)id : getServer().getStudent(id.getStudentId()));
			return student != null && iQuery.match(new StudentMatcher(student, iDefaultSectioningStatus, getServer(), isMyStudent(student)));
		}
	}
	
	public static String getStudentSchedulingPreference(XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		String pref = null;
		if (student.getModalityPreference() != null && student.getModalityPreference() != ModalityPreference.NO_PREFERENCE) {
			switch(student.getModalityPreference()) {
			case ONILNE_DISCOURAGED:
				pref = (pref == null ? "" : pref + "\n") + MSG.itemSchedulingModalityPreferFaceToFace();
				break;
			case ONLINE_PREFERRED:
				pref = (pref == null ? "" : pref + "\n") + MSG.itemSchedulingModalityPreferOnline();
				break;
			case ONLINE_REQUIRED:
				pref = (pref == null ? "" : pref + "\n") + MSG.itemSchedulingModalityRequireOnline();
				break;
			}
		}
		if (student.getBackToBackPreference() != null && student.getBackToBackPreference() != BackToBackPreference.NO_PREFERENCE) {
			switch(student.getBackToBackPreference()) {
			case BTB_DISCOURAGED:
				pref = (pref == null ? "" : pref + "\n") + MSG.itemSchedulingBackToBackDiscourage();
				break;
			case BTB_PREFERRED:
				pref = (pref == null ? "" : pref + "\n") + MSG.itemSchedulingBackToBackPrefer();
				break;
			}
		}
		if (student.getClassStartDate() != null || student.getClassEndDate() != null) {
			if (student.getClassStartDate() == null) {
				pref = (pref == null ? "" : pref + "\n") + 
						MSG.schedulingPrefClassesTo(Formats.getDateFormat(CONST.patternDateFormat()).format(
						new LocalDate(server.getAcademicSession().getDatePatternFirstDate()).plusDays(student.getClassEndDate()).toDate()
						));
			} else if (student.getClassEndDate() == null) {
				pref = (pref == null ? "" : pref + "\n") + 
						MSG.schedulingPrefClassesFrom(Formats.getDateFormat(CONST.patternDateFormat()).format(
						new LocalDate(server.getAcademicSession().getDatePatternFirstDate()).plusDays(student.getClassStartDate()).toDate()
						));
			} else {
				pref = (pref == null ? "" : pref + "\n") + 
						MSG.schedulingPrefClassesBetween(
						Formats.getDateFormat(CONST.patternDateFormat()).format(new LocalDate(server.getAcademicSession().getDatePatternFirstDate()).plusDays(student.getClassStartDate()).toDate()),
						Formats.getDateFormat(CONST.patternDateFormat()).format(new LocalDate(server.getAcademicSession().getDatePatternFirstDate()).plusDays(student.getClassEndDate()).toDate())
						);
			}
		}
		return pref;
	}
	
	public static AdvisedInfoInterface getAdvisedInfo(XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!student.hasAdvisorRequests()) return null;
		AdvisedInfoInterface info = new AdvisedInfoInterface();
		XAdvisorRequest last = null;
		XCourseId advFirstChoice = null;
		XCourseId firstEnrolled = null;
		XCourseRequest firstChoice = null;
		CourseDemand.Critical advCritical = CourseDemand.Critical.fromText(ApplicationProperty.AdvisorCourseRequestsAllowCritical.valueOfSession(server.getAcademicSession().getUniqueId()));
		boolean firstChoiceCritical = false;
		float minCred = 0f, maxCred = 0f, cm = 0f, cx = 0f;
		int nrCourses = 0, nrCriticalCourses = 0, nrCoursesFound = 0, nrCriticalCoursesFound = 0, nrSubstMisMatch = 0, nrCoursesAssigned = 0;
		int foundPrioMin = 0, foundPrioMax = 0;
		int points = 0, maxPoints = 0;
		int missingCrit = 0, missingPrim = 0;
		int notAssignedCrit = 0, notAssignedPrim = 0;
		for (XAdvisorRequest acr: student.getAdvisorRequests()) {
			if (acr.getPriority() == -1) continue;
			if (last != null && last.getPriority() != acr.getPriority()) {
				minCred += cm; maxCred += cx; cm = 0; cx = 0;
				if (advFirstChoice != null) { // ignore requests without any courses
					if (firstChoiceCritical && nrCoursesFound == 0) {
						missingCrit ++; missingPrim ++;
						if (nrCourses > 1)
							info.addMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageMissingImportantCourseWithAlts(advFirstChoice.getCourseName()) :
								advCritical == Critical.VITAL ? MSG.advMessageMissingVitalCourseWithAlts(advFirstChoice.getCourseName()) :
								MSG.advMessageMissingCriticalCourseWithAlts(advFirstChoice.getCourseName()));
						else
							info.addMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageMissingImportantCourse(advFirstChoice.getCourseName()) :
								advCritical == Critical.VITAL ? MSG.advMessageMissingVitalCourse(advFirstChoice.getCourseName()) :
								MSG.advMessageMissingCriticalCourse(advFirstChoice.getCourseName()));
					} else if (!last.isSubstitute() && nrCoursesFound - nrSubstMisMatch == 0) {
						missingPrim ++;
						if (nrCourses > 1)
							info.addMessage(MSG.advMessageMissingCourseWithAlts(advFirstChoice.getCourseName()));
						else
							info.addMessage(MSG.advMessageMissingCourse(advFirstChoice.getCourseName()));
					} else if (last.isSubstitute() && nrCoursesFound == 0) {
						if (nrCourses > 1)
							info.addMessage(MSG.advMessageMissingSubstituteCourseWithAlts(advFirstChoice.getCourseName()));
						else
							info.addMessage(MSG.advMessageMissingSubstituteCourse(advFirstChoice.getCourseName()));
					} else if (firstChoice == null && firstChoiceCritical) {
						info.addMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageMissingImportantCourseHasAlts(advFirstChoice.getCourseName()):
							advCritical == Critical.VITAL ? MSG.advMessageMissingVitalCourseHasAlts(advFirstChoice.getCourseName()) :
							MSG.advMessageMissingCriticalCourseHasAlts(advFirstChoice.getCourseName()));
					} else if (firstChoice == null) {
						info.addMessage(MSG.advMessageMissingCourseHasAlts(advFirstChoice.getCourseName()));
					} else if (nrCoursesFound < nrCourses) {
						info.addMessage(MSG.advMessageMissingAlternatives(advFirstChoice.getCourseName()));
					}
					
					if (nrCoursesFound > 0 && nrCoursesAssigned == 0 && !last.isSubstitute()) {
						notAssignedPrim ++;
						if (firstChoiceCritical) {
							notAssignedCrit ++;
							if (nrCourses > 1)
								info.addNotAssignedMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageNotEnrolledImportantCourseWithAlts(advFirstChoice.getCourseName()) :
									advCritical == Critical.VITAL ? MSG.advMessageNotEnrolledVitalCourseWithAlts(advFirstChoice.getCourseName()) :
									MSG.advMessageNotEnrolledCriticalCourseWithAlts(advFirstChoice.getCourseName()));
							else
								info.addNotAssignedMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageNotEnrolledImportantCourse(advFirstChoice.getCourseName()) :
									advCritical == Critical.VITAL ? MSG.advMessageNotEnrolledVitalCourse(advFirstChoice.getCourseName()) :
									MSG.advMessageNotEnrolledCriticalCourse(advFirstChoice.getCourseName()));
						} else {
							if (nrCourses > 1)
								info.addNotAssignedMessage(MSG.advMessageNotEnrolledCourseWithAlts(advFirstChoice.getCourseName()));
							else
								info.addNotAssignedMessage(MSG.advMessageNotEnrolledCourse(advFirstChoice.getCourseName()));
						}
					} else if (last.isSubstitute() && nrCoursesAssigned > 0) {
						notAssignedPrim --;
						info.addNotAssignedMessage(MSG.advMessageHasEnrolledSubstituteCourse(firstEnrolled.getCourseName()));
					}
					
					// 1st choice course match
					maxPoints += 4 + (firstChoiceCritical ? 2 : 0);
					if (firstChoice != null) {
						points += 2; // first choice course is present
						if (firstChoiceCritical && firstChoice.isCritical()) points +=2; // also is critical
						if (firstChoice.isAlternative() == last.isSubstitute()) points ++; // in the same table
						if (firstChoice.getCourseIds().get(0).equals(advFirstChoice)) points ++; // also first as choice (not alt to something else)
					}
					
					// number of courses match
					maxPoints += 2;
					if (nrCourses == nrCoursesFound) points += 2; // all courses exist
					
					if (nrCourses > 1) maxPoints ++;
					if (nrCourses > 1 && nrCoursesFound >= 1) points ++; // at least one
					
					if (nrCourses > 0 && !last.isSubstitute()) maxPoints += 2;
					if (nrCourses > 0 && !last.isSubstitute() && nrCoursesFound >= 1 && nrCoursesFound - nrSubstMisMatch > 0) points += 2; // and are proprity
					
					if (nrCourses > 1) maxPoints ++;
					if (nrCourses > 1 && nrCoursesFound > 1) points++; // at least two
					
					if (nrCourses > 2) maxPoints ++;
					if (nrCourses > 2 && nrCoursesFound > 2) points++; // at least three
					
					if (nrCoursesFound > 1) maxPoints ++;
					if (nrCoursesFound > 1 && foundPrioMin == foundPrioMax) points++; // and have same priority
					
					maxPoints ++;
					if (nrSubstMisMatch == 0 && nrCoursesFound > 0) points ++; // not priority mismatch
					
					if (nrCriticalCourses > 1) maxPoints += 3;
					if (nrCriticalCourses > 1 && nrCriticalCoursesFound > 1) points += 3; // critical
				}
				nrCourses = 0; nrCriticalCourses = 0; nrCoursesFound = 0; nrCriticalCoursesFound = 0; nrSubstMisMatch = 0;
				advFirstChoice = null; firstChoice = null; firstChoiceCritical = false;
				foundPrioMin = 0; foundPrioMax = 0;
				firstEnrolled = null; nrCoursesAssigned = 0;
			}
			if (!acr.isSubstitute()) {
				XCourse course = (acr.getCourseId() == null ? null : server.getCourse(acr.getCourseId().getCourseId()));
				if (course != null && course.hasCredit()) {
					if (acr.getAlternative() == 0 || course.getMinCredit() < cm) cm = course.getMinCredit();
					if (acr.getAlternative() == 0 || course.getMaxCredit() > cx) cx = course.getMaxCredit();
				} else if (acr.getAlternative() == 0) {
					cm = acr.getCreditMin(); cx = acr.getCreditMax();
				}					
			}
			if (acr.getCourseId() != null) {
				nrCourses ++;
				if (acr.isCritical() && !acr.isSubstitute())
					nrCriticalCourses ++;
				XCourseRequest request = null;
				for (XRequest r: student.getRequests()) {
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						if (cr.hasCourse(acr.getCourseId().getCourseId())) {
							request = cr; break;
						}
					}
				}
				if (advFirstChoice == null) {
					advFirstChoice = acr.getCourseId();
					firstChoiceCritical = acr.isCritical();
					firstChoice = request;
				}
				if (request != null) {
					if (request.getEnrollment() != null && request.getEnrollment().getCourseId().equals(acr.getCourseId().getCourseId())) {
						nrCoursesAssigned ++;
						if (firstEnrolled == null) firstEnrolled = acr.getCourseId();
					}
					nrCoursesFound ++;
					if (nrCoursesFound == 1) {
						foundPrioMin = request.getPriority();
						foundPrioMax = request.getPriority();
					} else {
						if (request.getPriority() < foundPrioMin) foundPrioMin = request.getPriority();
						if (request.getPriority() > foundPrioMax) foundPrioMax = request.getPriority();
					}
					if (acr.isSubstitute() != request.isAlternative()) nrSubstMisMatch ++;
					if (acr.isCritical() && request.isCritical() && !request.isAlternative()) nrCriticalCoursesFound ++;
				}
			}
			last = acr;
		}
		minCred += cm; maxCred += cx;
		info.setMinCredit(minCred); info.setMaxCredit(maxCred);
		if (advFirstChoice != null) { // ignore requests without any courses
			if (firstChoiceCritical && nrCoursesFound == 0) {
				missingCrit ++; missingPrim ++;
				if (nrCourses > 1)
					info.addMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageMissingImportantCourseWithAlts(advFirstChoice.getCourseName()) :
						advCritical == Critical.VITAL ? MSG.advMessageMissingVitalCourseWithAlts(advFirstChoice.getCourseName()) :
						MSG.advMessageMissingCriticalCourseWithAlts(advFirstChoice.getCourseName()));
				else
					info.addMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageMissingImportantCourse(advFirstChoice.getCourseName()) :
						advCritical == Critical.VITAL ? MSG.advMessageMissingVitalCourse(advFirstChoice.getCourseName()) :
						MSG.advMessageMissingCriticalCourse(advFirstChoice.getCourseName()));
			} else if (!last.isSubstitute() && nrCoursesFound - nrSubstMisMatch == 0) {
				missingPrim ++;
				if (nrCourses > 1)
					info.addMessage(MSG.advMessageMissingCourseWithAlts(advFirstChoice.getCourseName()));
				else
					info.addMessage(MSG.advMessageMissingCourse(advFirstChoice.getCourseName()));
			} else if (last.isSubstitute() && nrCoursesFound == 0) {
				if (nrCourses > 1)
					info.addMessage(MSG.advMessageMissingSubstituteCourseWithAlts(advFirstChoice.getCourseName()));
				else
					info.addMessage(MSG.advMessageMissingSubstituteCourse(advFirstChoice.getCourseName()));
			} else if (firstChoice == null && firstChoiceCritical) {
				info.addMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageMissingImportantCourseHasAlts(advFirstChoice.getCourseName()) :
					advCritical == Critical.VITAL ? MSG.advMessageMissingVitalCourseHasAlts(advFirstChoice.getCourseName()) :
					MSG.advMessageMissingCriticalCourseHasAlts(advFirstChoice.getCourseName()));
			} else if (firstChoice == null) {
				info.addMessage(MSG.advMessageMissingCourseHasAlts(advFirstChoice.getCourseName()));
			} else if (nrCoursesFound < nrCourses) {
				info.addMessage(MSG.advMessageMissingAlternatives(advFirstChoice.getCourseName()));
			}
			
			if (nrCoursesFound > 0 && nrCoursesAssigned == 0 && !last.isSubstitute()) {
				notAssignedPrim ++;
				if (firstChoiceCritical) {
					notAssignedCrit ++;
					if (nrCourses > 1)
						info.addNotAssignedMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageNotEnrolledImportantCourseWithAlts(advFirstChoice.getCourseName()) :
							advCritical == Critical.VITAL ? MSG.advMessageNotEnrolledVitalCourseWithAlts(advFirstChoice.getCourseName()) :
							MSG.advMessageNotEnrolledCriticalCourseWithAlts(advFirstChoice.getCourseName()));
					else
						info.addNotAssignedMessage(advCritical == Critical.IMPORTANT ? MSG.advMessageNotEnrolledImportantCourse(advFirstChoice.getCourseName()) :
							advCritical == Critical.VITAL ? MSG.advMessageNotEnrolledVitalCourse(advFirstChoice.getCourseName()) :
							MSG.advMessageNotEnrolledCriticalCourse(advFirstChoice.getCourseName()));
				} else {
					if (nrCourses > 1)
						info.addNotAssignedMessage(MSG.advMessageNotEnrolledCourseWithAlts(advFirstChoice.getCourseName()));
					else
						info.addNotAssignedMessage(MSG.advMessageNotEnrolledCourse(advFirstChoice.getCourseName()));
				}
			} else if (last.isSubstitute() && nrCoursesAssigned > 0) {
				notAssignedPrim --;
				info.addNotAssignedMessage(MSG.advMessageHasEnrolledSubstituteCourse(firstEnrolled.getCourseName()));
			}
			
			// 1st choice course match
			maxPoints += 4 + (firstChoiceCritical ? 2 : 0);
			if (firstChoice != null) {
				points += 2; // first choice course is present
				if (firstChoiceCritical && firstChoice.isCritical()) points +=2; // also is critical
				if (firstChoice.isAlternative() == last.isSubstitute()) points ++; // in the same table
				if (firstChoice.getCourseIds().get(0).equals(advFirstChoice)) points ++; // also first as choice (not alt to something else)
			}
			
			// number of courses match
			maxPoints += 2;
			if (nrCourses == nrCoursesFound) points += 2; // all courses exist
			
			if (nrCourses > 1) maxPoints ++;
			if (nrCourses > 1 && nrCoursesFound >= 1) points ++; // at least one
			
			if (nrCourses > 0 && !last.isSubstitute()) maxPoints += 2;
			if (nrCourses > 0 && !last.isSubstitute() && nrCoursesFound >= 1 && nrCoursesFound - nrSubstMisMatch > 0) points += 2; // and are proprity
			
			if (nrCourses > 1) maxPoints ++;
			if (nrCourses > 1 && nrCoursesFound > 1) points++; // at least two
			
			if (nrCourses > 2) maxPoints ++;
			if (nrCourses > 2 && nrCoursesFound > 2) points++; // at least three
			
			if (nrCoursesFound > 1) maxPoints ++;
			if (nrCoursesFound > 1 && foundPrioMin == foundPrioMax) points++; // and have same priority
			
			maxPoints ++;
			if (nrSubstMisMatch == 0 && nrCoursesFound > 0) points ++; // not priority mismatch
			
			if (nrCriticalCourses > 1) maxPoints += 3;
			if (nrCriticalCourses > 1 && nrCriticalCoursesFound > 1) points += 3; // critical
		}
		if (maxPoints == 0)
			info.setPercentage(0f);
		else
			info.setPercentage(((float)points)/maxPoints);
		info.setMissingCritical(missingCrit);
		info.setMissingPrimary(missingPrim);
		info.setNotAssignedCritical(notAssignedCrit);
		info.setNotAssignedPrimary(notAssignedPrim);
		info.setAdvisorCritical(advCritical.ordinal());
		
		return info;
	}
}