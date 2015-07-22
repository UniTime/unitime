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
            		"e.student.uniqueId = :studentId").setLong("studentId", studentId).list(), name());
			try {
				
				helper.beginTransaction();
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
			
			action.setEndTime(System.currentTimeMillis());
		}
		
		return true;
	}
	
	@Override
    public String name() { return "reload-student"; }
}
