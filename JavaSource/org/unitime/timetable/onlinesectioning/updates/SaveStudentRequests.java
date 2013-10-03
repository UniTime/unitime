/*
 * UniTime 3.2 (University Timetabling Application)
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

/**
 * @author Tomas Muller
 */
public class SaveStudentRequests implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private CourseRequestInterface iRequest;
	private boolean iKeepEnrollments;
	
	public SaveStudentRequests(Long studentId, CourseRequestInterface request, boolean keepEnrollments) {
		iStudentId = studentId;
		iRequest = request;
		iKeepEnrollments = keepEnrollments;
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
					newStudent = ReloadAllData.loadStudent(student, null, server, helper);
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
				server.execute(new NotifyStudentAction(getStudentId(), oldStudent), helper.getUser());
				
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

	
	public static Map<Long, CourseRequest>  saveRequest(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, CourseRequestInterface request, boolean keepEnrollments) throws SectioningException {
		Set<CourseDemand> remaining = new TreeSet<CourseDemand>(student.getCourseDemands());
		int priority = 0;
		Date ts = new Date();
		Map<Long, CourseRequest> course2request = new HashMap<Long, CourseRequest>();
		List<CourseRequest> unusedRequests = new ArrayList<CourseRequest>();
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
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getRequestedCourse()) : CourseOfferingDAO.getInstance().get(c.getCourseId(), helper.getHibSession()));
					if (co != null) courses.add(co);
				}
				if (r.hasFirstAlternative()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getFirstAlternative()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getFirstAlternative()) : CourseOfferingDAO.getInstance().get(c.getCourseId(), helper.getHibSession()));
					if (co != null) courses.add(co);
				}
				if (r.hasSecondAlternative()) {
					XCourseId c = (server == null ? null : server.getCourse(r.getSecondAlternative()));
					CourseOffering co = (c == null ? getCourse(helper.getHibSession(), request.getAcademicSessionId(), r.getSecondAlternative()) : CourseOfferingDAO.getInstance().get(c.getCourseId(), helper.getHibSession()));
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
						/*
						if (cr.getClassEnrollments() != null)
							cr.getClassEnrollments().clear();
							*/
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
						// cr.setClassEnrollments(new HashSet<StudentClassEnrollment>());
					}
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(order++);
					cr.setCourseOffering(co);
					course2request.put(co.getUniqueId(), cr);
				}
				while (requests.hasNext()) {
					unusedRequests.add(requests.next());
					requests.remove();
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
					/*
					if (cr.getClassEnrollments() == null)
						cr.setClassEnrollments(new HashSet<StudentClassEnrollment>());
					cr.getClassEnrollments().add(enrl);
					*/
					helper.getHibSession().saveOrUpdate(enrl);
				}
			} else {
				enrl.getClazz().getStudentEnrollments().remove(enrl);
				/*
				if (enrl.getCourseRequest() != null)
					enrl.getCourseRequest().getClassEnrollments().remove(enrl);
					*/
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
		
		for (CourseRequest cr: unusedRequests)
			helper.getHibSession().delete(cr);
		
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