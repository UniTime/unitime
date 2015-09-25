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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.TimetableManagerBuilder;


/**
 * MyEclipse Struts
 * Creation date: 04-11-2005
 *
 * XDoclet definition:
 * @struts:action
 * @struts:action-forward name="success" path="schedDeputyListTile" redirect="true"
 * @struts:action-forward name="fail" path="/error.jsp" redirect="true"
 *
 * @author Tomas Muller
 */
@Service("/timetableManagerList")
public class TimetableManagerListAction extends Action {
	
	@Autowired SessionContext sessionContext;

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods

    /**
     * Reads list of schedule deputies and assistants and displays them in the form of a HTML table
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        String errM = "";
        
        // Check permissions
        sessionContext.checkPermission(Right.TimetableManagers);

		WebTable.setOrder(sessionContext,"timetableManagerList.ord",request.getParameter("order"),1);
		
		boolean showAll = "1".equals(sessionContext.getUser().getProperty("TimetableManagers.showAll", "0"));
		if (request.getParameter("all") != null) {
			showAll = ("true".equalsIgnoreCase(request.getParameter("all")));
			sessionContext.getUser().setProperty("TimetableManagers.showAll", showAll ? "1" : "0");
		}
        
        PdfWebTable table =  new TimetableManagerBuilder().getManagersTable(sessionContext, true, showAll);
        int order = WebTable.getOrder(sessionContext,"timetableManagerList.ord");
        String tblData = (order>=1?table.printTable(order):table.printTable());
        
        if ("Export PDF".equals(request.getParameter("op"))) {
        	ExportUtils.exportPDF(
        			new TimetableManagerBuilder().getManagersTable(sessionContext, false, showAll),
        			order, response, "managers");
        	return null;
        }
            

        request.setAttribute("schedDeputyList", errM + tblData);
        request.setAttribute("showAllManagers", showAll); 
        return mapping.findForward("success");
    }
}
