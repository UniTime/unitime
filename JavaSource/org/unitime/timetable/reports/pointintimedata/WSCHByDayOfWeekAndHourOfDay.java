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
package org.unitime.timetable.reports.pointintimedata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.hibernate.Session;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.util.Constants;

/**
 * @author says
 *
 */
public class WSCHByDayOfWeekAndHourOfDay extends BasePointInTimeDataReports {
	protected HashMap<String, PeriodEnrollment> periodEnrollmentMap = new HashMap<String, PeriodEnrollment>();
	protected static final boolean startOnHalfHour = true;
	
	public static String dayOfWeekTimeLabelFor(java.util.Date date){
		return(Localization.getDateFormat("EEEE, HH:mm").format(date));
	}

	public WSCHByDayOfWeekAndHourOfDay() {
		super();
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDayOfWeek());
		addTimeColumns(hdr);
		setHeader(hdr);
	}
	
	protected String getPeriodTag(String id, Date period) {
        Calendar d = Calendar.getInstance(Locale.US);
        d.setTime(period);
		int minuteOfHour = d.get(Calendar.MINUTE);
		int hour = d.get(Calendar.HOUR_OF_DAY);
		int minute = (startOnHalfHour? 30 : 0);
		if (startOnHalfHour && minuteOfHour < 30) {
			hour -= 1;
		} 
		return(id + " " + d.get(Calendar.DAY_OF_WEEK) + " " + hour + " " + minute);
	}
	
	protected String getPeriodTag(String id, int dayOfWeek, int hourOfDay, int minute) {
		return(id + " " + dayOfWeek + " " + (startOnHalfHour && minute < 30 ? hourOfDay - 1 : hourOfDay) + " " + (startOnHalfHour? 30 : 0));
	}


	protected void addTimeColumns(ArrayList<String> header) {
		for(int hourOfDay = 0 ; hourOfDay < 24 ; hourOfDay++) {
			if (startOnHalfHour) {
				StringBuilder sb = new StringBuilder();
				sb.append(getTimeLabel(periodTime(hourOfDay, 30)))
                  .append(" - ");
				if (hourOfDay == 23) {
						sb.append(getTimeLabel(periodTime(0, 30)));
				} else {
					sb.append(getTimeLabel(periodTime(hourOfDay + 1, 30)));
				}
				header.add(sb.toString());
			} else {
				header.add(getTimeLabel(periodTime(hourOfDay, 0)) + " - " 
			        +  (getTimeLabel(periodTime((hourOfDay + 1), 0))));						
			}
		}
	
	}
	
	public String getTimeLabel(Date period) {
		return(Localization.getDateFormat("HH:mm").format(period));
	}

	public String getDayOfWeekLabel(Date period) {
		return(Localization.getDateFormat("EEEE").format(period));
	}

	@Override
	public String reportName() {
		return(MSG.wseByDayOfWeekAndHourOfDayReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.wseByDayOfWeekAndHourOfDayReportNote());
	}

	private String getPeriodTag(Date meetingDate, int timeSlot) {
        Calendar d = Calendar.getInstance(Locale.US);
        d.setTime(meetingDate);
		int minuteOfDay = ((timeSlot*Constants.SLOT_LENGTH_MIN) + Constants.FIRST_SLOT_TIME_MIN);
		int hour;
		int minute = (startOnHalfHour? 30 : 0);
		if (startOnHalfHour && (minuteOfDay%60) < 30) {
			hour = (minuteOfDay/60) - 1;
		} else {
			hour = (minuteOfDay/60);			
		}
		return(d.get(Calendar.DAY_OF_WEEK) + " " + hour + " " + minute);
	}

	private String getPeriodTag(int dayOfWeek, int hourOfDay, int minute) {
		return(dayOfWeek + " " + (startOnHalfHour && minute < 30 ? hourOfDay - 1 : hourOfDay) + " " + (startOnHalfHour? 30 : 0));
	}
	
	protected Date periodTime(int hourOfDay, int minute) {
		
        Calendar c = Calendar.getInstance(Locale.US);
        c.clear();
        c.set(Calendar.HOUR, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
	}
	
	protected Date periodDayOfWeek(int dayOfWeek) {
		
        Calendar c = Calendar.getInstance(Locale.US);
        c.clear();
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return c.getTime();
	}

	
	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		
		calculatePeriodsWithEnrollments(pointInTimeData, hibSession);
		
		int minute = (startOnHalfHour ? 30 : 0);
		for(int dayOfWeek = 1 ; dayOfWeek < 8 ; dayOfWeek++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(getDayOfWeekLabel(periodDayOfWeek(dayOfWeek)));
			for(int hourOfDay = 0 ; hourOfDay < 24 ; hourOfDay++) {
				String key = getPeriodTag(dayOfWeek, hourOfDay, minute);
				row.add(periodEnrollmentMap.get(key) == null ? "0": "" + periodEnrollmentMap.get(key).getWeeklyStudentEnrollment());
			}
			addDataRow(row);			
		}
				
	}

	@SuppressWarnings("unchecked")
	private void calculatePeriodsWithEnrollments (
			PointInTimeData pointInTimeData, Session hibSession) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct pc.uniqueId, pcm.meetingDate, pcmup.timeSlot, pc.enrollment")
		  .append("	from PitClass pc") 
		  .append(" inner join pc.pitClassEvents as pce")
		  .append(" inner join pce.pitClassMeetings as pcm")
		  .append(" inner join pcm.pitClassMeetingUtilPeriods as pcmup")
		  .append("	where pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.uniqueId = :sessId")
		  .append("   and  pc.pitSchedulingSubpart.itype.organized = true");
		
		for (Object[] result : (List<Object[]>) hibSession.createQuery(sb.toString())
								.setLong("sessId", pointInTimeData.getUniqueId().longValue())
								.setCacheable(true)
								.list()) {
			Date meetingDate = (Date) result[1];
			Integer timeSlot = (Integer) result[2];
			Integer enrollment = (Integer) result[3];
			
			String label = getPeriodTag(meetingDate, timeSlot);
			PeriodEnrollment pe = periodEnrollmentMap.get(label);
			if (pe == null) {
				pe = new PeriodEnrollment(label, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
				periodEnrollmentMap.put(label, pe);
			}
			pe.addEnrollment(enrollment);


		}
				

	}

	@Override
	protected void runReport(org.hibernate.Session hibSession) {
		PointInTimeData pitd = (PointInTimeData)hibSession
				.createQuery("from PointInTimeData pitd where pitd.uniqueId = :uid")
				.setLong("uid", getPointInTimeDataUniqueId().longValue())
				.uniqueResult();
		createRoomUtilizationReportFor(pitd, hibSession);
		
	}

	protected class PeriodEnrollment {
		private float iWeeklyStudentEnrollment = 0.0f;
		private float iPeriodsPerStandardReportingHourPerTerm;
		private String iPeriodTag;
		private Float iStandardWeeksInReportingTerm;
		private Float iStandardMinutesInReportingHour;		
		
		public String getPeriodTag() {
			return iPeriodTag;
		}
		
		public float getWeeklyStudentEnrollment() {
			return (this.iWeeklyStudentEnrollment / iPeriodsPerStandardReportingHourPerTerm) ;
		}
		
		public PeriodEnrollment(String periodTag, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
			this.iStandardMinutesInReportingHour = standardMinutesInReportingHour;
			this.iStandardWeeksInReportingTerm = standardWeeksInReportingTerm;
			this.iPeriodTag = periodTag;
			iPeriodsPerStandardReportingHourPerTerm = (this.iStandardMinutesInReportingHour.floatValue()/(Constants.SLOT_LENGTH_MIN * 1.0f)) * this.iStandardWeeksInReportingTerm.floatValue();
		}
		
		
		public void addEnrollment(Integer enrollment) {
			iWeeklyStudentEnrollment += enrollment.floatValue();
		}

		public void addEnrollment(float enrollment) {
			iWeeklyStudentEnrollment += enrollment;
		}

	}

	
}
