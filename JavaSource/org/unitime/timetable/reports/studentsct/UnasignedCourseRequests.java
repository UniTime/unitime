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
package org.unitime.timetable.reports.studentsct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.constraint.ConfigLimit;
import org.cpsolver.studentsct.constraint.CourseLimit;
import org.cpsolver.studentsct.constraint.HardDistanceConflicts;
import org.cpsolver.studentsct.constraint.SectionLimit;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.cpsolver.studentsct.reservation.Reservation;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
public class UnasignedCourseRequests extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    
    public UnasignedCourseRequests(StudentSectioningModel model) {
        super(model);
    }

    protected String curriculum(Student student) {
        String curriculum = "";
        for (AreaClassificationMajor acm: student.getAreaClassificationMajors())
                curriculum += (curriculum.isEmpty() ? "" : ", ") + acm.toString();
        return curriculum;
    }
    
    protected String group(Student student) {
    	String group = "";
    	for (StudentGroup aac: student.getGroups())
    		group += (group.isEmpty() ? "" : ", ") + aac.getReference();
    	return group;    	
    }
    
    protected String advisor(Student student) {
        String advisors = "";
        for (Instructor instructor: student.getAdvisors())
        	advisors += (advisors.isEmpty() ? "" : ", ") + instructor.getName();
        return advisors;
    }
    
    /**
     * Return minimum of two limits where -1 counts as unlimited (any limit is smaller)
     */
    private static int min(int l1, int l2) {
        return (l1 < 0 ? l2 : l2 < 0 ? l1 : Math.min(l1, l2));
    }
    
    /**
     * Add two limits where -1 counts as unlimited (unlimited plus anything is unlimited)
     */
    private static int add(int l1, int l2) {
        return (l1 < 0 ? -1 : l2 < 0 ? -1 : l1 + l2);
    }
    
    /**
     * Compute offering limit excluding cancelled and/or disabled sections
     */
    public static int getOfferingLimit(Offering offering, boolean skipCancelled, boolean skipDisabled) {
    	int offeringLimit = 0;
    	for (Config config: offering.getConfigs()) {
    		Integer configLimit = null;
    		for (Subpart subpart: config.getSubparts()) {
    			int subpartLimit = 0;
    			for (Section section: subpart.getSections()) {
    				if (skipCancelled && section.isCancelled()) continue;
    				if (skipDisabled && !section.isEnabled()) continue;
    				subpartLimit = add(subpartLimit, section.getLimit());
    			}
    			if (configLimit == null)
    				configLimit = subpartLimit;
    			else
    				configLimit = min(configLimit, subpartLimit);
    		}
    		if (configLimit != null)
    			offeringLimit = add(offeringLimit, min(configLimit, config.getLimit()));
    	}
    	return offeringLimit;
    }
    
    /**
     * Compute offering limit excluding cancelled and/or disabled sections
     */
    public static double getOfferingEnrollment(Offering offering, Assignment<Request, Enrollment> assignment, boolean skipCancelled, boolean skipDisabled) {
    	double enrollment = 0;
    	for (Config config: offering.getConfigs()) {
    		e: for (Enrollment e: config.getContext(assignment).getEnrollments()) {
    			if (skipCancelled)
    				for (Section s: e.getSections())
    					if (s.isCancelled()) continue e;
    			if (skipDisabled)
    				for (Section s: e.getSections())
    					if (!s.isEnabled()) continue e;
    			enrollment += e.getRequest().getWeight();
    		}
    	}
    	return enrollment;
    }
    
    public static void computeNoAvailableReasons(CourseRequest courseRequest, Assignment<Request, Enrollment> assignment, Set<String> reasons, Course course, Config config, HashSet<Section> sections, int idx) {
        if (idx == 0) { // run only once for each configuration
            if (courseRequest.isNotAllowed(course, config)) {
            	reasons.add(MSG.unavailableConfigNotAllowedDueToRestrictions(config.getInstructionalMethodName() != null ? config.getInstructionalMethodName() : config.getName()));
            	return;
            }
            boolean canOverLimit = false;
            for (Reservation r: courseRequest.getReservations(course)) {
                if (!r.canBatchAssignOverLimit()) continue;
                if (r.neverIncluded()) continue;
                if (!r.getConfigs().isEmpty() && !r.getConfigs().contains(config)) continue;
                if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                canOverLimit = true; break;
            }
            if (!canOverLimit) {
                if (config.getOffering().hasReservations()) {
                    boolean hasReservation = false, hasConfigReservation = false, reservationMustBeUsed = false;
                    for (Reservation r: courseRequest.getReservations(course)) {
                        if (r.mustBeUsed()) reservationMustBeUsed = true;
                        if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                        if (r.neverIncluded()) {
                        } else if (r.getConfigs().isEmpty()) {
                            hasReservation = true;
                        } else if (r.getConfigs().contains(config)) {
                            hasReservation = true;
                            hasConfigReservation = true;
                        } else if (!r.areRestrictionsInclusive()) {
                            hasReservation = true;
                        }
                    }
                    if (!hasReservation && reservationMustBeUsed) {
                    	reasons.add(MSG.unavailableMustUseReservationIsFull());
                        return;
                    }
                    if (!hasReservation && config.getOffering().getUnreservedSpace(assignment, courseRequest) < courseRequest.getWeight()) {
                    	reasons.add(MSG.unavailableCourseIsReserved(course.getName()));
                        return;
                    }
                    if (!hasConfigReservation && config.getUnreservedSpace(assignment, courseRequest) < courseRequest.getWeight()) {
                    	reasons.add(MSG.unavailableConfigIsReserved(config.getName()));
                        return;
                    }
                }
                if (config.getLimit() >= 0 && ConfigLimit.getEnrollmentWeight(assignment, config, courseRequest) > config.getLimit()) {
                	reasons.add(MSG.unavailableConfigIsFull(config.getName()));
                    return;
                }
            }
        }
        if (config.getSubparts().size() == idx) {
            Enrollment e = new Enrollment(courseRequest, 0, course, config, new HashSet<SctAssignment>(sections), null);
            if (courseRequest.isNotAllowed(e)) {
            	reasons.add(MSG.unavailableNotAllowed());
            } else if (config.getOffering().hasReservations()) {
                boolean mustHaveReservation = config.getOffering().getTotalUnreservedSpace() < courseRequest.getWeight();
                boolean mustHaveConfigReservation = config.getTotalUnreservedSpace() < courseRequest.getWeight();
                boolean mustHaveSectionReservation = false;
                boolean containDisabledSection = false;
                for (Section s: sections) {
                    if (s.getTotalUnreservedSpace() < courseRequest.getWeight()) {
                        mustHaveSectionReservation = true;
                    }
                    if (!courseRequest.getStudent().isAllowDisabled() && !s.isEnabled(courseRequest.getStudent())) {
                        containDisabledSection = true;
                    }
                }
                boolean canOverLimit = false;
                for (Reservation r: courseRequest.getReservations(course)) {
                    if (!r.canBatchAssignOverLimit() || !r.isIncluded(e)) continue;
                    if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                    if (containDisabledSection && !r.isAllowDisabled()) continue;
                    canOverLimit = true;
                }
                if (!canOverLimit) {
                    boolean reservationMustBeUsed = false;
                    reservations: for (Reservation r: courseRequest.getSortedReservations(assignment, course)) {
                        if (r.mustBeUsed()) reservationMustBeUsed = true;
                        if (!r.isIncluded(e)) continue;
                        if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                        if (mustHaveConfigReservation && r.getConfigs().isEmpty()) continue;
                        if (mustHaveSectionReservation)
                            for (Section s: sections)
                                if (r.getSections(s.getSubpart()) == null && s.getTotalUnreservedSpace() < courseRequest.getWeight()) continue reservations;
                        if (containDisabledSection && !r.isAllowDisabled()) continue;
                        return;
                    }
                    // a case w/o reservation
                    if (!(mustHaveReservation || mustHaveConfigReservation || mustHaveSectionReservation) &&
                        !(config.getOffering().getUnreservedSpace(assignment, courseRequest) < courseRequest.getWeight()) &&
                        !reservationMustBeUsed && !containDisabledSection) {
                        return;
                    }
                    reasons.add(MSG.unavailableDueToReservation());
                }
            }
        } else {
            Subpart subpart = config.getSubparts().get(idx);
            List<Section> sectionsThisSubpart = subpart.getSections();
            List<Section> matchingSectionsThisSubpart = new ArrayList<Section>(subpart.getSections().size());
            for (Section section : sectionsThisSubpart) {
                if (section.getParent() != null && !sections.contains(section.getParent()))
                    continue;

                boolean canOverLimit = false;
                for (Reservation r: courseRequest.getReservations(course)) {
                    if (!r.canBatchAssignOverLimit()) continue;
                    if (r.getSections(subpart) != null && !r.getSections(subpart).contains(section)) continue;
                    if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                    canOverLimit = true; break;
                }
                if (!canOverLimit) {
                    if (section.getLimit() >= 0 && SectionLimit.getEnrollmentWeight(assignment, section, courseRequest) > section.getLimit())
                        continue;
                    if (section.isCancelled()) {
                    	reasons.add(MSG.unavailableSectionCancelled(section.getName(course.getId())));
                        continue;
                    }
                    if (section.isOverlapping(sections)) {
                    	for (Section a : sections) {
                            if (a.isAllowOverlap()) continue;
                            if (a.getTime() == null) continue;
                            if (section.isToIgnoreStudentConflictsWith(a.getId())) continue;
                            if (section.getTime().hasIntersection(a.getTime()))
                            	reasons.add(MSG.unavailableSectionConflict(section.getName(course.getId()), a.getName(course.getId())));    	
                        }
                        continue;
                    }
                    if (config.getOffering().hasReservations()) {
                        boolean hasReservation = false, hasSectionReservation = false, reservationMustBeUsed = false;
                        for (Reservation r: courseRequest.getReservations(course)) {
                            if (r.mustBeUsed()) reservationMustBeUsed = true;
                            if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                            if (r.getSections(subpart) == null) {
                                hasReservation = true;
                            } else if (r.getSections(subpart).contains(section)) {
                                hasReservation = true;
                                hasSectionReservation = true;
                            }
                        }
                        if (!hasSectionReservation && section.getUnreservedSpace(assignment, courseRequest) < courseRequest.getWeight()) {
                        	reasons.add(MSG.unavailableSectionReserved(section.getName(course.getId())));
                            continue;
                        }
                        if (!hasReservation && reservationMustBeUsed) {
                        	reasons.add(MSG.unavailableDueToMustTakeReservation(section.getName(course.getId())));
                            continue;
                        }
                    }
                } else {
                    if (section.isCancelled()) {
                    	reasons.add(MSG.unavailableSectionCancelled(section.getName(course.getId())));
                        continue;
                    }
                    if (section.isOverlapping(sections)) {
                    	for (Section a : sections) {
                            if (a.isAllowOverlap()) continue;
                            if (a.getTime() == null) continue;
                            if (section.isToIgnoreStudentConflictsWith(a.getId())) continue;
                            if (section.getTime().hasIntersection(a.getTime()))
                            	reasons.add(MSG.unavailableSectionConflict(section.getName(course.getId()), a.getName(course.getId())));    	
                        }
                        continue;
                    }
                }

                if (courseRequest.getInitialAssignment() != null && (courseRequest.getModel() != null && ((StudentSectioningModel)courseRequest.getModel()).getKeepInitialAssignments()) &&
                        !courseRequest.getInitialAssignment().getAssignments().contains(section)) {
                	reasons.add(MSG.unavailableNotInitial());
                    continue;
                }
                if (courseRequest.isFixed() && !courseRequest.getFixedValue().getAssignments().contains(section)) {
                	reasons.add(MSG.unavailableNotFixed());
                    continue;
                }

                if (!courseRequest.isRequired(section)) {
                	reasons.add(MSG.unavailableStudentPrefs(section.getName(course.getId())));
                    continue;
                }
                if (courseRequest.isNotAllowed(course, section)) {
                	reasons.add(MSG.unavailableStudentRestrictions(section.getName(course.getId())));
                	continue;
                }
                if (!courseRequest.getStudent().isAvailable(section)) {
                    boolean canOverlap = false;
                    for (Reservation r: courseRequest.getReservations(course)) {
                        if (!r.isAllowOverlap()) continue;
                        if (r.getSections(subpart) != null && !r.getSections(subpart).contains(section)) continue;
                        if (r.getReservedAvailableSpace(assignment, config, courseRequest) < courseRequest.getWeight()) continue;
                        canOverlap = true; break;
                    }
                    if (!canOverlap) {
                    	reasons.add(MSG.unavailableStudentUnavailabilities(section.getName(course.getId())));
                    	continue;
                    }
                }

                if (!courseRequest.getStudent().isAllowDisabled() && !section.isEnabled(courseRequest.getStudent())) {
                    boolean allowDisabled = false;
                    for (Reservation r: courseRequest.getReservations(course)) {
                        if (!r.isAllowDisabled()) continue;
                        if (r.getSections(subpart) != null && !r.getSections(subpart).contains(section)) continue;
                        if (!r.getConfigs().isEmpty() && !r.getConfigs().contains(config)) continue;
                        allowDisabled = true; break;
                    }
                    if (!allowDisabled) {
                    	reasons.add(MSG.unavailableSectionDisabled(section.getName(course.getId())));
                    	continue;
                    }
                }
                matchingSectionsThisSubpart.add(section);
            }
            for (Section section: matchingSectionsThisSubpart) {
                sections.add(section);
                computeNoAvailableReasons(courseRequest, assignment, reasons, course, config, sections, idx + 1);
                sections.remove(section);
            }
        }
    }
    		
    
    public static String getNoAvailableMessage(CourseRequest courseRequest, Assignment<Request, Enrollment> assignment) {
    	Course course = courseRequest.getCourses().get(0);
    	Offering offering = course.getOffering();
    	
    	int limit = getOfferingLimit(offering, false, false);
    	double enrollment = getOfferingEnrollment(offering, assignment, false, false);
    	if (limit >= 0 && limit < courseRequest.getWeight() + enrollment)
    		return MSG.unavailableCourseIsFull(course.getName());
    	
    	if (course.getLimit() >= 0 && CourseLimit.getEnrollmentWeight(assignment, course, courseRequest) > course.getLimit())
    		return MSG.unavailableCourseIsFull(course.getName());
    	
    	boolean reservationMustBeUsed = false, hasReservation = false;
    	for (Reservation r: courseRequest.getSortedReservations(assignment, course)) {
            if (r.mustBeUsed()) reservationMustBeUsed = true;
            if (r.getReservedAvailableSpace(assignment, courseRequest) < courseRequest.getWeight()) continue;
            hasReservation = true;
        }
        if (reservationMustBeUsed && !hasReservation)
        	return MSG.unavailableMustTakeReservationIsFull();

    	Set<String> reasons = new TreeSet<String>();
    	for (Config config : offering.getConfigs()) {
    		computeNoAvailableReasons(courseRequest, assignment, reasons, course, config, new HashSet<Section>(), 0);
        }
    	if (!reasons.isEmpty()) {
    		String ret = "";
    		int count = 0;
    		for (String reason: reasons) {
    			if (count == 10) { return ret + "\n..."; } 
    			ret += (ret.isEmpty() ? "" : "\n") + reason;
    			count++;
    		}
    		return ret;
    	}
    	
    	return null;
    }
	
    @Override
    public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
		Set<String> types = new HashSet<String>();
		for (String type: properties.getProperty("type", "").split("\\,"))
			if (!type.isEmpty())
				types.add(type);		
		boolean skipFull = properties.getPropertyBoolean("skipFull", false);
		CSVFile csv = new CSVFile();
		if (types.size() != 1)
			csv.setHeader(new CSVFile.CSVField[] {
					new CSVFile.CSVField("__Student"),
	        		new CSVFile.CSVField(MSG.reportStudentId()),
	        		new CSVFile.CSVField(MSG.reportStudentName()),
	        		new CSVFile.CSVField(MSG.reportStudentEmail()),
	        		new CSVFile.CSVField(MSG.reportStudentPriority()),
	        		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
	        		new CSVFile.CSVField(MSG.reportStudentGroup()),
	        		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
	        		new CSVFile.CSVField(MSG.reportUnassignedCourse()),
	        		new CSVFile.CSVField(MSG.reportCourseRequestPriority()),
	        		new CSVFile.CSVField(MSG.reportAssignmentConflict()),
	        		new CSVFile.CSVField(MSG.reportConflictingCourseRequestPriority()),
	                });
		else
			csv.setHeader(new CSVFile.CSVField[] {
					new CSVFile.CSVField("__Student"),
	        		new CSVFile.CSVField(MSG.reportStudentId()),
	        		new CSVFile.CSVField(MSG.reportStudentName()),
	        		new CSVFile.CSVField(MSG.reportStudentEmail()),
	        		new CSVFile.CSVField(MSG.reportStudentPriority()),
	        		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
	        		new CSVFile.CSVField(MSG.reportStudentGroup()),
	        		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
	        		new CSVFile.CSVField(MSG.reportUnassignedCourse()),
	        		new CSVFile.CSVField(MSG.reportAssignmentConflict()),
	        		new CSVFile.CSVField(MSG.reportConflictingCourseRequestPriority()),
	                });
		for (Student student: getModel().getStudents()) {
        	for (Request request: student.getRequests()) {
        		if (request instanceof FreeTimeRequest) continue;
        		Enrollment enrollment = assignment.getValue(request);
        		if (enrollment != null || !student.canAssign(assignment, request)) continue;
        		if (!matches(request, enrollment)) continue;
        		CourseRequest courseRequest = (CourseRequest)request;
        		if (!types.isEmpty() && (courseRequest.getRequestPriority() == null || !types.contains(courseRequest.getRequestPriority().name()))) continue;
        		List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
        		line.add(new CSVFile.CSVField(student.getId()));
        		line.add(new CSVFile.CSVField(student.getExternalId()));
	            line.add(new CSVFile.CSVField(student.getName()));
	            org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(student.getId());
	            if (dbStudent != null)
	            	line.add(new CSVFile.CSVField(dbStudent.getEmail()));
	            else
	            	line.add(new CSVFile.CSVField(""));
	            line.add(new CSVFile.CSVField(student.getPriority() == null ? "" : student.getPriority().name()));
	            line.add(new CSVFile.CSVField(curriculum(student)));
	            line.add(new CSVFile.CSVField(group(student)));
	            line.add(new CSVFile.CSVField(advisor(student)));
	            line.add(new CSVFile.CSVField(courseRequest.getCourses().get(0).getName()));
	            if (types.size() != 1)
	            	line.add(new CSVFile.CSVField(courseRequest.getRequestPriority() == null ? "" : courseRequest.getRequestPriority().name()));
	            
				TreeSet<Enrollment> overlaps = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
					@Override
					public int compare(Enrollment o1, Enrollment o2) {
						return o1.getRequest().compareTo(o2.getRequest());
					}
				});
				TreeSet<String> other = new TreeSet<String>();
				Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
				List<Enrollment> av = courseRequest.getAvaiableEnrollmentsSkipSameTime(assignment);
				RequestPriority conflictPriority = null;
				if (av.isEmpty() || (av.size() == 1 && av.get(0).equals(courseRequest.getInitialAssignment()) && getModel().inConflict(assignment, av.get(0)))) {
					if (skipFull) continue;
					String message = getNoAvailableMessage(courseRequest, assignment);
					if (message == null)
						message = MSG.classNotAvailable();
					line.add(new CSVFile.CSVField(message));
				} else {
					for (Iterator<Enrollment> e = av.iterator(); e.hasNext();) {
						Enrollment enrl = e.next();
						for (Request q: enrl.getStudent().getRequests()) {
							if (q.equals(request)) continue;
							Enrollment x = assignment.getValue(q);
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
					        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
					        	SctAssignment a = i.next();
								if (a.isOverlapping(enrl.getAssignments()) || HardDistanceConflicts.inConflict(getModel().getStudentQuality(), a, enrl)) {
									overlaps.add(x);
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
								if (HardDistanceConflicts.inConflict(getModel().getStudentQuality(), (Section)section, unavailability)) {
									other.add(unavailability.getCourseName() + " " + unavailability.getSectionName());
									continue unavailabilities;
								}
							}
						}
					}
					unavailabilities: for (Unavailability unavailability: student.getUnavailabilities()) {
						for (Course course: courseRequest.getCourses())
							for (Config config: course.getOffering().getConfigs())
								for (Subpart subpart: config.getSubparts())
									for (Section section: subpart.getSections()) {
										if (section.getLimit() > 0 && unavailability.isOverlapping(section)) {
											other.add(unavailability.getCourseName() + " " + unavailability.getSectionName());
											continue unavailabilities;
										}
									}
					}
					if (!overlaps.isEmpty() || !other.isEmpty()) {
						TreeSet<String> ts = new TreeSet<String>(other);
						for (Enrollment q: overlaps) {
							if (q.getRequest() instanceof FreeTimeRequest) {
								ts.add(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
							} else {
								CourseRequest cr = (CourseRequest)q.getRequest();
								Course o = q.getCourse();
								String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
								if (overlapingSections.get(cr).size() == 1)
									for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
										Section s = i.next();
										ov += " " + s.getSubpart().getName();
										if (i.hasNext()) ov += ",";
									}
								ts.add(ov);
								if (cr.getRequestPriority() != null && (conflictPriority == null || conflictPriority.ordinal() > cr.getRequestPriority().ordinal()))
									conflictPriority = cr.getRequestPriority();
							}
						}
						String message = "";
						for (Iterator<String> i = ts.iterator(); i.hasNext();) {
							String x = i.next();
							if (message.isEmpty())
								message += MSG.conflictWithFirst(x);
							else if (!i.hasNext())
								message += MSG.conflictWithLast(x);
							else
								message += MSG.conflictWithMiddle(x);
						}
						line.add(new CSVFile.CSVField(message));
					} else {
						if (skipFull) continue;
						line.add(new CSVFile.CSVField(MSG.courseNotAssigned()));
					}
				}
				line.add(new CSVFile.CSVField(conflictPriority == null ? "" : conflictPriority.name()));
        		csv.addLine(line);
        	}
		}
		
		return csv;
	}

}
