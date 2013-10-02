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
package org.unitime.timetable.onlinesectioning;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XTime;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;

/**
 * @author Tomas Muller
 */
public interface OnlineSectioningServer {
	public String getHost();
	public String getUser();
	
	public AcademicSessionInfo getAcademicSession();
	public DistanceMetric getDistanceMetric();
	public DataProperties getConfig();
	
	public Collection<? extends XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher);
	public Collection<? extends XCourseId> findCourses(CourseMatcher matcher);
	public Collection<? extends XStudentId> findStudents(StudentMatcher matcher);
	
	public XCourse getCourse(Long courseId);
	public XCourseId getCourse(String course);
	public String getCourseDetails(Long courseId, CourseDetailsProvider provider);
	
	public XStudent getStudent(Long studentId);
	public XOffering getOffering(Long offeringId);
	public Collection<XCourseRequest> getRequests(Long offeringId);
	public XEnrollments getEnrollments(Long offeringId);
	public XExpectations getExpectations(Long offeringId);
	public void update(XExpectations expectations);
	
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
	
	public void addDistribution(XDistribution distribution);
	public Collection<XDistribution> getDistributions(Long offeringId);
	
	public Lock readLock();
	public Lock writeLock();
	public Lock lockAll();
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, boolean excludeLockedOfferings);
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, boolean excludeLockedOffering);
	public Lock lockRequest(CourseRequestInterface request);
	
	public boolean isOfferingLocked(Long offeringId);
	public void lockOffering(Long offeringId);
	public void unlockOffering(Long offeringId);
	public Collection<Long> getLockedOfferings();
	public void releaseAllOfferingLocks();
	
	public void persistExpectedSpaces(Long offeringId);
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge);
	public boolean needPersistExpectedSpaces(Long offeringId);
	
	public static enum Deadline { NEW, CHANGE, DROP };
	
	public boolean checkDeadline(Long courseId, XTime sectionTime, Deadline type);
	
	public void unload();
	
	public static interface Lock {
		void release();
	}
	
	public static interface ServerCallback<E> {
		public void onFailure(Throwable exception);
		public void onSuccess(E result);
	}
	
	public static interface CourseMatcher extends Serializable {
		public boolean match(XCourseId course);
	}

	public static interface StudentMatcher extends Serializable {
		public boolean match(XStudentId student);
	}
}
