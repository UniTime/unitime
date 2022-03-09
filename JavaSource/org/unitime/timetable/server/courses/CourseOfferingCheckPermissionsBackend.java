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
