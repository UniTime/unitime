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
package org.unitime.timetable.onlinesectioning.solver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.AcademicAreaCode;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

public class FindEnrollmentInfoAction implements OnlineSectioningAction<List<EnrollmentInfo>> {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private Query iQuery;
	private Long iCourseId;
	
	public FindEnrollmentInfoAction(String query, Long courseId) {
		iQuery = new Query(query);
		iCourseId = courseId;
	}
	
	public Query query() { return iQuery; }
	
	public Long courseId() { return iCourseId; }
	
	@Override
	public List<EnrollmentInfo> execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		List<EnrollmentInfo> ret = new ArrayList<EnrollmentInfo>();
		if (courseId() == null) {
			Set<Long> students = new HashSet<Long>();
			Set<Long> matchingStudents = new HashSet<Long>();
			
			int gEnrl = 0, gWait = 0, gRes = 0;
			int gtEnrl = 0, gtWait = 0, gtRes = 0;
			int gConNeed = 0, gConAppr = 0, gtConNeed = 0, gtConAppr = 0;
			
			for (CourseInfo info: server.findCourses(new OnlineSectioningServer.CourseInfoMatcher() {
				@Override
				public boolean match(CourseInfo course) {
					return query().match(new CourseInfoMatcher(helper, course));
				}
			})) {
				Course course = server.getCourse(info.getUniqueId());
				if (course == null) continue;
				EnrollmentInfo e = new EnrollmentInfo();
				e.setCourseId(info.getUniqueId());
				e.setOfferingId(course.getOffering().getId());
				e.setSubject(info.getSubjectArea());
				e.setCourseNbr(info.getCourseNbr());
				e.setTitle(info.getTitle());
				e.setConsent(info.getConsentAbbv());

				int match = 0, nomatch = 0;
				int enrl = 0, wait = 0, res = 0;
				int tEnrl = 0, tWait = 0, tRes = 0;
				int conNeed = 0, conAppr = 0, tConNeed = 0, tConAppr = 0;
				
				Set<Long> addedStudents = new HashSet<Long>();
				for (CourseRequest request: course.getRequests()) {
					if (students.add(request.getStudent().getId()))
						addedStudents.add(request.getStudent().getId());
					if (request.getAssignment() != null && request.getAssignment().getCourse().getId() != course.getId()) continue;
					CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, request);
					if (query().match(m)) {
						matchingStudents.add(request.getStudent().getId());
						match++;
						if (m.enrollment() != null) {
							enrl ++;
							if (m.enrollment().getReservation() != null) res ++;
							if (info.getConsent() != null && m.enrollment().getApproval() != null) conAppr ++;
							else if (info.getConsent() != null) conNeed ++;
						} else if (m.student().canAssign(m.request())) {
							wait ++;
						}
					} else {
						nomatch++;
					}
					
					if (m.enrollment() != null) {
						tEnrl ++;
						if (m.enrollment().getReservation() != null) tRes ++;
						if (info.getConsent() != null && m.enrollment().getApproval() != null) tConAppr ++;
						else if (info.getConsent() != null) tConNeed ++;
					} else if (m.student().canAssign(m.request())) {
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
				gConAppr += conAppr;
				gConNeed += conNeed;
				
				gtEnrl += tEnrl;
				gtWait += tWait;
				gtRes += tRes;
				gtConAppr += tConAppr;
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
				e.setAvailable(Math.min((int)Math.floor(Math.max(0.0, course.getOffering().getUnreservedSpace(null))), course.getLimit() - course.getEnrollments().size()));
				if (e.getAvailable() == Integer.MAX_VALUE) e.setAvailable(-1);
				
				e.setEnrollment(enrl);
				e.setReservation(res);
				e.setWaitlist(wait);
				
				e.setTotalEnrollment(tEnrl);
				e.setTotalReservation(tRes);
				e.setTotalWaitlist(tWait);
				
				e.setConsentNeeded(conNeed);
				e.setConsentApproved(conAppr);
				e.setTotalConsentNeeded(tConNeed);
				e.setTotalConsentApproved(tConAppr);

				ret.add(e);
			}
			
			if (students.size() > 0) {
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
				t.setConsentApproved(gConAppr);
				t.setTotalConsentNeeded(gtConNeed);
				t.setTotalConsentApproved(gtConAppr);

				ret.add(t);				
			}
		} else {
			CourseInfo info = server.getCourseInfo(courseId());
			Course course = server.getCourse(courseId());
			if (course == null) return ret;
			
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
					e.setSubpart("&nbsp;&nbsp;" + e.getSubpart());
					e.setClazz("&nbsp;&nbsp;" + e.getClazz());
					parent = parent.getParent();
				}
				
				int match = 0, nomatch = 0;
				int enrl = 0, wait = 0, res = 0;
				int tEnrl = 0, tWait = 0, tRes = 0;
				int conNeed = 0, conAppr = 0, tConNeed = 0, tConAppr = 0;
				int other = 0;

				for (Enrollment enrollment: section.getEnrollments()) {
					if (enrollment.getCourse().getId() != course.getId()) {other++; continue; }
					CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, (CourseRequest)enrollment.getRequest());
					if (query().match(m)) {
						match++;
						enrl ++;
						if (m.enrollment().getReservation() != null) res ++;
						if (info.getConsent() != null && m.enrollment().getApproval() != null) conAppr ++;
						else if (info.getConsent() != null) conNeed ++;
					} else {
						nomatch++;
					}
					
					tEnrl ++;
					if (m.enrollment().getReservation() != null) tRes ++;
					if (info.getConsent() != null && m.enrollment().getApproval() != null) tConAppr ++;
					else if (info.getConsent() != null) tConNeed ++;
				}

