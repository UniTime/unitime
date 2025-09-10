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
package org.unitime.timetable.onlinesectioning;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.match.StudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRule;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRules;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.model.XClassEnrollment;

/**
 * @author Tomas Muller
 */
public interface OnlineSectioningServer {
	public String getHost();
	public String getUser();
	
	public AcademicSessionInfo getAcademicSession();
	public DistanceMetric getDistanceMetric();
	public DistanceMetric getUnavailabilityDistanceMetric();
	public DataProperties getConfig();
	public OverExpectedCriterion getOverExpectedCriterion();
	
	public Collection<? extends XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher, Comparator<XCourseId> cmp);
	public Collection<? extends XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher);
	public Collection<? extends XCourseId> findCourses(CourseMatcher matcher);
	public Collection<? extends XStudentId> findStudents(StudentMatcher matcher);
	
	public XCourse getCourse(Long courseId);
	public XCourseId getCourse(String course);
	public XCourseId getCourse(Long courseId, String courseName);
	public String getCourseDetails(Long courseId, CourseDetailsProvider provider);
	
	public XStudent getStudent(Long studentId);
	public XOffering getOffering(Long offeringId);
	public Collection<XCourseRequest> getRequests(Long offeringId);
	public XEnrollments getEnrollments(Long offeringId);
	public XExpectations getExpectations(Long offeringId);
	public Collection<Long> getInstructedOfferings(String instructorExternalId);
	public Set<Long> getRequestedCourseIds(Long studentId);
	
	public void update(XExpectations expectations);
	
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) throws SectioningException;
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException;
	public <E> void execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user, ServerCallback<E> callback) throws SectioningException;
	
	public void remove(XStudent student);

	public void update(XStudent student, boolean updateRequests);
	
	public void remove(XOffering offering);
	
	public void update(XOffering offering);
	
	public void clearAll();
	
	public void clearAllStudents();
	
	public XCourseRequest assign(XCourseRequest request, XEnrollment enrollment);
	
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist);
	
	public Lock readLock();
	
	public Lock writeLock();
	
	public Lock lockAll();
	
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, String actionName);
	
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, String actionName);
	
	public Lock lockRequest(CourseRequestInterface request, String actionName);
	
	public boolean isOfferingLocked(Long offeringId);
	
	public void lockOffering(Long offeringId);
	
	public void unlockOffering(Long offeringId);
	
	public Collection<Long> getLockedOfferings();
	
	public void releaseAllOfferingLocks();
	
	public void persistExpectedSpaces(Long offeringId);
	
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge);
	
	public boolean needPersistExpectedSpaces(Long offeringId);

	public boolean isReady();
	
	public static enum Deadline { NEW, CHANGE, DROP };
	
	public boolean checkDeadline(Long courseId, XTime sectionTime, Deadline type);
	public CourseDeadlines getCourseDeadlines(Long courseId);
	
	public void unload();
	public void reload();
	
	public long getMemUsage();
	
	public <E> E getProperty(String name, E defaultValue);
	public <E> void setProperty(String name, E value);
	
	public void setSchedulingRules(XSchedulingRules rules);
	public XSchedulingRule getSchedulingRule(Long studentId, StudentSchedulingRule.Mode mode, boolean isAdvisor, boolean isAdmin);
	public XSchedulingRule getSchedulingRule(XStudent student, StudentSchedulingRule.Mode mode, boolean isAdvisor, boolean isAdmin);
	
	public static interface Lock {
		void release();
	}
	
	public static interface ServerCallback<E> {
		public void onFailure(Throwable exception);
		public void onSuccess(E result);
	}
	
	public static interface CourseDeadlines extends Serializable {
		public boolean isEnabled();
		public boolean checkDeadline(XTime sectionTime, Deadline type);
	}

	public XStudent getStudentForExternalId(String externalUniqueId);
	public Collection<XClassEnrollment> getStudentSchedule(String studentExternalId);
	public float[] getCredits(String studentExternalId);
	public float[] getCredits(XStudent student);
}
