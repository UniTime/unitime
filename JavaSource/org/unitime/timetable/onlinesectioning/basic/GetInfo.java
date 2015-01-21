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
package org.unitime.timetable.onlinesectioning.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
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
import org.cpsolver.studentsct.online.selection.StudentSchedulingAssistantWeights;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.match.AnyCourseMatcher;
import org.unitime.timetable.onlinesectioning.match.AnyStudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseReservation;
import org.unitime.timetable.onlinesectioning.model.XCurriculumReservation;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XGroupReservation;
import org.unitime.timetable.onlinesectioning.model.XIndividualReservation;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XSubpart;

/**
 * @author Tomas Muller
 */
public class GetInfo implements OnlineSectioningAction<Map<String, String>>{
	private static final long serialVersionUID = 1L;

	@Override
	public Map<String, String> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Map<String, String> info = new HashMap<String, String>();
		Lock lock = server.readLock();
		try {
			
			Map<Long, Offering> offerings = new HashMap<Long, Offering>();
    		Hashtable<Long, Course> courses = new Hashtable<Long, Course>();
    		Map<String, List<GroupReservation>> groups = new HashMap<String, List<GroupReservation>>();
    		Hashtable<Long, Config> configs = new Hashtable<Long, Config>();
    		Hashtable<Long, Subpart> subparts = new Hashtable<Long, Subpart>();
    		Hashtable<Long, Section> sections = new Hashtable<Long, Section>();
    		Hashtable<Long, Reservation> reservations = new Hashtable<Long, Reservation>();

    		for (XCourseId ci: server.findCourses(new AnyCourseMatcher())) {
	        	XOffering offering = server.getOffering(ci.getOfferingId());
	        	if (offering == null || offerings.containsKey(offering.getOfferingId())) continue;
        		Offering clonedOffering = new Offering(offering.getOfferingId(), offering.getName());
        		Long courseId = null;
        		for (XCourse course: offering.getCourses()) {
        			Course clonedCourse = new Course(course.getCourseId(), course.getSubjectArea(), course.getCourseNumber(), clonedOffering, course.getLimit(), course.getProjected());
	        		clonedCourse.setNote(course.getNote());
	        		courses.put(course.getCourseId(), clonedCourse);
	        		if (offering.getName().equals(course.getCourseName())) courseId = course.getCourseId();
        		}
        		for (XConfig config: offering.getConfigs()) {
        			Config clonedConfig = new Config(config.getConfigId(), config.getLimit(), config.getName(), clonedOffering);
        			configs.put(config.getConfigId(), clonedConfig);
        			for (XSubpart subpart: config.getSubparts()) {
        				Subpart clonedSubpart = new Subpart(subpart.getSubpartId(), subpart.getInstructionalType(), subpart.getName(), clonedConfig,
        						(subpart.getParentId() == null ? null: subparts.get(subpart.getParentId())));
        				clonedSubpart.setAllowOverlap(subpart.isAllowOverlap());
        				clonedSubpart.setCredit(subpart.getCredit(courseId));
        				subparts.put(subpart.getSubpartId(), clonedSubpart);
        				for (XSection section: subpart.getSections()) {
        					Section clonedSection = new Section(section.getSectionId(), section.getLimit(),
        							section.getName(), clonedSubpart, section.toPlacement(),
        							section.getInstructorIds(), section.getInstructorNames(),
        							(section.getParentId() == null ? null : sections.get(section.getParentId())));
        					clonedSection.setName(-1l, section.getName(-1l));
        					clonedSection.setNote(section.getNote());
        					for (XDistribution distribution: offering.getDistributions()) {
        						if (distribution.getDistributionType() == XDistributionType.IngoreConflicts && distribution.hasSection(section.getSectionId()))
        							for (Long id: distribution.getSectionIds())
        								if (!id.equals(section.getSectionId())) clonedSection.addIgnoreConflictWith(id);
        					}
        					sections.put(section.getSectionId(), clonedSection);
        				}
        			}
        		}
        		
        		for (XReservation reservation: offering.getReservations()) {
        			Reservation clonedReservation = null;
        			switch (reservation.getType()) {
        			case Course:
        				XCourseReservation courseR = (XCourseReservation) reservation;
        				clonedReservation = new CourseReservation(reservation.getReservationId(), courses.get(courseR.getCourseId()));
        				break;
        			case Curriculum:
        				XCurriculumReservation curriculumR = (XCurriculumReservation) reservation;
        				clonedReservation = new CurriculumReservation(reservation.getReservationId(), reservation.getLimit(), clonedOffering, curriculumR.getAcademicArea(), curriculumR.getClassifications(), curriculumR.getMajors());
        				break;
        			case Group:
        				if (reservation instanceof XIndividualReservation) {
            				XIndividualReservation indR = (XIndividualReservation) reservation;
            				clonedReservation = new GroupReservation(reservation.getReservationId(), reservation.getLimit(), clonedOffering, indR.getStudentIds());
        				} else {
            				XGroupReservation groupR = (XGroupReservation) reservation;
            				clonedReservation = new GroupReservation(reservation.getReservationId(), reservation.getLimit(), clonedOffering);
            				List<GroupReservation> list = groups.get(groupR.getGroup());
            				if (list == null) {
            					list = new ArrayList<GroupReservation>();
            					groups.put(groupR.getGroup(), list);
            				}
            				list.add((GroupReservation)clonedReservation);
        				}
        				break;
        			case Individual:
        				XIndividualReservation indR = (XIndividualReservation) reservation;
        				clonedReservation = new IndividualReservation(reservation.getReservationId(), clonedOffering, indR.getStudentIds());
        				break;
        			case Override:
        				XIndividualReservation ovrR = (XIndividualReservation) reservation;
        				clonedReservation = new ReservationOverride(reservation.getReservationId(), clonedOffering, ovrR.getStudentIds());
        				((ReservationOverride)clonedReservation).setMustBeUsed(ovrR.mustBeUsed());
        				((ReservationOverride)clonedReservation).setAllowOverlap(ovrR.isAllowOverlap());
        				((ReservationOverride)clonedReservation).setCanAssignOverLimit(ovrR.canAssignOverLimit());
        				break;
        			default:
        				clonedReservation = new DummyReservation(clonedOffering);
        			}
        			for (Long configId: reservation.getConfigsIds())
        				clonedReservation.addConfig(configs.get(configId));
        			for (Map.Entry<Long, Set<Long>> entry: reservation.getSections().entrySet()) {
        				Set<Section> clonedSections = new HashSet<Section>();
        				for (Long sectionId: entry.getValue())
        					clonedSections.add(sections.get(sectionId));
        				clonedReservation.getSections().put(subparts.get(entry.getKey()), clonedSections);
        			}
        			reservations.put(reservation.getReservationId(), clonedReservation);
        		}
        		
        		offerings.put(offering.getOfferingId(), clonedOffering);
        	}
	        
	        Map<Long, Student> students = new HashMap<Long, Student>();
	        Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
			for (XStudentId id: server.findStudents(new AnyStudentMatcher())) {
				XStudent student = (id instanceof XStudent ? (XStudent)id : server.getStudent(id.getStudentId()));
				if (student == null) return null;
				Student clonnedStudent = new Student(student.getStudentId());
				for (String g: student.getGroups()) {
					List<GroupReservation> list = groups.get(g);
					if (list != null)
						for (GroupReservation gr: list)
							gr.getStudentIds().add(student.getStudentId());
				}
				for (XRequest r: student.getRequests()) {
					if (r instanceof XFreeTimeRequest) {
						XFreeTimeRequest ft = (XFreeTimeRequest)r;
						new FreeTimeRequest(r.getRequestId(), r.getPriority(), r.isAlternative(), clonnedStudent,
								new TimeLocation(ft.getTime().getDays(), ft.getTime().getSlot(), ft.getTime().getLength(), 0, 0.0,
										-1l, "Free Time", server.getAcademicSession().getFreeTimePattern(), 0));
					} else {
						XCourseRequest cr = (XCourseRequest)r;
						List<Course> req = new ArrayList<Course>();
						for (XCourseId c: cr.getCourseIds()) {
							Course course = courses.get(c.getCourseId());
							if (course != null) req.add(course);
						}
						if (!req.isEmpty()) {
							CourseRequest clonnedRequest = new CourseRequest(r.getRequestId(), r.getPriority(), r.isAlternative(), clonnedStudent, req, cr.isWaitlist(), cr.getTimeStamp() == null ? null : cr.getTimeStamp().getTime());
							XEnrollment enrollment = cr.getEnrollment();
							if (enrollment != null) {
								Config config = configs.get(enrollment.getConfigId());
								Set<Section> assignments = new HashSet<Section>();
								for (Long sectionId: enrollment.getSectionIds()) {
									Section section = sections.get(sectionId);
									if (section != null) assignments.add(section);
								}
								Reservation reservation = (enrollment.getReservation() == null ? null : reservations.get(enrollment.getReservation().getReservationId()));
								if (config != null && !sections.isEmpty())
									assignment.assign(0, new Enrollment(clonnedRequest, 0, courses.get(enrollment.getCourseId()), config, assignments, reservation));
							}
						}
					}
					students.put(student.getStudentId(), clonnedStudent);
				}
			}
			
            DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
            StudentSchedulingAssistantWeights w = new StudentSchedulingAssistantWeights(server.getConfig());
            DistanceConflict dc = new DistanceConflict(server.getDistanceMetric(), server.getConfig());
            TimeOverlapsCounter toc = new TimeOverlapsCounter(null, server.getConfig());

			int nrVars = 0, assgnVars = 0, nrStud = 0, compStud = 0, dist = 0, overlap = 0, free = 0;
            double value = 0.0;

            for (Student student: students.values()) {
            	boolean complete = true;
                for (Request request: student.getRequests()) {
                	if (request instanceof FreeTimeRequest) continue;
                	Enrollment enrollment = assignment.getValue(request);
                	if (enrollment != null) {
                		assgnVars ++; nrVars ++;
                		value += w.getWeight(assignment, enrollment);
                	} else if (student.canAssign(assignment, request)) {
                		nrVars ++; complete = false;
                	}
                }
                nrStud ++;
                if (complete) compStud ++;
                for (int i = 0; i < student.getRequests().size() - 1; i++) {
                	Enrollment e1 = assignment.getValue(student.getRequests().get(i));
                    if (e1 == null || !e1.isCourseRequest()) continue;
                    dist += dc.nrConflicts(e1);
                    free += toc.nrFreeTimeConflicts(e1);
                    for (int j = i + 1; j < student.getRequests().size(); j++) {
                    	Request r2 = student.getRequests().get(j);
                        if (r2 instanceof FreeTimeRequest) continue;
                        Enrollment e2 = assignment.getValue(r2);
                        if (e2 == null) continue;
                        dist += dc.nrConflicts(e1, e2);
                        overlap += toc.nrConflicts(e1, e2);
                    }
                }
            }
            
            info.put("Assigned variables", df.format(100.0 * assgnVars / nrVars) + "% (" + assgnVars + "/" + nrVars + ")");
            info.put("Overall solution value", df.format(value));
            info.put("Students with complete schedule", df.format(100.0 * compStud / nrStud) + "% (" + compStud + "/" + nrStud + ")");
            info.put("Student distance conflicts", df.format(1.0 * dist / nrStud) + " (" + dist + ")");
            info.put("Time overlapping conflicts", df.format(overlap / 12.0 / nrStud) + "h (" + overlap + ")");
            info.put("Free time overlapping conflicts", df.format(free / 12.0 / nrStud) + "h (" + free + ")");
            
            double disbWeight = 0;
            int disbSections = 0;
            int disb10Sections = 0;
            boolean balanceUnlimited = server.getConfig().getPropertyBoolean("General.BalanceUnlimited", false);
            for (Offering offering: offerings.values()) {
            	for (Config config: offering.getConfigs()) {
            		double enrl = config.getEnrollments(assignment).size();
            		for (Subpart subpart: config.getSubparts()) {
                        if (subpart.getSections().size() <= 1) continue;
                        if (subpart.getLimit() > 0) {
                            // sections have limits -> desired size is section limit x (total enrollment / total limit)
                            double ratio = enrl / subpart.getLimit();
                            for (Section section: subpart.getSections()) {
                                double desired = ratio * section.getLimit();
                                disbWeight += Math.abs(section.getEnrollments(assignment).size() - desired);
                                disbSections ++;
                                if (Math.abs(desired - section.getEnrollments(assignment).size()) >= Math.max(1.0, 0.1 * section.getLimit()))
                                    disb10Sections++;
                            }
                        } else if (balanceUnlimited) {
                            // unlimited sections -> desired size is total enrollment / number of sections
                            for (Section section: subpart.getSections()) {
                                double desired = enrl / subpart.getSections().size();
                                disbWeight += Math.abs(section.getEnrollments(assignment).size() - desired);
                                disbSections ++;
                                if (Math.abs(desired - section.getEnrollments(assignment).size()) >= Math.max(1.0, 0.1 * desired))
                                    disb10Sections++;
                            }
                        }
                    }
                }
            }
            
            if (disbSections != 0) {
                info.put("Average disbalance", df.format(disbWeight / disbSections) + " (" + df.format(assgnVars == 0 ? 0.0 : 100.0 * disbWeight / assgnVars) + "%)");
                info.put("Sections disbalanced by 10% or more", disb10Sections + " (" + df.format(disbSections == 0 ? 0.0 : 100.0 * disb10Sections / disbSections) + "%)");
            }
		} finally {
			lock.release();
		}
		return info;		
	}
	
	@Override
	public String name() {
		return "info";
	}
}
