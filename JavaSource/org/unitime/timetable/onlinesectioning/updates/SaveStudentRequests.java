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

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningService;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction.DatabaseAction;

/**
 * @author Tomas Muller
 */
public class SaveStudentRequests extends DatabaseAction<Hashtable<Long, CourseRequest>>{
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
	public Hashtable<Long, CourseRequest> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
		if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
		return saveRequest(helper.getHibSession(), student, getRequest(), getKeepEnrollments());
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

	
	public static Hashtable<Long, CourseRequest> saveRequest(org.hibernate.Session hibSession, Student student, CourseRequestInterface request, boolean keepEnrollments) throws SectioningException {
		Hashtable<Long, CourseRequest> ret = new Hashtable<Long, CourseRequest>();
		OnlineSectioningServer server = OnlineSectioningService.getInstance(student.getSession().getUniqueId());
		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
			StudentClassEnrollment enrl = i.next();
			if (keepEnrollments) {
				if (enrl.getCourseRequest() != null) {
					enrl.getCourseRequest().getClassEnrollments().remove(enrl);
					enrl.setCourseRequest(null);
					hibSession.save(enrl);
				}
			} else {
				enrl.getClazz().getStudentEnrollments().remove(enrl);
				hibSession.delete(enrl);
			}
		}
		if (!keepEnrollments) student.getClassEnrollments().clear();
		for (Iterator<CourseDemand> j =  student.getCourseDemands().iterator(); j.hasNext(); ) {
			CourseDemand cd = j.next();
			if (cd.getFreeTime() != null) hibSession.delete(cd.getFreeTime());
			for (Iterator<CourseRequest> k = cd.getCourseRequests().iterator(); k.hasNext(); ) {
				CourseRequest cr = k.next();
				hibSession.delete(cr);
			}
			hibSession.delete(cd);
		}
		student.getCourseDemands().clear();
		int priority = 0;
		Date ts = new Date();
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedFreeTime()) {
				for (CourseRequestInterface.FreeTime ft: r.getRequestedFreeTime()) {
					CourseDemand cd = new CourseDemand();
					cd.setAlternative(false);
					cd.setPriority(priority);
					cd.setTimestamp(ts);
					cd.setWaitlist(false);
					FreeTime free = new FreeTime();
					free.setCategory(0);
					free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
					free.setStartSlot(ft.getStart());
					free.setLength(ft.getLength());
					free.setSession(student.getSession());
					free.setName(ft.toString());
					hibSession.saveOrUpdate(free);
					cd.setFreeTime(free);
					cd.setStudent(student);
					student.getCourseDemands().add(cd);
				}
			} else {
				CourseDemand cd = new CourseDemand();
				cd.setAlternative(false);
				cd.setPriority(priority);
				cd.setTimestamp(ts);
				cd.setWaitlist(false);
				cd.setCourseRequests(new HashSet<CourseRequest>());
				if (r.hasRequestedCourse()) {
					CourseInfo c = (server == null ? null : server.getCourseInfo(r.getRequestedCourse()));
					CourseOffering co = (c == null ? getCourse(hibSession, request.getAcademicSessionId(), r.getRequestedCourse()) : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
					if (co != null) {
						CourseRequest cr = new CourseRequest();
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(0);
						cr.setCourseOffering(co);
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						ret.put(co.getUniqueId(), cr);
					}
				}
				if (r.hasFirstAlternative()) {
					CourseInfo c = (server == null ? null : server.getCourseInfo(r.getFirstAlternative()));
					CourseOffering co = (c == null ? getCourse(hibSession, request.getAcademicSessionId(), r.getFirstAlternative()) : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
					if (co != null) {
						CourseRequest cr = new CourseRequest();
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(1);
						cr.setCourseOffering(co);
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						ret.put(co.getUniqueId(), cr);
					}
				}
				if (r.hasSecondAlternative()) {
					CourseInfo c = (server == null ? null : server.getCourseInfo(r.getSecondAlternative()));
					CourseOffering co = (c == null ? getCourse(hibSession, request.getAcademicSessionId(), r.getSecondAlternative()) : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
					if (co != null) {
						CourseRequest cr = new CourseRequest();
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(2);
						cr.setCourseOffering(co);
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						ret.put(co.getUniqueId(), cr);
					}
				}
				if (cd.getCourseRequests().isEmpty()) continue;
				cd.setStudent(student);
				student.getCourseDemands().add(cd);
			}
			priority++;
		}
		priority = 0;
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			CourseDemand cd = new CourseDemand();
			cd.setAlternative(true);
			cd.setPriority(priority);
			cd.setTimestamp(ts);
			cd.setWaitlist(false);
			cd.setCourseRequests(new HashSet<CourseRequest>());
			if (r.hasRequestedCourse()) {
				CourseInfo c = (server == null ? null : server.getCourseInfo(r.getRequestedCourse()));
				CourseOffering co = (c == null ? getCourse(hibSession, request.getAcademicSessionId(), r.getRequestedCourse()) : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
				if (co != null) {
					CourseRequest cr = new CourseRequest();
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(0);
					cr.setCourseOffering(co);
					cd.getCourseRequests().add(cr);
					cr.setCourseDemand(cd);
					ret.put(co.getUniqueId(), cr);
				}
			}
			if (r.hasFirstAlternative()) {
				CourseInfo c = (server == null ? null : server.getCourseInfo(r.getFirstAlternative()));
				CourseOffering co = (c == null ? getCourse(hibSession, request.getAcademicSessionId(), r.getFirstAlternative()) : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
				if (co != null) {
					CourseRequest cr = new CourseRequest();
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(1);
					cr.setCourseOffering(co);
					cd.getCourseRequests().add(cr);
					cr.setCourseDemand(cd);
					ret.put(co.getUniqueId(), cr);
				}
			}
			if (r.hasSecondAlternative()) {
				CourseInfo c = (server == null ? null : server.getCourseInfo(r.getSecondAlternative()));
				CourseOffering co = (c == null ? getCourse(hibSession, request.getAcademicSessionId(), r.getSecondAlternative()) : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
				if (co != null) {
					CourseRequest cr = new CourseRequest();
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(2);
					cr.setCourseOffering(co);
					cr.setCourseDemand(cd);
					cd.getCourseRequests().add(cr);
					ret.put(co.getUniqueId(), cr);
				}
			}
			if (cd.getCourseRequests().isEmpty()) continue;
			cd.setStudent(student);
			student.getCourseDemands().add(cd);
			priority++;
		}
		return ret;
	}

}