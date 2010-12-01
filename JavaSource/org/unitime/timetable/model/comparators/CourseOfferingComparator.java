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

import org.unitime.timetable.model.CourseOffering;


/**
 * Compares course offerings based on Subject Area and Course Number
 * 
 * @author Heston Fernandes
 */
public class CourseOfferingComparator implements Comparator {
    
    /** Compare 2 offerings on Subject Area and Course Number - Default **/
    public static final short COMPARE_BY_SUBJ_CRS = 1;
    
    /** Compare 2 offerings on Controlling Course Flag **/
    public static final short COMPARE_BY_CTRL_CRS = 2;
    
    // Decides method to compare 
    private short compareBy;
    
    public CourseOfferingComparator () {
		super();
		this.compareBy = COMPARE_BY_SUBJ_CRS;
	}
    
    public CourseOfferingComparator (short compareBy) {
		super();
		this.compareBy = compareBy;
	}
    
    public int compare (Object o1, Object o2){
        // Check if objects are of class Instructional Offering
        if (! (o1 instanceof CourseOffering)){
            throw new ClassCastException("o1 Class must be of type CourseOffering");
        }
        if (! (o2 instanceof CourseOffering)){
            throw new ClassCastException("o2 Class must be of type CourseOffering");
        }
        
        CourseOffering co1 = (CourseOffering) o1;
        CourseOffering co2 = (CourseOffering) o2;

        // Same Course Offering 
        if (co1.getUniqueId().equals(co2.getUniqueId())){
            return 0;
        }
        
        // One of the offerings is a Controlling Course
        if(compareBy==COMPARE_BY_CTRL_CRS) {
            if(co1.isIsControl().booleanValue())
                return -1;
            if(co2.isIsControl().booleanValue())
                return 1;
        }
        
        // Compare by course name (also used if neither is controlling)
        if (co1.getSubjectAreaAbbv().equals(co2.getSubjectAreaAbbv())){
        	if (co1.getCourseNbr().equals(co2.getCourseNbr())){
        		if (co1.getTitle() == null && co2.getTitle() == null){
        			return(0);
        		} else if (co1.getTitle() == null){
        			return(-1);
        		} else if (co2.getTitle() == null){
        			return(1);
        		}
        		return(co1.getTitle().compareTo(co2.getTitle()));
        	} else {
        		return(co1.getCourseNbr().compareTo(co2.getCourseNbr()));
        	}
        } else {
            return(co1.getSubjectAreaAbbv().compareTo(co2.getSubjectAreaAbbv()));
        }                  
    } 
}
