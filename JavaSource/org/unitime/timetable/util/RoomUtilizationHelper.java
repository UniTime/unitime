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

public class RoomUtilizationHelper extends RoomSummaryReportsHelper {

	public RoomUtilizationHelper(int iMinutesInPeriod, int iPeriodsInHour, int iAllDayStartPeriod, int iAllDayEndPeriod,
			int iStandardDayStartPeriod, int iStandardDayEndPeriod, int iWeekendDayStartPeriod,
			int iWeekendDayEndPeriod, int iFirstFullHourStartPeriod, int iLastFullHourStopPeriod, String iSchema) {
		super(iMinutesInPeriod, iPeriodsInHour, iAllDayStartPeriod, iAllDayEndPeriod, iStandardDayStartPeriod,
				iStandardDayEndPeriod, iWeekendDayStartPeriod, iWeekendDayEndPeriod, iFirstFullHourStartPeriod,
				iLastFullHourStopPeriod, iSchema);
	}

	public RoomUtilizationHelper() {
	}

	protected String getSummaryCalculationForPeriod(String first5MinutePeriod, String last5MinutePeriod, String additionalQueryField) {
		
		StringBuffer utilCal = new StringBuffer();
	    if (additionalQueryField != null) {
	    	utilCal.append("(");
	    }
		utilCal.append("((((case when z.stop_period > ");
	    utilCal.append(last5MinutePeriod);
	    utilCal.append(" then ");
	    utilCal.append(last5MinutePeriod);
	    utilCal.append(" else z.stop_period end) - (case when z.start_period < ");
	    utilCal.append(first5MinutePeriod);
	    utilCal.append(" then ");
	    utilCal.append(first5MinutePeriod);
	    utilCal.append(" else z.start_period end)) * ");
	    utilCal.append(getMinutesInPeriod());
	    utilCal.append(")  * 1/z.weeks_divisor_for_day_of_week/60.0)");
	    if (additionalQueryField != null) {
	    	utilCal.append(" * ");
	    	utilCal.append(additionalQueryField);
	    	utilCal.append(")");
	    }
	    return(utilCal.toString());
	}

	@Override
	protected String getBaseQueryAdditionalSelectColumns() {
		return "";
	}

