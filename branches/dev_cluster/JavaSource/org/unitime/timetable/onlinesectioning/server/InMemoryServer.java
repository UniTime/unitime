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
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.CourseDetails;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

public class InMemoryServer extends AbstractServer {
	private Hashtable<Long, CourseInfo> iCourseForId = new Hashtable<Long, CourseInfo>();
	private Hashtable<String, TreeSet<CourseInfo>> iCourseForName = new Hashtable<String, TreeSet<CourseInfo>>();
	private TreeSet<CourseInfo> iCourses = new TreeSet<CourseInfo>();
	
	private Hashtable<Long, XStudent> iStudentTable = new Hashtable<Long, XStudent>();
	private Hashtable<Long, XOffering> iOfferingTable = new Hashtable<Long, XOffering>();
	private Hashtable<Long, List<XDistribution>> iDistributions = new Hashtable<Long, List<XDistribution>>();
	private Hashtable<Long, List<XCourseRequest>> iOfferingRequests = new Hashtable<Long, List<XCourseRequest>>();
	private Hashtable<Long, XExpectations> iExpectations = new Hashtable<Long, XExpectations>();
	
	public InMemoryServer(Long sessionId, boolean waitTillStarted) throws SectioningException {
		super(sessionId, waitTillStarted);
	}

	@Override
	public Collection<CourseInfo> findCourses(String query, Integer limit, CourseInfoMatcher matcher) {
		Lock lock = readLock();
		try {
			List<CourseInfo> ret = new ArrayList<CourseInfo>(limit == null ? 100 : limit);
			String queryInLowerCase = query.toLowerCase();
			for (CourseInfo c : iCourses) {
				if (c.matchCourseName(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
				if (limit != null && ret.size() == limit) return ret;
			}
			if (queryInLowerCase.length() > 2) {
				for (CourseInfo c : iCourses) {
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
	public Collection<CourseInfo> findCourses(CourseInfoMatcher matcher) {
		Lock lock = readLock();
		try {
			List<CourseInfo> ret = new ArrayList<CourseInfo>();
			for (CourseInfo c : iCourses) {
				if (matcher.match(c)) ret.add(c);
			}
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public Collection<XStudent> findStudents(StudentMatcher matcher) {
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
	public CourseInfo getCourseInfo(String course) {
		Lock lock = readLock();
		try {
			if (course.indexOf('-') >= 0) {
				String courseName = course.substring(0, course.indexOf('-')).trim();
				String title = course.substring(course.indexOf('-') + 1).trim();
				TreeSet<CourseInfo> infos = iCourseForName.get(courseName.toLowerCase());
				if (infos!= null && !infos.isEmpty())
					for (CourseInfo info: infos)
						if (title.equalsIgnoreCase(info.getTitle())) return info;
				return null;
			} else {
				TreeSet<CourseInfo> infos = iCourseForName.get(course.toLowerCase());
				if (infos!= null && !infos.isEmpty()) return infos.first();
				return null;
			}
		} finally {
			lock.release();
		}
	}
	
	@Override
	public CourseInfo getCourseInfo(Long courseId) {
		Lock lock = readLock();
		try {
			return iCourseForId.get(courseId);
		} finally {
			lock.release();
		}
	}
	
	@Override
	public CourseDetails getCourseDetails(Long courseId) {
		Lock lock = readLock();
		try {
			CourseInfo course = iCourseForId.get(courseId);
			if (course == null) return null;
			XOffering offering = iOfferingTable.get(course.getOfferingId());
			return new CourseDetails(offering.getCourse(course.getUniqueId()), iOfferingRequests.get(course.getOfferingId()));
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
		Lock lock = writeLock();
		try {
			for (XCourse course: offering.getCourses()) {
				CourseInfo ci = iCourseForId.get(course.getCourseId());
				if (ci != null) {
					TreeSet<CourseInfo> courses = iCourseForName.get(ci.toString());
					if (courses != null) {
						courses.remove(ci);
						if (courses.isEmpty()) {
							iCourseForName.remove(ci.toString());
						} else if (courses.size() == 1) {
							for (CourseInfo x: courses)
								x.setHasUniqueName(true);
						}
					}
					iCourseForId.remove(ci.getUniqueId());
					iCourses.remove(ci);
				}
			}
			iOfferingTable.remove(offering.getOfferingId());
			List<XDistribution> distributions = iDistributions.get(offering.getOfferingId());
			if (distributions != null && !distributions.isEmpty())
				for (XDistribution distribution: new ArrayList<XDistribution>(distributions)) {
					for (Long offeringId: distribution.getOfferingIds()) {
						List<XDistribution> l = iDistributions.get(offeringId);
						if (l != null) l.remove(distribution);
					}
				}
			iExpectations.remove(offering.getOfferingId());
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(XOffering offering) {
		Lock lock = writeLock();
		try {
			XOffering old = iOfferingTable.get(offering.getOfferingId());
			if (old != null) remove(old);
			iOfferingTable.put(offering.getOfferingId(), offering);
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(CourseInfo info) {
		Lock lock = writeLock();
		try {
			CourseInfo old = iCourseForId.get(info.getUniqueId());
			iCourseForId.put(info.getUniqueId(), info);
			TreeSet<CourseInfo> courses = iCourseForName.get(info.toString());
			if (courses == null) {
				courses = new TreeSet<CourseInfo>();
				iCourseForName.put(info.toString(), courses);
			}
			if (old != null) {
				courses.remove(old);
				iCourses.remove(old);
			}
			courses.add(info);
			iCourses.add(info);
			if (courses.size() == 1) 
				for (CourseInfo x: courses) x.setHasUniqueName(true);
			else if (courses.size() > 1)
				for (CourseInfo x: courses) x.setHasUniqueName(false);
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
			iCourses.clear();
			iOfferingRequests.clear();
			iDistributions.clear();
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

	@Override
	public void addDistribution(XDistribution distribution) {
		Lock lock = writeLock();
		try {
			for (Long offeringId: distribution.getOfferingIds()) {
				List<XDistribution> distributions = iDistributions.get(offeringId);
				if (distributions == null) {
					distributions = new ArrayList<XDistribution>();
					iDistributions.put(offeringId, distributions);
				}
				distributions.add(distribution);
			}
		} finally {
			lock.release();
		}		
	}

	@Override
	public Collection<XDistribution> getDistributions(Long offeringId) {
		Lock lock = readLock();
		try {
			return iDistributions.get(offeringId);
		} finally {
			lock.release();
		}
	}

}
