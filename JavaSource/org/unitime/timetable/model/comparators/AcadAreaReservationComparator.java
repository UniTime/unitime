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

import org.unitime.timetable.model.AcadAreaReservation;


/**
 * Compares academic area reservations based on academic area short title
 * 
 * @author Heston Fernandes
 */
public class AcadAreaReservationComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        
        if (! (o1 instanceof AcadAreaReservation))
            throw new ClassCastException("o1 must be of type AcadAreaReservation");
        if (! (o2 instanceof AcadAreaReservation))
            throw new ClassCastException("o2 must be of type AcadAreaReservation");
        
        AcadAreaReservation a1 = (AcadAreaReservation) o1;
        AcadAreaReservation a2 = (AcadAreaReservation) o2;
        
        return a1.getAcademicArea().getShortTitle().compareTo(a2.getAcademicArea().getShortTitle());
    }

}
