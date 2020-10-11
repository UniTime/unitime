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
package org.unitime.timetable.server.teachingschedule;

import java.util.Collections;

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetSubjectAreas;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.SubjectArea;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;

@GwtRpcImplements(TeachingScheduleAPI.GetSubjectAreas.class)
public class TeachingScheduleGetSubjects implements GwtRpcImplementation<GetSubjectAreas, GwtRpcResponseList<SubjectArea>> {

	@Override
	public GwtRpcResponseList<SubjectArea> execute(GetSubjectAreas request, SessionContext context) {
		GwtRpcResponseList<SubjectArea> ret = new GwtRpcResponseList<SubjectArea>();
		for (org.unitime.timetable.model.SubjectArea sa: org.unitime.timetable.model.SubjectArea.getUserSubjectAreas(context.getUser())) {
			SubjectArea subject = new SubjectArea();
			subject.setId(sa.getUniqueId());
			subject.setLabel(sa.getTitle());
			subject.setReference(sa.getSubjectAreaAbbreviation());
			ret.add(subject);
		}
		Collections.sort(ret);
		return ret;
	}
}
