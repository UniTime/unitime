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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction.DatabaseAction;

/**
 * @author Tomas Muller
 */
public class EnrollStudent extends DatabaseAction<Collection<Long>> {
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
		Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
		if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
		Hashtable<Long, Class_> classes = new Hashtable<Long, Class_>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
			if (ca.isFreeTime() || ca.getClassId() == null) continue;
			Class_ clazz = Class_DAO.getInstance().get(ca.getClassId(), helper.getHibSession());
			if (!isAvailable(helper, student, clazz, server.getSection(ca.getClassId())))
				throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AVAILABLE, ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection());
			classes.put(clazz.getUniqueId(), clazz);
		}
		Hashtable<Long, org.unitime.timetable.model.CourseRequest> req = SaveStudentRequests.saveRequest(helper.getHibSession(), student, getRequest(), false);
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
		helper.getHibSession().refresh(student);
		new ReloadStudent(student.getUniqueId()).execute(server, helper);
		List<Long> ret = new ArrayList<Long>();
		for (Request r: server.getStudent(student.getUniqueId()).getRequests())
			if (r.getInitialAssignment() != null && r.getInitialAssignment().isCourseRequest())
				for (Section s: r.getInitialAssignment().getSections())
					ret.add(s.getId());
		return ret;
	}

	@SuppressWarnings("unchecked")
	private boolean isAvailable(OnlineSectioningHelper helper, Student student, Class_ clazz, Section section) {
		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) return true;
		int limit = clazz.getMaxExpectedCapacity();
		if (clazz.getExpectedCapacity() < clazz.getMaxExpectedCapacity()) {
			org.unitime.timetable.model.Assignment commited = clazz.getCommittedAssignment();
			int roomLimit = 0;
			if (commited != null) {
				int roomCap = 0;
				for (Iterator<Location> i = commited.getRooms().iterator(); i.hasNext(); ) roomCap += i.next().getCapacity();
				roomLimit = Math.round(clazz.getRoomRatio() * roomCap);
			}
			limit = Math.min(Math.max(roomLimit, clazz.getExpectedCapacity()), clazz.getMaxExpectedCapacity());
		}
		if (limit != section.getLimit()) {
			helper.warn("Limit of " + clazz.getClassLabel() + " changed (" + limit +" != " + section.getLimit() + ").");
		}
		if (clazz.getStudentEnrollments().size() != section.getEnrollments().size()) {
			helper.warn("Enrollment of " + clazz.getClassLabel() + " changed (" + clazz.getStudentEnrollments().size() +" != " + section.getEnrollments().size() + ").");
			enrl: for (Iterator<StudentClassEnrollment> i = clazz.getStudentEnrollments().iterator(); i.hasNext(); ) {
				StudentClassEnrollment enrl = i.next();
				for (Iterator<Enrollment> j = section.getEnrollments().iterator(); j.hasNext();) {
					Enrollment enrollment = j.next();
					if (enrollment.getStudent().getId() == enrl.getStudent().getUniqueId()) continue enrl;
				}
				helper.warn(" -- student " + enrl.getStudent().getExternalUniqueId() + " not present in section enrollments (solver).");
			}
			enrl: for (Iterator<Enrollment> i = section.getEnrollments().iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
				for (Iterator<StudentClassEnrollment> j = clazz.getStudentEnrollments().iterator(); j.hasNext(); ) {
					StudentClassEnrollment enrl = j.next();
					if (enrollment.getStudent().getId() == enrl.getStudent().getUniqueId()) continue enrl;
				}
				Student s = StudentDAO.getInstance().get(enrollment.getStudent().getId(), helper.getHibSession());
				helper.warn(" -- student " + s.getExternalUniqueId() + " not present in class enrollments (db).");
			}
		}
		if (clazz.getStudentEnrollments().size() < limit) return true;
		if (clazz.getStudentEnrollments().size() > limit) return false;
		for (Iterator<StudentClassEnrollment> i = clazz.getStudentEnrollments().iterator(); i.hasNext(); ) {
			StudentClassEnrollment enrl = i.next();
			if (enrl.getStudent().equals(student)) return true;
		}
		return false;
	}
	
	@Override
	public String name() {
		return "enroll";
	}
}
