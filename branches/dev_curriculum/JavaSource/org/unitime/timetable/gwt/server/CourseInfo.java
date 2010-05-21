/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.custom.CourseDetailsProvider;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.CourseOffering;

public class CourseInfo implements Comparable<CourseInfo> {
	private Long iUniqueId;
	private Long iAcademicSessionId;
	private String iSubjectArea;
	private String iCourseNbr;
	private String iTitle;
	private String iNote;
	private String iDetails = null;
	private boolean iHasUniqueName = true;
	private String iConsent = null;
	
	public CourseInfo(CourseOffering course)  throws SectioningException {
		iUniqueId = course.getUniqueId();
		iAcademicSessionId = course.getSubjectArea().getSession().getUniqueId();
		iSubjectArea = course.getSubjectArea().getSubjectAreaAbbreviation();
		iCourseNbr = course.getCourseNbr().trim();
		iTitle = (course.getTitle() == null ? null : course.getTitle().trim());
		iNote = course.getScheduleBookNote();
		if (course.getInstructionalOffering().getConsentType() != null)
			iConsent = course.getInstructionalOffering().getConsentType().getLabel();
	}
	
	public CourseInfo(CourseOffering course, String courseNbr)  throws SectioningException {
		this(course);
		iCourseNbr = courseNbr;
	}

	public Long getUniqueId() { return iUniqueId; }
	public Long getAcademicSessionId() { return iAcademicSessionId; }
	public String getSubjectArea() { return iSubjectArea; }
	public String getCourseNbr() { return iCourseNbr; }
	public String getTitle() { return iTitle; }
	public String getNote() { return iNote; }
	public boolean hasUniqueName() { return iHasUniqueName; }
	public void setHasUniqueName(boolean hasUniqueName) { iHasUniqueName = hasUniqueName; }
	public String getConsent() { return iConsent; }

	public int compareTo(CourseInfo c) {
		int cmp = getSubjectArea().compareToIgnoreCase(c.getSubjectArea());
		if (cmp!=0) return cmp;
		cmp = getCourseNbr().compareToIgnoreCase(c.getCourseNbr());
		if (cmp!=0) return cmp;
		cmp = (getTitle() == null ? "" : getTitle()).compareToIgnoreCase(c.getTitle() == null ? "" : c.getTitle());
		if (cmp!=0) return cmp;
		return getUniqueId().compareTo(c.getUniqueId());
	}
	
	public boolean matchCourseName(String queryInLowerCase) {
		if ((getSubjectArea()+" "+getCourseNbr()).toLowerCase().startsWith(queryInLowerCase)) return true;
		if ((getSubjectArea()+" "+getCourseNbr()+" "+getTitle()).toLowerCase().startsWith(queryInLowerCase)) return true;
		if ((getSubjectArea()+" "+getCourseNbr()+" - "+getTitle()).toLowerCase().startsWith(queryInLowerCase)) return true;
		/*
		if (queryInLowerCase.indexOf('-') > 0) {
			String courseName = queryInLowerCase.substring(0, queryInLowerCase.indexOf('-') - 1).trim();
			if ((getSubjectArea()+" "+getCourseNbr()).toLowerCase().equals(queryInLowerCase)) return true;
		}
		*/
		return false;
	}
	
	public boolean matchTitle(String queryInLowerCase) {
		if (matchCourseName(queryInLowerCase)) return false;
		if (queryInLowerCase.length()>2 && getTitle()!=null && getTitle().toLowerCase().contains(queryInLowerCase)) return true;
		return false;
	}

	public String getDetails() throws SectioningException {
		if (iDetails == null) {
			CourseDetailsProvider provider = null;
			try {
				provider = (CourseDetailsProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.CourseDetailsProvider")).newInstance();
			} catch (Exception e) {
				throw new SectioningException(SectioningExceptionType.NO_CUSTOM_COURSE_DETAILS);
			}
			iDetails = provider.getDetails(SectioningServer.getInstance(iAcademicSessionId).getAcademicSession(), iSubjectArea, iCourseNbr);
		}
		return iDetails;
	}
}
