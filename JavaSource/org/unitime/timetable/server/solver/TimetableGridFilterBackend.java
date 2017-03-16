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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridFilterRequest;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridFilterResponse;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TimetableGridFilterRequest.class)
public class TimetableGridFilterBackend implements GwtRpcImplementation<TimetableGridFilterRequest, TimetableGridFilterResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public TimetableGridFilterResponse execute(TimetableGridFilterRequest request, SessionContext context) {
		context.checkPermission(Right.TimetableGrid);
		TimetableGridFilterResponse response = new TimetableGridFilterResponse();
		
		// Weeks
		FilterParameterInterface weeks = new FilterParameterInterface();
		weeks.setName("weeks");
		weeks.setLabel(MESSAGES.propTimeGridWeek());
		weeks.setType("list");
		weeks.setMultiSelect(false);
		weeks.addOption("-100", MESSAGES.weekAll());
        Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
        int offset = ApplicationProperty.TimeGridFirstDayOfWeek.intValue();
		int startWeek = DateUtils.getWeek(session.getSessionBeginDateTime()) - (ApplicationProperty.SessionNrExcessDays.intValue() + offset + 6) / 7;
		Calendar endCal = Calendar.getInstance(Locale.US);
		endCal.setTime(session.getSessionEndDateTime());
		endCal.add(Calendar.DAY_OF_YEAR, ApplicationProperty.SessionNrExcessDays.intValue());
		int week = startWeek;
		Formats.Format<Date> dfShort = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
		Formats.Format<Date> dfLong = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG);
		while (DateUtils.getStartDate(session.getSessionStartYear(), week, offset).compareTo(endCal.getTime()) <= 0) {
			weeks.addOption(String.valueOf(week), dfShort.format(DateUtils.getStartDate(session.getSessionStartYear(), week, offset)) + " - " + dfLong.format(DateUtils.getEndDate(session.getSessionStartYear(), week, offset)));
			week++;
		}
		int lastWeek = Integer.parseInt(context.getUser().getProperty("TimetableGridTable.week", "-100")); 
		if (lastWeek < startWeek || lastWeek >= week) lastWeek = -100;
		weeks.setDefaultValue(String.valueOf(lastWeek));
		response.addParameter(weeks);
		
		// Resource
		FilterParameterInterface resource = new FilterParameterInterface();
		resource.setName("resource");
		resource.setLabel(MESSAGES.propTimeGridResource());
		resource.setType("list");
		resource.setMultiSelect(false);
		for (int i = 0; i < CONSTANTS.timeGridResource().length; i++)
			resource.addOption(String.valueOf(i), CONSTANTS.timeGridResource()[i]);
		resource.setDefaultValue(context.getUser().getProperty("TimetableGridTable.resourceType", "0"));
		response.addParameter(resource);
		
		// Filter
		FilterParameterInterface filter = new FilterParameterInterface();
		filter.setName("filter");
		filter.setLabel(MESSAGES.propTimeGridFilter());
		filter.setType("text");
		filter.setDefaultValue(context.getUser().getProperty("TimetableGridTable.findString", ""));
		response.addParameter(filter);
		
		// Days
		FilterParameterInterface days = new FilterParameterInterface();
		days.setName("days");
		days.setLabel(MESSAGES.propTimeGridDays());
		days.setType("list");
		days.setMultiSelect(false);
		for (int i = 0; true; i++) {
			String mode = ApplicationProperty.TimeGridDays.value(String.valueOf(1 + i), i < CONSTANTS.timeGridDays().length ? CONSTANTS.timeGridDays()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			String bitmap = mode.split("\\|")[0];
			days.addOption(bitmap, mode.substring(bitmap.length() + 1));
		}
		days.setDefaultValue(context.getUser().getProperty("TimetableGridTable.day", "1111100"));
		response.addParameter(days);
		
		// Times
		FilterParameterInterface times = new FilterParameterInterface();
		times.setName("times");
		times.setLabel(MESSAGES.propTimeGridTimes());
		times.setType("list");
		times.setMultiSelect(false);
		for (int i = 0; true; i++) {
			String mode = ApplicationProperty.TimeGridTimes.value(String.valueOf(1 + i), i < CONSTANTS.timeGridTimes().length ? CONSTANTS.timeGridTimes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			String name = mode.split("\\|")[0];
			times.addOption(mode.substring(name.length() + 1), name);
		}
		times.setDefaultValue(context.getUser().getProperty("TimetableGridTable.times", times.hasOptions() ? times.getOptions().get(0).getValue() : null));
		response.addParameter(times);
		
		// Display Mode
		FilterParameterInterface dispMode = new FilterParameterInterface();
		dispMode.setName("dispMode");
		dispMode.setLabel(MESSAGES.propTimeGridDisplayMode());
		dispMode.setType("list");
		dispMode.setMultiSelect(false);
		for (int i = 0; i < CONSTANTS.timeGridDisplayMode().length; i++)
			dispMode.addOption(String.valueOf(i), CONSTANTS.timeGridDisplayMode()[i]);
		dispMode.setDefaultValue(context.getUser().getProperty("TimetableGridTable.dispMode", "0"));
		response.addParameter(dispMode);
		
		// Background
		FilterParameterInterface background = new FilterParameterInterface();
		background.setName("background");
		background.setLabel(MESSAGES.propTimeGridBackground());
		background.setType("list");
		background.setMultiSelect(false);
		for (int i = 0; i < CONSTANTS.timeGridBackground().length; i++)
			background.addOption(String.valueOf(i), CONSTANTS.timeGridBackground()[i]);
		background.setDefaultValue(context.getUser().getProperty("TimetableGridTable.bgMode", "0"));
		response.addParameter(background);
		
		// Flags
		FilterParameterInterface showFreeTimes = new FilterParameterInterface();
		showFreeTimes.setName("showFreeTimes");
		showFreeTimes.setType("boolean");
		showFreeTimes.setLabel(MESSAGES.propTimeGridShowFreeTimes());
		showFreeTimes.setDefaultValue(context.getUser().getProperty("TimetableGridTable.showUselessTimes", "0"));
		response.addParameter(showFreeTimes);
		
		FilterParameterInterface showPreferences = new FilterParameterInterface();
		showPreferences.setName("showPreferences");
		showPreferences.setType("boolean");
		showPreferences.setLabel(MESSAGES.propTimeGridShowPreferences());
		showPreferences.setDefaultValue(context.getUser().getProperty("TimetableGridTable.showComments", "0"));
		response.addParameter(showPreferences);
		
		FilterParameterInterface showInstructors = new FilterParameterInterface();
		showInstructors.setName("showInstructors");
		showInstructors.setType("boolean");
		showInstructors.setLabel(MESSAGES.propTimeGridShowInstructors());
		showInstructors.setDefaultValue(context.getUser().getProperty("TimetableGridTable.showInstructors", "0"));
		response.addParameter(showInstructors);
		
		FilterParameterInterface showEvents = new FilterParameterInterface();
		showEvents.setName("showEvents");
		showEvents.setType("boolean");
		showEvents.setLabel(MESSAGES.propTimeGridShowEvents());
		showEvents.setDefaultValue(context.getUser().getProperty("TimetableGridTable.showEvents", "0"));
		response.addParameter(showEvents);
		
		FilterParameterInterface showTimes = new FilterParameterInterface();
		showTimes.setName("showTimes");
		showTimes.setType("boolean");
		showTimes.setLabel(MESSAGES.propTimeGridShowTimes());
		showTimes.setDefaultValue(context.getUser().getProperty("TimetableGridTable.showTimes", "0"));
		response.addParameter(showTimes);

		FilterParameterInterface orderBy = new FilterParameterInterface();
		orderBy.setName("orderBy");
		orderBy.setType("list");
		orderBy.setMultiSelect(false);
		for (int i = 0; i < CONSTANTS.timeGridOrderBy().length; i++)
			orderBy.addOption(String.valueOf(i), CONSTANTS.timeGridOrderBy()[i]);
		orderBy.setLabel(MESSAGES.propTimeGridOrderBy());
		orderBy.setDefaultValue(context.getUser().getProperty("TimetableGridTable.orderBy", "0"));
		response.addParameter(orderBy);
		
		return response;
	}

}
