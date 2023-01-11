package org.unitime.timetable.server.instructor.survey;

import java.util.Iterator;

import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyApplyRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(InstructorSurveyApplyRequest.class)
public class ApplyInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveyApplyRequest, GwtRpcResponseNull> {

	@Override
	public GwtRpcResponseNull execute(InstructorSurveyApplyRequest request, SessionContext context) {
		DepartmentalInstructor di = DepartmentalInstructorDAO.getInstance().get(request.getInstructorId());
		InstructorSurvey is = InstructorSurvey.getInstructorSurvey(di);
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		context.checkPermission(di, Right.InstructorPreferences);
		for (Iterator<Preference> i = di.getPreferences().iterator(); i.hasNext(); ) {
			Preference p = i.next();
			if (p instanceof TimePref)
				i.remove();
			else if (p instanceof RoomPref)
				i.remove();
			else if (p instanceof RoomFeaturePref)
				i.remove();
			else if (p instanceof RoomGroupPref)
				i.remove();
			else if (p instanceof BuildingPref)
				i.remove();
			else if (p instanceof DistributionPref)
				i.remove();
		}
		for (Preference p: is.getPreferences()) {
			if (p instanceof RoomPref)
				if (!di.getAvailableRooms().contains(((RoomPref)p).getRoom())) continue;
			if (p instanceof RoomFeaturePref)
				if (!di.getAvailableRoomFeatures().contains(((RoomFeaturePref)p).getRoomFeature())) continue;
			if (p instanceof RoomGroupPref)
				if (!di.getAvailableRoomGroups().contains(((RoomGroupPref)p).getRoomGroup())) continue;
			if (p instanceof BuildingPref)
				if (!di.getAvailableBuildings().contains(((BuildingPref)p).getBuilding())) continue;
			
			Preference x = (Preference)p.clone();
			x.setOwner(di);
			di.getPreferences().add(x);
		}
		if (is.getEmail() != null && !is.getEmail().isEmpty())
			di.setEmail(is.getEmail());
		hibSession.update(di);
		hibSession.flush();
		return new GwtRpcResponseNull();
	}

}
