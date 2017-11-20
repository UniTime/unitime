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


import org.cpsolver.studentsct.model.Request;
import org.unitime.timetable.model.CourseDemand;

/**
 * @author Tomas Muller
 */
public abstract class XRequest implements Serializable, Comparable<XRequest>, Externalizable {
	private static final long serialVersionUID = 1L;
	protected Long iRequestId = null;
	protected int iPriority = 0;
	protected boolean iAlternative = false;
	protected Long iStudentId;
    
    public XRequest() {}
    
    public XRequest(CourseDemand demand) {
    	iRequestId = demand.getUniqueId();
    	iPriority = demand.getPriority();
    	iAlternative = demand.isAlternative();
    	iStudentId = demand.getStudent().getUniqueId();
    }
    
    public XRequest(Request request) {
    	iRequestId = request.getId();
    	iPriority = request.getPriority();
    	iAlternative = request.isAlternative();
    	iStudentId = request.getStudent().getId();
    }
    
    public XRequest(XRequest request) {
    	iRequestId = request.getRequestId();
    	iPriority = request.getPriority();
    	iAlternative = request.isAlternative();
    	iStudentId = request.getStudentId();
    }

    /** Request id */
    public Long getRequestId() { return iRequestId; }

    /**
     * Request priority -- if there is a choice, request with lower priority is
     * more preferred to be assigned
     */
    public int getPriority() { return iPriority; }

    /**
     * True, if the request is alternative (alternative request can be assigned
     * instead of a non-alternative course requests, if it is left unassigned)
     */
    public boolean isAlternative() { return iAlternative; }

    /** Student to which this request belongs */
    public Long getStudentId() { return iStudentId; }
    
    @Override
    public int hashCode() {
        return (int)(getRequestId() ^ (getRequestId() >>> 32));
    }
    
    @Override
    public int compareTo(XRequest request) {
    	return (isAlternative() != request.isAlternative() ? isAlternative() ? 1 : -1 : getPriority() < request.getPriority() ? -1 : 1);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XRequest)) return false;
        return getRequestId().equals(((XRequest)o).getRequestId()) && getStudentId().equals(((XRequest)o).getStudentId());
    }
    
    @Override
    public String toString() {
    	return (isAlternative() ? "A" : "") + getPriority() + ".";
    }
    
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iRequestId = in.readLong();
		iPriority = in.readInt();
		iAlternative = in.readBoolean();
		iStudentId = in.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iRequestId);
		out.writeInt(iPriority);
		out.writeBoolean(iAlternative);
		out.writeLong(iStudentId);
	}
}