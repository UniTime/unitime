/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentFailure;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.solver.CheckAssignmentAction;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class EnrollStudent implements OnlineSectioningAction<ClassAssignmentInterface>, HasCacheMode {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private CourseRequestInterface iRequest;
	private List<ClassAssignmentInterface.ClassAssignment> iAssignment;
	
	public EnrollStudent forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public EnrollStudent withRequest(CourseRequestInterface request) {
		iRequest = request;
		return this;
	}

	public EnrollStudent withAssignment(List<ClassAssignmentInterface.ClassAssignment> assignment) {
		iAssignment = assignment;
		return this;
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
		List<EnrollmentFailure> failures = null;
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
				} else {
					offeringIds.add(course.getOfferingId());
				}
			}
		
		OnlineSectioningServer.ServerCallback<Boolean> offeringChecked = new OnlineSectioningServer.ServerCallback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Offering check failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		
		Lock lock = server.lockStudent(getStudentId(), offeringIds, true);
		try {
			helper.beginTransaction();
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

				Map<XCourse, List<XSection>> enrlCheck = server.createAction(CheckAssignmentAction.class).forStudent(getStudentId()).withAssignment(getAssignment()).check(server, helper);
				
				Student student = (Student)helper.getHibSession().createQuery(
						"select s from Student s " +
						"left join fetch s.courseDemands as cd " +
	                    "left join fetch cd.courseRequests as cr " +
	                    "left join fetch cd.freeTime as ft " +
	                    "left join fetch cr.courseOffering as co " +
	                    "left join fetch cr.courseRequestOptions as cro " +
	                    "left join fetch cr.classWaitLists as cwl " + 
	                    "left join fetch s.classEnrollments as e " +
	                    "left join fetch e.clazz as c " +
	                    "left join fetch c.managingDept as cmd " +
	                    "left join fetch c.schedulingSubpart as ss " +
						"where s.uniqueId = :studentId").setLong("studentId", getStudentId()).uniqueResult();
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				
				XStudent oldStudent = server.getStudent(getStudentId());

				action.getStudentBuilder().setUniqueId(student.getUniqueId())
					.setExternalId(oldStudent.getExternalId())
					.setName(oldStudent.getName());
				
				if (CustomStudentEnrollmentHolder.hasProvider()) {
					failures = CustomStudentEnrollmentHolder.getProvider().enroll(server, helper, oldStudent, enrlCheck);
					for (Iterator<ClassAssignmentInterface.ClassAssignment> i = getAssignment().iterator(); i.hasNext(); ) {
						ClassAssignmentInterface.ClassAssignment ca = i.next();
						if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
						for (EnrollmentFailure f: failures) {
							if (!f.isEnrolled() && f.getSection().getSectionId().equals(ca.getClassId())) {
								i.remove();
							}
						}
					}
				}
				
				Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
				int priority = 0;
				Date ts = new Date();
				Map<Long, CourseRequest> course2request = new HashMap<Long, CourseRequest>();
				for (CourseRequestInterface.Request r: getRequest().getCourses()) {
					if (r.hasRequestedFreeTime() && r.hasRequestedCourse() && server.getCourse(r.getRequestedCourse()) != null)
						r.getRequestedFreeTime().clear();
					if (r.hasRequestedFreeTime()) {
						for (CourseRequestInterface.FreeTime ft: r.getRequestedFreeTime()) {
							CourseDemand cd = null;
							for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
								CourseDemand adept = i.next();
								if (adept.getFreeTime() == null) continue;
								cd = adept; i.remove(); break;
							}
							if (cd == null) {
								cd = new CourseDemand();
								cd.setTimestamp(ts);
								cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
								student.getCourseDemands().add(cd);
								cd.setStudent(student);
							}
							cd.setAlternative(false);
							cd.setPriority(priority);
							cd.setWaitlist(false);
							FreeTime free = cd.getFreeTime();
							if (free == null) {
								free = new FreeTime();
								cd.setFreeTime(free);
							}
							free.setCategory(0);
							free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
							free.setStartSlot(ft.getStart());
							free.setLength(ft.getLength());
							free.setSession(student.getSession());
							free.setName(ft.toString());
							helper.getHibSession().saveOrUpdate(free);
							helper.getHibSession().saveOrUpdate(cd);
						}
					} else {
						List<XCourseId> courses = new ArrayList<XCourseId>();
						if (r.hasRequestedCourse()) {
							XCourseId c = server.getCourse(r.getRequestedCourse());
							if (c != null) courses.add(c);
						}
						if (r.hasFirstAlternative()) {
							XCourseId c = server.getCourse(r.getFirstAlternative());
							if (c != null) courses.add(c);
						}
						if (r.hasSecondAlternative()) {
							XCourseId c = server.getCourse(r.getSecondAlternative());
							if (c != null) courses.add(c);
						}
						if (courses.isEmpty()) continue;
						
						CourseDemand cd = null;
						adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
							CourseDemand adept = i.next();
							if (adept.getFreeTime() != null) continue;
							for (CourseRequest cr: adept.getCourseRequests())
								if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getCourseId())) {
									cd = adept; i.remove();  break adepts;
								}
						}
						if (cd == null) {
							cd = new CourseDemand();
							cd.setTimestamp(ts);
							cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
							cd.setCourseRequests(new HashSet<CourseRequest>());
							cd.setStudent(student);
							student.getCourseDemands().add(cd);
						}
						cd.setAlternative(false);
						cd.setPriority(priority);
						cd.setWaitlist(r.isWaitList());
						Iterator<CourseRequest> requests = new TreeSet<CourseRequest>(cd.getCourseRequests()).iterator();
						int order = 0;
						for (XCourseId co: courses) {
							CourseRequest cr = null;
							OnlineSectioningLog.CourseRequestOption.Builder option = options.get(co.getCourseId());
							if (requests.hasNext()) {
								cr = requests.next();
								if (cr.getCourseRequestOptions() != null && cr.getCourseRequestOptions().size() == 1 && option != null) {
									CourseRequestOption o = cr.getCourseRequestOptions().iterator().next();
									o.setOption(option.build());
								} else {
									if (cr.getCourseRequestOptions() != null) {
										for (Iterator<CourseRequestOption> i = cr.getCourseRequestOptions().iterator(); i.hasNext(); ) {
											helper.getHibSession().delete(i.next()); i.remove();
										}
									} else {
										cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
									}
									if (option != null) {
										CourseRequestOption o = new CourseRequestOption();
										o.setCourseRequest(cr);
										o.setOption(option.build());
										cr.getCourseRequestOptions().add(o);
									}
								}
								if (cr.getClassWaitLists() != null)
									for (Iterator<ClassWaitList> i = cr.getClassWaitLists().iterator(); i.hasNext(); ) {
										helper.getHibSession().delete(i.next());
										i.remove();
									}
							} else {
								cr = new CourseRequest();
								cd.getCourseRequests().add(cr);
								cr.setCourseDemand(cd);
								cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
								if (option != null) {
									CourseRequestOption o = new CourseRequestOption();
									o.setCourseRequest(cr);
									o.setOption(option.build());
									cr.getCourseRequestOptions().add(o);
								}
							}
							cr.setAllowOverlap(false);
							cr.setCredit(0);
							cr.setOrder(order++);
							if (cr.getCourseOffering() == null || !cr.getCourseOffering().getUniqueId().equals(co.getCourseId()))
								cr.setCourseOffering(CourseOfferingDAO.getInstance().get(co.getCourseId(), helper.getHibSession()));
							course2request.put(co.getCourseId(), cr);
						}
						while (requests.hasNext()) {
							CourseRequest cr = requests.next();
							cd.getCourseRequests().remove(cr);
							helper.getHibSession().delete(cr);
						}
						helper.getHibSession().saveOrUpdate(cd);
					}
					priority++;
				}
				
				for (CourseRequestInterface.Request r: getRequest().getAlternatives()) {
					if (r.hasRequestedFreeTime() && r.hasRequestedCourse() && server.getCourse(r.getRequestedCourse()) != null)
						r.getRequestedFreeTime().clear();
					if (r.hasRequestedFreeTime()) {
						for (CourseRequestInterface.FreeTime ft: r.getRequestedFreeTime()) {
							CourseDemand cd = null;
							for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
								CourseDemand adept = i.next();
								if (adept.getFreeTime() == null) continue;
								cd = adept; i.remove(); break;
							}
							if (cd == null) {
								cd = new CourseDemand();
								cd.setTimestamp(ts);
								cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
								student.getCourseDemands().add(cd);
								cd.setStudent(student);
							}
							cd.setAlternative(true);
							cd.setPriority(priority);
							cd.setWaitlist(false);
							FreeTime free = cd.getFreeTime();
							if (free == null) {
								free = new FreeTime();
								cd.setFreeTime(free);
							}
							free.setCategory(0);
							free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
							free.setStartSlot(ft.getStart());
							free.setLength(ft.getLength());
							free.setSession(student.getSession());
							free.setName(ft.toString());
							helper.getHibSession().saveOrUpdate(free);
							helper.getHibSession().saveOrUpdate(cd);
						}
					} else {
						List<XCourseId> courses = new ArrayList<XCourseId>();
						if (r.hasRequestedCourse()) {
							XCourseId c = server.getCourse(r.getRequestedCourse());
							if (c != null) courses.add(c);
						}
						if (r.hasFirstAlternative()) {
							XCourseId c = server.getCourse(r.getFirstAlternative());
							if (c != null) courses.add(c);
						}
						if (r.hasSecondAlternative()) {
							XCourseId c = server.getCourse(r.getSecondAlternative());
							if (c != null) courses.add(c);
						}
						if (courses.isEmpty()) continue;
						
						CourseDemand cd = null;
						adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
							CourseDemand adept = i.next();
							if (adept.getFreeTime() != null) continue;
							for (CourseRequest cr: adept.getCourseRequests())
								if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getCourseId())) {
									cd = adept; i.remove();  break adepts;
								}
						}
						if (cd == null) {
							cd = new CourseDemand();
							cd.setTimestamp(ts);
							cd.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
							cd.setCourseRequests(new HashSet<CourseRequest>());
							cd.setStudent(student);
							student.getCourseDemands().add(cd);
						}
						cd.setAlternative(true);
						cd.setPriority(priority);
						cd.setWaitlist(r.isWaitList());
						Iterator<CourseRequest> requests = new TreeSet<CourseRequest>(cd.getCourseRequests()).iterator();
						int order = 0;
						for (XCourseId co: courses) {
							CourseRequest cr = null;
							OnlineSectioningLog.CourseRequestOption.Builder option = options.get(co.getCourseId());
							if (requests.hasNext()) {
								cr = requests.next();
								if (cr.getCourseRequestOptions() != null && cr.getCourseRequestOptions().size() == 1 && option != null) {
									CourseRequestOption o = cr.getCourseRequestOptions().iterator().next();
									o.setOption(option.build());
								} else {
									if (cr.getCourseRequestOptions() != null) {
										for (Iterator<CourseRequestOption> i = cr.getCourseRequestOptions().iterator(); i.hasNext(); ) {
											helper.getHibSession().delete(i.next()); i.remove();
										}
									} else {
										cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
									}
									if (option != null) {
										CourseRequestOption o = new CourseRequestOption();
										o.setCourseRequest(cr);
										o.setOption(option.build());
										cr.getCourseRequestOptions().add(o);
									}
								}
								if (cr.getClassWaitLists() != null)
									for (Iterator<ClassWaitList> i = cr.getClassWaitLists().iterator(); i.hasNext(); ) {
										helper.getHibSession().delete(i.next());
										i.remove();
									}
							} else {
								cr = new CourseRequest();
								cd.getCourseRequests().add(cr);
								cr.setCourseDemand(cd);
								cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
								if (option != null) {
									CourseRequestOption o = new CourseRequestOption();
									o.setCourseRequest(cr);
									o.setOption(option.build());
									cr.getCourseRequestOptions().add(o);
								}
							}
							cr.setAllowOverlap(false);
							cr.setCredit(0);
							cr.setOrder(order++);
							if (cr.getCourseOffering() == null || !cr.getCourseOffering().getUniqueId().equals(co.getCourseId()))
								cr.setCourseOffering(CourseOfferingDAO.getInstance().get(co.getCourseId(), helper.getHibSession()));
							course2request.put(co.getCourseId(), cr);
						}
						while (requests.hasNext()) {
							CourseRequest cr = requests.next();
							cd.getCourseRequests().remove(cr);
							helper.getHibSession().delete(cr);
						}
						helper.getHibSession().saveOrUpdate(cd);
					}
					priority++;
				}
				
				Map<Long, StudentClassEnrollment> oldEnrollments = new HashMap<Long, StudentClassEnrollment>();
				Map<Long, Object[]> oldApprovals = new HashMap<Long, Object[]>();
				for (StudentClassEnrollment e: student.getClassEnrollments()) {
					oldEnrollments.put(e.getClazz().getUniqueId(), e);
					if (e.getApprovedBy() != null && !oldApprovals.containsKey(e.getCourseOffering().getUniqueId())) {
						oldApprovals.put(e.getCourseOffering().getUniqueId(), new Object[] {e.getApprovedBy(), e.getApprovedDate()});
					}
				}
				
				Map<Long, Class_> classes = new HashMap<Long, Class_>();
				String classIds = null;
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null || oldEnrollments.containsKey(ca.getClassId())) continue;
					if (classIds == null)
						classIds = ca.getClassId().toString();
					else
						classIds += "," + ca.getClassId();
				}
				if (classIds != null)
					for (Class_ clazz: (List<Class_>)helper.getHibSession().createQuery(
							"select c from Class_ c " +
							"left join fetch c.studentEnrollments as e " +
							"left join fetch c.schedulingSubpart as s " +
							"where c.uniqueId in (" + classIds + ")").list()) {
						classes.put(clazz.getUniqueId(), clazz);
					}
				
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
					CourseRequest cr = course2request.get(ca.getCourseId());
					if (cr == null) continue;
					
					StudentClassEnrollment enrl = oldEnrollments.remove(ca.getClassId());
					if (enrl != null) {
						if (!cr.equals(enrl.getCourseRequest())) {
							enrl.setCourseRequest(cr);
							helper.getHibSession().update(enrl);
						}
						continue;
					}

					Class_ clazz = classes.get(ca.getClassId());
					if (clazz == null) continue;

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

					enrl = new StudentClassEnrollment();
					enrl.setClazz(clazz);
					enrl.setStudent(student);
					enrl.setCourseOffering(cr.getCourseOffering());
					clazz.getStudentEnrollments().add(enrl);
					student.getClassEnrollments().add(enrl);
					enrl.setTimestamp(ts);
					enrl.setChangedBy(helper.getUser() == null ? null : helper.getUser().getExternalId());
					Object[] approval = oldApprovals.get(ca.getCourseId());
					if (approval != null) {
						enrl.setApprovedBy((String)approval[0]);
						enrl.setApprovedDate((Date)approval[1]);
					}
					enrl.setCourseRequest(cr);
				}
				
				for (CourseDemand cd: remaining) {
					if (cd.getFreeTime() != null)
						helper.getHibSession().delete(cd.getFreeTime());
					for (CourseRequest cr: cd.getCourseRequests())
						helper.getHibSession().delete(cr);
					student.getCourseDemands().remove(cd);
					helper.getHibSession().delete(cd);
				}
				
				for (StudentClassEnrollment enrl: oldEnrollments.values()) {
					enrl.getClazz().getStudentEnrollments().remove(enrl);
					student.getClassEnrollments().remove(enrl);
					helper.getHibSession().delete(enrl);
				}
				
				helper.getHibSession().saveOrUpdate(student);
				
				// Reload student
				XStudent newStudent = new XStudent(oldStudent, student.getCourseDemands(), helper, server.getAcademicSession().getFreeTimePattern());
		    	for (XRequest request: newStudent.getRequests()) {
		    		if (request instanceof XCourseRequest) {
		    			XCourseRequest courseRequest = (XCourseRequest)request;
		    			XEnrollment enrollment = courseRequest.getEnrollment();
		    			if (enrollment != null && enrollment.getReservation() == null) {
		    				XOffering offering = server.getOffering(enrollment.getOfferingId());
		    				if (offering != null && !offering.getReservations().isEmpty())
		    					enrollment.setReservation(offering.guessReservation(server.getRequests(enrollment.getOfferingId()), newStudent, enrollment));
		    			}
		    		}
		    	}
		    	server.update(newStudent, true);

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
						helper.debug("Check offering for " + oldEnrollment.getCourseName() + ": there are reservations.");
					} else {
						XEnrollments enrollments = server.getEnrollments(oldEnrollment.getOfferingId());
						for (Long sectionId: oldSections) {
							XSection section = offering.getSection(sectionId);
							if (section != null && section.getLimit() >= 0 && section.getLimit() - enrollments.countEnrollmentsForSection(sectionId) == 1) {
								checkOffering = true;
								helper.debug("Check offering for " + oldEnrollment.getCourseName() + ": section " + section + " became available.");
								break;
							}
						}
						if (!checkOffering && (newEnrollment == null || !newEnrollment.getConfigId().equals(oldEnrollment.getConfigId()))) {
							XConfig config = offering.getConfig(oldEnrollment.getConfigId());
							if (config != null && config.getLimit() >= 0 && config.getLimit() - enrollments.countEnrollmentsForConfig(config.getConfigId()) == 1) {
								checkOffering = true;
								helper.debug("Check offering for " + oldEnrollment.getCourseName() + ": config " + config + " became available.");
							}
						}
						if (!checkOffering && (newEnrollment == null || !newEnrollment.getCourseId().equals(oldEnrollment.getCourseId()))) {
							XCourse course = offering.getCourse(oldEnrollment.getCourseId());
							if (course != null && course.getLimit() >= 0 && course.getLimit() - enrollments.countEnrollmentsForCourse(course.getCourseId()) == 1) {
								checkOffering = true;
								helper.debug("Check offering for " + oldEnrollment.getCourseName() + ": course " + course + " became available.");
							}
						}
					}
					
					if (checkOffering)
						server.execute(server.createAction(CheckOfferingAction.class).forOfferings(oldEnrollment.getOfferingId()), helper.getUser(), offeringChecked);
					
					updateSpace(server,
							newEnrollment == null ? null : SectioningRequest.convert(newStudent, newRequest, server, offering, newEnrollment),
							oldEnrollment == null ? null : SectioningRequest.convert(oldStudent, (XCourseRequest)oldRequest, server, offering, oldEnrollment),
							offering);
					server.persistExpectedSpaces(oldEnrollment.getOfferingId());
				}
				OnlineSectioningLog.Enrollment.Builder previous = OnlineSectioningLog.Enrollment.newBuilder();
				previous.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				for (XRequest oldRequest: oldStudent.getRequests()) {
					XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
					if (oldEnrollment != null)
						for (XSection section: server.getOffering(oldEnrollment.getOfferingId()).getSections(oldEnrollment))
							previous.addSection(OnlineSectioningHelper.toProto(section, oldEnrollment));
				}
				action.addEnrollment(previous);
				
				requests: for (XRequest newRequest: newStudent.getRequests()) {
					XEnrollment newEnrollment = (newRequest instanceof XCourseRequest ? ((XCourseRequest)newRequest).getEnrollment() : null);
					if (newEnrollment == null) continue; // free time or not assigned
					if (oldStudent != null)
						for (XRequest oldRequest: oldStudent.getRequests()) {
							XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
							if (oldEnrollment != null && oldEnrollment.getOfferingId().equals(newEnrollment.getOfferingId()))
								continue requests;
						}
					XOffering offering = server.getOffering(newEnrollment.getOfferingId());
					updateSpace(server,
							SectioningRequest.convert(newStudent, (XCourseRequest)newRequest, server, offering, newEnrollment),
							null, offering);
					server.persistExpectedSpaces(newEnrollment.getOfferingId());
				}
				OnlineSectioningLog.Enrollment.Builder stored = OnlineSectioningLog.Enrollment.newBuilder();
				stored.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
				for (XRequest newRequest: newStudent.getRequests()) {
					XEnrollment newEnrollment = (newRequest instanceof XCourseRequest ? ((XCourseRequest)newRequest).getEnrollment() : null);
					if (newEnrollment != null)
						for (XSection section: server.getOffering(newEnrollment.getOfferingId()).getSections(newEnrollment))
							stored.addSection(OnlineSectioningHelper.toProto(section, newEnrollment));
				}
				action.addEnrollment(stored);
				
				server.execute(server.createAction(NotifyStudentAction.class).forStudent(getStudentId()).oldStudent(oldStudent), helper.getUser());
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				helper.error("Failed to enroll student " + getStudentId() + ": " + e.getMessage(), e);
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}
		
		return server.execute(server.createAction(GetAssignment.class).forStudent(getStudentId()).withMessages(failures), helper.getUser());
	}
	
	public static int getLimit(Enrollment enrollment, Map<Long, XSection> sections) {
		Integer limit = null;
		for (Section s: enrollment.getSections()) {
			XSection section = sections.get(s.getId());
			if (section != null && section.getLimit() >= 0) {
				if (limit == null)
					limit = section.getLimit();
				else
					limit = Math.min(limit, section.getLimit());
			}
		}
		return (limit == null ? -1 : limit);
	}
	
	public static void updateSpace(OnlineSectioningServer server, Enrollment newEnrollment, Enrollment oldEnrollment, XOffering offering) {
		updateSpace(server, newEnrollment, oldEnrollment, offering, offering);
	}
	
    public static void updateSpace(OnlineSectioningServer server, Enrollment newEnrollment, Enrollment oldEnrollment, XOffering newOffering, XOffering oldOffering) {
    	if (newEnrollment == null && oldEnrollment == null) return;
    	XExpectations expectations = server.getExpectations((newEnrollment == null ? oldEnrollment : newEnrollment).getOffering().getId());
    	Assignment<Request, Enrollment> assignment = new DefaultSingleAssignment<Request, Enrollment>();
    	if (oldEnrollment != null) {
        	Map<Long, XSection> sections = new HashMap<Long, XSection>();
        	if (oldOffering != null)
            	for (XConfig config: oldOffering.getConfigs())
            		for (XSubpart subpart: config.getSubparts())
            			for (XSection section: subpart.getSections())
            				sections.put(section.getSectionId(), section);
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            int totalLimit = 0;
            for (Enrollment enrl : oldEnrollment.getRequest().values(assignment)) {
            	if (!enrl.getCourse().equals(oldEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : oldEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(oldEnrollment.getRequest()) || !(otherRequest instanceof org.cpsolver.studentsct.model.CourseRequest))
                        continue;
                    Enrollment otherErollment = otherRequest.getInitialAssignment();
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
                    	int limit = getLimit(enrl, sections);
                        if (limit < 0) totalLimit = -1;
                        else totalLimit += limit;
                    }
                }
            }
            double increment = 1.0 / (totalLimit > 0 ? totalLimit : feasibleEnrollments.size());
            for (Enrollment feasibleEnrollment : feasibleEnrollments)
                for (Section section : feasibleEnrollment.getSections()) {
                	if (totalLimit > 0) {
                		expectations.incExpectedSpace(section.getId(), increment * getLimit(feasibleEnrollment, sections));
                    } else {
                    	expectations.incExpectedSpace(section.getId(), increment);
                    }
                }
    	}
    	if (newEnrollment != null) {
        	Map<Long, XSection> sections = new HashMap<Long, XSection>();
        	if (newOffering != null)
            	for (XConfig config: newOffering.getConfigs())
            		for (XSubpart subpart: config.getSubparts())
            			for (XSection section: subpart.getSections())
            				sections.put(section.getSectionId(), section);
            for (Section section : newEnrollment.getSections())
                section.setSpaceHeld(section.getSpaceHeld() - 1.0);
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            int totalLimit = 0;
            for (Enrollment enrl : newEnrollment.getRequest().values(assignment)) {
            	if (!enrl.getCourse().equals(newEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : newEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(newEnrollment.getRequest()) || !(otherRequest instanceof org.cpsolver.studentsct.model.CourseRequest))
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
                    	int limit = getLimit(enrl, sections);
                        if (limit < 0) totalLimit = -1;
                        else totalLimit += limit;
                    }
                }
            }
            double decrement = 1.0 / (totalLimit > 0 ? totalLimit : feasibleEnrollments.size());
            for (Enrollment feasibleEnrollment : feasibleEnrollments)
                for (Section section : feasibleEnrollment.getSections()) {
                	if (totalLimit > 0) {
                		expectations.incExpectedSpace(section.getId(), - decrement * getLimit(feasibleEnrollment, sections));
                    } else {
                    	expectations.incExpectedSpace(section.getId(), - decrement);
                    }
                }
    	}
    	server.update(expectations);
    }
	
	@Override
	public String name() {
		return "enroll";
	}

	@Override
	public CacheMode getCacheMode() {
		return CacheMode.IGNORE;
	}
}
