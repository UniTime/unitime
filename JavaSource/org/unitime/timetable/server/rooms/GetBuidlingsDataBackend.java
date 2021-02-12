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
			b.setCanEdit(context.hasPermission(building, Right.BuildingEdit));
			response.addBuilding(b);
		}
		
		return response;
	}

}
