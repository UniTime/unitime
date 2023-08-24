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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import org.cpsolver.studentsct.constraint.LinkedSections;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

/**
 * @author Tomas Muller
 */
public class XDistribution implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iDistributionId = null;
	private int iVariant = 0;
	private XDistributionType iType = null;
	private Set<Long> iOfferingIds = new HashSet<Long>();
	private Set<Long> iSectionIds = new HashSet<Long>();
	
	public XDistribution() {};
	
	public XDistribution(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
    
    public XDistribution(XDistributionType type, Long id, int variant, Collection<Class_> sections) {
    	iType = type;
    	iDistributionId = id;
    	iVariant = variant;
    	for (Class_ clazz: sections) {
    		iOfferingIds.add(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId());
    		iSectionIds.add(clazz.getUniqueId());
    	}
    }
    
    public XDistribution(XDistributionType type, Long id, Long offeringId, Collection<Long> sectionIds) {
    	iType = type;
    	iDistributionId = id;
    	iVariant = 0;
    	iOfferingIds.add(offeringId);
    	iSectionIds.addAll(sectionIds);
    }
    
    public XDistribution(LinkedSections link, long id) {
    	iType = XDistributionType.LinkedSections;
    	iDistributionId = - id;
    	iVariant = 0;
    	for (Offering offering: link.getOfferings()) {
    		iOfferingIds.add(offering.getId());
    		for (Subpart subpart: link.getSubparts(offering))
    			for (Section section: link.getSections(subpart))
    				iSectionIds.add(section.getId());
    	}
    }
    
    public XDistributionType getDistributionType() { return iType; }
    
    public Long getDistributionId() { return iDistributionId; }
    
    public int getVariant() { return iVariant; }
    
    public Set<Long> getOfferingIds() { return iOfferingIds; }
    
    public Set<Long> getSectionIds() { return iSectionIds; }
    
    public boolean hasSection(Long sectionId) {
    	return iSectionIds.contains(sectionId);
    }
    
    public boolean isViolated(XStudent student, OnlineSectioningServer server) {
    	int nrMatch = 0, nrMismatch = 0;
    	for (Long offeringId: getOfferingIds()) {
    		XOffering offering = server.getOffering(offeringId);
    		for (XRequest req: student.getRequests()) {
    			if (req instanceof XCourseRequest) {
    				XCourseRequest cr = (XCourseRequest)req;
    				XEnrollment enrl = cr.getEnrollment();
    				if (enrl != null && enrl.getOfferingId().equals(offeringId)) {
    					boolean match = true;
    					Set<Long> matchingSubparts = new HashSet<Long>();
    					for (Long sectionId: getSectionIds()) {
    						XSection section = offering.getSection(sectionId);
    						if (section != null && enrl.getSectionIds().contains(sectionId)) {
    							matchingSubparts.add(section.getSubpartId());
    						}
    					}
    					for (Long sectionId: getSectionIds()) {
    						XSection section = offering.getSection(sectionId);
    						if (section != null && !enrl.getSectionIds().contains(sectionId) && !matchingSubparts.contains(section.getSubpartId())) {
    							match = false;
    							break;
    						}
    					}
    					if (match) {
    						nrMatch ++;
    					} else {
    						nrMismatch ++;
    					}
    				}
    			}
    		}
    	}
    	return nrMatch > 0 && nrMismatch > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XDistribution)) return false;
        return getDistributionId().equals(((XDistribution)o).getDistributionId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getDistributionId() ^ (getDistributionId() >>> 32) ^ getVariant());
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iDistributionId = in.readLong();
		iVariant = in.readInt();
		iType = XDistributionType.values()[in.readInt()];
		
		int nrOfferings = in.readInt();
		iOfferingIds.clear();
		for (int i = 0; i < nrOfferings; i++)
			iOfferingIds.add(in.readLong());
		
		int nrSections = in.readInt();
		iSectionIds.clear();
		for (int i = 0; i < nrSections; i++)
			iSectionIds.add(in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iDistributionId);
		out.writeInt(iVariant);
		out.writeInt(iType.ordinal());
		
		out.writeInt(iOfferingIds.size());
		for (Long offeringId: iOfferingIds)
			out.writeLong(offeringId);
		
		out.writeInt(iSectionIds.size());
		for (Long sectionId: iSectionIds)
			out.writeLong(sectionId);
	}
}