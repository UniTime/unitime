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
package org.unitime.timetable.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public interface RoomAvailabilityInterface {
    public static final String sMidtermExamType = ApplicationProperty.RoomAvailabilityMidtermExamType.value(); 
    public static final String sFinalExamType = ApplicationProperty.RoomAvailabilityFinalExamType.value(); 
    public static final String sClassType = ApplicationProperty.RoomAvailabilityClassType.value();

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
