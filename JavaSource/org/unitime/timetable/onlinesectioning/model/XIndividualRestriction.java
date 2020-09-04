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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;

/**
 * @author Tomas Muller
 */
@SerializeWith(XIndividualRestriction.XIndividualRestrictionSerializer.class)
public class XIndividualRestriction extends XRestriction {
	private static final long serialVersionUID = 1L;
	private Set<Long> iStudentIds = new HashSet<Long>();
    
    public XIndividualRestriction() {
        super();
    }
    
    public XIndividualRestriction(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }

    public XIndividualRestriction(org.cpsolver.studentsct.reservation.IndividualRestriction reservation) {
        super(XRestrictionType.Individual, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
    }
    
    /**
     * Restriction is applicable for all students in the reservation
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
        return iStudentIds.contains(student.getStudentId());
    }
    
    /**
     * Students in the reservation
     */
    public Set<Long> getStudentIds() {
        return iStudentIds;
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	
    	int nrStudents = in.readInt();
    	iStudentIds.clear();
    	for (int i = 0; i < nrStudents; i++)
    		iStudentIds.add(in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iStudentIds.size());
		for (Long studentId: iStudentIds)
			out.writeLong(studentId);
	}
	
	public static class XIndividualRestrictionSerializer implements Externalizer<XIndividualRestriction> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XIndividualRestriction object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XIndividualRestriction readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XIndividualRestriction(input);
		}
	}
}