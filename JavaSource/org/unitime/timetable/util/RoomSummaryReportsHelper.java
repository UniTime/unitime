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
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomDAO;

public abstract class RoomSummaryReportsHelper {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	
	public RoomSummaryReportsHelper(int iMinutesInPeriod, int iPeriodsInHour, int iAllDayStartPeriod, int iAllDayEndPeriod,
			int iStandardDayStartPeriod, int iStandardDayEndPeriod, int iWeekendDayStartPeriod,
			int iWeekendDayEndPeriod, int iFirstFullHourStartPeriod, int iLastFullHourStopPeriod, String iSchema) {
		super();
		org.unitime.commons.hibernate.util.HibernateUtil.isOracle();

		this.iMinutesInPeriod = iMinutesInPeriod;
		this.iPeriodsInHour = iPeriodsInHour;
		this.iAllDayStartPeriod = iAllDayStartPeriod;
		this.iAllDayEndPeriod = iAllDayEndPeriod;
		this.iStandardDayStartPeriod = iStandardDayStartPeriod;
		this.iStandardDayEndPeriod = iStandardDayEndPeriod;
		this.iWeekendDayStartPeriod = iWeekendDayStartPeriod;
		this.iWeekendDayEndPeriod = iWeekendDayEndPeriod;
		this.iFirstFullHourStartPeriod = iFirstFullHourStartPeriod;
		this.iLastFullHourStopPeriod = iLastFullHourStopPeriod;
		this.iSchema = iSchema;
		this.iAllDays = new ArrayList<Integer>();
		this.iWeekDays = new ArrayList<Integer>();
		for (int i = 2; i <= 6; i++) {
			this.iAllDays.add(i);
			this.iWeekDays.add(i);
		}
		this.iAllDays.add(7);
		this.iAllDays.add(1);
		this.iSaturday = 7;
	}
	
	public RoomSummaryReportsHelper() {
		super();
		this.iMinutesInPeriod = 5;
		this.iPeriodsInHour = 12;
		this.iAllDayStartPeriod = 0;
		this.iAllDayEndPeriod = 288;
		this.iStandardDayStartPeriod = 90;
		this.iStandardDayEndPeriod = 210;
		this.iWeekendDayStartPeriod = 90;
		this.iWeekendDayEndPeriod = 162;
		this.iFirstFullHourStartPeriod = 6;
		this.iLastFullHourStopPeriod = 282;
		this.iSchema = "timetable"; //TODO: determine if this is necessary, or set it to the schema for the database in this instance
		this.iAllDays = new ArrayList<Integer>();
		this.iWeekDays = new ArrayList<Integer>();
		for (int i = 2; i <= 6; i++) {
			this.iAllDays.add(i);
			this.iWeekDays.add(i);
		}
		this.iAllDays.add(7);
		this.iAllDays.add(1);
		this.iSaturday = 7;
	}
	
	private int iMinutesInPeriod;
	private int iPeriodsInHour;
	private int iAllDayStartPeriod;
	private int iAllDayEndPeriod;
	private int iStandardDayStartPeriod;
	private int iStandardDayEndPeriod;
	private int iWeekendDayStartPeriod;
	private int iWeekendDayEndPeriod;
	private int iFirstFullHourStartPeriod;
	private int iLastFullHourStopPeriod;
	private String iSchema;
    private ArrayList<Integer> iAllDays = null;
    private ArrayList<Integer> iWeekDays = null ;
    private Integer iSaturday = null; 
    private Boolean iShowAdditionalPurdueData;
	
	public int getMinutesInPeriod() {
		return iMinutesInPeriod;
	}
	public void setMinutesInPeriod(int minutesInPeriod) {
		this.iMinutesInPeriod = minutesInPeriod;
	}
	public int getPeriodsInHour() {
		return iPeriodsInHour;
	}
	public void setPeriodsInHour(int periodsInHour) {
		this.iPeriodsInHour = periodsInHour;
	}
	public int getAllDayStartPeriod() {
		return iAllDayStartPeriod;
	}
	public void setAllDayStartPeriod(int allDayStartPeriod) {
		this.iAllDayStartPeriod = allDayStartPeriod;
	}
	public int getAllDayEndPeriod() {
		return iAllDayEndPeriod;
	}
	public void setAllDayEndPeriod(int allDayEndPeriod) {
		this.iAllDayEndPeriod = allDayEndPeriod;
	}
	public int getStandardDayStartPeriod() {
		return iStandardDayStartPeriod;
	}
	public void setStandardDayStartPeriod(int standardDayStartPeriod) {
		this.iStandardDayStartPeriod = standardDayStartPeriod;
	}
	public int getStandardDayEndPeriod() {
		return iStandardDayEndPeriod;
	}
	public void setStandardDayEndPeriod(int standardDayEndPeriod) {
		this.iStandardDayEndPeriod = standardDayEndPeriod;
	}
	public int getWeekendDayStartPeriod() {
		return iWeekendDayStartPeriod;
	}
	public void setWeekendDayStartPeriod(int weekendDayStartPeriod) {
		this.iWeekendDayStartPeriod = weekendDayStartPeriod;
	}
	public int getWeekendDayEndPeriod() {
		return iWeekendDayEndPeriod;
	}
	public void setWeekendDayEndPeriod(int weekendDayEndPeriod) {
		this.iWeekendDayEndPeriod = weekendDayEndPeriod;
	}
	public int getFirstFullHourStartPeriod() {
		return iFirstFullHourStartPeriod;
	}
	public void setFirstFullHourStartPeriod(int firstFullHourStartPeriod) {
		this.iFirstFullHourStartPeriod = firstFullHourStartPeriod;
	}
	public int getLastFullHourStopPeriod() {
		return iLastFullHourStopPeriod;
	}
	public void setLastFullHourStopPeriod(int lastFullHourStopPeriod) {
		this.iLastFullHourStopPeriod = lastFullHourStopPeriod;
	}
	public String getSchema() {
		return iSchema;
	}
	public void setSchema(String schema) {
		this.iSchema = schema;
	}

	public ArrayList<Integer> getAllDays() {
		return iAllDays;
	}

	public void setAllDays(ArrayList<Integer> allDays) {
		this.iAllDays = allDays;
	}

	public ArrayList<Integer> getWeekDays() {
		return iWeekDays;
	}

	public void setWeekDays(ArrayList<Integer> weekDays) {
		this.iWeekDays = weekDays;
	}

	public Integer getSaturday() {
		return iSaturday;
	}

