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

	/**
	 * Constructor for required fields
	 */
	public MidtermExamEvent (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int getEventType() { return sEventTypeMidtermExam; }

}