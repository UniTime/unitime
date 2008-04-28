package org.unitime.timetable.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;

public interface RoomAvailabilityInterface {
    public static final String sEveningExamType = ApplicationProperties.getProperty("tmtbl.room.availability.eventType.eveningExam","Evening Exam");
    public static final String sFinalExamType = ApplicationProperties.getProperty("tmtbl.room.availability.eventType.finalExam","Final Exam");
    public static final String sClassType = ApplicationProperties.getProperty("tmtbl.room.availability.eventType.class","Course Activity");

    public Collection<TimeBlock> getRoomAvailability(String roomExternalId, String buildingAbbv, String roomNbr, Date startTime, Date endTime, String[] excludeTypes);
    public void activate(Session session, Date startTime, Date endTime);
    
    public void startService();
    public void stopService();
    
    public interface TimeBlock extends Serializable {
        public String getEventName();
        public String getEventType();
        public Date getStartTime();
        public Date getEndTime();
    }
}
