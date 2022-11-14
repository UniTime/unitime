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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.util.Collection;
import java.util.List;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.AdvisorCourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;

/**
 * @author Tomas Muller
 */
public class TwoPhaseCourseRequestValidationProvider implements CourseRequestsValidationProvider, AdvisorCourseRequestsValidationProvider {
	SimplifiedCourseRequestsValidationProvider iSimplifiedValidation;
	PurdueCourseRequestsValidationProvider iFullValidation;
	
	public TwoPhaseCourseRequestValidationProvider() {
		iSimplifiedValidation = new SimplifiedCourseRequestsValidationProvider();
		iFullValidation = new PurdueCourseRequestsValidationProvider();
	}
	
	public boolean isFullValidation(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		String mode = ApplicationProperties.getProperty("purdue.specreg.courseReqValMode", "assistant");
		
		if ("simplified".equals(mode) || "simple".equals(mode)) {
			// always simplified
			return false;
		} else if ("full".equals(mode)) {
			// always full
			return true;
		} else if ("assistant".equals(mode)) {
			// when online scheduling server is running
			return server != null && !(server instanceof DatabaseServer);
		} else if ("online".equals(mode)) {
			// when online student scheduling is enabled
			return server != null && server.getAcademicSession().isSectioningEnabled();
		} else if ("published".equals(mode)) {
			if (server == null || server.getAcademicSession() == null) return false;
			Session session = SessionDAO.getInstance().get(server.getAcademicSession().getUniqueId(), helper.getHibSession());
			return session != null && session.canNoRoleReportClass();
		}
		
		// fallback: when online scheduling server is running
		return server != null && !(server instanceof DatabaseServer);
	}
	
	public CourseRequestsValidationProvider getCRVP(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (isFullValidation(server, helper))
			return iFullValidation;
		else
			return iSimplifiedValidation;
	}
	
	public AdvisorCourseRequestsValidationProvider getACRVP(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (isFullValidation(server, helper))
			return iFullValidation;
		else
			return iSimplifiedValidation;
	}

	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, Student student) throws SectioningException {
		getCRVP(server, helper).checkEligibility(server, helper, check, student);
	}

	@Override
	public void check(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
		getCRVP(server, helper).check(server, helper, request);
	}

	@Override
	public boolean updateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Builder action) throws SectioningException {
		return getCRVP(server, helper).updateStudent(server, helper, student, action);
	}

	@Override
	public boolean revalidateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Builder action) throws SectioningException {
		return getCRVP(server, helper).revalidateStudent(server, helper, student, action);
	}

	@Override
	public void validate(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request, CheckCoursesResponse response) throws SectioningException {
		getCRVP(server, helper).validate(server, helper, request, response);
	}

	@Override
	public void submit(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
		getCRVP(server, helper).submit(server, helper, request);
	}

	@Override
	public Collection<Long> updateStudents(OnlineSectioningServer server, OnlineSectioningHelper helper, List<Student> students) throws SectioningException {
		return getCRVP(server, helper).updateStudents(server, helper, students);
	}

	@Override
	public void validateAdvisorRecommendations(OnlineSectioningServer server, OnlineSectioningHelper helper, AdvisingStudentDetails request, CheckCoursesResponse response) throws SectioningException {
		getACRVP(server, helper).validateAdvisorRecommendations(server, helper, request, response);
	}

	@Override
	public void dispose() {
		iFullValidation.dispose();
		iSimplifiedValidation.dispose();
	}
}
