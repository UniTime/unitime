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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

public class CheckOfferingAction implements OnlineSectioningAction<Boolean>{
	private Collection<Long> iOfferingIds;
	
	public CheckOfferingAction(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
	}
	
	public CheckOfferingAction(Collection<Long> offeringIds) {
		iOfferingIds = offeringIds;
	}
	
	public Collection<Long> getOfferingIds() { return iOfferingIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		for (Long offeringId: getOfferingIds()) {
			// offering is locked -> assuming that the offering will get checked when it is unlocked
			if (server.isOfferingLocked(offeringId)) continue;
			// lock and check the offering
			Lock lock = server.lockOffering(offeringId, null, false);
			try {
				Offering offering = server.getOffering(offeringId);
				checkOffering(server, helper, offering);
				updateEnrollmentCounters(server, helper, offering);
			} finally {
				lock.release();
			}
		}
		return true;
	}
	
	public void checkOffering(OnlineSectioningServer server, OnlineSectioningHelper helper, Offering offering) {
		if (offering == null) return;
		
		Set<SectioningRequest> queue = new TreeSet<SectioningRequest>();

		for (Course course: offering.getCourses()) {
			for (CourseRequest request: course.getRequests()) {
				if (request.getAssignment() == null) {
					if (request.getStudent().canAssign(request)) 
						queue.add(new SectioningRequest(offering, request, null, null));
				} else if (!check(request.getAssignment())) {
					request.getSelectedChoices().clear();
					for (Section s: request.getAssignment().getSections())
						request.getSelectedChoices().add(s.getChoice());
					queue.add(new SectioningRequest(offering, request, null, request.getAssignment()));
				}
			}
		}
		
		if (!queue.isEmpty()) {
			DataProperties properties = new DataProperties();
			ResectioningWeights w = new ResectioningWeights(properties);
			DistanceConflict dc = new DistanceConflict(null, properties);
			TimeOverlapsCounter toc = new TimeOverlapsCounter(null, properties);
			Date ts = new Date();
			for (SectioningRequest r: queue) {
				// helper.info("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getAssignments()) + ")");
				Enrollment enrollment = r.resection(w, dc, toc);
				Lock wl = server.writeLock();
				try {
					if (enrollment != null) {
						r.getRequest().setInitialAssignment(enrollment);
						r.getRequest().assign(0, enrollment);
					} else if (r.getRequest() != null) {
						r.getRequest().setInitialAssignment(null);
						r.getRequest().unassign(0);
					}
				} finally {
					wl.release();
				}
				// helper.info("New: " + (r.getRequest().getAssignment() == null ? "not assigned" : r.getRequest().getAssignment().getAssignments()));
				if (r.getLastEnrollment() == null && r.getRequest().getAssignment() == null) continue;
				if (r.getLastEnrollment() != null && r.getLastEnrollment().equals(r.getRequest().getAssignment())) continue;
				
				
				server.notifyStudentChanged(r.getRequest().getStudent().getId(),
						r.getRequest(),
						r.getLastEnrollment());
				
				helper.beginTransaction();
				try {
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudent().getId(), helper.getHibSession());
					for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
						StudentClassEnrollment enrl = i.next();
						if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest().getId())) ||
							(r.getLastEnrollment() != null && enrl.getCourseOffering() != null && enrl.getCourseOffering().getUniqueId().equals(r.getLastEnrollment().getCourse().getId()))) {
							helper.info("Deleting " + enrl.getClazz().getClassLabel());
							enrl.getClazz().getStudentEnrollments().remove(enrl);
							if (enrl.getCourseRequest() != null)
								enrl.getCourseRequest().getClassEnrollments().remove(enrl);
							helper.getHibSession().delete(enrl);
							i.remove();
						}
					}
					CourseDemand cd = null;
					for (CourseDemand x: student.getCourseDemands())
						if (x.getUniqueId().equals(r.getRequest().getId())) {
							cd = x;
							break;
						}
					if (r.getRequest().getAssignment() != null) { // save enrollment
						org.unitime.timetable.model.CourseRequest cr = null;
						CourseOffering co = null;
						if (co == null) 
							co = CourseOfferingDAO.getInstance().get(r.getRequest().getAssignment().getCourse().getId(), helper.getHibSession());
						for (Section section: r.getRequest().getAssignment().getSections()) {
							Class_ clazz = Class_DAO.getInstance().get(section.getId());
							if (cd != null && cr == null) {
								for (org.unitime.timetable.model.CourseRequest x: cd.getCourseRequests())
									if (x.getCourseOffering().getUniqueId().equals(co.getUniqueId())) {
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
		
					EnrollStudent.updateSpace(helper, r.getRequest().getAssignment(), r.getLastEnrollment());

					server.notifyStudentChanged(r.getRequest().getStudent().getId(),
							r.getRequest(),
							r.getLastEnrollment());
					
					helper.commitTransaction();
				} catch (Exception e) {
					r.getRequest().setInitialAssignment(r.getLastEnrollment());
					if (r.getLastEnrollment() == null) {
						if (r.getRequest().getAssignment() != null)
							r.getRequest().unassign(0);
					} else {
						r.getRequest().assign(0, r.getLastEnrollment());
					}
					helper.rollbackTransaction();
					if (e instanceof SectioningException)
						throw (SectioningException)e;
					throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
				}
			}
		}
	}
	
	public static void updateEnrollmentCounters(OnlineSectioningServer server, OnlineSectioningHelper helper, Offering offering) {
		if (offering == null) return;
		helper.beginTransaction();
		try {
	     	helper.getHibSession().createQuery(
	     			"update CourseOffering c set c.enrollment = " +
	     			"(select count(distinct e.student) from StudentClassEnrollment e where e.courseOffering.uniqueId = c.uniqueId) " + 
	                 "where c.instructionalOffering.uniqueId = :offeringId").
	                 setLong("offeringId", offering.getId()).executeUpdate();
	     	
	     	helper.getHibSession().createQuery(
	     			"update Class_ c set c.enrollment = " +
	     			"(select count(distinct e.student) from StudentClassEnrollment e where e.clazz.uniqueId = c.uniqueId) " + 
	                 "where c.schedulingSubpart.uniqueId in " +
	                 "(select s.uniqueId from SchedulingSubpart s where s.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId)").
	                 setLong("offeringId", offering.getId()).executeUpdate();
			helper.commitTransaction();
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		}
	}

	public boolean check(Enrollment e) {
		if (e.getSections().size() != e.getConfig().getSubparts().size()) return false;
		for (Section s1: e.getSections())
			for (Section s2: e.getSections()) {
				if (s1.getId() < s2.getId() && s1.isOverlapping(s2)) return false;
				if (s1.getId() != s2.getId() && s1.getSubpart().getId() == s2.getSubpart().getId()) return false;
			}
		for (Request r: e.getStudent().getRequests()) {
			if (r.getId() != e.getRequest().getId() && r.getInitialAssignment() != null && r.getInitialAssignment().isOverlapping(e)) return false;
		}
		return true;
	}

	@Override
	public String name() {
		return "check-offering";
	}


}
