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

import org.cpsolver.studentsct.model.Course;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;

/**
 * @author Tomas Muller
 */
public class XCourse extends XCourseId {
	private static final long serialVersionUID = 1L;
    private String iDepartment = null;
    private String iConsentLabel = null, iConsentAbbv = null;
    private String iNote = null;
    private String iDetails = null;
    private int iLimit = 0;
    private int iSnapshotLimit = 0;
    private int iProjected = 0;
    private Integer iWkEnroll = null, iWkChange = null, iWkDrop = null;
    private XCredit iCredit = null;
    private Long iAlternativeCourseId = null;
    private Long iParentCourseId = null;
    private boolean iControl = false;
    private Set<String> iDisabledOverrides = new HashSet<String>();

    public XCourse() {
    	super();
    }
    
    public XCourse(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
    
    public XCourse(CourseOffering course, OnlineSectioningHelper helper) {
    	this(course);
    }

    public XCourse(CourseOffering course) {
    	super(course);
		iNote = course.getScheduleBookNote();
		iDepartment = (course.getSubjectArea().getDepartment().getDeptCode() == null ? course.getSubjectArea().getDepartment().getAbbreviation() : course.getSubjectArea().getDepartment().getDeptCode());
        boolean unlimited = false;
        iLimit = 0;
        for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
        	if (config.isUnlimitedEnrollment()) unlimited = true;
        	iLimit += config.getLimit();
        	Integer snapshot = config.getSnapShotLimit();
        	if (snapshot != null)
        		iSnapshotLimit += snapshot.intValue();
        }
        if (course.getReservation() != null)
        	iLimit = course.getReservation();
        if (iLimit >= 9999) unlimited = true;
        if (unlimited) iLimit = -1;
        iProjected = (course.getProjectedDemand() != null ? course.getProjectedDemand().intValue() : course.getDemand() != null ? course.getDemand().intValue() : 0);
		iWkEnroll = course.getInstructionalOffering().getLastWeekToEnroll();
		iWkChange = course.getInstructionalOffering().getLastWeekToChange();
		iWkDrop = course.getInstructionalOffering().getLastWeekToDrop();
		if (course.getConsentType() != null) {
			iConsentLabel = course.getConsentType().getLabel();
			iConsentAbbv = course.getConsentType().getAbbv();
		}
        if (course.getCredit() != null)
        	iCredit = new XCredit(course.getCredit());
        for (OverrideType override: course.getDisabledOverrides())
        	iDisabledOverrides.add(override.getReference());
        iAlternativeCourseId = (course.getAlternativeOffering() == null ? null : course.getAlternativeOffering().getUniqueId());
        iParentCourseId = (course.getParentOffering() == null ? null : course.getParentOffering().getUniqueId());
        iControl = course.isIsControl();
    }
    
    public XCourse(Course course) {
    	super(course);
		iNote = course.getNote();
        iLimit = course.getLimit();
        iProjected = course.getProjected();
        if (course.getCredit() != null)
        	iCredit = new XCredit(course.getCredit());
        iControl = course.getName().equals(course.getOffering().getName());
        iParentCourseId = (course.hasParent() ? course.getParent().getId() : null);
    }

    /** Course offering limit */
    public int getLimit() {
        return iLimit;
    }
    
    public int getSnapshotLimit() {
    	return iSnapshotLimit;
    }
    
    public int getProjected() { return iProjected; }
    
	public Integer getLastWeekToEnroll() { return iWkEnroll; }
	public Integer getLastWeekToChange() { return iWkChange; }
	public Integer getLastWeekToDrop() { return iWkDrop; }
	public String getDepartment() { return iDepartment; }
	public String getConsentLabel() { return iConsentLabel; }
	public String getConsentAbbv() { return iConsentAbbv; }
	public boolean isOverrideEnabled(String override) {
		return !iDisabledOverrides.contains(override);
	}
	public boolean areTimeConflictOverridesAllowed() { return isOverrideEnabled("TIME"); }
	public boolean areSpaceConflictOverridesAllowed() { return isOverrideEnabled("CLOS"); }
	public boolean areLinkedConflictOverridesAllowed() { return isOverrideEnabled("CORQ"); }

    /** Course note */
    public String getNote() { return iNote; }
    
	public String getDetails(AcademicSessionInfo session, CourseDetailsProvider provider) throws SectioningException {
		if (iDetails == null && provider != null)
			iDetails = provider.getDetails(session, getSubjectArea(), getCourseNumber());
		return iDetails;
	}
	
    /**
     * Get credit (Online Student Scheduling only)
     */
	public XCredit getCreditInfo() { return iCredit; }
	public boolean hasCredit() { return iCredit != null; }
    public String getCreditAbbv() { return (iCredit == null ? null : iCredit.getAbbreviation()); }
    public String getCreditText() { return (iCredit == null ? null : iCredit.getText()); }
    public String getCredit() { return (iCredit == null ? null : iCredit.getAbbreviation() + "|" + iCredit.getText()); }
    public Float getMinCredit() { return iCredit != null ? iCredit.getMinCredit() : null; }
    public Float getMaxCredit() { return iCredit != null ? iCredit.getMaxCredit() : null; }
    
    public boolean hasAlternativeCourse() { return iAlternativeCourseId != null; }
    public Long getAlternativeCourseId() { return iAlternativeCourseId; }
    
    public boolean hasParentCourse() { return iParentCourseId != null; }
    public Long getParentCourseId() { return iParentCourseId; }
    
    public boolean isControlling() { return iControl; }
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		iDepartment = (String)in.readObject();
		iConsentLabel = (String)in.readObject();
		iConsentAbbv = (String)in.readObject();
		iNote = (String)in.readObject();
		iDetails = (String)in.readObject();
		iLimit = in.readInt();
		iSnapshotLimit = in.readInt();
		iProjected = in.readInt();
		iWkEnroll = (in.readBoolean() ? in.readInt() : null);
		iWkChange = (in.readBoolean() ? in.readInt() : null);
		iWkDrop = (in.readBoolean() ? in.readInt() : null);
		iCredit = (in.readBoolean() ? new XCredit(in) : null);
		iAlternativeCourseId = (in.readBoolean() ? in.readLong() : null);
		iParentCourseId = (in.readBoolean() ? in.readLong() : null);
		iControl = in.readBoolean();
		int overrides = in.readInt();
		for (int i = 0; i < overrides; i++)
			iDisabledOverrides.add((String)in.readObject());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(iDepartment);
		out.writeObject(iConsentLabel);
		out.writeObject(iConsentAbbv);
		out.writeObject(iNote);
		out.writeObject(iDetails);
		out.writeInt(iLimit);
		out.writeInt(iSnapshotLimit);
		out.writeInt(iProjected);
		out.writeBoolean(iWkEnroll != null);
		if (iWkEnroll != null)
			out.writeInt(iWkEnroll);
		out.writeBoolean(iWkChange != null);
		if (iWkChange != null)
			out.writeInt(iWkChange);
		out.writeBoolean(iWkDrop != null);
		if (iWkDrop != null)
			out.writeInt(iWkDrop);
		out.writeBoolean(iCredit != null);
		if (iCredit != null)
			iCredit.writeExternal(out);
		out.writeBoolean(iAlternativeCourseId != null);
		if (iAlternativeCourseId != null)
			out.writeLong(iAlternativeCourseId);
		out.writeBoolean(iParentCourseId != null);
		if (iParentCourseId != null)
			out.writeLong(iParentCourseId);
		out.writeBoolean(iControl);
		out.writeInt(iDisabledOverrides.size());
		for (String override: iDisabledOverrides)
			out.writeObject(override);
	}
}