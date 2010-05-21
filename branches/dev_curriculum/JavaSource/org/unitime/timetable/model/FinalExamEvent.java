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

	/**
	 * Constructor for required fields
	 */
	public FinalExamEvent (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int getEventType() { return sEventTypeFinalExam; }


}