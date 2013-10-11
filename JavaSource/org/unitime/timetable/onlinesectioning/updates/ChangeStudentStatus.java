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

import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class ChangeStudentStatus implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;

	private Collection<Long> iStudentIds = null;
	private String iStatus = null;
	
	public ChangeStudentStatus(Collection<Long> studentIds, String status) {
		iStudentIds = studentIds;
		iStatus = status;
	}
	
	public String getStatus() { return iStatus; }
	public boolean hasStatus() { return iStatus != null && !iStatus.isEmpty(); }
	
	public Collection<Long> getStudentIds() { return iStudentIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		StudentSectioningStatus status = (hasStatus() ?
				(StudentSectioningStatus)helper.getHibSession().createQuery(
						"from StudentSectioningStatus where reference = :ref").setString("ref", getStatus()).uniqueResult()
				: null);
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, true);
			try {
				net.sf.cpsolver.studentsct.model.Student student = server.getStudent(studentId);
				Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
				if (student != null && dbStudent != null) {
					student.setStatus(getStatus());
					dbStudent.setSectioningStatus(status);
					helper.getHibSession().saveOrUpdate(dbStudent);
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
