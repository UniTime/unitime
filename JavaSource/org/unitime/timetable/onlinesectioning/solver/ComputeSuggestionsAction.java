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

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.constraint.DependentCourses;
import org.cpsolver.studentsct.constraint.HardDistanceConflicts;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.online.MaxOverExpectedConstraint;
import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.cpsolver.studentsct.online.expectations.MinimizeConflicts;
import org.cpsolver.studentsct.online.expectations.NeverOverExpected;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.cpsolver.studentsct.online.selection.BestPenaltyCriterion;
import org.cpsolver.studentsct.online.selection.MultiCriteriaBranchAndBoundSelection;
import org.cpsolver.studentsct.online.selection.MultiCriteriaBranchAndBoundSuggestions;
import org.cpsolver.studentsct.online.selection.SuggestionsBranchAndBound;
import org.cpsolver.studentsct.reservation.IndividualRestriction;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.Restriction;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment.CourseSection;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.solver.studentsct.StudentSolver;

/**
 * @author Tomas Muller
 */
public class ComputeSuggestionsAction extends FindAssignmentAction {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private ClassAssignmentInterface.ClassAssignment iSelection;
	private double iValue = 0.0;
	private String iFilter = null;
	
	public ComputeSuggestionsAction forRequest(CourseRequestInterface request) {
		super.forRequest(request);
		return this;
	}
	
	public ComputeSuggestionsAction withAssignment(Collection<ClassAssignmentInterface.ClassAssignment> assignment) {
		super.withAssignment(assignment);
		return this;
	}
	
	public ComputeSuggestionsAction withSelection(ClassAssignmentInterface.ClassAssignment selectedAssignment) {
		iSelection = selectedAssignment;
		return this;
	}
	
	public ComputeSuggestionsAction withFilter(String filter) {
		iFilter = filter;
		return this;
	}
		
	public ClassAssignmentInterface.ClassAssignment getSelection() {
		return iSelection;
	}
	

	public String getFilter() { return iFilter; }
	
