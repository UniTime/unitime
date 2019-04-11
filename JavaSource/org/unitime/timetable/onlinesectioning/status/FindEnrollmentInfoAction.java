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

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.commons.NaturalOrderComparator;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.match.AbstractCourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseInfoMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseLookup;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseRequestMatcher;
import org.unitime.timetable.solver.studentsct.StudentSolver;

/**
 * @author Tomas Muller
 */
public class FindEnrollmentInfoAction implements OnlineSectioningAction<List<EnrollmentInfo>> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected Query iQuery;
	protected Integer iLimit = null;
	protected Long iCourseId;
	protected Set<Long> iCoursesIcoordinate, iCoursesIcanApprove, iMyStudents;
	protected Set<String> iSubjectAreas;
	
	public FindEnrollmentInfoAction withParams(String query, Long courseId, Set<Long> coursesIcoordinage, Set<Long> coursesIcanApprove, Set<Long> myStudents, Set<String> subjects) {
		iQuery = new Query(query);
		iCourseId = courseId;
		iCoursesIcanApprove = coursesIcanApprove;
		iCoursesIcoordinate = coursesIcoordinage;
		iMyStudents = myStudents;
		iSubjectAreas = subjects;
		Matcher m = Pattern.compile("limit:[ ]?([0-9]*)", Pattern.CASE_INSENSITIVE).matcher(query);
		if (m.find()) {
			iLimit = Integer.parseInt(m.group(1));
		}
		return this;
	}
	
	protected SectioningStatusFilterRpcRequest iFilter = null;
	public FindEnrollmentInfoAction withFilter(SectioningStatusFilterRpcRequest filter) {
		iFilter = filter;
		return this;
	}
	
	public boolean isMyStudent(XStudentId student) {
		return iMyStudents != null && iMyStudents.contains(student.getStudentId());
	}
	
	public Query query() { return iQuery; }
	
	public Integer limit() { return iLimit; }
	
	public Long courseId() { return iCourseId; }
	
	public boolean isCourseVisible(Long courseId) {
		return iCoursesIcoordinate == null || iCoursesIcoordinate.contains(courseId);
	}
	
	public boolean isConsentToDoCourse(XCourse course) {
		return iCoursesIcanApprove != null && course.getConsentLabel() != null && iCoursesIcanApprove.contains(course.getCourseId());
	}
	
	public boolean hasMatchingSubjectArea(String subject) {
		return iSubjectAreas == null || iSubjectAreas.contains(subject);
	}	
	
	@Override
	public List<EnrollmentInfo> execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		List<EnrollmentInfo> ret = new ArrayList<EnrollmentInfo>();
		AcademicSessionInfo session = server.getAcademicSession();
		CourseLookup lookup = new CourseLookup(session);
		boolean solver = (server instanceof StudentSolver);
		Set<Long> studentIds = null;
		if (!solver) 
			studentIds = (iFilter == null ? null : server.createAction(SectioningStatusFilterAction.class).forRequest(iFilter).getStudentIds(server, helper));
		if (courseId() == null) {
			Set<Long> students = new HashSet<Long>();
			Set<Long> matchingStudents = new HashSet<Long>();
			
			int gEnrl = 0, gWait = 0, gRes = 0, gUnasg = 0, gUnasgPrim = 0;
			int gtEnrl = 0, gtWait = 0, gtRes = 0, gtUnasg = 0, gtUnasgPrim = 0;
			int gConNeed = 0, gtConNeed = 0, gOvrNeed = 0, gtOvrNeed = 0;
			
			for (XCourseId info: server.findCourses(new FindEnrollmentInfoCourseMatcher(iCoursesIcoordinate, iCoursesIcanApprove, iSubjectAreas, iQuery, lookup))) {
				XOffering offering = server.getOffering(info.getOfferingId());
				if (offering == null) continue;
				XCourse course = offering.getCourse(info.getCourseId());
				if (course == null) continue;
				XEnrollments enrollments = server.getEnrollments(info.getOfferingId());
				boolean isConsentToDoCourse = isConsentToDoCourse(course);
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(info.getCourseId());
				e.setOfferingId(offering.getOfferingId());
				e.setSubject(course.getSubjectArea());
				e.setCourseNbr(course.getCourseNumber());
				e.setTitle(course.getTitle());
				e.setConsent(course.getConsentAbbv());

				int match = 0;
				int enrl = 0, wait = 0, res = 0, unasg = 0, unasgPrim = 0;
				int tEnrl = 0, tWait = 0, tRes = 0, tUnasg = 0, tUnasgPrim = 0;
				int conNeed = 0, tConNeed = 0, ovrNeed = 0, tOvrNeed = 0;
				
				Set<Long> addedStudents = new HashSet<Long>();
				boolean checkOverrides = !query().hasAttribute("override");
				for (XCourseRequest request: enrollments.getRequests()) {
					if (!request.hasCourse(info.getCourseId())) continue;
					if (students.add(request.getStudentId()))
						addedStudents.add(request.getStudentId());
					if (request.getEnrollment() != null && !request.getEnrollment().getCourseId().equals(info.getCourseId())) continue;
					if (checkOverrides && request.getEnrollment() == null) {
						XOverride override = request.getOverride(info);
						if (override != null && !override.isApproved()) continue;
					}
					
					if (studentIds != null && !studentIds.contains(request.getStudentId())) {
						if (request.getEnrollment() != null) {
							tEnrl ++;
							if (request.getEnrollment().getReservation() != null) tRes ++;
							if (course.getConsentLabel() != null && request.getEnrollment().getApproval() == null) tConNeed ++;
						} else {
							XStudent student = server.getStudent(request.getStudentId());
							if (student != null && student.canAssign(request)) {
								tUnasg ++;
								if (!request.isAlternative() && request.isPrimary(info)) {
									tUnasgPrim ++;
									if (request.isWaitlist())
										tWait ++;
								}
							}
						}
						continue;
					}
					
					XStudent student = server.getStudent(request.getStudentId());
					if (student == null) continue;
					CourseRequestMatcher m = new CourseRequestMatcher(session, course, student, offering, request, isConsentToDoCourse, isMyStudent(student), lookup, server);
					if (query().match(m)) {
						matchingStudents.add(request.getStudentId());
						match++;
						if (m.enrollment() != null) {
							enrl ++;
							if (m.enrollment().getReservation() != null) res ++;
							if (course.getConsentLabel() != null && m.enrollment().getApproval() == null) conNeed ++;
						} else if (m.student().canAssign(m.request())) {
							unasg ++;
							if (!m.request().isAlternative() && m.request().isPrimary(info)) {
								unasgPrim ++;
								if (m.request().isWaitlist())
									wait ++;
							}
						}
						if (m.request().isOverridePending(course)) ovrNeed ++;
					} else if (solver) {
						if (request.getEnrollment() != null) {
							tEnrl ++;
							if (request.getEnrollment().getReservation() != null) tRes ++;
							if (course.getConsentLabel() != null && request.getEnrollment().getApproval() == null) tConNeed ++;
						} else {
							if (student != null && student.canAssign(request)) {
								tUnasg ++;
								if (!request.isAlternative() && request.isPrimary(info)) {
									tUnasgPrim ++;
									if (request.isWaitlist())
										tWait ++;
								}
							}
						}
						continue;
					}
					
					if (m.enrollment() != null) {
						tEnrl ++;
						if (m.enrollment().getReservation() != null) tRes ++;
						if (course.getConsentLabel() != null && m.enrollment().getApproval() == null) tConNeed ++;
					} else if (m.student().canAssign(m.request())) {
						tUnasg ++;
						if (!m.request().isAlternative() && m.request().isPrimary(info)) {
							tUnasgPrim ++;
							if (m.request().isWaitlist())
								tWait ++;
						}
					}
					if (m.request().isOverridePending(course)) tOvrNeed ++;
				}
				
				if (match == 0) {
					students.removeAll(addedStudents);
					continue;
				}
				
				gEnrl += enrl;
				gWait += wait;
				gUnasg += unasg;
				gUnasgPrim += unasgPrim;
				gRes += res;
				gConNeed += conNeed;
				gOvrNeed += ovrNeed;
				
				gtEnrl += tEnrl;
				gtWait += tWait;
				gtUnasg += tUnasg;
				gtUnasgPrim += tUnasgPrim;
				gtRes += tRes;
				gtConNeed += tConNeed;
				gtOvrNeed += tOvrNeed;
				
				int limit = 0;
				for (XConfig config: offering.getConfigs()) {
					if (config.getLimit() < 0) {
						limit = -1; break;
					} else {
						limit += config.getLimit();
					}
				}
									
				e.setLimit(course.getLimit());
				e.setProjection(course.getProjected());
				int av = (int)Math.max(0, offering.getUnreservedSpace(enrollments));
				if (course.getLimit() >= 0 && av > course.getLimit() - enrollments.countEnrollmentsForCourse(info.getCourseId()))
					av = course.getLimit() - enrollments.countEnrollmentsForCourse(info.getCourseId());
				if (av == Integer.MAX_VALUE) av = -1;
				e.setAvailable(av);
				if (av >= 0) {
					int other = 0;
					for (XCourse c: offering.getCourses())
						if (!c.equals(course))
							other += enrollments.countEnrollmentsForCourse(c.getCourseId());
					e.setOther(Math.min(course.getLimit() - enrollments.countEnrollmentsForCourse(info.getCourseId()) - av, other));
					int lim = 0;
					for (XConfig f: offering.getConfigs()) {
						if (lim < 0 || f.getLimit() < 0)
							lim = -1;
						else
							lim += f.getLimit();
					}
					if (lim >= 0 && lim < course.getLimit())
						e.setOther(e.getOther() + course.getLimit() - limit);
				}
				
				e.setEnrollment(enrl);
				e.setReservation(res);
				e.setWaitlist(wait);
				e.setUnassigned(unasg);
				e.setUnassignedPrimary(unasgPrim);
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);
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
			t.setUnassigned(gUnasg);
			t.setUnassignedPrimary(gUnasgPrim);
			
			t.setTotalEnrollment(gtEnrl);
			t.setTotalReservation(gtRes);
			t.setTotalWaitlist(gtWait);
			t.setTotalUnassigned(gtUnasg);
			t.setTotalUnassignedPrimary(gtUnasgPrim);
			
			t.setConsentNeeded(gConNeed);
			t.setTotalConsentNeeded(gtConNeed);
			t.setOverrideNeeded(gOvrNeed);
			t.setTotalOverrideNeeded(gtOvrNeed);
			ret.add(t);
		} else {
			XCourse info = server.getCourse(courseId());
			if (info == null) return ret;
			final XOffering offering = server.getOffering(info.getOfferingId());
			if (offering == null) return ret;
			XCourse course = offering.getCourse(info.getCourseId());
			if (course == null) return ret;
			XEnrollments enrollments = server.getEnrollments(info.getOfferingId());
			OverExpectedCriterion overExp = server.getOverExpectedCriterion();
			XExpectations expectations = server.getExpectations(info.getOfferingId());
			boolean isConsentToDoCourse = isConsentToDoCourse(info);
			List<XSection> sections = new ArrayList<XSection>();
			for (XConfig config: offering.getConfigs())
				for (XSubpart subpart: config.getSubparts())
					sections.addAll(subpart.getSections());
			Collections.sort(sections, new Comparator<XSection>() {
				public int compare(XConfig c1, XConfig c2) {
					int cmp = c1.getName().compareToIgnoreCase(c2.getName());
					if (cmp != 0) return cmp;
					return c1.getConfigId().compareTo(c2.getConfigId());
				}
				public boolean isParent(XSubpart s1, XSubpart s2) {
					XSubpart p1 = (s1.getParentId() == null ? null : offering.getSubpart(s1.getParentId()));
					if (p1==null) return false;
					if (p1.equals(s2)) return true;
					return isParent(p1, s2);
				}
				public int compare(XSubpart s1, XSubpart s2) {
					int cmp = compare(offering.getConfig(s1.getConfigId()), offering.getConfig(s2.getConfigId()));
					if (cmp != 0) return cmp;
			        if (isParent(s1,s2)) return 1;
			        if (isParent(s2,s1)) return -1;
			        cmp = s1.getInstructionalType().compareTo(s2.getInstructionalType());
			        if (cmp != 0) return cmp;
			        return s1.getSubpartId().compareTo(s2.getSubpartId());
				}
				public int compare(XSection s1, XSection s2) {
					if (s1.getSubpartId().equals(s2.getSubpartId())) {
						if (s1.getParentId() != null) {
							int cmp = compare(offering.getSection(s1.getParentId()), offering.getSection(s2.getParentId()));
							if (cmp != 0) return cmp;
						}
						try {
							int cmp = Integer.valueOf(s1.getName(courseId()) == null ? "0" : s1.getName(courseId())).compareTo(Integer.valueOf(s2.getName(courseId()) == null ? "0" : s2.getName(courseId())));
							if (cmp != 0) return cmp;
						} catch (NumberFormatException e) {}
						int cmp = (s1.getName(courseId()) == null ? "" : s1.getName(courseId())).compareTo(s2.getName(courseId()) == null ? "" : s2.getName(courseId()));
						if (cmp != 0) return cmp;
				        return s1.getSectionId().compareTo(s2.getSectionId());
					}
					XSection x = s1;
					while (x != null) {
						if (isParent(offering.getSubpart(s2.getSubpartId()), offering.getSubpart(x.getSubpartId()))) {
							XSection s = offering.getSection(s2.getParentId());
							while (!s.getSubpartId().equals(x.getSubpartId())) {
								s = offering.getSection(s.getParentId());
							}
							int cmp = compare(x, s);
							return (cmp == 0 ? x.equals(s1) ? -1 : compare(offering.getSubpart(x.getSubpartId()), offering.getSubpart(s.getSubpartId())) : cmp);
						}
						x = offering.getSection(x.getParentId());
					}
					x = s2;
					while (x != null) {
						if (isParent(offering.getSubpart(s1.getSubpartId()), offering.getSubpart(x.getSubpartId()))) {
							XSection s = offering.getSection(s1.getParentId());
							while (!s.getSubpartId().equals(x.getSubpartId())) {
								s = offering.getSection(s.getParentId());
							}
							int cmp = compare(s, x);
							return (cmp == 0 ? x.equals(s2) ? 1 : compare(offering.getSubpart(s.getSubpartId()), offering.getSubpart(x.getSubpartId())) : cmp);
						}
						x = offering.getSection(x.getParentId());
					}
					int cmp = compare(offering.getSubpart(s1.getSubpartId()), offering.getSubpart(s2.getSubpartId()));
					if (cmp != 0) return cmp;
					try {
						cmp = Integer.valueOf(s1.getName(courseId()) == null ? "0" : s1.getName(courseId())).compareTo(Integer.valueOf(s2.getName(courseId()) == null ? "0" : s2.getName(courseId())));
						if (cmp != 0) return cmp;
					} catch (NumberFormatException e) {}
					cmp = (s1.getName(courseId()) == null ? "" : s1.getName(courseId())).compareTo(s2.getName(courseId()) == null ? "" : s2.getName(courseId()));
					if (cmp != 0) return cmp;
			        return s1.getSectionId().compareTo(s2.getSectionId());
				}
			});
			boolean checkOverrides = !query().hasAttribute("override");
			
			Map<Long, Set<Long>> section2students = new HashMap<Long, Set<Long>>();
			for (XCourseRequest request: enrollments.getRequests()) {
				if (request.getEnrollment() != null || !request.hasCourse(courseId())) continue;
				XStudent student = server.getStudent(request.getStudentId());
				if (student == null || !student.canAssign(request)) continue;
				if (checkOverrides && request.getEnrollment() == null) {
					XOverride override = request.getOverride(info);
					if (override != null && !override.isApproved()) continue;
				}
				Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
				CourseRequest r = SectioningRequest.convert(assignment, request, server);
				values: for (Enrollment en: r.values(assignment)) {
					for (Request x: r.getStudent().getRequests()) {
						Enrollment xe = assignment.getValue(x);
						if (!x.equals(r) && xe != null && xe.isOverlapping(en)) {
							continue values;
						}
					}
					for (Section s: en.getSections()) {
						Set<Long> students = (Set<Long>)section2students.get(s.getId());
						if (students == null) {
							students = new HashSet<Long>();
							section2students.put(s.getId(), students);
						}
						students.add(student.getStudentId());
					}
				}
			}
			
			for (XSection section: sections) {
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(info.getCourseId());
				e.setOfferingId(offering.getOfferingId());
				e.setSubject(info.getSubjectArea());
				e.setCourseNbr(info.getCourseNumber());
				e.setTitle(info.getTitle());
				e.setConsent(info.getConsentAbbv());
				
				XSubpart subpart = offering.getSubpart(section.getSubpartId());
				XConfig config = offering.getConfig(subpart.getConfigId());
				e.setConfig(config.getName());
				e.setConfigId(config.getConfigId());
				
				e.setSubpart(subpart.getName());
				e.setSubpartId(subpart.getSubpartId());
				e.setClazz(section.getName(courseId()));
				e.setClazzId(section.getSectionId());
				XSection parent = (section.getParentId() == null ? null : offering.getSection(section.getParentId()));
				while (parent != null) {
					e.incLevel();
					parent = (parent.getParentId() == null ? null : offering.getSection(parent.getParentId()));
				}
				
				int match = 0;
				int enrl = 0, wait = 0, res = 0, unasg = 0, unasgPrim = 0;
				int tEnrl = 0, tWait = 0, tRes = 0, tUnasg = 0, tUnasgPrim = 0;
				int conNeed = 0, tConNeed = 0;
				int other = 0;

				for (XCourseRequest request: enrollments.getRequests()) {
					XEnrollment enrollment = request.getEnrollment();
					if (enrollment == null || !enrollment.getSectionIds().contains(section.getSectionId())) continue;
					if (!request.getEnrollment().getCourseId().equals(courseId())) {other++; continue; }
					XStudent student = server.getStudent(request.getStudentId());
					if (student == null) continue;
					CourseRequestMatcher m = new CourseRequestMatcher(session, info, student, offering, request, isConsentToDoCourse, isMyStudent(student), lookup, server);
					if (query().match(m)) {
						match++;
						enrl ++;
						if (m.enrollment().getReservation() != null) res ++;
						if (info.getConsentLabel() != null && m.enrollment().getApproval() == null) conNeed ++;
					}
					
					tEnrl ++;
					if (m.enrollment().getReservation() != null) tRes ++;
					if (info.getConsentLabel() != null && m.enrollment().getApproval() == null) tConNeed ++;
				}

				Set<Long> students = (Set<Long>)section2students.get(section.getSectionId());
				if (students != null)
					for (XCourseRequest request: enrollments.getRequests()) {
						if (!students.contains(request.getStudentId())) continue;
						if (request.getEnrollment() != null || !request.hasCourse(courseId())) continue;
						XStudent student = server.getStudent(request.getStudentId());
						if (student == null || !student.canAssign(request)) continue;
						if (checkOverrides && request.getEnrollment() == null) {
							XOverride override = request.getOverride(info);
							if (override != null && !override.isApproved()) continue;
						}
						CourseRequestMatcher m = new CourseRequestMatcher(session, info, student, offering, request, isConsentToDoCourse, isMyStudent(student), lookup, server);
						
						if (query().match(m)) {
							match++;
							unasg++;
							if (!request.isAlternative() && request.isPrimary(info)) {
								unasgPrim ++;
								if (request.isWaitlist())
									wait++;
							}
						}
						tUnasg ++;
						if (!request.isAlternative() && request.isPrimary(info)) {
							tUnasgPrim ++;
							if (request.isWaitlist())
								tWait ++;
						}
					}
				
				if (match == 0) continue;
				
				e.setLimit(section.getLimit());
				e.setOther(other);
				e.setAvailable(section.isCancelled() || !section.isEnabledForScheduling() ? 0 : Math.max(0, offering.getUnreservedSectionSpace(section.getSectionId(), enrollments)));
				if (e.getAvailable() == Integer.MAX_VALUE) e.setAvailable(-1);
				e.setProjection(tEnrl + (int)Math.round(expectations.getExpectedSpace(section.getSectionId())));
				
				e.setEnrollment(enrl);
				e.setReservation(res);
				e.setWaitlist(wait);
				e.setUnassigned(unasg);
				e.setUnassignedPrimary(unasgPrim);
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);
				e.setTotalUnassigned(tUnasg);
				e.setTotalUnassignedPrimary(tUnasgPrim);

				e.setConsentNeeded(conNeed);
				e.setTotalConsentNeeded(tConNeed);

				ClassAssignment a = new ClassAssignment();
				a.setClassId(section.getSectionId());
				a.setSubpart(section.getSubpartName());
				a.setClassNumber(section.getName(-1l));
				a.setSection(section.getName(courseId()));
				a.setExternalId(section.getExternalId(courseId()));
				a.setCancelled(section.isCancelled());
				a.setLimit(new int[] {enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
				if (section.getTime() != null) {
					for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
						a.addDay(d.getIndex());
					a.setStart(section.getTime().getSlot());
					a.setLength(section.getTime().getLength());
					a.setBreakTime(section.getTime().getBreakTime());
					a.setDatePattern(section.getTime().getDatePatternName());
				}
				if (section.getNrRooms() > 0) {
					for (XRoom rm: section.getRooms()) {
						a.addRoom(rm.getUniqueId(), rm.getName());
					}
				}
				if (section.getInstructors() != null) {
					for (XInstructor instructor: section.getInstructors()) {
						a.addInstructor(instructor.getName());
						a.addInstructoEmail(instructor.getEmail());
					}
				}
				if (section.getParentId()!= null)
					a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
				a.setSubpartId(section.getSubpartId());
				a.addNote(course.getNote());
				a.addNote(section.getNote());
				a.setCredit(subpart.getCredit(course.getCourseId()));
				Float creditOverride = section.getCreditOverride(course.getCourseId());
				if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
				if (a.getParentSection() == null) {
					String consent = server.getCourse(courseId()).getConsentLabel();
					if (consent != null)
						a.setParentSection(consent);
				}
				a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
				e.setAssignment(a);
				
				ret.add(e);
			}
		}
		return ret;
	}
	
	@Override
	public String name() {
		return "find-enrollment-info";
	}

	public static class FindEnrollmentInfoCourseMatcher extends AbstractCourseMatcher {
		protected static final long serialVersionUID = 1L;
		protected Set<Long> iCoursesIcoordinate;
		protected Set<Long> iCoursesIcanApprove;
		protected Set<String> iSubjectAreas;
		protected Query iQuery;
		protected CourseLookup iLookup;
		
		public FindEnrollmentInfoCourseMatcher(Set<Long> coursesIcoordinate, Set<Long> coursesIcanApprove, Set<String> subjects, Query query, CourseLookup lookup) {
			iCoursesIcoordinate = coursesIcoordinate;
			iCoursesIcanApprove = coursesIcanApprove;
			iSubjectAreas = subjects;
			iQuery = query;
			iLookup = lookup;
		}
		

		public boolean isCourseVisible(Long courseId) {
			return iCoursesIcoordinate == null || iCoursesIcoordinate.contains(courseId);
		}
		

		public boolean isConsentToDoCourse(XCourse course) {
			return iCoursesIcanApprove != null && course.getConsentLabel() != null && iCoursesIcanApprove.contains(course.getCourseId());
		}
		
		public boolean hasMatchingSubjectArea(String subject) {
			return iSubjectAreas == null || iSubjectAreas.contains(subject);
		}
		
		@Override
		public boolean match(XCourseId id) {
			XCourse course = (id instanceof XCourse ? (XCourse) id : getServer().getCourse(id.getCourseId()));
			return course != null && isCourseVisible(course.getCourseId()) && hasMatchingSubjectArea(course.getSubjectArea()) && iQuery.match(new CourseInfoMatcher(course, isConsentToDoCourse(course), iLookup));
		}
		
	}
}
