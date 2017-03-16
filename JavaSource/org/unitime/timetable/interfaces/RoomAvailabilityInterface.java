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
import java.util.Comparator;
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
    	public Long getEventId();
        public String getEventName();
        public String getEventType();
        public Date getStartTime();
        public Date getEndTime();
    }
    
    public static class TimeBlockComparator implements Comparator<TimeBlock> {
    	public int compare(TimeBlock t1, TimeBlock t2) {
    		int cmp = t1.getEventName().compareToIgnoreCase(t2.getEventName());
    		if (cmp != 0) return cmp;
    		cmp = t1.getEventType().compareToIgnoreCase(t2.getEventType());
    		if (cmp != 0) return cmp;
    		cmp = t1.getStartTime().compareTo(t2.getStartTime());
    		if (cmp != 0) return cmp;
    		return t1.getEndTime().compareTo(t2.getEndTime());
    	}
    }
}