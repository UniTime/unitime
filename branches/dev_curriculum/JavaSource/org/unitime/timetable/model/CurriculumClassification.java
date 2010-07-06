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

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculumClassification cc) {
	    if (getOrd()!=null && cc.getOrd()!=null && !getOrd().equals(cc.getOrd()))
	        return getOrd().compareTo(cc.getOrd());
	    int cmp = getName().compareToIgnoreCase(cc.getName());
	    if (cmp!=0) return cmp;
	    return getUniqueId().compareTo(cc.getUniqueId());
	}
}