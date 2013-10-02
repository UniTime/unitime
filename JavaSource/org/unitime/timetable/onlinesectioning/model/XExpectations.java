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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

public class XExpectations implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long iOfferingId = null;
	private Map<Long, Double> iExpectations = null;
	
	public XExpectations() {}
	
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
	
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XExpectations)) return false;
        return getOfferingId().equals(((XExpectations)o).getOfferingId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getOfferingId() ^ (getOfferingId() >>> 32));
    }

}
