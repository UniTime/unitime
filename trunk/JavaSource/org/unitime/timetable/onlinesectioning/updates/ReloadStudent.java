/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
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
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ReloadStudent extends ReloadAllData {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Collection<Long> iStudentIds = null;
	
	public ReloadStudent forStudents(Long... studentIds) {
		iStudentIds = new ArrayList<Long>();
		for (Long studentId: studentIds)
			iStudentIds.add(studentId);
		return this;
	}
	
	public ReloadStudent forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}

	
	public Collection<Long> getStudentIds() { return iStudentIds; }
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.info(getStudentIds().size() + " students changed.");

		helper.beginTransaction();
		try {
			for (Long studentId: getStudentIds()) {
				helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(studentId)
						.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
				
				OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
				action.setStudent(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(studentId)
						.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
				
				Lock lock = server.lockStudent(studentId, (List<Long>)helper.getHibSession().createQuery(
						"select distinct e.courseOffering.instructionalOffering.uniqueId from StudentClassEnrollment e where "+
                		"e.student.uniqueId = :studentId").setLong("studentId", studentId).list(), true);
				try {
					
					// Unload student
					XStudent oldStudent = server.getStudent(studentId);
					if (oldStudent != null) {
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
						for (XRequest oldRequest: oldStudent.getRequests()) {
							if (oldRequest instanceof XCourseRequest && ((XCourseRequest)oldRequest).getEnrollment() != null) {
								XEnrollment enrl = ((XCourseRequest)oldRequest).getEnrollment();
								XOffering offering = server.getOffering(enrl.getOfferingId());
								for (XSection section: offering.getSections(enrl))
									enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
							}
						}
						action.addEnrollment(enrollment);
						server.remove(oldStudent);
						action.getStudentBuilder().setUniqueId(oldStudent.getStudentId()).setExternalId(oldStudent.getExternalId()).setName(oldStudent.getName());
					}

					// Load student
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					XStudent newStudent = null;
					if (student != null) {
						newStudent = loadStudent(student, null, server, helper);
						if (newStudent != null) {
							server.update(newStudent, true);
							OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
							enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
							for (XRequest newRequest: newStudent.getRequests()) {
								action.addRequest(OnlineSectioningHelper.toProto(newRequest));
								if (newRequest instanceof XCourseRequest && ((XCourseRequest)newRequest).getEnrollment() != null) {
									XEnrollment enrl = ((XCourseRequest)newRequest).getEnrollment();
									XOffering offering = server.getOffering(enrl.getOfferingId());
									for (XSection section: offering.getSections(enrl))
										enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
								}
							}
							action.addEnrollment(enrollment);
						}
						action.getStudentBuilder().setUniqueId(newStudent.getStudentId()).setExternalId(newStudent.getExternalId()).setName(newStudent.getName());
					}
					
					server.execute(server.createAction(NotifyStudentAction.class).forStudent(studentId).oldStudent(oldStudent), helper.getUser());

				} finally {
					lock.release();
				}
				
				action.setEndTime(System.currentTimeMillis());
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
	
	@Override
    public String name() { return "reload-student"; }
}
