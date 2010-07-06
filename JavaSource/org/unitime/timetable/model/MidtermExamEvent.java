package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseMidtermExamEvent;



public class MidtermExamEvent extends BaseMidtermExamEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MidtermExamEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MidtermExamEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int getEventType() { return sEventTypeMidtermExam; }

}