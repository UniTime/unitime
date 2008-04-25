package org.unitime.timetable.util;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;

public class RoomAvailability {
    private static RoomAvailabilityInterface sInstance = null;
    
    public static RoomAvailabilityInterface getInstance() {
        if (sInstance!=null) return sInstance;
        if (ApplicationProperties.getProperty("tmtbl.room.availability.class")==null) return null;
        try {
            sInstance = (RoomAvailabilityInterface)Class.forName(ApplicationProperties.getProperty("tmtbl.room.availability.class")).getConstructor().newInstance();
            return sInstance;
        } catch (Exception e) {
            Debug.error(e);
            return null;
        }
    }
    
}
