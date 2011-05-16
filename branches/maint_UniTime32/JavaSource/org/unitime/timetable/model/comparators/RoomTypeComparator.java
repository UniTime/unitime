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

import org.unitime.timetable.model.Room;


/**
 * Compares rooms based on scheduled room type. 
 * If types are the same it compares based on capacity
 * 
 * @author Heston Fernandes
 */
public class RoomTypeComparator implements Comparator {

	/**
	 * Compares rooms based on scheduled room type, capacity
	 */
	public int compare(Object o1, Object o2) {
        if (! (o1 instanceof Room))
            throw new ClassCastException("o1 must be of type Room");
        if (! (o2 instanceof Room))
            throw new ClassCastException("o2 must be of type Room");
		
        Room r1 = (Room) o1;
        Room r2 = (Room) o2;
        
        String c1 = r1.getRoomTypeLabel();
        String c2 = r2.getRoomTypeLabel();
        
        if (c1.equals(c2)) {
        	Integer s1 = r1.getCapacity();
        	Integer s2 = r2.getCapacity();
        	
        	if (s1!=null && s2!=null)
        		return (s1.compareTo(s2) * -1);
        	else
        		return 0;
        }
        else {
        	return c1.compareTo(c2);
        }
	}
}
