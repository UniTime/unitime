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
package org.unitime.timetable.server.instructor.survey;

import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyDeleteRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveyDeleteRequest.class)
public class DeleteInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveyDeleteRequest, GwtRpcResponseNull> {

	@Override
	public GwtRpcResponseNull execute(InstructorSurveyDeleteRequest request, SessionContext context) {
		DepartmentalInstructor di = DepartmentalInstructorDAO.getInstance().get(request.getInstructorId());
		context.checkPermission(di.getDepartment(), Right.InstructorSurveyAdmin);
		InstructorSurvey is = InstructorSurvey.getInstructorSurvey(di);
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		hibSession.delete(is);
		hibSession.flush();
		return new GwtRpcResponseNull();
	}

}