	public void setSaturday(Integer saturday) {
		this.iSaturday = saturday;
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

	protected void indent(StringBuffer stringBuffer, int indentSizeInChars) {
	    for (int i = 0; i < indentSizeInChars; i++) {
	    	stringBuffer.append(" ");
	    }
	}

	protected void newline(StringBuffer stringBuffer, int indentSizeInChars) {
		stringBuffer.append("\n");
	    indent(stringBuffer, indentSizeInChars);
	}

	protected String getDayOfWeekLabel(Integer day) {
		StringBuffer dayLabel = new StringBuffer();
	    if (day == this.getAllDays().get(0)) {
	    	dayLabel.append(Constants.MSG.monday());
	    } else if (day == this.getAllDays().get(1)) {
	    	dayLabel.append(Constants.MSG.tuesday());
	    } else if (day == this.getAllDays().get(2)) {
	    	dayLabel.append(Constants.MSG.wednesday());
	    } else if (day == this.getAllDays().get(3)) {
	    	dayLabel.append(Constants.MSG.thursday());
	    } else if (day == this.getAllDays().get(4)) {
	    	dayLabel.append(Constants.MSG.friday());
	    } else if (day == this.getAllDays().get(5)) {
	    	dayLabel.append(Constants.MSG.saturday());
	    } else if (day == this.getAllDays().get(6)) {
	    	dayLabel.append(Constants.MSG.sunday());
	    }
	    return(dayLabel.toString());
	}

	protected String getTimeLabel(int fiveMinutePeriod, boolean isPretty) {
		StringBuffer timeLabel = new StringBuffer();
	    int hour = fiveMinutePeriod / getPeriodsInHour();
	    int minutes = (fiveMinutePeriod % getPeriodsInHour()) * getMinutesInPeriod();

	    if (hour == 0) {
	    	timeLabel.append("12");
	    }
	    else if (hour < 10) {
	        timeLabel.append(0);
	        timeLabel.append(hour);
	    } else if (hour < 13) {
	    	timeLabel.append(hour);
	    } else if (hour < 22) {
	    	timeLabel.append(0);
	        timeLabel.append(hour - 12);
	    }
	    else {
	        timeLabel.append(hour - 12);
	    }

	    if (isPretty) {
	    	timeLabel.append(":");
	    }

	    if (minutes < 10) {
	        timeLabel.append(0);
	        timeLabel.append(minutes);
	    } else {
	    	timeLabel.append(minutes);
	    }
	    if (isPretty) {
	    	timeLabel.append(" ");
	    }
	    if (hour < 12) {
	    	timeLabel.append("am");
	    } else {
	    	timeLabel.append("pm");
	    }
	    return(timeLabel.toString());  
	}

	protected String getDayTimeLabel(Integer day, int fiveMinutePeriod, String labelPrefix) {
		StringBuffer dayTime = new StringBuffer();
	    dayTime.append(labelPrefix);
	    dayTime.append("_");
	    dayTime.append(getDayOfWeekLabel(day));
	    dayTime.append("_");
	    dayTime.append(getTimeLabel(fiveMinutePeriod, false));
	    return(dayTime.toString());
	}

	protected String getPrettyDayTimeLabel(int order, Integer day, int fiveMinutePeriod) {
		StringBuffer dayTime = new StringBuffer();
		if (order < 100) {
			dayTime.append("0");
		}
		if (order < 10) {
			dayTime.append("0");			
		}
	    dayTime.append(order);
	    dayTime.append("-");
	    dayTime.append(getDayOfWeekLabel(day));
	    dayTime.append("_");
	    dayTime.append(getTimeLabel(fiveMinutePeriod, true));
	    return(dayTime.toString());
	}
	
	protected void appendSelectedField(StringBuffer sb, String tableName, String fieldName, boolean hasLeadingComma, boolean hasTrailingComma) {
		if(hasLeadingComma) {
			sb.append(", ");
		}
		sb.append(tableName)
		  .append(".")
		  .append(fieldName);
		if(hasTrailingComma) {
			sb.append(", ");
		}
	}


	protected String getEventTypeDecodeCaseStatement(int startingIndent) {
		StringBuffer sb = new StringBuffer();
	    sb.append("(case");
	    newline(sb, startingIndent + 4);
	    sb.append("when e.event_type = 0 then '")
	      .append(MESSAGES.utilSqlEventTypeClass())
	      .append("'");
	    newline(sb, startingIndent + 4);
	    sb.append("when e.event_type = 1 then '")
	      .append(MESSAGES.utilSqlEventTypeFinalExam())
	      .append("'");
	    newline(sb, startingIndent + 4);
	    sb.append("when e.event_type = 2 then '")
	      .append(MESSAGES.utilSqlEventTypeMidtermExam())
	      .append("'");
	    newline(sb, startingIndent + 4);
	    sb.append("when e.event_type = 3 then '")
	      .append(MESSAGES.utilSqlEventTypeCourseRelated())
	      .append("'");
	    newline(sb, startingIndent + 4);
	    sb.append("when e.event_type = 4 then '")
	      .append(MESSAGES.utilSqlEventTypeSpecialEvent())
	      .append("'");
	    newline(sb, startingIndent + 4);
	    sb.append("when e.event_type = 5 then '")
	      .append(MESSAGES.utilSqlEventTypeRoomNotAvailable())
	      .append("'");
	    newline(sb, startingIndent + 4);
	    sb.append("end)");
	    return(sb.toString());
	}

	protected String getDayEqualsRestriction(Integer day) {
		StringBuffer dayEquals = new StringBuffer();
	    dayEquals.append("to_char(z.meeting_date, 'D' ) = ");
	    dayEquals.append(day);
	    return(dayEquals.toString());
	}

	protected String getDayInRestriction(ArrayList<Integer> days) {
		StringBuffer dayIn = new StringBuffer();
	    dayIn.append("to_char(z.meeting_date, 'D' ) in (");
	    boolean first = true;
	    for (Integer day : days) {
	        if (first) {
	        	first = false;
	        } else {
	        	dayIn.append(", ");
	        }
	        dayIn.append(day);
	    }
	    dayIn.append(" )");
	    return(dayIn.toString());
	}

	protected String getTimeRestriction(String first5MinutePeriod, String last5MinutePeriod) {
		StringBuffer timeRestriction = new StringBuffer();
	    timeRestriction.append("z.start_period < ");
	    timeRestriction.append(last5MinutePeriod);
	    timeRestriction.append(" and z.stop_period > ");
	    timeRestriction.append(first5MinutePeriod);
	    return(timeRestriction.toString());
	}

	protected void buildElse0End(StringBuffer stringBuffer, int indentSizeInChars) {
	    indent(stringBuffer, indentSizeInChars);
	    stringBuffer.append("else 0");
	    newline(stringBuffer, indentSizeInChars);
	    stringBuffer.append("end)");
	}

	protected abstract String getSummaryCalculationForPeriod(String first5MinutePeriod, String last5MinutePeriod, String additionalQueryField);

	protected void buildWhenThen(StringBuffer stringBuffer, String dayRestriction, String first5MinutePeriod, String last5MinutePeriod, String additionalQueryField) {
	    indent(stringBuffer, 12);
	    stringBuffer.append("when ");
	    stringBuffer.append(dayRestriction);
	    stringBuffer.append(" and ");
	    stringBuffer.append(getTimeRestriction(first5MinutePeriod, last5MinutePeriod));
	    stringBuffer.append(" then");
	    newline(stringBuffer, 16);
	    stringBuffer.append(getSummaryCalculationForPeriod(first5MinutePeriod, last5MinutePeriod, additionalQueryField));
	    newline(stringBuffer, 0);
	}

	protected void buildSum(StringBuffer stringBuffer, ArrayList<ArrayList<String>> restrictionsArray, boolean leadingComma, String additionalQueryField) {
	    if (leadingComma) {
	    	stringBuffer.append(",");
	    }
	    newline(stringBuffer, 8);
	    stringBuffer.append("sum(case");
	    for (ArrayList<String> restrictions : restrictionsArray) {
	        newline(stringBuffer, 0);
	        buildWhenThen(stringBuffer, restrictions.get(0), restrictions.get(1), restrictions.get(2), additionalQueryField);
	    }
	    buildElse0End(stringBuffer, 12);
	}

	protected void buildSumWithAsLabel(StringBuffer stringBuffer, ArrayList<ArrayList<String>> restrictionsArray, String sumName, boolean leadingComma, String additionalQueryField) {
	    buildSum(stringBuffer, restrictionsArray, leadingComma, additionalQueryField);
	    indent(stringBuffer, 8);
	    stringBuffer.append("as ");
	    stringBuffer.append(sumName);
	}

	protected void buildStandardWeekdayHoursSum(StringBuffer stringBuffer, ArrayList<Integer> weekdays, boolean leadingComma, ArrayList<Object> headerRow, LabelFieldPair labelFieldPair) {
		ArrayList<ArrayList<String>> restrictionsArray = createNewRestrictionsArray();
		addToRestrictionsArray(restrictionsArray, getDayInRestriction(weekdays), getStandardDayStartPeriod(), getStandardDayEndPeriod());
	    buildSumWithAsLabel(stringBuffer, restrictionsArray, (labelFieldPair.getLabel() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix()), leadingComma, labelFieldPair.getField());
	    headerRow.add(labelFieldPair.getLabel() + MESSAGES.utilSqlTotalStandardWeekdayHoursSuffix()); 
	}

	protected void buildStandardHoursSum(StringBuffer stringBuffer, ArrayList<Integer> weekdays, Integer saturday, boolean leadingComma, ArrayList<Object> headerRow, LabelFieldPair labelFieldPair) {
		ArrayList<ArrayList<String>> restrictionsArray = createNewRestrictionsArray();
		addToRestrictionsArray(restrictionsArray, getDayInRestriction(weekdays), getStandardDayStartPeriod(), getStandardDayEndPeriod());
		addToRestrictionsArray(restrictionsArray, getDayEqualsRestriction(saturday), getWeekendDayStartPeriod(), getWeekendDayEndPeriod());
		buildSumWithAsLabel(stringBuffer, restrictionsArray, (labelFieldPair.getLabel() + MESSAGES.utilSqlTotalStandardHoursSuffix()), leadingComma, labelFieldPair.getField());
	    headerRow.add(labelFieldPair.getLabel() + MESSAGES.utilSqlTotalStandardHoursSuffix());
	}

	protected void buildAllHoursSum(StringBuffer sb, ArrayList<Integer> alldays, boolean leadingComma, ArrayList<Object> headerRow, LabelFieldPair labelFieldPair) {
		ArrayList<ArrayList<String>> restrictionsArray = createNewRestrictionsArray();
		addToRestrictionsArray(restrictionsArray, getDayInRestriction(alldays), getAllDayStartPeriod(), getAllDayEndPeriod());
	    buildSumWithAsLabel(sb, restrictionsArray, (labelFieldPair.getLabel() + MESSAGES.utilSqlTotalAllHoursSuffix()), leadingComma, labelFieldPair.getField());
	    headerRow.add(labelFieldPair.getLabel() + MESSAGES.utilSqlTotalAllHoursSuffix());
	}

	protected ArrayList<ArrayList<String>> createNewRestrictionsArray(){
		return new ArrayList<ArrayList<String>>();
	}

	protected void addToRestrictionsArray(ArrayList<ArrayList<String>> restrictionsArray, String restriction, int firstPeriod, int lastPeriod) {
		ArrayList<String> restrictions = new ArrayList<String>();
		restrictions.add(restriction);
		restrictions.add(Integer.toString(firstPeriod));
		restrictions.add(Integer.toString(lastPeriod));
	
		restrictionsArray.add(restrictions);
	}

	protected abstract void addSumStatementToStringBuffer(StringBuffer sb, String sum, String label, boolean leadingComma);
	
	protected void buildDayOfWeekTimeOfDaySums(StringBuffer sb, ArrayList<Integer> days, boolean leadingComma, ArrayList<Object> headerRow, boolean includeDayOfWkTimeOfDayInHeaderRow, LabelFieldPair labelFieldPair, boolean isFirstDayTime) {
		ArrayList<ArrayList<String>> restrictionsArray = null;
		boolean useComma = leadingComma;
		StringBuffer sbSum = null;
		for (Integer day : days) {
			restrictionsArray = createNewRestrictionsArray();
			addToRestrictionsArray(restrictionsArray, getDayEqualsRestriction(day), getAllDayStartPeriod(), getFirstFullHourStartPeriod());
			String label = getDayTimeLabel(day, getAllDayStartPeriod(), labelFieldPair.getLabel());
			sbSum = new StringBuffer();
			buildSum(sbSum, restrictionsArray, false, labelFieldPair.getField());
			addSumStatementToStringBuffer(sb, sbSum.toString(), label, useComma);
			if (!useComma) {
				useComma = true;
			}
			
			if (includeDayOfWkTimeOfDayInHeaderRow) {
		    	headerRow.add(label);
		    }
		    int startPeriod = getFirstFullHourStartPeriod();
		    while (startPeriod < getLastFullHourStopPeriod()) {
		        int stopPeriod = startPeriod + getPeriodsInHour();
				restrictionsArray = createNewRestrictionsArray();
				addToRestrictionsArray(restrictionsArray, getDayEqualsRestriction(day), startPeriod, stopPeriod);
				label = getDayTimeLabel(day, startPeriod, labelFieldPair.getLabel());
				sbSum = new StringBuffer();
				buildSum(sbSum, restrictionsArray, false, labelFieldPair.getField());
				addSumStatementToStringBuffer(sb, sbSum.toString(), label, useComma);
			    if (includeDayOfWkTimeOfDayInHeaderRow) {
			    	headerRow.add(label);
			    }
		        startPeriod = stopPeriod;
		    }
			restrictionsArray = createNewRestrictionsArray();
			addToRestrictionsArray(restrictionsArray, getDayEqualsRestriction(day), getLastFullHourStopPeriod(), getAllDayEndPeriod());
			label = getDayTimeLabel(day, getLastFullHourStopPeriod(), labelFieldPair.getLabel());
			sbSum = new StringBuffer();
			buildSum(sbSum, restrictionsArray, false, labelFieldPair.getField());
			addSumStatementToStringBuffer(sb, sbSum.toString(), label, useComma);
			if (includeDayOfWkTimeOfDayInHeaderRow) {
		    	headerRow.add(label);
		    }
		}
		if (!includeDayOfWkTimeOfDayInHeaderRow) {
			if (isFirstDayTime) {
				headerRow.add(MESSAGES.utilSqlDayTime());
			}
			headerRow.add(labelFieldPair.getLabel()); 
		}
	}

	protected String getUnPivotAsStatement(int order, Integer day, int period, boolean leadingComma, ArrayList<String> dayTimeLabelPrefixes) {
		StringBuffer sb = new StringBuffer();
		if (leadingComma) {
			sb.append(",");
		}
		newline(sb, 8);
		boolean first = true;
		sb.append(" ( ");
		for (String dayTimeLabelPrefix : dayTimeLabelPrefixes) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(getDayTimeLabel(day, period, dayTimeLabelPrefix));
		}
		sb.append(" ) as '");
		sb.append(getPrettyDayTimeLabel(order, day, period));
		sb.append("'");
		return(sb.toString());
	}

