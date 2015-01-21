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


import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.ifs.util.DistanceMetric;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.Location;

/**
 * @author Tomas Muller
 */
@SerializeWith(XRoom.XRoomSerializer.class)
public class XRoom implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iName;
	private String iExternalId;
	private boolean iIgnoreTooFar;
	private Double iX, iY;
	
	public XRoom() {}
	
	public XRoom(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XRoom(Location location) {
		iUniqueId = location.getUniqueId();
		iExternalId = location.getExternalUniqueId();
		iName = location.getLabel();
		iIgnoreTooFar = location.isIgnoreTooFar();
		iX = location.getCoordinateX();
		iY = location.getCoordinateY();
	}
	
	public XRoom(RoomLocation location) {
		iUniqueId = location.getId();
		iName = location.getName();
		iIgnoreTooFar = location.getIgnoreTooFar();
		iX = location.getPosX();
		iY = location.getPosY();
	}
	
	public Long getUniqueId() { return iUniqueId; }
	public String getName() { return iName; }
	public String getExternalId() { return iExternalId; }
	public boolean getIgnoreTooFar() { return iIgnoreTooFar; }
	public Double getX() { return iX; }
	public Double getY() { return iY; }
	
	@Override
	public String toString() {
		return getName();
	}
	
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XRoom)) return false;
        return getUniqueId().equals(((XRoom)o).getUniqueId());
    }
    
    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }
	
	public int getDistanceInMinutes(DistanceMetric m, XRoom other) {
		if (getUniqueId().equals(other.getUniqueId())) return 0;
        if (getIgnoreTooFar() || other.getIgnoreTooFar()) return 0;
        return  m.getDistanceInMinutes(getUniqueId(), getX(), getY(), other.getUniqueId(), other.getX(), other.getY());
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iName = (String)in.readObject();
		iExternalId = (String)in.readObject();
		iIgnoreTooFar = in.readBoolean();
		if (in.readBoolean()) {
			iX = in.readDouble();
			iY = in.readDouble();
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iName);
		out.writeObject(iExternalId);
		out.writeBoolean(iIgnoreTooFar);
		out.writeBoolean(iX != null && iY != null);
		if (iX != null && iY != null) {
			out.writeDouble(iX);
			out.writeDouble(iY);
		}
	}
	
	public static class XRoomSerializer implements Externalizer<XRoom> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XRoom object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XRoom readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XRoom(input);
		}
		
	}
}
