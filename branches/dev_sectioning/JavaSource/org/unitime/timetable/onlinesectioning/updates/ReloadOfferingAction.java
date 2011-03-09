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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

/**
 * @author Tomas Muller
 */
public class ReloadOfferingAction implements OnlineSectioningAction<Boolean> {
	private List<Long> iOfferingIds;
	
	public ReloadOfferingAction(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
	}
	
	public ReloadOfferingAction(List<Long> offeringIds) {
		iOfferingIds = offeringIds;
	}
	
	public List<Long> getOfferingIds() { return iOfferingIds; }
	
	public Collection<Long> getCourseIds(OnlineSectioningServer server) {
		Set<Long> courseIds = new HashSet<Long>();
		for (Long offeringId: getOfferingIds()) {
			Offering offering = server.getOffering(offeringId);
			if (offering != null) {
				for (Course course: offering.getCourses())
					courseIds.add(course.getId());
			}
		}
		return courseIds;
	}

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		try {
			for (Long offeringId: getOfferingIds()) {
				List<Long> studentIds = (List<Long>)helper.getHibSession().createQuery(
						"select distinct e.student.uniqueId from StudentClassEnrollment e where "+
                		"e.courseOffering.instructionalOffering.uniqueId = :offeringId").setLong("offeringId", offeringId).list();
				Lock lock = server.lockOffering(offeringId, studentIds);
				try {

					reloadOffering(server, helper, offeringId, studentIds);
					
				} finally {
					lock.release();
				}
									
			}				
			helper.commitTransaction();
			return true;			
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		}
	}
		
	public void reloadOffering(OnlineSectioningServer server, OnlineSectioningHelper helper, Long offeringId, List<Long> newStudentIds) {
		// Existing offering
		Offering oldOffering = server.getOffering(offeringId);
		server.remove(oldOffering);
		
		// New offering
		Offering newOffering = null;
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(offeringId, helper.getHibSession());
		if (oldOffering != null) {
			newOffering = ReloadAllData.loadOffering(io, server, helper);
			server.update(newOffering);
			for (CourseOffering co: io.getCourseOfferings())
				server.update(new CourseInfo(co));
			
			// Load sectioning info
        	List<SectioningInfo> infos = helper.getHibSession().createQuery(
        			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId")
        			.setLong("offeringId", offeringId).list();
        	for (SectioningInfo info : infos) {
        		Section section = server.getSection(info.getClazz().getUniqueId());
        		if (section != null) {
        			section.setSpaceExpected(info.getNbrExpectedStudents());
        			section.setSpaceHeld(info.getNbrHoldingStudents());
        		}
        	}
		}
		
		List<Student[]> students = new ArrayList<Student[]>();
		
		if (oldOffering != null) {
			for (Course course: oldOffering.getCourses())
				for (CourseRequest request: server.getRequests(course.getId())) {
					Student oldStudent = request.getStudent();
					server.remove(oldStudent);
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(oldStudent.getId(), helper.getHibSession());
					Student newStudent = (student == null ? null : ReloadAllData.loadStudent(student, server, helper));
					if (newStudent != null)
						server.update(newStudent);
					students.add(new Student[] {oldStudent, newStudent});
					newStudentIds.remove(oldStudent.getId());
				}	
		}
		for (Long studentId: newStudentIds) {
			Student oldStudent = server.getStudent(studentId);
			if (oldStudent != null)
				server.remove(oldStudent);
			org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
			Student newStudent = (student == null ? null : ReloadAllData.loadStudent(student, server, helper));
			if (newStudent != null)
				server.update(newStudent);
			students.add(new Student[] {oldStudent, newStudent});
		}
		

		Set<Resection> queue = new TreeSet<Resection>();
				
		for (Student[] student: students) {
			if (student[0] == null && student[1] == null) continue;
			Enrollment oldEnrollment = null;
			if (student[0] != null) {
				for (Request r: student[0].getRequests())
					if (r.getInitialAssignment() != null && r.getInitialAssignment().getOffering() != null &&
						offeringId.equals(r.getInitialAssignment().getOffering().getId()))
						oldEnrollment = r.getInitialAssignment();
			}

			CourseRequest newRequest = null; 
			Enrollment newEnrollment = null;
			if (student[1] != null) {
				for (Request r: student[1].getRequests())
					if (r instanceof CourseRequest) {
						CourseRequest cr = (CourseRequest)r;
						for (Course course: cr.getCourses())
							if (offeringId.equals(course.getOffering().getId())) {
								newRequest = cr;
								if (cr.getInitialAssignment() != null && offeringId.equals(cr.getInitialAssignment().getOffering().getId()))
									newEnrollment = cr.getInitialAssignment();
								break;
							}
					}
			}
			if (newRequest == null) {
				// nothing to re-assign
				server.notifyStudentChanged(student[0] == null ? student[1].getId() : student[0].getId(), student[0].getRequests(), student[1].getRequests());
				continue;
			}
			
			if (oldEnrollment == null) {
				if (newRequest.isWaitlist())
					queue.add(new Resection(newRequest, null));
				continue;
			}
			
			if (newEnrollment != null) {
				// new enrollment is valid and / or has all the same times
				if (check(newEnrollment) || isSame(oldEnrollment, newEnrollment)) {
					server.notifyStudentChanged(student[0] == null ? student[1].getId() : student[0].getId(), student[0].getRequests(), student[1].getRequests());
					continue;
				}
				newEnrollment.variable().unassign(0);
			}
			queue.add(new Resection(newRequest, oldEnrollment));
		}
		
		for (Resection r: queue) {
			resection(r);
			if (r.getRequest().getAssignment() != null) {
				// save enrollment s
			} else {
				// waitlist student
			}
			server.notifyStudentChanged(r.getRequest().getStudent().getId(),
					r.getLastEnrollment() == null ? null : r.getLastEnrollment().getStudent().getRequests(),
					r.getRequest().getStudent().getRequests());
		}
		
	}
	
	private boolean sameRooms(Section s1, Section s2) {
		if (s1.getPlacement() == null && s2.getPlacement() == null) return true;
		if (s1.getPlacement() == null || s2.getPlacement() == null) return false;
		return s1.getPlacement().sameRooms(s2.getPlacement());
	}
	
	private boolean sameTime(Section s1, Section s2) {
		if (s1.getPlacement() == null && s2.getPlacement() == null) return true;
		if (s1.getPlacement() == null || s2.getPlacement() == null) return false;
		return s1.getPlacement().sameTime(s2.getPlacement());
	}
	
	private boolean sameChoice(Section s1, Section s2) {
		if (s1.getChoice() == null && s2.getChoice() == null) return true;
		if (s1.getChoice() == null || s2.getChoice() == null) return false;
		return s1.getChoice().equals(s2.getChoice());
	}

	private boolean isSame(Enrollment e1, Enrollment e2) {
		if (e1.getSections().size() != e2.getSections().size()) return false;
		s1: for (Section s1: e1.getSections()) {
			for (Section s2: e2.getSections())
				if (sameChoice(s1, s2)) continue s1;
			return false;
		}
		return true;
	}
	
	private boolean check(Enrollment e) {
		if (e.getSections().size() != e.getConfig().getSubparts().size()) return false;
		for (Section s1: e.getSections())
			for (Section s2: e.getSections())
				if (s1.getId() < s2.getId() && s1.isOverlapping(s2)) return false;
		for (Request r: e.getStudent().getRequests()) {
			if (r.getId() != e.getRequest().getId() && r.getInitialAssignment() != null && r.getInitialAssignment().isOverlapping(e)) return false;
		}
		return true;
	}
	
	private void resection(Resection rx) {
		Enrollment enrollment = null;
		int bonus = 0;
		for (Enrollment e: rx.getRequest().getAvaiableEnrollments()) {
			int points = rx.evaluate(enrollment); 
			if (enrollment == null || points > bonus) {
				enrollment = e; bonus = points;
			}
		}
		if (enrollment != null) {
			rx.getRequest().setInitialAssignment(enrollment);
			rx.getRequest().assign(0, enrollment);
		}
	}

	private class Resection implements Comparable<Resection> {
		CourseRequest iRequest;
		Enrollment iLastEnrollment;
		
		public Resection(CourseRequest request, Enrollment lastEnrollment) {
			iRequest = request; iLastEnrollment = lastEnrollment;
		}
		
		public CourseRequest getRequest() { return iRequest; }
		public Enrollment getLastEnrollment() { return iLastEnrollment; }
		
		public int hashCode() { return new Long(getRequest().getStudent().getId()).hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Resection)) return false;
			return getRequest().getStudent().getId() == ((Resection)o).getRequest().getStudent().getId();
		}
		
		public int compareTo(Resection r) {
			// Requests with last enrollment (recently unassigned requests) have priority
			if (getLastEnrollment() == null && r.getLastEnrollment() != null) return 1;
			if (getLastEnrollment() != null && r.getLastEnrollment() == null) return -1;
			
			// Use priority
			int cmp = new Integer(getRequest().getPriority()).compareTo(r.getRequest().getPriority());
			if (cmp != 0) return cmp;
			
			cmp = (getRequest().isWaitlist() ? getRequest().getWaitListTimeStamp() : new Long(Long.MAX_VALUE)).compareTo(
					(r.getRequest().isWaitlist() ? r.getRequest().getWaitListTimeStamp() : Long.MAX_VALUE));
			if (cmp != 0) return cmp;
			
			// TODO use timestamp and reservations
			return new Long(getRequest().getId()).compareTo(r.getRequest().getId());
		}
		
		public int evaluate(Enrollment e) {
			int points = 0;
			if (getLastEnrollment() != null) {
				for (Section s1: getLastEnrollment().getSections()) {
					for (Section s2: e.getSections()) {
						if (sameChoice(s1, s2)) points += 10;
						if (sameTime(s1, s2)) points += 5;
						if (sameRooms(s1, s2)) points += 1;
						if (s1.getName().equals(s2.getName())) points += 3;
					}
				}
			}
			return points;
		}
	}
		
	@Override
    public String name() { return "reload-offering"; }
}
