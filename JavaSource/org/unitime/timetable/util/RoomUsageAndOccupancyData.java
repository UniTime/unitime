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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Session;

public class RoomUsageAndOccupancyData {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static String roomUsageTable = "room_usage_table";
	private static String seatUsageTable = "seat_usage_table";
	private static String buildingDataTable = "bldg_data";

	private OccupancyHelper occupancyHelper;
	private RoomUtilizationHelper roomUtilizationHelper;
	
	private Boolean iShowAdditionalPurdueData;

	
	public RoomUsageAndOccupancyData() {
		setOccupancyHelper(new OccupancyHelper());
		setRoomUtilizationHelper(new RoomUtilizationHelper());
	}

	public RoomUsageAndOccupancyData(int iMinutesInPeriod, int iPeriodsInHour, int iAllDayStartPeriod, int iAllDayEndPeriod,
			int iStandardDayStartPeriod, int iStandardDayEndPeriod, int iWeekendDayStartPeriod,
			int iWeekendDayEndPeriod, int iFirstFullHourStartPeriod, int iLastFullHourStopPeriod, String iSchema) {
		
		setOccupancyHelper(new OccupancyHelper(iMinutesInPeriod, iPeriodsInHour, iAllDayStartPeriod, iAllDayEndPeriod, iStandardDayStartPeriod,
				iStandardDayEndPeriod, iWeekendDayStartPeriod, iWeekendDayEndPeriod, iFirstFullHourStartPeriod,
				iLastFullHourStopPeriod, iSchema));
		setRoomUtilizationHelper(new RoomUtilizationHelper(iMinutesInPeriod, iPeriodsInHour, iAllDayStartPeriod, iAllDayEndPeriod, iStandardDayStartPeriod,
				iStandardDayEndPeriod, iWeekendDayStartPeriod, iWeekendDayEndPeriod, iFirstFullHourStartPeriod,
				iLastFullHourStopPeriod, iSchema));
	}

	public OccupancyHelper getOccupancyHelper() {
		return occupancyHelper;
	}

	public void setOccupancyHelper(OccupancyHelper occupancyHelper) {
		this.occupancyHelper = occupancyHelper;
	}

	public RoomUtilizationHelper getRoomUtilizationHelper() {
		return roomUtilizationHelper;
	}

	public void setRoomUtilizationHelper(RoomUtilizationHelper roomUtilizationHelper) {
		this.roomUtilizationHelper = roomUtilizationHelper;
	}
	
