/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server;

import java.util.BitSet;
import java.util.Date;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.DateUtils;

public class AcademicSessionInfo implements Comparable<AcademicSessionInfo> {
	private Long iUniqueId;
	private String iYear, iTerm, iCampus;
	private Long iDatePatternId = null;
	private BitSet iWeekPattern = null;
	private BitSet iFreeTimePattern = null;
	private Date iSessionBegin = null;
	private Date iDatePatternFirstDate = null;
	
	public AcademicSessionInfo(Session session) {
		iUniqueId = session.getUniqueId();
		iYear = session.getAcademicYear();
		iTerm = session.getAcademicTerm();
		iCampus = session.getAcademicInitiative();
		if (session.getDefaultDatePattern()!=null) {
			iDatePatternId = session.getDefaultDatePattern().getUniqueId();
			iWeekPattern = session.getDefaultDatePattern().getPatternBitSet();
		}
		iFreeTimePattern = getFreeTimeBitSet(session);
		iSessionBegin = session.getEventBeginDate();
		iDatePatternFirstDate = getDatePatternFirstDay(session);
	}
	
	public AcademicSessionInfo(Long uniqueId, String year, String term, String campus) {
		iUniqueId = uniqueId;
		iYear = year;
		iTerm = term;
		iCampus = campus;
	}
	
	public static Date getDatePatternFirstDay(Session s) {
		return DateUtils.getDate(1, s.getStartMonth() - 3, s.getSessionStartYear());
	}
	
	public static BitSet getFreeTimeBitSet(Session session) {
		int startMonth = session.getStartMonth() - 3;
		int endMonth = session.getEndMonth() + 3;
		int size = DateUtils.getDayOfYear(0, endMonth + 1, session.getSessionStartYear()) - DateUtils.getDayOfYear(1, startMonth, session.getSessionStartYear());
		BitSet ret = new BitSet(size);
		for (int i = 0; i < size; i++)
			ret.set(i);
		return ret;
	}

	
	public Long getUniqueId() { return iUniqueId; }
	public String getTerm() { return iTerm; }
	public String getCampus() { return iCampus; }
	public String getYear() { return iYear; }
	
	public Long getDefaultDatePatternId() { return iDatePatternId; }
	public BitSet getDefaultWeekPattern() { return iWeekPattern; }
	public BitSet getFreeTimePattern() { return iFreeTimePattern; }
	public Date getDatePatternFirstDate() { return iDatePatternFirstDate; }
	
	public int compareTo(AcademicSessionInfo a) {
		int cmp = iSessionBegin.compareTo(a.iSessionBegin);
		if (cmp != 0) return cmp;
		cmp = getYear().compareToIgnoreCase(a.getYear());
		if (cmp != 0) return cmp;
		cmp = getTerm().compareToIgnoreCase(a.getTerm());
		if (cmp != 0) return cmp;
		cmp = getCampus().compareToIgnoreCase(a.getCampus());
		if (cmp != 0) return cmp;
		return getUniqueId().compareTo(a.getUniqueId());
	}
	
	public String toString() {
		return getYear() + " " + getTerm() + " (" + getCampus() + ")";
	}

	public String toCompactString() {
		return getTerm() + getYear() + getCampus();
	}
}
