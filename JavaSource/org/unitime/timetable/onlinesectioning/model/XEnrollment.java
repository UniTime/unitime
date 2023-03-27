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
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Section;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

/**
 * @author Tomas Muller
 */
public class XEnrollment extends XCourseId implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long iStudentId = null;
	private Long iConfigId = null;
	private Set<Long> iSectionIds = new HashSet<Long>();
    private Date iTimeStamp = null;
    private XApproval iApproval = null;
    private XReservationId iReservation = null;

	public XEnrollment() {
		super();
	}
	
	public XEnrollment(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}
	
	public XEnrollment(Student student, CourseOffering course, OnlineSectioningHelper helper, Collection<StudentClassEnrollment> enrollments) {
		super(course);
		iStudentId = student.getUniqueId();
		for (StudentClassEnrollment enrl: enrollments) {
			if (iConfigId == null)
				iConfigId = enrl.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getUniqueId();
			iSectionIds.add(enrl.getClazz().getUniqueId());
			if (iTimeStamp == null || (enrl.getTimestamp() != null && enrl.getTimestamp().after(iTimeStamp)))
				iTimeStamp = enrl.getTimestamp();
        	if (iApproval == null && enrl.getApprovedBy() != null && enrl.getApprovedDate() != null) {
        		iApproval = new XApproval(enrl.getApprovedBy(), enrl.getApprovedDate(), helper.getApproverName(enrl.getApprovedBy(), student.getSession().getUniqueId()));
        	}
		}
	}
	
	public XEnrollment(XEnrollment enrollment) {
		super(enrollment);
		iStudentId = enrollment.getStudentId();
		iConfigId = enrollment.getConfigId();
		iSectionIds.addAll(enrollment.getSectionIds());
		iTimeStamp = enrollment.getTimeStamp();
		iApproval = enrollment.getApproval();
		iReservation = enrollment.getReservation();
	}
	
	public XEnrollment(Enrollment enrollment) {
		super(enrollment.getOffering().getId(), enrollment.getCourse().getId(), enrollment.getCourse().getSubjectArea(), enrollment.getCourse().getCourseNumber());
		iStudentId = enrollment.getStudent().getId();
		iConfigId = enrollment.getConfig().getId();
		for (Section section: enrollment.getSections()) {
			iSectionIds.add(section.getId());
		}
		iTimeStamp = (enrollment.getTimeStamp() == null ? null : new Date(enrollment.getTimeStamp()));
		iApproval = enrollment.getApproval() == null ? null : new XApproval(enrollment.getApproval().split(":"));
		iReservation = enrollment.getReservation() == null ? null : new XReservationId(enrollment.getReservation());
	}
	
	public XEnrollment(Student student, XCourseId courseId, Long configId, Collection<Long> sectionIds) {
		super(courseId);
		iStudentId = student.getUniqueId();
		iConfigId = configId;
		if (sectionIds != null)
			iSectionIds.addAll(sectionIds);
	}
	
	public Long getStudentId() { return iStudentId; }
	
	public Long getConfigId() { return iConfigId; }
	
	public Set<Long> getSectionIds() { return iSectionIds; }
	
	public Date getTimeStamp() { return iTimeStamp; }
	
	public XApproval getApproval() { return iApproval; }
	
	public void setApproval(XApproval approval) { iApproval = approval; }
	
	public XReservationId getReservation() { return iReservation; }

	public void setReservation(XReservationId reservation) {
		if (reservation == null)
			iReservation = null;
		else if (reservation instanceof XReservation)
			iReservation = new XReservationId(reservation);
		else
			iReservation = reservation;
	}
	
	public void setTimeStamp(Date ts) { iTimeStamp = ts; }
	
	public float getCredit(OnlineSectioningServer server) {
		XOffering offering = server.getOffering(getOfferingId());
		if (offering == null) return 0f;
		Float sectionCredit = null;
		for (Long sectionId: getSectionIds()) {
			XSection section = offering.getSection(sectionId);
			Float credit = (section == null ? null : section.getCreditOverride(getCourseId()));
			if (credit != null) {
				sectionCredit = (sectionCredit == null ? 0f : sectionCredit.floatValue()) + credit;
			}
		}
		if (sectionCredit != null) return sectionCredit;
		XCourse course = offering.getCourse(getCourseId());
		if (course != null && course.hasCredit())
			return course.getMinCredit();
		return 0f;
	}
	
	public boolean isRequired(CourseRequestInterface.RequestedCourse request, XOffering offering) {
		if (!request.hasSelectedClasses() && !request.hasSelectedIntructionalMethods()) return true;
		if (!getOfferingId().equals(offering.getOfferingId())) return true;
        XConfig config = offering.getConfig(getConfigId());
        // check all sections
        for (XSection section: offering.getSections(this)) {
            boolean hasConfig = false, hasMatchingConfig = false;
            boolean hasSubpart = false, hasMatchingSection = false;
            boolean hasSectionReq = false;
            
            if (request.hasSelectedIntructionalMethods()) {
            	for (CourseRequestInterface.Preference choice: request.getSelectedIntructionalMethods()) {
                	// only check required choices
            		if (!choice.isRequired()) continue;
                    // has config -> check config
            		hasConfig = true;
            		if (config.getInstructionalMethod() != null && choice.getId().equals(config.getInstructionalMethod().getUniqueId()))
            			hasMatchingConfig = true;
            	}
            }
            
            if (request.hasSelectedClasses()) {
            	for (CourseRequestInterface.Preference choice: request.getSelectedClasses()) {
                	// only check required choices
            		if (!choice.isRequired()) continue;
            		XSection reqSection = offering.getSection(choice.getId());
                    hasSectionReq = true;
                    // has section of the matching subpart -> check section
                    if (reqSection.getSubpartId().equals(section.getSubpartId())) {
                        hasSubpart = true;
                        if (reqSection.equals(section)) hasMatchingSection = true;
                    } else if (!hasMatchingConfig) {
                        for (XSubpart subpart: config.getSubparts()) {
                            if (reqSection.getSubpartId().equals(subpart.getSubpartId())) {
                                hasMatchingConfig = true;
                                break;
                            }
                        }
                    }
            	}
            }

            if (hasConfig && !hasMatchingConfig) return false;
            if (hasSubpart && !hasMatchingSection) return false;
            // no match, but there are section requirements for a different config -> not satisfied 
            if (!hasMatchingConfig && !hasMatchingSection && hasSectionReq) return false;
        }
        return true;
    }

	@Override
	public int compareTo(XCourseId courseId) {
		if (courseId instanceof XEnrollment) {
			XEnrollment enrollment = (XEnrollment)courseId;
			int cmp = getTimeStamp().compareTo(enrollment.getTimeStamp());
			if (cmp != 0) return cmp;
			cmp = getStudentId().compareTo(enrollment.getStudentId());
			if (cmp != 0) return cmp;
		}
		return super.compareTo(courseId);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof XEnrollment)) {
			if (o instanceof XCourseId)
				return ((XCourseId)o).equals(this);
			return false;
		}
		XEnrollment e = (XEnrollment)o;
		return getCourseId().equals(e.getCourseId()) && getConfigId().equals(e.getConfigId()) && getSectionIds().equals(e.getSectionIds());
	}
	
	@Override
	public String toString() {
		return getCourseName() + "/" + getSectionIds() + (getApproval() != null ? getReservation() != null ? " (ar)" : " (a)" : getReservation() != null ? " (r)" : ""); 
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		iStudentId = in.readLong();
		iConfigId = in.readLong();
		
		iSectionIds.clear();
		int nrSections = in.readInt();
		for (int i = 0; i < nrSections; i++)
			iSectionIds.add(in.readLong());
		
		iTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
		iApproval = (in.readBoolean() ? new XApproval(in) : null);
		iReservation = (in.readBoolean() ? new XReservationId(in) : null);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeLong(iStudentId);
		out.writeLong(iConfigId);
		
		out.writeInt(iSectionIds.size());
		for (Long sectionId: iSectionIds)
			out.writeLong(sectionId);
		
		out.writeBoolean(iTimeStamp != null);
		if (iTimeStamp != null)
			out.writeLong(iTimeStamp.getTime());
		
		out.writeBoolean(iApproval != null);
		if (iApproval != null)
			iApproval.writeExternal(out);
		
		out.writeBoolean(iReservation != null);
		if (iReservation != null)
			iReservation.writeExternal(out);
	}
}
