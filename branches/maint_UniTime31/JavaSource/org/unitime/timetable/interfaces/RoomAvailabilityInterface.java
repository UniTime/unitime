package org.unitime.timetable.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;

public interface RoomAvailabilityInterface {
    public static final String sMidtermExamType = 
        ApplicationProperties.getProperty("tmtbl.room.availability.eventType.midtermExam",Event.sEventTypes[Event.sEventTypeMidtermExam]);
    public static final String sFinalExamType = 
        ApplicationProperties.getProperty("tmtbl.room.availability.eventType.finalExam",Event.sEventTypes[Event.sEventTypeFinalExam]);
    public static final String sClassType = 
        ApplicationProperties.getProperty("tmtbl.room.availability.eventType.class",Event.sEventTypes[Event.sEventTypeClass]);

    public String getTimeStamp(Date startTime, Date endTime, String excludeType);
    public Collection<TimeBlock> getRoomAvailability(Location location, Date startTime, Date endTime, String excludeType);
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
