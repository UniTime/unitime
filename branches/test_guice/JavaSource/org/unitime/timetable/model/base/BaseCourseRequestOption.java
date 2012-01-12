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

import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;

public abstract class BaseCourseRequestOption implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iOptionType;
	private byte[] iValue;

	private CourseRequest iCourseRequest;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_OPTION_TYPE = "optionType";
	public static String PROP_VALUE = "value";

	public BaseCourseRequestOption() {
		initialize();
	}

	public BaseCourseRequestOption(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getOptionType() { return iOptionType; }
	public void setOptionType(Integer optionType) { iOptionType = optionType; }

	public byte[] getValue() { return iValue; }
	public void setValue(byte[] value) { iValue = value; }

	public CourseRequest getCourseRequest() { return iCourseRequest; }
	public void setCourseRequest(CourseRequest courseRequest) { iCourseRequest = courseRequest; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseRequestOption)) return false;
		if (getUniqueId() == null || ((CourseRequestOption)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseRequestOption)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseRequestOption["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseRequestOption[" +
			"\n	CourseRequest: " + getCourseRequest() +
			"\n	OptionType: " + getOptionType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
