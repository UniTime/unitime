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
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectHistory;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentSectHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Document iData;
	private Integer iType;
	private Date iTimestamp;

	private Student iStudent;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DATA = "data";
	public static String PROP_TYPE = "type";
	public static String PROP_TIMESTAMP = "timestamp";

	public BaseStudentSectHistory() {
		initialize();
	}

	public BaseStudentSectHistory(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Document getData() { return iData; }
	public void setData(Document data) { iData = data; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSectHistory)) return false;
		if (getUniqueId() == null || ((StudentSectHistory)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSectHistory)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentSectHistory["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentSectHistory[" +
			"\n	Data: " + getData() +
			"\n	Student: " + getStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
