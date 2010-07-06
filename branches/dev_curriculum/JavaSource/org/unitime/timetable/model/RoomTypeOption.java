package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseRoomTypeOption;



public class RoomTypeOption extends BaseRoomTypeOption {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomTypeOption () {
		super();
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public RoomTypeOption(RoomType roomType, Session session) {
		setRoomType(roomType);
		setSession(session);
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