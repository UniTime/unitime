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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.online.OnlineConfig;
import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.online.OnlineSection;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.cpsolver.studentsct.online.expectations.MinimizeConflicts;
import org.cpsolver.studentsct.online.expectations.NeverOverExpected;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.cpsolver.studentsct.online.selection.BestPenaltyCriterion;
import org.cpsolver.studentsct.online.selection.MultiCriteriaBranchAndBoundSelection;
import org.cpsolver.studentsct.online.selection.OnlineSectioningSelection;
import org.cpsolver.studentsct.online.selection.SuggestionSelection;
import org.cpsolver.studentsct.reservation.Reservation;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseReservation;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XDummyReservation;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.solver.studentsct.StudentSolver;

/**
 * @author Tomas Muller
 */
public class FindAssignmentAction implements OnlineSectioningAction<List<ClassAssignmentInterface>>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private CourseRequestInterface iRequest;
	private Collection<ClassAssignmentInterface.ClassAssignment> iAssignment;
	private Collection<ClassAssignmentInterface.ClassAssignment> iSpecialRegistration;
	
	public FindAssignmentAction forRequest(CourseRequestInterface request) {
		iRequest = request;
		return this;
	}
	
	public FindAssignmentAction withAssignment(Collection<ClassAssignmentInterface.ClassAssignment> assignment) {
		iAssignment = assignment;
		return this;
	}
	
	public CourseRequestInterface getRequest() {
		return iRequest;
	}
	
	public Collection<ClassAssignmentInterface.ClassAssignment> getAssignment() {
		return iAssignment;
	}
	
	public FindAssignmentAction withSpecialRegistration(Collection<ClassAssignmentInterface.ClassAssignment> assignment) {
		iSpecialRegistration = assignment;
		return this;
	}
	
	public Collection<ClassAssignmentInterface.ClassAssignment> getSpecialRegistration() {
		return iSpecialRegistration;
	}

	@Override
	public List<ClassAssignmentInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		long t0 = System.currentTimeMillis();
		OverExpectedCriterion overExpected = server.getOverExpectedCriterion();
		if ((getRequest().areSpaceConflictsAllowed() || getRequest().areTimeConflictsAllowed()) && server.getConfig().getPropertyBoolean("OverExpected.MinimizeConflicts", false)) {
			overExpected = new MinimizeConflicts(server.getConfig(), overExpected);
		}
		OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), overExpected);
		boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
		Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
		
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		
		if (getRequest().getStudentId() != null)
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getRequest().getStudentId()));
	
		Student student = new Student(getRequest().getStudentId() == null ? -1l : getRequest().getStudentId());

		Set<IdPair> enrolled = null;
		Lock readLock = server.readLock();
		ClassAssignmentInterface unavailabilities = null;
		try {
			XStudent original = (getRequest().getStudentId() == null ? null : server.getStudent(getRequest().getStudentId()));
			if (original != null) {
				unavailabilities = new ClassAssignmentInterface();
				GetAssignment.fillUnavailabilitiesIn(unavailabilities, original, server, helper, null);
				Collections.reverse(unavailabilities.getCourseAssignments());
				student.setExternalId(original.getExternalId());
				student.setName(original.getName());
				student.setNeedShortDistances(original.hasAccomodation(server.getDistanceMetric().getShortDistanceAccommodationReference()));
				student.setAllowDisabled(original.isAllowDisabled());
				if (server instanceof StudentSolver)
					student.setMaxCredit(original.getMaxCredit());
				action.getStudentBuilder().setUniqueId(original.getStudentId()).setExternalId(original.getExternalId()).setName(original.getName());
				enrolled = new HashSet<IdPair>();
				for (XRequest r: original.getRequests()) {
					if (r instanceof XCourseRequest && ((XCourseRequest)r).getEnrollment() != null) {
						XEnrollment e = ((XCourseRequest)r).getEnrollment();
						for (Long s: e.getSectionIds())
							enrolled.add(new IdPair(e.getCourseId(), s));
					}
				}
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
				for (XRequest oldRequest: original.getRequests()) {
					if (oldRequest instanceof XCourseRequest && ((XCourseRequest)oldRequest).getEnrollment() != null) {
						XCourseRequest cr = (XCourseRequest)oldRequest;
						XOffering offering = server.getOffering(cr.getEnrollment().getOfferingId());
						for (XSection section: offering.getSections(cr.getEnrollment()))
							enrollment.addSection(OnlineSectioningHelper.toProto(section, cr.getEnrollment()));
					}
				}
				action.addEnrollment(enrollment);
			}
			Map<Long, Section> classTable = new HashMap<Long, Section>();
			Set<XDistribution> distributions = new HashSet<XDistribution>();
			for (CourseRequestInterface.Request c: getRequest().getCourses())
				addRequest(server, model, assignment, student, original, c, false, false, classTable, distributions, getAssignment() != null);
			if (student.getRequests().isEmpty() && !CONSTANTS.allowEmptySchedule()) throw new SectioningException(MSG.exceptionNoCourse());
			for (CourseRequestInterface.Request c: getRequest().getAlternatives())
				addRequest(server, model, assignment, student, original, c, true, false, classTable, distributions, getAssignment() != null);
			if (helper.isAlternativeCourseEnabled()) {
				for (Request r: student.getRequests()) {
					if (r.isAlternative() || !(r instanceof CourseRequest)) continue;
					CourseRequest cr = (CourseRequest)r;
					if (cr.getCourses().size() == 1) {
						XCourse course = server.getCourse(cr.getCourses().get(0).getId());
						Long altCourseId = (course == null ? null : course.getAlternativeCourseId());
						if (altCourseId != null) {
							boolean hasCourse = false;
							for (Request x: student.getRequests())
								if (x instanceof CourseRequest)
									for (Course c: ((CourseRequest)x).getCourses())
										if (c.getId() == altCourseId) { hasCourse = true; break; }
							if (!hasCourse) {
								XCourseId ci = server.getCourse(altCourseId);
								if (ci != null) {
									XOffering x = server.getOffering(ci.getOfferingId());
									if (x != null) {
										cr.getCourses().add(clone(x, server.getEnrollments(x.getOfferingId()), ci.getCourseId(), student.getId(), original, classTable, server, model, getAssignment() != null));
										distributions.addAll(x.getDistributions());
									}
								}
							}
						}
					}
				}
			}
			if (student.getExternalId() != null && !student.getExternalId().isEmpty()) {
				Collection<Long> offeringIds = server.getInstructedOfferings(student.getExternalId());
				if (offeringIds != null)
					for (Long offeringId: offeringIds) {
						XOffering offering = server.getOffering(offeringId);
						if (offering != null)
							offering.fillInUnavailabilities(student);
					}
			}
			if (getRequest().areTimeConflictsAllowed() || getRequest().areSpaceConflictsAllowed()) {
				// Experimental: provide student with a blank override that allows for overlaps as well as over-limit
				for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
					Request r = (Request)e.next();
					if (r instanceof CourseRequest) {
						CourseRequest cr = (CourseRequest)r;
						for (Course course: cr.getCourses()) {
							XCourse xc = server.getCourse(course.getId());
							boolean time = getRequest().areTimeConflictsAllowed() && xc.areTimeConflictOverridesAllowed();
							boolean space = getRequest().areSpaceConflictsAllowed() && xc.areSpaceConflictOverridesAllowed();
							if (time || space) {
								new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), -100, space, 1, true, true, time, true) {
									@Override
									public boolean mustBeUsed() { return true; }
								};
							}
						}
					}
				}
			}
			model.addStudent(student);
			model.setDistanceConflict(new DistanceConflict(server.getDistanceMetric(), model.getProperties()));
			model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
			for (XDistribution link: distributions) {
				if (link.getDistributionType() == XDistributionType.LinkedSections) {
					List<Section> sections = new ArrayList<Section>();
					for (Long sectionId: link.getSectionIds()) {
						Section x = classTable.get(sectionId);
						if (x != null) sections.add(x);
					}
					if (sections.size() >= 2)
						model.addLinkedSections(linkedClassesMustBeUsed, sections);
				}
			}
		} finally {
			readLock.release();
		}
		
		Hashtable<CourseRequest, Set<Section>> preferredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		Hashtable<CourseRequest, Set<Section>> requiredOrSavedSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		HashSet<FreeTimeRequest> pinnedFreeTimes = new HashSet<FreeTimeRequest>();
		HashSet<FreeTimeRequest> requiredFreeTimes = new HashSet<FreeTimeRequest>();
		HashSet<CourseRequest> requiredUnassigned = new HashSet<CourseRequest>();

		int notAssigned = 0;
		double selectedPenalty = 0;
		if (getAssignment() != null && !getAssignment().isEmpty()) {

			OnlineSectioningLog.Enrollment.Builder requested = OnlineSectioningLog.Enrollment.newBuilder();
			requested.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
			for (ClassAssignmentInterface.ClassAssignment a: getAssignment())
				if (a != null && a.isAssigned())
					requested.addSection(OnlineSectioningHelper.toProto(a));
			action.addEnrollment(requested);
			
			
			Enrollment[] enrollmentArry = new Enrollment[student.getRequests().size()]; int idx = 0;
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				OnlineSectioningLog.Request.Builder rq = OnlineSectioningHelper.toProto(r); 
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					HashSet<Section> preferredSections = new HashSet<Section>();
					HashSet<Section> requiredSections = new HashSet<Section>();
					HashSet<Section> requiredOrSavedSections = new HashSet<Section>();
					HashSet<CourseRequest> allowOverlaps = new HashSet<CourseRequest>();
					boolean conflict = false;
					boolean assigned = false;
					List<ClassAssignmentInterface.ClassAssignment> specRegAdds = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
					Set<Long> specRegDrops = new HashSet<Long>();
					if (getSpecialRegistration() != null)
						for (ClassAssignmentInterface.ClassAssignment b: getSpecialRegistration()) {
							if (cr.getCourse(b.getCourseId()) != null) {
								if (b.isCourseAssigned()) {
									specRegAdds.add(b);
								} else {
									specRegDrops.add(b.getClassId());
								}
							}
						}
					a: for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
						if (a != null && !a.isFreeTime() && cr.getCourse(a.getCourseId()) != null && a.getClassId() != null) {
							assigned = true;
							Section section = cr.getSection(a.getClassId());
							if (section == null || section.getLimit() == 0) {
								continue a;
							}
							// check for drops
							if (specRegDrops.contains(a.getClassId())) {
								rq.addSection(OnlineSectioningHelper.toProto(section, cr.getCourse(a.getCourseId())).setPreference(OnlineSectioningLog.Section.Preference.DROP));
								continue a;
							}
							if (a.isPinned())
								requiredSections.add(section);
							if (a.isPinned() || (getSpecialRegistration() == null && a.isSaved()) || getRequest().isNoChange()) {
								if (!conflict) {
									for (Section s: requiredOrSavedSections)
										if (s.isOverlapping(section)) { conflict = true; break; }
									boolean allowOverlap = false;
									for (Reservation rx: cr.getReservations(cr.getCourse(a.getCourseId()))) {
										if (rx.isAllowOverlap()) { allowOverlap = true; break; }
									}
									if (allowOverlap) {
										allowOverlaps.add(cr);
									} else {
										for (Map.Entry<CourseRequest, Set<Section>> x: requiredOrSavedSectionsForCourse.entrySet()) {
											if (!allowOverlaps.contains(x.getKey()))
												for (Section s: x.getValue())
													if (s.isOverlapping(section)) { conflict = true; break; }
												if (conflict) break;
										}
									}
								}
								if (!conflict) requiredOrSavedSections.add(section);
							}
							selectedPenalty += model.getOverExpected(assignment, enrollmentArry, idx, section, cr);
							preferredSections.add(section);
							// cr.getSelectedChoices().add(section.getChoice());
							rq.addSection(OnlineSectioningHelper.toProto(section, cr.getCourse(a.getCourseId())).setPreference(
									a.isPinned() || a.isSaved() || getRequest().isNoChange() ? OnlineSectioningLog.Section.Preference.REQUIRED : OnlineSectioningLog.Section.Preference.PREFERRED));
						}
					}
					if (!assigned && getRequest().isNoChange()) {
						requiredUnassigned.add(cr);
					} else if (getSpecialRegistration() != null && (!specRegDrops.isEmpty() || !assigned) && specRegAdds.isEmpty()) {
						requiredUnassigned.add(cr);
					}
					// check for adds
					if (!specRegAdds.isEmpty()) {
						for (ClassAssignmentInterface.ClassAssignment a: specRegAdds) {
							Section section = cr.getSection(a.getClassId());
							if (section == null) continue;
							selectedPenalty += model.getOverExpected(assignment, enrollmentArry, idx, section, cr);
							preferredSections.add(section);
							if (a.isPinned())
								requiredSections.add(section);
							if (!conflict && a.isPinned()) {
								if (section.getLimit() == 0 && !getRequest().areSpaceConflictsAllowed())
									conflict = true;
								for (Section s: requiredOrSavedSections)
									if (s.isOverlapping(section)) { conflict = true; break; }
								boolean allowOverlap = false;
								for (Reservation rx: cr.getReservations(cr.getCourse(a.getCourseId()))) {
									if (rx.isAllowOverlap()) { allowOverlap = true; break; }
								}
								if (allowOverlap) {
									allowOverlaps.add(cr);
								} else {
									for (Map.Entry<CourseRequest, Set<Section>> x: requiredOrSavedSectionsForCourse.entrySet()) {
										if (!allowOverlaps.contains(x.getKey()))
											for (Section s: x.getValue())
												if (s.isOverlapping(section)) { conflict = true; break; }
											if (conflict) break;
									}
								}
								if (!conflict) requiredOrSavedSections.add(section);
							}
							rq.addSection(OnlineSectioningHelper.toProto(section, cr.getCourse(a.getCourseId())).setPreference(OnlineSectioningLog.Section.Preference.ADD));
						}
					}

					if (preferredSections.isEmpty()) notAssigned ++;
					preferredSectionsForCourse.put(cr, preferredSections);
					requiredSectionsForCourse.put(cr, requiredSections);
					if (!conflict)
						requiredOrSavedSectionsForCourse.put(cr, requiredOrSavedSections);
					if (!preferredSections.isEmpty()) {
						Section section = preferredSections.iterator().next();
						enrollmentArry[idx] = new Enrollment(cr, 0, section.getSubpart().getConfig(), preferredSections, assignment);
					}
				} else {
					FreeTimeRequest ft = (FreeTimeRequest)r;
					for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
						if (a != null && a.isFreeTime() && (a.isPinned() || getRequest().isNoChange()) && ft.getTime() != null &&
							ft.getTime().getStartSlot() == a.getStart() &&
							ft.getTime().getLength() == a.getLength() && 
							ft.getTime().getDayCode() == DayCode.toInt(DayCode.toDayCodes(a.getDays()))) {
							if (a.isPinned()) pinnedFreeTimes.add(ft);
							requiredFreeTimes.add(ft);
							for (OnlineSectioningLog.Time.Builder ftb: rq.getFreeTimeBuilderList())
								ftb.setPreference(OnlineSectioningLog.Section.Preference.REQUIRED);
						}
					}
				}
				action.addRequest(rq);
				idx++;
			}
		} else {
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();)
				action.addRequest(OnlineSectioningHelper.toProto(e.next())); 
		}
		long t1 = System.currentTimeMillis();
		
		boolean avoidOverExpected = server.getAcademicSession().isSectioningEnabled();
		if (avoidOverExpected && helper.getUser() != null && helper.getUser().hasType() && helper.getUser().getType() != OnlineSectioningLog.Entity.EntityType.STUDENT)
			avoidOverExpected = false;
		String override = ApplicationProperty.OnlineSchedulingAllowOverExpected.value();
		if (override != null)
			avoidOverExpected = "false".equalsIgnoreCase(override);
		
		double maxOverExpected = -1.0;
		if (avoidOverExpected && !(model.getOverExpectedCriterion() instanceof NeverOverExpected)) {
			if (notAssigned == 0 && getAssignment() != null && !getAssignment().isEmpty()) {
				maxOverExpected = selectedPenalty;
			} else {
				long x0 = System.currentTimeMillis();
				MultiCriteriaBranchAndBoundSelection selection = new MultiCriteriaBranchAndBoundSelection(model.getProperties());
				selection.setModel(model);
				selection.setPreferredSections(preferredSectionsForCourse);
				selection.setRequiredSections(requiredSectionsForCourse);
				selection.setRequiredFreeTimes(requiredFreeTimes);
				selection.setTimeout(100);
				BranchBoundNeighbour neighbour = selection.select(assignment, student, new BestPenaltyCriterion(student, model));
				long x1 = System.currentTimeMillis();
				if (neighbour != null) {
					maxOverExpected = 0;
					for (int i = 0; i < neighbour.getAssignment().length; i++) {
						Enrollment enrollment = neighbour.getAssignment()[i];
						if (enrollment != null && enrollment.getAssignments() != null && enrollment.isCourseRequest())
							for (Section section: enrollment.getSections())
								maxOverExpected += model.getOverExpected(assignment, neighbour.getAssignment(), i, section, enrollment.getRequest());
					}
					if (maxOverExpected < selectedPenalty) maxOverExpected = selectedPenalty;
					helper.debug("Maximum number of over-expected sections limited to " + maxOverExpected + " (computed in " + (x1 - x0) + " ms).");
				}
			}
		}
		
		OnlineSectioningSelection selection = null;
		
		if (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
			selection = new MultiCriteriaBranchAndBoundSelection(server.getConfig());
		} else {
			selection = new SuggestionSelection(server.getConfig());
		}
		
		selection.setModel(model);
		selection.setPreferredSections(preferredSectionsForCourse);
		selection.setRequiredSections(requiredOrSavedSectionsForCourse);
		selection.setRequiredFreeTimes(requiredFreeTimes);
		selection.setRequiredUnassinged(requiredUnassigned);
		if (maxOverExpected >= 0.0) selection.setMaxOverExpected(maxOverExpected);
		
		BranchBoundNeighbour neighbour = selection.select(assignment, student);
		boolean assigned = false;
		if (neighbour != null)
			for (Enrollment e: neighbour.getAssignment())
				if (e != null) { assigned = true; break; }
		if (!assigned) {
			selection.setRequiredSections(new Hashtable<CourseRequest, Set<Section>>());
			selection.setRequiredFreeTimes(new HashSet<FreeTimeRequest>());
			neighbour = selection.select(assignment, student);
		}
		if (neighbour == null && student.getRequests().isEmpty())
			neighbour = new BranchBoundNeighbour(student, 0, new Enrollment[] {});
		if (neighbour == null) throw new SectioningException(MSG.exceptionNoSolution());

		helper.debug("Using " + (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
				(server.getConfig().getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal") + " weighting model" +
				" with " + server.getConfig().getPropertyInt("Neighbour.BranchAndBoundTimeout", 1000) +" ms time limit.");

        neighbour.assign(assignment, 0);
        helper.debug("Solution: " + neighbour);
		
    	OnlineSectioningLog.Enrollment.Builder solution = OnlineSectioningLog.Enrollment.newBuilder();
    	solution.setType(OnlineSectioningLog.Enrollment.EnrollmentType.COMPUTED);
    	solution.setValue(-neighbour.value(assignment));
    	for (Enrollment e: neighbour.getAssignment()) {
			if (e != null && e.getAssignments() != null)
				for (SctAssignment section: e.getAssignments())
					solution.addSection(OnlineSectioningHelper.toProto(section, e));
		}
    	action.addEnrollment(solution);
        
		long t2 = System.currentTimeMillis();

		ClassAssignmentInterface ret = convert(server, model, assignment, student, neighbour, requiredSectionsForCourse, pinnedFreeTimes, enrolled);
		if (unavailabilities != null)
			for (ClassAssignmentInterface.CourseAssignment ca: unavailabilities.getCourseAssignments())
				ret.getCourseAssignments().add(0, ca);
		
		long t3 = System.currentTimeMillis();
		helper.debug("Sectioning took "+(t3-t0)+"ms (model "+(t1-t0)+"ms, sectioning "+(t2-t1)+"ms, conversion "+(t3-t2)+"ms)");

		List<ClassAssignmentInterface> rets = new ArrayList<ClassAssignmentInterface>(1);
		rets.add(ret);
		
		return rets;
	}
	
	@SuppressWarnings("unchecked")
	public static Course clone(XOffering offering, XEnrollments enrollments, Long courseId, long studentId, XStudent originalStudent, Map<Long, Section> sections, OnlineSectioningServer server, StudentSectioningModel model, boolean hasAssignment) {
		Offering clonedOffering = new Offering(offering.getOfferingId(), offering.getName());
		clonedOffering.setModel(model);
		XExpectations expectations = server.getExpectations(offering.getOfferingId());
		XCourse course = offering.getCourse(courseId);
		int courseLimit = course.getLimit();
		if (courseLimit >= 0) {
			courseLimit -= enrollments.countEnrollmentsForCourse(courseId);
			if (courseLimit < 0) courseLimit = 0;
			for (XEnrollment enrollment: enrollments.getEnrollmentsForCourse(courseId)) {
				if (enrollment.getStudentId().equals(studentId)) { courseLimit++; break; }
			}
		}
		Course clonedCourse = new Course(courseId, course.getSubjectArea(), course.getCourseNumber(), clonedOffering, courseLimit, course.getProjected());
		clonedCourse.setNote(course.getNote());
		Hashtable<Long, Config> configs = new Hashtable<Long, Config>();
		Hashtable<Long, Subpart> subparts = new Hashtable<Long, Subpart>();
		for (XConfig config: offering.getConfigs()) {
			int configLimit = config.getLimit();
			int configEnrl = enrollments.countEnrollmentsForConfig(config.getConfigId());
			boolean configStudent = false;
			if (studentId >= 0)
				for (XEnrollment enrollment: enrollments.getEnrollmentsForConfig(config.getConfigId()))
					if (enrollment.getStudentId().equals(studentId)) { configEnrl--; configStudent = true; break; }
			if (configLimit >= 0) {
				// limited configuration, deduct enrollments
				configLimit -= configEnrl;
				if (configLimit < 0) configLimit = 0; // over-enrolled, but not unlimited
				if (configStudent && configLimit == 0) configLimit = 1; // allow enrolled student in
			}
			OnlineConfig clonedConfig = new OnlineConfig(config.getConfigId(), configLimit, config.getName(), clonedOffering);
			if (config.getInstructionalMethod() != null) {
				clonedConfig.setInstructionalMethodId(config.getInstructionalMethod().getUniqueId());
				clonedConfig.setInstructionalMethodName(config.getInstructionalMethod().getLabel());
				clonedConfig.setInstructionalMethodReference(config.getInstructionalMethod().getReference());
			}
			clonedConfig.setEnrollment(configEnrl);
			configs.put(config.getConfigId(), clonedConfig);
			for (XSubpart subpart: config.getSubparts()) {
				Subpart clonedSubpart = new Subpart(subpart.getSubpartId(), subpart.getInstructionalType(), subpart.getName(), clonedConfig,
						(subpart.getParentId() == null ? null: subparts.get(subpart.getParentId())));
				clonedSubpart.setAllowOverlap(subpart.isAllowOverlap());
				clonedSubpart.setCredit(subpart.getCredit(courseId));
				subparts.put(subpart.getSubpartId(), clonedSubpart);
				for (XSection section: subpart.getSections()) {
					int limit = section.getLimit();
					int enrl = enrollments.countEnrollmentsForSection(section.getSectionId());
					boolean student = false;
					if (studentId >= 0)
						for (XEnrollment enrollment: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (enrollment.getStudentId().equals(studentId)) { enrl--; student = true; break; }
					if (limit >= 0) {
						// limited section, deduct enrollments
						limit -= enrl;
						if (limit < 0) limit = 0; // over-enrolled, but not unlimited
						if (student && limit == 0) limit = 1; // allow enrolled student in
					}
                    List<RoomLocation> rooms = new ArrayList<RoomLocation>();
                    for (XRoom r: section.getRooms())
                    	rooms.add(new RoomLocation(r.getUniqueId(), r.getName(), null, 0, 0, r.getX(), r.getY(), r.getIgnoreTooFar(), null));
                    Placement placement = section.getTime() == null || section.getTime().getDays() == 0 ? null : new Placement(
                    		new Lecture(section.getSectionId(), null, section.getSubpartId(), section.getName(), new ArrayList<TimeLocation>(), new ArrayList<RoomLocation>(), section.getNrRooms(), null, section.getLimit(), section.getLimit(), 1.0),
                    		new TimeLocation(section.getTime().getDays(), section.getTime().getSlot(), section.getTime().getLength(), 0, 0.0,
                    				section.getTime().getDatePatternId(), section.getTime().getDatePatternName(), section.getTime().getWeeks(),
                    				section.getTime().getBreakTime()),
                    		rooms);
					OnlineSection clonedSection = new OnlineSection(section.getSectionId(), limit,
							section.getName(course.getCourseId()), clonedSubpart, placement, section.toInstructors(),
							(section.getParentId() == null ? null : sections.get(section.getParentId())));
					clonedSection.setName(-1l, section.getName(-1l));
					clonedSection.setNote(section.getNote());
					clonedSection.setSpaceExpected(expectations.getExpectedSpace(section.getSectionId()));
					clonedSection.setEnrollment(enrl);
					clonedSection.setCancelled(section.isCancelled());
					clonedSection.setEnabled(student || section.isEnabledForScheduling());
					for (XDistribution distribution: offering.getDistributions())
						if (distribution.getDistributionType() == XDistributionType.IngoreConflicts && distribution.hasSection(section.getSectionId()))
							for (Long id: distribution.getSectionIds())
								if (!id.equals(section.getSectionId()))
									clonedSection.addIgnoreConflictWith(id);
			        if (limit > 0) {
			        	double available = Math.round(clonedSection.getSpaceExpected() - limit);
						clonedSection.setPenalty(available / section.getLimit());
			        }
					sections.put(section.getSectionId(), clonedSection);
				}
			}
		}
		boolean hasMustUse = false;
		for (XReservation reservation: offering.getReservations()) {
			int reservationLimit = (int)Math.round(reservation.getLimit());
			if (reservationLimit >= 0) {
				reservationLimit -= enrollments.countEnrollmentsForReservation(reservation.getReservationId());
				if (reservationLimit < 0) reservationLimit = 0;
				for (XEnrollment enrollment: enrollments.getEnrollmentsForReservation(reservation.getReservationId())) {
					if (enrollment.getStudentId().equals(studentId)) { reservationLimit++; break; }
				}
				if (reservationLimit <= 0 && !(reservation.mustBeUsed() & !reservation.isExpired())) continue;
			}
			boolean applicable = originalStudent != null && reservation.isApplicable(originalStudent, course);
			if (reservation instanceof XCourseReservation)
				applicable = ((XCourseReservation)reservation).getCourseId().equals(courseId);
			if (reservation instanceof XDummyReservation) {
				// Ignore by reservation only flag (dummy reservation) when the student is already enrolled in the course
				for (XEnrollment enrollment: enrollments.getEnrollmentsForCourse(courseId))
					if (enrollment.getStudentId().equals(studentId)) { applicable = true; break; }
			}
			if (applicable && reservation.mustBeUsed() && !reservation.isExpired()) hasMustUse = true;
			if (!applicable && reservation.isExpired()) continue;
			Reservation clonedReservation = new OnlineReservation(reservation.getType().ordinal(), reservation.getReservationId(), clonedOffering,
					reservation.getPriority(), reservation.canAssignOverLimit(), reservationLimit, 
					applicable, reservation.mustBeUsed(), reservation.isAllowOverlap(), reservation.isExpired());
			clonedReservation.setAllowDisabled(reservation.isAllowDisabled());
			for (Long configId: reservation.getConfigsIds())
				clonedReservation.addConfig(configs.get(configId));
			for (Map.Entry<Long, Set<Long>> entry: reservation.getSections().entrySet()) {
				Set<Section> clonedSections = new HashSet<Section>();
				for (Long sectionId: entry.getValue())
					clonedSections.add(sections.get(sectionId));
				clonedReservation.getSections().put(subparts.get(entry.getKey()), clonedSections);
			}
		}
		// There are reservations >> allow user to keep the current enrollment by providing a dummy reservation for it
		if (!offering.getReservations().isEmpty() && hasAssignment)
			for (XEnrollment enrollment: enrollments.getEnrollmentsForCourse(courseId))
				if (enrollment.getStudentId().equals(studentId)) {
					Reservation clonedReservation = null;
					if (hasMustUse) {
						clonedReservation = new OnlineReservation(XReservationType.Dummy.ordinal(), -2l, clonedOffering, 1000, false, 1, true, true, false, true) {
							@Override
							public boolean mustBeUsed() { return true; }
						};
					} else {
						clonedReservation = new OnlineReservation(XReservationType.Dummy.ordinal(), -2l, clonedOffering, 1000, false, 1, true, false, false, true);
					}
					clonedReservation.addConfig(configs.get(enrollment.getConfigId()));
					for (Long sectionId: enrollment.getSectionIds())
						clonedReservation.addSection(sections.get(sectionId));
					break;
				}
		return clonedCourse;
	}
	
	public static void addRequest(OnlineSectioningServer server, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, Student student, XStudent originalStudent, CourseRequestInterface.Request request, boolean alternative, boolean updateFromCache, Map<Long, Section> classTable, Set<XDistribution> distributions, boolean hasAssignment) {
		if (request.hasRequestedCourse()) {
			Vector<Course> cr = new Vector<Course>();
			Set<Choice> selChoices = new HashSet<Choice>();
			Set<Choice> reqChoices = new HashSet<Choice>();
			for (RequestedCourse rc: request.getRequestedCourse()) {
				if (rc.isFreeTime()) {
					for (CourseRequestInterface.FreeTime freeTime: rc.getFreeTime()) {
						int dayCode = 0;
						for (DayCode d: DayCode.values()) {
							if (freeTime.getDays().contains(d.getIndex()))
								dayCode |= d.getCode();
						}
						TimeLocation freeTimeLoc = new TimeLocation(dayCode, freeTime.getStart(), freeTime.getLength(), 0, 0, 
								-1l, "", server.getAcademicSession().getFreeTimePattern(), 0);
						new FreeTimeRequest(student.getRequests().size() + 1, student.getRequests().size(), alternative, student, freeTimeLoc);
					}
				} else if (rc.isCourse()) {
					XCourseId courseInfo = server.getCourse(rc.getCourseId(), rc.getCourseName());
					XOffering offering = null;
					if (courseInfo != null) offering = server.getOffering(courseInfo.getOfferingId());
					if (offering != null) {
						Course course = clone(offering, server.getEnrollments(offering.getOfferingId()), courseInfo.getCourseId(), student.getId(), originalStudent, classTable, server, model, hasAssignment);
						cr.add(course);
						if (rc.hasSelectedIntructionalMethods()) {
							for (Config config: course.getOffering().getConfigs()) {
								if (config.getInstructionalMethodId() != null && rc.isSelectedIntructionalMethod(config.getInstructionalMethodId())) {
									selChoices.add(new Choice(config));
									if (rc.isSelectedIntructionalMethod(config.getInstructionalMethodId(), true)) reqChoices.add(new Choice(config));
								}
							}
						}
						if (rc.hasSelectedClasses()) {
							for (Config config: course.getOffering().getConfigs())
								for (Subpart subpart: config.getSubparts())
									for (Section section: subpart.getSections())
										if (rc.isSelectedClass(section.getId())) {
											selChoices.add(new Choice(section));
											if (rc.isSelectedClass(section.getId(), true)) {
												Section s = section;
												while (s != null) {
													reqChoices.add(new Choice(s)); s = s.getParent();
												}
												reqChoices.add(new Choice(section.getSubpart().getConfig()));
											}
										}
						}
						distributions.addAll(offering.getDistributions());
					}	
				}
			}
			if (!cr.isEmpty()) {
				CourseRequest clonnedRequest = new CourseRequest(student.getRequests().size() + 1, student.getRequests().size(), alternative, student, cr, request.isWaitList(), null);
				clonnedRequest.getSelectedChoices().addAll(selChoices);
				clonnedRequest.getRequiredChoices().addAll(reqChoices);
				if (originalStudent != null)
					for (XRequest originalRequest: originalStudent.getRequests()) {
						XEnrollment originalEnrollment = (originalRequest instanceof XCourseRequest ? ((XCourseRequest)originalRequest).getEnrollment() : null);
						for (Course clonnedCourse: clonnedRequest.getCourses()) {
							if (originalEnrollment != null && originalEnrollment.getCourseId().equals(clonnedCourse.getId())) {
								if (!clonnedRequest.getRequiredChoices().isEmpty()) {
									boolean config = false;
									for (Long originalSectionId: originalEnrollment.getSectionIds()) {
										Section clonnedSection = classTable.get(originalSectionId);
										clonnedRequest.getRequiredChoices().add(new Choice(clonnedSection));
										if (!config) {
											clonnedRequest.getRequiredChoices().add(new Choice(clonnedSection.getSubpart().getConfig()));
											config = true;
										}
									}
								}
								if (clonnedCourse.getOffering().hasReservations()) {
									boolean needReservation = clonnedCourse.getOffering().getUnreservedSpace(assignment, clonnedRequest) < 1.0;
									if (!needReservation) {
										boolean configChecked = false;
										for (Long originalSectionId: originalEnrollment.getSectionIds()) {
											Section clonnedSection = classTable.get(originalSectionId);
											if (clonnedSection.getUnreservedSpace(assignment, clonnedRequest) < 1.0) { needReservation = true; break; }
											if (!configChecked && clonnedSection.getSubpart().getConfig().getUnreservedSpace(assignment, clonnedRequest) < 1.0) { needReservation = true; break; }
											configChecked = true;
										}
									}
									if (needReservation) {
										Reservation reservation = new OnlineReservation(XReservationType.Dummy.ordinal(),
												-originalStudent.getStudentId(), clonnedCourse.getOffering(), 1000, false, 1, true, false, false, true);
										for (Long originalSectionId: originalEnrollment.getSectionIds())
											reservation.addSection(classTable.get(originalSectionId));
									}
								}
								break;
							}
						}
					}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected ClassAssignmentInterface convert(OnlineSectioningServer server, Assignment<Request, Enrollment> assignment, Enrollment[] enrollments,
			Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse, HashSet<FreeTimeRequest> requiredFreeTimes,
			boolean computeOverlaps,
			DistanceConflict dc, Set<IdPair> savedClasses) throws SectioningException {
		DistanceMetric m = server.getDistanceMetric();
		OverExpectedCriterion overExp = server.getOverExpectedCriterion();
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
					XCourse xc = server.getCourse(course.getId());
					if (xc != null) ca.setTitle(xc.getTitle());
					ca.setWaitListed(r.isWaitlist());
					ca.setHasCrossList(course.getOffering().getCourses().size() > 1);
					if (!r.isWaitlist()) 
						nrUnassignedCourses++;
					if (computeOverlaps) {
						TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
							public int compare(Enrollment e1, Enrollment e2) {
								return e1.getRequest().compareTo(e2.getRequest());
							}
						});
						Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
						Collection<Enrollment> avEnrls = r.getAvaiableEnrollmentsSkipSameTime(assignment);
						for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							for (Enrollment x: enrollments) {
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
								if (x == enrollment) continue;
						        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
						        	SctAssignment a = i.next();
									if (a.isOverlapping(enrl.getAssignments())) {
										overlap.add(x);
										if (x.getRequest() instanceof CourseRequest) {
											CourseRequest cr = (CourseRequest)x.getRequest();
											TreeSet<Section> ss = overlapingSections.get(cr);
											if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
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
						unavailabilities: for (Unavailability unavailability: enrollment.getStudent().getUnavailabilities()) {
							for (Config config: course.getOffering().getConfigs())
								for (Subpart subpart: config.getSubparts())
									for (Section section: subpart.getSections()) {
										if (unavailability.isOverlapping(section)) {
											ca.addOverlap(MSG.teachingAssignment(unavailability.getSection().getName()));
											continue unavailabilities;
										}
									}
						}
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
						if (avEnrls.isEmpty()) {
							ca.setNotAvailable(true);
							ca.setFull(course.getLimit() == 0);
						}
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
					        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
					        	SctAssignment a = i.next();
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
				if (r.isAlternative() && assignment.getValue(r) != null) nrAssignedAlt++;
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
				ca.setHasCrossList(course.getOffering().getCourses().size() > 1);
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
				XOffering offering = server.getOffering(course.getOffering().getId());
				ca.setTitle(offering.getCourse(course.getId()).getTitle());
				XEnrollments enrl = server.getEnrollments(offering.getOfferingId());
				for (Iterator<Section> i = sections.iterator(); i.hasNext();) {
					Section section = (Section)i.next();
					ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
					a.setAlternative(r.isAlternative());
					a.setClassId(section.getId());
					a.setSubpart(section.getSubpart().getName());
					a.setSection(section.getName(course.getId()));
					a.setExternalId(offering.getSection(section.getId()).getExternalId(course.getId()));
					a.setClassNumber(section.getName(-1l));
					a.setCancelled(section.isCancelled());
					a.setLimit(new int[] {enrl.countEnrollmentsForSection(section.getId()), offering.getSection(section.getId()).getLimit()});
					if (section.getLimit() == 0) a.setOverlapNote(MSG.sectionIsFull());
					if (section.getTime() != null) {
						for (DayCode d : DayCode.toDayCodes(section.getTime().getDayCode()))
							a.addDay(d.getIndex());
						a.setStart(section.getTime().getStartSlot());
						a.setLength(section.getTime().getLength());
						a.setBreakTime(section.getTime().getBreakTime());
						a.setDatePattern(section.getTime().getDatePatternName());
					} else {
						XSection x = offering.getSection(section.getId());
						if (x != null && x.getTime() != null) {
							a.setDatePattern(x.getTime().getDatePatternName());
						}
					}
					if (section.getRooms() != null) {
						for (Iterator<RoomLocation> e = section.getRooms().iterator(); e.hasNext(); ) {
							RoomLocation rm = e.next();
							a.addRoom(rm.getId(), rm.getName());
						}
					} else {
						XSection x = offering.getSection(section.getId());
						if (x != null) {
							for (XRoom rm: x.getRooms()) {
								a.addRoom(rm.getUniqueId(), rm.getName());
							}
						}
					}
					if (section.hasInstructors()) {
						for (Instructor instructor: section.getInstructors()) {
							a.addInstructor(instructor.getName());
							a.addInstructoEmail(instructor.getEmail());
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
					XSection xSection = offering.getSection(section.getId());
					if (xSection != null) {
						Float credit = xSection.getCreditOverride(course.getId());
						if (credit != null) a.setCredit(FixedCreditUnitConfig.formatCredit(credit));
					}
					int dist = 0;
					String from = null;
					TreeSet<String> overlap = new TreeSet<String>();
					for (Enrollment x: enrollments) {
						if (x == null || !x.isCourseRequest() || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
						for (Iterator<Section> j=x.getSections().iterator(); j.hasNext();) {
							Section s = j.next();
							if (s == section || s.getTime() == null) continue;
							int d = distance(m, s, section);
							if (d > dist) {
								dist = d;
								from = "";
								for (Iterator<RoomLocation> k = s.getRooms().iterator(); k.hasNext();)
									from += k.next().getName() + (k.hasNext() ? ", " : "");
							}
							if (dc.inConflict(enrollment.getStudent(), s, section) && s.getTime().getStartSlot() < section.getTime().getStartSlot())
								a.setDistanceConflict(true);
							if (section.getTime() != null && section.getTime().hasIntersection(s.getTime()) && !section.isToIgnoreStudentConflictsWith(s.getId())) {
								overlap.add(MSG.clazz(x.getCourse().getSubjectArea(), x.getCourse().getCourseNumber(), s.getSubpart().getName(), s.getName(x.getCourse().getId())));
							}
						}
					}
					for (Unavailability unavailability: enrollment.getStudent().getUnavailabilities())
						if (section.getTime() != null && unavailability.getTime() != null && section.getTime().hasIntersection(unavailability.getTime()))
							overlap.add(unavailability.getSection().getName());
					if (!overlap.isEmpty()) {
						String note = null;
						for (Iterator<String> j = overlap.iterator(); j.hasNext(); ) {
							String n = j.next();
							if (note == null) {
								if (section.getLimit() == 0)
									note = MSG.noteFullSectionOverlapFirst(n);
								else
									note = MSG.noteAllowedOverlapFirst(n);
							} else if (j.hasNext())
								note += MSG.noteAllowedOverlapMiddle(n);
							else
								note += MSG.noteAllowedOverlapLast(n);
						}
						a.setOverlapNote(note);
					}
					a.setBackToBackDistance(dist);
					a.setBackToBackRooms(from);
					// if (dist > 0.0) a.setDistanceConflict(true);
					if (savedClasses != null && savedClasses.contains(new IdPair(course.getId(), section.getId()))) a.setSaved(true);
					if (a.getParentSection() == null)
						a.setParentSection(server.getCourse(course.getId()).getConsentLabel());
					a.setExpected(overExp.getExpected(section.getLimit(), section.getSpaceExpected()));
					if (getSpecialRegistration() != null)
						for (ClassAssignmentInterface.ClassAssignment b: getSpecialRegistration())
							if (b.getClassId().equals(a.getClassId())) {
								a.setSpecRegStatus(b.getSpecRegStatus());
								a.setSpecRegOperation(b.getSpecRegOperation());
								if (b.hasError()) a.setError(b.getError());
							}
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
	private ClassAssignmentInterface convert(OnlineSectioningServer server, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, Student student, BranchBoundNeighbour neighbour,
			Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse, HashSet<FreeTimeRequest> requiredFreeTimes, Set<IdPair> savedClasses) throws SectioningException {
        Enrollment [] enrollments = neighbour.getAssignment();
        if (enrollments == null || enrollments.length < student.getRequests().size())
        	throw new SectioningException(MSG.exceptionNoSolution());
        int idx=0;
        for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext(); idx++) {
        	Request r = e.next();
        	if (enrollments[idx] == null) {
        		Config c = null;
        		if (r instanceof CourseRequest)
        			c = (Config)((Course)((CourseRequest)r).getCourses().get(0)).getOffering().getConfigs().get(0);
        		enrollments[idx] = new Enrollment(r, 0, c, null, assignment);
        	}
        }
        
        ClassAssignmentInterface ret = convert(server, assignment, enrollments, requiredSectionsForCourse, requiredFreeTimes, true, model.getDistanceConflict(), savedClasses);
        ret.setValue(-neighbour.value(assignment));
        return ret;
	}
	
	@Override
	public String name() {
		return "section";
	}
	
	public int distance(DistanceMetric m, Section s1, Section s2) {
        if (s1.getPlacement()==null || s2.getPlacement()==null) return 0;
        TimeLocation t1 = s1.getTime();
        TimeLocation t2 = s2.getTime();
        if (!t1.shareDays(t2) || !t1.shareWeeks(t2)) return 0;
        int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
        if (m.doComputeDistanceConflictsBetweenNonBTBClasses()) {
        	if (a1 + t1.getNrSlotsPerMeeting() <= a2) {
        		int dist = Placement.getDistanceInMinutes(m, s1.getPlacement(), s2.getPlacement());
        		if (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
        			return dist;
        	}
        } else {
        	if (a1+t1.getNrSlotsPerMeeting()==a2)
        		return Placement.getDistanceInMinutes(m, s1.getPlacement(), s2.getPlacement());
        }
        return 0;
    }	
	
	public static class EnrollmentSectionComparator implements Comparator<Section> {
	    public boolean isParent(Section s1, Section s2) {
			Section p1 = s1.getParent();
			if (p1==null) return false;
			if (p1.equals(s2)) return true;
			return isParent(p1, s2);
		}

		public int compare(Section a, Section b) {
			if (isParent(a, b)) return 1;
	        if (isParent(b, a)) return -1;

	        int cmp = a.getSubpart().getInstructionalType().compareToIgnoreCase(b.getSubpart().getInstructionalType());
			if (cmp != 0) return cmp;
			
			return Double.compare(a.getId(), b.getId());
		}
	}
	
	public static class IdPair {
		private Long iId1, iId2;
		
		public IdPair(Long id1, Long id2) {
			iId1 = id1; iId2 = id2;
		}
		
		public Long getId1() { return iId1; }
		public Long getId2() { return iId2; }
		@Override
		public int hashCode() { return iId1.hashCode() ^ iId2.hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdPair)) return false;
			return iId1.equals(((IdPair)o).iId1) && iId2.equals(((IdPair)o).iId2);
		}
	}
}
