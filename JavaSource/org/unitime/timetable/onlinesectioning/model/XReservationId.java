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
import org.cpsolver.studentsct.reservation.LearningCommunityReservation;
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

	protected XReservationType iType;
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
			iType = XReservationType.IndividualOverride;
		else if (reservation instanceof LearningCommunityReservation)
			iType = XReservationType.LearningCommunity;
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
	
	public boolean isOverride() { return false; }

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
