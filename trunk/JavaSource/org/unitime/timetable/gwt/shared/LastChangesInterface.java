/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.shared;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class LastChangesInterface implements IsSerializable {
	
	public static class ChangeLogInterface implements IsSerializable {
		private Long iId;
		private String iPage;
		private String iObject;
		private String iOperation;
		private String iManager;
		private Date iDate;
		private String iDepartment;
		private Long iDepartmentId;
		private String iSubject;
		private Long iSubjectId;
		private String iSession;
		private Date iSessionDate;
		private String iSessionInitiative;
		private Long iSessionId;
		
		public ChangeLogInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getPage() { return iPage; }
		public void setPage(String page) { iPage = page; }
		
		public String getObject() { return iObject; }
		public void setObject(String object) { iObject = object; }
		
		public String getOperation() { return iOperation; }
		public void setOperation(String operation) { iOperation = operation; }
		
		public String getManager() { return iManager; }
		public void setManager(String manager) { iManager = manager; }
		
		public Date getDate() { return iDate; }
		public void setDate(Date date) { iDate = date; }
		
		public String getDepartment() { return iDepartment; }
		public void setDepartment(String department) { iDepartment = department; }
		
		public Long getDepartmentId() { return iDepartmentId; }
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		
		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
		
		public String getSession() { return iSession; }
		public void setSession(String session) { iSession = session; }
		
		public Date getSessionDate() { return iSessionDate; }
		public void setSessionDate(Date sessionDate) { iSessionDate = sessionDate; }

		public String getSessionInitiative() { return iSessionInitiative; }
		public void setSessionInitiative(String sessionInitiative) { iSessionInitiative = sessionInitiative; }

		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
	}

	
	public static class LastChangesRequest implements GwtRpcRequest<GwtRpcResponseList<ChangeLogInterface>> {
		private String iObjectType;
		private Long iObjectId;
		private Map<String, String> iOptions = new HashMap<String, String>();
		
		public LastChangesRequest() {}
		
		public String getObjectType() { return iObjectType; }
		public void setObjectType(String objectType) { iObjectType = objectType; }
		
		public Long getObjectId() { return iObjectId; }
		public void setObjectId(Long objectId) { iObjectId = objectId; }
		
		public void setOption(String name, String value) { iOptions.put(name, value); }
		public String getOption(String name) { return iOptions.get(name); }
		public String getOption(String name, String defaultValue) {
			String value = iOptions.get(name);
			return (value == null ? defaultValue : value);
		}
		public boolean hasOption(String name) { return iOptions.containsKey(name); }
		
		public static LastChangesRequest createRequest(String objectType, Long objectId, String... options) {
			LastChangesRequest request = new LastChangesRequest();
			request.setObjectType(objectType);
			request.setObjectId(objectId);
			for (int i = 0; i < options.length / 2; i ++)
				request.setOption(options[2*i], options[2*i + 1]);
			return request;
		}
		
		@Override
		public String toString() {
			return getObjectType() + "@" + getObjectId() + (iOptions.isEmpty() ? "" : " " + iOptions);
		}
	}
}
