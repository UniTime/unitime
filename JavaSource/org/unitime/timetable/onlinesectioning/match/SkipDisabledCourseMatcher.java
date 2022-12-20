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
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseMatcherProvider;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class SkipDisabledCourseMatcher extends AbstractCourseMatcher implements CourseMatcherProvider {
	private static final long serialVersionUID = 3185616799094748160L;
	protected boolean iShowDisabledWhenNotLoaded;
	
	public SkipDisabledCourseMatcher() {
		iShowDisabledWhenNotLoaded = "true".equalsIgnoreCase(ApplicationProperty.OnlineSchedulingParameter.value("Filter.ShowDisabledWhenNotLoaded", "true"));
	}

	protected boolean isEnabledForStudentScheduling(XConfig config) {
		if (config.getSubparts().isEmpty()) return false;
		for (XSubpart subpart: config.getSubparts()) {
			if (!isEnabledForStudentScheduling(subpart)) return false;
		}
		return true;
	}
	
	protected boolean isEnabledForStudentScheduling(XSubpart subpart) {
		for (XSection section: subpart.getSections()) {
			if (section.isEnabledForScheduling()) return true;
		}
		return false;
	}
	
	protected boolean isEnabledForStudentScheduling(XOffering offering) {
		for (XConfig config: offering.getConfigs())
			if (isEnabledForStudentScheduling(config)) return true;
		return false;
	}
	
	protected boolean isEnabledForStudentScheduling(InstrOfferingConfig config) {
		if (config.getSchedulingSubparts().isEmpty()) return false;
		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
			if (!isEnabledForStudentScheduling(subpart)) return false;
		}
		return true;
	}
	
	protected boolean isEnabledForStudentScheduling(SchedulingSubpart subpart) {
		for (Class_ section: subpart.getClasses()) {
			if (Boolean.TRUE.equals(section.isEnabledForStudentScheduling())) return true;
		}
		return false;
	}
	
	protected boolean isEnabledForStudentScheduling(InstructionalOffering offering) {
		for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
			if (isEnabledForStudentScheduling(config)) return true;
		return false;
	}
	
	protected boolean isEnabledForStudentScheduling(XCourseId course) {
		if (getServer() == null || getServer() instanceof DatabaseServer) {
			if (iShowDisabledWhenNotLoaded) return true;
			return isEnabledForStudentScheduling(InstructionalOfferingDAO.getInstance().get(course.getOfferingId()));
		} else {
			return isEnabledForStudentScheduling(getServer().getOffering(course.getOfferingId()));
		}
	}	

	@Override
	public boolean match(XCourseId course) {
		if (!isEnabledForStudentScheduling(course)) return false;
		return true;
	}

	@Override
	public CourseMatcher getCourseMatcher(OnlineSectioningServer server, SessionContext context, Long studentId) {
		return new SkipDisabledCourseMatcher();
	}
}
