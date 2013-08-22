/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseRoomTypeOption;



public class RoomTypeOption extends BaseRoomTypeOption {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomTypeOption () {
		super();
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public RoomTypeOption(RoomType roomType, Department department) {
		setRoomType(roomType);
		setDepartment(department);
		initialize();
	}

	public static enum Status {
		NoEventManagement(false, false, false, false),
		AuthenticatedUsersCanRequestEventsManagersCanApprove(true, true, true, true),
		DepartmentalUsersCanRequestEventsManagersCanApprove(false, true, true, true),
		EventManagersCanRequestOrApproveEvents(false, false, true, true),
		AuthenticatedUsersCanRequestEventsNoApproval(true, true, true, false),
		DepartmentalUsersCanRequestEventsNoApproval(false, true, true, false),
		EventManagersCanRequestEventsNoApproval(false, false, true, false),
		;
		
		private boolean iUserRequest, iDeptRequest, iMgrRequest, iMgrApproval;
		
		Status(boolean userRequest, boolean deptRequest, boolean mgrRequest, boolean mgrApproval) {
			iUserRequest = userRequest;
			iDeptRequest = deptRequest;
			iMgrRequest = mgrRequest;
			iMgrApproval = mgrApproval;
		}
		
		public boolean isAuthenticatedUsersCanRequestEvents() { return iUserRequest; }
		public boolean isDepartmentalUsersCanRequestEvents() { return iDeptRequest; }
		public boolean isEventManagersCanApprove() { return iMgrApproval; }
		public boolean isEventManagersCanRequestEvents() { return iMgrRequest; }
		
		@Override
		public String toString() { return name().replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2"); }
	}
	
    public static int getDefaultStatus() {
    	return Status.NoEventManagement.ordinal();
    }
    
    public Status getEventStatus() {
    	return Status.values()[getStatus() == null ? getDefaultStatus() : getStatus()];
    }

    @Deprecated
	public boolean canScheduleEvents() {
		switch (Status.values()[getStatus() == null ? getDefaultStatus() : getStatus()]) {
		case NoEventManagement:
			return false;
		default:
			return true;
		}
	}

}