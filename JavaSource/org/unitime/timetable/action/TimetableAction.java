/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.TimetableForm;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.timegrid.PdfTimetableGridTable;
import org.unitime.timetable.webutil.timegrid.TimetableGridModel;
import org.unitime.timetable.webutil.timegrid.TimetableGridTable;


/** 
 * @author Tomas Muller
 */
public class TimetableAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		TimetableForm myForm = (TimetableForm) form;
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
		
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (op==null && request.getParameter("resource")!=null) op="Change";
        
        if ("Change".equals(op) || "Export PDF".equals(op)) {
        	myForm.save(request.getSession());
        }
        
        myForm.load(request.getSession());
        
        TimetableGridTable table = (TimetableGridTable)request.getSession().getAttribute("Timetable.table");
        
        if ("Show".equals(op)) {
        	table.setFindString(request.getParameter("filter"));
        	if ("i".equals(request.getParameter("mode")))
        		table.setResourceType(TimetableGridModel.sResourceTypeInstructor);
        	if ("r".equals(request.getParameter("mode")))
        		table.setResourceType(TimetableGridModel.sResourceTypeRoom);
        	myForm.load(request.getSession());
        }
        
		myForm.setLoaded(table.reload(request));

        if ("Export PDF".equals(op)) {
        	File file = ApplicationProperties.getTempFile("timetable", "pdf");
        	PdfTimetableGridTable.export2Pdf(table, file);
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
       		//response.sendRedirect("temp/"+file.getName());
        }

        myForm.setOp("Change");
        return mapping.findForward("showTimetable");
	}

}

