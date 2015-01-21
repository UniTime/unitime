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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.online.selection.ResectioningWeights;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
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
		for (Long offeringId: getOfferingIds()) {
			helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(offeringId)
					.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
			
			List<Long> studentIds = (List<Long>)
					helper.getHibSession().createQuery(
							"select distinct cr.courseDemand.student.uniqueId from CourseRequest cr " +
							"where cr.courseOffering.instructionalOffering.uniqueId = :offeringId")
							.setLong("offeringId", offeringId).list();
			studentIds.addAll(
					helper.getHibSession().createQuery(
							"select distinct e.student.uniqueId from StudentClassEnrollment e " +
							"where e.courseOffering.instructionalOffering.uniqueId = :offeringId and e.courseRequest is null")
							.setLong("offeringId", offeringId).list());
			/*
			List<Long> studentIds = (List<Long>)helper.getHibSession().createQuery(
					"select distinct s.uniqueId from Student s " +
					"left outer join s.classEnrollments e " +
					"left outer join s.courseDemands d left outer join d.courseRequests r left outer join r.courseOffering co " +
					"where e.courseOffering.instructionalOffering.uniqueId = :offeringId or " +
					"co.instructionalOffering.uniqueId = :offeringId").setLong("offeringId", offeringId).list();
			*/
			
			Lock lock = server.lockOffering(offeringId, studentIds, true);
			try {
				helper.beginTransaction();
				try {
					reloadOffering(server, helper, offeringId);
					
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
		return true;			
	}
		
	public void reloadOffering(final OnlineSectioningServer server, OnlineSectioningHelper helper, Long offeringId) {
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
                "left join fetch s.academicAreaClassifications as a " +
                "left join fetch s.posMajors as mj " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "where s.uniqueId in (select xe.student.uniqueId from StudentClassEnrollment xe where xe.courseOffering.instructionalOffering.uniqueId = :offeringId) " +
                "or s.uniqueId in (select xr.courseDemand.student.uniqueId from CourseRequest xr where xr.courseOffering.instructionalOffering.uniqueId = :offeringId)"
                ).setLong("offeringId", offeringId).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		*/
		for (org.unitime.timetable.model.Student student : (List<org.unitime.timetable.model.Student>)helper.getHibSession().createQuery(
                "select distinct s from Student s " +
                "left join fetch s.courseDemands as cd " +
                "left join fetch cd.courseRequests as cr " +
                "left join fetch cr.courseOffering as co " +
                "left join fetch cr.classWaitLists as cwl " + 
                "left join fetch s.classEnrollments as e " +
                "left join fetch s.academicAreaClassifications as a " +
                "left join fetch s.posMajors as mj " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "where cr.courseOffering.instructionalOffering.uniqueId = :offeringId"
                ).setLong("offeringId", offeringId).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		for (org.unitime.timetable.model.Student student : (List<org.unitime.timetable.model.Student>)helper.getHibSession().createQuery(
                "select distinct s from Student s " +
                "left join fetch s.courseDemands as cd " +
                "left join fetch cd.courseRequests as cr " +
                "left join fetch cr.courseOffering as co " +
                "left join fetch cr.classWaitLists as cwl " + 
                "left join fetch s.classEnrollments as e " +
                "left join fetch s.academicAreaClassifications as a " +
                "left join fetch s.posMajors as mj " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "where e.courseOffering.instructionalOffering.uniqueId = :offeringId and e.courseRequest is null"
                ).setLong("offeringId", offeringId).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		
		// Persist expected spaces if needed
		if (server.needPersistExpectedSpaces(offeringId))
			PersistExpectedSpacesAction.persistExpectedSpaces(offeringId, false, server, helper);
		
		// Existing offering
		XOffering oldOffering = server.getOffering(offeringId);
		XEnrollments oldEnrollments = server.getEnrollments(offeringId); 
		if (oldOffering != null)
			server.remove(oldOffering);
		
		// New offering
		XOffering newOffering = null;
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(offeringId, helper.getHibSession());
		List<XDistribution> distributions = new ArrayList<XDistribution>();
		if (io != null) {
			// Load linked sections and ignore student conflict constraints
	    	List<DistributionPref> distPrefs = helper.getHibSession().createQuery(
	        		"select distinct p from DistributionPref p inner join p.distributionObjects o, Department d, " +
	        		"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io " +
	        		"where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId " +
	        		"and io.uniqueId = :offeringId and (o.prefGroup = c or o.prefGroup = c.schedulingSubpart) " +
	        		"and p.owner = d and p.prefLevel.prefProlog = :pref")
	        		.setString("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference())
	        		.setString("ref2", IgnoreStudentConflictsConstraint.REFERENCE)
	        		.setString("pref", PreferenceLevel.sRequired)
	        		.setLong("sessionId", server.getAcademicSession().getUniqueId())
	        		.setLong("offeringId", offeringId)
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
			
			// Load sectioning info
        	List<Object[]> infos = helper.getHibSession().createQuery(
        			"select i.clazz.uniqueId, i.nbrExpectedStudents from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId")
        			.setLong("offeringId", offeringId).list();
        	XExpectations expectations = new XExpectations(offeringId);
        	for (Object[] info : infos) {
        		Long sectionId = (Long)info[0];
        		Double expected = (Double)info[1];
        		expectations.setExpectedSpace(sectionId, expected);
        	}
        	server.update(expectations);
		}
		
		List<XStudent[]> students = new ArrayList<XStudent[]>();
		
		if (oldEnrollments != null) {
			for (XRequest old: oldEnrollments.getRequests()) {
				XStudent oldStudent = server.getStudent(old.getStudentId());
				server.remove(oldStudent);
				org.unitime.timetable.model.Student student = newStudents.get(oldStudent.getStudentId());
				if (student == null)
					student = StudentDAO.getInstance().get(oldStudent.getStudentId(), helper.getHibSession());
				XStudent newStudent = (student == null ? null : ReloadAllData.loadStudent(student, null, server, helper));
				if (newStudent != null)
					server.update(newStudent, true);
				students.add(new XStudent[] {oldStudent, newStudent});
				newStudents.remove(oldStudent.getStudentId());
			}	
		}
		for (org.unitime.timetable.model.Student student: newStudents.values()) {
			XStudent oldStudent = server.getStudent(student.getUniqueId());
			if (oldStudent != null)
				server.remove(oldStudent);
			XStudent newStudent = ReloadAllData.loadStudent(student, null, server, helper);
			if (newStudent != null)
				server.update(newStudent, true);
			students.add(new XStudent[] {oldStudent, newStudent});
		}
		
		if (!server.getAcademicSession().isSectioningEnabled())
			return;
		
		if (!CustomStudentEnrollmentHolder.isAllowWaitListing())
			return;
		
		Set<SectioningRequest> queue = new TreeSet<SectioningRequest>();
						
		for (XStudent[] student: students) {
			if (student[0] == null && student[1] == null) continue;
			XEnrollment oldEnrollment = null;
			XCourseRequest oldRequest = null;
			if (student[0] != null) {
				for (XRequest r: student[0].getRequests())
					if (r instanceof XCourseRequest && ((XCourseRequest)r).getEnrollment() != null && offeringId.equals(((XCourseRequest)r).getEnrollment().getOfferingId())) {
						oldRequest = (XCourseRequest)r;
						oldEnrollment = oldRequest.getEnrollment();
					}
			}

			XCourseRequest newRequest = null; 
			XEnrollment newEnrollment = null;
			if (student[1] != null) {
				for (XRequest r: student[1].getRequests())
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						for (XCourseId course: cr.getCourseIds())
							if (offeringId.equals(course.getOfferingId())) {
								newRequest = cr;
								if (cr.getEnrollment() != null && offeringId.equals(cr.getEnrollment().getOfferingId()))
									newEnrollment = cr.getEnrollment();
								break;
							}
					}
			}
			
			OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(student[0] == null ? student[1].getStudentId() : student[0].getStudentId())
					.setExternalId(student[0] == null ? student[1].getExternalId() : student[0].getExternalId()));
			action.addOther(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(offeringId)
					.setName(newOffering == null ? oldOffering.getName() : newOffering.getName())
					.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
			
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
				// nothing to re-assign
				action.setEndTime(System.currentTimeMillis());
				server.execute(server.createAction(NotifyStudentAction.class).forStudent(student[0] == null ? student[1].getStudentId() : student[0].getStudentId()).oldEnrollment(oldOffering, oldEnrollment), helper.getUser());
				continue;
			} else {
				action.addRequest(OnlineSectioningHelper.toProto(newRequest));
			}
			
			if (oldEnrollment == null && newEnrollment == null) {
				if (student[1].canAssign(newRequest) && isWaitListed(student[1], newRequest, server, helper))
					queue.add(new SectioningRequest(newOffering, newRequest, student[0], null, action, (oldRequest == null ? newRequest : oldRequest).getOptions(offeringId)));
				continue;
			}
			
			if (newEnrollment != null) {
				// new enrollment is valid and / or has all the same times
				if (check(newOffering, distributions, student[1], newEnrollment, server)) {// || isSame(oldEnrollment, newEnrollment)) {
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (XSection assignment: newOffering.getSections(newEnrollment))
						enrollment.addSection(OnlineSectioningHelper.toProto(assignment, newEnrollment));
					action.addEnrollment(enrollment);
					action.setEndTime(System.currentTimeMillis());
					
					if (!isVerySame(newEnrollment.getCourseId(), newOffering.getSections(newEnrollment), oldOffering.getSections(oldEnrollment)))
						server.execute(server.createAction(NotifyStudentAction.class).forStudent(student[0] == null ? student[1].getStudentId() : student[0].getStudentId()).oldEnrollment(oldOffering, oldEnrollment), helper.getUser());
					continue;
				}
			}
			newRequest = server.assign(newRequest, null);
			queue.add(new SectioningRequest(newOffering, newRequest, student[0], oldEnrollment, action, (oldRequest == null ? newRequest : oldRequest).getOptions(offeringId)));
		}
		
		if (!queue.isEmpty()) {
			DataProperties properties = new DataProperties();
			ResectioningWeights w = new ResectioningWeights(properties);
			DistanceConflict dc = new DistanceConflict(server.getDistanceMetric(), properties);
			TimeOverlapsCounter toc = new TimeOverlapsCounter(null, properties);
			Date ts = new Date();
			for (SectioningRequest r: queue) {
				helper.debug("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getSectionIds()) + ")");
				long c0 = OnlineSectioningHelper.getCpuTime();
				XEnrollment e = r.resection(server, w, dc, toc);
				if (e == null && r.getLastEnrollment() == null) { // remained unassigned
					continue;
				}
				if (e != null) {
					e.setTimeStamp(ts);
					r.setRequest(server.assign(r.getRequest(), e));
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (Long sectionId: e.getSectionIds())
						enrollment.addSection(OnlineSectioningHelper.toProto(newOffering.getSection(sectionId), e));
					r.getAction().addEnrollment(enrollment);
				}
				helper.debug("New: " + (e == null ? "not assigned" : e.getSectionIds()));
				
				org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudentId(), helper.getHibSession());
				Map<Long, StudentClassEnrollment> enrollmentMap = new HashMap<Long, StudentClassEnrollment>();
				String approvedBy = null; Date approvedDate = null;
				for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
					StudentClassEnrollment enrl = i.next();
					if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest())) ||
						(enrl.getCourseOffering() != null && enrl.getCourseOffering().getInstructionalOffering().getUniqueId().equals(offeringId))) {
						helper.debug("Deleting " + enrl.getClazz().getClassLabel());
						enrollmentMap.put(enrl.getClazz().getUniqueId(), enrl);
						if (approvedBy == null && enrl.getApprovedBy() != null) {
							approvedBy = enrl.getApprovedBy();
							approvedDate = enrl.getApprovedDate();
						}
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
						/*
						if (cr != null) {
							if (cr.getClassEnrollments() == null) cr.setClassEnrollments(new HashSet<StudentClassEnrollment>());
							cr.getClassEnrollments().add(enrl);
						}
						*/
						helper.debug("Adding " + enrl.getClazz().getClassLabel());
					}
				} else if (!r.getRequest().isAlternative()) { // wait-list
					if (cd != null && !cd.isWaitlist()) {
						cd.setWaitlist(true);
						helper.getHibSession().saveOrUpdate(cd);
					}
					if (!r.getRequest().isWaitlist())
						r.setRequest(server.waitlist(r.getRequest(), true));
				}
				
				helper.getHibSession().save(student);
			
				EnrollStudent.updateSpace(server,
						r.getRequest().getEnrollment() == null ? null : SectioningRequest.convert(server.getStudent(r.getRequest().getStudentId()), r.getRequest(), server, newOffering, r.getRequest().getEnrollment()),
						r.getLastEnrollment() == null ? null : SectioningRequest.convert(r.getOldStudent(), r.getRequest(), server, oldOffering, r.getLastEnrollment()),
						newOffering, oldOffering);
				server.persistExpectedSpaces(offeringId);

				server.execute(server.createAction(NotifyStudentAction.class).forStudent(r.getRequest().getStudentId()).oldEnrollment(oldOffering, r.getLastEnrollment()), helper.getUser());
				
				
				r.getAction().setResult(e == null ? OnlineSectioningLog.Action.ResultType.NULL : OnlineSectioningLog.Action.ResultType.SUCCESS);
				r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				r.getAction().setEndTime(System.currentTimeMillis());
			}
		}
	}
	
	public boolean check(XOffering offering, Collection<XDistribution> distributions, XStudent student, XEnrollment enrollment, OnlineSectioningServer server) {
		List<XSection> sections = offering.getSections(enrollment);
		if (sections.size() != offering.getConfig(enrollment.getConfigId()).getSubparts().size()) return false;
		for (XSection s1: sections)
			for (XSection s2: sections)
				if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(distributions, s2)) return false;
		if (!offering.isAllowOverlap(student, enrollment.getConfigId(), sections))
			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					if (cr.getEnrollment() != null && !cr.getEnrollment().getOfferingId().equals(offering.getOfferingId())) {
						XOffering other = server.getOffering(cr.getEnrollment().getOfferingId());
						if (other != null) {
							List<XSection> assignment = other.getSections(cr.getEnrollment());
							if (!other.isAllowOverlap(student, cr.getEnrollment().getConfigId(), assignment))
								for (XSection section: sections)
									if (section.isOverlapping(offering.getDistributions(), assignment)) return false;
						}
					}
				}
			}
		return true;
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
	
}
