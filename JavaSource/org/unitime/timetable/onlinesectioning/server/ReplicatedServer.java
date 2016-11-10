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
package org.unitime.timetable.onlinesectioning.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.context.Flag;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedCallable;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.jmx.CacheJmxRegistration;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
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
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;

/**
 * @author Tomas Muller
 */
public class ReplicatedServer extends AbstractServer {
	private boolean iLockStartsTransaction = false;
	
	private EmbeddedCacheManager iCacheManager;
	private Cache<Long, XCourseId> iCourseForId;
	private Cache<String, TreeSet<XCourseId>> iCourseForName;
	private Cache<Long, XStudent> iStudentTable;
	private Cache<Long, XOffering> iOfferingTable;
	private Cache<Long, Set<XCourseRequest>> iOfferingRequests;
	private Cache<Long, XExpectations> iExpectations;
	private Cache<Long, Boolean> iOfferingLocks;
	private Cache<String, Set<Long>> iInstructedOfferings; 

	public ReplicatedServer(OnlineSectioningServerContext context) throws SectioningException {
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
		iOfferingRequests = getCache("OfferingRequests");
		iExpectations = getCache("Expectations");
		iOfferingLocks = getCache("OfferingLocks");
		iInstructedOfferings = getCache("InstructedOfferings");

		Map<String, Object> original = new HashMap<String, Object>(iProperties);
		iProperties = getCache("Config");
		if (iProperties.isEmpty()) iProperties.putAll(original);

		if (isOptimisticLocking())
			iLog.info("Using optimistic locking.");
		super.load(context);
	}
	
	private boolean isOptimisticLocking() {
		return iOfferingLocks.getAdvancedCache().getCacheConfiguration().transaction().lockingMode() == LockingMode.OPTIMISTIC;
	}
	
	protected void removeCache(Cache<?,?> cache) {
		iCacheManager.getGlobalComponentRegistry().removeCache(cache.getName());
		CacheJmxRegistration jmx = cache.getAdvancedCache().getComponentRegistry().getComponent(CacheJmxRegistration.class);
		cache.stop();
		if (jmx != null)
			jmx.unregisterCacheMBean();
	}
	
	@Override
	public void unload() {
		super.unload();
		removeCache(iCourseForId);
		removeCache(iCourseForName);
		removeCache(iStudentTable);
		removeCache(iOfferingTable);
		removeCache(iOfferingRequests);
		removeCache(iExpectations);
		removeCache(iOfferingLocks);
		removeCache(iInstructedOfferings);
		removeCache((Cache<String, Object>)iProperties);
	}
	
	private TransactionManager getTransactionManager() {
		return iOfferingTable.getAdvancedCache().getTransactionManager();
	}
	
