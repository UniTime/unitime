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
package org.unitime.timetable.webutil.timegrid;

import java.io.Serializable;
import java.util.BitSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

/**
 * @author Tomas Muller
 */
public class TimetableGridContext implements Serializable {
	private static final long serialVersionUID = 1L;
	private String iFilter;
	private int iStartDayDayOfWeek;
	private int iResourceType;
	private int iFirstDay;
	private int iBgMode;
	private boolean iShowEvents;
	private int iFirstSlot, iLastSlot, iDayCode;
	private BitSet iPattern;
	private float iNrWeeks;
	private int iSlotsPerWeek;
	
	public TimetableGridContext() {}
	
	public TimetableGridContext(TimetableGridTable table, Session session) {
		DatePattern dp = session.getDefaultDatePatternNotNull();
		iFilter = table.getFindString();
		iStartDayDayOfWeek = Constants.getDayOfWeek(DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()));
		iFirstDay = (table.getWeek() == -100 ? -1 : DateUtils.getFirstDayOfWeek(session.getSessionStartYear(), table.getWeek()) - session.getDayOfYear(1, session.getPatternStartMonth()) - 1);
		iResourceType = table.getResourceType();
		iBgMode = table.getBgMode();
		iShowEvents = table.getShowEvents();
		iFirstSlot = table.firstSlot();
		iLastSlot = table.lastSlot();
		iDayCode = 0;
		for (int day = table.startDay(); day <= table.endDay(); day ++)
			iDayCode += Constants.DAY_CODES[day];
		iPattern = dp.getPatternBitSet();
		iNrWeeks = dp.getEffectiveNumberOfWeeks();
		
		if (iFirstDay < 0 && ApplicationProperty.TimetableGridUtilizationSkipHolidays.isTrue()) {
			int nrDays = 0;
			int idx = -1;
			int daysInWeek[] = new int[] {0, 0, 0, 0, 0, 0, 0};
	        while ((idx = iPattern.nextSetBit(1 + idx)) >= 0) {
	        	int dow = ((idx + iStartDayDayOfWeek) % 7);
	        	if ((iDayCode & Constants.DAY_CODES[dow]) != 0) {
	        		nrDays ++;
	        		daysInWeek[dow] ++;
	        	}
	        }
	        float weekDays = 1f / (table.endDay() - table.startDay() + 1);
	        if (weekDays >= 0.2f) {
	        	iNrWeeks = weekDays * nrDays;
	        } else {
	        	iNrWeeks = 0.2f * (daysInWeek[0] + daysInWeek[1] + daysInWeek[2] + daysInWeek[3] + daysInWeek[4]);
	        }
		}
		iSlotsPerWeek = (iLastSlot - iFirstSlot + 1) * (table.endDay() - table.startDay() + 1);
	}
	
	public String getFilter() { return iFilter; }
	
	public int getResourceType() { return iResourceType; }
	
	public int getFirstDay() { return iFirstDay; }
	
	public int getBgMode() { return iBgMode; }
	
	public boolean isShowEvents() { return iShowEvents; }
	
	public int getStartDayDayOfWeek() { return iStartDayDayOfWeek; }
	
	public int getFirstSlot() { return iFirstSlot; }
	
	public int getLastSlot() { return iLastSlot; }
	
	public int getDayCode() { return iDayCode; }
	
	public BitSet getDefaultDatePattern() { return iPattern; }
	
	public float getNumberOfWeeks() { return (iFirstDay >= 0 ? 1.0f : iNrWeeks); }
	
	public int getSlotsPerWeek() { return iSlotsPerWeek; }
}
