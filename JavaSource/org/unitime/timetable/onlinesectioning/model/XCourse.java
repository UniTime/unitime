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

import net.sf.cpsolver.studentsct.model.Course;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;

public class XCourse extends XCourseId {
	private static final long serialVersionUID = 1L;
    private String iSubjectArea = null;
    private String iCourseNumber = null;
    private String iDepartment = null;
    private String iConsentLabel = null, iConsentAbbv = null;
    private String iNote = null;
    private String iDetails = null;
    private int iLimit = 0;
    private int iProjected = 0;
    private Integer iWkEnroll = null, iWkChange = null, iWkDrop = null;

    public XCourse() {
    	super();
    }
    
    public XCourse(CourseOffering course, OnlineSectioningHelper helper) {
    	this(course);
    }

    public XCourse(CourseOffering course) {
    	super(course);
		iSubjectArea = course.getSubjectAreaAbbv().trim();
		iCourseNumber = course.getCourseNbr().trim();
		iNote = course.getScheduleBookNote();
		iDepartment = (course.getSubjectArea().getDepartment().getDeptCode() == null ? course.getSubjectArea().getDepartment().getAbbreviation() : course.getSubjectArea().getDepartment().getDeptCode());
        boolean unlimited = false;
        iLimit = 0;
        for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
        	if (config.isUnlimitedEnrollment()) unlimited = true;
        	iLimit += config.getLimit();
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
    }
    
    public XCourse(Course course) {
    	super(course);
		iSubjectArea = course.getSubjectArea();
		iCourseNumber = course.getCourseNumber();
		iNote = course.getNote();
        iLimit = course.getLimit();
        iProjected = course.getProjected();
    }

    /** Subject area */
    public String getSubjectArea() {
        return iSubjectArea;
    }

    /** Course number */
    public String getCourseNumber() {
        return iCourseNumber;
    }

    /** Course offering limit */
    public int getLimit() {
        return iLimit;
    }
    
    public int getProjected() { return iProjected; }
    
	public Integer getLastWeekToEnroll() { return iWkEnroll; }
	public Integer getLastWeekToChange() { return iWkChange; }
	public Integer getLastWeekToDrop() { return iWkDrop; }
	public String getDepartment() { return iDepartment; }
	public String getConsentLabel() { return iConsentLabel; }
	public String getConsentAbbv() { return iConsentAbbv; }

    /** Course note */
    public String getNote() { return iNote; }
    
	public String getDetails(AcademicSessionInfo session, CourseDetailsProvider provider) throws SectioningException {
		if (iDetails == null && provider != null)
			iDetails = provider.getDetails(session, getSubjectArea(), getCourseNumber());
		return iDetails;
	}
}