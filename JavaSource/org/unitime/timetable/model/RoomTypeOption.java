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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseRoomTypeOption;



/**
 * @author Tomas Muller
 */
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
		AuthenticatedUsersCanRequestAutomaticApproval(true, true, true, true, true),
		DepartmentalUsersCanRequestAutomaticApproval(false, true, true, true, true),
		EventManagersCanRequestAutomaticApproval(false, false, true, true, true),
		;
		
		private boolean iUserRequest, iDeptRequest, iMgrRequest, iMgrApproval, iAutoApproval;
		
		Status(boolean userRequest, boolean deptRequest, boolean mgrRequest, boolean mgrApproval, boolean autoApproval) {
			iUserRequest = userRequest;
			iDeptRequest = deptRequest;
			iMgrRequest = mgrRequest;
			iMgrApproval = mgrApproval;
			iAutoApproval = autoApproval;
		}
		Status(boolean userRequest, boolean deptRequest, boolean mgrRequest, boolean mgrApproval) {
			this(userRequest, deptRequest, mgrRequest, mgrApproval, false);
		}
		
		public boolean isAuthenticatedUsersCanRequestEvents() { return iUserRequest; }
		public boolean isDepartmentalUsersCanRequestEvents() { return iDeptRequest; }
		public boolean isEventManagersCanApprove() { return iMgrApproval; }
		public boolean isEventManagersCanRequestEvents() { return iMgrRequest; }
		public boolean isAutomaticApproval() { return iAutoApproval; }
		
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