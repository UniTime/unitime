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
package org.unitime.timetable.server.solver;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.server.solver.TimetableGridHelper.DisplayMode;
import org.unitime.timetable.server.solver.TimetableGridHelper.ResourceType;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class TimetableGridContext implements Serializable {
	private static final long serialVersionUID = 1L;
	private String iFilter;
	private int iStartDayDayOfWeek;
	private int iResourceType;
	private int iFirstDay, iFirstSessionDay;
	private int iBgMode, iDispMode;
	private boolean iShowEvents;
	private int iFirstSlot, iLastSlot, iDayCode;
	private BitSet iPattern;
	private float iNrWeeks;
	private int iSlotsPerWeek;
	private Date iFirstDate;
	private boolean iShowInstructor, iShowTime, iShowPreference, iShowFreeTimes, iShowDate;
	private Date iSessionStartDate, iSessionEndDate;
	private boolean iShowCrossLists, iShowClassSuffix, iShowConfigName, iShowClassNameTwoLines, iShowCourseTitle;
	private String iLocale;
	private String iInstructorNameFormat = NameFormat.SHORT.reference();
	private int iWeekOffset = 0;
	
	public TimetableGridContext() {}
	
	public TimetableGridContext(FilterInterface filter, Session session) {
        iWeekOffset = ApplicationProperty.TimeGridFirstDayOfWeek.intValue();
		iLocale = Localization.getLocale();
		DatePattern dp = session.getDefaultDatePatternNotNull();
		if (dp == null)
			throw new RuntimeException("No default date pattern is defined for " + session.getLabel() + ". Use the <a href='sessionEdit.do?doit=editSession&sessionId=" + session.getUniqueId() + "'>Edit Academic Session</a> page to set a default date pattern.");
		iFilter = filter.getParameterValue("filter", "");
		iStartDayDayOfWeek = Constants.getDayOfWeek(DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()));
		int week = Integer.parseInt(filter.getParameterValue("weeks", "-100"));
		iFirstDay = (week == -100 ? -1 : DateUtils.getFirstDayOfWeek(session.getSessionStartYear(), week) - session.getDayOfYear(1, session.getPatternStartMonth()) - 1 + iWeekOffset);
		iResourceType = Integer.valueOf(filter.getParameterValue("resource", "0"));
		iBgMode = Integer.valueOf(filter.getParameterValue("background", "0"));
		iShowEvents = "1".equals(filter.getParameterValue("showEvents"));
		String[] times = filter.getParameterValue("times", "0|288|6").split("\\|");
		iFirstSlot = Integer.parseInt(times[0]);
		int step = Integer.parseInt(times[2]);
		int nrTimes = (Integer.parseInt(times[1]) - iFirstSlot) / step;
		iLastSlot = iFirstSlot + (step * nrTimes) - 1;
		iDayCode = 0;
		int nrWeekDays = 0;
		String bitmap = filter.getParameterValue("days", "1111111");
		for (int i = 0; i < bitmap.length() && i < 7; i++)
			if (bitmap.charAt(i) == '1') {
				iDayCode += Constants.DAY_CODES[i];
				nrWeekDays ++;
			}
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
	        float weekDays = 1f / nrWeekDays;
	        if (weekDays >= 0.2f) {
	        	iNrWeeks = weekDays * nrDays;
	        } else {
	        	iNrWeeks = 0.2f * (daysInWeek[0] + daysInWeek[1] + daysInWeek[2] + daysInWeek[3] + daysInWeek[4]);
	        }
		}
		iSlotsPerWeek = (iLastSlot - iFirstSlot + 1) * nrWeekDays;
		
		int startWeek = DateUtils.getWeek(session.getSessionBeginDateTime()) - ApplicationProperty.SessionNrExcessDays.intValue()/7;
		iFirstDate = DateUtils.getStartDate(session.getSessionStartYear(), startWeek);
		iFirstSessionDay = DateUtils.getFirstDayOfWeek(session.getSessionStartYear(),startWeek) - session.getDayOfYear(1,session.getPatternStartMonth()) - 1;
		
		iShowInstructor = "1".equals(filter.getParameterValue("showInstructors"));
		iShowTime = "1".equals(filter.getParameterValue("showTimes"));
		iShowPreference = "1".equals(filter.getParameterValue("showPreferences"));
		iShowFreeTimes = "1".equals(filter.getParameterValue("showFreeTimes"));
		iShowDate = "-100".equals(filter.getParameterValue("weeks"));
		iDispMode = Integer.parseInt(filter.getParameterValue("dispMode", "0"));
		
		Calendar startDateCal = Calendar.getInstance(Locale.US);
        startDateCal.setTime(DateUtils.getDate(1, session.getStartMonth(), session.getSessionStartYear()));
        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
        startDateCal.set(Calendar.MINUTE, 0);
        startDateCal.set(Calendar.SECOND, 0);
        iSessionStartDate = startDateCal.getTime();
        Calendar endDateCal = Calendar.getInstance(Locale.US);
        endDateCal.setTime(DateUtils.getDate(0, session.getEndMonth() + 1, session.getSessionStartYear()));
        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
        endDateCal.set(Calendar.MINUTE, 59);
        endDateCal.set(Calendar.SECOND, 59);
        iSessionEndDate = endDateCal.getTime();
        
        iShowCrossLists = ApplicationProperty.TimeGridShowCrosslists.isTrue();
        iShowClassSuffix = ApplicationProperty.SolverShowClassSufix.isTrue();
        iShowConfigName = ApplicationProperty.SolverShowConfiguratioName.isTrue();
        iShowClassNameTwoLines = ApplicationProperty.TimeGridShowNameInTwoLines.isTrue();
        iShowCourseTitle = "1".equals(filter.getParameterValue("showTitles"));
	}
	
	public String getFilter() { return iFilter; }
	
	public int getResourceType() { return iResourceType; }
	
	public int getFirstDay() { return iFirstDay; }
	
	public int getFirstSessionDay() { return iFirstSessionDay; }
	
	public int getBgMode() { return iBgMode; }
	
	public boolean isShowEvents() { return iShowEvents; }
	
	public int getStartDayDayOfWeek() { return iStartDayDayOfWeek; }
	
	public int getFirstSlot() { return iFirstSlot; }
	
	public int getLastSlot() { return iLastSlot; }
	
	public int getDayCode() { return iDayCode; }
	
	public BitSet getDefaultDatePattern() { return iPattern; }
	
	public float getNumberOfWeeks() { return (iFirstDay >= 0 ? 1.0f : iNrWeeks); }
	
	public int getSlotsPerWeek() { return iSlotsPerWeek; }
	
	public Date getFirstDate() { return iFirstDate; }
	
	public boolean isShowInstructor() { return iShowInstructor; }
	public boolean isShowTime() { return iShowTime; }
	public boolean isShowPreference() { return iShowPreference; }
	public boolean isShowFreeTimes() { return iShowFreeTimes && iResourceType <= 1; }
	public boolean isShowRoom() { return iResourceType != ResourceType.ROOM.ordinal(); }
	public boolean isShowDate() { return iShowDate; }
	
	public int getDisplayMode() { return iDispMode; }
	public boolean isVertical() { return iDispMode == DisplayMode.PerWeekVertical.ordinal(); }
	
	public Date getSessionStartDate() { return iSessionStartDate; }
	public Date getSessionEndDate() { return iSessionEndDate; }
	
	public boolean isShowCrossLists() { return iShowCrossLists; }
	public boolean isShowClassSuffix() { return iShowClassSuffix; }
	public boolean isShowConfigName() { return iShowConfigName; }
	public boolean isShowClassNameTwoLines() { return iShowClassNameTwoLines; }
	public boolean isShowCourseTitle() { return iShowCourseTitle; }
	
	public void ensureLocalizationIsSet() { Localization.setLocale(iLocale); }
	
	public String getInstructorNameFormat() { return iInstructorNameFormat; }
	public void setInstructorNameFormat(String format) { iInstructorNameFormat = format; }
	
	public int getWeekOffset() { return iWeekOffset; }
}
