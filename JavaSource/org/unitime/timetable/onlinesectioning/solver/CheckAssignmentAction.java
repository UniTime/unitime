/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;

public class CheckAssignmentAction implements OnlineSectioningAction<Map<Long, List<XSection>>>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Long iStudentId;
	private Collection<ClassAssignmentInterface.ClassAssignment> iAssignment;
	
	public CheckAssignmentAction(Long studentId, Collection<ClassAssignmentInterface.ClassAssignment> assignment) {
		iStudentId = studentId;
		iAssignment = assignment;
	}
	
	public Long getStudentId() { return iStudentId; }
	public Collection<ClassAssignmentInterface.ClassAssignment> getAssignment() { return iAssignment; }

	@Override
	public Map<Long, List<XSection>> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock readLock = server.readLock();
		try {
			Set<Long> offeringIds = new HashSet<Long>();
			for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
				if (ca != null && !ca.isFreeTime()) {
					XCourse course = server.getCourse(ca.getCourseId());
					if (course != null) offeringIds.add(course.getOfferingId());
				}
			
			Lock lock = server.lockStudent(getStudentId(), offeringIds, false);
			try {
				return check(server, helper);
			} finally {
				lock.release();
			}
		} finally {
			readLock.release();
		}
	}
	
	public Map<Long, List<XSection>> check(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		XStudent student = server.getStudent(getStudentId());
		if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
		Hashtable<Long, XCourse> config2course = new Hashtable<Long, XCourse>();
		Hashtable<Long, XOffering> config2offering = new Hashtable<Long, XOffering>();
		Hashtable<Long, List<XSection>> config2sections = new Hashtable<Long, List<XSection>>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
			// Skip free times
			if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
			
			XCourse ci = server.getCourse(ca.getCourseId());
			if (ci == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(MSG.courseName(ca.getSubject(), ca.getClassNumber())));
			XOffering offering = server.getOffering(ci.getOfferingId());
			if (offering == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(MSG.courseName(ca.getSubject(), ca.getClassNumber())));
			
			// Check section limits
			XSection section = offering.getSection(ca.getClassId());
			if (section == null)
				throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
			
			XSubpart subpart = offering.getSubpart(section.getSubpartId());
			
			List<XSection> sections = config2sections.get(subpart.getConfigId());
			if (sections == null) {
				sections = new ArrayList<XSection>();
				config2sections.put(subpart.getConfigId(), sections);
				XCourse course = offering.getCourse(ca.getCourseId());
				if (course == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				config2course.put(subpart.getConfigId(), course);
				config2offering.put(subpart.getConfigId(), offering);
			}
			sections.add(section);
		}
		
		// Check for NEW and CHANGE deadlines
		check: for (Map.Entry<Long, List<XSection>> entry: config2sections.entrySet()) {
			XCourse course = config2course.get(entry.getKey());
			List<XSection> sections = entry.getValue();

			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
					if (enrollment != null && enrollment.getCourseId().equals(course.getCourseId())) { // course change
						for (XSection s: sections)
							if (!enrollment.getSectionIds().contains(s.getSectionId()) && !server.checkDeadline(course.getCourseId(), s.getTime(), OnlineSectioningServer.Deadline.CHANGE))
								throw new SectioningException(MSG.exceptionEnrollDeadlineChange(MSG.courseName(course.getSubjectArea(), course.getCourseNumber())));
						continue check;
					}
				}
			}
			
			// new course
			for (XSection section: sections) {
				if (!server.checkDeadline(course.getOfferingId(), section.getTime(), OnlineSectioningServer.Deadline.NEW))
					throw new SectioningException(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()));
			}
		}
		
		// Check for DROP deadlines
		for (XRequest r: student.getRequests()) {
			if (r instanceof XCourseRequest) {
				XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
				if (enrollment != null && !config2sections.containsKey(enrollment.getConfigId())) {
					XOffering offering = server.getOffering(enrollment.getOfferingId());
					if (offering != null)
						for (XSection section: offering.getSections(enrollment)) {
							if (!server.checkDeadline(offering.getOfferingId(), section.getTime(), OnlineSectioningServer.Deadline.DROP))
								throw new SectioningException(MSG.exceptionEnrollDeadlineDrop(enrollment.getCourseName()));
						}
				}
			}
		}
		
		for (Map.Entry<Long, List<XSection>> entry: config2sections.entrySet()) {
			XOffering offering = config2offering.get(entry.getKey());
			XConfig config = offering.getConfig(entry.getKey());
			XCourse course = config2course.get(entry.getKey());
			XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
			List<XSection> sections = entry.getValue();

			XReservation reservation = null;
			reservations: for (XReservation r: offering.getReservations()) {
				if (!r.isApplicable(student)) continue;
				if (r.getLimit() >= 0 && r.getLimit() <= enrollments.countEnrollmentsForReservation(r.getReservationId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForReservation(r.getReservationId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain) continue;
				}
				if (!r.getConfigsIds().isEmpty() && !r.getConfigsIds().contains(entry.getKey())) continue;
				for (XSection section: sections)
					if (r.getSectionIds(section.getSubpartId()) != null && !r.getSectionIds(section.getSubpartId()).contains(section.getSectionId())) continue reservations;
				if (reservation == null || r.compareTo(reservation) < 0)
					reservation = r;
			}
			
			if (reservation == null || !reservation.canAssignOverLimit()) {
				for (XSection section: sections) {
					if (section.getLimit() >= 0 && section.getLimit() <= enrollments.countEnrollmentsForSection(section.getSectionId())) {
						boolean contain = false;
						for (XEnrollment e: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
						if (!contain)
							throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName())));
					}
					if ((reservation == null || !offering.getSectionReservations(section.getSectionId()).contains(reservation)) && offering.getUnreservedSectionSpace(section.getSectionId(), enrollments) <= 0) {
						boolean contain = false;
						for (XEnrollment e: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
						if (!contain)
							throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName())));
					}
				}
				
				if (config.getLimit() >= 0 && config.getLimit() <= enrollments.countEnrollmentsForConfig(config.getConfigId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForConfig(config.getConfigId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.courseName(course.getSubjectArea(), course.getCourseNumber())) + " " + config.getName());
				}
				if ((reservation == null || !offering.getConfigReservations(config.getConfigId()).contains(reservation)) && offering.getUnreservedConfigSpace(config.getConfigId(), enrollments) <= 0) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForConfig(config.getConfigId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.courseName(course.getSubjectArea(), course.getCourseNumber())) + " " + config.getName());
				}
				
				if (course.getLimit() >= 0 && course.getLimit() <= enrollments.countEnrollmentsForCourse(course.getCourseId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForCourse(course.getCourseId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.courseName(course.getSubjectArea(), course.getCourseNumber())));
				}
			}
		}
		
		return config2sections;
	}

	@Override
	public String name() {
		return "check-assignment";
	}
}