	protected String getUnPivot(ArrayList<Integer> allDays, ArrayList<String> newColumns, ArrayList<String> dayTimeLabelPrefixes) {
		StringBuffer sb = new StringBuffer();
	    sb.append("unpivot (");
	    newline(sb, 4);
	    boolean first = true;
	    sb.append("( ");
	    for (String newColumn : newColumns) {
	    	if (first) {
	    		first = false;
	    	} else {
	    		sb.append(", ");
	    	}
		    sb.append(newColumn);
	    }
	    sb.append(" )");
	    newline(sb, 4);
	    sb.append("for ")
	      .append(MESSAGES.utilSqlDayTime());
	    newline(sb, 4);
	    sb.append("in (");
	    first = true;
	    int periodCount = 1;
	    for (Integer day : allDays) {
	        sb.append(getUnPivotAsStatement(periodCount, day, getAllDayStartPeriod(), !first, dayTimeLabelPrefixes));
	        periodCount = periodCount + 1;
	        if (first) {
	        	first = false;
	        }
	        int startPeriod = getFirstFullHourStartPeriod();
	        while (startPeriod < getLastFullHourStopPeriod()) {
	            sb.append(getUnPivotAsStatement(periodCount, day, startPeriod, !first, dayTimeLabelPrefixes));
	            startPeriod = startPeriod + getPeriodsInHour();
	            periodCount = periodCount + 1;
	        }
	        sb.append(getUnPivotAsStatement(periodCount, day, getLastFullHourStopPeriod(), !first, dayTimeLabelPrefixes));
	    }
	    sb.append(")");
	    newline(sb, 0);
	    sb.append(")");
	    newline(sb, 0);
	    return(sb.toString());
	}

