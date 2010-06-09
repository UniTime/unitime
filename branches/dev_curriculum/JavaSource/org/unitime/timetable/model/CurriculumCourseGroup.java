package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseCurriculumCourseGroup;



public class CurriculumCourseGroup extends BaseCurriculumCourseGroup implements Comparable<CurriculumCourseGroup> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumCourseGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumCourseGroup (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CurriculumCourseGroup (
		java.lang.Long uniqueId,
		java.lang.String name,
		java.lang.Integer type,
		org.unitime.timetable.model.Curriculum curriculum) {

		super (
			uniqueId,
			name,
			type,
			curriculum);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculumCourseGroup c) {
	    //if (getOrd()!=null && c.getOrd()!=null && !getOrd().equals(c.getOrd())) return getOrd().compareTo(c.getOrd());
	    if (getName().equals(c.getName())) return getName().compareTo(c.getName());
	    return getUniqueId().compareTo(c.getUniqueId());
	}
	
}