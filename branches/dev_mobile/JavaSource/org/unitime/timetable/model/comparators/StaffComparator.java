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

import org.unitime.timetable.model.Staff;


/**
 * Compares Staff based on specified criteria
 * Defaults to compare by name
 * 
 * @author Heston Fernandes, Tomas Muller
 */
public class StaffComparator implements Comparator {

    public static final short COMPARE_BY_NAME = 1;
    public static final short COMPARE_BY_POSITION = 2;
    
    private short compareBy;
    
    public StaffComparator() {
        compareBy = COMPARE_BY_NAME;
    }
    
    public StaffComparator(short compareBy) {
        if (compareBy!=COMPARE_BY_NAME 
                && compareBy!=COMPARE_BY_POSITION) {
            this.compareBy = COMPARE_BY_NAME;
        }
        else {
            this.compareBy = compareBy;
        }
    }

    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Staff) || o1==null)
            throw new ClassCastException("o1 Class must be of type Staff and cannot be null");
        if (!(o2 instanceof Staff) || o2==null)
            throw new ClassCastException("o2 Class must be of type Staff and cannot be null");
        
        Staff s1 = (Staff) o1;
        Staff s2 = (Staff) o2;
        
        if (compareBy==COMPARE_BY_POSITION) {
            Integer l1 = new Integer(-1);
            if (s1.getPositionType()!=null)
                l1 = s1.getPositionType().getSortOrder();
            
            Integer l2 = new Integer(-1);
            if (s2.getPositionType()!=null)
                l2 = s2.getPositionType().getSortOrder();
            
            int ret = l1.compareTo(l2);
            if (ret!=0)
                return ret;
        }
        
        return  s1.nameLastNameFirst().compareToIgnoreCase(s2.nameLastNameFirst());
    }

}
