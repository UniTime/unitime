package org.unitime.timetable.util;

import java.util.ArrayList;

public class ICHERoomUtilizationHelper extends RoomUtilizationHelper {

	public ICHERoomUtilizationHelper(int iMinutesInPeriod, int iPeriodsInHour, int iAllDayStartPeriod,
			int iAllDayEndPeriod, int iStandardDayStartPeriod, int iStandardDayEndPeriod, int iWeekendDayStartPeriod,
			int iWeekendDayEndPeriod, int iFirstFullHourStartPeriod, int iLastFullHourStopPeriod, String iSchema) {
		super(iMinutesInPeriod, iPeriodsInHour, iAllDayStartPeriod, iAllDayEndPeriod, iStandardDayStartPeriod,
				iStandardDayEndPeriod, iWeekendDayStartPeriod, iWeekendDayEndPeriod, iFirstFullHourStartPeriod,
				iLastFullHourStopPeriod, iSchema);
	}

	public ICHERoomUtilizationHelper() {
		super();

		setStandardDayStartPeriod(96);
		setStandardDayEndPeriod(216);
		setWeekendDayStartPeriod(96);
		setWeekendDayEndPeriod(168);
		setFirstFullHourStartPeriod(12);
		setLastFullHourStopPeriod(276);	
	}
	
	public ICHERoomUtilizationHelper(String schema) {
		// TODO Auto-generated constructor stub
		super();

		setStandardDayStartPeriod(96);
		setStandardDayEndPeriod(216);
		setWeekendDayStartPeriod(96);
		setWeekendDayEndPeriod(168);
		setFirstFullHourStartPeriod(12);
		setLastFullHourStopPeriod(276);	

		setSchema(schema);
	}
	
	@Override
	protected String getTimeRestriction(String first5MinutePeriod, String last5MinutePeriod) {
	    String first5 = first5MinutePeriod;
	    String last5 = Integer.toString((Integer.parseInt(first5)) + 1);
		StringBuffer timeRestriction = new StringBuffer();
	    timeRestriction.append("z.start_period < ");
	    timeRestriction.append(last5);
	    timeRestriction.append(" and z.stop_period > ");
	    timeRestriction.append(first5);
	    return(timeRestriction.toString());
	}
	
	@Override
	protected String getSummaryCalculationForPeriod(String first5MinutePeriod, String last5MinutePeriod, String additionalQueryField) {
		StringBuffer utilCal = new StringBuffer();
	    String first5 = first5MinutePeriod;
	    String last5 = Integer.toString((Integer.parseInt(first5)) + 1);
	    if (additionalQueryField != null) {
	    	utilCal.append("(");
	    }
		utilCal.append("((((case when z.stop_period > ");
	    utilCal.append(last5);
	    utilCal.append(" then ");
	    utilCal.append(last5);
	    utilCal.append(" else ");
	    utilCal.append(last5);
	    utilCal.append(" end) - (case when z.start_period < ");
	    utilCal.append(first5);
	    utilCal.append(" then ");
	    utilCal.append(first5);
	    utilCal.append(" else ");
	    utilCal.append(first5);
	    utilCal.append(" end)) * ");
	    utilCal.append(getMinutesInPeriod());
	    utilCal.append(")  * 1/z.weeks_divisor_for_day_of_week/5.0)");
	    if (additionalQueryField != null) {
	    	utilCal.append(" * ");
	    	utilCal.append(additionalQueryField);
	    	utilCal.append(")");
	    }
	    return(utilCal.toString());
	}
	
	@Override
	protected void buildStandardWeekdayHoursSum(StringBuffer stringBuffer, ArrayList<Integer> weekdays, boolean leadingComma, ArrayList<Object> headerRow, LabelFieldPair labelFieldPair) {
		// skip the standard weekday hours sum
	}

	@Override
	protected void buildStandardHoursSum(StringBuffer stringBuffer, ArrayList<Integer> weekdays, Integer saturday, boolean leadingComma, ArrayList<Object> headerRow, LabelFieldPair labelFieldPair) {
	    // skip the standard hours sum
	}

	@Override
	protected void buildAllHoursSum(StringBuffer sb, ArrayList<Integer> alldays, boolean leadingComma, ArrayList<Object> headerRow, LabelFieldPair labelFieldPair) {
	    // skip the all hours sum
	}

	@Override
	protected void buildWeekdayStandardHoursSummarySum(StringBuffer sb, boolean leadingComma, String labelPrefix) {
		// skip the standard weekday hours summary sum
	}
	
	@Override
	protected void buildStandardHoursSummarySum(StringBuffer sb, boolean leadingComma, String labelPrefix) {
	    // skip the standard hours summary sum
	}

	@Override
	protected void buildAllHoursSummarySum(StringBuffer sb, boolean leadingComma, String labelPrefix) {
	    // skip the all hours sum
	}

	
}
