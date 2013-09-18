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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningService;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class ListClasses implements OnlineSectioningAction<Collection<ClassAssignmentInterface.ClassAssignment>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private String iCourse;
	private Long iStudentId;
	
	public ListClasses(String course, Long studentId) {
		iCourse = course;
		iStudentId = studentId;
	}
	
	public String getCourse() {
		return iCourse;
	}
	
	public Long getStudentId() {
		return iStudentId;
	}

	@Override
	public Collection<ClassAssignment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		Lock lock = server.readLock();
		try {
			CourseInfo c = server.getCourseInfo(getCourse());
			if (c == null) throw new SectioningException(MSG.exceptionCourseDoesNotExist(getCourse()));
			List<Section> sections = server.getSections(c);
			Collections.sort(sections, new Comparator<Section>() {
				public int compare(Config c1, Config c2) {
					int cmp = c1.getName().compareToIgnoreCase(c2.getName());
					if (cmp != 0) return cmp;
					return Double.compare(c1.getId(), c2.getId());
				}
				public boolean isParent(Subpart s1, Subpart s2) {
					Subpart p1 = s1.getParent();
					if (p1==null) return false;
					if (p1.equals(s2)) return true;
					return isParent(p1, s2);
				}
				public int compare(Subpart s1, Subpart s2) {
					int cmp = compare(s1.getConfig(), s2.getConfig());
					if (cmp != 0) return cmp;
			        if (isParent(s1,s2)) return 1;
			        if (isParent(s2,s1)) return -1;
			        cmp = s1.getInstructionalType().compareTo(s2.getInstructionalType());
			        if (cmp != 0) return cmp;
			        return Double.compare(s1.getId(), s2.getId());
				}
				public int compare(Section s1, Section s2) {
					int cmp = compare(s1.getSubpart(), s2.getSubpart());
					if (cmp != 0) return cmp;
					cmp = (s1.getName() == null ? "" : s1.getName()).compareTo(s2.getName() == null ? "" : s2.getName());
					if (cmp != 0) return cmp;
			        return Double.compare(s1.getId(), s2.getId());
				}
			});
			Map<Long, int[]> limits = null;
			if (OnlineSectioningService.sSectionLimitProvider != null) {
				limits = OnlineSectioningService.sSectionLimitProvider.getSectionLimits(server.getAcademicSession(), c.getUniqueId(), sections);
			}
			ClassAssignmentInterface.CourseAssignment courseAssign = new ClassAssignmentInterface.CourseAssignment();
			courseAssign.setCourseId(c.getUniqueId());
			courseAssign.setCourseNbr(c.getCourseNbr());
			courseAssign.setSubject(c.getSubjectArea());
			for (Section section: sections) {
				if (section.getLimit() == 0) continue;
				String room = null;
				if (section.getRooms() != null) {
					for (RoomLocation rm: section.getRooms()) {
						if (room == null) room = ""; else room += ", ";
						room += rm.getName();
					}
				}
				int[] limit = (limits == null ? new int[] { section.getEnrollments().size(), section.getLimit()} : limits.get(section.getId()));
				ClassAssignmentInterface.ClassAssignment a = courseAssign.addClassAssignment();
				a.setClassId(section.getId());
				a.setSubpart(section.getSubpart().getName());
				a.setSection(section.getName(c.getUniqueId()));
				a.setClassNumber(section.getName(-1l));
				a.setLimit(limit);
				if (getStudentId() != null) {
					for (Iterator<Enrollment> i = section.getEnrollments().iterator(); i.hasNext();) {
						Enrollment enrollment = i.next();
						if (enrollment.getStudent().getId() == getStudentId()) { a.setSaved(true); break; }
					}
				}
				a.addNote(c.getNote());
				a.addNote(section.getNote());
				a.setCredit(section.getSubpart().getCredit());
				if (section.getTime() != null) {
					for (DayCode d: DayCode.toDayCodes(section.getTime().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(section.getTime().getStartSlot());
					a.setLength(section.getTime().getLength());
					a.setBreakTime(section.getTime().getBreakTime());
					a.setDatePattern(section.getTime().getDatePatternName());
				}
				if (section.getRooms() != null) {
					for (RoomLocation rm: section.getRooms()) {
						a.addRoom(rm.getName());
					}
				}
				if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
					String[] instructors = section.getChoice().getInstructorNames().split(":");
					for (String instructor: instructors) {
						String[] nameEmail = instructor.split("\\|");
						a.addInstructor(nameEmail[0]);
						a.addInstructoEmailr(nameEmail.length < 2 ? "" : nameEmail[1]);
					}
				}
				if (section.getParent() != null)
					a.setParentSection(section.getParent().getName(c.getUniqueId()));
				a.setSubpartId(section.getSubpart().getId());
				if (a.getParentSection() == null)
					a.setParentSection(c.getConsent());
				a.setExpected(section.getSpaceExpected());
				ret.add(a);
			}
		} finally {
			lock.release();
		}
		if (ret.isEmpty())
			throw new SectioningException(MSG.exceptionNoClassesForCourse(getCourse()));
		return ret;
	}

	@Override
	public String name() {
		return "list-classes";
	}

}
