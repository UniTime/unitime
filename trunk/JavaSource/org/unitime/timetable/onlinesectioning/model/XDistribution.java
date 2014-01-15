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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.cpsolver.studentsct.constraint.LinkedSections;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.Class_;

/**
 * @author Tomas Muller
 */
@SerializeWith(XDistribution.XDistributionSerializer.class)
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
	
	public static class XDistributionSerializer implements Externalizer<XDistribution> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XDistribution object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XDistribution readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XDistribution(input);
		}
	}
}