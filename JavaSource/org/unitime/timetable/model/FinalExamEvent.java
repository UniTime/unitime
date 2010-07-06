package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseFinalExamEvent;



public class FinalExamEvent extends BaseFinalExamEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public FinalExamEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public FinalExamEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int getEventType() { return sEventTypeFinalExam; }


}