	@Override
	public List<ClassAssignmentInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		long t0 = System.currentTimeMillis();
		OverExpectedCriterion overExpected = server.getOverExpectedCriterion();
		if ((getRequest().areSpaceConflictsAllowed() || getRequest().areTimeConflictsAllowed() || getRequest().areLinkedConflictsAllowed()) && server.getConfig().getPropertyBoolean("OverExpected.MinimizeConflicts", false)) {
			overExpected = new MinimizeConflicts(server.getConfig(), overExpected);
		}
		OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), overExpected);
		model.setDayOfWeekOffset(server.getAcademicSession().getDayOfWeekOffset());
		Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
		boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);

		OnlineSectioningLog.Action.Builder action = helper.getAction();

		if (getRequest().getStudentId() != null)
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getRequest().getStudentId()));

		Student student = new Student(getRequest().getStudentId() == null ? -1l : getRequest().getStudentId());
		Set<IdPair> enrolled = null;

		ClassAssignmentInterface unavailabilities = null;
		List<CourseSection> providedUnavailabilities = null;
		Lock readLock = server.readLock();
		try {
			boolean checkDeadlines = server.getConfig().getPropertyBoolean("FindAssignment.CheckDeadlines", false) && !getRequest().areDeadlineConflictsAllowed();
			Integer currentDateIndex = null;
			if (server.getConfig().getPropertyBoolean("FindAssignment.AvoidPastSections", true))
				currentDateIndex = Days.daysBetween(new LocalDate(server.getAcademicSession().getDatePatternFirstDate()), new LocalDate()).getDays() + server.getConfig().getPropertyInt("FindAssignment.AvoidPastOffset", 0);
			boolean onlineOnlyFilter = true;
			if (helper.hasAdminPermission() && server.getConfig().getPropertyBoolean("Load.OnlineOnlyAdminOverride", false))
				onlineOnlyFilter = false;
			else if (helper.hasAvisorPermission() && server.getConfig().getPropertyBoolean("Load.OnlineOnlyAdvisorOverride", false))
				onlineOnlyFilter = false;

			XStudent original = (getRequest().getStudentId() == null ? null : server.getStudent(getRequest().getStudentId()));
			if (original != null) {
				unavailabilities = new ClassAssignmentInterface();
				providedUnavailabilities = GetAssignment.fillUnavailabilitiesIn(unavailabilities, original, server, helper, null);
				Collections.reverse(unavailabilities.getCourseAssignments());
				student.setExternalId(original.getExternalId());
				student.setName(original.getName());
				student.setNeedShortDistances(original.hasAccomodation(server.getDistanceMetric().getShortDistanceAccommodationReference()));
				student.setAllowDisabled(original.isAllowDisabled());
				if (server instanceof StudentSolver)
					student.setMaxCredit(original.getMaxCredit());
				student.setClassFirstDate(original.getClassStartDate());
				student.setClassLastDate(original.getClassEndDate());
				student.setBackToBackPreference(original.getBackToBackPreference());
				student.setModalityPreference(original.getModalityPreference());
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
			Map<Long, Course> courseTable = new HashMap<Long, Course>();
			Map<Long, Long> parentCourses = new HashMap<Long, Long>();
			Set<XDistribution> distributions = new HashSet<XDistribution>();
			if (getAssignment() != null) getRequest().moveActiveSubstitutionsUp();
			for (CourseRequestInterface.Request c: getRequest().getCourses())
				addRequest(server, model, assignment, student, original, c, false, true, classTable, courseTable, parentCourses, distributions, getAssignment() != null, getAssignment() != null, checkDeadlines, currentDateIndex, onlineOnlyFilter, isCanRequirePreferences(), helper);
			if (student.getRequests().isEmpty()) throw new SectioningException(MSG.exceptionNoCourse());
			for (CourseRequestInterface.Request c: getRequest().getAlternatives())
				addRequest(server, model, assignment, student, original, c, true, true, classTable, courseTable, parentCourses, distributions, getAssignment() != null, getAssignment() != null, checkDeadlines, currentDateIndex, onlineOnlyFilter, isCanRequirePreferences(), helper);
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
										cr.getCourses().add(clone(x, server.getEnrollments(x.getOfferingId()), ci.getCourseId(), student.getId(), original, classTable, courseTable, parentCourses, server, model, getAssignment() != null, checkDeadlines, currentDateIndex, onlineOnlyFilter, helper));
										distributions.addAll(x.getDistributions());
									}
								}
							}
						}
					}
				}
			}
			boolean hasParent = false;
			for (Map.Entry<Long, Long> e: parentCourses.entrySet()) {
				Course course = courseTable.get(e.getKey());
				Course parent = courseTable.get(e.getValue());
				if (course != null && parent != null) {
					course.setParent(parent);
					hasParent = true;
				}
			}
			if (hasParent && model.getProperties().getPropertyBoolean("Sectioning.DependentCourses", true)) 
				model.addGlobalConstraint(new DependentCourses());
			if (providedUnavailabilities != null) {
				for (CourseSection cs: providedUnavailabilities) {
					if (!cs.getSection().isCancelled() && cs.getSection().getTime() != null) {
						Unavailability ua = new Unavailability(student,
								new Section(
										cs.getSection().getSectionId(),
										cs.getSection().getLimit(),
										cs.getCourse().getCourseName() + " " + cs.getSection().getSubpartName() + " " + cs.getSection().getName(cs.getCourse().getCourseId()),
										null,
										cs.getSection().toPlacement(0), null),
								cs.isAllowOverlap());
						ua.setTeachingAssignment(cs.isTeachingAssignment());
						ua.setCourseId(cs.getCourse().getCourseId());
					}
				}
			}
