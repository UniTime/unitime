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
package org.unitime.timetable.server.courses;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckExists;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckExistsInterface;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;

@GwtRpcImplements(CourseOfferingCheckExists.class)
public class CourseOfferingCheckExistsBackend implements GwtRpcImplementation<CourseOfferingCheckExists, CourseOfferingCheckExistsInterface> {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public CourseOfferingCheckExistsInterface execute(CourseOfferingCheckExists request, SessionContext context) {

		CourseOfferingCheckExistsInterface response = new CourseOfferingCheckExistsInterface();

		Boolean isEdit = request.getIsEdit();
		SubjectArea sa = new SubjectAreaDAO().get(request.getSubjectAreaId());
		CourseOffering co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(sa.getSessionId(), sa.getSubjectAreaAbbreviation(), request.getCourseNumber());
		if (!isEdit && co != null) {
			response.setResponseText(MSG.errorCourseCannotBeCreated());
			
		} else if (isEdit && co!=null && !co.getUniqueId().equals(request.getCourseOfferingId())) {
			response.setResponseText(MSG.errorCourseCannotBeRenamed());
		} else {
			response.setResponseText("");
		}
		
		
		return response;
	}
	
}