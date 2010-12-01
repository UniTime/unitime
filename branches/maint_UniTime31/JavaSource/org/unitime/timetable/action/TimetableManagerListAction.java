/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.util.Constants;
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
 */
public class TimetableManagerListAction extends Action {

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
        if(!Web.hasRole( request.getSession(),
       		 			 new String[] {Roles.ADMIN_ROLE} )) {
		   throw new Exception ("Access Denied.");
		}

		WebTable.setOrder(request.getSession(),"timetableManagerList.ord",request.getParameter("order"),1);
        
        PdfWebTable table =  new TimetableManagerBuilder().getManagersTable(request,false,true);
        int order = WebTable.getOrder(request.getSession(),"timetableManagerList.ord");
        String tblData = (order>=1?table.printTable(order):table.printTable());
        
        if ("Export PDF".equals(request.getParameter("op"))) {
            PdfWebTable pdfTable =  new TimetableManagerBuilder().getManagersTable(request,false,false);
            File file = ApplicationProperties.getTempFile("managers", "pdf");
            pdfTable.exportPdf(file, order);
            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
            

        request.setAttribute("schedDeputyList", errM + tblData);
        return mapping.findForward("success");
    }
}