	protected String getCalculationForCountOfDayOfWeek(String meetingDateStr, String firstDateStr, String lastDateStr, boolean inclusiveOfEndDate, int startingIndentSize) {
		StringBuffer sb = new StringBuffer();
	    sb.append("floor((");
	    sb.append(lastDateStr);
	    sb.append(" - ");
	    sb.append(firstDateStr);
	    if (inclusiveOfEndDate) {
	    	sb.append(" + 1");
	    }
	    sb.append(") / 7) * 1.0 + ");
	    newline(sb, startingIndentSize);
	    sb.append("(case");
	    newline(sb, startingIndentSize + 4);
	    sb.append("when mod((") ;
	    sb.append(lastDateStr);
	    sb.append(" - ");
	    sb.append(firstDateStr);
	    if (inclusiveOfEndDate) {
	    	sb.append(" + 1");
	    }
	    sb.append("), 7) > 0 ");
	    newline(sb, startingIndentSize + 4);
	    sb.append("then ");
	    newline(sb, startingIndentSize + 8);
	    sb.append("(case");
	    newline(sb, startingIndentSize + 12);
	    sb.append("when to_char(");
	    sb.append(meetingDateStr);
	    sb.append(", 'd') >= to_char(");
	    sb.append(firstDateStr);
	    sb.append(", 'd')");
	    newline(sb, startingIndentSize + 16);
	    sb.append("and to_char(");
	    sb.append(meetingDateStr);
	    sb.append(", 'd') <= (to_char(");
	    sb.append(firstDateStr);
	    sb.append(", 'd') + (mod((");
	    sb.append(lastDateStr);
	    sb.append(" - ");
	    sb.append(firstDateStr);
	    if (inclusiveOfEndDate) {
	    	sb.append(" + 1");
	    }
	    sb.append("), 7) - 1 ))");
	    newline(sb, startingIndentSize + 12);
	    sb.append("then 1.0");
	    newline(sb, startingIndentSize + 12);
	    sb.append("else 0.0");
	    newline(sb, startingIndentSize + 12);
	    sb.append("end )");
	    newline(sb, startingIndentSize + 4);
	    sb.append("else 0.0");
	    newline(sb, startingIndentSize + 4);
	    sb.append("end)");
	    return(sb.toString());
	}

	protected String getCampusRegionSubQuery() {
		StringBuffer sb = new StringBuffer();
	    sb.append("select listagg (rg.name, ' ') within group ( order by rg.name) from ");
	    sb.append(getSchema());
	    sb.append(".room_group_room rgr inner join ");
	    sb.append(getSchema());
	    sb.append(".room_group rg on rg.uniqueid = rgr.room_group_id and rg.department_id is null and rg.abbv in ( 'North', 'Central', 'South', 'Village', 'Remote' ) where rgr.room_id = r.uniqueId");
	    return(sb.toString());
	}

	protected String getLLRandLALRsubQuery() {
		StringBuffer sb = new StringBuffer();
	    sb.append("select listagg ((case when d.dept_code = '1977' then 'Large Active Learn Room' when d.dept_code = '1994' then 'Large Lecture Room' else null end) , ' ') within group ( order by d.dept_code) from ");
	    sb.append(getSchema());
	    sb.append(".room_dept rd inner join ");
	    sb.append(getSchema());
	    sb.append(".department d on d.uniqueid = rd.department_id and d.dept_code in ( '1977', '1994' ) where rd.room_id = r.uniqueId");
	    return(sb.toString());
	}

	protected String getClassroomSubTypeSubQuery() {
		StringBuffer sb = new StringBuffer();
	    sb.append("select listagg ((case when d.dept_code = '1975' then 'Active Learn Room' when d.dept_code = '1979' then 'Traditional Classroom' else null end) , ' ') within group ( order by d.dept_code) from ");
	    sb.append(getSchema());
	    sb.append(".room_dept rd inner join ");
	    sb.append(getSchema());
	    sb.append(".department d on d.uniqueid = rd.department_id and d.dept_code in ( '1975', '1979' ) where rd.room_id = r.uniqueId");
	    return(sb.toString());
	}

	protected String getTraditionalUtilizationCheck() {
		StringBuffer sb = new StringBuffer();
	    sb.append("e.event_type in (0, 1, 2, 3) and m.meeting_date >= sess.session_begin_date_time and m.meeting_date < sess.exam_begin_date");
	    return(sb.toString());
	}

	protected String getFinalExamUtilizationCheck() {
		StringBuffer sb = new StringBuffer();
	    sb.append("e.event_type = 1 and m.meeting_date >= sess.exam_begin_date and m.meeting_date <= sess.session_end_date_time");
	    return(sb.toString());
	}

	protected String getUtilizationTypeCaseStatement() {
		StringBuffer sb = new StringBuffer();
	    sb.append("case");
	    newline(sb, 8);
	    sb.append("when ");
	    sb.append(getTraditionalUtilizationCheck());
	    newline(sb, 8);
	    sb.append("then '")
	      .append(MESSAGES.utilSqlUtilizationTypeTraditional())
	      .append("' -- classes = divide by 15.0 for Summer and Fall, summer divide by number of occurances of each day of the week in the term.");
	    newline(sb, 8);
	    sb.append("when ");
	    sb.append(getFinalExamUtilizationCheck());
	    newline(sb, 8);
	    sb.append("then '")
	      .append(MESSAGES.utilSqlUtilizationTypeFinalExamsWeek())
	      .append("' -- final exams = divide by 1.0");
	    newline(sb, 8);
	    sb.append("else '")
	      .append(MESSAGES.utilSqlUtilizationTypeSpecialEvent())
	      .append("' -- events = divide by number of occurances of the day of the week between the event start date and the event end date");
	    newline(sb, 8);
	    sb.append("end");
	    return(sb.toString());
	}

