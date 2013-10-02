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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XEnrollments implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<XCourseRequest> iRequests = new ArrayList<XCourseRequest>();
	private List<XEnrollment> iEnrollments = new ArrayList<XEnrollment>();
	private Map<Long, List<XEnrollment>> iConfig2Enrl = new HashMap<Long, List<XEnrollment>>();
	private Map<Long, List<XEnrollment>> iCourse2Enrl = new HashMap<Long, List<XEnrollment>>();
	private Map<Long, List<XEnrollment>> iSection2Enrl = new HashMap<Long, List<XEnrollment>>();
	private Map<Long, List<XEnrollment>> iReservation2Enrl = new HashMap<Long, List<XEnrollment>>();
	
	public XEnrollments(Long offeringId, Collection<XCourseRequest> requests) {
		if (requests != null)
			for (XCourseRequest request: requests) {
				XEnrollment enrollment = request.getEnrollment();
				if (enrollment == null) {
					iRequests.add(request);
				} else if (enrollment.getOfferingId().equals(offeringId)) {
					iRequests.add(request);
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


}