	public Boolean isShowAdditionalPurdueData() {
		if (iShowAdditionalPurdueData == null) {
			iShowAdditionalPurdueData = "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.room_summary_reports.show_purdue_columns", "false"));
		}
		return iShowAdditionalPurdueData;
	}

	public void setShowAdditionalPurdueData(Boolean showAdditionalPurdueData) {
		this.iShowAdditionalPurdueData = showAdditionalPurdueData;
	}

	
	private void appendValueEqualsSql(StringBuffer sb, String table1, String table2, String fieldName, boolean hasLeadingAnd) {
		if (hasLeadingAnd) {
			sb.append("and ");
		}
		sb.append(table1)
		  .append(".")
		  .append(fieldName)
		  .append(" = ")
		  .append(table2)
		  .append(".")
		  .append(fieldName);

	}
	
	private void appendValueNotNullAndEqualsSql(StringBuffer sb, String table1, String table2, String fieldName, boolean hasLeadingAnd) {
		if (hasLeadingAnd) {
			sb.append("and ");
		}
		sb.append(table1)
		  .append(".")
		  .append(fieldName)
		  .append(" is not null");
		newline(sb, 4);
		sb.append("and ")
		  .append(table2)
		  .append(".")
		  .append(fieldName)
		  .append(" is not null ");
		newline(sb, 4);
		appendValueEqualsSql(sb, table1, table2, fieldName, hasLeadingAnd);
	}
	
	private void appendValuesAreNull(StringBuffer sb, String table1, String table2, String fieldName, boolean hasLeadingAnd) {
		if (hasLeadingAnd) {
			sb.append("and ");
		}
		sb.append(table1)
		  .append(".")
		  .append(fieldName)
		  .append(" is null and ")
		  .append(table2)
		  .append(".")
		  .append(fieldName)
		  .append(" is null");
	}

	private void fromClause(StringBuffer sb, Session acadSession, boolean hasDayTime, boolean includeSubjectArea, boolean includeDept, boolean isCheckNull) {
		fromClause(sb, acadSession, hasDayTime, includeSubjectArea, includeDept, false, false, isCheckNull);
	}

	private void fromClause(StringBuffer sb, Session acadSession, boolean hasDayTime, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, boolean isCheckNull) {
		ArrayList<Object> headerRow1 = new ArrayList<Object>();
		ArrayList<Object> headerRow2 = new ArrayList<Object>();
		String utilizationQuery = getRoomUtilizationHelper()
				.getUnpivotedRoomUtilizationQuery(
						getRoomUtilizationHelper().getAllDays(), 
						getRoomUtilizationHelper().getWeekDays(), 
						getRoomUtilizationHelper().getSaturday(), 
						acadSession.getAcademicInitiative(), 
						acadSession.getAcademicYear(), 
						acadSession.getAcademicTerm(), 
						headerRow1, 
						includeSubjectArea, 
						includeDept,
						includeSection,
						includeRoomControlingDepartment); 
		String occupancyQuery = getOccupancyHelper()
				.getUnpivotedRoomUtilizationQuery(
						getRoomUtilizationHelper().getAllDays(), 
						getRoomUtilizationHelper().getWeekDays(), 
						getRoomUtilizationHelper().getSaturday(), 
						acadSession.getAcademicInitiative(), 
						acadSession.getAcademicYear(), 
						acadSession.getAcademicTerm(), 
						headerRow2, 
						includeSubjectArea, 
						includeDept,
						includeSection,
						includeRoomControlingDepartment);
		
		sb.append("from");
		newline(sb, 0);
		sb.append("(");
		newline(sb, 0);
		sb.append(occupancyQuery);
		newline(sb, 0);
		sb.append(") ")
		  .append(seatUsageTable);
		newline(sb, 0);
		sb.append("left outer join");
		newline(sb, 0);
		sb.append("(");
		newline(sb, 0);
		sb.append(utilizationQuery);
		newline(sb, 0);
		sb.append(") ")
		  .append(roomUsageTable);
		newline(sb, 0);
		sb.append("on ");
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlAcademicInitiative(), false);
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlAcademicTerm(), true);
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlAcademicYear(), true);
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlBuilding(), true);
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlRoomType(), true);
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlRoom(), true);
		if (hasDayTime) {
			newline(sb, 4);
			appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlDayTime(), true);
		}
		if (includeDept) {
			newline(sb, 4);
			if (isCheckNull) {
				appendValuesAreNull(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlDepartment(), true);				
			} else {
				appendValueNotNullAndEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlDepartment(), true);
			}
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			if (isCheckNull) {
				appendValuesAreNull(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlRoomDept(), true);				
			} else {
				appendValueNotNullAndEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlRoomDept(), true);
			}
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			if (isCheckNull) {
				appendValuesAreNull(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlSubject(), true);
			} else {
				appendValueNotNullAndEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlSubject(), true);
			}
		}
		if (includeSection) {
			newline(sb, 4);
			if (isCheckNull) {
				appendValuesAreNull(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlCourseNbr(), true);
			} else {
				appendValueNotNullAndEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlCourseNbr(), true);
			}
			newline(sb, 4);
			if (isCheckNull) {
				appendValuesAreNull(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlItype(), true);
			} else {
				appendValueNotNullAndEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlItype(), true);
			}
			newline(sb, 4);
			if (isCheckNull) {
				appendValuesAreNull(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlSection(), true);
			} else {
				appendValueNotNullAndEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlSection(), true);
			}
	
		}
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlEventType(), true);
		newline(sb, 4);
		appendValueEqualsSql(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlUtilizationType(), true);
	}

	private void appendStationOccupancyRateCalc(StringBuffer sb, String utilTableName, String occTableName, 
			String seatUsageTotalSuffix, String stationsUsedPrefix, String stationOccupancyPrefix, 
			boolean hasTrailingComma, ArrayList<String> headerRow) {
		sb.append("case");
		newline(sb, 8);
		sb.append("when ")
		   .append(utilTableName)
		   .append(".")
		  .append(MESSAGES.utilSqlUsageSeatHours())
		  .append(seatUsageTotalSuffix)
		  .append(" > 0");
		newline(sb, 8);
		sb.append("then to_char(")
		  .append(occTableName)
		  .append(".")
		  .append(stationsUsedPrefix)
		  .append(seatUsageTotalSuffix)
		  .append(" / ")
		  .append(utilTableName)
		  .append(".")
		  .append(MESSAGES.utilSqlUsageSeatHours())
		  .append(seatUsageTotalSuffix)
		  .append(")");
		newline(sb, 8);
		sb.append("else 'undefined'");
		newline(sb, 4);
		sb.append("end as ")
		  .append(stationOccupancyPrefix)
		  .append(seatUsageTotalSuffix);
		if (hasTrailingComma) {
			sb.append(",");
		}
		newline(sb, 4);
		if (headerRow != null) {
			headerRow.add(stationOccupancyPrefix + seatUsageTotalSuffix);
		}

	}
	
	private void appendSummedStationOccupancyRateCalc(StringBuffer sb, String utilTableName, String occTableName, String seatUsageTotalSuffix, String stationsUsedPrefix, String stationOccupancyPrefix, boolean hasTrailingComma, ArrayList<String> headerRow) {
	
		sb.append("case");
		newline(sb, 8);
		sb.append("when sum(")
		  .append(utilTableName)
		  .append(".")
		  .append(MESSAGES.utilSqlUsageSeatHours())
		  .append(seatUsageTotalSuffix)
		  .append(") > 0");
		newline(sb, 8);
		sb.append("then to_char(sum(")
		  .append(occTableName)
		  .append(".")
		  .append(stationsUsedPrefix)
		  .append(seatUsageTotalSuffix)
		  .append(") / sum(")
		  .append(utilTableName)
		  .append(".")
		  .append(MESSAGES.utilSqlUsageSeatHours())
		  .append(seatUsageTotalSuffix)
 		  .append("))");
		newline(sb, 8);
		sb.append("else 'undefined'");
		newline(sb, 4);
		sb.append("end as ")
		  .append(stationOccupancyPrefix)
		  .append(seatUsageTotalSuffix);
		if (hasTrailingComma) {
			sb.append(",");
		}
		newline(sb, 4);
		if (headerRow != null) {
			headerRow.add(stationOccupancyPrefix + seatUsageTotalSuffix);
		}
	}

	
	private void appendSumZeroIfNull(StringBuffer sb, String tableName, String fieldName, boolean hasTrailingComma, ArrayList<String> headerRow) {
		sb.append("sum(case when ")
		  .append(tableName)
		  .append('.')
		  .append(fieldName)
		  .append(" is null then 0 else ")
		  .append(tableName)
		  .append('.')
		  .append(fieldName)
		  .append(" end) as ")
		  .append(fieldName);
		if (hasTrailingComma) {
			sb.append(",");
		}
		if (headerRow != null) {
			headerRow.add(fieldName);
		}

	}
	
	private void appendDataZeroIfNull(StringBuffer sb, String tableName, String fieldName, boolean hasTrailingComma, ArrayList<String> headerRow) {
		sb.append("case when ")
		  .append(tableName)
		  .append(".")
		  .append(fieldName)
		  .append(" is null then 0 else ")
		  .append(tableName)
		  .append(".")
		  .append(fieldName)
		  .append(" end as ")
		  .append(fieldName);
		if (hasTrailingComma)
		  sb.append(",");
		if (headerRow != null) {
			headerRow.add(fieldName);
		}

	}
	
	private void appendSelectedField(StringBuffer sb, String tableName, String fieldName, boolean hasLeadingComma, boolean hasTrailingComma, ArrayList<String> headerRow) {
		if(hasLeadingComma) {
			sb.append(", ");
		}
		sb.append(tableName)
		  .append(".")
		  .append(fieldName);
		if(hasTrailingComma) {
			sb.append(", ");
		}
		if (headerRow != null) {
			headerRow.add(fieldName);
		}
	}
	
	public void getRoomUsageAndOccupancyQuery(StringBuffer sb, Session acadSession, boolean includeSubjectArea, boolean includeDept, 
			boolean isCheckNull, ArrayList<String> headerRow) {
		getRoomUsageAndOccupancyQuery(sb, acadSession, includeSubjectArea, includeDept, 
				false, false, isCheckNull, headerRow);

	}
	
	public void getRoomUsageAndOccupancyQuery(StringBuffer sb, Session acadSession, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, boolean isCheckNull, ArrayList<String> headerRow) {
		
		sb.append("select ");
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicInitiative(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicTerm(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicYear(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomType(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlBuilding(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoom(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomSize(), false, true, headerRow);
		if (isShowAdditionalPurdueData()) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlCampusRegion(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlLlrLalrPool(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlClassroomSubtype(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRangeOfSizes(), false, true, headerRow);

		
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDepartment(), false, true, headerRow);
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomDept(), false, true, headerRow);			
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSubject(), false, true, headerRow);
		}
		if (includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlCourseNbr(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlItype(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSection(), false, true, headerRow);			
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventType(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventTypeDescription(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUtilizationType(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendStationOccupancyRateCalc(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), 
				MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendStationOccupancyRateCalc(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlTotalStandardHoursSuffix(), 
				MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendStationOccupancyRateCalc(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlTotalAllHoursSuffix(), 
				MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendStationOccupancyRateCalc(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), 
				MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlRequestedStationOccupancyRate(),true, headerRow);		
		newline(sb, 4);
		appendStationOccupancyRateCalc(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlTotalStandardHoursSuffix(), 
				MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlRequestedStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendStationOccupancyRateCalc(sb, roomUsageTable, seatUsageTable, MESSAGES.utilSqlTotalAllHoursSuffix(), 
				MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlRequestedStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDayTime(), false, true, headerRow);
		newline(sb,4);
		appendDataZeroIfNull(sb, roomUsageTable,  MESSAGES.utilSqlUsage(), true, headerRow);
		newline(sb, 4);
		appendDataZeroIfNull(sb, roomUsageTable,  MESSAGES.utilSqlUsageSeatHours(), true, headerRow);
		newline(sb,4);
		appendDataZeroIfNull(sb, seatUsageTable,  MESSAGES.utilSqlStationsUsed(), true, headerRow);
		newline(sb, 4);
		appendDataZeroIfNull(sb, seatUsageTable,  MESSAGES.utilSqlStationsRequested(), false, headerRow);
		newline(sb, 0);
		fromClause(sb, acadSession, true, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment,isCheckNull);
		newline(sb, 0);
		

	}

	public String getRoomUsageAndOccupancyQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept, ArrayList<String> headerRow) {
		return getRoomUsageAndOccupancyQuery(acadSession, includeSubjectArea, includeDept, false, false, headerRow);
	}

	
	public String getRoomUsageAndOccupancyQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, ArrayList<String> headerRow) {
		StringBuffer sb = new StringBuffer();
		getRoomUsageAndOccupancyQuery(sb, acadSession, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, false, headerRow);
		newline(sb, 0);
		sb.append("where ")
		  .append(roomUsageTable)
		  .append(".")
		  .append(MESSAGES.utilSqlAcademicInitiative())
		  .append(" is not null");
		if (includeDept || includeSubjectArea || includeSection || includeRoomControlingDepartment) {
			sb.append(" union all ");
			newline(sb, 0);
			getRoomUsageAndOccupancyQuery(sb, acadSession, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, true, null);
			newline(sb, 0);
			sb.append("where ")
			  .append(roomUsageTable)
			  .append(".")
			  .append(MESSAGES.utilSqlAcademicInitiative())
			  .append(" is not null");
			newline(sb, 0);
		}
		newline(sb, 0);
	    sb.append("order by ")
	    .append(MESSAGES.utilSqlAcademicInitiative())
	    .append(", ")
	    .append(MESSAGES.utilSqlAcademicTerm())
	    .append(", ")
	    .append(MESSAGES.utilSqlAcademicYear())
	    .append( " desc, ")
	    .append(MESSAGES.utilSqlRoomType())
	    .append(", ");
	    newline(sb, 10);
	    sb.append(MESSAGES.utilSqlRoomSize())
	    .append(" desc, ")
	    .append(MESSAGES.utilSqlBuilding())
	    .append(", ")
	    .append(MESSAGES.utilSqlRoom());
	    if (includeDept) {
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlDepartment());	    	
	    }
	    if (includeRoomControlingDepartment) {
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlRoomDept());	    	
	    }
	    if (includeSubjectArea || includeSection) {
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSubject());	    	
	    }
	    if (includeSection) {
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlCourseNbr());	   
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlItype());	    	
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSection());	    		    
	    }
	    sb.append(",");
		newline(sb, 4);
		sb.append(MESSAGES.utilSqlDayTime());
		newline(sb, 0);

		return sb.toString();
	}

	public void getBuildingUsageAndOccupancyTimeDayQuery(StringBuffer sb, Session acadSession, 
			boolean isByRoomType, boolean includeSubjectArea, boolean includeDept, 
			boolean isCheckNull, ArrayList<String> headerRow) {
		getBuildingUsageAndOccupancyTimeDayQuery(sb, acadSession, 
				isByRoomType, includeSubjectArea, includeDept, 
				false, false, 
				isCheckNull, headerRow);
	}
	
	public void getBuildingUsageAndOccupancyTimeDayQuery(StringBuffer sb, Session acadSession, 
			boolean isByRoomType, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, 
			boolean isCheckNull, ArrayList<String> headerRow) {

		sb.append("select ");
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicInitiative(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicTerm(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicYear(), false, true, headerRow);
		if (isByRoomType) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomType(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlBuilding(), false, true, headerRow);
		
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDepartment(), false, true, headerRow);
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomDept(), false, true, headerRow);
			
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSubject(), false, true, headerRow);
		}
		if (includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlCourseNbr(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlItype(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSection(), false, true, headerRow);
			
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventType(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventTypeDescription(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUtilizationType(), false, true, headerRow);

				
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDayTime(), false, true, headerRow);
		newline(sb,4);
		appendSumZeroIfNull(sb, roomUsageTable, MESSAGES.utilSqlUsage(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested(), false, headerRow);
		newline(sb, 0);
		fromClause(sb, acadSession, true, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, isCheckNull);
		newline(sb, 0);
		sb.append("where ")
		  .append(roomUsageTable)
		  .append(".")
		  .append(MESSAGES.utilSqlAcademicInitiative())
		  .append(" is not null");
		newline(sb, 0);		
	    sb.append("group by ");
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicInitiative(), false, true, null);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicTerm(), false, true, null);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicYear(), false, true, null);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUtilizationType(), false, true, null);
		if (isByRoomType) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomType(), false, true, null);
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlBuilding(), false, true, null);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventType(), false, true, null);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventTypeDescription(), false, true, null);
		
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDepartment(), false, true, null);
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomDept(), false, true, null);		
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSubject(), false, true, null);
		}
		if (includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlCourseNbr(), false, true, null);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlItype(), false, true, null);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSection(), false, true, null);
			
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDayTime(), false, false, null);
		
		newline(sb, 0);
	
	}

	public String getBuildingUsageAndOccupancyTimeDayQuery(Session acadSession, boolean isByRoomType, 
			boolean includeSubjectArea, boolean includeDept, ArrayList<String> headerRow) {
		return getBuildingUsageAndOccupancyTimeDayQuery(acadSession, isByRoomType, 
				includeSubjectArea, includeDept, 
				false, false, headerRow);
	}
	
	public String getBuildingUsageAndOccupancyTimeDayQuery(Session acadSession, boolean isByRoomType, 
			boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, ArrayList<String> headerRow) {
		StringBuffer sb = new StringBuffer();
		getBuildingUsageAndOccupancyTimeDayQuery(sb, acadSession, isByRoomType, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment,false, headerRow);
		newline(sb, 0);
		if(includeDept || includeSubjectArea || includeSection || includeRoomControlingDepartment) {
			sb.append(" union all ");
			newline(sb, 0);
			getBuildingUsageAndOccupancyTimeDayQuery(sb, acadSession, isByRoomType, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, true, null);
			newline(sb, 0);
		}
		newline(sb, 0);
	    sb.append("order by ")
	      .append(MESSAGES.utilSqlAcademicInitiative())
	      .append(", ")
	      .append(MESSAGES.utilSqlAcademicTerm())
	      .append(", ")
	      .append(MESSAGES.utilSqlAcademicYear())
	      .append( " desc, ")
  	      .append(MESSAGES.utilSqlBuilding())
	      .append(", ");
	    if (isByRoomType) {
	    	sb.append(MESSAGES.utilSqlRoomType())
	    	  .append(", ");
	    }
	    newline(sb, 10);
	    if (includeDept) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlDepartment());	    	
		    sb.append(",");
	    }
	    if (includeRoomControlingDepartment) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlRoomDept());	    	
		    sb.append(",");	    	
	    }
	    if (includeSubjectArea || includeSection) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSubject());	    	
		    sb.append(",");
	    }
	    if (includeSection) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlCourseNbr());	    	
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlItype());	    	
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSection());	    	
		    sb.append(",");
	    }
	    newline(sb, 10);
	    sb.append(MESSAGES.utilSqlEventType());	    	
	    sb.append(",");
	    newline(sb, 10);
	    sb.append(MESSAGES.utilSqlUtilizationType());	    	
	    sb.append(",");
	    
		newline(sb, 4);
		sb.append(MESSAGES.utilSqlDayTime());	
		newline(sb, 0);
	
		return sb.toString();
   }
	
	
	private void buildingData(StringBuffer sb, Session acadSession, boolean isByRoomType, boolean includeSubjectArea,
			boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, boolean isCheckNull, ArrayList<String> headerRow) {
		sb.append("(");
		newline(sb, 0);
		
		sb.append("select distinct ");

		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicInitiative(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicTerm(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlAcademicYear(), false, true, headerRow);
		if (isByRoomType) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomType(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlBuilding(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoom(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomSize(), false, true, headerRow);
		if (isShowAdditionalPurdueData()) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlCampusRegion(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlLlrLalrPool(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlClassroomSubtype(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRangeOfSizes(), false, true, headerRow);

		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlDepartment(), false, true, headerRow);
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlRoomDept(), false, true, headerRow);			
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSubject(), false, true, headerRow);
		}
		if (includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlCourseNbr(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlItype(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlSection(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventType(), false, true, headerRow);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlEventTypeDescription(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUtilizationType(), false, true, headerRow);

		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, roomUsageTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalStandardHoursSuffix(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, seatUsageTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalAllHoursSuffix(), false, false, headerRow);
		newline(sb, 4);
		newline(sb, 0);
		fromClause(sb, acadSession, false, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, isCheckNull);
		newline(sb, 0);
		sb.append(") ")
		  .append(buildingDataTable);
		newline(sb, 0);

	}
	
	public void getBuildingUsageAndOccupancyQuery(StringBuffer sb, Session acadSession, boolean isByRoomType, boolean includeSubjectArea, 
			boolean includeDept, boolean isCheckNull, ArrayList<String> headerRow) {
		getBuildingUsageAndOccupancyQuery(sb, acadSession, isByRoomType, includeSubjectArea, 
				includeDept, false, false, isCheckNull, headerRow);
	}

	
	public void getBuildingUsageAndOccupancyQuery(StringBuffer sb, Session acadSession, boolean isByRoomType, boolean includeSubjectArea, 
			boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, boolean isCheckNull, ArrayList<String> headerRow) {
		
		newline(sb, 0);
		sb.append("select ");
		
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlAcademicInitiative(), false, true, headerRow);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlAcademicTerm(), false, true, headerRow);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlAcademicYear(), false, true, headerRow);
		if (isByRoomType) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlRoomType(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlBuilding(), false, true, headerRow);
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlDepartment(), false, true, headerRow);
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlRoomDept(), false, true, headerRow);			
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlSubject(), false, true, headerRow);
		}
		if (includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlCourseNbr(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlItype(), false, true, headerRow);
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlSection(), false, true, headerRow);
		}
		newline(sb, 4);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlEventType(), false, true, headerRow);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlEventTypeDescription(), false, true, headerRow);
		newline(sb, 4);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlUtilizationType(), false, true, headerRow);


		
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalStandardHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlUsage() + MESSAGES.utilSqlTotalAllHoursSuffix(), true, headerRow);

		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalStandardHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlUsageSeatHours() + MESSAGES.utilSqlTotalAllHoursSuffix(), true, headerRow);

		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalStandardHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlStationsUsed() + MESSAGES.utilSqlTotalAllHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSummedStationOccupancyRateCalc(sb, buildingDataTable, buildingDataTable, MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), 
				MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendSummedStationOccupancyRateCalc(sb, buildingDataTable, buildingDataTable, MESSAGES.utilSqlTotalStandardHoursSuffix(), 
				MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendSummedStationOccupancyRateCalc(sb, buildingDataTable, buildingDataTable, MESSAGES.utilSqlTotalAllHoursSuffix(), 
				MESSAGES.utilSqlStationsUsed(), MESSAGES.utilSqlStationOccupancyRate(), true, headerRow);		

		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalStandardHoursSuffix(), true, headerRow);
		newline(sb, 4);
		appendSumZeroIfNull(sb, buildingDataTable, MESSAGES.utilSqlStationsRequested() + MESSAGES.utilSqlTotalAllHoursSuffix(), true, headerRow);

		newline(sb, 4);
		appendSummedStationOccupancyRateCalc(sb, buildingDataTable, buildingDataTable, MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix(),
				MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlRequestedStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendSummedStationOccupancyRateCalc(sb, buildingDataTable, buildingDataTable, MESSAGES.utilSqlTotalStandardHoursSuffix(), 
				MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlRequestedStationOccupancyRate(), true, headerRow);		
		newline(sb, 4);
		appendSummedStationOccupancyRateCalc(sb, buildingDataTable, buildingDataTable, MESSAGES.utilSqlTotalAllHoursSuffix(), 
				MESSAGES.utilSqlStationsRequested(), MESSAGES.utilSqlRequestedStationOccupancyRate(), false, headerRow);		

		newline(sb, 0);
		sb.append("from");
		newline(sb, 0);
		buildingData(sb, acadSession, isByRoomType, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, isCheckNull, null);
		newline(sb, 0);
		sb.append("where ")
		  .append(buildingDataTable)
		  .append(".")
		  .append(MESSAGES.utilSqlAcademicInitiative())
		  .append(" is not null");
		newline(sb, 0);
	    sb.append("group by ");
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlAcademicInitiative(), false, true, null);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlAcademicTerm(), false, true, null);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlAcademicYear(), false, true, null);
		newline(sb, 4);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlUtilizationType(), false, true, null);
		if (isByRoomType) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlRoomType(), false, true, null);
		}
		newline(sb, 4);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlBuilding(), false, true, null);
		newline(sb, 4);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlEventType(), false, true, null);
		appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlEventTypeDescription(), false, false, null);
		
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlDepartment(), true, false, null);
		}
		if (includeRoomControlingDepartment) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlRoomDept(), true, false, null);
		}
		if (includeSubjectArea || includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlSubject(), true, false, null);
		}
		if (includeSection) {
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlCourseNbr(), true, false, null);
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlItype(), true, false, null);
			newline(sb, 4);
			appendSelectedField(sb, buildingDataTable, MESSAGES.utilSqlSection(), true, false, null);
		}
		
		newline(sb, 0);
	
	}
	
	public String getBuildingUsageAndOccupancyQuery(Session acadSession, boolean isByRoomType, boolean includeSubjectArea, boolean includeDept, 
			 ArrayList<String> headerRow) {
		return getBuildingUsageAndOccupancyQuery(acadSession, isByRoomType, includeSubjectArea, includeDept, 
				false, false, headerRow);
	}
	public String getBuildingUsageAndOccupancyQuery(Session acadSession, boolean isByRoomType, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, ArrayList<String> headerRow) {
		StringBuffer sb = new StringBuffer();
		getBuildingUsageAndOccupancyQuery(sb, acadSession, isByRoomType, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, false, headerRow);
		newline(sb, 0);
		if(includeDept || includeSubjectArea || includeSection || includeRoomControlingDepartment) {
			newline(sb, 0);
			sb.append(" union all ");
			newline(sb, 0);
			getBuildingUsageAndOccupancyQuery(sb, acadSession, isByRoomType, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, true, null);
			newline(sb, 0);
		}
		newline(sb, 0);
	    sb.append("order by ")
	      .append(MESSAGES.utilSqlAcademicInitiative())
	      .append(", ")
	      .append(MESSAGES.utilSqlAcademicTerm())
	      .append(", ")
	      .append(MESSAGES.utilSqlAcademicYear())
	      .append( " desc, ")
	      .append(MESSAGES.utilSqlBuilding())
	      .append(", ");
	    if (isByRoomType) {
	    	sb.append(MESSAGES.utilSqlRoomType())
	    	  .append(", ");
	    }
	    newline(sb, 10);
	    if (includeDept) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlDepartment());	    	
		    sb.append(",");
	    }
	    if (includeRoomControlingDepartment) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlRoomDept());	    	
		    sb.append(",");	    	
	    }
	    if (includeSubjectArea || includeSection) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSubject());	    	
		    sb.append(",");
	    }
	    if (includeSection) {
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlCourseNbr());	    	
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlItype());	    	
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSection());	    	
		    sb.append(",");
	    }
	    newline(sb, 10);
	    sb.append(MESSAGES.utilSqlEventType());	    	
	    sb.append(",");
	    newline(sb, 10);
	    sb.append(MESSAGES.utilSqlUtilizationType());	    	
		newline(sb, 0);
	
		return sb.toString();
}
	

	public String getBuildingUsageAndOccupancyByRoomTypeQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept, ArrayList<String> headerRow) {
		return getBuildingUsageAndOccupancyByRoomTypeQuery(acadSession, includeSubjectArea, includeDept, false, false, headerRow);
	}

	public String getBuildingUsageAndOccupancyByRoomTypeQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept, 
			boolean includeSection, boolean includeRoomControlingDepartment, ArrayList<String> headerRow) {
		return getBuildingUsageAndOccupancyQuery(acadSession, true, includeSubjectArea, includeDept, 
				includeSection, includeRoomControlingDepartment, headerRow);
	}

	public String getBuildingUsageAndOccupancyWholeBuildingQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept, ArrayList<String> headerRow) {
		return getBuildingUsageAndOccupancyWholeBuildingQuery(acadSession, includeSubjectArea, includeDept, false, false, headerRow);
	}

	public String getBuildingUsageAndOccupancyWholeBuildingQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept,
			boolean includeSection, boolean includeRoomControlingDepartment, ArrayList<String> headerRow) {
		return getBuildingUsageAndOccupancyQuery(acadSession, false, includeSubjectArea, includeDept, includeSection, includeRoomControlingDepartment, headerRow);
	}

	protected void indent(StringBuffer stringBuffer, int indentSizeInChars) {
	    for (int i = 0; i < indentSizeInChars; i++) {
	    	stringBuffer.append(" ");
	    }
	}

	protected void newline(StringBuffer stringBuffer, int indentSizeInChars) {
		stringBuffer.append("\n");
	    indent(stringBuffer, indentSizeInChars);
	}

}
