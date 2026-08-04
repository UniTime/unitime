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

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.QueryLogStatsPage.Charts;
import org.unitime.timetable.gwt.client.admin.QueryLogStatsPage.QueryLogStatsRequest;
import org.unitime.timetable.gwt.client.admin.QueryLogStatsPage.QueryLogStatsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.model.dao.QueryLogDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(QueryLogStatsRequest.class)
public class QueryLogStatsBackend implements GwtRpcImplementation<QueryLogStatsRequest, QueryLogStatsResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public QueryLogStatsResponse execute(QueryLogStatsRequest request, SessionContext context) {
		context.checkPermission(Right.PageStatistics);
		QueryLogStatsResponse response = new QueryLogStatsResponse();
		
		for (QueryLog.ChartWindow w: QueryLog.ChartWindow.values()) {
			Charts ch = response.addCharts(w.getName());
			ch.setLeftChartAxeTitles(QueryLog.getColumns(w, QueryLog.ChartType.USERS));
			ch.setLeftChartData(QueryLog.getChart(w, QueryLog.ChartType.USERS));
			ch.setRightChartAxeTitles(QueryLog.getColumns(w, QueryLog.ChartType.TIME));
			ch.setRightChartData(QueryLog.getChart(w, QueryLog.ChartType.TIME));
		}

		response.setTable(getTable(7));
		
		return response;
	}
	
	protected TableInterface getTable(int days) {
		TableInterface table = new TableInterface();
		table.setId("PageStatistics");
		table.setDefaultSortCookie("!" + MSG.columnQueryLogCalls());
		table.setName(MSG.sectPageStatistics(days));
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnQueryLogURI());
        header.addCell(MSG.columnQueryLogCalls()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnQueryLogCallsOver10ms()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnQueryLogCallsOver100ms()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnQueryLogCallsOver1min()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnQueryLogAverageTime()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnQueryLogMaxTime()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnQueryLogErrors()).setTextAlignment(Alignment.RIGHT);
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, -days);
		DecimalFormat df = new DecimalFormat("#,##0.00");
		HashMap<String, Integer> errors = new HashMap<String, Integer>();
		for (Object[] o: QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.exception is not null group by q.uri", Object[].class).setParameter("date", c.getTime()).list()) {
			errors.put((String)o[0],((Number)o[1]).intValue());
		}
		HashMap<String, Integer> overMinutes = new HashMap<String, Integer>();
		for (Object[] o: QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.timeSpent > 1000 group by q.uri", Object[].class).setParameter("date", c.getTime()).list()) {
			overMinutes.put((String)o[0],((Number)o[1]).intValue());
		}
		HashMap<String, Integer> over100mss = new HashMap<String, Integer>();
		for (Object[] o: QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.timeSpent > 100 group by q.uri", Object[].class).setParameter("date", c.getTime()).list()) {
			over100mss.put((String)o[0],((Number)o[1]).intValue());
		}
		HashMap<String, Integer> over10mss = new HashMap<String, Integer>();
		for (Object[] o: QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q) from "+
				"QueryLog q where q.timeStamp > :date and q.timeSpent > 10 group by q.uri", Object[].class).setParameter("date", c.getTime()).list()) {
			over10mss.put((String)o[0],((Number)o[1]).intValue());
		}

		for (Object[] o: QueryLogDAO.getInstance().getSession().createQuery(
				"select q.uri, count(q), avg(q.timeSpent), max(q.timeSpent) from "+
				"QueryLog q where q.timeStamp > :date group by q.uri", Object[].class).setParameter("date", c.getTime()).list()) {
			Integer nrErrors = errors.get((String)o[0]);
			if (nrErrors == null) nrErrors = 0;
			Integer overMinute = overMinutes.get((String)o[0]);
			if (overMinute == null) overMinute = 0;
			Integer over100ms = over100mss.get((String)o[0]);
			if (over100ms == null) over100ms = 0;
			Integer over10ms = over10mss.get((String)o[0]);
			if (over10ms == null) over10ms = 0;
			
			LineInterface line = table.addLine();
			line.addCell((String)o[0]);
			line.addCell(((Number)o[1]).toString())
				.setComparable(((Number)o[1]).longValue())
				.setTextAlignment(Alignment.RIGHT);
			line.addCell(over10ms.toString())
				.setComparable(over10ms)
				.setTextAlignment(Alignment.RIGHT);;
			line.addCell(over100ms.toString())
				.setComparable(over100ms)
				.setTextAlignment(Alignment.RIGHT);;
			line.addCell(overMinute.toString())
				.setComparable(overMinute)
				.setTextAlignment(Alignment.RIGHT);;
			line.addCell(df.format(((Number)o[2]).doubleValue()))
				.setComparable(((Number)o[2]).doubleValue())
				.setTextAlignment(Alignment.RIGHT);;
			line.addCell(df.format(((Number)o[3]).doubleValue() / 1000.0))
				.setComparable(((Number)o[3]).doubleValue())
				.setTextAlignment(Alignment.RIGHT);;
			line.addCell(nrErrors.toString())
				.setComparable(nrErrors)
				.setTextAlignment(Alignment.RIGHT);;
		}
		return table;
	}

}
