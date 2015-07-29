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
package org.unitime.timetable.server.sectioning;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.CSVFile.CSVLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GenerateSectioningReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:sct-report.csv")
public class SectioningReportsExporter implements Exporter {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverServerService solverServerService;
	
	@Override
	public String reference() {
		return "sct-report.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		DataProperties parameters = new DataProperties();
		for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
			String name = e.nextElement();
			parameters.put(name, helper.getParameter(name));
		}
		parameters.put("useAmPm", CONSTANTS.useAmPm() ? "true" : "false");
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null && helper.getSessionContext().isAuthenticated())
			sessionId = helper.getSessionContext().getUser().getCurrentAcademicSessionId();
		if (sessionId == null)
			throw new GwtRpcException("No academic session provided.");
		
		CSVFile csv = null;
		boolean online = parameters.getPropertyBoolean("online", true);
		SessionContext context = helper.getSessionContext();
		if (online) {
			context.checkPermissionAnyAuthority(sessionId, "Session", Right.SchedulingDashboard);
			
			OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
			if (server == null)
				throw new GwtRpcException("Online student scheduling is not enabled for session " + sessionId + ".");
			
			OnlineSectioningLog.Entity user = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(context.getUser().getExternalUserId())
					.setName(context.getUser().getName() == null ? context.getUser().getUsername() : context.getUser().getName())
					.setType(context.hasPermission(Right.StudentSchedulingAdvisor) ? OnlineSectioningLog.Entity.EntityType.MANAGER : OnlineSectioningLog.Entity.EntityType.STUDENT).build();
			
			csv = server.execute(server.createAction(GenerateSectioningReport.class).withParameters(parameters), user);
		} else {
			context.checkPermission(Right.StudentSectioningSolver);
			
			StudentSolverProxy solver = studentSectioningSolverService.getSolver();
			if (solver == null)
				throw new GwtRpcException("No student solver is running.");
			
			csv = solver.getReport(parameters);
		}
		if (csv == null)
			throw new GwtRpcException("No report was created.");
		
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), helper.getParameter("name").toLowerCase().replace('_', '-') + ".csv", false);
		
		String[] header = new String[csv.getHeader().getFields().size()];
		for (int i = 0; i < csv.getHeader().getFields().size(); i++)
			header[i] = csv.getHeader().getField(i).toString();
		out.printHeader(header);
		
		DecimalFormat pf = new DecimalFormat("0.00%");
		List<Row> rows = new ArrayList<Row>();
		Row prev = null;
		if (csv.getLines() != null)
			for (CSVLine line: csv.getLines()) {
				if (line.getFields().isEmpty()) continue;
				Row data = new Row(line);
				while (prev != null) {
					if (data.getNrBlanks() > prev.getNrBlanks()) break;
					prev = prev.getParent();
				}
				if (prev != null)
					data.setParent(prev);
				rows.add(data);
				prev = data;
			}

		String sort = helper.getParameter("sort");
		if (sort != null && !"0".equals(sort)) {
			final boolean asc = Integer.parseInt(sort) > 0;
			final int col = Math.abs(Integer.parseInt(sort)) - 1;
			Collections.sort(rows, new Comparator<Row>() {
				@Override
				public int compare(Row o1, Row o2) {
					return (asc ? o1.compareTo(o2, col) : o2.compareTo(o1, col));
				}
			});
		}
		
		prev = null;
		for (Row row: rows) {
			boolean prevHide = true;
			String[] line = new String[csv.getHeader().size()];
			for (int x = 0; x < csv.getHeader().size(); x++) {
				boolean hide = true;
				if (prev == null || !prevHide || !prev.getCell(x).equals(row.getCell(x))) hide = false;
				String text = row.getCell(x);
				boolean number = false;
				if (csv.getHeader().getField(x).toString().contains("%")) {
					if (x > 0)
						try {
							Double.parseDouble(text);
							number = true;
						} catch (Exception e) {}
					if (number)
						text = pf.format(Double.parseDouble(text));
				}
				line[x] = (hide ? "" : text);
				prevHide = hide;
			}
			if (prev != null && !prev.getCell(0).equals(row.getCell(0)))
				out.printLine();
			out.printLine(line);
			prev = row;
		}
		
		out.flush();
		out.close();
	}
	
	private static class Row {
		CSVLine iLine;
		Row iParent;
		
		public Row(CSVLine line) {
			iLine = line;
		}
		
		public Row getParent() { return iParent; }
		
		public void setParent(Row parent) { iParent = parent; }
		
		public boolean isBlank(int col) {
			return iLine.getFields().size() <= col || iLine.getField(col).toString().isEmpty();
		}
		
		public String getCell(int col) {
			if (isBlank(col)) {
				if (getParent() != null)
					return getParent().getCell(col);
				else
					return "";
			} else {
				return iLine.getField(col).toString();
			}
		}
		
		public int getLevel() {
			return getParent() == null ? 0 : getParent().getLevel() + 1;
		}
		
		public int getLength() {
			return getParent() == null ? iLine.getFields().size() : getParent().getLength();
		}
		
		public int getNrBlanks() {
			for (int i = 0; i < getLength(); i++)
				if (!isBlank(i)) return i;
			return getLength();
		}
		
		public int compareTo(Row b, int col) {
			Row a = this;
			while (a.getLevel() > b.getLevel()) a = a.getParent();
			while (b.getLevel() > a.getLevel()) b = b.getParent();
			try {
				int cmp = Double.valueOf(a.getCell(col) == null ? "0" : a.getCell(col)).compareTo(Double.valueOf(b.getCell(col) == null ? "0" : b.getCell(col)));
				if (cmp != 0) return cmp;
			} catch (NumberFormatException e) {
				int cmp = (a.getCell(col) == null ? "" : a.getCell(col)).compareTo(b.getCell(col) == null ? "" : b.getCell(col));
				if (cmp != 0) return cmp;
			}
			return 0;
		}
		
	}
	
}
