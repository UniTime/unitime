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

	public static final int sStatusNoOptions = 0;
	public static final int sStatusScheduleEvents = 1;
	
    public boolean can(int operation) {
        return (getStatus().intValue() & operation) == operation;
    }
    
    public void set(int operation) {
        if (!can(operation)) setStatus(getStatus()+operation);
    }
	
    public void reset(int operation) {
        if (can(operation)) setStatus(getStatus()-operation);
    }

    public boolean canScheduleEvents() {
	    return can(sStatusScheduleEvents);
	}
	
	public void setScheduleEvents(boolean enable) {
	    if (enable) set(sStatusScheduleEvents); else reset(sStatusScheduleEvents);
	}

}