package org.unitime.timetable.server.courses;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckExists;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CourseOfferingCheckExistsInterface;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

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