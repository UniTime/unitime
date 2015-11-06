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
package org.unitime.timetable.gwt.server;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class UniTimePrincipal implements Principal, Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalId;
	private String iName;
	private HashMap<Long, Long> iStudentId = new HashMap<Long, Long>();
	private String iStudentExternalId;
	
	public UniTimePrincipal(String externalId, String studentExternalId, String name) {
		if (externalId == null) throw new NullPointerException();
		iExternalId = externalId;
		iStudentExternalId = studentExternalId;
		iName = name;
		
		org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
		try {
			List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", externalId).list();
			if (!student.isEmpty()) {
				for (Student s: student) {
					addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
					iName = NameFormat.LAST_FIRST_MIDDLE.format(s);
				}
			}
		} finally {
			hibSession.close();
		}

	}
	
	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { iExternalId = externalId; }
	
	public String getStudentExternalId() { return iStudentExternalId; }
	public void setStudentExternalId(String externalId) { iStudentExternalId = externalId; }
	
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
