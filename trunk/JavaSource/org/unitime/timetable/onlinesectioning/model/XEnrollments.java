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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.marshall.Externalizer;

/**
 * @author Tomas Muller
 */
public class XEnrollments implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	
	private Long iOfferingId;
	private List<XCourseRequest> iRequests = new ArrayList<XCourseRequest>();
	private List<XEnrollment> iEnrollments = null;
	private Map<Long, List<XEnrollment>> iConfig2Enrl = null;
	private Map<Long, List<XEnrollment>> iCourse2Enrl = null;
	private Map<Long, List<XEnrollment>> iSection2Enrl = null;
	private Map<Long, List<XEnrollment>> iReservation2Enrl = null;
	
	public XEnrollments() {}
	
	public XEnrollments(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XEnrollments(Long offeringId, Collection<XCourseRequest> requests) {
		iOfferingId = offeringId;
		if (requests != null)
			iRequests.addAll(requests);
		init();
	}
	
	private void init() {
		iEnrollments = new ArrayList<XEnrollment>();
		iConfig2Enrl = new HashMap<Long, List<XEnrollment>>();
		iCourse2Enrl = new HashMap<Long, List<XEnrollment>>();
		iSection2Enrl = new HashMap<Long, List<XEnrollment>>();
		iReservation2Enrl = new HashMap<Long, List<XEnrollment>>();
		for (XCourseRequest request: iRequests) {
			XEnrollment enrollment = request.getEnrollment();
			if (enrollment != null && enrollment.getOfferingId().equals(iOfferingId)) {
				iEnrollments.add(enrollment);
				
				List<XEnrollment> cfgEnrl = iConfig2Enrl.get(enrollment.getConfigId());
				if (cfgEnrl == null) {
					cfgEnrl = new ArrayList<XEnrollment>();
					iConfig2Enrl.put(enrollment.getConfigId(), cfgEnrl);
				}
				cfgEnrl.add(enrollment);
				
				List<XEnrollment> coEnrl = iCourse2Enrl.get(enrollment.getCourseId());
				if (coEnrl == null) {
					coEnrl = new ArrayList<XEnrollment>();
					iCourse2Enrl.put(enrollment.getCourseId(), coEnrl);
				}
				coEnrl.add(enrollment);
				
				if (enrollment.getReservation() != null) {
					List<XEnrollment> resEnrl = iReservation2Enrl.get(enrollment.getReservation().getReservationId());
					if (resEnrl == null) {
						resEnrl = new ArrayList<XEnrollment>();
						iReservation2Enrl.put(enrollment.getReservation().getReservationId(), resEnrl);
					}
					resEnrl.add(enrollment);
				}
				
				for (Long sectionId: enrollment.getSectionIds()) {
					List<XEnrollment> enrl = iSection2Enrl.get(sectionId);
					if (enrl == null) {
						enrl = new ArrayList<XEnrollment>();
						iSection2Enrl.put(sectionId, enrl);
					}
					enrl.add(enrollment);
				}
			}
		}
	}
	
	public List<XCourseRequest> getRequests() {
		return iRequests;
	}
	
	public List<XEnrollment> getEnrollments() {
		return iEnrollments;
	}
	
	public int countEnrollments() {
		return iEnrollments == null ? 0 : iEnrollments.size();
	}

	public List<XEnrollment> getEnrollmentsForSection(Long sectionId) {
		List<XEnrollment> ret = iSection2Enrl.get(sectionId);
		return ret == null ? new ArrayList<XEnrollment>() : ret;
	}

	public List<XEnrollment> getEnrollmentsForCourse(Long courseId) {
		List<XEnrollment> ret = iCourse2Enrl.get(courseId);
		return ret == null ? new ArrayList<XEnrollment>() : ret;
	}

	public List<XEnrollment> getEnrollmentsForConfig(Long configId) {
		List<XEnrollment> ret = iConfig2Enrl.get(configId);
		return ret == null ? new ArrayList<XEnrollment>() : ret;
	}

	public List<XEnrollment> getEnrollmentsForReservation(Long reservationId) {
		List<XEnrollment> ret = iReservation2Enrl.get(reservationId);
		return ret == null ? new ArrayList<XEnrollment>() : ret;
	}

	public int countEnrollmentsForSection(Long sectionId) {
		List<XEnrollment> ret = iSection2Enrl.get(sectionId);
		return ret == null ? 0 : ret.size();
	}
	
	public int countEnrollmentsForCourse(Long courseId) {
		List<XEnrollment> ret = iCourse2Enrl.get(courseId);
		return ret == null ? 0 : ret.size();
	}

	public int countEnrollmentsForConfig(Long configId) {
		List<XEnrollment> ret = iConfig2Enrl.get(configId);
		return ret == null ? 0 : ret.size();
	}
	
	public int countEnrollmentsForReservation(Long reservationId) {
		List<XEnrollment> ret = iReservation2Enrl.get(reservationId);
		return ret == null ? 0 : ret.size();
	}
	
	private boolean contain(List<XEnrollment> enrollments, Long studentId) {
		if (studentId == null || enrollments == null) return false;
		for (XEnrollment e: enrollments)
			if (e.getStudentId().equals(studentId)) return true;
		return false;
	}
	
	public int countEnrollmentsForSection(Long sectionId, Long excludeStudentId) {
		List<XEnrollment> ret = iSection2Enrl.get(sectionId);
		return ret == null ? 0 : contain(ret, excludeStudentId) ? ret.size() - 1 : ret.size();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iOfferingId = in.readLong();
		
		int nrRequests = in.readInt();
		iRequests.clear();
		for (int i = 0; i < nrRequests; i++)
			iRequests.add(new XCourseRequest(in));

		init();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iOfferingId);
		out.writeInt(iRequests.size());
		for (XCourseRequest request: iRequests)
			request.writeExternal(out);
	}

	public static class XEnrollmentsSerializer implements Externalizer<XEnrollments> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XEnrollments object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XEnrollments readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XEnrollments(input);
		}
	}

}
