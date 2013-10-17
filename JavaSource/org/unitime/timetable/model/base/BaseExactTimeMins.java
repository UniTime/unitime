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

import org.unitime.timetable.model.ExactTimeMins;

/**
 * @author Tomas Muller
 */
public abstract class BaseExactTimeMins implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iMinsPerMtgMin;
	private Integer iMinsPerMtgMax;
	private Integer iNrSlots;
	private Integer iBreakTime;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_MINS_MIN = "minsPerMtgMin";
	public static String PROP_MINS_MAX = "minsPerMtgMax";
	public static String PROP_NR_SLOTS = "nrSlots";
	public static String PROP_BREAK_TIME = "breakTime";

	public BaseExactTimeMins() {
		initialize();
	}

	public BaseExactTimeMins(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getMinsPerMtgMin() { return iMinsPerMtgMin; }
	public void setMinsPerMtgMin(Integer minsPerMtgMin) { iMinsPerMtgMin = minsPerMtgMin; }

	public Integer getMinsPerMtgMax() { return iMinsPerMtgMax; }
	public void setMinsPerMtgMax(Integer minsPerMtgMax) { iMinsPerMtgMax = minsPerMtgMax; }

	public Integer getNrSlots() { return iNrSlots; }
	public void setNrSlots(Integer nrSlots) { iNrSlots = nrSlots; }

	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExactTimeMins)) return false;
		if (getUniqueId() == null || ((ExactTimeMins)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExactTimeMins)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExactTimeMins["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExactTimeMins[" +
			"\n	BreakTime: " + getBreakTime() +
			"\n	MinsPerMtgMax: " + getMinsPerMtgMax() +
			"\n	MinsPerMtgMin: " + getMinsPerMtgMin() +
			"\n	NrSlots: " + getNrSlots() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
