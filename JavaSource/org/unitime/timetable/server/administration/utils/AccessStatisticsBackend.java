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
package org.unitime.timetable.server.administration.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.AccessStatisticsPage.AccessStatisticsRequest;
import org.unitime.timetable.gwt.client.admin.AccessStatisticsPage.AccessStatisticsResponse;
import org.unitime.timetable.gwt.client.admin.AccessStatisticsPage.Interval;
import org.unitime.timetable.gwt.client.admin.AccessStatisticsPage.Page;
import org.unitime.timetable.gwt.client.admin.AccessStatisticsPage.Type;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.AccessStatistics;
import org.unitime.timetable.model.dao.AccessStatisticsDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(AccessStatisticsRequest.class)
public class AccessStatisticsBackend implements GwtRpcImplementation<AccessStatisticsRequest, AccessStatisticsResponse>{
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT_MSG = Localization.create(GwtMessages.class);

	@Override
	public AccessStatisticsResponse execute(AccessStatisticsRequest request, SessionContext context) {
		context.checkPermission(Right.AccessStatistics);
		AccessStatisticsResponse response = new AccessStatisticsResponse();
		
		Date iFrom, iTo = null;
		if (request.getInterval() == Interval.CUSTOM) {
			iFrom = request.getFromDate();
			iTo = request.getToDate();
		} else {
			iFrom = getSince(request.getInterval());
		}
		
		List<String> hosts = AccessStatisticsDAO.getInstance().getSession().createQuery(
				"select host from AccessStatistics where access > 0 and page = :page " +
				"and timeStamp > :since " + 
				"group by host order by host", String.class)
				.setParameter("page", request.getPage().name())
				.setParameter("since", iFrom)
				.setCacheable(true).list();
		
		for (String host: hosts) {
			List<AccessStatistics> data = AccessStatisticsDAO.getInstance().getSession().createQuery(
					"from AccessStatistics where access > 0 and host = :host and page = :page and " +
					"timeStamp >= :from and (:to is null or timeStamp <= :to) " +
					"order by timeStamp", AccessStatistics.class)
					.setParameter("host", host)
					.setParameter("page", request.getPage().name())
					.setParameter("from", iFrom)
					.setParameter("to", iTo)
					.list();
			if (data == null || data.isEmpty()) continue;
			int mod = 1;
			if (data.size() > 1000)
				mod = data.size() / 1000;
			switch(request.getType()) {
			case ACTIVE:
				response.addChart(getChartLabel(request, host), getVisualisationDataActive(data, mod));
				break;
			case BASIC:
				response.addChart(getChartLabel(request, host), getVisualisationDataBasic(data, mod));
				break;
			case TIME:
				response.addChart(getChartLabel(request, host), getVisualisationDataTimes(data, mod));
				break;
			}
		}
		
		return response;
	}
	
