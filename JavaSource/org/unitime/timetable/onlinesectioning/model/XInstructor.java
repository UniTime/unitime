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

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.DepartmentalInstructor;

/**
 * @author Tomas Muller
 */
@SerializeWith(XInstructor.XInstructorSerializer.class)
public class XInstructor implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iExternalId;
	private String iName;
	private String iEmail;
	
	public XInstructor() {}
	
	public XInstructor(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XInstructor(DepartmentalInstructor instructor, String nameFormat) {
		iUniqueId = instructor.getUniqueId();
		iExternalId = instructor.getExternalUniqueId();
		iName = instructor.getName(nameFormat);
		iEmail = instructor.getEmail();
	}
	
	public XInstructor(Long uniqueId, String externalId, String name, String email) {
		iUniqueId = uniqueId;
		iExternalId = externalId;
		iName = name;
		iEmail = email;
	}
	
	public Long getIntructorId() {
		return iUniqueId;
	}
	
	public String getExternalId() {
		return iExternalId;
	}
	
	public String getName() {
		return iName;
	}
	
	public String getEmail() {
		return iEmail;
	}
	
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XInstructor)) return false;
        return getIntructorId().equals(((XInstructor)o).getIntructorId());
    }
    
    @Override
    public int hashCode() {
        return new Long(getIntructorId()).hashCode();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iExternalId = (String)in.readObject();
		iName = (String)in.readObject();
		iEmail = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iExternalId);
		out.writeObject(iName);
		out.writeObject(iEmail);
	}
	
	public static class XInstructorSerializer implements Externalizer<XInstructor> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XInstructor object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XInstructor readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XInstructor(input);
		}
	}
}
