/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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

import java.util.Collection;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XStudent;
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
	
	public ChangeStudentStatus forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	public ChangeStudentStatus withStatus(String status) {
		iStatus = status;
		return this;
	}
	
	public String getStatus() { return iStatus; }
	public boolean hasStatus() { return iStatus != null && !iStatus.isEmpty(); }
	
	public Collection<Long> getStudentIds() { return iStudentIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		StudentSectioningStatus status = (hasStatus() ? (StudentSectioningStatus)helper.getHibSession().createQuery(
				"from StudentSectioningStatus where reference = :ref").setString("ref", getStatus()).uniqueResult() : null);
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, true);
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
						
						student.setStatus(getStatus());
						dbStudent.setSectioningStatus(status);
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
