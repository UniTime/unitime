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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;


import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Subpart;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XConfig.XConfigSerializer.class)
public class XConfig implements Serializable, Comparable<XConfig>, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId = null;
    private String iName = null;
    private Long iOfferingId = null;
    private int iLimit = -1;
    private List<XSubpart> iSubparts = new ArrayList<XSubpart>();

    public XConfig() {
    }
    
    public XConfig(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }

    public XConfig(InstrOfferingConfig config, OnlineSectioningHelper helper) {
    	iUniqueId = config.getUniqueId();
    	iName = config.getName();
    	iOfferingId = config.getInstructionalOffering().getUniqueId();
    	iLimit = (config.isUnlimitedEnrollment() ? -1 : config.getLimit());
    	if (iLimit >= 9999) iLimit = -1;
    	TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
        subparts.addAll(config.getSchedulingSubparts());
        boolean subpartCredit = false;
        for (SchedulingSubpart ss: subparts) {
        	if (ss.getCredit() != null) { subpartCredit = true; break; }
        }
        CourseCreditUnitConfig credit = (subpartCredit ? null : config.getInstructionalOffering().getCredit());
        for (SchedulingSubpart subpart: subparts) {
    		iSubparts.add(new XSubpart(subpart, credit, helper));
    		if (credit != null) credit = null;
        }
    	Collections.sort(iSubparts, new XSubpartComparator());
    }
    
    public XConfig(Config config) {
    	iUniqueId = config.getId();
    	iName = config.getName();
    	iOfferingId = config.getOffering().getId();
    	iLimit = config.getLimit();
    	for (Subpart subpart: config.getSubparts())
    		iSubparts.add(new XSubpart(subpart));
    }

    /** Configuration id */
    public Long getConfigId() {
        return iUniqueId;
    }
    
    /** Get subpart of the given id */
    public XSubpart getSubpart(Long subpartId) {
    	for (XSubpart subpart: iSubparts)
    		if (subpart.getSubpartId().equals(subpartId))
    			return subpart;
    	return null;
    }
    
    /**
     * Configuration limit. This is defines the maximal number of students that can be
     * enrolled into this configuration at the same time. It is -1 in the case of an
     * unlimited configuration
     */
    public int getLimit() {
        return iLimit;
    }

    /** Set configuration limit */
    public void setLimit(int limit) {
        iLimit = limit;
    }

    /** Configuration name */
    public String getName() {
        return iName;
    }

    /** Instructional offering to which this configuration belongs. */
    public Long getOfferingId() {
        return iOfferingId;
    }

    /** List of subparts */
    public List<XSubpart> getSubparts() {
        return iSubparts;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XConfig)) return false;
        return getConfigId().equals(((XConfig)o).getConfigId());
    }
    
    @Override
    public int hashCode() {
        return new Long(getConfigId()).hashCode();
    }

	@Override
	public int compareTo(XConfig config) {
		return getName().equals(config.getName()) ? getConfigId().compareTo(config.getConfigId()) : getName().compareTo(config.getName());
	}
	
	private class XSubpartComparator implements Serializable, Comparator<XSubpart> {
		private static final long serialVersionUID = 1L;

		public boolean isParent(XSubpart s1, XSubpart s2) {
			Long p1 = s1.getParentId();
			if (p1==null) return false;
			if (p1.equals(s2.getSubpartId())) return true;
			return isParent(getSubpart(p1), s2);
		}

		public int compare(XSubpart a, XSubpart b) {
			if (isParent(a, b)) return 1;
	        if (isParent(b, a)) return -1;

	        int cmp = a.getInstructionalType().compareToIgnoreCase(b.getInstructionalType());
			if (cmp != 0) return cmp;
			
			return Double.compare(a.getSubpartId(), b.getSubpartId());
		}		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iName = (String)in.readObject();
		iOfferingId = in.readLong();
		iLimit = in.readInt();
		
		int nrSubparts = in.readInt();
		iSubparts.clear();
		for (int i = 0; i < nrSubparts; i++)
			iSubparts.add(new XSubpart(in));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iName);
		out.writeLong(iOfferingId);
		out.writeInt(iLimit);
		
		out.writeInt(iSubparts.size());
		for (XSubpart subpart: iSubparts)
			subpart.writeExternal(out);
	}
	
	public static class XConfigSerializer implements Externalizer<XConfig> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XConfig object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XConfig readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XConfig(input);
		}
	}

}
