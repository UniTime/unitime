/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.onlinesectioning;

import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
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
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;

/**
 * @author Tomas Muller
 */
public interface OnlineSectioningServer {
	public boolean isMaster();
	@CheckMaster(Master.REQUIRED)
	public void releaseMasterLockIfHeld();
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
	
	@CheckMaster(Master.REQUIRED)
	public void update(XExpectations expectations);
	
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException;
	public <E> void execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user, ServerCallback<E> callback) throws SectioningException;
	
	@CheckMaster(Master.REQUIRED)
	public void remove(XStudent student);
	
	@CheckMaster(Master.REQUIRED)
	public void update(XStudent student, boolean updateRequests);
	
	@CheckMaster(Master.REQUIRED)
	public void remove(XOffering offering);
	
	@CheckMaster(Master.REQUIRED)
	public void update(XOffering offering);
	
	@CheckMaster(Master.REQUIRED)
	public void clearAll();
	
	@CheckMaster(Master.REQUIRED)
	public void clearAllStudents();
	
	@CheckMaster(Master.REQUIRED)
	public XCourseRequest assign(XCourseRequest request, XEnrollment enrollment);
	
	@CheckMaster(Master.REQUIRED)
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist);
	
	@CheckMaster(Master.REQUIRED)
	public Lock readLock();
	
	@CheckMaster(Master.REQUIRED)
	public Lock writeLock();
	
	@CheckMaster(Master.REQUIRED)
	public Lock lockAll();
	@CheckMaster(Master.REQUIRED)
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, boolean excludeLockedOfferings);
	
	@CheckMaster(Master.REQUIRED)
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, boolean excludeLockedOffering);
	
	@CheckMaster(Master.REQUIRED)
	public Lock lockRequest(CourseRequestInterface request);
	
	public boolean isOfferingLocked(Long offeringId);
	
	@CheckMaster(Master.REQUIRED)
	public void lockOffering(Long offeringId);
	
	@CheckMaster(Master.REQUIRED)
	public void unlockOffering(Long offeringId);
	
	public Collection<Long> getLockedOfferings();
	
	@CheckMaster(Master.REQUIRED)
	public void releaseAllOfferingLocks();
	
	@CheckMaster(Master.REQUIRED)
	public void persistExpectedSpaces(Long offeringId);
	
	@CheckMaster(Master.REQUIRED)
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge);
	
	@CheckMaster(Master.REQUIRED)
	public boolean needPersistExpectedSpaces(Long offeringId);
	
	@CheckMaster(Master.REQUIRED)
	public boolean isReady();
	
	public static enum Deadline { NEW, CHANGE, DROP };
	
	public boolean checkDeadline(Long courseId, XTime sectionTime, Deadline type);
	
	public void unload(boolean remove);
	
	public long getMemUsage();
	
	public <E> E getProperty(String name, E defaultValue);
	public <E> void setProperty(String name, E value);
	
	public static interface Lock {
		void release();
	}
	
	public static interface ServerCallback<E> {
		public void onFailure(Throwable exception);
		public void onSuccess(E result);
	}
}
