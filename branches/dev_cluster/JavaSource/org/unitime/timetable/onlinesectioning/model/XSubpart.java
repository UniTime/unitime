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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

public class XSubpart implements Serializable {
	private static final long serialVersionUID = 1L;
	private static DecimalFormat sF3Z = new DecimalFormat("000"); 
	private Long iUniqueId = null;
    private String iInstructionalType = null;
    private String iName = null;
    private List<XSection> iSections = new ArrayList<XSection>();
    private Long iConfigId = null;
    private Long iParentId = null;
    private String iCreditAbbv = null, iCreditText = null;
    private boolean iAllowOverlap = false;

    public XSubpart() {}
    
    public XSubpart(SchedulingSubpart subpart, CourseCreditUnitConfig credit, OnlineSectioningHelper helper) {
    	iUniqueId = subpart.getUniqueId();
        iInstructionalType = sF3Z.format(subpart.getItype().getItype()) + subpart.getSchedulingSubpartSuffix(helper.getHibSession());
        iAllowOverlap = subpart.isStudentAllowOverlap();
        iName = subpart.getItype().getAbbv().trim();
        iConfigId = subpart.getInstrOfferingConfig().getUniqueId();
        iParentId = subpart.getParentSubpart() == null ? null : subpart.getParentSubpart().getUniqueId();
        if (subpart.getCredit() != null) {
        	iCreditAbbv = subpart.getCredit().creditAbbv();
        	iCreditText = subpart.getCredit().creditText();
        } else if (credit != null) {
        	iCreditAbbv = credit.creditAbbv();
        	iCreditText = credit.creditText();
        }
        for (Class_ clazz: subpart.getClasses())
        	iSections.add(new XSection(clazz, helper));
        
        Collections.sort(iSections);
    }
    
    public XSubpart(Subpart subpart) {
    	iUniqueId = subpart.getId();
    	iInstructionalType = subpart.getInstructionalType();
    	iAllowOverlap = subpart.isAllowOverlap();
    	iName = subpart.getName();
    	iConfigId = subpart.getConfig().getId();
    	iParentId = (subpart.getParent() == null ? null : subpart.getParent().getId());
    	if (subpart.getCredit() != null) {
    		String[] cred = subpart.getCredit().split("\\|");
    		iCreditAbbv = cred[0];
    		iCreditText = cred[1];
    	}
    	for (Section section: subpart.getSections())
    		iSections.add(new XSection(section));
    }

    /** Subpart id */
    public Long getSubpartId() {
        return iUniqueId;
    }

    /** Instructional type, e.g., Lecture, Recitation or Laboratory */
    public String getInstructionalType() {
        return iInstructionalType;
    }

    /** Subpart name */
    public String getName() {
        return iName;
    }

    /** Instructional offering configuration to which this subpart belongs */
    public Long getConfigId() {
        return iConfigId;
    }

    /** List of sections */
    public List<XSection> getSections() {
        return iSections;
    }

    /** Parent subpart, if parent-child relation is defined between subparts */
    public Long getParentId() {
        return iParentId;
    }
    
    /** Return true if overlaps are allowed, but the number of overlapping slots should be minimized. */
    public boolean isAllowOverlap() {
        return iAllowOverlap;
    }

    @Override
    public String toString() {
        return getName();
    }
        
    /** Return total of section limits of this subpart */
    public int getLimit() {
        int limit = 0;
        for (XSection section: getSections()) {
            if (section.getLimit() < 0) return -1;
            limit += section.getLimit();
        }
        return limit;
    }
    
    /**
     * Get credit (Online Student Scheduling only)
     */
    public String getCreditAbbv() { return iCreditAbbv; }
    public String getCreditText() { return iCreditText; }
    public String getCredit() { return getCreditAbbv() == null ? null : getCreditAbbv() + "|" + getCreditText(); }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XSubpart)) return false;
        return getSubpartId().equals(((XSubpart)o).getSubpartId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getSubpartId() ^ (getSubpartId() >>> 32));
    }
    

}
