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

import java.util.Comparator;

import org.unitime.timetable.model.CourseOffering;


/**
 * Compares course offerings based on Subject Area and Course Number
 * 
 * @author Heston Fernandes, Stephanie Schluttenhofer
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
