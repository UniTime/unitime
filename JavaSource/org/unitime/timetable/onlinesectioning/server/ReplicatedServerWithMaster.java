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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.context.Flag;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedCallable;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.manager.EmbeddedCacheManager;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerContext;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.match.StudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XHashSet;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XTreeSet;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
public class ReplicatedServerWithMaster extends AbstractLockingServer {
	private EmbeddedCacheManager iCacheManager;
	private Cache<Long, XCourseId> iCourseForId;
	private Cache<String, XTreeSet<XCourseId>> iCourseForName;
	private Cache<Long, XStudent> iStudentTable;
	private Cache<Long, XOffering> iOfferingTable;
	private Cache<Long, XHashSet<XDistribution>> iDistributions;
	private Cache<Long, XHashSet<XCourseRequest>> iOfferingRequests;
	private Cache<Long, XExpectations> iExpectations;
	private Cache<Long, Boolean> iOfferingLocks;

	public ReplicatedServerWithMaster(OnlineSectioningServerContext context) throws SectioningException {
		super(context);
	}
	
	private String cacheName(String table) {
		return getAcademicSession().toCompactString() + "[" + table + "]";
	}
	
	private <U,T> Cache<U,T> getCache(String name) {
		Configuration config = iCacheManager.getCacheConfiguration(name);
		if (config != null) {
			iLog.info("Using " + config + " for " + name + " cache.");
			iCacheManager.defineConfiguration(cacheName(name), config);
		}
		return iCacheManager.getCache(cacheName(name), true);
	}
	
	@Override
	protected void load(OnlineSectioningServerContext context) throws SectioningException {
		iCacheManager = context.getCacheManager();
		iCourseForId = getCache("CourseForId");
		iCourseForName = getCache("CourseForName");
		iStudentTable = getCache("StudentTable");
		iOfferingTable = getCache("OfferingTable");
		iDistributions = getCache("Distributions");
		iOfferingRequests = getCache("OfferingRequests");
		iExpectations = getCache("Expectations");
		iOfferingLocks = getCache("OfferingLocks");
		super.load(context);
	}
	
	@Override
	public void unload(boolean remove) {
		boolean master = isMaster();
		super.unload(remove);
		if (master && remove) {
			iLog.info("Removing cache.");
			iCacheManager.removeCache(cacheName("CourseForId"));
			iCacheManager.removeCache(cacheName("CourseForName"));
			iCacheManager.removeCache(cacheName("StudentTable"));
			iCacheManager.removeCache(cacheName("OfferingTable"));
			iCacheManager.removeCache(cacheName("Distributions"));
			iCacheManager.removeCache(cacheName("OfferingRequests"));
			iCacheManager.removeCache(cacheName("Expectations"));
			iCacheManager.removeCache(cacheName("OfferingLocks"));
		}
	}
	
