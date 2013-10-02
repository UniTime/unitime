/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.util.HashSet;
import java.util.Set;

import net.sf.cpsolver.studentsct.reservation.GroupReservation;

import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.Student;

public class XIndividualReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private Set<Long> iStudentIds = new HashSet<Long>();
	private Integer iLimit = null;
    
    public XIndividualReservation() {
        super();
    }

    public XIndividualReservation(XOffering offering, IndividualReservation reservation) {
        super(XReservationType.Individual, offering, reservation);
        for (Student student: reservation.getStudents())
        	iStudentIds.add(student.getUniqueId());
    }
    
    public XIndividualReservation(net.sf.cpsolver.studentsct.reservation.IndividualReservation reservation) {
        super(XReservationType.Individual, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
    }
    
    public XIndividualReservation(GroupReservation reservation) {
        super(XReservationType.Group, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
        iLimit = (int)Math.round(reservation.getReservationLimit());
    }

    /**
     * Individual reservations are the only reservations that can be assigned over the limit.
     */
    @Override
    public boolean canAssignOverLimit() {
    	switch (getType()) {
    	case Individual:
    		return true;
    	default:
    		return false;
    	}
    }
    
    /**
     * Individual or group reservation must be used (unless it is expired)
     * @return true if not expired, false if expired
     */
    @Override
    public boolean mustBeUsed() {
        return !isExpired();
    }

    /**
     * Individual reservations are of the top priority
     */
    @Override
    public int getPriority() {
    	switch (getType()) {
    	case Individual:
    		return 0;
    	default:
    		return 1;
    	}
    }

    /**
     * Reservation is applicable for all students in the reservation
     */
    @Override
    public boolean isApplicable(XStudent student) {
        return iStudentIds.contains(student.getStudentId());
    }
    
    /**
     * Students in the reservation
     */
    public Set<Long> getStudentIds() {
        return iStudentIds;
    }

    /**
     * Reservation limit == number of students in the reservation
     */
    @Override
    public int getReservationLimit() {
        return (iLimit == null ? iStudentIds.size() : iLimit);
    }
    
    /**
     * Overlaps are allowed for individual reservations. 
     */
    @Override
    public boolean isAllowOverlap() {
    	switch (getType()) {
    	case Individual:
    		return true;
    	default:
    		return false;
    	}
    }
}