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

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckPermissions;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingPermissionsInterface;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(CourseOfferingCheckPermissions.class)
public class CourseOfferingCheckPermissionsBackend implements GwtRpcImplementation<CourseOfferingCheckPermissions, CourseOfferingPermissionsInterface> {
	
	@Override
	public CourseOfferingPermissionsInterface execute(CourseOfferingCheckPermissions request, SessionContext context) {
		CourseOfferingPermissionsInterface response = new CourseOfferingPermissionsInterface();

		response.setCanAddCourseOffering(context.hasPermission(request.getSubjectAreaId(), "SubjectArea", Right.AddCourseOffering));
		response.setCanEditCourseOffering(context.hasPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering));
		response.setCanEditCourseOfferingNote(context.hasPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote));
		response.setCanEditCourseOfferingCoordinators(context.hasPermission(request.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators));

		return response;
	}
	
}
