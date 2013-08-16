/*
 * UniTime 3.3 (University Timetabling Application)
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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.cpsolver.studentsct.model.AcademicAreaCode;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseInfoMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseRequestMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;

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

	public boolean isConsentToDoCourse(CourseInfo course) {
		return iCoursesIcanApprove != null && course.getConsent() != null && iCoursesIcanApprove.contains(course.getUniqueId());
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
		
		for (CourseInfo info: server.findCourses(new OnlineSectioningServer.CourseInfoMatcher() {
			@Override
			public boolean match(CourseInfo course) {
				return isCourseVisible(course.getUniqueId()) && query().match(
						new CourseInfoMatcher(helper, course, isConsentToDoCourse(course)));
			}
		})) {
			Course course = server.getCourse(info.getUniqueId());
			if (course == null) continue;
			boolean isConsentToDoCourse = isConsentToDoCourse(info);
			
			for (CourseRequest request: course.getRequests()) {
				if (request.getAssignment() != null && request.getAssignment().getCourse().getId() != course.getId()) { continue; }
				CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, request, isConsentToDoCourse);
				if (query().match(m)) {
					StudentInfo s = students.get(request.getStudent().getId());
					if (s == null) {
						s = new StudentInfo();
						students.put(request.getStudent().getId(), s);
						ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
						st.setId(request.getStudent().getId());
						st.setExternalId(request.getStudent().getExternalId());
						st.setName(request.getStudent().getName());
						for (AcademicAreaCode ac: request.getStudent().getAcademicAreaClasiffications()) {
							st.addArea(ac.getArea());
							st.addClassification(ac.getCode());
						}
						for (AcademicAreaCode ac: request.getStudent().getMajors()) {
							st.addMajor(ac.getCode());
						}
						for (AcademicAreaCode ac: request.getStudent().getMinors()) {
							if ("A".equals(ac.getArea()))
								st.addAccommodation(ac.getCode());
							else
								st.addGroup(ac.getCode());
						}
						int tEnrl = 0, tWait = 0, tRes = 0, tConNeed = 0, tReq = 0;
						for (Request r: request.getStudent().getRequests()) {
							if (r instanceof CourseRequest) {
								if (!r.isAlternative()) tReq ++;
								if (r.getAssignment() == null) {
									if (request.getStudent().canAssign(r)) {
										tWait ++; gtWait ++;
									}
								} else {
									tEnrl ++; gtEnrl ++;
									if (r.getAssignment().getReservation() != null) {
										tRes ++; gtRes ++;
									}
									if (r.getAssignment().getApproval() == null) {
										CourseInfo i = server.getCourseInfo(r.getAssignment().getCourse().getId());
										if (i != null && i.getConsent() != null) {
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
						s.setStatus(request.getStudent().getStatus() == null ? server.getAcademicSession().getDefaultSectioningStatus() : request.getStudent().getStatus());
						s.setEmailDate(request.getStudent().getEmailTimeStamp() == null ? null : new Date(request.getStudent().getEmailTimeStamp()));
					}
					if (m.enrollment() != null) {
						s.setEnrollment(s.getEnrollment() + 1); gEnrl ++;
						if (m.enrollment().getReservation() != null) { s.setReservation(s.getReservation() + 1); gRes ++; }
						if (info.getConsent() != null && m.enrollment().getApproval() == null) {
							s.setConsentNeeded(s.getConsentNeeded() + 1); gConNeed ++;
						}
						if (m.enrollment().getTimeStamp() != null) {
							if (s.getEnrolledDate() == null)
								s.setEnrolledDate(new Date(m.enrollment().getTimeStamp()));
							else
								s.setEnrolledDate(new Date(Math.max(m.enrollment().getTimeStamp(), s.getEnrolledDate().getTime())));
						}
						if (m.enrollment().getApproval() != null) {
							long ts = Long.parseLong(m.enrollment().getApproval().split(":")[0]);
							if (s.getApprovedDate() == null)
								s.setApprovedDate(new Date(ts));
							else
								s.setApprovedDate(new Date(Math.max(ts, s.getApprovedDate().getTime())));
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
							s.setRequestedDate(new Date(m.request().getTimeStamp()));
						else
							s.setRequestedDate(new Date(Math.max(m.request().getTimeStamp(), s.getRequestedDate().getTime())));
					}
				}
			}
		}
		
		List<StudentInfo> ret = new ArrayList<StudentInfo>(students.values());
		
		for (Student student: server.findStudents(new OnlineSectioningServer.StudentMatcher() {
			@Override
			public boolean match(Student student) {
				return student.getRequests().isEmpty() && query().match(new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus()));
			}
		})) {
			StudentInfo s = new StudentInfo();
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
			st.setId(student.getId());
			st.setExternalId(student.getExternalId());
			st.setName(student.getName());
			for (AcademicAreaCode ac: student.getAcademicAreaClasiffications()) {
				st.addArea(ac.getArea());
				st.addClassification(ac.getCode());
			}
			for (AcademicAreaCode ac: student.getMajors()) {
				st.addMajor(ac.getCode());
			}
			s.setStatus(student.getStatus() == null ? server.getAcademicSession().getDefaultSectioningStatus() : student.getStatus());
			s.setEmailDate(student.getEmailTimeStamp() == null ? null : new Date(student.getEmailTimeStamp()));
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

}