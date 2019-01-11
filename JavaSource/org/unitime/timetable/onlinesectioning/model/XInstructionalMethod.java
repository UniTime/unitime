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
import org.unitime.timetable.model.InstructionalMethod;

/**
 * @author Tomas Muller
 */
@SerializeWith(XInstructionalMethod.XInstructionalMethodSerializer.class)
public class XInstructionalMethod implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iLabel;
	private String iReference;
	
	public XInstructionalMethod() {}
	
	public XInstructionalMethod(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XInstructionalMethod(InstructionalMethod method) {
		iUniqueId = method.getUniqueId();
		iReference = method.getReference();
		iLabel = method.getLabel();
	}
	
	public XInstructionalMethod(Long id, String label, String reference) {
		iUniqueId = id;
		iReference = reference;
		iLabel = label;
	}
	
	public Long getUniqueId() { return iUniqueId; }
	public String getLabel() { return iLabel; }
	public String getReference() { return iReference; }
	
	@Override
	public String toString() {
		return getLabel();
	}
	
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XInstructionalMethod)) return false;
        return getUniqueId().equals(((XInstructionalMethod)o).getUniqueId());
    }
    
    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iReference = (String)in.readObject();
		iLabel = (String)in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iReference);
		out.writeObject(iLabel);
	}
	
	public static class XInstructionalMethodSerializer implements Externalizer<XInstructionalMethod> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XInstructionalMethod object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XInstructionalMethod readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XInstructionalMethod(input);
		}
		
	}
}
