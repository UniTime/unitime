/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;

/**
 * @author Tomas Muller
 */
public class UniTimePrincipal implements Principal, Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalId;
	private String iName;
	private HashMap<Long, Long> iStudentId = new HashMap<Long, Long>();
	
	public UniTimePrincipal(String externalId, String name) {
		if (externalId == null) throw new NullPointerException();
		iExternalId = externalId;
		iName = name;
		
		org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
		try {
			List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", externalId).list();
			if (!student.isEmpty()) {
				for (Student s: student) {
					addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
					iName = s.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
				}
			}
		} finally {
			hibSession.close();
		}

	}
	
	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { iExternalId = externalId; }
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public Long getStudentId(Long sessionId) { return iStudentId.get(sessionId); }
	public void addStudentId(Long sessionId, Long studentId) { iStudentId.put(sessionId, studentId); }
	
	public int hashCode() { return iExternalId.hashCode(); }
	public boolean equals(Object o) {
		if (o == null || !(o instanceof UniTimePrincipal)) return false;
		return getExternalId().equals(((UniTimePrincipal)o).getExternalId());
	}
	public String toString() { return "UniTimePrincipal{id:" + getExternalId() + ", name:" + getName() + "}"; }	
}
