/*
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.solver.CheckAssignmentAction;

/**
 * @author Tomas Muller
 */
public class EnrollStudent implements OnlineSectioningAction<Collection<Long>> {
	private Long iStudentId;
	private CourseRequestInterface iRequest;
	private List<ClassAssignmentInterface.ClassAssignment> iAssignment;
	
	public EnrollStudent(Long studentId, CourseRequestInterface request, List<ClassAssignmentInterface.ClassAssignment> assignment) {
		iStudentId = studentId;
		iRequest = request;
		iAssignment = assignment;
	}
	
	public Long getStudentId() { return iStudentId; }
	public CourseRequestInterface getRequest() { return iRequest; }
	public List<ClassAssignmentInterface.ClassAssignment> getAssignment() { return iAssignment; }

	@Override
	public Collection<Long> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Set<Long> offeringIds = new HashSet<Long>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
			if (!ca.isFreeTime()) {
				Course course = server.getCourse(ca.getClassId());
				if (course != null) offeringIds.add(course.getOffering().getId());
			}
		
		List<Long> ret = new ArrayList<Long>();
		Lock lock = server.lockStudent(getStudentId(), offeringIds);
		try {
			helper.beginTransaction();
			try {
				new CheckAssignmentAction(getStudentId(), getAssignment()).check(server, helper);
				
				Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
				if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
				
				Hashtable<Long, Class_> classes = new Hashtable<Long, Class_>();
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca.isFreeTime() || ca.getClassId() == null) continue;
					Class_ clazz = Class_DAO.getInstance().get(ca.getClassId(), helper.getHibSession());
					if (clazz == null)
						throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AVAILABLE, ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection());
					classes.put(clazz.getUniqueId(), clazz);
				}
				
				Map<Long, org.unitime.timetable.model.CourseRequest> req = SaveStudentRequests.saveRequest(server, helper, student, getRequest(), false);
				Date ts = new Date();
				
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca.isFreeTime() || ca.getClassId() == null) continue;
					Class_ clazz = classes.get(ca.getClassId());
					org.unitime.timetable.model.CourseRequest cr = req.get(ca.getCourseId());
					if (clazz == null || cr == null) continue;
					StudentClassEnrollment enrl = new StudentClassEnrollment();
					enrl.setClazz(clazz);
					clazz.getStudentEnrollments().add(enrl);
					enrl.setCourseOffering(cr.getCourseOffering());
					enrl.setCourseRequest(cr);
					enrl.setTimestamp(ts);
					enrl.setStudent(student);
					student.getClassEnrollments().add(enrl);
				}
				
				helper.getHibSession().save(student);
				helper.getHibSession().flush();
				
				// Reload student
				net.sf.cpsolver.studentsct.model.Student oldStudent = server.getStudent(getStudentId());
				net.sf.cpsolver.studentsct.model.Student newStudent = null;
				try {
					server.remove(oldStudent);
					newStudent = ReloadAllData.loadStudent(student, server, helper);
					server.update(newStudent);
				} catch (Exception e) {
					// Put back the old student (the database will get rollbacked)
					server.update(oldStudent);
					if (e instanceof RuntimeException)
						throw (RuntimeException)e;
					throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
				}
				server.notifyStudentChanged(getStudentId(), oldStudent.getRequests(), newStudent.getRequests());
				
				for (Request r: server.getStudent(getStudentId()).getRequests())
					if (r.getInitialAssignment() != null && r.getInitialAssignment().isCourseRequest())
						for (Section s: r.getInitialAssignment().getSections())
							ret.add(s.getId());
			
				helper.commitTransaction();
				return ret;
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
			}
		} finally {
			lock.release();
		}
	}
	
	@Override
	public String name() {
		return "enroll";
	}
}
