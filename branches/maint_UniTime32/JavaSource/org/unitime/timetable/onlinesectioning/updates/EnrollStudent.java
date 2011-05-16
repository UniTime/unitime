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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.SectioningInfo;
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
public class EnrollStudent implements OnlineSectioningAction<ClassAssignmentInterface> {
	private static DecimalFormat sDF = new DecimalFormat("+0.000;-0.000");
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
	public ClassAssignmentInterface execute(OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		Set<Long> offeringIds = new HashSet<Long>();
		Set<Long> lockedCourses = new HashSet<Long>();
		for (ClassAssignmentInterface.ClassAssignment ca: getAssignment())
			if (ca != null && !ca.isFreeTime()) {
				Course course = server.getCourse(ca.getCourseId());
				if (course == null)
					throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AVAILABLE, ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection());
				if (server.isOfferingLocked(course.getOffering().getId())) {
					lockedCourses.add(course.getId());
					// throw new SectioningException(SectioningExceptionType.COURSE_LOCKED, course.getName());
				} else {
					offeringIds.add(course.getOffering().getId());
				}
			}
		
		OnlineSectioningServer.Callback<Boolean> enrollmentsUpdated = new OnlineSectioningServer.Callback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Update enrollment counts failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};

		OnlineSectioningServer.Callback<Boolean> offeringChecked = new OnlineSectioningServer.Callback<Boolean>() {
			@Override
			public void onFailure(Throwable exception) {
				helper.error("Offering check failed: " + exception.getMessage(), exception);
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		};
		
		Lock lock = server.lockStudent(getStudentId(), offeringIds, true);
		try {
			helper.beginTransaction();
			try {
				new CheckAssignmentAction(getStudentId(), getAssignment()).check(server, helper);
				
				Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
				if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);

				Hashtable<Long, Class_> classes = new Hashtable<Long, Class_>();
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
					Class_ clazz = Class_DAO.getInstance().get(ca.getClassId(), helper.getHibSession());
					if (clazz == null)
						throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AVAILABLE, ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection());
					classes.put(clazz.getUniqueId(), clazz);
				}
				
				Map<Long, org.unitime.timetable.model.CourseRequest> req = SaveStudentRequests.saveRequest(server, helper, student, getRequest(), false);
				Date ts = new Date();
				
				for (ClassAssignmentInterface.ClassAssignment ca: getAssignment()) {
					if (ca == null || ca.isFreeTime() || ca.getClassId() == null) continue;
					Class_ clazz = classes.get(ca.getClassId());
					org.unitime.timetable.model.CourseRequest cr = req.get(ca.getCourseId());
					if (clazz == null || cr == null) continue;
					if (lockedCourses.contains(ca.getCourseId())) {
						ClassWaitList cwl = new ClassWaitList();
						cwl.setClazz(clazz);
						cwl.setCourseRequest(cr);
						cwl.setStudent(student);
						cwl.setType(ClassWaitList.Type.LOCKED.ordinal());
						cwl.setTimestamp(ts);
						if (cr.getClassWaitLists() == null)
							cr.setClassWaitLists(new HashSet<ClassWaitList>());
						cr.getClassWaitLists().add(cwl);
						helper.getHibSession().saveOrUpdate(cwl);
						continue;
					}
					StudentClassEnrollment enrl = new StudentClassEnrollment();
					enrl.setClazz(clazz);
					clazz.getStudentEnrollments().add(enrl);
					enrl.setCourseOffering(cr.getCourseOffering());
					enrl.setCourseRequest(cr);
					if (cr.getClassEnrollments() != null)
						cr.getClassEnrollments().add(enrl);
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
				
				if (oldStudent != null) {
					for (Request oldRequest: oldStudent.getRequests()) {
						Enrollment oldEnrollment = oldRequest.getInitialAssignment();
						if (oldEnrollment == null || !oldEnrollment.isCourseRequest()) continue; // free time or not assigned
						Enrollment newEnrollment = null;
						if (newStudent != null)
							for (Request newRequest: newStudent.getRequests())
								if (newRequest.getAssignment() != null && newRequest.getAssignment().isCourseRequest() && newRequest.getAssignment().getOffering().getId() == oldEnrollment.getOffering().getId()) {
									newEnrollment = newRequest.getAssignment(); break;
								}
						if (newEnrollment != null && newEnrollment.getSections().equals(oldEnrollment.getSections())) continue; // same assignment
						
						server.execute(new CheckOfferingAction(oldEnrollment.getOffering().getId()), offeringChecked);
						updateSpace(helper, newEnrollment, oldEnrollment);
					}
				}
				
				if (newStudent != null) {
					requests: for (Request newRequest: newStudent.getRequests()) {
						Enrollment newEnrollment = newRequest.getAssignment();
						if (newEnrollment == null || !newEnrollment.isCourseRequest()) continue; // free time or not assigned
						if (oldStudent != null)
							for (Request oldRequest: oldStudent.getRequests())
								if (oldRequest.getInitialAssignment() != null && oldRequest.getInitialAssignment().isCourseRequest() && oldRequest.getInitialAssignment().getOffering().getId() == newEnrollment.getOffering().getId())
									continue requests;
						updateSpace(helper, newEnrollment, null);
						server.execute(new UpdateEnrollmentCountsAction(newEnrollment.getOffering().getId()), enrollmentsUpdated);
					}
					
				}

				server.notifyStudentChanged(getStudentId(), (oldStudent == null ? null : oldStudent.getRequests()), (newStudent == null ? null : newStudent.getRequests()));
				
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
			}
		} finally {
			lock.release();
		}
		
		return server.getAssignment(getStudentId());
	}
	
    public static void updateSpace(OnlineSectioningHelper helper, Enrollment newEnrollment, Enrollment oldEnrollment) {
    	Map<Long, Section> sections = new Hashtable<Long, Section>();
    	if (oldEnrollment != null) {
            for (Section section : oldEnrollment.getSections()) {
                section.setSpaceHeld(section.getSpaceHeld() + 1.0);
                sections.put(section.getId(), section);
            }
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            for (Enrollment enrl : oldEnrollment.getRequest().values()) {
            	if (!enrl.getCourse().equals(oldEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : oldEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(oldEnrollment.getRequest()) || !(otherRequest instanceof CourseRequest))
                        continue;
                    Enrollment otherErollment = otherRequest.getInitialAssignment();
                    if (otherErollment == null)
                        continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps)
                    feasibleEnrollments.add(enrl);
            }
            double increment = 1.0 / feasibleEnrollments.size();
            for (Enrollment feasibleEnrollment : feasibleEnrollments) {
                for (Section section : feasibleEnrollment.getSections()) {
                    section.setSpaceExpected(section.getSpaceExpected() + increment);
                    sections.put(section.getId(), section);
                }
            }
    	}
    	if (newEnrollment != null) {
            for (Section section : newEnrollment.getSections()) {
                section.setSpaceHeld(section.getSpaceHeld() - 1.0);
                sections.put(section.getId(), section);
            }
            List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
            for (Enrollment enrl : newEnrollment.getRequest().values()) {
            	if (!enrl.getCourse().equals(newEnrollment.getCourse())) continue;
                boolean overlaps = false;
                for (Request otherRequest : newEnrollment.getRequest().getStudent().getRequests()) {
                    if (otherRequest.equals(newEnrollment.getRequest()) || !(otherRequest instanceof CourseRequest))
                        continue;
                    Enrollment otherErollment = otherRequest.getAssignment();
                    if (otherErollment == null)
                        continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps)
                    feasibleEnrollments.add(enrl);
            }
            double decrement = 1.0 / feasibleEnrollments.size();
            for (Enrollment feasibleEnrollment : feasibleEnrollments) {
                for (Section section : feasibleEnrollment.getSections()) {
                    section.setSpaceExpected(section.getSpaceExpected() - decrement);
                    sections.put(section.getId(), section);
                }
            }
    	}
    	
    	for (SectioningInfo info: (List<SectioningInfo>)helper.getHibSession().createQuery(
    			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
    			setLong("offeringId", (newEnrollment == null ? oldEnrollment : newEnrollment).getCourse().getOffering().getId()).
    			setCacheable(true).list()) {
    		Section section = sections.remove(info.getClazz().getUniqueId());
    		if (section == null) continue;
    		if (info.getNbrExpectedStudents() == section.getSpaceExpected() && info.getNbrHoldingStudents() == section.getSpaceHeld()) continue;
    		helper.debug(info.getClazz().getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(section.getSpaceExpected() - info.getNbrExpectedStudents()) +
    				", held " + sDF.format(section.getSpaceHeld() - info.getNbrHoldingStudents()));
    		if (section.getLimit() >= 0 && section.getLimit() >= info.getNbrExpectedStudents() && section.getLimit() < section.getSpaceExpected())
    			helper.info(info.getClazz().getClassLabel(helper.getHibSession()) + ": become over-expected");
    		if (section.getLimit() >= 0 && section.getLimit() < info.getNbrExpectedStudents() && section.getLimit() >= section.getSpaceExpected())
    			helper.info(info.getClazz().getClassLabel(helper.getHibSession()) + ": no longer over-expected");
    		info.setNbrExpectedStudents(section.getSpaceExpected());
    		info.setNbrHoldingStudents(section.getSpaceHeld());
    		helper.getHibSession().saveOrUpdate(info);
    	}
    	
    	if (!sections.isEmpty())
        	for (Class_ clazz: (List<Class_>)helper.getHibSession().createQuery(
        			"select c from Class_ c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
        			setLong("offeringId", (newEnrollment == null ? oldEnrollment : newEnrollment).getCourse().getOffering().getId()).
        			setCacheable(true).list()) {
        		Section section = sections.remove(clazz.getUniqueId());
        		if (section == null) continue;
                SectioningInfo info = new SectioningInfo();
        		helper.debug(clazz.getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(section.getSpaceExpected()) +
        				", held " + sDF.format(section.getSpaceHeld()) + " (new)");
        		if (section.getLimit() >= 0 && section.getLimit() < section.getSpaceExpected())
        			helper.info(clazz.getClassLabel(helper.getHibSession()) + ": become over-expected");
                info.setClazz(clazz);
                info.setNbrExpectedStudents(section.getSpaceExpected());
                info.setNbrHoldingStudents(section.getSpaceHeld());
                helper.getHibSession().saveOrUpdate(info);
        	}
    }
	
	@Override
	public String name() {
		return "enroll";
	}
}
