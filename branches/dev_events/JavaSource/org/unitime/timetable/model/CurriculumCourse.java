/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseCurriculumCourse;



public class CurriculumCourse extends BaseCurriculumCourse implements Comparable<CurriculumCourse> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumCourse () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumCourse (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(CurriculumCourse c) {
	    if (getOrd()!=null && c.getOrd()!=null && !getOrd().equals(c.getOrd())) return getOrd().compareTo(c.getOrd());
	    if (getCourse().equals(c.getCourse())) return getClassification().compareTo(c.getClassification());
	    for (CurriculumClassification cc1: new TreeSet<CurriculumClassification>(getClassification().getCurriculum().getClassifications())) {
	        CurriculumClassification cc2 = null;
	        for (Iterator i=c.getClassification().getCurriculum().getClassifications().iterator();i.hasNext();) {
	            CurriculumClassification cc = (CurriculumClassification)i.next();
	            if (cc1.getAcademicClassification()!=null && cc1.getAcademicClassification().equals(cc.getAcademicClassification())) {
	                cc2 = cc; break;
	            }
	            if (cc1.getAcademicClassification()==null && cc1.getName().equals(cc.getName())) {
	                cc2 = cc; break;
	            }
	        }
	        float s1 = 0f, s2 = 0f;
	        for (Iterator i=cc1.getCourses().iterator();i.hasNext();) {
	            CurriculumCourse x = (CurriculumCourse)i.next();
	            if (x.getCourse().equals(getCourse())) { s1 = x.getPercShare(); break; }
	        }
	        if (cc2!=null)
	            for (Iterator i=cc2.getCourses().iterator();i.hasNext();) {
	                CurriculumCourse x = (CurriculumCourse)i.next();
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
	
	public CurriculumCourse clone(CourseOffering newCourse) {
		CurriculumCourse cc = new CurriculumCourse();
		cc.setPercShare(getPercShare());
		cc.setOrd(getOrd());
		cc.setClassification(getClassification());
		cc.setCourse(newCourse);
		cc.setGroups(new HashSet<CurriculumCourseGroup>(getGroups()));
		return cc;
	}
	
}