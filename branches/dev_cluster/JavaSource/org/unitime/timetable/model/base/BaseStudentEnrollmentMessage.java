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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.StudentEnrollmentMessage;

public abstract class BaseStudentEnrollmentMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iMessage;
	private Integer iLevel;
	private Integer iType;
	private Date iTimestamp;
	private Integer iOrder;

	private CourseDemand iCourseDemand;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_MESSAGE = "message";
	public static String PROP_MSG_LEVEL = "level";
	public static String PROP_TYPE = "type";
	public static String PROP_TIMESTAMP = "timestamp";
	public static String PROP_ORD = "order";

	public BaseStudentEnrollmentMessage() {
		initialize();
	}

	public BaseStudentEnrollmentMessage(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	public Integer getLevel() { return iLevel; }
	public void setLevel(Integer level) { iLevel = level; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public Integer getOrder() { return iOrder; }
	public void setOrder(Integer order) { iOrder = order; }

	public CourseDemand getCourseDemand() { return iCourseDemand; }
	public void setCourseDemand(CourseDemand courseDemand) { iCourseDemand = courseDemand; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentEnrollmentMessage)) return false;
		if (getUniqueId() == null || ((StudentEnrollmentMessage)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentEnrollmentMessage)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentEnrollmentMessage["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentEnrollmentMessage[" +
			"\n	CourseDemand: " + getCourseDemand() +
			"\n	Level: " + getLevel() +
			"\n	Message: " + getMessage() +
			"\n	Order: " + getOrder() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
