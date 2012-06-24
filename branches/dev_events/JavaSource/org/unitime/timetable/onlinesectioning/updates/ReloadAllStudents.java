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

import java.util.List;

import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

/**
 * @author Tomas Muller
 */
public class ReloadAllStudents extends ReloadAllData {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.load", "true"))) return false;
		Lock lock = server.lockAll();
		try {
			helper.beginTransaction();
			try {
				server.clearAllStudents();

				List<org.unitime.timetable.model.Student> students = helper.getHibSession().createQuery(
		                "select distinct s from Student s " +
		                "left join fetch s.courseDemands as cd " +
		                "left join fetch cd.courseRequests as cr " +
		                "left join fetch s.classEnrollments as e " +
		                "where s.session.uniqueId=:sessionId").
		                setLong("sessionId", server.getAcademicSession().getUniqueId()).list();
		        for (org.unitime.timetable.model.Student student: students) {
		        	Student s = loadStudent(student, server, helper);
		        	if (s != null)
		        		server.update(s);
		        }

				helper.commitTransaction();
				return true;
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
	
	@Override
    public String name() { return "reload-all-students"; }

}
