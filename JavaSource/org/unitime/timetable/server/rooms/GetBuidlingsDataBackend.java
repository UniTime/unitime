package org.unitime.timetable.server.rooms;

import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingsDataResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.GetBuildingsRequest;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(GetBuildingsRequest.class)
public class GetBuidlingsDataBackend implements GwtRpcImplementation<GetBuildingsRequest, BuildingsDataResponse>{

	@Override
	public BuildingsDataResponse execute(GetBuildingsRequest request, SessionContext context) {
		context.checkPermission(Right.BuildingList);
		
		BuildingsDataResponse response = new BuildingsDataResponse();
		response.setCanAdd(context.hasPermission(Right.BuildingAdd));
		response.setCanExportPDF(context.hasPermission(Right.BuildingExportPdf));
		response.setCanUpdateData(context.hasPermission(Right.BuildingUpdateData));
		
		DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value());
		response.setEllipsoid(ellipsoid.getEclipsoindName());
		
		for (Building building: Building.findAll(context.getUser().getCurrentAcademicSessionId())) {
			BuildingInterface b = new BuildingInterface();
			b.setId(building.getUniqueId());
			b.setAbbreviation(building.getAbbreviation());
			b.setName(building.getName());
			b.setX(building.getCoordinateX());
			b.setY(building.getCoordinateY());
			b.setExternalId(building.getExternalUniqueId());
			b.setCanDelete(context.hasPermission(building, Right.BuildingDelete));
			b.setCanEdit(context.hasPermission(building, Right.BuildingEdit));
			response.addBuilding(b);
		}
		
		return response;
	}

}
