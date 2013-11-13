/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;

import org.hibernate.CacheMode;
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
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class CheckOfferingAction extends WaitlistedOnlineSectioningAction<Boolean> implements HasCacheMode {
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
		
		boolean result = true;
		
		for (Long offeringId: getOfferingIds()) {
			try {
				// offering is locked -> assuming that the offering will get checked when it is unlocked
				if (server.isOfferingLocked(offeringId)) continue;
				
				helper.beginTransaction();
				// lock and check the offering
				Lock lock = server.lockOffering(offeringId, null, false);
				try {
					XOffering offering = server.getOffering(offeringId);
					helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(offeringId)
							.setName(offering.getName())
							.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
					checkOffering(server, helper, offering);
				} finally {
					lock.release();
				}
				helper.commitTransaction();
				
			} catch (Exception e) {
				helper.rollbackTransaction();
				helper.fatal("Unable to check offering " + offeringId + ", reason: " + e.getMessage(), e);
				result = false;
			}
		}
		
		return result;
	}
	
	public void checkOffering(OnlineSectioningServer server, OnlineSectioningHelper helper, XOffering offering) {
		if (!server.getAcademicSession().isSectioningEnabled() || offering == null) return;
		
		Set<SectioningRequest> queue = new TreeSet<SectioningRequest>();
		
		XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
		
		for (XCourseRequest request: enrollments.getRequests()) {
			XStudent student = server.getStudent(request.getStudentId());
			if (request.getEnrollment() == null) {
				if (!student.canAssign(request) || !isWaitListed(student, request, server, helper)) continue;
				OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
				action.setStudent(
						OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(student.getStudentId())
						.setExternalId(student.getExternalId())
						.setName(student.getName()));
				action.addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(offering.getOfferingId())
						.setName(offering.getName())
						.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
				action.addRequest(OnlineSectioningHelper.toProto(request));
				queue.add(new SectioningRequest(offering, request, null, null, action, null));
			} else if (!check(server, student, offering, request)) {
				OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
				action.setStudent(
						OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(student.getStudentId())
						.setExternalId(student.getExternalId())
						.setName(student.getName()));
				action.addRequest(OnlineSectioningHelper.toProto(request));
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				for (XSection section: offering.getSections(request.getEnrollment()))
					enrollment.addSection(OnlineSectioningHelper.toProto(section, request.getEnrollment()));
				action.addEnrollment(enrollment);
				queue.add(new SectioningRequest(offering, request, student, request.getEnrollment(), action, null));
			}
		}
		
		if (!queue.isEmpty()) {
			
			DataProperties properties = new DataProperties();
			ResectioningWeights w = new ResectioningWeights(properties);
			DistanceConflict dc = new DistanceConflict(server.getDistanceMetric(), properties);
			TimeOverlapsCounter toc = new TimeOverlapsCounter(null, properties);
			Date ts = new Date();
			for (SectioningRequest r: queue) {
				// helper.info("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getAssignments()) + ")");
				r.setOriginalEnrollment(r.getRequest().getOptions(offering.getOfferingId()));
				long c0 = OnlineSectioningHelper.getCpuTime();
				XEnrollment enrollment = r.resection(server, w, dc, toc);
				XCourseRequest prev = r.getRequest();
				Long studentId = prev.getStudentId();
				if (enrollment != null) {
					enrollment.setTimeStamp(ts);
					r.setRequest(server.assign(r.getRequest(), enrollment));
				} else if (r.getRequest() != null) {
					r.setRequest(server.assign(r.getRequest(), null));
				}
				if (r.getRequest() == null) {
					helper.fatal("Failed to assign " + studentId + ": " + (enrollment == null ? prev.toString() : enrollment.toString()));
					continue;
				}
				if (enrollment != null) {
					OnlineSectioningLog.Enrollment.Builder e = OnlineSectioningLog.Enrollment.newBuilder();
					e.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (Long sectionId: enrollment.getSectionIds())
						e.addSection(OnlineSectioningHelper.toProto(offering.getSection(sectionId), enrollment));
					r.getAction().addEnrollment(e);
				}
				// helper.info("New: " + (r.getRequest().getAssignment() == null ? "not assigned" : r.getRequest().getAssignment().getAssignments()));
				if (r.getLastEnrollment() == null && r.getRequest().getEnrollment() == null) continue;
				if (r.getLastEnrollment() != null && r.getLastEnrollment().equals(r.getRequest().getEnrollment())) continue;
				
				boolean tx = helper.beginTransaction();
				try {
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudentId(), helper.getHibSession());
					Map<Long, StudentClassEnrollment> oldEnrollments = new HashMap<Long, StudentClassEnrollment>();
					String approvedBy = null; Date approvedDate = null;
					for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
						StudentClassEnrollment enrl = i.next();
						if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest().getRequestId())) ||
							(r.getLastEnrollment() != null && enrl.getCourseOffering() != null && enrl.getCourseOffering().getUniqueId().equals(r.getLastEnrollment().getCourseId()))) {
							helper.info("Deleting " + enrl.getClazz().getClassLabel());
							oldEnrollments.put(enrl.getClazz().getUniqueId(), enrl);
							if (approvedBy == null && enrl.getApprovedBy() != null) {
								approvedBy = enrl.getApprovedBy();
								approvedDate = enrl.getApprovedDate();
							}
							enrl.getClazz().getStudentEnrollments().remove(enrl);
							/*
							if (enrl.getCourseRequest() != null)
								enrl.getCourseRequest().getClassEnrollments().remove(enrl);
								*/
							helper.getHibSession().delete(enrl);
							i.remove();
						}
					}
					CourseDemand cd = null;
					for (CourseDemand x: student.getCourseDemands())
						if (x.getUniqueId().equals(r.getRequest().getRequestId())) {
							cd = x;
							break;
						}
					if (r.getRequest().getEnrollment() != null) { // save enrollment
						org.unitime.timetable.model.CourseRequest cr = null;
						CourseOffering co = null;
						if (co == null) 
							co = CourseOfferingDAO.getInstance().get(r.getRequest().getEnrollment().getCourseId(), helper.getHibSession());
						for (Long sectionId: r.getRequest().getEnrollment().getSectionIds()) {
							Class_ clazz = Class_DAO.getInstance().get(sectionId, helper.getHibSession());
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
							StudentClassEnrollment old = oldEnrollments.get(sectionId);
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
		
					EnrollStudent.updateSpace(server,
							r.getRequest().getEnrollment() == null ? null : SectioningRequest.convert(server.getStudent(r.getRequest().getStudentId()), r.getRequest(), server, offering, r.getRequest().getEnrollment()).getAssignment(),
							r.getLastEnrollment() == null ? null : SectioningRequest.convert(r.getOldStudent(), r.getRequest(), server, offering, r.getLastEnrollment()).getAssignment());
					server.persistExpectedSpaces(offering.getOfferingId());

					server.execute(new NotifyStudentAction(r.getRequest().getStudentId(), offering, r.getLastEnrollment()), helper.getUser());
					
					if (tx) helper.commitTransaction();
					r.getAction().setResult(enrollment == null ? OnlineSectioningLog.Action.ResultType.NULL : OnlineSectioningLog.Action.ResultType.SUCCESS);
				} catch (Exception e) {
					server.assign(r.getRequest(), r.getLastEnrollment());
					r.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
					r.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getMessage() == null ? "null" : e.getMessage()));
					if (tx) helper.rollbackTransaction();
					helper.error("Unable to resection student: " + e.getMessage(), e);
				}
				
				r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				r.getAction().setEndTime(System.currentTimeMillis());
			}
		}
	}
	
	public boolean check(OnlineSectioningServer server, XStudent student, XOffering offering, XCourseRequest request) {
		if (request.getEnrollment() == null) return true;
		if (!offering.getOfferingId().equals(request.getEnrollment().getOfferingId())) return true;
		List<XSection> sections = offering.getSections(request.getEnrollment());
		XConfig config = offering.getConfig(request.getEnrollment().getConfigId());
		if (config == null || sections.size() != config.getSubparts().size()) {
			return false;
		}
		for (XSection s1: sections) {
			for (XSection s2: sections) {
				if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(offering.getDistributions(), s2)) {
					return false;
				}
				if (!s1.getSectionId().equals(s2.getSectionId()) && s1.getSubpartId().equals(s2.getSubpartId())) {
					return false;
				}
			}
			if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId())) {
				return false;
			}
		}
		for (XRequest r: student.getRequests())
			if (r instanceof XCourseRequest && !r.getRequestId().equals(request.getRequestId()) && ((XCourseRequest)r).getEnrollment() != null) {
				XEnrollment e = ((XCourseRequest)r).getEnrollment();
				XOffering other = server.getOffering(e.getOfferingId());
				if (other != null) {
					List<XSection> assignment = other.getSections(e);
					for (XSection section: sections)
						if (section.isOverlapping(offering.getDistributions(), assignment)) {
							return false;
						}
				}
			}
		return true;
	}

	@Override
	public String name() {
		return "check-offering";
	}
	
	@Override
	public CacheMode getCacheMode() {
		return CacheMode.REFRESH;
	}
}
