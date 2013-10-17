/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.Comparator;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;


/**
 *  @author Heston Fernandes
 */
public class InstructionalOfferingComparator implements Comparator {
    
    private Long subjectUID;

    public InstructionalOfferingComparator(Long subjectUID) {
        this.subjectUID = subjectUID;
    }

    public int compare(Object o1, Object o2) {
        InstructionalOffering i1 = (InstructionalOffering) o1;
        InstructionalOffering i2 = (InstructionalOffering) o2;

        if (i1.getCourseOfferings()==null || i1.getCourseOfferings().isEmpty())
            throw new IndexOutOfBoundsException("i1 - Instructional Offering must have at least on Course Offering");

        if (i2.getCourseOfferings()==null || i2.getCourseOfferings().isEmpty())
            throw new IndexOutOfBoundsException("i2 - Instructional Offering must have at least on Course Offering");
        
        if (i1.getUniqueId().equals(i2.getUniqueId())) return 0;
        
        CourseOffering co1 = i1.findSortCourseOfferingForSubjectArea(getSubjectUID());
        CourseOffering co2 = i2.findSortCourseOfferingForSubjectArea(getSubjectUID());
        
        int cmp = co1.getSubjectAreaAbbv().compareTo(co2.getSubjectAreaAbbv());
        if (cmp!=0) return cmp;

        cmp = (co1.getCourseNbr().compareTo(co2.getCourseNbr()));
        if (cmp!=0) return cmp;
        
        return co1.getUniqueId().compareTo(co2.getUniqueId());
    }

    /**
     * @return Returns the subjectUID.
     */
    public Long getSubjectUID() {
        return subjectUID;
    }

    /**
     * @param subjectUID
     *            The subjectUID to set.
     */
    public void setSubjectUID(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
}
