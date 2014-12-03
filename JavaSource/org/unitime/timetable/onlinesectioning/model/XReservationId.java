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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;


/**
 * @author Tomas Muller
 */
@SerializeWith(XReservationId.XReservationIdSerializer.class)
public class XReservationId implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;

	private XReservationType iType;
	private Long iOfferingId;
	private Long iReservationId;
	
	public XReservationId() {}
	
	public XReservationId(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XReservationId(XReservationType type, Long offeringId, Long reservationId) {
		iType = type; iOfferingId = offeringId; iReservationId = reservationId;
	}
	
	public XReservationId(XReservationId reservation) {
		iType = reservation.getType();
		iOfferingId = reservation.getOfferingId();
		iReservationId = reservation.getReservationId();
	}
	
	public XReservationId(Reservation reservation) {
		iOfferingId = reservation.getOffering().getId();
		iReservationId = reservation.getId();
		if (reservation instanceof OnlineReservation)
			iType = XReservationType.values()[((OnlineReservation)reservation).getType()];
		else if (reservation instanceof ReservationOverride)
			iType = XReservationType.Override;
		else if (reservation instanceof GroupReservation)
			iType = XReservationType.Group;
		else if (reservation instanceof IndividualReservation)
			iType = XReservationType.Individual;
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

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iType = XReservationType.values()[in.readInt()];
		iOfferingId = in.readLong();
		iReservationId = in.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(iType.ordinal());
		out.writeLong(iOfferingId);
		out.writeLong(iReservationId);
	}
	
	public static class XReservationIdSerializer implements Externalizer<XReservationId> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XReservationId object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XReservationId readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XReservationId(input);
		}
	}
}
