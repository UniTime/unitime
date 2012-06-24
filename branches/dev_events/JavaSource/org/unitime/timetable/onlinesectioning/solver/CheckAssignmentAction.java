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

import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.reservation.Reservation;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class CheckAssignmentAction implements OnlineSectioningAction<Map<Config, List<Section>>>{
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
	public Map<Config, List<Section>> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock readLock = server.readLock();
		try {
			Set<Long> offeringIds = new HashSet<Long>();
			for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
				if (ca != null && !ca.isFreeTime()) {
					Course course = server.getCourse(ca.getCourseId());
					if (course != null) offeringIds.add(course.getOffering().getId());
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
	
	public Map<Config, List<Section>> check(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Student student = server.getStudent(getStudentId());
		if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());
		Hashtable<Config, Course> config2course = new Hashtable<Config, Course>();
		Hashtable<Config, List<Section>> config2sections = new Hashtable<Config, List<Section>>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
			// Skip free times
			if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
			
			// Check section limits
			Section section = server.getSection(ca.getClassId());
			if (section == null)
				throw new SectioningException(MSG.exceptionEnrollNotAvailable(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection()));
			
			Config config = section.getSubpart().getConfig();
			List<Section> sections = config2sections.get(config);
			if (sections == null) {
				sections = new ArrayList<Section>();
				config2sections.put(config, sections);
				Course course = null;
				for (Course cx: config.getOffering().getCourses()) {
					if (cx.getId() == ca.getCourseId()) { course = cx; break; }
				}
				if (course == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection()));
				config2course.put(config, course);
			}
			sections.add(section);
		}
		
		// Check for NEW and CHANGE deadlines
		check: for (Map.Entry<Config, List<Section>> entry: config2sections.entrySet()) {
			Config config = entry.getKey();
			Course course = config2course.get(config);
			List<Section> sections = entry.getValue();

			for (Request r: student.getRequests()) {
				if (r.getAssignment() != null && course.equals(r.getAssignment().getCourse())) { // course change
					for (Section s: sections)
						if (!r.getAssignment().getSections().contains(s)) {
							if (!server.checkDeadline(s, OnlineSectioningServer.Deadline.CHANGE))
								throw new SectioningException(MSG.exceptionEnrollDeadlineChange(course.getSubjectArea() + " " + course.getCourseNumber() + " " + s.getSubpart().getName() + " " + s.getName(course.getId())));
						}
					continue check;
				}
			}
			
			// new course
			for (Section s: sections) {
				if (!server.checkDeadline(s, OnlineSectioningServer.Deadline.NEW))
					throw new SectioningException(MSG.exceptionEnrollDeadlineNew(course.getSubjectArea() + " " + course.getCourseNumber() + " " + s.getSubpart().getName() + " " + s.getName(course.getId())));
			}
		}
		
		// Check for DROP deadlines
		for (Request r: student.getRequests()) {
			if (r.getAssignment() != null && r.getAssignment().getCourse() != null && !r.getAssignment().getCourse().equals(config2course.get(r.getAssignment().getConfig()))) {
				Course course = r.getAssignment().getCourse(); // course dropped
				for (Section s: r.getAssignment().getSections()) {
					if (!server.checkDeadline(s, OnlineSectioningServer.Deadline.DROP))
						throw new SectioningException(MSG.exceptionEnrollDeadlineDrop(course.getSubjectArea() + " " + course.getCourseNumber() + " " + s.getSubpart().getName() + " " + s.getName(course.getId())));
				}
			}
		}
		
		for (Map.Entry<Config, List<Section>> entry: config2sections.entrySet()) {
			Config config = entry.getKey();
			Course course = config2course.get(config);
			List<Section> sections = entry.getValue();

			Reservation reservation = null;
			reservations: for (Reservation r: course.getOffering().getReservations()) {
				if (!r.isApplicable(student)) continue;
				if (r.getLimit() >= 0 && r.getLimit() <= r.getEnrollments().size()) {
					boolean contain = false;
					for (Enrollment e: r.getEnrollments())
						if (e.getStudent().getId() == student.getId()) { contain = true; break; }
					if (!contain) continue;
				}
				if (!r.getConfigs().isEmpty() && !r.getConfigs().contains(config)) continue;
				for (Section section: sections)
					if (r.getSections(section.getSubpart()) != null && !r.getSections(section.getSubpart()).contains(section)) continue reservations;
				if (reservation == null || r.compareTo(reservation) < 0)
					reservation = r;
			}
			
			if (reservation == null || !reservation.canAssignOverLimit()) {
				for (Section section: sections) {
					if (section.getLimit() >= 0 && section.getLimit() <= section.getEnrollments().size()) {
						boolean contain = false;
						for (Enrollment e: section.getEnrollments())
							if (e.getStudent().getId() == student.getId()) { contain = true; break; }
						if (!contain)
							throw new SectioningException(MSG.exceptionEnrollNotAvailable(course.getSubjectArea() + " " + course.getCourseNumber() + " " + section.getSubpart().getName() + " " + section.getName()));
					}
					if ((reservation == null || !section.getSectionReservations().contains(reservation)) && section.getUnreservedSpace(null) <= 0) {
						boolean contain = false;
						for (Enrollment e: section.getEnrollments())
							if (e.getStudent().getId() == student.getId()) { contain = true; break; }
						if (!contain)
							throw new SectioningException(MSG.exceptionEnrollNotAvailable(course.getSubjectArea() + " " + course.getCourseNumber() + " " + section.getSubpart().getName() + " " + section.getName()));
					}
				}
				
				if (config.getLimit() >= 0 && config.getLimit() <= config.getEnrollments().size()) {
					boolean contain = false;
					for (Enrollment e: config.getEnrollments())
						if (e.getStudent().getId() == student.getId()) { contain = true; break; }
					if (!contain)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(course.getSubjectArea() + " " + course.getCourseNumber() + " " + config.getName()));
				}
				if ((reservation == null || !config.getConfigReservations().contains(reservation)) && config.getUnreservedSpace(null) <= 0) {
					boolean contain = false;
					for (Enrollment e: config.getEnrollments())
						if (e.getStudent().getId() == student.getId()) { contain = true; break; }
					if (!contain)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(course.getSubjectArea() + " " + course.getCourseNumber() + " " + config.getName()));
				}
				
				if (course.getLimit() >= 0 && course.getLimit() <= course.getEnrollments().size()) {
					boolean contain = false;
					for (Enrollment e: course.getEnrollments())
						if (e.getStudent().getId() == student.getId()) { contain = true; break; }
					if (!contain)
						throw new SectioningException(MSG.exceptionEnrollNotAvailable(course.getSubjectArea() + " " + course.getCourseNumber()));
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
