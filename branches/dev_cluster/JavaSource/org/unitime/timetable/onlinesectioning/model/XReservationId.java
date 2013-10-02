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

import java.io.Serializable;

import org.unitime.timetable.onlinesectioning.model.XOffering.SimpleReservation;

import net.sf.cpsolver.studentsct.reservation.CourseReservation;
import net.sf.cpsolver.studentsct.reservation.CurriculumReservation;
import net.sf.cpsolver.studentsct.reservation.GroupReservation;
import net.sf.cpsolver.studentsct.reservation.IndividualReservation;
import net.sf.cpsolver.studentsct.reservation.Reservation;

public class XReservationId implements Serializable {
	private static final long serialVersionUID = 1L;

	private XReservationType iType;
	private Long iOfferingId;
	private Long iReservationId;
	
	public XReservationId() {}
	
	public XReservationId(XReservationType type, Long offeringId, Long reservationId) {
		iType = type; iOfferingId = offeringId; iReservationId = reservationId;
	}
	
	public XReservationId(Reservation reservation) {
		iOfferingId = reservation.getOffering().getId();
		iReservationId = reservation.getId();
		if (reservation instanceof SimpleReservation)
			iType = ((SimpleReservation)reservation).getType();
		else if (reservation instanceof IndividualReservation)
			iType = XReservationType.Individual;
		else if (reservation instanceof GroupReservation)
			iType = XReservationType.Group;
		else if (reservation instanceof CurriculumReservation)
			iType = XReservationType.Curriculum;
		else if (reservation instanceof CourseReservation)
			iType = XReservationType.Course;
		else
			iType = XReservationType.Dummy;
	}
	
	public XReservationType getType() { return iType; }
	
	public Long getOfferingId() { return iOfferingId; }
	
	public Long getReservationId() { return iReservationId; }

    @Override
    public int hashCode() {
        return (int)(getReservationId() ^ (getReservationId() >>> 32));
    }
        
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XReservationId)) return false;
        return getReservationId() == ((XReservationId)o).getReservationId() && getOfferingId().equals(((XReservationId)o).getOfferingId());
    }
}