	private boolean inTransaction() {
		try {
			Transaction tx = getTransactionManager().getTransaction();
			return tx != null && tx.getStatus() == Status.STATUS_ACTIVE;
		} catch (SystemException e) {
			return false;
		}
	}
		
	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
		Lock lock = readLock();
		try {
			DistributedExecutorService ex = new DefaultExecutorService(iCourseForId);
			SubSet<XCourseId> ret = new SubSet<XCourseId>(limit, new CourseComparator(query));
			String queryInLowerCase = query.toLowerCase();
			
			List<Future<Collection<XCourseId>>> futures = ex.submitEverywhere(new FindCoursesCallable(getAcademicSession().getUniqueId(), queryInLowerCase, limit, matcher));
			if (limit == null) {
				for (Future<Collection<XCourseId>> future: futures)
					ret.addAll(future.get());
			} else {
				for (Future<Collection<XCourseId>> future: futures) {
					for (XCourseId c: future.get()) {
						if (c.matchCourseName(queryInLowerCase)) ret.add(c);
					}
				}
				if (!ret.isLimitReached() && queryInLowerCase.length() > 2) {
					for (Future<Collection<XCourseId>> future: futures) {
						for (XCourseId c: future.get()) {
							ret.add(c);
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
			for (int idx = course.indexOf('-'); idx >= 0; idx = course.indexOf('-', idx + 1)) {
				String courseName = course.substring(0, idx).trim();
				String title = course.substring(idx + 1).trim();
				TreeSet<XCourseId> infos = iCourseForName.get(courseName.toLowerCase());
				if (infos!= null && !infos.isEmpty())
					for (XCourseId info: infos)
						if (title.equalsIgnoreCase(info.getTitle())) return info;
			}
			TreeSet<XCourseId> infos = iCourseForName.get(course.toLowerCase());
			if (infos!= null && !infos.isEmpty()) return infos.first();
			return null;
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
		Lock lock = writeLock();
		try {
			iExpectations.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(expectations.getOfferingId(), expectations);
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
							Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
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
		Lock lock = writeLock();
		try {
			if (updateRequests) {
				XStudent oldStudent = iStudentTable.get(student.getStudentId());
				iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
				if (oldStudent != null) {
					for (XRequest request: oldStudent.getRequests())
						if (request instanceof XCourseRequest)
							for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
								Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
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
							Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
							if (requests == null)
								requests = new HashSet<XCourseRequest>();
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
		remove(offering, true);
	}

	protected void remove(XOffering offering, boolean removeExpectations) {
		Lock lock = writeLock();
		try {
			for (XCourse course: offering.getCourses()) {
				iCourseForId.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(course.getCourseId());
				TreeSet<XCourseId> courses = iCourseForName.get(course.getCourseNameInLowerCase());
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
			if (removeExpectations)
				iExpectations.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(offering.getOfferingId());
			for (String externalId: offering.getInstructorExternalIds()) {
				Set<Long> offeringIds = iInstructedOfferings.get(externalId);
				if (offeringIds != null) {
					if (offeringIds.remove(offering.getOfferingId()))
						iInstructedOfferings.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(externalId, offeringIds);
				}
			}
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
			
			iOfferingTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(offering.getOfferingId(), offering);
			for (XCourse course: offering.getCourses()) {
				iCourseForId.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseId(), new XCourseId(course));
				TreeSet<XCourseId> courses = iCourseForName.get(course.getCourseNameInLowerCase());
				if (courses == null) {
					courses = new TreeSet<XCourseId>();
					courses.add(new XCourseId(course));
					iCourseForName.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseNameInLowerCase(), courses);
				} else {
					courses.add(new XCourseId(course));
					if (courses.size() == 1) 
						for (XCourseId x: courses) x.setHasUniqueName(true);
					else if (courses.size() > 1)
						for (XCourseId x: courses) x.setHasUniqueName(false);
					iCourseForName.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getCourseNameInLowerCase(), courses);
				}
			}
			for (String externalId: offering.getInstructorExternalIds()) {
				Set<Long> offeringIds = iInstructedOfferings.get(externalId);
				if (offeringIds == null)
					offeringIds = new HashSet<Long>();
				offeringIds.add(offering.getOfferingId());
				iInstructedOfferings.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(externalId, offeringIds);
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
			iInstructedOfferings.clear();
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
						Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
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
						Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests == null)
							requests = new HashSet<XCourseRequest>();
						requests.add(cr);
						iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
					}
					
					iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
					return cr;
				}
			}
			iLog.warn("ASSIGN[3]: Request " + student + " " + request + " was not found among student requests");
			for (XCourseId course: request.getCourseIds()) {
				Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
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
		Lock lock = writeLock();
		try {
			XStudent student = iStudentTable.get(request.getStudentId());
			for (XRequest r: student.getRequests()) {
				if (r.equals(request)) {
					XCourseRequest cr = (XCourseRequest)r;

					// remove old requests
					for (XCourseId course: cr.getCourseIds()) {
						Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
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
						Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
						if (requests == null)
							requests = new HashSet<XCourseRequest>();
						iOfferingRequests.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(course.getOfferingId(), requests);
					}
					
					iStudentTable.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(student.getStudentId(), student);
					return cr;
				}
			}
			iLog.warn("WAITLIST[3]: Request " + student + " " + request + " was not found among student requests");
			for (XCourseId course: request.getCourseIds()) {
				Set<XCourseRequest> requests = iOfferingRequests.get(course.getOfferingId());
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

	private static OnlineSectioningServer getLocalServer(Long sessionId) {
		SolverServer server = SolverServerImplementation.getInstance();
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
			SubSet<XCourseId> ret = new SubSet<XCourseId>(iLimit, new CourseComparator(iQuery));
			for (XCourseId c : iCache.values()) {
				if (iQuery != null && !c.matchCourseName(iQuery)) continue;
				if (iMatcher != null && !iMatcher.match(c)) continue;
				ret.add(c);
			}
			if (!ret.isLimitReached() && iQuery != null && iQuery.length() > 2) {
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
	public Lock readLock() {
		if (!iLockStartsTransaction) {
			return new Lock() {
				public void release() {}
			};
		}
		try {
			TransactionManager tm = getTransactionManager();
			if (tm.getTransaction() == null) {
				tm.setTransactionTimeout(3600);
				tm.begin();
				return new Lock() {
					public void release() {
						try {
							TransactionManager tm = getTransactionManager();
							if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK)
								tm.rollback();
							else
								tm.commit();
						} catch (Exception e) {
							throw new SectioningException("Failed to commit a transaction: " + e.getMessage(), e);
						}
					}
				};
			} else {
				return new Lock() {
					public void release() {}
				};
			}
		} catch (Exception e) {
			throw new SectioningException("Failed to begin a transaction: " + e.getMessage(), e);
		}
	}

	@Override
	public Lock writeLock() {
		return readLock();
	}

	@Override
	public Lock lockAll() {
		return readLock();
	}

	@Override
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, String actionName) {
		boolean lockStudents = getConfig().getPropertyBoolean(actionName + ".LockStudents", true);
		boolean lockOfferings = getConfig().getPropertyBoolean(actionName + ".LockOfferings", true);
		boolean excludeLockedOfferings = lockOfferings && getConfig().getPropertyBoolean(actionName + ".ExcludeLockedOfferings", true);
		Lock lock = writeLock();
		try {
			if (!inTransaction()) {
				iLog.warn("Failed to lock a student " + studentId + ": No transaction has been started.");
				return lock;
			}
			if (isOptimisticLocking()) {
				iLog.warn("Failed to lock a student " + studentId + ": No eager locks in optimistic locking.");
				return lock;
			}

			Set<Long> ids = new HashSet<Long>();

			if (lockStudents) {
				ids.add(-studentId);
			}
			
			if (lockOfferings) {
				if (offeringIds != null)
					for (Long offeringId: offeringIds)
						if (!excludeLockedOfferings || !iOfferingLocks.containsKey(offeringId))
							ids.add(offeringId);
				
				XStudent student = getStudent(studentId);
				
				if (student != null)
					for (XRequest r: student.getRequests()) {
						if (r instanceof XCourseRequest && ((XCourseRequest)r).getEnrollment() != null) {
							Long offeringId = ((XCourseRequest)r).getEnrollment().getOfferingId();
							if (!excludeLockedOfferings || !iOfferingLocks.containsKey(offeringId)) ids.add(offeringId);
						}
					}
			}
			
			while (!iOfferingLocks.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY).lock(ids)) {
				iLog.info("Failed to lock a student " + studentId + ", retrying...");
			}
			
			return lock;
		} catch (Exception e) {
			lock.release();
			throw new SectioningException("Failed to lock a student: " + e.getMessage(), e);
		}
	}

	@Override
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, String actionName) {
		boolean lockStudents = getConfig().getPropertyBoolean(actionName + ".LockStudents", true);
		boolean lockOfferings = getConfig().getPropertyBoolean(actionName + ".LockOfferings", true);
		boolean excludeLockedOffering = lockOfferings && getConfig().getPropertyBoolean(actionName + ".ExcludeLockedOfferings", true);
		Lock lock = writeLock();
		try {
			if (!inTransaction()) {
				iLog.warn("Failed to lock an offering " + offeringId + ": No transaction has been started.");
				return lock;
			}
			if (isOptimisticLocking()) {
				iLog.warn("Failed to lock an offering " + offeringId + ": No eager locks in optimistic locking.");
				return lock;
			}
			Set<Long> ids = new HashSet<Long>();
			
			if (lockOfferings) {
				if (!excludeLockedOffering || !iOfferingLocks.containsKey(offeringId))
					ids.add(offeringId);
			}
			
			if (lockStudents) {
				if (studentIds != null)
					for (Long studentId: studentIds)
						ids.add(-studentId);
			
				Collection<XCourseRequest> requests = getRequests(offeringId);
				if (requests != null) {
					for (XCourseRequest request: requests)
						ids.add(-request.getStudentId());
				}
			}
			
			while (!iOfferingLocks.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY).lock(ids)) {
				iLog.info("Failed to lock an offering " + offeringId + ", retrying...");
			}
			
			return lock;
		} catch (Exception e) {
			lock.release();
			throw new SectioningException("Failed to lock an offering: " + e.getMessage(), e);
		}
	}
	
	private Long getOfferingIdFromCourseName(String courseName) {
		if (courseName == null) return null;
		XCourseId c = getCourse(courseName);
		return (c == null ? null : c.getOfferingId());
	}

	@Override
	public Lock lockRequest(CourseRequestInterface request, String actionName) {
		boolean lockStudents = getConfig().getPropertyBoolean(actionName + ".LockStudents", true);
		boolean lockOfferings = getConfig().getPropertyBoolean(actionName + ".LockOfferings", true);
		boolean excludeLockedOffering = lockOfferings && getConfig().getPropertyBoolean(actionName + ".ExcludeLockedOfferings", true);
		Lock lock = writeLock();
		try {
			if (!inTransaction()) {
				iLog.warn("Failed to lock a request for student " + request.getStudentId() + ": No transaction has been started.");
				return lock;
			}
			if (isOptimisticLocking()) {
				iLog.warn("Failed to lock a request for student " + request.getStudentId() + ": No eager locks in optimistic locking.");
				return lock;
			}
			Set<Long> ids = new HashSet<Long>();
			
			if (lockStudents) {
				ids.add(-request.getStudentId());
			}
			
			if (lockOfferings) {
				for (CourseRequestInterface.Request r: request.getCourses()) {
					if (r.hasRequestedCourse()) {
						for (CourseRequestInterface.RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.hasCourseId()) {
								XCourseId c = getCourse(rc.getCourseId());
								Long id = (c == null ? null : c.getOfferingId());
								if (id != null && (!excludeLockedOffering || !isOfferingLocked(id))) ids.add(id);
							} else if (rc.hasCourseName()) {
								Long id = getOfferingIdFromCourseName(rc.getCourseName());
								if (id != null && (!excludeLockedOffering || !isOfferingLocked(id))) ids.add(id);
							}
						}
					}
				}
				for (CourseRequestInterface.Request r: request.getAlternatives()) {
					if (r.hasRequestedCourse()) {
						for (CourseRequestInterface.RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.hasCourseId()) {
								XCourseId c = getCourse(rc.getCourseId());
								Long id = (c == null ? null : c.getOfferingId());
								if (id != null && (!excludeLockedOffering || !isOfferingLocked(id))) ids.add(id);
							} else if (rc.hasCourseName()) {
								Long id = getOfferingIdFromCourseName(rc.getCourseName());
								if (id != null && (!excludeLockedOffering || !isOfferingLocked(id))) ids.add(id);
							}
						}
					}
				}				
			}
			
			while (!iOfferingLocks.getAdvancedCache().withFlags(Flag.FAIL_SILENTLY).lock(ids)) {
				iLog.info("Failed to lock a request for student " + request.getStudentId() + ", retrying...");
			}

			return lock;
		} catch (Exception e) {
			lock.release();
			throw new SectioningException("Failed to lock a request: " + e.getMessage(), e);
		}
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

	@Override
	public Collection<Long> getInstructedOfferings(String instructorExternalId) {
		Lock lock = readLock();
		try {
			return iInstructedOfferings.get(instructorExternalId);
		} finally {
			lock.release();
		}
	}
}
