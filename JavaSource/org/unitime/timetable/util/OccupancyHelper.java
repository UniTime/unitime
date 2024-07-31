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
package org.unitime.timetable.util;

import java.util.ArrayList;

public class OccupancyHelper extends RoomSummaryReportsHelper {

	public OccupancyHelper(int iMinutesInPeriod, int iPeriodsInHour, int iAllDayStartPeriod, int iAllDayEndPeriod,
			int iStandardDayStartPeriod, int iStandardDayEndPeriod, int iWeekendDayStartPeriod,
			int iWeekendDayEndPeriod, int iFirstFullHourStartPeriod, int iLastFullHourStopPeriod, String iSchema) {
		super(iMinutesInPeriod, iPeriodsInHour, iAllDayStartPeriod, iAllDayEndPeriod, iStandardDayStartPeriod,
				iStandardDayEndPeriod, iWeekendDayStartPeriod, iWeekendDayEndPeriod, iFirstFullHourStartPeriod,
				iLastFullHourStopPeriod, iSchema);
	}

	public OccupancyHelper() {
		super();
	}

	@Override
	protected String getSummaryCalculationForPeriod(String first5MinutePeriod, String last5MinutePeriod, String additionalQueryField) {
		StringBuffer utilCal = new StringBuffer();
	    utilCal.append("(case when z.nbr_rooms > 0 and ");
	    utilCal.append(additionalQueryField);
	    utilCal.append(" > 0 then ");
	    utilCal.append("(((((case when z.stop_period > ");
	    utilCal.append(last5MinutePeriod);
	    utilCal.append(" then ");
	    utilCal.append(last5MinutePeriod);
	    utilCal.append(" else z.stop_period end) - (case when z.start_period < ");
	    utilCal.append(first5MinutePeriod);
	    utilCal.append(" then ");
	    utilCal.append(first5MinutePeriod);
	    utilCal.append(" else z.start_period end)) * ");
	    utilCal.append(getMinutesInPeriod());
	    utilCal.append(") * (");
	    utilCal.append(additionalQueryField);
	    utilCal.append(" * z.room_proration))  * 1/z.weeks_divisor_for_day_of_week/60.0)");
	    utilCal.append(" else 0 end) ");
	    return(utilCal.toString());
	}
	
	protected String getRoomProration(){
		StringBuffer sb = new StringBuffer();
		sb.append("(case when r.capacity > 0");
		newline(sb, 8);
		sb.append("then");
		newline(sb, 12);
		sb.append("(select (r.capacity / sum(oth_r.capacity)) ");
		newline(sb, 12);
		sb.append("from ")
	      .append(getSchema())
		  .append(".meeting om");
		newline(sb, 12);
		sb.append("inner join ")
	      .append(getSchema())
		  .append(".room oth_r on oth_r.session_id = sess.uniqueid ");
		newline(sb, 16);
		sb.append("and oth_r.permanent_id = om.location_perm_id  ");  
		newline(sb, 12);
		sb.append("where om.event_id = m.event_id ");
		newline(sb, 16);
		sb.append("and om.meeting_date = m.meeting_date ");
		newline(sb, 16);
		sb.append("and om.start_period = m.start_period ");
		newline(sb, 16);
		sb.append("and om.stop_period = m.stop_period");
		newline(sb, 16);
		sb.append("and r.capacity > 0"); 
		newline(sb, 12);
		sb.append("group by om.event_id");
		newline(sb, 12);
		sb.append(")");
		newline(sb, 8);
		sb.append("else");
		newline(sb, 12);
		sb.append("1");
		newline(sb, 8);
		sb.append("end");
		newline(sb, 8);
		sb.append(") as room_proration");		
		return sb.toString();
	}

	@Override
	protected String getBaseQueryAdditionalSelectColumns() {
		StringBuffer sb = new StringBuffer();
		
	    newline(sb, 4);
	    sb.append("( ").append(getSeatsUsedCaseStatement()).append(" ) as ").append(MESSAGES.utilSqlStationsUsed()).append(",");
	    newline(sb, 4);
	    sb.append("( ").append(getSeatsRequestedCaseStatement()).append(" ) as ").append(MESSAGES.utilSqlStationsRequested()).append(",");
	    newline(sb, 4);
	    sb.append("m.uniqueId as meeting_id,");
	    newline(sb, 4);
	    sb.append(getRoomProration())
          .append(",");
		return sb.toString();
	}

	@Override
	public String getPivotedQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, Integer saturday,
			String campus, String year, String term, ArrayList<Object> headerRow,
			boolean includeDayOfWkTimeOfDayInHeaderRow, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment) {
		return getPivotedBaseSummaryQuery(allDays, weekDays, saturday, campus, year, term, headerRow, includeDayOfWkTimeOfDayInHeaderRow, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment);
	}

	@Override
	protected ArrayList<LabelFieldPair> getLabelPrefixToAdditionalQueryFieldMapping() {
		ArrayList<LabelFieldPair> mapping = new ArrayList<LabelFieldPair>();
		mapping.add(new LabelFieldPair(MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationsUsed()));
		mapping.add(new LabelFieldPair(MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlStationsRequested()));
		return mapping;
	}

	@Override
	protected void addSumStatementToStringBuffer(StringBuffer sb, String sum, String label, boolean leadingComma) {
	    if (leadingComma) {
	    	sb.append(",");
	    }
	    newline(sb, 8);
	    sb.append(sum);
	    newline(sb, 8);
	    sb.append(" as ")
	      .append(label);
	}

}
