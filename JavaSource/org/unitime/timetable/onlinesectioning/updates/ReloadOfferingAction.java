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

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Offering;
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
		
		if (oldOffering == null) return;
		
		List<Student[]> students = new ArrayList<Student[]>();
		
		if (newOffering == null) {
			// Reload affected students
			for (Course course: oldOffering.getCourses())
				for (CourseRequest request: server.getRequests(course.getId())) {
					Student oldStudent = request.getStudent();
					server.remove(oldStudent);
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(oldStudent.getId(), helper.getHibSession());
					Student newStudent = (student == null ? null : ReloadAllData.loadStudent(student, server, helper));
					if (newStudent != null)
						server.update(newStudent);
					students.add(new Student[] {oldStudent, newStudent});
				}
		} else {
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
		}
		
		//TODO: Check for conflicts among the new students, do some sectioning
		
		for (Student[] student: students) {
			if (student[0] == null && student[1] == null) continue;
			server.notifyStudentChanged(student[0] == null ? student[1].getId() : student[0].getId(),
					(student[0] == null ? null : student[0].getRequests()),
					(student[1] == null ? null : student[1].getRequests()));
		}
	}
		
	@Override
    public String name() { return "reload-offering"; }
}
