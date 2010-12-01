/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.StudentGroupReservation;


/**
 * Compares academic area reservations based on student group name
 * 
 * @author Heston Fernandes
 */
public class StudentGroupReservationComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
        if (! (o1 instanceof StudentGroupReservation))
            throw new ClassCastException("o1 must be of type StudentGroupReservation");
        if (! (o2 instanceof StudentGroupReservation))
            throw new ClassCastException("o2 must be of type StudentGroupReservation");
        
        StudentGroupReservation a1 = (StudentGroupReservation) o1;
        StudentGroupReservation a2 = (StudentGroupReservation) o2;
        
        return a1.getStudentGroup().getGroupName().compareTo(a2.getStudentGroup().getGroupName());
    }

}
