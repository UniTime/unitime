/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.unitime.commons.web.WebTable;
import org.unitime.timetable.model.base.BaseQueryLog;
import org.unitime.timetable.model.dao.QueryLogDAO;

/**
 * @author Tomas Muller
 */
public class QueryLog extends BaseQueryLog {
	private static final long serialVersionUID = 7073111443207707716L;

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
			from.add(Calendar.DAY_OF_YEAR, - 1);
			break;
		}
		Calendar to = Calendar.getInstance(Locale.US);
		to.setTime(from.getTime());
		switch (w) {
		case SEVEN_DAYS:
			to.add(Calendar.DAY_OF_YEAR, + 7);
			break;
		case THREE_HOUR:
			to.add(Calendar.HOUR_OF_DAY, + 3);
			break;
		case FIFTEEN_MINUTES:
			to.add(Calendar.MINUTE, + 15);
			break;
		}
		String axe = "";
		List<Double>[] data = new List[] { new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>()}; 
		double max[] = new double[] { 0, 0};
		int index = 0;
		while (to.getTime().compareTo(ts) <= 0) {
			if (index > 0) {
				axe += "|";
			}
			switch (w) {
			case SEVEN_DAYS:
				if (from.get(Calendar.DAY_OF_MONTH) == 1)
					axe += new SimpleDateFormat("MMM/dd").format(from.getTime());
				break;
			case THREE_HOUR:
				if (from.get(Calendar.HOUR_OF_DAY) < 3)
					axe += new SimpleDateFormat("MM/dd").format(from.getTime()).toLowerCase();
				break;
			case FIFTEEN_MINUTES:
				if (from.get(Calendar.MINUTE) < 15)
					axe += new SimpleDateFormat("H").format(from.getTime()).toLowerCase();
				break;
			}
			switch (t) {
			case USERS:
				 Object[] o = (Object[])QueryLogDAO.getInstance().getSession().createQuery(
					"select count(distinct uid), count(distinct sessionId), count(distinct uniqueId) from QueryLog where timeStamp > :from and timeStamp <= :to and not type = :type1 and not type = :type2")
					.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type1", Type.GWT.ordinal()).setInteger("type2", Type.RPC.ordinal()).uniqueResult();
				 double gwtCallsPerMinute = ((Number)QueryLogDAO.getInstance().getSession().createQuery(
					"select count(distinct uniqueId) from QueryLog where timeStamp > :from and timeStamp <= :to and (type = :type1 or type = :type2)")
					.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type1", Type.GWT.ordinal()).setInteger("type2", Type.RPC.ordinal()).uniqueResult()).doubleValue();
				 double distinctUsers = ((Number)o[0]).doubleValue();
				 data[0].add(distinctUsers);
				 double distinctSessions = ((Number)o[1]).doubleValue();
				 data[1].add(distinctSessions);
				 max[0] = Math.max(max[0], Math.max(distinctUsers, distinctSessions));
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
				 data[2].add(callsPerMinute);
				 data[3].add(gwtCallsPerMinute);
				 max[1] = Math.max(max[1], Math.max(callsPerMinute, gwtCallsPerMinute));
				 break;
			case TIME:
				o = (Object[])QueryLogDAO.getInstance().getSession().createQuery(
					"select avg(q.timeSpent), max(q.timeSpent) from QueryLog q where q.timeStamp > :from and q.timeStamp <= :to and not type = :type1 and not type = :type2")
					.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type1", Type.GWT.ordinal()).setInteger("type2", Type.RPC.ordinal()).uniqueResult();
				Object[] p = (Object[])QueryLogDAO.getInstance().getSession().createQuery(
				"select avg(q.timeSpent), max(q.timeSpent) from QueryLog q where q.timeStamp > :from and q.timeStamp <= :to and (type = :type1 or type = :type2)")
				.setTimestamp("from", from.getTime()).setTimestamp("to", to.getTime()).setInteger("type1", Type.GWT.ordinal()).setInteger("type2", Type.RPC.ordinal()).uniqueResult();
				double avgTime = (o[0] == null ? 0 : ((Number)o[0]).doubleValue());
				double maxTime = (o[1] == null ? 0 : ((Number)o[1]).doubleValue()) / 1000.0;
				double gwtAvgTime = (p[0] == null ? 0 : ((Number)p[0]).doubleValue());
				double gwtMaxTime = (p[1] == null ? 0 : ((Number)p[1]).doubleValue()) / 1000.0;
				data[0].add(avgTime);
				data[1].add(maxTime);
				data[2].add(gwtAvgTime);
				data[3].add(gwtMaxTime);
				max[0] = Math.max(max[0], Math.max(avgTime, gwtAvgTime));
				max[1] = Math.max(max[1], Math.max(maxTime, gwtMaxTime));
				break;			
			}
			switch (w) {
			case SEVEN_DAYS:
				from.add(Calendar.DAY_OF_YEAR, +1);
				to.add(Calendar.DAY_OF_YEAR, + 1);
				break;
			case THREE_HOUR:
				from.add(Calendar.HOUR_OF_DAY, +3);
				to.add(Calendar.HOUR_OF_DAY, + 3);
			case FIFTEEN_MINUTES:
				from.add(Calendar.MINUTE, +15);
				to.add(Calendar.MINUTE, + 15);
			}
			index++;
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
				"&chdl=Distinct+Users|Distinct+HTTP+Sessions|Pages+per+Minute|GWT+Calls+per+Minute&chco=0000FF,00FF00,FF0000,FFA500" +
				"&chdlp=t&chds=0," + df.format(max[0]) + ",0," + df.format(max[0]) + ",0," + df.format(max[1]) + ",0," + df.format(max[1]) +
				"&chxl=4:||e|t|u|n|i|M|+|r|e|p|+|s|l|l|a|C||2:|s|n|o|i|s|s|e|s|+|s|r|e|s|u|+|f|o|+|r|b|N" +
				"&chxs=1,0000FF|2,00FF00|3,FF0000|4,FFA500";
		case TIME:
			return "http://chart.apis.google.com/chart?" +
				"cht=lc&chd=e:" + encode(data[0], max[0]) + "," + encode(data[1], max[1]) + "," + encode(data[2], max[0]) + "," + encode(data[3], max[1]) +
				"&chs=400x300&chl=" + axe + "&chxt=x,y,y,r,r&chxr=1,0," + df.format(max[0]) + "," + df.format(step[0]) + "|3,0," + df.format(max[1]) + "," + df.format(step[1]) +
				"&chdlp=t&chds=0," + df.format(max[0]) + ",0," + df.format(max[1]) + ",0," + df.format(max[0]) + ",0," + df.format(max[1]) +
				"&chdl=Average+Time+[ms]|Max+Time+[s]|GWT+Average+Time+[ms]|GWT+Max+Time+[s]&chco=0000FF,FF0000,00FF00,FFA500" + 
				"&chxl=2:||e|m|i|T|+|e|g|a|r|e|v|A||4:||e|m|i|T|+|x|a|M|" +
				"&chxs=1,0000FF|2,00FF00|3,FF0000|4,FFA500";
		default:
			return "";
		}
	}
}
