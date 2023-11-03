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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cpsolver.ifs.heuristics.RouletteWheelSelection;
import org.hibernate.criterion.Order;
import org.hibernate.type.LongType;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Filter;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.dao.OverrideTypeDAO;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRule;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
import org.unitime.timetable.util.NameInterface;

/**
 * @author Tomas Muller
 */
public class ListCourseOfferings implements OnlineSectioningAction<Collection<ClassAssignmentInterface.CourseAssignment>> {
	private static final long serialVersionUID = 1L;
	
	protected String iQuery = null;
	protected CourseRequestInterface.Request iRequest = null;
	protected Integer iLimit = null;
	protected CourseMatcher iMatcher = null;
	protected Long iStudentId;
	protected String iFilterIM = null;
	private transient XStudent iStudent = null;
	private transient XSchedulingRule iRule = null;
	protected Filter iFilter;
	
	public ListCourseOfferings forQuery(String query) {
		iQuery = query; return this;
	}
	
	public ListCourseOfferings forRequest(CourseRequestInterface.Request request) {
		iRequest = request; return this;	
	}
	
	public ListCourseOfferings withLimit(Integer limit) {
		iLimit = limit; return this;
	}
	
	public ListCourseOfferings withMatcher(CourseMatcher matcher) {
		iMatcher = matcher; return this;
	}
	
	public ListCourseOfferings forStudent(Long studentId) {
		iStudentId = studentId; return this;
	}
	
	public ListCourseOfferings withFilter(Filter filter) {
		iFilter = filter; return this;
	}
	
	public Long getStudentId() {
		return iStudentId;
	}
	
	@Override
	public Collection<CourseAssignment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			iStudent = (getStudentId() == null ? null : server.getStudent(getStudentId()));
			if (iStudent != null) {
				iRule = server.getSchedulingRule(iStudent,
						StudentSchedulingRule.Mode.Filter,
						helper.hasAvisorPermission(),
						helper.hasAdminPermission());
				if (iRule == null) {
					String filter = server.getConfig().getProperty("Filter.OnlineOnlyStudentFilter", null);
					if (filter != null && !filter.isEmpty()) {
						if (new Query(filter).match(new StudentMatcher(iStudent, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
							iFilterIM = server.getConfig().getProperty("Filter.OnlineOnlyInstructionalModeRegExp");
						} else if (server.getConfig().getPropertyBoolean("Filter.OnlineOnlyExclusiveCourses", false)) {
							iFilterIM = server.getConfig().getProperty("Filter.ResidentialInstructionalModeRegExp");
						}
					}
					if (iFilterIM != null) {
						if (helper.hasAdminPermission() && server.getConfig().getPropertyBoolean("Filter.OnlineOnlyAdminOverride", false))
							iFilterIM = null;
						else if (helper.hasAvisorPermission() && server.getConfig().getPropertyBoolean("Filter.OnlineOnlyAdvisorOverride", false))
							iFilterIM = null;
					}
				}
			}
			List<CourseAssignment> courses = null;
			if (iRequest != null) {
				courses = new ArrayList<CourseAssignment>();
				for (Long courseId: iRequest.getCourseIds()) {
					XCourse course = server.getCourse(courseId);
					if (course != null)
						courses.add(convert(course, server));
				}
			} else {
				courses = listCourses(server, helper);
			}
			if (courses != null && !courses.isEmpty() && courses.size() <= 1000) {
				List<OverrideType> overrides = OverrideTypeDAO.getInstance().findAll(helper.getHibSession(), Order.asc("label"));
				if (overrides != null && !overrides.isEmpty()) {
					Map<Long, CourseAssignment> table = new HashMap<Long, CourseAssignment>();
					for (CourseAssignment ca: courses)
						table.put(ca.getCourseId(), ca);
					for (CourseOffering co: (List<CourseOffering>)helper.getHibSession().createQuery("from CourseOffering co left join fetch co.disabledOverrides do where co.uniqueId in :courseIds")
							.setParameterList("courseIds", table.keySet(), LongType.INSTANCE).list()) {
						for (OverrideType override: overrides)
							if (!co.getDisabledOverrides().contains(override))
								table.get(co.getUniqueId()).addOverride(override.getReference(), override.getLabel());
					}
				}
			}
			if (ApplicationProperty.ListCourseOfferingsMatchingCampusFirst.isTrue() &&  iStudent != null && courses != null && !courses.isEmpty()) {
				XAreaClassificationMajor primary = iStudent.getPrimaryMajor();
				final String campus = (primary == null ? null : primary.getCampus());
				if (campus != null && !campus.equals(server.getAcademicSession().getCampus())) {
					ExternalTermProvider ext = Customization.ExternalTermProvider.getProvider();
					List<CourseAssignment> ret = new ArrayList<CourseAssignment>(courses.size());
					for (CourseAssignment ca: courses) {
						if (ext == null) {
							if (ca.getSubject().startsWith(campus + " - ")) ret.add(ca);
						} else {
							if (campus.equals(ext.getExternalCourseCampus(server.getAcademicSession(), ca.getSubject(), ca.getCourseNbr())))
								ret.add(ca);
						}
					}
					if (ret.isEmpty()) return courses;
					for (CourseAssignment ca: courses) {
						if (ext == null) {
							if (!ca.getSubject().startsWith(campus + " - ")) ret.add(ca);
						} else {
							if (!campus.equals(ext.getExternalCourseCampus(server.getAcademicSession(), ca.getSubject(), ca.getCourseNbr())))
								ret.add(ca);
						}
					}
					return ret;
				}
			}
			return courses;
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
			if (course != null && matchFilter(server, iFilter, course))
				ret.add(convert(course, server));
		}
		return ret;
	}
	
