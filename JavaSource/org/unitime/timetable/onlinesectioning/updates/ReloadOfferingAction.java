/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.online.selection.ResectioningWeights;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpart;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpartCache;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.WaitListComparatorProvider;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest.ReschedulingReason;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequestComparator;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ReloadOfferingAction extends WaitlistedOnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private List<Long> iOfferingIds;
	
	public ReloadOfferingAction forOfferings(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
		return this;
	}
	
	public ReloadOfferingAction forOfferings(List<Long> offeringIds) {
		iOfferingIds = offeringIds;
		return this;
	}
	
	public List<Long> getOfferingIds() { return iOfferingIds; }
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (ApplicationProperty.OnlineSchedulingGradableIType.isTrue() && Class_.getExternalClassNameHelper() != null) {
			if (Class_.getExternalClassNameHelper() instanceof HasGradableSubpartCache) {
				helper.setGradableSubpartsProvider(((HasGradableSubpartCache)Class_.getExternalClassNameHelper()).getGradableSubparts(getOfferingIds(), helper.getHibSession()));
			} else if (Class_.getExternalClassNameHelper() instanceof HasGradableSubpart) {
				helper.setGradableSubpartsProvider((HasGradableSubpart)Class_.getExternalClassNameHelper());
			}
		}

		Set<Long> recheck = new HashSet<Long>();
		for (Long offeringId: getOfferingIds()) {
			helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(offeringId)
					.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
			
			List<Long> studentIds =
					helper.getHibSession().createQuery(
							"select distinct cr.courseDemand.student.uniqueId from CourseRequest cr " +
							"where cr.courseOffering.instructionalOffering.uniqueId = :offeringId", Long.class)
							.setParameter("offeringId", offeringId, Long.class).list();
			studentIds.addAll(
					helper.getHibSession().createQuery(
							"select distinct e.student.uniqueId from StudentClassEnrollment e " +
							"where e.courseOffering.instructionalOffering.uniqueId = :offeringId and e.courseRequest is null", Long.class)
							.setParameter("offeringId", offeringId, Long.class).list());
			
			/*
			List<Long> studentIds = (List<Long>)helper.getHibSession().createQuery(
					"select distinct s.uniqueId from Student s " +
					"left outer join s.classEnrollments e " +
					"left outer join s.courseDemands d left outer join d.courseRequests r left outer join r.courseOffering co " +
					"where e.courseOffering.instructionalOffering.uniqueId = :offeringId or " +
					"co.instructionalOffering.uniqueId = :offeringId").setParameter("offeringId", offeringId, Long.class).list();
			*/
			
			Lock lock = server.lockOffering(offeringId, studentIds, name());
			try {
				helper.beginTransaction();
				try {
					reloadOffering(server, helper, offeringId, recheck);
					
					helper.commitTransaction();
				} catch (Exception e) {
					helper.rollbackTransaction();
					if (e instanceof SectioningException)
						throw (SectioningException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				}
			} finally {
				lock.release();
			}
		}
		
		if (!recheck.isEmpty())
			server.execute(server.createAction(CheckOfferingAction.class).forOfferings(recheck), helper.getUser());
		
		return true;			
	}
		
	public void reloadOffering(final OnlineSectioningServer server, OnlineSectioningHelper helper, Long offeringId, Set<Long> recheckOfferingIds) {
		if (recheckOfferingIds != null) recheckOfferingIds.remove(offeringId);
		// Load new students
		Map<Long, org.unitime.timetable.model.Student> newStudents = new HashMap<Long, org.unitime.timetable.model.Student>();
		/*
		for (org.unitime.timetable.model.Student student : (List<org.unitime.timetable.model.Student>)helper.getHibSession().createQuery(
                "select s from Student s " +
                "left join fetch s.courseDemands as cd " +
                "left join fetch cd.courseRequests as cr " +
                "left join fetch cr.courseOffering as co " +
                "left join fetch cr.classWaitLists as cwl " + 
                "left join fetch s.classEnrollments as e " +
                "left join fetch s.areaClasfMajors as acm " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "left join fetch s.notes as n " +
                "where s.uniqueId in (select xe.student.uniqueId from StudentClassEnrollment xe where xe.courseOffering.instructionalOffering.uniqueId = :offeringId) " +
                "or s.uniqueId in (select xr.courseDemand.student.uniqueId from CourseRequest xr where xr.courseOffering.instructionalOffering.uniqueId = :offeringId)"
                ).setParameter("offeringId", offeringId, Long.class).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		*/
		for (org.unitime.timetable.model.Student student : helper.getHibSession().createQuery(
                "select distinct s from Student s " +
                "left join s.courseDemands as cd " +
                "left join cd.courseRequests as cr " +
                "left join fetch s.classEnrollments as e " +
                "left join fetch s.areaClasfMajors as acm " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "left join fetch s.notes as n " +
                "where cr.courseOffering.instructionalOffering.uniqueId = :offeringId", org.unitime.timetable.model.Student.class
                ).setParameter("offeringId", offeringId, Long.class).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		for (org.unitime.timetable.model.Student student : helper.getHibSession().createQuery(
                "select distinct s from Student s " +
                "left join fetch s.courseDemands as cd " +
                "left join fetch cd.courseRequests as cr " +
                "left join fetch cr.courseOffering as co " +
                "left join fetch cr.classWaitLists as cwl " + 
                "left join fetch s.classEnrollments as e " +
                "left join fetch s.areaClasfMajors as acm " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "left join fetch s.notes as n " +
                "where e.courseOffering.instructionalOffering.uniqueId = :offeringId and e.courseRequest is null",
                org.unitime.timetable.model.Student.class
                ).setParameter("offeringId", offeringId, Long.class).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		
		// Persist expected spaces if needed
		if (server.needPersistExpectedSpaces(offeringId))
			PersistExpectedSpacesAction.persistExpectedSpaces(offeringId, false, server, helper);
		
		// Existing offering
		XOffering oldOffering = server.getOffering(offeringId);
		XEnrollments oldEnrollments = server.getEnrollments(offeringId); 
		
		// New offering
		XOffering newOffering = null;
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(offeringId, helper.getHibSession());
		List<XDistribution> distributions = new ArrayList<XDistribution>();
		if (io != null && io.isAllowStudentScheduling()) {
			// Load linked sections and ignore student conflict constraints
	    	List<DistributionPref> distPrefs = helper.getHibSession().createQuery(
	        		"select distinct p from DistributionPref p inner join p.distributionObjects o, Department d, " +
	        		"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io " +
	        		"where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId " +
	        		"and io.uniqueId = :offeringId and (o.prefGroup = c or o.prefGroup = c.schedulingSubpart) " +
	        		"and p.owner = d and p.prefLevel.prefProlog = :pref", DistributionPref.class)
	        		.setParameter("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference(), String.class)
	        		.setParameter("ref2", IgnoreStudentConflictsConstraint.REFERENCE, String.class)
	        		.setParameter("pref", PreferenceLevel.sRequired, String.class)
	        		.setParameter("sessionId", server.getAcademicSession().getUniqueId(), Long.class)
	        		.setParameter("offeringId", offeringId, Long.class)
	        		.list();
	        if (!distPrefs.isEmpty()) {
	        	for (DistributionPref pref: distPrefs) {
	        		int variant = 0;
	        		for (Collection<Class_> sections: ReloadAllData.getSections(pref)) {
	        			XDistributionType type = XDistributionType.IngoreConflicts;
	        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference()))
	        				type = XDistributionType.LinkedSections;
	        			XDistribution distribution = new XDistribution(type, pref.getUniqueId(), variant++, sections);
	        			distributions.add(distribution);
	        		}
	        	}
	        }
	        
			newOffering = ReloadAllData.loadOffering(io, distributions, server, helper);
			if (newOffering != null)
				server.update(newOffering);
			else if (oldOffering != null)
				server.remove(oldOffering);
			
			// Load sectioning info
        	List<Object[]> infos = helper.getHibSession().createQuery(
        			"select i.clazz.uniqueId, i.nbrExpectedStudents from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId",
        			Object[].class).setParameter("offeringId", offeringId, Long.class).list();
        	XExpectations expectations = new XExpectations(offeringId);
        	for (Object[] info : infos) {
        		Long sectionId = (Long)info[0];
        		Double expected = (Double)info[1];
        		expectations.setExpectedSpace(sectionId, expected);
        	}
        	server.update(expectations);
		} else if (oldOffering != null) {
			server.remove(oldOffering);
		}
		
		List<XStudent[]> students = new ArrayList<XStudent[]>();
		
		if (oldEnrollments != null) {
			Set<Long> checked = new HashSet<Long>();
			for (XRequest old: oldEnrollments.getRequests()) {
				if (!checked.add(old.getStudentId())) continue;
				XStudent oldStudent = server.getStudent(old.getStudentId());
				org.unitime.timetable.model.Student student = newStudents.get(oldStudent.getStudentId());
				if (student == null)
					student = StudentDAO.getInstance().get(oldStudent.getStudentId(), helper.getHibSession());
				XStudent newStudent = (student == null ? null : ReloadAllData.loadStudent(student, null, server, helper, WaitList.WaitListType.RELOAD));
				if (newStudent != null)
					server.update(newStudent, true);
				else
					server.remove(oldStudent);
				students.add(new XStudent[] {oldStudent, newStudent});
				newStudents.remove(oldStudent.getStudentId());
			}	
		}
		for (org.unitime.timetable.model.Student student: newStudents.values()) {
			XStudent oldStudent = server.getStudent(student.getUniqueId());
			XStudent newStudent = ReloadAllData.loadStudent(student, null, server, helper, WaitList.WaitListType.RELOAD);
			if (newStudent != null)
				server.update(newStudent, true);
			else if (oldStudent != null)
				server.remove(oldStudent);
			students.add(new XStudent[] {oldStudent, newStudent});
		}
		
		if (!server.getAcademicSession().isSectioningEnabled())
			return;
		
		if (!CustomStudentEnrollmentHolder.isAllowWaitListing())
			return;
		
		if (newOffering == null && oldOffering == null)
			return;
		
		if (newOffering != null && !newOffering.isReSchedule())
			return;
		
		WaitListComparatorProvider cmp = Customization.WaitListComparatorProvider.getProvider();
		Set<SectioningRequest> queue = new TreeSet<SectioningRequest>(cmp == null ? new SectioningRequestComparator() : cmp.getComparator(server, helper));
		
		Set<XCourseId> courseIds = new HashSet<XCourseId>();
		if (newOffering != null)
			courseIds.addAll(newOffering.getCourses());
		if (oldOffering != null)
			courseIds.addAll(oldOffering.getCourses());
		
		if (server.getConfig().getPropertyBoolean("Enrollment.ReSchedulingEnabled", false) || 
				server.getConfig().getPropertyBoolean("Enrollment.ReSchedulingOnUnlock", false)) {
			// re-scheduling
			for (XCourseId course: courseIds) {
				for (XStudent[] student: students) {
					XStudent oldStudent = student[0];
					XCourseRequest oldRequest = getRequest(oldStudent, course);
					XEnrollment oldEnrollment = getEnrollment(oldRequest, offeringId);
					XStudent newStudent = student[1];
					XCourseRequest newRequest = getRequest(newStudent, course); 
					XEnrollment newEnrollment = getEnrollment(newRequest, offeringId);
					if (oldRequest == null && newRequest == null) continue;
					if (!hasReSchedulingStatus(newStudent == null ? oldStudent : newStudent, server)) continue; // no changes for students that cannot be re-scheduled
					
					OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
					action.setStudent(
							OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student[0] == null ? student[1].getStudentId() : student[0].getStudentId())
							.setExternalId(student[0] == null ? student[1].getExternalId() : student[0].getExternalId())
							.setName(student[0] == null ? student[1].getName() : student[0].getName()));
					action.addOther(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(offeringId)
							.setName(newOffering == null ? oldOffering.getName() : newOffering.getName())
							.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
					action.addOther(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(course.getCourseId())
							.setName(course.getCourseName())
							.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
					
					if (oldEnrollment != null) {
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
						for (Long sectionId: oldEnrollment.getSectionIds())
							enrollment.addSection(OnlineSectioningHelper.toProto(oldOffering.getSection(sectionId), oldEnrollment));
						action.addEnrollment(enrollment);
						if (newRequest == null)
							action.addRequest(OnlineSectioningHelper.toProto(oldRequest));
					}
					
					if (newRequest == null) {
						// nothing to re-assign, but there could be a drop
						action.setResult(OnlineSectioningLog.Action.ResultType.NULL);
						Exception exception = null;
						if (oldEnrollment != null && CustomStudentEnrollmentHolder.hasProvider()) {
							try {
								CustomStudentEnrollmentHolder.getProvider().resection(server, helper,
									new SectioningRequest(newOffering, newRequest, course, newStudent, ReschedulingReason.NO_REQUEST, getStudentPriority(newStudent, server, helper), action).setOldOffering(oldOffering).setOldStudent(oldStudent).setOldRequest(oldRequest).setLastEnrollment(oldEnrollment),
									null);
							} catch (Exception ex) {
								action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
								helper.error(course.getCourseName() + ": " + (ex.getMessage() == null ? "Unable to drop student." : ex.getMessage()), ex, action);
								exception = ex;
							}
						}
						action.setEndTime(System.currentTimeMillis());
						NotifyStudentAction notifyAction = server.createAction(NotifyStudentAction.class)
								.forStudent(oldStudent == null ? newStudent.getStudentId() : oldStudent.getStudentId())
								.fromAction(name())
								.oldEnrollment(oldOffering, course, oldEnrollment)
								.rescheduling(ReschedulingReason.NO_REQUEST);
						if (exception != null)
							notifyAction.failedEnrollment(oldOffering, course, null, exception);
						server.execute(notifyAction, helper.getUser());
						continue;
					}
					
					action.addRequest(OnlineSectioningHelper.toProto(newRequest));
					
					if (oldEnrollment == null && newEnrollment == null) {
						if (isWaitListed(newStudent, newRequest, newOffering == null ? oldOffering : newOffering, server, helper)) {
							queue.add(new SectioningRequest(newOffering, newRequest, course, newStudent, null, getStudentPriority(newStudent, server, helper), action).setOldOffering(oldOffering).setOldRequest(oldRequest).setOldStudent(oldStudent));
						}
						continue;
					}

					ReschedulingReason check = null;
					if (newEnrollment != null) {
						// new enrollment is valid and / or has all the same times
						check = check(newOffering, course, distributions, newStudent, newEnrollment, server); 
						if (check == null) {// || isSame(oldEnrollment, newEnrollment)) {
							OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
							enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
							for (XSection assignment: newOffering.getSections(newEnrollment))
								enrollment.addSection(OnlineSectioningHelper.toProto(assignment, newEnrollment));
							action.addEnrollment(enrollment);
							action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
							
							// there may have been a change
							if (CustomStudentEnrollmentHolder.hasProvider()) {
								try {
									CustomStudentEnrollmentHolder.getProvider().resection(server, helper,
											new SectioningRequest(newOffering, newRequest, course, newStudent, null, getStudentPriority(newStudent, server, helper), action).setOldOffering(oldOffering).setOldStudent(oldStudent).setOldRequest(oldRequest).setLastEnrollment(oldEnrollment),
											newEnrollment);
								} catch (Exception ex) {
									action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
									helper.error(course.getCourseName() + ": " + (ex.getMessage() == null ? "Unable to resection student." : ex.getMessage()), ex, action);
								}
							}
							action.setEndTime(System.currentTimeMillis());
							
							if (!isVerySame(newEnrollment.getCourseId(), newOffering.getSections(newEnrollment), oldOffering.getSections(oldEnrollment)))
								server.execute(server.createAction(NotifyStudentAction.class)
										.forStudent(oldStudent == null ? newStudent.getStudentId() : oldStudent.getStudentId())
										.fromAction(name()).oldEnrollment(oldOffering, course, oldEnrollment), helper.getUser());
							
							if (newOffering != null && newEnrollment.equals(newRequest.getWaitListSwapWithCourseOffering()) && isWaitListed(newStudent, newRequest, newOffering, server, helper)) {
								queue.add(new SectioningRequest(newOffering, newRequest, course, newStudent, null, getStudentPriority(newStudent, server, helper), action).setOldOffering(oldOffering).setOldRequest(oldRequest).setOldStudent(oldStudent).setLastEnrollment(oldEnrollment).setNewEnrollment(newEnrollment));
							}
							continue;
						}
					}
					newRequest = server.assign(newRequest, null);
					queue.add(new SectioningRequest(newOffering, newRequest, course, newStudent, check, getStudentPriority(newStudent, server, helper), action).setOldOffering(oldOffering).setOldRequest(oldRequest).setOldStudent(oldStudent).setLastEnrollment(oldEnrollment).setNewEnrollment(newEnrollment));
				}
			}
		} else if (newOffering != null) {
			// only wait-listing
			for (XCourseId course: courseIds) {
				for (XStudent[] student: students) {
					XStudent oldStudent = student[0];
					XStudent newStudent = student[1];
					XCourseRequest newRequest = getRequest(newStudent, course);
					XEnrollment newEnrollment = getEnrollment(newRequest, offeringId);
					if (newRequest != null && newStudent.canAssign(newRequest, WaitListMode.WaitList) && isWaitListed(student[1], newRequest, newOffering, server, helper)) {
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						action.setStudent(
								OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(student[0] == null ? student[1].getStudentId() : student[0].getStudentId())
								.setExternalId(student[0] == null ? student[1].getExternalId() : student[0].getExternalId())
								.setName(student[0] == null ? student[1].getName() : student[0].getName()));
						action.addOther(OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(offeringId)
								.setName(newOffering.getName())
								.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
						action.addOther(OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(course.getCourseId())
								.setName(course.getCourseName())
								.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
						
						XCourseRequest oldRequest = getRequest(oldStudent, course);
						XEnrollment oldEnrollment = getEnrollment(oldRequest, offeringId);
						if (oldEnrollment != null) {
							OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
							enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
							for (Long sectionId: oldEnrollment.getSectionIds())
								enrollment.addSection(OnlineSectioningHelper.toProto(oldOffering.getSection(sectionId), oldEnrollment));
							action.addEnrollment(enrollment);
						}

						action.addRequest(OnlineSectioningHelper.toProto(newRequest));
						
						queue.add(new SectioningRequest(newOffering, newRequest, course, newStudent, null, getStudentPriority(newStudent, server, helper), action).setOldOffering(oldOffering).setOldRequest(oldRequest).setOldStudent(oldStudent).setLastEnrollment(oldEnrollment).setNewEnrollment(newEnrollment));
					}
				}
			}
		}
		
		if (!queue.isEmpty()) {
			DataProperties properties = server.getConfig();
			ResectioningWeights w = new ResectioningWeights(properties);
			StudentQuality sq = new StudentQuality(server.getDistanceMetric(), properties);
			Date ts = new Date();
			int index = 1;
			for (SectioningRequest r: queue) {
				helper.debug("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getSectionIds()) + ")", r.getAction());
				long c0 = OnlineSectioningHelper.getCpuTime();
				r.getAction().setStartTime(System.currentTimeMillis());
				r.getAction().addOptionBuilder().setKey("Index").setValue(index + " of " + queue.size()); index++;
				if (r.isRescheduling())
					r.getAction().addOptionBuilder().setKey("Issue").setValue(r.getReschedulingReason().name());
				XEnrollment dropEnrollment = r.getDropEnrollment();
				XEnrollment e = r.resection(server, w, sq);
				
				if (dropEnrollment != null) {
					XOffering dropOffering = server.getOffering(dropEnrollment.getOfferingId());
					if (dropOffering != null) {
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
						for (Long sectionId: dropEnrollment.getSectionIds())
							enrollment.addSection(OnlineSectioningHelper.toProto(dropOffering.getSection(sectionId), dropEnrollment));
						r.getAction().addEnrollment(enrollment);
					}
				}
				
				if (e != null) {
					e.setTimeStamp(ts);
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.COMPUTED);
					for (Long sectionId: e.getSectionIds())
						enrollment.addSection(OnlineSectioningHelper.toProto(newOffering.getSection(sectionId), e));
					r.getAction().addEnrollment(enrollment);
				}
				
				if (e == null && !r.isRescheduling()) {
					// wait-listing only
					r.getAction().setResult(OnlineSectioningLog.Action.ResultType.FALSE);
					r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
					r.getAction().setEndTime(System.currentTimeMillis());
					continue;
				}
				
				if (CustomStudentEnrollmentHolder.hasProvider()) {
					try {
						e = CustomStudentEnrollmentHolder.getProvider().resection(server, helper, r, e);
					} catch (Exception ex) {
						r.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
						helper.warn(r.getCourseId().getCourseName() + ": " + (ex.getMessage() == null ? "Unable to resection student." : ex.getMessage()), r.getAction());
						if (r.getNewEnrollment() != null)
							server.assign(r.getRequest(), r.getNewEnrollment());
						r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
						r.getAction().setEndTime(System.currentTimeMillis());
						if (ApplicationProperty.OnlineSchedulingEmailConfirmationWhenFailed.isTrue())
							server.execute(server.createAction(NotifyStudentAction.class)
									.forStudent(r.getRequest().getStudentId()).fromAction(name())
									.failedEnrollment(newOffering, r.getCourseId(), e, ex)
									.dropEnrollment(dropEnrollment)
									.oldEnrollment(oldOffering, r.getCourseId(), r.getLastEnrollment())
									.rescheduling(r.getReschedulingReason()),
									helper.getUser());
						continue;
					}
				}
				
				if (e == null && r.getLastEnrollment() == null) {
					// remained unassigned
					r.getAction().setResult(OnlineSectioningLog.Action.ResultType.FALSE);
					r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
					r.getAction().setEndTime(System.currentTimeMillis());
					continue;
				}
				
				if (dropEnrollment != null)
					server.assign(r.getDropRequest(), null);
				if (e != null) {
					r.setRequest(server.assign(r.getRequest(), e));
				}
				helper.debug("New: " + (e == null ? "not assigned" : e.getSectionIds()), r.getAction());
				
				Date dropTS = null;
				org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudentId(), helper.getHibSession());
				WaitListMode wlMode = student.getWaitListMode();
				Map<Long, StudentClassEnrollment> enrollmentMap = new HashMap<Long, StudentClassEnrollment>();
				String approvedBy = null; Date approvedDate = null;
				for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
					StudentClassEnrollment enrl = i.next();
					if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest().getRequestId())) ||
						(enrl.getCourseOffering() != null && enrl.getCourseOffering().getUniqueId().equals(r.getCourseId().getCourseId()))) {
						helper.debug("Deleting " + enrl.getClazz().getClassLabel(), r.getAction());
						if (dropTS == null || (enrl.getTimestamp() != null && enrl.getTimestamp().before(dropTS)))
							dropTS = enrl.getTimestamp();
						enrollmentMap.put(enrl.getClazz().getUniqueId(), enrl);
						if (approvedBy == null && enrl.getApprovedBy() != null) {
							approvedBy = enrl.getApprovedBy();
							approvedDate = enrl.getApprovedDate();
						}
						enrl.getClazz().getStudentEnrollments().remove(enrl);
						helper.getHibSession().delete(enrl);
						i.remove();
					} else if (dropEnrollment != null && dropEnrollment.getCourseId().equals(enrl.getCourseOffering().getUniqueId())) {
						helper.debug("Deleting " + enrl.getClazz().getClassLabel(), r.getAction());
						enrl.getClazz().getStudentEnrollments().remove(enrl);
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
				
				if (r.getRequest().getEnrollment() != null) { // save enrollment
					org.unitime.timetable.model.CourseRequest cr = null;
					CourseOffering co = null;
					if (co == null) 
						for (CourseOffering x: io.getCourseOfferings())
							if (x.getUniqueId().equals(r.getRequest().getEnrollment().getCourseId()))
								co = x;
					for (Long sectionId: r.getRequest().getEnrollment().getSectionIds()) {
						Class_ clazz = Class_DAO.getInstance().get(sectionId, helper.getHibSession());
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
						StudentClassEnrollment old = enrollmentMap.get(sectionId);
						enrl.setTimestamp(old != null ? old.getTimestamp() : ts);
						enrl.setChangedBy(old != null ? old.getChangedBy() : helper.getUser() == null ? StudentClassEnrollment.SystemChange.SYSTEM.toString() : helper.getUser().getExternalId());
						enrl.setStudent(student);
						enrl.setApprovedBy(approvedBy);
						enrl.setApprovedDate(approvedDate);
						student.getClassEnrollments().add(enrl);
						helper.debug("Adding " + enrl.getClazz().getClassLabel(), r.getAction());
					}
					
					if (cd != null && cd.isWaitlist()) {
						cd.setWaitlist(false);
						helper.getHibSession().saveOrUpdate(cd);
						student.addWaitList(co, WaitList.WaitListType.WAIT_LIST_PORCESSING, false, helper.getUser().getExternalId(), ts, helper.getHibSession());
					} else if (cd != null) {
						student.addWaitList(co, WaitList.WaitListType.RE_BATCH_ON_RELOAD, false, helper.getUser().getExternalId(), ts, helper.getHibSession());
					}
					if (r.getRequest().isWaitlist())
						r.setRequest(server.waitlist(r.getRequest(), false));
				} else if (r.getOffering().isWaitList() && hasWaitListingStatus(r.getStudent(), server)) { // wait-list
					if (cd != null && !cd.isWaitlist()) {
						// ensure that the dropped course is the first choice 
						CourseRequest cr = (r.getCourseId() == null ? null : cd.getCourseRequest(r.getCourseId().getCourseId()));
						if (cr != null && cr.getOrder() > 0) {
							for (CourseRequest x: cd.getCourseRequests()) {
								if (x.getOrder() < cr.getOrder()) {
									x.setOrder(x.getOrder() + 1);
									helper.getHibSession().update(x);
								}
							}
							cr.setOrder(0);
							helper.getHibSession().update(cr);
						}
						// ensure that the course request is not a substitute 
						if (cd.isAlternative()) {
							int priority = cd.getPriority();
							for (CourseDemand x: cd.getStudent().getCourseDemands()) {
								if (x.isAlternative() && x.getPriority() < cd.getPriority()) {
									if (priority > x.getPriority()) priority = x.getPriority();
									x.setPriority(1 + x.getPriority());
									helper.getHibSession().update(x);
								}
							}
							cd.setPriority(priority);
							cd.setAlternative(false);
						}
						cd.setWaitlistedTimeStamp(dropTS == null ? ts : dropTS);
						cd.setWaitlist(true);
						cd.setWaitListSwapWithCourseOffering(null);
						helper.getHibSession().saveOrUpdate(cd);
						student.addWaitList(
								CourseOfferingDAO.getInstance().get(r.getCourseId().getCourseId(), helper.getHibSession()),
								WaitList.WaitListType.WAIT_LIST_PORCESSING, true, helper.getUser().getExternalId(), ts, helper.getHibSession());
					}
					if (!r.getRequest().isWaitlist()) {
						r.getRequest().setWaitListedTimeStamp(dropTS == null ? ts : dropTS);
						r.getRequest().setWaitListSwapWithCourseOffering(null);
						boolean otherRequestsChanged = false;
						XStudent xs = r.getStudent();
						// ensure that the dropped course is the first choice
						XCourseId course = r.getCourseId();
						int idx = (course == null ? -1 : r.getRequest().getCourseIds().indexOf(course));
						if (idx > 0) {
							r.getRequest().getCourseIds().remove(idx);
							r.getRequest().getCourseIds().add(0, course);
							otherRequestsChanged = true;
						}
						// ensure that the course request is not a substitute
						if (r.getRequest().isAlternative()) {
							int priority = r.getRequest().getPriority();
							for (XRequest x: xs.getRequests()) {
								if (x.isAlternative() && x.getPriority() < cd.getPriority()) {
									if (priority > x.getPriority()) priority = x.getPriority();
									x.setPriority(1 + x.getPriority());
								}
							}
							r.getRequest().setPriority(priority);
							r.getRequest().setAlternative(false);
							otherRequestsChanged = true;
						}
						if (otherRequestsChanged) {
							r.getRequest().setWaitlist(true);
							server.update(xs, true);
						} else
							r.setRequest(server.waitlist(r.getRequest(), true));
					}
				} else if (r.getLastEnrollment() != null) {
					CourseOffering co = CourseOfferingDAO.getInstance().get(r.getLastEnrollment().getCourseId(), helper.getHibSession());
					if (co != null)
						student.addWaitList(co, WaitList.WaitListType.RE_BATCH_ON_RELOAD, false, helper.getUser().getExternalId(), ts, helper.getHibSession());
				}
				
				helper.getHibSession().save(student);
			
				EnrollStudent.updateSpace(server,
						r.getRequest().getEnrollment() == null ? null : SectioningRequest.convert(r.getStudent(), r.getRequest(), server, newOffering, r.getRequest().getEnrollment(), wlMode),
						r.getLastEnrollment() == null ? null : SectioningRequest.convert(r.getOldStudent(), r.getOldRequest(), server, oldOffering, r.getLastEnrollment(), wlMode),
						newOffering, oldOffering);
				server.persistExpectedSpaces(offeringId);

				server.execute(server.createAction(NotifyStudentAction.class)
						.forStudent(r.getRequest().getStudentId())
						.fromAction(name())
						.oldEnrollment(oldOffering, r.getCourseId(), r.getLastEnrollment())
						.dropEnrollment(dropEnrollment)
						.rescheduling(r.getReschedulingReason()),
						helper.getUser());
				
				if (recheckOfferingIds != null && dropEnrollment != null && CheckOfferingAction.isCheckNeeded(server, helper, dropEnrollment,
						e != null && e.getOfferingId().equals(dropEnrollment.getOfferingId()) ? e : null)) 
					recheckOfferingIds.add(dropEnrollment.getOfferingId());
				
				r.getAction().setResult(e == null ? OnlineSectioningLog.Action.ResultType.NULL : OnlineSectioningLog.Action.ResultType.SUCCESS);
				r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				r.getAction().setEndTime(System.currentTimeMillis());
			}
		}
	}
	
	protected XCourseRequest getRequest(XStudent student, XCourseId course) {
		if (student == null) return null;
		for (XRequest r: student.getRequests())
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				for (XCourseId c: cr.getCourseIds())
					if (c.equals(course)) {
						return cr;
					}
			}
		return null;
	}
	
	protected XEnrollment getEnrollment(XCourseRequest cr, Long offeringId) {
		if (cr != null && cr.getEnrollment() != null && offeringId.equals(cr.getEnrollment().getOfferingId()))
			return cr.getEnrollment();
		return null;
	}
	
	public ReschedulingReason check(XOffering offering, XCourseId course, Collection<XDistribution> distributions, XStudent student, XEnrollment enrollment, OnlineSectioningServer server) {
		List<XSection> sections = offering.getSections(enrollment);
		XConfig config = offering.getConfig(enrollment.getConfigId());
		if (sections.size() != config.getSubparts().size()) {
			for (XSection s1: sections) {
				if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId())) {
					return ReschedulingReason.MULTIPLE_CONFIGS;
				}
			}
			return sections.size() < config.getSubparts().size() ? 
					ReschedulingReason.MISSING_CLASS : ReschedulingReason.MULTIPLE_ENRLS;
		}
		boolean ignoreBreakTime = server.getConfig().getPropertyBoolean("ReScheduling.IgnoreBreakTimeConflicts", false);
		for (XSection s1: sections) {
			for (XSection s2: sections) {
				if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(distributions, s2, ignoreBreakTime))
					return ReschedulingReason.TIME_CONFLICT;
				if (!s1.getSectionId().equals(s2.getSectionId()) && s1.getSubpartId().equals(s2.getSubpartId())) {
					return ReschedulingReason.CLASS_LINK;
				}
			}
			if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId())) {
				return ReschedulingReason.MULTIPLE_CONFIGS;
			}
		}
		if (!offering.isAllowOverlap(student, enrollment.getConfigId(), enrollment, sections) &&
			!server.getConfig().getPropertyBoolean("Enrollment.CanKeepTimeConflict", false))
			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					if (cr.getEnrollment() != null && !course.getCourseId().equals(cr.getEnrollment().getCourseId())) {
						XOffering other = server.getOffering(cr.getEnrollment().getOfferingId());
						if (other != null) {
							List<XSection> assignment = other.getSections(cr.getEnrollment());
							if (!other.isAllowOverlap(student, cr.getEnrollment().getConfigId(), cr.getEnrollment(), assignment))
								for (XSection section: sections)
									if (section.isOverlapping(offering.getDistributions(), assignment, ignoreBreakTime)) return ReschedulingReason.TIME_CONFLICT;
						}
					}
				}
			}
		if (!server.getConfig().getPropertyBoolean("Enrollment.CanKeepCancelledClass", false))
			for (XSection section: sections)
				if (section.isCancelled())
					return ReschedulingReason.CLASS_CANCELLED;
		return null;
	}

	public static boolean sameRooms(XSection s, List<XRoom> rooms) {
        if (s.getRooms() == null && rooms == null) return true;
        if (s.getRooms() == null || rooms == null) return false;
        return
        		s.getRooms().size() == rooms.size() &&
                s.getRooms().containsAll(rooms);
	}
	
	public static boolean sameTime(XSection s, XTime t) {
        if (s.getTime() == null && t == null) return true;
        if (s.getTime() == null || t == null) return false;
        return
                s.getTime().getSlot() == t.getSlot() &&
                s.getTime().getLength() == t.getLength() && 
                s.getTime().getDays() == t.getDays() && 
                ToolBox.equals(s.getTime().getDatePatternName(), t.getDatePatternName());
	}
	
    public static boolean sameName(Long courseId, XSection s1, XSection s2) {
            return s1.getName(courseId).equals(s2.getName(courseId));
    }
    
    public static boolean isVerySame(Long courseId, List<XSection> e1, List<XSection> e2) {
            if (e1.size() != e2.size()) return false;
            s1: for (XSection s1: e1) {
                    for (XSection s2: e2)
                            if (sameName(courseId, s1, s2) && sameTime(s1, s2.getTime()) && sameRooms(s1, s2.getRooms())) continue s1;
                    return false;
            }
            return true;
    }
		
	@Override
    public String name() { return "reload-offering"; }
	
	protected static class StudentPair {
		XStudent iOldStudent, iNewStudent;
		
		StudentPair(XStudent oldStudent, XStudent newStudent) {
			iOldStudent = oldStudent;
			iNewStudent = newStudent;
		}
		
		public XStudent getNewStudent() { return iNewStudent; }
		public boolean hasNewStudent() { return iNewStudent != null; }
		
		public XStudent getOldStudent() { return iOldStudent; }
		public boolean hasOldStudent() { return iOldStudent != null; }
	}
	
}
