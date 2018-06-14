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

import java.util.Collection;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentNote;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ChangeStudentStatus implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private Collection<Long> iStudentIds = null;
	private String iStatus = null;
	private String iNote = null;
	
	public ChangeStudentStatus forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	public ChangeStudentStatus withStatus(String status) {
		iStatus = status;
		return this;
	}
	
	public ChangeStudentStatus withNote(String note) {
		iNote = note;
		return this;
	}
	
	public String getStatus() { return iStatus; }
	public boolean hasStatus() { return iStatus != null && !iStatus.isEmpty(); }
	public boolean changeStatus() { return !"-".equals(iStatus); }

	public String getNote() { return iNote; }
	public boolean hasNote() { return iNote != null; }

	public Collection<Long> getStudentIds() { return iStudentIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		StudentSectioningStatus status = (changeStatus() && hasStatus() ? (StudentSectioningStatus)helper.getHibSession().createQuery(
				"from StudentSectioningStatus where reference = :ref").setString("ref", getStatus()).uniqueResult() : null);
		Date ts = new Date();
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, name());
			try {
				XStudent student = server.getStudent(studentId);
				helper.beginTransaction();
				try {
					Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					if (student != null && dbStudent != null) {
						
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student.getStudentId())
							.setExternalId(student.getExternalId())
							.setName(student.getName()));
						if (status != null) {
							action.addOther(OnlineSectioningLog.Entity.newBuilder()
									.setUniqueId(status.getUniqueId())
									.setName(status.getLabel())
									.setExternalId(status.getReference())
									.setType(OnlineSectioningLog.Entity.EntityType.OTHER));
						}
						
						if (hasNote()) {
							action.addMessage(OnlineSectioningLog.Message.newBuilder().setText(getNote()).setTimeStamp(ts.getTime()).setLevel(OnlineSectioningLog.Message.Level.INFO));
							StudentNote note = new StudentNote();
							note.setStudent(dbStudent);
							note.setTextNote(getNote());
							note.setTimeStamp(ts);
							note.setUserId(helper.getUser().getExternalId());
							dbStudent.addTonotes(note);
							student.setLastNote(new XStudentNote(note));
						}
						
						if (changeStatus()) {
							student.setStatus(status == null ? null : status.getReference());
							dbStudent.setSectioningStatus(status);
						}
						
						helper.getHibSession().saveOrUpdate(dbStudent);
						server.update(student, false);
					}
					helper.commitTransaction();
				} catch (Exception e) {
					helper.rollbackTransaction();
					if (e instanceof SectioningException) throw (SectioningException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				}

			} finally {
				lock.release();
			}
		}
		return true;			
	}

	@Override
	public String name() {
		return "status-change";
	}

}
