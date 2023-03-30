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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.BufferedPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.JSONPrinter;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.AccessDeniedException;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:hql-report.json")
public class SavedHqlExportToJSON extends SavedHqlExportToCSV {
	
	@Override
	public String reference() {
		return "hql-report.json";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		// Check rights
		SessionContext context = new EventContext(helper.getSessionContext(), helper.getAcademicSessionId());
		context.checkPermission(helper.getAcademicSessionId(), Right.HQLReports);
		
		// Retrive report
		String report = helper.getParameter("report");
		if (report == null) throw new IllegalArgumentException("No report provided, please set the report parameter.");
		SavedHQL hql = null;
		try {
			hql = SavedHQLDAO.getInstance().get(Long.valueOf(report));
		} catch (NumberFormatException e) {}
		if (hql == null)
			hql = (SavedHQL)SavedHQLDAO.getInstance().getSession().createQuery("from SavedHQL where name = :name").setParameter("name", report, org.hibernate.type.StringType.INSTANCE).setMaxResults(1).uniqueResult();
		if (hql == null) throw new IllegalArgumentException("Report " + report + " does not exist.");
				
		List<SavedHQLInterface.IdValue> params = new ArrayList<SavedHQLInterface.IdValue>();
		if (helper.getParameter("params") != null) {
			String[] p = helper.getParameter("params").split(":");
			int i = 0;
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
				if (hql.getQuery().contains("%" + o.name() + "%")) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(o.name());
					v.setText(i < p.length ? p[i] : "");
					params.add(v);
					i++;
				}
			}
		}
		for (SavedHQL.Option option: SavedHQL.Option.values()) {
			if (!hql.getQuery().contains("%" + option.name() + "%")) continue;
			if (option.allowMultiSelection()) {
				String[] values = helper.getParameterValues(option.name());
				if (values != null && values.length > 0) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(option.name());
					String text = "";
					for (String value: values) {
						Long id = option.lookupValue(context.getUser(), value);
						if (id == null) {
							try {
								id = Long.valueOf(value);
							} catch (NumberFormatException e) {}
						}
						if (id != null)
							text += (text.isEmpty() ? "" : ",") + id;
					}
					v.setText(text);
					params.add(v);
				}
			} else {
				String value = helper.getParameter(option.name());
				if (value != null) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setValue(option.name());
					Long id = option.lookupValue(context.getUser(), value);
					if (id == null) {
						try {
							id = Long.valueOf(value);
						} catch (NumberFormatException e) {}
					}
					v.setText(id == null ? "" : id.toString());
					params.add(v);
				}
			}
		}
		for (SavedHQLParameter p: hql.getParameters()) {
			SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
			v.setValue(p.getName());
			String value = helper.getParameter(p.getName());
			v.setText(value == null ? p.getDefaultValue() : value);
			params.add(v);
		}
		
		boolean hasAppearancePermission = false;
		for (SavedHQL.Flag flag: SavedHQL.Flag.values()) {
			if (hql.isSet(flag)) {
				if (flag.getAppearance() != null && !hasAppearancePermission && (flag.getPermission() == null || context.hasPermission(flag.getPermission())))
					hasAppearancePermission = true;
				if (flag.getAppearance() == null && flag.getPermission() != null)
					context.checkPermission(flag.getPermission());
			}
		}
		if (!hasAppearancePermission) throw new AccessDeniedException();
		
		BufferedPrinter out = new BufferedPrinter(new JSONPrinter(helper.getWriter()));
		helper.setup(out.getContentType(), hql.getName().replace('/', '-').replace('\\', '-').replace(':', '-') + ".json", true);
		
		execute(context.getUser(), out, hql.getQuery(), params, 0, -1, hql.getParameters());
		
		String sort = helper.getParameter("sort");
		if (sort != null && !"0".equals(sort)) {
			final boolean asc = Integer.parseInt(sort) > 0;
			final int col = Math.abs(Integer.parseInt(sort)) - 1;
			Collections.sort(out.getBuffer(), new Comparator<String[]>() {
				int compare(String[] a, String[] b, int col) {
					for (int i = 0; i < a.length; i++) {
						int c = (col + i) % a.length;
						try {
							int cmp = Double.valueOf(a[c] == null ? "0" : a[c]).compareTo(Double.valueOf(b[c] == null ? "0" : b[c]));
							if (cmp != 0) return cmp;
						} catch (NumberFormatException e) {
							int cmp = (a[c] == null ? "" : a[c]).compareTo(b[c] == null ? "" : b[c]);
							if (cmp != 0) return cmp;
						}
					}
					return 0;
				}	
				@Override
				public int compare(String[] a, String[] b) {
					return asc ? compare(a, b, col) : compare(b, a, col);
				}
			});
		}
		
		out.close();
	}
}
