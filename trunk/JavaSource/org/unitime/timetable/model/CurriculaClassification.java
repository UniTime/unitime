package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseCurriculaClassification;



public class CurriculaClassification extends BaseCurriculaClassification implements Comparable<CurriculaClassification> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculaClassification () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculaClassification (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CurriculaClassification (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Curricula curricula,
		java.lang.String name,
		java.lang.Integer nrStudents,
		java.lang.Integer ord) {

		super (
			uniqueId,
			curricula,
			name,
			nrStudents,
			ord);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculaClassification cc) {
	    if (getOrd()!=null && cc.getOrd()!=null && !getOrd().equals(cc.getOrd()))
	        return getOrd().compareTo(cc.getOrd());
	    int cmp = getName().compareToIgnoreCase(cc.getName());
	    if (cmp!=0) return cmp;
	    return getUniqueId().compareTo(cc.getUniqueId());
	}
}