	@Override
	public String getPivotedQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, Integer saturday,
			String campus, String year, String term, ArrayList<Object> headerRow,
			boolean includeDayOfWkTimeOfDayInHeaderRow, boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		appendSelectedField(sb, "zz", MESSAGES.utilSqlAcademicInitiative(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlAcademicTerm(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlAcademicYear(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlRoomType(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlBuilding(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlRoom(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlRoomSize(), false, true);

	    if (isShowAdditionalPurdueData()) {
		    newline(sb, 4);
			appendSelectedField(sb, "zz", MESSAGES.utilSqlCampusRegion(), false, true);
		    newline(sb, 4);
			appendSelectedField(sb, "zz", MESSAGES.utilSqlLlrLalrPool(), false, true);
		    newline(sb, 4);
			appendSelectedField(sb, "zz", MESSAGES.utilSqlClassroomSubtype(), false, true);
	    }
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlRangeOfSizes(), false, true);
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, "zz", MESSAGES.utilSqlDepartment(), false, true);
		}
		if (includeSubjectArea) {
			newline(sb, 4);
			appendSelectedField(sb, "zz", MESSAGES.utilSqlSubject(), false, true);
		}
	    
	    newline(sb, 4);
		appendSelectedField(sb, "zz", MESSAGES.utilSqlEventType(), false, true);
	    newline(sb, 4);
	    
		appendSelectedField(sb, "zz", MESSAGES.utilSqlEventTypeDescription(), false, true);
	    newline(sb, 4);
	    
		appendSelectedField(sb, "zz", MESSAGES.utilSqlUtilizationType(), false, false);
		newline(sb, 4);
		
		for (LabelFieldPair labelFieldPair : getLabelPrefixToAdditionalQueryFieldMapping()) {
			buildWeekdayStandardHoursSummarySum(sb, true, labelFieldPair.getLabel());
			buildStandardHoursSummarySum(sb, true, labelFieldPair.getLabel());
			buildAllHoursSummarySum(sb, true, labelFieldPair.getLabel());
		}
		for (LabelFieldPair labelFieldPair : getLabelPrefixToAdditionalQueryFieldMapping()) {
			buildAllHoursLabelsCommaSeparated(sb, true, labelFieldPair.getLabel());
		}
		newline(sb, 0);
		sb.append("from ( ");
		newline(sb,4);
		sb.append(getPivotedBaseSummaryQuery(allDays, weekDays, saturday, campus, year, term, headerRow, includeDayOfWkTimeOfDayInHeaderRow, includeSubjectArea, includeDept));
		newline(sb,4);
		sb.append(" ) zz ");
		return sb.toString();
	}

	@Override
	protected ArrayList<LabelFieldPair> getLabelPrefixToAdditionalQueryFieldMapping() {
		ArrayList<LabelFieldPair> mapping = new ArrayList<LabelFieldPair>();
		mapping.add(new LabelFieldPair(MESSAGES.utilSqlUsage(), null)); 
		mapping.add(new LabelFieldPair(MESSAGES.utilSqlUsageSeatHours(), "z." + MESSAGES.utilSqlRoomSize())); 
			return mapping;
	}
	
	protected void buildAllHoursSummaryBaseSum(StringBuffer sb, String labelPrefix) {
		sb.append("(");
		boolean first = true;
		for (Integer day : getAllDays()) {
			String label = getDayTimeLabel(day, getAllDayStartPeriod(), labelPrefix);
			if (first) {
				first = false;
			} else {
				sb.append(" +");
			}
			newline(sb, 12);
			sb.append(label);
		    int startPeriod = getFirstFullHourStartPeriod();
		    while (startPeriod < getLastFullHourStopPeriod()) {
		        int stopPeriod = startPeriod + getPeriodsInHour();
				label = getDayTimeLabel(day, startPeriod, labelPrefix);
				sb.append(" +");
				newline(sb, 12);
				sb.append(label);
		        startPeriod = stopPeriod;
		    }
			label = getDayTimeLabel(day, getLastFullHourStopPeriod(), labelPrefix);
			sb.append(" +");
			newline(sb, 12);
			sb.append(label);
		}
		newline(sb, 8);
		sb.append(")");
	}
	
	protected void buildAllHoursSummarySum(StringBuffer sb, boolean leadingComma, String labelPrefix) {
		if (leadingComma) {
			sb.append(",");			
		}
		newline(sb, 8);
		buildAllHoursSummaryBaseSum(sb, labelPrefix);
		sb.append(" as ")
		   .append(labelPrefix)
		   .append(MESSAGES.utilSqlTotalAllHoursSuffix()); 

	}


	protected void buildWeekdayStandardHoursSummaryBaseSum(StringBuffer sb, String labelPrefix) {
		sb.append("(");
		boolean first = true;
		for (Integer day : getWeekDays()) {
			String label = null;
		    int startPeriod = getStandardDayStartPeriod();
		    while (startPeriod < getStandardDayEndPeriod()) {
		        int stopPeriod = startPeriod + getPeriodsInHour();
				label = getDayTimeLabel(day, startPeriod, labelPrefix);
				if (first) {
					first = false;
				} else {
					sb.append(" +");
				}
				newline(sb, 12);
				sb.append(label);
		        startPeriod = stopPeriod;
		    }
		}
		newline(sb, 8);
		sb.append(")");
	}

	protected void buildWeekdayStandardHoursSummarySum(StringBuffer sb, boolean leadingComma, String labelPrefix) {
		if (leadingComma) {
			sb.append(",");			
		}
		newline(sb, 8);
		buildWeekdayStandardHoursSummaryBaseSum(sb, labelPrefix);
		sb.append(" as ")
		   .append(labelPrefix)
		   .append(MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix());

	}

	protected void buildStandardHoursSummaryBaseSum(StringBuffer sb, String labelPrefix) {
		sb.append("(");
		boolean first = true;
		for (Integer day : getWeekDays()) {
			String label = null;
		    int startPeriod = getStandardDayStartPeriod();
		    while (startPeriod < getStandardDayEndPeriod()) {
		        int stopPeriod = startPeriod + getPeriodsInHour();
				label = getDayTimeLabel(day, startPeriod, labelPrefix);
				if (first) {
					first = false;
				} else {
					sb.append(" +");
				}
				newline(sb, 12);
				sb.append(label);
		        startPeriod = stopPeriod;
		    }
		}
		Integer day = getSaturday();
	    int startPeriod = getWeekendDayStartPeriod();
	    while (startPeriod < getWeekendDayEndPeriod()) {
	        int stopPeriod = startPeriod + getPeriodsInHour();
			String label = null;
			label = getDayTimeLabel(day, startPeriod, labelPrefix);
			if (first) {
				first = false;
			} else {
				sb.append(" +");
			}
			newline(sb, 12);
			sb.append(label);
	        startPeriod = stopPeriod;
	    }

		newline(sb, 8);
		sb.append(")");
	}
	protected void buildStandardHoursSummarySum(StringBuffer sb, boolean leadingComma, String labelPrefix) {
		if (leadingComma) {
			sb.append(",");			
		}
		newline(sb, 8);
		buildStandardHoursSummaryBaseSum(sb, labelPrefix);
		sb.append(" as ")
		   .append(labelPrefix)
		   .append(MESSAGES.utilSqlTotalStandardHoursSuffix());
	}

	protected void buildAllHoursLabelsCommaSeparated(StringBuffer sb, boolean leadingComma, String labelPrefix) {
		if (leadingComma) {
			sb.append(",");			
		}
		newline(sb, 8);
		boolean first = true;
		for (Integer day : getAllDays()) {
			String label = getDayTimeLabel(day, getAllDayStartPeriod(), labelPrefix);
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			newline(sb, 8);
			sb.append(label);
		    int startPeriod = getFirstFullHourStartPeriod();
		    while (startPeriod < getLastFullHourStopPeriod()) {
		        int stopPeriod = startPeriod + getPeriodsInHour();
				label = getDayTimeLabel(day, startPeriod, labelPrefix);
				sb.append(", ");
				newline(sb, 8);
				sb.append(label);
		        startPeriod = stopPeriod;
		    }
			label = getDayTimeLabel(day, getLastFullHourStopPeriod(), labelPrefix);
			sb.append(", ");
			newline(sb, 8);
			sb.append(label);
		}
	}

	@Override
	protected void addSumStatementToStringBuffer(StringBuffer sb, String sum, String label, boolean leadingComma) {
	    if (leadingComma) {
	    	sb.append(",");
	    }
	    newline(sb, 8);
	    sb.append("case");
	    newline(sb, 8);
	    sb.append("when");
	    newline(sb, 8);
	    sb.append(sum);
	    if (sum.contains("z." + MESSAGES.utilSqlRoomSize())) {
		    sb.append(" > z.").append(MESSAGES.utilSqlRoomSize());
	    } else {
	    	sb.append(" > 1");
	    }
	    newline(sb, 8);
	    sb.append("then");
	    newline(sb, 8);
	    if (sum.contains("z." + MESSAGES.utilSqlRoomSize())) {
		    sb.append("z.").append(MESSAGES.utilSqlRoomSize());
	    } else {
		    sb.append(1);
	    }
	    newline(sb, 8);
	    sb.append("else");
	    newline(sb, 8);
	    sb.append(sum);
	    newline(sb, 8);
	    sb.append("end");
	    newline(sb, 8);
	    sb.append(" as ")
	      .append(label);
	}


}
