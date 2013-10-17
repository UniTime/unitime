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

import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;


/**
 * Compares ManagerRole or Roles objects and orders by role reference
 * 
 * @author Heston Fernandes
 */
public class RolesComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        
        if (o1==null || o2==null)
            return 0;
        
        // Check if ManagerRole object
        if ((o1 instanceof ManagerRole) && (o2 instanceof ManagerRole) ) {
	        ManagerRole r1 = (ManagerRole) o1;
	        ManagerRole r2 = (ManagerRole) o2;
	        
	        return (r1.getRole().getReference().compareTo(r2.getRole().getReference()));
        }

        // Check if Roles object
        if ((o1 instanceof Roles) && (o2 instanceof Roles) ) {
            Roles r1 = (Roles) o1;
            Roles r2 = (Roles) o2;
	        
	        return (r1.getReference().compareTo(r2.getReference()));
        }

        // All other cases
        return 0;        
    }

}
