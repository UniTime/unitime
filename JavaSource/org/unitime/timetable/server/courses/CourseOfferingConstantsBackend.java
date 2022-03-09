package org.unitime.timetable.server.courses;

import java.util.logging.Logger;

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingConstantsInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingConstantsRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;

/**
 * @author Alec Macleod
 */
@GwtRpcImplements(CourseOfferingConstantsRequest.class)
public class CourseOfferingConstantsBackend implements GwtRpcImplementation<CourseOfferingConstantsRequest, CourseOfferingConstantsInterface> {

	Logger logger = java.util.logging.Logger.getLogger("CourseOfferingConstantsBackend");

	@Override
	public CourseOfferingConstantsInterface execute(CourseOfferingConstantsRequest request, SessionContext context) {
		
		CourseOfferingConstantsInterface response = new CourseOfferingConstantsInterface();
		
		response.setPrefRowsAdded(Constants.PREF_ROWS_ADDED);

		return response;
	}
}
