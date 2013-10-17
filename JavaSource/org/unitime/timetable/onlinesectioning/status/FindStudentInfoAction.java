/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.match.AbstractStudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XAcademicAreaCode;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentInfoAction.FindEnrollmentInfoCourseMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseRequestMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;

/**
 * @author Tomas Muller
 */
public class FindStudentInfoAction implements OnlineSectioningAction<List<StudentInfo>> {
	private static final long serialVersionUID = 1L;
	private Query iQuery;
	private Integer iLimit = null;
	private Set<Long> iCoursesIcoordinate, iCoursesIcanApprove;
	
	public FindStudentInfoAction(String query, Set<Long> coursesIcoordinage, Set<Long> coursesIcanApprove) {
		iQuery = new Query(query);
		iCoursesIcanApprove = coursesIcanApprove;
		iCoursesIcoordinate = coursesIcoordinage;
		Matcher m = Pattern.compile("limit:[ ]?([0-9]*)", Pattern.CASE_INSENSITIVE).matcher(query);
		if (m.find()) {
			iLimit = Integer.parseInt(m.group(1));
		}
	}
	
	public Query query() { return iQuery; }
	
	public Integer limit() { return iLimit; }

	public boolean isConsentToDoCourse(XCourse course) {
		return iCoursesIcanApprove != null && course.getConsentLabel() != null && iCoursesIcanApprove.contains(course.getCourseId());
	}
	
	public boolean isCourseVisible(Long courseId) {
		return iCoursesIcoordinate == null || iCoursesIcoordinate.contains(courseId);
	}
	
	@Override
	public List<StudentInfo> execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		Map<Long, StudentInfo> students = new HashMap<Long, StudentInfo>();
		
