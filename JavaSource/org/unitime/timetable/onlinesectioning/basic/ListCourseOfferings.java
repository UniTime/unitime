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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.heuristics.RouletteWheelSelection;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XOffering;

/**
 * @author Tomas Muller
 */
public class ListCourseOfferings implements OnlineSectioningAction<Collection<ClassAssignmentInterface.CourseAssignment>> {
	private static final long serialVersionUID = 1L;
	
	protected String iQuery = null;
	protected Integer iLimit = null;
	protected CourseMatcher iMatcher = null; 
	
	public ListCourseOfferings forQuery(String query) {
		iQuery = query; return this;
	}
	
	public ListCourseOfferings withLimit(Integer limit) {
		iLimit = limit; return this;
	}
	
	public ListCourseOfferings withMatcher(CourseMatcher matcher) {
		iMatcher = matcher; return this;
	}
	
	@Override
	public Collection<CourseAssignment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			return listCourses(server, helper);
		} finally {
			lock.release();
		}
	}
	
	protected List<CourseAssignment> listCourses(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<CourseAssignment> ret = customCourseLookup(server, helper);
		if (ret != null && !ret.isEmpty()) return ret;
				
		ret = new ArrayList<CourseAssignment>();
		for (XCourseId id: server.findCourses(iQuery, iLimit, iMatcher)) {
			XCourse course = server.getCourse(id.getCourseId());
			if (course != null)
				ret.add(convert(course, server));
		}
		return ret;
	}
	
	protected List<CourseAssignment> customCourseLookup(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			if (iQuery != null && !iQuery.isEmpty() && CustomCourseLookupHolder.hasProvider()) {
				List<XCourse> courses = CustomCourseLookupHolder.getProvider().getCourses(server, helper, iQuery, true);
				if (courses != null && !courses.isEmpty()) {
					List<CourseAssignment> ret = new ArrayList<CourseAssignment>();
					for (XCourse course: courses) {
						if (course != null && (iMatcher == null || iMatcher.match(course)))
							ret.add(convert(course, server));
					}
					setSelection(ret);
					return ret;
				}
			}
		} catch (Exception e) {
			helper.error("Failed to use the custom course lookup: " + e.getMessage(), e);
		}
		return null;
	}
	
	protected CourseAssignment convert(XCourse c, OnlineSectioningServer server) {
		CourseAssignment course = new CourseAssignment();
		course.setCourseId(c.getCourseId());
		course.setSubject(c.getSubjectArea());
		course.setCourseNbr(c.getCourseNumber());
		course.setTitle(c.getTitle());
		course.setNote(c.getNote());
		course.setCreditAbbv(c.getCreditAbbv());
		course.setCreditText(c.getCreditText());
		course.setTitle(c.getTitle());
		course.setHasUniqueName(c.hasUniqueName());
		course.setLimit(c.getLimit());
		course.setSnapShotLimit(c.getSnapshotLimit());
		XOffering offering = server.getOffering(c.getOfferingId());
		if (offering != null) {
			course.setAvailability(offering.getCourseAvailability(server.getRequests(c.getOfferingId()), c));
			for (XConfig config: offering.getConfigs()) {
				if (config.getInstructionalMethod() != null)
					course.addInstructionalMethod(config.getInstructionalMethod().getUniqueId(), config.getInstructionalMethod().getLabel());
				else
					course.setHasNoInstructionalMethod(true);
			}
			course.setHasCrossList(offering.hasCrossList());
		}
		return course;
	}
	
	static interface SelectionModeInterface {
		public int getPoints(CourseAssignment ca);
	}
	
	public static enum SelectionMode implements Comparator<CourseAssignment>{
		availability(new SelectionModeInterface() {
			@Override
			public int getPoints(CourseAssignment ca) {
				int p = 0;
				if (ca.getLimit() != null)
					p += 4 * (ca.getLimit() < 0 ? 9999 : ca.getLimit());
				if (ca.getEnrollment() != null)
					p -= 3 * (ca.getEnrollment());
				if (ca.getRequested() != null)
					p -= ca.getRequested();
				return p;
			}
			
		}),
		limit(new SelectionModeInterface() {
			@Override
			public int getPoints(CourseAssignment ca) {
				return (ca.getLimit() < 0 ? 999 : ca.getLimit());
			}
			
		}),
		snapshot(new SelectionModeInterface() {
			@Override
			public int getPoints(CourseAssignment ca) {
				int snapshot = (ca.getSnapShotLimit() == null ? 0 : ca.getSnapShotLimit() < 0 ? 999 : ca.getSnapShotLimit());
				int limit = (ca.getLimit() < 0 ? 999 : ca.getLimit());
				return Math.max(snapshot, limit);
			}
			
		}),
		;
		
		SelectionModeInterface iMode;
		SelectionMode(SelectionModeInterface mode) {
			iMode = mode;
		}
		
		public int getPoints(CourseAssignment ca) {
			return iMode.getPoints(ca);
		}

		@Override
		public int compare(CourseAssignment ca1, CourseAssignment ca2) {
			int p1 = getPoints(ca1);
			int p2 = getPoints(ca2);
			if (p1 != p2) return (p1 > p2 ? -1 : 1);
			return ca1.getCourseNameWithTitle().compareTo(ca2.getCourseNameWithTitle());
		}
	}
	
	public static void setSelection(List<CourseAssignment> courses) {
		if (courses == null || courses.isEmpty()) return;
		SelectionMode mode = SelectionMode.valueOf(ApplicationProperty.ListCourseOfferingsSelectionMode.value());
		int limit = ApplicationProperty.ListCourseOfferingsSelectionLimit.intValue();
		if (ApplicationProperty.ListCourseOfferingsSelectionRandomize.isTrue()) {
			RouletteWheelSelection<CourseAssignment> roulette = new RouletteWheelSelection<CourseAssignment>();
			for (CourseAssignment ca: courses) {
				int p = mode.getPoints(ca);
				if (p > 0) roulette.add(ca, p);
			}
			int idx = 0;
			while (roulette.hasMoreElements() && idx < limit) {
				CourseAssignment ca = roulette.nextElement();
				ca.setSelection(idx++);
			}
		} else {
			List<CourseAssignment> sorted = new ArrayList<CourseAssignment>(courses);
			Collections.sort(sorted, mode);
			int idx = 0;
			for (CourseAssignment ca: sorted) {
				int p = mode.getPoints(ca);
				if (p <= 0 || idx >= limit) break;
				ca.setSelection(idx++);
			}
		}
	}

	@Override
	public String name() {
		return "list-courses";
	}
}
