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

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class GetStudentPreferences implements OnlineSectioningAction<StudentSchedulingPreferencesInterface> {
	private static final long serialVersionUID = 1L;

	private Long iStudentId = null;

	public GetStudentPreferences forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}

	@Override
	public StudentSchedulingPreferencesInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		StudentSchedulingPreferencesInterface ret = new StudentSchedulingPreferencesInterface();
		ApplicationProperties.setSessionId(server.getAcademicSession().getUniqueId());
		ret.setAllowClassDates(ApplicationProperty.OnlineSchedulingStudentPreferencesDatesAllowed.isTrue());
		ret.setAllowRequireOnline(ApplicationProperty.OnlineSchedulingStudentPreferencesReqOnlineAllowed.isTrue());
		ret.setCustomNote(ApplicationProperty.OnlineSchedulingStudentPreferencesNote.value());
		XStudent student = server.getStudent(iStudentId);
		if (student != null) {
			ret.setClassModality(student.getPreferredClassModality());
			ret.setScheduleGaps(student.getPreferredScheduleGaps());
			ret.setClassDateFrom(student.getClassStartDate(server.getAcademicSession().getDatePatternFirstDate()));
			ret.setClassDateTo(student.getClassEndDate(server.getAcademicSession().getDatePatternFirstDate()));
		}
		return ret;
	}

	@Override
	public String name() {
		return "get-schedule-prefs";
	}

}
