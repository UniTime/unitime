/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerContext;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.match.StudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class InMemoryServer extends AbstractLockingServer {
	private Hashtable<Long, XCourseId> iCourseForId = new Hashtable<Long, XCourseId>();
	private Hashtable<String, TreeSet<XCourseId>> iCourseForName = new Hashtable<String, TreeSet<XCourseId>>();
	
	private Hashtable<Long, XStudent> iStudentTable = new Hashtable<Long, XStudent>();
	private Hashtable<Long, XOffering> iOfferingTable = new Hashtable<Long, XOffering>();
	private Hashtable<Long, List<XCourseRequest>> iOfferingRequests = new Hashtable<Long, List<XCourseRequest>>();
	private Hashtable<Long, XExpectations> iExpectations = new Hashtable<Long, XExpectations>();
	
	public InMemoryServer(OnlineSectioningServerContext context) throws SectioningException {
		super(context);
	}

	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		Lock lock = readLock();
		try {
			Set<XCourseId> ret = new TreeSet<XCourseId>();
			String queryInLowerCase = query.toLowerCase();
			for (XCourseId c : iCourseForId.values()) {
				if (c.matchCourseName(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
				if (limit != null && ret.size() == limit) return ret;
			}
			if (queryInLowerCase.length() > 2) {
				for (XCourseId c : iCourseForId.values()) {
					if (c.matchTitle(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
					if (limit != null && ret.size() == limit) return ret;
				}
			}
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public Collection<XCourseId> findCourses(CourseMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		Lock lock = readLock();
		try {
			Set<XCourseId> ret = new TreeSet<XCourseId>();
			for (XCourseId c : iCourseForId.values()) {
				if (matcher.match(c)) ret.add(c);
			}
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public Collection<XStudent> findStudents(StudentMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		Lock lock = readLock();
		try {
			List<XStudent> ret = new ArrayList<XStudent>();
			for (XStudent s: iStudentTable.values())
				if (matcher.match(s)) ret.add(s);
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public XCourseId getCourse(String course) {
		Lock lock = readLock();
		try {
			if (course.indexOf('-') >= 0) {
				String courseName = course.substring(0, course.indexOf('-')).trim();
				String title = course.substring(course.indexOf('-') + 1).trim();
				TreeSet<XCourseId> infos = iCourseForName.get(courseName.toLowerCase());
				if (infos!= null && !infos.isEmpty())
					for (XCourseId info: infos)
						if (title.equalsIgnoreCase(info.getTitle())) return info;
				return null;
			} else {
				TreeSet<XCourseId> infos = iCourseForName.get(course.toLowerCase());
				if (infos!= null && !infos.isEmpty()) return infos.first();
				return null;
			}
		} finally {
			lock.release();
		}
	}
	
	private XCourse toCourse(XCourseId course) {
		if (course == null) return null;
		if (course instanceof XCourse)
			return (XCourse)course;
		XOffering offering = getOffering(course.getOfferingId());
		return offering == null ? null : offering.getCourse(course);
	}
	
	@Override
	public XCourse getCourse(Long courseId) {
		Lock lock = readLock();
		try {
			return toCourse(iCourseForId.get(courseId));
		} finally {
			lock.release();
		}
	}

	@Override
	public XStudent getStudent(Long studentId) {
		Lock lock = readLock();
		try {
			return iStudentTable.get(studentId);
		} finally {
			lock.release();
		}
	}

	@Override
	public XOffering getOffering(Long offeringId) {
		Lock lock = readLock();
		try {
			return iOfferingTable.get(offeringId);
		} finally {
			lock.release();
		}
	}

	@Override
	public Collection<XCourseRequest> getRequests(Long offeringId) {
		Lock lock = readLock();
		try {
			return iOfferingRequests.get(offeringId);
		} finally {
			lock.release();
		}		
	}

	@Override
	public XExpectations getExpectations(Long offeringId) {
		Lock lock = readLock();
		try {
			XExpectations expectations = iExpectations.get(offeringId);
			return expectations == null ? new XExpectations(offeringId) : expectations;
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(XExpectations expectations) {
		Lock lock = writeLock();
		try {
			iExpectations.put(expectations.getOfferingId(), expectations);
		} finally {
			lock.release();
		}
	}

	@Override
	public void remove(XStudent student) {
		Lock lock = writeLock();
		try {
			XStudent oldStudent = iStudentTable.remove(student.getStudentId());
			if (oldStudent != null) {
				for (XRequest request: oldStudent.getRequests())
					if (request instanceof XCourseRequest)
						for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
							List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
							if (requests != null) requests.remove(request);
						}
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(XStudent student, boolean updateRequests) {
		Lock lock = writeLock();
		try {
			XStudent oldStudent = iStudentTable.put(student.getStudentId(), student);
			if (updateRequests) {
				if (oldStudent != null) {
					for (XRequest request: oldStudent.getRequests())
						if (request instanceof XCourseRequest)
							for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
								List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
								if (requests != null) requests.remove(request);
							}
				}
				for (XRequest request: student.getRequests())
					if (request instanceof XCourseRequest)
						for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
							List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
							if (requests == null) {
								requests = new ArrayList<XCourseRequest>();
								iOfferingRequests.put(course.getOfferingId(), requests);
							}
							requests.add((XCourseRequest)request);
						}
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void remove(XOffering offering) {
		remove(offering, true);
	}
	
	protected void remove(XOffering offering, boolean removeExpectations) {
		Lock lock = writeLock();
		try {
			for (XCourse course: offering.getCourses()) {
				iCourseForId.remove(course.getCourseId());
				TreeSet<XCourseId> courses = iCourseForName.get(course.getCourseNameInLowerCase());
				if (courses != null) {
					courses.remove(course);
					if (courses.size() == 1) 
						for (XCourseId x: courses) x.setHasUniqueName(true);
					if (courses.isEmpty())
						iCourseForName.remove(course.getCourseNameInLowerCase());
				}
			}
			iOfferingTable.remove(offering.getOfferingId());
			if (removeExpectations)
				iExpectations.remove(offering.getOfferingId());
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(XOffering offering) {
		Lock lock = writeLock();
		try {
			XOffering oldOffering = iOfferingTable.get(offering.getOfferingId());
			if (oldOffering != null)
				remove(oldOffering, false);
			
			iOfferingTable.put(offering.getOfferingId(), offering);
			for (XCourse course: offering.getCourses()) {
				iCourseForId.put(course.getCourseId(), course);
				TreeSet<XCourseId> courses = iCourseForName.get(course.getCourseNameInLowerCase());
				if (courses == null) {
					courses = new TreeSet<XCourseId>();
					iCourseForName.put(course.getCourseNameInLowerCase(), courses);
				}
				courses.add(course);
				if (courses.size() == 1) 
					for (XCourseId x: courses) x.setHasUniqueName(true);
				else if (courses.size() > 1)
					for (XCourseId x: courses) x.setHasUniqueName(false);
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void clearAll() {
		Lock lock = writeLock();
		try {
			iStudentTable.clear();
			iOfferingTable.clear();
			iCourseForId.clear();
			iCourseForName.clear();
			iOfferingRequests.clear();
		} finally {
			lock.release();
		}
	}

	@Override
	public void clearAllStudents() {
		Lock lock = writeLock();
		try {
			iStudentTable.clear();
			iOfferingRequests.clear();
		} finally {
			lock.release();
		}
	}

	@Override
	public XCourseRequest assign(XCourseRequest request, XEnrollment enrollment) {
		Lock lock = writeLock();
		try {
			XStudent student = iStudentTable.get(request.getStudentId());
			for (XRequest r: student.getRequests()) {
				if (r.equals(request)) {
					XCourseRequest cr = (XCourseRequest)r;

					// remove old requests
					for (XCourseId course: cr.getCourseIds()) {
						List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests != null) requests.remove(cr);
					}

					// assign
					cr.setEnrollment(enrollment);
					
					// put new requests
					for (XCourseId course: cr.getCourseIds()) {
						List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests == null) {
							requests = new ArrayList<XCourseRequest>();
							iOfferingRequests.put(course.getOfferingId(), requests);
						}
						requests.add(cr);
					}
					
					return cr;
				}
			}
			return null;
		} finally {
			lock.release();
		}
	}

	@Override
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist) {
		Lock lock = writeLock();
		try {
			XStudent student = iStudentTable.get(request.getStudentId());
			for (XRequest r: student.getRequests()) {
				if (r.equals(request)) {
					XCourseRequest cr = (XCourseRequest)r;

					// remove old requests
					for (XCourseId course: cr.getCourseIds()) {
						List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests != null) requests.remove(cr);
					}

					// assign
					cr.setWaitlist(waitlist);
					
					// put new requests
					for (XCourseId course: cr.getCourseIds()) {
						List<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests == null) {
							requests = new ArrayList<XCourseRequest>();
							iOfferingRequests.put(course.getOfferingId(), requests);
						}
						requests.add(cr);
					}
					
					return cr;
				}
			}
			return null;
		} finally {
			lock.release();
		}
	}

}
