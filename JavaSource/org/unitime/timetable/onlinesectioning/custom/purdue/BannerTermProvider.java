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

import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;

/**
 * @author Tomas Muller
 */
public class BannerTermProvider implements ExternalTermProvider {

	@Override
	public String getExternalTerm(AcademicSessionInfo session) {
		if (session.getTerm().toLowerCase().startsWith("spr")) return session.getYear() + "20";
		if (session.getTerm().toLowerCase().startsWith("sum")) return session.getYear() + "30";
		if (session.getTerm().toLowerCase().startsWith("fal"))
			return String.valueOf(Integer.parseInt(session.getYear()) + 1) + "10";
		return session.getYear() + session.getTerm().toLowerCase();
	}

	@Override
	public String getExternalCampus(AcademicSessionInfo session) {
		return session.getCampus();
	}

	@Override
	public String getExternalSubject(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return subjectArea;
	}

	@Override
	public String getExternalCourseNumber(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return courseNumber.length() > 5 ? courseNumber.substring(0, 5) : courseNumber;
	}
}
