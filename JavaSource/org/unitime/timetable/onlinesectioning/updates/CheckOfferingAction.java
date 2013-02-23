/*
 * UniTime 3.3 (University Timetabling Application)
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

public class CheckOfferingAction extends WaitlistedOnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
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
		if (!server.getAcademicSession().isSectioningEnabled())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());
		
		for (Long offeringId: getOfferingIds()) {
			// offering is locked -> assuming that the offering will get checked when it is unlocked
			if (server.isOfferingLocked(offeringId)) continue;
			// lock and check the offering
			Lock lock = server.lockOffering(offeringId, null, false);
			try {
				Offering offering = server.getOffering(offeringId);
				helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(offeringId)
						.setName(offering.getName())
						.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
				checkOffering(server, helper, offering);
			} finally {
				lock.release();
			}
		}
		return true;
	}
	
	public void checkOffering(OnlineSectioningServer server, OnlineSectioningHelper helper, Offering offering) {
		if (!server.getAcademicSession().isSectioningEnabled() || offering == null) return;
		
		Set<SectioningRequest> queue = new TreeSet<SectioningRequest>();
		
		for (Course course: offering.getCourses()) {
			for (CourseRequest request: course.getRequests()) {
				if (request.getAssignment() == null) {
					if (!request.getStudent().canAssign(request) || !isWaitListed(request, server, helper)) continue;
					OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
					action.setStudent(
							OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(request.getStudent().getId())
							.setExternalId(request.getStudent().getExternalId()));
					action.addOther(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(offering.getId())
							.setName(offering.getName())
							.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
					action.addRequest(OnlineSectioningHelper.toProto(request));
					queue.add(new SectioningRequest(offering, request, null, null, action, null));
				} else if (!check(request.getAssignment())) {
					OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
					action.setStudent(
							OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(request.getStudent().getId())
							.setExternalId(request.getStudent().getExternalId()));
					action.addRequest(OnlineSectioningHelper.toProto(request));
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
					for (Assignment assignment: request.getAssignment().getAssignments())
						enrollment.addSection(OnlineSectioningHelper.toProto(assignment, request.getAssignment()));
					action.addEnrollment(enrollment);
					request.getSelectedChoices().clear();
					for (Section s: request.getAssignment().getSections())
						request.getSelectedChoices().add(s.getChoice());
					queue.add(new SectioningRequest(offering, request, null, request.getAssignment(), action, null));
				}
			}
		}
		
		if (!queue.isEmpty()) {
			
			// Load course request options
			Hashtable<Long, OnlineSectioningLog.CourseRequestOption> options = new Hashtable<Long, OnlineSectioningLog.CourseRequestOption>();
			helper.beginTransaction();
			try {
				for (Object[] o: (List<Object[]>)helper.getHibSession().createQuery(
						"select o.courseRequest.courseDemand.student.uniqueId, o.value from CourseRequestOption o " +
						"where o.courseRequest.courseOffering.instructionalOffering.uniqueId = :offeringId and " +
						"o.optionType = :type")
						.setLong("offeringId", offering.getId())
						.setInteger("type", OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT.getNumber())
						.list()) {
					Long studentId = (Long)o[0];
					try {
						options.put(studentId, OnlineSectioningLog.CourseRequestOption.parseFrom((byte[])o[1]));
					} catch (Exception e) {
						helper.warn("Unable to parse course request options for student " + studentId + ": " + e.getMessage());
					}
				}
				helper.commitTransaction();
			} catch (Exception e) {
				helper.warn("Unable to parse course request options: " + e.getMessage());
			}
			
			DataProperties properties = new DataProperties();
			ResectioningWeights w = new ResectioningWeights(properties);
			DistanceConflict dc = new DistanceConflict(server.getDistanceMetric(), properties);
			TimeOverlapsCounter toc = new TimeOverlapsCounter(null, properties);
			Date ts = new Date();
			for (SectioningRequest r: queue) {
				// helper.info("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getAssignments()) + ")");
				r.setOriginalEnrollment(options.get(r.getRequest().getStudent().getId()));
				long c0 = OnlineSectioningHelper.getCpuTime();
				Enrollment enrollment = r.resection(server, w, dc, toc);
				Lock wl = server.writeLock();
				try {
					if (enrollment != null) {
						r.getRequest().setInitialAssignment(enrollment);
						r.getRequest().assign(0, enrollment);
						enrollment.setTimeStamp(ts.getTime());
					} else if (r.getRequest() != null) {
						r.getRequest().setInitialAssignment(null);
						r.getRequest().unassign(0);
					}
				} finally {
					wl.release();
				}
				if (enrollment != null) {
					OnlineSectioningLog.Enrollment.Builder e = OnlineSectioningLog.Enrollment.newBuilder();
					e.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (Assignment assignment: enrollment.getAssignments())
						e.addSection(OnlineSectioningHelper.toProto(assignment, enrollment));
					r.getAction().addEnrollment(e);
				}
				// helper.info("New: " + (r.getRequest().getAssignment() == null ? "not assigned" : r.getRequest().getAssignment().getAssignments()));
				if (r.getLastEnrollment() == null && r.getRequest().getAssignment() == null) continue;
				if (r.getLastEnrollment() != null && r.getLastEnrollment().equals(r.getRequest().getAssignment())) continue;
				
				helper.beginTransaction();
				try {
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudent().getId(), helper.getHibSession());
					Map<Long, StudentClassEnrollment> oldEnrollments = new HashMap<Long, StudentClassEnrollment>();
					String approvedBy = null; Date approvedDate = null;
					for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
						StudentClassEnrollment enrl = i.next();
						if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest().getId())) ||
							(r.getLastEnrollment() != null && enrl.getCourseOffering() != null && enrl.getCourseOffering().getUniqueId().equals(r.getLastEnrollment().getCourse().getId()))) {
							helper.info("Deleting " + enrl.getClazz().getClassLabel());
							oldEnrollments.put(enrl.getClazz().getUniqueId(), enrl);
							if (approvedBy == null && enrl.getApprovedBy() != null) {
								approvedBy = enrl.getApprovedBy();
								approvedDate = enrl.getApprovedDate();
							}
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
							Class_ clazz = Class_DAO.getInstance().get(section.getId(), helper.getHibSession());
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
							StudentClassEnrollment old = oldEnrollments.get(section.getId());
							enrl.setChangedBy(old != null ? old.getChangedBy() : helper.getUser() == null ? StudentClassEnrollment.SystemChange.WAITLIST.toString() : helper.getUser().getExternalId());
							clazz.getStudentEnrollments().add(enrl);
							enrl.setCourseOffering(co);
							enrl.setCourseRequest(cr);
							enrl.setTimestamp(old != null ? old.getTimestamp() : ts);
							enrl.setStudent(student);
							enrl.setApprovedBy(approvedBy);
							enrl.setApprovedDate(approvedDate);
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
					server.persistExpectedSpaces(offering.getId());

					server.notifyStudentChanged(r.getRequest().getStudent().getId(),
							r.getRequest(),
							r.getLastEnrollment(), helper.getUser());
					
					helper.commitTransaction();
					r.getAction().setResult(enrollment == null ? OnlineSectioningLog.Action.ResultType.NULL : OnlineSectioningLog.Action.ResultType.SUCCESS);
				} catch (Exception e) {
					r.getRequest().setInitialAssignment(r.getLastEnrollment());
					if (r.getLastEnrollment() == null) {
						if (r.getRequest().getAssignment() != null)
							r.getRequest().unassign(0);
					} else {
						r.getRequest().assign(0, r.getLastEnrollment());
					}
					r.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
					r.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getMessage()));
					helper.rollbackTransaction();
					helper.error("Unable to resection student: " + e.getMessage(), e);
				}
				
				r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				r.getAction().setEndTime(System.currentTimeMillis());
			}
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