				for (CourseRequest request: course.getRequests()) {
					if (request.getAssignment() != null || !request.getStudent().canAssign(request)) continue;
					CourseRequestMatcher m = new CourseRequestMatcher(helper, server, info, request);
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
					} else {
						nomatch++;
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
				e.setConsentApproved(conAppr);
				e.setTotalConsentNeeded(tConNeed);
				e.setTotalConsentApproved(tConAppr);

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
				a.addNote(section.getNote());
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
		return "find-enrollment-infos";
	}
	
	public static class CourseInfoMatcher implements TermMatcher {
		private CourseInfo iInfo;
		private OnlineSectioningHelper iHelper;
		
		public CourseInfoMatcher(OnlineSectioningHelper helper, CourseInfo course) {
			iHelper = helper;
			iInfo = course;
		}
		
		public OnlineSectioningHelper helper() { return iHelper; }

		public CourseInfo info() { return iInfo; }
		
		@Override
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || "name".equals(attr) || "course".equals(attr)) {
				return info().getSubjectArea().equalsIgnoreCase(term) || info().getCourseNbr().equalsIgnoreCase(term) || (info().getSubjectArea() + " " + info().getCourseNbr()).equalsIgnoreCase(term);
			}
			if ((attr == null && term.length() > 2) || "title".equals(attr)) {
				return info().getTitle().toLowerCase().contains(term.toLowerCase());
			}
			if (attr == null || "subject".equals(attr)) {
				return info().getSubjectArea().equalsIgnoreCase(term);
			}
			if (attr == null || "number".equals(attr)) {
				return info().getCourseNbr().equalsIgnoreCase(term);
			}
			if ("department".equals(attr)) {
				return info().getDepartment().equalsIgnoreCase(term);
				
			}
			if ("consent".equals(attr)) {
				if ("none".equalsIgnoreCase(term))
					return info().getConsent() == null;
				else
					return info().getConsent() != null;
			}
			return attr != null; // pass unknown attributes lower
		}
	}
	
	public static class CourseRequestMatcher extends CourseInfoMatcher {
		private CourseRequest iRequest;
		private Date iFirstDate;
		
		public CourseRequestMatcher(OnlineSectioningHelper helper, OnlineSectioningServer server, CourseInfo info, CourseRequest request) {
			super(helper, info);
			iFirstDate = server.getAcademicSession().getDatePatternFirstDate();
			iRequest = request;
		}
		
		public CourseRequest request() { return iRequest; }
		public Enrollment enrollment() { return iRequest.getAssignment(); }
		public Student student() { return iRequest.getStudent(); }
		public Course course() {
			if (enrollment() != null) return enrollment().getCourse();
			for (Course course: request().getCourses())
				if (course.getId() == info().getUniqueId()) return course;
			return request().getCourses().get(0);
		}

		@Override
		public boolean match(String attr, String term) {
			if (attr == null || "name".equals(attr) || "title".equals(attr) || "subject".equals(attr) || "number".equals(attr) || "course".equals(attr) || "department".equals(attr))
				return super.match(attr, term);
			
			if ("area".equals(attr)) {
				for (AcademicAreaCode ac: student().getAcademicAreaClasiffications())
					if (eq(ac.getArea(), term)) return true;
			}
			
			if ("clasf".equals(attr) || "classification".equals(attr)) {
				for (AcademicAreaCode ac: student().getAcademicAreaClasiffications())
					if (eq(ac.getCode(), term)) return true;
			}
			
			if ("major".equals(attr)) {
				for (AcademicAreaCode ac: student().getMajors())
					if (eq(ac.getCode(), term)) return true;
			}
			
			if ("group".equals(attr)) {
				for (AcademicAreaCode ac: student().getMinors())
					if (eq(ac.getCode(), term)) return true;
			}

			
			if ("student".equals(attr)) {
				return has(student().getName(), term) || eq(student().getExternalId(), term) || eq(student().getName(), term);
			}
			
			if ("assigned".equals(attr) || "scheduled".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return enrollment() != null;
				else
					return enrollment() == null;
			}
			
			if ("waitlisted".equals(attr) || "waitlist".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return enrollment() == null;
				else
					return enrollment() != null;
			}
			
			if ("reservation".equals(attr) || "reserved".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return enrollment() != null && enrollment().getReservation() != null;
				else
					return enrollment() != null && enrollment().getReservation() == null;
			}
			
			if ("consent".equals(attr)) {
				if (eq("none", term)) {
					return info().getConsent() == null;
				} else if (eq("required", term)) {
					return info().getConsent() != null;
				} else if (eq("approved", term)) {
					return info().getConsent() != null && enrollment() != null && enrollment().getApproval() != null;
				} else if (eq("waiting", term)) {
					return info().getConsent() != null && enrollment() != null && enrollment().getApproval() == null;
				} else {
					return info().getConsent() != null && ((enrollment() != null && enrollment().getApproval() != null && (has(enrollment().getApproval().split(":")[2], term) || eq(enrollment().getApproval().split(":")[1], term))) || eq(info().getConsentAbbv(), term));
				}
			}
			
			if (enrollment() != null) {
				
				for (Section section: enrollment().getSections()) {
					if (attr == null || attr.equals("crn") || attr.equals("id") || attr.equals("externalId") || attr.equals("exid") || attr.equals("name")) {
						if (section.getName(info().getUniqueId()) != null && section.getName(info().getUniqueId()).toLowerCase().startsWith(term.toLowerCase()))
							return true;
					}
					if (attr == null || attr.equals("day")) {
						if (section.getTime() == null && term.equalsIgnoreCase("none")) return true;
						if (section.getTime() != null) {
							int day = parseDay(term);
							if (day > 0 && (section.getTime().getDayCode() & day) == day) return true;
						}
					}
					if (attr == null || attr.equals("time")) {
						if (section.getTime() == null && term.equalsIgnoreCase("none")) return true;
						if (section.getTime() != null) {
							int start = parseStart(term);
							if (start >= 0 && section.getTime().getStartSlot() == start) return true;
						}
					}
					if (attr != null && attr.equals("before")) {
						if (section.getTime() != null) {
							int end = parseStart(term);
							if (end >= 0 && section.getTime().getStartSlot() + section.getTime().getLength() - section.getTime().getBreakTime() / 5 <= end) return true;
						}
					}
					if (attr != null && attr.equals("after")) {
						if (section.getTime() != null) {
							int start = parseStart(term);
							if (start >= 0 && section.getTime().getStartSlot() >= start) return true;
						}
					}
					if (attr == null || attr.equals("date")) {
						if (section.getTime() == null && term.equalsIgnoreCase("none")) return true;
						if (section.getTime() != null && !section.getTime().getWeekCode().isEmpty()) {
							SimpleDateFormat df = new SimpleDateFormat(CONSTANTS.patternDateFormat());
					    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
					    	cal.setTime(iFirstDate);
					    	for (int i = 0; i < section.getTime().getWeekCode().size(); i++) {
					    		if (section.getTime().getWeekCode().get(i)) {
					    			DayCode day = null;
					    			switch (cal.get(Calendar.DAY_OF_WEEK)) {
					    			case Calendar.MONDAY:
					    				day = DayCode.MON; break;
					    			case Calendar.TUESDAY:
					    				day = DayCode.TUE; break;
					    			case Calendar.WEDNESDAY:
					    				day = DayCode.WED; break;
					    			case Calendar.THURSDAY:
					    				day = DayCode.THU; break;
					    			case Calendar.FRIDAY:
					    				day = DayCode.FRI; break;
					    			case Calendar.SATURDAY:
					    				day = DayCode.SAT; break;
					    			case Calendar.SUNDAY:
					    				day = DayCode.SUN; break;
					    			}
					    			if ((section.getTime().getDayCode() & day.getCode()) == day.getCode()) {
						    			int d = cal.get(Calendar.DAY_OF_MONTH);
						    			int m = cal.get(Calendar.MONTH) + 1;
						    			if (df.format(cal.getTime()).equalsIgnoreCase(term) || eq(d + "." + m + ".",term) || eq(m + "/" + d, term)) return true;
					    			}
					    		}
					    		cal.add(Calendar.DAY_OF_YEAR, 1);
					    	}
						}
					}
					if (attr == null || attr.equals("room")) {
						if ((section.getRooms() == null || section.getRooms().isEmpty()) && term.equalsIgnoreCase("none")) return true;
						if (section.getRooms() != null) {
							for (RoomLocation r: section.getRooms()) {
								if (has(r.getName(), term)) return true;
							}
						}
					}
					if (attr == null || attr.equals("instr") || attr.equals("instructor")) {
						if (attr != null && (section.getChoice().getInstructorNames() == null || section.getChoice().getInstructorNames().isEmpty()) && term.equalsIgnoreCase("none")) return true;
						for (String instructor: section.getChoice().getInstructorNames().split(":")) {
							String[] nameEmail = instructor.split("\\|");
							if (has(nameEmail[0], term)) return true;
							if (nameEmail.length == 2) {
								String email = nameEmail[1];
								if (email.indexOf('@') >= 0) email = email.substring(0, email.indexOf('@'));
								if (eq(email, term)) return true;
							}
						}
					}
					if (attr != null && section.getTime() != null) {
						int start = parseStart(attr + ":" + term);
						if (start >= 0 && section.getTime().getStartSlot() == start) return true;
					}					
				}
			}
			
			return false;
		}

		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			if (eq(name, term)) return true;
			for (String t: name.split(" |,"))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
		
		private int parseDay(String token) {
			int days = 0;
			boolean found = false;
			do {
				found = false;
				for (int i=0; i<CONSTANTS.longDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.longDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.longDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.days()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].substring(0,2).toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(2);
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.shortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.shortDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.shortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.freeTimeShortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.freeTimeShortDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.freeTimeShortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
			} while (found);
			return (token.isEmpty() ? days : 0);
		}
		
		private int parseStart(String token) {
			int startHour = 0, startMin = 0;
			String number = "";
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) return -1;
			if (number.length() > 2) {
				startHour = Integer.parseInt(number) / 100;
				startMin = Integer.parseInt(number) % 100;
			} else {
				startHour = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith(":")) {
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (number.isEmpty()) return -1;
				startMin = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			boolean hasAmOrPm = false;
			if (token.toLowerCase().startsWith("am")) { token = token.substring(2); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("a")) { token = token.substring(1); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (startHour < 7 && !hasAmOrPm) startHour += 12;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			return (60 * startHour + startMin) / 5;
		}
	}

}
