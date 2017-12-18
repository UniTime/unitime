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
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XEnrollment.XEnrollmentSerializer.class)
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
		super(enrollment.getOffering().getId(), enrollment.getCourse().getId(), enrollment.getCourse().getName());
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
	
	public static class XEnrollmentSerializer implements Externalizer<XEnrollment> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XEnrollment object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XEnrollment readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XEnrollment(input);
		}
	}
}
