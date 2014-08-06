/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.dom4j.Document;
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
	private Document iDetail;

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

	public Document getDetail() { return iDetail; }
	public void setDetail(Document detail) { iDetail = detail; }

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