	protected List<CourseAssignment> customCourseLookup(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			if (iMatcher != null) iMatcher.setServer(server);
			if (iQuery != null && !iQuery.isEmpty() && CustomCourseLookupHolder.hasProvider()) {
				List<XCourse> courses = CustomCourseLookupHolder.getProvider().getCourses(server, helper, iQuery, true);
				if (courses != null && !courses.isEmpty()) {
					List<CourseAssignment> ret = new ArrayList<CourseAssignment>();
					for (XCourse course: courses) {
						if (course != null && (iMatcher == null || iMatcher.match(course)) && matchFilter(server, iFilter, course))
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
		XEnrollment enrollment = null;
		if ((iRule != null || iFilterIM != null) && iStudent != null) {
			XCourseRequest r = iStudent.getRequestForCourse(c.getCourseId());
			enrollment = (r == null ? null : r.getEnrollment());
		}
		if (offering != null) {
			course.setAvailability(offering.getCourseAvailability(server.getRequests(c.getOfferingId()), c));
			for (XConfig config: offering.getConfigs()) {
				if (iRule != null && iRule.isDisjunctive()) {
					if (iRule.hasCourseName() && iRule.matchesCourseName(c.getCourseName())) {
					} else if (iRule.hasCourseType() && iRule.matchesCourseType(c.getType())) {
					} else if (iRule.hasInstructionalMethod() && iRule.matchesInstructionalMethod(config.getInstructionalMethod())) {
					} else if (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId())) {
						continue;
					}
				} else {
					if (iRule != null && (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId()))) {
						if (!iRule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
					} else if (iFilterIM != null && (enrollment == null || !config.getConfigId().equals(enrollment.getConfigId()))) {
						String imRef = (config.getInstructionalMethod() == null ? null : config.getInstructionalMethod().getReference());
	        			if (iFilterIM.isEmpty()) {
	        				if (imRef != null && !imRef.isEmpty())
	        					continue;
	        			} else {
	        				if (imRef == null || !imRef.matches(iFilterIM))
	        					continue;
	        			}
					}
				}
				if (config.getInstructionalMethod() != null)
					course.addInstructionalMethod(config.getInstructionalMethod().getUniqueId(), config.getInstructionalMethod().getLabel());
				else
					course.setHasNoInstructionalMethod(true);
			}
			course.setHasCrossList(offering.hasCrossList());
			course.setCanWaitList(offering.isWaitList());
			
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
	
	protected boolean isAllowDisabled(XEnrollments enrollments, XStudent student, XOffering offering, XCourseId course, XConfig config, XSection section) {
		if (student == null) return false;
		if (student.isAllowDisabled()) return true;
		for (XReservation reservation: offering.getReservations())
			if (reservation.isAllowDisabled() && reservation.isApplicable(student, course) && reservation.isIncluded(offering, config.getConfigId(), section)) {
				return true;
			}
		for (XEnrollment enrollment: enrollments.getEnrollmentsForSection(section.getSectionId()))
			if (enrollment.getStudentId().equals(getStudentId())) {
				return true;
			}
		return false;
	}
	
	protected boolean matchFilter(OnlineSectioningServer server, Filter filter, XCourse co) {
		if (filter == null) return true;
		if (filter.getCreditMin() != null && co.getCreditInfo() != null) {
			Float credit = co.getCreditInfo().getMaxCredit();
			if (credit != null && credit < filter.getCreditMin()) return false;
		}
		if (filter.getCreditMax() != null && co.getCreditInfo() != null) {
			Float credit = co.getCreditInfo().getMinCredit();
			if (credit != null && credit > filter.getCreditMax()) return false;
		}
		XOffering io = null;
		XEnrollments enrl = null;
		if (filter.hasInstructor()) {
			if (io == null) io = server.getOffering(co.getOfferingId());
			boolean match = false;
			cfg: for (XConfig cfg: io.getConfigs())
				for (XSubpart ss: cfg.getSubparts())
					for (XSection c: ss.getSections()) {
						if (matchInstructorName(filter, c) && matchDates(server, filter, c)) {
							if (!c.isEnabledForScheduling()) {
								if (enrl == null) enrl = server.getEnrollments(co.getOfferingId());
								if (!isAllowDisabled(enrl, iStudent, io, co, cfg, c)) continue;
							}
							match = true; break cfg;
						}
					}
			if (!match) return false;
		}
		if (filter.hasDates()) {
			if (io == null) io = server.getOffering(co.getOfferingId());
			boolean match = false;
			cfg: for (XConfig cfg: io.getConfigs()) {
				for (XSubpart ss: cfg.getSubparts()) {
					boolean matchClass = false;
					c: for (XSection c: ss.getSections()) {
						if (matchDates(server, filter, c)) {
							if (!c.isEnabledForScheduling()) {
								if (enrl == null) enrl = server.getEnrollments(co.getOfferingId());
								if (!isAllowDisabled(enrl, iStudent, io, co, cfg, c)) continue;
							}
							XSection p = io.getSection(c.getParentId());
							while (p != null) {
								if (!matchDates(server, filter, p)) continue c;
								if (!p.isEnabledForScheduling()) {
									if (enrl == null) enrl = server.getEnrollments(co.getOfferingId());
									if (!isAllowDisabled(enrl, iStudent, io, co, cfg, p)) continue c;
								}
								p = io.getSection(p.getParentId());
							}
							matchClass = true; break;
						}
					}
					if (!matchClass) continue cfg;
				}
				match = true; break;
			}
			if (!match) return false;
		}
		return true;
	}
	
	protected boolean matchInstructorName(Filter filter, XSection clazz) {
		if (!filter.hasInstructor()) return true;
		for (XInstructor ci: clazz.getInstructors())
			if (ci.isAllowDisplay() && matchName(filter.getInstructor(), ci)) return true;
		return false;
	}
	
	protected boolean matchDates(OnlineSectioningServer server, Filter filter, XSection clazz) {
		if (!filter.hasDates() || clazz.getTime() == null) return true;
		if (filter.getDaysFrom() != null && clazz.getTime().getFirstMeeting(server.getAcademicSession().getDayOfWeekOffset()) < filter.getDaysFrom()) return false;
		if (filter.getDaysTo() != null && clazz.getTime().getLastMeeting(server.getAcademicSession().getDayOfWeekOffset()) > filter.getDaysTo()) return false;
		return true;
	}
	
	protected boolean matchName(String instructor, NameInterface name) {
		for (StringTokenizer s = new StringTokenizer(instructor); s.hasMoreTokens(); ) {
			String token = s.nextToken().toLowerCase();
			if (name.getFirstName() != null && name.getFirstName().toLowerCase().startsWith(token)) continue;
			if (name.getMiddleName() != null && name.getMiddleName().toLowerCase().startsWith(token)) continue;
			if (name.getLastName() != null && name.getLastName().toLowerCase().startsWith(token)) continue;
			if (name.getAcademicTitle() != null && name.getAcademicTitle().toLowerCase().startsWith(token)) continue;
			return false;
		}
		return true;
	}

	@Override
	public String name() {
		return "list-courses";
	}
}
