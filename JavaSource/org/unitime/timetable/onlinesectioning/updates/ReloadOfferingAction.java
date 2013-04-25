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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.constraint.LinkedSections;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.solver.studentsct.StudentSectioningDatabaseLoader;

/**
 * @author Tomas Muller
 */
public class ReloadOfferingAction extends WaitlistedOnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
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
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		try {
			for (Long offeringId: getOfferingIds()) {
				
				helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(offeringId)
						.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
				
				List<Long> studentIds = (List<Long>)helper.getHibSession().createQuery(
						"select distinct s.uniqueId from Student s " +
						"left outer join s.classEnrollments e " +
						"left outer join s.courseDemands d left outer join d.courseRequests r left outer join r.courseOffering co " +
						"where e.courseOffering.instructionalOffering.uniqueId = :offeringId or " +
						"co.instructionalOffering.uniqueId = :offeringId").setLong("offeringId", offeringId).list();
				Lock lock = server.lockOffering(offeringId, studentIds, true);
				try {

					reloadOffering(server, helper, offeringId);
					
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
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
		
	public void reloadOffering(final OnlineSectioningServer server, OnlineSectioningHelper helper, Long offeringId) {
		// Load new students
		Map<Long, org.unitime.timetable.model.Student> newStudents = new HashMap<Long, org.unitime.timetable.model.Student>();
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
                "left join fetch cr.classEnrollments as cre "+
                "left join fetch s.groups as g " +
                "where s.uniqueId in (select xe.student.uniqueId from StudentClassEnrollment xe where xe.courseOffering.instructionalOffering.uniqueId = :offeringId) " +
                "or s.uniqueId in (select xr.courseDemand.student.uniqueId from CourseRequest xr where xr.courseOffering.instructionalOffering.uniqueId = :offeringId)"
                ).setLong("offeringId", offeringId).list()) {
			newStudents.put(student.getUniqueId(), student);
		}
		
		// Persist expected spaces if needed
		if (server.needPersistExpectedSpaces(offeringId))
			PersistExpectedSpacesAction.persistExpectedSpaces(offeringId, false, server, helper);
		
		// Load course request options
		Hashtable<Long, OnlineSectioningLog.CourseRequestOption> options = new Hashtable<Long, OnlineSectioningLog.CourseRequestOption>();
		for (Object[] o: (List<Object[]>)helper.getHibSession().createQuery(
				"select o.courseRequest.courseDemand.student.uniqueId, o.value from CourseRequestOption o " +
				"where o.courseRequest.courseOffering.instructionalOffering.uniqueId = :offeringId and " +
				"o.optionType = :type")
				.setLong("offeringId", offeringId)
				.setInteger("type", OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT.getNumber())
				.list()) {
			Long studentId = (Long)o[0];
			try {
				options.put(studentId, OnlineSectioningLog.CourseRequestOption.parseFrom((byte[])o[1]));
			} catch (Exception e) {
				helper.warn("Unable to parse course request options for student " + studentId + ": " + e.getMessage());
			}
		}
		
		// Existing offering
		Offering oldOffering = server.getOffering(offeringId);
		if (oldOffering != null) {
			server.remove(oldOffering);
			server.removeLinkedSections(offeringId);
			// Remove ignore student conflict relations that involve the old offering
			for (Config config: oldOffering.getConfigs()) {
				for (Subpart subpart: config.getSubparts()) {
					for (Section section: subpart.getSections()) {
						if (section.getIgnoreConflictWithSectionIds() != null && !section.getIgnoreConflictWithSectionIds().isEmpty()) {
							for (Long otherSectionId: section.getIgnoreConflictWithSectionIds()) {
								Section other = server.getSection(otherSectionId);
								if (other != null && other.getIgnoreConflictWithSectionIds() != null) other.getIgnoreConflictWithSectionIds().remove(section.getId());
							}
							section.getIgnoreConflictWithSectionIds().clear();
						}
					}
				}
			}
		}
		
		// New offering
		Offering newOffering = null;
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(offeringId, helper.getHibSession());
		if (io != null) {
			newOffering = ReloadAllData.loadOffering(io, server, helper);
			if (newOffering != null)
				server.update(newOffering);
			for (CourseOffering co: io.getCourseOfferings())
				server.update(new CourseInfo(co));
			
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
	        	StudentSectioningDatabaseLoader.SectionProvider p = new StudentSectioningDatabaseLoader.SectionProvider() {
					@Override
					public Section get(Long classId) {
						return server.getSection(classId);
					}
				};
	        	for (DistributionPref pref: distPrefs) {
	        		for (Collection<Section> sections: StudentSectioningDatabaseLoader.getSections(pref, p)) {
	        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference())) {
	        				server.addLinkedSections(new LinkedSections(sections));   				
	        			} else {
	        				for (Section s1: sections)
	                			for (Section s2: sections)
	                				if (!s1.equals(s2)) s1.addIgnoreConflictWith(s2.getId());
	        			}
	        		}
	        	}
	        }
			
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
					org.unitime.timetable.model.Student student = newStudents.get(oldStudent.getId());
					if (student == null)
						student = StudentDAO.getInstance().get(oldStudent.getId(), helper.getHibSession());
					Student newStudent = (student == null ? null : ReloadAllData.loadStudent(student, server, helper));
					if (newStudent != null)
						server.update(newStudent);
					students.add(new Student[] {oldStudent, newStudent});
					newStudents.remove(oldStudent.getId());
				}	
		}
		for (org.unitime.timetable.model.Student student: newStudents.values()) {
			Student oldStudent = server.getStudent(student.getUniqueId());
			if (oldStudent != null)
				server.remove(oldStudent);
			Student newStudent = ReloadAllData.loadStudent(student, server, helper);
			if (newStudent != null)
				server.update(newStudent);
			students.add(new Student[] {oldStudent, newStudent});
		}
		
		if (!server.getAcademicSession().isSectioningEnabled())
			return;
		
		Set<SectioningRequest> queue = new TreeSet<SectioningRequest>();
						
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
			
			OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(student[0] == null ? student[1].getId() : student[0].getId())
					.setExternalId(student[0] == null ? student[1].getExternalId() : student[0].getExternalId()));
			action.addOther(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(offeringId)
					.setName(newOffering == null ? oldOffering.getName() : newOffering.getName())
					.setType(OnlineSectioningLog.Entity.EntityType.OFFERING));
			
			if (oldEnrollment != null) {
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
				for (Assignment assignment: oldEnrollment.getAssignments())
					enrollment.addSection(OnlineSectioningHelper.toProto(assignment, oldEnrollment));
				action.addEnrollment(enrollment);
				if (newRequest == null)
					action.addRequest(OnlineSectioningHelper.toProto(oldEnrollment.getRequest()));
			}
			
			if (newRequest == null) {
				// nothing to re-assign
				action.setEndTime(System.currentTimeMillis());
				server.notifyStudentChanged(student[0] == null ? student[1].getId() : student[0].getId(), oldEnrollment.getRequest(), oldEnrollment, helper.getUser());
				continue;
			} else {
				action.addRequest(OnlineSectioningHelper.toProto(newRequest));
			}
			
			if (oldEnrollment == null && newEnrollment == null) {
				if (newRequest.getStudent().canAssign(newRequest) && isWaitListed(newRequest, server, helper))
					queue.add(new SectioningRequest(newOffering, newRequest, student[0], null, action,
							options.get(student[0] == null ? student[1].getId() : student[0].getId())));
				continue;
			}
			
			if (newEnrollment != null) {
				// new enrollment is valid and / or has all the same times
				if (check(newEnrollment)) {// || isSame(oldEnrollment, newEnrollment)) {
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (Assignment assignment: newEnrollment.getAssignments())
						enrollment.addSection(OnlineSectioningHelper.toProto(assignment, newEnrollment));
					action.addEnrollment(enrollment);
					action.setEndTime(System.currentTimeMillis());
					
					if (!ResectioningWeights.isVerySame(newEnrollment, oldEnrollment))
						server.notifyStudentChanged(student[0] == null ? student[1].getId() : student[0].getId(), newRequest, oldEnrollment, helper.getUser());
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
			queue.add(new SectioningRequest(newOffering, newRequest, student[0], oldEnrollment, action,
					options.get(student[0] == null ? student[1].getId() : student[0].getId())));
		}
		
		if (!queue.isEmpty()) {
			DataProperties properties = new DataProperties();
			ResectioningWeights w = new ResectioningWeights(properties);
			DistanceConflict dc = new DistanceConflict(server.getDistanceMetric(), properties);
			TimeOverlapsCounter toc = new TimeOverlapsCounter(null, properties);
			Date ts = new Date();
			for (SectioningRequest r: queue) {
				helper.info("Resectioning " + r.getRequest() + " (was " + (r.getLastEnrollment() == null ? "not assigned" : r.getLastEnrollment().getAssignments()) + ")");
				long c0 = OnlineSectioningHelper.getCpuTime();
				Enrollment e = r.resection(server, w, dc, toc);
				if (e == null && r.getLastEnrollment() == null) { // remained unassigned
					continue;
				}
				if (e != null) {
					Lock wl = server.writeLock();
					try {
						r.getRequest().setInitialAssignment(e);
						r.getRequest().assign(0, e);
					} finally {
						wl.release();
					}
					e.setTimeStamp(ts.getTime());
					OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
					enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
					for (Assignment assignment: e.getAssignments())
						enrollment.addSection(OnlineSectioningHelper.toProto(assignment, e));
					r.getAction().addEnrollment(enrollment);
				}
				helper.info("New: " + (r.getRequest().getAssignment() == null ? "not assigned" : r.getRequest().getAssignment().getAssignments()));
				
				org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(r.getRequest().getStudent().getId(), helper.getHibSession());
				Map<Long, StudentClassEnrollment> oldEnrollments = new HashMap<Long, StudentClassEnrollment>();
				String approvedBy = null; Date approvedDate = null;
				for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext();) {
					StudentClassEnrollment enrl = i.next();
					if ((enrl.getCourseRequest() != null && enrl.getCourseRequest().getCourseDemand().getUniqueId().equals(r.getRequest().getId())) ||
						(enrl.getCourseOffering() != null && enrl.getCourseOffering().getInstructionalOffering().getUniqueId().equals(offeringId))) {
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
				demands: for (CourseDemand x: student.getCourseDemands())
					for (org.unitime.timetable.model.CourseRequest q: x.getCourseRequests())
						if (q.getCourseOffering().getInstructionalOffering().getUniqueId().equals(offeringId)) {
							cd = x;
							break demands;
						}
				
				if (r.getRequest().getAssignment() != null) { // save enrollment
					org.unitime.timetable.model.CourseRequest cr = null;
					CourseOffering co = null;
					if (co == null) 
						for (CourseOffering x: io.getCourseOfferings())
							if (x.getUniqueId().equals(r.getRequest().getAssignment().getCourse().getId()))
								co = x;
					for (Section section: r.getRequest().getAssignment().getSections()) {
						Class_ clazz = Class_DAO.getInstance().get(section.getId(), helper.getHibSession());
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
						StudentClassEnrollment old = oldEnrollments.get(section.getId());
						enrl.setTimestamp(old != null ? old.getTimestamp() : ts);
						enrl.setChangedBy(old != null ? old.getChangedBy() : helper.getUser() == null ? StudentClassEnrollment.SystemChange.SYSTEM.toString() : helper.getUser().getExternalId());
						enrl.setStudent(student);
						enrl.setApprovedBy(approvedBy);
						enrl.setApprovedDate(approvedDate);
						student.getClassEnrollments().add(enrl);
						if (cr != null) {
							if (cr.getClassEnrollments() == null) cr.setClassEnrollments(new HashSet<StudentClassEnrollment>());
							cr.getClassEnrollments().add(enrl);
						}
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
				server.persistExpectedSpaces(offeringId);

				server.notifyStudentChanged(r.getRequest().getStudent().getId(),
						r.getRequest(), r.getLastEnrollment(), helper.getUser());
				
				r.getAction().setResult(e == null ? OnlineSectioningLog.Action.ResultType.NULL : OnlineSectioningLog.Action.ResultType.SUCCESS);
				r.getAction().setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				r.getAction().setEndTime(System.currentTimeMillis());
			}			
		}
		
		/*
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
         */
	}
	
	public boolean check(Enrollment e) {
		if (e.getSections().size() != e.getConfig().getSubparts().size()) return false;
		for (Section s1: e.getSections())
			for (Section s2: e.getSections())
				if (s1.getId() < s2.getId() && s1.isOverlapping(s2)) return false;
		for (Request r: e.getStudent().getRequests()) {
			if (r.getId() != e.getRequest().getId() && r.getInitialAssignment() != null && r.getInitialAssignment().isOverlapping(e)) return false;
		}
		return true;
	}

		
	@Override
    public String name() { return "reload-offering"; }
	
}
