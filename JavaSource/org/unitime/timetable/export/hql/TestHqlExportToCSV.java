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
package org.unitime.timetable.export.hql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.BufferedPrinter;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.IdValue;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

import jakarta.persistence.Tuple;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:hql-test.csv")
public class TestHqlExportToCSV extends SavedHqlExportToCSV {
	private static Log sLog = LogFactory.getLog(SavedHqlExportToCSV.class);
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public String reference() {
		return "hql-test.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		BufferedPrinter out = new BufferedPrinter(new CSVPrinter(helper, false));
		helper.setup(out.getContentType(), reference(), false);
		export(out, helper);
		out.close();
	}
	
	protected void export(BufferedPrinter out, ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		// Check rights
		SessionContext context = new EventContext(helper.getSessionContext(), helper.getAcademicSessionId());
		context.checkPermission(Right.TestHQL);
		
		// Retrive report
		String hql = helper.getParameter("hql");
		if (hql == null || hql.isEmpty()) throw new IllegalArgumentException("No query provided, please set the hql parameter.");
		
		List<IdValue> options = new ArrayList<IdValue>();
		if (helper.getParameter("params") != null) {
			String[] p = helper.getParameter("params").split(":");
			int i = 0;
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
				if (hql.contains("%" + o.name() + "%")) {
					options.add(new IdValue(o.name(), i < p.length ? p[i] : ""));
					i++;
				}
			}
		}
		
		execute(context.getUser(), out, hql, options, 0, -1);
		
		sort(out.getBuffer(), helper);
	}
	
	public static void execute(UserContext user, Printer out, String hql, List<IdValue> options, int fromRow, int maxRows) throws SavedHQLException, PageAccessException {
		try {
			org.hibernate.Session hibSession = SavedHQLDAO.getInstance().getSession();
	        for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (hql.indexOf("%" + o.name() + "%") >= 0) {
					String value = null;
					if (options != null)
						for (IdValue v: options)
							if (o.name().equals(v.getValue())) { value = v.getText(); break; }
					if (value == null || value.isEmpty()) {
						Map<Long, String> vals = o.values(user);
						if (vals == null || vals.isEmpty())
							throw new SavedHQLException(MESSAGES.errorUnableToSetParameterNoValues(o.name()));
						value = "";
						for (Long id: vals.keySet()) {
							if (!value.isEmpty()) value += ",";
							value += id.toString();
							if (!o.allowMultiSelection()) break;
						}
					}
					hql = hql.replace("%" + o.name() + "%", "(" + value + ")");
				}
			}
			if (hql.indexOf("%USER%") >= 0)
				hql = hql.replace("%USER%", "'" + HibernateUtil.escapeSql(user.getExternalUserId()) + "'");
			org.hibernate.query.Query<Tuple> q = hibSession.createQuery(hql, Tuple.class);
			if (maxRows > 0)
				q.setMaxResults(maxRows);
			if (fromRow > 0)
				q.setFirstResult(fromRow);
			q.setCacheable(true);
			int len = -1;
			for (Tuple o: q.list()) {
				if (len < 0) {
					len = length(o);
					String[] line = new String[len];
					header(line, o);
					if (line.length > 0 && line[0].startsWith("__")) out.hideColumn(0);
					out.printHeader(line);
				}
				String[] line = new String[len];
				line(line, o);
				out.printLine(line);
				out.flush();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SavedHQLException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SavedHQLException(MESSAGES.failedExecution(e.getMessage() + (e.getCause() == null ? "" : " (" + e.getCause().getMessage() + ")")));
		}
	}
}
