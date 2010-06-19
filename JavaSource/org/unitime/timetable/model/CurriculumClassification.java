package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseCurriculumClassification;



public class CurriculumClassification extends BaseCurriculumClassification implements Comparable<CurriculumClassification> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumClassification () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumClassification (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CurriculumClassification (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Curriculum curriculum,
		org.unitime.timetable.model.AcademicClassification academicClassification,
		java.lang.String name,
		java.lang.Integer nrStudents,
		java.lang.Integer ord) {

		super (
			uniqueId,
			curriculum,
			academicClassification,
			name,
			nrStudents,
			ord);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculumClassification cc) {
	    if (getOrd()!=null && cc.getOrd()!=null && !getOrd().equals(cc.getOrd()))
	        return getOrd().compareTo(cc.getOrd());
	    int cmp = getName().compareToIgnoreCase(cc.getName());
	    if (cmp!=0) return cmp;
	    return getUniqueId().compareTo(cc.getUniqueId());
	}
}