		int gEnrl = 0, gWait = 0, gRes = 0;
		int gtEnrl = 0, gtWait = 0, gtRes = 0;
		int gConNeed = 0, gtConNeed = 0;
		for (XCourseId info: server.findCourses(new FindEnrollmentInfoCourseMatcher(iCoursesIcoordinate, iCoursesIcanApprove, iQuery))) {
			XOffering offering = server.getOffering(info.getOfferingId());
			if (offering == null) continue;
			XCourse course = offering.getCourse(info.getCourseId());
			XEnrollments enrollments = server.getEnrollments(info.getOfferingId());
			if (enrollments == null) continue;
			boolean isConsentToDoCourse = isConsentToDoCourse(course);
			
			for (XCourseRequest request: enrollments.getRequests()) {
				if (!request.hasCourse(info.getCourseId())) continue;
				if (request.getEnrollment() != null && !request.getEnrollment().getCourseId().equals(info.getCourseId())) continue;
				XStudent student = server.getStudent(request.getStudentId());
				if (student == null) continue;
				CourseRequestMatcher m = new CourseRequestMatcher(server, course, student, offering, request, isConsentToDoCourse);
				if (query().match(m)) {
					StudentInfo s = students.get(request.getStudentId());
					if (s == null) {
						s = new StudentInfo();
						students.put(request.getStudentId(), s);
						ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
						st.setId(request.getStudentId());
						st.setExternalId(student.getExternalId());
						st.setName(student.getName());
						for (XAcademicAreaCode ac: student.getAcademicAreaClasiffications()) {
							st.addArea(ac.getArea());
							st.addClassification(ac.getCode());
						}
						for (XAcademicAreaCode ac: student.getMajors()) {
							st.addMajor(ac.getCode());
						}
						for (String acc: student.getAccomodations()) {
							st.addAccommodation(acc);
						}
						for (String gr: student.getGroups()) {
							st.addGroup(gr);
						}
						int tEnrl = 0, tWait = 0, tRes = 0, tConNeed = 0, tReq = 0;
						for (XRequest r: student.getRequests()) {
							if (r instanceof XCourseRequest) {
								XCourseRequest cr = (XCourseRequest)r;
								if (!r.isAlternative()) tReq ++;
								if (cr.getEnrollment() == null) {
									if (student.canAssign(cr)) {
										tWait ++; gtWait ++;
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
								}
							}
						}
						s.setTotalEnrollment(tEnrl);
						s.setTotalReservation(tRes);
						s.setTotalWaitlist(tWait);
						s.setTotalConsentNeeded(tConNeed);
						s.setEnrollment(0);
						s.setReservation(0);
						s.setWaitlist(0);
						s.setConsentNeeded(0);
						s.setRequested(tReq);
						s.setStatus(student.getStatus() == null ? server.getAcademicSession().getDefaultSectioningStatus() : student.getStatus());
						s.setEmailDate(student.getEmailTimeStamp() == null ? null : student.getEmailTimeStamp());
					}
					if (m.enrollment() != null) {
						s.setEnrollment(s.getEnrollment() + 1); gEnrl ++;
						if (m.enrollment().getReservation() != null) { s.setReservation(s.getReservation() + 1); gRes ++; }
						if (course.getConsentLabel() != null && m.enrollment().getApproval() == null) {
							s.setConsentNeeded(s.getConsentNeeded() + 1); gConNeed ++;
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
					} else if (m.student().canAssign(m.request()) && m.request().isWaitlist()) {
						s.setWaitlist(s.getWaitlist() + 1); gWait ++;
						if (s.getTopWaitingPriority() == null)
							s.setTopWaitingPriority(1 + m.request().getPriority());
						else
							s.setTopWaitingPriority(Math.min(1 + m.request().getPriority(), s.getTopWaitingPriority()));
					}
					if (m.request().getTimeStamp() != null) {
						if (s.getRequestedDate() == null)
							s.setRequestedDate(m.request().getTimeStamp());
						else if (m.request().getTimeStamp().after(s.getRequestedDate()))
							s.setRequestedDate(m.request().getTimeStamp());
					}
				}
			}
		}
		
		List<StudentInfo> ret = new ArrayList<StudentInfo>(students.values());
		
		for (XStudentId id: server.findStudents(new FindStudentInfoMatcher(server, query()))) {
			XStudent student = (id instanceof XStudent ? (XStudent)id : server.getStudent(id.getStudentId()));
			StudentInfo s = new StudentInfo();
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
			st.setId(student.getStudentId());
			st.setExternalId(student.getExternalId());
			st.setName(student.getName());
			for (XAcademicAreaCode ac: student.getAcademicAreaClasiffications()) {
				st.addArea(ac.getArea());
				st.addClassification(ac.getCode());
			}
			for (XAcademicAreaCode ac: student.getMajors()) {
				st.addMajor(ac.getCode());
			}
			s.setStatus(student.getStatus() == null ? server.getAcademicSession().getDefaultSectioningStatus() : student.getStatus());
			s.setEmailDate(student.getEmailTimeStamp() == null ? null : student.getEmailTimeStamp());
			ret.add(s);
		}
		
		Collections.sort(ret, new Comparator<StudentInfo>() {
			@Override
			public int compare(StudentInfo s1, StudentInfo s2) {
				int cmp = s1.getStudent().getName().compareTo(s2.getStudent().getName());
				if (cmp != 0) return cmp;
				return new Long(s1.getStudent().getId()).compareTo(s2.getStudent().getId());
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
		
		t.setTotalEnrollment(gtEnrl);
		t.setTotalReservation(gtRes);
		t.setTotalWaitlist(gtWait);
		
		t.setConsentNeeded(gConNeed);
		t.setTotalConsentNeeded(gtConNeed);

		ret.add(t);				
		
		return ret;
	}

	@Override
	public String name() {
		return "find-student-infos";
	}
	
	public static class FindStudentInfoMatcher extends AbstractStudentMatcher {
		private static final long serialVersionUID = 1L;
		private Query iQuery;
		private String iDefaultSectioningStatus;
		
		public FindStudentInfoMatcher(OnlineSectioningServer server, Query query) {
			iQuery = query;
			iDefaultSectioningStatus = server.getAcademicSession().getDefaultSectioningStatus();
		}

		@Override
		public boolean match(XStudentId id) {
			XStudent student = (id instanceof XStudent ? (XStudent)id : getServer().getStudent(id.getStudentId()));
			return student != null && student.getRequests().isEmpty() && iQuery.match(new StudentMatcher(student, iDefaultSectioningStatus));
		}
	}
}