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
import java.util.HashMap;
import java.util.Map;

import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;


/**
 * @author Tomas Muller
 */
@SerializeWith(XExpectations.XExpectationsSerializer.class)
public class XExpectations implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iOfferingId = null;
	private Map<Long, Double> iExpectations = null;
	
	public XExpectations() {}
	
	public XExpectations(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XExpectations(Long offeringId) {
		this(offeringId, null);
	}
	
	public XExpectations(Long offeringId, Map<Long, Double> expectations) {
		iOfferingId = offeringId;
		iExpectations = expectations;
	}
	
	public XExpectations(Offering offering) {
		iOfferingId = offering.getId();
		iExpectations = new HashMap<Long, Double>();
		for (Config config: offering.getConfigs())
			for (Subpart subpart: config.getSubparts())
				for (Section section: subpart.getSections())
					iExpectations.put(section.getId(), section.getSpaceExpected());
	}
	
	public Long getOfferingId() {
		return iOfferingId;
	}
	
	public Double getExpectedSpace(Long sectionId) {
		if (iExpectations == null) return 0.0;
		Double expected = iExpectations.get(sectionId);
		return expected == null ? 0.0 : expected.doubleValue();
	}
	
	public void setExpectedSpace(Long sectionId, double expectedSpace) {
		if (iExpectations == null)
			iExpectations = new HashMap<Long, Double>();
		iExpectations.put(sectionId, expectedSpace);
	}
	
	public void incExpectedSpace(Long sectionId, double inc) {
		if (iExpectations == null)
			iExpectations = new HashMap<Long, Double>();
		Double expected = iExpectations.get(sectionId);
		iExpectations.put(sectionId, (expected == null ? 0.0 : expected.doubleValue()) + inc);
	}
	
	public Map<Long, Double> toMap() {
		return iExpectations == null ? new HashMap<Long, Double>() : new HashMap<Long, Double>(iExpectations);
	}
	
	public boolean hasExpectations() {
		if (iExpectations == null) return false;
		for (Double exp: iExpectations.values())
			if (exp > 0.0) return true;
		return false;
	}
	
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XExpectations)) return false;
        return getOfferingId().equals(((XExpectations)o).getOfferingId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getOfferingId() ^ (getOfferingId() >>> 32));
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iOfferingId = in.readLong();
		int nrExpectations = in.readInt();
		if (nrExpectations == 0)
			iExpectations = null;
		else {
			iExpectations = new HashMap<Long, Double>();
			for (int i = 0; i < nrExpectations; i++)
				iExpectations.put(in.readLong(), in.readDouble());
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iOfferingId);
		out.writeInt(iExpectations == null ? 0 : iExpectations.size());
		if (iExpectations != null) {
			for (Map.Entry<Long, Double> entry: iExpectations.entrySet()) {
				out.writeLong(entry.getKey());
				out.writeDouble(entry.getValue());
			}
		}
	}
	
	public static class XExpectationsSerializer implements Externalizer<XExpectations> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XExpectations object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XExpectations readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XExpectations(input);
		}
	}
}
