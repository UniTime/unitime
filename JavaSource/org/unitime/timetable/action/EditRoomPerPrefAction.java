/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EditRoomPerPrefForm;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao.LocationDAO;

/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomPref" name="editRoomPrefForm" input="/admin/setUpRoomPref.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 */
public class EditRoomPerPrefAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
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
	    
		EditRoomPerPrefForm myForm = (EditRoomPerPrefForm) form;
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		MessageResources rsc = getResources(request);
		String op = myForm.getOp();
		
		//return to room list
		if ("Back".equals(op)) {
			//response.sendRedirect("roomDetail.do?id="+myForm.getUniqueId());
			//return null;
		} else if ("Update".equals(op)) {
		    myForm.save(request);
		} else if (request.getParameter("id")!=null) {
		    Location location = new LocationDAO().get(Long.valueOf(request.getParameter("id")));
		    if (location!=null) {
		        myForm.load(location);
		        return mapping.findForward("edit");
		    }
		}
		
		return mapping.findForward("back");
	}

}

