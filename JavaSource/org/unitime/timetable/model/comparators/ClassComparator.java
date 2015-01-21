/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
