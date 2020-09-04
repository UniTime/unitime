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

import org.cpsolver.studentsct.reservation.CourseRestriction;
import org.cpsolver.studentsct.reservation.CurriculumRestriction;
import org.cpsolver.studentsct.reservation.IndividualRestriction;
import org.cpsolver.studentsct.reservation.Restriction;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;


/**
 * @author Tomas Muller
 */
@SerializeWith(XRestrictionId.XRestrictionIdSerializer.class)
public class XRestrictionId implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;

	protected XRestrictionType iType;
	private Long iOfferingId;
	private Long iRestrictionId;
	
	public XRestrictionId() {}
	
	public XRestrictionId(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XRestrictionId(XRestrictionType type, Long offeringId, Long reservationId) {
		iType = type; iOfferingId = offeringId; iRestrictionId = reservationId;
	}
	
	public XRestrictionId(XRestrictionId reservation) {
		iType = reservation.getType();
		iOfferingId = reservation.getOfferingId();
		iRestrictionId = reservation.getRestrictionId();
	}
	
	public XRestrictionId(Restriction restriction) {
		iOfferingId = restriction.getOffering().getId();
		iRestrictionId = restriction.getId();
		if (restriction instanceof IndividualRestriction)
			iType = XRestrictionType.Individual;
		else if (restriction instanceof CurriculumRestriction)
			iType = XRestrictionType.Curriculum;
		else if (restriction instanceof CourseRestriction)
			iType = XRestrictionType.Course;
		else
			iType = XRestrictionType.Individual;
	}
	
	public XRestrictionType getType() { return iType; }
	
	public Long getOfferingId() { return iOfferingId; }
	
	public Long getRestrictionId() { return iRestrictionId; }
	
	public boolean isOverride() { return false; }
	
	public boolean canBreakLinkedSections() { return false; }

    @Override
    public int hashCode() {
        return (int)(getRestrictionId() ^ (getRestrictionId() >>> 32));
    }
        
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XRestrictionId)) return false;
        return getRestrictionId() == ((XRestrictionId)o).getRestrictionId() && getOfferingId().equals(((XRestrictionId)o).getOfferingId());
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iType = XRestrictionType.values()[in.readInt()];
		iOfferingId = in.readLong();
		iRestrictionId = in.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(iType.ordinal());
		out.writeLong(iOfferingId);
		out.writeLong(iRestrictionId);
	}
	
	public static class XRestrictionIdSerializer implements Externalizer<XRestrictionId> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XRestrictionId object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XRestrictionId readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XRestrictionId(input);
		}
	}
}
