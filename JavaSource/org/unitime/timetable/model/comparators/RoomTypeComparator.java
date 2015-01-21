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

import org.unitime.timetable.model.Room;


/**
 * Compares rooms based on scheduled room type. 
 * If types are the same it compares based on capacity
 * 
 * @author Heston Fernandes, Tomas Muller
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
