package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseCurriculaCourse;



public class CurriculaCourse extends BaseCurriculaCourse implements Comparable<CurriculaCourse> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculaCourse () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculaCourse (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CurriculaCourse (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CurriculaClassification classification,
		org.unitime.timetable.model.CourseOffering course,
		java.lang.Float percShare,
		java.lang.Integer ord) {

		super (
			uniqueId,
			classification,
			course,
			percShare,
			ord);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculaCourse c) {
	    if (getOrd()!=null && c.getOrd()!=null && !getOrd().equals(c.getOrd())) return getOrd().compareTo(c.getOrd());
	    if (getCourse().equals(c.getCourse())) return getClassification().compareTo(c.getClassification());
	    for (CurriculaClassification cc1: new TreeSet<CurriculaClassification>(getClassification().getCurricula().getClassifications())) {
	        CurriculaClassification cc2 = null;
	        for (Iterator i=c.getClassification().getCurricula().getClassifications().iterator();i.hasNext();) {
	            CurriculaClassification cc = (CurriculaClassification)i.next();
	            if (cc1.getAcademicClassification()!=null && cc1.getAcademicClassification().equals(cc.getAcademicClassification())) {
	                cc2 = cc; break;
	            }
	            if (cc1.getAcademicClassification()==null && cc1.getName().equals(cc.getName())) {
	                cc2 = cc; break;
	            }
	        }
	        float s1 = 0f, s2 = 0f;
	        for (Iterator i=cc1.getCourses().iterator();i.hasNext();) {
	            CurriculaCourse x = (CurriculaCourse)i.next();
	            if (x.getCourse().equals(getCourse())) { s1 = x.getPercShare(); break; }
	        }
	        if (cc2!=null)
	            for (Iterator i=cc2.getCourses().iterator();i.hasNext();) {
	                CurriculaCourse x = (CurriculaCourse)i.next();
	                if (x.getCourse().equals(c.getCourse())) { s2 = x.getPercShare(); break; }
	            }
	        int cmp = -Double.compare(s1, s2);
	        if (cmp!=0) return cmp;
	    }
	    int cmp = getCourse().getSubjectArea().compareTo(c.getCourse().getSubjectArea());
	    if (cmp!=0) return cmp;
	    cmp = getCourse().getCourseNbr().compareToIgnoreCase(c.getCourse().getCourseNbr());
	    if (cmp!=0) return cmp;
	    return getCourse().getUniqueId().compareTo(c.getCourse().getUniqueId());
	}
	
}