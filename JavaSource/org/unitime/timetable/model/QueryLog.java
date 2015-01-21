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
package org.unitime.timetable.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.model.base.BaseQueryLog;
import org.unitime.timetable.model.dao.QueryLogDAO;

/**
 * @author Tomas Muller
 */
public class QueryLog extends BaseQueryLog {
	private static final long serialVersionUID = 7073111443207707716L;
	protected static Log sLog = LogFactory.getLog(QueryLog.class);

	public QueryLog() {
		super();
	}
	
	public static enum Type {
		STRUCTS, GWT, OTHER, RPC
	}
	
	public static int getNrSessions(int days) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, -days);
		return ((Number)QueryLogDAO.getInstance().getSession().createQuery(
				"select count(distinct q.sessionId) from QueryLog q where q.timeStamp > :date").
				setTimestamp("date", c.getTime()).uniqueResult()).intValue();
	}
	
	public static int getNrActiveUsers(int days) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, -days);
		return ((Number)QueryLogDAO.getInstance().getSession().createQuery(
				"select count(distinct q.uid) from QueryLog q where q.timeStamp > :date").
				setTimestamp("date", c.getTime()).uniqueResult()).intValue();
	}

	public static WebTable getTopQueries(int days) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, -days);
		WebTable table = new WebTable(8, "Page Statistics (last " + days + " days)", "stats.do?ord=%%",
				new String[] {"URI", "Calls", "Calls [>10ms]", "Calls [>100ms]", "Calls [>1min]", "AvgTime [ms]", "MaxTime [s]", "Errors"},
				new String[] {"left", "right", "right", "right", "right", "right", "right", "right"},
				new boolean[] {true, false, false, false, false, false, false, false});
		DecimalFormat df = new DecimalFormat("#,##0.00");
		HashMap<String, Integer> errors = new HashMap<String, Integer>();
		for (Object[] o: (List<Object[]>)QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.exception is not null group by q.uri").setTimestamp("date", c.getTime()).list()) {
			errors.put((String)o[0],((Number)o[1]).intValue());
		}
		HashMap<String, Integer> overMinutes = new HashMap<String, Integer>();
		for (Object[] o: (List<Object[]>)QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.timeSpent > 1000 group by q.uri").setTimestamp("date", c.getTime()).list()) {
			overMinutes.put((String)o[0],((Number)o[1]).intValue());
		}
		HashMap<String, Integer> over100mss = new HashMap<String, Integer>();
		for (Object[] o: (List<Object[]>)QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.timeSpent > 100 group by q.uri").setTimestamp("date", c.getTime()).list()) {
			over100mss.put((String)o[0],((Number)o[1]).intValue());
		}
		HashMap<String, Integer> over10mss = new HashMap<String, Integer>();
		for (Object[] o: (List<Object[]>)QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.timeSpent > 10 group by q.uri").setTimestamp("date", c.getTime()).list()) {
			over10mss.put((String)o[0],((Number)o[1]).intValue());
		}

		for (Object[] o: (List<Object[]>)QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q), avg(q.timeSpent), max(q.timeSpent) from "+
				"QueryLog q where q.timeStamp > :date group by q.uri").setTimestamp("date", c.getTime()).list()) {
			Integer nrErrors = errors.get((String)o[0]);
			if (nrErrors == null) nrErrors = 0;
			Integer overMinute = overMinutes.get((String)o[0]);
			if (overMinute == null) overMinute = 0;
			Integer over100ms = over100mss.get((String)o[0]);
			if (over100ms == null) over100ms = 0;
			Integer over10ms = over10mss.get((String)o[0]);
			if (over10ms == null) over10ms = 0;
			table.addLine(new String[] {
					(String)o[0],
					((Number)o[1]).toString(),
					over10ms.toString(),
					over100ms.toString(),
					overMinute.toString(),
					df.format(((Number)o[2]).doubleValue()),
					df.format(((Number)o[3]).doubleValue() / 1000.0),
					nrErrors.toString()},
					new Comparable[] {
						(String)o[0],
						((Number)o[1]).intValue(),
						over10ms,
						over100ms,
						overMinute,
						((Number)o[2]).doubleValue(),
						((Number)o[3]).doubleValue(),
						nrErrors});
		}
		return table;
	}
	
	public static enum ChartWindow {
		LAST_HOUR("Last 3 Hours", "kkmm", Hours.THREE, 10, Minutes.ONE,
				"k:mm", DateTimeFieldType.minuteOfHour(), 20, 0, 10, "Minute",
				"to_char(timeStamp, 'HH24MI')", "timeStamp > current_date() - 1",
				"date_format(timeStamp, '%H%i')", "timeStamp > adddate(current_date(), -1)"),
/*		LAST_DAY("Last 24 Hours (1-hour average)", "kk", Days.ONE, 1, Hours.ONE,
				"k", 2, 60, "Hour",
				"to_char(timeStamp, 'HH24')", "timeStamp > current_date() - 1",
				"date_format(timeStamp, '%H')", "timeStamp > adddate(current_date(), -1)"),*/
		LAST_WEEK("Last 7 Days", "ddkk", Days.SEVEN, 1, Hours.ONE,
				"MM/d", DateTimeFieldType.hourOfDay(), 24, 0, 60, "Hour",
				"to_char(timeStamp, 'DDHH24')", "timeStamp > current_date() - 7",
				"date_format(timeStamp, '%d%H')", "timeStamp > adddate(current_date(), -7)"),
		LAST_MONTH("Last 3 Months", "MMdd", Months.THREE, 1, Days.ONE,
				"MMM/d", DateTimeFieldType.dayOfMonth(), 32, 1, 24 * 60, "Day",
				"to_char(timeStamp, 'MMDD')", "timeStamp > current_date() - 92",
				"date_format(timeStamp, '%m%d')", "timeStamp > adddate(current_date(), -92)"),
		;
		private String iName;
		private DateFormat iFormat;
		private ReadablePeriod iStart, iIncrement;
		private int iWindow;
		private String iOracleFormat, iOracleCondition;
		private String iMySqlFormat, iMySqlCondition;
		private String iAxeFormat, iBase;
		private int iMinutes;
		private DateTimeFieldType iAxeType; int iAxeMod, iAxeValue;
		
		ChartWindow(String name, String format,
				ReadablePeriod start, int window, ReadablePeriod increment,
				String axeFormat, DateTimeFieldType axeType, int axeMod, int axeValue, int minutes, String base,
				String oracleFormat, String oracleCondition, String mySqlFormat, String mySqlCondition) {
			iName = name;
			iFormat = new SimpleDateFormat(format, Locale.US);
			iStart = start; iWindow = window; iIncrement = increment;
			iAxeFormat = axeFormat; iAxeType = axeType; iAxeMod = axeMod; iAxeValue = axeValue;
			iMinutes = minutes; iBase = base;
			iOracleFormat = oracleFormat; iOracleCondition = oracleCondition;
			iMySqlFormat = mySqlFormat; iMySqlCondition = mySqlCondition;
		}
		public String getName() { return iName; }
		public String getBase() { return iBase; }
		public String format(DateTime date) { return iFormat.format(date.getMillis()); }
		public DateTime getFirst(DateTime now) { return now.minus(iStart); }
		public DateTime next(DateTime date, DateTime now) {
			DateTime ret = date.plus(iIncrement);
			return (ret.isAfter(now) ? null : ret);
		}
		public int getMinutes() { return iMinutes; }
		
		public Map<String, int[]> getUsersAndSessions(org.hibernate.Session hibSession) {
			String query = null;
			if (HibernateUtil.isMySQL()) {
				query = "select " + iMySqlFormat + ", count(distinct uid), count(distinct sessionId) from " + 
						"QueryLog where " + iMySqlCondition + " group by " + iMySqlFormat;
			} else {
				query = "select " + iOracleFormat + ", count(distinct uid), count(distinct sessionId) from " + 
						"QueryLog where " + iOracleCondition + " group by " + iOracleFormat;
			}
			Map<String, int[]> ret = new HashMap<String, int[]>();
			for (Object[] o: (List<Object[]>)hibSession.createQuery(query).list()) {
				String dt = (String)o[0];
				int users = ((Number)o[1]).intValue();
				int sessions = ((Number)o[2]).intValue();
				ret.put(dt, new int[] {users, sessions});
			}
			return ret;
		}
		
		public Map<String, int[]> getQueriesPerType(org.hibernate.Session hibSession) {
			String query = null;
			if (HibernateUtil.isMySQL()) {
				query = "select " + iMySqlFormat + ", type, count(uniqueId) from " + 
						"QueryLog where " + iMySqlCondition + " group by type, " + iMySqlFormat;
			} else {
				query = "select " + iOracleFormat + ", type, count(uniqueId) from " + 
						"QueryLog where " + iOracleCondition + " group by type, " + iOracleFormat;
			}
			Map<String, int[]> ret = new HashMap<String, int[]>();
			for (Object[] o: (List<Object[]>)hibSession.createQuery(query).list()) {
				String dt = (String)o[0];
				int type = ((Number)o[1]).intValue();
				int queries = ((Number)o[2]).intValue();
				int[] counts = ret.get(dt);
				if (counts == null) {
					counts = new int[Type.values().length];
					for (int i = 0; i < counts.length; i++) counts[i] = 0;
					ret.put(dt, counts);
				}
				counts[type] = queries;
			}
			return ret;
		}
		
		public Map<String, double[]> getTimes(org.hibernate.Session hibSession) {
			String query = null;
			if (HibernateUtil.isMySQL()) {
				query = "select " + iMySqlFormat + ", type, count(uniqueId), sum(timeSpent), max(timeSpent) from " + 
						"QueryLog where " + iMySqlCondition + " group by type, " + iMySqlFormat;
			} else {
				query = "select " + iOracleFormat + ", type, count(uniqueId), sum(timeSpent), max(timeSpent) from " + 
						"QueryLog where " + iOracleCondition + " group by type, " + iOracleFormat;
			}
			Map<String, double[]> ret = new HashMap<String, double[]>();
			for (Object[] o: (List<Object[]>)hibSession.createQuery(query).list()) {
				String dt = (String)o[0];
				int type = ((Number)o[1]).intValue();
				int cnt = ((Number)o[2]).intValue();
				double sum = ((Number)o[3]).doubleValue();
				double max = ((Number)o[4]).doubleValue() / 1000.0;
				double[] counts = ret.get(dt);
				if (counts == null) {
					counts = new double[3 * Type.values().length];
					for (int i = 0; i < counts.length; i++) counts[i] = 0;
					ret.put(dt, counts);
				}
				counts[3 * type] = sum;
				counts[3 * type + 1] = cnt;
				counts[3 * type + 2] = max;
			}
			return ret;
		}
		
		public double[] countUsers(Map<String, int[]> table, DateTime date) {
			DateTime d = date;
			int[] ret = new int[2];
			for (int i = 0; i < ret.length; i++) ret[i] = 0;
			for (int i = 0; i < iWindow; i++) {
				int[] count = table.get(format(d));
				if (count != null)
					for (int j = 0; j < count.length; j++) ret[j] += count[j];
				d = d.plus(iIncrement);
			}
			return new double[] { ((double)ret[0]) / iWindow, ((double)ret[1]) / iWindow };
		}
		
		public int[] countQueries(Map<String, int[]> table, DateTime date) {
			DateTime d = date;
			int[] ret = new int[Type.values().length];
			for (int i = 0; i < ret.length; i++) ret[i] = 0;
			for (int i = 0; i < iWindow; i++) {
				int[] count = table.get(format(d));
				if (count != null)
					for (int j = 0; j < count.length; j++) ret[j] += count[j];
				d = d.plus(iIncrement);
			}
			return ret;
		}
		
		public double[] countTimes(Map<String, double[]> table, DateTime date) {
			DateTime d = date;
			double[] ret = new double[3 * Type.values().length];
			for (int i = 0; i < ret.length; i++) ret[i] = 0;
			for (int i = 0; i < iWindow; i++) {
				double[] count = table.get(format(d));
				if (count != null)
					for (int j = 0; j < count.length; j++) {
						if ((j % 3) == 2)
							ret[j] = Math.max(ret[j], count[j]);
						else
							ret[j] += count[j];
					}
				d = d.plus(iIncrement);
			}
			return ret;
		}
		
		public String axe(DateTime now) {
			SimpleDateFormat format = new SimpleDateFormat(iAxeFormat, Localization.getJavaLocale());
			DateTime dt = getFirst(now);
			int i = 0;
			StringBuffer ret = new StringBuffer();
			while (dt != null) {
				if (i > 0) ret.append("|");
				if ((dt.get(iAxeType) % iAxeMod) == iAxeValue)
					ret.append(format.format(dt.getMillis()));
				i++;
				dt = next(dt, now);
			}
			return ret.toString();
		}
	}
	
	public static enum ChartType {
		USERS, TIME
	}
	
	private static String sExtendedEncoding = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.";
	private static int sExtendedEncodingMax = sExtendedEncoding.length() * sExtendedEncoding.length();
	
	private static String encode(List<Double> data, double max) {
		StringBuffer ret = new StringBuffer();
		for (Double val: data) {
			int scaled = (int)Math.floor(sExtendedEncodingMax * val / max);
			if (scaled >= sExtendedEncodingMax) {
				ret.append("..");
			} else if (scaled < 0) {
				ret.append("__");
			} else {
				ret.append(sExtendedEncoding.charAt(scaled / sExtendedEncoding.length()));
				ret.append(sExtendedEncoding.charAt(scaled % sExtendedEncoding.length()));
			}
		}
		return ret.toString();
	}
	
	public static String getChart(ChartWindow w, ChartType t) {
		DateTime now = DateTime.now();
		String axe = w.axe(now);

		DateTime dt = w.getFirst(now);
		List<Double>[] data = new List[] { new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>()};
		double max[] = new double[] { 0, 0 };
		if (t == ChartType.USERS) {
			Map<String, int[]> queries = w.getQueriesPerType(QueryLogDAO.getInstance().getSession());
			Map<String, int[]> usersAndSessions = w.getUsersAndSessions(QueryLogDAO.getInstance().getSession());
			while (dt != null) {
				double[] us = w.countUsers(usersAndSessions, dt);
				int[] q = w.countQueries(queries, dt);
				
				double users = us[0];
				data[0].add(users);
				double sessions = us[1];
				data[1].add(sessions);
				max[0] = Math.max(max[0], Math.max(users, sessions));
				
				double calls = ((double)(q[Type.STRUCTS.ordinal()] + q[Type.OTHER.ordinal()])) / w.getMinutes();
				double gwtCalls = ((double)(q[Type.GWT.ordinal()] + q[Type.RPC.ordinal()])) / w.getMinutes();
				data[2].add(calls);
				data[3].add(gwtCalls);
				max[1] = Math.max(max[1], Math.max(calls, gwtCalls));
				
				dt = w.next(dt, now);
			}
			sLog.debug("[" + w.name() + "] Users: " + max[0] + " / " + data[0]);
			sLog.debug("[" + w.name() + "] Sessions: " + max[0] + " / " + data[1]);
			sLog.debug("[" + w.name() + "] Calls: " + max[1] + " / " + data[2]);
			sLog.debug("[" + w.name() + "] GWT: " + max[1] + " / " + data[3]);
		} else {
			Map<String, double[]> times = w.getTimes(QueryLogDAO.getInstance().getSession());
			while (dt != null) {
				double[] tm = w.countTimes(times, dt);
				
				double sumTime = (tm[3 * Type.STRUCTS.ordinal()] + tm[3 * Type.OTHER.ordinal()]);
				double cntTime = (tm[3 * Type.STRUCTS.ordinal() + 1] + tm[3 * Type.OTHER.ordinal() + 1]);
				double avgTime =  (cntTime > 0.0 ? sumTime / cntTime : 0.0);
				double gwtSumTime = (tm[3 * Type.GWT.ordinal()] + tm[3 * Type.RPC.ordinal()]);
				double gwtCntTime = (tm[3 * Type.GWT.ordinal() + 1] + tm[3 * Type.RPC.ordinal() + 1]);
				double gwtAvgTime = (gwtCntTime > 0.0 ? gwtSumTime / gwtCntTime : 0.0);
				double maxTime = Math.max(tm[3 * Type.STRUCTS.ordinal() + 2],tm[3 * Type.OTHER.ordinal() + 2]);
				double gwtMaxTime = Math.max(tm[3 * Type.GWT.ordinal() + 2],tm[3 * Type.RPC.ordinal() + 2]);
				
				data[0].add(maxTime);
				data[1].add(avgTime);
				data[2].add(gwtMaxTime);
				data[3].add(gwtAvgTime);
				max[0] = Math.max(max[0], Math.max(maxTime, gwtMaxTime));
				max[1] = Math.max(max[1], Math.max(avgTime, gwtAvgTime));
				
				dt = w.next(dt, now);
			}
			sLog.debug("[" + w.name() + "] Max Time: " + max[0] + " / " + data[0]);
			sLog.debug("[" + w.name() + "] Avg Time: " + max[1] + " / " + data[1]);
			sLog.debug("[" + w.name() + "] Gwt Max Time: " + max[0] + " / " + data[2]);
			sLog.debug("[" + w.name() + "] Gwt Avg Time: " + max[1] + " / " + data[3]);
		}

		DecimalFormat df = new DecimalFormat("0.0");
		double range[] = new double[] { 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000, 2000, 2500, 5000, 10000, 20000, 50000, 100000, 1000000, 10000000};
		double step[] = new double[] { 1, 1};
		for (int i = 0; i < max.length; i++) {
			if (max[i] <= 1.0) { max[i] = 1.0; step[i] = 0.1; continue; }
			int x = 0;
			while (max[i] / range[x] > 16) x++;
			step[i] = range[x];
		}
		switch (t) {
		case USERS:
			return "http://chart.apis.google.com/chart?" + 
				"cht=lc&chd=e:" + encode(data[0], max[0]) + "," + encode(data[1], max[0]) + "," + encode(data[2], max[1]) + "," + encode(data[3], max[1]) +
				"&chs=400x300&chl=" + axe + "&chxt=x,y,y,r,r&chxr=1,0," + df.format(max[0]) + "," + df.format(step[0]) + "|3,0," + df.format(max[1]) + "," + df.format(step[1]) +
				"&chdl=Users+per+" + w.getBase() + "|HTTP+Sessions+per+" + w.getBase() + "|Pages+per+Minute|GWT+Calls+per+Minute&chco=0000FF,00FF00,FF0000,FFA500" +
				"&chdlp=t&chds=0," + df.format(max[0]) + ",0," + df.format(max[0]) + ",0," + df.format(max[1]) + ",0," + df.format(max[1]) +
				"&chxl=4:||e|t|u|n|i|M|+|r|e|p|+|s|l|l|a|C||2:|s|n|o|i|s|s|e|s|+|s|r|e|s|u|+|f|o|+|r|b|N" +
				"&chxs=1,0000FF|2,00FF00|3,FF0000|4,FFA500";
		case TIME:
			return "http://chart.apis.google.com/chart?" +
				"cht=lc&chd=e:" + encode(data[0], max[0]) + "," + encode(data[2], max[0]) + "," + encode(data[1], max[1]) + "," + encode(data[3], max[1]) +
				"&chs=400x300&chl=" + axe + "&chxt=x,y,y,r,r&chxr=1,0," + df.format(max[0]) + "," + df.format(step[0]) + "|3,0," + df.format(max[1]) + "," + df.format(step[1]) +
				"&chdlp=t&chds=0," + df.format(max[0]) + ",0," + df.format(max[0]) + ",0," + df.format(max[1]) + ",0," + df.format(max[1]) +
				"&chdl=Max+Time+[s]|GWT+Max+Time+[s]|Average+Time+[ms]|GWT+Average+Time+[ms]&chco=0000FF,00FF00,FF0000,FFA500" + 
				"&chxl=4:||e|m|i|T|+|e|g|a|r|e|v|A||2:||e|m|i|T|+|x|a|M|" +
				"&chxs=1,0000FF|2,00FF00|3,FF0000|4,FFA500";
		default:
			return "";
		}
	}
}
