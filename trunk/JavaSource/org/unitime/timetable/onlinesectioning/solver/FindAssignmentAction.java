/*
 * UniTime 3.2 (University Timetabling Application)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.constraint.LinkedSections;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;
import net.sf.cpsolver.studentsct.reservation.CourseReservation;
import net.sf.cpsolver.studentsct.reservation.Reservation;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerImpl.DummyReservation;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerImpl.EnrollmentSectionComparator;
import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection;

/**
 * @author Tomas Muller
 */
public class FindAssignmentAction implements OnlineSectioningAction<List<ClassAssignmentInterface>>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private CourseRequestInterface iRequest;
	private Collection<ClassAssignmentInterface.ClassAssignment> iAssignment;
	private double iValue;
	
	public FindAssignmentAction(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> assignment) {
		iRequest = request;
		iAssignment = assignment;
	}
	
	public CourseRequestInterface getRequest() {
		return iRequest;
	}
	
	public Collection<ClassAssignmentInterface.ClassAssignment> getAssignment() {
		return iAssignment;
	}

	@Override
	public List<ClassAssignmentInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		long t0 = System.currentTimeMillis();
		StudentSectioningModel model = new StudentSectioningModel(server.getConfig());
		
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		
		if (getRequest().getStudentId() != null)
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getRequest().getStudentId()));
	
		Student student = new Student(getRequest().getStudentId() == null ? -1l : getRequest().getStudentId());

		Set<Long> enrolled = null;
		Lock readLock = server.readLock();
		try {
			Student original = (getRequest().getStudentId() == null ? null : server.getStudent(getRequest().getStudentId()));
			if (original != null) {
				action.getStudentBuilder().setUniqueId(original.getId()).setExternalId(original.getExternalId());
				enrolled = new HashSet<Long>();
				for (Request r: original.getRequests()) {
					if (r.getInitialAssignment() != null && r.getInitialAssignment().isCourseRequest())
						for (Section s: r.getInitialAssignment().getSections())
							enrolled.add(s.getId());
				}
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
				for (Request oldRequest: original.getRequests()) {
					if (oldRequest.getInitialAssignment() != null && oldRequest.getInitialAssignment().isCourseRequest())
						for (Section section: oldRequest.getInitialAssignment().getSections())
							enrollment.addSection(OnlineSectioningHelper.toProto(section, oldRequest.getInitialAssignment()));
				}
				action.addEnrollment(enrollment);
			}
			Map<Long, Section> classTable = new HashMap<Long, Section>();
			Set<LinkedSections> linkedSections = new HashSet<LinkedSections>();
			for (CourseRequestInterface.Request c: getRequest().getCourses())
				addRequest(server, model, student, original, c, false, false, classTable, linkedSections);
			if (student.getRequests().isEmpty()) throw new SectioningException(MSG.exceptionNoCourse());
			for (CourseRequestInterface.Request c: getRequest().getAlternatives())
				addRequest(server, model, student, original, c, true, false, classTable, linkedSections);
			model.addStudent(student);
			model.setDistanceConflict(new DistanceConflict(server.getDistanceMetric(), model.getProperties()));
			model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
			for (LinkedSections link: linkedSections) {
				List<Section> sections = new ArrayList<Section>();
				for (Offering offering: link.getOfferings())
					for (Subpart subpart: link.getSubparts(offering))
						for (Section section: link.getSections(subpart)) {
							Section x = classTable.get(section.getId());
							if (x != null) sections.add(x);
						}
				if (sections.size() >= 2)
					model.addLinkedSections(sections);
			}
		} finally {
			readLock.release();
		}
		
		Hashtable<CourseRequest, Set<Section>> preferredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		HashSet<FreeTimeRequest> requiredFreeTimes = new HashSet<FreeTimeRequest>();

		if (getAssignment() != null && !getAssignment().isEmpty()) {

			OnlineSectioningLog.Enrollment.Builder requested = OnlineSectioningLog.Enrollment.newBuilder();
			requested.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
			for (ClassAssignmentInterface.ClassAssignment assignment: getAssignment())
				if (assignment != null)
					requested.addSection(OnlineSectioningHelper.toProto(assignment));
			action.addEnrollment(requested);
			
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				OnlineSectioningLog.Request.Builder rq = OnlineSectioningHelper.toProto(r); 
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					HashSet<Section> preferredSections = new HashSet<Section>();
					HashSet<Section> requiredSections = new HashSet<Section>();
					a: for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
						if (a != null && !a.isFreeTime() && cr.getCourse(a.getCourseId()) != null && a.getClassId() != null) {
							Section section = cr.getSection(a.getClassId());
							if (section == null || section.getLimit() == 0) {
								continue a;
							}
							if (a.isPinned())
								requiredSections.add(section);
							preferredSections.add(section);
							cr.getSelectedChoices().add(section.getChoice());
							rq.addSection(OnlineSectioningHelper.toProto(section, cr.getCourse(a.getCourseId())).setPreference(
									a.isPinned() ? OnlineSectioningLog.Section.Preference.REQUIRED : OnlineSectioningLog.Section.Preference.PREFERRED));
						}
					}
					preferredSectionsForCourse.put(cr, preferredSections);
					requiredSectionsForCourse.put(cr, requiredSections);
				} else {
					FreeTimeRequest ft = (FreeTimeRequest)r;
					for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
						if (a != null && a.isFreeTime() && a.isPinned() && ft.getTime() != null &&
							ft.getTime().getStartSlot() == a.getStart() &&
							ft.getTime().getLength() == a.getLength() && 
							ft.getTime().getDayCode() == DayCode.toInt(DayCode.toDayCodes(a.getDays()))) {
							requiredFreeTimes.add(ft);
							for (OnlineSectioningLog.Time.Builder ftb: rq.getFreeTimeBuilderList())
								ftb.setPreference(OnlineSectioningLog.Section.Preference.REQUIRED);
						}
					}
				}
				action.addRequest(rq);
			}
		} else {
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();)
				action.addRequest(OnlineSectioningHelper.toProto(e.next())); 
		}
		long t1 = System.currentTimeMillis();
		
		OnlineSectioningSelection selection = null;
		
		if (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
			selection = new MultiCriteriaBranchAndBoundSelection(server.getConfig());
		} else {
			selection = new SuggestionSelection(server.getConfig());
		}
		
		selection.setModel(model);
		selection.setPreferredSections(preferredSectionsForCourse);
		selection.setRequiredSections(requiredSectionsForCourse);
		selection.setRequiredFreeTimes(requiredFreeTimes);
		
		BranchBoundNeighbour neighbour = selection.select(student);
		if (neighbour == null) throw new SectioningException(MSG.exceptionNoSolution());

		helper.info("Using " + (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
				(server.getConfig().getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal") + " weighting model" +
				" with " + server.getConfig().getPropertyInt("Neighbour.BranchAndBoundTimeout", 1000) +" ms time limit.");

        neighbour.assign(0);
        helper.info("Solution: " + neighbour);
		iValue = -neighbour.value();
		
    	OnlineSectioningLog.Enrollment.Builder solution = OnlineSectioningLog.Enrollment.newBuilder();
    	solution.setType(OnlineSectioningLog.Enrollment.EnrollmentType.COMPUTED);
    	solution.setValue(-neighbour.value());
    	for (Enrollment e: neighbour.getAssignment()) {
			if (e != null && e.getAssignments() != null)
				for (Assignment section: e.getAssignments())
					solution.addSection(OnlineSectioningHelper.toProto(section, e));
		}
    	action.addEnrollment(solution);
        
		long t2 = System.currentTimeMillis();

		ClassAssignmentInterface ret = convert(server, model, student, neighbour, requiredSectionsForCourse, requiredFreeTimes, enrolled);
		
		long t3 = System.currentTimeMillis();
		helper.info("Sectioning took "+(t3-t0)+"ms (model "+(t1-t0)+"ms, sectioning "+(t2-t1)+"ms, conversion "+(t3-t2)+"ms)");

		List<ClassAssignmentInterface> rets = new ArrayList<ClassAssignmentInterface>(1);
		rets.add(ret);
		
		return rets;
	}
	
	public double value() { return iValue; }
	
	@SuppressWarnings("unchecked")
	protected Course clone(Course course, long studentId, Student originalStudent, Map<Long, Section> classTable) {
		Offering clonedOffering = new Offering(course.getOffering().getId(), course.getOffering().getName());
		int courseLimit = course.getLimit();
		if (courseLimit >= 0) {
			courseLimit -= course.getEnrollments().size();
			if (courseLimit < 0) courseLimit = 0;
			for (Iterator<Enrollment> i = course.getEnrollments().iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
				if (enrollment.getStudent().getId() == studentId) { courseLimit++; break; }
			}
		}
		Course clonedCourse = new Course(course.getId(), course.getSubjectArea(), course.getCourseNumber(), clonedOffering, courseLimit, course.getProjected());
		clonedCourse.setNote(course.getNote());
		Hashtable<Config, Config> configs = new Hashtable<Config, Config>();
		Hashtable<Subpart, Subpart> subparts = new Hashtable<Subpart, Subpart>();
		Hashtable<Section, Section> sections = new Hashtable<Section, Section>();
		for (Iterator<Config> e = course.getOffering().getConfigs().iterator(); e.hasNext();) {
			Config config = e.next();
			int configLimit = config.getLimit();
			if (configLimit >= 0) {
				configLimit -= config.getEnrollments().size();
				if (configLimit < 0) configLimit = 0;
				for (Iterator<Enrollment> i = config.getEnrollments().iterator(); i.hasNext();) {
					Enrollment enrollment = i.next();
					if (enrollment.getStudent().getId() == studentId) { configLimit++; break; }
				}
			}
			Config clonedConfig = new Config(config.getId(), configLimit, config.getName(), clonedOffering);
			configs.put(config, clonedConfig);
			for (Iterator<Subpart> f = config.getSubparts().iterator(); f.hasNext();) {
				Subpart subpart = f.next();
				Subpart clonedSubpart = new Subpart(subpart.getId(), subpart.getInstructionalType(), subpart.getName(), clonedConfig,
						(subpart.getParent() == null ? null: subparts.get(subpart.getParent())));
				clonedSubpart.setAllowOverlap(subpart.isAllowOverlap());
				clonedSubpart.setCredit(subpart.getCredit());
				subparts.put(subpart, clonedSubpart);
				for (Iterator<Section> g = subpart.getSections().iterator(); g.hasNext();) {
					Section section = g.next();
					int limit = section.getLimit();
					if (limit >= 0) {
						// limited section, deduct enrollments
						limit -= section.getEnrollments().size();
						if (limit < 0) limit = 0; // over-enrolled, but not unlimited
						if (studentId >= 0)
							for (Enrollment enrollment: section.getEnrollments())
								if (enrollment.getStudent().getId() == studentId) { limit++; break; }
					}
					Section clonedSection = new Section(section.getId(), limit,
							section.getName(course.getId()), clonedSubpart, section.getPlacement(),
							section.getChoice().getInstructorIds(), section.getChoice().getInstructorNames(),
							(section.getParent() == null ? null : sections.get(section.getParent())));
					clonedSection.setName(-1l, section.getName(-1l));
					clonedSection.setNote(section.getNote());
					clonedSection.setSpaceExpected(section.getSpaceExpected());
					clonedSection.setSpaceHeld(section.getSpaceHeld());
			        if (section.getIgnoreConflictWithSectionIds() != null)
			        	for (Long id: section.getIgnoreConflictWithSectionIds())
			        		clonedSection.addIgnoreConflictWith(id);
			        if (limit > 0) {
			        	double available = Math.round(section.getSpaceExpected() - limit);
						clonedSection.setPenalty(available / section.getLimit());
			        }
					sections.put(section, clonedSection);
					classTable.put(section.getId(), clonedSection);
				}
			}
		}
		if (course.getOffering().hasReservations()) {
			for (Reservation reservation: course.getOffering().getReservations()) {
				int reservationLimit = (int)Math.round(reservation.getLimit());
				if (reservationLimit >= 0) {
					reservationLimit -= reservation.getEnrollments().size();
					if (reservationLimit < 0) reservationLimit = 0;
					for (Iterator<Enrollment> i = reservation.getEnrollments().iterator(); i.hasNext();) {
						Enrollment enrollment = i.next();
						if (enrollment.getStudent().getId() == studentId) { reservationLimit++; break; }
					}
					if (reservationLimit <= 0) continue;
				}
				boolean applicable = originalStudent != null && reservation.isApplicable(originalStudent);
				if (reservation instanceof CourseReservation)
					applicable = (course.getId() == ((CourseReservation)reservation).getCourse().getId());
				if (reservation instanceof net.sf.cpsolver.studentsct.reservation.DummyReservation) {
					// Ignore by reservation only flag (dummy reservation) when the student is already enrolled in the course
					for (Enrollment enrollment: course.getEnrollments())
						if (enrollment.getStudent().getId() == studentId) { applicable = true; break; }
				}
				Reservation clonedReservation = new DummyReservation(reservation.getId(), clonedOffering,
						reservation.getPriority(), reservation.canAssignOverLimit(), reservationLimit, 
						applicable, reservation.mustBeUsed(), reservation.isAllowOverlap(), reservation.isExpired());
				for (Config config: reservation.getConfigs())
					clonedReservation.addConfig(configs.get(config));
				for (Map.Entry<Subpart, Set<Section>> entry: reservation.getSections().entrySet()) {
					Set<Section> clonedSections = new HashSet<Section>();
					for (Section section: entry.getValue())
						clonedSections.add(sections.get(section));
					clonedReservation.getSections().put(
							subparts.get(entry.getKey()),
							clonedSections);
				}
			}
		}
		return clonedCourse;
	}
	
	protected void addRequest(OnlineSectioningServer server, StudentSectioningModel model, Student student, Student originalStudent, CourseRequestInterface.Request request, boolean alternative, boolean updateFromCache, Map<Long, Section> classTable, Set<LinkedSections> linkedSections) {
		if (request.hasRequestedFreeTime() && request.hasRequestedCourse() && server.getCourseInfo(request.getRequestedCourse()) != null)
			request.getRequestedFreeTime().clear();			
		if (request.hasRequestedFreeTime()) {
			for (CourseRequestInterface.FreeTime freeTime: request.getRequestedFreeTime()) {
				int dayCode = 0;
				for (DayCode d: DayCode.values()) {
					if (freeTime.getDays().contains(d.getIndex()))
						dayCode |= d.getCode();
				}
				TimeLocation freeTimeLoc = new TimeLocation(dayCode, freeTime.getStart(), freeTime.getLength(), 0, 0, 
						-1l, "", server.getAcademicSession().getFreeTimePattern(), 0);
				new FreeTimeRequest(student.getRequests().size() + 1, student.getRequests().size(), alternative, student, freeTimeLoc);
			}
		} else if (request.hasRequestedCourse()) {
			CourseInfo courseInfo = server.getCourseInfo(request.getRequestedCourse());
			Course course = null;
			if (courseInfo != null) course = server.getCourse(courseInfo.getUniqueId());
			if (course != null) {
				Vector<Course> cr = new Vector<Course>();
				cr.add(clone(course, student.getId(), originalStudent, classTable));
				if (request.hasFirstAlternative()) {
					CourseInfo ci = server.getCourseInfo(request.getFirstAlternative());
					if (ci != null) {
						Course x = server.getCourse(ci.getUniqueId());
						if (x != null) cr.add(clone(x, student.getId(), originalStudent, classTable));
					}
				}
				if (request.hasSecondAlternative()) {
					CourseInfo ci = server.getCourseInfo(request.getSecondAlternative());
					if (ci != null) {
						Course x = server.getCourse(ci.getUniqueId());
						if (x != null) cr.add(clone(x, student.getId(), originalStudent, classTable));
					}
				}
				for (Course clonedCourse: cr) {
					Collection<LinkedSections> links = server.getLinkedSections(clonedCourse.getOffering().getId());
					if (links != null) linkedSections.addAll(links);
				}
				CourseRequest clonnedRequest = new CourseRequest(student.getRequests().size() + 1, student.getRequests().size(), alternative, student, cr, request.isWaitList(), null);
				if (originalStudent != null)
					for (Request originalRequest: originalStudent.getRequests()) {
						Enrollment originalEnrollment = originalRequest.getAssignment();
						for (Course clonnedCourse: clonnedRequest.getCourses()) {
							if (!clonnedCourse.getOffering().hasReservations()) continue;
							if (originalEnrollment != null && clonnedCourse.equals(originalEnrollment.getCourse())) {
								boolean needReservation = clonnedCourse.getOffering().getUnreservedSpace(clonnedRequest) < 1.0;
								if (!needReservation) {
									boolean configChecked = false;
									for (Section originalSection: originalEnrollment.getSections()) {
										Section clonnedSection = classTable.get(originalSection.getId()); 
										if (clonnedSection.getUnreservedSpace(clonnedRequest) < 1.0) { needReservation = true; break; }
										if (!configChecked && clonnedSection.getSubpart().getConfig().getUnreservedSpace(clonnedRequest) < 1.0) { needReservation = true; break; }
										configChecked = true;
									}
								}
								if (needReservation) {
									Reservation reservation = new DummyReservation(-originalStudent.getId(), clonnedCourse.getOffering(), 5, false, 1, true, false, false, true);
									for (Section originalSection: originalEnrollment.getSections())
										reservation.addSection(classTable.get(originalSection.getId()));
								}
								break;
							}
						}
					}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private int[] getLimit(OnlineSectioningServer server, Section section, Long studentId) {
		Section original = server.getSection(section.getId());
		int actual = original.getEnrollments().size();
		/*
		if (studentId != null) {
			for (Iterator<Enrollment> i = original.getEnrollments().iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
				if (enrollment.getStudent().getId() == studentId) { actual--; break; }
			}
		}
		*/
		return new int[] {actual, original.getLimit()};
	}
	
	@SuppressWarnings("unchecked")
	protected ClassAssignmentInterface convert(OnlineSectioningServer server, Enrollment[] enrollments,
			Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse, HashSet<FreeTimeRequest> requiredFreeTimes,
			boolean computeOverlaps,
			DistanceConflict dc, Set<Long> savedClasses) throws SectioningException {
        ClassAssignmentInterface ret = new ClassAssignmentInterface();
		int nrUnassignedCourses = 0;
		int nrAssignedAlt = 0;
		for (Enrollment enrollment: enrollments) {
			if (enrollment == null) continue;
			if (enrollment.getRequest().isAlternative() && nrAssignedAlt >= nrUnassignedCourses &&
				(enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty())) continue;
			if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) {
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				if (enrollment.getRequest() instanceof CourseRequest) {
					CourseRequest r = (CourseRequest)enrollment.getRequest();
					Course course = enrollment.getCourse();
					if (server.isOfferingLocked(course.getOffering().getId()))
						ca.setLocked(true);
					ca.setAssigned(false);
					ca.setCourseId(course.getId());
					ca.setSubject(course.getSubjectArea());
					ca.setCourseNbr(course.getCourseNumber());
					ca.setWaitListed(r.isWaitlist());
					if (computeOverlaps) {
						TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
							public int compare(Enrollment e1, Enrollment e2) {
								return e1.getRequest().compareTo(e2.getRequest());
							}
						});
						Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
						Collection<Enrollment> avEnrls = r.getAvaiableEnrollmentsSkipSameTime();
						for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							for (Enrollment x: enrollments) {
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
								if (x == enrollment) continue;
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
						for (Enrollment x: enrollments) {
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
							if (x == enrollment) continue;
							if (x.getRequest().isAlternative() && x.getRequest() instanceof CourseRequest) {
								if (--alt == 0) {
									Course o = x.getCourse();
									ca.setInstead(o.getSubjectArea() + " " +o.getCourseNumber());
									break;
								}
							}
						}
						if (avEnrls.isEmpty()) ca.setNotAvailable(true);
					}
					ret.add(ca);
				} else {
					FreeTimeRequest r = (FreeTimeRequest)enrollment.getRequest();
					ca.setAssigned(false);
					ca.setCourseId(null);
					if (computeOverlaps) {
						for (Enrollment x: enrollments) {
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
							if (x == enrollment) continue;
					        for (Iterator<Assignment> i = x.getAssignments().iterator(); i.hasNext();) {
					        	Assignment a = i.next();
								if (r.isOverlapping(a) && x.getRequest() instanceof CourseRequest) {
									Course o = x.getCourse();
									Section s = (Section)a;
									ca.addOverlap(o.getSubjectArea() + " " + o.getCourseNumber() + " " + s.getSubpart().getName());
								}
					        }
						}
					}
					if (ca.getOverlaps() == null) ca.setAssigned(true);
					ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
					a.setAlternative(r.isAlternative());
					for (DayCode d : DayCode.toDayCodes(r.getTime().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(r.getTime().getStartSlot());
					a.setLength(r.getTime().getLength());
					ret.add(ca);
				}
			} else if (enrollment.getRequest() instanceof CourseRequest) {
				CourseRequest r = (CourseRequest)enrollment.getRequest();
				Set<Section> requiredSections = null;
				if (requiredSectionsForCourse != null) requiredSections = requiredSectionsForCourse.get(r);
				if (r.isAlternative() && r.isAssigned()) nrAssignedAlt++;
				TreeSet<Section> sections = new TreeSet<Section>(new EnrollmentSectionComparator());
				sections.addAll(enrollment.getSections());
				Course course = enrollment.getCourse();
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				if (server.isOfferingLocked(course.getOffering().getId()))
					ca.setLocked(true);
				ca.setAssigned(true);
				ca.setWaitListed(r.isWaitlist());
				ca.setCourseId(course.getId());
				ca.setSubject(course.getSubjectArea());
				ca.setCourseNbr(course.getCourseNumber());
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
					a.setSection(section.getName(course.getId()));
					a.setClassNumber(section.getName(-1l));
					a.setLimit(getLimit(server, section, r.getStudent().getId()));
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
					if (requiredSections != null && requiredSections.contains(section)) a.setPinned(true);
					a.setSubpartId(section.getSubpart().getId());
					a.setHasAlternatives(hasAlt);
					a.addNote(course.getNote());
					a.addNote(section.getNote());
					a.setCredit(section.getSubpart().getCredit());
					int dist = 0;
					String from = null;
					TreeSet<String> overlap = new TreeSet<String>();
					for (Enrollment x: enrollments) {
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
						}
						a.addNote(note);
					}
					a.setBackToBackDistance(dist);
					a.setBackToBackRooms(from);
					// if (dist > 0.0) a.setDistanceConflict(true);
					if (savedClasses != null && savedClasses.contains(section.getId())) a.setSaved(true);
					if (a.getParentSection() == null)
						a.setParentSection(server.getCourseInfo(course.getId()).getConsent());
					a.setExpected(Math.round(section.getSpaceExpected()));
				}
				ret.add(ca);
			} else {
				FreeTimeRequest r = (FreeTimeRequest)enrollment.getRequest();
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				ca.setAssigned(true);
				ca.setCourseId(null);
				ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
				a.setAlternative(r.isAlternative());
				for (DayCode d : DayCode.toDayCodes(r.getTime().getDayCode()))
					a.addDay(d.getIndex());
				a.setStart(r.getTime().getStartSlot());
				a.setLength(r.getTime().getLength());
				if (requiredFreeTimes != null && requiredFreeTimes.contains(r)) a.setPinned(true);
				ret.add(ca);
			}
		}
		
		return ret;	
	}

    @SuppressWarnings("unchecked")
	private ClassAssignmentInterface convert(OnlineSectioningServer server, StudentSectioningModel model, Student student, BranchBoundNeighbour neighbour,
			Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse, HashSet<FreeTimeRequest> requiredFreeTimes, Set<Long> savedClasses) throws SectioningException {
        Enrollment [] enrollments = neighbour.getAssignment();
        if (enrollments == null || enrollments.length == 0)
        	throw new SectioningException(MSG.exceptionNoSolution());
        int idx=0;
        for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext(); idx++) {
        	Request r = e.next();
        	if (enrollments[idx] == null) {
        		Config c = null;
        		if (r instanceof CourseRequest)
        			c = (Config)((Course)((CourseRequest)r).getCourses().get(0)).getOffering().getConfigs().get(0);
        		enrollments[idx] = new Enrollment(r, 0, c, null);
        	}
        }
        
        return convert(server, enrollments, requiredSectionsForCourse, requiredFreeTimes, true, model.getDistanceConflict(), savedClasses);
	}
	
	@Override
	public String name() {
		return "section";
	}
}
