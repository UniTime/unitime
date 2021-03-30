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
package org.unitime.timetable.onlinesectioning.custom;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.util.DefaultExternalClassNameHelper;

/**
 * @author Tomas Muller
 */
public enum Customization {
	CustomCourseLookup(CustomCourseLookup.class, ApplicationProperty.CustomizationCourseLookup),
	CourseRequestsProvider(CourseRequestsProvider.class, ApplicationProperty.CustomizationCourseRequests),
	CourseRequestsValidationProvider(CourseRequestsValidationProvider.class, ApplicationProperty.CustomizationCourseRequestsValidation),
	CriticalCoursesProvider(CriticalCoursesProvider.class, ApplicationProperty.CustomizationCriticalCourses),
	DegreePlansProvider(DegreePlansProvider.class, ApplicationProperty.CustomizationDegreePlans),
	SpecialRegistrationProvider(SpecialRegistrationProvider.class, ApplicationProperty.CustomizationSpecialRegistration),
	StudentEnrollmentProvider(StudentEnrollmentProvider.class, ApplicationProperty.CustomizationStudentEnrollments),
	CourseMatcherProvider(CourseMatcherProvider.class, ApplicationProperty.CustomizationCourseMatcher),
	CourseDetailsProvider(CourseDetailsProvider.class, ApplicationProperty.CustomizationCourseDetails, DefaultCourseDetailsProvider.class),
	ExternalTermProvider(ExternalTermProvider.class, ApplicationProperty.CustomizationExternalTerm),
	StudentEmailProvider(StudentEmailProvider.class, ApplicationProperty.CustomizationStudentEmail),
	CustomClassAttendanceProvider(CustomClassAttendanceProvider.class, ApplicationProperty.CustomizationCustomClassAttendance),
	StudentHoldsCheckProvider(StudentHoldsCheckProvider.class, ApplicationProperty.CustomizationStudentHoldsCheck),
	ClassNamingHelper(ExternalClassNameHelperInterface.class, ApplicationProperty.ClassNamingHelper, DefaultExternalClassNameHelper.class),
	SpecialRegistrationDashboardUrlProvider(SpecialRegistrationDashboardUrlProvider.class, ApplicationProperty.CustomizationSpecialRegistrationDashboardUrl),
	AdvisorCourseRequestsValidationProvider(AdvisorCourseRequestsValidationProvider.class, ApplicationProperty.CustomizationAdvisorsCourseRequestsValidation),
	VariableTitleCourseProvider(VariableTitleCourseProvider.class, ApplicationProperty.CustomizationVariableTitleCourseProvider),
	;
	
	private Holder<?> iHolder;
	<T> Customization(Class<T> name, ApplicationProperty property) {
		iHolder = new Holder<T>(name, property);
	}
	<T> Customization(Class<T> name, ApplicationProperty property, Class<? extends T> defaultProvider) {
		iHolder = new Holder<T>(name, property, defaultProvider);
	}
	
	public <T> T getProvider() {
		return (T)iHolder.getProvider();
	}
	
	public void release() {
		iHolder.release();
	}
	
	public boolean hasProvider() {
		return iHolder.hasProvider();
	}
}
