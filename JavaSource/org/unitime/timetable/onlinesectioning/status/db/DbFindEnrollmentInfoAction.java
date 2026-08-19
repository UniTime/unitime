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
package org.unitime.timetable.onlinesectioning.status.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.status.CourseLookup;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentInfoAction;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class DbFindEnrollmentInfoAction extends FindEnrollmentInfoAction {
	private static final long serialVersionUID = 1L;
	
	public boolean isMyStudent(Student student) {
		return iMyStudents != null && iMyStudents.contains(student.getUniqueId());
	}
	
	protected String datePatternName(DatePattern pattern, String datePatternFormat) {
    	if ("never".equals(datePatternFormat)) return pattern.getName();
    	if ("extended".equals(datePatternFormat) && !pattern.isExtended()) return pattern.getName();
    	if ("alternate".equals(datePatternFormat) && pattern.isAlternate()) return pattern.getName();
		Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
		Date first = pattern.getStartDate();
		Date last = pattern.getEndDate();
		return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
	}

	@Override
	public List<EnrollmentInfo> execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (iFilter == null) return super.execute(server, helper);
		
		List<EnrollmentInfo> ret = new ArrayList<EnrollmentInfo>();
		AcademicSessionInfo session = server.getAcademicSession();
		CourseLookup lookup = new CourseLookup(server.getAcademicSession());
		if (courseId() == null) {
			Set<Long> students = new HashSet<Long>();
			Set<Long> matchingStudents = new HashSet<Long>();
			
			int gEnrl = 0, gWait = 0, gRes = 0, gUnasg = 0, gUnasgPrim = 0, gNoSub = 0, gSwap = 0;
			int gtEnrl = 0, gtWait = 0, gtRes = 0, gtUnasg = 0, gtUnasgPrim = 0, gtNoSub = 0, gtSwap = 0;
			int gConNeed = 0, gtConNeed = 0, gOvrNeed = 0, gtOvrNeed = 0;
			
			DbFindEnrollmentInfoCourseMatcher m = new DbFindEnrollmentInfoCourseMatcher(iCoursesIcoordinate, iCoursesIcanApprove, iSubjectAreas, iQuery, lookup, server);
			
			Map<CourseOffering, List<CourseRequest>> requests = new HashMap<CourseOffering, List<CourseRequest>>();
			cr: for (CourseRequest cr: (List<CourseRequest>)SectioningStatusFilterAction.getCourseQuery(iFilter, server, helper).select("distinct cr").query(helper.getHibSession()).list()) {
				if (!m.match(cr.getCourseOffering())) continue;
				if (cr.getClassEnrollments().isEmpty()) { // skip course requests where course demand is enrolled to some other course
					for (CourseRequest x: cr.getCourseDemand().getCourseRequests())
						if (!x.equals(cr) && !x.getClassEnrollments().isEmpty()) continue cr;
				}
				List<CourseRequest> list = requests.get(cr.getCourseOffering());
				if (list == null) {
					list = new ArrayList<CourseRequest>();
					requests.put(cr.getCourseOffering(), list);
				}
				list.add(cr);
			}
			if (iShowUnmatchedCourses && SectioningStatusFilterAction.hasNoMatchCourses(iFilter, helper)) {
				try {
					for (CourseOffering co: (List<CourseOffering>)SectioningStatusFilterAction.getCourseQuery(iFilter, server, helper).selectCourses("distinct co", iFilter).query(helper.getHibSession()).list()) {
						if (requests.containsKey(co)) continue;
						if (!m.match(co)) continue;
						requests.put(co, new ArrayList<CourseRequest>());
					}
				} catch (Exception e) {
					iShowUnmatchedCourses = false;
				}
			} else {
				iShowUnmatchedCourses = false;
			}
			
			boolean checkOverrides = !query().hasAttribute("override");
			
			for (Map.Entry<CourseOffering, List<CourseRequest>> entry: requests.entrySet()) {
				CourseOffering course = entry.getKey();
				InstructionalOffering offering = course.getInstructionalOffering();
				Set<Long> allStudents = new HashSet<Long>();
				
				boolean isConsentToDoCourse = isConsentToDoCourse(course);
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(course.getUniqueId());
				e.setOfferingId(offering.getUniqueId());
				e.setSubject(course.getSubjectAreaAbbv());
				e.setCourseNbr(course.getCourseNbr());
				e.setTitle(course.getTitle());
				e.setConsent(course.getConsentType() == null ? null : course.getConsentType().getAbbv());
				e.setControl(course.isIsControl());

				int match = 0;
				int enrl = 0, wait = 0, res = 0, unasg = 0, unasgPrim = 0, noSub = 0, swap = 0;
				int tEnrl = 0, tWait = 0, tRes = 0, tUnasg = 0, tUnasgPrim = 0, tNoSub = 0, tSwap = 0;
				int conNeed = 0, tConNeed = 0, ovrNeed = 0, tOvrNeed = 0;
				
				request: for (CourseRequest request: helper.getHibSession().createQuery(
						"from CourseRequest where courseOffering.uniqueId = :courseId", CourseRequest.class
						).setParameter("courseId", course.getUniqueId()).setCacheable(true).list()) {
					
					if (checkOverrides && !request.isRequestApproved() && !request.isRequestNotNeeded() && request.getClassEnrollments().isEmpty()) continue;
					
					if (request.getClassEnrollments().isEmpty()) { // skip course requests where course demand is enrolled to some other course
						for (CourseRequest x: request.getCourseDemand().getCourseRequests())
							if (!x.equals(request) && !x.getClassEnrollments().isEmpty()) continue request;
					}
					
					DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, isMyStudent(request.getCourseDemand().getStudent()), helper.getStudentNameFormat(), lookup);
					if (!query().match(crm)) {
						allStudents.add(crm.student().getUniqueId());
						if (!crm.enrollment().isEmpty()) {
							tEnrl ++;
							if (crm.reservation() != null) tRes ++;
							if (request.getCourseOffering().getConsentType() != null && crm.approval() == null) tConNeed ++;
							if (crm.request().getCourseDemand().effectiveWaitList() && !crm.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) tSwap ++;
						} else if (crm.canAssign()) {
							tUnasg ++;
							if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
								tUnasgPrim ++;
								if (request.getCourseDemand().effectiveWaitList())
									tWait ++;
								if (request.getCourseDemand().effectiveNoSub())
									tNoSub ++;
							}
						}
					}
				}

				Set<Long> addedStudents = new HashSet<Long>();
				for (CourseRequest request: entry.getValue()) {
					if (checkOverrides && !request.isRequestApproved() && !request.isRequestNotNeeded() && request.getClassEnrollments().isEmpty()) continue;
					
					Student student = request.getCourseDemand().getStudent();
					
					if (students.add(student.getUniqueId()))
						addedStudents.add(student.getUniqueId());
					
					DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, isMyStudent(student), helper.getStudentNameFormat(), lookup);
					if (query().match(crm)) {
						matchingStudents.add(student.getUniqueId());
						match++;
						if (!crm.enrollment().isEmpty()) {
							enrl ++;
							if (crm.reservation() != null) res ++;
							if (request.getCourseOffering().getConsentType() != null && crm.approval() == null) conNeed ++;
							if (crm.request().getCourseDemand().effectiveWaitList() && !crm.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) swap ++;
						} else if (crm.canAssign()) {
							unasg ++;
							if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
								unasgPrim ++;
								if (request.getCourseDemand().effectiveWaitList())
									wait ++;
								if (request.getCourseDemand().effectiveNoSub())
									noSub ++;
							}
						}
						if (request.isRequestPending()) ovrNeed ++;
					}
					
					if (allStudents.add(crm.student().getUniqueId())) {
						if (!crm.enrollment().isEmpty()) {
							tEnrl ++;
							if (crm.reservation() != null) tRes ++;
							if (request.getCourseOffering().getConsentType() != null && crm.approval() == null) tConNeed ++;
							if (crm.request().getCourseDemand().effectiveWaitList() && !crm.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) tSwap ++;
						} else if (crm.canAssign()) {
							tUnasg ++;
							if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
								tUnasgPrim ++;
								if (request.getCourseDemand().effectiveWaitList())
									tWait ++;
								if (request.getCourseDemand().effectiveNoSub())
									tNoSub ++;
							}
						}
					}
					if (request.isRequestPending()) tOvrNeed ++;
				}
				
				if (match == 0 && !iShowUnmatchedCourses) {
					students.removeAll(addedStudents);
					continue;
				}
				e.setNoMatch(match == 0);
				
				gEnrl += enrl;
				gWait += wait;
				gNoSub += noSub;
				gSwap += swap;
				gUnasg += unasg;
				gUnasgPrim += unasgPrim;
				gRes += res;
				gConNeed += conNeed;
				gOvrNeed += ovrNeed;
				
				gtEnrl += tEnrl;
				gtWait += tWait;
				gtNoSub += tNoSub;
				gtSwap += tSwap;
				gtUnasg += tUnasg;
				gtUnasgPrim += tUnasgPrim;
				gtRes += tRes;
				gtConNeed += tConNeed;
				gtOvrNeed += tOvrNeed;
				
				int limit = 0;
				for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
					if (config.isUnlimitedEnrollment()) {
						limit = -1; break;
					} else {
						limit += config.getLimit();
					}
				}

				e.setLimit(course.getReservation() != null ? course.getReservation() : limit);
				e.setSnapshot(course.getInstructionalOffering().getSnapshotLimit());
				e.setProjection(course.getProjectedDemand() != null ? course.getProjectedDemand().intValue() : course.getDemand() != null ? course.getDemand().intValue() : 0);
				int av = (int)Math.max(0, offering.getUnreservedSpace());
				if (e.getLimit() >= 0 && av > e.getLimit() - course.getEnrollment())
					av = e.getLimit() - course.getEnrollment();
				if (av == Integer.MAX_VALUE) av = -1;
				e.setAvailable(av);
				if (av >= 0) {
					int other = 0;
					for (CourseOffering c: offering.getCourseOfferings())
						if (!c.equals(course))
							other += c.getEnrollment();
					e.setOther(Math.min(e.getLimit() - course.getEnrollment() - av, other));
					int lim = 0;
					for (InstrOfferingConfig f: offering.getInstrOfferingConfigs()) {
						if (lim < 0 || f.isUnlimitedEnrollment())
							lim = -1;
						else
							lim += f.getLimit();
					}
					if (lim >= 0 && lim < e.getLimit())
						e.setOther(e.getOther() + e.getLimit() - limit);
				}
				
				e.setEnrollment(enrl);
				e.setReservation(res);
				e.setWaitlist(wait);
				e.setNoSub(noSub);
				e.setSwap(swap);
				e.setUnassigned(unasg);
				e.setUnassignedPrimary(unasgPrim);
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);
				e.setTotalNoSub(tNoSub);
				e.setTotalSwap(tSwap);
				e.setTotalUnassigned(tUnasg);
				e.setTotalUnassignedPrimary(tUnasgPrim);
				
				e.setConsentNeeded(conNeed);
				e.setTotalConsentNeeded(tConNeed);
				e.setOverrideNeeded(ovrNeed);
				e.setTotalOverrideNeeded(tOvrNeed);

				ret.add(e);
				if (limit() != null && ret.size() >= limit()) break;
			}
			
			final Comparator noc = new NaturalOrderComparator();
			Collections.sort(ret, new Comparator<EnrollmentInfo>() {
				@Override
				public int compare(EnrollmentInfo e1, EnrollmentInfo e2) {
					int cmp = noc.compare(e1.getSubject(), e2.getSubject());
					if (cmp != 0) return cmp;
					cmp = e1.getCourseNbr().compareTo(e2.getCourseNbr());
					if (cmp != 0) return cmp;
					return 0;
				}
			});
			
			EnrollmentInfo t = new EnrollmentInfo();
			t.setSubject(MSG.total());
			t.setCourseNbr("");
			
			t.setLimit(students.size());
			t.setAvailable(matchingStudents.size());
			
			t.setEnrollment(gEnrl);
			t.setReservation(gRes);
			t.setWaitlist(gWait);
			t.setNoSub(gNoSub);
			t.setSwap(gSwap);
			t.setUnassigned(gUnasg);
			t.setUnassignedPrimary(gUnasgPrim);
			
			t.setTotalEnrollment(gtEnrl);
			t.setTotalReservation(gtRes);
			t.setTotalWaitlist(gtWait);
			t.setTotalNoSub(gtNoSub);
			t.setTotalSwap(gtSwap);
			t.setTotalUnassigned(gtUnasg);
			t.setTotalUnassignedPrimary(gtUnasgPrim);
			
			t.setConsentNeeded(gConNeed);
			t.setTotalConsentNeeded(gtConNeed);
			t.setOverrideNeeded(gOvrNeed);
			t.setTotalOverrideNeeded(gtOvrNeed);
			ret.add(t);
		} else {
			final CourseOffering course = CourseOfferingDAO.getInstance().get(courseId(), helper.getHibSession());
			if (course == null) return ret;
			final InstructionalOffering offering = course.getInstructionalOffering();
			if (offering == null) return ret;
			List<CourseRequest> requests = helper.getHibSession().createQuery(
					"from CourseRequest where courseOffering.instructionalOffering.uniqueId = :offeringId", CourseRequest.class
					).setParameter("offeringId", offering.getUniqueId()).setCacheable(true).list();
			OverExpectedCriterion overExp = server.getOverExpectedCriterion();
			boolean checkOverrides = !query().hasAttribute("override");
			
			if (offering.getCourseOfferings().size() > 1) {
				Set<Long> allStudents = new HashSet<Long>();
				for (CourseOffering other: offering.getCourseOfferings()) {
					if (course.equals(other)) continue;

					boolean isConsentToDoCourse = isConsentToDoCourse(other);
					EnrollmentInfo e = new EnrollmentInfo();
					e.setCourseId(other.getUniqueId());
					e.setOfferingId(offering.getUniqueId());
					e.setSubject(other.getSubjectAreaAbbv());
					e.setCourseNbr(other.getCourseNbr());
					e.setTitle(other.getTitle());
					e.setConsent(other.getConsentType() == null ? null : other.getConsentType().getAbbv());
					e.setControl(other.isIsControl());
					e.setMasterCourseId(course.getUniqueId());
					e.setMasterSubject(course.getSubjectAreaAbbv());
					e.setMasterCourseNbr(course.getCourseNbr());
					e.setConfigId(-1l);

					int enrl = 0, wait = 0, res = 0, unasg = 0, unasgPrim = 0, noSub = 0, swap = 0;
					int tEnrl = 0, tWait = 0, tRes = 0, tUnasg = 0, tUnasgPrim = 0, tNoSub = 0, tSwap = 0;
					int conNeed = 0, tConNeed = 0, ovrNeed = 0, tOvrNeed = 0;
					
					for (CourseRequest request: requests) {
						if (!request.getCourseOffering().equals(other)) continue;
						if (checkOverrides && !request.isRequestApproved() && !request.isRequestNotNeeded() && request.getClassEnrollments().isEmpty()) continue;
						
						DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, isMyStudent(request.getCourseDemand().getStudent()), helper.getStudentNameFormat(), lookup);
						if (query().match(crm)) {
							if (!crm.enrollment().isEmpty()) {
								enrl ++;
								if (crm.reservation() != null) res ++;
								if (request.getCourseOffering().getConsentType() != null && crm.approval() == null) conNeed ++;
								if (crm.request().getCourseDemand().effectiveWaitList() && !crm.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) swap ++;
							} else if (crm.canAssign()) {
								unasg ++;
								if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
									unasgPrim ++;
									if (request.getCourseDemand().effectiveWaitList())
										wait ++;
									if (request.getCourseDemand().effectiveNoSub())
										noSub ++;
								}
							}
							if (request.isRequestPending()) ovrNeed ++;
						}
						if (allStudents.add(crm.student().getUniqueId())) {
							if (!crm.enrollment().isEmpty()) {
								tEnrl ++;
								if (crm.reservation() != null) tRes ++;
								if (request.getCourseOffering().getConsentType() != null && crm.approval() == null) tConNeed ++;
								if (crm.request().getCourseDemand().effectiveWaitList() && !crm.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) tSwap ++;
							} else if (crm.canAssign()) {
								tUnasg ++;
								if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
									tUnasgPrim ++;
									if (request.getCourseDemand().effectiveWaitList())
										tWait ++;
									if (request.getCourseDemand().effectiveNoSub())
										tNoSub ++;
								}
							}
							if (request.isRequestPending()) tOvrNeed ++;
						}
					}
					
					int limit = 0;
					for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
						if (config.isUnlimitedEnrollment()) {
							limit = -1; break;
						} else {
							limit += config.getLimit();
						}
					}

					e.setLimit(other.getReservation() != null ? other.getReservation() : limit);
					e.setProjection(other.getProjectedDemand() != null ? other.getProjectedDemand().intValue() : other.getDemand() != null ? other.getDemand().intValue() : 0);
					int av = (int)Math.max(0, offering.getUnreservedSpace());
					if (e.getLimit() >= 0 && av > e.getLimit() - other.getEnrollment())
						av = e.getLimit() - other.getEnrollment();
					if (av == Integer.MAX_VALUE) av = -1;
					e.setAvailable(av);
					if (av >= 0) {
						int otherEnrl = 0;
						for (CourseOffering c: offering.getCourseOfferings())
							if (!c.equals(other))
								otherEnrl += c.getEnrollment();
						e.setOther(Math.min(e.getLimit() - other.getEnrollment() - av, otherEnrl));
						int lim = 0;
						for (InstrOfferingConfig f: offering.getInstrOfferingConfigs()) {
							if (lim < 0 || f.isUnlimitedEnrollment())
								lim = -1;
							else
								lim += f.getLimit();
						}
						if (lim >= 0 && lim < e.getLimit())
							e.setOther(e.getOther() + e.getLimit() - limit);
					}
					
					e.setEnrollment(enrl);
					e.setReservation(res);
					e.setWaitlist(wait);
					e.setNoSub(noSub);
					e.setSwap(swap);
					e.setUnassigned(unasg);
					e.setUnassignedPrimary(unasgPrim);
					
					e.setTotalEnrollment(tEnrl);
					e.setTotalReservation(tRes);
					e.setTotalWaitlist(tWait);
					e.setTotalNoSub(tNoSub);
					e.setTotalSwap(tSwap);
					e.setTotalUnassigned(tUnasg);
					e.setTotalUnassignedPrimary(tUnasgPrim);
					
					e.setConsentNeeded(conNeed);
					e.setTotalConsentNeeded(tConNeed);
					e.setOverrideNeeded(ovrNeed);
					e.setTotalOverrideNeeded(tOvrNeed);

					ret.add(e);
					if (limit() != null && ret.size() >= limit()) break;
				}
				
				final Comparator noc = new NaturalOrderComparator();
				Collections.sort(ret, new Comparator<EnrollmentInfo>() {
					@Override
					public int compare(EnrollmentInfo e1, EnrollmentInfo e2) {
						int cmp = noc.compare(e1.getSubject(), e2.getSubject());
						if (cmp != 0) return cmp;
						cmp = e1.getCourseNbr().compareTo(e2.getCourseNbr());
						if (cmp != 0) return cmp;
						return 0;
					}
				});
			}
			
			boolean isConsentToDoCourse = isConsentToDoCourse(course);
			List<Class_> sections = new ArrayList<Class_>();
			for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
				for (SchedulingSubpart subpart: config.getSchedulingSubparts())
					sections.addAll(subpart.getClasses());
			Collections.sort(sections, new Comparator<Class_>() {
				public int compare(InstrOfferingConfig c1, InstrOfferingConfig c2) {
					int cmp = c1.getName().compareToIgnoreCase(c2.getName());
					if (cmp != 0) return cmp;
					return c1.getUniqueId().compareTo(c2.getUniqueId());
				}
				public boolean isParent(SchedulingSubpart s1, SchedulingSubpart s2) {
					SchedulingSubpart p1 = s1.getParentSubpart();
					if (p1==null) return false;
					if (p1.equals(s2)) return true;
					return isParent(p1, s2);
				}
				public int compare(SchedulingSubpart s1, SchedulingSubpart s2) {
					int cmp = compare(s1.getInstrOfferingConfig(), s2.getInstrOfferingConfig());
					if (cmp != 0) return cmp;
			        if (isParent(s1,s2)) return 1;
			        if (isParent(s2,s1)) return -1;
			        cmp = s1.getItype().compareTo(s2.getItype());
			        if (cmp != 0) return cmp;
			        return s1.getUniqueId().compareTo(s2.getUniqueId());
				}
				public int compare(Class_ s1, Class_ s2) {
					if (s1.getSchedulingSubpart().equals(s2.getSchedulingSubpart())) {
						if (s1.getParentClass() != null) {
							int cmp = compare(s1.getParentClass(), s2.getParentClass());
							if (cmp != 0) return cmp;
						}
						try {
							int cmp = Integer.valueOf(s1.getClassSuffix(course) == null ? "0" : s1.getClassSuffix(course)).compareTo(Integer.valueOf(s2.getClassSuffix(course) == null ? "0" : s2.getClassSuffix(course)));
							if (cmp != 0) return cmp;
						} catch (NumberFormatException e) {}
						int cmp = (s1.getClassSuffix(course) == null ? "" : s1.getClassSuffix(course)).compareTo(s2.getClassSuffix(course) == null ? "" : s2.getClassSuffix(course));
						if (cmp != 0) return cmp;
				        return s1.getUniqueId().compareTo(s2.getUniqueId());
					}
					Class_ x = s1;
					while (x != null) {
						if (isParent(s2.getSchedulingSubpart(), x.getSchedulingSubpart())) {
							Class_ s = s2.getParentClass();
							while (!s.getSchedulingSubpart().equals(x.getSchedulingSubpart())) {
								s = s.getParentClass();
							}
							int cmp = compare(x, s);
							return (cmp == 0 ? x.equals(s1) ? -1 : compare(x.getSchedulingSubpart(), s.getSchedulingSubpart()) : cmp);
						}
						x = x.getParentClass();
					}
					x = s2;
					while (x != null) {
						if (isParent(s1.getSchedulingSubpart(), x.getSchedulingSubpart())) {
							Class_ s = s1.getParentClass();
							while (!s.getSchedulingSubpart().equals(x.getSchedulingSubpart())) {
								s = s.getParentClass();
							}
							int cmp = compare(s, x);
							return (cmp == 0 ? x.equals(s2) ? 1 : compare(x.getSchedulingSubpart(), x.getSchedulingSubpart()) : cmp);
						}
						x = x.getParentClass();
					}
					int cmp = compare(s1.getSchedulingSubpart(), s2.getSchedulingSubpart());
					if (cmp != 0) return cmp;
					try {
						cmp = Integer.valueOf(s1.getClassSuffix(course) == null ? "0" : s1.getClassSuffix(course)).compareTo(Integer.valueOf(s2.getClassSuffix(course) == null ? "0" : s2.getClassSuffix(course)));
						if (cmp != 0) return cmp;
					} catch (NumberFormatException e) {}
					cmp = (s1.getClassSuffix(course) == null ? "" : s1.getClassSuffix(course)).compareTo(s2.getClassSuffix(course) == null ? "" : s2.getClassSuffix(course));
					if (cmp != 0) return cmp;
			        return s1.getUniqueId().compareTo(s2.getUniqueId());
				}
			});

			for (Class_ section: sections) {
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(course.getUniqueId());
				e.setOfferingId(offering.getUniqueId());
				e.setSubject(course.getSubjectAreaAbbv());
				e.setCourseNbr(course.getCourseNbr());
				e.setTitle(course.getTitle());
				e.setConsent(course.getConsentType() == null ? null : course.getConsentType().getAbbv());
				
				SchedulingSubpart subpart = section.getSchedulingSubpart();
				InstrOfferingConfig config = subpart.getInstrOfferingConfig();
				e.setConfig(config.getName());
				e.setConfigId(config.getUniqueId());
				
				e.setSubpart(subpart.getItype().getAbbv().trim());
				if (subpart.getInstrOfferingConfig().getInstructionalMethod() != null)
					e.setSubpart(e.getSubpart() + " (" + subpart.getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
				e.setSubpartId(subpart.getUniqueId());
				e.setClazz(section.getClassSuffix(course));
				if (e.getClazz() == null)
					e.setClazz(section.getSectionNumberString());
				e.setClazzId(section.getUniqueId());
				Class_ parent = section.getParentClass();
				while (parent != null) {
					e.incLevel();
					parent = parent.getParentClass();
				}
				
				int match = 0;
				int enrl = 0, wait = 0, res = 0, unasg = 0, unasgPrim = 0, noSub = 0, swap = 0;
				int tEnrl = 0, tWait = 0, tRes = 0, tUnasg = 0, tUnasgPrim = 0, tNoSub = 0, tSwap = 0;
				int conNeed = 0, tConNeed = 0;
				int other = 0;

				for (CourseRequest request: requests) {
					DbCourseRequestMatcher m = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, isMyStudent(request.getCourseDemand().getStudent()), helper.getStudentNameFormat(), lookup, section);
					boolean contains = false;
					for (StudentClassEnrollment x: m.enrollment()) {
						if (x.getClazz().equals(section)) { contains = true; break; }
					}
					if (!contains) continue;
					if (!request.getCourseOffering().equals(course)) {other++; continue; }
					if (query().match(m)) {
						match++;
						enrl ++;
						if (m.reservation() != null) res ++;
						if (course.getConsentType() != null && m.approval() == null) conNeed ++;
						if (m.request().getCourseDemand().effectiveWaitList() && !m.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) swap ++;
					}
					
					tEnrl ++;
					if (m.reservation() != null) tRes ++;
					if (course.getConsentType() != null && m.approval() == null) tConNeed ++;
					if (m.request().getCourseDemand().effectiveWaitList() && !m.request().getCourseDemand().isEnrolledExceptForWaitListSwap()) tSwap ++;
				}

				for (CourseRequest request: requests) {
					DbCourseRequestMatcher m = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, isMyStudent(request.getCourseDemand().getStudent()), helper.getStudentNameFormat(), lookup, section);
					if (!m.enrollment().isEmpty() || !request.getCourseOffering().equals(course)) continue;
					if (!m.canAssign()) continue;
					if (checkOverrides && !request.isRequestApproved() && !request.isRequestNotNeeded()) continue;
					
					if (query().match(m)) {
						match++;
						unasg++;
						if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
							unasgPrim ++;
							if (request.getCourseDemand().effectiveWaitList())
								wait++;
							if (request.getCourseDemand().effectiveNoSub())
								noSub ++;
						}
					}
					tUnasg ++;
					if (!request.getCourseDemand().isAlternative() && request.getOrder() == 0) {
						tUnasgPrim ++;
						if (request.getCourseDemand().effectiveWaitList())
							tWait ++;
						if (request.getCourseDemand().effectiveNoSub())
							tNoSub ++;
					}
				}
				
				if (match == 0 && !iShowUnmatchedClasses) continue;
				e.setNoMatch(match == 0);
				
				e.setLimit(section.getSectioningLimit());
				e.setOther(other);
				e.setAvailable(section.isCancelled() || !section.isEnabledForStudentScheduling() ? 0 : Math.max(0, section.getUnreservedSectionSpace()));
				if (e.getAvailable() == Integer.MAX_VALUE) e.setAvailable(-1);
				e.setProjection(tEnrl + Math.max(0, (int)Math.round(section.getSectioningInfo() == null ? 0 : section.getSectioningInfo().getNbrExpectedStudents())));
				e.setSnapshot(section.getSnapshotLimit());
				
				e.setEnrollment(enrl);
				e.setReservation(res);
				e.setWaitlist(wait);
				e.setSwap(swap);
				e.setNoSub(noSub);
				e.setUnassigned(unasg);
				e.setUnassignedPrimary(unasgPrim);
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);
				e.setTotalSwap(tSwap);
				e.setTotalNoSub(tNoSub);
				e.setTotalUnassigned(tUnasg);
				e.setTotalUnassignedPrimary(tUnasgPrim);

				e.setConsentNeeded(conNeed);
				e.setTotalConsentNeeded(tConNeed);

				ClassAssignment a = new ClassAssignment();
				a.setClassId(section.getUniqueId());
				a.setSubpart(subpart.getItype().getAbbv().trim());
				if (subpart.getInstrOfferingConfig().getInstructionalMethod() != null)
					a.setSubpart(a.getSubpart() + " (" + subpart.getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
				a.setClassNumber(section.getClassSuffix() == null ? section.getSectionNumber(helper.getHibSession()) + section.getSchedulingSubpart().getSchedulingSubpartSuffix(helper.getHibSession()) : section.getClassSuffix());
				a.setSection(section.getClassSuffix(course));
				a.setExternalId(section.getExternalId(course));
				a.setCancelled(section.isCancelled());
				a.setLimit(new int[] { section.getEnrollment(), section.getSectioningLimit()});
				Assignment assignment = section.getCommittedAssignment();
				if (assignment != null) {
					for (DayCode d : DayCode.toDayCodes(assignment.getDays()))
						a.addDay(d.getIndex());
					a.setStart(assignment.getStartSlot());
					a.setLength(assignment.getSlotPerMtg());
					a.setBreakTime(assignment.getBreakTime());
					a.setDatePattern(XTime.datePatternName(assignment, helper.getDatePatternFormat()));
					for (Location rm: assignment.getRooms())
						a.addRoom(rm.getUniqueId(), rm.getLabelWithDisplayName());
				} else {
		        	for (Iterator<?> i = section.effectivePreferences(RoomPref.class).iterator(); i.hasNext(); ) {
		        		RoomPref p = (RoomPref)i.next();
		        		if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) {
		        			a.addRoom(p.getRoom().getUniqueId(), p.getRoom().getLabel());
		        		}
		        	}
		        	DatePattern dp = section.effectiveDatePattern();
		        	if (dp != null)
		        		a.setDatePattern(datePatternName(dp, helper.getDatePatternFormat()));
				}
				if (section.isDisplayInstructor() && !section.getClassInstructors().isEmpty()) {
					for (ClassInstructor instructor: section.getClassInstructors()) {
						a.addInstructor(helper.getInstructorNameFormat().format(instructor.getInstructor()));
						a.addInstructoEmail(instructor.getInstructor().getEmail());
					}
				}
				if (section.getParentClass()!= null)
					a.setParentSection(section.getParentClass().getClassSuffix(course));
				a.setSubpartId(section.getSchedulingSubpart().getUniqueId());
				a.addNote(course.getScheduleBookNote());
				a.addNote(section.getSchedulePrintNote());
				if (section.getSchedulingSubpart().getCredit() != null) {
					a.setCredit(section.getSchedulingSubpart().getCredit().creditAbbv() + "|" + section.getSchedulingSubpart().getCredit().creditText());
				} else if (section.getParentClass() != null && course.getCredit() != null) {
					a.setCredit(course.getCredit().creditAbbv() + "|" + course.getCredit().creditText());
				}
				Float creditOverride = section.getCredit(course);
				if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
				if (a.getParentSection() == null) {
					String consent = (course.getConsentType() == null ? null : course.getConsentType().getLabel());
					if (consent != null)
						a.setParentSection(consent);
				}
				a.setExpected(overExp.getExpected(section.getSectioningLimit(), section.getSectioningInfo() == null ? 0.0 : section.getSectioningInfo().getNbrExpectedStudents()));
				e.setAssignment(a);
				
				ret.add(e);
			}
		}
		return ret;
	}
	
	public boolean isConsentToDoCourse(CourseOffering course) {
		return iCoursesIcanApprove != null && course.getConsentType() != null && iCoursesIcanApprove.contains(course.getUniqueId());
	}
	
	public static class DbFindEnrollmentInfoCourseMatcher extends FindEnrollmentInfoCourseMatcher {
		private static final long serialVersionUID = 1L;
		
		public DbFindEnrollmentInfoCourseMatcher(Set<Long> coursesIcoordinate, Set<Long> coursesIcanApprove, Set<String> subjects, Query query, CourseLookup lookup, OnlineSectioningServer server) {
			super(coursesIcoordinate, coursesIcanApprove, subjects, query, lookup, server);
		}
		
		public boolean isConsentToDoCourse(CourseOffering co) {
			return iCoursesIcanApprove != null && co.getConsentType() != null && iCoursesIcanApprove.contains(co.getUniqueId());
		}
		
		public boolean match(CourseOffering co) {
			return co != null && isCourseVisible(co.getUniqueId()) && hasMatchingSubjectArea(co.getSubjectAreaAbbv()) && iQuery.match(new DbCourseInfoMatcher(co, isConsentToDoCourse(co), iLookup));
		}
		
	} 
}
