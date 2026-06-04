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
package org.unitime.timetable.server.exams;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridFilterRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.ListItem;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.exams.ExamGridContext.Background;
import org.unitime.timetable.server.exams.ExamGridContext.DispMode;
import org.unitime.timetable.server.exams.ExamGridContext.OrderBy;
import org.unitime.timetable.server.exams.ExamGridContext.Resource;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(ExamGridFilterRequest.class)
public class ExamGridFilterBackend implements GwtRpcImplementation<ExamGridFilterRequest, ClassesFilterResponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ClassesFilterResponse execute(ExamGridFilterRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationTimetable);
		ClassesFilterResponse filter = new ClassesFilterResponse();
		
		FilterParameterInterface showSections = new FilterParameterInterface();
		showSections.setName("showSections");
		showSections.setLabel(MESSAGES.filterShowClassesCourses());
		showSections.setType("boolean");
		showSections.setDefaultValue(context.getUser().getProperty("ExamGrid.showSections", "1"));
		filter.addParameter(showSections);
		
		FilterParameterInterface examType = new FilterParameterInterface();
		examType.setName("examType");
		examType.setType("list");
		examType.setMultiSelect(false);
		examType.setLabel(MESSAGES.propExamType());
        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable))
        	examType.addOption(type.getUniqueId().toString(), type.getLabel());
        Object et = context.getAttribute(SessionAttribute.ExamType);
        if (request.hasFilter()) {
        	String t = request.getFilter().getParameterValue("examType");
        	if (t != null && !t.isEmpty()) et = Long.valueOf(t);
        }
        examType.setDefaultValue(et == null ? null : et.toString());
        if (!examType.hasDefaultValue() && examType.hasOptions()) {
        	examType.setDefaultValue(examType.getOptions().get(0).getValue());
        	et = Long.valueOf(examType.getOptions().get(0).getValue());
        }
		filter.addParameter(examType);
		
		FilterParameterInterface resource = new FilterParameterInterface();
		resource.setName("resource");
		resource.setType("list");
		resource.setMultiSelect(false);
		resource.setLabel(MESSAGES.propertyResource());
		for (Resource r: Resource.values())
			resource.addOption(String.valueOf(r.ordinal()), r.getLabel());
		resource.setDefaultValue(context.getUser().getProperty("ExamGrid.resource", String.valueOf(Resource.Room.ordinal())));
		filter.addParameter(resource);
		
		FilterParameterInterface textFilter = new FilterParameterInterface();
		textFilter.setName("filter");
		textFilter.setType("text");
		textFilter.setLabel(MESSAGES.filterTextFilter());
		textFilter.setDefaultValue(context.getUser().getProperty("ExamGrid.filter"));
		filter.addParameter(textFilter);
		
		String res = resource.getDefaultValue();
        if (request.hasFilter()) {
        	res = request.getFilter().getParameterValue("resource");
        	resource.setDefaultValue(res);
        }
        if (String.valueOf(Resource.Room.ordinal()).equals(res)) {
    		FilterParameterInterface roomFilter = new FilterParameterInterface();
    		roomFilter.setName("roomFilter");
    		roomFilter.setLabel(GWT.propRoomFilter());
    		roomFilter.setType("text");
    		roomFilter.setDefaultValue(context.getUser().getProperty("ExamGrid.roomFilter", ""));
    		roomFilter.setSessionId(context.getUser().getCurrentAcademicSessionId());
    		filter.addParameter(roomFilter);
        }

		FilterParameterInterface date = new FilterParameterInterface();
		date.setName("date");
		date.setType("list");
		date.setMultiSelect(false);
		date.setLabel(MESSAGES.propertyPeriodDate());
		date.addOption("", MESSAGES.itemAllDates());
		filter.addParameter(date);
		
		FilterParameterInterface start = new FilterParameterInterface();
		start.setName("start");
		start.setType("list");
		start.setMultiSelect(false);
		start.setLabel(MESSAGES.propertyPeriodTime());
		start.setComposite(true);
		start.setPrefix(GWT.propFrom());
		filter.addParameter(start);
		
		FilterParameterInterface end = new FilterParameterInterface();
		end.setName("end");
		end.setType("list");
		end.setMultiSelect(false);
		end.setLabel(MESSAGES.propertyPeriodTime());
		end.setComposite(true);
		end.setDefaultValue(context.getUser().getProperty("ExamGrid.end", ""));
		end.setPrefix(GWT.propTo());
		filter.addParameter(end);
		
		if (et != null) {
			ExamType x = ExamTypeDAO.getInstance().get((Long)et);
			TreeSet<Integer> allDates = new TreeSet<Integer>();
			TreeSet<Integer> allStarts = new TreeSet<Integer>();
			TreeSet<Integer> allEnds = new TreeSet<Integer>();
			Calendar c = Calendar.getInstance(Localization.getJavaLocale());
			Session session = null;
			if (x != null) {
				for (ExamPeriod period: ExamPeriod.findAll(context.getUser().getCurrentAcademicSessionId(), x)) {
					if (session == null) session = period.getSession();
					allDates.add(period.getDateOffset());
					allStarts.add(period.getStartSlot());
					allEnds.add(period.getEndSlot());
				}
			}
			Map<Integer, Integer[]> weeks = new HashMap<Integer, Integer[]>();
			for (Integer d: allDates) {
		        c.setTime(session.getExamBeginDate());
		        c.add(Calendar.DAY_OF_YEAR, d);
		        int week = c.get(Calendar.WEEK_OF_YEAR);
		        Integer[] firstLast = weeks.get(week);
		        if (firstLast == null) {
		        	weeks.put(week, new Integer[] {d, d});
		        } else {
		        	weeks.put(week, new Integer[] {Math.min(firstLast[0], d), Math.max(firstLast[1], d)});
		        }
			}
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
			Formats.Format<Date> dfShort = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
			Integer prev = null;
			for (Integer d: allDates) {
		        c.setTime(session.getExamBeginDate());
		        c.add(Calendar.DAY_OF_YEAR, d);
		        int week = c.get(Calendar.WEEK_OF_YEAR);
		        if (prev == null || prev != week) {
		        	prev = week;
		        	Integer[] firstLast = weeks.get(week);
		        	if (firstLast != null && firstLast[0] < firstLast[1]) {
		        		while (week == c.get(Calendar.WEEK_OF_YEAR)) {
		        			c.add(Calendar.DAY_OF_YEAR, -1);
		        		}
		        		c.add(Calendar.DAY_OF_YEAR, +1);
		        		Date d1 = c.getTime();
		        		c.add(Calendar.DAY_OF_YEAR, +6);
		        		Date d2 = c.getTime();
		        		date.addOption(firstLast[0] + "|" + firstLast[1], MESSAGES.week(week) + " (" + dfShort.format(d1) + " - " + dfShort.format(d2) + ")");
				        c.setTime(session.getExamBeginDate());
				        c.add(Calendar.DAY_OF_YEAR, d);
		        	}
		        }
		        date.addOption(String.valueOf(d), df.format(c.getTime()));
			}
			for (Integer t: allStarts)
				start.addOption(String.valueOf(t), Constants.slot2str(t));
			for (Integer t: allEnds)
				end.addOption(String.valueOf(t), Constants.slot2str(t));
			String defStart = context.getUser().getProperty("ExamGrid.start");
			if (start.hasOption(defStart)) {
				start.setDefaultValue(defStart);
			} else if (!allStarts.isEmpty()) {
				start.setDefaultValue(String.valueOf(allStarts.first()));
			} else {
				start.addOption("", "");
				start.setDefaultValue("");
			}
			String defEnd = context.getUser().getProperty("ExamGrid.end");
			if (end.hasOption(defEnd)) {
				end.setDefaultValue(defEnd);
			} else if (!allEnds.isEmpty()) {
				end.setDefaultValue(String.valueOf(allEnds.last()));
			} else {
				end.addOption("", "");
				end.setDefaultValue("");
			}
		} else {
			start.addOption("", "");
			start.setDefaultValue("");
			end.addOption("", "");
			end.setDefaultValue("");
		}
		date.setDefaultValue(context.getUser().getProperty("ExamGrid.date", ""));
		
		FilterParameterInterface dispMode = new FilterParameterInterface();
		dispMode.setName("dispMode");
		dispMode.setType("list");
		dispMode.setMultiSelect(false);
		dispMode.setLabel(MESSAGES.propDisplayMode());
		for (DispMode r: DispMode.values())
			dispMode.addOption(String.valueOf(r.ordinal()), r.getLabel());
		dispMode.setDefaultValue(context.getUser().getProperty("ExamGrid.dispMode", String.valueOf(DispMode.PerWeekVertical.ordinal())));
		filter.addParameter(dispMode);
		
		FilterParameterInterface background = new FilterParameterInterface();
		background.setName("background");
		background.setType("list");
		background.setMultiSelect(false);
		background.setLabel(MESSAGES.propBackground());
		for (Background r: Background.values())
			background.addOption(String.valueOf(r.ordinal()), r.getLabel());
		background.setDefaultValue(context.getUser().getProperty("ExamGrid.background", String.valueOf(Background.None.ordinal())));
		filter.addParameter(background);
		
		FilterParameterInterface bgPreferences = new FilterParameterInterface();
		bgPreferences.setName("bgPreferences");
		bgPreferences.setLabel(MESSAGES.propShowPeriodPreferences());
		bgPreferences.setType("boolean");
		bgPreferences.setDefaultValue(context.getUser().getProperty("ExamGrid.bgPreferences", "0"));
		filter.addParameter(bgPreferences);
		
		FilterParameterInterface showConflicts = new FilterParameterInterface();
		showConflicts.setName("studentConf");
		showConflicts.setLabel(MESSAGES.propShowStudentConflicts());
		showConflicts.setType("boolean");
		showConflicts.setDefaultValue(context.getUser().getProperty("ExamGrid.studentConf", "1"));
		filter.addParameter(showConflicts);
		
		FilterParameterInterface order = new FilterParameterInterface();
		order.setName("order");
		order.setType("list");
		order.setMultiSelect(false);
		order.setLabel(MESSAGES.propOrderBy());
		for (OrderBy r: OrderBy.values())
			order.addOption(String.valueOf(r.ordinal()), r.getLabel());
		order.setDefaultValue(context.getUser().getProperty("ExamGrid.order", String.valueOf(OrderBy.NameAsc.ordinal())));
		filter.addParameter(order);
		
		if (request.hasFilter()) {
			for (FilterParameterInterface old: request.getFilter().getParameters()) {
				FilterParameterInterface p = filter.getParameter(old.getName());
				if (p != null) {
					if (p.hasOptions() && old.hasValue()) {
						for (ListItem item: p.getOptions()) {
							if (old.getValue().equals(item.getValue())) {
								p.setValue(old.getValue());
								break;
							}
						}
					} else {
						p.setValue(old.getValue());
					}
				}
			}
		}
		
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		filter.setCanExport(true);
		if (request.getFilter() != null) {
			for (FilterParameterInterface p: request.getFilter().getParameters()) {
				if (p.getValue() == null) continue;
				FilterParameterInterface q = filter.getParameter(p.getName());
				if (q == null) continue;
				if (q.hasOptions() && !q.hasOption(p.getValue())) continue;
				q.setDefaultValue(p.getValue());
			}
		}
		
		return filter;
	}
}
