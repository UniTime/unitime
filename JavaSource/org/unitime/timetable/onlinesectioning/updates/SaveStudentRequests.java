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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class SaveStudentRequests implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private CourseRequestInterface iRequest;
	private boolean iKeepEnrollments;
	
	public SaveStudentRequests forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public SaveStudentRequests withRequest(CourseRequestInterface request, boolean keepEnrollments) {
		iRequest = request;
		iKeepEnrollments = keepEnrollments;
		return this;
	}
	
	public SaveStudentRequests withRequest(CourseRequestInterface request) {
		return withRequest(request, true);
	}

	public Long getStudentId() { return iStudentId; }
	public CourseRequestInterface getRequest() { return iRequest; }
	public boolean getKeepEnrollments() { return iKeepEnrollments; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.lockStudent(getStudentId(), null, true);
		try {
			helper.beginTransaction();
			try {
				Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
				
				OnlineSectioningLog.Action.Builder action = helper.getAction();
				
				if (getRequest().getStudentId() != null)
					action.setStudent(
							OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(getStudentId()));
				
				// Save requests
				saveRequest(server, helper, student, getRequest(), getKeepEnrollments());
				
				// Reload student
				XStudent oldStudent = server.getStudent(getStudentId());
				XStudent newStudent = null;
				try {
					newStudent = ReloadAllData.loadStudentNoCheck(student, server, helper);
					server.update(newStudent, true);
					action.getStudentBuilder()
						.setUniqueId(newStudent.getStudentId())
						.setExternalId(newStudent.getExternalId())
						.setName(newStudent.getName());
					
					for (XRequest r: newStudent.getRequests())
						action.addRequest(OnlineSectioningHelper.toProto(r));
						
				} catch (Exception e) {
					if (e instanceof RuntimeException)
						throw (RuntimeException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				}
				server.execute(server.createAction(NotifyStudentAction.class).forStudent(getStudentId()).oldStudent(oldStudent), helper.getUser());
				
				helper.commitTransaction();
				
				return true;
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "save-request";
	}
	
	public static CourseOffering getCourse(org.hibernate.Session hibSession, Long sessionId, String courseName) {
		for (CourseOffering co: (List<CourseOffering>)hibSession.createQuery(
				"select c from CourseOffering c where " +
				"c.subjectArea.session.uniqueId = :sessionId and " +
				"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
				.setString("course", courseName.toLowerCase())
				.setLong("sessionId", sessionId)
				.setCacheable(true).setMaxResults(1).list()) {
			return co;
		}
		return null;
	}
	
	private static CourseOffering getCourse(org.hibernate.Session hibSession, long courseId) {
		return CourseOfferingDAO.getInstance().get(courseId, hibSession);
	}
	
	public static Map<Long, CourseRequest>  saveRequest(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, CourseRequestInterface request, boolean keepEnrollments) throws SectioningException {
		Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
		int priority = 0;
		Date ts = new Date();
		Map<Long, CourseRequest> course2request = new HashMap<Long, CourseRequest>();
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedFreeTime() && r.hasRequestedCourse() && ((server == null && getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getRequestedCourse()) != null) || (server != null && server.getCourse(r.getRequestedCourse()) != null)))
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
			} else if (r.hasRequestedCourse() || r.hasFirstAlternative() || r.hasSecondAlternative()) {
				List<CourseOffering> courses = new ArrayList<CourseOffering>();
				if (r.hasRequestedCourse()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getRequestedCourse()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getRequestedCourse()) : getCourse(helper.getHibSession(), c.getCourseId()));
					if (co != null) courses.add(co);
				}
				if (r.hasFirstAlternative()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getFirstAlternative()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getFirstAlternative()) : getCourse(helper.getHibSession(), c.getCourseId()));
					if (co != null) courses.add(co);
				}
				if (r.hasSecondAlternative()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getSecondAlternative()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getSecondAlternative()) : getCourse(helper.getHibSession(), c.getCourseId()));
					if (co != null) courses.add(co);
				}
				if (courses.isEmpty()) continue;
				
				CourseDemand cd = null;
				adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
					CourseDemand adept = i.next();
					if (adept.getFreeTime() != null) continue;
					for (CourseRequest cr: adept.getCourseRequests())
						if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getUniqueId())) {
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
				for (CourseOffering co: courses) {
					CourseRequest cr = null;
					if (requests.hasNext()) {
						cr = requests.next();
						if (cr.getCourseRequestOptions() != null) {
							for (Iterator<CourseRequestOption> i = cr.getCourseRequestOptions().iterator(); i.hasNext(); )
								helper.getHibSession().delete(i.next());
							cr.getCourseRequestOptions().clear();
						}
					} else {
						cr = new CourseRequest();
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
					}
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(order++);
					cr.setCourseOffering(co);
					course2request.put(co.getUniqueId(), cr);
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
		
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedFreeTime() && r.hasRequestedCourse() && ((server == null && getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getRequestedCourse()) != null) || (server != null && server.getCourse(r.getRequestedCourse()) != null)))
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
			} else if (r.hasRequestedCourse() || r.hasFirstAlternative() || r.hasSecondAlternative()) {
				List<CourseOffering> courses = new ArrayList<CourseOffering>();
				if (r.hasRequestedCourse()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getRequestedCourse()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getRequestedCourse()) : getCourse(helper.getHibSession(), c.getCourseId()));
					if (co != null) courses.add(co);
				}
				if (r.hasFirstAlternative()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getFirstAlternative()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getFirstAlternative()) : getCourse(helper.getHibSession(), c.getCourseId()));
					if (co != null) courses.add(co);
				}
				if (r.hasSecondAlternative()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getSecondAlternative()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getSecondAlternative()) : getCourse(helper.getHibSession(), c.getCourseId()));
					if (co != null) courses.add(co);
				}
				if (courses.isEmpty()) continue;
				
				CourseDemand cd = null;
				adepts: for (Iterator<CourseDemand> i = remaining.iterator(); i.hasNext(); ) {
					CourseDemand adept = i.next();
					if (adept.getFreeTime() != null) continue;
					for (CourseRequest cr: adept.getCourseRequests())
						if (cr.getCourseOffering().getUniqueId().equals(courses.get(0).getUniqueId())) {
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
				for (CourseOffering co: courses) {
					CourseRequest cr = null;
					if (requests.hasNext()) {
						cr = requests.next();
						if (cr.getCourseRequestOptions() != null) {
							for (Iterator<CourseRequestOption> i = cr.getCourseRequestOptions().iterator(); i.hasNext(); )
								helper.getHibSession().delete(i.next());
							cr.getCourseRequestOptions().clear();
						}
					} else {
						cr = new CourseRequest();
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
					}
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(order++);
					cr.setCourseOffering(co);
					course2request.put(co.getUniqueId(), cr);
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
		
		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
			StudentClassEnrollment enrl = i.next();
			if (keepEnrollments) {
				CourseRequest cr = course2request.get(enrl.getCourseOffering().getUniqueId());
				if (cr == null) {
					enrl.setCourseRequest(null);
					helper.getHibSession().saveOrUpdate(enrl);
				} else {
					enrl.setCourseRequest(cr);
					helper.getHibSession().saveOrUpdate(enrl);
				}
			} else {
				enrl.getClazz().getStudentEnrollments().remove(enrl);
				helper.getHibSession().delete(enrl);
				i.remove();
			}
		}
		
		if (!keepEnrollments) {
			for (CourseDemand cd: student.getCourseDemands())
				if (cd.getCourseRequests() != null)
					for (CourseRequest cr: cd.getCourseRequests())
						if (cr.getClassWaitLists() != null)
							for (Iterator<ClassWaitList> i = cr.getClassWaitLists().iterator(); i.hasNext(); ) {
								helper.getHibSession().delete(i.next());
								i.remove();
							}
		}
		
		for (CourseDemand cd: remaining) {
			if (cd.getFreeTime() != null)
				helper.getHibSession().delete(cd.getFreeTime());
			for (CourseRequest cr: cd.getCourseRequests())
				helper.getHibSession().delete(cr);
			student.getCourseDemands().remove(cd);
			helper.getHibSession().delete(cd);
		}
		
		helper.getHibSession().saveOrUpdate(student);
		helper.getHibSession().flush();
		
		return course2request;
	}
	
}