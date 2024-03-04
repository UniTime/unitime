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
package org.unitime.timetable.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.AccessStatistics;
import org.unitime.timetable.model.dao.AccessStatisticsDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.IdValue;

/** 
 * @author Tomas Muller
 */
@Action(value = "accessStats", results = {
		@Result(name = "show", type = "tiles", location = "stats.tiles")
	})
@TilesDefinition(name = "accessStats.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Access Statistics"),
		@TilesPutAttribute(name = "body", value = "/admin/accessStats.jsp")
	})
public class AccessStatsAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = 1827478747175650862L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT_MSG = Localization.create(GwtMessages.class);
	
	private Page iPage;
	private Type iType;
	private Interval iInterval;
	private DateFormat sDF = new SimpleDateFormat("yyyyMMdd-HHmm");
	private Date iFrom, iTo;
	
	private String iFromDate, iToDate;
	private Integer iFromSlot, iToSlot;
	
	public String getPage() {
		return iPage == null ? null : iPage.getId();
	}

	public void setPage(String page) {
		if (page == null || page.isEmpty())
			iPage = null;
		else
			iPage = Page.valueOf(page);
	}
	
	public String getType() {
		return (iType == null ? null : iType.getId());
	}
	
	public void setType(String type) {
		if (type == null || type.isEmpty())
			iType = null;
		else
			iType = Type.valueOf(type);
	}
	
	public String getInterval() {
		if (iFrom != null) return Interval.CUSTOM.getId();
		return (iInterval == null ? null : iInterval.getId());
	}
	
	public void setInterval(String interval) {
		if (interval == null || interval.isEmpty()) {
			iInterval = null;
		} else {
			iInterval = Interval.valueOf(interval);
		}
	}
	
	public String getFrom() {return iFrom == null ? null : sDF.format(iFrom); }
	public void setFrom(String date) {
		if (date == null || date.isEmpty())
			iFrom = null;
		else {
			try {
				iFrom = sDF.parse(date);
			} catch (ParseException e) {
				iFrom = null;
			}
		}
	}
	public void setFromDate(String date) { iFromDate = date; }
	public String getFromDate() {
		if (iFrom != null) {
			return Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).format(iFrom);
		} else {
			return iFromDate;
		}
	}
	public void setFromSlot(Integer slot) { iFromSlot = slot; }
	public Integer getFromSlot() {
		if (iFrom != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(iFrom);
			return c.get(Calendar.HOUR_OF_DAY) * 12 + c.get(Calendar.MINUTE) / 5;
		} else {
			return iFromSlot;
		}
	}
	
	public String getTo() { return iTo == null ? null : sDF.format(iTo); }
	public void setTo(String date) {
		if (date == null || date.isEmpty())
			iTo = null;
		else {
			try {
				iTo = sDF.parse(date);
			} catch (ParseException e) {
				iTo = null;
			}
		}
	}
	public void setToDate(String date) { iToDate = date; }
	public String getToDate() {
		if (iTo != null) {
			return Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).format(iTo);
		} else {
			return iToDate;
		}
	}
	public void setToSlot(Integer slot) { iToSlot = slot; }
	public Integer getToSlot() {
		if (iTo != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(iTo);
			return c.get(Calendar.HOUR_OF_DAY) * 12 + c.get(Calendar.MINUTE) / 5;
		} else {
			return iToSlot;
		}
	}
	
	public Date getFromDateTime() {
		if (iFrom != null) return iFrom;
		if (iFromDate == null || iFromDate.isEmpty()) return null;
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).parse(iFromDate));
		} catch (ParseException e) {
			return null;
		}
		if (iFromSlot != null) {
			c.set(Calendar.HOUR_OF_DAY, iFromSlot / 12);
			c.set(Calendar.MINUTE, 5 * (iFromSlot % 12));
		}
		return c.getTime();
	}
	
	public Date getToDateTime() {
		if (iTo != null) return iTo;
		if (iToDate == null || iToDate.isEmpty()) return null;
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).parse(iToDate));
		} catch (ParseException e) {
			return null;
		}
		if (iToSlot != null) {
			c.set(Calendar.HOUR_OF_DAY, iToSlot / 12);
			c.set(Calendar.MINUTE, 5 * (iToSlot % 12));
		} else
			c.add(Calendar.DAY_OF_YEAR, 1);
		return c.getTime();
	}
	
	@Override
	public String execute() {
		sessionContext.checkPermission(Right.AccessStatistics);
		return "show";
	}
	
	public List<String> getHosts() {
		if (iPage == null || iType == null) return null;
		if (iFrom != null) {
			return AccessStatisticsDAO.getInstance().getSession().createQuery(
					"select host from AccessStatistics where access > 0 and page = :page " +
					"and timeStamp > :since " + 
					"group by host order by host", String.class)
					.setParameter("page", iPage.name())
					.setParameter("since", iFrom)
					.setCacheable(true).list();
		} else if (iInterval == Interval.CUSTOM) {
			Date since = getFromDateTime();
			if (since == null) return null;
			return AccessStatisticsDAO.getInstance().getSession().createQuery(
					"select host from AccessStatistics where access > 0 and page = :page " +
					"and timeStamp > :since " + 
					"group by host order by host", String.class)
					.setParameter("page", iPage.name())
					.setParameter("since", since)
					.setCacheable(true).list();
		} else if (iInterval != null) {
			return AccessStatisticsDAO.getInstance().getSession().createQuery(
					"select host from AccessStatistics where access > 0 and page = :page " +
					"and timeStamp > :since " + 
					"group by host order by host", String.class)
					.setParameter("page", iPage.name())
					.setParameter("since", iInterval.getSince())
					.setCacheable(true).list();
		} else {
			return null;
		}
	}
	
	public static enum Page {
		sectioning,
		requests,
		;
		public String getId() { return name(); }
		public String getLabel() {
			switch(this) {
			case requests: return GWT_MSG.pageStudentCourseRequests();
			case sectioning: return GWT_MSG.pageStudentSchedulingAssistant();
			default: return name();
			}
		}
	}
	
	public static enum Type {
		BASIC,
		ACTIVE,
		TIME,
		;
		public String getId() { return name(); }
		public String getLabel() {
			switch(this) {
			case BASIC: return MSG.chartModeBasic();
			case ACTIVE: return MSG.chartModeActive();
			case TIME: return MSG.chartModeTimes();
			default: return name();
			}
		}
	}
	
	public static enum Interval {
		LAST_HOUR,
		LAST_3HOURS,
		LAST_DAY,
		LAST_WEEK,
		LAST_MONTH,
		CUSTOM,
		;

		public String getId() { return name(); }
		public String getLabel() {
			switch(this) {
			case LAST_DAY: return MSG.chartIntervalLastDay();
			case LAST_3HOURS: return MSG.chartIntervalLast3Hours();
			case LAST_HOUR: return MSG.chartIntervalLastHour();
			case LAST_WEEK: return MSG.chartIntervalLastWeek();
			case LAST_MONTH: return MSG.chartIntervalLastMonth();
			case CUSTOM: return MSG.chartIntervalCustom();
			default: return name();
			}
		}
		public Date getSince() {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			switch (this) {
			case LAST_3HOURS: c.add(Calendar.HOUR_OF_DAY, -3); break;
			case LAST_DAY: c.add(Calendar.DAY_OF_YEAR, -1); break;
			case LAST_HOUR: c.add(Calendar.HOUR_OF_DAY, -1); break;
			case LAST_MONTH: c.add(Calendar.MONTH, -1); break;
			case LAST_WEEK: c.add(Calendar.DAY_OF_YEAR, -7); break;
			}
			return c.getTime();
		}
	}
	
	public Type[] getTypes() { return Type.values(); }
	
	public Page[] getPages() { return Page.values(); }
	
	public Interval[] getIntervals() { return Interval.values(); }
	
	public List<AccessStatistics> getData(String host) {
		if (iFrom != null) {
			if (iTo == null) {
				return AccessStatisticsDAO.getInstance().getSession().createQuery(
						"from AccessStatistics where access > 0 and host = :host and page = :page and " +
						"timeStamp >= :from " +
						"order by timeStamp", AccessStatistics.class)
						.setParameter("host", host)
						.setParameter("page", iPage.name())
						.setParameter("from", iFrom)
						.list();
			} else {
				return AccessStatisticsDAO.getInstance().getSession().createQuery(
						"from AccessStatistics where access > 0 and host = :host and page = :page and " +
						"timeStamp >= :from and timeStamp <= :to " +
						"order by timeStamp", AccessStatistics.class)
						.setParameter("host", host)
						.setParameter("page", iPage.name())
						.setParameter("from", iFrom)
						.setParameter("to", iTo)
						.list();
			}
		} else if (iInterval == Interval.CUSTOM) {
			Date from = getFromDateTime();
			Date to = getToDateTime();
			if (from != null) {
				if (to == null) {
					return AccessStatisticsDAO.getInstance().getSession().createQuery(
							"from AccessStatistics where access > 0 and host = :host and page = :page and " +
							"timeStamp >= :from " +
							"order by timeStamp", AccessStatistics.class)
							.setParameter("host", host)
							.setParameter("page", iPage.name())
							.setParameter("from", from)
							.list();
				} else {
					return AccessStatisticsDAO.getInstance().getSession().createQuery(
							"from AccessStatistics where access > 0 and host = :host and page = :page and " +
							"timeStamp >= :from and timeStamp <= :to " +
							"order by timeStamp", AccessStatistics.class)
							.setParameter("host", host)
							.setParameter("page", iPage.name())
							.setParameter("from", from)
							.setParameter("to", to)
							.list();
				}
			} else {
				return null;
			}
		} else if (iInterval != null ) {
			return AccessStatisticsDAO.getInstance().getSession().createQuery(
					"from AccessStatistics where access > 0 and host = :host and page = :page and " +
					"timeStamp >= :since " +
					"order by timeStamp", AccessStatistics.class)
					.setParameter("host", host)
					.setParameter("page", iPage.name())
					.setParameter("since", iInterval.getSince())
					.list();
		} else {
			return null;
		}
	}
	
	public String getChartData(String host) {
		if (iPage == null || iType == null) return null;
		List<AccessStatistics> data = getData(host);
		if (data == null || data.isEmpty()) return null;
		int mod = 1;
		if (data.size() > 1000)
			mod = data.size() / 1000;
		switch(iType) {
		case ACTIVE:
			return getVisualisationDataActive(data, mod);
		case BASIC:
			return getVisualisationDataBasic(data, mod);
		case TIME:
			return getVisualisationDataTimes(data, mod);
		default:
			return null;
		}
	}
	
	public String getChartLabel(String host) {
		if (iPage == null || iType == null) return null;
		if (iFrom != null) {
			Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
			if (iTo == null) {
				return iPage.getLabel() + " " + iType.getLabel() + " (" + dateFormat.format(iFrom) + " @ " + host + ")";
			} else {
				return iPage.getLabel() + " " + iType.getLabel() + " (" + dateFormat.format(iFrom) + " - " + dateFormat.format(iTo) + " @ " + host + ")";
			}
		} else if (iInterval != null) {
			return iPage.getLabel() + " " + iType.getLabel() + " (" + iInterval.getLabel() + " @ " + host + ")";
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
	
	public List<IdValue> getTimes() {
		List<IdValue> ret = new ArrayList<>();
		for (int slot = 0; slot <= 288; slot += 3)
			ret.add(new IdValue((long)slot, Constants.slot2str(slot)));
		return ret;
	}
}