/*			if (student.getExternalId() != null && !student.getExternalId().isEmpty()) {
				Collection<Long> offeringIds = server.getInstructedOfferings(student.getExternalId());
				if (offeringIds != null)
					for (Long offeringId: offeringIds) {
						XOffering offering = server.getOffering(offeringId);
						if (offering != null)
							offering.fillInUnavailabilities(student);
					}
				if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessions", false))
					GetInfo.fillInUnavailabilitiesFromOtherSessions(student, server, helper);
				else if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessionsUsingDatabase", false))
					GetInfo.fillInUnavailabilitiesFromOtherSessionsUsingDatabase(student, server, helper);
			}*/
			model.addStudent(student);
			model.setStudentQuality(new StudentQuality(server.getDistanceMetric(), model.getProperties()));
			// model.setDistanceConflict(new DistanceConflict(server.getDistanceMetric(), model.getProperties()));
			// model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
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
		
		long t1 = System.currentTimeMillis();

		Hashtable<CourseRequest, Set<Section>> preferredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		HashSet<FreeTimeRequest> requiredFreeTimes = new HashSet<FreeTimeRequest>();
        ArrayList<ClassAssignmentInterface> ret = new ArrayList<ClassAssignmentInterface>();
        ClassAssignmentInterface messages = new ClassAssignmentInterface();
        ret.add(messages);
        
		OnlineSectioningLog.Enrollment.Builder requested = OnlineSectioningLog.Enrollment.newBuilder();
		requested.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
		for (ClassAssignmentInterface.ClassAssignment a: getAssignment())
			if (a != null && a.isAssigned())
				requested.addSection(OnlineSectioningHelper.toProto(a));
		action.addEnrollment(requested);

		Request selectedRequest = null;
		Section selectedSection = null;
		double selectedPenalty = 0;
		Enrollment enrollmentArray[] = new Enrollment[student.getRequests().size()]; int idx = 0;
		for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
			Request r = (Request)e.next();
			OnlineSectioningLog.Request.Builder rq = OnlineSectioningHelper.toProto(r); 
			if (r instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)r;
				// Experimental: provide student with a blank override that allows for overlaps as well as over-limit
				if (getRequest().areTimeConflictsAllowed() || getRequest().areSpaceConflictsAllowed() || getRequest().areLinkedConflictsAllowed()) {
					for (Course course: cr.getCourses()) {
						XCourse xc = server.getCourse(course.getId());
						boolean time = getRequest().areTimeConflictsAllowed() && xc.areTimeConflictOverridesAllowed();
						boolean space = getRequest().areSpaceConflictsAllowed() && xc.areSpaceConflictOverridesAllowed();
						boolean linked = getRequest().areLinkedConflictsAllowed() && xc.areLinkedConflictOverridesAllowed();
						boolean hasNeverIncludedReservation = false;
						if (!server.getConfig().getPropertyBoolean("Reservations.NeverIncludedAllowOverride", false))
							for (Reservation res: course.getOffering().getReservations()) {
								if (res.neverIncluded()) hasNeverIncludedReservation = true;
							}
						if (!hasNeverIncludedReservation && (time || space || linked)) {
							OnlineReservation dummy = new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), -100, space, 1, true, true, time, true, true);
							dummy.setBreakLinkedSections(linked);
							for (Config g: course.getOffering().getConfigs()) {
								dummy.addConfig(g);
								for (Subpart s: g.getSubparts()) {
									for (Section x: s.getSections()) {
										dummy.addSection(x, false);
									}
								}
							}
						}
						if ((time || space || linked) && server.getConfig().getPropertyBoolean("Restrictions.AllowOverride", false)) {
							if (course.getOffering().hasRestrictions()) {
								Restriction restriction = new IndividualRestriction(-3l, course.getOffering(), student.getId());
								for (Config c: course.getOffering().getConfigs())
									restriction.addConfig(c);
							}
						}
					}
				}

				if (!getSelection().isFreeTime() && cr.getCourse(getSelection().getCourseId()) != null) {
					selectedRequest = r;
					if (getSelection().getClassId() != null) {
						Section section = cr.getSection(getSelection().getClassId());
						if (section != null)
							selectedSection = section;
					}
				}
				HashSet<Section> preferredSections = new HashSet<Section>();
				HashSet<Section> requiredSections = new HashSet<Section>();
				a: for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
					if (a != null && !a.isFreeTime() && cr.getCourse(a.getCourseId()) != null && a.getClassId() != null) {
						Section section = cr.getSection(a.getClassId());
						boolean hasIndividualReservation = false;
						if (section != null && section.getLimit() == 0) {
							for (Reservation res: cr.getReservations(cr.getCourse(a.getCourseId()))) {
								if (!res.canAssignOverLimit()) continue;
								Set<Section> sect = res.getSections(section.getSubpart());
								if (sect == null || sect.contains(section)) hasIndividualReservation = true;
							}
						}
						if (section == null || (section.getLimit() == 0  && !hasIndividualReservation)) {
							messages.addMessage((a.isSaved() ? "Enrolled class " : a.isPinned() ? "Required class " : "Previously selected class ") + a.getSubject() + " " + a.getCourseNbr() + " " + a.getSubpart() + " " + a.getSection() + " is no longer available.");
							if (getSelection().getCourseId() != null && cr.getCourse(getSelection().getCourseId()) != null)
								continue a;
						}
						selectedPenalty += model.getOverExpected(assignment, enrollmentArray, idx, section, cr);
						if (a.isPinned() && !getSelection().equals(a)) 
							requiredSections.add(section);
						preferredSections.add(section);
						rq.addSection(OnlineSectioningHelper.toProto(section, cr.getCourse(a.getCourseId())).setPreference(
								getSelection().equals(a) ? OnlineSectioningLog.Section.Preference.SELECTED :
								a.isPinned() ? OnlineSectioningLog.Section.Preference.REQUIRED : OnlineSectioningLog.Section.Preference.PREFERRED));
					}
				}
				preferredSectionsForCourse.put(cr, preferredSections);
				requiredSectionsForCourse.put(cr, requiredSections);
				if (!preferredSections.isEmpty()) {
					Section section = preferredSections.iterator().next();
					enrollmentArray[idx] = new Enrollment(cr, 0, section.getSubpart().getConfig(), preferredSections, assignment);
				}
			} else {
				FreeTimeRequest ft = (FreeTimeRequest)r;
				if (getSelection().isFreeTime() && ft.getTime() != null &&
					ft.getTime().getStartSlot() == getSelection().getStart() &&
					ft.getTime().getLength() == getSelection().getLength() && 
					ft.getTime().getDayCode() == DayCode.toInt(DayCode.toDayCodes(getSelection().getDays()))) {
					selectedRequest = r;
					for (OnlineSectioningLog.Time.Builder ftb: rq.getFreeTimeBuilderList())
						ftb.setPreference(OnlineSectioningLog.Section.Preference.SELECTED);
				} else for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
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
			idx++;
			action.addRequest(rq);
		}
		
		long t2 = System.currentTimeMillis();
		
		if (selectedRequest == null) return new ArrayList<ClassAssignmentInterface>();
		
		SuggestionsBranchAndBound suggestionBaB = null;
		
		boolean avoidOverExpected = server.getAcademicSession().isSectioningEnabled();
		if (avoidOverExpected && helper.getUser() != null && helper.getUser().hasType() && helper.getUser().getType() != OnlineSectioningLog.Entity.EntityType.STUDENT)
			avoidOverExpected = false;
		String override = ApplicationProperty.OnlineSchedulingAllowOverExpected.value();
		if (override != null)
			avoidOverExpected = "false".equalsIgnoreCase(override);
		
		double maxOverExpected = -1.0;
		if (avoidOverExpected && !(model.getOverExpectedCriterion() instanceof NeverOverExpected)) {
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
		
		SuggestionsFilter filter = null;
		if (getFilter() != null && !getFilter().isEmpty()) {
			filter = new SuggestionsFilter(getFilter(), server.getAcademicSession().getDatePatternFirstDate());
		}
		
		if (maxOverExpected >= 0.0)
			model.addGlobalConstraint(new MaxOverExpectedConstraint(maxOverExpected));
		
		if (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
			suggestionBaB = new MultiCriteriaBranchAndBoundSuggestions(
					model.getProperties(), student, assignment,
					requiredSectionsForCourse, requiredFreeTimes, preferredSectionsForCourse,
					selectedRequest, selectedSection,
					filter, maxOverExpected, server.getConfig().getPropertyBoolean("StudentWeights.PriorityWeighting", true));
		} else {
			suggestionBaB = new SuggestionsBranchAndBound(model.getProperties(), student, assignment,
					requiredSectionsForCourse, requiredFreeTimes, preferredSectionsForCourse,
					selectedRequest, selectedSection, filter, maxOverExpected);
		}
		
		helper.debug("Using " + (server.getConfig().getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
				(server.getConfig().getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal") + " weighting model" +
				" with " + server.getConfig().getPropertyInt("Suggestions.Timeout", 5000) +" ms time limit" +
				(maxOverExpected < 0 ? "" : ", maximal over-expected of " + maxOverExpected) +
				" and maximal depth of " + server.getConfig().getPropertyInt("Suggestions.MaxDepth", 4) + ".");

		TreeSet<SuggestionsBranchAndBound.Suggestion> suggestions = suggestionBaB.computeSuggestions();
		iValue = (suggestions.isEmpty() ? 0.0 : - suggestions.first().getValue());
        
		long t3 = System.currentTimeMillis();
		helper.debug("  -- suggestion B&B took "+suggestionBaB.getTime()+"ms"+(suggestionBaB.isTimeoutReached()?", timeout reached":""));

		for (SuggestionsBranchAndBound.Suggestion suggestion : suggestions) {
			ClassAssignmentInterface ca = convert(server, assignment, suggestion.getEnrollments(), requiredSectionsForCourse, requiredFreeTimes, true, model.getStudentQuality(), enrolled); 
			if (unavailabilities != null)
				for (ClassAssignmentInterface.CourseAssignment u: unavailabilities.getCourseAssignments())
					ca.getCourseAssignments().add(0, u);
        	ret.add(ca);
			
        	OnlineSectioningLog.Enrollment.Builder solution = OnlineSectioningLog.Enrollment.newBuilder();
        	solution.setType(OnlineSectioningLog.Enrollment.EnrollmentType.COMPUTED);
        	solution.setValue(- suggestion.getValue());
    		for (Enrollment e: suggestion.getEnrollments()) {
    			if (e != null && e.getAssignments() != null)
    				for (SctAssignment section: e.getAssignments())
    					solution.addSection(OnlineSectioningHelper.toProto(section, e));
    		}
			action.addEnrollment(solution);
        }
		
		// No suggestions -- compute conflicts with message
		if (suggestions.isEmpty() && selectedRequest != null && selectedRequest instanceof CourseRequest) {
			TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
				@Override
				public int compare(Enrollment o1, Enrollment o2) {
					return o1.getRequest().compareTo(o2.getRequest());
				}
			});
			Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
			CourseRequest request = (CourseRequest)selectedRequest;
			Course course = request.getCourses().get(0);
			Collection<Enrollment> avEnrls = request.getAvaiableEnrollmentsSkipSameTime(assignment);
			TreeSet<String> overlapMessages = new TreeSet<String>();
			for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
				Enrollment enrl = e.next();
				for (Request q: enrl.getStudent().getRequests()) {
					if (q.equals(request)) continue;
					Enrollment x = assignment.getValue(q);
					if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
			        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
			        	SctAssignment a = i.next();
						if (a.isOverlapping(enrl.getAssignments()) || HardDistanceConflicts.inConflict(model.getStudentQuality(), a, enrl)) {
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
				unavailabilities: for (Unavailability unavailability: student.getUnavailabilities()) {
					for (SctAssignment section: enrl.getAssignments()) {
						if (HardDistanceConflicts.inConflict(model.getStudentQuality(), (Section)section, unavailability)) {
							overlapMessages.add(unavailability.getCourseName() + " " + unavailability.getSectionName());
							continue unavailabilities;
						}
					}
				}
			}
			unavailabilities: for (Unavailability unavailability: student.getUnavailabilities()) {
				for (Config config: course.getOffering().getConfigs())
					for (Subpart subpart: config.getSubparts())
						for (Section section: subpart.getSections()) {
							if (section.getLimit() > 0 && unavailability.isOverlapping(section)) {
								overlapMessages.add(unavailability.getCourseName() + " " + unavailability.getSectionName());
								continue unavailabilities;
							}
						}
			}
			for (Iterator<Enrollment> i = overlap.iterator(); i.hasNext();) {
				Enrollment q = i.next();
				String ov = null;
				if (q.getRequest() instanceof FreeTimeRequest) {
					ov = OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest());
				} else {
					CourseRequest cr = (CourseRequest)q.getRequest();
					Course o = q.getCourse();
					ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
					if (overlapingSections.get(cr).size() == 1)
						for (Iterator<Section> j = overlapingSections.get(cr).iterator(); j.hasNext();) {
							Section s = j.next();
							ov += " " + s.getSubpart().getName();
						}
				}
				overlapMessages.add(ov);
			}
			if (!overlapMessages.isEmpty()) {
				String overlapMessage = null;
				for (Iterator<String> i = overlapMessages.iterator(); i.hasNext(); ) {
					String ov = i.next();
					if (overlapMessage == null)
						overlapMessage = ov;
					else if (i.hasNext()) {
						overlapMessage += MSG.conflictWithMiddle(ov);
					} else {
						overlapMessage += MSG.conflictWithLast(ov);
					}
				}
				messages.addMessage(MSG.suggestionsNoChoicesCourseIsConflicting(MSG.course(course.getSubjectArea(), course.getCourseNumber()), overlapMessage));
			} else if (course.getLimit() == 0) {
				messages.addMessage(MSG.suggestionsNoChoicesCourseIsFull(MSG.course(course.getSubjectArea(), course.getCourseNumber())));
			} else if (SectioningRequest.hasInconsistentRequirements(request, course.getId())) {
				messages.addMessage(MSG.suggestionsNoChoicesDueToStudentPrefs(MSG.course(course.getSubjectArea(), course.getCourseNumber())));
			}
			if (student.hasMaxCredit()) {
				float assignedCredit = 0;
				for (Request q: student.getRequests()) {
					if (q.equals(request)) continue;
					Enrollment e = q.getAssignment(assignment);
					if (e != null) assignedCredit += e.getCredit();
				}
				Float minCred = null;
				for (Course c: request.getCourses()) {
					if (c.hasCreditValue() && (minCred == null || minCred > c.getCreditValue()))
						minCred = c.getCreditValue();
				}
				if (minCred != null && assignedCredit + minCred > student.getMaxCredit())
					messages.addMessage(MSG.conflictOverMaxCredit(student.getMaxCredit()));
			}
		}
        
		long t4 = System.currentTimeMillis();
		helper.debug("Sectioning took "+(t4-t0)+"ms (model "+(t1-t0)+"ms, solver init "+(t2-t1)+"ms, sectioning "+(t3-t2)+"ms, conversion "+(t4-t3)+"ms)");

		return ret;
	}

	@Override
	public String name() {
		return "suggestions";
	}
	
	public double value() {
		return iValue;
	}

}
