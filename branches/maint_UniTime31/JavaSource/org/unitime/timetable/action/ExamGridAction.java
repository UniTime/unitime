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
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamGridForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.timegrid.PdfExamGridTable;


/** 
 * @author Tomas Muller
 */
public class ExamGridAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamGridForm myForm = (ExamGridForm) form;
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
        
        if ("Cbs".equals(op)) {
            if (request.getParameter("resource")!=null)
                myForm.setResource(Integer.parseInt(request.getParameter("resource")));
            if (request.getParameter("filter")!=null)
                myForm.setFilter(request.getParameter("filter"));
        }
        
        if (RoomAvailability.getInstance()!=null) {
            Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
            Date[] bounds = ExamPeriod.getBounds(session, myForm.getExamType());
            String exclude = (myForm.getExamType()==org.unitime.timetable.model.Exam.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType);
            RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],exclude,false);
            RoomAvailability.setAvailabilityWarning(request, session, myForm.getExamType(), true, false);
        }
        
        PdfExamGridTable table = new PdfExamGridTable(myForm, request.getSession());
        
        request.setAttribute("table", table);

        if ("Export PDF".equals(op)) {
        	File file = ApplicationProperties.getTempFile("timetable", "pdf");
        	table.export(file);
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }

        myForm.setOp("Change");
        return mapping.findForward("showGrid");
	}

}

