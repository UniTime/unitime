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
package org.unitime.timetable.onlinesectioning.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.ifs.util.JProf;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.StudentPreferencePenalties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.StudentSectioningXMLLoader;
import org.cpsolver.studentsct.StudentSectioningXMLSaver;
import org.cpsolver.studentsct.constraint.LinkedSections;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import org.cpsolver.studentsct.heuristics.studentord.StudentChoiceOrder;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.unitime.timetable.onlinesectioning.model.OnlineConfig;
import org.unitime.timetable.onlinesectioning.model.OnlineSection;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.reports.OnlineSectioningReport.Counter;
import org.unitime.timetable.onlinesectioning.solver.OnlineSectioningModel;
import org.unitime.timetable.onlinesectioning.solver.OnlineSectioningSelection;
import org.unitime.timetable.onlinesectioning.solver.StudentSchedulingAssistantWeights;
import org.unitime.timetable.onlinesectioning.solver.SuggestionSelection;
import org.unitime.timetable.onlinesectioning.solver.SuggestionsBranchAndBound;
import org.unitime.timetable.onlinesectioning.solver.expectations.AvoidUnbalancedWhenNoExpectations;
import org.unitime.timetable.onlinesectioning.solver.expectations.FractionallyUnbalancedWhenNoExpectations;
import org.unitime.timetable.onlinesectioning.solver.expectations.FractionallyOverExpected;
import org.unitime.timetable.onlinesectioning.solver.expectations.PercentageOverExpected;
import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection;
import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSuggestions;


/**
 * @author Tomas Muller
 */
public class InMemorySectioningTest {
	public static Logger sLog = Logger.getLogger(InMemorySectioningTest.class);
	
	private OnlineSectioningModel iModel;
	private Assignment<Request, Enrollment> iAssignment;
	private boolean iSuggestions = false;
	
	private Map<String, Counter> iCounters = new HashMap<String, Counter>();
	
