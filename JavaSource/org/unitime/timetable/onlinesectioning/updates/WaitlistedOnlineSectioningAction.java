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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.model.Student.StudentPriority;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.WaitListComparatorProvider;
import org.unitime.timetable.onlinesectioning.custom.WaitListValidationProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudent.XGroup;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequestComparator;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction;

/**
 * @author Tomas Muller
 */
public abstract class WaitlistedOnlineSectioningAction<T> implements OnlineSectioningAction<T> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Set<String> iWaitlistStatuses = null;
	private Map<StudentPriority, String> iPriorityStudentGroupReference = null;
	private Map<StudentPriority, Query> iPriorityStudentQuery = null;
	
	public boolean isWaitListed(XStudent student, XCourseRequest request, XOffering offering, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		// Check wait-list toggle first or if already enrolled
		if (student == null || request == null || !request.isWaitlist() || request.getEnrollment() != null) return false;
		
		// Check if student can assign
		if (!student.canAssign(request, WaitListMode.WaitList)) return false;
		
		// Check student status
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status != null) {
			if (iWaitlistStatuses == null)
				iWaitlistStatuses = StudentSectioningStatus.getMatchingStatuses(server.getAcademicSession().getUniqueId(), StudentSectioningStatus.Option.waitlist, StudentSectioningStatus.Option.enrollment);
			if (!iWaitlistStatuses.contains(status)) return false;
		}
		
		// Check wait-list overrides, when configured
		if (Customization.WaitListValidationProvider.hasProvider()) {
			// Student has a max credit override >> check credit
			Float credit = null;
			if (student.getMaxCreditOverride() != null && student.getMaxCredit() != null) {
				credit = 0f;
				for (XRequest r: student.getRequests()) {
					if (r instanceof XCourseRequest && ((XCourseRequest)r).getEnrollment() != null) {
						credit += ((XCourseRequest)r).getEnrollment().getCredit(server);
					}
				}
			}
			for (XCourse course: offering.getCourses()) {
				if (!request.hasCourse(course.getCourseId())) continue;
				XOverride override = request.getOverride(course);
				if (override != null) {
					if ("TBD".equals(override.getExternalId())) continue; // override not requested --> ignore
					WaitListValidationProvider wp = Customization.WaitListValidationProvider.getProvider();
					try {
						if (wp.updateStudent(server, helper, student, helper.getAction()))
							override = request.getOverride(course);
					} catch (Exception e) {
						helper.warn("Failed to check wait-list status for student " + student.getExternalId() + ": " + e.getMessage());
					}
				} else if (credit != null && course.hasCredit() && credit + course.getMinCredit() > student.getMaxCredit()) {
					WaitListValidationProvider wp = Customization.WaitListValidationProvider.getProvider();
					try {
						wp.updateStudent(server, helper, student, helper.getAction());
					} catch (Exception e) {
						helper.warn("Failed to check wait-list status for student " + student.getExternalId() + ": " + e.getMessage());
					}	
				}
				// override for this course is not approved
				if (override != null && !override.isApproved()) continue;
				if (credit != null && course.hasCredit() && credit + course.getMinCredit() > student.getMaxCredit()) continue;
				return true;
			}
			return false;
		}
		
		return true;
	}
	
	public boolean isWaitListedAssumeApproved(XStudent student, XCourseRequest request, XOffering offering, XCourseId courseId, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		// No student
		if (student == null) return false;
		
		// Check wait-list toggle first
		if (request == null || !request.isWaitlist()) return false;
		
		// Ignore enrolled requests
		if (request.getEnrollment() != null) return false;
		
		// Check that the offering can be wait-listed
		if (!offering.isWaitList()) return false;
		
		// No matching course
		if (courseId == null) return false;
		
		// Check student status
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status != null) {
			if (iWaitlistStatuses == null)
				iWaitlistStatuses = StudentSectioningStatus.getMatchingStatuses(server.getAcademicSession().getUniqueId(), StudentSectioningStatus.Option.waitlist, StudentSectioningStatus.Option.enrollment);
			if (!iWaitlistStatuses.contains(status)) return false;
		}
		
		return true;
	}
	
	public StudentPriority getStudentPriority(XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iPriorityStudentGroupReference == null) {
			iPriorityStudentGroupReference = new HashMap<StudentPriority, String>();
			iPriorityStudentQuery = new HashMap<StudentPriority, Query>();
			DataProperties config = server.getConfig();
			for (StudentPriority priority: StudentPriority.values()) {
	        	if (priority == StudentPriority.Normal) break;
	            String priorityStudentFilter = config.getProperty("Load." + priority.name() + "StudentFilter", null);
	            if (priorityStudentFilter != null && !priorityStudentFilter.isEmpty()) {
	            	Query q = new Query(priorityStudentFilter);
	            	iPriorityStudentQuery.put(priority, q);
	            }
	            String groupRef = config.getProperty("Load." + priority.name() + "StudentGroupReference", null);
	            if (groupRef != null && !groupRef.isEmpty()) {
	            	iPriorityStudentGroupReference.put(priority, groupRef);
	            }
	        }
		}
		for (StudentPriority priority: StudentPriority.values()) {
        	if (priority == StudentPriority.Normal) break;
        	Query query = iPriorityStudentQuery.get(priority);
        	String groupRef = iPriorityStudentGroupReference.get(priority);
        	if (query != null && query.match(new StatusPageSuggestionsAction.StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
            	return priority;
        	} else if (groupRef != null) {
        		for (XGroup g: student.getGroups()) {
            		if (groupRef.equals(g.getAbbreviation())) {
            			return priority;
            		}
            	}
        	}
        }
		return StudentPriority.Normal;
	}
	
	public String getWaitListPosition(XOffering offering, XStudent student, XCourseRequest request, XCourseId courseId, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!isWaitListedAssumeApproved(student, request, offering, courseId, server, helper)) return null;
		if (!courseId.equals(request.getCourseIdByOfferingId(offering.getOfferingId()))) return null;
		XEnrollments enrl = server.getEnrollments(offering.getOfferingId());
		if (enrl == null) return null;
		
		WaitListComparatorProvider cmpProvider = Customization.WaitListComparatorProvider.getProvider();
		Comparator<SectioningRequest> cmp = (cmpProvider == null ? new SectioningRequestComparator() : cmpProvider.getComparator(server, helper));
		
		SectioningRequest sr = new SectioningRequest(offering, request, courseId, student, getStudentPriority(student, server, helper), null);
		int before = 0, total = 0;
		for (XCourseRequest cr: enrl.getRequests()) {
			if (!cr.isWaitlist() || cr.getEnrollment() != null) continue; // skip enrolled or not wait-listed
			XStudent s = server.getStudent(cr.getStudentId());
			XCourseId c = cr.getCourseIdByOfferingId(offering.getOfferingId());
			if (!isWaitListedAssumeApproved(s, request, offering, c, server, helper)) continue; // skip not wait-listed
			total ++;
			if (!cr.equals(request)) {
				SectioningRequest other = new SectioningRequest(offering, cr, c, s, getStudentPriority(s, server, helper), null);
				if (cmp.compare(other, sr) < 0) before ++;
			}
		}
		
		return MSG.waitListPosition(before + 1, total);
	}
}
