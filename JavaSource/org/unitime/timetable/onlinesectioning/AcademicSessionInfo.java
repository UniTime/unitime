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
package org.unitime.timetable.onlinesectioning;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Date;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.util.DateUtils;

/**
 * @author Tomas Muller
 */
public class AcademicSessionInfo implements Comparable<AcademicSessionInfo>, Serializable, Qualifiable {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iYear, iTerm, iCampus;
	private Long iDatePatternId = null;
	private BitSet iWeekPattern = null;
	private BitSet iFreeTimePattern = null;
	private Date iSessionBegin = null;
	private Date iDatePatternFirstDate = null;
	private boolean iSectioningEnabled = false;;
	private int iWkEnroll = 1, iWkChange = 1, iWkDrop = 4;
	private String iDefaultStatus = null;
	
	public AcademicSessionInfo(Session session) {
		update(session);
	}
	
	public void update(Session session) {
		iUniqueId = session.getUniqueId();
		iYear = session.getAcademicYear();
		iTerm = session.getAcademicTerm();
		iCampus = session.getAcademicInitiative();
		if (session.getDefaultDatePattern()!=null) {
			iDatePatternId = session.getDefaultDatePattern().getUniqueId();
			iWeekPattern = session.getDefaultDatePattern().getPatternBitSet();
		}
		iFreeTimePattern = getFreeTimeBitSet(session);
		iSessionBegin = session.getSessionBeginDateTime();
		iDatePatternFirstDate = getDatePatternFirstDay(session);
		iSectioningEnabled = session.getStatusType().canOnlineSectionStudents();
		iWkEnroll = session.getLastWeekToEnroll();
		iWkChange = session.getLastWeekToChange();
		iWkDrop = session.getLastWeekToDrop();
		iDefaultStatus = session.getDefaultSectioningStatus() == null ? null : session.getDefaultSectioningStatus().getReference();
	}
	
	public AcademicSessionInfo(Long uniqueId, String year, String term, String campus) {
		iUniqueId = uniqueId;
		iYear = year;
		iTerm = term;
		iCampus = campus;
	}
	
	public static Date getDatePatternFirstDay(Session s) {
		return DateUtils.getDate(1, s.getPatternStartMonth(), s.getSessionStartYear());
	}
	
	public static BitSet getFreeTimeBitSet(Session session) {
		int startMonth = session.getPatternStartMonth();
		int endMonth = session.getPatternEndMonth();
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
	public Date getSessionBeginDate() { return iSessionBegin; }
	
	public int compareTo(AcademicSessionInfo a) {
		int cmp = iSessionBegin.compareTo(a.iSessionBegin);
		if (cmp != 0) return cmp;
		cmp = getYear().compareToIgnoreCase(a.getYear());
		if (cmp != 0) return cmp;
		cmp = getTerm().compareToIgnoreCase(a.getTerm());
		if (cmp != 0) return cmp;
		cmp = getCampus().compareToIgnoreCase(a.getCampus());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(a.getUniqueId() == null ? -1 : a.getUniqueId());
	}
	
	public String toString() {
		return getYear() + " " + getTerm() + " (" + getCampus() + ")";
	}

	public String toCompactString() {
		return getTerm() + getYear() + getCampus();
	}
	
	public boolean isSectioningEnabled() { return iSectioningEnabled; }
	public void setSectioningEnabled(boolean enable) { iSectioningEnabled = enable; }
	
	public int getLastWeekToEnroll() { return iWkEnroll; }
	public int getLastWeekToChange() { return iWkChange; }
	public int getLastWeekToDrop() { return iWkDrop; }
	
	public String getDefaultSectioningStatus() { return iDefaultStatus; }

	@Override
	public Serializable getQualifierId() {
		return getUniqueId();
	}

	@Override
	public String getQualifierType() {
		return Session.class.getSimpleName();
	}

	@Override
	public String getQualifierReference() {
		return toCompactString();
	}

	@Override
	public String getQualifierLabel() {
		return toString();
	}
}
