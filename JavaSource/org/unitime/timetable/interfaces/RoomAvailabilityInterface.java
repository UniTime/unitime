/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public interface RoomAvailabilityInterface {
    public static final String sMidtermExamType = 
        ApplicationProperties.getProperty("tmtbl.room.availability.eventType.midtermExam",Event.sEventTypes[Event.sEventTypeMidtermExam]);
    public static final String sFinalExamType = 
        ApplicationProperties.getProperty("tmtbl.room.availability.eventType.finalExam",Event.sEventTypes[Event.sEventTypeFinalExam]);
    public static final String sClassType = 
        ApplicationProperties.getProperty("tmtbl.room.availability.eventType.class",Event.sEventTypes[Event.sEventTypeClass]);

    public String getTimeStamp(Date startTime, Date endTime, String excludeType);
    public Collection<TimeBlock> getRoomAvailability(Location location, Date startTime, Date endTime, String excludeType);
    public Collection<TimeBlock> getInstructorAvailability(DepartmentalInstructor instructor, Date startTime, Date endTime, String excludeType);
    public void activate(Session session, Date startTime, Date endTime, String excludeType, boolean waitForSync);
    
    public void startService();
    public void stopService();
    
    public interface TimeBlock extends Serializable {
        public String getEventName();
        public String getEventType();
        public Date getStartTime();
        public Date getEndTime();
    }
}
