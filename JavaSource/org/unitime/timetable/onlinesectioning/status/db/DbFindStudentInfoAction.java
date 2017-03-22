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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbCourseRequestMatcher;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbFindStudentInfoMatcher;

/**
 * @author Tomas Muller
 */
public class DbFindStudentInfoAction extends FindStudentInfoAction {
	private static final long serialVersionUID = 1L;

	@Override
	public List<StudentInfo> execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (iFilter == null) return super.execute(server, helper);
		
		Map<Long, StudentInfo> students = new HashMap<Long, StudentInfo>();
		
		int gEnrl = 0, gWait = 0, gRes = 0, gUnasg = 0;
		int gtEnrl = 0, gtWait = 0, gtRes = 0, gtUnasg = 0;
		int gConNeed = 0, gtConNeed = 0;
		Set<Long> unassigned = new HashSet<Long>();
		AcademicSessionInfo session = server.getAcademicSession();
		
		DbFindStudentInfoMatcher sm = new DbFindStudentInfoMatcher(session, iQuery, helper.getStudentNameFormat());
		
		Map<CourseOffering, List<CourseRequest>> requests = new HashMap<CourseOffering, List<CourseRequest>>();
		for (CourseRequest cr: (List<CourseRequest>)SectioningStatusFilterAction.getCourseQuery(iFilter, server).select("distinct cr").query(helper.getHibSession()).list()) {
			if (!query().match(new DbCourseRequestMatcher(session, cr, isConsentToDoCourse(cr.getCourseOffering()), helper.getStudentNameFormat()))) continue;
			List<CourseRequest> list = requests.get(cr.getCourseOffering());
			if (list == null) {
				list = new ArrayList<CourseRequest>();
				requests.put(cr.getCourseOffering(), list);
			}
			list.add(cr);
		}
		
