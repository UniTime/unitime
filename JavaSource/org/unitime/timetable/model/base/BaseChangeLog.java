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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseChangeLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private String iObjectType;
	private String iObjectTitle;
	private Long iObjectUniqueId;
	private String iSourceString;
	private String iOperationString;
	private byte[] iDetail;

	private Session iSession;
	private TimetableManager iManager;
	private SubjectArea iSubjectArea;
	private Department iDepartment;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_OBJ_TYPE = "objectType";
	public static String PROP_OBJ_TITLE = "objectTitle";
	public static String PROP_OBJ_UID = "objectUniqueId";
	public static String PROP_SOURCE = "sourceString";
	public static String PROP_OPERATION = "operationString";
	public static String PROP_DETAIL = "detail";

	public BaseChangeLog() {
		initialize();
	}

	public BaseChangeLog(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public String getObjectType() { return iObjectType; }
	public void setObjectType(String objectType) { iObjectType = objectType; }

	public String getObjectTitle() { return iObjectTitle; }
	public void setObjectTitle(String objectTitle) { iObjectTitle = objectTitle; }

	public Long getObjectUniqueId() { return iObjectUniqueId; }
	public void setObjectUniqueId(Long objectUniqueId) { iObjectUniqueId = objectUniqueId; }

	public String getSourceString() { return iSourceString; }
	public void setSourceString(String sourceString) { iSourceString = sourceString; }

	public String getOperationString() { return iOperationString; }
	public void setOperationString(String operationString) { iOperationString = operationString; }

	public byte[] getDetail() { return iDetail; }
	public void setDetail(byte[] detail) { iDetail = detail; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public TimetableManager getManager() { return iManager; }
	public void setManager(TimetableManager manager) { iManager = manager; }

	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ChangeLog)) return false;
		if (getUniqueId() == null || ((ChangeLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ChangeLog)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ChangeLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ChangeLog[" +
			"\n	Department: " + getDepartment() +
			"\n	Detail: " + getDetail() +
			"\n	Manager: " + getManager() +
			"\n	ObjectTitle: " + getObjectTitle() +
			"\n	ObjectType: " + getObjectType() +
			"\n	ObjectUniqueId: " + getObjectUniqueId() +
			"\n	OperationString: " + getOperationString() +
			"\n	Session: " + getSession() +
			"\n	SourceString: " + getSourceString() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
