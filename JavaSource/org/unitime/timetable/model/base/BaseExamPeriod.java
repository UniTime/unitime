/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public abstract class BaseExamPeriod implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iDateOffset;
	private Integer iStartSlot;
	private Integer iLength;
	private Integer iEventStartOffset;
	private Integer iEventStopOffset;

	private Session iSession;
	private ExamType iExamType;
	private PreferenceLevel iPrefLevel;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DATE_OFS = "dateOffset";
	public static String PROP_START_SLOT = "startSlot";
	public static String PROP_LENGTH = "length";
	public static String PROP_EVENT_START_OFFSET = "eventStartOffset";
	public static String PROP_EVENT_STOP_OFFSET = "eventStopOffset";

	public BaseExamPeriod() {
		initialize();
	}

	public BaseExamPeriod(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getDateOffset() { return iDateOffset; }
	public void setDateOffset(Integer dateOffset) { iDateOffset = dateOffset; }

	public Integer getStartSlot() { return iStartSlot; }
	public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }

	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	public Integer getEventStartOffset() { return iEventStartOffset; }
	public void setEventStartOffset(Integer eventStartOffset) { iEventStartOffset = eventStartOffset; }

	public Integer getEventStopOffset() { return iEventStopOffset; }
	public void setEventStopOffset(Integer eventStopOffset) { iEventStopOffset = eventStopOffset; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public ExamType getExamType() { return iExamType; }
	public void setExamType(ExamType examType) { iExamType = examType; }

	public PreferenceLevel getPrefLevel() { return iPrefLevel; }
	public void setPrefLevel(PreferenceLevel prefLevel) { iPrefLevel = prefLevel; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamPeriod)) return false;
		if (getUniqueId() == null || ((ExamPeriod)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamPeriod)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExamPeriod["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExamPeriod[" +
			"\n	DateOffset: " + getDateOffset() +
			"\n	EventStartOffset: " + getEventStartOffset() +
			"\n	EventStopOffset: " + getEventStopOffset() +
			"\n	ExamType: " + getExamType() +
			"\n	Length: " + getLength() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	Session: " + getSession() +
			"\n	StartSlot: " + getStartSlot() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