	protected String getWeeksDivisorCaseStatement() {
		StringBuffer sb = new StringBuffer();
	    sb.append("case");
	    newline(sb, 8);
	    sb.append("when ");
	    sb.append(getTraditionalUtilizationCheck());
	    newline(sb, 8);
	    sb.append("then");
	    newline(sb, 12);
	    sb.append("(case");
	    newline(sb, 16);
	    sb.append("when sess.academic_term = 'Summer'");
	    newline(sb, 16);
	    sb.append("then ");
	    sb.append(getCalculationForCountOfDayOfWeek("m.meeting_date", "sess.session_begin_date_time", "sess.exam_begin_date", false, 20));
	    newline(sb, 16);
	    sb.append("else 15.0");
	    newline(sb, 16);
	    sb.append("end )");
	    newline(sb, 8);
	    sb.append("when ");
	    sb.append(getFinalExamUtilizationCheck());
	    newline(sb, 8);
	    sb.append("then ");
	    sb.append(getCalculationForCountOfDayOfWeek("m.meeting_date", "sess.exam_begin_date", "sess.session_end_date_time", true, 12));
	    newline(sb, 8);
	    sb.append("else ");
	    sb.append(getCalculationForCountOfDayOfWeek("m.meeting_date", "sess.event_begin_date", "sess.event_end_date", true, 12));
	    newline(sb, 8);
	    sb.append("end");
	    return(sb.toString());
	}

