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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseInfoMatcher;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseRequestMatcher;

public class FindEnrollmentInfoAction implements OnlineSectioningAction<List<EnrollmentInfo>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Query iQuery;
	private Integer iLimit = null;
	private Long iCourseId;
	private Set<Long> iCoursesIcoordinate, iCoursesIcanApprove;
	
	public FindEnrollmentInfoAction(String query, Long courseId, Set<Long> coursesIcoordinage, Set<Long> coursesIcanApprove) {
		iQuery = new Query(query);
		iCourseId = courseId;
		iCoursesIcanApprove = coursesIcanApprove;
		iCoursesIcoordinate = coursesIcoordinage;
		Matcher m = Pattern.compile("limit:[ ]?([0-9]*)", Pattern.CASE_INSENSITIVE).matcher(query);
		if (m.find()) {
			iLimit = Integer.parseInt(m.group(1));
		}
	}
	
	public Query query() { return iQuery; }
	
	public Integer limit() { return iLimit; }
	
	public Long courseId() { return iCourseId; }
	
	public boolean isConsentToDoCourse(CourseInfo course) {
		return iCoursesIcanApprove != null && course.getConsent() != null && iCoursesIcanApprove.contains(course.getUniqueId());
	}
	
	public boolean isCourseVisible(Long courseId) {
		return iCoursesIcoordinate == null || iCoursesIcoordinate.contains(courseId);
	}
	
	@Override
	public List<EnrollmentInfo> execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		List<EnrollmentInfo> ret = new ArrayList<EnrollmentInfo>();
		if (courseId() == null) {
			Set<Long> students = new HashSet<Long>();
			Set<Long> matchingStudents = new HashSet<Long>();
			
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
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(info.getUniqueId());
				e.setOfferingId(course.getOffering().getId());
				e.setSubject(info.getSubjectArea());
				e.setCourseNbr(info.getCourseNbr());
				e.setTitle(info.getTitle());
				e.setConsent(info.getConsentAbbv());

				int match = 0;
				int enrl = 0, wait = 0, res = 0;
				int tEnrl = 0, tWait = 0, tRes = 0;
				int conNeed = 0, tConNeed = 0;
				
				Set<Long> addedStudents = new HashSet<Long>();
				for (CourseRequest request: course.getRequests()) {
					if (students.add(request.getStudent().getId()))
						addedStudents.add(request.getStudent().getId());
					if (request.getAssignment() != null && request.getAssignment().getCourse().getId() != course.getId()) { continue; }
					CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, request, isConsentToDoCourse);
					if (query().match(m)) {
						matchingStudents.add(request.getStudent().getId());
						match++;
						if (m.enrollment() != null) {
							enrl ++;
							if (m.enrollment().getReservation() != null) res ++;
							if (info.getConsent() != null && m.enrollment().getApproval() == null) conNeed ++;
						} else if (m.student().canAssign(m.request())) { // && m.request().isWaitlist()) {
							wait ++;
						}
					}
					
					if (m.enrollment() != null) {
						tEnrl ++;
						if (m.enrollment().getReservation() != null) tRes ++;
						if (info.getConsent() != null && m.enrollment().getApproval() == null) tConNeed ++;
					} else if (m.student().canAssign(m.request())) {// && m.request().isWaitlist()) {
						tWait ++;
					}
				}
				
				if (match == 0) {
					students.removeAll(addedStudents);
					continue;
				}
				
				gEnrl += enrl;
				gWait += wait;
				gRes += res;
				gConNeed += conNeed;
				
				gtEnrl += tEnrl;
				gtWait += tWait;
				gtRes += tRes;
				gtConNeed += tConNeed;
				
				int limit = 0;
				for (Config config: course.getOffering().getConfigs()) {
					if (config.getLimit() < 0) {
						limit = -1; break;
					} else {
						limit += config.getLimit();
					}
				}
									
				e.setLimit(course.getLimit());
				e.setProjection(course.getProjected());
				int av = (int)Math.floor(Math.max(0.0, course.getOffering().getUnreservedSpace(null)));
				if (course.getLimit() >= 0 && av > course.getLimit() - course.getEnrollments().size())
					av = course.getLimit() - course.getEnrollments().size();
				if (av == Integer.MAX_VALUE) av = -1;
				e.setAvailable(av);
				if (av >= 0) {
					int other = 0;
					for (Course c: course.getOffering().getCourses())
						if (!c.equals(course))
							other += c.getEnrollments().size();
					e.setOther(Math.min(course.getLimit() - course.getEnrollments().size() - av, other));
					int lim = 0;
					for (Config f: course.getOffering().getConfigs()) {
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
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);
				
				e.setConsentNeeded(conNeed);
				e.setTotalConsentNeeded(tConNeed);

				ret.add(e);
				if (limit() != null && ret.size() >= limit()) break;
			}
			
			// if students.size() > 0) {
			EnrollmentInfo t = new EnrollmentInfo();
			t.setSubject(MSG.total());
			t.setCourseNbr("");
			
			t.setLimit(students.size());
			t.setAvailable(matchingStudents.size());
			
			t.setEnrollment(gEnrl);
			t.setReservation(gRes);
			t.setWaitlist(gWait);
			
			t.setTotalEnrollment(gtEnrl);
			t.setTotalReservation(gtRes);
			t.setTotalWaitlist(gtWait);
			
			t.setConsentNeeded(gConNeed);
			t.setTotalConsentNeeded(gtConNeed);

			ret.add(t);				
		} else {
			CourseInfo info = server.getCourseInfo(courseId());
			Course course = server.getCourse(courseId());
			if (info == null || course == null) return ret;
			boolean isConsentToDoCourse = isConsentToDoCourse(info);
			
			List<Section> sections = server.getSections(info);
			Collections.sort(sections, new Comparator<Section>() {
				public int compare(Config c1, Config c2) {
					int cmp = c1.getName().compareToIgnoreCase(c2.getName());
					if (cmp != 0) return cmp;
					return Double.compare(c1.getId(), c2.getId());
				}
				public boolean isParent(Subpart s1, Subpart s2) {
					Subpart p1 = s1.getParent();
					if (p1==null) return false;
					if (p1.equals(s2)) return true;
					return isParent(p1, s2);
				}
				public int compare(Subpart s1, Subpart s2) {
					int cmp = compare(s1.getConfig(), s2.getConfig());
					if (cmp != 0) return cmp;
			        if (isParent(s1,s2)) return 1;
			        if (isParent(s2,s1)) return -1;
			        cmp = s1.getInstructionalType().compareTo(s2.getInstructionalType());
			        if (cmp != 0) return cmp;
			        return Double.compare(s1.getId(), s2.getId());
				}
				public int compare(Section s1, Section s2) {
					if (s1.getSubpart().equals(s2.getSubpart())) {
						if (s1.getParent() != null) {
							int cmp = compare(s1.getParent(), s2.getParent());
							if (cmp != 0) return cmp;
						}
						try {
							int cmp = Integer.valueOf(s1.getName(courseId()) == null ? "0" : s1.getName(courseId())).compareTo(Integer.valueOf(s2.getName(courseId()) == null ? "0" : s2.getName(courseId())));
							if (cmp != 0) return cmp;
						} catch (NumberFormatException e) {}
						int cmp = (s1.getName(courseId()) == null ? "" : s1.getName(courseId())).compareTo(s2.getName(courseId()) == null ? "" : s2.getName(courseId()));
						if (cmp != 0) return cmp;
				        return Double.compare(s1.getId(), s2.getId());
					}
					Section x = s1;
					while (x != null) {
						if (isParent(s2.getSubpart(), x.getSubpart())) {
							Section s = s2.getParent();
							while (!s.getSubpart().equals(x.getSubpart())) {
								s = s.getParent();
							}
							int cmp = compare(x, s);
							return (cmp == 0 ? x.equals(s1) ? -1 : compare(x.getSubpart(), s.getSubpart()) : cmp);
						}
						x = x.getParent();
					}
					x = s2;
					while (x != null) {
						if (isParent(s1.getSubpart(), x.getSubpart())) {
							Section s = s1.getParent();
							while (!s.getSubpart().equals(x.getSubpart())) {
								s = s.getParent();
							}
							int cmp = compare(s, x);
							return (cmp == 0 ? x.equals(s2) ? 1 : compare(s.getSubpart(), x.getSubpart()) : cmp);
						}
						x = x.getParent();
					}
					int cmp = compare(s1.getSubpart(), s2.getSubpart());
					if (cmp != 0) return cmp;
					try {
						cmp = Integer.valueOf(s1.getName(courseId()) == null ? "0" : s1.getName(courseId())).compareTo(Integer.valueOf(s2.getName(courseId()) == null ? "0" : s2.getName(courseId())));
						if (cmp != 0) return cmp;
					} catch (NumberFormatException e) {}
					cmp = (s1.getName(courseId()) == null ? "" : s1.getName(courseId())).compareTo(s2.getName(courseId()) == null ? "" : s2.getName(courseId()));
					if (cmp != 0) return cmp;
			        return Double.compare(s1.getId(), s2.getId());
				}
			});
			
			for (Section section: sections) {
						
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(info.getUniqueId());
				e.setOfferingId(course.getOffering().getId());
				e.setSubject(info.getSubjectArea());
				e.setCourseNbr(info.getCourseNbr());
				e.setTitle(info.getTitle());
				e.setConsent(info.getConsentAbbv());
				
				e.setConfig(section.getSubpart().getConfig().getName());
				e.setConfigId(section.getSubpart().getConfig().getId());
				
				e.setSubpart(section.getSubpart().getName());
				e.setSubpartId(section.getSubpart().getId());
				e.setClazz(section.getName(courseId()));
				e.setClazzId(section.getId());
				Section parent = section.getParent();
				while (parent != null) {
					e.incLevel();
					parent = parent.getParent();
				}
				
				int match = 0;
				int enrl = 0, wait = 0, res = 0;
				int tEnrl = 0, tWait = 0, tRes = 0;
				int conNeed = 0, tConNeed = 0;
				int other = 0;

				for (Enrollment enrollment: section.getEnrollments()) {
					if (enrollment.getCourse().getId() != course.getId()) {other++; continue; }
					CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, (CourseRequest)enrollment.getRequest(), isConsentToDoCourse);
					if (query().match(m)) {
						match++;
						enrl ++;
						if (m.enrollment().getReservation() != null) res ++;
						if (info.getConsent() != null && m.enrollment().getApproval() == null) conNeed ++;
					}
					
					tEnrl ++;
					if (m.enrollment().getReservation() != null) tRes ++;
					if (info.getConsent() != null && m.enrollment().getApproval() == null) tConNeed ++;
				}

				for (CourseRequest request: course.getRequests()) {
					if (request.getAssignment() != null || !request.getStudent().canAssign(request)) continue;
					CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, request, isConsentToDoCourse);
					boolean hasEnrollment = true;
					values: for (Enrollment en: request.values()) {
						if (!en.getSections().contains(section)) continue;
						for (Request x: request.getStudent().getRequests()) {
							if (!x.equals(request) && x.getAssignment() != null && x.getAssignment().isOverlapping(en)) {
								continue values;
							}
						}
						hasEnrollment = true; break;
					}
					if (!hasEnrollment) continue;
					if (query().match(m)) {
						match++;
						wait++;
					}
					tWait ++;
				}
				
				if (match == 0) continue;
				
				e.setLimit(section.getLimit() < 0 ? section.getLimit() : section.getLimit());
				e.setOther(other);
				e.setAvailable((int)Math.floor(Math.max(0.0, section.getUnreservedSpace(null))));
				if (e.getAvailable() == Integer.MAX_VALUE) e.setAvailable(-1);
				e.setProjection((int)Math.round(tEnrl + section.getSpaceExpected()));
				
				e.setEnrollment(enrl);
				e.setReservation(res);
				e.setWaitlist(wait);
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);

				e.setConsentNeeded(conNeed);
				e.setTotalConsentNeeded(tConNeed);

				ClassAssignment a = new ClassAssignment();
				a.setClassId(section.getId());
				a.setSubpart(section.getSubpart().getName());
				a.setClassNumber(section.getName(-1l));
				a.setSection(section.getName(course.getId()));
				a.setLimit(new int[] {section.getEnrollments().size(), section.getLimit()});
				if (section.getTime() != null) {
					for (DayCode d : DayCode.toDayCodes(section.getTime().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(section.getTime().getStartSlot());
					a.setLength(section.getTime().getLength());
					a.setBreakTime(section.getTime().getBreakTime());
					a.setDatePattern(section.getTime().getDatePatternName());
				}
				if (section.getRooms() != null) {
					for (Iterator<RoomLocation> i = section.getRooms().iterator(); i.hasNext(); ) {
						RoomLocation rm = i.next();
						a.addRoom(rm.getName());
					}
				}
				if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
					String[] instructors = section.getChoice().getInstructorNames().split(":");
					for (String instructor: instructors) {
						String[] nameEmail = instructor.split("\\|");
						a.addInstructor(nameEmail[0]);
						a.addInstructoEmailr(nameEmail.length < 2 ? "" : nameEmail[1]);
					}
				}
				if (section.getParent() != null)
					a.setParentSection(section.getParent().getName(course.getId()));
				a.setSubpartId(section.getSubpart().getId());
				a.addNote(course.getNote());
				a.addNote(section.getNote());
				a.setCredit(section.getSubpart().getCredit());
				if (a.getParentSection() == null) {
					String consent = server.getCourseInfo(course.getId()).getConsent();
					if (consent != null)
						a.setParentSection(consent);
				}
				a.setExpected(Math.round(section.getSpaceExpected()));
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

}
