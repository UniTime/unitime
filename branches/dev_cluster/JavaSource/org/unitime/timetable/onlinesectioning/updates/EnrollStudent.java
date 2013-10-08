/*
 * UniTime 3.3 (University Timetabling Application)
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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.solver.CheckAssignmentAction;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
public class EnrollStudent implements OnlineSectioningAction<ClassAssignmentInterface>, HasCacheMode {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private CourseRequestInterface iRequest;
	private List<ClassAssignmentInterface.ClassAssignment> iAssignment;
	
	public EnrollStudent(Long studentId, CourseRequestInterface request, List<ClassAssignmentInterface.ClassAssignment> assignment) {
		iStudentId = studentId;
		iRequest = request;
		iAssignment = assignment;
	}
	
	public Long getStudentId() { return iStudentId; }
	public CourseRequestInterface getRequest() { return iRequest; }
	public List<ClassAssignmentInterface.ClassAssignment> getAssignment() { return iAssignment; }

	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		Set<Long> offeringIds = new HashSet<Long>();
		Set<Long> lockedCourses = new HashSet<Long>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
			if (ca != null && !ca.isFreeTime()) {
				XCourse course = server.getCourse(ca.getCourseId());
				if (course == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				if (server.isOfferingLocked(course.getOfferingId())) {
					lockedCourses.add(course.getCourseId());
					for (CourseRequestInterface.Request r: getRequest().getCourses())
						if (!r.isWaitList() && !r.hasRequestedFreeTime()) {
							if ((r.hasRequestedCourse() && r.getRequestedCourse().equalsIgnoreCase(MSG.course(course.getSubjectArea(), course.getCourseNumber()))) ||
								(r.hasFirstAlternative() && r.getFirstAlternative().equalsIgnoreCase(MSG.course(course.getSubjectArea(), course.getCourseNumber()))) ||
								(r.hasSecondAlternative() && r.getSecondAlternative().equalsIgnoreCase(MSG.course(course.getSubjectArea(), course.getCourseNumber()))))
								r.setWaitList(true);
						}
					// throw new SectioningException(SectioningExceptionType.COURSE_LOCKED, course.getName());
				} else {
					offeringIds.add(course.getOfferingId());
				}
			}
		
		/*
		OnlineSectioningServer.ServerCallback<Boolean> enrollmentsUpdated = new OnlineSectioningServer.ServerCallback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Update enrollment counts failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		*/

		OnlineSectioningServer.ServerCallback<Boolean> offeringChecked = new OnlineSectioningServer.ServerCallback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Offering check failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		
		helper.beginTransaction();
		try {
			Lock lock = server.lockStudent(getStudentId(), offeringIds, true);
			try {
				OnlineSectioningLog.Action.Builder action = helper.getAction();
				
				if (getRequest().getStudentId() != null)
					action.setStudent(
							OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(getStudentId()));
				
				OnlineSectioningLog.Enrollment.Builder requested = OnlineSectioningLog.Enrollment.newBuilder();
				requested.setType(OnlineSectioningLog.Enrollment.EnrollmentType.REQUESTED);
				Map<Long, OnlineSectioningLog.CourseRequestOption.Builder> options = new Hashtable<Long, OnlineSectioningLog.CourseRequestOption.Builder>();
				for (ClassAssignmentInterface.ClassAssignment assignment: getAssignment())
					if (assignment != null) {
						OnlineSectioningLog.Section s = OnlineSectioningHelper.toProto(assignment); 
						requested.addSection(s);
						if (!assignment.isFreeTime()) {
							OnlineSectioningLog.CourseRequestOption.Builder option = options.get(assignment.getCourseId());
							if (option == null) {
								option = OnlineSectioningLog.CourseRequestOption.newBuilder().setType(OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT);
								options.put(assignment.getCourseId(), option);
							}
							option.addSection(s);
						}
					}
				action.addEnrollment(requested);
				for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(getRequest()))
					action.addRequest(r);

				new CheckAssignmentAction(getStudentId(), getAssignment()).check(server, helper);
				
				Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				action.getStudentBuilder().setUniqueId(student.getUniqueId())
					.setExternalId(student.getExternalUniqueId())
					.setName(student.getName(DepartmentalInstructor.sNameFormatFirstMiddleLast));

				Hashtable<Long, Class_> classes = new Hashtable<Long, Class_>();
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
					Class_ clazz = Class_DAO.getInstance().get(ca.getClassId(), helper.getHibSession());
					if (clazz == null)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
					classes.put(clazz.getUniqueId(), clazz);
				}
				
				Map<Long, StudentClassEnrollment> oldEnrollments = new HashMap<Long, StudentClassEnrollment>();
				Map<Long, Object[]> oldApprovals = new HashMap<Long, Object[]>();
				for (StudentClassEnrollment e: student.getClassEnrollments()) {
					oldEnrollments.put(e.getClazz().getUniqueId(), e);
					if (e.getApprovedBy() != null && !oldApprovals.containsKey(e.getCourseOffering().getUniqueId())) {
						oldApprovals.put(e.getCourseOffering().getUniqueId(), new Object[] {e.getApprovedBy(), e.getApprovedDate()});
					}
				}
				Map<Long, org.unitime.timetable.model.CourseRequest> req = SaveStudentRequests.saveRequest(server, helper, student, getRequest(), false);
				
				// save requested enrollment
				for (Map.Entry<Long, org.unitime.timetable.model.CourseRequest> e: req.entrySet()) {
					OnlineSectioningLog.CourseRequestOption.Builder option = options.get(e.getKey());
					if (option != null) {
						CourseRequestOption o = new CourseRequestOption();
						o.setCourseRequest(e.getValue());
						o.setOption(option.build());
						e.getValue().getCourseRequestOptions().add(o);
						helper.getHibSession().saveOrUpdate(o);

					}
				}

				Date ts = new Date();
				
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
					Class_ clazz = classes.get(ca.getClassId());
					org.unitime.timetable.model.CourseRequest cr = req.get(ca.getCourseId());
					if (clazz == null || cr == null) continue;
					if (lockedCourses.contains(ca.getCourseId())) {
						ClassWaitList cwl = new ClassWaitList();
						cwl.setClazz(clazz);
						cwl.setCourseRequest(cr);
						cwl.setStudent(student);
						cwl.setType(ClassWaitList.Type.LOCKED.ordinal());
						cwl.setTimestamp(ts);
						if (cr.getClassWaitLists() == null)
							cr.setClassWaitLists(new HashSet<ClassWaitList>());
						cr.getClassWaitLists().add(cwl);
						helper.getHibSession().saveOrUpdate(cwl);
						continue;
					}
					StudentClassEnrollment enrl = new StudentClassEnrollment();
					StudentClassEnrollment old = oldEnrollments.get(ca.getClassId());
					enrl.setClazz(clazz);
					clazz.getStudentEnrollments().add(enrl);
					enrl.setCourseOffering(cr.getCourseOffering());
					enrl.setCourseRequest(cr);
					enrl.setTimestamp(old != null ? old.getTimestamp() : ts);
					enrl.setStudent(student);
					enrl.setChangedBy(old != null ? old.getChangedBy() : helper.getUser() == null ? null : helper.getUser().getExternalId());
					Object[] approval = oldApprovals.get(ca.getCourseId());
					if (approval != null) {
						enrl.setApprovedBy((String)approval[0]);
						enrl.setApprovedDate((Date)approval[1]);
					}
					student.getClassEnrollments().add(enrl);
				}
				
				helper.getHibSession().save(student);
				helper.getHibSession().flush();
				
				// Reload student
				XStudent oldStudent = server.getStudent(getStudentId());
				XStudent newStudent = null;
				try {
					newStudent = ReloadAllData.loadStudentNoCheck(student, server, helper);
					server.update(newStudent, true);
				} catch (Exception e) {
					if (e instanceof RuntimeException)
						throw (RuntimeException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				}
				
				if (oldStudent != null) {
					for (XRequest oldRequest: oldStudent.getRequests()) {
						XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
						if (oldEnrollment == null) continue; // free time or not assigned
						XCourseRequest newRequest = null;
						XEnrollment newEnrollment = null;
						if (newStudent != null)
							for (XRequest r: newStudent.getRequests()) {
								XEnrollment e = (r instanceof XCourseRequest ? ((XCourseRequest)r).getEnrollment() : null);
								if (e != null && e.getOfferingId().equals(oldEnrollment.getOfferingId())) {
									newRequest = (XCourseRequest)r; newEnrollment = e; break;
								}
							}
						
						Set<Long> oldSections;
						if (newEnrollment == null) {
							oldSections = oldEnrollment.getSectionIds();
						} else {
							oldSections = new HashSet<Long>();
							for (Long sectionId: oldEnrollment.getSectionIds())
								if (!newEnrollment.getSectionIds().contains(sectionId))
									oldSections.add(sectionId);
						}
						
						if (oldSections.isEmpty()) continue; // same assignment
						
						boolean checkOffering = false;
						XOffering offering = server.getOffering(oldEnrollment.getOfferingId());
						if (!offering.getReservations().isEmpty()) {
							checkOffering = true;
							helper.info("Check offering for " + oldEnrollment.getCourseName() + ": there are reservations.");
						} else {
							XEnrollments enrollments = server.getEnrollments(oldEnrollment.getOfferingId());
							for (Long sectionId: oldSections) {
								XSection section = offering.getSection(sectionId);
								if (section != null && section.getLimit() >= 0 && section.getLimit() - enrollments.countEnrollmentsForSection(sectionId) == 1) {
									checkOffering = true;
									helper.info("Check offering for " + oldEnrollment.getCourseName() + ": section " + section + " became available.");
									break;
								}
							}
							if (!checkOffering && (newEnrollment == null || !newEnrollment.getConfigId().equals(oldEnrollment.getConfigId()))) {
								XConfig config = offering.getConfig(oldEnrollment.getConfigId());
								if (config != null && config.getLimit() >= 0 && config.getLimit() - enrollments.countEnrollmentsForConfig(config.getConfigId()) == 1) {
									checkOffering = true;
									helper.info("Check offering for " + oldEnrollment.getCourseName() + ": config " + config + " became available.");
								}
							}
							if (!checkOffering && (newEnrollment == null || !newEnrollment.getCourseId().equals(oldEnrollment.getCourseId()))) {
								XCourse course = offering.getCourse(oldEnrollment.getCourseId());
								if (course != null && course.getLimit() >= 0 && course.getLimit() - enrollments.countEnrollmentsForCourse(course.getCourseId()) == 1) {
									checkOffering = true;
									helper.info("Check offering for " + oldEnrollment.getCourseName() + ": course " + course + " became available.");
								}
							}
						}
						
						if (checkOffering)
							server.execute(new CheckOfferingAction(oldEnrollment.getOfferingId()), helper.getUser(), offeringChecked);
						
						updateSpace(server,
								newEnrollment == null ? null : SectioningRequest.convert(newStudent, newRequest, server, server.getOffering(newEnrollment.getOfferingId()), newEnrollment).getAssignment(),
								oldEnrollment == null ? null : SectioningRequest.convert(oldStudent, (XCourseRequest)oldRequest, server, server.getOffering(oldEnrollment.getOfferingId()), oldEnrollment).getAssignment());
						server.persistExpectedSpaces(oldEnrollment.getOfferingId());
					}
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
					for (XRequest oldRequest: oldStudent.getRequests()) {
						XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
						if (oldEnrollment != null)
							for (XSection section: server.getOffering(oldEnrollment.getOfferingId()).getSections(oldEnrollment))
								enrollment.addSection(OnlineSectioningHelper.toProto(section, oldEnrollment));
					}
					action.addEnrollment(enrollment);
				}
				
				if (newStudent != null) {
					requests: for (XRequest newRequest: newStudent.getRequests()) {
						XEnrollment newEnrollment = (newRequest instanceof XCourseRequest ? ((XCourseRequest)newRequest).getEnrollment() : null);
						if (newEnrollment == null) continue; // free time or not assigned
						if (oldStudent != null)
							for (XRequest oldRequest: oldStudent.getRequests()) {
								XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
								if (oldEnrollment != null && oldEnrollment.getOfferingId().equals(newEnrollment.getOfferingId()))
									continue requests;
							}
						updateSpace(server,
								SectioningRequest.convert(newStudent, (XCourseRequest)newRequest, server, server.getOffering(newEnrollment.getOfferingId()), newEnrollment).getAssignment(),
								null);
						server.persistExpectedSpaces(newEnrollment.getOfferingId());
						// server.execute(new UpdateEnrollmentCountsAction(newEnrollment.getOffering().getId()), helper.getUser(), enrollmentsUpdated);
					}
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (XRequest newRequest: newStudent.getRequests()) {
						XEnrollment newEnrollment = (newRequest instanceof XCourseRequest ? ((XCourseRequest)newRequest).getEnrollment() : null);
						if (newEnrollment != null)
							for (XSection section: server.getOffering(newEnrollment.getOfferingId()).getSections(newEnrollment))
								enrollment.addSection(OnlineSectioningHelper.toProto(section, newEnrollment));
					}
					action.addEnrollment(enrollment);
				}

				server.execute(new NotifyStudentAction(getStudentId(), oldStudent), helper.getUser());
			} finally {
				lock.release();
			}
			helper.commitTransaction();
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
		
		return server.execute(new GetAssignment(getStudentId()), helper.getUser());
	}
	
    public static void updateSpace(OnlineSectioningServer server, Enrollment newEnrollment, Enrollment oldEnrollment) {
    	if (newEnrollment == null && oldEnrollment == null) return;
    	XExpectations expectations = server.getExpectations((newEnrollment == null ? oldEnrollment : newEnrollment).getOffering().getId());
    	if (oldEnrollment != null) {
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            for (Enrollment enrl : oldEnrollment.getRequest().values()) {
            	if (!enrl.getCourse().equals(oldEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : oldEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(oldEnrollment.getRequest()) || !(otherRequest instanceof CourseRequest))
                        continue;
                    Enrollment otherErollment = otherRequest.getInitialAssignment();
                    if (otherErollment == null)
                        continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps)
                    feasibleEnrollments.add(enrl);
            }
            double increment = 1.0 / feasibleEnrollments.size();
            for (Enrollment feasibleEnrollment : feasibleEnrollments)
                for (Section section : feasibleEnrollment.getSections())
                	expectations.incExpectedSpace(section.getId(), increment);
    	}
    	if (newEnrollment != null) {
            for (Section section : newEnrollment.getSections())
                section.setSpaceHeld(section.getSpaceHeld() - 1.0);
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            for (Enrollment enrl : newEnrollment.getRequest().values()) {
            	if (!enrl.getCourse().equals(newEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : newEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(newEnrollment.getRequest()) || !(otherRequest instanceof CourseRequest))
                        continue;
                    Enrollment otherErollment = otherRequest.getAssignment();
                    if (otherErollment == null)
                        continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps)
                    feasibleEnrollments.add(enrl);
            }
            double decrement = 1.0 / feasibleEnrollments.size();
            for (Enrollment feasibleEnrollment : feasibleEnrollments)
                for (Section section : feasibleEnrollment.getSections())
                	expectations.incExpectedSpace(section.getId(), - decrement);
    	}
    	server.update(expectations);
    }
	
	@Override
	public String name() {
		return "enroll";
	}

	@Override
	public CacheMode getCacheMode() {
		return CacheMode.REFRESH;
	}
}
