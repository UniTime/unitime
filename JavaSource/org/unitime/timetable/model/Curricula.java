package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseCurricula;



public class Curricula extends BaseCurricula {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Curricula () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Curricula (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Curricula (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Department department,
		java.lang.String abbv,
		java.lang.String name) {

		super (
			uniqueId,
			department,
			abbv,
			name);
	}

/*[CONSTRUCTOR MARKER END]*/


}