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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.model.AcademicAreaCode;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
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
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.cpsolver.studentsct.report.SectionConflictTable;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.match.AnyCourseMatcher;
import org.unitime.timetable.onlinesectioning.match.AnyStudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
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
public class GenerateSectioningReport implements OnlineSectioningAction<CSVFile> {
	private static final long serialVersionUID = 1L;
	private DataProperties iParameters = null;
	
	public GenerateSectioningReport withParameters(Properties parameters) {
		if (parameters instanceof DataProperties)
			iParameters = (DataProperties)parameters;
		else
			iParameters = new DataProperties(parameters);
		return this;
	}

	@Override
	public CSVFile execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
			DistanceMetric dm = server.getDistanceMetric();
			model.setDistanceConflict(new DistanceConflict(dm, model.getProperties()));
			model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
			boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);

			Map<Long, Offering> offerings = new HashMap<Long, Offering>();
    		Hashtable<Long, Course> courses = new Hashtable<Long, Course>();
    		Map<String, List<GroupReservation>> groups = new HashMap<String, List<GroupReservation>>();
    		Hashtable<Long, Config> configs = new Hashtable<Long, Config>();
    		Hashtable<Long, Subpart> subparts = new Hashtable<Long, Subpart>();
    		Hashtable<Long, Section> sections = new Hashtable<Long, Section>();
    		Hashtable<Long, Reservation> reservations = new Hashtable<Long, Reservation>();
    		Set<XDistribution> linkedSections = new HashSet<XDistribution>();

    		for (XCourseId ci: server.findCourses(new AnyCourseMatcher())) {
	        	XOffering offering = server.getOffering(ci.getOfferingId());
	        	if (offering == null || offerings.containsKey(offering.getOfferingId())) continue;
        		Offering clonedOffering = new Offering(offering.getOfferingId(), offering.getName());
        		clonedOffering.setModel(model);
        		for (XCourse course: offering.getCourses()) {
        			Course clonedCourse = new Course(course.getCourseId(), course.getSubjectArea(), course.getCourseNumber(), clonedOffering, course.getLimit(), course.getProjected());
	        		clonedCourse.setNote(course.getNote());
	        		clonedCourse.setCredit(course.getCredit());
	        		courses.put(course.getCourseId(), clonedCourse);
        		}
        		for (XConfig config: offering.getConfigs()) {
        			Config clonedConfig = new Config(config.getConfigId(), config.getLimit(), config.getName(), clonedOffering);
        			if (config.getInstructionalMethod() != null) {
        				clonedConfig.setInstructionalMethodId(config.getInstructionalMethod().getUniqueId());
        				clonedConfig.setInstructionalMethodName(config.getInstructionalMethod().getLabel());
        			}
        			configs.put(config.getConfigId(), clonedConfig);
        			for (XSubpart subpart: config.getSubparts()) {
        				Subpart clonedSubpart = new Subpart(subpart.getSubpartId(), subpart.getInstructionalType(), subpart.getName(), clonedConfig,
        						(subpart.getParentId() == null ? null: subparts.get(subpart.getParentId())));
        				clonedSubpart.setAllowOverlap(subpart.isAllowOverlap());
        				clonedSubpart.setCredit(subpart.getCredit(null));
        				subparts.put(subpart.getSubpartId(), clonedSubpart);
        				for (XSection section: subpart.getSections()) {
        					Section clonedSection = new Section(section.getSectionId(), section.getLimit(),
        							section.getName(), clonedSubpart, section.toPlacement(), section.toInstructors(),
        							(section.getParentId() == null ? null : sections.get(section.getParentId())));
        					clonedSection.setName(-1l, section.getName(-1l));
        					clonedSection.setNote(section.getNote());
        					clonedSection.setCancelled(section.isCancelled());
        					for (XDistribution distribution: offering.getDistributions()) {
        						if (distribution.getDistributionType() == XDistributionType.IngoreConflicts && distribution.hasSection(section.getSectionId())) {
        							for (Long id: distribution.getSectionIds())
        								if (!id.equals(section.getSectionId())) clonedSection.addIgnoreConflictWith(id);
        						} else if (distribution.getDistributionType() == XDistributionType.LinkedSections) {
        							linkedSections.add(distribution);
        						}
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
        		model.addOffering(clonedOffering);
        	}
	        
	        Map<Long, Student> students = new HashMap<Long, Student>();
	        Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
			for (XStudentId id: server.findStudents(new AnyStudentMatcher())) {
				XStudent student = (id instanceof XStudent ? (XStudent)id : server.getStudent(id.getStudentId()));
				if (student == null) return null;
				Student clonnedStudent = new Student(student.getStudentId());
				clonnedStudent.setExternalId(student.getExternalId());
				clonnedStudent.setName(student.getName());
				clonnedStudent.setNeedShortDistances(student.hasAccomodation(dm.getShortDistanceAccommodationReference()));
				for (String g: student.getGroups()) {
					clonnedStudent.getMinors().add(new AcademicAreaCode("", g));
					List<GroupReservation> list = groups.get(g);
					if (list != null)
						for (GroupReservation gr: list)
							gr.getStudentIds().add(student.getStudentId());
				}
				for (XAreaClassificationMajor acm: student.getMajors())
					clonnedStudent.getAreaClassificationMajors().add(new AreaClassificationMajor(acm.getArea(), acm.getClassification(), acm.getMajor()));
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
				}
				students.put(student.getStudentId(), clonnedStudent);
				model.addStudent(clonnedStudent);
				
				if (clonnedStudent.getExternalId() != null && !clonnedStudent.getExternalId().isEmpty()) {
					Collection<Long> offeringIds = server.getInstructedOfferings(clonnedStudent.getExternalId());
					if (offeringIds != null)
						for (Long offeringId: offeringIds) {
							XOffering offering = server.getOffering(offeringId);
							if (offering != null)
								offering.fillInUnavailabilities(clonnedStudent);
						}
				}
			}
			
			for (XDistribution distribution: linkedSections) {
				List<Section> linked = new ArrayList<Section>();
				for (Long id: distribution.getSectionIds()) {
					Section section = sections.get(id);
					if (section != null)
						linked.add(section);
				}
				if (linked.size() > 1)
					model.addLinkedSections(linkedClassesMustBeUsed, linked);
			}

			String name = iParameters.getProperty("report", SectionConflictTable.class.getName());
			Class<StudentSectioningReport> clazz = (Class<StudentSectioningReport>) Class.forName(name);
			StudentSectioningReport report = clazz.getConstructor(StudentSectioningModel.class).newInstance(model);
			
			return report.create(assignment, iParameters);
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);	
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "report";
	}

}
