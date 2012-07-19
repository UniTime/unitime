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
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

public class LocationPermissions {
	
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
			
			if (!user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && source.getExamType() != null && source.getExamType() != 0)
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
		public Class<Room> type() { return Room.class; }
	}
	
	@PermissionForRight(Right.NonUniversityLocationDelete)
	public static class NonUniversityLocationDelete implements Permission<NonUniversityLocation> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, NonUniversityLocation source) {
			if (source.isUsed()) return false;
			
			if (!user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && source.getExamType() != null && source.getExamType() != 0)
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
	
	@PermissionForRight(Right.AddSpecialUseRoom)
	public static class AddSpecialUseRoom implements Permission<ExternalRoom> {

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
	
	@PermissionForRight(Right.AddNonUnivLocation)
	public static class AddNonUnivLocation implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
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
}
