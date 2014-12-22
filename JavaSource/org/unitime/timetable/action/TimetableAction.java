/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.form.TimetableForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.timegrid.PdfTimetableGridTable;
import org.unitime.timetable.webutil.timegrid.TimetableGridModel;
import org.unitime.timetable.webutil.timegrid.TimetableGridTable;


/** 
 * @author Tomas Muller
 */
@Service("/timetable")
public class TimetableAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		TimetableForm myForm = (TimetableForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.TimetableGrid);
		
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (op==null && request.getParameter("resource")!=null) op="Change";
        
        if ("Change".equals(op) || "Export PDF".equals(op))
        	save(myForm);
        
        load(myForm);
        
        TimetableGridTable table = (TimetableGridTable)request.getSession().getAttribute("Timetable.table");
        
        if ("Show".equals(op)) {
        	table.setFindString(request.getParameter("filter"));
        	if ("i".equals(request.getParameter("mode")))
        		table.setResourceType(TimetableGridModel.sResourceTypeInstructor);
        	if ("r".equals(request.getParameter("mode")))
        		table.setResourceType(TimetableGridModel.sResourceTypeRoom);
        	load(myForm);
        }
        
		myForm.setLoaded(table.reload(request, sessionContext, courseTimetablingSolverService.getSolver()));

        if ("Export PDF".equals(op)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "timetable");
        	PdfTimetableGridTable.export2Pdf(table, out);
        	out.flush(); out.close();
        	return null;
        }

        myForm.setOp("Change");
        return mapping.findForward("showTimetable");
	}
	
	private void load(TimetableForm form) throws Exception {
		TimetableGridTable table = (TimetableGridTable)sessionContext.getAttribute("Timetable.table");
		if (table==null) {
			table = new TimetableGridTable();
			table.load(sessionContext.getUser());
			sessionContext.setAttribute("Timetable.table",table);
		}
		form.setResource(TimetableGridModel.sResourceTypes[table.getResourceType()]);
		form.setDay(TimetableGridTable.sDays[table.getDays()]);
		form.setDayMode(TimetableGridTable.sDayMode[table.getDayMode()]);
		form.setFind(table.getFindString());
		form.setOrderBy(TimetableGridTable.sOrderBy[table.getOrderBy()]);
		form.setDispMode(TimetableGridTable.sDispModes[table.getDispMode()]);
		form.setBgColor(TimetableGridModel.sBgModes[table.getBgMode()]);
		form.setWeeks(table.getWeeks(sessionContext));
		form.setWeek(table.getWeek());
		form.setShowUselessTimes(table.getShowUselessTimes());
		form.setShowInstructors(table.getShowInstructors());
		form.setShowEvents(table.getShowEvents());
		form.setShowComments(table.getShowComments());
		form.setShowTimes(table.getShowTimes());
	}
	
	public void save(TimetableForm form) throws Exception {
		TimetableGridTable table = (TimetableGridTable)sessionContext.getAttribute("Timetable.table");
		if (table==null) {
			table = new TimetableGridTable();
			sessionContext.setAttribute("Timetable.table",table);
		}
		table.setResourceType(form.getResourceInt());
		table.setDays(form.getDayInt());
		table.setDayMode(form.getDayModeInt());
		table.setFindString(form.getFind());
		table.setOrderBy(form.getOrderByInt());
		table.setDispMode(form.getDispModeInt());
		table.setBgMode(form.getBgColorInt());
		table.setShowUselessTimes(form.getShowUselessTimes());
		table.setShowInstructors(form.getShowInstructors());
		table.setShowComments(form.getShowComments());
		table.setShowEvents(form.getShowEvents());
		table.setShowTimes(form.getShowTimes());
		if (form.getWeek() != null)
			table.setWeek(form.getWeek());
		table.save(sessionContext.getUser());
	}

}