		for (Map.Entry<CourseOffering, List<CourseRequest>> entry: requests.entrySet()) {
			CourseOffering course = entry.getKey();
			boolean isConsentToDoCourse = isConsentToDoCourse(course);
			
			for (CourseRequest request: entry.getValue()) {
				Student student = request.getCourseDemand().getStudent();
				StudentInfo s = students.get(student.getUniqueId());
				if (s == null) {
					s = new StudentInfo();
					students.put(student.getUniqueId(), s);
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
					st.setId(student.getUniqueId());
					st.setSessionId(session.getUniqueId());
					st.setExternalId(student.getExternalUniqueId());
					st.setCanShowExternalId(iCanShowExtIds);
					st.setCanRegister(iCanRegister);
					st.setCanUseAssistant(iCanUseAssistant);
					st.setName(helper.getStudentNameFormat().format(student));
					for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
						st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation());
						st.addClassification(acm.getAcademicClassification().getCode());
						st.addMajor(acm.getMajor().getCode());
					}
					for (StudentAccomodation acc: student.getAccomodations()) {
						st.addAccommodation(acc.getAbbreviation());
					}
					for (StudentGroup gr: student.getGroups()) {
						st.addGroup(gr.getGroupAbbreviation());
					}
					int tEnrl = 0, tWait = 0, tRes = 0, tConNeed = 0, tReq = 0, tUnasg = 0;
					float tCred = 0f;
					for (CourseDemand demand: student.getCourseDemands()) {
						if (!demand.getCourseRequests().isEmpty()) {
							if (!demand.isAlternative()) tReq ++;
							List<StudentClassEnrollment> enrollment = null;
							CourseRequest assigned = null;
							for (CourseRequest r: demand.getCourseRequests()) {
								enrollment = r.getClassEnrollments();
								if (!enrollment.isEmpty()) {
									assigned = r; break;
								}
							}
							if (enrollment.isEmpty()) {
								CourseRequest first = null;
								for (CourseRequest r: demand.getCourseRequests()) {
									if (first == null || r.getOrder() < first.getOrder()) first = r;
								}
								DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, first, isConsentToDoCourse(first.getCourseOffering()), helper.getStudentNameFormat());
								if (crm.canAssign()) {
									tUnasg ++; gtUnasg ++;
									if (demand.isWaitlist()) {
										tWait ++; gtWait ++;
									}
								}
							} else {
								tEnrl ++; gtEnrl ++;
								DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, assigned, isConsentToDoCourse(assigned.getCourseOffering()), helper.getStudentNameFormat());
								if (crm.reservation() != null) {
									tRes ++; gtRes ++;
								}
								if (assigned.getCourseOffering().getConsentType() != null && crm.approval() == null) {
									tConNeed ++; gtConNeed ++;
								}
								if (assigned.getCourseOffering().getCredit() != null) {
									tCred += guessCredit(assigned.getCourseOffering().getCredit().creditAbbv());
								} else {
									for (StudentClassEnrollment e: enrollment) {
										if (e.getClazz().getSchedulingSubpart().getCredit() != null)
											tCred += guessCredit(e.getClazz().getSchedulingSubpart().getCredit().creditAbbv());
									}
								}
							}
						}
					}
						
					s.setTotalEnrollment(tEnrl);
					s.setTotalReservation(tRes);
					s.setTotalWaitlist(tWait);
					s.setTotalUnassigned(tUnasg);
					s.setTotalConsentNeeded(tConNeed);
					s.setEnrollment(0);
					s.setReservation(0);
					s.setWaitlist(0);
					s.setUnassigned(0);
					s.setConsentNeeded(0);
					s.setRequested(tReq);
					s.setStatus(student.getSectioningStatus() == null ? session.getDefaultSectioningStatus() : student.getSectioningStatus().getReference());
					s.setEmailDate(student.getScheduleEmailedDate() == null ? null : student.getScheduleEmailedDate());
					s.setCredit(0f);
					s.setTotalCredit(tCred);
					
					StudentNote note = null;
					for (StudentNote n: student.getNotes())
						if (note == null || note.compareTo(n) > 0) note = n;
					if (note != null) s.setNote(note.getTextNote());
				}
				DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, helper.getStudentNameFormat());
				if (!crm.enrollment().isEmpty()) {
					s.setEnrollment(s.getEnrollment() + 1); gEnrl ++;
					if (crm.reservation() != null) { s.setReservation(s.getReservation() + 1); gRes ++; }
					if (course.getConsentType() != null && crm.approval() == null) {
						s.setConsentNeeded(s.getConsentNeeded() + 1); gConNeed ++;
					}
					for (StudentClassEnrollment e: crm.enrollment()) {
						if (e.getTimestamp() != null) {
							if (s.getEnrolledDate() == null)
								s.setEnrolledDate(e.getTimestamp());
							else if (e.getTimestamp().after(s.getEnrolledDate()))
								s.setEnrolledDate(e.getTimestamp());
						}
					}
					if (crm.approval() != null) {
						for (StudentClassEnrollment e: crm.enrollment()) {
							if (e.getApprovedDate() != null) {
								if (s.getApprovedDate() == null)
									s.setApprovedDate(e.getApprovedDate());
								else if (e.getApprovedDate().after(s.getApprovedDate()))
									s.setApprovedDate(e.getApprovedDate());
							}
						}
					}
					if (course.getCredit() != null) {
						s.setCredit(s.getCredit() + guessCredit(course.getCredit().creditAbbv()));
					} else {
						for (StudentClassEnrollment e: crm.enrollment()) {
							if (e.getClazz().getSchedulingSubpart().getCredit() != null)
								s.setCredit(s.getCredit() + guessCredit(e.getClazz().getSchedulingSubpart().getCredit().creditAbbv()));
						}
					}
				} else if (crm.canAssign() && unassigned.add(crm.request().getUniqueId())) {
					if (crm.request().getCourseDemand().isWaitlist()) {
						s.setWaitlist(s.getWaitlist() + 1); gWait ++;
						if (s.getTopWaitingPriority() == null)
							s.setTopWaitingPriority(1 + crm.request().getCourseDemand().getPriority());
						else
							s.setTopWaitingPriority(Math.min(1 + crm.request().getCourseDemand().getPriority(), s.getTopWaitingPriority()));
					}
					s.setUnassigned(s.getUnassigned() + 1); gUnasg ++;
				}
				if (crm.request().getCourseDemand().getTimestamp() != null) {
					if (s.getRequestedDate() == null)
						s.setRequestedDate(crm.request().getCourseDemand().getTimestamp());
					else if (crm.request().getCourseDemand().getTimestamp().after(s.getRequestedDate()))
						s.setRequestedDate(crm.request().getCourseDemand().getTimestamp());
				}
			}
		}
		
		List<StudentInfo> ret = new ArrayList<StudentInfo>(students.values());
		
		for (Student student: (List<Student>)SectioningStatusFilterAction.getQuery(iFilter, server).select("distinct s").query(helper.getHibSession()).list()) {
			if (students.containsKey(student.getUniqueId())) continue;
			if (!sm.match(student)) continue;
			StudentInfo s = new StudentInfo();
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
			st.setId(student.getUniqueId());
			st.setSessionId(session.getUniqueId());
			st.setExternalId(student.getExternalUniqueId());
			st.setCanShowExternalId(iCanShowExtIds);
			st.setCanRegister(iCanRegister);
			st.setCanUseAssistant(iCanUseAssistant);
			st.setName(helper.getStudentNameFormat().format(student));
			for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
				st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation());
				st.addClassification(acm.getAcademicClassification().getCode());
				st.addMajor(acm.getMajor().getCode());
			}
			for (StudentAccomodation acc: student.getAccomodations()) {
				st.addAccommodation(acc.getAbbreviation());
			}
			for (StudentGroup gr: student.getGroups()) {
				st.addGroup(gr.getGroupAbbreviation());
			}
			s.setStatus(student.getSectioningStatus() == null ? session.getDefaultSectioningStatus() : student.getSectioningStatus().getReference());
			s.setEmailDate(student.getScheduleEmailedDate() == null ? null : student.getScheduleEmailedDate());
			
			StudentNote note = null;
			for (StudentNote n: student.getNotes())
				if (note == null || note.compareTo(n) > 0) note = n;
			if (note != null) s.setNote(note.getTextNote());
			
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
		t.setUnassigned(gUnasg);
		
		t.setTotalEnrollment(gtEnrl);
		t.setTotalReservation(gtRes);
		t.setTotalWaitlist(gtWait);
		t.setTotalUnassigned(gtUnasg);
		
		t.setConsentNeeded(gConNeed);
		t.setTotalConsentNeeded(gtConNeed);

		ret.add(t);				
		
		return ret;
	}
	
	public boolean isConsentToDoCourse(CourseOffering course) {
		return iCoursesIcanApprove != null && course.getConsentType() != null && iCoursesIcanApprove.contains(course.getUniqueId());
	}
}