	@Override
	protected void loadOnMaster(OnlineSectioningServerContext context) throws SectioningException {
		clearAll();
		releaseAllOfferingLocks();
		super.loadOnMaster(context);
	}
	
	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
		Lock lock = readLock();
		try {
			DistributedExecutorService ex = new DefaultExecutorService(iCourseForId);
			Set<XCourseId> ret = new TreeSet<XCourseId>();
			String queryInLowerCase = query.toLowerCase();
			
			List<Future<Collection<XCourseId>>> futures = ex.submitEverywhere(new FindCoursesCallable(getAcademicSession().getUniqueId(), queryInLowerCase, limit, matcher));
			if (limit == null) {
				for (Future<Collection<XCourseId>> future: futures)
					ret.addAll(future.get());
			} else {
				for (Future<Collection<XCourseId>> future: futures) {
					for (XCourseId c: future.get()) {
						if (c.matchCourseName(queryInLowerCase)) ret.add(c);
						if (ret.size() == limit) return ret;
					}
				}
				if (queryInLowerCase.length() > 2) {
					for (Future<Collection<XCourseId>> future: futures) {
						for (XCourseId c: future.get()) {
							ret.add(c);
							if (ret.size() == limit) return ret;
						}
					}
				}				
			}
			return ret;
		} catch (InterruptedException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new SectioningException(e.getMessage(), e);
		} finally {
			lock.release();
		}
	}
	
	@Override
	public Collection<XCourseId> findCourses(CourseMatcher matcher) {
		Lock lock = readLock();
		try {
			DistributedExecutorService ex = new DefaultExecutorService(iCourseForId);
			Set<XCourseId> ret = new TreeSet<XCourseId>();
			
			List<Future<Collection<XCourseId>>> futures = ex.submitEverywhere(new FindCoursesCallable(getAcademicSession().getUniqueId(), null, null, matcher));
			for (Future<Collection<XCourseId>> future: futures)
				ret.addAll(future.get());
			
			return ret;
		} catch (InterruptedException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new SectioningException(e.getMessage(), e);
		} finally {
			lock.release();
		}
	}

	@Override
	public Collection<XStudent> findStudents(StudentMatcher matcher) {
		Lock lock = readLock();
		try {
			DistributedExecutorService ex = new DefaultExecutorService(iStudentTable);
			Set<XStudent> ret = new TreeSet<XStudent>();
			
			List<Future<Collection<XStudent>>> futures = ex.submitEverywhere(new FindStudentsCallable(getAcademicSession().getUniqueId(), matcher));
			for (Future<Collection<XStudent>> future: futures)
				ret.addAll(future.get());
			
			return ret;
		} catch (InterruptedException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new SectioningException(e.getMessage(), e);
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
			Collection<XCourseRequest> requests = iOfferingRequests.get(offeringId);
			return requests == null ? null : new ArrayList<XCourseRequest>(requests);
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
		if (!isMaster())
			iLog.warn("Updating expectations on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			iExpectations.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(expectations.getOfferingId(), expectations);
		} finally {
			lock.release();
		}
	}

	@Override
	public void remove(XStudent student) {
		if (!isMaster())
			iLog.warn("Removing student on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			XStudent oldStudent = iStudentTable.remove(student.getStudentId());
			if (oldStudent != null) {
				iLog.info("Remove " + oldStudent + " with requests " + oldStudent.getRequests());
				for (XRequest request: oldStudent.getRequests())
					if (request instanceof XCourseRequest)
						for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
							XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
							if (requests != null) {
								if (!requests.remove(request))
									iLog.warn("REMOVE[1]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
								iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
							} else {
								iLog.warn("REMOVE[2]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
							}
						}
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(XStudent student, boolean updateRequests) {
		iLog.info("Update " + student + " with requests " + student.getRequests());
		if (!isMaster())
			iLog.warn("Updating student on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			if (updateRequests) {
				XStudent oldStudent = iStudentTable.get(student.getStudentId());
				iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
				if (oldStudent != null) {
					iLog.info("  Was " + oldStudent + " with requests " + oldStudent.getRequests());
					for (XRequest request: oldStudent.getRequests())
						if (request instanceof XCourseRequest)
							for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
								XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
								if (requests != null) {
									if (!requests.remove(request))
										iLog.warn("UPDATE[1]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
									iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
								} else {
									iLog.warn("UPDATE[2]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
								}
							}
				}
				for (XRequest request: student.getRequests())
					if (request instanceof XCourseRequest)
						for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
							XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
							if (requests == null)
								requests = new XHashSet<XCourseRequest>(new XCourseRequest.XCourseRequestSerializer());
							requests.add((XCourseRequest)request);
							iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
						}
			} else {
				iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void remove(XOffering offering) {
		if (!isMaster())
			iLog.warn("Removing offering on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			for (XCourse course: offering.getCourses()) {
				iCourseForId.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(course.getCourseId());
				XTreeSet<XCourseId> courses = iCourseForName.get(course.getCourseNameInLowerCase());
				if (courses != null) {
					courses.remove(course);
					if (courses.size() == 1) 
						for (XCourseId x: courses) x.setHasUniqueName(true);
					if (courses.isEmpty())
						iCourseForName.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(course.getCourseNameInLowerCase());
					else
						iCourseForName.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseNameInLowerCase(), courses);
				}
			}
			iOfferingTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(offering.getOfferingId());
			XHashSet<XDistribution> distributions = iDistributions.get(offering.getOfferingId());
			if (distributions != null && !distributions.isEmpty())
				for (XDistribution distribution: new ArrayList<XDistribution>(distributions)) {
					for (Long offeringId: distribution.getOfferingIds()) {
						XHashSet<XDistribution> l = iDistributions.get(offeringId);
						if (l != null) {
							l.remove(distribution);
							if (l.isEmpty())
								iDistributions.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(offeringId);
							else
								iDistributions.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(offeringId, distributions);
						}
					}
				}
			iExpectations.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(offering.getOfferingId());
		} finally {
			lock.release();
		}
	}

	@Override
	public void update(XOffering offering) {
		if (!isMaster())
			iLog.warn("Updating offering on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			XOffering oldOffering = iOfferingTable.get(offering.getOfferingId());
			if (oldOffering != null)
				remove(oldOffering);
			
			iOfferingTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(offering.getOfferingId(), offering);
			for (XCourse course: offering.getCourses()) {
				iCourseForId.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseId(), new XCourseId(course));
				XTreeSet<XCourseId> courses = iCourseForName.get(course.getCourseNameInLowerCase());
				if (courses == null) {
					courses = new XTreeSet<XCourseId>(new XCourseId.XCourseIdSerializer());
					courses.add(new XCourseId(course));
					iCourseForName.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseNameInLowerCase(), courses);
				} else {
					courses.add(course);
					if (courses.size() == 1) 
						for (XCourseId x: courses) x.setHasUniqueName(true);
					else if (courses.size() > 1)
						for (XCourseId x: courses) x.setHasUniqueName(false);
					iCourseForName.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseNameInLowerCase(), courses);
				}
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void clearAll() {
		if (!isMaster())
			iLog.warn("Clearing all data on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			iStudentTable.clear();
			iOfferingTable.clear();
			iCourseForId.clear();
			iCourseForName.clear();
			iOfferingRequests.clear();
			iDistributions.clear();
		} finally {
			lock.release();
		}
	}

	@Override
	public void clearAllStudents() {
		if (!isMaster())
			iLog.warn("Clearing all students on a slave node. That is suspicious.");
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
		iLog.info("Assign " + request + " with " + enrollment);
		if (!isMaster())
			iLog.warn("Assigning a request on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			XStudent student = iStudentTable.get(request.getStudentId());
			for (XRequest r: student.getRequests()) {
				if (r.equals(request)) {
					XCourseRequest cr = (XCourseRequest)r;

					// remove old requests
					for (XCourseId course: cr.getCourseIds()) {
						XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests != null) {
							if (!requests.remove(cr))
								iLog.warn("ASSIGN[1]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
							iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
						} else {
							iLog.warn("ASSIGN[2]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
						}
					}

					// assign
					cr.setEnrollment(enrollment);
					
					// put new requests
					for (XCourseId course: cr.getCourseIds()) {
						XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests == null)
							requests = new XHashSet<XCourseRequest>(new XCourseRequest.XCourseRequestSerializer());
						requests.add(cr);
						iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
					}
					
					iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
					return cr;
				}
			}
			iLog.warn("ASSIGN[3]: Request " + student + " " + request + " was not found among student requests");
			for (XCourseId course: request.getCourseIds()) {
				XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
				if (requests != null) {
					requests.remove(request);
					iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
				}
			}
			return null;
		} finally {
			lock.release();
		}
	}

	@Override
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist) {
		if (!isMaster())
			iLog.warn("Wait-listing a request on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			XStudent student = iStudentTable.get(request.getStudentId());
			for (XRequest r: student.getRequests()) {
				if (r.equals(request)) {
					XCourseRequest cr = (XCourseRequest)r;

					// remove old requests
					for (XCourseId course: cr.getCourseIds()) {
						XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests != null) {
							if (!requests.remove(cr))
								iLog.warn("WAITLIST[1]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
							iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
						} else {
							iLog.warn("WAITLIST[2]: Request " + student + " " + request + " was not present in the offering requests table for " + course);
						}
					}

					// assign
					cr.setWaitlist(waitlist);
					
					// put new requests
					for (XCourseId course: cr.getCourseIds()) {
						XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests == null)
							requests = new XHashSet<XCourseRequest>(new XCourseRequest.XCourseRequestSerializer());
						iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
					}
					
					iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
					return cr;
				}
			}
			iLog.warn("WAITLIST[3]: Request " + student + " " + request + " was not found among student requests");
			for (XCourseId course: request.getCourseIds()) {
				XHashSet<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
				if (requests != null) {
					requests.remove(request);
					iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
				}
			}
			return null;
		} finally {
			lock.release();
		}
	}

	@Override
	public void addDistribution(XDistribution distribution) {
		if (!isMaster())
			iLog.warn("Adding distribution on a slave node. That is suspicious.");
		Lock lock = writeLock();
		try {
			for (Long offeringId: distribution.getOfferingIds()) {
				XHashSet<XDistribution> distributions = iDistributions.get(offeringId);
				if (distributions == null)
					distributions = new XHashSet<XDistribution>(new XDistribution.XDistributionSerializer());
				distributions.add(distribution);
				iDistributions.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(offeringId, distributions);
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
	
	private static OnlineSectioningServer getLocalServer(Long sessionId) {
		SolverServer server = null;
		
		if (SpringApplicationContextHolder.isInitialized()) {
			// Spring -> user solver server service
			server = ((SolverServerService)SpringApplicationContextHolder.getBean("solverServerService")).getLocalServer();
		} else {
			// Standalone -> use get instance
			server = SolverServerImplementation.getInstance();
		}
		
		return server == null ? null : server.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
	}
	
	public static class FindCoursesCallable implements DistributedCallable<Long, XCourseId, Collection<XCourseId>>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private String iQuery;
		private Integer iLimit;
		private CourseMatcher iMatcher;
		private transient Cache<Long, XCourseId> iCache;
		
		public FindCoursesCallable(Long sessionId, String queryInLowerCase, Integer limit, CourseMatcher matcher) {
			iSessionId = sessionId;
			iQuery = queryInLowerCase;
			iLimit = limit;
			iMatcher = matcher;
		}
		
		@Override
		public void setEnvironment(Cache<Long, XCourseId> cache, Set<Long> inputKeys) {
			iCache = cache;
		}

		@Override
		public Collection<XCourseId> call() throws Exception {
			if (iMatcher != null) iMatcher.setServer(getLocalServer(iSessionId));
			Set<XCourseId> ret = new TreeSet<XCourseId>();
			for (XCourseId c : iCache.values()) {
				if (iQuery != null && !c.matchCourseName(iQuery)) continue;
				if (iMatcher != null && !iMatcher.match(c)) continue;
				ret.add(c);
				if (iLimit != null && ret.size() == iLimit) return ret;
			}
			if (iQuery != null && iQuery.length() > 2) {
				for (XCourseId c : iCache.values()) {
					if (!c.matchTitle(iQuery)) continue;
					if (iMatcher != null && !iMatcher.match(c)) continue;
					ret.add(c);
				}
			}
			return ret;
		}
	}
	
	public static class GetKeysCallable<T> implements DistributedCallable<Long, T, Collection<Long>>, Serializable {
		private static final long serialVersionUID = 1L;
		private transient Cache<Long, T> iCache;
		
		public GetKeysCallable() {
		}
		
		@Override
		public void setEnvironment(Cache<Long, T> cache, Set<Long> inputKeys) {
			iCache = cache;
		}

		@Override
		public Collection<Long> call() throws Exception {
			return new ArrayList<Long>(iCache.keySet());
		}
	}
	
	public static class FindStudentsCallable implements DistributedCallable<Long, XStudent, Collection<XStudent>>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private StudentMatcher iMatcher;
		private transient Cache<Long, XStudent> iCache;
		
		public FindStudentsCallable(Long sessionId, StudentMatcher matcher) {
			iSessionId = sessionId;
			iMatcher = matcher;
		}
		
		@Override
		public void setEnvironment(Cache<Long, XStudent> cache, Set<Long> inputKeys) {
			iCache = cache;
		}

		@Override
		public Collection<XStudent> call() throws Exception {
			if (iMatcher != null) iMatcher.setServer(getLocalServer(iSessionId));
			List<XStudent> ret = new ArrayList<XStudent>();
			for (XStudent s : iCache.values()) {
				if (iMatcher.match(s))
					ret.add(s);
			}
			return ret;
		}
	}

	@Override
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, boolean excludeLockedOfferings) {
		if (!isMaster()) {
			iLog.warn("Failed to lock a student " + studentId + ": not executed on master.");
			return new NoLock();
		}
		return super.lockStudent(studentId, offeringIds, excludeLockedOfferings);
	}

	@Override
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, boolean excludeLockedOffering) {
		if (!isMaster()) {
			iLog.warn("Failed to lock an offering " + offeringId + ": not executed on master.");
			return new NoLock();
		}
		return super.lockOffering(offeringId, studentIds, excludeLockedOffering);
	}
	
	@Override
	public Lock lockRequest(CourseRequestInterface request) {
		if (!isMaster()) {
			iLog.warn("Failed to lock a request for student " + request.getStudentId() + ": not executed on master.");
			return new NoLock();
		}
		return super.lockRequest(request);
	}
	
	@Override
	public boolean isOfferingLocked(Long offeringId) {
		return iOfferingLocks.containsKey(offeringId);
	}

	@Override
	public void lockOffering(Long offeringId) {
		iOfferingLocks.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(offeringId, Boolean.TRUE);
	}

	@Override
	public void unlockOffering(Long offeringId) {
		iOfferingLocks.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(offeringId);
	}
	
	@Override
	public Collection<Long> getLockedOfferings() {
		Lock lock = readLock();
		try {
			DistributedExecutorService ex = new DefaultExecutorService(iOfferingLocks);
			Set<Long> ret = new HashSet<Long>();
			
			List<Future<Collection<Long>>> futures = ex.submitEverywhere(new GetKeysCallable<Boolean>());
			for (Future<Collection<Long>> future: futures)
				ret.addAll(future.get());
			
			return ret;
		} catch (InterruptedException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new SectioningException(e.getMessage(), e);
		} finally {
			lock.release();
		}
	}
	
	@Override
	public void releaseAllOfferingLocks() {
		iOfferingLocks.clear();
	}
	
	private static class NoLock implements Lock {
		@Override
		public void release() {}
	}
	
	@Override
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException {
		CheckMaster ch = action.getClass().getAnnotation(CheckMaster.class);
		if (ch != null && ch.value() == Master.REQUIRED && !isMaster())
			iLog.warn("Executing action " + action.name() + " (master required) on a slave node.");
		else if (ch != null && ch.value() == Master.AVOID && isMaster())
			iLog.warn("Executing action " + action.name() + " (avoid master) on a master node.");
		E ret = super.execute(action, user);
		return ret;
	}
	
	@Override
	public Lock writeLock() {
		if (!isMaster())
			iLog.warn("Asking for a WRITE lock on a slave node. That is suspicious.");
		return super.writeLock();
	}
	
	@Override
	public Lock lockAll() {
		if (!isMaster())
			iLog.warn("Asking for an ALL lock on a slave node. That is suspicious.");
		return super.lockAll();
	}
}
