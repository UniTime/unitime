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
package org.unitime.timetable.onlinesectioning.match;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseMatcherProvider;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XInstructionalMethod;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class RuleCheckingCourseMatcherProvider implements CourseMatcherProvider {

	@Override
	public CourseMatcher getCourseMatcher(OnlineSectioningServer server, SessionContext context, Long studentId) {
		StudentSchedulingRule rule = StudentSchedulingRule.getRuleFilter(studentId, server, context);
		if (rule != null)
			return new SchedulingRuleCourseMatcher(rule);
		return new FallbackCourseMatcher();
	}
	
	public static class FallbackCourseMatcher extends SkipDisabledCourseMatcher {
		private static final long serialVersionUID = 1L;
		protected boolean iShowDisabled;
		
		public FallbackCourseMatcher() {
			iShowDisabled = "true".equalsIgnoreCase(ApplicationProperty.OnlineSchedulingParameter.value("Filter.ShowDisabled", "true"));
		}
		
		@Override
		protected boolean isEnabledForStudentScheduling(InstrOfferingConfig config) {
			if (iShowDisabledWhenNotLoaded) return true;
			return super.isEnabledForStudentScheduling(config);
		}
		
		@Override
		public boolean match(XCourseId course) {
			return (iShowDisabled || isEnabledForStudentScheduling(course));

		}
	}
	
	public static class SchedulingRuleCourseMatcher extends FallbackCourseMatcher {
		private static final long serialVersionUID = 1L;
		private String iInstructionalMode;
		private String iCourseRegExp;
		
		public SchedulingRuleCourseMatcher(StudentSchedulingRule rule) {
			super();
			iInstructionalMode = rule.getInstructonalMethod();
			iCourseRegExp = rule.getCourseName();
		}
		
		public boolean matchesInstructionalMethod(String im) {
			// not set > always matches
			if (iInstructionalMode == null || iInstructionalMode.isEmpty()) return true;
			if (iInstructionalMode.equals("-")) {
				return im == null || im.isEmpty();
			}
			if (iInstructionalMode.startsWith("!")) {
				return im != null && !im.matches(iInstructionalMode.substring(1));
			}
			return im != null && im.matches(iInstructionalMode);
		}
		
		public boolean matchesInstructionalMethod(InstructionalMethod im) {
			return matchesInstructionalMethod(im == null ? null : im.getReference());
		}
		
		public boolean matchesInstructionalMethod(XInstructionalMethod im) {
			return matchesInstructionalMethod(im == null ? null : im.getReference());
		}
		
		public boolean matchesCourseName(String cn) {
			// not set > always matches
			if (iCourseRegExp == null || iCourseRegExp.isEmpty()) return true;
			if (iCourseRegExp.startsWith("!")) {
				return cn != null && !cn.matches(iCourseRegExp.substring(1));
			}
			return cn != null && cn.matches(iCourseRegExp);
		}

		@Override
		public boolean match(XCourseId course) {
			if (!matchesCourseName(course.getCourseName())) {
				return false;
			} else if (iInstructionalMode != null) {
				if (getServer() != null && !(getServer() instanceof DatabaseServer)) {
					XOffering offering = getServer().getOffering(course.getOfferingId());
					if (offering != null) {
						for (XConfig config: offering.getConfigs())
							if (matchesInstructionalMethod(config.getInstructionalMethod())) {
								if (iShowDisabled || isEnabledForStudentScheduling(config)) return true;
							}
					}
				} else {
					InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId());
					if (offering != null) {
						for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
							if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) {
								if (iShowDisabled || isEnabledForStudentScheduling(config)) return true;
							}
						}
					}
				}
				return false;
			} else {
				return (iShowDisabled || isEnabledForStudentScheduling(course));
			}
		}
	}
}
