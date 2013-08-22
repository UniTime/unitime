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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.Comparator;
import java.util.TreeSet;

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class GetRequest implements OnlineSectioningAction<CourseRequestInterface> {
	private static final long serialVersionUID = 1L;
	
	private Long iStudentId;
	
	public GetRequest(Long studentId) {
		iStudentId = studentId;
	}

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			Student student = server.getStudent(iStudentId);
			if (student == null) return null;
			CourseRequestInterface request = new CourseRequestInterface();
			request.setStudentId(iStudentId);
			request.setSaved(true);
			request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
			TreeSet<Request> requests = new TreeSet<Request>(new Comparator<Request>() {
				public int compare(Request d1, Request d2) {
					if (d1.isAlternative() && !d2.isAlternative()) return 1;
					if (!d1.isAlternative() && d2.isAlternative()) return -1;
					int cmp = new Integer(d1.getPriority()).compareTo(d2.getPriority());
					if (cmp != 0) return cmp;
					return new Long(d1.getId()).compareTo(d2.getId());
				}
			});
			requests.addAll(student.getRequests());
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			for (Request cd: requests) {
				CourseRequestInterface.Request r = null;
				if (cd instanceof FreeTimeRequest) {
					FreeTimeRequest ftr = (FreeTimeRequest)cd;
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setStart(ftr.getTime().getStartSlot());
					ft.setLength(ftr.getTime().getLength());
					for (DayCode day : DayCode.toDayCodes(ftr.getTime().getDayCode()))
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
				} else if (cd instanceof CourseRequest) {
					r = new CourseRequestInterface.Request();
					int order = 0;
					for (Course course: ((CourseRequest)cd).getCourses()) {
						CourseInfo c = server.getCourseInfo(course.getId());
						if (c == null) continue;
						switch (order) {
							case 0: 
								r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
								break;
							case 1:
								r.setFirstAlternative(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
								break;
							case 2:
								r.setSecondAlternative(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
							}
						order++;
						}
					r.setWaitList(((CourseRequest)cd).isWaitlist());
					if (r.hasRequestedCourse()) {
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
					}
					lastRequest = r;
					lastRequestPriority = cd.getPriority();
				}
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
