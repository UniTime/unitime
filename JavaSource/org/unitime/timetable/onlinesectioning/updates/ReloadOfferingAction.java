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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.studentsct.model.Choice;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
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
						"select distinct s.uniqueId from Student s " +
						"left outer join s.classEnrollments e " +
						"left outer join s.courseDemands d inner join d.courseRequests r " +
						"where e.courseOffering.instructionalOffering.uniqueId = :offeringId or " +
						"r.courseOffering.instructionalOffering.uniqueId = :offeringId").setLong("offeringId", offeringId).list();
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
				for (CourseRequest request: new ArrayList<CourseRequest>(course.getRequests())) {
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
				if (newRequest.getStudent().canAssign(newRequest))
					queue.add(new Resection(newRequest, student[0], null));
				continue;
			}
			
			if (newEnrollment != null) {
				// new enrollment is valid and / or has all the same times
				if (check(newEnrollment)) {// || isSame(oldEnrollment, newEnrollment)) {
					if (!isVerySame(newEnrollment, oldEnrollment))
						server.notifyStudentChanged(student[0] == null ? student[1].getId() : student[0].getId(), student[0].getRequests(), student[1].getRequests());
					continue;
				}
				newRequest.getSelectedChoices().clear();
				for (Section s: newEnrollment.getSections())
					newRequest.getSelectedChoices().add(s.getChoice());
			}
			if (newRequest.getAssignment() != null)
				newRequest.unassign(0);
			if (newRequest.getInitialAssignment() != null)
				newRequest.setInitialAssignment(null);
			queue.add(new Resection(newRequest, student[0], oldEnrollment));
		}
		
		Date ts = new Date();
		for (Resection r: queue) {
			helper.info("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getAssignments()) + ")");
			resection(r);
			helper.info("New: " + (r.getRequest().getAssignment() == null ? "not assigned" : r.getRequest().getAssignment().getAssignments()));
			
			org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudent().getId(), helper.getHibSession());
			for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
				StudentClassEnrollment enrl = i.next();
				if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest().getId())) ||
					(enrl.getCourseOffering() != null && enrl.getCourseOffering().getInstructionalOffering().getUniqueId().equals(offeringId))) {
					helper.info("Deleting " + enrl.getClazz().getClassLabel());
					enrl.getClazz().getStudentEnrollments().remove(enrl);
					if (enrl.getCourseRequest() != null)
						enrl.getCourseRequest().getClassEnrollments().remove(enrl);
					helper.getHibSession().delete(enrl);
					i.remove();
				}
			}
			CourseDemand cd = null;
			demands: for (CourseDemand x: student.getCourseDemands())
				for (org.unitime.timetable.model.CourseRequest q: x.getCourseRequests())
					if (q.getCourseOffering().getInstructionalOffering().getUniqueId().equals(offeringId)) {
						cd = x;
						break demands;
					}
			
			if (r.getRequest().getAssignment() != null) { // save enrollment
				org.unitime.timetable.model.CourseRequest cr = null;
				CourseOffering co = null;
				if (cr != null)
					co = cr.getCourseOffering();
				if (co == null) 
					for (CourseOffering x: io.getCourseOfferings())
						if (x.getUniqueId().equals(r.getRequest().getAssignment().getCourse().getId()))
							co = x;
				for (Section section: r.getRequest().getAssignment().getSections()) {
					Class_ clazz = Class_DAO.getInstance().get(section.getId());
					if (cd != null && cr == null) {
						for (org.unitime.timetable.model.CourseRequest x: cd.getCourseRequests())
							if (x.getCourseOffering().getInstructionalOffering().getUniqueId().equals(offeringId)) {
								cr = x; break;
							}
					}
					if (co == null)
						co = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
					StudentClassEnrollment enrl = new StudentClassEnrollment();
					enrl.setClazz(clazz);
					clazz.getStudentEnrollments().add(enrl);
					enrl.setCourseOffering(co);
					enrl.setCourseRequest(cr);
					enrl.setTimestamp(ts);
					enrl.setStudent(student);
					student.getClassEnrollments().add(enrl);
					helper.info("Adding " + enrl.getClazz().getClassLabel());
				}
			} else if (!r.getRequest().isAlternative()) { // wait-list
				if (cd != null && !cd.isWaitlist()) {
					cd.setWaitlist(true);
					helper.getHibSession().saveOrUpdate(cd);
				}
				r.getRequest().setWaitlist(true);
			}
			
			helper.getHibSession().save(student);
		
			server.notifyStudentChanged(r.getRequest().getStudent().getId(),
					r.getOldStudent() == null ? null : r.getOldStudent().getRequests(),
					r.getRequest().getStudent().getRequests());
		}
		
     	helper.getHibSession().createQuery(
     			"update CourseOffering c set c.enrollment = " +
     			"(select count(distinct e.student) from StudentClassEnrollment e where e.courseOffering.uniqueId = c.uniqueId) " + 
                 "where c.instructionalOffering.uniqueId = :offeringId").
                 setLong("offeringId", offeringId).executeUpdate();
     	
     	helper.getHibSession().createQuery(
     			"update Class_ c set c.enrollment = " +
     			"(select count(distinct e.student) from StudentClassEnrollment e where e.clazz.uniqueId = c.uniqueId) " + 
                 "where c.schedulingSubpart.uniqueId in " +
                 "(select s.uniqueId from SchedulingSubpart s where s.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId)").
                 setLong("offeringId", offeringId).executeUpdate();
	}
	
	public static boolean sameRooms(Section s1, Section s2) {
		if (s1.getPlacement() == null && s2.getPlacement() == null) return true;
		if (s1.getPlacement() == null || s2.getPlacement() == null) return false;
		return s1.getPlacement().sameRooms(s2.getPlacement());
	}
	
	public static boolean sameTime(Section s1, Section s2) {
		if (s1.getPlacement() == null && s2.getPlacement() == null) return true;
		if (s1.getPlacement() == null || s2.getPlacement() == null) return false;
		return s1.getPlacement().sameTime(s2.getPlacement());
	}
	
	public static boolean sameChoice(Section s1, Section s2) {
		if (s1.getChoice() == null && s2.getChoice() == null) return true;
		if (s1.getChoice() == null || s2.getChoice() == null) return false;
		return s1.getChoice().equals(s2.getChoice());
	}

	public static boolean sameName(Section s1, Enrollment e1, Section s2, Enrollment e2) {
		return s1.getName(e1.getCourse().getId()).equals(s2.getName(e2.getCourse().getId()));
	}

	public static boolean isSame(Enrollment e1, Enrollment e2) {
		if (e1.getSections().size() != e2.getSections().size()) return false;
		s1: for (Section s1: e1.getSections()) {
			for (Section s2: e2.getSections())
				if (sameChoice(s1, s2)) continue s1;
			return false;
		}
		return true;
	}
	
	public static boolean isVerySame(Enrollment e1, Enrollment e2) {
		if (e1.getSections().size() != e2.getSections().size()) return false;
		s1: for (Section s1: e1.getSections()) {
			for (Section s2: e2.getSections())
				if (sameName(s1, e1, s2, e2) && sameTime(s1, s2) && sameRooms(s1, s2)) continue s1;
			return false;
		}
		return true;
	}

	public static boolean check(Enrollment e) {
		if (e.getSections().size() != e.getConfig().getSubparts().size()) return false;
		for (Section s1: e.getSections())
			for (Section s2: e.getSections())
				if (s1.getId() < s2.getId() && s1.isOverlapping(s2)) return false;
		for (Request r: e.getStudent().getRequests()) {
			if (r.getId() != e.getRequest().getId() && r.getInitialAssignment() != null && r.getInitialAssignment().isOverlapping(e)) return false;
		}
		return true;
	}
	
	private static void resection(Resection rx) {
		Enrollment enrollment = null;
		int bonus = 0;
		enrollments: for (Enrollment e: rx.getRequest().getAvaiableEnrollments()) {
			for (Request other: rx.getRequest().getStudent().getRequests())
				if (other.getAssignment() != null && !other.equals(rx.getRequest()) && other.getAssignment().isOverlapping(e))
					continue enrollments;
			int points = rx.evaluate(e); 
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
		Student iOldStudent;
		Enrollment iLastEnrollment;
		
		public Resection(CourseRequest request, Student oldStudent, Enrollment lastEnrollment) {
			iRequest = request; iOldStudent = oldStudent; iLastEnrollment = lastEnrollment;
		}
		
		public CourseRequest getRequest() { return iRequest; }
		public Enrollment getLastEnrollment() { return iLastEnrollment; }
		public Student getOldStudent() { return iOldStudent; }
		
		public int hashCode() { return new Long(getRequest().getStudent().getId()).hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Resection)) return false;
			return getRequest().getStudent().getId() == ((Resection)o).getRequest().getStudent().getId();
		}
		
		public int compareTo(Resection r) {
			// Requests with last enrollment (recently unassigned requests) have priority
			if (getLastEnrollment() == null && r.getLastEnrollment() != null) return 1;
			if (getLastEnrollment() != null && r.getLastEnrollment() == null) return -1;
			
			// Alternative requests last
			if (getRequest().isAlternative() && !r.getRequest().isAlternative()) return 1;
			if (!getRequest().isAlternative() && r.getRequest().isAlternative()) return -1;
			
			// Use priority
			int cmp = new Integer(getRequest().getPriority()).compareTo(r.getRequest().getPriority());
			if (cmp != 0) return cmp;
			
			cmp = (getRequest().getTimeStamp() != null ? getRequest().getTimeStamp() : new Long(Long.MAX_VALUE)).compareTo(
					(r.getRequest().getTimeStamp() != null ? r.getRequest().getTimeStamp() : Long.MAX_VALUE));
			if (cmp != 0) return cmp;
			
			// Do we need to consider reservations ???
			
			return new Long(getRequest().getStudent().getId()).compareTo(r.getRequest().getStudent().getId());
		}
		
		public int evaluate(Enrollment e) {
			int points = 0;
			if (getLastEnrollment() != null) {
				for (Section s1: getLastEnrollment().getSections()) {
					for (Section s2: e.getSections()) {
						if (sameChoice(s1, s2)) points += 10;
						if (sameTime(s1, s2)) points += 5;
						if (sameRooms(s1, s2)) points += 1;
						if (sameName(s1, getLastEnrollment(), s2, e)) points += 3;
					}
				}
			}
			for (Choice c: getRequest().getSelectedChoices()) {
				for (Section s2: e.getSections()) {
					if (c.equals(s2.getChoice())) points += 10;
				}
			}
			return points;
		}
	}
		
	@Override
    public String name() { return "reload-offering"; }
}
