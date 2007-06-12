/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.Department;


/**
 *  @author Heston Fernandes
 */
public class DepartmentComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        
        // Check if objects are of Department
        if (!(o1 instanceof Department)) {
            throw new ClassCastException(
                    "o1 Class must be of type Department");
        }
        if (!(o2 instanceof Department)) {
            throw new ClassCastException(
                    "o2 Class must be of type Department");
        }

        Department d1 = (Department) o1;
        Department d2 = (Department) o2;

        if (d1.getUniqueId() != null && d2.getUniqueId() != null) {
	        if (d1.getUniqueId().equals(d2.getUniqueId())) {
	            return 0;
	        }else {
	        	if (d1.getDeptCode().equals(d2.getDeptCode())){
        			return(d1.getSessionId().compareTo(d2.getSessionId()));
        		} else {
        			return (d1.getDeptCode().compareTo(d2.getDeptCode()));
        		}
	        }
        } else {
        	if (d1.getDeptCode() != null && d2.getDeptCode() != null){
        		if (d1.getDeptCode().equals(d2.getDeptCode())){
        			if (d1.getSessionId() != null && d2.getSessionId() != null){
        				return(d1.getSessionId().compareTo(d2.getSessionId()));
        			} else {
        				return(-1);
        			}
        		} else {
        			return (d1.getDeptCode().compareTo(d2.getDeptCode()));
        		}
        	} else {
    			return(-1);
    		}
        }
    }
}
