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
package org.unitime.timetable.model.comparators;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.SchedulingSubpart;


/**
 * Compare Classes based on specified parameter
 * Choices are - ID (default), Label and Itype
 * 
 * @author Heston Fernandes, Tomas Muller
 */
public class ClassComparator implements Comparator<Class_> {
	private Long subjectUID = null;
	
    /** Compare 2 classes on UniqueId - Default **/
    public static final short COMPARE_BY_ID = 1;
    
    /** Compare 2 classes on Class Label **/
    public static final short COMPARE_BY_LABEL = 2;
    
    /** Compare 2 classes on Subpart / Itype **/
    public static final short COMPARE_BY_ITYPE = 3;
    
    /** Compare 2 classes on SubjArea, CourseNumber, Itype, Section **/
    public static final short COMPARE_BY_SUBJ_NBR_ITYP_SEC = 4;
    
    public static final short COMPARE_BY_HIERARCHY = 5;
    
    // Decides method to compare 
    private short compareBy;

    public ClassComparator (Long subjectUID, short compareBy) {
		this.subjectUID = subjectUID;
		this.compareBy = compareBy;
	}
    
    public ClassComparator (short compareBy) {
    	this(null, compareBy);
    }
    
    public static int compare(Comparable c1, Comparable c2) {
    	return (c1==null?(c2==null?0:-1):(c2==null?1:c1.compareTo(c2)));
    }
    
    public static int compareInstructors(List<DepartmentalInstructor> i1, List<DepartmentalInstructor> i2) {
    	if (i1.isEmpty() || i2.isEmpty())
    		return Double.compare(i1.size(),i2.size());
    	if (i1.size()>1) Collections.sort(i1);
    	if (i2.size()>1) Collections.sort(i2);
    	for (int i=0;i<Math.min(i1.size(),i2.size());i++) {
    		int cmp = compare(i1.get(i),i2.get(i));
    		if (cmp!=0) return cmp;
    	}
    	return Double.compare(i1.size(),i2.size());
    }
    
    public boolean isParentSameIType(SchedulingSubpart s1, SchedulingSubpart s2) {
		SchedulingSubpart p1 = s1.getParentSubpart();
		if (p1==null) return false;
		if (p1.equals(s2)) return true;
		if (!p1.getItype().equals(s2.getItype())) return false;
		return isParentSameIType(p1, s2);
	}

    public int compare(Class_ c1, Class_ c2) {
        int cmp = 0;
        switch (compareBy) {
        	case COMPARE_BY_LABEL :
        		cmp = c1.getSchedulingSubpart().getSchedulingSubpartLabel().compareTo(c1.getSchedulingSubpart().getSchedulingSubpartLabel());
        		if (cmp!=0) return cmp;
        		cmp = c1.getClassLabel().compareTo(c2.getClassLabel());
        		if (cmp!=0) return cmp;
        	case COMPARE_BY_ITYPE :
        		cmp = c1.getSchedulingSubpart().getItype().getItype().compareTo(
        				c2.getSchedulingSubpart().getItype().getItype());
        		if (cmp!=0) return cmp;
        	case COMPARE_BY_HIERARCHY :
        		if (!c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart())) {
        			Comparator comparator = new SchedulingSubpartComparator(subjectUID);
        			return comparator.compare(c1.getSchedulingSubpart(), c2.getSchedulingSubpart());
        		}
        		cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
        		if (cmp!=0) return cmp;
        	case COMPARE_BY_SUBJ_NBR_ITYP_SEC :
        		cmp = c1.getSchedulingSubpart().getControllingCourseOffering().getCourseName().compareTo(
        				c2.getSchedulingSubpart().getControllingCourseOffering().getCourseName());
        		if (cmp!=0) return cmp;
        		cmp = c1.getSchedulingSubpart().getItype().getItype().compareTo(c2.getSchedulingSubpart().getItype().getItype());
        		if (cmp!=0) return cmp;
        		cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
        		if (cmp!=0) return cmp;
        		cmp = c1.getSchedulingSubpart().getSchedulingSubpartSuffix().compareTo(
        				c2.getSchedulingSubpart().getSchedulingSubpartSuffix());
        		if (cmp!=0) return cmp;
        	case COMPARE_BY_ID :
        	default :
        		return c1.getUniqueId().compareTo(c2.getUniqueId());
        }
    }
    
    public Long getSubjectUID() {
        return subjectUID;
    }

    public void setSubjectUID(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
}
