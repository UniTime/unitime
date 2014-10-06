/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.cpsolver.studentsct.online.selection.BestPenaltyCriterion;
import org.cpsolver.studentsct.online.selection.MultiCriteriaBranchAndBoundSelection;
import org.cpsolver.studentsct.online.selection.MultiCriteriaBranchAndBoundSuggestions;
import org.cpsolver.studentsct.online.selection.SuggestionsBranchAndBound;
import org.cpsolver.studentsct.reservation.Reservation;
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
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;

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
	
	public ClassAssignmentInterface.ClassAssignment getSelection() { return iSelection; }
	
	public String getFilter() { return iFilter; }
	
	@Override
	public List<ClassAssignmentInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		long t0 = System.currentTimeMillis();
		OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
		Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();

		OnlineSectioningLog.Action.Builder action = helper.getAction();

		if (getRequest().getStudentId() != null)
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(getRequest().getStudentId()));

		Student student = new Student(getRequest().getStudentId() == null ? -1l : getRequest().getStudentId());
		Set<Long> enrolled = null;

		Lock readLock = server.readLock();
		try {
			XStudent original = (getRequest().getStudentId() == null ? null : server.getStudent(getRequest().getStudentId()));
			if (original != null) {
				action.getStudentBuilder().setUniqueId(original.getStudentId()).setExternalId(original.getExternalId()).setName(original.getName());
				enrolled = new HashSet<Long>();
				for (XRequest r: original.getRequests()) {
					if (r instanceof XCourseRequest && ((XCourseRequest)r).getEnrollment() != null)
						for (Long s: ((XCourseRequest)r).getEnrollment().getSectionIds())
							enrolled.add(s);
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
				addRequest(server, model, assignment, student, original, c, false, true, classTable, distributions);
			if (student.getRequests().isEmpty()) throw new SectioningException(MSG.exceptionNoCourse());
			for (CourseRequestInterface.Request c: getRequest().getAlternatives())
				addRequest(server, model, assignment, student, original, c, true, true, classTable, distributions);
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
						model.addLinkedSections(sections);
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
		int notAssigned = 0;
		double selectedPenalty = 0;
		for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
			Request r = (Request)e.next();
			OnlineSectioningLog.Request.Builder rq = OnlineSectioningHelper.toProto(r); 
			if (r instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)r;
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
							messages.addMessage((a.isSaved() ? "Enrolled class" : a.isPinned() ? "Required class " : "Previously selected class ") + a.getSubject() + " " + a.getCourseNbr() + " " + a.getSubpart() + " " + a.getSection() + " is no longer available.");
							continue a;
						}
						selectedPenalty += model.getOverExpected(assignment, section, cr);
						if (a.isPinned() && !getSelection().equals(a)) 
							requiredSections.add(section);
						preferredSections.add(section);
						cr.getSelectedChoices().add(section.getChoice());
						rq.addSection(OnlineSectioningHelper.toProto(section, cr.getCourse(a.getCourseId())).setPreference(
								getSelection().equals(a) ? OnlineSectioningLog.Section.Preference.SELECTED :
								a.isPinned() ? OnlineSectioningLog.Section.Preference.REQUIRED : OnlineSectioningLog.Section.Preference.PREFERRED));
					}
				}
				if (preferredSections.isEmpty()) notAssigned ++;
				preferredSectionsForCourse.put(cr, preferredSections);
				requiredSectionsForCourse.put(cr, requiredSections);
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
			action.addRequest(rq);
		}
		
		long t2 = System.currentTimeMillis();
        
		SuggestionsBranchAndBound suggestionBaB = null;
		
		boolean avoidOverExpected = server.getAcademicSession().isSectioningEnabled();
		if (avoidOverExpected && helper.getUser() != null && helper.getUser().hasType() && helper.getUser().getType() != OnlineSectioningLog.Entity.EntityType.STUDENT)
			avoidOverExpected = false;
		String override = ApplicationProperty.OnlineSchedulingAllowOverExpected.value();
		if (override != null)
			avoidOverExpected = "false".equalsIgnoreCase(override);
		
		double maxOverExpected = -1.0;
		if (avoidOverExpected) {
			if (notAssigned == 0) {
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
					for (Enrollment enrollment: neighbour.getAssignment()) {
						if (enrollment != null && enrollment.isCourseRequest())
							for (Section section: enrollment.getSections())
								maxOverExpected += model.getOverExpected(assignment, section, enrollment.getRequest());
					}
					if (maxOverExpected < selectedPenalty) maxOverExpected = selectedPenalty;
					helper.debug("Maximum number of over-expected sections limited to " + maxOverExpected + " (computed in " + (x1 - x0) + " ms).");
				}				
			}
		}
		
		SuggestionsFilter filter = null;
		if (getFilter() != null && !getFilter().isEmpty()) {
			filter = new SuggestionsFilter(getFilter(), server.getAcademicSession().getDatePatternFirstDate());
		}
		
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
        	ret.add(convert(server, assignment, suggestion.getEnrollments(), requiredSectionsForCourse, requiredFreeTimes, true, model.getDistanceConflict(), enrolled));
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
