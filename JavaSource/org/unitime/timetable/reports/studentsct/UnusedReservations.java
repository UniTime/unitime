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
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.constraint.HardDistanceConflicts;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
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
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.LearningCommunityReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.cpsolver.studentsct.reservation.UniversalOverride;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
public class UnusedReservations extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    
    public UnusedReservations(StudentSectioningModel model) {
        super(model);
    }
    
    protected String type(Reservation reservation) {
    	if (reservation instanceof LearningCommunityReservation) {
    		return "lc";
    	} else if (reservation instanceof GroupReservation) {
    		return "group";
        } else if (reservation instanceof ReservationOverride) {
        	return "override";
        } else if (reservation instanceof IndividualReservation) {
        	return "individual";
        } else if (reservation instanceof CurriculumReservation) {
        	return "curriculum";
        } else if (reservation instanceof CourseReservation) {
        	return "course";
        } else if (reservation instanceof DummyReservation) {
        	return "dummy";
        } else if (reservation instanceof UniversalOverride) {
        	return "universal";
        } else {
        	return "other";
        }
    }
    
    protected String name(Reservation reservation) {
    	if (reservation instanceof LearningCommunityReservation) {
    		return MSG.reservationLearningCommunity();
    	} else if (reservation instanceof GroupReservation) {
    		return MSG.reservationGroup();
        } else if (reservation instanceof ReservationOverride) {
        	return MSG.reservationOverride();
        } else if (reservation instanceof IndividualReservation) {
        	return MSG.reservationIndividual();
        } else if (reservation instanceof CurriculumReservation) {
        	return MSG.reservationCurriculum();
        } else if (reservation instanceof CourseReservation) {
        	return MSG.reservationCourse();
        } else if (reservation instanceof DummyReservation) {
        	return MSG.reservationDummy();
        } else if (reservation instanceof UniversalOverride) {
        	return MSG.reservationUniversal();
        } else {
        	return MSG.reservationOther();
        }
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

	@Override
	public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
		Set<String> types = new HashSet<String>();
		for (String type: properties.getProperty("type", "group").split("\\,"))
			types.add(type);
		Map<Long, List<Reservation>> unused = new HashMap<Long, List<Reservation>>();
		for (Offering offering: getModel().getOfferings()) {
			if (offering.isDummy()) continue;
			for (Reservation reservation: offering.getReservations()) {
				if (!types.contains(type(reservation))) continue;
				Set<Long> studentIds = null;
				if (reservation instanceof IndividualReservation) {
					studentIds = new HashSet<Long>(((IndividualReservation)reservation).getStudentIds());
				} else if (reservation instanceof CourseReservation) {
					studentIds = new HashSet<Long>();
					for (CourseRequest cr: ((CourseReservation)reservation).getCourse().getRequests())
						if (matches(cr) && matches(offering.getCourse(cr.getStudent())))
							studentIds.add(cr.getStudent().getId());
				} else {
					studentIds = new HashSet<Long>();
					for (Course course: reservation.getOffering().getCourses())
						for (CourseRequest cr: course.getRequests())
							if (reservation.isApplicable(cr.getStudent()) && matches(cr) && matches(offering.getCourse(cr.getStudent())))
								studentIds.add(cr.getStudent().getId());
				}
				if (studentIds != null && !studentIds.isEmpty()) {
					for (Enrollment e: reservation.getEnrollments(assignment))
						studentIds.remove(e.getStudent().getId());
					for (Long studentId: studentIds) {
						List<Reservation> studentReservations = unused.get(studentId);
						if (studentReservations == null) {
							studentReservations = new ArrayList<Reservation>();
							unused.put(studentId, studentReservations);
						}
						studentReservations.add(reservation);
					}
				}
			}
		}
		CSVFile csv = new CSVFile();
		if (types.contains("universal"))
			csv.setHeader(new CSVFile.CSVField[] {
					new CSVFile.CSVField("__Student"),
	        		new CSVFile.CSVField(MSG.reportStudentId()),
	        		new CSVFile.CSVField(MSG.reportStudentName()),
	        		new CSVFile.CSVField(MSG.reportStudentEmail()),
	        		new CSVFile.CSVField(MSG.reportStudentPriority()),
	        		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
	        		new CSVFile.CSVField(MSG.reportStudentGroup()),
	        		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
	        		new CSVFile.CSVField(MSG.reportRequestedCourse()),
	        		new CSVFile.CSVField(MSG.reportCourseRequestPriority()),
	        		new CSVFile.CSVField(MSG.reportAssignmentConflict()),
	        		new CSVFile.CSVField(MSG.reportUniversalReservationStudentFilter())
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
	        		new CSVFile.CSVField(MSG.reportRequestedCourse()),
	        		new CSVFile.CSVField(MSG.reportCourseRequestPriority()),
	        		new CSVFile.CSVField(MSG.reportAssignmentConflict())
	                });


		
		if (unused.isEmpty()) return csv;
		for (Student student: getModel().getStudents()) {
			List<Reservation> studentReservations = unused.get(student.getId());
			if (studentReservations == null || studentReservations.isEmpty()) continue;
			for (Reservation reservation: studentReservations) {
				if (!matches(student)) continue;
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
	            CourseRequest courseRequest = null;
	            Course course = null;
	            requests: for (Request r: student.getRequests()) {
	            	if (r instanceof CourseRequest) {
	            		CourseRequest cr = (CourseRequest)r;
	            		for (Course c: cr.getCourses()) {
	            			if (reservation.getOffering().equals(c.getOffering())) {
	            				courseRequest = cr; course = c; break requests;
	            			}
	            		}
	            	}
	            }
	            if (course == null) {
	            	Course c = (reservation instanceof CourseReservation ? ((CourseReservation)reservation).getCourse() : reservation.getOffering().getCourses().get(0));
	            	if (!matches(c)) continue;
	            	line.add(new CSVFile.CSVField(reservation instanceof CourseReservation ? ((CourseReservation)reservation).getCourse().getName() : reservation.getOffering().getName()));
	            	line.add(new CSVFile.CSVField(""));
	            	line.add(new CSVFile.CSVField(MSG.courseNotRequested()));
	            } else {
	            	if (!matches(courseRequest)) continue;
	            	line.add(new CSVFile.CSVField(course.getName()));
	            	line.add(new CSVFile.CSVField(courseRequest.getRequestPriority() == null ? "" : courseRequest.getRequestPriority().name()));
		        	Enrollment enrollment = courseRequest.getAssignment(assignment);
		        	if (enrollment != null && reservation.isIncluded(enrollment)) continue;
		            TreeSet<Enrollment> overlaps = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
						@Override
						public int compare(Enrollment o1, Enrollment o2) {
							return o1.getRequest().compareTo(o2.getRequest());
						}
					});
		            TreeSet<String> other = new TreeSet<String>();
					Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
					List<Enrollment> av = courseRequest.getAvaiableEnrollmentsSkipSameTime(assignment);
					if (enrollment != null) {
						if (enrollment.getOffering().equals(reservation.getOffering()))
							line.add(new CSVFile.CSVField(MSG.reservationNotUsed()));
						else
							line.add(new CSVFile.CSVField(MSG.enrolledInAlt(enrollment.getCourse().getName())));
					} else if (av.isEmpty() || (av.size() == 1 && av.get(0).equals(courseRequest.getInitialAssignment()) && getModel().inConflict(assignment, av.get(0)))) {
						if (courseRequest.getCourses().get(0).getLimit() >= 0)
							line.add(new CSVFile.CSVField(MSG.courseIsFull()));
						else if (SectioningRequest.hasInconsistentRequirements(courseRequest, null))
							line.add(new CSVFile.CSVField(MSG.classNotAvailableDueToStudentPrefs()));
						else
							line.add(new CSVFile.CSVField(MSG.classNotAvailable()));
					} else {
						for (Iterator<Enrollment> e = av.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							for (Request q: enrl.getStudent().getRequests()) {
								if (q.equals(courseRequest)) continue;
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
						        unavailabilities: for (Unavailability unavailability: student.getUnavailabilities()) {
									for (SctAssignment section: enrl.getAssignments()) {
										if (HardDistanceConflicts.inConflict(getModel().getStudentQuality(), (Section)section, unavailability)) {
											other.add(unavailability.getCourseName() + " " + unavailability.getSectionName());
											continue unavailabilities;
										}
									}
								}
							}
						}
						unavailabilities: for (Unavailability unavailability: student.getUnavailabilities()) {
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
							line.add(new CSVFile.CSVField(MSG.courseNotAssigned()));
						}
					}
	            }
	            if (reservation instanceof UniversalOverride)
	            	line.add(new CSVFile.CSVField(((UniversalOverride)reservation).getFilter()));
	            csv.addLine(line);
			}
		}
		return csv;
	}

}
