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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XSubpart.XSubpartSerializer.class)
public class XSubpart implements Serializable, Externalizable {
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
    private Map<Long, String[]> iCreditByCourse = new HashMap<Long, String[]>();

    public XSubpart() {}
    
    public XSubpart(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
    
    public XSubpart(SchedulingSubpart subpart, boolean courseCredit, OnlineSectioningHelper helper) {
    	iUniqueId = subpart.getUniqueId();
        iInstructionalType = sF3Z.format(subpart.getItype().getItype()) + subpart.getSchedulingSubpartSuffix(helper.getHibSession());
        iAllowOverlap = subpart.isStudentAllowOverlap();
        iName = subpart.getItype().getAbbv().trim();
        if (subpart.getInstrOfferingConfig().getInstructionalMethod() != null)
        	iName += " (" + subpart.getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")";
        iConfigId = subpart.getInstrOfferingConfig().getUniqueId();
        iParentId = subpart.getParentSubpart() == null ? null : subpart.getParentSubpart().getUniqueId();
        if (subpart.getCredit() != null) {
        	iCreditAbbv = subpart.getCredit().creditAbbv();
        	iCreditText = subpart.getCredit().creditText();
        }
        if (courseCredit) {
        	for (CourseOffering co: subpart.getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
        		if (co.getCredit() != null)
        			iCreditByCourse.put(co.getUniqueId(), new String[] {co.getCredit().creditAbbv(), co.getCredit().creditText()});
        	}
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
    public String getCreditAbbv(Long courseId) {
    	String[] credit = (courseId != null ? iCreditByCourse.get(courseId) : null);
    	return (credit != null ? credit[0] : iCreditAbbv);
    }
    public String getCreditText(Long courseId) {
    	String[] credit = (courseId != null ? iCreditByCourse.get(courseId) : null);
    	return (credit != null ? credit[1] : iCreditText);
    }
    public String getCredit(Long courseId) {
    	if (courseId != null) {
        	String[] credit = iCreditByCourse.get(courseId);
        	if (credit != null)
        		return credit[0] + "|" + credit[1];
    	}
    	return iCreditAbbv == null ? null : iCreditAbbv + "|" + iCreditText;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XSubpart)) return false;
        return getSubpartId().equals(((XSubpart)o).getSubpartId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getSubpartId() ^ (getSubpartId() >>> 32));
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iInstructionalType = (String)in.readObject();
		iName = (String)in.readObject();
		
		int nrSections = in.readInt();
		iSections.clear();
		for (int i = 0; i < nrSections; i++)
			iSections.add(new XSection(in));
		
		iConfigId = in.readLong();
		iParentId = in.readLong();
		if (iParentId < 0) iParentId = null;
		
		iCreditAbbv = (String)in.readObject();
		iCreditText = (String)in.readObject();
		
		iAllowOverlap = in.readBoolean();
		
		int nrCredits = in.readInt();
		iCreditByCourse.clear();
		for (int i = 0; i < nrCredits; i++)
			iCreditByCourse.put(in.readLong(), new String[] {(String)in.readObject(), (String)in.readObject()});
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iInstructionalType);
		out.writeObject(iName);
		
		out.writeInt(iSections.size());
		for (XSection section: iSections)
			section.writeExternal(out);
		
		out.writeLong(iConfigId);
		out.writeLong(iParentId == null ? -1l : iParentId);
		
		out.writeObject(iCreditAbbv);
		out.writeObject(iCreditText);
		
		out.writeBoolean(iAllowOverlap);
		
		out.writeInt(iCreditByCourse.size());
		for (Map.Entry<Long, String[]> entry: iCreditByCourse.entrySet()) {
			out.writeLong(entry.getKey());
			out.writeObject(entry.getValue()[0]);
			out.writeObject(entry.getValue()[1]);
		}
	}

	public static class XSubpartSerializer implements Externalizer<XSubpart> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XSubpart object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XSubpart readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XSubpart(input);
		}
	}

}
