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

import java.util.Comparator;

import org.unitime.timetable.model.ClassInstructor;


/**
 *  Compares instructors for a class
 *  COMPARE_BY_LEAD - if both are not lead then compares by percent share.  
 *  COMPARE_BY_PCT_SHARE - if both have the same percent share then compares by name
 *  
 *  @author Heston Fernandes
 */
public class InstructorComparator implements Comparator {

    public final short COMPARE_BY_NAME = 1;
    public final short COMPARE_BY_LEAD = 2;
    public final short COMPARE_BY_PCT_SHARE = 3;
    private short compareBy;
    
    public InstructorComparator() {
        this.compareBy = COMPARE_BY_NAME;
    }
    
    public void setCompareBy(short compareBy) {
        this.compareBy = compareBy;
    }
    
    public static int compareStrings(String s1, String s2) {
    	return (s1==null?"":s1.toUpperCase()).compareTo(s2==null?"":s2.toUpperCase());
    }
    
    public int compare(Object o1, Object o2) {
        
        // Check if objects are of class Instructional Offering
        if (!(o1 instanceof ClassInstructor)) {
            throw new ClassCastException(
                    "o1 Class must be of type ClassInstructor");
        }
        if (!(o2 instanceof ClassInstructor)) {
            throw new ClassCastException(
                    "o2 Class must be of type ClassInstructor");
        }

        ClassInstructor ci1 = (ClassInstructor) o1;
        ClassInstructor ci2 = (ClassInstructor) o2;

        if (ci1.getUniqueId().equals(ci2.getUniqueId())) return 0;
        
       	int cmp = Double.compare(ci1.isLead().booleanValue()?0:1,ci2.isLead().booleanValue()?0:1);
       	if (cmp!=0) return cmp;
        
        if (compareBy==COMPARE_BY_LEAD || compareBy==COMPARE_BY_PCT_SHARE) {
            cmp = ci1.getPercentShare().compareTo(ci1.getPercentShare());
            if (cmp!=0) return cmp;
        }
        
        cmp = compareStrings(ci1.getInstructor().getLastName(),ci2.getInstructor().getLastName());
        if (cmp!=0) return cmp;
        cmp = compareStrings(ci1.getInstructor().getFirstName(),ci2.getInstructor().getFirstName());
        if (cmp!=0) return cmp;
        cmp = compareStrings(ci1.getInstructor().getMiddleName(),ci2.getInstructor().getMiddleName());
        if (cmp!=0) return cmp;
        
        return ci1.getInstructor().getUniqueId().compareTo(ci2.getInstructor().getUniqueId());
    }
}
