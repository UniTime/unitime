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
import org.unitime.timetable.model.CourseType;
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
import org.unitime.timetable.onlinesectioning.model.XSchedulingRule;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class RuleCheckingCourseMatcherProvider implements CourseMatcherProvider {

	@Override
	public CourseMatcher getCourseMatcher(OnlineSectioningServer server, SessionContext context, Long studentId) {
		if (server != null && !(server instanceof DatabaseServer)) {
			XSchedulingRule rule = server.getSchedulingRule(studentId,
					StudentSchedulingRule.Mode.Filter,
					context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdvisor),
					context.hasPermissionAnySession(server.getAcademicSession(), Right.StudentSchedulingAdmin));
			if (rule != null)
				return new SchedulingRuleCourseMatcher(rule);
		} else {
			StudentSchedulingRule rule = StudentSchedulingRule.getRuleFilter(studentId, server, context);
			if (rule != null)
				return new SchedulingRuleCourseMatcher(rule);
		}
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
		private String iCourseName;
		private String iCourseType;
		private String iInstructonalMethod;
		private Boolean iDisjunctive;
		
		public SchedulingRuleCourseMatcher(StudentSchedulingRule rule) {
			super();
			iCourseName = rule.getCourseName();
			iCourseType = rule.getCourseType();
			iInstructonalMethod = rule.getInstructonalMethod();
			iDisjunctive = rule.isDisjunctive();
		}
		
		public SchedulingRuleCourseMatcher(XSchedulingRule rule) {
			super();
			iCourseName = rule.getCourseName();
			iCourseType = rule.getCourseType();
			iInstructonalMethod = rule.getInstructonalMethod();
			iDisjunctive = rule.isDisjunctive();
		}
		
		public String getCourseName() { return iCourseName; }
		public String getCourseType() { return iCourseType; }
		public String getInstructonalMethod() { return iInstructonalMethod; }
		public Boolean isDisjunctive() { return iDisjunctive; }
		
		public boolean hasInstructionalMethod() {
			return getInstructonalMethod() != null && !getInstructonalMethod().isEmpty();
		}
		
		public boolean matchesInstructionalMethod(String im) {
			// not set > always matches
			if (!hasInstructionalMethod()) return true;
			if (getInstructonalMethod().startsWith("!")) {
				return im != null && !im.matches(getInstructonalMethod().substring(1));
			} else {
				return im != null && im.matches(getInstructonalMethod());
			}
		}
		
		public boolean matchesInstructionalMethod(InstructionalMethod im) {
			return matchesInstructionalMethod(im == null ? null : im.getReference());
		}
		
		public boolean matchesInstructionalMethod(XInstructionalMethod im) {
			return matchesInstructionalMethod(im == null ? null : im.getReference());
		}
		
		public boolean hasCourseName() {
			return getCourseName() != null && !getCourseName().isEmpty();
		}
		
		public boolean matchesCourseName(String cn) {
			// not set > always matches
			if (!hasCourseName()) return true;
			if (getCourseName().startsWith("!")) {
				return cn != null && !cn.matches(getCourseName().substring(1));
			}
			return cn != null && cn.matches(getCourseName());
		}
		
		public boolean hasCourseType() {
			return getCourseType() != null && !getCourseType().isEmpty();
		}
		
		public boolean matchesCourseType(String ct) {
			// not set > always matches
			if (!hasCourseType()) return true;
			if (getCourseType().startsWith("!")) {
				return ct != null && !ct.matches(getCourseType().substring(1));
			} else {
				return ct != null && ct.matches(getCourseType());
			}
		}
		
		public boolean matchesCourseType(CourseType ct) {
			return matchesCourseType(ct == null ? null : ct.getReference());
		}
		
		public boolean matchesCourse(XCourseId course) {
			if (getServer() != null && !(getServer() instanceof DatabaseServer)) {
				if (isDisjunctive()) {
					if (hasCourseName() && matchesCourseName(course.getCourseName())) return true;
					if (hasCourseType() && matchesCourseType(course.getType())) return true;
					if (hasInstructionalMethod()) {
						XOffering offering = getServer().getOffering(course.getOfferingId());
						if (offering != null)
							for (XConfig config: offering.getConfigs()) {
								if (matchesInstructionalMethod(config.getInstructionalMethod())) return true;	
							}
					}
					return false;
				} else {
					if (hasCourseName() && !matchesCourseName(course.getCourseName())) return false;
					if (hasCourseType() && !matchesCourseType(course.getType())) return false;
					if (hasInstructionalMethod()) {
						XOffering offering = getServer().getOffering(course.getOfferingId());
						boolean hasMatchingConfig = false;
						if (offering != null)
							for (XConfig config: offering.getConfigs()) {
								if (matchesInstructionalMethod(config.getInstructionalMethod())) {
									hasMatchingConfig = true;	
									break;
								}
							}
						if (!hasMatchingConfig) return false;
					}
					return true;
				}
			} else {
				if (isDisjunctive()) {
					if (hasCourseName() && matchesCourseName(course.getCourseName())) return true;
					if (hasCourseType() && matchesCourseType(course.getType())) return true;
					if (hasInstructionalMethod()) {
						InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId());
						if (offering != null)
							for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
								if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) return true;	
							}
					}
					return false;
				} else {
					if (hasCourseName() && !matchesCourseName(course.getCourseName())) return false;
					if (hasCourseType() && !matchesCourseType(course.getType())) return false;
					if (hasInstructionalMethod()) {
						InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId());
						boolean hasMatchingConfig = false;
						if (offering != null)
							for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
								if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) {
									hasMatchingConfig = true;	
									break;
								}
							}
						if (!hasMatchingConfig) return false;
					}
					return true;
				}
			}
		}

		@Override
		public boolean match(XCourseId course) {
			if (!matchesCourse(course)) return false;
			return (iShowDisabled || isEnabledForStudentScheduling(course));
		}
	}
}
