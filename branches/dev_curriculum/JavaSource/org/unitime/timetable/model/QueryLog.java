/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.unitime.commons.web.WebTable;
import org.unitime.timetable.model.base.BaseQueryLog;
import org.unitime.timetable.model.dao.QueryLogDAO;

public class QueryLog extends BaseQueryLog {
	private static final long serialVersionUID = 7073111443207707716L;

	public QueryLog() {
		super();
	}
	
	public static enum Type {
		STRUCTS, GWT, OTHER
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
						over10ms,
						over100ms,
						overMinute,
						((Number)o[1]).intValue(),
						((Number)o[2]).doubleValue(),
						((Number)o[3]).doubleValue(),
						nrErrors});
		}
		return table;
	}
	
	public static enum ChartWindow {
		FIFTEEN_MINUTES("Last 24 Hours (15-minute average)"),
		THREE_HOUR("Last 7 Days (3-hour average)"),
		SEVEN_DAYS("Last 3 Months (7-day average)");
		private String iName;
		ChartWindow(String name) { iName = name; }
		public String getName() { return iName; }
	}
	
	public static enum ChartType {
		USERS, TIME
	}
	
	public static String getChart(ChartWindow w, ChartType t) {
		Date ts = new Date();
		Calendar from = Calendar.getInstance(Locale.US);
		from.setTime(ts);
		switch (w) {
		case SEVEN_DAYS:
			from.add(Calendar.MONTH, - 3);
			break;
		case THREE_HOUR:
			from.add(Calendar.DAY_OF_YEAR, - 7);
			break;
		case FIFTEEN_MINUTES:
			from.add(Calendar.HOUR, - 24);
			break;
		}
		Calendar to = Calendar.getInstance(Locale.US);
		to.setTime(from.getTime());
		switch (w) {
		case SEVEN_DAYS:
			to.add(Calendar.DAY_OF_YEAR, + 7);
			break;
		case THREE_HOUR:
			to.add(Calendar.HOUR, + 3);
			break;
		case FIFTEEN_MINUTES:
			to.add(Calendar.MINUTE, + 15);
			break;
		}
		String data[] = new String[] {"", "", "", "", ""};
		DecimalFormat df = new DecimalFormat("0.0");
		double max[] = new double[] { 0, 0};
		while (to.getTime().compareTo(ts) <= 0) {
			if (!data[1].isEmpty()) {
				data[0] += "|";
				for (int i = 1; i < data.length; i++)
					data[i] += ",";
			}
			switch (w) {
			case SEVEN_DAYS:
				if (from.get(Calendar.DAY_OF_MONTH) == 1)
					data[0] += new SimpleDateFormat("MMM/dd").format(from.getTime());
				break;
			case THREE_HOUR:
				if (from.get(Calendar.HOUR_OF_DAY) < 3)
					data[0] += new SimpleDateFormat("MM/dd").format(from.getTime()).toLowerCase();
				//else if (from.get(Calendar.HOUR_OF_DAY) == 7 || from.get(Calendar.HOUR_OF_DAY) == 12 || from.get(Calendar.HOUR_OF_DAY) == 17)
				//	data[0] += new SimpleDateFormat("H").format(from.getTime()).toLowerCase();
				break;
			case FIFTEEN_MINUTES:
				if (from.get(Calendar.MINUTE) < 15)
					data[0] += new SimpleDateFormat("H").format(from.getTime()).toLowerCase();
				break;
			}
			switch (t) {
			case USERS:
				 Object[] o = (Object[])QueryLogDAO.getInstance().getSession().createQuery(
					"select count(distinct uid), count(distinct sessionId), count(distinct uniqueId) from QueryLog where timeStamp > :from and timeStamp <= :to and not type = :type")
					.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type", Type.GWT.ordinal()).uniqueResult();
				 double gwtCallsPerMinute = ((Number)QueryLogDAO.getInstance().getSession().createQuery(
					"select count(distinct uniqueId) from QueryLog where timeStamp > :from and timeStamp <= :to and type = :type")
					.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type", Type.GWT.ordinal()).uniqueResult()).doubleValue();
				 int distinctUsers = ((Number)o[0]).intValue();
				 data[1] += distinctUsers;
				 int distinctSessions = ((Number)o[1]).intValue();
				 data[2] += distinctSessions;
				 max[0] = Math.max(max[0], distinctSessions);
				 double callsPerMinute = ((Number)o[2]).doubleValue();
				 switch (w) {
				 case SEVEN_DAYS:
					 callsPerMinute /= 7 * 24 * 60;
					 gwtCallsPerMinute /= 7 * 24 * 60;
					 break;
				 case THREE_HOUR:
					 callsPerMinute /= 3 * 60;
					 gwtCallsPerMinute /= 3 * 60;
					break;
				 case FIFTEEN_MINUTES:
					 callsPerMinute /= 15;
					 gwtCallsPerMinute /= 15;
					break;
				 }
				 data[3] += df.format(callsPerMinute);
				 max[1] = Math.max(max[1], callsPerMinute);
				 data[4] += df.format(gwtCallsPerMinute);
				 max[1] = Math.max(max[1], gwtCallsPerMinute);
				 break;
			case TIME:
				o = (Object[])QueryLogDAO.getInstance().getSession().createQuery(
					"select avg(q.timeSpent), max(q.timeSpent) from QueryLog q where q.timeStamp > :from and q.timeStamp <= :to and not type = :type")
					.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type", Type.GWT.ordinal()).uniqueResult();
				Object[] p = (Object[])QueryLogDAO.getInstance().getSession().createQuery(
				"select avg(q.timeSpent), max(q.timeSpent) from QueryLog q where q.timeStamp > :from and q.timeStamp <= :to and type = :type")
				.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type", Type.GWT.ordinal()).uniqueResult();
				int avgTime = (o[0] == null ? 0 : ((Number)o[0]).intValue());
				data[1] += avgTime;
				max[0] = Math.max(max[0], avgTime);
				double maxTime = (o[1] == null ? 0 : ((Number)o[1]).intValue()) / 1000.0;
				data[2] += maxTime;
				max[1] = Math.max(max[1], maxTime);
				int gwtAvgTime = (p[0] == null ? 0 : ((Number)p[0]).intValue());
				data[3] += gwtAvgTime;
				max[0] = Math.max(max[0], gwtAvgTime);
				double gwtMaxTime = (p[1] == null ? 0 : ((Number)p[1]).intValue()) / 1000.0;
				data[4] += gwtMaxTime;
				max[1] = Math.max(max[1], gwtMaxTime);
				break;			
			}
			switch (w) {
			case SEVEN_DAYS:
				from.add(Calendar.DAY_OF_YEAR, +1);
				to.add(Calendar.DAY_OF_YEAR, + 1);
				break;
			case THREE_HOUR:
				from.add(Calendar.HOUR, +3);
				to.add(Calendar.HOUR, + 3);
			case FIFTEEN_MINUTES:
				from.add(Calendar.MINUTE, +15);
				to.add(Calendar.MINUTE, + 15);
			}				
		}
		double range[] = new double[] { 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000, 2000, 2500, 5000, 10000, 20000, 50000, 100000, 1000000, 10000000};
		double step[] = new double[] { 1, 1};
		for (int i = 0; i < max.length; i++) {
			if (max[i] <= 1.0) { max[i] = 1.0; step[i] = 0.1; break; }
			int x = 0;
			while (max[i] / range[x] > 16) x++;
			step[i] = range[x];
		}
		switch (t) {
		case USERS:
			return "http://chart.apis.google.com/chart?" + 
				"cht=lc&chd=t:" + data[1] + "|" + data[2] + "|" + data[3] + "|" + data[4] +
				"&chs=400x300&chl=" + data[0] + "&chxt=x,y,y,r,r&chxr=1,0," + df.format(max[0]) + "," + df.format(step[0]) + "|3,0," + df.format(max[1]) + "," + df.format(step[1]) +
				"&chdl=Distinct+Users|Distinct+HTTP+Sessions|Pages+per+Minute|GWT+Calls+per+Minute&chco=0000FF,00FF00,FF0000,FFA500" +
				"&chdlp=t&chds=0," + df.format(max[0]) + ",0," + df.format(max[0]) + ",0," + df.format(max[1]) + ",0," + df.format(max[1]) +
				"&chxl=4:||e|t|u|n|i|M|+|r|e|p|+|s|l|l|a|C||2:|s|n|o|i|s|s|e|s|+|s|r|e|s|u|+|f|o|+|r|b|N" +
				"&chxs=1,0000FF|2,00FF00|3,FF0000|4,FFA500";
		case TIME:
			return "http://chart.apis.google.com/chart?" + 
				"cht=lc&chd=t:" + data[1] + "|" + data[2] + "|" + data[3] + "|" + data[4] + 
				"&chs=400x300&chl=" + data[0] + "&chxt=x,y,y,r,r&chxr=1,0," + df.format(max[0]) + "," + df.format(step[0]) + "|3,0," + df.format(max[1]) + "," + df.format(step[1]) +
				"&chdlp=t&chds=0," + df.format(max[0]) + ",0," + df.format(max[1]) + ",0," + df.format(max[0]) + ",0," + df.format(max[1]) +
				"&chdl=Average+Time+[ms]|Max+Time+[s]|GWT+Average+Time+[ms]|GWT+Max+Time+[s]&chco=0000FF,FF0000,00FF00,FFA500" + 
				"&chxl=2:||e|m|i|T|+|e|g|a|r|e|v|A||4:||e|m|i|T|+|x|a|M|" +
				"&chxs=1,0000FF|2,00FF00|3,FF0000|4,FFA500";
		default:
			return "";
		}
	}
}
