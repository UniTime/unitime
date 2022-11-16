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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;

/**
 * @author Tomas Muller
 */
public abstract class XRestriction extends XRestrictionId {
	private static final long serialVersionUID = 1L;
	
    private Set<Long> iConfigs = new HashSet<Long>();
    private Map<Long, Set<Long>> iSections = new HashMap<Long, Set<Long>>();
    private Set<Long> iIds = new HashSet<Long>();
    
    public XRestriction() {
    	super();
    }
    
    
    public XRestriction(XRestrictionType type, org.cpsolver.studentsct.reservation.Restriction restriction) {
    	super(type, restriction.getOffering().getId(), restriction.getId());
    	for (Config config: restriction.getConfigs()) {
    		iConfigs.add(config.getId());
    		iIds.add(-config.getId());
    	}
    	for (Map.Entry<Subpart, Set<Section>> entry: restriction.getSections().entrySet()) {
    		Set<Long> sections = new HashSet<Long>();
    		for (Section section: entry.getValue()) {
    			sections.add(section.getId());
    			iIds.add(section.getId());
    		}
    		iSections.put(entry.getKey().getId(), sections);
    	}
    }
    
    /**
     * Returns true if the student is applicable for the reservation
     * @param student a student 
     * @return true if student can use the reservation to get into the course / configuration / section
     */
    public abstract boolean isApplicable(XStudent student, XCourseId course);

    /**
     * One or more configurations on which the reservation is set (optional).
     */
    public Set<Long> getConfigsIds() { return iConfigs; }
    
    public boolean hasConfigRestriction(Long configId) { return iIds.contains(-configId); }
    public boolean hasSectionRestriction(Long sectionId) { return iIds.contains(sectionId); }
        
    /**
     * One or more sections on which the reservation is set (optional).
     */
    public Map<Long, Set<Long>> getSections() { return iSections; }
    
    /**
     * One or more sections on which the reservation is set (optional).
     */
    public Set<Long> getSectionIds(Long subpartId) {
        return iSections.get(subpartId);
    }
    

    /**
     * Return true if the given enrollment meets the reservation.
     */
    public boolean isIncluded(Long configId, Collection<XSection> sections) {
        // no restrictions -> not included
        if (iConfigs.isEmpty() && iSections.isEmpty()) return false;
        
        // If there are configurations, check the configuration
        if (!iConfigs.isEmpty() && !iConfigs.contains(configId)) return false;
        
        // Check all the sections of the enrollment
        for (XSection section: sections) {
            Set<Long> reserved = iSections.get(section.getSubpartId());
            if (reserved != null && !reserved.contains(section.getSectionId()))
                return false;
        }
        
        return true;
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);

    	int nrConfigs = in.readInt();
    	iConfigs.clear();
    	for (int i = 0; i < nrConfigs; i++)
    		iConfigs.add(in.readLong());
    	
    	int nrSubparts = in.readInt();
    	iSections.clear(); 
    	for (int i = 0; i < nrSubparts; i++) {
    		Set<Long> sections = new HashSet<Long>();
    		iSections.put(in.readLong(), sections);
    		int nrSection = in.readInt();
    		for (int j = 0; j < nrSection; j++) {
    			sections.add(in.readLong());
    		}
    	}
    	iIds.clear();
    	int nrIds = in.readInt();
    	for (int i = 0; i < nrIds; i++)
    		iIds.add(in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iConfigs.size());
		for (Long config: iConfigs)
			out.writeLong(config);
		
		Set<Map.Entry<Long, Set<Long>>> entries = iSections.entrySet();
		out.writeInt(entries.size());
		for (Map.Entry<Long, Set<Long>> entry: entries) {
			out.writeLong(entry.getKey());
			out.writeInt(entry.getValue().size());
			for (Long id: entry.getValue())
				out.writeLong(id);
		}
		
		out.writeInt(iIds.size());
		for (Long id: iIds)
			out.writeLong(id);
	}
}