package org.unitime.timetable.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public interface RoomAvailabilityInterface {
    public static final String sExamType = "exam";
    public static final String sClassType = "class";

    public Collection<TimeBlock> getRoomAvailability(String roomExternalId, String buildingAbbv, String roomNbr, Date startTime, Date endTime, String[] excludeTypes);
    public void activate(Date startTime, Date endTime);
    
    public void startService();
    public void stopService();
    
    public interface TimeBlock extends Serializable {
        public String getEventName();
        public String getEventType();
        public Date getStartTime();
        public Date getEndTime();
    }
}