	protected String getSeatsRequestedCaseStatement() {
		StringBuffer sb = new StringBuffer();
	    sb.append("case");
	    newline(sb, 8);
	    sb.append("when e.event_type = 0");
	    newline(sb, 8);
	    sb.append("then");
	    newline(sb, 12);
	    sb.append("(select c.expected_capacity from ")
	      .append(getSchema())
	      .append(".class_ c where c.uniqueid = e.class_id)");
	    newline(sb, 8);
	    sb.append("when e.event_type in (1, 2)");
	    newline(sb, 8);
	    sb.append("then ");
	    newline(sb, 12);
	    sb.append("case when");
	    newline(sb, 12);
	    sb.append("e.max_capacity > ");
	    appendExamsSeatsUsedQuery(sb);
	    newline(sb, 12);
        sb.append("then");
	    newline(sb, 12);
	    sb.append("e.max_capacity");
	    newline(sb, 12);
        sb.append("else");
	    newline(sb, 12);
	    appendExamsSeatsUsedQuery(sb);
	    newline(sb, 12);
        sb.append("end");
	    newline(sb, 8);
	    sb.append("when e.event_type = 3");
	    newline(sb, 8);
	    sb.append("then ");
	    newline(sb, 12);
	    sb.append("(select count (distinct s.uniqueid)");
	    newline(sb, 12);
	    sb.append("from ")
	      .append(getSchema())
	      .append(".related_course_info rci, ")
	      .append(getSchema())
	      .append(".student s ");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".student_class_enrl sce on sce.student_id = s.uniqueid");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".class_ c on c.uniqueid = sce.class_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".scheduling_subpart ss on ss.uniqueid = c.subpart_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".instr_offering_config ioc on ioc.uniqueid = ss.config_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".course_offering co on co.uniqueid = sce.course_offering_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".instructional_offering io on io.uniqueid = co.instr_offr_id");
	    newline(sb, 12);
	    sb.append("where rci.event_id = e.uniqueid");
	    newline(sb, 16);
	    sb.append("    and ((rci.owner_type = 3 and rci.owner_id = c.uniqueid)");
	    newline(sb, 16);
	    sb.append("	or (rci.owner_type = 2 and rci.owner_id = ioc.uniqueid)");
	    newline(sb, 20);
	    sb.append("	or (rci.owner_type = 1 and rci.owner_id = co.uniqueid)");
	    newline(sb, 20);
	    sb.append("	or (rci.owner_type = 0 and rci.owner_id = io.uniqueid)))");	    
	    newline(sb, 8);
	    sb.append("else");
	    newline(sb, 12);
	    sb.append("(case");
	    newline(sb, 16);
	    sb.append("when e.max_capacity is not null");
	    newline(sb, 16);
	    sb.append("then ");
	    newline(sb, 20);
	    sb.append("e.max_capacity");
	    newline(sb, 16);
	    sb.append("when e.min_capacity is not null");
	    newline(sb, 16);
	    sb.append("then ");
	    newline(sb, 20);
	    sb.append("e.min_capacity");
	    newline(sb, 16);
	    sb.append("else 0 ");
	    newline(sb, 12);
	    sb.append("end )");
	    newline(sb, 8);
	    sb.append("end");
	    return(sb.toString());
	
	}
	
	protected void appendExamsSeatsUsedQuery(StringBuffer sb) {
	    sb.append("(select count (distinct s.uniqueid)");
	    newline(sb, 12);
	    sb.append("from ")
	      .append(getSchema())
	      .append(".exam ex");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".exam_owner eo on eo.exam_id = ex.uniqueid, ");
	    newline(sb, 12);
	    sb.append(getSchema())
	      .append(".student s ");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".student_class_enrl sce on sce.student_id = s.uniqueid");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".class_ c on c.uniqueid = sce.class_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".scheduling_subpart ss on ss.uniqueid = c.subpart_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".instr_offering_config ioc on ioc.uniqueid = ss.config_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".course_offering co on co.uniqueid = sce.course_offering_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".instructional_offering io on io.uniqueid = co.instr_offr_id");
	    newline(sb, 12);
	    sb.append("where ex.uniqueid = e.exam_id");
	    newline(sb, 16);
	    sb.append("and s.session_id = ex.session_id ");
	    newline(sb, 16);
	    sb.append("and ((eo.owner_type = 3 and eo.owner_id = c.uniqueid)");
	    newline(sb, 16);
	    sb.append("or (eo.owner_type = 2 and eo.owner_id = ioc.uniqueid)");
	    newline(sb, 20);
	    sb.append("or (eo.owner_type = 1 and eo.owner_id = co.uniqueid)");
	    newline(sb, 20);
	    sb.append("or (eo.owner_type = 0 and eo.owner_id = io.uniqueid)");
	    newline(sb, 16);
	    sb.append("))");

	}

	protected String getSeatsUsedCaseStatement() {
		StringBuffer sb = new StringBuffer();
	    sb.append("case");
	    newline(sb, 8);
	    sb.append("when e.event_type = 0");
	    newline(sb, 8);
	    sb.append("then");
	    newline(sb, 12);
	    sb.append("(select count (distinct stu.uniqueid) from ")
	      .append(getSchema())
	      .append(".student stu inner join ")
	      .append(getSchema())
	      .append(".student_class_enrl stu_enrl on stu_enrl.student_id = stu.uniqueid and stu_enrl.class_id = e.class_id)");
	    newline(sb, 8);
	    sb.append("when e.event_type in (1, 2)");
	    newline(sb, 8);
	    sb.append("then ");
	    newline(sb, 12);
	    appendExamsSeatsUsedQuery(sb);
	    newline(sb, 8);
	    sb.append("when e.event_type = 3");
	    newline(sb, 8);
	    sb.append("then ");
	    newline(sb, 12);
	    sb.append("(select count (distinct s.uniqueid)");
	    newline(sb, 12);
	    sb.append("from ")
	      .append(getSchema())
	      .append(".related_course_info rci, ")
	      .append(getSchema())
	      .append(".student s ");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".student_class_enrl sce on sce.student_id = s.uniqueid");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".class_ c on c.uniqueid = sce.class_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".scheduling_subpart ss on ss.uniqueid = c.subpart_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".instr_offering_config ioc on ioc.uniqueid = ss.config_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".course_offering co on co.uniqueid = sce.course_offering_id");
	    newline(sb, 12);
	    sb.append("inner join ")
	      .append(getSchema())
	      .append(".instructional_offering io on io.uniqueid = co.instr_offr_id");
	    newline(sb, 12);
	    sb.append("where rci.event_id = e.uniqueid");
	    newline(sb, 16);
	    sb.append("    and ((rci.owner_type = 3 and rci.owner_id = c.uniqueid)");
	    newline(sb, 16);
	    sb.append("	or (rci.owner_type = 2 and rci.owner_id = ioc.uniqueid)");
	    newline(sb, 20);
	    sb.append("	or (rci.owner_type = 1 and rci.owner_id = co.uniqueid)");
	    newline(sb, 20);
	    sb.append("	or (rci.owner_type = 0 and rci.owner_id = io.uniqueid)))");	    sb.append("else");
	    newline(sb, 12);
	    sb.append("(case");
	    newline(sb, 16);
	    sb.append("when e.max_capacity is not null");
	    newline(sb, 16);
	    sb.append("then ");
	    newline(sb, 20);
	    sb.append("e.max_capacity");
	    newline(sb, 16);
	    sb.append("when e.min_capacity is not null");
	    newline(sb, 16);
	    sb.append("then ");
	    newline(sb, 20);
	    sb.append("e.min_capacity");
	    newline(sb, 16);
	    sb.append("else 0 ");
	    newline(sb, 12);
	    sb.append("end )");
	    newline(sb, 8);
	    sb.append("end");
	    return(sb.toString());
	}

	protected abstract String getBaseQueryAdditionalSelectColumns();
	
	protected String getNumRoomsCalculation() {
		StringBuffer sb = new StringBuffer();
		sb.append("(select count(distinct r.uniqueid)");
	    newline(sb, 8);
		sb.append("from ")
	      .append(getSchema())
		  .append(".meeting om");
	    newline(sb, 8);
		sb.append("inner join ")
	      .append(getSchema())
		  .append(".room oth_r on oth_r.session_id = sess.uniqueid"); 
	    newline(sb, 8);
		sb.append("     and oth_r.permanent_id = om.location_perm_id");
	    newline(sb, 8);
		sb.append(" where om.event_id = m.event_id");
	    newline(sb, 8);
		sb.append("and om.meeting_date = m.meeting_date"); 
	    newline(sb, 8);
		sb.append("and om.start_period = m.start_period");
	    newline(sb, 8);
		sb.append("and om.stop_period = m.stop_period");
	    newline(sb, 8);
		sb.append(") as nbr_rooms");
       return sb.toString();
	}
	
	protected String getBaseQuerySelectClause(boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer();
	    sb.append("select distinct sess.academic_initiative as ")
	      .append(MESSAGES.utilSqlAcademicInitiative())
	      .append(",");
	    newline(sb, 4);
	    sb.append("sess.academic_term as ")
	      .append(MESSAGES.utilSqlAcademicTerm())
	      .append(",");
	    newline(sb, 4);
	    sb.append("sess.academic_year as ")
	      .append(MESSAGES.utilSqlAcademicYear())
	      .append(",");
	    newline(sb, 4);
	    sb.append("b.abbreviation as ")
	      .append(MESSAGES.utilSqlBuilding())
	      .append(",");	    
	    newline(sb, 4);
	    sb.append("r.uniqueid as room_id,");
	    newline(sb, 4);
	    sb.append("r.room_number as ")
	      .append(MESSAGES.utilSqlRoom())
	      .append(",");	    
	    newline(sb, 4);
	    sb.append("rt.label as ")
	      .append(MESSAGES.utilSqlRoomType())
	      .append(",");	    
	    newline(sb, 4);
	    sb.append("r.capacity as ")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(",");	    
	    newline(sb, 4);
	    if (isShowAdditionalPurdueData()) {
		    sb.append("( ").append(getCampusRegionSubQuery()).append(" ) as ")
		      .append(MESSAGES.utilSqlCampusRegion())
		      .append(",");	    
		    newline(sb, 4);
		    sb.append("( ").append(getLLRandLALRsubQuery()).append(" ) as ")
		      .append(MESSAGES.utilSqlLlrLalrPool())
		      .append(",");	    
		    newline(sb, 4);
		    sb.append("( ").append(getClassroomSubTypeSubQuery()).append(" ) as ")
		      .append(MESSAGES.utilSqlClassroomSubtype())
		      .append(",");	    
		    newline(sb, 4);
	    }
	    sb.append("e.event_type as ")
	      .append(MESSAGES.utilSqlEventType())
	      .append(",");	    
	    newline(sb, 4);
	    sb.append("( ").append(getEventTypeDecodeCaseStatement(4)).append(" ) as ")
	      .append(MESSAGES.utilSqlEventTypeDescription())
	      .append(",");	    
	    newline(sb, 4);
	    sb.append("( ").append(getUtilizationTypeCaseStatement()).append(" ) as ")
	      .append(MESSAGES.utilSqlUtilizationType())
	      .append(",");	    
	    newline(sb, 4);
	    sb.append("( ").append(getWeeksDivisorCaseStatement()).append(" ) as weeks_divisor_for_day_of_week,");
	    newline(sb, 4);
	    sb.append("m.meeting_date,");
	    newline(sb, 4);
	    sb.append("m.start_period,");
	    newline(sb, 4);
	    sb.append("m.stop_period,");
	    if (includeDept) {
		    newline(sb, 4);
		    sb.append(" ( ")
		      .append(ApplicationProperty.ExternalCourseDepartmentCodeLookupSQL.value().replace("%SCHEMA%", getSchema()))
		      .append(" ) as ")
		      .append(MESSAGES.utilSqlDepartment())
		      .append(",");	    
	    }
	    if (includeSubjectArea) {
		    newline(sb, 4);
		    sb.append("sa.subject_area_abbreviation as ")
		      .append(MESSAGES.utilSqlSubject())
		      .append(",");	    
	    }
	    sb.append(getBaseQueryAdditionalSelectColumns());
	    newline(sb, 4);
	    sb.append(getNumRoomsCalculation());
	    return(sb.toString());
	}

	protected String getBaseQueryFromClause() {
		StringBuffer sb = new StringBuffer();
	    sb.append("from ");
	    sb.append(getSchema());
	    sb.append(".sessions sess");
	    newline(sb, 0);
	    sb.append("inner join ");
	    sb.append(getSchema());
	    sb.append(".room r on r.session_id = sess.uniqueid");
	    newline(sb, 0);
	    sb.append("inner join ");
	    sb.append(getSchema());
	    sb.append(".building b on b.uniqueid = r.building_id");
	    newline(sb, 0);
	    sb.append("inner join ");
	    sb.append(getSchema());
	    sb.append(".room_type rt on rt.uniqueid = r.room_type");
	    newline(sb, 0);
	    sb.append("inner join ");
	    sb.append(getSchema());
	    sb.append(".meeting m on m.meeting_date >= sess.event_begin_date and m.meeting_date <= sess.event_end_date and m.approval_status = 1");
	    newline(sb, 0);
	    sb.append("inner join ");
	    sb.append(getSchema());
	    sb.append(".event e on e.uniqueId = m.event_id");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".class_ c on c.uniqueid = e.class_id");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".scheduling_subpart ss on ss.uniqueid = c.subpart_id");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".itype_desc i on i.itype = ss.itype");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".instr_offering_config ioc on ioc.uniqueId = ss.config_id");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".instructional_offering io on io.uniqueid = ioc.instr_offr_id");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".course_offering co on co.instr_offr_id = io.uniqueid");
	    newline(sb, 0);
	    sb.append("left outer join ");
	    sb.append(getSchema());
	    sb.append(".subject_area sa on sa.uniqueid = co.subject_area_id");
	    return(sb.toString());
	}

	protected String getBaseQueryWhereClause(String campus, String year, String term) {
		StringBuffer sb = new StringBuffer();
	    sb.append("where sess.academic_Initiative = '");
	    sb.append(campus);
	    sb.append("'");
	    newline(sb, 2);
	    sb.append("and sess.academic_year = '");
	    sb.append(year);
	    sb.append("' ");
	    newline(sb, 2);
	    sb.append("and sess.academic_Term = '");
	    sb.append(term);
	    sb.append("' ");
	    newline(sb, 0);
	    sb.append("--  and e.event_type = 0");
	    newline(sb, 0);
	    sb.append("--  and m.meeting_date >= sess.session_begin_date_time and m.meeting_date <= sess.classes_end_date_time");
	    newline(sb, 2);
	    sb.append("and r.permanent_id = m.location_perm_id");
	    newline(sb, 2);
	    sb.append(" and (c.uniqueId is null or (c.expected_capacity > 0 or 0 < (select count(cstu_enrl.uniqueid) from ")
	      .append(getSchema())
	      .append(".student_class_enrl cstu_enrl where cstu_enrl.class_id = c.uniqueid)))");
	    newline(sb, 2);
	    sb.append("and (c.uniqueId is null or c.nbr_rooms is not null)");
	    newline(sb, 2);
	    sb.append("and (c.uniqueId is null or c.nbr_rooms > 0)");
	    newline(sb, 2);
	    sb.append("and (sa.subject_area_abbreviation is null or sa.subject_area_abbreviation != 'REG')");
	    newline(sb, 2);
	    sb.append("and (co.uniqueId is null or co.is_Control = 1)");
	    newline(sb, 2);
	    sb.append("and 0 = ( select count(1) from ");
	    sb.append(getSchema());
	    sb.append(".course_type ct where ct.uniqueid = co.course_type_id and ct.reference in ( 'Fake', 'Not-Available') )");
	    return(sb.toString());
	}

	protected String getBaseQuery(String campus, String year, String term, boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer();
	    sb.append(getBaseQuerySelectClause(includeSubjectArea, includeDept));
	    newline(sb, 0);
	    sb.append(getBaseQueryFromClause());
	    newline(sb, 0);
	    sb.append(getBaseQueryWhereClause(campus, year, term));
	    newline(sb, 0);
	    return(sb.toString());
	}

	protected String getCapacityRangeCase() {
		StringBuffer sb = new StringBuffer();
	    sb.append("(case");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 0 then '0'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 0 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 20 then '1 to 20'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 20 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 30 then '21 to 30'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 30 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 40 then '31 to 40'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 40 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 60 then '41 to 60'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 60 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 100 then '61 to 100'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 100 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 150 then '101 to 150'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 150 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 200 then '151 to 200'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 200 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 250 then '201 to 250'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 250 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 300 then '251 to 300'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 300 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 350 then '301 to 350'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 350 and z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" <= 500 then '351 to 500'");
	    newline(sb, 8);
	    sb.append("when z.")
	      .append(MESSAGES.utilSqlRoomSize())
	      .append(" > 500 then 'Greater than 500'");
	    newline(sb, 8);
	    sb.append("end)");
	    return(sb.toString());
	}

	protected abstract ArrayList<LabelFieldPair> getLabelPrefixToAdditionalQueryFieldMapping();
	
	protected String getPivotedBaseSummaryQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, 
			Integer saturday, String campus, String year, String term, 
			ArrayList<Object> headerRow, boolean includeDayOfWkTimeOfDayInHeaderRow, 
			boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer();
	    sb.append("select ");
		appendSelectedField(sb, "z", MESSAGES.utilSqlAcademicInitiative(), false, true);
	    headerRow.add(MESSAGES.utilSqlAcademicInitiative()); 
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlAcademicTerm(), false, true);
	    headerRow.add(MESSAGES.utilSqlAcademicTerm()); 
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlAcademicYear(), false, true);
	    headerRow.add(MESSAGES.utilSqlAcademicYear()); 
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlRoomType(), false, true);
	    headerRow.add(MESSAGES.utilSqlRoomType());
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlBuilding(), false, true);
	    headerRow.add(MESSAGES.utilSqlBuilding());
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlRoom(), false, true);
	    headerRow.add(MESSAGES.utilSqlRoom());
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlRoomSize(), false, true);
	    headerRow.add(MESSAGES.utilSqlRoomSize());

	    if (isShowAdditionalPurdueData()) {
		    newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlCampusRegion(), false, true);
		    headerRow.add(MESSAGES.utilSqlCampusRegion());
		    newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlLlrLalrPool(), false, true);
		    headerRow.add(MESSAGES.utilSqlLlrLalrPool()); 
		    newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlClassroomSubtype(), false, true);
		    headerRow.add(MESSAGES.utilSqlClassroomSubtype());
	    }
	    newline(sb, 4);
	    sb.append(getCapacityRangeCase());
	    sb.append(" as ")
	      .append(MESSAGES.utilSqlRangeOfSizes())
	      .append(",");
	    headerRow.add(MESSAGES.utilSqlRangeOfSizes()); 
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlDepartment(), false, true);
			headerRow.add(MESSAGES.utilSqlDepartment());
		}
		if (includeSubjectArea) {
			newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlSubject(), false, true);
		    headerRow.add(MESSAGES.utilSqlSubject());
		}
	    
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlEventType(), false, true);
	    headerRow.add(MESSAGES.utilSqlEventType());
	    newline(sb, 4);
	    
		appendSelectedField(sb, "z", MESSAGES.utilSqlEventTypeDescription(), false, true);
	    headerRow.add(MESSAGES.utilSqlEventTypeDescription());
	    newline(sb, 4);
	    
		appendSelectedField(sb, "z", MESSAGES.utilSqlUtilizationType(), false, false);
	    headerRow.add(MESSAGES.utilSqlUtilizationType()); 
	    
	    for (LabelFieldPair labelFieldPair : getLabelPrefixToAdditionalQueryFieldMapping()) {
		    buildStandardWeekdayHoursSum(sb, weekDays, true, headerRow, labelFieldPair);
		    buildStandardHoursSum(sb, weekDays, saturday, true, headerRow, labelFieldPair);
		    buildAllHoursSum(sb, allDays, true, headerRow, labelFieldPair);	    	
	    }
	    
	    boolean first = true;
	    for (LabelFieldPair labelFieldPair : getLabelPrefixToAdditionalQueryFieldMapping()) {
	    	buildDayOfWeekTimeOfDaySums(sb, allDays, true, headerRow, includeDayOfWkTimeOfDayInHeaderRow, labelFieldPair, first);
	    	if (first) {
	    		first = false;
	    	}
	    }
	    newline(sb, 0);
	    sb.append("from (");
	    sb.append(getBaseQuery(campus, year, term, includeSubjectArea, includeDept));
	    sb.append(") z");
	    newline(sb, 0);
	    sb.append("group by ");
	    
		appendSelectedField(sb, "z", MESSAGES.utilSqlAcademicInitiative(), false, true);
		appendSelectedField(sb, "z", MESSAGES.utilSqlAcademicTerm(), false, true);
		appendSelectedField(sb, "z", MESSAGES.utilSqlAcademicYear(), false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlUtilizationType(), false, true);
		appendSelectedField(sb, "z", MESSAGES.utilSqlRoomType(), false, true);
		appendSelectedField(sb, "z", "room_id", false, true);
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlBuilding(), false, true);
		appendSelectedField(sb, "z", MESSAGES.utilSqlRoom(), false, true);
		appendSelectedField(sb, "z", MESSAGES.utilSqlRoomSize(), false, true);
	
	    if (isShowAdditionalPurdueData()) {
		    newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlCampusRegion(), false, true);
		    newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlLlrLalrPool(), false, true);
		    newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlClassroomSubtype(), false, true);
	    }
	    newline(sb, 4);
		appendSelectedField(sb, "z", MESSAGES.utilSqlEventType(), false, true);
	    newline(sb, 4);	    
		appendSelectedField(sb, "z", MESSAGES.utilSqlEventTypeDescription(), false, true);
	    newline(sb, 4);
	    sb.append(getCapacityRangeCase());
		if (includeDept) {
			newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlDepartment(), true, false);
		}
		if (includeSubjectArea) {
			newline(sb, 4);
			appendSelectedField(sb, "z", MESSAGES.utilSqlSubject(), true, false);
		}
	    
	    return(sb.toString());
	}
	
	public abstract String getPivotedQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, Integer saturday, String campus, String year, String term, ArrayList<Object> headerRow, boolean includeDayOfWkTimeOfDayInHeaderRow, boolean includeSubjectArea, boolean includeDept); 

	protected String getSortedRoomUtilizationQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, Integer saturday, String campus, String year, String term, ArrayList<Object> headerRow, boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer() ;
	    sb.append(getPivotedQuery(allDays, weekDays, saturday, campus, year, term, headerRow, true, includeSubjectArea, includeDept));
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
	    if (includeSubjectArea) {
		    sb.append(",");
		    newline(sb, 10);
		    sb.append(MESSAGES.utilSqlSubject());	    	
	    }
	    return(sb.toString());
	}

	public String getUnpivotedRoomUtilizationQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, Integer saturday, String campus, String year, String term, ArrayList<Object> headerRow, boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer() ;
		ArrayList<String> newColumns = new ArrayList<String>();
		ArrayList<String> dayTimeColumnPrefixes = new ArrayList<String>();
		for (LabelFieldPair labelFieldPair : getLabelPrefixToAdditionalQueryFieldMapping()) {
			newColumns.add(labelFieldPair.getLabel());
			dayTimeColumnPrefixes.add(labelFieldPair.getLabel());

		}
	    sb.append("select *");
	    newline(sb, 0);
	    sb.append("from");
	    newline(sb, 0);
	    sb.append("(");
	    sb.append("select *");
	    newline(sb, 0);
	    sb.append("from");
	    newline(sb, 0);
	    sb.append("(");
	    newline(sb, 0);
	    sb.append(getPivotedQuery (allDays, weekDays, saturday, campus, year, term, headerRow, false, includeSubjectArea, includeDept));
	    newline(sb, 0);
	    sb.append(") y");
	    newline(sb, 0);
	    sb.append(getUnPivot(allDays, newColumns, dayTimeColumnPrefixes));
	    newline(sb, 0);
	    sb.append(") x");
	    boolean first = true;
	    for (String column : newColumns) {
	    	if (first) {
			    newline(sb, 0);
			    sb.append("where ");
			    first = false;
	    	} else {
			    newline(sb, 3);
			    sb.append("or ");	    		
	    	}
	    sb.append("x.");
	    sb.append(column);
	    sb.append("!= 0");
	    }
	    return(sb.toString());
	}

	protected String getSortedUnpivotedRoomUtilizationQuery(ArrayList<Integer> allDays, ArrayList<Integer> weekDays, Integer saturday, String campus, String year, String term, ArrayList<Object> headerRow, boolean includeSubjectArea, boolean includeDept) {
		StringBuffer sb = new StringBuffer() ;
	    sb.append(getUnpivotedRoomUtilizationQuery(allDays, weekDays, saturday, campus, year, term, headerRow, includeSubjectArea, includeDept));
	    newline(sb, 0);
	    sb.append("order by ")
	      .append(MESSAGES.utilSqlAcademicInitiative())
	      .append(", ")
	      .append(MESSAGES.utilSqlAcademicTerm())
          .append( ", ")
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
  	    if (includeSubjectArea) {
  		    sb.append(",");
  		    newline(sb, 10);
  		    sb.append(MESSAGES.utilSqlSubject());	    	
  	    }
	    sb.append(",");
	    newline(sb, 10);
	    sb.append(MESSAGES.utilSqlDayTime());	    	
	  	return(sb.toString());
	}

	public String getPivotedAndUnpivotedUtilizationQueries(Session acadSession, boolean includeSubjectArea, boolean includeDept) {
	    
		StringBuffer sb = new StringBuffer() ;
		ArrayList<Object> headerRow1 = new ArrayList<Object>();
		ArrayList<Object> headerRow2 = new ArrayList<Object>();
	    String utilQuery = getSortedRoomUtilizationQuery(getAllDays(), getWeekDays(), getSaturday(), acadSession.getAcademicInitiative(), acadSession.getAcademicYear(), acadSession.getAcademicTerm(), headerRow1, includeSubjectArea, includeDept);
	    sb.append(utilQuery);
	    sb.append(";");
	    newline(sb, 0);
	    newline(sb, 0);
	    String unpivotedUtilQuery = getSortedUnpivotedRoomUtilizationQuery(getAllDays(), getWeekDays(), getSaturday(), acadSession.getAcademicInitiative(), acadSession.getAcademicYear(), acadSession.getAcademicTerm(), headerRow2, includeSubjectArea, includeDept);
	    sb.append(unpivotedUtilQuery);
	    sb.append(";");
	    newline(sb, 0);
	    return sb.toString();
	}

	public List<List<Object>> getQueryResultsForSortedRoomUtilizationQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept){
		ArrayList<Object> headerRow = new ArrayList<Object>();
		String query = getSortedRoomUtilizationQuery(getAllDays(), getWeekDays(), getSaturday(), acadSession.getAcademicInitiative(), acadSession.getAcademicYear(), acadSession.getAcademicTerm(), headerRow, includeSubjectArea, includeDept);
		 
		return getUtilQueryResultsForQuery(query, headerRow);
	}

	public List<List<Object>> getQueryResultsForSortedUnPivotedRoomUtilizationQuery(Session acadSession, boolean includeSubjectArea, boolean includeDept){
		ArrayList<Object> headerRow = new ArrayList<Object>();
		String query = getSortedUnpivotedRoomUtilizationQuery(getAllDays(), getWeekDays(), getSaturday(), acadSession.getAcademicInitiative(), acadSession.getAcademicYear(), acadSession.getAcademicTerm(), headerRow, includeSubjectArea, includeDept);
 
		return getUtilQueryResultsForQuery(query, headerRow);
	}
	
	@SuppressWarnings("unchecked")
	public List<List<Object>> getUtilQueryResultsForQuery(String query, ArrayList<Object> headerRow) {
		ArrayList<List<Object>> headerPlusResults = new ArrayList<List<Object>>();
		headerPlusResults.add(headerRow);
		RoomDAO rdao = new RoomDAO();
		org.hibernate.Session hibSession = rdao.getSession();
		headerPlusResults.addAll((List<List<Object>>) hibSession.createSQLQuery(query).list());
		return headerPlusResults;
	
	}
	
	protected String getDeptSubquery() {
		StringBuffer sb = new StringBuffer();
		
		return sb.toString();
	}
	
	
	protected class LabelFieldPair {
		public LabelFieldPair(String label, String field) {
			super();
			this.label = label;
			this.field = field;
		}
		String label;
		String field;

		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getField() {
			return field;
		}
		public void setField(String field) {
			this.field = field;
		}		
		
	}
	
}
