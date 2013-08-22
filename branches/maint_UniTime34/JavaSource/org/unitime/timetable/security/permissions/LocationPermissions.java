/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

public class LocationPermissions {
	
	@PermissionForRight(Right.Rooms)
	public static class Rooms implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source) || source.isExternalManager();
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.RoomsExportPdf)
	public static class RoomsExportPdf extends Rooms {}
	
	@PermissionForRight(Right.RoomsExportCsv)
	public static class RoomsExportCsv extends Rooms {}

	@PermissionForRight(Right.RoomDetail)
	public static class RoomDetail implements Permission<Location> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Location source) {
			if (source.getRoomDepts().isEmpty())
				return user.getCurrentAuthority().hasRight(Right.DepartmentIndependent);
			
			for (RoomDept rd: source.getRoomDepts())
				if (permissionDepartment.check(user, rd.getDepartment()))
					return true;
			
			return false;
		}

		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.RoomEdit)
	public static class RoomEdit implements Permission<Room> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Room source) {
			boolean controls = (source.getRoomDepts().isEmpty() ? true: false);
			boolean allDepts = true;
			
			for (RoomDept rd: source.getRoomDepts()) {
				if (rd.isControl() && permissionDepartment.check(user, rd.getDepartment()))
					controls = true;
				if (!permissionDepartment.check(user, rd.getDepartment()))
					allDepts = false;
			}			
			return controls || allDepts;
		}

		@Override
		public Class<Room> type() { return Room.class; }
	}
	
	@PermissionForRight(Right.NonUniversityLocationEdit)
	public static class NonUniversityLocationEdit implements Permission<NonUniversityLocation> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, NonUniversityLocation source) {
			boolean controls = (source.getRoomDepts().isEmpty() ? true: false);
			boolean allDepts = true;
			
			for (RoomDept rd: source.getRoomDepts()) {
				if (rd.isControl() && permissionDepartment.check(user, rd.getDepartment()))
					controls = true;
				if (!permissionDepartment.check(user, rd.getDepartment()))
					allDepts = false;
			}			
			return controls || allDepts;
		}

		@Override
		public Class<NonUniversityLocation> type() { return NonUniversityLocation.class; }
	}
	
	@PermissionForRight(Right.RoomEditChangeControll)
	public static class RoomEditChangeControll implements Permission<Location> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Location source) {
			boolean controls = (source.getRoomDepts().isEmpty() ? true: false);
			boolean allDepts = true;
			for (RoomDept rd: source.getRoomDepts()) {
				if (rd.isControl() && permissionDepartment.check(user, rd.getDepartment()))
					controls = true;
				if (!permissionDepartment.check(user, rd.getDepartment()))
					allDepts = false;
			}
			
			return controls || allDepts;
		}

		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.RoomEditChangeCapacity)
	public static class RoomEditChangeCapacity implements Permission<Location> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Location source) {
			boolean controls = (source.getRoomDepts().isEmpty() ? true: false);
			boolean allDepts = true;
			
			for (RoomDept rd: source.getRoomDepts()) {
				if (rd.isControl() && permissionDepartment.check(user, rd.getDepartment()))
					controls = true;
				if (!permissionDepartment.check(user, rd.getDepartment()))
					allDepts = false;
			}			
			return controls || allDepts;
		}

		@Override
		public Class<Location> type() { return Location.class; }
	}

	@PermissionForRight(Right.RoomEditChangeExaminationStatus)
	public static class RoomEditChangeExaminationStatus extends RoomEditChangeCapacity {}
	
	@PermissionForRight(Right.RoomEditChangeExternalId)
	public static class RoomEditChangeExternalId extends RoomEditChangeCapacity {}

	@PermissionForRight(Right.RoomEditChangeRoomProperties)
	public static class RoomEditChangeRoomProperties extends RoomEditChangeCapacity {}

	@PermissionForRight(Right.RoomEditChangeType)
	public static class RoomEditChangeType extends RoomEditChangeCapacity {}

	@PermissionForRight(Right.RoomEditChangeEventProperties)
	public static class RoomEditChangeEventProperties extends RoomEditChangeCapacity {}

	@PermissionForRight(Right.EditRoomDepartments)
	public static class EditRoomDepartments implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) ||
					(permissionDepartment.check(user, source) && user.getCurrentAuthority().getQualifiers("Department").size() > 1);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.RoomDepartments)
	public static class RoomDepartments extends EditRoomDepartments {}
	
	@PermissionForRight(Right.EditRoomDepartmentsExams)
	public static class EditRoomDepartmentsExams implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.BuildingList)
	public static class BuildingList implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.BuildingAdd)
	public static class BuildingAdd extends BuildingList {}
	
	@PermissionForRight(Right.BuildingExportPdf)
	public static class BuildingExportPdf extends BuildingList {}

	@PermissionForRight(Right.BuildingUpdateData)
	public static class BuildingUpdateData extends BuildingList {}
 
	@PermissionForRight(Right.BuildingEdit)
	public static class BuildingEdit implements Permission<Building> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Building source) {
			return permissionSession.check(user, source.getSession());
		}

		@Override
		public Class<Building> type() { return Building.class; }
	}
	
	@PermissionForRight(Right.BuildingDelete)
	public static class BuildingDelete implements Permission<Building> {
		@Autowired PermissionSession permissionSession;

		@Autowired Permission<Room> permissionRoomDelete;
		
		@Override
		public boolean check(UserContext user, Building source) {
			if (!permissionSession.check(user, source.getSession())) return false;
			
			for (Room room: RoomDAO.getInstance().findByBuilding(RoomDAO.getInstance().getSession(), source.getUniqueId()))
				if (!permissionRoomDelete.check(user, room))
					return false;
			
			return true;
		}

		@Override
		public Class<Building> type() { return Building.class; }
	}
	
	@PermissionForRight(Right.RoomDelete)
	public static class RoomDelete implements Permission<Room> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Room source) {
			if (source.isUsed()) return false;
			
			if (!user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && !source.getExamTypes().isEmpty())
				return false;
			
			boolean controls = (source.getRoomDepts().isEmpty() ? true: false);
			boolean allDepts = true;
			for (RoomDept rd: source.getRoomDepts()) {
				if (rd.isControl() && permissionDepartment.check(user, rd.getDepartment()))
					controls = true;
				if (!permissionDepartment.check(user, rd.getDepartment()))
					allDepts = false;
			}
			
			if (!controls && !allDepts) return false;
			
			return true;
		}

		@Override
		public Class<Room> type() { return Room.class; }
	}
	
	@PermissionForRight(Right.NonUniversityLocationDelete)
	public static class NonUniversityLocationDelete implements Permission<NonUniversityLocation> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, NonUniversityLocation source) {
			if (source.isUsed()) return false;
			
			if (!user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && !source.getExamTypes().isEmpty())
				return false;
			
			boolean controls = false;
			boolean allDepts = true;
			for (RoomDept rd: source.getRoomDepts()) {
				if (rd.isControl() && permissionDepartment.check(user, rd.getDepartment()))
					controls = true;
				if (!permissionDepartment.check(user, rd.getDepartment()))
					allDepts = false;
			}
			
			if (!controls && !allDepts) return false;
			
			return true;
		}

		@Override
		public Class<NonUniversityLocation> type() { return NonUniversityLocation.class; }
	}
	
	@PermissionForRight(Right.AddSpecialUseRoomExternalRoom)
	public static class AddSpecialUseRoomExternalRoom implements Permission<ExternalRoom> {

		@Override
		public boolean check(UserContext user, ExternalRoom source) {
			if (user == null || user.getCurrentAuthority() == null || source == null) return false;
			
			UserAuthority authority = user.getCurrentAuthority();
			
			if (!authority.hasRight(Right.AddSpecialUseRoom)) return false;
			
			if (authority.hasRight(Right.DepartmentIndependent)) {
				return true;
			} else {
				for (ExternalRoomDepartment dept: source.getRoomDepartments()) {
					if (authority.hasQualifier(new SimpleQualifier("Department", dept.getDepartmentCode())))
						return true;
				}
				
				return false;
			}
		}

		@Override
		public Class<ExternalRoom> type() { return ExternalRoom.class; }
	}
	
	@PermissionForRight(Right.AddSpecialUseRoom)
	public static class AddSpecialUseRoom implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source) && !ExternalRoom.findAll(source.getSessionId()).isEmpty();
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.AddNonUnivLocation)
	public static class AddNonUnivLocation implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.AddRoom)
	public static class AddRoom implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source) && !Building.findAll(source.getSessionId()).isEmpty();
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.TravelTimesLoad)
	public static class TravelTimesLoad extends BuildingList {}

	@PermissionForRight(Right.TravelTimesSave)
	public static class TravelTimesSave extends BuildingList {}
	
	@PermissionForRight(Right.RoomAvailability)
	public static class RoomAvailability implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source) &&
					(Exam.hasFinalExams(source.getUniqueId()) || Exam.hasMidtermExams(source.getUniqueId()));
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.RoomDetailAvailability)
	public static class RoomDetailAvailability extends RoomDetail {}
	
	@PermissionForRight(Right.RoomDetailPeriodPreferences)
	public static class RoomDetailPeriodPreferences extends RoomDetail {}
	
	@PermissionForRight(Right.RoomEditAvailability)
	public static class RoomEditAvailability implements Permission<Location> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Location source) {
			if (source.getRoomDepts().isEmpty())
				return user.getCurrentAuthority().hasRight(Right.DepartmentIndependent);
			
			for (RoomDept rd: source.getRoomDepts())
				if (permissionDepartment.check(user, rd.getDepartment()))
					return true;
			
			return false;
		}

		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.RoomDetailEventAvailability)
	public static class RoomDetailEventAvailability implements Permission<Location> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Location source) {
			return source.getEventDepartment() != null && permissionDepartment.check(user, source.getEventDepartment());
		}

		@Override
		public Class<Location> type() { return Location.class; }
	}

	@PermissionForRight(Right.RoomEditEventAvailability)
	public static class RoomEditEventAvailability extends RoomDetailEventAvailability{}
	
	@PermissionForRight(Right.RoomEditPreference)
	public static class RoomEditPreference extends RoomEditAvailability{}

	@PermissionForRight(Right.RoomEditFeatures)
	public static class RoomEditFeatures extends RoomEditAvailability{}

	@PermissionForRight(Right.RoomEditGroups)
	public static class RoomEditGroups extends RoomEditAvailability{}

	@PermissionForRight(Right.RoomEditGlobalFeatures)
	public static class RoomEditGlobalFeatures extends RoomEditAvailability{}

	@PermissionForRight(Right.RoomEditGlobalGroups)
	public static class RoomEditGlobalGroups extends RoomEditAvailability{}
	
	@PermissionForRight(Right.RoomFeatures)
	public static class RoomFeatures extends Rooms{}

	@PermissionForRight(Right.RoomFeaturesExportPdf)
	public static class RoomFeaturesExportPdf extends RoomsExportPdf{}

	@PermissionForRight(Right.DepartmenalRoomFeatureEdit)
	public static class DepartmenalRoomFeatureEdit implements Permission<DepartmentRoomFeature> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, DepartmentRoomFeature source) {
			return permissionDepartment.check(user, source.getDepartment());
		}

		@Override
		public Class<DepartmentRoomFeature> type() { return DepartmentRoomFeature.class; }
	}

	@PermissionForRight(Right.DepartmenalRoomFeatureDelete)
	public static class DepartmenalRoomFeatureDelete extends DepartmenalRoomFeatureEdit {}

	@PermissionForRight(Right.GlobalRoomFeatureEdit)
	public static class GlobalRoomFeatureEdit implements Permission<GlobalRoomFeature> {
		@Autowired PermissionSession permissionSession;
		
		@Override
		public boolean check(UserContext user, GlobalRoomFeature source) {
			return permissionSession.check(user, source.getSession());
		}

		@Override
		public Class<GlobalRoomFeature> type() { return GlobalRoomFeature.class; }
	}
	
	@PermissionForRight(Right.GlobalRoomFeatureDelete)
	public static class GlobalRoomFeatureDelete extends GlobalRoomFeatureEdit {}

	
	@PermissionForRight(Right.GlobalRoomFeatureAdd)
	public static class GlobalRoomFeatureAdd implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		
		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.DepartmentRoomFeatureAdd)
	public static class DepartmentRoomFeatureAdd implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.RoomGroups)
	public static class RoomGroups extends Rooms{}

	@PermissionForRight(Right.RoomGroupsExportPdf)
	public static class RoomGroupsExportPdf extends RoomsExportPdf{}

	@PermissionForRight(Right.DepartmenalRoomGroupEdit)
	public static class DepartmenalRoomGroupEdit implements Permission<RoomGroup> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, RoomGroup source) {
			return !source.isGlobal() && permissionDepartment.check(user, source.getDepartment());
		}

		@Override
		public Class<RoomGroup> type() { return RoomGroup.class; }
	}

	@PermissionForRight(Right.DepartmenalRoomGroupDelete)
	public static class DepartmenalRoomGroupDelete extends DepartmenalRoomGroupEdit {}

	@PermissionForRight(Right.GlobalRoomGroupEdit)
	public static class GlobalRoomGroupEdit implements Permission<RoomGroup> {
		@Autowired PermissionSession permissionSession;
		
		@Override
		public boolean check(UserContext user, RoomGroup source) {
			return source.isGlobal() && permissionSession.check(user, source.getSession());
		}

		@Override
		public Class<RoomGroup> type() { return RoomGroup.class; }
	}
	
	@PermissionForRight(Right.GlobalRoomGroupEditSetDefault)
	public static class GlobalRoomGroupEditSetDefault extends GlobalRoomGroupEdit {}
	
	@PermissionForRight(Right.GlobalRoomGroupDelete)
	public static class GlobalRoomGroupDelete extends GlobalRoomGroupEdit {}

	
	@PermissionForRight(Right.GlobalRoomGroupAdd)
	public static class GlobalRoomGroupAdd implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		
		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.DepartmentRoomGroupAdd)
	public static class DepartmentRoomGroupAdd implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
}
