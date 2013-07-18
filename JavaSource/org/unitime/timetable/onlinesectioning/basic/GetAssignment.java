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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;

import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerImpl.EnrollmentSectionComparator;
import org.unitime.timetable.util.Formats;

public class GetAssignment implements OnlineSectioningAction<ClassAssignmentInterface>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	private Long iStudentId;
	
	public GetAssignment(Long studentId) {
		iStudentId = studentId;
	}

	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
			Student student = server.getStudent(iStudentId);
			if (student == null) return null;
	        ClassAssignmentInterface ret = new ClassAssignmentInterface();
			int nrUnassignedCourses = 0;
			for (Request request: student.getRequests()) {
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				if (request instanceof CourseRequest) {
					CourseRequest r = (CourseRequest)request;
					Course course = (request.getAssignment() == null ? r.getCourses().get(0) : r.getAssignment().getCourse());
					if (server.isOfferingLocked(course.getOffering().getId()))
						ca.setLocked(true);
					ca.setAssigned(r.getAssignment() != null);
					ca.setCourseId(course.getId());
					ca.setSubject(course.getSubjectArea());
					ca.setWaitListed(r.isWaitlist());
					ca.setCourseNbr(course.getCourseNumber());
					if (r.getAssignment() == null) {
						TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
							public int compare(Enrollment e1, Enrollment e2) {
								return e1.getRequest().compareTo(e2.getRequest());
							}
						});
						Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
						Collection<Enrollment> avEnrls = r.getAvaiableEnrollmentsSkipSameTime();
						for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							for (Request q: student.getRequests()) {
								if (q.equals(request)) continue;
								Enrollment x = q.getAssignment();
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
						        for (Iterator<Assignment> i = x.getAssignments().iterator(); i.hasNext();) {
						        	Assignment a = i.next();
									if (a.isOverlapping(enrl.getAssignments())) {
										overlap.add(x);
										if (x.getRequest() instanceof CourseRequest) {
											CourseRequest cr = (CourseRequest)x.getRequest();
											TreeSet<Section> ss = overlapingSections.get(cr);
											if (ss == null) { ss = new TreeSet<Section>(); overlapingSections.put(cr, ss); }
											ss.add((Section)a);
										}
									}
						        }
							}
							for (Enrollment q: overlap) {
								if (q.getRequest() instanceof FreeTimeRequest) {
									ca.addOverlap(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
								} else {
									CourseRequest cr = (CourseRequest)q.getRequest();
									Course o = q.getCourse();
									String ov = o.getSubjectArea() + " " + o.getCourseNumber();
									if (overlapingSections.get(cr).size() == 1)
										for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
											Section s = i.next();
											ov += " " + s.getSubpart().getName();
											if (i.hasNext()) ov += ",";
										}
									ca.addOverlap(ov);
								}
							}
							nrUnassignedCourses++;
							int alt = nrUnassignedCourses;
							for (Request q: student.getRequests()) {
								if (q.equals(request)) continue;
								Enrollment x = q.getAssignment();
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
								if (x.getRequest().isAlternative() && x.getRequest() instanceof CourseRequest) {
									if (--alt == 0) {
										Course o = x.getCourse();
										ca.setInstead(o.getSubjectArea() + " " +o.getCourseNumber());
										break;
									}
								}
							}
						}
						if (avEnrls.isEmpty()) ca.setNotAvailable(true);
					} else {
						TreeSet<Section> sections = new TreeSet<Section>(new EnrollmentSectionComparator());
						sections.addAll(r.getAssignment().getSections());
						boolean hasAlt = false;
						if (r.getCourses().size() > 1) {
							hasAlt = true;
						} else if (course.getOffering().getConfigs().size() > 1) {
							hasAlt = true;
						} else {
							for (Iterator<Subpart> i = ((Config)course.getOffering().getConfigs().get(0)).getSubparts().iterator(); i.hasNext();) {
								Subpart s = i.next();
								if (s.getSections().size() > 1) { hasAlt = true; break; }
							}
						}
						for (Iterator<Section> i = sections.iterator(); i.hasNext();) {
							Section section = (Section)i.next();
							ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
							a.setAlternative(r.isAlternative());
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
								for (Iterator<RoomLocation> e = section.getRooms().iterator(); e.hasNext(); ) {
									RoomLocation rm = e.next();
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
							a.setHasAlternatives(hasAlt);
							a.addNote(course.getNote());
							a.addNote(section.getNote());
							a.setCredit(section.getSubpart().getCredit());
							int dist = 0;
							String from = null;
							TreeSet<String> overlap = new TreeSet<String>();
							for (Request q: student.getRequests()) {
								Enrollment x = q.getAssignment();
								if (x == null || !x.isCourseRequest() || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
								for (Iterator<Section> j=x.getSections().iterator(); j.hasNext();) {
									Section s = j.next();
									if (s == section || s.getTime() == null) continue;
									int d = server.distance(s, section);
									if (d > dist) {
										dist = d;
										from = "";
										for (Iterator<RoomLocation> k = s.getRooms().iterator(); k.hasNext();)
											from += k.next().getName() + (k.hasNext() ? ", " : "");
									}
									if (d > s.getTime().getBreakTime()) {
										a.setDistanceConflict(true);
									}
									if (section.getTime() != null && section.getTime().hasIntersection(s.getTime()) && !section.isToIgnoreStudentConflictsWith(s.getId())) {
										overlap.add(MSG.clazz(x.getCourse().getSubjectArea(), x.getCourse().getCourseNumber(), s.getSubpart().getName(), s.getName(x.getCourse().getId())));
									}
								}
							}
							if (!overlap.isEmpty()) {
								String note = null;
								for (Iterator<String> j = overlap.iterator(); j.hasNext(); ) {
									String n = j.next();
									if (note == null)
										note = MSG.noteAllowedOverlapFirst(n);
									else if (j.hasNext())
										note += MSG.noteAllowedOverlapMiddle(n);
									else
										note += MSG.noteAllowedOverlapLast(n);
								}								a.addNote(note);
							}
							a.setBackToBackDistance(dist);
							a.setBackToBackRooms(from);
							a.setSaved(true);
							if (a.getParentSection() == null) {
								String consent = server.getCourseInfo(course.getId()).getConsent();
								if (consent != null) {
									if (r.getAssignment().getApproval() != null) {
										String[] approval = r.getAssignment().getApproval().split(":");
										a.setParentSection(MSG.consentApproved(df.format(new Date(Long.parseLong(approval[0])))));
									} else
										a.setParentSection(MSG.consentWaiting(consent.toLowerCase()));
								}
							}
							a.setExpected(Math.round(section.getSpaceExpected()));
						}
					}
				} else if (request instanceof FreeTimeRequest) {
					FreeTimeRequest r = (FreeTimeRequest)request;
					ca.setAssigned(r.getAssignment() != null);
					ca.setCourseId(null);
					if (r.getAssignment() == null) {
						for (Request q: student.getRequests()) {
							if (q.equals(request)) continue;
							Enrollment x = q.getAssignment();
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
					        for (Iterator<Assignment> i = x.getAssignments().iterator(); i.hasNext();) {
					        	Assignment a = i.next();
								if (r.isOverlapping(a) && x.getRequest() instanceof CourseRequest) {
									Course o = x.getCourse();
									Section s = (Section)a;
									ca.addOverlap(o.getSubjectArea() + " " + o.getCourseNumber() + " " + s.getSubpart().getName());
								}
					        }
						}
						if (ca.getOverlaps() == null)
							ca.setAssigned(true);
					}
					ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
					a.setAlternative(r.isAlternative());
					for (DayCode d : DayCode.toDayCodes(r.getTime().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(r.getTime().getStartSlot());
					a.setLength(r.getTime().getLength());
				}
				ret.add(ca);
			}
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "get-assignment";
	}

}
