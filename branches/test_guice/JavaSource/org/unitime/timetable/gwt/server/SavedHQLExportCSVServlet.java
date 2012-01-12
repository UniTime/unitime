/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unitime.timetable.gwt.services.SavedHQLService;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class SavedHQLExportCSVServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/* Inject dependencies */
	@Inject Provider<SavedHQLService> iSavedHQLService;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SavedHQL hql = SavedHQLDAO.getInstance().get(Long.valueOf(request.getParameter("report")));
		if (hql == null) throw new SavedHQLException("No report provided.");
		SavedHQLInterface.Query q = new SavedHQLInterface.Query();
		q.setName(hql.getName());
		q.setId(hql.getUniqueId());
		q.setQuery(hql.getQuery());
		q.setFlags(hql.getType());
		q.setDescription(hql.getDescription());
		List<SavedHQLInterface.IdValue> params = new ArrayList<SavedHQLInterface.IdValue>();
		if (request.getParameter("params") != null) {
			String[] p = request.getParameter("params").split(":");
			int i = 0;
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
				SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
				v.setValue(o.name());
				v.setText(i < p.length ? p[i] : "");
				params.add(v);
				i++;
			}
		}
		List<String[]> report = iSavedHQLService.get().execute(q, params, 0, -1);
		
		response.setContentType("application/csv; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + q.getName() + ".csv\"" );
        
		PrintWriter out = response.getWriter();
		try {
			boolean skipFirstColumn = false;
			for (int i = 0; i < report.size(); i++) {
				String[] line = report.get(i);
				for (int j = 0; j < line.length; j++) {
					String field = (line[j] == null ? "" : line[j]);
					if (i == 0 && j == 0 && field.startsWith("__")) skipFirstColumn = true;
					if (skipFirstColumn && j == 0) continue;
					if (i == 0) field = field.replace('_', ' ').trim();
					out.print((j > (skipFirstColumn ? 1 : 0) ? "," : "") + "\"" + field.replace("\"", "\"\"") + "\"");
				}
				out.println();
			}
			out.flush();
		} finally {
			out.close();
		}
	}

}
