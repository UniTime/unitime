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
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseMatcherProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbStudentMatcher;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class OnlineOnlyCourseMatcherProvider implements CourseMatcherProvider {

	@Override
	public CourseMatcher getCourseMatcher(OnlineSectioningServer server, SessionContext context, Long studentId) {
		String filter = ApplicationProperty.OnlineSchedulingParameter.value("Load.OnlineOnlyStudentFilter", null);
		if (filter == null || filter.isEmpty()) return null;
		if (server != null && !(server instanceof DatabaseServer)) {
			if (context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdvisor)) {
				if ("true".equalsIgnoreCase(ApplicationProperty.OnlineSchedulingParameter.value("OnlineOnlyAdminOverride", "false")))  return null;
			}
			XStudent student = server.getStudent(studentId);
			if (student == null) return null;
			if (new Query(filter).match(new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false)))
				return new OnlineOnlyCourseMatcher(ApplicationProperty.OnlineSchedulingParameter.value("Load.OnlineOnlyInstructionalModeRegExp", ""));
			return null;
		} else {
			Student student = StudentDAO.getInstance().get(studentId);
			if (student == null) return null;
			if (context.hasPermissionAnySession(student.getSession(), Right.StudentSchedulingAdvisor)) {
				if ("true".equalsIgnoreCase(ApplicationProperty.OnlineSchedulingParameter.value("OnlineOnlyAdminOverride", "false")))  return null;
			}
			if (new Query(filter).match(new DbStudentMatcher(student)))
				return new OnlineOnlyCourseMatcher(ApplicationProperty.OnlineSchedulingParameter.value("Load.OnlineOnlyInstructionalModeRegExp", ""));
			return null;
		}
	}

	public static class OnlineOnlyCourseMatcher implements CourseMatcher {
		private static final long serialVersionUID = 1L;
		private transient OnlineSectioningServer iServer;
		private String iInstructionalMode;
		
		public OnlineOnlyCourseMatcher(String im) {
			iInstructionalMode = im;
		}

		@Override
		public void setServer(OnlineSectioningServer server) { iServer = server; }

		@Override
		public OnlineSectioningServer getServer() { return iServer; }

		@Override
		public boolean match(XCourseId course) {
			if (getServer() != null && !(getServer() instanceof DatabaseServer)) {
				XOffering offering = getServer().getOffering(course.getOfferingId());
				if (offering != null) {
					for (XConfig config: offering.getConfigs()) {
	        			if (iInstructionalMode.isEmpty()) {
	        				if (config.getInstructionalMethod() == null || config.getInstructionalMethod().getReference() == null || config.getInstructionalMethod().getReference().isEmpty())
	        					return true;
	        			} else {
	        				if (config.getInstructionalMethod() != null && config.getInstructionalMethod().getReference() != null && config.getInstructionalMethod().getReference().matches(iInstructionalMode))
	        					return true;
	        			}
	        		}
				}
			} else {
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId());
				if (offering != null) {
					for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
						if (iInstructionalMode.isEmpty()) {
							if (config.getInstructionalMethod() == null || config.getInstructionalMethod().getReference() == null || config.getInstructionalMethod().getReference().isEmpty())
	        					return true;
						} else {
							if (config.getInstructionalMethod() != null && config.getInstructionalMethod().getReference() != null && config.getInstructionalMethod().getReference().matches(iInstructionalMode))
	        					return true;
						}
					}
				}
			}
			return false;
		}

	}
}
