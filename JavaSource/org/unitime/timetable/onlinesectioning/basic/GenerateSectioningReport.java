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
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumOverride;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.LearningCommunityReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.cpsolver.studentsct.reservation.UniversalOverride;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
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
import org.unitime.timetable.onlinesectioning.model.XLearningCommunityReservation;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudent.XAdvisor;
import org.unitime.timetable.onlinesectioning.model.XStudent.XGroup;
import org.unitime.timetable.server.sectioning.SectioningReportTypesBackend.ReportType;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XUniversalReservation;

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
		try {
			OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
			DistanceMetric dm = server.getDistanceMetric();
			model.setDistanceConflict(new DistanceConflict(dm, model.getProperties()));
			model.setTimeOverlaps(new TimeOverlapsCounter(null, model.getProperties()));
			model.setDayOfWeekOffset(server.getAcademicSession().getDayOfWeekOffset());
			boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
	        Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();

			Map<Long, Offering> offerings = new HashMap<Long, Offering>();
			Hashtable<Long, Course> courses = new Hashtable<Long, Course>();
			Map<XGroup, List<GroupReservation>> groups = new HashMap<XGroup, List<GroupReservation>>();
			Hashtable<Long, Config> configs = new Hashtable<Long, Config>();
			Hashtable<Long, Subpart> subparts = new Hashtable<Long, Subpart>();
			Hashtable<Long, Section> sections = new Hashtable<Long, Section>();
			Hashtable<Long, Reservation> reservations = new Hashtable<Long, Reservation>();
			Set<XDistribution> linkedSections = new HashSet<XDistribution>();

			AcademicSessionInfo session = server.getAcademicSession();
			Set<String> wlStates = new HashSet<String>();
			Set<String> noSubStates = new HashSet<String>();
			Session dbSession = SessionDAO.getInstance().get(session.getUniqueId());
			for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll(helper.getHibSession())) {
					if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.waitlist))
						wlStates.add(status.getReference());
					else if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.nosubs))
						noSubStates.add(status.getReference());
			}
	        boolean checkUnavailabilitiesFromOtherSessions = server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessionsForReporting", false);
			
			Lock lock = server.readLock();
			
			try {
				Map<Long, Long> parentCourses = new HashMap<Long, Long>();
	    		for (XCourseId ci: server.findCourses(new AnyCourseMatcher())) {
		        	XOffering offering = server.getOffering(ci.getOfferingId());
		        	if (offering == null || offerings.containsKey(offering.getOfferingId())) continue;
	        		Offering clonedOffering = new Offering(offering.getOfferingId(), offering.getName());
	        		clonedOffering.setModel(model);
	        		for (XCourse course: offering.getCourses()) {
	        			Course clonedCourse = new Course(course.getCourseId(), course.getSubjectArea(), course.getCourseNumber(), clonedOffering, course.getLimit(), course.getProjected());
		        		clonedCourse.setNote(course.getNote());
		        		clonedCourse.setType(course.getType());
		        		clonedCourse.setTitle(course.getTitle());
		        		clonedCourse.setCredit(course.getCredit());
		        		courses.put(course.getCourseId(), clonedCourse);
		        		if (course.getParentCourseId() != null)
		        			parentCourses.put(course.getCourseId(), course.getParentCourseId());
	        		}
	        		for (XConfig config: offering.getConfigs()) {
	        			Config clonedConfig = new Config(config.getConfigId(), config.getLimit(), config.getName(), clonedOffering);
	        			if (config.getInstructionalMethod() != null) {
	        				clonedConfig.setInstructionalMethodId(config.getInstructionalMethod().getUniqueId());
	        				clonedConfig.setInstructionalMethodName(config.getInstructionalMethod().getLabel());
	        				clonedConfig.setInstructionalMethodReference(config.getInstructionalMethod().getReference());
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
	        					clonedSection.setEnabled(section.isEnabledForScheduling());
	        					clonedSection.setOnline(section.isOnline());
	        					clonedSection.setPast(section.isPast());
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
	        				clonedReservation = new CurriculumReservation(reservation.getReservationId(), reservation.getLimit(), clonedOffering, curriculumR.getAcademicAreas(), curriculumR.getClassifications(), curriculumR.getMajors(), curriculumR.getMinors());
	        				for (String major: curriculumR.getMajors()) {
	        					Set<String> concs = curriculumR.getConcentrations(major);
	        					if (concs != null)
	        						for (String conc: concs) ((CurriculumReservation)clonedReservation).addConcentration(major, conc);
	        				}
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
	        			case IndividualOverride:
	        				XIndividualReservation ovrR = (XIndividualReservation) reservation;
	        				if (ovrR.isOverride())
	        					clonedReservation = new ReservationOverride(reservation.getReservationId(), clonedOffering, ovrR.getStudentIds());
	        				else
	        					clonedReservation = new IndividualReservation(reservation.getReservationId(), clonedOffering, ovrR.getStudentIds());
	    					clonedReservation.setMustBeUsed(ovrR.mustBeUsed());
	    					clonedReservation.setAllowOverlap(ovrR.isAllowOverlap());
	    					clonedReservation.setCanAssignOverLimit(ovrR.canAssignOverLimit());
	    					break;
	        			case GroupOverride:
	        				XGroupReservation groupR = (XGroupReservation) reservation;
	        				clonedReservation = new GroupReservation(reservation.getReservationId(), reservation.getLimit(), clonedOffering);
	        				List<GroupReservation> list = groups.get(groupR.getGroup());
	        				if (list == null) {
	        					list = new ArrayList<GroupReservation>();
	        					groups.put(groupR.getGroup(), list);
	        				}
	        				list.add((GroupReservation)clonedReservation);
	        				((GroupReservation)clonedReservation).setMustBeUsed(groupR.mustBeUsed());
	        				((GroupReservation)clonedReservation).setAllowOverlap(groupR.isAllowOverlap());
	        				((GroupReservation)clonedReservation).setCanAssignOverLimit(groupR.canAssignOverLimit());
	        				break;
	        			case LearningCommunity:
	        				XLearningCommunityReservation lcR = (XLearningCommunityReservation) reservation;
	        				clonedReservation = new LearningCommunityReservation(reservation.getReservationId(), reservation.getLimit(), courses.get(lcR.getCourseId()), lcR.getStudentIds());
	        				if (lcR.getGroup() != null) {
	        					list = groups.get(lcR.getGroup());
	            				if (list == null) {
	            					list = new ArrayList<GroupReservation>();
	            					groups.put(lcR.getGroup(), list);
	            				}
	            				list.add((GroupReservation)clonedReservation);
	        				}
	        				break;
	        			case CurriculumOverride:
	        				XCurriculumReservation curR = (XCurriculumReservation) reservation;
	        				if (curR.isOverride())
	        					clonedReservation = new CurriculumOverride(reservation.getReservationId(), reservation.getLimit(), clonedOffering, curR.getAcademicAreas(), curR.getClassifications(), curR.getMajors(), curR.getMinors());
	        				else
	        					clonedReservation = new CurriculumReservation(reservation.getReservationId(), reservation.getLimit(), clonedOffering, curR.getAcademicAreas(), curR.getClassifications(), curR.getMajors(), curR.getMinors());
	        				((CurriculumReservation)clonedReservation).setMustBeUsed(curR.mustBeUsed());
	        				((CurriculumReservation)clonedReservation).setAllowOverlap(curR.isAllowOverlap());
	        				((CurriculumReservation)clonedReservation).setCanAssignOverLimit(curR.canAssignOverLimit());
	        				for (String major: curR.getMajors()) {
	        					Set<String> concs = curR.getConcentrations(major);
	        					if (concs != null)
	        						for (String conc: concs) ((CurriculumReservation)clonedReservation).addConcentration(major, conc);
	        				}
	        				break;
	        			case Universal:
	        				XUniversalReservation uniR = (XUniversalReservation) reservation;
	        				clonedReservation = new UniversalOverride(reservation.getReservationId(), reservation.isOverride(), reservation.getLimit(), clonedOffering, uniR.getFilter());
	        				((UniversalOverride)clonedReservation).setMustBeUsed(uniR.mustBeUsed());
	        				((UniversalOverride)clonedReservation).setAllowOverlap(uniR.isAllowOverlap());
	        				((UniversalOverride)clonedReservation).setCanAssignOverLimit(uniR.canAssignOverLimit());
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
				for (Map.Entry<Long, Long> e: parentCourses.entrySet()) {
					Course course = courses.get(e.getKey());
					Course parent = courses.get(e.getValue());
					if (course != null && parent != null)
						course.setParent(parent);
				}
		        
		        Map<Long, Student> students = new HashMap<Long, Student>();
				for (XStudentId id: server.findStudents(new AnyStudentMatcher())) {
					XStudent student = (id instanceof XStudent ? (XStudent)id : server.getStudent(id.getStudentId()));
					if (student == null) return null;
					
					String status = (student.getStatus() == null ? session.getDefaultSectioningStatus() : student.getStatus());
					WaitListMode wl = WaitListMode.None;
					if (status == null || wlStates.contains(status))
						wl = WaitListMode.WaitList;
					else if (noSubStates.contains(status))
						wl = WaitListMode.NoSubs;

					Student clonnedStudent = new Student(student.getStudentId());
					clonnedStudent.setExternalId(student.getExternalId());
					clonnedStudent.setName(student.getName());
					clonnedStudent.setNeedShortDistances(student.hasAccomodation(dm.getShortDistanceAccommodationReference()));
					clonnedStudent.setAllowDisabled(student.isAllowDisabled());
					clonnedStudent.setClassFirstDate(student.getClassStartDate());
					clonnedStudent.setClassLastDate(student.getClassEndDate());
					clonnedStudent.setBackToBackPreference(student.getBackToBackPreference());
					clonnedStudent.setModalityPreference(student.getModalityPreference());
					for (XStudent.XGroup g: student.getGroups()) {
						clonnedStudent.getGroups().add(new StudentGroup(g.getType(), g.getAbbreviation(), g.getTitle()));
						List<GroupReservation> list = groups.get(g);
						if (list != null)
							for (GroupReservation gr: list)
								gr.getStudentIds().add(student.getStudentId());
					}
					for (XAreaClassificationMajor acm: student.getMajors())
						clonnedStudent.getAreaClassificationMajors().add(new AreaClassificationMajor(
								acm.getArea(), acm.getAreaLabel(),
								acm.getClassification(), acm.getClassificationLabel(),
								acm.getMajor(), acm.getMajorLabel(),
								acm.getConcentration(), acm.getConcentrationLabel(),
								acm.getDegree(), acm.getDegreeLabel(),
								acm.getProgram(), acm.getProgramLabel(),
								acm.getWeight()));
					for (XAreaClassificationMajor acm: student.getMinors())
						clonnedStudent.getAreaClassificationMinors().add(new AreaClassificationMajor(
								acm.getArea(), acm.getAreaLabel(),
								acm.getClassification(), acm.getClassificationLabel(),
								acm.getMajor(), acm.getMajorLabel(),
								acm.getConcentration(), acm.getConcentrationLabel(),
								acm.getDegree(), acm.getDegreeLabel(),
								acm.getProgram(), acm.getProgramLabel(),
								acm.getWeight()));
					for (XStudent.XGroup acc: student.getAccomodations())
						clonnedStudent.getAccommodations().add(acc.getAbbreviation());
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
								CourseRequest clonnedRequest = new CourseRequest(r.getRequestId(), r.getPriority(), r.isAlternative(), clonnedStudent, req, cr.isWaitListOrNoSub(wl), cr.getTimeStamp() == null ? null : cr.getTimeStamp().getTime());
								if (cr.isCritical()) {
									if (cr.getCritical() == CourseDemand.Critical.CRITICAL.ordinal())
										clonnedRequest.setRequestPriority(RequestPriority.Critical);
									else if (cr.getCritical() == CourseDemand.Critical.IMPORTANT.ordinal())
										clonnedRequest.setRequestPriority(RequestPriority.Important);
									else if (cr.getCritical() == CourseDemand.Critical.VITAL.ordinal())
										clonnedRequest.setRequestPriority(RequestPriority.Vital);
									else if (cr.getCritical() == CourseDemand.Critical.LC.ordinal())
										clonnedRequest.setRequestPriority(RequestPriority.LC);
									else if (cr.getCritical() == CourseDemand.Critical.VISITING_F2F.ordinal())
										clonnedRequest.setRequestPriority(RequestPriority.VisitingF2F);
								}
								cr.fillChoicesIn(clonnedRequest);
								XEnrollment enrollment = cr.getEnrollment();
								if (enrollment != null) {
									Config config = configs.get(enrollment.getConfigId());
									Set<Section> assignments = new HashSet<Section>();
									for (Long sectionId: enrollment.getSectionIds()) {
										Section section = sections.get(sectionId);
										if (section != null) assignments.add(section);
									}
									Reservation reservation = (enrollment.getReservation() == null ? null : reservations.get(enrollment.getReservation().getReservationId()));
									if (config != null && !sections.isEmpty()) {
										Course course = courses.get(enrollment.getCourseId());
										assignment.assign(0, new Enrollment(clonnedRequest, clonnedRequest.getCourses().indexOf(course), course, config, assignments, reservation));
									}
								}
							}
						}
					}
					for (XAdvisor advisor: student.getAdvisors())
						clonnedStudent.getAdvisors().add(new Instructor(0, advisor.getExternalId(), advisor.getName(), advisor.getEmail()));
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
				
				if (checkUnavailabilitiesFromOtherSessions)
					GetInfo.fillInAllUnavailabilitiesFromOtherSessionsUsingDatabase(students, server, helper);
				
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
			} finally {
				lock.release();
			}
			
			String name = iParameters.getProperty("report", null);
			if (name == null) {
				String reference = iParameters.getProperty("name");
				if (reference != null)
					name = ReportType.valueOf(reference).getImplementation();
			}
			if (name == null || name.isEmpty()) return null;
			Class<StudentSectioningReport> clazz = (Class<StudentSectioningReport>) Class.forName(name);
			StudentSectioningReport report = clazz.getConstructor(StudentSectioningModel.class).newInstance(model);
			
			return report.create(assignment, iParameters);
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);	
		}
	}

	@Override
	public String name() {
		return "report";
	}

}
