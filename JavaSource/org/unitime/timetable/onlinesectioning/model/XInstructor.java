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

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

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
	
	public XInstructor(DepartmentalInstructor instructor, OnlineSectioningHelper helper) {
		iUniqueId = instructor.getUniqueId();
		iExternalId = instructor.getExternalUniqueId();
		iName = helper.getInstructorNameFormat().format(instructor);
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
