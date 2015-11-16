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

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public interface RoomAvailabilityInterface {
    public static final String sMidtermExamType = ApplicationProperty.RoomAvailabilityMidtermExamType.value(); 
    public static final String sFinalExamType = ApplicationProperty.RoomAvailabilityFinalExamType.value(); 
    public static final String sClassType = ApplicationProperty.RoomAvailabilityClassType.value();

    public String getTimeStamp(Date startTime, Date endTime, String excludeType);
    public Collection<TimeBlock> getRoomAvailability(Long locationId, Date startTime, Date endTime, String excludeType);
    public Collection<TimeBlock> getInstructorAvailability(Long instructorId, Date startTime, Date endTime, String excludeType);
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