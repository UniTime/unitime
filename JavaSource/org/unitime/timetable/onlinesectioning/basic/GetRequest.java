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
package org.unitime.timetable.onlinesectioning.basic;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class GetRequest implements OnlineSectioningAction<CourseRequestInterface> {
	private static final long serialVersionUID = 1L;
	
	private Long iStudentId;
	
	public GetRequest forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
			XStudent student = server.getStudent(iStudentId);
			if (student == null) return null;
			CourseRequestInterface request = new CourseRequestInterface();
			action.getStudentBuilder().setExternalId(student.getExternalId());
			action.getStudentBuilder().setName(student.getName());
			request.setStudentId(iStudentId);
			request.setSaved(true);
			request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			for (XRequest cd: student.getRequests()) {
				CourseRequestInterface.Request r = null;
				if (cd instanceof XFreeTimeRequest) {
					XFreeTimeRequest ftr = (XFreeTimeRequest)cd;
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setStart(ftr.getTime().getSlot());
					ft.setLength(ftr.getTime().getLength());
					for (DayCode day : DayCode.toDayCodes(ftr.getTime().getDays()))
						ft.addDay(day.getIndex());
					if (lastRequest != null && lastRequestPriority == cd.getPriority()) {
						r = lastRequest;
						lastRequest.addRequestedFreeTime(ft);
						lastRequest.setRequestedCourse(lastRequest.getRequestedCourse() + ", " + ft.toString());
					} else {
						r = new CourseRequestInterface.Request();
						r.addRequestedFreeTime(ft);
						r.setRequestedCourse(ft.toString());
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
				} else if (cd instanceof XCourseRequest) {
					r = new CourseRequestInterface.Request();
					int order = 0;
					for (XCourseId courseId: ((XCourseRequest)cd).getCourseIds()) {
						XCourse c = server.getCourse(courseId.getCourseId());
						if (c == null) continue;
						switch (order) {
							case 0: 
								r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
								break;
							case 1:
								r.setFirstAlternative(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
								break;
							case 2:
								r.setSecondAlternative(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
							}
						order++;
						}
					r.setWaitList(((XCourseRequest)cd).isWaitlist());
					if (r.hasRequestedCourse()) {
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					lastRequest = r;
					lastRequestPriority = cd.getPriority();
				}
				action.addRequest(OnlineSectioningHelper.toProto(cd));
			}
			return request;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "get-request";
	}
	

}