	public InMemorySectioningTest(DataProperties config) {
		iModel = new TestModel(config);
		iModel.setDistanceConflict(new DistanceConflict(new DistanceMetric(iModel.getProperties()), iModel.getProperties()));
		iModel.getDistanceConflict().register(iModel);
		iModel.getDistanceConflict().setAssignmentContextReference(iModel.createReference(iModel.getDistanceConflict()));
		iModel.setTimeOverlaps(new TimeOverlapsCounter(null, iModel.getProperties()));
		iModel.getTimeOverlaps().register(iModel);
		iModel.getTimeOverlaps().setAssignmentContextReference(iModel.createReference(iModel.getTimeOverlaps()));
		iModel.setStudentWeights(new StudentSchedulingAssistantWeights(iModel.getProperties()));
		iAssignment = new DefaultSingleAssignment<Request, Enrollment>();
		iSuggestions = "true".equals(System.getProperty("suggestions", iSuggestions ? "true" : "false"));
		
		String overexp = System.getProperty("overexp");
		if (overexp != null) {
			boolean bal = false;
			if (overexp.startsWith("b")) {
				bal = true;
				overexp = overexp.substring(1);
			}
			String[] x = overexp.split("[/\\-]");
			if (x.length == 1) {
				iModel.setOverExpectedCriterion(new PercentageOverExpected(Double.valueOf(x[0])));
			} else if (x.length == 2) {
				iModel.setOverExpectedCriterion(bal
						? new AvoidUnbalancedWhenNoExpectations(Double.valueOf(x[0]), Double.valueOf(x[1]) / 100.0)
						: new FractionallyOverExpected(Double.valueOf(x[0]), Double.valueOf(x[1])));
			} else {
				iModel.setOverExpectedCriterion(
						new FractionallyUnbalancedWhenNoExpectations(Double.valueOf(x[0]), Double.valueOf(x[1]), Double.valueOf(x[2]) / 100.0));
			}
		}
		
		sLog.info("Using " + (config.getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
				(config.getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal") + " weighting model" +
				" with over-expected " + iModel.getOverExpectedCriterion() +
				(iSuggestions ? ", suggestions" : "") +
				", " + System.getProperty("sort", "shuffle") + " order" +
				" and " + config.getPropertyInt("Neighbour.BranchAndBoundTimeout", 1000) +" ms time limit.");
	}
	
	public OnlineSectioningModel model() { return iModel; }
	
	public Assignment<Request, Enrollment> assignment() { return iAssignment; }
	
	public void inc(String name, double value) {
		synchronized (iCounters) {
			Counter c = iCounters.get(name);
			if (c == null) {
				c = new Counter();
				iCounters.put(name, c);
			}
			c.inc(value);
		}
	}
	
	public void inc(String name) {
		inc(name, 1.0);
	}
	
	public Counter get(String name) {
		synchronized (iCounters) {
			Counter c = iCounters.get(name);
			if (c == null) {
				c = new Counter();
				iCounters.put(name, c);
			}
			return c;
		}
	}
	
	public double getPercDisbalancedSections(Assignment<Request, Enrollment> assignment, double perc) {
		boolean balanceUnlimited = model().getProperties().getPropertyBoolean("General.BalanceUnlimited", false);
		double disb10Sections = 0, nrSections = 0;
        for (Offering offering: model().getOfferings()) {
            for (Config config: offering.getConfigs()) {
                double enrl = config.getEnrollmentWeight(assignment, null);
                for (Subpart subpart: config.getSubparts()) {
                    if (subpart.getSections().size() <= 1) continue;
                	nrSections += subpart.getSections().size();
                    if (subpart.getLimit() > 0) {
                        // sections have limits -> desired size is section limit x (total enrollment / total limit)
                        double ratio = enrl / subpart.getLimit();
                        for (Section section: subpart.getSections()) {
                            double desired = ratio * section.getLimit();
                            if (Math.abs(desired - section.getEnrollmentWeight(assignment, null)) >= Math.max(1.0, perc * section.getLimit()))
                                disb10Sections++;
                        }
                    } else if (balanceUnlimited) {
                        // unlimited sections -> desired size is total enrollment / number of sections
                        for (Section section: subpart.getSections()) {
                            double desired = enrl / subpart.getSections().size();
                            if (Math.abs(desired - section.getEnrollmentWeight(assignment, null)) >= Math.max(1.0, perc * desired))
                                disb10Sections++;
                        }
                    }
                }
            }
        }
        return 100.0 * disb10Sections / nrSections;
	}
	
	protected Course clone(Course course, long studentId, Student originalStudent, Map<Long, Section> classTable, StudentSectioningModel model) {
		Offering clonedOffering = new Offering(course.getOffering().getId(), course.getOffering().getName());
		clonedOffering.setModel(model);
		int courseLimit = course.getLimit();
		if (courseLimit >= 0) {
			courseLimit -= course.getEnrollments(assignment()).size();
			if (courseLimit < 0) courseLimit = 0;
			for (Iterator<Enrollment> i = course.getEnrollments(assignment()).iterator(); i.hasNext();) {
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
			int configEnrollment = config.getEnrollments(assignment()).size();
			if (configLimit >= 0) {
				configLimit -= config.getEnrollments(assignment()).size();
				if (configLimit < 0) configLimit = 0;
				for (Iterator<Enrollment> i = config.getEnrollments(assignment()).iterator(); i.hasNext();) {
					Enrollment enrollment = i.next();
					if (enrollment.getStudent().getId() == studentId) { configLimit++; configEnrollment--; break; }
				}
			}
			OnlineConfig clonedConfig = new OnlineConfig(config.getId(), configLimit, config.getName(), clonedOffering);
			clonedConfig.setEnrollment(configEnrollment);
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
					int enrl = section.getEnrollments(assignment()).size();
					if (limit >= 0) {
						// limited section, deduct enrollments
						limit -= section.getEnrollments(assignment()).size();
						if (limit < 0) limit = 0; // over-enrolled, but not unlimited
						if (studentId >= 0)
							for (Enrollment enrollment: section.getEnrollments(assignment()))
								if (enrollment.getStudent().getId() == studentId) { limit++; enrl--; break; }
					}
					OnlineSection clonedSection = new OnlineSection(section.getId(), limit,
							section.getName(course.getId()), clonedSubpart, section.getPlacement(),
							section.getChoice().getInstructorIds(), section.getChoice().getInstructorNames(),
							(section.getParent() == null ? null : sections.get(section.getParent())));
					clonedSection.setName(-1l, section.getName(-1l));
					clonedSection.setNote(section.getNote());
					clonedSection.setSpaceExpected(section.getSpaceExpected());
					clonedSection.setSpaceHeld(section.getSpaceHeld());
					clonedSection.setEnrollment(enrl);
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
					reservationLimit -= reservation.getEnrollments(assignment()).size();
					if (reservationLimit < 0) reservationLimit = 0;
					for (Iterator<Enrollment> i = reservation.getEnrollments(assignment()).iterator(); i.hasNext();) {
						Enrollment enrollment = i.next();
						if (enrollment.getStudent().getId() == studentId) { reservationLimit++; break; }
					}
					if (reservationLimit <= 0) continue;
				}
				boolean applicable = originalStudent != null && reservation.isApplicable(originalStudent);
				if (reservation instanceof CourseReservation)
					applicable = (course.getId() == ((CourseReservation)reservation).getCourse().getId());
				if (reservation instanceof org.cpsolver.studentsct.reservation.DummyReservation) {
					// Ignore by reservation only flag (dummy reservation) when the student is already enrolled in the course
					for (Enrollment enrollment: course.getEnrollments(assignment()))
						if (enrollment.getStudent().getId() == studentId) { applicable = true; break; }
				}
				Reservation clonedReservation = new XOffering.SimpleReservation(XReservationType.Dummy, reservation.getId(), clonedOffering,
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
	
	protected Request addRequest(Student student, Student original, Request request, Map<Long, Section> classTable, StudentSectioningModel model) {
		if (request instanceof FreeTimeRequest) {
			return new FreeTimeRequest(student.getRequests().size() + 1, student.getRequests().size(), request.isAlternative(), student, ((FreeTimeRequest) request).getTime());
		} else if (request instanceof CourseRequest) {
			List<Course> courses = new ArrayList<Course>();
			for (Course course: ((CourseRequest) request).getCourses())
				courses.add(clone(course, student.getId(), original, classTable, model));
			CourseRequest clonnedRequest = new CourseRequest(student.getRequests().size() + 1, student.getRequests().size(), request.isAlternative(), student, courses, ((CourseRequest) request).isWaitlist(), null);
			for (Request originalRequest: original.getRequests()) {
				Enrollment originalEnrollment = assignment().getValue(originalRequest);
				for (Course clonnedCourse: clonnedRequest.getCourses()) {
					if (!clonnedCourse.getOffering().hasReservations()) continue;
					if (originalEnrollment != null && clonnedCourse.equals(originalEnrollment.getCourse())) {
						boolean needReservation = clonnedCourse.getOffering().getUnreservedSpace(assignment(), clonnedRequest) < 1.0;
						if (!needReservation) {
							boolean configChecked = false;
							for (Section originalSection: originalEnrollment.getSections()) {
								Section clonnedSection = classTable.get(originalSection.getId()); 
								if (clonnedSection.getUnreservedSpace(assignment(), clonnedRequest) < 1.0) { needReservation = true; break; }
								if (!configChecked && clonnedSection.getSubpart().getConfig().getUnreservedSpace(assignment(), clonnedRequest) < 1.0) { needReservation = true; break; }
								configChecked = true;
							}
						}
						if (needReservation) {
							Reservation reservation = new XOffering.SimpleReservation(XReservationType.Dummy, -original.getId(), clonnedCourse.getOffering(), 5, false, 1, true, false, false, true);
							for (Section originalSection: originalEnrollment.getSections())
								reservation.addSection(classTable.get(originalSection.getId()));
						}
						break;
					}
				}
			}	
			return clonnedRequest;
		} else {
			return null;
		}
	}
	
	public boolean section(Student original) {
		OnlineSectioningModel model = new TestModel(iModel.getProperties());
		model.setOverExpectedCriterion(iModel.getOverExpectedCriterion());
		Student student = new Student(original.getId());
		Hashtable<CourseRequest, Set<Section>> preferredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		Map<Long, Section> classTable = new HashMap<Long, Section>();
		
		synchronized (iModel) {
			for (Request request: original.getRequests()) {
				Request clonnedRequest = addRequest(student, original, request, classTable, model);
				Enrollment enrollment = assignment().getValue(request);
				if (enrollment != null && enrollment.isCourseRequest()) {
					Set<Section> sections = new HashSet<Section>();
					for (Section section: enrollment.getSections())
						sections.add(classTable.get(section.getId()));
					preferredSectionsForCourse.put((CourseRequest)clonnedRequest, sections);
				}
			}
		}
		
		model.addStudent(student);
		model.setDistanceConflict(new DistanceConflict(iModel.getDistanceConflict().getDistanceMetric(), model.getProperties()));
		model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
		for (LinkedSections link: iModel.getLinkedSections()) {
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
		OnlineSectioningSelection selection = null;
		if (model.getProperties().getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
			selection = new MultiCriteriaBranchAndBoundSelection(iModel.getProperties());
		} else {
			selection = new SuggestionSelection(model.getProperties());
		}
		
		selection.setModel(model);
		selection.setPreferredSections(preferredSectionsForCourse);
		selection.setRequiredSections(new Hashtable<CourseRequest, Set<Section>>());
		selection.setRequiredFreeTimes(new HashSet<FreeTimeRequest>());
		
		long t0 = JProf.currentTimeMillis();
		Assignment<Request, Enrollment> newAssignment = new AssignmentMap<Request, Enrollment>();
		BranchBoundNeighbour neighbour = selection.select(newAssignment, student);
		long time = JProf.currentTimeMillis() - t0;
		inc("[C] CPU Time", time);
		if (neighbour == null) {
			inc("[F] Failure");
		} else {
			if (iSuggestions) {
				StudentPreferencePenalties penalties = new StudentPreferencePenalties(StudentPreferencePenalties.sDistTypePreference);
				double maxOverExpected = 0;
				int assigned = 0;
				double penalty = 0.0;
				Hashtable<CourseRequest, Set<Section>> enrollments = new Hashtable<CourseRequest, Set<Section>>();
				List<RequestSectionPair> pairs = new ArrayList<InMemorySectioningTest.RequestSectionPair>();
				
				for (int i = 0; i < neighbour.getAssignment().length; i++) {
					Enrollment enrl = neighbour.getAssignment()[i];
					if (enrl != null && enrl.isCourseRequest() && enrl.getAssignments() != null) {
						assigned ++;
						for (Section section: enrl.getSections()) {
							maxOverExpected += model.getOverExpected(newAssignment, section, enrl.getRequest());
							pairs.add(new RequestSectionPair(enrl.variable(), section));
						}
						enrollments.put((CourseRequest) enrl.variable(), enrl.getSections());
						penalty += penalties.getPenalty(enrl);
					}
				}
				penalty /= assigned;
				inc("[S] Initial Penalty", penalty);
				double nrSuggestions = 0.0, nrAccepted = 0.0, totalSuggestions = 0.0, nrTries = 0.0;
				for (int i = 0; i < pairs.size(); i++) {
					RequestSectionPair pair = pairs.get(i);
					SuggestionsBranchAndBound suggestionBaB = null;
					if (model.getProperties().getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
						suggestionBaB = new MultiCriteriaBranchAndBoundSuggestions(
								model.getProperties(), student, newAssignment,
								new Hashtable<CourseRequest, Set<Section>>(), new HashSet<FreeTimeRequest>(), enrollments,
								pair.getRequest(), pair.getSection(),
								null, null, maxOverExpected,
								iModel.getProperties().getPropertyBoolean("StudentWeights.PriorityWeighting", true));
					} else {
						suggestionBaB = new SuggestionsBranchAndBound(model.getProperties(), student, newAssignment,
								new Hashtable<CourseRequest, Set<Section>>(), new HashSet<FreeTimeRequest>(), enrollments,
								pair.getRequest(), pair.getSection(),
								null, null, maxOverExpected);
					}
					
					long x0 = JProf.currentTimeMillis();
					TreeSet<SuggestionsBranchAndBound.Suggestion> suggestions = suggestionBaB.computeSuggestions();
					inc("[S] Suggestion CPU Time", JProf.currentTimeMillis() - x0);
					totalSuggestions += suggestions.size();
					if (!suggestions.isEmpty()) nrSuggestions += 1.0;
					nrTries += 1.0;

					SuggestionsBranchAndBound.Suggestion best = null;
					for (SuggestionsBranchAndBound.Suggestion suggestion: suggestions) {
						int a = 0;
						double p = 0.0;
						for (int j = 0; j < suggestion.getEnrollments().length; j++) {
							Enrollment e = suggestion.getEnrollments()[j];
							if (e != null && e.isCourseRequest() && e.getAssignments() != null) {
								p += penalties.getPenalty(e);
								a ++;
							}
						}
						p /= a;
						if (a > assigned || (assigned == a && p < penalty)) {
							best = suggestion;
						}
					}
					if (best != null) {
						nrAccepted += 1.0;
						Enrollment[] e = best.getEnrollments();
						for (int j = 0; j < e.length; j++)
							if (e[j] != null && e[j].getAssignments() == null) e[j] = null;
						neighbour = new BranchBoundNeighbour(student, best.getValue(), e);
						assigned = 0; penalty = 0.0;
						enrollments.clear(); pairs.clear();
						for (int j = 0; j < neighbour.getAssignment().length; j++) {
							Enrollment enrl = neighbour.getAssignment()[j];
							if (enrl != null && enrl.isCourseRequest() && enrl.getAssignments() != null) {
								assigned ++;
								for (Section section: enrl.getSections())
									pairs.add(new RequestSectionPair(enrl.variable(), section));
								enrollments.put((CourseRequest) enrl.variable(), enrl.getSections());
								penalty += penalties.getPenalty(enrl);
							}
						}
						penalty /= assigned;
						inc("[S] Improved Penalty", penalty);
					}
				}
				inc("[S] Final Penalty", penalty);
				if (nrSuggestions > 0) {
					inc("[S] Classes with suggestion", nrSuggestions);
					inc("[S] Avg. # of suggestions", totalSuggestions / nrSuggestions);
					inc("[S] Suggestion acceptance rate [%]", nrAccepted / nrSuggestions);
				} else {
					inc("[S] Student with no suggestions available", 1.0);
				}
				if (!pairs.isEmpty())
					inc("[S] Probability that a class has suggestions [%]", nrSuggestions / nrTries);
			}
			
			List<Enrollment> enrollments = new ArrayList<Enrollment>();
			i: for (int i = 0; i < neighbour.getAssignment().length; i++) {
				Request request = original.getRequests().get(i);
				Enrollment clonnedEnrollment = neighbour.getAssignment()[i];
				if (clonnedEnrollment != null && clonnedEnrollment.getAssignments() != null) {
					if (request instanceof FreeTimeRequest) {
						enrollments.add(((FreeTimeRequest)request).createEnrollment());
					} else {
						for (Course course: ((CourseRequest)request).getCourses())
							if (course.getId() == clonnedEnrollment.getCourse().getId())
								for (Config config: course.getOffering().getConfigs())
									if (config.getId() == clonnedEnrollment.getConfig().getId()) {
										Set<Section> assignments = new HashSet<Section>();
										for (Subpart subpart: config.getSubparts())
											for (Section section: subpart.getSections()) {
												if (clonnedEnrollment.getSections().contains(section)) {
													assignments.add(section);
												}
											}
										Reservation reservation = null;
										if (clonnedEnrollment.getReservation() != null) {
											for (Reservation r: course.getOffering().getReservations())
												if (r.getId() == clonnedEnrollment.getReservation().getId()) { reservation = r; break; }
										}
										enrollments.add(new Enrollment(request, clonnedEnrollment.getPriority(), course, config, assignments, reservation));
										continue i;
									}
					}
				}
			}
			synchronized (iModel) {
				for (Request r: original.getRequests()) {
					Enrollment e = assignment().getValue(r);
                	r.setInitialAssignment(e);
                	if (e != null) updateSpace(assignment(), e, true);
				}
				for (Request r: original.getRequests())
					if (assignment().getValue(r) != null) assignment().unassign(0, r);
				boolean fail = false;
				for (Enrollment enrl: enrollments) {
					if (iModel.conflictValues(assignment(), enrl).isEmpty()) {
						assignment().assign(0, enrl);
					} else {
						fail = true; break;
					}
				}
				if (fail) {
					for (Request r: original.getRequests())
						if (assignment().getValue(r) != null) assignment().unassign(0, r);
					for (Request r: original.getRequests())
						if (r.getInitialAssignment() != null) assignment().assign(0, r.getInitialAssignment());
					for (Request r: original.getRequests())
						if (assignment().getValue(r) != null) updateSpace(assignment(), assignment().getValue(r), false);
				} else {
					for (Enrollment enrl: enrollments)
						updateSpace(assignment(), enrl, false);
				}
				if (fail) return false;
			}
			neighbour.assign(newAssignment, 0);
			int a = 0, u = 0, np = 0, zp = 0, pp = 0, cp = 0;
			double over = 0;
			double p = 0.0;
			for (Request r: student.getRequests()) {
				if (r instanceof CourseRequest) {
					Enrollment e = newAssignment.getValue(r);
					if (e != null) {
						for (Section s: e.getSections()) {
							if (s.getPenalty() < 0.0) np ++;
							if (s.getPenalty() == 0.0) zp ++;
							if (s.getPenalty() > 0.0) pp++;
							if (s.getLimit() > 0) {
								p += s.getPenalty(); cp ++;
							}
							over += model.getOverExpected(newAssignment, s, r);
						}
						a++;
					} else {
						u++;
					}
				}
			}
			inc("[A] Student");
			if (over > 0.0)
				inc("[O] Over", over);
			if (a > 0)
				inc("[A] Assigned", a);
			if (u > 0)
				inc("[A] Not Assigned", u);
			inc("[V] Value", neighbour.value(newAssignment));
			if (zp > 0)
				inc("[P] Zero penalty", zp);
			if (np > 0)
				inc("[P] Negative penalty", np);
			if (pp > 0)
				inc("[P] Positive penalty", pp);
			if (cp > 0)
				inc("[P] Average penalty", p / cp);
		}
		inc("[T0] Time <10ms", time < 10 ? 1 : 0);
		inc("[T1] Time <100ms", time < 100 ? 1 : 0);
		inc("[T2] Time <250ms", time < 250 ? 1 : 0);
		inc("[T3] Time <500ms", time < 500 ? 1 : 0);
		inc("[T4] Time <1s", time < 1000 ? 1 : 0);
		inc("[T5] Time >=1s", time >= 1000 ? 1 : 0);
		return true;
	}
	
	public static void updateSpace(Assignment<Request, Enrollment> assignment, Enrollment enrollment, boolean increment) {
    	if (enrollment == null || !enrollment.isCourseRequest()) return;
        for (Section section : enrollment.getSections())
            section.setSpaceHeld(section.getSpaceHeld() + (increment ? 1.0 : -1.0));
        List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
        int totalLimit = 0;
        for (Enrollment enrl : enrollment.getRequest().values(assignment)) {
        	if (!enrl.getCourse().equals(enrollment.getCourse())) continue;
            boolean overlaps = false;
            for (Request otherRequest : enrollment.getRequest().getStudent().getRequests()) {
                if (otherRequest.equals(enrollment.getRequest()) || !(otherRequest instanceof CourseRequest))
                    continue;
                Enrollment otherErollment = assignment.getValue(otherRequest);
                if (otherErollment == null)
                    continue;
                if (enrl.isOverlapping(otherErollment)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                feasibleEnrollments.add(enrl);
                if (totalLimit >= 0) {
                    int limit = enrl.getLimit();
                    if (limit < 0) totalLimit = -1;
                    else totalLimit += limit;
                }
            }
        }
        double change = enrollment.getRequest().getWeight() / (totalLimit > 0 ? totalLimit : feasibleEnrollments.size());
        for (Enrollment feasibleEnrollment : feasibleEnrollments)
            for (Section section : feasibleEnrollment.getSections()) {
            	if (totalLimit > 0) {
                    section.setSpaceExpected(section.getSpaceExpected() + (increment ? +change : -change) * feasibleEnrollment.getLimit());
                } else {
                	section.setSpaceExpected(section.getSpaceExpected() + (increment ? +change : -change));
                }
            }
    }
	
	public void run() {
        sLog.info("Input: " + ToolBox.dict2string(model().getExtendedInfo(assignment()), 2));

        List<Student> students = new ArrayList<Student>(model().getStudents());
        String sort = System.getProperty("sort", "shuffle");
        if ("shuffle".equals(sort)) {
        	Collections.shuffle(students);
        } else if ("choice".equals(sort)) {
        	StudentChoiceOrder ord = new StudentChoiceOrder(model().getProperties()); ord.setReverse(false);
        	Collections.sort(students, ord);
        } else if ("referse".equals(sort)) {
        	StudentChoiceOrder ord = new StudentChoiceOrder(model().getProperties()); ord.setReverse(true);
        	Collections.sort(students, ord);
        }
        
        Iterator<Student> iterator = students.iterator();
        int nrThreads = Integer.parseInt(System.getProperty("nrConcurrent", "10"));
        List<Executor> executors = new ArrayList<InMemorySectioningTest.Executor>();
        for (int i = 0; i < nrThreads; i++) {
        	Executor executor = new Executor(iterator);
        	executor.start();
        	executors.add(executor);
        }

        long t0 = System.currentTimeMillis();
        while (iterator.hasNext()) {
        	try {
        		Thread.sleep(60000);
        	} catch (InterruptedException e) {}
        	long time = System.currentTimeMillis() - t0;
        	synchronized (iModel) {
        		sLog.info("Progress [" + (time / 60000) + "m]: " + ToolBox.dict2string(model().getExtendedInfo(assignment()), 2));	
			}
        }
        
        for (Executor executor: executors) {
        	try {
        		executor.join();
        	} catch (InterruptedException e) {}
        }
        
        sLog.info("Output: " + ToolBox.dict2string(model().getExtendedInfo(assignment()), 2));
        long time = System.currentTimeMillis() - t0;
        inc("[T] Run Time [m]", time / 60000.0);

	}
	
    public class Executor extends Thread {
		private Iterator<Student> iStudents = null;
		
		public Executor(Iterator<Student> students) {
			iStudents = students;
		}
		
		@Override
		public void run() {
			try {
				for (;;) {
					Student student = iStudents.next();
					int attempt = 1;
					while (!section(student)) {
						sLog.warn(attempt + ". attempt failed for " + student.getId());
						inc("[F] Failed attempt", attempt);
						attempt ++;
						if (attempt == 101) break;
						if (attempt > 10) {
							try {
								Thread.sleep(ToolBox.random(100 * attempt));
							} catch (InterruptedException e) {}
						}
					}
					if (attempt > 100)
						inc("[F] Failed enrollment (all 100 attempts)");
				}
			} catch (NoSuchElementException e) {}
		}
		
	}
	
	public class TestModel extends OnlineSectioningModel {
		public TestModel(DataProperties config) {
			super(config);
		}

		@Override
		public Map<String,String> getExtendedInfo(Assignment<Request, Enrollment> assignment) {
			Map<String, String> ret = super.getExtendedInfo(assignment);
			for (Map.Entry<String, Counter> e: iCounters.entrySet())
				ret.put(e.getKey(), e.getValue().toString());
			ret.put("Weighting model",
					(model().getProperties().getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
					(model().getProperties().getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal"));
			ret.put("B&B time limit", model().getProperties().getPropertyInt("Neighbour.BranchAndBoundTimeout", 1000) +" ms");
			if (iSuggestions) {
				ret.put("Suggestion time limit", model().getProperties().getPropertyInt("Suggestions.Timeout", 1000) +" ms");
			}
			return ret;
		}
	}
	
	public static class RequestSectionPair {
		private Request iRequest;
		private Section iSection;
		RequestSectionPair (Request request, Section section) {
			iRequest = request; iSection = section;
		}
		public Request getRequest() { return iRequest; }
		public Section getSection() { return iSection; }
	}
	
	private void stats(File input) throws IOException {
		File file = new File(input.getParentFile(), "stats.csv");
		DecimalFormat df = new DecimalFormat("0.0000");
		boolean ex = file.exists();
		PrintWriter pw = new PrintWriter(new FileWriter(file, true));
        if (!ex) {
        	pw.println("Input File,Run Time [m],Model,Sort,Over Expected,Not Assigned,Disb. Sections [%],Distance Confs.,Time Confs. [m],CPU Assignment [ms],Has Suggestions [%],Nbr Suggestions,Acceptance [%],CPU Suggestions [ms]");
        }
        pw.print(input.getName() + ",");
        pw.print(df.format(get("[T] Run Time [m]").sum()) + ",");
        pw.print(model().getProperties().getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria " : "");
        pw.print(model().getProperties().getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal");
        pw.print(iSuggestions ? " with suggestions": "");  pw.print(",");
        pw.print(System.getProperty("sort", "shuffle") + ",");
        pw.print("\"" + model().getOverExpectedCriterion() + "\",");
        
        pw.print(get("[A] Not Assigned").sum() + ",");
        pw.print(df.format(getPercDisbalancedSections(assignment(), 0.1)) + ",");
        pw.print(df.format(((double) model().getDistanceConflict().getTotalNrConflicts(assignment())) / model().getStudents().size()) + ",");
        pw.print(df.format(5.0 * model().getTimeOverlaps().getTotalNrConflicts(assignment()) / model().getStudents().size()) + ",");
        pw.print(df.format(get("[C] CPU Time").avg()) + ",");
        if (iSuggestions) {
        	pw.print(df.format(get("[S] Probability that a class has suggestions [%]").avg()) + ",");
        	pw.print(df.format(get("[S] Avg. # of suggestions").avg()) + ",");
        	pw.print(df.format(get("[S] Suggestion acceptance rate [%]").avg()) + ",");
        	pw.print(df.format(get("[S] Suggestion CPU Time").avg()));
        }
        pw.println();
        
        pw.flush();
        pw.close();
	}
	
	public static void main(String[] args) {
		try {
			System.setProperty("jprof", "cpu");
			BasicConfigurator.configure();
			
            DataProperties cfg = new DataProperties();
			cfg.setProperty("Neighbour.BranchAndBoundTimeout", "5000");
			cfg.setProperty("Suggestions.Timeout", "1000");
			cfg.setProperty("Extensions.Classes", DistanceConflict.class.getName() + ";" + TimeOverlapsCounter.class.getName());
			cfg.setProperty("StudentWeights.Class",  StudentSchedulingAssistantWeights.class.getName());
			cfg.setProperty("StudentWeights.PriorityWeighting", "true");
			cfg.setProperty("StudentWeights.LeftoverSpread", "true");
			cfg.setProperty("StudentWeights.BalancingFactor", "0.0");
			cfg.setProperty("Reservation.CanAssignOverTheLimit", "true");
			cfg.setProperty("Distances.Ellipsoid", DistanceMetric.Ellipsoid.WGS84.name());
			cfg.setProperty("StudentWeights.MultiCriteria", "true");
			cfg.setProperty("CourseRequest.SameTimePrecise", "true");
			
            cfg.setProperty("log4j.rootLogger", "INFO, A1");
            cfg.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            cfg.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            cfg.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            cfg.setProperty("log4j.logger.org.hibernate","INFO");
            cfg.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            cfg.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            cfg.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            cfg.setProperty("log4j.logger.net","INFO");
            
            cfg.setProperty("Xml.LoadBest", "false");
            
            cfg.putAll(System.getProperties());

            PropertyConfigurator.configure(cfg);

            final InMemorySectioningTest test = new InMemorySectioningTest(cfg);
            
            final File input = new File(args[0]);
            StudentSectioningXMLLoader loader = new StudentSectioningXMLLoader(test.model(), test.assignment());
            loader.setInputFile(input);
            loader.load();
            
            test.run();
    		
            Solver<Request, Enrollment> s = new Solver<Request, Enrollment>(cfg);
            s.setInitalSolution(test.model());
            StudentSectioningXMLSaver saver = new StudentSectioningXMLSaver(s);
            File output = new File(input.getParentFile(), input.getName().substring(0, input.getName().lastIndexOf('.')) + "-" + cfg.getProperty("run", "r0") + ".xml");
            saver.save(output);
            
            test.stats(input);
		} catch (Exception e) {
			sLog.error("Test failed: " + e.getMessage(), e);
		}
	}
}
