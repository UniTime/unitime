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

import org.unitime.timetable.model.DistributionObject;


/**
 * Compares 2 distribution objects based on sequence number
 * 
 * @author Heston Fernandes
 */
public class DistributionObjectsComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
        
        // Check if objects are of class Instructional Offering
        if (!(o1 instanceof DistributionObject)) {
            throw new ClassCastException(
                    "o1 Class must be of type DistributionObject");
        }
        if (!(o2 instanceof DistributionObject)) {
            throw new ClassCastException(
                    "o2 Class must be of type DistributionObject");
        }

        DistributionObject do1 = (DistributionObject) o1;
        DistributionObject do2 = (DistributionObject) o2;

        return (do1.getSequenceNumber().compareTo(do2.getSequenceNumber()));
    }
}