	public Date getSince(Interval interval) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		switch (interval) {
		case LAST_3HOURS: c.add(Calendar.HOUR_OF_DAY, -3); break;
		case LAST_DAY: c.add(Calendar.DAY_OF_YEAR, -1); break;
		case LAST_HOUR: c.add(Calendar.HOUR_OF_DAY, -1); break;
		case LAST_MONTH: c.add(Calendar.MONTH, -1); break;
		case LAST_WEEK: c.add(Calendar.DAY_OF_YEAR, -7); break;
		default:
		}
		return c.getTime();
	}
	
	protected String getLabel(Page page) {
		switch(page) {
		case requests: return GWT_MSG.pageStudentCourseRequests();
		case sectioning: return GWT_MSG.pageStudentSchedulingAssistant();
		default: return page.name();
		}
	}
	
	protected String getLabel(Type type) {
		switch(type) {
		case BASIC: return MSG.chartModeBasic();
		case ACTIVE: return MSG.chartModeActive();
		case TIME: return MSG.chartModeTimes();
		default: return type.name();
		}
	}
	
	public String getLabel(Interval interval) {
		switch(interval) {
		case LAST_DAY: return MSG.chartIntervalLastDay();
		case LAST_3HOURS: return MSG.chartIntervalLast3Hours();
		case LAST_HOUR: return MSG.chartIntervalLastHour();
		case LAST_WEEK: return MSG.chartIntervalLastWeek();
		case LAST_MONTH: return MSG.chartIntervalLastMonth();
		case CUSTOM: return MSG.chartIntervalCustom();
		default: return interval.name();
		}
	}

	public String getChartLabel(AccessStatisticsRequest request, String host) {
		if (request.getPage() == null || request.getType() == null) return null;
		if (request.getFromDate() != null) {
			Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
			if (request.getToDate() == null) {
				return getLabel(request.getPage()) + " " + getLabel(request.getType()) + " (" + dateFormat.format(request.getFromDate()) + " @ " + host + ")";
			} else {
				return getLabel(request.getPage()) + " " + getLabel(request.getType()) + " (" + dateFormat.format(request.getFromDate()) + " - " + dateFormat.format(request.getToDate()) + " @ " + host + ")";
			}
		} else if (request.getInterval() != null) {
			return getLabel(request.getPage()) + " " + getLabel(request.getType()) + " (" + getLabel(request.getInterval()) + " @ " + host + ")";
		} else {
			return null;
		}
	}
	
	public static String getVisualisationDataBasic(List<AccessStatistics> data, int mod) {
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
		Formats.Format<Number> numberFormat = Formats.getNumberFormat("0.##");
		double opened = 0;
		double access = 0;
		double active = 0;
		double waiting = 0;
		double gotIn = 0;
		double left = 0;
		double gaveUp = 0;
		int cnt = 0;
		long increment = 1000 * (60 * mod + 30);
		long first = -1, last = -1;
		List<DataLine> lines = new ArrayList<>();
		for (AccessStatistics stat: data) {
			if (cnt == mod || (cnt > 0 && first + increment < stat.getTimeStamp().getTime())) {
				lines.add(new DataLine(dateFormat.format(new Date(first)), opened / cnt, access / cnt, active / cnt, waiting / cnt, gotIn / cnt, left / cnt, gaveUp / cnt));
				cnt = 0; opened = 0; access = 0; active = 0; waiting = 0; gotIn = 0; left = 0; gaveUp = 0;
			}
			if (cnt == 0) first = stat.getTimeStamp().getTime();
			last = stat.getTimeStamp().getTime();
			cnt ++;
			opened += stat.getOpened();
			access += stat.getAccess();
			active += stat.getActive();
			waiting += stat.getWaiting();
			if (stat.getGotIn() != null)
				gotIn += stat.getGotIn();
			if (stat.getLeft() != null)
				left += stat.getLeft();
			if (stat.getGaveUp() != null)
				gaveUp += stat.getGaveUp();
		}
		if (cnt > 0)
			lines.add(new DataLine(dateFormat.format(new Date(last)), opened / cnt, access / cnt, active / cnt, waiting / cnt, gotIn / cnt, left / cnt, gaveUp / cnt));
		String ret = "[\n['" + MSG.chartBasicDate() +
				"', '" + MSG.chartBasicOpened() +
				"', '" + MSG.chartBasicAccess() + 
				"', '" + MSG.chartBasicActive() + 
				"', '" + MSG.chartBasicWaiting() + 
				"', '" + MSG.chartBasicGotIn() + 
				"', '" + MSG.chartBasicLeft() +
				"', '" + MSG.chartBasicGaveUp() + "']";
		for (DataLine line: lines)
			ret += ",\n" + line.toString(numberFormat);
		ret += "\n]";
		return ret;
	}
	
	public static String getVisualisationDataActive(List<AccessStatistics> data, int mod) {
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
		Formats.Format<Number> numberFormat = Formats.getNumberFormat("0.##");
		double active1 = 0;
		double active2 = 0;
		double active5 = 0;
		double active10 = 0;
		double active15 = 0;
		int cnt = 0;
		long increment = 1000 * (60 * cnt + 30);
		long first = -1, last = -1;
		List<DataLine> lines = new ArrayList<>();
		for (AccessStatistics stat: data) {
			if (cnt == mod || (cnt > 0 && first + increment < stat.getTimeStamp().getTime())) {
				lines.add(new DataLine(dateFormat.format(new Date(first)), active1 / cnt, active2 / cnt, active5 / cnt, active10 / cnt, active15 / cnt));
				cnt = 0; active1 = 0; active2 = 0; active5 = 0; active10 = 0; active15 = 0;
			}
			if (cnt == 0) first = stat.getTimeStamp().getTime();
			last = stat.getTimeStamp().getTime();
			cnt ++;
			active1 += stat.getActive1m();
			active2 += stat.getActive2m();
			active5 += stat.getActive5m();
			active10 += stat.getActive10m();
			active15 += stat.getActive15m();
		}
		if (cnt > 0)
			lines.add(new DataLine(dateFormat.format(new Date(last)), active1 / cnt, active2 / cnt, active5 / cnt, active10 / cnt, active15 / cnt));
		if (lines.isEmpty()) return null;
		String ret = "[\n['" + MSG.chartBasicDate() +
				"', '" + MSG.chartActive1m() +
				"', '" + MSG.chartActive2m() +
				"', '" + MSG.chartActive5m() +
				"', '" + MSG.chartActive10m() +
				"', '" + MSG.chartActive15m() + "']";
		for (DataLine line: lines)
			ret += ",\n" + line.toString(numberFormat);
		ret += "\n]";
		return ret;
	}
	
	public static String getVisualisationDataTimes(List<AccessStatistics> data, int mod) {
		Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
		Formats.Format<Number> numberFormat = Formats.getNumberFormat("0.##");
		double avgWait = 0; int wait = 0;
		double avgAcces = 0; int access = 0;
		double avgWaitGotIn = 0; int gotIn = 0;
		double avgAccesLeft = 0; int left = 0;
		int cnt = 0;
		long increment = 1000 * (60 * cnt + 30);
		long first = -1, last = -1;
		List<DataLine> lines = new ArrayList<>();
		for (AccessStatistics stat: data) {
			if (cnt == mod || (cnt > 0 && first + increment < stat.getTimeStamp().getTime())) {
				lines.add(new DataLine(dateFormat.format(new Date(last)),
						(access > 0 ? avgAcces / access : 0.0) / 60.0,
						(wait > 0 ? avgWait / wait: 0.0) / 60.0,
						(left > 0 ? avgAccesLeft / left : 0.0) / 60.0,
						(gotIn > 0 ? avgWaitGotIn / gotIn : 0.0) / 60.0));
				cnt = 0;
				avgWait = 0; wait = 0;
				avgAcces = 0; access = 0;
				avgWaitGotIn = 0; gotIn = 0;
				avgAccesLeft = 0; left = 0;
			}
			if (cnt == 0) first = stat.getTimeStamp().getTime();
			last = stat.getTimeStamp().getTime();
			cnt ++;
			if (stat.getAvgWaitTime() != null && stat.getWaiting() > 0) {
				avgWait += stat.getAvgWaitTime() * stat.getWaiting(); wait += stat.getWaiting();
			}
			if (stat.getAvgAccessTime() != null && stat.getAccess() > 0) {
				avgAcces += stat.getAvgAccessTime() * stat.getAccess(); access += stat.getAccess();
			}
			if (stat.getAvgAccessTimeWhenLeft() != null && stat.getLeft() > 0) {
				avgAccesLeft += stat.getAvgAccessTimeWhenLeft() * stat.getLeft(); left += stat.getLeft();
			}
			if (stat.getAvgWaitTimeWhenGotIn() != null && stat.getGotIn() > 0) {
				avgWaitGotIn += stat.getAvgWaitTimeWhenGotIn() * stat.getGotIn(); gotIn += stat.getGotIn();
			}
		}
		if (cnt > 0)
			lines.add(new DataLine(dateFormat.format(new Date(last)),
					(access > 0 ? avgAcces / access : 0.0) / 60.0,
					(wait > 0 ? avgWait / wait: 0.0) / 60.0,
					(left > 0 ? avgAccesLeft / left : 0.0) / 60.0,
					(gotIn > 0 ? avgWaitGotIn / gotIn : 0.0) / 60.0));
		if (lines.isEmpty()) return null;
		String ret = "[\n['" + MSG.chartBasicDate() +
				"', '" + MSG.chartTimesAccess() +
				"', '" + MSG.chartTimesWait() +
				"', '" + MSG.chartTimesAccessLeft() +
				"', '" + MSG.chartTimesWaitGotIn() + "']";
		for (DataLine line: lines)
			ret += ",\n" + line.toString(numberFormat);
		ret += "\n]";
		return ret;
	}

	private static class DataLine {
		String iName;
		double[] iData;
		
		private DataLine(String name, double... data) {
			iName = name;
			iData = data;
		}
		
		public String toString(Formats.Format<Number> format) {
			String ret = "['" + iName + "'";
			for (double d: iData)
				ret += "," + format.format(d);
			ret += "]";
			return ret;
		}
	}
}
