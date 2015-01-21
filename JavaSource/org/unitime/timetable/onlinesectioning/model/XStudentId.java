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
import org.unitime.timetable.model.Student;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XStudentId.XStudentIdSerializer.class)
public class XStudentId implements Serializable, Comparable<XStudentId>, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iStudentId;
    private String iExternalId = null, iName = null;

    public XStudentId() {}
    
    public XStudentId(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }

    public XStudentId(Student student, OnlineSectioningHelper helper) {
    	iStudentId = student.getUniqueId();
    	iExternalId = student.getExternalUniqueId();
    	iName = helper.getStudentNameFormat().format(student);
    }
    
    public XStudentId(org.cpsolver.studentsct.model.Student student) {
    	iStudentId = student.getId();
    	iExternalId = student.getExternalId();
    	iName = student.getName();
    }

    public XStudentId(XStudentId student) {
    	iStudentId = student.getStudentId();
    	iExternalId = student.getExternalId();
    	iName = student.getName();
    }
    
    public XStudentId(Long studentId, String externalId, String name) {
    	iStudentId = studentId;
    	iExternalId = externalId;
    	iName = name;
    }

    /** Student unique id */
    public Long getStudentId() {
        return iStudentId;
    }

    /**
     * Get student external id
     */
    public String getExternalId() { return iExternalId; }

    /**
     * Get student name
     */
    public String getName() { return iName; }

    /**
     * Compare two students for equality. Two students are considered equal if
     * they have the same id.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof XStudentId))
            return false;
        return getStudentId().equals(((XStudentId) object).getStudentId());
    }

    /**
     * Hash code (base only on student id)
     */
    @Override
    public int hashCode() {
        return (int) (getStudentId() ^ (getStudentId() >>> 32));
    }

	@Override
	public int compareTo(XStudentId id) {
		int cmp = getName().compareToIgnoreCase(id.getName());
		if (cmp != 0) return cmp;
		return getStudentId().compareTo(id.getStudentId());
	}

	@Override
    public String toString() {
        return getName();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iStudentId = in.readLong();
		iExternalId = (String)in.readObject();
		iName = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iStudentId);
		out.writeObject(iExternalId);
		out.writeObject(iName);
	}
	
	public static class XStudentIdSerializer implements Externalizer<XStudentId> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XStudentId object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XStudentId readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XStudentId(input);
		}
		
	}
}
